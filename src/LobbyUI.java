import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LobbyUI {
    private JFrame frame;
    private JTable roomTable;          // 방 목록 테이블
    private JTextArea chatArea;        // 채팅창
    private JTextField chatInput;      // 채팅 입력창
    private JTable userInfoTable;      // 유저 목록 테이블
    private JLabel userNameLabel, userImageLabel;  // 유저 정보 라벨
    private CatchMindClient client;
    private DefaultTableModel userModel;
    private DefaultTableModel roomTableModel;

    public LobbyUI(CatchMindClient client) {
        // 메인 프레임 설정
        this.client = client;
        frame = new JFrame("CatchMind - 로비");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);

        // 배경 패널 생성
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon backgroundImage = new ImageIcon("image/backGround.png"); // 배경 이미지 경로
                g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this); // 패널 크기에 맞게 이미지 그리기
            }
        };
        backgroundPanel.setLayout(new BorderLayout()); // 내부 컴포넌트를 배치하기 위

        // --------------------- 상단 패널: 게임 제목 + 방 만들기 ---------------------
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false); // 패널 투명 설정
        // CatchMind 로고 이미지 추가
        ImageIcon logoIcon = new ImageIcon("image/Logo.png"); // 로고 이미지 경로
        Image resizedLogoImage = logoIcon.getImage().getScaledInstance(200, 80, Image.SCALE_SMOOTH); // 크기 조정
        ImageIcon resizedLogoIcon = new ImageIcon(resizedLogoImage);
        JLabel logoLabel = new JLabel(resizedLogoIcon);

        // 상단 패널의 크기 설정
        topPanel.setPreferredSize(new Dimension(1000, 100)); // 상단 패널 크기
        logoLabel.setPreferredSize(new Dimension(200, 50)); // 로고 라벨 크기
        topPanel.add(logoLabel, BorderLayout.CENTER); // 로고를 상단 중앙에 배치

        // 방 만들기 버튼에 이미지 추가
        ImageIcon buttonIcon = new ImageIcon("image/createRoom.png"); // 버튼 이미지 경로
        Image resizedButtonImage = buttonIcon.getImage().getScaledInstance(100, 30, Image.SCALE_SMOOTH); // 크기 조정
        ImageIcon resizedButtonIcon = new ImageIcon(resizedButtonImage);

        JButton createRoomButton = new JButton(resizedButtonIcon); // 이미지로 버튼 생성
        createRoomButton.setPreferredSize(new Dimension(100, 30)); // 버튼 크기 설정
        createRoomButton.setBorderPainted(false); // 버튼 테두리 제거
        createRoomButton.setFocusPainted(false);  // 포커스 테두리 제거
        createRoomButton.setContentAreaFilled(false); // 버튼 배경 제거
        createRoomButton.addActionListener(this::showCreateRoomDialog);

        // 방 만들기 버튼을 감싸는 패널 생성
        JPanel buttonPanel = new JPanel(new GridBagLayout()); // 버튼을 세로 중앙에 정렬하기 위한 레이아웃
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20)); // 오른쪽 여백 추가 (20px)
        buttonPanel.setOpaque(false); // 배경 투명
        buttonPanel.add(createRoomButton); // 버튼 추가

        topPanel.add(buttonPanel, BorderLayout.EAST); // 버튼 패널을 상단 패널의 오른쪽에 추가

        backgroundPanel.add(topPanel, BorderLayout.NORTH);

        // --------------------- 중앙 패널: 방 목록과 채팅창 ---------------------
        // --------------------- 중앙 패널: 방 목록과 채팅창 ---------------------
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        // 방 목록 테이블
        String[] columnNames = {"방 제목", "방장", "현재 인원", "비밀방", "상태"};
        roomTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 모든 셀을 수정 불가능하게 설정
            }
        };
        roomTable = new JTable(roomTableModel);
        roomTable.setRowHeight(20);
        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // 단일 행 선택 모드 설정
        roomTable.setDefaultEditor(Object.class, null); // 사용자가 셀 편집을 시도하지 못하도록 설정

        // 방 목록에 MouseListener 추가
        roomTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) { // 더블 클릭 감지
                    int selectedRow = roomTable.getSelectedRow();
                    if (selectedRow != -1) { // 유효한 행이 선택된 경우
                        String roomName = (String) roomTableModel.getValueAt(selectedRow, 0); // 방 제목
                        String roomOwner = (String) roomTableModel.getValueAt(selectedRow, 1); // 방장
                        String roomPassword = (String) roomTableModel.getValueAt(selectedRow, 3); // 상태
                        String roomStatus = (String) roomTableModel.getValueAt(selectedRow, 4); // 상태
                        String password = "";
                        if(roomPassword.equals("예")) {
                            password = JOptionPane.showInputDialog(null, "비밀번호를 입력하세요:", "비밀번호 입력", JOptionPane.QUESTION_MESSAGE);
                            if (password != null) {
                                client.sendMessage("JoinRoom " + roomName + " " + password);
                            }
                        }
                        else{
                            client.sendMessage("JoinRoom "+ roomName+ " "  );
                        }

//                        frame.dispose();
//                        new GameRoom(client);
                    }
                }
            }
        });

//        // 방 목록 데이터 추가 (예제)
//        roomTableModel.addRow(new Object[]{"재밌는 게임", "사용자1", "2/4", "아니오", "대기"});
//        roomTableModel.addRow(new Object[]{"긴장감 넘치는 방", "사용자2", "1/4", "예", "대기"});

        // 스크롤 패널로 감싸기
        JScrollPane roomScrollPane = new JScrollPane(roomTable);
        roomScrollPane.setPreferredSize(new Dimension(600, 300)); // 방 목록 높이를 조정

        // 채팅창
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setOpaque(false); // 패널 투명 설정
        chatPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatInput = new JTextField();
        chatInput.addActionListener(e -> sendMessage());
        JButton sendButton = new JButton("입력");
        sendButton.addActionListener(e -> sendMessage());

        JPanel chatInputPanel = new JPanel(new BorderLayout());
        chatInputPanel.add(chatInput, BorderLayout.CENTER);
        chatInputPanel.add(sendButton, BorderLayout.EAST);

        chatPanel.add(chatScrollPane, BorderLayout.CENTER);
        chatPanel.add(chatInputPanel, BorderLayout.SOUTH);

        centerPanel.add(roomScrollPane, BorderLayout.NORTH); // 방 목록 상단
        centerPanel.add(chatPanel, BorderLayout.CENTER);     // 채팅창 아래쪽
        backgroundPanel.add(centerPanel, BorderLayout.CENTER);
        // --------------------- 오른쪽 패널: 유저 정보와 유저 목록 ---------------------
        JPanel rightPanel = new JPanel(new BorderLayout());

        rightPanel.setOpaque(false); // 패널 투명 설정

        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 20));


        // 유저 정보 패널
        JPanel userInfoPanel = new JPanel(new BorderLayout());
        userImageLabel = new JLabel(
                new ImageIcon(new ImageIcon(client.getImagePath())
                        .getImage()
                        .getScaledInstance(100, 100, Image.SCALE_SMOOTH)),
                JLabel.CENTER
        );
        userImageLabel.setPreferredSize(new Dimension(100, 200));
        userNameLabel = new JLabel("닉네임: "+client.getName(), SwingConstants.CENTER);
        userNameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        // 유저 이미지와 닉네임을 묶는 상단 패널
        JPanel userHeaderPanel = new JPanel(new BorderLayout());
        userHeaderPanel.add(userImageLabel, BorderLayout.CENTER);
        userHeaderPanel.add(userNameLabel, BorderLayout.SOUTH);

        // 유저 목록 테이블
        String[] userColumns = {"닉네임"};
        userModel = new DefaultTableModel(userColumns, 0);
        userInfoTable = new JTable(userModel);
        userInfoTable.setRowHeight(20);
        userModel.addRow(new Object[]{"사용자1"});
        userModel.addRow(new Object[]{"사용자2"});
        JScrollPane userScrollPane = new JScrollPane(userInfoTable);
        userScrollPane.setPreferredSize(new Dimension(200, 300));

        rightPanel.add(userHeaderPanel, BorderLayout.NORTH); // 상단에 샘플 이미지와 닉네임
        rightPanel.add(userScrollPane, BorderLayout.CENTER); // 하단에 유저 목록
        backgroundPanel.add(rightPanel, BorderLayout.EAST);

        // **배경 패널을 프레임에 설정**
        frame.setContentPane(backgroundPanel);

        // 모든 버튼에 손 모양 커서를 적용
        UIUtils.applyHandCursorToAll(backgroundPanel);

        frame.setVisible(true);

        client.setLobbyUI(this);

        initLobby(); //처음 로비들어왔을때 정보 받아오기
    }

    // 채팅 메시지 전송 메서드
    private void sendMessage() {
        String message = chatInput.getText().trim();
        if (!message.isEmpty()) {
            client.sendMessage("AllChat "+client.getName()+" "+message);
            chatArea.append("[나] " + message + "\n");
            chatInput.setText("");
        }
    }

    public void updateMessage(String message){

        // ' '를 기준으로 문자열 나누기
        String[] parts = message.split(" ");

        String senderName = parts[1];
        // 첫 번째 인덱스 이후의 요소들을 합치기
        StringBuilder result = new StringBuilder();
        for (int i = 2; i < parts.length; i++) { // 첫 번째 인덱스 제외
            result.append(parts[i]);
            if (i < parts.length - 1) { // 마지막 부분이 아니라면 ':' 추가
                result.append(" ");
            }
        }

        chatArea.append(senderName +": " + result.toString().trim() + "\n");
    }
    //처음 접속했을때 정보 받아오기
    public void initLobby(){
        client.sendMessage("initLobby ");
    }

    //방정보 업데이트
    public void updateRooms(String message){
        roomTableModel.setRowCount(0);
        String[] roomDetails = message.substring("AllRoomInfo ".length()).split("} ");
        for (String roomDetail : roomDetails) {
            roomDetail = roomDetail.replace("{", "").replace("}", ""); // 중괄호 제거
            String[] properties = roomDetail.split(", ");
            roomTableModel.addRow(new Object[]{properties[0], properties[1], properties[2], properties[3], properties[4]});
        }
    }

    //유저정보 업데이트
    public void updateUsers(String[] type){
        //기존 데이터 삭제
        userModel.setRowCount(0);

        for (int i=1; i<type.length; i++) {
            System.out.println(type[i]);
        }
        // 새 데이터 추가
        for (int i=1; i<type.length; i++) {
            userModel.addRow(new Object[]{type[i]});
        }

    }
    //방 생성 다이어로그
    private void showCreateRoomDialog(ActionEvent e) {
        // 다이얼로그 생성
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));

        // 방 이름 입력
        JLabel nameLabel = new JLabel("방 이름:");
        JTextField nameField = new JTextField();

        // 비밀번호 입력
        JLabel passwordLabel = new JLabel("비밀번호:");
        JPasswordField passwordField = new JPasswordField();

        // 라운드 수 입력
        JLabel roundLabel = new JLabel("라운드 수:");
        JTextField roundField = new JTextField();

        // 패널에 컴포넌트 추가
        panel.add(nameLabel);
        panel.add(nameField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(roundLabel);
        panel.add(roundField);

        // 다이얼로그 표시
        int result = JOptionPane.showConfirmDialog(frame, panel, "방 만들기",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String roomName = nameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String roundStr = roundField.getText().trim();

            // 입력 검증
            if (roomName.isEmpty() || roundStr.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "모든 필드를 입력해야 합니다.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int round;
            try {
                round = Integer.parseInt(roundStr); // 라운드 수는 숫자여야 함
                if (round <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "라운드 수는 양의 정수여야 합니다.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 입력된 데이터 처리
            JOptionPane.showMessageDialog(frame, "방 이름: " + roomName + "\n비밀번호: " + password + "\n라운드 수: " + round, "방 생성 완료", JOptionPane.INFORMATION_MESSAGE);
            frame.dispose();
            client.sendMessage("CreateRoom "+ roomName + " " + password + " " + round);
            new GameRoom(client,true);
        }
    }

    public void joinRoom(String message) {
        String[] result = message.split(" ");
        if ("Fail".equals(result[1])) {
            if ("RoomNotFound".equals(result[2])) {
                JOptionPane.showMessageDialog(frame, "방을 찾을 수 없습니다.", "입장 실패", JOptionPane.ERROR_MESSAGE);
            } else if ("WrongPassword".equals(result[2])) {
                JOptionPane.showMessageDialog(frame, "비밀번호가 틀렸습니다.", "입장 실패", JOptionPane.ERROR_MESSAGE);
            } else if ("RoomIsFull".equals(result[2])) {
                JOptionPane.showMessageDialog(frame, "방 인원이 꽉 찼습니다.", "입장 실패", JOptionPane.ERROR_MESSAGE);
            }
        } else if ("Success".equals(result[1])) {
            String roomName = result[2];
            JOptionPane.showMessageDialog(frame, roomName + " 방에 입장했습니다!", "입장 성공", JOptionPane.INFORMATION_MESSAGE);
            frame.dispose();
            new GameRoom(client, false); // 방 화면으로 이동
        }
    }
}

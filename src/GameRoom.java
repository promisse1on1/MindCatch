import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GameRoom {
    private JFrame frame; // 게임 방 프레임
    private JLabel timerLabel; // 게임 타이머
    private JLabel roundLabel; // 라운드 수
    private JLabel currentUserLabel; // 그리는 유저 닉네임
    private JLabel keywordLabel; // 제시어 출력 필드
    private JTextArea chatArea; // 채팅창
    private JTextField chatInput; // 채팅 입력창
    private DrawingPanel drawingPanel; // 그림판 패널
    private CatchMindClient client;
    private ArrayList<JLabel> userImages = new ArrayList<>();
    private ArrayList<JLabel> userNames = new ArrayList<>();
    private ArrayList<JLabel> scoreLabels = new ArrayList<>();
    private JButton startButton;
    private boolean owner;

    public GameRoom(CatchMindClient client, boolean owner) {
        this.client = client;
        this.owner = owner;
        client.setGameRoom(this);
        frame = new JFrame("게임 방 - " + "roomTitle");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1500, 900);

        // 배경 패널 생성
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon backgroundImage = new ImageIcon("image/backGround.png"); // 배경 이미지 경로
                g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };
        backgroundPanel.setLayout(new BorderLayout());
        // ----------------- 상단 패널: 라운드, 타이머, 그리는 유저 -----------------
        JPanel topPanel = new JPanel(new GridLayout(1, 3));
        topPanel.setPreferredSize(new Dimension(frame.getWidth(), 100));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // 상, 좌,
        topPanel.setOpaque(false); // 투명 설정


        roundLabel = new JLabel("라운드: 1/5", SwingConstants.CENTER);
        roundLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        timerLabel = new JLabel("타이머: 00:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        currentUserLabel = new JLabel("그리는 유저: " + "roomOwner", SwingConstants.CENTER);
        currentUserLabel.setFont(new Font("SansSerif", Font.BOLD, 20));

        topPanel.add(roundLabel);
        topPanel.add(timerLabel);
        topPanel.add(currentUserLabel);
        frame.add(topPanel, BorderLayout.NORTH);

        // ----------------- 왼쪽 패널: 색상 선택 및 지우기 버튼 -----------------
        JPanel leftPanel = new JPanel(new GridLayout(8, 1));
        leftPanel.setPreferredSize(new Dimension(150, frame.getHeight()));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10)); // 상, 좌, 하, 우 여백
        leftPanel.setOpaque(false); // 투명 설정

        JButton[] colorButtons = new JButton[6];
        Color[] colors = {Color.BLACK, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.ORANGE};
        String[] imageFiles = {"black.png", "red.png", "green.png", "blue.png", "yellow.png", "orange.png"};

        for (int i = 0; i < 6; i++) {
            JPanel buttonWrapper = new JPanel(new BorderLayout()); // 버튼을 감싸는 패널
            buttonWrapper.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0)); // 상하 5픽셀 여백
            buttonWrapper.setOpaque(false); // 패널을 투명하게 설정

            // 이미지 버튼 설정
            ImageIcon buttonIcon = new ImageIcon(new ImageIcon("image/" + imageFiles[i])
                    .getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)); // 이미지 크기 조정
            colorButtons[i] = new JButton(buttonIcon);
            colorButtons[i].setOpaque(false); // 투명 설정
            colorButtons[i].setContentAreaFilled(false); // 배경 제거
            colorButtons[i].setBorderPainted(false); // 테두리 제거

            final int index = i;
            colorButtons[i].addActionListener(e -> {
                if(drawingPanel.isCanDraw())
                    drawingPanel.setCurrentColor(colors[index]);
            });
            leftPanel.add(colorButtons[i]);
        }

        // 부분 지우기 버튼 이미지 설정
        JPanel partialClearWrapper = new JPanel(new BorderLayout());
        partialClearWrapper.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0)); // 상하 5픽셀 여백
        partialClearWrapper.setOpaque(false);

        ImageIcon eraserIcon = new ImageIcon(new ImageIcon("image/eraser.png")
                .getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)); // 이미지 크기 조정
        JButton partialClearButton = new JButton(eraserIcon);
        partialClearButton.setOpaque(false); // 투명 설정
        partialClearButton.setContentAreaFilled(false); // 배경 제거
        partialClearButton.setBorderPainted(false); // 테두리 제거
        partialClearButton.addActionListener(e -> {
            if(drawingPanel.isCanDraw())
                drawingPanel.enableEraser();
        });
        partialClearWrapper.add(partialClearButton, BorderLayout.CENTER);
        leftPanel.add(partialClearWrapper);



        // 전체 지우기 버튼 이미지 설정
        JPanel fullClearWrapper = new JPanel(new BorderLayout());
        fullClearWrapper.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0)); // 상하 5픽셀 여백
        fullClearWrapper.setOpaque(false);

        ImageIcon basketIcon = new ImageIcon(new ImageIcon("image/basket.png")
                .getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)); // 이미지 크기 조정
        JButton fullClearButton = new JButton(basketIcon);
        fullClearButton.setOpaque(false); // 투명 설정
        fullClearButton.setContentAreaFilled(false); // 배경 제거
        fullClearButton.setBorderPainted(false); // 테두리 제거
        fullClearButton.addActionListener(e -> {
            if(drawingPanel.isCanDraw())
                drawingPanel.clearMyCanvas();
        });

        fullClearWrapper.add(fullClearButton, BorderLayout.CENTER);
        leftPanel.add(fullClearWrapper);

        frame.add(leftPanel, BorderLayout.WEST);


        // ----------------- 가운데 패널: 그림판 -----------------
        drawingPanel = new DrawingPanel(client);

        // 여백을 위한 패널 생성
        JPanel drawingPanelWrapper = new JPanel(new BorderLayout());
        drawingPanelWrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        drawingPanelWrapper.add(drawingPanel, BorderLayout.CENTER);

        frame.add(drawingPanel, BorderLayout.CENTER);

        // ----------------- 오른쪽 패널: 제시어 출력, 채팅창, 나가기 버튼 -----------------
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(300, frame.getHeight()));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        rightPanel.setOpaque(false); // 투명 설정

        // 제시어 출력 패널
        JPanel keywordPanel = new JPanel(new BorderLayout());
        keywordPanel.setBackground(new Color(240, 240, 240)); // 배경 색상 약간의 아이보리
        keywordPanel.setPreferredSize(new Dimension(300, 150)); // 높이 늘리기
        keywordPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        keywordPanel.setOpaque(false); // 투명 설정

        // "<제시어>" 라벨
        JLabel keywordTitleLabel = new JLabel("<제시어>", SwingConstants.CENTER);
        keywordTitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 18)); // 상단 라벨 글씨체
        keywordTitleLabel.setForeground(Color.BLACK);
        keywordTitleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        keywordTitleLabel.setOpaque(false); // 라벨 투명 설정

        // 제시어 텍스트 필드 (내용 출력)
        keywordLabel = new JLabel("오토바이", SwingConstants.CENTER);
        keywordLabel.setFont(new Font("SansSerif", Font.BOLD, 28)); // 굵고 큰 글씨
        keywordLabel.setOpaque(true); // 배경 색상 활성화
        keywordLabel.setBackground(Color.WHITE); // 배경 흰색
        keywordLabel.setForeground(Color.BLACK); // 글씨 색상
        keywordLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));

        // 패널에 컴포넌트 추가
        keywordPanel.add(keywordTitleLabel, BorderLayout.NORTH);
        keywordPanel.add(keywordLabel, BorderLayout.CENTER);
        rightPanel.add(keywordPanel, BorderLayout.NORTH);

        // 채팅 패널
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatInput = new JTextField();
        chatInput.addActionListener(e -> sendMessage());
        JButton sendButton = new JButton("전송");
        sendButton.addActionListener(e -> sendMessage());

        JPanel chatInputPanel = new JPanel(new BorderLayout());
        chatInputPanel.add(chatInput, BorderLayout.CENTER);
        chatInputPanel.add(sendButton, BorderLayout.EAST);

        chatPanel.add(chatScrollPane, BorderLayout.CENTER);
        chatPanel.add(chatInputPanel, BorderLayout.SOUTH);
        rightPanel.add(chatPanel, BorderLayout.CENTER);


        frame.add(rightPanel, BorderLayout.EAST);

        // ----------------- 하단 패널: 유저 이미지, 닉네임, 맞춘 문제 수 -----------------
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setPreferredSize(new Dimension(frame.getWidth(), 150));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        bottomPanel.setOpaque(false); // 하단 패널 자체를 투명화

        // "나가기" 버튼 이미지 설정
        ImageIcon exitIcon = new ImageIcon(new ImageIcon("image/exit.png")
                .getImage().getScaledInstance(60, 80, Image.SCALE_SMOOTH)); // 이미지 크기 조정
        JButton exitButton = new JButton(exitIcon);
        exitButton.setOpaque(false);
        exitButton.setContentAreaFilled(false);
        exitButton.setBorderPainted(false);
        exitButton.addActionListener(e -> exitRoom());

        // 유저 정보 패널 (왼쪽에 배치)
        JPanel userInfoPanel = new JPanel(new GridLayout(1, 4));
        userInfoPanel.setOpaque(false); // 유저 정보 패널 투명화

        for (int i = 1; i <= 4; i++) {
            JPanel userPanel = new JPanel(new BorderLayout());
            userPanel.setOpaque(false);

            JLabel userImage = new JLabel(new ImageIcon(new ImageIcon("image/camel.png")
                    .getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH)), SwingConstants.CENTER);
            JLabel userName = new JLabel("NoUser" + i, SwingConstants.CENTER);
            JLabel scoreLabel = new JLabel("맞춘 문제 수: 0", SwingConstants.CENTER); // 문제 수 추가

            userName.setOpaque(false);
            scoreLabel.setOpaque(false);

            userImages.add(userImage);
            userNames.add(userName);
            scoreLabels.add(scoreLabel);

            userPanel.add(userImage, BorderLayout.CENTER);
            userPanel.add(userName, BorderLayout.NORTH);
            userPanel.add(scoreLabel, BorderLayout.SOUTH);
            userInfoPanel.add(userPanel);
        }

        // "시작하기" 버튼 (오른쪽에 배치)
        ImageIcon startIcon = new ImageIcon(new ImageIcon("image/gamestart.png")
                .getImage().getScaledInstance(100, 80, Image.SCALE_SMOOTH)); // 이미지 크기 조정
        startButton = new JButton(startIcon);
        startButton.setVisible(owner);
        startButton.setEnabled(false);
        startButton.setOpaque(false);
        startButton.setContentAreaFilled(false);
        startButton.setBorderPainted(false);
        startButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, "게임을 시작합니다!", "알림", JOptionPane.INFORMATION_MESSAGE);
            startButton.setEnabled(false);
            drawingPanel.clearMyCanvas();
            client.sendMessage("Start Game");
        });

        // 하단 패널에 유저 정보와 시작 버튼 배치
        bottomPanel.add(exitButton, BorderLayout.WEST);
        bottomPanel.add(userInfoPanel, BorderLayout.CENTER);
        bottomPanel.add(startButton, BorderLayout.EAST);

        frame.add(bottomPanel, BorderLayout.SOUTH);

        backgroundPanel.add(topPanel, BorderLayout.NORTH);
        backgroundPanel.add(leftPanel, BorderLayout.WEST);
        backgroundPanel.add(drawingPanel, BorderLayout.CENTER);
        backgroundPanel.add(rightPanel, BorderLayout.EAST);
        backgroundPanel.add(bottomPanel, BorderLayout.SOUTH);

        frame.setContentPane(backgroundPanel); // 배경 패널 설정

        // 모든 버튼에 손 모양 커서를 적용
        UIUtils.applyHandCursorToAll(backgroundPanel);

        frame.setVisible(true);

        client.sendMessage("Enter Room");
    }

    private void sendMessage() {
        String message = chatInput.getText().trim();
        if (!message.isEmpty()) {
            chatArea.append("[나]: " + message + "\n");
            chatInput.setText("");
            client.sendMessage("GameRoomChat "+client.getName()+": " +message);
        }
    }

    public void updateState(String[] word){
        if (timerLabel != null) {
            timerLabel.setText(word[1]); // 게임 타이머
        }
        if (roundLabel != null) {
            roundLabel.setText(word[5] + "/" + word[4]); // 라운드 수
        }
        if (currentUserLabel != null) {
            currentUserLabel.setText(word[2]); // 그리는 유저 닉네임
        }
        if (keywordLabel != null) {
            keywordLabel.setText(word[3]); // 제시어 출력 필드
        }
    }

    public DrawingPanel getDrawingPanel() {
        return drawingPanel;
    }

    // 유저 이름 변경 메서드
    public void updateUserName(int userIndex, String newName) {
        if (userIndex >= 0 && userIndex < userNames.size()) {
            userNames.get(userIndex).setText(newName);
        }
    }

    // 점수 변경 메서드
    public void updateUserScore(int userIndex, int newScore) {
        if (userIndex >= 0 && userIndex < scoreLabels.size()) {
            scoreLabels.get(userIndex).setText("맞춘 문제 수: " + newScore);
        }
    }

    // 이미지 경로 변경 메서드
    public void updateUserImage(int userIndex, String imagePath) {
        if (userIndex >= 0 && userIndex < userImages.size()) {
            ImageIcon newIcon = new ImageIcon(new ImageIcon(imagePath)
                    .getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            userImages.get(userIndex).setIcon(newIcon);
        }
    }

    public void updateUserInfo(String message) {
        String clientDataString = message.replaceFirst("UserInfo ", "");
        // 데이터를 세미콜론으로 분리
        String[] clients = clientDataString.split(";");

//        int i = 0;
        for(int i=0; i<4; i++){
            if(i<clients.length){
                String[] fields = clients[i].split(",");

                String name = fields[0];
                String imagePath = fields[1];
                int score = Integer.parseInt(fields[2]);
                updateUserName(i,name);
                updateUserScore(i,score);
                updateUserImage(i,imagePath);

                System.out.println("Name: " + name);
                System.out.println("Image Path: " + imagePath);
                System.out.println("Score: " + score);
            }else{
                updateUserName(i,"NoUser");
                updateUserScore(i,0);
                updateUserImage(i,"image/camel.png");
            }
        }
//        for (String clientData : clients) {
//            // 각 클라이언트 정보를 콤마로 분리
//            String[] fields = clientData.split(",");
//
//            String name = fields[0];
//            String imagePath = fields[1];
//            int score = Integer.parseInt(fields[2]);
//            updateUserName(i,name);
//            updateUserScore(i,score);
//            updateUserImage(i,imagePath);
//
//            System.out.println("Name: " + name);
//            System.out.println("Image Path: " + imagePath);
//            System.out.println("Score: " + score);
//
//            i++;
//        }
    }

    public void setStartButton(boolean enable) {
        startButton.setEnabled(enable);
    }

    public void chatFromServer(String message) {
        String messageContent = message.replace("GameRoomChat ","").trim();
        chatArea.append(messageContent + "\n");
    }

    public void getAnswer(String message) {
        String answerUser = message.replace("Answer ","").trim();
        chatArea.append(answerUser+"님이 정답을 맞췄습니다. \n");
    }

    public void endGame(String message) {
        String endMessage = message.replace("GameEnd ","").trim();
        JOptionPane.showMessageDialog(frame, endMessage, "게임 종료", JOptionPane.INFORMATION_MESSAGE);
    }

    public void exitRoom(){
        client.sendMessage("Exit Room");
        frame.dispose();
        new LobbyUI(client);
    }
}

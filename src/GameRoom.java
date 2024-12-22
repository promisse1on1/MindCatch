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
        frame.setLayout(new BorderLayout());

        // ----------------- 상단 패널: 라운드, 타이머, 그리는 유저 -----------------
        JPanel topPanel = new JPanel(new GridLayout(1, 3));
        topPanel.setPreferredSize(new Dimension(frame.getWidth(), 100));

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

        JButton[] colorButtons = new JButton[6];
        Color[] colors = {Color.BLACK, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.ORANGE};
        for (int i = 0; i < 6; i++) {
            colorButtons[i] = new JButton();
            colorButtons[i].setBackground(colors[i]);
            colorButtons[i].setOpaque(true);
            colorButtons[i].setBorderPainted(false);
            final int index = i;
            colorButtons[i].addActionListener(e -> {
                if(drawingPanel.isCanDraw())
                    drawingPanel.setCurrentColor(colors[index]);
            });
            leftPanel.add(colorButtons[i]);
        }

        JButton partialClearButton = new JButton("부분 지우기");
        partialClearButton.addActionListener(e -> {
            if(drawingPanel.isCanDraw())
                drawingPanel.enableEraser();
        });
        JButton fullClearButton = new JButton("전체 지우기");
        fullClearButton.addActionListener(e -> {
            if(drawingPanel.isCanDraw())
                drawingPanel.clearMyCanvas();
        });

        leftPanel.add(partialClearButton);
        leftPanel.add(fullClearButton);
        frame.add(leftPanel, BorderLayout.WEST);

        // ----------------- 가운데 패널: 그림판 -----------------
        drawingPanel = new DrawingPanel(client);
        frame.add(drawingPanel, BorderLayout.CENTER);

        // ----------------- 오른쪽 패널: 제시어 출력, 채팅창, 나가기 버튼 -----------------
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(300, frame.getHeight()));

        // 제시어 출력 패널
        JPanel keywordPanel = new JPanel(new BorderLayout());
        keywordPanel.setBackground(new Color(240, 240, 240)); // 배경 색상 약간의 아이보리
        keywordPanel.setPreferredSize(new Dimension(300, 150)); // 높이 늘리기

        // "<제시어>" 라벨
        JLabel keywordTitleLabel = new JLabel("<제시어>", SwingConstants.CENTER);
        keywordTitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 18)); // 상단 라벨 글씨체
        keywordTitleLabel.setForeground(Color.BLACK);
        keywordTitleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // 여백 추가

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

        JButton exitButton = new JButton("나가기");
        exitButton.addActionListener(e -> exitRoom());
        rightPanel.add(exitButton, BorderLayout.SOUTH);

        frame.add(rightPanel, BorderLayout.EAST);

        // ----------------- 하단 패널: 유저 이미지, 닉네임, 맞춘 문제 수 -----------------
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setPreferredSize(new Dimension(frame.getWidth(), 150));

        // 유저 정보 패널 (왼쪽에 배치)
        JPanel userInfoPanel = new JPanel(new GridLayout(1, 4));
        for (int i = 1; i <= 4; i++) {
            JPanel userPanel = new JPanel(new BorderLayout());
            JLabel userImage = new JLabel(new ImageIcon(new ImageIcon("image/camel.png")
                    .getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH)), SwingConstants.CENTER);
            JLabel userName = new JLabel("유저" + i, SwingConstants.CENTER);
            JLabel scoreLabel = new JLabel("맞춘 문제 수: 0", SwingConstants.CENTER); // 문제 수 추가

            userImages.add(userImage);
            userNames.add(userName);
            scoreLabels.add(scoreLabel);

            userPanel.add(userImage, BorderLayout.CENTER);
            userPanel.add(userName, BorderLayout.NORTH);
            userPanel.add(scoreLabel, BorderLayout.SOUTH);
            userInfoPanel.add(userPanel);
        }

        // "시작하기" 버튼 (오른쪽에 배치)
        startButton = new JButton("시작하기");
        startButton.setVisible(owner);
        startButton.setEnabled(false);
        startButton.setFont(new Font("SansSerif", Font.BOLD, 24));
        startButton.setPreferredSize(new Dimension(200, 50));
        startButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, "게임을 시작합니다!", "알림", JOptionPane.INFORMATION_MESSAGE);
            startButton.setEnabled(false);
            drawingPanel.clearMyCanvas();
            client.sendMessage("Start Game");
        });

        // 하단 패널에 유저 정보와 시작 버튼 배치
        bottomPanel.add(userInfoPanel, BorderLayout.CENTER);
        bottomPanel.add(startButton, BorderLayout.EAST);

        frame.add(bottomPanel, BorderLayout.SOUTH);

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

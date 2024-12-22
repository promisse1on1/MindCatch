import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class StartUI extends JFrame{
    private CatchMindClient client;
        public StartUI(CatchMindClient client) {
            this.client = client;
            setTitle("Catch Mind Login");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(1000, 600);
            setLocationRelativeTo(null);

            // 배경 패널
            JPanel backgroundPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    // 배경을 흰색으로 설정
                    g.setColor(Color.WHITE); // 흰색 설정
                    g.fillRect(0, 0, getWidth(), getHeight()); // 패널 전체를 흰색으로 채움
                }
            };
            backgroundPanel.setLayout(null);

            // 타이틀 라벨 제거 및 이미지로 교체
            ImageIcon titleIcon = new ImageIcon("image/Logo.png"); // 이미지 경로
            Image resizedTitleImage = titleIcon.getImage().getScaledInstance(200, 100, Image.SCALE_SMOOTH); // 크기 조정
            ImageIcon resizedTitleIcon = new ImageIcon(resizedTitleImage); // 조정된 이미지로 새로운 ImageIcon 생성

            JLabel titleLabel = new JLabel(resizedTitleIcon); // 이미지를 라벨로 추가
            titleLabel.setBounds(100, 50, 300, 150); // 위치와 크기 설정
            backgroundPanel.add(titleLabel); // 배경 패널에 추가

            // User Name 입력
            JLabel nameLabel = new JLabel("User Name:");
            nameLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            nameLabel.setBounds(100, 190, 120, 30);
            backgroundPanel.add(nameLabel);

            JTextField nameField = new JTextField();
            nameField.setBounds(250, 190, 150, 30);
            backgroundPanel.add(nameField);

            // IP Address 입력
            JLabel ipLabel = new JLabel("IP Address:");
            ipLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            ipLabel.setBounds(100, 240, 120, 30);
            backgroundPanel.add(ipLabel);

            JTextField ipField = new JTextField("127.0.0.1");
            ipField.setBounds(250, 240, 150, 30);
            backgroundPanel.add(ipField);

            // Port Number 입력
            JLabel portLabel = new JLabel("Port Number:");
            portLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            portLabel.setBounds(100, 290, 120, 30);
            backgroundPanel.add(portLabel);

            JTextField portField = new JTextField("30000");
            portField.setBounds(250, 290, 150, 30);
            backgroundPanel.add(portField);




            JPanel gridPanel = new JPanel();
            gridPanel.setLayout(new GridLayout(2, 2, 10, 10)); // 2x2 그리드 레이아웃
            gridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            gridPanel.setBounds(500,50,450,450);

            String[] imageNames = {"image/camel.png", "image/hippo.png", "image/mammoth.png", "image/shark.png"};
            String[] imageLabels = {"Camel", "hippo", "Mammoth", "Shark"};

            JRadioButton[] radioButtons = new JRadioButton[imageLabels.length];
            ButtonGroup buttonGroup = new ButtonGroup();

            // 이미지와 버튼 추가
            for (int i = 0; i < 4; i++) {
                JPanel itemPanel = new JPanel();
                itemPanel.setLayout(new BorderLayout());
                itemPanel.setBackground(Color.LIGHT_GRAY);

                // 이미지 라벨
                ImageIcon imageIcon = new ImageIcon(imageNames[i]); // 이미지 경로 (image1.png ~ image4.png)
                Image image = imageIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH); // 크기 조절
                JLabel imageLabel = new JLabel(new ImageIcon(image));
                imageLabel.setHorizontalAlignment(JLabel.CENTER);

                // 선택 버튼
                radioButtons[i]  = new JRadioButton(imageLabels[i]);
                radioButtons[i] .setHorizontalAlignment(JRadioButton.CENTER);
                buttonGroup.add(radioButtons[i]);

                // 아이템 패널에 이미지와 버튼 추가
                itemPanel.add(imageLabel, BorderLayout.CENTER);
                itemPanel.add(radioButtons[i] , BorderLayout.SOUTH);

                gridPanel.add(itemPanel); // 그리드 패널에 추가
            }
            backgroundPanel.add(gridPanel);
            // LET'S GO 버튼
            ImageIcon originalIcon = new ImageIcon("image/start.png"); // 원본 이미지 로드
            Image resizedImage = originalIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH); // 버튼 크기에 맞게 이미지 크기 조정
            ImageIcon resizedIcon = new ImageIcon(resizedImage); // 조정된 이미지로 새로운 ImageIcon 생성

            JButton startButton = new JButton(resizedIcon); // 이미지를 버튼에 추가
            startButton.setBounds(160, 330, 150, 150); // 버튼 위치와 크기 설정

            // 버튼 기본 설정 제거
            startButton.setContentAreaFilled(false); // 버튼 배경 제거
            startButton.setBorderPainted(false); // 버튼 테두리 제거
            startButton.setFocusPainted(false); // 버튼 포커스 효과 제거
            startButton.setOpaque(false); // 투명 배경 처리

            backgroundPanel.add(startButton); // 배경 패널에 버튼 추가

            // 버튼 이벤트 (서버에 연결)
            startButton.addActionListener(e -> {
                String userName = nameField.getText();
                String serverIp = ipField.getText();
                int serverPort = Integer.parseInt(portField.getText());
                int selectedIndex = -1; // 선택된 인덱스 초기화

                // ButtonGroup의 라디오 버튼을 순회하면서 인덱스 찾기
                for (int i = 0; i < radioButtons.length; i++) {
                    if (radioButtons[i].isSelected()) {
                        selectedIndex = i;
                        break;
                    }
                }

                try {
                    client.initClient(userName,serverIp, serverPort,imageNames[selectedIndex]);
                    client.sendMessage(userName+" "+imageNames[selectedIndex]); // 서버에 사용자 이름 전송
                    //client.receiveMessages(); // 서버 메시지 수신
                    JOptionPane.showMessageDialog(this, "Connected to Server!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    // 연결 성공 시 StartUI 종료하고 LobbyUI 실행
                    dispose(); // StartUI 종료
                    new LobbyUI(client); // 새로운 로비 화면 생성
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Failed to connect: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            add(backgroundPanel);

            // 모든 버튼에 손 모양 커서를 적용
            UIUtils.applyHandCursorToAll(backgroundPanel);

            setVisible(true);


        }

}

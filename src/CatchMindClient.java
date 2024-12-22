import java.io.*;
import java.net.Socket;

public class CatchMindClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String name;
    private String imagePath;
    private LobbyUI lobbyUI;
    private GameRoom gameRoom;

//    public CatchMindClient(String name, String serverIp, int serverPort, String imagePath) throws IOException {
//        socket = new Socket(serverIp, serverPort);
//        out = new PrintWriter(socket.getOutputStream(), true);
//        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//        this.name = name;
//        this.imagePath = imagePath;
//        System.out.println("Connected to server: " + serverIp + ":" + serverPort);
//    }

    public void initClient(String name, String serverIp, int serverPort, String imagePath) throws IOException {
        this.socket = new Socket(serverIp, serverPort);
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.name = name;
        this.imagePath = imagePath;
        System.out.println("Connected to server: " + serverIp + ":" + serverPort);

        receiveMessages();
    }

    // 서버로 메시지 전송
    public void sendMessage(String message) {
        out.println(message);
    }

    public String getName() {
        return name;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setLobbyUI(LobbyUI lobbyUI) {
        this.lobbyUI = lobbyUI;
    }

    public void setGameRoom(GameRoom gameRoom) {
        this.gameRoom = gameRoom;
    }

    // 서버로부터 메시지 수신 (별도 스레드에서 실행)
    public void receiveMessages() {
        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Server: " + message);
                    excute(message);
                }
            } catch (IOException e) {
                System.out.println("Disconnected from server.");
            }
        }).start();
    }

    //서버로 부터 메시지 받았을때 작동되는 행동들
    private void excute(String message) {
        String[] type = message.split(" ");
        switch (type[0]) {
            case "clientNames":
                lobbyUI.updateUsers(type);
                break;
            case "AllChat":
                lobbyUI.updateMessage(message);
                break;
            case "GameStatus":
                if(gameRoom != null) {
                    gameRoom.updateState(type);
                }
                break;
            case "AllRoomInfo":
                lobbyUI.updateRooms(message);
                break;
            case "JoinRoomResult":
                lobbyUI.joinRoom(message);
                break;
            case "Draw":
                gameRoom.getDrawingPanel().drawFromServer(message);
                break;
            case "Color":
                gameRoom.getDrawingPanel().ColorFromServer(message);
                break;
            case "Eraser":
                gameRoom.getDrawingPanel().isEraserFromServer();
                break;
            case "Clear":
                gameRoom.getDrawingPanel().clearCanvas();
                break;
            case "UserInfo":
                gameRoom.updateUserInfo(message);
                break;
            case "StartButton":
                gameRoom.setStartButton(true);
                break;
            case "GameRoomChat":
                gameRoom.chatFromServer(message);
                break;
            case "Answer":
                gameRoom.getAnswer(message);
                break;
            case "Can":
                if(type[1].equals("true"))
                    gameRoom.getDrawingPanel().setCanDraw(true);
                else gameRoom.getDrawingPanel().setCanDraw(false);
                break;
            case "GameEnd":
                gameRoom.endGame(message);
                gameRoom.setStartButton(true);
                break;
        }
    }


    // 연결 종료
    public void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

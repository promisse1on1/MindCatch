import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// 클라이언트 핸들링 클래스
class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientName;
    private String roomName;
    private String imagePath;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // 입출력 스트림 초기화
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // 클라이언트로부터 이름 수신
//            out.println("Enter your name:");
            String userInfo = in.readLine();
            clientName = userInfo.split(" ")[0];
            imagePath = userInfo.split(" ")[1];

            System.out.println(clientName + " has joined the game. imagePath = "+imagePath);
            CatchMindServer.broadcast(clientName + " has joined the game.", this);

            String message;
            // 클라이언트로부터 메시지 수신
            while ((message = in.readLine()) != null) {
                System.out.println(clientName + ": " + message);
                CatchMindServer.broadcast( message, this);
            }
        } catch (IOException e) {
            System.out.println(clientName + " disconnected.");
        } finally {
            // 클라이언트 종료 처리
            CatchMindServer.removeClient(this);
            closeConnection();
        }
    }

    // 메시지 전송 메서드
    public void sendMessage(String message) {
        out.println(message);
    }

    public String getClientName() {
        return clientName;
    }

    // 연결 종료
    private void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getImagePath() {
        return imagePath;
    }
}

import java.io.*;
import java.net.*;
import java.util.*;

public class CatchMindServer {
    private static final int PORT = 30000; // 서버 포트
    private static List<ClientHandler> clients = new ArrayList<>(); // 연결된 클라이언트 리스트
    private static Map<String, RoomHandler> rooms = new LinkedHashMap<>();

    public static void main(String[] args) {
        System.out.println("Catch Mind Server is starting...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);

            while (true) {
                // 클라이언트 연결 대기
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                // 새 클라이언트 핸들러 생성 및 시작
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 모든 클라이언트에게 메시지 전송
    public static void broadcast(String message, ClientHandler sender) {
        String[] type = message.split(" ");
        switch (type[0]) {
            case "initLobby":
                snedUserName(sender);
                sendAllRoomInfo();
                break;
            case "AllChat":
                sendMessage(message, sender);
                break;
            case "CreateRoom":
                createRoom(message, sender);
                sendAllRoomInfo();
                break;
            case "JoinRoom":
                String password = type.length > 2 ? type[2] : ""; // 클라이언트에서 전달된 비밀번호
                joinRoom(type[1], password, sender);
                sendAllRoomInfo();
                break;
            case "Draw":
                draw(message, sender);
                break;
            case "Color":
                draw(message, sender);
                break;
            case "Eraser":
                draw(message, sender);
                break;
            case "Clear":
                draw(message, sender);
                break;
            case "Enter":
                userInfo(sender);
                sendAllRoomInfo();
                break;
            case "Start":
                startGame(sender);
                sendAllRoomInfo();
                break;
            case "GameRoomChat":
                gameRoomChat(message,sender);
                break;
            case "Exit":
                exitRoom(sender);
                sendAllRoomInfo();

        }

//        for (ClientHandler client : clients) {
//            if (client != sender) { // 메시지를 보낸 클라이언트는 제외
//                client.sendMessage(message);
//            }
//        }
    }

    public static void userInfo(ClientHandler sender) {
        StringBuilder clientsData = new StringBuilder();

        RoomHandler roomHandler = rooms.get(sender.getRoomName());
        Map<ClientHandler, Integer> clinetWithScore = roomHandler.getClinetWithScore();
        List<ClientHandler> clientHandlers = roomHandler.getParticipants();
        for (ClientHandler client : clientHandlers) {
            String name = client.getClientName();
            String imagePath = client.getImagePath();
            int score = clinetWithScore.get(client);
            // 클라이언트 데이터 추가 (구분자: , )
            clientsData.append(name)
                    .append(",")
                    .append(imagePath)
                    .append(",")
                    .append(score)
                    .append(";");
        }
        // 문자열 끝에 불필요한 세미콜론 제거
        if (clientsData.length() > 0) {
            clientsData.setLength(clientsData.length() - 1);
        }
        for (ClientHandler client : clientHandlers) {
            client.sendMessage("UserInfo "+clientsData.toString());
        }
    }

    private static void draw(String message, ClientHandler sender) {
        RoomHandler roomHandler = rooms.get(sender.getRoomName());
        List<ClientHandler> clientHandlers = roomHandler.getParticipants();
        for (ClientHandler client : clientHandlers) {
            if (client != sender) { // 메시지를 보낸 클라이언트는 제외
                client.sendMessage(message);
            }
        }
    }

    private static void gameRoomChat(String message, ClientHandler sender){
        draw(message, sender);
        RoomHandler roomHandler = rooms.get(sender.getRoomName());
        if(roomHandler.getisStart()){
            String word = message.split(":")[1].trim();
            if(sender != roomHandler.getPainterClient()  && word.equals(roomHandler.getCurrentWord())){  //출제자가 아니고 정답일때
                List<ClientHandler> clientHandlers = roomHandler.getParticipants();
                for (ClientHandler client : clientHandlers) {
                        client.sendMessage("Answer "+ sender.getClientName());
                }
                int score = roomHandler.getClinetWithScore().get(sender);
                roomHandler.getClinetWithScore().put(sender,++score);
                userInfo(sender);
                roomHandler.nextRound();
            }
        }
    }

    private static void sendMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) { // 메시지를 보낸 클라이언트는 제외
                client.sendMessage(message);
            }
        }
    }

    private static void sendMessageAll(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public static void snedUserName(ClientHandler sender) {
        String usersName = "clientNames ";
        for (ClientHandler client : clients) {
            usersName += client.getClientName();
            usersName += " ";
        }
        for (ClientHandler client : clients) {
            client.sendMessage(usersName);
        }
//        sender.sendMessage(usersName);
    }

    public static void createRoom(String message, ClientHandler owner) {
        // 방 생성
        String[] words = message.split(" ");
        String roomName = words[1];
        String password = words[2];
        int maxRound = Integer.parseInt(words[3]);
        RoomHandler roomHandler = new RoomHandler(roomName, owner, password, maxRound);
        rooms.put(roomName, roomHandler);
        owner.setRoomName(roomName);
    }

    public static void joinRoom(String roomName, String password, ClientHandler joiner) {
        //방 참가
        RoomHandler room = rooms.get(roomName);
        if (room == null) {
            joiner.sendMessage("JoinRoomResult Fail RoomNotFound");
            return;
        }

        //인원 다 차있으면
        if(room.getClinetWithScore().size() >= 4){
            joiner.sendMessage("JoinRoomResult Fail RoomIsFull");
            return;
        }

        // 비밀번호 확인
        if (room.getPassword() != null && !room.getPassword().isEmpty()) {
            if (!room.getPassword().equals(password)) {
                joiner.sendMessage("JoinRoomResult Fail WrongPassword");
                return;
            }
        }

        // 비밀번호가 맞으면 방에 추가
        room.addParticipant(joiner);
        joiner.sendMessage("JoinRoomResult Success " + roomName);
        joiner.setRoomName(roomName);

        int joinerCount = room.getParticipants().size();
        if(joinerCount >= 2 && !room.getisStart()){
            room.getRoomOwner().sendMessage("StartButton enable");
        }
    }

    public static void exitRoom(ClientHandler exiter) {
        //방 나가기
        RoomHandler roomHandler = rooms.get(exiter.getRoomName());
        roomHandler.removeParticipant(exiter);
        if(roomHandler.getClinetWithScore().isEmpty()) rooms.remove(exiter.getRoomName());
        exiter.setRoomName(null);
        userInfo(roomHandler.getRoomOwner());
    }

    public static void startGame(ClientHandler owner) {
        RoomHandler roomHandler = rooms.get(owner.getRoomName());
        roomHandler.setStart(true);

    }

    // 클라이언트 종료 시 리스트에서 제거
    public static void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        System.out.println("Client disconnected: " + clientHandler.getClientName());
    }

    public static void sendAllRoomInfo() {
        if (rooms.isEmpty()) return;
        StringBuilder allRoomInfo = new StringBuilder();
        allRoomInfo.append("AllRoomInfo ");

        for (RoomHandler room : rooms.values()) { // 모든 RoomHandler에 대해
            allRoomInfo.append("{")
                    .append(room.getRoomName()).append(", ")
                    .append(room.getRoomOwner().getClientName()).append(", ")
                    .append(room.getClinetWithScore().size()).append("/4").append(", ")
                    .append(room.getPassword() != null && !room.getPassword().isEmpty() ? "예" : "아니오").append(", ")
                    .append(room.getisStart() ? "진행중" : "대기")
                    .append("} ");
        }

        String RoomInfos = allRoomInfo.toString().trim(); // 마지막 공백 제거

        sendMessageAll(RoomInfos);
    }
}


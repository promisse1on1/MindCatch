import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RoomHandler {
    private String roomName;
    private ClientHandler roomOwner;
    private ClientHandler painterClient;
    private Map<ClientHandler, Integer> clinetWithScore = new LinkedHashMap<>();//사용자와 점수
    private List<String> Word = new ArrayList<>(Arrays.asList("apple", "banana", "cherry"));
    private int curruntWordIndex = 0;
    private int timer = 30;
    private String password;
    private String painter;
    private int maxRound;
    private int currentRound;
    private boolean isStart = false;



    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); // 타이머 스케줄러


    public RoomHandler(String roomName, ClientHandler roomOwner, String password, int maxRound) {
        this.roomName = roomName;
        this.roomOwner = roomOwner;
        this.password = password;
        this.maxRound = maxRound;
        this.painter = roomOwner.getClientName();
        this.painterClient = roomOwner;
        clinetWithScore.put(roomOwner, 0);

        startGameTimer();
        System.out.println("룸 핸들러 생김");
    }

    public String getRoomName() {
        return roomName;
    }

    public ClientHandler getRoomOwner() {
        return roomOwner;
    }

    public List<ClientHandler> getParticipants() {
        Set<ClientHandler> keys = clinetWithScore.keySet();
        List<ClientHandler> userNames = new ArrayList<>(keys);
        return userNames;
    }

    public void addParticipant(ClientHandler user) {
        clinetWithScore.put(user, 0);
    }

    public void removeParticipant(ClientHandler user) {
        clinetWithScore.remove(user);
    }

    public boolean isEmpty() {
        return clinetWithScore.isEmpty();
    }

    public void startGameTimer() {
        scheduler.scheduleAtFixedRate(() -> {
            // 1초마다 실행되는 코드
            if (isStart) {
                timer--;
            }

            if (timer <= 0) {
                nextRound(); // 라운드 전환
            }

            // 모든 클라이언트에게 상태 전송
            sendGameStatus();
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void nextRound() {
        timer = 30; // 타이머 초기화
        currentRound++; // 라운드 증가

        for (ClientHandler client : clinetWithScore.keySet()) {
                client.sendMessage("Clear NextRound");
        }
        // 라운드 종료 체크
        if (currentRound >= maxRound) {
            endGame();
            currentRound = 0;
            curruntWordIndex = (curruntWordIndex + 1) % Word.size(); // 다음 단어로 전환
            painter = getNextPainter(); // 새로운 그림 그리는 사람 설정
            painterClient = getNextPainterClient();
        } else {
            curruntWordIndex = (curruntWordIndex + 1) % Word.size(); // 다음 단어로 전환
            painter = getNextPainter(); // 새로운 그림 그리는 사람 설정
            painterClient = getNextPainterClient();
            sendCanDraw();
        }
    }

    public void sendGameStatus() {
        String playerMessage =
                "GameStatus " +
                        timer + " " +
                        painter + " " +
                        "맞혀보세요" + " " +
                        maxRound + " " +
                        currentRound;

        String ownerMessage =
                "GameStatus " +
                        timer + " " +
                        painter + " " +
                        getCurrentWord() + " " +
                        maxRound + " " +
                        currentRound;

        for (ClientHandler client : clinetWithScore.keySet()) {
            if(client == painterClient) {
                client.sendMessage(ownerMessage);

            }else{
            client.sendMessage(playerMessage); // 각 클라이언트로 메시지 전송
                }
        }
    }

    public void endGame() {
        isStart = false;
        List<ClientHandler> winer = new ArrayList<>();
        String winerNames = "";
        // 값 중 최댓값 찾기
        int maxScore = Collections.max(clinetWithScore.values());
        // 최댓값에 해당하는 사용자 찾기
        for (Map.Entry<ClientHandler, Integer> entry : clinetWithScore.entrySet()) {
            if (entry.getValue() == maxScore) {
                System.out.println("최고 점수 사용자: " + entry.getKey().getClientName());
                System.out.println("점수: " + entry.getValue());
                winer.add(entry.getKey());
            }
        }
        for(ClientHandler c : winer){
            winerNames += (c.getClientName()+" 님 ");
        }
        winerNames += "이 승리하셨습니다.";
        for (ClientHandler client : clinetWithScore.keySet()) {
            client.sendMessage("GameEnd " + winerNames);
            clinetWithScore.put(client, 0);
        }
        CatchMindServer.userInfo(roomOwner);
        CatchMindServer.sendAllRoomInfo();
    }

    public String getNextPainter() {
        List<ClientHandler> participants = new ArrayList<>(clinetWithScore.keySet());
        int nextPainterIndex = currentRound % participants.size();
        return participants.get(nextPainterIndex).getClientName(); // 다음 그림 그리는 사람
    }

    public ClientHandler getNextPainterClient() {
        List<ClientHandler> participants = new ArrayList<>(clinetWithScore.keySet());
        int nextPainterIndex = currentRound % participants.size();
        return participants.get(nextPainterIndex); // 다음 그림 그리는 사람
    }

    public String getCurrentWord() {
        return Word.get(curruntWordIndex);
    }


    public String getPassword() {
        return password;
    }

    public int getTimer() {
        return timer;
    }

    public Map<ClientHandler, Integer> getClinetWithScore() {
        return clinetWithScore;
    }

    public List<String> getWord() {
        return Word;
    }

    public String getPainter() {
        return painter;
    }

    public int getMaxRound() {
        return maxRound;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void setRoomOwner(ClientHandler roomOwner) {
        this.roomOwner = roomOwner;
    }

    public void setClinetWithScore(Map<ClientHandler, Integer> clinetWithScore) {
        this.clinetWithScore = clinetWithScore;
    }

    public void setWord(List<String> word) {
        Word = word;
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPainter(String painter) {
        this.painter = painter;
    }

    public void setMaxRound(int maxRound) {
        this.maxRound = maxRound;
    }

    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }

    public boolean getisStart() {
        return isStart;
    }

    public void setStart(boolean start) {
        isStart = start;
        sendCanDraw();
    }

    public ClientHandler getPainterClient() {
        return painterClient;
    }

    public void sendCanDraw(){
        List<ClientHandler> clientHandlers = getParticipants();
        for (ClientHandler client : clientHandlers) {
            if (client != getPainterClient()) { // 메시지를 보낸 클라이언트는 제외
                client.sendMessage("Can false");
            }
            else client.sendMessage("Can true");
        }
    }

    public void initGame(){

    }
}

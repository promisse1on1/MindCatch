import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        CatchMindClient client = new CatchMindClient();
        StartUI startUI = new StartUI(client);
    }
}
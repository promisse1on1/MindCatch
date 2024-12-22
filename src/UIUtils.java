import javax.swing.*;
import java.awt.*;

public class UIUtils {
    // 재귀적으로 모든 버튼에 손 모양 커서를 적용하는 메서드
    public static void applyHandCursorToAll(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JButton) {
                component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else if (component instanceof Container) {
                applyHandCursorToAll((Container) component); // 재귀 호출
            }
        }
    }
}

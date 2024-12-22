import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class DrawingPanel extends JPanel {
    private BufferedImage canvas; // 그림을 그릴 캔버스
    private Graphics2D g2d; // 그래픽스 객체
    private Color currentColor = Color.BLACK; // 현재 색상
    private boolean isEraser = false; // 지우기 모드 여부
    private CatchMindClient client;
    private int startX, startY, endX, endY;
    private boolean canDraw;

    public DrawingPanel(CatchMindClient client) {
        this.client = client;
        this.canDraw = false;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(600, 400));
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        // 마우스 이벤트 추가
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(!canDraw) return;
                startX = e.getX();
                startY = e.getY();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if(!canDraw) return;
                if (g2d == null) {
                    initializeCanvas();
                }

                endX = e.getX();
                endY = e.getY();

                // 지우기 모드일 경우
                if (isEraser) {
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(20)); // 지우기 크기
                } else {
                    g2d.setColor(currentColor);
                    g2d.setStroke(new BasicStroke(2)); // 기본 선 크기
                }

                String message = "Draw " + startX + "," + startY + "," + endX + "," + endY;
                client.sendMessage(message);

                // 선 그리기
                g2d.drawLine(startX, startY, endX, endY);
                startX = endX;
                startY = endY;

                repaint();
            }
        });
    }

    private void initializeCanvas() {
        canvas = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        g2d = canvas.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(currentColor);
        g2d.setStroke(new BasicStroke(2));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (canvas == null) {
            initializeCanvas();
        }
        g.drawImage(canvas, 0, 0, null);
    }

    public void clearMyCanvas() {
        clearCanvas();

        String clearMessage = "Clear Canvas";
        client.sendMessage(clearMessage);
    }
    public void clearCanvas() {
        if (g2d != null) {
            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setComposite(AlphaComposite.SrcOver);
            repaint();

        }
    }

    public void setCurrentColor(Color color) {
        this.currentColor = color;
        this.isEraser = false; // 색상 변경 시 지우기 모드 해제

        String colorMessage = "Color " + color.getRGB();
        client.sendMessage(colorMessage);
    }

    public void enableEraser() {
        this.isEraser = true; // 지우기 모드 활성화
        String eraserMessage = "Eraser true";
        client.sendMessage(eraserMessage);
    }

    public void drawFromServer(String message) {
        String result = message.replaceFirst("Draw", "").trim();
        String[] parts = result.split(",");
        int startX = Integer.parseInt(parts[0]);
        int startY = Integer.parseInt(parts[1]);
        int endX = Integer.parseInt(parts[2]);
        int endY = Integer.parseInt(parts[3]);

        Graphics g = getGraphics();
        if (g2d == null) {
            initializeCanvas();
        }

        if (isEraser) {
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(20)); // 지우기 크기
        } else {
            g2d.setColor(currentColor);
            g2d.setStroke(new BasicStroke(2)); // 기본 선 크기
        }

        g2d.drawLine(startX, startY, endX, endY);

        repaint();
    }

    public void ColorFromServer(String color){
        String colorValue = color.replaceFirst("Color", "").trim();
        int rgb = Integer.parseInt(colorValue);
        Color newColor = new Color(rgb);

        this.currentColor = newColor;
        this.isEraser = false; // 색상 변경 시 지우기 모드 해제
        // 색상 변경 적용
    }

    public void isEraserFromServer(){
        this.isEraser = true;
    }

    public void setCanDraw(boolean canDraw) {
        this.canDraw = canDraw;
    }

    public boolean isCanDraw() {
        return canDraw;
    }
}

package personalfinancemanager.view.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Lớp JButton tùy chỉnh với bo góc và hiệu ứng di chuột (Hover).
 */
public class RoundedButton extends JButton {
    private int cornerRadius = 15;
    private Color normalColor;
    private Color hoverColor;
    private Color activeColor;

    public RoundedButton(String text) {
        super(text);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Thiết lập màu sắc mặc định
        normalColor = new Color(41, 128, 185); // Accents Blue
        hoverColor = normalColor.brighter();
        activeColor = normalColor.darker();
        
        setBackground(normalColor);
        setForeground(Color.WHITE);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(hoverColor);
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(normalColor);
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                setBackground(activeColor);
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (getBounds().contains(e.getPoint())) {
                    setBackground(hoverColor);
                } else {
                    setBackground(normalColor);
                }
                repaint();
            }
        });
    }

    public RoundedButton(String text, Color baseColor) {
        this(text);
        setColors(baseColor);
    }

    /**
     * Thay đổi màu sắc nút và tự động tạo màu hover/active.
     */
    public void setColors(Color baseColor) {
        this.normalColor = baseColor;
        this.hoverColor = new Color(
            Math.min(baseColor.getRed() + 20, 255),
            Math.min(baseColor.getGreen() + 20, 255),
            Math.min(baseColor.getBlue() + 20, 255)
        );
        this.activeColor = baseColor.darker();
        setBackground(normalColor);
        repaint();
    }

    public int getCornerRadius() {
        return cornerRadius;
    }

    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Vẽ màu nền bo góc
        g2.setColor(getBackground());
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));

        g2.dispose();
        
        // Vẽ chữ của nút
        super.paintComponent(g);
    }
}

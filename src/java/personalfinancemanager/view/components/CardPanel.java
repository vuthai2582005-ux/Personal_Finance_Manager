package personalfinancemanager.view.components;

import personalfinancemanager.config.AppConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Thẻ (Card) hiển thị số liệu thống kê tổng quan (Thu, Chi, Số dư).
 */
public class CardPanel extends JPanel {
    private final JLabel lblTitle;
    private final JLabel lblValue;
    private final JLabel lblIcon;
    private int cornerRadius = 20;

    public CardPanel(String title, String initialValue, String iconText, Color bgCol, Color textCol) {
        setOpaque(false);
        setBackground(bgCol);
        setLayout(new BorderLayout(15, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Panel chứa thông tin chữ bên trái
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new GridLayout(2, 1, 5, 5));

        lblTitle = new JLabel(title);
        lblTitle.setFont(AppConstants.FONT_SMALL);
        lblTitle.setForeground(textCol);
        textPanel.add(lblTitle);

        lblValue = new JLabel(initialValue);
        lblValue.setFont(new Font(AppConstants.FONT_FAMILY, Font.BOLD, 22));
        lblValue.setForeground(textCol);
        textPanel.add(lblValue);

        add(textPanel, BorderLayout.CENTER);

        // Label chứa ký tự/icon tượng trưng bên phải
        lblIcon = new JLabel(iconText);
        lblIcon.setFont(new Font(AppConstants.FONT_FAMILY, Font.PLAIN, 36));
        lblIcon.setForeground(textCol);
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        add(lblIcon, BorderLayout.EAST);
    }

    public void setValue(String val) {
        lblValue.setText(val);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Vẽ nền thẻ bo góc
        g2.setColor(getBackground());
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));

        // Vẽ viền nhẹ cho nổi bật
        g2.setColor(new Color(0, 0, 0, 15));
        g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius));

        g2.dispose();
    }
}

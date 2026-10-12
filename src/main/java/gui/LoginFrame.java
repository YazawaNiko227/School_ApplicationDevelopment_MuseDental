/*
    Người làm phần này: Phan Đức Toàn
    MSSV: 24741631
*/

package gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton; 
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JToggleButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;

public final class LoginFrame extends JFrame {
    // Bảng màu dùng chung để giữ giao diện nhất quán giữa các khu vực.
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color PINK = new Color(239, 103, 151);
    private static final Color LINE = new Color(222, 232, 235);

    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JCheckBox rememberCheckBox = new JCheckBox("Ghi nhớ đăng nhập");

    public LoginFrame() {
        super("Nha khoa Muse | Đăng nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 620));
        setSize(1120, 720);
        setLocationRelativeTo(null);
        setContentPane(createContent());
        getRootPane().setDefaultButton(createLoginButtonReference);
    }

    private JButton createLoginButtonReference;

    private JPanel createContent() {
        // Chia thẻ đăng nhập thành phần giới thiệu và phần nhập thông tin.
        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(new Color(243, 248, 249));
        background.setBorder(new EmptyBorder(28, 34, 28, 34));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(232, 239, 241)));

        GridBagConstraints cardConstraints = new GridBagConstraints();
        cardConstraints.gridy = 0;
        cardConstraints.weighty = 1;
        cardConstraints.fill = GridBagConstraints.BOTH;
        cardConstraints.gridx = 0;
        cardConstraints.weightx = 1.05;
        card.add(createWelcomePanel(), cardConstraints);
        cardConstraints.gridx = 1;
        cardConstraints.weightx = 0.95;
        card.add(createLoginPanel(), cardConstraints);

        GridBagConstraints backgroundConstraints = new GridBagConstraints();
        backgroundConstraints.fill = GridBagConstraints.BOTH;
        backgroundConstraints.weightx = 1;
        backgroundConstraints.weighty = 1;
        background.add(card, backgroundConstraints);
        return background;
    }

    private JPanel createWelcomePanel() {
        // Khu vực nhận diện thương hiệu với nền chuyển sắc được vẽ riêng.
        JPanel panel = new GradientPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(new EmptyBorder(44, 48, 40, 44));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        JPanel brand = new JPanel(new BorderLayout(12, 0));
        brand.setOpaque(false);
        brand.add(new ToothMark(), BorderLayout.WEST);
        JPanel brandText = new JPanel(new BorderLayout());
        brandText.setOpaque(false);
        JLabel name = new JLabel("Nha khoa Muse");
        name.setFont(new Font("Arial", Font.BOLD, 21));
        name.setForeground(Color.WHITE);
        //JLabel tagline = new JLabel("DENTAL CLINIC");
        //tagline.setFont(new Font("Arial", Font.BOLD, 9));
        //tagline.setForeground(new Color(207, 234, 235));
        brandText.add(name, BorderLayout.NORTH);
        //brandText.add(tagline, BorderLayout.SOUTH);
        brand.add(brandText, BorderLayout.CENTER);
        c.gridy = 0;
        panel.add(brand, c);

        JLabel eyebrow = new JLabel("CHĂM SÓC NỤ CƯỜI CỦA BẠN");
        eyebrow.setFont(new Font("Arial", Font.BOLD, 11));
        eyebrow.setForeground(new Color(171, 231, 226));
        c.gridy = 1;
        c.insets = new Insets(92, 0, 14, 0);
        panel.add(eyebrow, c);

        JLabel heading = new JLabel("<html>Khởi đầu cho<br>nụ cười khỏe đẹp.</html>");
        heading.setFont(new Font("Arial", Font.BOLD, 38));
        heading.setForeground(Color.WHITE);
        c.gridy = 2;
        c.insets = new Insets(0, 0, 18, 0);
        panel.add(heading, c);

        JLabel description = new JLabel(
                "<html>Đăng nhập để quản lý lịch hẹn, hồ sơ bệnh nhân<br>"
                        + "và hoạt động phòng khám một cách dễ dàng.</html>");
        description.setFont(new Font("Arial", Font.PLAIN, 14));
        description.setForeground(new Color(218, 236, 237));
        c.gridy = 3;
        c.insets = new Insets(0, 0, 0, 0);
        panel.add(description, c);

        JPanel feature = new JPanel(new BorderLayout(12, 0));
        feature.setOpaque(false);
        JLabel check = new JLabel("✓", SwingConstants.CENTER);
        check.setFont(new Font("Arial", Font.BOLD, 13));
        check.setForeground(Color.WHITE);
        check.setPreferredSize(new Dimension(28, 28));
        check.setOpaque(true);
        check.setBackground(new Color(255, 255, 255, 36));
        JLabel featureText = new JLabel("Chăm sóc tận tâm · Quản lý thông minh");
        featureText.setFont(new Font("Arial", Font.PLAIN, 12));
        featureText.setForeground(new Color(221, 240, 240));
        feature.add(check, BorderLayout.WEST);
        feature.add(featureText, BorderLayout.CENTER);
        c.gridy = 4;
        c.insets = new Insets(62, 0, 0, 0);
        panel.add(feature, c);

        JLabel copyright = new JLabel("© 2026 Muse Dental · Hệ thống quản lý phòng khám");
        copyright.setFont(new Font("Arial", Font.PLAIN, 10));
        copyright.setForeground(new Color(184, 218, 220));
        c.gridy = 5;
        c.weighty = 1;
        c.anchor = GridBagConstraints.SOUTHWEST;
        c.insets = new Insets(26, 0, 0, 0);
        panel.add(copyright, c);
        return panel;
    }

    private JPanel createLoginPanel() {
        // Bố trí biểu mẫu theo từng hàng để dễ căn chỉnh và thay đổi.
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(42, 54, 38, 54));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;

        JLabel welcome = new JLabel("Chào mừng trở lại");
        welcome.setFont(new Font("Arial", Font.BOLD, 27));
        welcome.setForeground(INK);
        c.gridy = 0;
        c.insets = new Insets(0, 0, 8, 0);
        panel.add(welcome, c);

        JLabel subtitle = new JLabel("Vui lòng đăng nhập vào tài khoản của bạn");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);
        c.gridy = 1;
        c.insets = new Insets(0, 0, 32, 0);
        panel.add(subtitle, c);

        addFieldLabel(panel, c, "TÊN ĐĂNG NHẬP", 2);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameField.setForeground(INK);
        usernameField.setCaretColor(TEAL);
        usernameField.setToolTipText("Nhập tên đăng nhập");
        c.gridy = 3;
        c.insets = new Insets(9, 0, 22, 0);
        panel.add(createInputContainer(usernameField, "♙", null), c);

        addFieldLabel(panel, c, "MẬT KHẨU", 4);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setForeground(INK);
        passwordField.setCaretColor(TEAL);
        passwordField.setToolTipText("Nhập mật khẩu");
        JToggleButton visibilityButton = new JToggleButton("Hiện");
        visibilityButton.setFont(new Font("Arial", Font.PLAIN, 11));
        visibilityButton.setForeground(MUTED);
        visibilityButton.setBorderPainted(false);
        visibilityButton.setContentAreaFilled(false);
        visibilityButton.setFocusPainted(false);
        visibilityButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        visibilityButton.addActionListener(e -> {
            // Đổi ký tự che mật khẩu và nhãn nút theo trạng thái hiện/ẩn.
            passwordField.setEchoChar(visibilityButton.isSelected() ? (char) 0 : '•');
            visibilityButton.setText(visibilityButton.isSelected() ? "Ẩn" : "Hiện");
        });
        passwordField.setEchoChar('•');
        c.gridy = 5;
        c.insets = new Insets(9, 0, 12, 0);
        panel.add(createInputContainer(passwordField, "●", visibilityButton), c);

        rememberCheckBox.setFont(new Font("Arial", Font.PLAIN, 12));
        rememberCheckBox.setForeground(MUTED);
        rememberCheckBox.setOpaque(false);
        rememberCheckBox.setFocusPainted(false);
        rememberCheckBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JButton forgotButton = new JButton("Quên mật khẩu?");
        forgotButton.setFont(new Font("Arial", Font.BOLD, 12));
        forgotButton.setForeground(TEAL);
        forgotButton.setBorderPainted(false);
        forgotButton.setContentAreaFilled(false);
        forgotButton.setFocusPainted(false);
        forgotButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotButton.addActionListener(this::showForgotPasswordMessage);

        JPanel options = new JPanel(new BorderLayout());
        options.setOpaque(false);
        options.add(rememberCheckBox, BorderLayout.WEST);
        options.add(forgotButton, BorderLayout.EAST);
        c.gridy = 6;
        c.insets = new Insets(0, 0, 22, 0);
        panel.add(options, c);

        JButton loginButton = createLoginButton();
        createLoginButtonReference = loginButton;
        c.gridy = 7;
        c.insets = new Insets(0, 0, 18, 0);
        panel.add(loginButton, c);

        // Hai đường kẻ hai bên làm nổi bật nhãn bảo mật ở chính giữa.
        JPanel divider = new JPanel(new BorderLayout(12, 0));
        divider.setOpaque(false);
        JPanel leftLine = new JPanel();
        leftLine.setBackground(LINE);
        JPanel rightLine = new JPanel();
        rightLine.setBackground(LINE);
        JLabel secureText = new JLabel("BẢO MẬT & RIÊNG TƯ", SwingConstants.CENTER);
        secureText.setFont(new Font("Arial", Font.BOLD, 9));
        secureText.setForeground(new Color(157, 174, 182));
        divider.add(leftLine, BorderLayout.WEST);
        divider.add(secureText, BorderLayout.CENTER);
        divider.add(rightLine, BorderLayout.EAST);
        c.gridy = 8;
        c.insets = new Insets(0, 0, 17, 0);
        panel.add(divider, c);

        JLabel securityNote = new JLabel("Thông tin của bạn được bảo vệ an toàn.");
        securityNote.setFont(new Font("Arial", Font.PLAIN, 11));
        securityNote.setForeground(MUTED);
        securityNote.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy = 9;
        c.insets = new Insets(0, 0, 0, 0);
        panel.add(securityNote, c);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(new JLabel("Muse Dental"), BorderLayout.WEST);
        JLabel version = new JLabel("STAFF PORTAL  ·  V1.0");
        version.setFont(new Font("Arial", Font.BOLD, 9));
        version.setForeground(new Color(160, 177, 184));
        footer.add(version, BorderLayout.EAST);
        c.gridy = 10;
        c.weighty = 1;
        c.anchor = GridBagConstraints.SOUTH;
        c.insets = new Insets(28, 0, 0, 0);
        panel.add(footer, c);
        return panel;
    }

    private void addFieldLabel(JPanel panel, GridBagConstraints c, String text, int row) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 10));
        label.setForeground(new Color(77, 101, 114));
        c.gridy = row;
        c.insets = new Insets(0, 0, 0, 0);
        panel.add(label, c);
    }

    private JPanel createInputContainer(JTextComponent input, String icon, JComponent trailing) {
        // Dùng chung khung nhập liệu cho tên đăng nhập và mật khẩu;
        // trailing cho phép đặt nút hiện/ẩn cạnh trường mật khẩu.
        JPanel container = new JPanel(new BorderLayout(11, 0));
        container.setBackground(Color.WHITE);
        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(0, 13, 0, 12)));
        container.setPreferredSize(new Dimension(330, 49));
        JLabel leading = new JLabel(icon, SwingConstants.CENTER);
        leading.setFont(new Font("Arial", Font.PLAIN, 15));
        leading.setForeground(new Color(144, 165, 174));
        leading.setPreferredSize(new Dimension(18, 20));
        input.setBorder(BorderFactory.createEmptyBorder());
        input.setOpaque(false);
        container.add(leading, BorderLayout.WEST);
        container.add(input, BorderLayout.CENTER);
        if (trailing != null) {
            container.add(trailing, BorderLayout.EAST);
        }
        return container;
    }

    private JButton createLoginButton() {
        JButton button = new JButton("ĐĂNG NHẬP");
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(TEAL);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(330, 49));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(this::submitLogin);
        return button;
    }

    private void submitLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();

        // Chỉ kiểm tra dữ liệu bắt buộc; xác thực tài khoản cần được nối với backend.
        if (username.isEmpty()) {
            showValidationMessage("Vui lòng nhập tên đăng nhập.", usernameField);
        } else if (password.length == 0) {
            showValidationMessage("Vui lòng nhập mật khẩu.", passwordField);
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Giao diện đăng nhập đã sẵn sàng. Chức năng xác thực sẽ được kết nối sau.",
                    "Muse Dental",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        // Xóa bản sao mật khẩu khỏi bộ nhớ sau khi xử lý.
        java.util.Arrays.fill(password, '\0');
    }

    private void showValidationMessage(String message, JComponent field) {
        JOptionPane.showMessageDialog(this, message, "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
        field.requestFocusInWindow();
    }

    private void showForgotPasswordMessage(ActionEvent event) {
        JOptionPane.showMessageDialog(
                this,
                "Vui lòng liên hệ quản trị viên phòng khám để được hỗ trợ đặt lại mật khẩu.",
                "Quên mật khẩu",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private static final class GradientPanel extends JPanel {
        private GradientPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            // Vẽ nền chuyển sắc và các điểm trang trí nhẹ cho panel thương hiệu.
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setPaint(new GradientPaint(0, 0, new Color(20, 126, 139),
                    getWidth(), getHeight(), new Color(30, 75, 103)));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(new Color(255, 255, 255, 13));
            g.fillOval(getWidth() - 190, 38, 250, 250);
            g.fillOval(getWidth() - 105, 110, 155, 155);
            g.setColor(new Color(239, 103, 151, 95));
            g.fillOval(getWidth() - 58, 72, 13, 13);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static final class ToothMark extends JComponent {
        private ToothMark() {
            setPreferredSize(new Dimension(42, 42));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            // Vẽ biểu tượng chiếc răng bằng các nét vector để không cần ảnh phụ.
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(255, 255, 255, 35));
            g.fillOval(0, 0, 42, 42);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int[] x = {12, 10, 11, 15, 20, 25, 30, 32, 30, 27, 24, 21, 19, 16, 13};
            int[] y = {12, 16, 22, 25, 25, 23, 19, 14, 11, 10, 11, 10, 10, 11, 12};
            g.drawPolyline(x, y, x.length);
            g.drawLine(16, 15, 14, 20);
            g.drawLine(25, 14, 27, 18);
            g.dispose();
        }
    }

    public static void main(String[] args) {
        // Khởi tạo Swing trên Event Dispatch Thread để giao diện hoạt động an toàn.
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception exception) {
                System.err.println("Không thể áp dụng giao diện hệ điều hành: " + exception.getMessage());
            }
            new LoginFrame().setVisible(true);
        });
    }
}

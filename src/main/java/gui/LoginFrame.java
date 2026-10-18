/*
    Người làm phần này: Phan Đức Toàn
    MSSV: 24741631
*/

package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;

/**
 * LoginFrame là màn hình đăng nhập của ứng dụng Muse Dental.
 *
 * Nó tập trung vào trải nghiệm xác thực ban đầu: hiển thị hình ảnh giới thiệu ở một nửa màn hình,
 * còn nửa bên phải chứa các trường tên đăng nhập, mật khẩu, checkbox ghi nhớ và nút đăng nhập.
 * Sau khi người dùng nhập thông tin hợp lệ, màn hình này sẽ chuyển sang `MainFrame` để vào dashboard
 * chính của hệ thống phòng khám.
 */
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

    /**
     * Tạo toàn bộ bố cục của màn hình đăng nhập.
     * Đây là bước khởi tạo giao diện cho cả hai vùng: phần ảnh chào mừng ở trái và phần form đăng nhập
     * ở phải, tạo cảm giác thẩm mỹ và đồng bộ với thiết kế hệ thống nha khoa.
     */
    private JPanel createContent() {
        // Chia thẻ đăng nhập thành phần giới thiệu và phần nhập thông tin.
        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(new Color(243, 248, 249));
        background.setBorder(new EmptyBorder(28, 34, 28, 34));

        // GridLayout chia đều chiều rộng cho hai panel bất kể kích thước nội dung.
        JPanel card = new JPanel(new GridLayout(1, 2));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(232, 239, 241)));
        card.add(createWelcomePanel());
        card.add(createLoginPanel());

        GridBagConstraints backgroundConstraints = new GridBagConstraints();
        backgroundConstraints.fill = GridBagConstraints.BOTH;
        backgroundConstraints.weightx = 1;
        backgroundConstraints.weighty = 1;
        background.add(card, backgroundConstraints);
        return background;
    }

    /**
     * Tải ảnh nền phía trái màn hình để tạo cảnh quan thương hiệu cho ứng dụng.
     * Nếu resource không tồn tại, hệ thống sẽ ném lỗi ngay để lập trình viên biết asset chưa được
     * thêm vào `resources/images`, tránh lỗi giao diện ẩn ở runtime.
     */
    private JPanel createWelcomePanel() {
        URL imageResource = getClass().getResource("/images/LoginFrame_Left.png");
        if (imageResource == null) {
            throw new IllegalStateException(
                    "Không tìm thấy ảnh giao diện bên trái tại resources/images/LoginFrame_Left.png.");
        }
        return new WelcomeImagePanel(new ImageIcon(imageResource));
    }

    /**
     * Xây dựng form đăng nhập bao gồm tiêu đề, nhãn trường nhập liệu, nút hiển thị/ẩn mật khẩu,
     * checkbox ghi nhớ và nút đăng nhập.
     * Mỗi hàng trong layout được đặt theo thứ tự logic để người dùng thao tác theo luồng tự nhiên:
     * nhập username -> password -> lưu tùy chọn -> bấm đăng nhập.
     */
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
        panel.add(createInputContainer(usernameField, "●", null), c);

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
        divider.add(leftLine, BorderLayout.WEST);
        divider.add(rightLine, BorderLayout.EAST);
        c.gridy = 8;
        c.insets = new Insets(0, 0, 17, 0);
        panel.add(divider, c);

        c.gridy = 9;
        c.insets = new Insets(0, 0, 0, 0);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(new JLabel("Nha khoa Muse"), BorderLayout.WEST);
        JLabel version = new JLabel("Hệ thống nha khoa Muse  ·  V1.0");
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

    /**
     * Tạo nhãn cho từng trường thông tin nhập liệu như tên đăng nhập và mật khẩu.
     * Hàm này giúp form có độ rõ ràng cao hơn và đồng bộ về kiểu chữ, khoảng cách và màu sắc.
     */
    private void addFieldLabel(JPanel panel, GridBagConstraints c, String text, int row) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 10));
        label.setForeground(new Color(77, 101, 114));
        c.gridy = row;
        c.insets = new Insets(0, 0, 0, 0);
        panel.add(label, c);
    }

    /**
     * Tạo khung nhập liệu dùng chung cho cả username và password.
     * `icon` đặt ở đầu ô nhập để nhận diện trường, còn `trailing` dùng để chèn nút hành động như
     * "Hiện/Ẩn" cho mật khẩu mà không làm mất cấu trúc của form.
     */
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

    /**
     * Tạo nút đăng nhập với màu nổi bật và kích thước chuẩn để người dùng dễ bấm.
     * Khi nhấn, nút sẽ gọi `submitLogin` để kiểm tra nội dung nhập và điều hướng sang dashboard.
     */
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

    /**
     * Xử lý sự kiện đăng nhập khi người dùng bấm nút hoặc nhấn Enter.
     * Hàm này kiểm tra tính hợp lệ của dữ liệu đầu vào trước khi cho phép chuyển sang màn hình chính.
     * Ở phiên bản hiện tại, đây là bước xác thực sơ bộ trên UI; logic xác thực phía backend sẽ được bổ sung sau.
     */
    private void submitLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();

        // Chỉ kiểm tra dữ liệu bắt buộc; xác thực tài khoản cần được nối với backend.
        if (username.isEmpty()) {
            showValidationMessage("Vui lòng nhập tên đăng nhập.", usernameField);
        } else if (password.length == 0) {
            showValidationMessage("Vui lòng nhập mật khẩu.", passwordField);
        } else {
            // Màn hình đăng nhập hiện chưa kết nối xác thực; mở dashboard với tài khoản đã nhập.
            new MainFrame(username).setVisible(true);
            dispose();
        }
        // Xóa bản sao mật khẩu khỏi bộ nhớ sau khi xử lý.
        java.util.Arrays.fill(password, '\0');
    }

    /**
     * Hiển thị hộp cảnh báo và đặt con trỏ vào trường đang thiếu dữ liệu.
     * Cách làm này giúp người dùng biết chính xác trường nào chưa được nhập và sửa ngay trên màn hình
     * mà không phải mất thời gian tìm kiếm lỗi trong toàn bộ form.
     */
    private void showValidationMessage(String message, JComponent field) {
        JOptionPane.showMessageDialog(this, message, "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
        field.requestFocusInWindow();
    }

    /**
     * Hiển thị hướng dẫn hỗ trợ khi người dùng quên mật khẩu.
     * Thay vì xử lý reset mật khẩu trực tiếp trong UI, hệ thống chỉ ra hướng liên hệ quản trị viên,
     * phù hợp với mô hình bảo mật và giúp giảm rủi ro truy cập trái phép.
     */
    private void showForgotPasswordMessage(ActionEvent event) {
        JOptionPane.showMessageDialog(
                this,
                "Vui lòng liên hệ quản trị viên phòng khám để được hỗ trợ đặt lại mật khẩu.",
                "Quên mật khẩu",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private static final class WelcomeImagePanel extends JPanel {
        private final ImageIcon backgroundImage;

        private WelcomeImagePanel(ImageIcon backgroundImage) {
            this.backgroundImage = backgroundImage;
            setOpaque(true);
            setBackground(new Color(20, 126, 139));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (getWidth() == 0 || getHeight() == 0) {
                return;
            }

            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            // Co ảnh theo chiều rộng để hiển thị trọn ảnh, không cắt mất nội dung.
            int imageWidth = backgroundImage.getIconWidth();
            int imageHeight = backgroundImage.getIconHeight();
            double scale = Math.min(
                    (double) getWidth() / imageWidth,
                    (double) getHeight() / imageHeight);
            int scaledWidth = (int) Math.round(imageWidth * scale);
            int scaledHeight = (int) Math.round(imageHeight * scale);
            int x = (getWidth() - scaledWidth) / 2;
            int y = (getHeight() - scaledHeight) / 2;
            g.drawImage(backgroundImage.getImage(), x, y, scaledWidth, scaledHeight, this);
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

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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;

/**
 * Biểu mẫu đổi mật khẩu hiện tại; việc lưu cần được nối với dịch vụ xác thực.
 */
public final class DoiMatKhauPanel extends JPanel {
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(102, 119, 130);
    private static final Color LINE = new Color(226, 234, 237);
    private static final Color ACCENT = new Color(218, 241, 239);

    private final String username;
    private final JPasswordField currentPassword = new JPasswordField();
    private final JPasswordField newPassword = new JPasswordField();
    private final JPasswordField confirmPassword = new JPasswordField();
    private final JLabel feedback = new JLabel("Mật khẩu mới nên có ít nhất 8 ký tự.");

    public DoiMatKhauPanel(String username) {
        this.username = username;
        setLayout(new BorderLayout());
        setOpaque(false);
        add(createFormCard(), BorderLayout.NORTH);
    }

    // Tạo thẻ biểu mẫu với hướng dẫn bảo mật và các trường mật khẩu.
    private JPanel createFormCard() {
        JPanel card = new JPanel(new BorderLayout(0, 18));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(24, 26, 24, 26)));

        JPanel heading = new JPanel(new BorderLayout(0, 6));
        heading.setOpaque(false);
        JLabel title = new JLabel("Đổi mật khẩu tài khoản");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(INK);
        JLabel account = new JLabel("Tài khoản: " + username);
        account.setFont(new Font("Arial", Font.PLAIN, 12));
        account.setForeground(MUTED);
        heading.add(title, BorderLayout.NORTH);
        heading.add(account, BorderLayout.SOUTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        addPasswordRow(fields, 0, "Mật khẩu hiện tại", currentPassword);
        addPasswordRow(fields, 1, "Mật khẩu mới", newPassword);
        addPasswordRow(fields, 2, "Xác nhận mật khẩu mới", confirmPassword);
        GridBagConstraints feedbackConstraints = new GridBagConstraints();
        feedbackConstraints.gridx = 1;
        feedbackConstraints.gridy = 3;
        feedbackConstraints.weightx = 1;
        feedbackConstraints.fill = GridBagConstraints.HORIZONTAL;
        feedbackConstraints.insets = new Insets(8, 10, 0, 0);
        feedback.setFont(new Font("Arial", Font.PLAIN, 11));
        feedback.setForeground(MUTED);
        fields.add(feedback, feedbackConstraints);

        JButton clearButton = createButton("Xóa nội dung", false);
        clearButton.addActionListener(event -> clearFields());
        JButton submitButton = createButton("Lưu thay đổi", true);
        submitButton.addActionListener(event -> validateAndSubmit());
        JPanel actions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(clearButton);
        actions.add(submitButton);

        JLabel note = new JLabel(
                "<html><b>Lưu ý:</b> Không dùng lại mật khẩu cũ hoặc chia sẻ mật khẩu với người khác.</html>");
        note.setFont(new Font("Arial", Font.PLAIN, 11));
        note.setForeground(MUTED);

        JPanel bottom = new JPanel(new BorderLayout(0, 14));
        bottom.setOpaque(false);
        bottom.add(note, BorderLayout.NORTH);
        bottom.add(actions, BorderLayout.SOUTH);

        card.add(heading, BorderLayout.NORTH);
        card.add(fields, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    // Thêm một nhãn và trường mật khẩu vào lưới biểu mẫu.
    private void addPasswordRow(JPanel panel, int row, String labelText, JPasswordField field) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(7, 0, 7, 10);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(INK);
        panel.add(label, labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.weightx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(7, 0, 7, 0);
        field.setFont(new Font("Arial", Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(340, 38));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(0, 10, 0, 10)));
        panel.add(field, fieldConstraints);
    }

    // Đồng bộ màu và kích thước cho nút thao tác.
    private JButton createButton(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setForeground(INK);
        button.setBackground(primary ? ACCENT : Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primary ? ACCENT : LINE),
                new EmptyBorder(9, 14, 9, 14)));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    // Kiểm tra các trường trước khi báo trạng thái tích hợp backend.
    private void validateAndSubmit() {
        char[] current = currentPassword.getPassword();
        char[] next = newPassword.getPassword();
        char[] confirmation = confirmPassword.getPassword();
        try {
            if (current.length == 0) {
                showValidation("Vui lòng nhập mật khẩu hiện tại.", currentPassword);
                return;
            }
            if (next.length < 8) {
                showValidation("Mật khẩu mới phải có ít nhất 8 ký tự.", newPassword);
                return;
            }
            if (java.util.Arrays.equals(current, next)) {
                showValidation("Mật khẩu mới phải khác mật khẩu hiện tại.", newPassword);
                return;
            }
            if (!java.util.Arrays.equals(next, confirmation)) {
                showValidation("Mật khẩu xác nhận không khớp.", confirmPassword);
                return;
            }
            JOptionPane.showMessageDialog(
                    this,
                    "Thông tin đã qua kiểm tra, nhưng hệ thống chưa kết nối chức năng xác thực để lưu mật khẩu.",
                    "Chưa thể lưu mật khẩu",
                    JOptionPane.INFORMATION_MESSAGE);
        } finally {
            java.util.Arrays.fill(current, '\0');
            java.util.Arrays.fill(next, '\0');
            java.util.Arrays.fill(confirmation, '\0');
        }
    }

    // Hiển thị lỗi kiểm tra và đưa con trỏ tới trường cần chỉnh.
    private void showValidation(String message, JPasswordField field) {
        feedback.setText(message);
        feedback.setForeground(new Color(184, 55, 55));
        JOptionPane.showMessageDialog(this, message, "Thông tin chưa hợp lệ", JOptionPane.WARNING_MESSAGE);
        field.requestFocusInWindow();
    }

    // Xóa nội dung các trường và khôi phục hướng dẫn ban đầu.
    private void clearFields() {
        currentPassword.setText("");
        newPassword.setText("");
        confirmPassword.setText("");
        feedback.setText("Mật khẩu mới nên có ít nhất 8 ký tự.");
        feedback.setForeground(MUTED);
        currentPassword.requestFocusInWindow();
    }
}

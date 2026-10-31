package util;

import connectDB.ConnectDB;
import java.io.Console;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import security.PasswordHasher;

public final class TaoMatKhauBanDau {
    private TaoMatKhauBanDau() {
    }

    public static void main(String[] args) {
        Console console = System.console();
        if (console == null) {
            System.err.println("Hãy chạy công cụ này trong PowerShell hoặc Command Prompt, không chạy trong Output của IDE.");
            return;
        }

        char[] password = console.readPassword("Mật khẩu mới (ít nhất 8 ký tự): ");
        char[] confirmation = console.readPassword("Nhập lại mật khẩu: ");
        try {
            if (password.length < 8) {
                System.err.println("Mật khẩu phải có ít nhất 8 ký tự.");
                return;
            }
            if (!Arrays.equals(password, confirmation)) {
                System.err.println("Mật khẩu nhập lại không khớp.");
                return;
            }

            ConnectDB.getInstance().connect();
            String username = console.readLine("Tên đăng nhập mẫu cần khởi tạo: ").trim();
            if (username.isEmpty()) {
                System.err.println("Tên đăng nhập không được để trống.");
                return;
            }
            String sql = "UPDATE TaiKhoan SET MatKhauMaHoa = ? "
                    + "WHERE TenDangNhap = ? AND MatKhauMaHoa LIKE N'$2a$10$mauMaHoaBcrypt%'";
            try (PreparedStatement statement = ConnectDB.getConnection().prepareStatement(sql)) {
                statement.setString(1, PasswordHasher.hash(password));
                statement.setString(2, username);
                if (statement.executeUpdate() != 1) {
                    System.err.println(
                            "Không tìm thấy tài khoản mẫu cần khởi tạo. Không thay đổi mật khẩu tài khoản hiện có.");
                    return;
                }
            }
            console.printf("Đã đặt mật khẩu ban đầu cho tài khoản %s.%n", username);
        } catch (SQLException exception) {
            System.err.println("Không thể cập nhật mật khẩu: " + exception.getMessage());
        } finally {
            Arrays.fill(password, '\0');
            Arrays.fill(confirmation, '\0');
            ConnectDB.dongKetNoi();
        }
    }
}

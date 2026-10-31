package dao;

import connectDB.ConnectDB;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import security.PasswordHasher;

public class DoiMatKhauDAO {
    public boolean doiMatKhau(String tenDangNhap, char[] matKhauHienTai, char[] matKhauMoi)
            throws SQLException {
        if (tenDangNhap == null || tenDangNhap.trim().isEmpty()
                || matKhauHienTai == null || matKhauMoi == null
                || matKhauHienTai.length == 0 || matKhauMoi.length < 8) {
            throw new IllegalArgumentException("Thông tin đổi mật khẩu không hợp lệ.");
        }

        try {
            String hashHienTai = layHashDangHoatDong(tenDangNhap);
            if (hashHienTai == null || !PasswordHasher.verify(matKhauHienTai, hashHienTai)) {
                return false;
            }

            String hashMoi = PasswordHasher.hash(matKhauMoi);
            String sql = "UPDATE TaiKhoan SET MatKhauMaHoa = ? "
                    + "WHERE TenDangNhap = ? AND MatKhauMaHoa = ? AND TrangThaiTaiKhoan = N'Kich hoat'";
            try (PreparedStatement statement = ConnectDB.getConnection().prepareStatement(sql)) {
                statement.setString(1, hashMoi);
                statement.setString(2, tenDangNhap.trim());
                statement.setString(3, hashHienTai);
                return statement.executeUpdate() == 1;
            }
        } finally {
            Arrays.fill(matKhauHienTai, '\0');
            Arrays.fill(matKhauMoi, '\0');
        }
    }

    private String layHashDangHoatDong(String tenDangNhap) throws SQLException {
        String sql = "SELECT MatKhauMaHoa FROM TaiKhoan "
                + "WHERE TenDangNhap = ? AND TrangThaiTaiKhoan = N'Kich hoat'";
        try (PreparedStatement statement = ConnectDB.getConnection().prepareStatement(sql)) {
            statement.setString(1, tenDangNhap.trim());
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getString("MatKhauMaHoa") : null;
            }
        }
    }
}

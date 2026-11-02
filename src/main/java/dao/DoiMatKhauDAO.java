package dao;

import connectDB.ConnectDB;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class DoiMatKhauDAO {
    public boolean doiMatKhau(String tenDangNhap, char[] matKhauHienTai, char[] matKhauMoi)
            throws SQLException {
        if (tenDangNhap == null || tenDangNhap.trim().isEmpty()
                || matKhauHienTai == null || matKhauMoi == null
                || matKhauHienTai.length == 0 || matKhauMoi.length < 8) {
            throw new IllegalArgumentException("Thông tin đổi mật khẩu không hợp lệ.");
        }

        try {
            String matKhauDangLuu = layMatKhauDangHoatDong(tenDangNhap);
            if (matKhauDangLuu == null || !khopMatKhau(matKhauHienTai, matKhauDangLuu)) {
                return false;
            }

            String sql = "UPDATE TaiKhoan SET MatKhau = ? "
                    + "WHERE TenDangNhap = ? AND MatKhau = ? AND TrangThaiTaiKhoan = N'Kich hoat'";
            try (PreparedStatement statement = ConnectDB.getConnection().prepareStatement(sql)) {
                statement.setString(1, new String(matKhauMoi));
                statement.setString(2, tenDangNhap.trim());
                statement.setString(3, matKhauDangLuu);
                return statement.executeUpdate() == 1;
            }
        } finally {
            Arrays.fill(matKhauHienTai, '\0');
            Arrays.fill(matKhauMoi, '\0');
        }
    }

    private String layMatKhauDangHoatDong(String tenDangNhap) throws SQLException {
        String sql = "SELECT MatKhau FROM TaiKhoan "
                + "WHERE TenDangNhap = ? AND TrangThaiTaiKhoan = N'Kich hoat'";
        try (PreparedStatement statement = ConnectDB.getConnection().prepareStatement(sql)) {
            statement.setString(1, tenDangNhap.trim());
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getString("MatKhau") : null;
            }
        }
    }

    private static boolean khopMatKhau(char[] matKhau, String matKhauTrongCSDL) {
        char[] storedPassword = matKhauTrongCSDL.toCharArray();
        try {
            return Arrays.equals(matKhau, storedPassword);
        } finally {
            Arrays.fill(storedPassword, '\0');
        }
    }
}

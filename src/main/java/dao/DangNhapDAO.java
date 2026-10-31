package dao;

import connectDB.ConnectDB;
import entity.NhanVien;
import entity.TaiKhoan;
import entity.VaiTro;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Arrays;
import security.PasswordHasher;

public class DangNhapDAO {
    public TaiKhoan dangNhap(String tenDangNhap, char[] matKhau) throws SQLException {
        if (tenDangNhap == null || tenDangNhap.trim().isEmpty() || matKhau == null || matKhau.length == 0) {
            return null;
        }

        String sql = "SELECT TK.TenDangNhap, TK.MatKhauMaHoa, TK.TrangThaiTaiKhoan, TK.LanDangNhapCuoi, "
                + "TK.MaVaiTro, VT.TenVaiTro, NV.MaNhanVien, NV.HoTen, NV.NgaySinh, NV.GioiTinh, "
                + "NV.SoDienThoai, NV.Email, NV.DiaChi, NV.NgayVaoLam, NV.TrangThaiLamViec, "
                + "NV.ChuyenMon, NV.SoChungChiHanhNghe "
                + "FROM TaiKhoan TK "
                + "JOIN VaiTro VT ON VT.MaVaiTro = TK.MaVaiTro "
                + "JOIN NhanVien NV ON NV.MaNhanVien = TK.MaNhanVien "
                + "WHERE TK.TenDangNhap = ?";

        TaiKhoan taiKhoan = null;
        try (PreparedStatement statement = ConnectDB.getConnection().prepareStatement(sql)) {
            statement.setString(1, tenDangNhap.trim());
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()
                        || !"Kich hoat".equals(result.getString("TrangThaiTaiKhoan"))
                        || !"Dang lam viec".equals(result.getString("TrangThaiLamViec"))
                        || !PasswordHasher.verify(matKhau, result.getString("MatKhauMaHoa"))) {
                    return null;
                }

                NhanVien nhanVien = new NhanVien(
                        result.getString("MaNhanVien"),
                        result.getString("HoTen"),
                        layNgay(result, "NgaySinh"),
                        result.getString("GioiTinh"),
                        result.getString("SoDienThoai"),
                        result.getString("Email"),
                        result.getString("DiaChi"),
                        layNgay(result, "NgayVaoLam"),
                        result.getString("TrangThaiLamViec"),
                        result.getString("ChuyenMon"),
                        result.getString("SoChungChiHanhNghe"));
                VaiTro vaiTro = new VaiTro(result.getString("MaVaiTro"), result.getString("TenVaiTro"));
                taiKhoan = new TaiKhoan(
                        result.getString("TenDangNhap"),
                        null,
                        result.getString("TrangThaiTaiKhoan"),
                        result.getTimestamp("LanDangNhapCuoi") == null
                                ? null
                                : result.getTimestamp("LanDangNhapCuoi").toLocalDateTime(),
                        nhanVien,
                        vaiTro);
            }
            capNhatLanDangNhap(taiKhoan.getTenDangNhap());
            return taiKhoan;
        } finally {
            Arrays.fill(matKhau, '\0');
        }
    }

    private void capNhatLanDangNhap(String tenDangNhap) throws SQLException {
        String sql = "UPDATE TaiKhoan SET LanDangNhapCuoi = ? WHERE TenDangNhap = ?";
        try (PreparedStatement statement = ConnectDB.getConnection().prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(java.time.LocalDateTime.now()));
            statement.setString(2, tenDangNhap);
            statement.executeUpdate();
        }
    }

    private static LocalDate layNgay(ResultSet result, String column) throws SQLException {
        java.sql.Date date = result.getDate(column);
        return date == null ? null : date.toLocalDate();
    }
}

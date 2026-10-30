package dao;

import connectDB.ConnectDB;
import entity.NhanVien;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO đọc bảng NhanVien cho các chức năng cần thông tin nhân viên đang đăng
 * nhập (quota nhỏ, phục vụ lịch làm việc cá nhân và ghi nhận vật tư tiêu hao).
 */
public class NhanVienDAO {

    /** Truy nhân viên theo mã; không tìm thấy trả null. */
    public NhanVien timTheoMa(String maNhanVien) throws SQLException {
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(
                SQL + " WHERE MaNhanVien = ?")) {
            ps.setString(1, maNhanVien);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? docBanGhi(rs) : null;
            }
        }
    }

    /** Truy nhân viên theo tên đăng nhập (JOIN TaiKhoan); không thấy trả null. */
    public NhanVien timTheoTenDangNhap(String tenDangNhap) throws SQLException {
        String sql = "SELECT NV.MaNhanVien, NV.HoTen, NV.NgaySinh, NV.GioiTinh, NV.SoDienThoai, NV.Email, "
                + "NV.DiaChi, NV.NgayVaoLam, NV.TrangThaiLamViec, NV.ChuyenMon, NV.SoChungChiHanhNghe "
                + "FROM NhanVien NV JOIN TaiKhoan TK ON TK.MaNhanVien = NV.MaNhanVien "
                + "WHERE TK.TenDangNhap = ?";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            ps.setString(1, tenDangNhap);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? docBanGhi(rs) : null;
            }
        }
    }

    private static final String SQL =
            "SELECT MaNhanVien, HoTen, NgaySinh, GioiTinh, SoDienThoai, Email, DiaChi, NgayVaoLam, "
                    + "TrangThaiLamViec, ChuyenMon, SoChungChiHanhNghe FROM NhanVien ";

    private static NhanVien docBanGhi(ResultSet rs) throws SQLException {
        return new NhanVien(
                rs.getString("MaNhanVien"),
                rs.getString("HoTen"),
                rs.getDate("NgaySinh") == null ? null : rs.getDate("NgaySinh").toLocalDate(),
                rs.getString("GioiTinh"),
                rs.getString("SoDienThoai"),
                rs.getString("Email"),
                rs.getString("DiaChi"),
                rs.getDate("NgayVaoLam") == null ? null : rs.getDate("NgayVaoLam").toLocalDate(),
                rs.getString("TrangThaiLamViec"),
                rs.getString("ChuyenMon"),
                rs.getString("SoChungChiHanhNghe"));
    }
}

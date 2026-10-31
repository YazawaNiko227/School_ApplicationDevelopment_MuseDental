package dao;

import connectDB.ConnectDB;
import entity.NhanVien;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO đọc bảng NhanVien cho các chức năng cần thông tin nhân viên đang đăng
 * nhập (quota nhỏ, phục vụ lịch làm việc cá nhân và ghi nhận vật tư tiêu hao).
 */
public class NhanVienDAO {
    private static final String SQL =
            "SELECT MaNhanVien, HoTen, NgaySinh, GioiTinh, SoDienThoai, Email, DiaChi, NgayVaoLam, "
                    + "TrangThaiLamViec, ChuyenMon, SoChungChiHanhNghe FROM NhanVien ";

    public List<NhanVien> layTatCa() throws SQLException {
        List<NhanVien> nhanVienList = new ArrayList<>();
        try (PreparedStatement statement = ConnectDB.getConnection()
                .prepareStatement(SQL + "ORDER BY MaNhanVien");
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                nhanVienList.add(docBanGhi(result));
            }
        }
        return nhanVienList;
    }

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

    public boolean them(NhanVien nhanVien) throws SQLException {
        String sql = "INSERT INTO NhanVien (MaNhanVien, HoTen, NgaySinh, GioiTinh, SoDienThoai, Email, "
                + "DiaChi, NgayVaoLam, TrangThaiLamViec, ChuyenMon, SoChungChiHanhNghe) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = ConnectDB.getConnection().prepareStatement(sql)) {
            ganDuLieu(statement, nhanVien, true);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean capNhat(NhanVien nhanVien) throws SQLException {
        String sql = "UPDATE NhanVien SET HoTen = ?, NgaySinh = ?, GioiTinh = ?, SoDienThoai = ?, "
                + "Email = ?, DiaChi = ?, NgayVaoLam = ?, TrangThaiLamViec = ?, ChuyenMon = ?, "
                + "SoChungChiHanhNghe = ? WHERE MaNhanVien = ?";
        try (PreparedStatement statement = ConnectDB.getConnection().prepareStatement(sql)) {
            ganDuLieu(statement, nhanVien, false);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean capNhatTrangThai(String maNhanVien, String trangThai) throws SQLException {
        String sql = "UPDATE NhanVien SET TrangThaiLamViec = ? WHERE MaNhanVien = ?";
        try (PreparedStatement statement = ConnectDB.getConnection().prepareStatement(sql)) {
            statement.setString(1, trangThai);
            statement.setString(2, maNhanVien);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean xoa(String maNhanVien) throws SQLException {
        String sql = "DELETE FROM NhanVien WHERE MaNhanVien = ?";
        try (PreparedStatement statement = ConnectDB.getConnection().prepareStatement(sql)) {
            statement.setString(1, maNhanVien);
            return statement.executeUpdate() == 1;
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

    private static void ganDuLieu(PreparedStatement statement, NhanVien nhanVien, boolean them)
            throws SQLException {
        int index = 1;
        if (them) {
            statement.setString(index++, nhanVien.getMaNhanVien());
        }
        statement.setString(index++, nhanVien.getHoTen());
        ganNgay(statement, index++, nhanVien.getNgaySinh());
        ganChuoi(statement, index++, nhanVien.getGioiTinh());
        ganChuoi(statement, index++, nhanVien.getSoDienThoai());
        ganChuoi(statement, index++, nhanVien.getEmail());
        ganChuoi(statement, index++, nhanVien.getDiaChi());
        ganNgay(statement, index++, nhanVien.getNgayVaoLam());
        ganChuoi(statement, index++, nhanVien.getTrangThaiLamViec());
        ganChuoi(statement, index++, nhanVien.getChuyenMon());
        ganChuoi(statement, index++, nhanVien.getSoChungChiHanhNghe());
        if (!them) {
            statement.setString(index, nhanVien.getMaNhanVien());
        }
    }

    private static void ganNgay(PreparedStatement statement, int index, java.time.LocalDate value)
            throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.DATE);
        } else {
            statement.setDate(index, Date.valueOf(value));
        }
    }

    private static void ganChuoi(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            statement.setNull(index, Types.NVARCHAR);
        } else {
            statement.setString(index, value.trim());
        }
    }

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

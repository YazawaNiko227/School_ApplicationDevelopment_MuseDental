package dao;

import connectDB.ConnectDB;
import entity.ChuyenMon;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO thao tác bảng ChuyenMon (chỉ làm việc với CSDL, không chứa nghiệp vụ).
 */
public class ChuyenMonDAO {

    public List<ChuyenMon> layTatCa() throws SQLException {
        List<ChuyenMon> list = new ArrayList<>();
        String sql = "SELECT MaChuyenMon, TenChuyenMon FROM ChuyenMon ORDER BY MaChuyenMon";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(docBanGhi(rs));
            }
        }
        return list;
    }

    /** Không tìm thấy trả null. */
    public ChuyenMon timTheoMa(String ma) throws SQLException {
        String sql = "SELECT MaChuyenMon, TenChuyenMon FROM ChuyenMon WHERE MaChuyenMon = ?";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            ps.setString(1, ma);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? docBanGhi(rs) : null;
            }
        }
    }

    /**
     * Không tìm thấy trả null. Collation mặc định của SQL Server không phân biệt
     * hoa/thường.
     */
    public ChuyenMon timTheoTen(String ten) throws SQLException {
        String sql = "SELECT MaChuyenMon, TenChuyenMon FROM ChuyenMon WHERE TenChuyenMon = ?";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            ps.setString(1, ten);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? docBanGhi(rs) : null;
            }
        }
    }

    public boolean them(ChuyenMon chuyenMon) throws SQLException {
        String sql = "INSERT INTO ChuyenMon (MaChuyenMon, TenChuyenMon) VALUES (?, ?)";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            ps.setString(1, chuyenMon.getMaChuyenMon());
            ps.setString(2, chuyenMon.getTenChuyenMon());
            return ps.executeUpdate() == 1;
        }
    }

    public boolean capNhat(ChuyenMon chuyenMon) throws SQLException {
        String sql = "UPDATE ChuyenMon SET TenChuyenMon = ? WHERE MaChuyenMon = ?";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            ps.setString(1, chuyenMon.getTenChuyenMon());
            ps.setString(2, chuyenMon.getMaChuyenMon());
            return ps.executeUpdate() == 1;
        }
    }

    public boolean xoa(String ma) throws SQLException {
        String sql = "DELETE FROM ChuyenMon WHERE MaChuyenMon = ?";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            ps.setString(1, ma);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Số nhân viên đang gắn với một chuyên môn (dùng chặn xóa khi còn dữ liệu liên
     * quan).
     */
    public int demNhanVien(String maChuyenMon) throws SQLException {
        String sql = "SELECT COUNT(*) FROM NhanVien WHERE MaChuyenMon = ?";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            ps.setString(1, maChuyenMon);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Map MaChuyenMon -> số nhân viên, dùng hiển thị cột "Số nhân viên" trên bảng.
     */
    public Map<String, Integer> demNhanVienTheoChuyenMon() throws SQLException {
        Map<String, Integer> map = new HashMap<>();
        String sql = "SELECT MaChuyenMon, COUNT(*) AS SoLuong FROM NhanVien "
                + "WHERE MaChuyenMon IS NOT NULL GROUP BY MaChuyenMon";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("MaChuyenMon"), rs.getInt("SoLuong"));
            }
        }
        return map;
    }

    private static ChuyenMon docBanGhi(ResultSet rs) throws SQLException {
        return new ChuyenMon(rs.getString("MaChuyenMon"), rs.getString("TenChuyenMon"));
    }
}
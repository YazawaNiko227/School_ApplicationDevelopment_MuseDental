package dao;

import connectDB.ConnectDB;
import entity.ChiTietKeHoachDieuTriDichVu;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** DAO bảng KeHoachDieuTri_DichVu (chỉ đọc + thêm/xóa lẻ; dùng khi cần). */
public class KeHoachDieuTri_DichVuDAO {
    private static final String SELECT = "SELECT MaKeHoach, MaDichVu, SoLuong FROM KeHoachDieuTri_DichVu ";

    public List<ChiTietKeHoachDieuTriDichVu> layTheoKeHoach(String maKeHoach) throws SQLException {
        List<ChiTietKeHoachDieuTriDichVu> ds = new ArrayList<>();
        try (PreparedStatement ps = ConnectDB.getConnection()
                .prepareStatement(SELECT + "WHERE MaKeHoach = ? ORDER BY MaDichVu")) {
            ps.setString(1, maKeHoach);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(new ChiTietKeHoachDieuTriDichVu(rs.getString("MaKeHoach"),
                            rs.getString("MaDichVu"), rs.getInt("SoLuong")));
                }
            }
        }
        return ds;
    }

    public boolean them(ChiTietKeHoachDieuTriDichVu ct) throws SQLException {
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(
                "INSERT INTO KeHoachDieuTri_DichVu (MaKeHoach, MaDichVu, SoLuong) VALUES (?,?,?)")) {
            ps.setString(1, ct.getMaKeHoach());
            ps.setString(2, ct.getMaDichVu());
            ps.setInt(3, ct.getSoLuong());
            return ps.executeUpdate() == 1;
        }
    }

    public boolean xoaTheoKeHoach(String maKeHoach) throws SQLException {
        try (PreparedStatement ps = ConnectDB.getConnection()
                .prepareStatement("DELETE FROM KeHoachDieuTri_DichVu WHERE MaKeHoach = ?")) {
            ps.setString(1, maKeHoach);
            return ps.executeUpdate() > 0;
        }
    }
}
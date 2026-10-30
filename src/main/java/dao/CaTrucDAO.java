package dao;

import connectDB.ConnectDB;
import entity.CaTruc;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho bảng CaTruc phục vụ chức năng "Lịch làm việc cá nhân".
 * Ca trực được xếp bởi chức năng phân công của y tá, panel này chỉ đọc.
 */
public class CaTrucDAO {

    /** Toàn bộ ca trực của một nhân viên. */
    public List<CaTruc> layTheoNhanVien(String maNhanVien) throws SQLException {
        String sql = "SELECT MaCa, Ngay, GioBatDau, GioKetThuc, LoaiCa, MaNhanVien, MaPhong "
                + "FROM CaTruc WHERE MaNhanVien = ? ORDER BY Ngay, GioBatDau";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            ps.setString(1, maNhanVien);
            try (ResultSet rs = ps.executeQuery()) {
                List<CaTruc> ketQua = new ArrayList<>();
                while (rs.next()) {
                    ketQua.add(docBanGhi(rs));
                }
                return ketQua;
            }
        }
    }

    /** Ca trực của một nhân viên trong khoảng từ đầu tuần đến cuối tuần. */
    public List<CaTruc> layTheoTuan(String maNhanVien, LocalDate dauTuan, LocalDate cuoiTuan)
            throws SQLException {
        String sql = "SELECT MaCa, Ngay, GioBatDau, GioKetThuc, LoaiCa, MaNhanVien, MaPhong "
                + "FROM CaTruc WHERE MaNhanVien = ? AND Ngay BETWEEN ? AND ? ORDER BY Ngay, GioBatDau";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            ps.setString(1, maNhanVien);
            ps.setDate(2, Date.valueOf(dauTuan));
            ps.setDate(3, Date.valueOf(cuoiTuan));
            try (ResultSet rs = ps.executeQuery()) {
                List<CaTruc> ketQua = new ArrayList<>();
                while (rs.next()) {
                    ketQua.add(docBanGhi(rs));
                }
                return ketQua;
            }
        }
    }

    private static CaTruc docBanGhi(ResultSet rs) throws SQLException {
        return new CaTruc(
                rs.getString("MaCa"),
                rs.getDate("Ngay").toLocalDate(),
                rs.getTime("GioBatDau").toLocalTime(),
                rs.getTime("GioKetThuc").toLocalTime(),
                rs.getString("LoaiCa"),
                rs.getString("MaNhanVien"),
                rs.getString("MaPhong"));
    }
}

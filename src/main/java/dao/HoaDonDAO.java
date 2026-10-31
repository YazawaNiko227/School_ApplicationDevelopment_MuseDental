package dao;

import connectDB.ConnectDB;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HoaDonDAO {

    public boolean lapHoaDon(String maHD, LocalDate ngayLap, String trangThaiThanhToan,
            String pttt, String maNV, String maBN, String maChanDoan) throws SQLException {
        String sql = "INSERT INTO HoaDon "
                + "(MaHoaDon, NgayLap, TrangThaiThanhToan, PhuongThucThanhToan, "
                + "MaNhanVien, MaBenhNhan, MaChanDoan) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = ConnectDB.getConnection().prepareStatement(sql)) {
            stmt.setString(1, maHD);
            stmt.setDate(2, Date.valueOf(ngayLap));
            stmt.setString(3, trangThaiThanhToan);
            stmt.setString(4, pttt);
            stmt.setString(5, maNV);
            stmt.setString(6, maBN);
            stmt.setString(7, maChanDoan);
            return stmt.executeUpdate() == 1;
        }
    }

    public List<String[]> traCuuHoaDon(String tuKhoa) throws SQLException {
        String sql = "SELECT hd.MaHoaDon, bn.HoTen, hd.NgayLap, "
                + "COALESCE((SELECT SUM(ct.SoLuong * ct.DonGiaTaiThoiDiem) "
                + "FROM ChiTietHoaDon ct WHERE ct.MaHoaDon = hd.MaHoaDon), 0) "
                + "+ COALESCE((SELECT SUM(ctt.SoLuong * ctt.DonGiaTaiThoiDiem) "
                + "FROM ChiTietHoaDonThuoc ctt WHERE ctt.MaHoaDon = hd.MaHoaDon), 0) AS ThanhTien "
                + "FROM HoaDon hd LEFT JOIN BenhNhan bn ON hd.MaBenhNhan = bn.MaBenhNhan "
                + "WHERE hd.MaHoaDon LIKE ? OR bn.MaBenhNhan LIKE ? OR bn.HoTen LIKE ?";
        String keyword = tuKhoa == null ? "" : tuKhoa.trim();
        String pattern = "%" + keyword + "%";
        List<String[]> ds = new ArrayList<>();
        try (PreparedStatement stmt = ConnectDB.getConnection().prepareStatement(sql)) {
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ds.add(new String[] {
                            rs.getString("MaHoaDon"),
                            rs.getString("HoTen"),
                            rs.getString("NgayLap"),
                            String.valueOf(rs.getDouble("ThanhTien"))
                    });
                }
            }
        }
        return ds;
    }
}

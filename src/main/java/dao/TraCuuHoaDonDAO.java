package dao;

import connectDB.ConnectDB;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TraCuuHoaDonDAO {

    /**
     * Tra cứu danh sách hóa đơn theo từ khóa (mã hóa đơn, mã bệnh nhân hoặc tên
     * bệnh nhân).
     */
    public List<String[]> traCuuTheoTuKhoa(String tuKhoa) throws SQLException {
        List<String[]> ds = new ArrayList<>();
        String sql = "SELECT hd.MaHoaDon, bn.MaBenhNhan, bn.HoTen, hd.NgayLap, "
                + "COALESCE((SELECT SUM(ct.SoLuong * ct.DonGiaTaiThoiDiem) "
                + "FROM ChiTietHoaDon ct WHERE ct.MaHoaDon = hd.MaHoaDon), 0) "
                + "+ COALESCE((SELECT SUM(ctt.SoLuong * ctt.DonGiaTaiThoiDiem) "
                + "FROM ChiTietHoaDonThuoc ctt WHERE ctt.MaHoaDon = hd.MaHoaDon), 0) AS ThanhTien, "
                + "hd.PhuongThucThanhToan "
                + "FROM HoaDon hd "
                + "LEFT JOIN BenhNhan bn ON hd.MaBenhNhan = bn.MaBenhNhan "
                + "WHERE hd.MaHoaDon LIKE ? OR bn.MaBenhNhan LIKE ? OR bn.HoTen LIKE ?";
        String keyword = tuKhoa == null ? "" : tuKhoa.trim();
        String pattern = "%" + keyword + "%";
        try (PreparedStatement stmt = ConnectDB.getConnection().prepareStatement(sql)) {
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ds.add(new String[] {
                            rs.getString("MaHoaDon"),
                            rs.getString("MaBenhNhan"),
                            rs.getString("HoTen"),
                            rs.getString("NgayLap"),
                            String.valueOf(rs.getDouble("ThanhTien")),
                            rs.getString("PhuongThucThanhToan")
                    });
                }
            }
        }
        return ds;
    }

    /** Lấy các dịch vụ đã sử dụng trong hóa đơn theo mã hóa đơn. */
    public List<String[]> getChiTietHoaDonByMaHD(String maHoaDon) throws SQLException {
        List<String[]> ds = new ArrayList<>();
        String sql = "SELECT ct.MaDichVu, dv.TenDichVu, ct.SoLuong, ct.DonGiaTaiThoiDiem, "
                + "(ct.SoLuong * ct.DonGiaTaiThoiDiem) AS ThanhTien "
                + "FROM ChiTietHoaDon ct "
                + "JOIN DichVu dv ON ct.MaDichVu = dv.MaDichVu "
                + "WHERE ct.MaHoaDon = ?";
        try (PreparedStatement stmt = ConnectDB.getConnection().prepareStatement(sql)) {
            stmt.setString(1, maHoaDon);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ds.add(new String[] {
                            rs.getString("MaDichVu"),
                            rs.getString("TenDichVu"),
                            String.valueOf(rs.getInt("SoLuong")),
                            String.valueOf(rs.getDouble("DonGiaTaiThoiDiem")),
                            String.valueOf(rs.getDouble("ThanhTien"))
                    });
                }
            }
        }
        return ds;
    }
}

/* trinhKiet */
package dao;

import connectDB.ConnectDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TraCuuHoaDonDAO {

    /**
     * Tra cứu danh sách hóa đơn theo từ khóa (Mã hóa đơn, Mã bệnh nhân hoặc Tên
     * bệnh nhân)
     */
    public List<String[]> traCuuTheoTuKhoa(String tuKhoa) {
        List<String[]> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        if (con == null)
            return ds;

        String sql = "SELECT hd.maHoaDon, bn.maBenhNhan, bn.hoTen, hd.ngayLap, hd.thanhTien, hd.phuongThucThanhToan "
                + "FROM HoaDon hd "
                + "LEFT JOIN BenhNhan bn ON hd.maBenhNhan = bn.maBenhNhan "
                + "WHERE hd.maHoaDon LIKE ? OR bn.maBenhNhan LIKE ? OR bn.hoTen LIKE ?";
        try {
            PreparedStatement stmt = con.prepareStatement(sql);
            String keywordPattern = "%" + tuKhoa.trim() + "%";
            stmt.setString(1, keywordPattern);
            stmt.setString(2, keywordPattern);
            stmt.setString(3, keywordPattern);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ds.add(new String[] {
                        rs.getString("maHoaDon"),
                        rs.getString("maBenhNhan"),
                        rs.getString("hoTen"),
                        rs.getString("ngayLap"),
                        String.valueOf(rs.getDouble("thanhTien")),
                        rs.getString("phuongThucThanhToan")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    /**
     * Lấy chi tiết các dịch vụ đã sử dụng trong một hóa đơn theo Mã Hóa Đơn
     */
    public List<String[]> getChiTietHoaDonByMaHD(String maHoaDon) {
        List<String[]> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        if (con == null)
            return ds;

        String sql = "SELECT ct.maDichVu, dv.tenDichVu, ct.soLuong, ct.donGia, ct.thanhTien "
                + "FROM ChiTietHoaDon ct "
                + "JOIN DichVu dv ON ct.maDichVu = dv.maDichVu "
                + "WHERE ct.maHoaDon = ?";
        try {
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maHoaDon);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ds.add(new String[] {
                        rs.getString("maDichVu"),
                        rs.getString("tenDichVu"),
                        String.valueOf(rs.getInt("soLuong")),
                        String.valueOf(rs.getDouble("donGia")),
                        String.valueOf(rs.getDouble("thanhTien"))
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }
}
/* end trinhKiet */
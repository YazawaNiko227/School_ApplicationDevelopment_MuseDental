/* trinhKiet */
package dao;

import connectDB.ConnectDB;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HoaDonDAO {

    public boolean lapHoaDon(String maHD, String maBN, String maNV, LocalDate ngayLap, double tongTien, double giamGia,
            double thanhTien, String pttt, String ghiChu) {
        Connection con = ConnectDB.getConnection();
        if (con == null)
            return false;

        String sql = "INSERT INTO HoaDon (maHoaDon, maBenhNhan, maNhanVien, ngayLap, tongTien, giamGia, thanhTien, phuongThucThanhToan, ghiChu) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maHD);
            stmt.setString(2, maBN);
            stmt.setString(3, maNV);
            stmt.setDate(4, ngayLap != null ? Date.valueOf(ngayLap) : null);
            stmt.setDouble(5, tongTien);
            stmt.setDouble(6, giamGia);
            stmt.setDouble(7, thanhTien);
            stmt.setString(8, pttt);
            stmt.setString(9, ghiChu);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String[]> traCuuHoaDon(String tuKhoa) {
        List<String[]> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        if (con == null)
            return ds;

        String sql = "SELECT hd.maHoaDon, bn.hoTen, hd.ngayLap, hd.thanhTien "
                + "FROM HoaDon hd LEFT JOIN BenhNhan bn ON hd.maBenhNhan = bn.maBenhNhan "
                + "WHERE hd.maHoaDon LIKE ? OR bn.maBenhNhan LIKE ? OR bn.hoTen LIKE ?";
        try {
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, "%" + tuKhoa + "%");
            stmt.setString(2, "%" + tuKhoa + "%");
            stmt.setString(3, "%" + tuKhoa + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ds.add(new String[] {
                        rs.getString("maHoaDon"),
                        rs.getString("hoTen"),
                        rs.getString("ngayLap"),
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
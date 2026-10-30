/* trinhKiet */
package dao;

import connectDB.ConnectDB;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;

public class CongNoTraGopDAO {

    public boolean ghiNhanThuTienDot(String maDot, String maCongNo, LocalDate ngayThanhToan, double soTien, String pttt,
            String ghiChu) {
        Connection con = ConnectDB.getConnection();
        if (con == null)
            return false;

        String sqlDot = "INSERT INTO DotThanhToan (maDot, maCongNo, ngayThanhToan, soTien, phuongThuc, ghiChu) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlUpdateCongNo = "UPDATE CongNoTraGop SET daThanhToan = daThanhToan + ?, conNo = conNo - ?, "
                + "trangThai = CASE WHEN conNo - ? <= 0 THEN N'Đã tất toán' ELSE trangThai END "
                + "WHERE maCongNo = ?";

        try {
            con.setAutoCommit(false);

            PreparedStatement stmtDot = con.prepareStatement(sqlDot);
            stmtDot.setString(1, maDot);
            stmtDot.setString(2, maCongNo);
            stmtDot.setDate(3, ngayThanhToan != null ? Date.valueOf(ngayThanhToan) : null);
            stmtDot.setDouble(4, soTien);
            stmtDot.setString(5, pttt);
            stmtDot.setString(6, ghiChu);
            stmtDot.executeUpdate();

            PreparedStatement stmtCN = con.prepareStatement(sqlUpdateCongNo);
            stmtCN.setDouble(1, soTien);
            stmtCN.setDouble(2, soTien);
            stmtCN.setDouble(3, soTien);
            stmtCN.setString(4, maCongNo);
            stmtCN.executeUpdate();

            con.commit();
            con.setAutoCommit(true);
            return true;
        } catch (Exception e) {
            try {
                if (con != null) {
                    con.rollback();
                    con.setAutoCommit(true);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
        return false;
    }
}
/* end trinhKiet */
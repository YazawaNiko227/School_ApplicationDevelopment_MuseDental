package dao;

import connectDB.ConnectDB;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class CongNoTraGopDAO {

    /**
     * Ghi nhận thanh toán trọn một đợt, cập nhật số đợt còn lại và tất toán hóa
     * đơn khi không còn đợt nào.
     */
    public boolean thanhToanDot(String maDot, LocalDate ngayThanhToan) throws SQLException {
        if (ngayThanhToan == null) {
            throw new IllegalArgumentException("Ngày thanh toán không được null.");
        }

        Connection con = ConnectDB.getConnection();
        boolean autoCommitCu = con.getAutoCommit();
        try {
            con.setAutoCommit(false);
            String maCongNo;
            try (PreparedStatement stmt = con.prepareStatement(
                    "SELECT MaCongNo FROM DotThanhToan WITH (UPDLOCK, HOLDLOCK) "
                            + "WHERE MaDot = ? AND TrangThai <> N'Da thanh toan'")) {
                stmt.setString(1, maDot);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return false;
                    }
                    maCongNo = rs.getString("MaCongNo");
                }
            }

            try (PreparedStatement stmt = con.prepareStatement(
                    "UPDATE DotThanhToan SET TrangThai = N'Da thanh toan', "
                            + "NgayThanhToanThucTe = ? "
                            + "WHERE MaDot = ? AND MaCongNo = ? AND TrangThai <> N'Da thanh toan'")) {
                stmt.setDate(1, Date.valueOf(ngayThanhToan));
                stmt.setString(2, maDot);
                stmt.setString(3, maCongNo);
                if (stmt.executeUpdate() != 1) {
                    throw new SQLException("Đợt thanh toán đã được cập nhật bởi thao tác khác.");
                }
            }

            try (PreparedStatement stmt = con.prepareStatement(
                    "UPDATE CongNoTraGop SET SoDotConLai = SoDotConLai - 1 "
                            + "WHERE MaCongNo = ? AND SoDotConLai > 0")) {
                stmt.setString(1, maCongNo);
                if (stmt.executeUpdate() != 1) {
                    throw new SQLException("Không còn đợt thanh toán chưa thu cho công nợ " + maCongNo + ".");
                }
            }

            try (PreparedStatement stmt = con.prepareStatement(
                    "UPDATE hd SET TrangThaiThanhToan = N'Da thanh toan' "
                            + "FROM HoaDon hd "
                            + "JOIN CongNoTraGop cn ON cn.MaHoaDon = hd.MaHoaDon "
                            + "WHERE cn.MaCongNo = ? AND cn.SoDotConLai = 0")) {
                stmt.setString(1, maCongNo);
                stmt.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException | RuntimeException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(autoCommitCu);
        }
    }
}

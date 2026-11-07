package dao;

import connectDB.ConnectDB;
import entity.DotThanhToan;
import entity.HoSoCongNo;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CongNoTraGopDAO {
    /** Lấy toàn bộ hồ sơ trả góp cùng các kỳ hạn và thông tin bệnh nhân. */
    public List<HoSoCongNo> layTatCa() throws SQLException {
        String sql = "SELECT cn.MaCongNo, cn.MaHoaDon, cn.TongSoTien, bn.MaBenhNhan, bn.HoTen, "
                + "bn.SoDienThoai, dt.MaDot, dt.NgayDenHan, dt.SoTienDot, dt.TrangThai, "
                + "dt.NgayThanhToanThucTe "
                + "FROM CongNoTraGop cn "
                + "JOIN HoaDon hd ON hd.MaHoaDon = cn.MaHoaDon "
                + "JOIN BenhNhan bn ON bn.MaBenhNhan = hd.MaBenhNhan "
                + "JOIN DotThanhToan dt ON dt.MaCongNo = cn.MaCongNo "
                + "ORDER BY cn.MaCongNo, dt.NgayDenHan, dt.MaDot";
        Map<String, HoSoCongNo> ketQua = new LinkedHashMap<>();
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String maCongNo = rs.getString("MaCongNo");
                HoSoCongNo congNo = ketQua.get(maCongNo);
                if (congNo == null) {
                    congNo = new HoSoCongNo(maCongNo, rs.getString("MaHoaDon"),
                            rs.getString("MaBenhNhan"), rs.getString("HoTen"),
                            rs.getString("SoDienThoai"), rs.getDouble("TongSoTien"));
                    ketQua.put(maCongNo, congNo);
                }
                Date ngayThanhToan = rs.getDate("NgayThanhToanThucTe");
                congNo.addDot(new DotThanhToan(
                        rs.getString("MaDot"),
                        rs.getDate("NgayDenHan").toLocalDate(),
                        rs.getDouble("SoTienDot"),
                        rs.getString("TrangThai"),
                        ngayThanhToan == null ? null : ngayThanhToan.toLocalDate(),
                        maCongNo));
            }
        }
        return new ArrayList<>(ketQua.values());
    }

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
                    "SELECT selectedDot.MaCongNo FROM DotThanhToan selectedDot WITH (UPDLOCK, HOLDLOCK) "
                            + "WHERE selectedDot.MaDot = ? AND selectedDot.TrangThai <> N'Da thanh toan' "
                            + "AND selectedDot.MaDot = (SELECT TOP (1) d.MaDot FROM DotThanhToan d "
                            + "WITH (UPDLOCK, HOLDLOCK) WHERE d.MaCongNo = selectedDot.MaCongNo "
                            + "AND d.TrangThai <> N'Da thanh toan' ORDER BY d.NgayDenHan, d.MaDot)")) {
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
                            + "WHERE MaCongNo = ? AND SoDotConLai > 0 "
                            + "AND SoDotConLai = (SELECT COUNT(*) FROM DotThanhToan "
                            + "WHERE MaCongNo = ? AND TrangThai <> N'Da thanh toan') + 1")) {
                stmt.setString(1, maCongNo);
                stmt.setString(2, maCongNo);
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

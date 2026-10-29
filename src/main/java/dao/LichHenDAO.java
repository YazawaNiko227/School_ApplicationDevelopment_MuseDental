package dao;

import connectDB.ConnectDB;
import dto.LichHenInfo;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO đọc bảng LichHen kèm tên bệnh nhân (JOIN BenhNhan) cho hai chức năng:
 * - "Lịch làm việc cá nhân": nha sĩ xem lịch hẹn phụ trách trong tuần.
 * - "Ghi nhận vật tư tiêu hao": chọn lần khám đang khám / vừa khám xong để
 *   xuất kho.
 */
public class LichHenDAO {

    /** Lịch hẹn bệnh nhân của một nha sĩ trong khoảng tuần đang xem. */
    public List<LichHenInfo> layTheoNhaSiTrongTuan(String maNhaSi, LocalDate dauTuan, LocalDate cuoiTuan)
            throws SQLException {
        String sql = "SELECT LH.MaLichHen, LH.ThoiGianHen, LH.TrangThai, LH.MaBenhNhan, BN.HoTen, LH.MaPhong "
                + "FROM LichHen LH JOIN BenhNhan BN ON BN.MaBenhNhan = LH.MaBenhNhan "
                + "WHERE LH.MaNhaSiPhuTrach = ? AND CAST(LH.ThoiGianHen AS DATE) BETWEEN ? AND ? "
                + "ORDER BY LH.ThoiGianHen";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            ps.setString(1, maNhaSi);
            ps.setDate(2, Date.valueOf(dauTuan));
            ps.setDate(3, Date.valueOf(cuoiTuan));
            try (ResultSet rs = ps.executeQuery()) {
                List<LichHenInfo> ketQua = new ArrayList<>();
                while (rs.next()) {
                    ketQua.add(docBanGhi(rs));
                }
                return ketQua;
            }
        }
    }

    /** Các lịch hẹn đang khám hoặc vừa khám xong (TrangThai = 'Dang kham' /
     * 'Hoan thanh'), dùng để chọn lần ghi nhận vật tư tiêu hao.
     */
    public List<LichHenInfo> layChoXuatKho() throws SQLException {
        String sql = "SELECT LH.MaLichHen, LH.ThoiGianHen, LH.TrangThai, LH.MaBenhNhan, BN.HoTen, LH.MaPhong "
                + "FROM LichHen LH JOIN BenhNhan BN ON BN.MaBenhNhan = LH.MaBenhNhan "
                + "WHERE LH.TrangThai IN (N'Dang kham', N'Hoan thanh') "
                + "ORDER BY LH.ThoiGianHen DESC, LH.MaLichHen DESC";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            List<LichHenInfo> ketQua = new ArrayList<>();
            while (rs.next()) {
                ketQua.add(docBanGhi(rs));
            }
            return ketQua;
        }
    }

    private static LichHenInfo docBanGhi(ResultSet rs) throws SQLException {
        return new LichHenInfo(
                rs.getString("MaLichHen"),
                rs.getTimestamp("ThoiGianHen").toLocalDateTime(),
                rs.getString("TrangThai"),
                rs.getString("MaBenhNhan"),
                rs.getString("HoTen"),
                rs.getString("MaPhong"));
    }
}

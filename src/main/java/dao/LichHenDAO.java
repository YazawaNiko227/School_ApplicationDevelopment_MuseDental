package dao;

import connectDB.ConnectDB;
import entity.BenhNhan;
import entity.ChanDoan;
import entity.LichHen;
import entity.PhongKham;

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
 * xuất kho.
 */
public class LichHenDAO {

    /** Lịch hẹn bệnh nhân của một nha sĩ trong khoảng tuần đang xem. */
    public List<LichHen> layTheoNhaSiTrongTuan(String maNhaSi, LocalDate dauTuan, LocalDate cuoiTuan)
            throws SQLException {
        String sql = "SELECT LH.MaLichHen, LH.ThoiGianHen, LH.TrangThai, LH.MaBenhNhan, "
                + "BN.HoTen, LH.MaPhong, CD.MaChanDoan "
                + "FROM LichHen LH JOIN BenhNhan BN ON BN.MaBenhNhan = LH.MaBenhNhan "
                + "LEFT JOIN ChanDoan CD ON CD.MaLichHen = LH.MaLichHen "
                + "WHERE LH.MaNhaSiPhuTrach = ? AND CAST(LH.ThoiGianHen AS DATE) BETWEEN ? AND ? "
                + "ORDER BY LH.ThoiGianHen";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            ps.setString(1, maNhaSi);

            ps.setDate(2, Date.valueOf(dauTuan));
            ps.setDate(3, Date.valueOf(cuoiTuan));
            try (ResultSet rs = ps.executeQuery()) {
                List<LichHen> ketQua = new ArrayList<>();
                while (rs.next()) {
                    ketQua.add(docBanGhi(rs));
                }
                return ketQua;
            }
        }
    }

    /**
     * Các lịch hẹn đang khám hoặc vừa khám xong (TrangThai = 'Dang kham' /
     * 'Hoan thanh'), dùng để chọn lần ghi nhận vật tư tiêu hao.
     */
    public List<LichHen> layChoXuatKho() throws SQLException {
        String sql = "SELECT LH.MaLichHen, LH.ThoiGianHen, LH.TrangThai, LH.MaBenhNhan, "
                + "BN.HoTen, LH.MaPhong, CD.MaChanDoan "
                + "FROM LichHen LH JOIN BenhNhan BN ON BN.MaBenhNhan = LH.MaBenhNhan "
                + "LEFT JOIN ChanDoan CD ON CD.MaLichHen = LH.MaLichHen "
                + "WHERE (LH.TrangThai IN (N'Dang kham', N'Hoan thanh') "
                + "OR CD.TrangThai = N'Nhap') "
                + "AND NOT EXISTS (SELECT 1 FROM PhieuXuatKho PX WHERE PX.MaLichHen = LH.MaLichHen) "
                + "ORDER BY LH.ThoiGianHen DESC, LH.MaLichHen DESC";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            List<LichHen> ketQua = new ArrayList<>();
            while (rs.next()) {
                ketQua.add(docBanGhi(rs));
            }
            return ketQua;
        }
    }

    private static LichHen docBanGhi(ResultSet rs) throws SQLException {
        BenhNhan benhNhan = new BenhNhan();
        benhNhan.setMaBenhNhan(rs.getString("MaBenhNhan"));
        benhNhan.setHoTen(rs.getString("HoTen"));
        PhongKham phongKham = new PhongKham();
        phongKham.setMaPhong(rs.getString("MaPhong"));
        LichHen lichHen = new LichHen();
        lichHen.setMaLichHen(rs.getString("MaLichHen"));
        lichHen.setThoiGianHen(rs.getTimestamp("ThoiGianHen").toLocalDateTime());
        lichHen.setTrangThai(rs.getString("TrangThai"));
        lichHen.setBenhNhan(benhNhan);
        lichHen.setPhongKham(phongKham);
        String maChanDoan = rs.getString("MaChanDoan");
        if (maChanDoan != null) {
            ChanDoan chanDoan = new ChanDoan();
            chanDoan.setMaChanDoan(maChanDoan);
            lichHen.setChanDoan(chanDoan);
        }
        return lichHen;
    }
}

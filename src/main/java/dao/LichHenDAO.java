package dao;

import connectDB.ConnectDB;
import dto.LichHenInfo;
import entity.LichHenKhamInfo;
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

    /** Eligible appointments for today's clinical examination queue. */
    public List<LichHenKhamInfo> layChoKhamHomNay(LocalDate ngay) throws SQLException {
        String sql = "SELECT LH.MaLichHen, LH.ThoiGianHen, LH.TrangThai, LH.MaBenhNhan, BN.HoTen, "
                + "BN.GioiTinh, BN.NgaySinh, BN.TienSuBenhLy, BN.ThongTinDiUng, BN.NguoiBaoHo, "
                + "HS.MaHoSo, LH.MaNhaSiPhuTrach, NS.HoTen AS TenNhaSi, LH.MaPhong, PK.TrangThai AS TrangThaiPhong, "
                + "LH.ThoiGianCheckIn "
                + "FROM LichHen LH "
                + "JOIN BenhNhan BN ON BN.MaBenhNhan = LH.MaBenhNhan "
                + "JOIN NhanVien NS ON NS.MaNhanVien = LH.MaNhaSiPhuTrach "
                + "JOIN PhongKham PK ON PK.MaPhong = LH.MaPhong "
                + "LEFT JOIN HoSoBenhAn HS ON HS.MaBenhNhan = LH.MaBenhNhan "
                + "WHERE CAST(LH.ThoiGianHen AS DATE) = ? "
                + "AND LH.TrangThai IN (N'Da check-in', N'Dang kham', N'Hoan thanh') "
                + "ORDER BY LH.ThoiGianHen, LH.MaLichHen";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(ngay));
            try (ResultSet rs = ps.executeQuery()) {
                List<LichHenKhamInfo> ketQua = new ArrayList<>();
                while (rs.next()) {
                    ketQua.add(new LichHenKhamInfo(
                            rs.getString("MaLichHen"),
                            rs.getTimestamp("ThoiGianHen").toLocalDateTime(),
                            rs.getString("TrangThai"),
                            rs.getString("MaBenhNhan"),
                            rs.getString("HoTen"),
                            rs.getString("GioiTinh"),
                            rs.getDate("NgaySinh") == null ? null : rs.getDate("NgaySinh").toLocalDate(),
                            rs.getString("TienSuBenhLy"),
                            rs.getString("ThongTinDiUng"),
                            rs.getString("NguoiBaoHo"),
                            rs.getString("MaHoSo"),
                            rs.getString("MaNhaSiPhuTrach"),
                            rs.getString("TenNhaSi"),
                            rs.getString("MaPhong"),
                            rs.getString("TrangThaiPhong"),
                            rs.getTimestamp("ThoiGianCheckIn") == null
                                    ? null : rs.getTimestamp("ThoiGianCheckIn").toLocalDateTime()));
                }
                return ketQua;
            }
        }
    }

    /** Lịch hẹn bệnh nhân của một nha sĩ trong khoảng tuần đang xem. */
    public List<LichHenInfo> layTheoNhaSiTrongTuan(String maNhaSi, LocalDate dauTuan, LocalDate cuoiTuan)
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
                rs.getString("MaPhong"),
                rs.getString("MaChanDoan"));
    }
}

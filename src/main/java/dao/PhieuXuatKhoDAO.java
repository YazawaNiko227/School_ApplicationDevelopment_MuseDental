package dao;

import connectDB.ConnectDB;
import entity.BenhNhan;
import entity.ChiTietXuatKho;
import entity.LichHen;
import entity.PhieuXuatKho;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho bảng PhieuXuatKho và ChiTietXuatKho phục vụ chức năng "Ghi nhận
 * vật tư tiêu hao" (xuất kho dùng trong lần khám hoặc dùng chung).
 *
 * Lưu phiếu xả xuất trừ tồn kho (VatTu.SoLuongTon); ba bước "tạo phiếu, ghi
 * chi tiết, trừ tồn" chạy trong MỘT giao tác: vật tư nào không đủ tồn kho thì
 * toàn bộ phiếu bị hủy (khớp CHECK SoLuongTon >= 0 của DB).
 */
public class PhieuXuatKhoDAO {

    /** Ghi phiếu xuất kho: phiếu + chi tiết + trừ tồn, trong một giao tác. */
    public boolean luuPhieuXuat(PhieuXuatKho phieu, List<ChiTietXuatKho> chiTiet) throws SQLException {
        Connection con = ConnectDB.getConnection();
        VatTuDAO vatTuDAO = new VatTuDAO();
        boolean autoCommitCu = con.getAutoCommit();
        try {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO PhieuXuatKho (MaPhieuXuat, NgayXuat, MaNhanVien, MaLichHen) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, phieu.getMaPhieuXuat());
                ps.setDate(2, java.sql.Date.valueOf(phieu.getNgayXuat()));
                ps.setString(3, phieu.getMaNhanVienId());
                ps.setString(4, phieu.getMaLichHenId());
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO ChiTietXuatKho (MaPhieuXuat, MaVatTu, SoLuongSuDung) VALUES (?, ?, ?)")) {
                for (ChiTietXuatKho dong : chiTiet) {
                    vatTuDAO.truTon(con, dong.getMaVatTuId(), dong.getSoLuongSuDung());
                    ps.setString(1, phieu.getMaPhieuXuat());
                    ps.setString(2, dong.getMaVatTuId());
                    ps.setInt(3, dong.getSoLuongSuDung());
                    ps.executeUpdate();
                }
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

    /**
     * Lịch sử đã xuất của một lần khám.
     *
     * @param maLichHen mã lịch hẹn; null để xem các phiếu xuất dùng chung
     *                  (PhieuXuatKho.MaLichHen = NULL)
     */
    public List<ChiTietXuatKho> layChiTietTheoLichHen(String maLichHen) throws SQLException {
        String sql = "SELECT CT.MaPhieuXuat, CT.MaVatTu, CT.SoLuongSuDung "
                + "FROM PhieuXuatKho PX JOIN ChiTietXuatKho CT ON CT.MaPhieuXuat = PX.MaPhieuXuat WHERE "
                // Null -> lấy nhóm phiếu xuất dùng chung (không gắn lịch hẹn).
                + (maLichHen == null ? "PX.MaLichHen IS NULL" : "PX.MaLichHen = ?") + " "
                + "ORDER BY PX.NgayXuat DESC, PX.MaPhieuXuat, CT.MaVatTu";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            if (maLichHen != null) {
                ps.setString(1, maLichHen);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<ChiTietXuatKho> ketQua = new ArrayList<>();
                while (rs.next()) {
                    ketQua.add(new ChiTietXuatKho(
                            rs.getString("MaPhieuXuat"),
                            rs.getString("MaVatTu"),
                            rs.getInt("SoLuongSuDung")));
                }
                return ketQua;
            }
        }
    }

    /** Tra cứu phiếu xuất theo từ khóa và khoảng ngày xuất. */
    public List<PhieuXuatKho> timKiemPhieuXuat(String tuKhoa, LocalDate tuNgay, LocalDate denNgay)
            throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT PX.MaPhieuXuat, PX.NgayXuat, PX.MaLichHen, LH.MaBenhNhan, BN.HoTen, "
                        + "COUNT(CT.MaVatTu) AS SoLoaiVatTu, SUM(CT.SoLuongSuDung) AS TongSoLuong "
                        + "FROM PhieuXuatKho PX "
                        + "JOIN ChiTietXuatKho CT ON CT.MaPhieuXuat = PX.MaPhieuXuat "
                        + "LEFT JOIN LichHen LH ON LH.MaLichHen = PX.MaLichHen "
                        + "LEFT JOIN BenhNhan BN ON BN.MaBenhNhan = LH.MaBenhNhan "
                        + "WHERE 1 = 1 ");
        boolean coTuKhoa = tuKhoa != null && !tuKhoa.trim().isEmpty();
        if (coTuKhoa) {
            sql.append("AND (PX.MaPhieuXuat LIKE ? OR PX.MaLichHen LIKE ? OR BN.MaBenhNhan LIKE ? "
                    + "OR BN.HoTen LIKE ? OR EXISTS (SELECT 1 FROM ChiTietXuatKho CT2 "
                    + "JOIN VatTu VT ON VT.MaVatTu = CT2.MaVatTu WHERE CT2.MaPhieuXuat = PX.MaPhieuXuat "
                    + "AND (VT.MaVatTu LIKE ? OR VT.TenVatTu LIKE ?))) ");
        }
        if (tuNgay != null) {
            sql.append("AND PX.NgayXuat >= ? ");
        }
        if (denNgay != null) {
            sql.append("AND PX.NgayXuat <= ? ");
        }
        sql.append("GROUP BY PX.MaPhieuXuat, PX.NgayXuat, PX.MaLichHen, LH.MaBenhNhan, BN.HoTen "
                + "ORDER BY PX.NgayXuat DESC, PX.MaPhieuXuat DESC");

        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql.toString())) {
            int parameter = 1;
            if (coTuKhoa) {
                String pattern = "%" + tuKhoa.trim() + "%";
                for (int i = 0; i < 6; i++) {
                    ps.setString(parameter++, pattern);
                }
            }
            if (tuNgay != null) {
                ps.setDate(parameter++, Date.valueOf(tuNgay));
            }
            if (denNgay != null) {
                ps.setDate(parameter, Date.valueOf(denNgay));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<PhieuXuatKho> ketQua = new ArrayList<>();
                while (rs.next()) {
                    PhieuXuatKho phieu = new PhieuXuatKho();
                    phieu.setMaPhieuXuat(rs.getString("MaPhieuXuat"));
                    phieu.setNgayXuat(rs.getDate("NgayXuat").toLocalDate());
                    phieu.setMaLichHen(rs.getString("MaLichHen"));
                    phieu.setSoLoaiVatTu(rs.getInt("SoLoaiVatTu"));
                    phieu.setTongSoLuong(rs.getInt("TongSoLuong"));
                    String maBenhNhan = rs.getString("MaBenhNhan");
                    if (maBenhNhan != null) {
                        BenhNhan benhNhan = new BenhNhan();
                        benhNhan.setMaBenhNhan(maBenhNhan);
                        benhNhan.setHoTen(rs.getString("HoTen"));
                        LichHen lichHen = new LichHen();
                        lichHen.setMaLichHen(rs.getString("MaLichHen"));
                        lichHen.setBenhNhan(benhNhan);
                        phieu.setLichHen(lichHen);
                    }
                    ketQua.add(phieu);
                }
                return ketQua;
            }
        }
    }

    /** Chi tiết vật tư của một phiếu xuất cụ thể. */
    public List<ChiTietXuatKho> layChiTietPhieuXuat(String maPhieuXuat) throws SQLException {
        String sql = "SELECT CT.MaPhieuXuat, CT.MaVatTu, VT.TenVatTu, VT.DonViTinh, CT.SoLuongSuDung "
                + "FROM ChiTietXuatKho CT JOIN VatTu VT ON VT.MaVatTu = CT.MaVatTu "
                + "WHERE CT.MaPhieuXuat = ? ORDER BY CT.MaVatTu";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            ps.setString(1, maPhieuXuat);
            try (ResultSet rs = ps.executeQuery()) {
                List<ChiTietXuatKho> ketQua = new ArrayList<>();
                while (rs.next()) {
                    entity.VatTu vatTu = new entity.VatTu();
                    vatTu.setMaVatTu(rs.getString("MaVatTu"));
                    vatTu.setTenVatTu(rs.getString("TenVatTu"));
                    vatTu.setDonViTinh(rs.getString("DonViTinh"));
                    ChiTietXuatKho chiTiet = new ChiTietXuatKho();
                    chiTiet.setMaPhieuXuat(maPhieuXuat);
                    chiTiet.setVatTu(vatTu);
                    chiTiet.setSoLuongSuDung(rs.getInt("SoLuongSuDung"));
                    ketQua.add(chiTiet);
                }
                return ketQua;
            }
        }
    }

    /** Sinh mã PX### tiếp theo (qua mã lớn nhất hiện có để không trùng). */
    public String sinhMa() throws SQLException {
        int max = 0;
        try (PreparedStatement ps = ConnectDB.getConnection()
                .prepareStatement("SELECT MaPhieuXuat FROM PhieuXuatKho WHERE MaPhieuXuat LIKE 'PX%'");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                max = Math.max(max, soThuTu(rs.getString(1)));
            }
        }
        return String.format("PX%03d", max + 1);
    }

    private static int soThuTu(String maPhieuXuat) {
        try {
            return Integer.parseInt(maPhieuXuat.substring(2));
        } catch (RuntimeException saiDoi) {
            // Mã không theo định dạng PX###; bỏ qua khi tính mã lớn nhất.
            return 0;
        }
    }
}

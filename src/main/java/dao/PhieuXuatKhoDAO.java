package dao;

import connectDB.ConnectDB;
import entity.ChiTietXuatKho;
import entity.PhieuXuatKho;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
                ps.setString(3, phieu.getMaNhanVien());
                ps.setString(4, phieu.getMaLichHen());
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO ChiTietXuatKho (MaPhieuXuat, MaVatTu, SoLuongSuDung) VALUES (?, ?, ?)")) {
                for (ChiTietXuatKho dong : chiTiet) {
                    vatTuDAO.truTon(con, dong.getMaVatTu(), dong.getSoLuongSuDung());
                    ps.setString(1, phieu.getMaPhieuXuat());
                    ps.setString(2, dong.getMaVatTu());
                    ps.setInt(3, dong.getSoLuongSuDung());
                    ps.executeUpdate();
                }
            }
            con.commit();
            return true;
        } catch (SQLException e) {
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

    /**
     * Lịch sử chi tiết vật tư đã xuất (kèm tên và đơn vị tính) để hiển thị lên bảng giao diện.
     */
    public List<dto.ChiTietXuatInfo> layLichSuXuatTheoLichHen(String maLichHen) throws SQLException {
        String sql = "SELECT CT.MaPhieuXuat, CT.MaVatTu, VT.TenVatTu, VT.DonViTinh, CT.SoLuongSuDung "
                + "FROM PhieuXuatKho PX "
                + "JOIN ChiTietXuatKho CT ON CT.MaPhieuXuat = PX.MaPhieuXuat "
                + "JOIN VatTu VT ON VT.MaVatTu = CT.MaVatTu WHERE "
                + (maLichHen == null ? "PX.MaLichHen IS NULL" : "PX.MaLichHen = ?") + " "
                + "ORDER BY PX.NgayXuat DESC, PX.MaPhieuXuat DESC, CT.MaVatTu";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            if (maLichHen != null) {
                ps.setString(1, maLichHen);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<dto.ChiTietXuatInfo> ketQua = new ArrayList<>();
                while (rs.next()) {
                    ketQua.add(new dto.ChiTietXuatInfo(
                            rs.getString("MaPhieuXuat"),
                            rs.getString("MaVatTu"),
                            rs.getString("TenVatTu"),
                            rs.getString("DonViTinh"),
                            rs.getInt("SoLuongSuDung")));
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

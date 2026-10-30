package dao;

import connectDB.ConnectDB;
import dto.LanKham;
import entity.ChiTietDonThuoc;
import entity.DonThuoc;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho bảng DonThuoc và ChiTietDonThuoc phục vụ chức năng "Kê đơn thuốc
 * điện tử".
 *
 * Quy tắc nghiệp vụ trong DB:
 * - Mỗi lần khám (ChanDoan) có tối đa 1 đơn thuốc (UQ_DonThuoc_ChanDoan).
 * - Mỗi thuốc chỉ có 1 dòng trong đơn (khóa chính (MaDonThuoc, MaVatTu)).
 * Nên thao tác lưu được gọn trong một phương thức luuDonThuoc(): tạo mới hoặc
 * cập nhật + thay toàn bộ chi tiết, chạy trong MỘT giao tác (transaction).
 */
public class DonThuocDAO {

    /** Lần khám đang chờ kê đơn (TrangThai = 'Nhap') kèm thông tin bệnh nhân. */
    public List<LanKham> layLanKhamChoKeDon() throws SQLException {
        String sql = "SELECT CD.MaChanDoan, BN.MaBenhNhan, BN.HoTen, CD.KetLuanChanDoan, BN.ThongTinDiUng "
                + "FROM ChanDoan CD "
                + "JOIN HoSoBenhAn HS ON HS.MaHoSo = CD.MaHoSo "
                + "JOIN BenhNhan BN ON BN.MaBenhNhan = HS.MaBenhNhan "
                + "WHERE CD.TrangThai = N'Nhap' "
                + "ORDER BY CD.NgayKham DESC, CD.MaChanDoan DESC";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            List<LanKham> ketQua = new ArrayList<>();
            while (rs.next()) {
                ketQua.add(new LanKham(
                        rs.getString("MaChanDoan"),
                        rs.getString("MaBenhNhan"),
                        rs.getString("HoTen"),
                        rs.getString("KetLuanChanDoan"),
                        rs.getString("ThongTinDiUng")));
            }
            return ketQua;
        }
    }

    /** Đơn thuốc đã lưu của một lần khám; chưa có trả null. */
    public DonThuoc timTheoChanDoan(String maChanDoan) throws SQLException {
        String sql = "SELECT MaDonThuoc, NgayKe, LoiDanChamSoc, MaChanDoan FROM DonThuoc WHERE MaChanDoan = ?";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            ps.setString(1, maChanDoan);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new DonThuoc(
                        rs.getString("MaDonThuoc"),
                        rs.getDate("NgayKe").toLocalDate(),
                        rs.getString("LoiDanChamSoc"),
                        rs.getString("MaChanDoan"));
            }
        }
    }

    /** Các dòng thuốc của một đơn thuốc (liều dùng, đường dùng, số lượng). */
    public List<ChiTietDonThuoc> layChiTiet(String maDonThuoc) throws SQLException {
        String sql = "SELECT MaDonThuoc, MaVatTu, LieuDung, DuongDung, SoLuong "
                + "FROM ChiTietDonThuoc WHERE MaDonThuoc = ? ORDER BY MaVatTu";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            ps.setString(1, maDonThuoc);
            try (ResultSet rs = ps.executeQuery()) {
                List<ChiTietDonThuoc> ketQua = new ArrayList<>();
                while (rs.next()) {
                    ketQua.add(new ChiTietDonThuoc(
                            rs.getString("MaDonThuoc"),
                            rs.getString("MaVatTu"),
                            rs.getString("LieuDung"),
                            rs.getString("DuongDung"),
                            rs.getInt("SoLuong")));
                }
                return ketQua;
            }
        }
    }

    /** Sinh mã DT### tiếp theo (qua mã lớn nhất hiện có để không trùng). */
    public String sinhMa() throws SQLException {
        int max = 0;
        try (PreparedStatement ps = ConnectDB.getConnection()
                .prepareStatement("SELECT MaDonThuoc FROM DonThuoc WHERE MaDonThuoc LIKE 'DT%'");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                max = Math.max(max, soThuTu(rs.getString(1)));
            }
        }
        return String.format("DT%03d", max + 1);
    }

    /**
     * Lưu đơn thuốc (thao tác chính của chức năng):
     * lần khám chưa có đơn thì tạo mới, có rồi thì cập nhật lời dặn, sau đó
     * xóa toàn bộ dòng chi tiết và ghi lại theo danh sách mới.
     * Tất cả chạy trong MỘT giao tác: lỗi ở bất kỳ bước nào cũng giữ nguyên DB.
     *
     * @param don     đơn thuốc (đã có MaDonThuoc và MaChanDoan)
     * @param chiTiet các dòng thuốc của đơn (ít nhất 1 dòng)
     * @return đơn thuốc vừa lưu (mã đã xác nhận)
     */
    public DonThuoc luuDonThuoc(DonThuoc don, List<ChiTietDonThuoc> chiTiet) throws SQLException {
        Connection con = ConnectDB.getConnection();
        boolean autoCommitCu = con.getAutoCommit();
        try {
            con.setAutoCommit(false);
            if (tonTai(con, don.getMaDonThuoc())) {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE DonThuoc SET LoiDanChamSoc = ? WHERE MaDonThuoc = ?")) {
                    ps.setString(1, don.getLoiDanChamSoc());
                    ps.setString(2, don.getMaDonThuoc());
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO DonThuoc (MaDonThuoc, NgayKe, LoiDanChamSoc, MaChanDoan) "
                                + "VALUES (?, ?, ?, ?)")) {
                    ps.setString(1, don.getMaDonThuoc());
                    ps.setDate(2, Date.valueOf(don.getNgayKe()));
                    ps.setString(3, don.getLoiDanChamSoc());
                    ps.setString(4, don.getMaChanDoan());
                    ps.executeUpdate();
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM ChiTietDonThuoc WHERE MaDonThuoc = ?")) {
                ps.setString(1, don.getMaDonThuoc());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO ChiTietDonThuoc (MaDonThuoc, MaVatTu, LieuDung, DuongDung, SoLuong) "
                            + "VALUES (?, ?, ?, ?, ?)")) {
                for (ChiTietDonThuoc dong : chiTiet) {
                    ps.setString(1, don.getMaDonThuoc());
                    ps.setString(2, dong.getMaVatTu());
                    ps.setString(3, dong.getLieuDung());
                    ps.setString(4, dong.getDuongDung());
                    ps.setInt(5, dong.getSoLuong());
                    ps.executeUpdate();
                }
            }
            con.commit();
            return don;
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(autoCommitCu);
        }
    }

    private static boolean tonTai(Connection con, String maDonThuoc) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM DonThuoc WHERE MaDonThuoc = ?")) {
            ps.setString(1, maDonThuoc);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static int soThuTu(String maDonThuoc) {
        try {
            return Integer.parseInt(maDonThuoc.substring(2));
        } catch (RuntimeException saiDoi) {
            // Mã không theo định dạng DT###; bỏ qua khi tính mã lớn nhất.
            return 0;
        }
    }
}

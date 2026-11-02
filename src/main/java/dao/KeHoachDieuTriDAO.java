package dao;

import connectDB.ConnectDB;
import entity.KeHoachDieuTri;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** DAO bảng KeHoachDieuTri (phác đồ điều trị). */
public class KeHoachDieuTriDAO {
    private static final String SELECT = "SELECT MaKeHoach, TenPhacDo, ThoiGianDuKien, TrangThai, "
            + "MaChanDoan, MaBenhNhan FROM KeHoachDieuTri ";

    public static final class ThongTinBenhNhan {
        private final String maBenhNhan;
        private final String hoTen;

        private ThongTinBenhNhan(String maBenhNhan, String hoTen) {
            this.maBenhNhan = maBenhNhan;
            this.hoTen = hoTen;
        }

        public String getMaBenhNhan() {
            return maBenhNhan;
        }

        public String getHoTen() {
            return hoTen;
        }
    }

    public List<KeHoachDieuTri> layTatCa() throws SQLException {
        List<KeHoachDieuTri> ds = new ArrayList<>();
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(SELECT + "ORDER BY MaKeHoach");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                ds.add(doc(rs));
        }
        return ds;
    }

    public KeHoachDieuTri timTheoMa(String ma) throws SQLException {
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(SELECT + "WHERE MaKeHoach = ?")) {
            ps.setString(1, ma);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? doc(rs) : null;
            }
        }
    }

    public KeHoachDieuTri timTheoChanDoan(String maChanDoan) throws SQLException {
        try (PreparedStatement ps = ConnectDB.getConnection()
                .prepareStatement(SELECT + "WHERE MaChanDoan = ?")) {
            ps.setString(1, maChanDoan);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? doc(rs) : null;
            }
        }
    }

    public ThongTinBenhNhan layThongTinBenhNhanTheoChanDoan(String maChanDoan) throws SQLException {
        String sql = "SELECT B.MaBenhNhan, B.HoTen FROM ChanDoan C "
                + "JOIN HoSoBenhAn H ON H.MaHoSo = C.MaHoSo "
                + "JOIN BenhNhan B ON B.MaBenhNhan = H.MaBenhNhan "
                + "WHERE C.MaChanDoan = ?";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            ps.setString(1, maChanDoan);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new ThongTinBenhNhan(rs.getString("MaBenhNhan"), rs.getString("HoTen")) : null;
            }
        }
    }

    public String sinhMa() throws SQLException {
        return BenhNhanDAO.sinhMa(ConnectDB.getConnection(), "KeHoachDieuTri", "MaKeHoach", "KH");
    }

    /**
     * Lưu phác đồ + toàn bộ dịch vụ trong 1 transaction:
     * - Nếu chưa có (maKeHoach null) -> INSERT, sinh mã mới.
     * - Nếu đã có -> UPDATE, xóa hết dịch vụ cũ rồi INSERT lại.
     * 
     * @return mã kế hoạch (mới hoặc cũ)
     */
    public String luuPhacDo(KeHoachDieuTri kh, List<String[]> dsDichVu) throws SQLException {
        if (kh == null) {
            throw new IllegalArgumentException("Phác đồ không được null.");
        }
        if (dsDichVu == null || dsDichVu.isEmpty()) {
            throw new IllegalArgumentException("Phác đồ cần có ít nhất một dịch vụ.");
        }
        for (String[] dong : dsDichVu) {
            if (dong == null || dong.length < 2 || dong[0] == null || dong[0].trim().isEmpty()) {
                throw new IllegalArgumentException("Thông tin dịch vụ trong phác đồ không hợp lệ.");
            }
            try {
                if (Integer.parseInt(dong[1]) <= 0) {
                    throw new IllegalArgumentException("Số lượng dịch vụ phải lớn hơn 0.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Số lượng dịch vụ phải là số nguyên dương.", e);
            }
        }
        Connection con = ConnectDB.getConnection();
        boolean autoCommitCu = con.getAutoCommit();
        try {
            con.setAutoCommit(false);
            boolean taoMoi = kh.getMaKeHoach() == null || kh.getMaKeHoach().trim().isEmpty();
            if (taoMoi) {
                String maMoi = BenhNhanDAO.sinhMa(con, "KeHoachDieuTri", "MaKeHoach", "KH");
                kh.setMaKeHoach(maMoi);
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO KeHoachDieuTri (MaKeHoach, TenPhacDo, ThoiGianDuKien, TrangThai, "
                                + "MaChanDoan, MaBenhNhan) VALUES (?,?,?,?,?,?)")) {
                    ps.setString(1, maMoi);
                    ps.setString(2, kh.getTenPhacDo());
                    if (kh.getThoiGianDuKien() == null)
                        ps.setNull(3, Types.INTEGER);
                    else
                        ps.setInt(3, kh.getThoiGianDuKien());
                    ps.setString(4, kh.getTrangThai());
                    ps.setString(5, kh.getMaChanDoan());
                    ps.setString(6, kh.getMaBenhNhan());
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE KeHoachDieuTri SET TenPhacDo=?, ThoiGianDuKien=?, TrangThai=? WHERE MaKeHoach=?")) {
                    ps.setString(1, kh.getTenPhacDo());
                    if (kh.getThoiGianDuKien() == null)
                        ps.setNull(2, Types.INTEGER);
                    else
                        ps.setInt(2, kh.getThoiGianDuKien());
                    ps.setString(3, kh.getTrangThai());
                    ps.setString(4, kh.getMaKeHoach());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM KeHoachDieuTri_DichVu WHERE MaKeHoach = ?")) {
                    ps.setString(1, kh.getMaKeHoach());
                    ps.executeUpdate();
                }
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO KeHoachDieuTri_DichVu (MaKeHoach, MaDichVu, SoLuong) VALUES (?,?,?)")) {
                for (String[] dong : dsDichVu) {
                    ps.setString(1, kh.getMaKeHoach());
                    ps.setString(2, dong[0]);
                    ps.setInt(3, Integer.parseInt(dong[1]));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            con.commit();
            return kh.getMaKeHoach();
        } catch (SQLException | RuntimeException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(autoCommitCu);
        }
    }

    public boolean xoa(String ma) throws SQLException {
        Connection con = ConnectDB.getConnection();
        boolean autoCommitCu = con.getAutoCommit();
        try {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM KeHoachDieuTri_DichVu WHERE MaKeHoach = ?")) {
                ps.setString(1, ma);
                ps.executeUpdate();
            }
            int n;
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM KeHoachDieuTri WHERE MaKeHoach = ?")) {
                ps.setString(1, ma);
                n = ps.executeUpdate();
            }
            con.commit();
            return n == 1;
        } catch (SQLException | RuntimeException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(autoCommitCu);
        }
    }

    private static KeHoachDieuTri doc(ResultSet rs) throws SQLException {
        int tg = rs.getInt("ThoiGianDuKien");
        Integer thoiGian = rs.wasNull() ? null : tg;
        return new KeHoachDieuTri(rs.getString("MaKeHoach"), rs.getString("TenPhacDo"), thoiGian,
                rs.getString("TrangThai"), rs.getString("MaChanDoan"), rs.getString("MaBenhNhan"));
    }
}
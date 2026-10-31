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
        } catch (SQLException e) {
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
        } catch (SQLException e) {
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
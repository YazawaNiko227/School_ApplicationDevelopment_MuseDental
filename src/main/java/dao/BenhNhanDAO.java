package dao;

import connectDB.ConnectDB;
import entity.BenhNhan;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** DAO bảng BenhNhan (+ tạo HoSoBenhAn 1-1 khi thêm bệnh nhân mới). */
public class BenhNhanDAO {
    private static final String SELECT = "SELECT MaBenhNhan, HoTen, NgaySinh, GioiTinh, SoDienThoai, DiaChi, "
            + "SoCCCD, TienSuBenhLy, ThongTinDiUng, NguoiBaoHo FROM BenhNhan ";

    public List<BenhNhan> layTatCa() throws SQLException {
        List<BenhNhan> ds = new ArrayList<>();
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(SELECT + "ORDER BY MaBenhNhan");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(doc(rs));
            }
        }
        return ds;
    }

    public BenhNhan timTheoMa(String ma) throws SQLException {
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(SELECT + "WHERE MaBenhNhan = ?")) {
            ps.setString(1, ma);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? doc(rs) : null;
            }
        }
    }

    public String sinhMa() throws SQLException {
        return sinhMa(ConnectDB.getConnection(), "BenhNhan", "MaBenhNhan", "BN");
    }

    /** Thêm bệnh nhân và tạo luôn HoSoBenhAn trong một giao tác. */
    public boolean them(BenhNhan bn) throws SQLException {
        Connection con = ConnectDB.getConnection();
        boolean autoCommitCu = con.getAutoCommit();
        try {
            con.setAutoCommit(false);
            String sql = "INSERT INTO BenhNhan (MaBenhNhan, HoTen, NgaySinh, GioiTinh, SoDienThoai, DiaChi, "
                    + "SoCCCD, TienSuBenhLy, ThongTinDiUng, NguoiBaoHo) VALUES (?,?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, bn.getMaBenhNhan());
                ps.setString(2, bn.getHoTen());
                if (bn.getNgaySinh() == null) {
                    ps.setNull(3, Types.DATE);
                } else {
                    ps.setDate(3, Date.valueOf(bn.getNgaySinh()));
                }
                ps.setString(4, bn.getGioiTinh());
                ps.setString(5, bn.getSoDienThoai());
                ps.setString(6, bn.getDiaChi());
                ps.setString(7, bn.getSoCCCD());
                ps.setString(8, bn.getTienSuBenhLy());
                ps.setString(9, bn.getThongTinDiUng());
                ps.setString(10, bn.getNguoiBaoHo());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO HoSoBenhAn (MaHoSo, MaBenhNhan) VALUES (?, ?)")) {
                ps.setString(1, sinhMa(con, "HoSoBenhAn", "MaHoSo", "HS"));
                ps.setString(2, bn.getMaBenhNhan());
                ps.executeUpdate();
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

    public boolean capNhat(BenhNhan bn) throws SQLException {
        String sql = "UPDATE BenhNhan SET HoTen=?, NgaySinh=?, GioiTinh=?, SoDienThoai=?, DiaChi=?, "
                + "SoCCCD=?, TienSuBenhLy=?, ThongTinDiUng=?, NguoiBaoHo=? WHERE MaBenhNhan=?";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            ps.setString(1, bn.getHoTen());
            if (bn.getNgaySinh() == null) {
                ps.setNull(2, Types.DATE);
            } else {
                ps.setDate(2, Date.valueOf(bn.getNgaySinh()));
            }
            ps.setString(3, bn.getGioiTinh());
            ps.setString(4, bn.getSoDienThoai());
            ps.setString(5, bn.getDiaChi());
            ps.setString(6, bn.getSoCCCD());
            ps.setString(7, bn.getTienSuBenhLy());
            ps.setString(8, bn.getThongTinDiUng());
            ps.setString(9, bn.getNguoiBaoHo());
            ps.setString(10, bn.getMaBenhNhan());
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Xóa bệnh nhân (và HoSoBenhAn rỗng). Nếu đã có dữ liệu khám/lịch hẹn/hóa đơn
     * thì
     * SQL Server báo lỗi FK (547), giao tác rollback; GUI bắt getErrorCode()==547.
     */
    public boolean xoa(String ma) throws SQLException {
        Connection con = ConnectDB.getConnection();
        boolean autoCommitCu = con.getAutoCommit();
        try {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM HoSoBenhAn WHERE MaBenhNhan = ?")) {
                ps.setString(1, ma);
                ps.executeUpdate();
            }
            boolean ok;
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM BenhNhan WHERE MaBenhNhan = ?")) {
                ps.setString(1, ma);
                ok = ps.executeUpdate() == 1;
            }
            con.commit();
            return ok;
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(autoCommitCu);
        }
    }

    /** Mã hồ sơ bệnh án của bệnh nhân, null nếu chưa có. */
    public String layMaHoSo(String maBenhNhan) throws SQLException {
        try (PreparedStatement ps = ConnectDB.getConnection()
                .prepareStatement("SELECT MaHoSo FROM HoSoBenhAn WHERE MaBenhNhan = ?")) {
            ps.setString(1, maBenhNhan);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    static String sinhMa(Connection con, String bang, String cot, String tienTo) throws SQLException {
        String sql = "SELECT COALESCE(MAX(TRY_CONVERT(int, SUBSTRING(" + cot + ", 3, 18))), 0) + 1 FROM "
                + bang + " WHERE " + cot + " LIKE ? AND TRY_CONVERT(int, SUBSTRING(" + cot + ", 3, 18)) IS NOT NULL";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tienTo + "%");
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return String.format("%s%03d", tienTo, rs.getInt(1));
            }
        }
    }

    private static BenhNhan doc(ResultSet rs) throws SQLException {
        Date ns = rs.getDate("NgaySinh");
        return new BenhNhan(rs.getString("MaBenhNhan"), rs.getString("HoTen"),
                ns == null ? null : ns.toLocalDate(), rs.getString("GioiTinh"), rs.getString("SoDienThoai"),
                rs.getString("DiaChi"), rs.getString("SoCCCD"), rs.getString("TienSuBenhLy"),
                rs.getString("ThongTinDiUng"), rs.getString("NguoiBaoHo"));
    }

    public List<BenhNhan> timKiem(String keyword) throws SQLException {
        List<BenhNhan> ds = new ArrayList<>();
        String sql = SELECT + "WHERE MaBenhNhan LIKE ? OR HoTen LIKE ? OR SoDienThoai LIKE ? OR SoCCCD LIKE ? "
                + "ORDER BY MaBenhNhan";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            String p = "%" + (keyword == null ? "" : keyword.trim()) + "%";
            ps.setString(1, p);
            ps.setString(2, p);
            ps.setString(3, p);
            ps.setString(4, p);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(doc(rs));
                }
            }
        }
        return ds;
    }
}
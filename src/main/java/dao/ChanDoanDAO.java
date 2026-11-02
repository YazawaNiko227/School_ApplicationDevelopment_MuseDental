package dao;

import connectDB.ConnectDB;
import entity.ChanDoan;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO bảng ChanDoan + nghiệp vụ tạo nháp / chốt chẩn đoán (transaction với
 * LichHen, PhongKham).
 */
public class ChanDoanDAO {
    private static final String SELECT = "SELECT MaChanDoan, NgayKham, LyDoKham, TrieuChung, KetLuanChanDoan, "
            + "TrangThai, MaLichHen, MaHoSo, MaNhaSi FROM ChanDoan ";

    public List<ChanDoan> layTatCa() throws SQLException {
        List<ChanDoan> ds = new ArrayList<>();
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(SELECT + "ORDER BY NgayKham DESC");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                ds.add(doc(rs));
        }
        return ds;
    }

    public ChanDoan timTheoMa(String ma) throws SQLException {
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(SELECT + "WHERE MaChanDoan = ?")) {
            ps.setString(1, ma);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? doc(rs) : null;
            }
        }
    }

    public List<ChanDoan> layTheoHoSo(String maHoSo) throws SQLException {
        List<ChanDoan> ds = new ArrayList<>();
        try (PreparedStatement ps = ConnectDB.getConnection()
                .prepareStatement(SELECT + "WHERE MaHoSo = ? ORDER BY NgayKham DESC")) {
            ps.setString(1, maHoSo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    ds.add(doc(rs));
            }
        }
        return ds;
    }

    /**
     * Các chẩn đoán đang khám (TrangThai = 'Nhap') – dùng cho màn Kê đơn, Phác đồ.
     */
    public List<ChanDoan> layDangKham() throws SQLException {
        List<ChanDoan> ds = new ArrayList<>();
        try (PreparedStatement ps = ConnectDB.getConnection()
                .prepareStatement(SELECT + "WHERE TrangThai = N'Nhap' ORDER BY NgayKham DESC");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                ds.add(doc(rs));
        }
        return ds;
    }

    public String sinhMa() throws SQLException {
        return BenhNhanDAO.sinhMa(ConnectDB.getConnection(), "ChanDoan", "MaChanDoan", "CD");
    }

    /**
     * Bác sĩ bấm "Vào khám": tạo chẩn đoán nháp + chuyển lịch hẹn & phòng sang
     * 'Dang kham'.
     * Transaction đảm bảo cả 3 bảng cập nhật cùng lúc.
     */
    public ChanDoan taoNhap(String maChanDoan, LocalDate ngayKham, String maLichHen,
            String maHoSo, String maNhaSi, String maPhong) throws SQLException {
        Connection con = ConnectDB.getConnection();
        boolean autoCommitCu = con.getAutoCommit();
        try {
            con.setAutoCommit(false);
            String sql = "INSERT INTO ChanDoan (MaChanDoan, NgayKham, TrangThai, MaLichHen, MaHoSo, MaNhaSi) "
                    + "VALUES (?, ?, N'Nhap', ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maChanDoan);
                ps.setDate(2, Date.valueOf(ngayKham));
                ps.setString(3, maLichHen);
                ps.setString(4, maHoSo);
                ps.setString(5, maNhaSi);
                if (ps.executeUpdate() != 1) {
                    throw new SQLException("Không tạo được bản ghi chẩn đoán nháp.");
                }
            }
            if (maLichHen != null) {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE LichHen SET TrangThai = N'Dang kham' "
                                + "WHERE MaLichHen = ? AND TrangThai = N'Da check-in'")) {
                    ps.setString(1, maLichHen);
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Không cập nhật được trạng thái lịch hẹn.");
                    }
                }
            }
            if (maPhong != null) {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE PhongKham SET TrangThai = N'Dang kham' "
                                + "WHERE MaPhong = ? AND TrangThai = N'San sang'")) {
                    ps.setString(1, maPhong);
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Không cập nhật được trạng thái phòng khám.");
                    }
                }
            }
            ChanDoan taoMoi = timTheoMa(maChanDoan);
            if (taoMoi == null) {
                throw new SQLException("Không đọc lại được chẩn đoán nháp vừa tạo.");
            }
            con.commit();
            return taoMoi;
        } catch (SQLException | RuntimeException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(autoCommitCu);
        }
    }

    /** Lưu nháp (TrangThai vẫn là 'Nhap'). */
    public boolean capNhatNhap(ChanDoan cd) throws SQLException {
        String sql = "UPDATE ChanDoan SET LyDoKham=?, TrieuChung=?, KetLuanChanDoan=? "
                + "WHERE MaChanDoan=? AND TrangThai=N'Nhap'";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            ps.setString(1, cd.getLyDoKham());
            ps.setString(2, cd.getTrieuChung());
            ps.setString(3, cd.getKetLuanChanDoan());
            ps.setString(4, cd.getMaChanDoan());
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Bác sĩ bấm "Hoàn tất khám": chốt chẩn đoán + lịch hẹn 'Hoan thanh'
     * + phòng 'Can ve sinh'. Transaction 3 bảng.
     */
    public boolean chot(ChanDoan cd, String maLichHen, String maPhong) throws SQLException {
        Connection con = ConnectDB.getConnection();
        boolean autoCommitCu = con.getAutoCommit();
        try {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE ChanDoan SET LyDoKham=?, TrieuChung=?, KetLuanChanDoan=?, TrangThai=N'Da chot' "
                            + "WHERE MaChanDoan=? AND TrangThai=N'Nhap'")) {
                ps.setString(1, cd.getLyDoKham());
                ps.setString(2, cd.getTrieuChung());
                ps.setString(3, cd.getKetLuanChanDoan());
                ps.setString(4, cd.getMaChanDoan());
                if (ps.executeUpdate() != 1) {
                    throw new SQLException("Không tìm thấy chẩn đoán nháp để chốt.");
                }
            }
            if (maLichHen != null) {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE LichHen SET TrangThai = N'Hoan thanh' "
                                + "WHERE MaLichHen = ? AND TrangThai = N'Dang kham'")) {
                    ps.setString(1, maLichHen);
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Không cập nhật được trạng thái lịch hẹn.");
                    }
                }
            }
            if (maPhong != null) {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE PhongKham SET TrangThai = N'Can ve sinh' "
                                + "WHERE MaPhong = ? AND TrangThai = N'Dang kham'")) {
                    ps.setString(1, maPhong);
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Không cập nhật được trạng thái phòng khám.");
                    }
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

    private static ChanDoan doc(ResultSet rs) throws SQLException {
        return new ChanDoan(rs.getString("MaChanDoan"), rs.getDate("NgayKham").toLocalDate(),
                rs.getString("LyDoKham"), rs.getString("TrieuChung"), rs.getString("KetLuanChanDoan"),
                rs.getString("TrangThai"), rs.getString("MaLichHen"), rs.getString("MaHoSo"),
                rs.getString("MaNhaSi"));
    }
}
package dao;

import connectDB.ConnectDB;
import dto.LanKham;
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

    public ChanDoan timTheoLichHen(String maLichHen) throws SQLException {
        try (PreparedStatement ps = ConnectDB.getConnection()
                .prepareStatement(SELECT + "WHERE MaLichHen = ?")) {
            ps.setString(1, maLichHen);
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

    /** Lần khám đang mở kèm thông tin bệnh nhân, dùng cho kê đơn và lập phác đồ. */
    public List<LanKham> layThongTinDangKham() throws SQLException {
        String sql = "SELECT cd.MaChanDoan, bn.MaBenhNhan, bn.HoTen, "
                + "cd.KetLuanChanDoan, bn.ThongTinDiUng "
                + "FROM ChanDoan cd "
                + "INNER JOIN HoSoBenhAn hs ON hs.MaHoSo = cd.MaHoSo "
                + "INNER JOIN BenhNhan bn ON bn.MaBenhNhan = hs.MaBenhNhan "
                + "WHERE cd.TrangThai = N'Nhap' "
                + "ORDER BY cd.NgayKham DESC, cd.MaChanDoan DESC";
        List<LanKham> danhSach = new ArrayList<>();
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                danhSach.add(new LanKham(
                        rs.getString("MaChanDoan"),
                        rs.getString("MaBenhNhan"),
                        rs.getString("HoTen"),
                        rs.getString("KetLuanChanDoan"),
                        rs.getString("ThongTinDiUng")));
            }
        }
        return danhSach;
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
            String maHoSoTrongLich;
            String maNhaSiTrongLich;
            String maPhongTrongLich;
            String trangThaiLich;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT lh.TrangThai, lh.MaNhaSiPhuTrach, lh.MaPhong, hs.MaHoSo "
                            + "FROM LichHen lh WITH (UPDLOCK, HOLDLOCK) "
                            + "LEFT JOIN HoSoBenhAn hs ON hs.MaBenhNhan = lh.MaBenhNhan "
                            + "WHERE lh.MaLichHen = ?")) {
                ps.setString(1, maLichHen);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Không tìm thấy lịch hẹn cần vào khám.");
                    }
                    trangThaiLich = rs.getString("TrangThai");
                    maNhaSiTrongLich = rs.getString("MaNhaSiPhuTrach");
                    maPhongTrongLich = rs.getString("MaPhong");
                    maHoSoTrongLich = rs.getString("MaHoSo");
                }
            }
            if (!"Da check-in".equals(trangThaiLich) && !"Dang kham".equals(trangThaiLich)) {
                throw new SQLException("Lịch hẹn không còn ở trạng thái Đã check-in.");
            }
            if (maHoSoTrongLich == null || !maHoSoTrongLich.equals(maHoSo)) {
                throw new SQLException("Bệnh nhân chưa có hồ sơ bệnh án hợp lệ.");
            }
            if (!maNhaSiTrongLich.equals(maNhaSi) || !maPhongTrongLich.equals(maPhong)) {
                throw new SQLException("Thông tin nha sĩ hoặc phòng của lịch hẹn đã thay đổi. Hãy tải lại hàng chờ.");
            }

            ChanDoan chanDoanDaCo = timTheoLichHenTrongKetNoi(con, maLichHen);
            if (chanDoanDaCo != null && !"Nhap".equals(chanDoanDaCo.getTrangThai())) {
                throw new SQLException("Lịch hẹn này đã có chẩn đoán được chốt.");
            }
            if ("Dang kham".equals(trangThaiLich) && chanDoanDaCo == null) {
                throw new SQLException("Lịch đang ở trạng thái Đang khám nhưng chưa có chẩn đoán nháp.");
            }
            if ("Dang kham".equals(trangThaiLich)) {
                con.commit();
                return chanDoanDaCo;
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT 1 FROM PhongKham WITH (UPDLOCK, HOLDLOCK) "
                            + "WHERE MaPhong = ? AND TrangThai = N'San sang'")) {
                ps.setString(1, maPhong);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Phòng " + maPhong
                                + " không tồn tại hoặc chưa ở trạng thái Sẵn sàng.");
                    }
                }
            }

            if (chanDoanDaCo != null) {
                capNhatTrangThaiVaoKham(con, maLichHen, maPhong);
                con.commit();
                return chanDoanDaCo;
            }

            String sql = "INSERT INTO ChanDoan (MaChanDoan, NgayKham, TrangThai, MaLichHen, MaHoSo, MaNhaSi) "
                    + "VALUES (?, ?, N'Nhap', ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maChanDoan);
                ps.setDate(2, Date.valueOf(ngayKham));
                ps.setString(3, maLichHen);
                ps.setString(4, maHoSo);
                ps.setString(5, maNhaSi);
                ps.executeUpdate();
            }
            capNhatTrangThaiVaoKham(con, maLichHen, maPhong);
            con.commit();
            return new ChanDoan(maChanDoan, ngayKham, null, null, null, "Nhap",
                    maLichHen, maHoSo, maNhaSi);
        } catch (SQLException e) {
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
                    throw new SQLException("Chẩn đoán không còn ở trạng thái nháp.");
                }
            }
            if (maLichHen != null) {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE LichHen SET TrangThai = N'Hoan thanh' "
                                + "WHERE MaLichHen = ? AND TrangThai = N'Dang kham'")) {
                    ps.setString(1, maLichHen);
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Lịch hẹn không còn ở trạng thái Đang khám.");
                    }
                }
            }
            if (maPhong != null) {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE PhongKham SET TrangThai = N'Can ve sinh' "
                                + "WHERE MaPhong = ? AND TrangThai = N'Dang kham'")) {
                    ps.setString(1, maPhong);
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Phòng khám không còn ở trạng thái Đang khám.");
                    }
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

    private ChanDoan timTheoLichHenTrongKetNoi(Connection con, String maLichHen) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SELECT + "WITH (UPDLOCK, HOLDLOCK) WHERE MaLichHen = ?")) {
            ps.setString(1, maLichHen);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? doc(rs) : null;
            }
        }
    }

    private void capNhatTrangThaiVaoKham(Connection con, String maLichHen, String maPhong) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE LichHen SET TrangThai = N'Dang kham' "
                        + "WHERE MaLichHen = ? AND TrangThai = N'Da check-in'")) {
            ps.setString(1, maLichHen);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("Không thể chuyển lịch hẹn sang trạng thái Đang khám.");
            }
        }
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE PhongKham SET TrangThai = N'Dang kham' "
                        + "WHERE MaPhong = ? AND TrangThai = N'San sang'")) {
            ps.setString(1, maPhong);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("Không thể chuyển phòng khám sang trạng thái Đang khám.");
            }
        }
    }

    private static ChanDoan doc(ResultSet rs) throws SQLException {
        return new ChanDoan(rs.getString("MaChanDoan"), rs.getDate("NgayKham").toLocalDate(),
                rs.getString("LyDoKham"), rs.getString("TrieuChung"), rs.getString("KetLuanChanDoan"),
                rs.getString("TrangThai"), rs.getString("MaLichHen"), rs.getString("MaHoSo"),
                rs.getString("MaNhaSi"));
    }
}
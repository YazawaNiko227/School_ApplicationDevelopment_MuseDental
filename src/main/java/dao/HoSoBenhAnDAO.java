package dao;

import connectDB.ConnectDB;
import entity.HoSoBenhAn;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** DAO bảng HoSoBenhAn. Việc tạo hồ sơ khi thêm BN do BenhNhanDAO.them() lo. */
public class HoSoBenhAnDAO {
    private static final String SELECT = "SELECT MaHoSo, NgayTao, MaBenhNhan FROM HoSoBenhAn ";

    public List<HoSoBenhAn> layTatCa() throws SQLException {
        List<HoSoBenhAn> ds = new ArrayList<>();
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(SELECT + "ORDER BY MaHoSo");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                ds.add(doc(rs));
        }
        return ds;
    }

    public HoSoBenhAn timTheoMa(String ma) throws SQLException {
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(SELECT + "WHERE MaHoSo = ?")) {
            ps.setString(1, ma);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? doc(rs) : null;
            }
        }
    }

    public HoSoBenhAn timTheoBenhNhan(String maBenhNhan) throws SQLException {
        try (PreparedStatement ps = ConnectDB.getConnection()
                .prepareStatement(SELECT + "WHERE MaBenhNhan = ?")) {
            ps.setString(1, maBenhNhan);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? doc(rs) : null;
            }
        }
    }

    /** Lấy mã hồ sơ theo mã bệnh nhân, null nếu chưa có. */
    public String layMaHoSo(String maBenhNhan) throws SQLException {
        try (PreparedStatement ps = ConnectDB.getConnection()
                .prepareStatement("SELECT MaHoSo FROM HoSoBenhAn WHERE MaBenhNhan = ?")) {
            ps.setString(1, maBenhNhan);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    private static HoSoBenhAn doc(ResultSet rs) throws SQLException {
        LocalDate ngayTao = rs.getDate("NgayTao") == null ? null : rs.getDate("NgayTao").toLocalDate();
        return new HoSoBenhAn(rs.getString("MaHoSo"), ngayTao, rs.getString("MaBenhNhan"));
    }
}
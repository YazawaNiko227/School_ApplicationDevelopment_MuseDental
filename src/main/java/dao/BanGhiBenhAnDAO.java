package dao;

import connectDB.ConnectDB;
import entity.BanGhiBenhAn;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO view BanGhiBenhAn – tổng hợp từ ChanDoan + HoSoBenhAn + BenhNhan +
 * NhanVien.
 */
public class BanGhiBenhAnDAO {
    private static final String BASE_SQL = "SELECT cd.MaChanDoan, cd.MaHoSo, hs.MaBenhNhan, bn.HoTen AS TenBenhNhan, "
            + "cd.NgayKham, cd.MaNhaSi, nv.HoTen AS TenNhaSi, "
            + "cd.LyDoKham, cd.TrieuChung, cd.KetLuanChanDoan "
            + "FROM ChanDoan cd "
            + "JOIN HoSoBenhAn hs ON cd.MaHoSo = hs.MaHoSo "
            + "JOIN BenhNhan bn ON hs.MaBenhNhan = bn.MaBenhNhan "
            + "JOIN NhanVien nv ON cd.MaNhaSi = nv.MaNhanVien ";

    public List<BanGhiBenhAn> layTatCa() throws SQLException {
        List<BanGhiBenhAn> ds = new ArrayList<>();
        try (PreparedStatement ps = ConnectDB.getConnection()
                .prepareStatement(BASE_SQL + "ORDER BY cd.NgayKham DESC");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                ds.add(doc(rs));
        }
        return ds;
    }

    /** Tìm kiếm theo từ khóa, nha sĩ, khoảng ngày (null = bỏ qua điều kiện đó). */
    public List<BanGhiBenhAn> timKiem(String keyword, String tenNhaSi,
            LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SQL).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (cd.MaChanDoan LIKE ? OR hs.MaHoSo LIKE ? OR bn.HoTen LIKE ? "
                    + "OR cd.LyDoKham LIKE ? OR cd.KetLuanChanDoan LIKE ?) ");
            String p = "%" + keyword.trim() + "%";
            for (int i = 0; i < 5; i++)
                params.add(p);
        }
        if (tenNhaSi != null && !tenNhaSi.trim().isEmpty()) {
            sql.append("AND nv.HoTen = ? ");
            params.add(tenNhaSi);
        }
        if (tuNgay != null) {
            sql.append("AND cd.NgayKham >= ? ");
            params.add(Date.valueOf(tuNgay));
        }
        if (denNgay != null) {
            sql.append("AND cd.NgayKham <= ? ");
            params.add(Date.valueOf(denNgay));
        }
        sql.append("ORDER BY cd.NgayKham DESC");

        List<BanGhiBenhAn> ds = new ArrayList<>();
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++)
                ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    ds.add(doc(rs));
            }
        }
        return ds;
    }

    private static BanGhiBenhAn doc(ResultSet rs) throws SQLException {
        return new BanGhiBenhAn(rs.getString("MaHoSo"), rs.getString("MaChanDoan"),
                rs.getString("MaBenhNhan"), rs.getString("TenBenhNhan"),
                rs.getDate("NgayKham").toLocalDate(), rs.getString("MaNhaSi"),
                rs.getString("TenNhaSi"), rs.getString("LyDoKham"),
                rs.getString("TrieuChung"), rs.getString("KetLuanChanDoan"));
    }
}
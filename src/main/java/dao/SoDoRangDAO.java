package dao;

import connectDB.ConnectDB;
import entity.SoDoRang;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** DAO bảng SoDoRang (khóa chính phức hợp MaHoSo + MaRang). */
public class SoDoRangDAO {
    private static final String SELECT = "SELECT MaHoSo, MaRang, LoaiRang, TinhTrangBenhLy, GhiChu FROM SoDoRang ";

    public List<SoDoRang> layTheoHoSo(String maHoSo) throws SQLException {
        List<SoDoRang> ds = new ArrayList<>();
        try (PreparedStatement ps = ConnectDB.getConnection()
                .prepareStatement(SELECT + "WHERE MaHoSo = ? ORDER BY MaRang")) {
            ps.setString(1, maHoSo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    ds.add(doc(rs));
            }
        }
        return ds;
    }

    /** Lưu hoặc cập nhật 1 răng (UPSERT bằng MERGE). */
    public boolean luuHoacCapNhat(SoDoRang r) throws SQLException {
        String sql = "MERGE INTO SoDoRang AS t "
                + "USING (SELECT ? AS MaHoSo, ? AS MaRang) AS s "
                + "ON t.MaHoSo = s.MaHoSo AND t.MaRang = s.MaRang "
                + "WHEN MATCHED THEN UPDATE SET LoaiRang=?, TinhTrangBenhLy=?, GhiChu=? "
                + "WHEN NOT MATCHED THEN INSERT (MaHoSo, MaRang, LoaiRang, TinhTrangBenhLy, GhiChu) "
                + "VALUES (?,?,?,?,?);";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            ps.setString(1, r.getMaHoSo());
            ps.setInt(2, r.getMaRang());
            ps.setString(3, r.getLoaiRang());
            ps.setString(4, r.getTinhTrangBenhLy());
            ps.setString(5, r.getGhiChu());
            ps.setString(6, r.getMaHoSo());
            ps.setInt(7, r.getMaRang());
            ps.setString(8, r.getLoaiRang());
            ps.setString(9, r.getTinhTrangBenhLy());
            ps.setString(10, r.getGhiChu());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean xoa(String maHoSo, int maRang) throws SQLException {
        try (PreparedStatement ps = ConnectDB.getConnection()
                .prepareStatement("DELETE FROM SoDoRang WHERE MaHoSo = ? AND MaRang = ?")) {
            ps.setString(1, maHoSo);
            ps.setInt(2, maRang);
            return ps.executeUpdate() == 1;
        }
    }

    private static SoDoRang doc(ResultSet rs) throws SQLException {
        return new SoDoRang(rs.getString("MaHoSo"), rs.getInt("MaRang"), rs.getString("LoaiRang"),
                rs.getString("TinhTrangBenhLy"), rs.getString("GhiChu"));
    }
}
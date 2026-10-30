package dao;

import connectDB.ConnectDB;
import entity.PhongKham;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PhongKhamDAO {
    private static final DateTimeFormatter GIO_HEN_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public List<PhongKham> layTatCa() throws SQLException {
        String sql = "SELECT MaPhong, TenPhong, TrangThai FROM PhongKham ORDER BY MaPhong";
        List<PhongKham> danhSach = new ArrayList<>();
        try (PreparedStatement statement = getConnection().prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                danhSach.add(new PhongKham(
                        result.getString("MaPhong"),
                        result.getString("TenPhong"),
                        result.getString("TrangThai")));
            }
        }
        return danhSach;
    }

    public Map<String, String> layLichHenKeTiep() throws SQLException {
        String sql = "WITH LichHenSapToi AS ("
                + "SELECT lh.MaPhong, lh.ThoiGianHen, bn.HoTen, "
                + "ROW_NUMBER() OVER (PARTITION BY lh.MaPhong "
                + "ORDER BY lh.ThoiGianHen, lh.MaLichHen) AS ThuTu "
                + "FROM LichHen lh "
                + "INNER JOIN BenhNhan bn ON bn.MaBenhNhan = lh.MaBenhNhan "
                + "WHERE lh.ThoiGianHen >= SYSDATETIME() "
                + "AND lh.TrangThai IN (N'Cho xac nhan', N'Da xac nhan')) "
                + "SELECT MaPhong, ThoiGianHen, HoTen FROM LichHenSapToi WHERE ThuTu = 1";
        Map<String, String> lichHenTheoPhong = new LinkedHashMap<>();
        try (PreparedStatement statement = getConnection().prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                Timestamp thoiGianHen = result.getTimestamp("ThoiGianHen");
                lichHenTheoPhong.put(result.getString("MaPhong"),
                        thoiGianHen.toLocalDateTime().format(GIO_HEN_FORMAT) + " - " + result.getString("HoTen"));
            }
        }
        return lichHenTheoPhong;
    }

    public boolean capNhatTrangThai(String maPhong, String trangThai) throws SQLException {
        String sql = "UPDATE PhongKham SET TrangThai = ? WHERE MaPhong = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, trangThai);
            statement.setString(2, maPhong);
            return statement.executeUpdate() == 1;
        }
    }

    private Connection getConnection() throws SQLException {
        Connection connection = ConnectDB.getConnection();
        if (connection == null || connection.isClosed()) {
            throw new SQLException("Chưa có kết nối cơ sở dữ liệu.");
        }
        return connection;
    }
}

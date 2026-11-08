package dao;

import connectDB.ConnectDB;
import entity.BenhNhan;
import entity.LichHen;
import entity.NhanVien;
import entity.PhongKham;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DieuPhoiPhongKhamDAO {
    public List<LichHen> layLichHomNay() throws SQLException {
        return layLichTheoNgay(LocalDate.now());
    }

    public List<LichHen> layLichTheoNgay(LocalDate ngay) throws SQLException {
        if (ngay == null) {
            throw new IllegalArgumentException("Ngày điều phối không được để trống.");
        }
        String sql = "SELECT lh.MaLichHen, lh.ThoiGianHen, lh.TrangThai, lh.NgayTao, lh.ThoiGianCheckIn, "
                + "lh.MaNhanVienLap, lh.MaNhaSiPhuTrach, ns.HoTen AS TenNhaSi, "
                + "lh.MaBenhNhan, bn.HoTen AS TenBenhNhan, lh.MaPhong, lh.MaKeHoach "
                + "FROM LichHen lh "
                + "INNER JOIN BenhNhan bn ON bn.MaBenhNhan = lh.MaBenhNhan "
                + "INNER JOIN NhanVien ns ON ns.MaNhanVien = lh.MaNhaSiPhuTrach "
                + "WHERE lh.ThoiGianHen >= ? AND lh.ThoiGianHen < ? "
                + "ORDER BY lh.ThoiGianHen, lh.MaLichHen";
        List<LichHen> danhSach = new ArrayList<>();
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(ngay.atStartOfDay()));
            statement.setTimestamp(2, Timestamp.valueOf(ngay.plusDays(1).atStartOfDay()));
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    BenhNhan benhNhan = new BenhNhan();
                    benhNhan.setMaBenhNhan(result.getString("MaBenhNhan"));
                    benhNhan.setHoTen(result.getString("TenBenhNhan"));

                    NhanVien nhaSi = new NhanVien();
                    nhaSi.setMaNhanVien(result.getString("MaNhaSiPhuTrach"));
                    nhaSi.setHoTen(result.getString("TenNhaSi"));

                    PhongKham phongKham = new PhongKham();
                    phongKham.setMaPhong(result.getString("MaPhong"));

                    LichHen lichHen = new LichHen();
                    lichHen.setMaLichHen(result.getString("MaLichHen"));
                    lichHen.setThoiGianHen(result.getTimestamp("ThoiGianHen").toLocalDateTime());
                    lichHen.setTrangThai(result.getString("TrangThai"));
                    lichHen.setNgayTao(result.getDate("NgayTao").toLocalDate());
                    Timestamp thoiGianCheckIn = result.getTimestamp("ThoiGianCheckIn");
                    lichHen.setThoiGianCheckIn(
                            thoiGianCheckIn == null ? null : thoiGianCheckIn.toLocalDateTime());
                    lichHen.setMaNhanVienLap(result.getString("MaNhanVienLap"));
                    lichHen.setNhaSiPhuTrach(nhaSi);
                    lichHen.setBenhNhan(benhNhan);
                    lichHen.setPhongKham(phongKham);
                    lichHen.setMaKeHoach(result.getString("MaKeHoach"));
                    danhSach.add(lichHen);
                }
            }
        }
        return danhSach;
    }

    public List<PhongKham> layTatCaPhong() throws SQLException {
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

    public boolean checkIn(String maLichHen) throws SQLException {
        String sql = "UPDATE LichHen SET TrangThai = N'Da check-in', ThoiGianCheckIn = SYSDATETIME() "
                + "WHERE MaLichHen = ? AND TrangThai = N'Da xac nhan'";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, maLichHen);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean doiPhong(String maLichHen, String maPhongMoi) throws SQLException {
        Connection connection = getConnection();
        boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            LocalDateTime thoiGianHen = layThoiGianLichDaCheckIn(connection, maLichHen);
            kiemTraPhongSanSang(connection, maPhongMoi);
            kiemTraTrungPhong(connection, maLichHen, maPhongMoi, thoiGianHen);

            String sql = "UPDATE LichHen SET MaPhong = ? "
                    + "WHERE MaLichHen = ? AND TrangThai = N'Da check-in'";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, maPhongMoi);
                statement.setString(2, maLichHen);
                if (statement.executeUpdate() != 1) {
                    throw new SQLException("Lịch hẹn không còn ở trạng thái Đã check-in.");
                }
            }
            connection.commit();
            return true;
        } catch (SQLException | RuntimeException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(autoCommit);
        }
    }

    public boolean danhDauVang(String maLichHen, String trangThaiHienTai) throws SQLException {
        String sql = "UPDATE LichHen SET TrangThai = N'Vang' "
                + "WHERE MaLichHen = ? AND TrangThai = ? "
                + "AND ? IN (N'Cho xac nhan', N'Da xac nhan')";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, maLichHen);
            statement.setString(2, trangThaiHienTai);
            statement.setString(3, trangThaiHienTai);
            return statement.executeUpdate() == 1;
        }
    }

    private LocalDateTime layThoiGianLichDaCheckIn(Connection connection, String maLichHen)
            throws SQLException {
        String sql = "SELECT ThoiGianHen FROM LichHen WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE MaLichHen = ? AND TrangThai = N'Da check-in'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maLichHen);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("Lịch hẹn không còn ở trạng thái Đã check-in.");
                }
                return result.getTimestamp("ThoiGianHen").toLocalDateTime();
            }
        }
    }

    private void kiemTraPhongSanSang(Connection connection, String maPhong) throws SQLException {
        String sql = "SELECT 1 FROM PhongKham WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE MaPhong = ? AND TrangThai = N'San sang'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maPhong);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("Phòng được chọn không tồn tại hoặc không ở trạng thái Sẵn sàng.");
                }
            }
        }
    }

    private void kiemTraTrungPhong(Connection connection, String maLichHen, String maPhong,
            LocalDateTime thoiGianHen) throws SQLException {
        String sql = "SELECT TOP 1 1 FROM LichHen WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE MaPhong = ? AND ThoiGianHen = ? AND MaLichHen <> ? "
                + "AND TrangThai IN (N'Cho xac nhan', N'Da xac nhan', N'Da check-in', N'Dang kham')";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maPhong);
            statement.setTimestamp(2, Timestamp.valueOf(thoiGianHen));
            statement.setString(3, maLichHen);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    throw new SQLException("Phòng đã được xếp cho lịch hẹn khác cùng giờ.");
                }
            }
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

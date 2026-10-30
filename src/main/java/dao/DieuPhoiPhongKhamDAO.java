package dao;

import connectDB.ConnectDB;

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
    public static final class LichHenDieuPhoi {
        private final String maLichHen;
        private final LocalDateTime thoiGianHen;
        private final String tenBenhNhan;
        private final String nhaSi;
        private final String maPhong;
        private final String trangThai;
        private final LocalDateTime thoiGianCheckIn;

        private LichHenDieuPhoi(String maLichHen, LocalDateTime thoiGianHen, String tenBenhNhan,
                String nhaSi, String maPhong, String trangThai, LocalDateTime thoiGianCheckIn) {
            this.maLichHen = maLichHen;
            this.thoiGianHen = thoiGianHen;
            this.tenBenhNhan = tenBenhNhan;
            this.nhaSi = nhaSi;
            this.maPhong = maPhong;
            this.trangThai = trangThai;
            this.thoiGianCheckIn = thoiGianCheckIn;
        }

        public String getMaLichHen() {
            return maLichHen;
        }

        public LocalDateTime getThoiGianHen() {
            return thoiGianHen;
        }

        public String getTenBenhNhan() {
            return tenBenhNhan;
        }

        public String getNhaSi() {
            return nhaSi;
        }

        public String getMaPhong() {
            return maPhong;
        }

        public String getTrangThai() {
            return trangThai;
        }

        public LocalDateTime getThoiGianCheckIn() {
            return thoiGianCheckIn;
        }
    }

    public static final class PhongDieuPhoi {
        private final String maPhong;
        private final String tenPhong;
        private final String trangThai;

        private PhongDieuPhoi(String maPhong, String tenPhong, String trangThai) {
            this.maPhong = maPhong;
            this.tenPhong = tenPhong;
            this.trangThai = trangThai;
        }

        public String getMaPhong() {
            return maPhong;
        }

        public String getTenPhong() {
            return tenPhong;
        }

        public String getTrangThai() {
            return trangThai;
        }
    }

    public List<LichHenDieuPhoi> layLichHomNay() throws SQLException {
        String sql = "SELECT lh.MaLichHen, lh.ThoiGianHen, bn.HoTen AS TenBenhNhan, "
                + "ns.MaNhanVien, ns.HoTen AS TenNhaSi, lh.MaPhong, lh.TrangThai, lh.ThoiGianCheckIn "
                + "FROM LichHen lh "
                + "INNER JOIN BenhNhan bn ON bn.MaBenhNhan = lh.MaBenhNhan "
                + "INNER JOIN NhanVien ns ON ns.MaNhanVien = lh.MaNhaSiPhuTrach "
                + "WHERE lh.ThoiGianHen >= ? AND lh.ThoiGianHen < ? "
                + "ORDER BY lh.ThoiGianHen, lh.MaLichHen";
        LocalDate homNay = LocalDate.now();
        List<LichHenDieuPhoi> danhSach = new ArrayList<>();
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(homNay.atStartOfDay()));
            statement.setTimestamp(2, Timestamp.valueOf(homNay.plusDays(1).atStartOfDay()));
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Timestamp thoiGianHen = result.getTimestamp("ThoiGianHen");
                    Timestamp thoiGianCheckIn = result.getTimestamp("ThoiGianCheckIn");
                    danhSach.add(new LichHenDieuPhoi(
                            result.getString("MaLichHen"),
                            thoiGianHen.toLocalDateTime(),
                            result.getString("TenBenhNhan"),
                            result.getString("MaNhanVien") + " - " + result.getString("TenNhaSi"),
                            result.getString("MaPhong"),
                            result.getString("TrangThai"),
                            thoiGianCheckIn == null ? null : thoiGianCheckIn.toLocalDateTime()));
                }
            }
        }
        return danhSach;
    }

    public List<PhongDieuPhoi> layTatCaPhong() throws SQLException {
        String sql = "SELECT MaPhong, TenPhong, TrangThai FROM PhongKham ORDER BY MaPhong";
        List<PhongDieuPhoi> danhSach = new ArrayList<>();
        try (PreparedStatement statement = getConnection().prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                danhSach.add(new PhongDieuPhoi(
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

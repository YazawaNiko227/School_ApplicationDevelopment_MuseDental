package dao;

import connectDB.ConnectDB;
import entity.BenhNhan;
import entity.NhanVien;
import entity.PhongKham;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatLichHenDAO {
    public static final class LichHenThongTin {
        private final String maLichHen;
        private final LocalDateTime thoiGianHen;
        private final String maBenhNhan;
        private final String tenBenhNhan;
        private final String tenNhaSi;
        private final String tenPhong;
        private final String trangThai;

        private LichHenThongTin(String maLichHen, LocalDateTime thoiGianHen, String maBenhNhan,
                String tenBenhNhan, String tenNhaSi, String tenPhong, String trangThai) {
            this.maLichHen = maLichHen;
            this.thoiGianHen = thoiGianHen;
            this.maBenhNhan = maBenhNhan;
            this.tenBenhNhan = tenBenhNhan;
            this.tenNhaSi = tenNhaSi;
            this.tenPhong = tenPhong;
            this.trangThai = trangThai;
        }

        public String getMaLichHen() {
            return maLichHen;
        }

        public LocalDateTime getThoiGianHen() {
            return thoiGianHen;
        }

        public String getMaBenhNhan() {
            return maBenhNhan;
        }

        public String getTenBenhNhan() {
            return tenBenhNhan;
        }

        public String getTenNhaSi() {
            return tenNhaSi;
        }

        public String getTenPhong() {
            return tenPhong;
        }

        public String getTrangThai() {
            return trangThai;
        }
    }

    public List<BenhNhan> timBenhNhan(String tuKhoa) throws SQLException {
        String sql = "SELECT TOP 50 MaBenhNhan, HoTen, NgaySinh, GioiTinh, SoDienThoai, DiaChi "
                + "FROM BenhNhan WHERE MaBenhNhan LIKE ? OR HoTen LIKE ? OR SoDienThoai LIKE ? "
                + "ORDER BY CASE WHEN MaBenhNhan = ? THEN 0 "
                + "WHEN SoDienThoai = ? THEN 1 WHEN HoTen = ? THEN 2 ELSE 3 END, HoTen";
        String pattern = "%" + tuKhoa.trim() + "%";
        List<BenhNhan> danhSach = new ArrayList<>();
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            statement.setString(4, tuKhoa.trim());
            statement.setString(5, tuKhoa.trim());
            statement.setString(6, tuKhoa.trim());
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Date ngaySinh = result.getDate("NgaySinh");
                    BenhNhan benhNhan = new BenhNhan();
                    benhNhan.setMaBenhNhan(result.getString("MaBenhNhan"));
                    benhNhan.setHoTen(result.getString("HoTen"));
                    benhNhan.setNgaySinh(ngaySinh == null ? null : ngaySinh.toLocalDate());
                    benhNhan.setGioiTinh(result.getString("GioiTinh"));
                    benhNhan.setSoDienThoai(result.getString("SoDienThoai"));
                    benhNhan.setDiaChi(result.getString("DiaChi"));
                    danhSach.add(benhNhan);
                }
            }
        }
        return danhSach;
    }

    public List<NhanVien> layNhaSiDangLamViec() throws SQLException {
        String sql = "SELECT MaNhanVien, HoTen FROM NhanVien "
                + "WHERE TrangThaiLamViec = N'Dang lam viec' AND ChuyenMon LIKE N'Nha si%' "
                + "ORDER BY HoTen";
        List<NhanVien> danhSach = new ArrayList<>();
        try (PreparedStatement statement = getConnection().prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                NhanVien nhaSi = new NhanVien();
                nhaSi.setMaNhanVien(result.getString("MaNhanVien"));
                nhaSi.setHoTen(result.getString("HoTen"));
                danhSach.add(nhaSi);
            }
        }
        return danhSach;
    }

    public List<PhongKham> layPhongKhongBaoTri() throws SQLException {
        String sql = "SELECT MaPhong, TenPhong, TrangThai FROM PhongKham "
                + "WHERE TrangThai <> N'Bao tri' ORDER BY MaPhong";
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

    public List<LichHenThongTin> layDanhSachLichHen() throws SQLException {
        String sql = "SELECT lh.MaLichHen, lh.ThoiGianHen, lh.MaBenhNhan, bn.HoTen AS TenBenhNhan, "
                + "ns.HoTen AS TenNhaSi, pk.TenPhong, lh.TrangThai "
                + "FROM LichHen lh "
                + "INNER JOIN BenhNhan bn ON bn.MaBenhNhan = lh.MaBenhNhan "
                + "INNER JOIN NhanVien ns ON ns.MaNhanVien = lh.MaNhaSiPhuTrach "
                + "INNER JOIN PhongKham pk ON pk.MaPhong = lh.MaPhong "
                + "ORDER BY lh.ThoiGianHen DESC, lh.MaLichHen DESC";
        List<LichHenThongTin> danhSach = new ArrayList<>();
        try (PreparedStatement statement = getConnection().prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                Timestamp thoiGianHen = result.getTimestamp("ThoiGianHen");
                danhSach.add(new LichHenThongTin(
                        result.getString("MaLichHen"),
                        thoiGianHen.toLocalDateTime(),
                        result.getString("MaBenhNhan"),
                        result.getString("TenBenhNhan"),
                        result.getString("TenNhaSi"),
                        result.getString("TenPhong"),
                        result.getString("TrangThai")));
            }
        }
        return danhSach;
    }

    public String themLichHen(BenhNhan benhNhanMoi, String maBenhNhan, LocalDateTime thoiGianHen,
            String maNhaSi, String maPhong, String tenDangNhap) throws SQLException {
        Connection connection = getConnection();
        boolean autoCommit = connection.getAutoCommit();
        int isolation = connection.getTransactionIsolation();
        connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        connection.setAutoCommit(false);
        try {
            String maNhanVienLap = layMaNhanVienLap(connection, tenDangNhap);
            String maBenhNhanDatLich = maBenhNhan;
            if (benhNhanMoi != null) {
                maBenhNhanDatLich = taoMa(connection, "BenhNhan", "MaBenhNhan", "BN");
                benhNhanMoi.setMaBenhNhan(maBenhNhanDatLich);
                themBenhNhan(connection, benhNhanMoi);
                taoHoSoBenhAn(connection, maBenhNhanDatLich);
            }

            kiemTraPhong(connection, maPhong);
            kiemTraCaTruc(connection, maNhaSi, thoiGianHen);
            kiemTraLichTrung(connection, maBenhNhanDatLich, maNhaSi, maPhong, thoiGianHen);

            String maLichHen = taoMa(connection, "LichHen", "MaLichHen", "LH");
            String sql = "INSERT INTO LichHen "
                    + "(MaLichHen, ThoiGianHen, TrangThai, MaNhanVienLap, MaNhaSiPhuTrach, "
                    + "MaBenhNhan, MaPhong) VALUES (?, ?, N'Cho xac nhan', ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, maLichHen);
                statement.setTimestamp(2, Timestamp.valueOf(thoiGianHen));
                statement.setString(3, maNhanVienLap);
                statement.setString(4, maNhaSi);
                statement.setString(5, maBenhNhanDatLich);
                statement.setString(6, maPhong);
                statement.executeUpdate();
            }
            connection.commit();
            return maLichHen;
        } catch (SQLException | RuntimeException ex) {
            connection.rollback();
            throw ex;
        } finally {
            try {
                connection.setAutoCommit(autoCommit);
            } finally {
                connection.setTransactionIsolation(isolation);
            }
        }
    }

    public boolean capNhatTrangThai(String maLichHen, String trangThaiHienTai, String trangThaiMoi)
            throws SQLException {
        String sql = "UPDATE LichHen SET TrangThai = ? WHERE MaLichHen = ? AND TrangThai = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, trangThaiMoi);
            statement.setString(2, maLichHen);
            statement.setString(3, trangThaiHienTai);
            return statement.executeUpdate() == 1;
        }
    }

    private String layMaNhanVienLap(Connection connection, String tenDangNhap) throws SQLException {
        String sql = "SELECT nv.MaNhanVien FROM TaiKhoan tk "
                + "INNER JOIN NhanVien nv ON nv.MaNhanVien = tk.MaNhanVien "
                + "WHERE tk.TenDangNhap = ? AND tk.TrangThaiTaiKhoan = N'Kich hoat'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tenDangNhap);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("Không tìm thấy tài khoản nhân viên đang hoạt động: " + tenDangNhap);
                }
                return result.getString("MaNhanVien");
            }
        }
    }

    private void themBenhNhan(Connection connection, BenhNhan benhNhan) throws SQLException {
        String sql = "INSERT INTO BenhNhan "
                + "(MaBenhNhan, HoTen, NgaySinh, GioiTinh, SoDienThoai, DiaChi) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, benhNhan.getMaBenhNhan());
            statement.setString(2, benhNhan.getHoTen());
            if (benhNhan.getNgaySinh() == null) {
                statement.setNull(3, Types.DATE);
            } else {
                statement.setDate(3, Date.valueOf(benhNhan.getNgaySinh()));
            }
            statement.setString(4, gioiTinhTrongDb(benhNhan.getGioiTinh()));
            statement.setString(5, benhNhan.getSoDienThoai());
            statement.setString(6, benhNhan.getDiaChi());
            statement.executeUpdate();
        }
    }

    private void taoHoSoBenhAn(Connection connection, String maBenhNhan) throws SQLException {
        String maHoSo = taoMa(connection, "HoSoBenhAn", "MaHoSo", "HS");
        String sql = "INSERT INTO HoSoBenhAn (MaHoSo, MaBenhNhan) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maHoSo);
            statement.setString(2, maBenhNhan);
            statement.executeUpdate();
        }
    }

    private String taoMa(Connection connection, String tableName, String columnName, String prefix)
            throws SQLException {
        String sql = "SELECT COALESCE(MAX(TRY_CONVERT(int, SUBSTRING(" + columnName
                + ", 3, 18))), 0) + 1 FROM " + tableName
                + " WITH (TABLOCKX, HOLDLOCK) WHERE " + columnName + " LIKE ? "
                + "AND TRY_CONVERT(int, SUBSTRING(" + columnName + ", 3, 18)) IS NOT NULL";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, prefix + "%");
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return String.format("%s%03d", prefix, result.getInt(1));
            }
        }
    }

    private void kiemTraPhong(Connection connection, String maPhong) throws SQLException {
        String sql = "SELECT TrangThai FROM PhongKham WITH (UPDLOCK, HOLDLOCK) WHERE MaPhong = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maPhong);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("Phòng khám đã chọn không tồn tại.");
                }
                if ("Bao tri".equals(result.getString("TrangThai"))) {
                    throw new SQLException("Phòng khám đang bảo trì, vui lòng chọn phòng khác.");
                }
            }
        }
    }

    private void kiemTraCaTruc(Connection connection, String maNhaSi, LocalDateTime thoiGianHen)
            throws SQLException {
        String sql = "SELECT TOP 1 1 FROM CaTruc ct "
                + "INNER JOIN NhanVien nv ON nv.MaNhanVien = ct.MaNhanVien "
                + "WHERE ct.MaNhanVien = ? AND ct.Ngay = ? "
                + "AND ct.GioBatDau <= CAST(? AS TIME(0)) "
                + "AND ct.GioKetThuc > CAST(? AS TIME(0)) "
                + "AND nv.TrangThaiLamViec = N'Dang lam viec' "
                + "AND nv.ChuyenMon LIKE N'Nha si%'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maNhaSi);
            statement.setDate(2, Date.valueOf(thoiGianHen.toLocalDate()));
            statement.setTime(3, Time.valueOf(thoiGianHen.toLocalTime()));
            statement.setTime(4, Time.valueOf(thoiGianHen.toLocalTime()));
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("Nha sĩ không có ca trực tại thời điểm đã chọn.");
                }
            }
        }
    }

    private void kiemTraLichTrung(Connection connection, String maBenhNhan, String maNhaSi,
            String maPhong, LocalDateTime thoiGianHen) throws SQLException {
        String sql = "SELECT TOP 1 1 FROM LichHen WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE ThoiGianHen = ? "
                + "AND TrangThai IN (N'Cho xac nhan', N'Da xac nhan', N'Da check-in', N'Dang kham') "
                + "AND (MaBenhNhan = ? OR MaNhaSiPhuTrach = ? OR MaPhong = ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(thoiGianHen));
            statement.setString(2, maBenhNhan);
            statement.setString(3, maNhaSi);
            statement.setString(4, maPhong);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    throw new SQLException("Bệnh nhân, nha sĩ hoặc phòng đã có lịch hẹn tại thời điểm này.");
                }
            }
        }
    }

    private String gioiTinhTrongDb(String gioiTinh) {
        if ("Nữ".equals(gioiTinh)) {
            return "Nu";
        }
        return gioiTinh;
    }

    private Connection getConnection() throws SQLException {
        Connection connection = ConnectDB.getConnection();
        if (connection == null || connection.isClosed()) {
            throw new SQLException("Chưa có kết nối cơ sở dữ liệu.");
        }
        return connection;
    }
}

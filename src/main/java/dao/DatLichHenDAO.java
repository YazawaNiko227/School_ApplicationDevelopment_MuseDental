package dao;

import connectDB.ConnectDB;
import entity.BenhNhan;
import entity.LichHen;
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
    public List<BenhNhan> timBenhNhan(String tuKhoa) throws SQLException {
        String sql = "SELECT TOP 50 MaBenhNhan, HoTen, NgaySinh, GioiTinh, SoDienThoai, DiaChi, SoCCCD "
                + "FROM BenhNhan WHERE MaBenhNhan LIKE ? OR HoTen LIKE ? OR SoDienThoai LIKE ? OR SoCCCD LIKE ? "
                + "ORDER BY CASE WHEN MaBenhNhan = ? THEN 0 "
                + "WHEN SoDienThoai = ? THEN 1 WHEN SoCCCD = ? THEN 2 WHEN HoTen = ? THEN 3 ELSE 4 END, HoTen";
        String pattern = "%" + tuKhoa.trim() + "%";
        List<BenhNhan> danhSach = new ArrayList<>();
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            statement.setString(4, pattern);
            statement.setString(5, tuKhoa.trim());
            statement.setString(6, tuKhoa.trim());
            statement.setString(7, tuKhoa.trim());
            statement.setString(8, tuKhoa.trim());
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
                    benhNhan.setSoCCCD(result.getString("SoCCCD"));
                    danhSach.add(benhNhan);
                }
            }
        }
        return danhSach;
    }

    public List<NhanVien> layNhaSiDangLamViec() throws SQLException {
        String sql = "SELECT NV.MaNhanVien, NV.HoTen FROM NhanVien NV "
                + "JOIN ChuyenMon CM ON CM.MaChuyenMon = NV.MaChuyenMon "
                + "WHERE NV.TrangThaiLamViec = N'Dang lam viec' AND CM.TenChuyenMon LIKE N'Nha sĩ%' "
                + "ORDER BY NV.HoTen";
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

    public List<NhanVien> layNhaSiRanh(LocalDateTime thoiGianHen) throws SQLException {
        if (thoiGianHen == null) {
            throw new IllegalArgumentException("Thời gian hẹn không được để trống.");
        }
        String sql = "SELECT DISTINCT NV.MaNhanVien, NV.HoTen FROM NhanVien NV "
                + "JOIN ChuyenMon CM ON CM.MaChuyenMon = NV.MaChuyenMon "
                + "JOIN CaTruc CT ON CT.MaNhanVien = NV.MaNhanVien "
                + "WHERE NV.TrangThaiLamViec = N'Dang lam viec' "
                + "AND CM.TenChuyenMon LIKE N'Nha sĩ%' "
                + "AND CT.Ngay = ? AND CT.GioBatDau <= CAST(? AS TIME(0)) "
                + "AND CT.GioKetThuc > CAST(? AS TIME(0)) "
                + "AND NOT EXISTS (SELECT 1 FROM LichHen LH "
                + "WHERE LH.MaNhaSiPhuTrach = NV.MaNhanVien AND LH.ThoiGianHen = ? "
                + "AND LH.TrangThai IN (N'Cho xac nhan', N'Da xac nhan', N'Da check-in', N'Dang kham')) "
                + "ORDER BY NV.HoTen";
        List<NhanVien> danhSach = new ArrayList<>();
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(thoiGianHen.toLocalDate()));
            statement.setTime(2, Time.valueOf(thoiGianHen.toLocalTime()));
            statement.setTime(3, Time.valueOf(thoiGianHen.toLocalTime()));
            statement.setTimestamp(4, Timestamp.valueOf(thoiGianHen));
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    NhanVien nhaSi = new NhanVien();
                    nhaSi.setMaNhanVien(result.getString("MaNhanVien"));
                    nhaSi.setHoTen(result.getString("HoTen"));
                    danhSach.add(nhaSi);
                }
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

    public List<LichHen> layDanhSachLichHen() throws SQLException {
        String sql = "SELECT lh.MaLichHen, lh.ThoiGianHen, lh.TrangThai, lh.NgayTao, lh.ThoiGianCheckIn, "
                + "lh.MaNhanVienLap, lh.MaNhaSiPhuTrach, ns.HoTen AS TenNhaSi, "
                + "lh.MaBenhNhan, bn.HoTen AS TenBenhNhan, lh.MaPhong, pk.TenPhong, lh.MaKeHoach "
                + "FROM LichHen lh "
                + "INNER JOIN BenhNhan bn ON bn.MaBenhNhan = lh.MaBenhNhan "
                + "INNER JOIN NhanVien ns ON ns.MaNhanVien = lh.MaNhaSiPhuTrach "
                + "INNER JOIN PhongKham pk ON pk.MaPhong = lh.MaPhong "
                + "ORDER BY lh.ThoiGianHen DESC, lh.MaLichHen DESC";
        List<LichHen> danhSach = new ArrayList<>();
        try (PreparedStatement statement = getConnection().prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                Timestamp thoiGianHen = result.getTimestamp("ThoiGianHen");
                Timestamp thoiGianCheckIn = result.getTimestamp("ThoiGianCheckIn");

                BenhNhan benhNhan = new BenhNhan();
                benhNhan.setMaBenhNhan(result.getString("MaBenhNhan"));
                benhNhan.setHoTen(result.getString("TenBenhNhan"));

                NhanVien nhaSi = new NhanVien();
                nhaSi.setMaNhanVien(result.getString("MaNhaSiPhuTrach"));
                nhaSi.setHoTen(result.getString("TenNhaSi"));

                PhongKham phongKham = new PhongKham();
                phongKham.setMaPhong(result.getString("MaPhong"));
                phongKham.setTenPhong(result.getString("TenPhong"));

                LichHen lichHen = new LichHen();
                lichHen.setMaLichHen(result.getString("MaLichHen"));
                lichHen.setThoiGianHen(thoiGianHen.toLocalDateTime());
                lichHen.setTrangThai(result.getString("TrangThai"));
                lichHen.setNgayTao(result.getDate("NgayTao").toLocalDate());
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
                + "AND EXISTS (SELECT 1 FROM ChuyenMon cm "
                + "WHERE cm.MaChuyenMon = nv.MaChuyenMon AND cm.TenChuyenMon LIKE N'Nha sĩ%')";
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

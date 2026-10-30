package dao;

import connectDB.ConnectDB;
import entity.CaTruc;
import entity.NhanVien;
import entity.PhongKham;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class CaTrucDAO {
    public List<CaTruc> layTatCa() throws SQLException {
        String sql = "SELECT MaCa, Ngay, GioBatDau, GioKetThuc, LoaiCa, MaNhanVien, MaPhong "
                + "FROM CaTruc ORDER BY Ngay, GioBatDau, MaNhanVien";
        List<CaTruc> caTrucList = new ArrayList<>();
        try (PreparedStatement statement = getConnection().prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                caTrucList.add(new CaTruc(
                        result.getString("MaCa"),
                        result.getDate("Ngay").toLocalDate(),
                        result.getTime("GioBatDau").toLocalTime(),
                        result.getTime("GioKetThuc").toLocalTime(),
                        loaiCaHienThi(result.getString("LoaiCa")),
                        result.getString("MaNhanVien"),
                        result.getString("MaPhong")));
            }
        }
        return caTrucList;
    }

    public List<NhanVien> layTatCaNhanVien() throws SQLException {
        String sql = "SELECT MaNhanVien, HoTen, ChuyenMon, TrangThaiLamViec FROM NhanVien ORDER BY MaNhanVien";
        List<NhanVien> nhanVienList = new ArrayList<>();
        try (PreparedStatement statement = getConnection().prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                NhanVien nhanVien = new NhanVien();
                nhanVien.setMaNhanVien(result.getString("MaNhanVien"));
                nhanVien.setHoTen(result.getString("HoTen"));
                nhanVien.setChuyenMon(result.getString("ChuyenMon"));
                nhanVien.setTrangThaiLamViec(trangThaiHienThi(result.getString("TrangThaiLamViec")));
                nhanVienList.add(nhanVien);
            }
        }
        return nhanVienList;
    }

    public List<PhongKham> layTatCaPhong() throws SQLException {
        String sql = "SELECT MaPhong, TenPhong, TrangThai FROM PhongKham ORDER BY MaPhong";
        List<PhongKham> phongList = new ArrayList<>();
        try (PreparedStatement statement = getConnection().prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                phongList.add(new PhongKham(
                        result.getString("MaPhong"),
                        result.getString("TenPhong"),
                        trangThaiPhongEntity(result.getString("TrangThai"))));
            }
        }
        return phongList;
    }

    public boolean coCaTrung(CaTruc ca, String maCaBoQua) throws SQLException {
        String sql = "SELECT TOP 1 1 FROM CaTruc "
                + "WHERE MaNhanVien = ? AND Ngay = ? AND GioBatDau < ? AND GioKetThuc > ? "
                + "AND (? IS NULL OR MaCa <> ?)";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, ca.getMaNhanVien());
            statement.setDate(2, Date.valueOf(ca.getNgay()));
            statement.setTime(3, Time.valueOf(ca.getGioKetThuc()));
            statement.setTime(4, Time.valueOf(ca.getGioBatDau()));
            if (maCaBoQua == null) {
                statement.setNull(5, Types.NVARCHAR);
            } else {
                statement.setString(5, maCaBoQua);
            }
            statement.setString(6, maCaBoQua);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public void them(CaTruc ca) throws SQLException {
        Connection connection = getConnection();
        boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            String nextIdSql = "SELECT COALESCE(MAX(TRY_CONVERT(int, SUBSTRING(MaCa, 3, 18))), 0) + 1 "
                    + "FROM CaTruc WITH (TABLOCKX, HOLDLOCK) "
                    + "WHERE MaCa LIKE 'CA%' AND TRY_CONVERT(int, SUBSTRING(MaCa, 3, 18)) IS NOT NULL";
            try (PreparedStatement statement = connection.prepareStatement(nextIdSql);
                    ResultSet result = statement.executeQuery()) {
                result.next();
                ca.setMaCa(String.format("CA%03d", result.getInt(1)));
            }

            String insertSql = "INSERT INTO CaTruc "
                    + "(MaCa, Ngay, GioBatDau, GioKetThuc, LoaiCa, MaNhanVien, MaPhong) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
                setParameters(statement, ca);
                statement.executeUpdate();
            }
            connection.commit();
        } catch (SQLException | RuntimeException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(autoCommit);
        }
    }

    public boolean capNhat(CaTruc ca) throws SQLException {
        String sql = "UPDATE CaTruc SET Ngay = ?, GioBatDau = ?, GioKetThuc = ?, "
                + "LoaiCa = ?, MaNhanVien = ?, MaPhong = ? WHERE MaCa = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(ca.getNgay()));
            statement.setTime(2, Time.valueOf(ca.getGioBatDau()));
            statement.setTime(3, Time.valueOf(ca.getGioKetThuc()));
            statement.setString(4, loaiCaTrongDb(ca.getLoaiCa()));
            statement.setString(5, ca.getMaNhanVien());
            statement.setString(6, ca.getMaPhong());
            statement.setString(7, ca.getMaCa());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean xoa(String maCa) throws SQLException {
        String sql = "DELETE FROM CaTruc WHERE MaCa = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, maCa);
            return statement.executeUpdate() == 1;
        }
    }

    private void setParameters(PreparedStatement statement, CaTruc ca) throws SQLException {
        statement.setString(1, ca.getMaCa());
        statement.setDate(2, Date.valueOf(ca.getNgay()));
        statement.setTime(3, Time.valueOf(ca.getGioBatDau()));
        statement.setTime(4, Time.valueOf(ca.getGioKetThuc()));
        statement.setString(5, loaiCaTrongDb(ca.getLoaiCa()));
        statement.setString(6, ca.getMaNhanVien());
        statement.setString(7, ca.getMaPhong());
    }

    private String loaiCaHienThi(String loaiCa) {
        if ("Ca sang".equalsIgnoreCase(loaiCa)) {
            return "Ca sáng";
        }
        if ("Ca chieu".equalsIgnoreCase(loaiCa)) {
            return "Ca chiều";
        }
        if ("Hanh chinh".equalsIgnoreCase(loaiCa)) {
            return "Hành chính";
        }
        return loaiCa;
    }

    private String loaiCaTrongDb(String loaiCa) {
        if ("Ca sáng".equalsIgnoreCase(loaiCa)) {
            return "Ca sang";
        }
        if ("Ca chiều".equalsIgnoreCase(loaiCa)) {
            return "Ca chieu";
        }
        if ("Hành chính".equalsIgnoreCase(loaiCa)) {
            return "Hanh chinh";
        }
        return loaiCa;
    }

    private String trangThaiHienThi(String trangThai) {
        if ("Dang lam viec".equalsIgnoreCase(trangThai)) {
            return "Đang làm việc";
        }
        if ("Da nghi viec".equalsIgnoreCase(trangThai)) {
            return "Đã nghỉ việc";
        }
        if ("San sang".equalsIgnoreCase(trangThai)) {
            return "Sẵn sàng";
        }
        if ("Dang kham".equalsIgnoreCase(trangThai)) {
            return "Đang khám";
        }
        if ("Can ve sinh".equalsIgnoreCase(trangThai)) {
            return "Cần vệ sinh";
        }
        if ("Bao tri".equalsIgnoreCase(trangThai)) {
            return "Bảo trì";
        }
        return trangThai;
    }

    private String trangThaiPhongEntity(String trangThai) throws SQLException {
        if ("San sang".equalsIgnoreCase(trangThai) || "Sẵn sàng".equalsIgnoreCase(trangThai)) {
            return "San sang";
        }
        if ("Dang kham".equalsIgnoreCase(trangThai) || "Đang khám".equalsIgnoreCase(trangThai)) {
            return "Dang kham";
        }
        if ("Can ve sinh".equalsIgnoreCase(trangThai) || "Cần vệ sinh".equalsIgnoreCase(trangThai)) {
            return "Can ve sinh";
        }
        if ("Bao tri".equalsIgnoreCase(trangThai) || "Bảo trì".equalsIgnoreCase(trangThai)) {
            return "Bao tri";
        }
        throw new SQLException("Trạng thái phòng khám không hợp lệ trong cơ sở dữ liệu: " + trangThai);
    }

    private Connection getConnection() throws SQLException {
        Connection connection = ConnectDB.getConnection();
        if (connection == null || connection.isClosed()) {
            throw new SQLException("Chưa có kết nối cơ sở dữ liệu.");
        }
        return connection;
    }
}

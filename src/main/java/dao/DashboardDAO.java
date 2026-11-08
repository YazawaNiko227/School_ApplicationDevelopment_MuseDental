package dao;

import connectDB.ConnectDB;
import entity.BenhNhan;
import entity.LichHen;
import entity.NhanVien;
import entity.PhongKham;
import entity.ThongKeTongQuan;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DashboardDAO {
    public ThongKeTongQuan layThongKeHomNay() throws SQLException {
        String sql = "SELECT "
                + "(SELECT COUNT(*) FROM LichHen WHERE CAST(ThoiGianHen AS DATE) = CAST(GETDATE() AS DATE)) "
                + "AS LichHenHomNay, "
                + "(SELECT COUNT(*) FROM BenhNhan) AS TongBenhNhan, "
                + "(SELECT COUNT(*) FROM LichHen WHERE CAST(ThoiGianHen AS DATE) = CAST(GETDATE() AS DATE) "
                + "AND TrangThai IN (N'Cho xac nhan', N'Da xac nhan')) AS ChoTiepDon, "
                + "(SELECT COALESCE(SUM(Tong.Tien), 0) FROM HoaDon HD CROSS APPLY ("
                + "SELECT COALESCE((SELECT SUM(CT.SoLuong * CT.DonGiaTaiThoiDiem) "
                + "FROM ChiTietHoaDon CT WHERE CT.MaHoaDon = HD.MaHoaDon), 0) "
                + "+ COALESCE((SELECT SUM(CTT.SoLuong * CTT.DonGiaTaiThoiDiem) "
                + "FROM ChiTietHoaDonThuoc CTT WHERE CTT.MaHoaDon = HD.MaHoaDon), 0) AS Tien"
                + ") Tong WHERE HD.NgayLap = CAST(GETDATE() AS DATE) "
                + "AND HD.TrangThaiThanhToan = N'Da thanh toan') AS DoanhThuHomNay";

        try (PreparedStatement statement = ConnectDB.getConnection().prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            if (!result.next()) {
                throw new SQLException("Không lấy được số liệu tổng quan.");
            }
            return new ThongKeTongQuan(
                    result.getInt("LichHenHomNay"),
                    result.getInt("TongBenhNhan"),
                    result.getInt("ChoTiepDon"),
                    result.getBigDecimal("DoanhThuHomNay"));
        }
    }

    public List<LichHen> layLichHenSapToi(int gioiHan) throws SQLException {
        if (gioiHan < 1 || gioiHan > 100) {
            throw new IllegalArgumentException("Giới hạn lịch hẹn phải từ 1 đến 100.");
        }
        String sql = "SELECT TOP (?) LH.MaLichHen, LH.ThoiGianHen, LH.TrangThai, LH.NgayTao, "
                + "LH.ThoiGianCheckIn, LH.MaNhanVienLap, LH.MaNhaSiPhuTrach, NV.HoTen AS TenNhaSi, "
                + "LH.MaBenhNhan, BN.HoTen AS TenBenhNhan, LH.MaPhong, LH.MaKeHoach "
                + "FROM LichHen LH "
                + "JOIN BenhNhan BN ON BN.MaBenhNhan = LH.MaBenhNhan "
                + "JOIN NhanVien NV ON NV.MaNhanVien = LH.MaNhaSiPhuTrach "
                + "WHERE LH.ThoiGianHen >= GETDATE() "
                + "AND LH.TrangThai NOT IN (N'Huy', N'Hoan thanh', N'Vang') "
                + "ORDER BY LH.ThoiGianHen";
        List<LichHen> appointments = new ArrayList<>();
        try (PreparedStatement statement = ConnectDB.getConnection().prepareStatement(sql)) {
            statement.setInt(1, gioiHan);
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
                    java.sql.Timestamp thoiGianCheckIn = result.getTimestamp("ThoiGianCheckIn");
                    lichHen.setThoiGianCheckIn(
                            thoiGianCheckIn == null ? null : thoiGianCheckIn.toLocalDateTime());
                    lichHen.setMaNhanVienLap(result.getString("MaNhanVienLap"));
                    lichHen.setNhaSiPhuTrach(nhaSi);
                    lichHen.setBenhNhan(benhNhan);
                    lichHen.setPhongKham(phongKham);
                    lichHen.setMaKeHoach(result.getString("MaKeHoach"));
                    appointments.add(lichHen);
                }
            }
        }
        return appointments;
    }
}

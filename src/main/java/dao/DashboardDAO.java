package dao;

import connectDB.ConnectDB;
import entity.LichHenTongQuan;
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

    public List<LichHenTongQuan> layLichHenSapToi(int gioiHan) throws SQLException {
        if (gioiHan < 1 || gioiHan > 100) {
            throw new IllegalArgumentException("Giới hạn lịch hẹn phải từ 1 đến 100.");
        }
        String sql = "SELECT TOP (?) LH.ThoiGianHen, BN.HoTen AS TenBenhNhan, "
                + "NV.HoTen AS TenNhaSi, LH.TrangThai "
                + "FROM LichHen LH "
                + "JOIN BenhNhan BN ON BN.MaBenhNhan = LH.MaBenhNhan "
                + "JOIN NhanVien NV ON NV.MaNhanVien = LH.MaNhaSiPhuTrach "
                + "WHERE LH.ThoiGianHen >= GETDATE() "
                + "AND LH.TrangThai NOT IN (N'Huy', N'Hoan thanh', N'Vang') "
                + "ORDER BY LH.ThoiGianHen";
        List<LichHenTongQuan> appointments = new ArrayList<>();
        try (PreparedStatement statement = ConnectDB.getConnection().prepareStatement(sql)) {
            statement.setInt(1, gioiHan);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    appointments.add(new LichHenTongQuan(
                            result.getTimestamp("ThoiGianHen").toLocalDateTime(),
                            result.getString("TenBenhNhan"),
                            "Nha sĩ: " + result.getString("TenNhaSi"),
                            result.getString("TrangThai")));
                }
            }
        }
        return appointments;
    }
}

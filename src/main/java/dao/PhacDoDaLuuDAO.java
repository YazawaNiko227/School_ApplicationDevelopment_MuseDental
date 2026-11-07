package dao;

import connectDB.ConnectDB;
import dto.DichVuPhacDoDaLuu;
import dto.PhacDoDaLuu;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PhacDoDaLuuDAO {
    public List<PhacDoDaLuu> layDanhSach() throws SQLException {
        String sql = "SELECT kh.MaKeHoach, kh.TenPhacDo, kh.ThoiGianDuKien, kh.TrangThai, "
                + "bn.MaBenhNhan, bn.HoTen AS TenBenhNhan, cd.MaChanDoan, cd.NgayKham, cd.KetLuanChanDoan "
                + "FROM KeHoachDieuTri kh "
                + "JOIN BenhNhan bn ON bn.MaBenhNhan = kh.MaBenhNhan "
                + "JOIN ChanDoan cd ON cd.MaChanDoan = kh.MaChanDoan "
                + "ORDER BY cd.NgayKham DESC, kh.MaKeHoach DESC";
        List<PhacDoDaLuu> plans = new ArrayList<>();
        try (PreparedStatement statement = ConnectDB.getConnection().prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                int days = result.getInt("ThoiGianDuKien");
                Integer expectedDays = result.wasNull() ? null : days;
                java.sql.Date date = result.getDate("NgayKham");
                plans.add(new PhacDoDaLuu(
                        result.getString("MaKeHoach"),
                        result.getString("TenPhacDo"),
                        expectedDays,
                        result.getString("TrangThai"),
                        result.getString("MaBenhNhan"),
                        result.getString("TenBenhNhan"),
                        result.getString("MaChanDoan"),
                        date == null ? null : date.toLocalDate(),
                        result.getString("KetLuanChanDoan")));
            }
        }
        return plans;
    }

    public List<DichVuPhacDoDaLuu> layDichVu(String maKeHoach) throws SQLException {
        String sql = "SELECT dv.MaDichVu, dv.TenDichVu, khdv.SoLuong, dv.DonGia "
                + "FROM KeHoachDieuTri_DichVu khdv "
                + "JOIN DichVu dv ON dv.MaDichVu = khdv.MaDichVu "
                + "WHERE khdv.MaKeHoach = ? ORDER BY dv.TenDichVu";
        List<DichVuPhacDoDaLuu> services = new ArrayList<>();
        try (PreparedStatement statement = ConnectDB.getConnection().prepareStatement(sql)) {
            statement.setString(1, maKeHoach);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    services.add(new DichVuPhacDoDaLuu(
                            result.getString("MaDichVu"),
                            result.getString("TenDichVu"),
                            result.getInt("SoLuong"),
                            result.getDouble("DonGia")));
                }
            }
        }
        return services;
    }
}

package dao;

import connectDB.ConnectDB;
import entity.CaKham;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Đọc hàng chờ khám cùng thông tin hồ sơ và chẩn đoán đã có. */
public class KhamLamSangDAO {
    public List<CaKham> layHangCho() throws SQLException {
        String sql = "SELECT lh.MaLichHen, lh.ThoiGianHen, lh.TrangThai AS TrangThaiLich, "
                + "lh.MaBenhNhan, lh.MaNhaSiPhuTrach AS MaNhaSi, lh.MaPhong, "
                + "bn.HoTen, bn.GioiTinh, bn.NgaySinh, bn.TienSuBenhLy, bn.ThongTinDiUng, bn.NguoiBaoHo, "
                + "hs.MaHoSo, ns.HoTen AS TenNhaSi, pk.TrangThai AS TrangThaiPhong, "
                + "cd.MaChanDoan, cd.LyDoKham, cd.TrieuChung, cd.KetLuanChanDoan, "
                + "cd.TrangThai AS TrangThaiChanDoan "
                + "FROM LichHen lh "
                + "INNER JOIN BenhNhan bn ON bn.MaBenhNhan = lh.MaBenhNhan "
                + "LEFT JOIN HoSoBenhAn hs ON hs.MaBenhNhan = bn.MaBenhNhan "
                + "INNER JOIN NhanVien ns ON ns.MaNhanVien = lh.MaNhaSiPhuTrach "
                + "LEFT JOIN PhongKham pk ON pk.MaPhong = lh.MaPhong "
                + "LEFT JOIN ChanDoan cd ON cd.MaLichHen = lh.MaLichHen "
                + "WHERE lh.TrangThai IN (N'Da check-in', N'Dang kham') "
                + "OR (lh.TrangThai = N'Hoan thanh' "
                + "AND lh.ThoiGianHen >= CAST(GETDATE() AS date) "
                + "AND lh.ThoiGianHen < DATEADD(day, 1, CAST(GETDATE() AS date))) "
                + "ORDER BY lh.ThoiGianHen, lh.MaLichHen";
        List<CaKham> danhSach = new ArrayList<>();
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                java.sql.Date ngaySinhValue = rs.getDate("NgaySinh");
                danhSach.add(new CaKham(
                        rs.getString("MaLichHen"),
                        rs.getTimestamp("ThoiGianHen").toLocalDateTime(),
                        rs.getString("MaBenhNhan"),
                        rs.getString("HoTen"),
                        rs.getString("GioiTinh"),
                        ngaySinhValue == null ? null : ngaySinhValue.toLocalDate(),
                        rs.getString("TienSuBenhLy"),
                        rs.getString("ThongTinDiUng"),
                        rs.getString("NguoiBaoHo"),
                        rs.getString("MaHoSo"),
                        rs.getString("MaNhaSi"),
                        rs.getString("TenNhaSi"),
                        rs.getString("MaPhong"),
                        rs.getString("TrangThaiPhong"),
                        rs.getString("TrangThaiLich"),
                        rs.getString("MaChanDoan"),
                        rs.getString("LyDoKham"),
                        rs.getString("TrieuChung"),
                        rs.getString("KetLuanChanDoan"),
                        rs.getString("TrangThaiChanDoan")));
            }
        }
        return danhSach;
    }
}

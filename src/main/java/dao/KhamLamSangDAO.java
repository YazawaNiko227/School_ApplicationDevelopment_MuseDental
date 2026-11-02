package dao;

import connectDB.ConnectDB;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Đọc hàng chờ khám cùng thông tin hồ sơ và chẩn đoán đã có. */
public class KhamLamSangDAO {
    public static final class CaKham {
        private final String maLichHen;
        private final LocalDateTime thoiGianHen;
        private final String maBenhNhan;
        private final String hoTen;
        private final String gioiTinh;
        private final LocalDate ngaySinh;
        private final String tienSuBenhLy;
        private final String thongTinDiUng;
        private final String nguoiBaoHo;
        private final String maHoSo;
        private final String maNhaSi;
        private final String tenNhaSi;
        private final String maPhong;
        private final String trangThaiPhong;
        private final String trangThaiLich;
        private final String maChanDoan;
        private final String lyDoKham;
        private final String trieuChung;
        private final String ketLuanChanDoan;
        private final String trangThaiChanDoan;

        private CaKham(ResultSet rs) throws SQLException {
            maLichHen = rs.getString("MaLichHen");
            thoiGianHen = rs.getTimestamp("ThoiGianHen").toLocalDateTime();
            maBenhNhan = rs.getString("MaBenhNhan");
            hoTen = rs.getString("HoTen");
            gioiTinh = rs.getString("GioiTinh");
            java.sql.Date ngaySinhValue = rs.getDate("NgaySinh");
            ngaySinh = ngaySinhValue == null ? null : ngaySinhValue.toLocalDate();
            tienSuBenhLy = rs.getString("TienSuBenhLy");
            thongTinDiUng = rs.getString("ThongTinDiUng");
            nguoiBaoHo = rs.getString("NguoiBaoHo");
            maHoSo = rs.getString("MaHoSo");
            maNhaSi = rs.getString("MaNhaSi");
            tenNhaSi = rs.getString("TenNhaSi");
            maPhong = rs.getString("MaPhong");
            trangThaiPhong = rs.getString("TrangThaiPhong");
            trangThaiLich = rs.getString("TrangThaiLich");
            maChanDoan = rs.getString("MaChanDoan");
            lyDoKham = rs.getString("LyDoKham");
            trieuChung = rs.getString("TrieuChung");
            ketLuanChanDoan = rs.getString("KetLuanChanDoan");
            trangThaiChanDoan = rs.getString("TrangThaiChanDoan");
        }

        public String getMaLichHen() { return maLichHen; }
        public LocalDateTime getThoiGianHen() { return thoiGianHen; }
        public String getMaBenhNhan() { return maBenhNhan; }
        public String getHoTen() { return hoTen; }
        public String getGioiTinh() { return gioiTinh; }
        public LocalDate getNgaySinh() { return ngaySinh; }
        public String getTienSuBenhLy() { return tienSuBenhLy; }
        public String getThongTinDiUng() { return thongTinDiUng; }
        public String getNguoiBaoHo() { return nguoiBaoHo; }
        public String getMaHoSo() { return maHoSo; }
        public String getMaNhaSi() { return maNhaSi; }
        public String getTenNhaSi() { return tenNhaSi; }
        public String getMaPhong() { return maPhong; }
        public String getTrangThaiPhong() { return trangThaiPhong; }
        public String getTrangThaiLich() { return trangThaiLich; }
        public String getMaChanDoan() { return maChanDoan; }
        public String getLyDoKham() { return lyDoKham; }
        public String getTrieuChung() { return trieuChung; }
        public String getKetLuanChanDoan() { return ketLuanChanDoan; }
        public String getTrangThaiChanDoan() { return trangThaiChanDoan; }
    }

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
                danhSach.add(new CaKham(rs));
            }
        }
        return danhSach;
    }
}

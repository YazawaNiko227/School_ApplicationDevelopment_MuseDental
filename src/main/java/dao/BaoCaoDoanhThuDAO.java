package dao;

import connectDB.ConnectDB;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BaoCaoDoanhThuDAO {
    public List<ChiTietDoanhThu> layChiTietDoanhThu(LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        String sql = "WITH HoaDonHopLe AS ("
                + "SELECT MaHoaDon, NgayLap, TrangThaiThanhToan, "
                + "COALESCE(PhuongThucThanhToan, N'Chưa chọn') AS PhuongThucThanhToan "
                + "FROM HoaDon "
                + "WHERE NgayLap >= ? AND NgayLap < DATEADD(day, 1, ?) "
                + "AND TrangThaiThanhToan IN (N'Da thanh toan', N'Đã thanh toán', "
                + "N'Dang tra gop', N'Đang trả góp')) "
                + "SELECT hd.MaHoaDon, hd.NgayLap, hd.PhuongThucThanhToan, "
                + "N'Dịch vụ' AS Loai, dv.TenDichVu AS TenMuc, "
                + "(ct.SoLuong * ct.DonGiaTaiThoiDiem) AS ThanhTien "
                + "FROM HoaDonHopLe hd "
                + "JOIN ChiTietHoaDon ct ON ct.MaHoaDon = hd.MaHoaDon "
                + "JOIN DichVu dv ON dv.MaDichVu = ct.MaDichVu "
                + "UNION ALL "
                + "SELECT hd.MaHoaDon, hd.NgayLap, hd.PhuongThucThanhToan, "
                + "N'Thuốc' AS Loai, vt.TenVatTu AS TenMuc, "
                + "(ct.SoLuong * ct.DonGiaTaiThoiDiem) AS ThanhTien "
                + "FROM HoaDonHopLe hd "
                + "JOIN ChiTietHoaDonThuoc ct ON ct.MaHoaDon = hd.MaHoaDon "
                + "JOIN VatTu vt ON vt.MaVatTu = ct.MaVatTu "
                + "ORDER BY NgayLap, MaHoaDon, Loai, TenMuc";
        List<ChiTietDoanhThu> ds = new ArrayList<>();
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(tuNgay));
            ps.setDate(2, Date.valueOf(denNgay));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(new ChiTietDoanhThu(rs.getString("MaHoaDon"), rs.getDate("NgayLap").toLocalDate(),
                            rs.getString("PhuongThucThanhToan"), rs.getString("Loai"),
                            rs.getString("TenMuc"), rs.getDouble("ThanhTien")));
                }
            }
        }
        return ds;
    }

    public static final class ChiTietDoanhThu {
        private final String maHoaDon;
        private final LocalDate ngayLap;
        private final String phuongThucThanhToan;
        private final String loai;
        private final String tenMuc;
        private final double thanhTien;

        private ChiTietDoanhThu(String maHoaDon, LocalDate ngayLap, String phuongThucThanhToan,
                String loai, String tenMuc, double thanhTien) {
            this.maHoaDon = maHoaDon;
            this.ngayLap = ngayLap;
            this.phuongThucThanhToan = phuongThucThanhToan;
            this.loai = loai;
            this.tenMuc = tenMuc;
            this.thanhTien = thanhTien;
        }

        public String getMaHoaDon() {
            return maHoaDon;
        }

        public LocalDate getNgayLap() {
            return ngayLap;
        }

        public String getPhuongThucThanhToan() {
            return phuongThucThanhToan;
        }

        public String getLoai() {
            return loai;
        }

        public String getTenMuc() {
            return tenMuc;
        }

        public double getThanhTien() {
            return thanhTien;
        }
    }
}

package dao;

import connectDB.ConnectDB;
import entity.BaoCaoDoanhThu;
import entity.BaoCaoDoanhThu.NhomDoanhThu;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Đọc doanh thu đã ghi nhận trên các dòng chi tiết hóa đơn. */
public class ThongKeDoanhThuDAO {
    public static final String THEO_NGAY = "Theo ngày";
    public static final String THEO_THANG = "Theo tháng";
    public static final String THEO_MUC = "Theo dịch vụ / thuốc";
    public static final String THEO_PHUONG_THUC = "Theo phương thức thanh toán";

    public BaoCaoDoanhThu thongKe(LocalDate tuNgay, LocalDate denNgay, String cachNhom)
            throws SQLException {
        if (tuNgay == null || denNgay == null || tuNgay.isAfter(denNgay)) {
            throw new IllegalArgumentException("Khoảng ngày báo cáo không hợp lệ.");
        }
        String bieuThucNhom = bieuThucNhom(cachNhom);
        int soHoaDon = demHoaDon(tuNgay, denNgay);
        List<NhomDoanhThu> dsNhom = layNhomDoanhThu(tuNgay, denNgay, bieuThucNhom);
        double tong = 0;
        double tienDichVu = 0;
        double tienThuoc = 0;
        for (NhomDoanhThu nhom : dsNhom) {
            tong += nhom.getTongDoanhThu();
            tienDichVu += nhom.getTienDichVu();
            tienThuoc += nhom.getTienThuoc();
        }
        return new BaoCaoDoanhThu(soHoaDon, tong, tienDichVu, tienThuoc, dsNhom);
    }

    private static String bieuThucNhom(String cachNhom) {
        if (THEO_NGAY.equals(cachNhom)) {
            return "CONVERT(char(10), NgayLap, 23)";
        }
        if (THEO_THANG.equals(cachNhom)) {
            return "CONVERT(char(7), NgayLap, 126)";
        }
        if (THEO_MUC.equals(cachNhom)) {
            return "Loai + N': ' + Ten";
        }
        if (THEO_PHUONG_THUC.equals(cachNhom)) {
            return "CASE PhuongThucThanhToan "
                    + "WHEN N'Tien mat' THEN N'Tiền mặt' "
                    + "WHEN N'Chuyen khoan' THEN N'Chuyển khoản' "
                    + "WHEN N'Tra gop' THEN N'Trả góp' ELSE N'Chưa chọn' END";
        }
        throw new IllegalArgumentException("Cách nhóm báo cáo không hợp lệ.");
    }

    private int demHoaDon(LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        String sql = "SELECT COUNT(*) FROM HoaDon "
                + "WHERE NgayLap BETWEEN ? AND ? "
                + "AND TrangThaiThanhToan IN (N'Da thanh toan', N'Dang tra gop')";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            ganKhoangNgay(ps, tuNgay, denNgay);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private List<NhomDoanhThu> layNhomDoanhThu(LocalDate tuNgay, LocalDate denNgay,
            String bieuThucNhom) throws SQLException {
        String sql = "WITH HoaDonHopLe AS ("
                + "SELECT MaHoaDon, NgayLap, PhuongThucThanhToan FROM HoaDon "
                + "WHERE NgayLap BETWEEN ? AND ? "
                + "AND TrangThaiThanhToan IN (N'Da thanh toan', N'Dang tra gop')"
                + "), ChiTietDoanhThu AS ("
                + "SELECT hd.MaHoaDon, hd.NgayLap, hd.PhuongThucThanhToan, "
                + "N'Dịch vụ' AS Loai, dv.TenDichVu AS Ten, "
                + "ct.SoLuong * ct.DonGiaTaiThoiDiem AS ThanhTien "
                + "FROM HoaDonHopLe hd JOIN ChiTietHoaDon ct ON ct.MaHoaDon = hd.MaHoaDon "
                + "JOIN DichVu dv ON dv.MaDichVu = ct.MaDichVu "
                + "UNION ALL "
                + "SELECT hd.MaHoaDon, hd.NgayLap, hd.PhuongThucThanhToan, "
                + "N'Thuốc' AS Loai, vt.TenVatTu AS Ten, "
                + "ct.SoLuong * ct.DonGiaTaiThoiDiem AS ThanhTien "
                + "FROM HoaDonHopLe hd JOIN ChiTietHoaDonThuoc ct ON ct.MaHoaDon = hd.MaHoaDon "
                + "JOIN VatTu vt ON vt.MaVatTu = ct.MaVatTu"
                + ") SELECT " + bieuThucNhom + " AS TenNhom, "
                + "COUNT(DISTINCT MaHoaDon) AS SoHoaDon, SUM(ThanhTien) AS TongDoanhThu, "
                + "SUM(CASE WHEN Loai = N'Dịch vụ' THEN ThanhTien ELSE 0 END) AS TienDichVu, "
                + "SUM(CASE WHEN Loai = N'Thuốc' THEN ThanhTien ELSE 0 END) AS TienThuoc "
                + "FROM ChiTietDoanhThu GROUP BY " + bieuThucNhom + " ORDER BY TenNhom";

        List<NhomDoanhThu> ds = new ArrayList<>();
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            ganKhoangNgay(ps, tuNgay, denNgay);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(new NhomDoanhThu(rs.getString("TenNhom"), rs.getInt("SoHoaDon"),
                            rs.getDouble("TongDoanhThu"), rs.getDouble("TienDichVu"),
                            rs.getDouble("TienThuoc")));
                }
            }
        }
        return ds;
    }

    private static void ganKhoangNgay(PreparedStatement ps, LocalDate tuNgay, LocalDate denNgay)
            throws SQLException {
        ps.setDate(1, Date.valueOf(tuNgay));
        ps.setDate(2, Date.valueOf(denNgay));
    }
}

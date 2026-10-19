package entity;

import java.time.LocalDate;

/**
 * Một dòng dữ liệu của báo cáo doanh thu, tương ứng với 1 hóa đơn.
 *
 * Dữ liệu được gộp từ các bảng: HoaDon, BenhNhan, NhanVien (thu ngân),
 * ChiTietHoaDon (tổng tiền) và CongNoTraGop/DotThanhToan (số tiền đã thu).
 *
 * - tongTien : SUM(ChiTietHoaDon.SoLuong * DonGiaTaiThoiDiem) của hóa đơn.
 * - daThu    : số tiền thực tế đã thu. Hóa đơn "Đã thanh toán" = tongTien;
 *              hóa đơn trả góp = tổng SoTienDot của các đợt đã thanh toán;
 *              hóa đơn chưa thanh toán = 0.
 */
public class RevenueRecord {
    private final String maHoaDon;
    private final LocalDate ngayLap;
    private final String maBenhNhan;
    private final String tenBenhNhan;
    private final String maNhanVien;
    private final String tenThuNgan;
    private final String phuongThucThanhToan; // có thể null
    private final String trangThaiThanhToan;
    private final double tongTien;
    private final double daThu;

    public RevenueRecord(String maHoaDon, LocalDate ngayLap, String maBenhNhan, String tenBenhNhan,
            String maNhanVien, String tenThuNgan, String phuongThucThanhToan,
            String trangThaiThanhToan, double tongTien, double daThu) {
        this.maHoaDon = maHoaDon;
        this.ngayLap = ngayLap;
        this.maBenhNhan = maBenhNhan;
        this.tenBenhNhan = tenBenhNhan;
        this.maNhanVien = maNhanVien;
        this.tenThuNgan = tenThuNgan;
        this.phuongThucThanhToan = phuongThucThanhToan;
        this.trangThaiThanhToan = trangThaiThanhToan;
        this.tongTien = tongTien;
        this.daThu = daThu;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public LocalDate getNgayLap() {
        return ngayLap;
    }

    public String getMaBenhNhan() {
        return maBenhNhan;
    }

    public String getTenBenhNhan() {
        return tenBenhNhan;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public String getTenThuNgan() {
        return tenThuNgan;
    }

    public String getPhuongThucThanhToan() {
        return phuongThucThanhToan;
    }

    public String getTrangThaiThanhToan() {
        return trangThaiThanhToan;
    }

    public double getTongTien() {
        return tongTien;
    }

    public double getDaThu() {
        return daThu;
    }

    /** Số tiền còn nợ của hóa đơn. */
    public double getConLai() {
        return Math.max(0, tongTien - daThu);
    }
}

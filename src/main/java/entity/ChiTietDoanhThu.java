package entity;

import java.time.LocalDate;

public class ChiTietDoanhThu {
    private final String maHoaDon;
    private final LocalDate ngayLap;
    private final String phuongThucThanhToan;
    private final String loai;
    private final String tenMuc;
    private final double thanhTien;

    public ChiTietDoanhThu(String maHoaDon, LocalDate ngayLap, String phuongThucThanhToan,
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

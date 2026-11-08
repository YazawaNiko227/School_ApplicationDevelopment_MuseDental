package entity;

import java.time.LocalDate;

public class DotHoaDon {
    public final String ma;
    public final LocalDate ngayDenHan;
    public final double soTien;
    public final String trangThai;
    public final LocalDate ngayThanhToan;

    public DotHoaDon(String ma, LocalDate ngayDenHan, double soTien, String trangThai, LocalDate ngayThanhToan) {
        this.ma = ma;
        this.ngayDenHan = ngayDenHan;
        this.soTien = soTien;
        this.trangThai = trangThai;
        this.ngayThanhToan = ngayThanhToan;
    }

    public String ma() {
        return ma;
    }

    public LocalDate ngayDenHan() {
        return ngayDenHan;
    }

    public double soTien() {
        return soTien;
    }

    public String trangThai() {
        return trangThai;
    }

    public LocalDate ngayThanhToan() {
        return ngayThanhToan;
    }
}

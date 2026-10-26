package entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class HoaDon {
    private String maHoaDon;
    private LocalDate ngayLap;
    private String trangThaiThanhToan;
    private String phuongThucThanhToan;
    private String maNhanVien;
    private String maBenhNhan;
    private String maChanDoan;

    public HoaDon() {
        this.trangThaiThanhToan = "Chua thanh toan";
        this.ngayLap = LocalDate.now();
    }

    public HoaDon(String maHoaDon, LocalDate ngayLap, String trangThaiThanhToan, String phuongThucThanhToan, String maNhanVien, String maBenhNhan, String maChanDoan) {
        setMaHoaDon(maHoaDon);
        setNgayLap(ngayLap);
        setTrangThaiThanhToan(trangThaiThanhToan);
        setPhuongThucThanhToan(phuongThucThanhToan);
        setMaNhanVien(maNhanVien);
        setMaBenhNhan(maBenhNhan);
        setMaChanDoan(maChanDoan);
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        if (maHoaDon == null || maHoaDon.trim().isEmpty()) throw new IllegalArgumentException("maHoaDon không được để trống");
        if (maHoaDon != null && maHoaDon.length() > 20) throw new IllegalArgumentException("maHoaDon tối đa 20 ký tự");
        this.maHoaDon = maHoaDon;
    }

    public LocalDate getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(LocalDate ngayLap) {
        if (ngayLap == null) throw new IllegalArgumentException("ngayLap không được null");
        this.ngayLap = ngayLap;
    }

    public String getTrangThaiThanhToan() {
        return trangThaiThanhToan;
    }

    public void setTrangThaiThanhToan(String trangThaiThanhToan) {
        if (trangThaiThanhToan == null || trangThaiThanhToan.trim().isEmpty()) throw new IllegalArgumentException("trangThaiThanhToan không được để trống");
        if (trangThaiThanhToan != null && trangThaiThanhToan.length() > 30) throw new IllegalArgumentException("trangThaiThanhToan tối đa 30 ký tự");
        this.trangThaiThanhToan = trangThaiThanhToan;
    }

    public String getPhuongThucThanhToan() {
        return phuongThucThanhToan;
    }

    public void setPhuongThucThanhToan(String phuongThucThanhToan) {
        if (phuongThucThanhToan != null && phuongThucThanhToan.length() > 30) throw new IllegalArgumentException("phuongThucThanhToan tối đa 30 ký tự");
        this.phuongThucThanhToan = phuongThucThanhToan;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        if (maNhanVien == null || maNhanVien.trim().isEmpty()) throw new IllegalArgumentException("maNhanVien không được để trống");
        if (maNhanVien != null && maNhanVien.length() > 20) throw new IllegalArgumentException("maNhanVien tối đa 20 ký tự");
        this.maNhanVien = maNhanVien;
    }

    public String getMaBenhNhan() {
        return maBenhNhan;
    }

    public void setMaBenhNhan(String maBenhNhan) {
        if (maBenhNhan == null || maBenhNhan.trim().isEmpty()) throw new IllegalArgumentException("maBenhNhan không được để trống");
        if (maBenhNhan != null && maBenhNhan.length() > 20) throw new IllegalArgumentException("maBenhNhan tối đa 20 ký tự");
        this.maBenhNhan = maBenhNhan;
    }

    public String getMaChanDoan() {
        return maChanDoan;
    }

    public void setMaChanDoan(String maChanDoan) {
        if (maChanDoan != null && maChanDoan.length() > 20) throw new IllegalArgumentException("maChanDoan tối đa 20 ký tự");
        this.maChanDoan = maChanDoan;
    }

}

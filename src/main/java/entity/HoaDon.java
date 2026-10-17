package entity;

import java.time.LocalDate;

public class HoaDon {
    private String maHoaDon;
    private LocalDate ngayLap;
    private String trangThaiThanhToan;
    private String phuongThucThanhToan;
    private String maNhanVien;
    private String maBenhNhan;

    public HoaDon() {
        this.ngayLap = LocalDate.now();
        this.trangThaiThanhToan = "Chua thanh toan";
    }

    public HoaDon(String maHoaDon, LocalDate ngayLap, String trangThaiThanhToan, String phuongThucThanhToan, String maNhanVien, String maBenhNhan) {
        setMaHoaDon(maHoaDon);
        setNgayLap(ngayLap);
        setTrangThaiThanhToan(trangThaiThanhToan);
        setPhuongThucThanhToan(phuongThucThanhToan);
        setMaNhanVien(maNhanVien);
        setMaBenhNhan(maBenhNhan);
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        if (maHoaDon == null) throw new IllegalArgumentException("MaHoaDon không được null.");
        if (maHoaDon != null && maHoaDon.length() > 20) throw new IllegalArgumentException("MaHoaDon tối đa 20 ký tự.");
        this.maHoaDon = maHoaDon;
    }

    public LocalDate getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(LocalDate ngayLap) {
        if (ngayLap == null) throw new IllegalArgumentException("NgayLap không được null.");
        this.ngayLap = ngayLap;
    }

    public String getTrangThaiThanhToan() {
        return trangThaiThanhToan;
    }

    public void setTrangThaiThanhToan(String trangThaiThanhToan) {
        if (trangThaiThanhToan == null) throw new IllegalArgumentException("TrangThaiThanhToan không được null.");
        if (trangThaiThanhToan != null && trangThaiThanhToan.length() > 30) throw new IllegalArgumentException("TrangThaiThanhToan tối đa 30 ký tự.");
        this.trangThaiThanhToan = trangThaiThanhToan;
    }

    public String getPhuongThucThanhToan() {
        return phuongThucThanhToan;
    }

    public void setPhuongThucThanhToan(String phuongThucThanhToan) {
        if (phuongThucThanhToan != null && phuongThucThanhToan.length() > 30) throw new IllegalArgumentException("PhuongThucThanhToan tối đa 30 ký tự.");
        this.phuongThucThanhToan = phuongThucThanhToan;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        if (maNhanVien == null) throw new IllegalArgumentException("MaNhanVien không được null.");
        if (maNhanVien != null && maNhanVien.length() > 20) throw new IllegalArgumentException("MaNhanVien tối đa 20 ký tự.");
        this.maNhanVien = maNhanVien;
    }

    public String getMaBenhNhan() {
        return maBenhNhan;
    }

    public void setMaBenhNhan(String maBenhNhan) {
        if (maBenhNhan == null) throw new IllegalArgumentException("MaBenhNhan không được null.");
        if (maBenhNhan != null && maBenhNhan.length() > 20) throw new IllegalArgumentException("MaBenhNhan tối đa 20 ký tự.");
        this.maBenhNhan = maBenhNhan;
    }

}

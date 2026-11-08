package entity;

import java.time.LocalDate;

/** Entity ánh xạ bảng PhieuNhapKho trong QuanLyPhongKhamMuse. */
public class PhieuNhapKho {
    private String maPhieuNhap;
    private LocalDate ngayNhap;
    private String nhaCungCap;
    private NhanVien maNhanVien;

    public PhieuNhapKho() {
        this.ngayNhap = LocalDate.now();
    }

    public PhieuNhapKho(String maPhieuNhap, LocalDate ngayNhap, String nhaCungCap, String maNhanVien) {
        setMaPhieuNhap(maPhieuNhap);
        setNgayNhap(ngayNhap);
        setNhaCungCap(nhaCungCap);
        setMaNhanVien(maNhanVien);
    }

    public String getMaPhieuNhap() {
        return maPhieuNhap;
    }

    public void setMaPhieuNhap(String maPhieuNhap) {
        if (maPhieuNhap == null)
            throw new IllegalArgumentException("MaPhieuNhap không được null.");
        if (maPhieuNhap != null && maPhieuNhap.length() > 20)
            throw new IllegalArgumentException("MaPhieuNhap tối đa 20 ký tự.");
        this.maPhieuNhap = maPhieuNhap;
    }

    public LocalDate getNgayNhap() {
        return ngayNhap;
    }

    public void setNgayNhap(LocalDate ngayNhap) {
        if (ngayNhap == null)
            throw new IllegalArgumentException("NgayNhap không được null.");
        this.ngayNhap = ngayNhap;
    }

    public String getNhaCungCap() {
        return nhaCungCap;
    }

    public void setNhaCungCap(String nhaCungCap) {
        if (nhaCungCap != null && nhaCungCap.length() > 150)
            throw new IllegalArgumentException("NhaCungCap tối đa 150 ký tự.");
        this.nhaCungCap = nhaCungCap;
    }

    public NhanVien getMaNhanVien() {
        return maNhanVien;
    }

    public String getMaNhanVienId() {
        return maNhanVien == null ? null : maNhanVien.getMaNhanVien();
    }

    public NhanVien getNhanVien() {
        return maNhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        if (nhanVien == null)
            throw new IllegalArgumentException("NhanVien không được null.");
        this.maNhanVien = nhanVien;
    }

    public void setMaNhanVien(NhanVien nhanVien) {
        setNhanVien(nhanVien);
    }

    public void setMaNhanVien(String maNhanVien) {
        if (maNhanVien == null)
            throw new IllegalArgumentException("MaNhanVien không được null.");
        if (maNhanVien != null && maNhanVien.length() > 20)
            throw new IllegalArgumentException("MaNhanVien tối đa 20 ký tự.");
        NhanVien nhanVien = new NhanVien();
        nhanVien.setMaNhanVien(maNhanVien);
        this.maNhanVien = nhanVien;
    }

}

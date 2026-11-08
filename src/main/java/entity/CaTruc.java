package entity;

import java.time.LocalDate;
import java.time.LocalTime;

public class CaTruc {
    private String maCa;
    private LocalDate ngay;
    private LocalTime gioBatDau;
    private LocalTime gioKetThuc;
    private String loaiCa;
    private NhanVien maNhanVien;
    private PhongKham maPhong;

    public CaTruc() {
    }

    public CaTruc(String maCa, LocalDate ngay, LocalTime gioBatDau, LocalTime gioKetThuc,
            String loaiCa, String maNhanVien, String maPhong) {
        setMaCa(maCa);
        setNgay(ngay);
        setKhoangGio(gioBatDau, gioKetThuc);
        setLoaiCa(loaiCa);
        setMaNhanVien(maNhanVien);
        setMaPhong(maPhong);
    }

    public String getMaCa() {
        return maCa;
    }

    public void setMaCa(String maCa) {
        if (maCa == null)
            throw new IllegalArgumentException("MaCa không được null.");
        if (maCa != null && maCa.length() > 20)
            throw new IllegalArgumentException("MaCa tối đa 20 ký tự.");
        this.maCa = maCa;
    }

    public LocalDate getNgay() {
        return ngay;
    }

    public void setNgay(LocalDate ngay) {
        if (ngay == null)
            throw new IllegalArgumentException("Ngay không được null.");
        this.ngay = ngay;
    }

    public LocalTime getGioBatDau() {
        return gioBatDau;
    }

    public void setKhoangGio(LocalTime gioBatDau, LocalTime gioKetThuc) {
        if (gioBatDau == null)
            throw new IllegalArgumentException("GioBatDau không được null.");
        if (gioKetThuc == null)
            throw new IllegalArgumentException("GioKetThuc không được null.");
        if (!gioKetThuc.isAfter(gioBatDau))
            throw new IllegalArgumentException("GioKetThuc phải sau GioBatDau.");
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
    }

    public void setGioBatDau(LocalTime gioBatDau) {
        if (gioBatDau == null)
            throw new IllegalArgumentException("GioBatDau không được null.");
        this.gioBatDau = gioBatDau;
    }

    public LocalTime getGioKetThuc() {
        return gioKetThuc;
    }

    public void setGioKetThuc(LocalTime gioKetThuc) {
        if (gioKetThuc == null)
            throw new IllegalArgumentException("GioKetThuc không được null.");
        this.gioKetThuc = gioKetThuc;
    }

    public String getLoaiCa() {
        return loaiCa;
    }

    public void setLoaiCa(String loaiCa) {
        if (loaiCa != null && loaiCa.length() > 30)
            throw new IllegalArgumentException("LoaiCa tối đa 30 ký tự.");
        this.loaiCa = loaiCa;
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

    public PhongKham getMaPhong() {
        return maPhong;
    }

    public String getMaPhongId() {
        return maPhong == null ? null : maPhong.getMaPhong();
    }

    public PhongKham getPhongKham() {
        return maPhong;
    }

    public void setPhongKham(PhongKham phongKham) {
        if (phongKham == null)
            throw new IllegalArgumentException("PhongKham không được null.");
        this.maPhong = phongKham;
    }

    public void setMaPhong(PhongKham phongKham) {
        setPhongKham(phongKham);
    }

    public void setMaPhong(String maPhong) {
        if (maPhong == null)
            throw new IllegalArgumentException("MaPhong không được null.");
        if (maPhong != null && maPhong.length() > 20)
            throw new IllegalArgumentException("MaPhong tối đa 20 ký tự.");
        PhongKham phongKham = new PhongKham();
        phongKham.setMaPhong(maPhong);
        this.maPhong = phongKham;
    }

}

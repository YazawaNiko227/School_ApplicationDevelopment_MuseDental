package entity;

import java.time.LocalDate;

public class NhanVien {
    private String maNhanVien;
    private String hoTen;
    private LocalDate ngaySinh;
    private String gioiTinh;
    private String soDienThoai;
    private String email;
    private String diaChi;
    private LocalDate ngayVaoLam;
    private String trangThaiLamViec;
    private String chuyenMon;
    private String soChungChiHanhNghe;

    public NhanVien() {
        this.trangThaiLamViec = "Dang lam viec";
    }

    public NhanVien(String maNhanVien, String hoTen, LocalDate ngaySinh, String gioiTinh, String soDienThoai, String email, String diaChi, LocalDate ngayVaoLam, String trangThaiLamViec, String chuyenMon, String soChungChiHanhNghe) {
        setMaNhanVien(maNhanVien);
        setHoTen(hoTen);
        setNgaySinh(ngaySinh);
        setGioiTinh(gioiTinh);
        setSoDienThoai(soDienThoai);
        setEmail(email);
        setDiaChi(diaChi);
        setNgayVaoLam(ngayVaoLam);
        setTrangThaiLamViec(trangThaiLamViec);
        setChuyenMon(chuyenMon);
        setSoChungChiHanhNghe(soChungChiHanhNghe);
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        if (maNhanVien == null) throw new IllegalArgumentException("MaNhanVien không được null.");
        if (maNhanVien != null && maNhanVien.length() > 20) throw new IllegalArgumentException("MaNhanVien tối đa 20 ký tự.");
        this.maNhanVien = maNhanVien;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        if (hoTen == null) throw new IllegalArgumentException("HoTen không được null.");
        if (hoTen != null && hoTen.length() > 100) throw new IllegalArgumentException("HoTen tối đa 100 ký tự.");
        this.hoTen = hoTen;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        if (gioiTinh != null && gioiTinh.length() > 10) throw new IllegalArgumentException("GioiTinh tối đa 10 ký tự.");
        this.gioiTinh = gioiTinh;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        if (soDienThoai != null && soDienThoai.length() > 15) throw new IllegalArgumentException("SoDienThoai tối đa 15 ký tự.");
        this.soDienThoai = soDienThoai;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email != null && email.length() > 100) throw new IllegalArgumentException("Email tối đa 100 ký tự.");
        this.email = email;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        if (diaChi != null && diaChi.length() > 200) throw new IllegalArgumentException("DiaChi tối đa 200 ký tự.");
        this.diaChi = diaChi;
    }

    public LocalDate getNgayVaoLam() {
        return ngayVaoLam;
    }

    public void setNgayVaoLam(LocalDate ngayVaoLam) {
        this.ngayVaoLam = ngayVaoLam;
    }

    public String getTrangThaiLamViec() {
        return trangThaiLamViec;
    }

    public void setTrangThaiLamViec(String trangThaiLamViec) {
        if (trangThaiLamViec == null) throw new IllegalArgumentException("TrangThaiLamViec không được null.");
        if (trangThaiLamViec != null && trangThaiLamViec.length() > 30) throw new IllegalArgumentException("TrangThaiLamViec tối đa 30 ký tự.");
        this.trangThaiLamViec = trangThaiLamViec;
    }

    public String getChuyenMon() {
        return chuyenMon;
    }

    public void setChuyenMon(String chuyenMon) {
        if (chuyenMon != null && chuyenMon.length() > 100) throw new IllegalArgumentException("ChuyenMon tối đa 100 ký tự.");
        this.chuyenMon = chuyenMon;
    }

    public String getSoChungChiHanhNghe() {
        return soChungChiHanhNghe;
    }

    public void setSoChungChiHanhNghe(String soChungChiHanhNghe) {
        if (soChungChiHanhNghe != null && soChungChiHanhNghe.length() > 50) throw new IllegalArgumentException("SoChungChiHanhNghe tối đa 50 ký tự.");
        this.soChungChiHanhNghe = soChungChiHanhNghe;
    }

}

package entity;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entity ánh xạ bảng NhanVien trong CSDL QuanLyPhongKhamMuse.
 * Nếu project đã có sẵn lớp này thì chỉ cần đảm bảo có đủ constructor và
 * getter/setter như bên dưới để DanhMucQuanLyNhanVienPanel biên dịch được.
 */
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
    }

    public NhanVien(String maNhanVien, String hoTen, LocalDate ngaySinh, String gioiTinh,
            String soDienThoai, String email, String diaChi, LocalDate ngayVaoLam,
            String trangThaiLamViec, String chuyenMon, String soChungChiHanhNghe) {
        this.maNhanVien = maNhanVien;
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.diaChi = diaChi;
        this.ngayVaoLam = ngayVaoLam;
        this.trangThaiLamViec = trangThaiLamViec;
        this.chuyenMon = chuyenMon;
        this.soChungChiHanhNghe = soChungChiHanhNghe;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
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
        this.gioiTinh = gioiTinh;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
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
        this.trangThaiLamViec = trangThaiLamViec;
    }

    public String getChuyenMon() {
        return chuyenMon;
    }

    public void setChuyenMon(String chuyenMon) {
        this.chuyenMon = chuyenMon;
    }

    public String getSoChungChiHanhNghe() {
        return soChungChiHanhNghe;
    }

    public void setSoChungChiHanhNghe(String soChungChiHanhNghe) {
        this.soChungChiHanhNghe = soChungChiHanhNghe;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NhanVien)) {
            return false;
        }
        return Objects.equals(maNhanVien, ((NhanVien) o).maNhanVien);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maNhanVien);
    }
}
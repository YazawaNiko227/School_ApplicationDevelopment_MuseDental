package entity;

import java.time.LocalDate;

/**
 * Entity ánh xạ bảng PhieuXuatKho: phiếu xuất vật tư y tế, vật liệu tiêu hao
 * phục vụ cho một lịch hẹn khám hoặc xuất dùng chung phòng khám.
 */
public class PhieuXuatKho {
    private String maPhieuXuat;
    private LocalDate ngayXuat;
    private String maNhanVien;
    private String maLichHen;

    public PhieuXuatKho() {
        this.ngayXuat = LocalDate.now();
    }

    public PhieuXuatKho(String maPhieuXuat, LocalDate ngayXuat, String maNhanVien, String maLichHen) {
        setMaPhieuXuat(maPhieuXuat);
        setNgayXuat(ngayXuat);
        setMaNhanVien(maNhanVien);
        setMaLichHen(maLichHen);
    }

    public String getMaPhieuXuat() {
        return maPhieuXuat;
    }

    public void setMaPhieuXuat(String maPhieuXuat) {
        if (maPhieuXuat == null || maPhieuXuat.trim().isEmpty())
            throw new IllegalArgumentException("MaPhieuXuat không được để trống.");
        if (maPhieuXuat.length() > 20)
            throw new IllegalArgumentException("MaPhieuXuat tối đa 20 ký tự.");
        this.maPhieuXuat = maPhieuXuat.trim();
    }

    public LocalDate getNgayXuat() {
        return ngayXuat;
    }

    public void setNgayXuat(LocalDate ngayXuat) {
        if (ngayXuat == null)
            throw new IllegalArgumentException("NgayXuat không được null.");
        this.ngayXuat = ngayXuat;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        if (maNhanVien == null || maNhanVien.trim().isEmpty())
            throw new IllegalArgumentException("MaNhanVien không được để trống.");
        if (maNhanVien.length() > 20)
            throw new IllegalArgumentException("MaNhanVien tối đa 20 ký tự.");
        this.maNhanVien = maNhanVien.trim();
    }

    public String getMaLichHen() {
        return maLichHen;
    }

    public void setMaLichHen(String maLichHen) {
        if (maLichHen != null && maLichHen.length() > 20)
            throw new IllegalArgumentException("MaLichHen tối đa 20 ký tự.");
        this.maLichHen = maLichHen == null || maLichHen.trim().isEmpty() ? null : maLichHen.trim();
    }
}

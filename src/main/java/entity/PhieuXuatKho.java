package entity;

import java.time.LocalDate;

public class PhieuXuatKho {
    private String maPhieuXuat;
    private LocalDate ngayXuat;
    private String maNhanVien;
    private String maLichHen;

    public PhieuXuatKho() {
        this.ngayXuat = LocalDate.now();
    }

    public PhieuXuatKho(String maPhieuXuat, LocalDate ngayXuat, String maNhanVien, String maLichHen) {
        this.maPhieuXuat = maPhieuXuat;
        this.ngayXuat = ngayXuat;
        this.maNhanVien = maNhanVien;
        this.maLichHen = maLichHen;
    }

    public String getMaPhieuXuat() {
        return maPhieuXuat;
    }

    public void setMaPhieuXuat(String maPhieuXuat) {
        this.maPhieuXuat = maPhieuXuat;
    }

    public LocalDate getNgayXuat() {
        return ngayXuat;
    }

    public void setNgayXuat(LocalDate ngayXuat) {
        this.ngayXuat = ngayXuat;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public String getMaLichHen() {
        return maLichHen;
    }

    public void setMaLichHen(String maLichHen) {
        this.maLichHen = maLichHen;
    }
}

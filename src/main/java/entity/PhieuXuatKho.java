package entity;

import java.time.LocalDate;

/**
 * Entity ánh xạ bảng PhieuXuatKho: phiếu xuất vật tư y tế, vật liệu tiêu hao
 * phục vụ cho một lịch hẹn khám hoặc xuất dùng chung phòng khám.
 */
public class PhieuXuatKho {
    private String maPhieuXuat;
    private LocalDate ngayXuat;
    private NhanVien maNhanVien;
    private LichHen maLichHen;
    private int soLoaiVatTu;
    private int tongSoLuong;

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
        if (maNhanVien == null || maNhanVien.trim().isEmpty())
            throw new IllegalArgumentException("MaNhanVien không được để trống.");
        if (maNhanVien.length() > 20)
            throw new IllegalArgumentException("MaNhanVien tối đa 20 ký tự.");
        NhanVien nhanVien = new NhanVien();
        nhanVien.setMaNhanVien(maNhanVien.trim());
        this.maNhanVien = nhanVien;
    }

    public LichHen getMaLichHen() {
        return maLichHen;
    }

    public String getMaLichHenId() {
        return maLichHen == null ? null : maLichHen.getMaLichHen();
    }

    public LichHen getLichHen() {
        return maLichHen;
    }

    public void setLichHen(LichHen lichHen) {
        this.maLichHen = lichHen;
    }

    public void setMaLichHen(LichHen lichHen) {
        setLichHen(lichHen);
    }

    public void setMaLichHen(String maLichHen) {
        if (maLichHen != null && maLichHen.length() > 20)
            throw new IllegalArgumentException("MaLichHen tối đa 20 ký tự.");
        if (maLichHen == null || maLichHen.trim().isEmpty()) {
            this.maLichHen = null;
        } else {
            LichHen lichHen = new LichHen();
            lichHen.setMaLichHen(maLichHen.trim());
            this.maLichHen = lichHen;
        }
    }

    public int getSoLoaiVatTu() {
        return soLoaiVatTu;
    }

    public void setSoLoaiVatTu(int soLoaiVatTu) {
        this.soLoaiVatTu = soLoaiVatTu;
    }

    public int getTongSoLuong() {
        return tongSoLuong;
    }

    public void setTongSoLuong(int tongSoLuong) {
        this.tongSoLuong = tongSoLuong;
    }
}

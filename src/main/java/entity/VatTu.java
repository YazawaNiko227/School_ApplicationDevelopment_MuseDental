package entity;

import java.time.LocalDate;

/**
 * Entity ánh xạ bảng VatTu: thuốc và vật tư y tế dùng chung bảng, phân biệt
 * bằng LoaiVatTu ("Thuoc", "Tieu hao", "Vat lieu").
 */
public class VatTu {
    private String maVatTu;
    private String tenVatTu;
    private String loaiVatTu;
    private String donViTinh;
    private int soLuongTon;
    private int nguongToiThieu;
    private LocalDate hanSuDung;
    private double donGiaBan;

    public VatTu() {
    }

    public VatTu(String maVatTu, String tenVatTu, String loaiVatTu, String donViTinh, int soLuongTon,
            int nguongToiThieu, LocalDate hanSuDung, double donGiaBan) {
        setMaVatTu(maVatTu);
        setTenVatTu(tenVatTu);
        setLoaiVatTu(loaiVatTu);
        setDonViTinh(donViTinh);
        setSoLuongTon(soLuongTon);
        setNguongToiThieu(nguongToiThieu);
        setHanSuDung(hanSuDung);
        setDonGiaBan(donGiaBan);
    }

    public String getMaVatTu() {
        return maVatTu;
    }

    public void setMaVatTu(String maVatTu) {
        if (maVatTu == null || maVatTu.trim().isEmpty())
            throw new IllegalArgumentException("MaVatTu không được để trống.");
        if (maVatTu.length() > 20)
            throw new IllegalArgumentException("MaVatTu tối đa 20 ký tự.");
        this.maVatTu = maVatTu.trim();
    }

    public String getTenVatTu() {
        return tenVatTu;
    }

    public void setTenVatTu(String tenVatTu) {
        if (tenVatTu == null || tenVatTu.trim().isEmpty())
            throw new IllegalArgumentException("TenVatTu không được để trống.");
        if (tenVatTu.length() > 150)
            throw new IllegalArgumentException("TenVatTu tối đa 150 ký tự.");
        this.tenVatTu = tenVatTu.trim();
    }

    public String getLoaiVatTu() {
        return loaiVatTu;
    }

    public void setLoaiVatTu(String loaiVatTu) {
        if (loaiVatTu != null && loaiVatTu.length() > 50)
            throw new IllegalArgumentException("LoaiVatTu tối đa 50 ký tự.");
        this.loaiVatTu = loaiVatTu;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        if (donViTinh != null && donViTinh.length() > 20)
            throw new IllegalArgumentException("DonViTinh tối đa 20 ký tự.");
        this.donViTinh = donViTinh;
    }

    public int getSoLuongTon() {
        return soLuongTon;
    }

    public void setSoLuongTon(int soLuongTon) {
        if (soLuongTon < 0)
            throw new IllegalArgumentException("SoLuongTon phải >= 0.");
        this.soLuongTon = soLuongTon;
    }

    public int getNguongToiThieu() {
        return nguongToiThieu;
    }

    public void setNguongToiThieu(int nguongToiThieu) {
        this.nguongToiThieu = nguongToiThieu;
    }

    public LocalDate getHanSuDung() {
        return hanSuDung;
    }

    public void setHanSuDung(LocalDate hanSuDung) {
        this.hanSuDung = hanSuDung;
    }

    public double getDonGiaBan() {
        return donGiaBan;
    }

    public void setDonGiaBan(double donGiaBan) {
        if (donGiaBan < 0)
            throw new IllegalArgumentException("DonGiaBan phải >= 0.");
        if (!Double.isFinite(donGiaBan))
            throw new IllegalArgumentException("DonGiaBan phải là số hữu hạn.");
        this.donGiaBan = donGiaBan;
    }
}

package entity;

import java.time.LocalDate;

public class VatTu {
    private String maVatTu;
    private String tenVatTu;
    private String loaiVatTu;
    private String donViTinh;
    private int soLuongTon;
    private int nguongToiThieu;
    private LocalDate hanSuDung;

    public VatTu() {
    }

    public VatTu(String maVatTu, String tenVatTu, String loaiVatTu, String donViTinh,
            int soLuongTon, int nguongToiThieu, LocalDate hanSuDung) {
        this.maVatTu = maVatTu;
        this.tenVatTu = tenVatTu;
        this.loaiVatTu = loaiVatTu;
        this.donViTinh = donViTinh;
        this.soLuongTon = soLuongTon;
        this.nguongToiThieu = nguongToiThieu;
        this.hanSuDung = hanSuDung;
    }

    /** Tồn kho đã chạm hoặc thấp hơn ngưỡng tối thiểu. */
    public boolean isSapHetHang() {
        return soLuongTon <= nguongToiThieu;
    }

    public boolean isHetHan() {
        return hanSuDung != null && hanSuDung.isBefore(LocalDate.now());
    }

    /** Còn hạn nhưng sẽ hết trong vòng 30 ngày tới. */
    public boolean isSapHetHan() {
        return hanSuDung != null && !isHetHan() && hanSuDung.isBefore(LocalDate.now().plusDays(30));
    }

    public String getMaVatTu() {
        return maVatTu;
    }

    public void setMaVatTu(String maVatTu) {
        this.maVatTu = maVatTu;
    }

    public String getTenVatTu() {
        return tenVatTu;
    }

    public void setTenVatTu(String tenVatTu) {
        this.tenVatTu = tenVatTu;
    }

    public String getLoaiVatTu() {
        return loaiVatTu;
    }

    public void setLoaiVatTu(String loaiVatTu) {
        this.loaiVatTu = loaiVatTu;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public int getSoLuongTon() {
        return soLuongTon;
    }

    public void setSoLuongTon(int soLuongTon) {
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
}

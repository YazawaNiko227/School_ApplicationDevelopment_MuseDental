package entity;

public class ChiTietXuatKho {
    private String maPhieuXuat;
    private String maVatTu;
    private int soLuongSuDung;

    public ChiTietXuatKho() {
    }

    public ChiTietXuatKho(String maPhieuXuat, String maVatTu, int soLuongSuDung) {
        this.maPhieuXuat = maPhieuXuat;
        this.maVatTu = maVatTu;
        this.soLuongSuDung = soLuongSuDung;
    }

    public String getMaPhieuXuat() {
        return maPhieuXuat;
    }

    public void setMaPhieuXuat(String maPhieuXuat) {
        this.maPhieuXuat = maPhieuXuat;
    }

    public String getMaVatTu() {
        return maVatTu;
    }

    public void setMaVatTu(String maVatTu) {
        this.maVatTu = maVatTu;
    }

    public int getSoLuongSuDung() {
        return soLuongSuDung;
    }

    public void setSoLuongSuDung(int soLuongSuDung) {
        this.soLuongSuDung = soLuongSuDung;
    }
}

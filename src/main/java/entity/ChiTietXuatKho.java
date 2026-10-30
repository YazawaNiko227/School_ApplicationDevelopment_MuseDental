package entity;

/**
 * Entity ánh xạ bảng ChiTietXuatKho: một dòng vật tư tiêu hao trong phiếu xuất
 * (khóa chính phức hợp (MaPhieuXuat, MaVatTu)).
 */
public class ChiTietXuatKho {
    private String maPhieuXuat;
    private String maVatTu;
    private int soLuongSuDung;

    public ChiTietXuatKho() {
    }

    public ChiTietXuatKho(String maPhieuXuat, String maVatTu, int soLuongSuDung) {
        setMaPhieuXuat(maPhieuXuat);
        setMaVatTu(maVatTu);
        setSoLuongSuDung(soLuongSuDung);
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

    public int getSoLuongSuDung() {
        return soLuongSuDung;
    }

    public void setSoLuongSuDung(int soLuongSuDung) {
        if (soLuongSuDung <= 0)
            throw new IllegalArgumentException("SoLuongSuDung phải > 0.");
        this.soLuongSuDung = soLuongSuDung;
    }
}

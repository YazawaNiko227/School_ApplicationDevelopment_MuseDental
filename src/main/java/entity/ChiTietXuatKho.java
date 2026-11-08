package entity;

/**
 * Entity ánh xạ bảng ChiTietXuatKho: một dòng vật tư tiêu hao trong phiếu xuất
 * (khóa chính phức hợp (MaPhieuXuat, MaVatTu)).
 */
public class ChiTietXuatKho {
    private PhieuXuatKho maPhieuXuat;
    private VatTu maVatTu;
    private int soLuongSuDung;

    public ChiTietXuatKho() {
    }

    public ChiTietXuatKho(String maPhieuXuat, String maVatTu, int soLuongSuDung) {
        setMaPhieuXuat(maPhieuXuat);
        setMaVatTu(maVatTu);
        setSoLuongSuDung(soLuongSuDung);
    }

    public PhieuXuatKho getMaPhieuXuat() {
        return maPhieuXuat;
    }

    public String getMaPhieuXuatId() {
        return maPhieuXuat == null ? null : maPhieuXuat.getMaPhieuXuat();
    }

    public PhieuXuatKho getPhieuXuatKho() {
        return maPhieuXuat;
    }

    public void setPhieuXuatKho(PhieuXuatKho phieuXuatKho) {
        if (phieuXuatKho == null)
            throw new IllegalArgumentException("PhieuXuatKho không được null.");
        this.maPhieuXuat = phieuXuatKho;
    }

    public void setMaPhieuXuat(PhieuXuatKho phieuXuatKho) {
        setPhieuXuatKho(phieuXuatKho);
    }

    public void setMaPhieuXuat(String maPhieuXuat) {
        if (maPhieuXuat == null || maPhieuXuat.trim().isEmpty())
            throw new IllegalArgumentException("MaPhieuXuat không được để trống.");
        if (maPhieuXuat.length() > 20)
            throw new IllegalArgumentException("MaPhieuXuat tối đa 20 ký tự.");
        PhieuXuatKho phieuXuatKho = new PhieuXuatKho();
        phieuXuatKho.setMaPhieuXuat(maPhieuXuat.trim());
        this.maPhieuXuat = phieuXuatKho;
    }

    public VatTu getMaVatTu() {
        return maVatTu;
    }

    public String getMaVatTuId() {
        return maVatTu == null ? null : maVatTu.getMaVatTu();
    }

    public VatTu getVatTu() {
        return maVatTu;
    }

    public void setVatTu(VatTu vatTu) {
        if (vatTu == null)
            throw new IllegalArgumentException("VatTu không được null.");
        this.maVatTu = vatTu;
    }

    public void setMaVatTu(VatTu vatTu) {
        setVatTu(vatTu);
    }

    public void setMaVatTu(String maVatTu) {
        if (maVatTu == null || maVatTu.trim().isEmpty())
            throw new IllegalArgumentException("MaVatTu không được để trống.");
        if (maVatTu.length() > 20)
            throw new IllegalArgumentException("MaVatTu tối đa 20 ký tự.");
        VatTu vatTu = new VatTu();
        vatTu.setMaVatTu(maVatTu.trim());
        this.maVatTu = vatTu;
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

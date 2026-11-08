package entity;

/**
 * Entity ánh xạ bảng ChiTietDonThuoc: một dòng thuốc trong đơn
 * (khóa chính (MaDonThuoc, MaVatTu)).
 */
public class ChiTietDonThuoc {
    private DonThuoc maDonThuoc;
    private VatTu maVatTu;
    private String lieuDung;
    private String duongDung;
    private int soLuong;

    public ChiTietDonThuoc() {
    }

    public ChiTietDonThuoc(String maDonThuoc, String maVatTu, String lieuDung, String duongDung, int soLuong) {
        setMaDonThuoc(maDonThuoc);
        setMaVatTu(maVatTu);
        setLieuDung(lieuDung);
        setDuongDung(duongDung);
        setSoLuong(soLuong);
    }

    public DonThuoc getMaDonThuoc() {
        return maDonThuoc;
    }

    public String getMaDonThuocId() {
        return maDonThuoc == null ? null : maDonThuoc.getMaDonThuoc();
    }

    public DonThuoc getDonThuoc() {
        return maDonThuoc;
    }

    public void setDonThuoc(DonThuoc donThuoc) {
        if (donThuoc == null)
            throw new IllegalArgumentException("DonThuoc không được null.");
        this.maDonThuoc = donThuoc;
    }

    public void setMaDonThuoc(DonThuoc donThuoc) {
        setDonThuoc(donThuoc);
    }

    public void setMaDonThuoc(String maDonThuoc) {
        if (maDonThuoc == null)
            throw new IllegalArgumentException("MaDonThuoc không được null.");
        if (maDonThuoc.length() > 20)
            throw new IllegalArgumentException("MaDonThuoc tối đa 20 ký tự.");
        DonThuoc donThuoc = new DonThuoc();
        donThuoc.setMaDonThuoc(maDonThuoc);
        this.maDonThuoc = donThuoc;
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
        if (maVatTu == null)
            throw new IllegalArgumentException("MaVatTu không được null.");
        if (maVatTu.length() > 20)
            throw new IllegalArgumentException("MaVatTu tối đa 20 ký tự.");
        VatTu vatTu = new VatTu();
        vatTu.setMaVatTu(maVatTu);
        this.maVatTu = vatTu;
    }

    public String getLieuDung() {
        return lieuDung;
    }

    public void setLieuDung(String lieuDung) {
        if (lieuDung != null && lieuDung.length() > 100)
            throw new IllegalArgumentException("LieuDung tối đa 100 ký tự.");
        this.lieuDung = lieuDung;
    }

    public String getDuongDung() {
        return duongDung;
    }

    public void setDuongDung(String duongDung) {
        if (duongDung != null && duongDung.length() > 50)
            throw new IllegalArgumentException("DuongDung tối đa 50 ký tự.");
        this.duongDung = duongDung;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        if (soLuong <= 0)
            throw new IllegalArgumentException("SoLuong phải > 0.");
        this.soLuong = soLuong;
    }
}

package entity;


public class ChiTietDonThuoc {
    private String maDonThuoc;
    private String maVatTu;
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

    public String getMaDonThuoc() {
        return maDonThuoc;
    }

    public void setMaDonThuoc(String maDonThuoc) {
        if (maDonThuoc == null) throw new IllegalArgumentException("MaDonThuoc không được null.");
        if (maDonThuoc != null && maDonThuoc.length() > 20) throw new IllegalArgumentException("MaDonThuoc tối đa 20 ký tự.");
        this.maDonThuoc = maDonThuoc;
    }

    public String getMaVatTu() {
        return maVatTu;
    }

    public void setMaVatTu(String maVatTu) {
        if (maVatTu == null) throw new IllegalArgumentException("MaVatTu không được null.");
        if (maVatTu != null && maVatTu.length() > 20) throw new IllegalArgumentException("MaVatTu tối đa 20 ký tự.");
        this.maVatTu = maVatTu;
    }

    public String getLieuDung() {
        return lieuDung;
    }

    public void setLieuDung(String lieuDung) {
        if (lieuDung != null && lieuDung.length() > 100) throw new IllegalArgumentException("LieuDung tối đa 100 ký tự.");
        this.lieuDung = lieuDung;
    }

    public String getDuongDung() {
        return duongDung;
    }

    public void setDuongDung(String duongDung) {
        if (duongDung != null && duongDung.length() > 50) throw new IllegalArgumentException("DuongDung tối đa 50 ký tự.");
        this.duongDung = duongDung;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        if (soLuong <= 0) throw new IllegalArgumentException("SoLuong phải > 0.");
        this.soLuong = soLuong;
    }

}

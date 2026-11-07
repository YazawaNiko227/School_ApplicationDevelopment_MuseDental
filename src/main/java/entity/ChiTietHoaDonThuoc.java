package entity;

public class ChiTietHoaDonThuoc {
    private HoaDon maHoaDon;
    private VatTu maVatTu;
    private int soLuong;
    private double donGiaTaiThoiDiem;

    public ChiTietHoaDonThuoc() {
    }

    public ChiTietHoaDonThuoc(String maHoaDon, String maVatTu, int soLuong, double donGiaTaiThoiDiem) {
        setMaHoaDon(maHoaDon);
        setMaVatTu(maVatTu);
        setSoLuong(soLuong);
        setDonGiaTaiThoiDiem(donGiaTaiThoiDiem);
    }

    public HoaDon getMaHoaDon() {
        return maHoaDon;
    }

    public String getMaHoaDonId() {
        return maHoaDon == null ? null : maHoaDon.getMaHoaDon();
    }

    public HoaDon getHoaDon() {
        return maHoaDon;
    }

    public void setHoaDon(HoaDon hoaDon) {
        if (hoaDon == null) throw new IllegalArgumentException("hoaDon không được null.");
        this.maHoaDon = hoaDon;
    }

    public void setMaHoaDon(HoaDon hoaDon) { setHoaDon(hoaDon); }

    public void setMaHoaDon(String maHoaDon) {
        if (maHoaDon == null || maHoaDon.trim().isEmpty()) throw new IllegalArgumentException("maHoaDon không được để trống");
        if (maHoaDon != null && maHoaDon.length() > 20) throw new IllegalArgumentException("maHoaDon tối đa 20 ký tự");
        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaHoaDon(maHoaDon);
        this.maHoaDon = hoaDon;
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
        if (vatTu == null) throw new IllegalArgumentException("vatTu không được null.");
        this.maVatTu = vatTu;
    }

    public void setMaVatTu(VatTu vatTu) { setVatTu(vatTu); }

    public void setMaVatTu(String maVatTu) {
        if (maVatTu == null || maVatTu.trim().isEmpty()) throw new IllegalArgumentException("maVatTu không được để trống");
        if (maVatTu != null && maVatTu.length() > 20) throw new IllegalArgumentException("maVatTu tối đa 20 ký tự");
        VatTu vatTu = new VatTu();
        vatTu.setMaVatTu(maVatTu);
        this.maVatTu = vatTu;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        if (soLuong <= 0) throw new IllegalArgumentException("soLuong phải > 0");
        this.soLuong = soLuong;
    }

    public double getDonGiaTaiThoiDiem() {
        return donGiaTaiThoiDiem;
    }

    public void setDonGiaTaiThoiDiem(double donGiaTaiThoiDiem) {
        if (donGiaTaiThoiDiem < 0) throw new IllegalArgumentException("donGiaTaiThoiDiem phải >= 0");
        if (!Double.isFinite(donGiaTaiThoiDiem)) throw new IllegalArgumentException("donGiaTaiThoiDiem phải là số hữu hạn");
        this.donGiaTaiThoiDiem = donGiaTaiThoiDiem;
    }

}

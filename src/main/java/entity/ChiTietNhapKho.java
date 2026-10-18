package entity;


public class ChiTietNhapKho {
    private String maPhieuNhap;
    private String maVatTu;
    private int soLuong;
    private double donGiaNhap;

    public ChiTietNhapKho() {
    }

    public ChiTietNhapKho(String maPhieuNhap, String maVatTu, int soLuong, double donGiaNhap) {
        setMaPhieuNhap(maPhieuNhap);
        setMaVatTu(maVatTu);
        setSoLuong(soLuong);
        setDonGiaNhap(donGiaNhap);
    }

    public String getMaPhieuNhap() {
        return maPhieuNhap;
    }

    public void setMaPhieuNhap(String maPhieuNhap) {
        if (maPhieuNhap == null) throw new IllegalArgumentException("MaPhieuNhap không được null.");
        if (maPhieuNhap != null && maPhieuNhap.length() > 20) throw new IllegalArgumentException("MaPhieuNhap tối đa 20 ký tự.");
        this.maPhieuNhap = maPhieuNhap;
    }

    public String getMaVatTu() {
        return maVatTu;
    }

    public void setMaVatTu(String maVatTu) {
        if (maVatTu == null) throw new IllegalArgumentException("MaVatTu không được null.");
        if (maVatTu != null && maVatTu.length() > 20) throw new IllegalArgumentException("MaVatTu tối đa 20 ký tự.");
        this.maVatTu = maVatTu;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        if (soLuong <= 0) throw new IllegalArgumentException("SoLuong phải > 0.");
        this.soLuong = soLuong;
    }

    public double getDonGiaNhap() {
        return donGiaNhap;
    }

    public void setDonGiaNhap(double donGiaNhap) {
        if (!Double.isFinite(donGiaNhap)) throw new IllegalArgumentException("DonGiaNhap phải là số hữu hạn.");
        if (donGiaNhap < 0) throw new IllegalArgumentException("DonGiaNhap phải >= 0.");
        this.donGiaNhap = donGiaNhap;
    }

}

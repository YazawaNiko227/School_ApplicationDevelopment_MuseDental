package entity;


public class ChiTietNhapKho {
    private PhieuNhapKho maPhieuNhap;
    private VatTu maVatTu;
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

    public PhieuNhapKho getMaPhieuNhap() {
        return maPhieuNhap;
    }

    public String getMaPhieuNhapId() {
        return maPhieuNhap == null ? null : maPhieuNhap.getMaPhieuNhap();
    }

    public PhieuNhapKho getPhieuNhapKho() {
        return maPhieuNhap;
    }

    public void setPhieuNhapKho(PhieuNhapKho phieuNhapKho) {
        if (phieuNhapKho == null)
            throw new IllegalArgumentException("PhieuNhapKho không được null.");
        this.maPhieuNhap = phieuNhapKho;
    }

    public void setMaPhieuNhap(PhieuNhapKho phieuNhapKho) { setPhieuNhapKho(phieuNhapKho); }

    public void setMaPhieuNhap(String maPhieuNhap) {
        if (maPhieuNhap == null) throw new IllegalArgumentException("MaPhieuNhap không được null.");
        if (maPhieuNhap != null && maPhieuNhap.length() > 20) throw new IllegalArgumentException("MaPhieuNhap tối đa 20 ký tự.");
        PhieuNhapKho phieuNhapKho = new PhieuNhapKho();
        phieuNhapKho.setMaPhieuNhap(maPhieuNhap);
        this.maPhieuNhap = phieuNhapKho;
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

    public void setMaVatTu(VatTu vatTu) { setVatTu(vatTu); }

    public void setMaVatTu(String maVatTu) {
        if (maVatTu == null) throw new IllegalArgumentException("MaVatTu không được null.");
        if (maVatTu != null && maVatTu.length() > 20) throw new IllegalArgumentException("MaVatTu tối đa 20 ký tự.");
        VatTu vatTu = new VatTu();
        vatTu.setMaVatTu(maVatTu);
        this.maVatTu = vatTu;
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

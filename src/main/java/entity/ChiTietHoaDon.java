package entity;


public class ChiTietHoaDon {
    private String maHoaDon;
    private String maDichVu;
    private int soLuong;
    private double donGiaTaiThoiDiem;

    public ChiTietHoaDon() {
    }

    public ChiTietHoaDon(String maHoaDon, String maDichVu, int soLuong, double donGiaTaiThoiDiem) {
        setMaHoaDon(maHoaDon);
        setMaDichVu(maDichVu);
        setSoLuong(soLuong);
        setDonGiaTaiThoiDiem(donGiaTaiThoiDiem);
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        if (maHoaDon == null) throw new IllegalArgumentException("MaHoaDon không được null.");
        if (maHoaDon != null && maHoaDon.length() > 20) throw new IllegalArgumentException("MaHoaDon tối đa 20 ký tự.");
        this.maHoaDon = maHoaDon;
    }

    public String getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(String maDichVu) {
        if (maDichVu == null) throw new IllegalArgumentException("MaDichVu không được null.");
        if (maDichVu != null && maDichVu.length() > 20) throw new IllegalArgumentException("MaDichVu tối đa 20 ký tự.");
        this.maDichVu = maDichVu;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        if (soLuong <= 0) throw new IllegalArgumentException("SoLuong phải > 0.");
        this.soLuong = soLuong;
    }

    public double getDonGiaTaiThoiDiem() {
        return donGiaTaiThoiDiem;
    }

    public void setDonGiaTaiThoiDiem(double donGiaTaiThoiDiem) {
        if (!Double.isFinite(donGiaTaiThoiDiem)) throw new IllegalArgumentException("DonGiaTaiThoiDiem phải là số hữu hạn.");
        if (donGiaTaiThoiDiem < 0) throw new IllegalArgumentException("DonGiaTaiThoiDiem phải >= 0.");
        this.donGiaTaiThoiDiem = donGiaTaiThoiDiem;
    }

}

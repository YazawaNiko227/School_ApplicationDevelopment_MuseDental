package entity;


public class ChiTietHoaDon {
    private HoaDon maHoaDon;
    private DichVu maDichVu;
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
        if (maHoaDon == null) throw new IllegalArgumentException("MaHoaDon không được null.");
        if (maHoaDon != null && maHoaDon.length() > 20) throw new IllegalArgumentException("MaHoaDon tối đa 20 ký tự.");
        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaHoaDon(maHoaDon);
        this.maHoaDon = hoaDon;
    }

    public DichVu getMaDichVu() {
        return maDichVu;
    }

    public String getMaDichVuId() {
        return maDichVu == null ? null : maDichVu.getMaDichVu();
    }

    public DichVu getDichVu() {
        return maDichVu;
    }

    public void setDichVu(DichVu dichVu) {
        if (dichVu == null) throw new IllegalArgumentException("dichVu không được null.");
        this.maDichVu = dichVu;
    }

    public void setMaDichVu(DichVu dichVu) { setDichVu(dichVu); }

    public void setMaDichVu(String maDichVu) {
        if (maDichVu == null) throw new IllegalArgumentException("MaDichVu không được null.");
        if (maDichVu != null && maDichVu.length() > 20) throw new IllegalArgumentException("MaDichVu tối đa 20 ký tự.");
        DichVu dichVu = new DichVu();
        dichVu.setMaDichVu(maDichVu);
        this.maDichVu = dichVu;
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

package entity;


public class DichVu {
    private String maDichVu;
    private String tenDichVu;
    private double donGia;
    private Integer thoiGianBaoHanh;
    private String moTa;

    public DichVu() {
    }

    public DichVu(String maDichVu, String tenDichVu, double donGia, Integer thoiGianBaoHanh, String moTa) {
        setMaDichVu(maDichVu);
        setTenDichVu(tenDichVu);
        setDonGia(donGia);
        setThoiGianBaoHanh(thoiGianBaoHanh);
        setMoTa(moTa);
    }

    public String getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(String maDichVu) {
        if (maDichVu == null) throw new IllegalArgumentException("MaDichVu không được null.");
        if (maDichVu != null && maDichVu.length() > 20) throw new IllegalArgumentException("MaDichVu tối đa 20 ký tự.");
        this.maDichVu = maDichVu;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public void setTenDichVu(String tenDichVu) {
        if (tenDichVu == null) throw new IllegalArgumentException("TenDichVu không được null.");
        if (tenDichVu != null && tenDichVu.length() > 150) throw new IllegalArgumentException("TenDichVu tối đa 150 ký tự.");
        this.tenDichVu = tenDichVu;
    }

    public double getDonGia() {
        return donGia;
    }

    public void setDonGia(double donGia) {
        if (!Double.isFinite(donGia)) throw new IllegalArgumentException("DonGia phải là số hữu hạn.");
        if (donGia < 0) throw new IllegalArgumentException("DonGia phải >= 0.");
        this.donGia = donGia;
    }

    public Integer getThoiGianBaoHanh() {
        return thoiGianBaoHanh;
    }

    public void setThoiGianBaoHanh(Integer thoiGianBaoHanh) {
        this.thoiGianBaoHanh = thoiGianBaoHanh;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        if (moTa != null && moTa.length() > 500) throw new IllegalArgumentException("MoTa tối đa 500 ký tự.");
        this.moTa = moTa;
    }

}

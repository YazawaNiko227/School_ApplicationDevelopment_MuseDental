package dto;

public class DichVuPhacDoDaLuu {
    private final String maDichVu;
    private final String tenDichVu;
    private final int soLuong;
    private final double donGia;

    public DichVuPhacDoDaLuu(String maDichVu, String tenDichVu, int soLuong, double donGia) {
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    public String getMaDichVu() {
        return maDichVu;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public double getDonGia() {
        return donGia;
    }

    public double getThanhTien() {
        return donGia * soLuong;
    }
}

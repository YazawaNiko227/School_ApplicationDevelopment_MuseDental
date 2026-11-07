package entity;

public class DongTinhTien {
    private final String ma;
    private final String ten;
    private final int soLuong;
    private final double donGia;

    public DongTinhTien(String ma, String ten, int soLuong, double donGia) {
        this.ma = ma;
        this.ten = ten;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    public String ma() { return ma; }
    public String ten() { return ten; }
    public int soLuong() { return soLuong; }
    public double donGia() { return donGia; }
    public double thanhTien() { return soLuong * donGia; }
}

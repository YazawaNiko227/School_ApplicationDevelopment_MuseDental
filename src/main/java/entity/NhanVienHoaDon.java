package entity;

public class NhanVienHoaDon {
    public final String ma;
    public final String ten;

    public NhanVienHoaDon(String ma, String ten) {
        this.ma = ma;
        this.ten = ten;
    }

    @Override
    public String toString() { return ma + " - " + ten; }
}

package entity;

public class NhomDoanhThu {
    private final String tenNhom;
    private final int soHoaDon;
    private final double tongDoanhThu;
    private final double tienDichVu;
    private final double tienThuoc;

    public NhomDoanhThu(String tenNhom, int soHoaDon, double tongDoanhThu,
            double tienDichVu, double tienThuoc) {
        this.tenNhom = tenNhom;
        this.soHoaDon = soHoaDon;
        this.tongDoanhThu = tongDoanhThu;
        this.tienDichVu = tienDichVu;
        this.tienThuoc = tienThuoc;
    }

    public String getTenNhom() { return tenNhom; }
    public int getSoHoaDon() { return soHoaDon; }
    public double getTongDoanhThu() { return tongDoanhThu; }
    public double getTienDichVu() { return tienDichVu; }
    public double getTienThuoc() { return tienThuoc; }
}

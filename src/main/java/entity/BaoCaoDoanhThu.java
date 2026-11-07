package entity;

import java.util.List;

public class BaoCaoDoanhThu {
    private final int soHoaDon;
    private final double tongDoanhThu;
    private final double tienDichVu;
    private final double tienThuoc;
    private final List<NhomDoanhThu> dsNhom;

    public BaoCaoDoanhThu(int soHoaDon, double tongDoanhThu,
            double tienDichVu, double tienThuoc, List<NhomDoanhThu> dsNhom) {
        this.soHoaDon = soHoaDon;
        this.tongDoanhThu = tongDoanhThu;
        this.tienDichVu = tienDichVu;
        this.tienThuoc = tienThuoc;
        this.dsNhom = List.copyOf(dsNhom);
    }

    public int getSoHoaDon() { return soHoaDon; }
    public double getTongDoanhThu() { return tongDoanhThu; }
    public double getTienDichVu() { return tienDichVu; }
    public double getTienThuoc() { return tienThuoc; }
    public List<NhomDoanhThu> getDsNhom() { return dsNhom; }
}

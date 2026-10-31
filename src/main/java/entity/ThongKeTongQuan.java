package entity;

import java.math.BigDecimal;

public final class ThongKeTongQuan {
    private final int lichHenHomNay;
    private final int tongBenhNhan;
    private final int choTiepDon;
    private final BigDecimal doanhThuHomNay;

    public ThongKeTongQuan(int lichHenHomNay, int tongBenhNhan, int choTiepDon, BigDecimal doanhThuHomNay) {
        this.lichHenHomNay = lichHenHomNay;
        this.tongBenhNhan = tongBenhNhan;
        this.choTiepDon = choTiepDon;
        this.doanhThuHomNay = doanhThuHomNay;
    }

    public int getLichHenHomNay() {
        return lichHenHomNay;
    }

    public int getTongBenhNhan() {
        return tongBenhNhan;
    }

    public int getChoTiepDon() {
        return choTiepDon;
    }

    public BigDecimal getDoanhThuHomNay() {
        return doanhThuHomNay;
    }
}

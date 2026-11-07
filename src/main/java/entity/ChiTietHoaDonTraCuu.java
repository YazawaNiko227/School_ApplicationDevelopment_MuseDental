package entity;

import java.util.ArrayList;
import java.util.List;

public class ChiTietHoaDonTraCuu {
    public final HoaDonTraCuu hoaDon;
    public final String maChanDoan;
    public final String ketLuan;
    public final List<DongTinhTien> dichVu = new ArrayList<>();
    public final List<DongTinhTien> thuoc = new ArrayList<>();
    public final List<DotHoaDon> dots = new ArrayList<>();

    public ChiTietHoaDonTraCuu(HoaDonTraCuu hoaDon, String maChanDoan, String ketLuan) {
        this.hoaDon = hoaDon;
        this.maChanDoan = maChanDoan;
        this.ketLuan = ketLuan;
    }
}

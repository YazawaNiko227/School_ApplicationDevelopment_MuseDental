package dto;

import java.time.LocalDate;

public final class HoaDonTraCuuView {
    public final String ma;
    public final LocalDate ngayLap;
    public final String maBenhNhan;
    public final String tenBenhNhan;
    public final String maNhanVien;
    public final String tenNhanVien;
    public final String phuongThuc;
    public final String trangThai;
    public final double tongTienDb;
    public final Object[][] dichVu;
    public final Object[][] thuoc;
    public final Object[][] dots;

    public HoaDonTraCuuView(String ma, LocalDate ngayLap, String maBenhNhan, String tenBenhNhan, String maNhanVien,
            String tenNhanVien, String phuongThuc, String trangThai, Object[][] dichVu, Object[][] thuoc,
            Object[][] dots, double tongTienDb) {
        this.ma = ma;
        this.ngayLap = ngayLap;
        this.maBenhNhan = maBenhNhan;
        this.tenBenhNhan = tenBenhNhan;
        this.maNhanVien = maNhanVien;
        this.tenNhanVien = tenNhanVien;
        this.phuongThuc = phuongThuc;
        this.trangThai = trangThai;
        this.dichVu = dichVu;
        this.thuoc = thuoc;
        this.dots = dots;
        this.tongTienDb = tongTienDb;
    }

    public double tienDichVu() {
        return tong(dichVu);
    }

    public double tienThuoc() {
        return tong(thuoc);
    }

    public double tongTien() {
        return tongTienDb;
    }

    private static double tong(Object[][] dong) {
        double tong = 0;
        for (Object[] d : dong) {
            tong += (Double) d[2] * (Integer) d[3];
        }
        return tong;
    }
}

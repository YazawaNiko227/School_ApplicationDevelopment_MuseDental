package entity;

import java.time.LocalDate;

public class HoaDonTraCuu {
    private final String maHoaDon;
    private final LocalDate ngayLap;
    private final String maBenhNhan;
    private final String tenBenhNhan;
    private final String maNhanVien;
    private final String tenNhanVien;
    private final String phuongThuc;
    private final String trangThai;
    private final double tongTien;

    public HoaDonTraCuu(String maHoaDon, LocalDate ngayLap, String maBenhNhan, String tenBenhNhan,
            String maNhanVien, String tenNhanVien, String phuongThuc, String trangThai, double tongTien) {
        this.maHoaDon = maHoaDon;
        this.ngayLap = ngayLap;
        this.maBenhNhan = maBenhNhan;
        this.tenBenhNhan = tenBenhNhan;
        this.maNhanVien = maNhanVien;
        this.tenNhanVien = tenNhanVien;
        this.phuongThuc = phuongThuc;
        this.trangThai = trangThai;
        this.tongTien = tongTien;
    }

    public String maHoaDon() { return maHoaDon; }
    public LocalDate ngayLap() { return ngayLap; }
    public String maBenhNhan() { return maBenhNhan; }
    public String tenBenhNhan() { return tenBenhNhan; }
    public String maNhanVien() { return maNhanVien; }
    public String tenNhanVien() { return tenNhanVien; }
    public String phuongThuc() { return phuongThuc; }
    public String trangThai() { return trangThai; }
    public double tongTien() { return tongTien; }
}

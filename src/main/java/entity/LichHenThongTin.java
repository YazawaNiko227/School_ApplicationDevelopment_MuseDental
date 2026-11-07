package entity;

import java.time.LocalDateTime;

public class LichHenThongTin {
    private final String maLichHen;
    private final LocalDateTime thoiGianHen;
    private final String maBenhNhan;
    private final String tenBenhNhan;
    private final String tenNhaSi;
    private final String tenPhong;
    private final String trangThai;

    public LichHenThongTin(String maLichHen, LocalDateTime thoiGianHen, String maBenhNhan,
            String tenBenhNhan, String tenNhaSi, String tenPhong, String trangThai) {
        this.maLichHen = maLichHen;
        this.thoiGianHen = thoiGianHen;
        this.maBenhNhan = maBenhNhan;
        this.tenBenhNhan = tenBenhNhan;
        this.tenNhaSi = tenNhaSi;
        this.tenPhong = tenPhong;
        this.trangThai = trangThai;
    }

    public String getMaLichHen() { return maLichHen; }
    public LocalDateTime getThoiGianHen() { return thoiGianHen; }
    public String getMaBenhNhan() { return maBenhNhan; }
    public String getTenBenhNhan() { return tenBenhNhan; }
    public String getTenNhaSi() { return tenNhaSi; }
    public String getTenPhong() { return tenPhong; }
    public String getTrangThai() { return trangThai; }
}

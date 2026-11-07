package entity;

import java.time.LocalDateTime;

public class LichHenDieuPhoi {
    private final String maLichHen;
    private final LocalDateTime thoiGianHen;
    private final String tenBenhNhan;
    private final String nhaSi;
    private final String maPhong;
    private final String trangThai;
    private final LocalDateTime thoiGianCheckIn;

    public LichHenDieuPhoi(String maLichHen, LocalDateTime thoiGianHen, String tenBenhNhan,
            String nhaSi, String maPhong, String trangThai, LocalDateTime thoiGianCheckIn) {
        this.maLichHen = maLichHen;
        this.thoiGianHen = thoiGianHen;
        this.tenBenhNhan = tenBenhNhan;
        this.nhaSi = nhaSi;
        this.maPhong = maPhong;
        this.trangThai = trangThai;
        this.thoiGianCheckIn = thoiGianCheckIn;
    }

    public String getMaLichHen() { return maLichHen; }
    public LocalDateTime getThoiGianHen() { return thoiGianHen; }
    public String getTenBenhNhan() { return tenBenhNhan; }
    public String getNhaSi() { return nhaSi; }
    public String getMaPhong() { return maPhong; }
    public String getTrangThai() { return trangThai; }
    public LocalDateTime getThoiGianCheckIn() { return thoiGianCheckIn; }
}

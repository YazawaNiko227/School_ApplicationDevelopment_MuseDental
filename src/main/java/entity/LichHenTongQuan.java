package entity;

import java.time.LocalDateTime;

public final class LichHenTongQuan {
    private final LocalDateTime thoiGianHen;
    private final String tenBenhNhan;
    private final String noiDung;
    private final String trangThai;

    public LichHenTongQuan(LocalDateTime thoiGianHen, String tenBenhNhan, String noiDung, String trangThai) {
        this.thoiGianHen = thoiGianHen;
        this.tenBenhNhan = tenBenhNhan;
        this.noiDung = noiDung;
        this.trangThai = trangThai;
    }

    public LocalDateTime getThoiGianHen() {
        return thoiGianHen;
    }

    public String getTenBenhNhan() {
        return tenBenhNhan;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public String getTrangThai() {
        return trangThai;
    }
}

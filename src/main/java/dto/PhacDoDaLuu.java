package dto;

import java.time.LocalDate;

public class PhacDoDaLuu {
    private final String maKeHoach;
    private final String tenPhacDo;
    private final Integer thoiGianDuKien;
    private final String trangThai;
    private final String maBenhNhan;
    private final String tenBenhNhan;
    private final String maChanDoan;
    private final LocalDate ngayKham;
    private final String ketLuanChanDoan;

    public PhacDoDaLuu(String maKeHoach, String tenPhacDo, Integer thoiGianDuKien, String trangThai,
            String maBenhNhan, String tenBenhNhan, String maChanDoan, LocalDate ngayKham,
            String ketLuanChanDoan) {
        this.maKeHoach = maKeHoach;
        this.tenPhacDo = tenPhacDo;
        this.thoiGianDuKien = thoiGianDuKien;
        this.trangThai = trangThai;
        this.maBenhNhan = maBenhNhan;
        this.tenBenhNhan = tenBenhNhan;
        this.maChanDoan = maChanDoan;
        this.ngayKham = ngayKham;
        this.ketLuanChanDoan = ketLuanChanDoan;
    }

    public String getMaKeHoach() {
        return maKeHoach;
    }

    public String getTenPhacDo() {
        return tenPhacDo;
    }

    public Integer getThoiGianDuKien() {
        return thoiGianDuKien;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public String getMaBenhNhan() {
        return maBenhNhan;
    }

    public String getTenBenhNhan() {
        return tenBenhNhan;
    }

    public String getMaChanDoan() {
        return maChanDoan;
    }

    public LocalDate getNgayKham() {
        return ngayKham;
    }

    public String getKetLuanChanDoan() {
        return ketLuanChanDoan;
    }
}

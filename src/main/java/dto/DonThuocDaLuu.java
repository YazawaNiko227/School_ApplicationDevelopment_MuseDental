package dto;

import java.time.LocalDate;

public class DonThuocDaLuu {
    private final String maDonThuoc;
    private final LocalDate ngayKe;
    private final String loiDanChamSoc;
    private final String maChanDoan;
    private final String ketLuanChanDoan;
    private final String maBenhNhan;
    private final String tenBenhNhan;

    public DonThuocDaLuu(String maDonThuoc, LocalDate ngayKe, String loiDanChamSoc,
            String maChanDoan, String ketLuanChanDoan, String maBenhNhan, String tenBenhNhan) {
        this.maDonThuoc = maDonThuoc;
        this.ngayKe = ngayKe;
        this.loiDanChamSoc = loiDanChamSoc;
        this.maChanDoan = maChanDoan;
        this.ketLuanChanDoan = ketLuanChanDoan;
        this.maBenhNhan = maBenhNhan;
        this.tenBenhNhan = tenBenhNhan;
    }

    public String getMaDonThuoc() {
        return maDonThuoc;
    }

    public LocalDate getNgayKe() {
        return ngayKe;
    }

    public String getLoiDanChamSoc() {
        return loiDanChamSoc;
    }

    public String getMaChanDoan() {
        return maChanDoan;
    }

    public String getKetLuanChanDoan() {
        return ketLuanChanDoan;
    }

    public String getMaBenhNhan() {
        return maBenhNhan;
    }

    public String getTenBenhNhan() {
        return tenBenhNhan;
    }
}

package dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class CaKhamView {
    public final String maLich;
    public final LocalDateTime gioHen;
    public final String maBenhNhan;
    public final String hoTen;
    public final String gioiTinh;
    public final LocalDate ngaySinh;
    public final String tienSu;
    public final String diUng;
    public final String nguoiBaoHo;
    public final String maHoSo;
    public final String maNhaSi;
    public final String nhaSi;
    public String maPhong;
    public String trangThaiPhong;
    public String trangThai;
    public final String maChanDoan;
    public String lyDo;
    public String trieuChung;
    public String ketLuan;
    public final boolean daChot;

    public CaKhamView(String maLich, LocalDateTime gioHen, String maBenhNhan, String hoTen, String gioiTinh,
            LocalDate ngaySinh, String tienSu, String diUng, String nguoiBaoHo, String maHoSo,
            String maNhaSi, String nhaSi, String maPhong, String trangThaiPhong, String trangThai,
            String maChanDoan, String lyDo, String trieuChung, String ketLuan, String trangThaiChanDoan) {
        this.maLich = maLich;
        this.gioHen = gioHen;
        this.maBenhNhan = maBenhNhan;
        this.hoTen = hoTen;
        this.gioiTinh = gioiTinh;
        this.ngaySinh = ngaySinh;
        this.tienSu = tienSu;
        this.diUng = diUng;
        this.nguoiBaoHo = nguoiBaoHo;
        this.maHoSo = maHoSo;
        this.maNhaSi = maNhaSi;
        this.nhaSi = nhaSi;
        this.maPhong = maPhong;
        this.trangThaiPhong = trangThaiPhong;
        this.trangThai = trangThai;
        this.maChanDoan = maChanDoan;
        this.lyDo = lyDo == null ? "" : lyDo;
        this.trieuChung = trieuChung == null ? "" : trieuChung;
        this.ketLuan = ketLuan == null ? "" : ketLuan;
        this.daChot = "Da chot".equals(trangThaiChanDoan);
    }
}

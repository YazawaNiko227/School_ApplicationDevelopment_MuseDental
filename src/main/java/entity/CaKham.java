package entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CaKham {
    public final String maLich;
    public final LocalDateTime gioHen;
    public final String tienSu;
    public final String diUng;
    public final String nhaSi;
    public final String trangThai;
    public String lyDo;
    public String ketLuan;
    public final boolean daChot;

    public final String maLichHen;
    public final LocalDateTime thoiGianHen;
    public final String maBenhNhan;
    public final String hoTen;
    public final String gioiTinh;
    public final LocalDate ngaySinh;
    public final String tienSuBenhLy;
    public final String thongTinDiUng;
    public final String nguoiBaoHo;
    public final String maHoSo;
    public final String maNhaSi;
    public final String tenNhaSi;
    public final String maPhong;
    public final String trangThaiPhong;
    public final String trangThaiLich;
    public final String maChanDoan;
    public final String lyDoKham;
    public String trieuChung;
    public final String ketLuanChanDoan;
    public final String trangThaiChanDoan;

    public CaKham(String maLichHen, LocalDateTime thoiGianHen, String maBenhNhan, String hoTen,
            String gioiTinh, LocalDate ngaySinh, String tienSuBenhLy, String thongTinDiUng,
            String nguoiBaoHo, String maHoSo, String maNhaSi, String tenNhaSi, String maPhong,
            String trangThaiPhong, String trangThaiLich, String maChanDoan, String lyDoKham,
            String trieuChung, String ketLuanChanDoan, String trangThaiChanDoan) {
        this.maLichHen = maLichHen;
        this.thoiGianHen = thoiGianHen;
        this.maBenhNhan = maBenhNhan;
        this.hoTen = hoTen;
        this.gioiTinh = gioiTinh;
        this.ngaySinh = ngaySinh;
        this.tienSuBenhLy = tienSuBenhLy;
        this.thongTinDiUng = thongTinDiUng;
        this.nguoiBaoHo = nguoiBaoHo;
        this.maHoSo = maHoSo;
        this.maNhaSi = maNhaSi;
        this.tenNhaSi = tenNhaSi;
        this.maPhong = maPhong;
        this.trangThaiPhong = trangThaiPhong;
        this.trangThaiLich = trangThaiLich;
        this.maChanDoan = maChanDoan;
        this.lyDoKham = lyDoKham;
        this.trieuChung = trieuChung;
        this.ketLuanChanDoan = ketLuanChanDoan;
        this.trangThaiChanDoan = trangThaiChanDoan;

        this.maLich = maLichHen;
        this.gioHen = thoiGianHen;
        this.tienSu = tienSuBenhLy;
        this.diUng = thongTinDiUng;
        this.nhaSi = tenNhaSi;
        this.trangThai = trangThaiLich;
        this.lyDo = lyDoKham == null ? "" : lyDoKham;
        this.ketLuan = ketLuanChanDoan == null ? "" : ketLuanChanDoan;
        this.daChot = "Da chot".equals(trangThaiChanDoan);
    }

    public String getMaLichHen() {
        return maLichHen;
    }

    public LocalDateTime getThoiGianHen() {
        return thoiGianHen;
    }

    public String getMaBenhNhan() {
        return maBenhNhan;
    }

    public String getHoTen() {
        return hoTen;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public String getTienSuBenhLy() {
        return tienSuBenhLy;
    }

    public String getThongTinDiUng() {
        return thongTinDiUng;
    }

    public String getNguoiBaoHo() {
        return nguoiBaoHo;
    }

    public String getMaHoSo() {
        return maHoSo;
    }

    public String getMaNhaSi() {
        return maNhaSi;
    }

    public String getTenNhaSi() {
        return tenNhaSi;
    }

    public String getMaPhong() {
        return maPhong;
    }

    public String getTrangThaiPhong() {
        return trangThaiPhong;
    }

    public String getTrangThaiLich() {
        return trangThaiLich;
    }

    public String getMaChanDoan() {
        return maChanDoan;
    }

    public String getLyDoKham() {
        return lyDoKham;
    }

    public String getTrieuChung() {
        return trieuChung;
    }

    public String getKetLuanChanDoan() {
        return ketLuanChanDoan;
    }

    public String getTrangThaiChanDoan() {
        return trangThaiChanDoan;
    }
}

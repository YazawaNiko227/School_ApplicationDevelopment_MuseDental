package entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CaKham {
    private final String maLichHen;
    private final LocalDateTime thoiGianHen;
    private final String maBenhNhan;
    private final String hoTen;
    private final String gioiTinh;
    private final LocalDate ngaySinh;
    private final String tienSuBenhLy;
    private final String thongTinDiUng;
    private final String nguoiBaoHo;
    private final String maHoSo;
    private final String maNhaSi;
    private final String tenNhaSi;
    private final String maPhong;
    private final String trangThaiPhong;
    private final String trangThaiLich;
    private final String maChanDoan;
    private final String lyDoKham;
    private final String trieuChung;
    private final String ketLuanChanDoan;
    private final String trangThaiChanDoan;

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
    }

    public String getMaLichHen() { return maLichHen; }
    public LocalDateTime getThoiGianHen() { return thoiGianHen; }
    public String getMaBenhNhan() { return maBenhNhan; }
    public String getHoTen() { return hoTen; }
    public String getGioiTinh() { return gioiTinh; }
    public LocalDate getNgaySinh() { return ngaySinh; }
    public String getTienSuBenhLy() { return tienSuBenhLy; }
    public String getThongTinDiUng() { return thongTinDiUng; }
    public String getNguoiBaoHo() { return nguoiBaoHo; }
    public String getMaHoSo() { return maHoSo; }
    public String getMaNhaSi() { return maNhaSi; }
    public String getTenNhaSi() { return tenNhaSi; }
    public String getMaPhong() { return maPhong; }
    public String getTrangThaiPhong() { return trangThaiPhong; }
    public String getTrangThaiLich() { return trangThaiLich; }
    public String getMaChanDoan() { return maChanDoan; }
    public String getLyDoKham() { return lyDoKham; }
    public String getTrieuChung() { return trieuChung; }
    public String getKetLuanChanDoan() { return ketLuanChanDoan; }
    public String getTrangThaiChanDoan() { return trangThaiChanDoan; }
}

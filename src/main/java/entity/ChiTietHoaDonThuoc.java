package entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ChiTietHoaDonThuoc {
    private String maHoaDon;
    private String maVatTu;
    private int soLuong;
    private double donGiaTaiThoiDiem;

    public ChiTietHoaDonThuoc() {
    }

    public ChiTietHoaDonThuoc(String maHoaDon, String maVatTu, int soLuong, double donGiaTaiThoiDiem) {
        setMaHoaDon(maHoaDon);
        setMaVatTu(maVatTu);
        setSoLuong(soLuong);
        setDonGiaTaiThoiDiem(donGiaTaiThoiDiem);
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        if (maHoaDon == null || maHoaDon.trim().isEmpty()) throw new IllegalArgumentException("maHoaDon không được để trống");
        if (maHoaDon != null && maHoaDon.length() > 20) throw new IllegalArgumentException("maHoaDon tối đa 20 ký tự");
        this.maHoaDon = maHoaDon;
    }

    public String getMaVatTu() {
        return maVatTu;
    }

    public void setMaVatTu(String maVatTu) {
        if (maVatTu == null || maVatTu.trim().isEmpty()) throw new IllegalArgumentException("maVatTu không được để trống");
        if (maVatTu != null && maVatTu.length() > 20) throw new IllegalArgumentException("maVatTu tối đa 20 ký tự");
        this.maVatTu = maVatTu;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        if (soLuong <= 0) throw new IllegalArgumentException("soLuong phải > 0");
        this.soLuong = soLuong;
    }

    public double getDonGiaTaiThoiDiem() {
        return donGiaTaiThoiDiem;
    }

    public void setDonGiaTaiThoiDiem(double donGiaTaiThoiDiem) {
        if (donGiaTaiThoiDiem < 0) throw new IllegalArgumentException("donGiaTaiThoiDiem phải >= 0");
        if (!Double.isFinite(donGiaTaiThoiDiem)) throw new IllegalArgumentException("donGiaTaiThoiDiem phải là số hữu hạn");
        this.donGiaTaiThoiDiem = donGiaTaiThoiDiem;
    }

}

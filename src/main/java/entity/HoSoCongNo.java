package entity;

import java.util.ArrayList;
import java.util.List;

public class HoSoCongNo {
    private final String maCongNo;
    private final String maHoaDon;
    private final String maBenhNhan;
    private final String hoTen;
    private final String soDienThoai;
    private final double tongSoTien;
    private final List<DotThanhToan> dsDot = new ArrayList<>();

    public HoSoCongNo(String maCongNo, String maHoaDon, String maBenhNhan,
            String hoTen, String soDienThoai, double tongSoTien) {
        this.maCongNo = maCongNo;
        this.maHoaDon = maHoaDon;
        this.maBenhNhan = maBenhNhan;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.tongSoTien = tongSoTien;
    }

    public String maCongNo() {
        return maCongNo;
    }

    public String maHoaDon() {
        return maHoaDon;
    }

    public String maBenhNhan() {
        return maBenhNhan;
    }

    public String hoTen() {
        return hoTen;
    }

    public String soDienThoai() {
        return soDienThoai;
    }

    public double tongSoTien() {
        return tongSoTien;
    }

    public List<DotThanhToan> dsDot() {
        return List.copyOf(dsDot);
    }

    public void addDot(DotThanhToan dot) {
        if (dot == null) {
            throw new IllegalArgumentException("Đợt thanh toán không được null.");
        }
        dsDot.add(dot);
    }
}

package entity;

import java.util.ArrayList;
import java.util.List;

public class CongNoHoaDon {
    public final String maCongNo;
    public final String maHoaDon;
    public final String maBenhNhan;
    public final String tenBenhNhan;
    public final String soDienThoai;
    public final double tongTien;
    public final int soDotConLai;
    public final List<DotHoaDon> dots = new ArrayList<>();

    public CongNoHoaDon(String maCongNo, String maHoaDon, String maBenhNhan, String tenBenhNhan,
            String soDienThoai, double tongTien, int soDotConLai) {
        this.maCongNo = maCongNo;
        this.maHoaDon = maHoaDon;
        this.maBenhNhan = maBenhNhan;
        this.tenBenhNhan = tenBenhNhan;
        this.soDienThoai = soDienThoai;
        this.tongTien = tongTien;
        this.soDotConLai = soDotConLai;
    }
}

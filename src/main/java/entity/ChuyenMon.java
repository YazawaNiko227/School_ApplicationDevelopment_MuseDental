package entity;

import java.util.Objects;

/** Entity ánh xạ bảng ChuyenMon trong CSDL QuanLyPhongKhamMuse. */
public class ChuyenMon {
    private String maChuyenMon;
    private String tenChuyenMon;

    public ChuyenMon() {
    }

    public ChuyenMon(String maChuyenMon, String tenChuyenMon) {
        this.maChuyenMon = maChuyenMon;
        this.tenChuyenMon = tenChuyenMon;
    }

    public String getMaChuyenMon() {
        return maChuyenMon;
    }

    public void setMaChuyenMon(String maChuyenMon) {
        this.maChuyenMon = maChuyenMon;
    }

    public String getTenChuyenMon() {
        return tenChuyenMon;
    }

    public void setTenChuyenMon(String tenChuyenMon) {
        this.tenChuyenMon = tenChuyenMon;
    }

    @Override
    public String toString() {
        return tenChuyenMon == null ? "" : tenChuyenMon;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ChuyenMon)) {
            return false;
        }
        return Objects.equals(maChuyenMon, ((ChuyenMon) other).maChuyenMon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maChuyenMon);
    }
}

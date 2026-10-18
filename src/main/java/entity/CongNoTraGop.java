package entity;

public class CongNoTraGop {
    private String maCongNo;
    private double tongSoTien;
    private int soDotConLai;
    private String maHoaDon;

    public CongNoTraGop() {
        this.soDotConLai = 0;
    }

    public CongNoTraGop(String maCongNo, double tongSoTien, int soDotConLai, String maHoaDon) {
        setMaCongNo(maCongNo);
        setTongSoTien(tongSoTien);
        setSoDotConLai(soDotConLai);
        setMaHoaDon(maHoaDon);
    }

    public String getMaCongNo() {
        return maCongNo;
    }

    public void setMaCongNo(String maCongNo) {
        if (maCongNo == null) throw new IllegalArgumentException("MaCongNo không được null.");
        if (maCongNo != null && maCongNo.length() > 20) throw new IllegalArgumentException("MaCongNo tối đa 20 ký tự.");
        this.maCongNo = maCongNo;
    }

    public double getTongSoTien() {
        return tongSoTien;
    }

    public void setTongSoTien(double tongSoTien) {
        if (!Double.isFinite(tongSoTien)) throw new IllegalArgumentException("TongSoTien phải là số hữu hạn.");
        if (tongSoTien < 0) throw new IllegalArgumentException("TongSoTien phải >= 0.");
        this.tongSoTien = tongSoTien;
    }

    public int getSoDotConLai() {
        return soDotConLai;
    }

    public void setSoDotConLai(int soDotConLai) {
        this.soDotConLai = soDotConLai;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        if (maHoaDon == null) throw new IllegalArgumentException("MaHoaDon không được null.");
        if (maHoaDon != null && maHoaDon.length() > 20) throw new IllegalArgumentException("MaHoaDon tối đa 20 ký tự.");
        this.maHoaDon = maHoaDon;
    }

}

package entity;

public class PhongKham {
    public static final String SAN_SANG = "San sang";
    public static final String DANG_CHUAN_BI = "Dang chuan bi";
    public static final String DANG_SU_DUNG = "Dang su dung";
    public static final String DANG_VE_SINH = "Dang ve sinh";
    public static final String BAO_TRI = "Bao tri";

    public static final String[] CAC_TRANG_THAI = {
            SAN_SANG, DANG_CHUAN_BI, DANG_SU_DUNG, DANG_VE_SINH, BAO_TRI
    };

    private String maPhong;
    private String tenPhong;
    private String trangThai;

    public PhongKham() {
        this.trangThai = SAN_SANG;
    }

    public PhongKham(String maPhong, String tenPhong, String trangThai) {
        this.maPhong = maPhong;
        this.tenPhong = tenPhong;
        this.trangThai = trangThai;
    }

    public String getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(String maPhong) {
        this.maPhong = maPhong;
    }

    public String getTenPhong() {
        return tenPhong;
    }

    public void setTenPhong(String tenPhong) {
        this.tenPhong = tenPhong;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    @Override
    public String toString() {
        return maPhong + " - " + tenPhong;
    }
}

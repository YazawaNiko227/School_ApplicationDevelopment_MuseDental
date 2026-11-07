package dto;

public class ThuocDonThuocDaLuu {
    private final String maThuoc;
    private final String tenThuoc;
    private final String donViTinh;
    private final String lieuDung;
    private final String duongDung;
    private final int soLuong;

    public ThuocDonThuocDaLuu(String maThuoc, String tenThuoc, String donViTinh,
            String lieuDung, String duongDung, int soLuong) {
        this.maThuoc = maThuoc;
        this.tenThuoc = tenThuoc;
        this.donViTinh = donViTinh;
        this.lieuDung = lieuDung;
        this.duongDung = duongDung;
        this.soLuong = soLuong;
    }

    public String getMaThuoc() {
        return maThuoc;
    }

    public String getTenThuoc() {
        return tenThuoc;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public String getLieuDung() {
        return lieuDung;
    }

    public String getDuongDung() {
        return duongDung;
    }

    public int getSoLuong() {
        return soLuong;
    }
}

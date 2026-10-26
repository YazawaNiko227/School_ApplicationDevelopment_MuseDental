package entity;

import java.util.Objects;

/**
 * Vai trò của tài khoản (bảng VaiTro).
 * Mã vai trò: QUAN_TRI, BAC_SI, Y_TA, LE_TAN, THU_NGAN, THU_KHO.
 */
public class VaiTro {
    // Hằng số mã vai trò, dùng khi so sánh/lọc menu để tránh gõ sai chuỗi.
    public static final String QUAN_TRI = "QUAN_TRI";
    public static final String BAC_SI = "BAC_SI";
    public static final String Y_TA = "Y_TA";
    public static final String LE_TAN = "LE_TAN";
    public static final String THU_NGAN = "THU_NGAN";
    public static final String THU_KHO = "THU_KHO";

    private String maVaiTro;
    private String tenVaiTro;

    public VaiTro() {
    }

    public VaiTro(String maVaiTro) {
        this.maVaiTro = maVaiTro;
    }

    public VaiTro(String maVaiTro, String tenVaiTro) {
        this.maVaiTro = maVaiTro;
        this.tenVaiTro = tenVaiTro;
    }

    public String getMaVaiTro() {
        return maVaiTro;
    }

    public void setMaVaiTro(String maVaiTro) {
        this.maVaiTro = maVaiTro;
    }

    public String getTenVaiTro() {
        return tenVaiTro;
    }

    public void setTenVaiTro(String tenVaiTro) {
        this.tenVaiTro = tenVaiTro;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VaiTro)) {
            return false;
        }
        VaiTro other = (VaiTro) o;
        return Objects.equals(maVaiTro, other.maVaiTro);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maVaiTro);
    }

    @Override
    public String toString() {
        return "VaiTro{maVaiTro='" + maVaiTro + "', tenVaiTro='" + tenVaiTro + "'}";
    }
}

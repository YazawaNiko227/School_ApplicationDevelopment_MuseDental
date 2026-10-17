package entity;

import java.time.LocalDate;


public class HoSoBenhAn {
    private String maHoSo;
    private LocalDate ngayTao;
    private String maBenhNhan;

    public HoSoBenhAn() {
        this.ngayTao = LocalDate.now();
    }

    public HoSoBenhAn(String maHoSo, LocalDate ngayTao, String maBenhNhan) {
        setMaHoSo(maHoSo);
        setNgayTao(ngayTao);
        setMaBenhNhan(maBenhNhan);
    }

    public String getMaHoSo() {
        return maHoSo;
    }

    public void setMaHoSo(String maHoSo) {
        if (maHoSo == null) throw new IllegalArgumentException("MaHoSo không được null.");
        if (maHoSo != null && maHoSo.length() > 20) throw new IllegalArgumentException("MaHoSo tối đa 20 ký tự.");
        this.maHoSo = maHoSo;
    }

    public LocalDate getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDate ngayTao) {
        if (ngayTao == null) throw new IllegalArgumentException("NgayTao không được null.");
        this.ngayTao = ngayTao;
    }

    public String getMaBenhNhan() {
        return maBenhNhan;
    }

    public void setMaBenhNhan(String maBenhNhan) {
        if (maBenhNhan == null) throw new IllegalArgumentException("MaBenhNhan không được null.");
        if (maBenhNhan != null && maBenhNhan.length() > 20) throw new IllegalArgumentException("MaBenhNhan tối đa 20 ký tự.");
        this.maBenhNhan = maBenhNhan;
    }

}

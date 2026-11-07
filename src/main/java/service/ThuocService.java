package service;

import dao.VatTuDAO;
import entity.VatTu;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ThuocService {
    public static final String LOAI_THUOC = "Thuốc";

    private final VatTuDAO vatTuDAO = new VatTuDAO();

    public List<VatTu> timKiem(String tuKhoa) throws SQLException {
        return vatTuDAO.timKiem(tuKhoa, LOAI_THUOC);
    }

    public VatTu timTheoMa(String ma) throws SQLException {
        return vatTuDAO.timTheoMa(ma);
    }

    public String sinhMa() throws SQLException {
        return vatTuDAO.sinhMa();
    }

    public VatTu taoThuoc(String ma, String ten, String donVi, String tonText,
            String nguongText, String giaBanText, LocalDate hanSuDung) {
        ten = VatTuYTeService.trim(ten);
        if (ten.isEmpty()) {
            throw new IllegalArgumentException("Tên thuốc không được để trống.");
        }
        int ton = VatTuYTeService.parseNonNegativeInt(tonText, "Số lượng tồn");
        int nguong = VatTuYTeService.parseNonNegativeInt(nguongText, "Ngưỡng tối thiểu");
        double giaBan = VatTuYTeService.parseMoney(giaBanText, "Giá bán");
        donVi = VatTuYTeService.trim(donVi);
        return new VatTu(VatTuYTeService.trim(ma), ten, LOAI_THUOC,
                donVi.isEmpty() ? null : donVi, ton, nguong, hanSuDung, giaBan);
    }

    public boolean them(VatTu vatTu) throws SQLException {
        if (vatTuDAO.timTheoMa(vatTu.getMaVatTu()) != null) {
            throw new IllegalArgumentException("Mã thuốc đã tồn tại trong hệ thống.");
        }
        return vatTuDAO.them(vatTu);
    }

    public boolean capNhat(VatTu vatTu) throws SQLException {
        return vatTuDAO.capNhat(vatTu);
    }

    public boolean xoa(String ma) throws SQLException {
        return vatTuDAO.xoa(ma);
    }
}

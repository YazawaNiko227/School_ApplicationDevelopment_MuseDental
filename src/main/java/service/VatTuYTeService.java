package service;

import dao.VatTuDAO;
import entity.VatTu;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class VatTuYTeService {
    private final VatTuDAO vatTuDAO = new VatTuDAO();

    public List<VatTu> layDanhSach() throws SQLException {
        return vatTuDAO.getAllExceptLoai(ThuocService.LOAI_THUOC);
    }

    public String sinhMa() throws SQLException {
        return vatTuDAO.getNextId();
    }

    public VatTu taoVatTu(String ma, String ten, String loai, String donVi, String tonText,
            String nguongText, String giaBanText, LocalDate hanSuDung) {
        ten = trim(ten);
        if (ten.isEmpty()) {
            throw new IllegalArgumentException("Tên vật tư không được để trống.");
        }
        loai = trim(loai);
        if (loai.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn loại vật tư.");
        }
        if (ThuocService.LOAI_THUOC.equalsIgnoreCase(loai)) {
            throw new IllegalArgumentException("Loại \"Thuoc\" được quản lý ở mục Quản Lý Thuốc.");
        }
        int ton = parseNonNegativeInt(tonText, "Số lượng tồn");
        int nguong = parseNonNegativeInt(nguongText, "Ngưỡng tối thiểu");
        double giaBan = parseMoney(giaBanText, "Giá bán");
        donVi = trim(donVi);
        return new VatTu(trim(ma), ten, loai, donVi.isEmpty() ? null : donVi,
                ton, nguong, hanSuDung, giaBan);
    }

    public boolean them(VatTu vatTu) throws SQLException {
        if (vatTuDAO.timTheoMa(vatTu.getMaVatTu()) != null) {
            throw new IllegalArgumentException("Mã vật tư đã tồn tại.");
        }
        return vatTuDAO.insert(vatTu);
    }

    public boolean capNhat(VatTu vatTu) throws SQLException {
        if (vatTuDAO.timTheoMa(vatTu.getMaVatTu()) == null) {
            throw new IllegalArgumentException("Không tìm thấy vật tư cần cập nhật.");
        }
        return vatTuDAO.update(vatTu);
    }

    public boolean xoa(String ma) throws SQLException {
        return vatTuDAO.delete(ma);
    }

    static int parseNonNegativeInt(String text, String name) {
        try {
            int value = Integer.parseInt(trim(text));
            if (value < 0) {
                throw new NumberFormatException();
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(name + " phải là số nguyên không âm.");
        }
    }

    static double parseMoney(String text, String name) {
        text = trim(text);
        if (text.isEmpty()) {
            return 0;
        }
        String digits = text.replaceAll("[^0-9]", "");
        if (text.contains("-") || digits.isEmpty()) {
            throw new IllegalArgumentException(name + " phải là số tiền không âm.");
        }
        try {
            return Double.parseDouble(digits);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(name + " phải là số tiền không âm.");
        }
    }

    static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}

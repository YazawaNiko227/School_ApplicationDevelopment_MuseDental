package service;

import dao.ThongKeDoanhThuDAO;
import entity.BaoCaoDoanhThu;
import java.sql.SQLException;
import java.time.LocalDate;

public class RevenueReportService {
    public static final String THEO_NGAY = ThongKeDoanhThuDAO.THEO_NGAY;
    public static final String THEO_THANG = ThongKeDoanhThuDAO.THEO_THANG;
    public static final String THEO_MUC = ThongKeDoanhThuDAO.THEO_MUC;
    public static final String THEO_PHUONG_THUC = ThongKeDoanhThuDAO.THEO_PHUONG_THUC;

    private final ThongKeDoanhThuDAO doanhThuDAO = new ThongKeDoanhThuDAO();

    public String validateDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            return "Vui lòng chọn đầy đủ Từ ngày và Đến ngày.";
        }
        if (from.isAfter(to)) {
            return "Từ ngày không được sau Đến ngày.";
        }
        return null;
    }

    public BaoCaoDoanhThu thongKe(LocalDate from, LocalDate to, String grouping) throws SQLException {
        String validationError = validateDateRange(from, to);
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }
        return doanhThuDAO.thongKe(from, to, grouping);
    }
}

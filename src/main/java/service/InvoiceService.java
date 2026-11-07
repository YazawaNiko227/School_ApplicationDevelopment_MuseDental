package service;

import dao.HoaDonDAO;
import entity.ChiTietHoaDonTraCuu;
import entity.DongTinhTien;
import entity.HoaDon;
import dto.LanKham;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class InvoiceService {
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();

    public List<DongTinhTien> layDichVuTheoChanDoan(String diagnosisId) throws SQLException {
        return hoaDonDAO.layDichVuTheoChanDoan(diagnosisId);
    }

    public List<DongTinhTien> layThuocTheoChanDoan(String diagnosisId) throws SQLException {
        return hoaDonDAO.layThuocTheoChanDoan(diagnosisId);
    }

    public String validateDraft(String employeeId, String patientId, String diagnosisId,
            double total, String paymentStatus, String paymentMethod, int installmentCount) {
        if (isBlank(employeeId)) {
            return "Không xác định được nhân viên đang đăng nhập để ghi nhận người lập hóa đơn.";
        }
        if (isBlank(patientId) || isBlank(diagnosisId)) {
            return "Thiếu dữ liệu bắt buộc để lập hóa đơn.";
        }
        if (!Double.isFinite(total) || total <= 0) {
            return "Lần khám này không có khoản nào để tính tiền.";
        }

        return validatePayment(paymentStatus, paymentMethod, installmentCount);
    }

    private String validatePayment(String paymentStatus, String paymentMethod, int installmentCount) {
        boolean installments = installmentCount > 0;
        boolean installmentStatus = HoaDon.DANG_TRA_GOP.equals(paymentStatus);
        boolean installmentMethod = "Tra gop".equals(paymentMethod);
        if (installments != installmentStatus || installments != installmentMethod) {
            return "Trạng thái và phương thức thanh toán không khớp.";
        }
        if (installments && (installmentCount < 2 || installmentCount > 12)) {
            return "Số đợt trả góp phải từ 2 đến 12.";
        }
        if (!installments && !HoaDon.DA_THANH_TOAN.equals(paymentStatus)) {
            return "Hóa đơn thanh toán một lần phải ở trạng thái đã thanh toán.";
        }
        return null;
    }

    public String lapHoaDon(LocalDate invoiceDate, String paymentStatus, String paymentMethod,
            String employeeId, String patientId, String diagnosisId, int installmentCount) throws SQLException {
        String validationError = validatePayment(paymentStatus, paymentMethod, installmentCount);
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }
        return hoaDonDAO.lapHoaDon(invoiceDate, paymentStatus, paymentMethod,
                employeeId, patientId, diagnosisId, installmentCount);
    }

    public ChiTietHoaDonTraCuu layChiTietHoaDon(String invoiceId) throws SQLException {
        return hoaDonDAO.layChiTietHoaDon(invoiceId);
    }

    public String layMaNhanVienTheoTenDangNhap(String username) throws SQLException {
        return hoaDonDAO.layMaNhanVienTheoTenDangNhap(username);
    }

    public List<LanKham> layLanKhamChuaLapHoaDon() throws SQLException {
        return hoaDonDAO.layLanKhamChuaLapHoaDon();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

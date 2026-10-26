/*
    Người làm phần này: Phan Đức Toàn
    MSSV: 24741631
*/
package util;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Ma trận phân quyền theo menu: vai trò nào được thấy mục nào trong sidebar.
 *
 * Cấu hình nằm hoàn toàn trong mã nguồn (không lưu CSDL). Mã vai trò khớp cột
 * TaiKhoan.MaVaiTro / bảng VaiTro. Tên menu PHẢI khớp đúng chuỗi trong mảng
 * items của ManHinhChinhFrame.addCollapsibleSection, nếu không menu sẽ bị ẩn
 * nhầm.
 *
 * Căn cứ phân vai theo "Luồng khám bệnh chính":
 * - Lễ tân: đặt lịch, xác nhận, check-in/điều phối.
 * - Y tá: chuẩn bị phòng, ghi nhận vật tư, vệ sinh phòng.
 * - Bác sĩ (nha sĩ): khám lâm sàng, phác đồ, kê đơn; vào khám từ Dashboard điều
 * phối.
 * - Thu ngân: lập hóa đơn, công nợ trả góp.
 * - Nhân viên kho: thuốc, vật tư, ghi nhận vật tư tiêu hao.
 * - Bước 11 (tra cứu EMR/hóa đơn, báo cáo) chỉ đọc: mọi vai trò đều thấy.
 */
public final class PhanQuyenMenu {
    public static final String QUAN_TRI = "QUAN_TRI";
    public static final String BAC_SI = "BAC_SI";
    public static final String Y_TA = "Y_TA";
    public static final String LE_TAN = "LE_TAN";
    public static final String THU_NGAN = "THU_NGAN";
    public static final String THU_KHO = "THU_KHO";

    /** Mục luôn hiển thị, nằm ngoài các nhóm accordion. */
    public static final String TONG_QUAN = "Tổng quan";

    // Tên menu (đúng chuỗi trong ManHinhChinhFrame).
    private static final String DOI_MAT_KHAU = "Đổi mật khẩu";
    private static final String PHAN_QUYEN = "Phân quyền hệ thống RBAC";
    private static final String QL_NHAN_VIEN = "Quản lý nhân viên";
    private static final String QL_BENH_NHAN = "Quản lý bệnh nhân";
    private static final String QL_THUOC = "Quản lý thuốc";
    private static final String QL_VAT_TU = "Quản lý vật tư y tế";
    private static final String QL_DICH_VU = "Quản lý dịch vụ và bảng giá";
    private static final String KE_DON = "Kê đơn thuốc điện tử";
    private static final String PHAC_DO = "Phác đồ điều trị và dự toán chi phí";
    private static final String GHI_NHAN_VAT_TU = "Ghi nhận vật tư tiêu hao";
    private static final String CONG_NO = "Công nợ và trả góp theo đợt";
    private static final String TIM_EMR = "Tìm hồ sơ bệnh án EMR";
    private static final String TRA_CUU_HOA_DON = "Tra cứu hóa đơn / giao dịch";
    private static final String LICH_CA_NHAN = "Lịch làm việc cá nhân";
    private static final String DASHBOARD_DIEU_PHOI = "Dashboard điều phối phòng khám";
    private static final String DAT_LICH = "Đặt lịch hẹn khám";
    private static final String CHUAN_BI_PHONG = "Chuẩn bị dụng cụ và cập nhật trạng thái phòng";
    private static final String PHAN_CONG_CA = "Phân công lịch làm việc và ca trực";
    private static final String KHAM_LAM_SANG = "Khám lâm sàng và sơ đồ răng";
    private static final String LAP_HOA_DON = "Lập hóa đơn";
    private static final String BAO_CAO = "Báo cáo doanh thu";

    /** Thứ tự vai trò hiển thị: mã -> tên. */
    private static final Map<String, String> VAI_TRO = new LinkedHashMap<>();

    /** Nhóm menu theo đúng thứ tự sidebar: tên nhóm -> các mục. */
    private static final Map<String, List<String>> NHOM_MENU = new LinkedHashMap<>();

    private static final Map<String, Set<String>> MENU_THEO_VAI_TRO = new LinkedHashMap<>();

    static {
        VAI_TRO.put(QUAN_TRI, "Quản trị viên");
        VAI_TRO.put(BAC_SI, "Bác sĩ");
        VAI_TRO.put(Y_TA, "Y tá");
        VAI_TRO.put(LE_TAN, "Lễ tân");
        VAI_TRO.put(THU_NGAN, "Thu ngân");
        VAI_TRO.put(THU_KHO, "Nhân viên kho");

        NHOM_MENU.put("HỆ THỐNG", Arrays.asList(DOI_MAT_KHAU, PHAN_QUYEN));
        NHOM_MENU.put("DANH MỤC", Arrays.asList(QL_NHAN_VIEN, QL_BENH_NHAN, QL_THUOC, QL_VAT_TU, QL_DICH_VU));
        NHOM_MENU.put("CẬP NHẬT", Arrays.asList(KE_DON, PHAC_DO, GHI_NHAN_VAT_TU, CONG_NO));
        NHOM_MENU.put("TÌM KIẾM", Arrays.asList(TIM_EMR, TRA_CUU_HOA_DON, LICH_CA_NHAN));
        NHOM_MENU.put("XỬ LÝ", Arrays.asList(DASHBOARD_DIEU_PHOI, DAT_LICH, CHUAN_BI_PHONG, PHAN_CONG_CA,
                KHAM_LAM_SANG, LAP_HOA_DON));
        NHOM_MENU.put("THỐNG KÊ", Arrays.asList(BAO_CAO));

        // Quản trị viên: toàn bộ menu (gom từ NHOM_MENU để không bao giờ bị sót).
        Set<String> tatCa = new LinkedHashSet<>();
        for (List<String> items : NHOM_MENU.values()) {
            tatCa.addAll(items);
        }
        MENU_THEO_VAI_TRO.put(QUAN_TRI, Collections.unmodifiableSet(tatCa));

        // Mục chỉ đọc + đổi mật khẩu mà ai cũng cần (luồng bước 11).
        MENU_THEO_VAI_TRO.put(BAC_SI, tap(DOI_MAT_KHAU, DASHBOARD_DIEU_PHOI, KHAM_LAM_SANG, PHAC_DO, KE_DON,
                TIM_EMR, TRA_CUU_HOA_DON, LICH_CA_NHAN, BAO_CAO));
        MENU_THEO_VAI_TRO.put(Y_TA, tap(DOI_MAT_KHAU, DASHBOARD_DIEU_PHOI, CHUAN_BI_PHONG, GHI_NHAN_VAT_TU,
                TIM_EMR, TRA_CUU_HOA_DON, LICH_CA_NHAN, BAO_CAO));
        MENU_THEO_VAI_TRO.put(LE_TAN, tap(DOI_MAT_KHAU, QL_BENH_NHAN, DAT_LICH, DASHBOARD_DIEU_PHOI,
                TIM_EMR, TRA_CUU_HOA_DON, LICH_CA_NHAN, BAO_CAO));
        MENU_THEO_VAI_TRO.put(THU_NGAN, tap(DOI_MAT_KHAU, LAP_HOA_DON, CONG_NO,
                TIM_EMR, TRA_CUU_HOA_DON, LICH_CA_NHAN, BAO_CAO));
        MENU_THEO_VAI_TRO.put(THU_KHO, tap(DOI_MAT_KHAU, QL_THUOC, QL_VAT_TU, GHI_NHAN_VAT_TU,
                TIM_EMR, TRA_CUU_HOA_DON, LICH_CA_NHAN, BAO_CAO));
    }

    private PhanQuyenMenu() {
    }

    private static Set<String> tap(String... menu) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(menu)));
    }

    /** Vai trò này có được thấy menu này không. "Tổng quan" luôn được thấy. */
    public static boolean duocXem(String maVaiTro, String menu) {
        if (TONG_QUAN.equals(menu)) {
            return true;
        }
        if (maVaiTro == null) {
            return false;
        }
        return MENU_THEO_VAI_TRO.getOrDefault(maVaiTro, Collections.emptySet()).contains(menu);
    }

    /** Nhóm có ít nhất một mục được thấy thì mới hiện tiêu đề nhóm. */
    public static boolean nhomCoMucHienThi(String maVaiTro, String[] items) {
        for (String item : items) {
            if (duocXem(maVaiTro, item)) {
                return true;
            }
        }
        return false;
    }

    /** Danh sách mã vai trò theo thứ tự hiển thị. */
    public static List<String> danhSachMaVaiTro() {
        return Collections.unmodifiableList(new java.util.ArrayList<>(VAI_TRO.keySet()));
    }

    /** Tên hiển thị của vai trò; mã lạ trả lại chính mã đó. */
    public static String tenVaiTro(String maVaiTro) {
        return VAI_TRO.getOrDefault(maVaiTro, maVaiTro == null ? "" : maVaiTro);
    }

    /**
     * Các nhóm menu và mục con, theo thứ tự sidebar (dùng cho panel phân quyền).
     */
    public static Map<String, List<String>> nhomMenu() {
        return Collections.unmodifiableMap(NHOM_MENU);
    }
}

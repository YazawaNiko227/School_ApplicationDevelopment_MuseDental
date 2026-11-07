package service;

import dao.CongNoTraGopDAO;
import entity.DotThanhToan;
import entity.HoSoCongNo;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CongNoTraGopService {
    public static final String DOT_DA_THANH_TOAN = "Đã thanh toán";
    public static final String DOT_CHUA_DEN_HAN = "Chưa đến hạn";
    public static final String DOT_QUA_HAN = "Quá hạn";

    public static final String DANG_TRA_GOP = "Đang trả góp";
    public static final String QUA_HAN = "Quá hạn";
    public static final String HOAN_TAT = "Hoàn tất";
    public static final String TAT_CA = "Tất cả";
    public static final int NGAY_SAP_DEN_HAN = 7;

    private static final DecimalFormat TIEN = new DecimalFormat("#,##0");
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final CongNoTraGopDAO congNoDAO;

    public CongNoTraGopService() {
        this(new CongNoTraGopDAO());
    }

    CongNoTraGopService(CongNoTraGopDAO congNoDAO) {
        if (congNoDAO == null) {
            throw new IllegalArgumentException("DAO công nợ không được null.");
        }
        this.congNoDAO = congNoDAO;
    }

    public List<HoSoCongNo> layTatCa() throws SQLException {
        return congNoDAO.layTatCa();
    }

    public List<HoSoCongNo> locDanhSach(List<HoSoCongNo> danhSach, String tuKhoa,
            String trangThai, LocalDate homNay) {
        String keyword = tuKhoa == null ? "" : tuKhoa.trim().toLowerCase();
        List<HoSoCongNo> ketQua = new ArrayList<>();
        for (HoSoCongNo hoSo : danhSach) {
            boolean khopTuKhoa = keyword.isEmpty()
                    || hoSo.maCongNo().toLowerCase().contains(keyword)
                    || hoSo.maHoaDon().toLowerCase().contains(keyword)
                    || hoSo.hoTen().toLowerCase().contains(keyword);
            boolean khopTrangThai = trangThai == null || TAT_CA.equals(trangThai)
                    || trangThai.equals(trangThai(hoSo, homNay));
            if (khopTuKhoa && khopTrangThai) {
                ketQua.add(hoSo);
            }
        }
        return ketQua;
    }

    public ThongKe thongKe(List<HoSoCongNo> danhSach, LocalDate homNay) {
        int dangTraGop = 0;
        int dotQuaHan = 0;
        int sapDenHan = 0;
        double conPhaiThu = 0;
        for (HoSoCongNo hoSo : danhSach) {
            if (soDotConLai(hoSo) > 0) {
                dangTraGop++;
                conPhaiThu += conLai(hoSo);
            }
            for (DotThanhToan dot : hoSo.dsDot()) {
                if (daThanhToan(dot)) {
                    continue;
                }
                if (dot.getNgayDenHan().isBefore(homNay)) {
                    dotQuaHan++;
                } else if (!dot.getNgayDenHan().isAfter(homNay.plusDays(NGAY_SAP_DEN_HAN))) {
                    sapDenHan++;
                }
            }
        }
        return new ThongKe(dangTraGop, dotQuaHan, sapDenHan, conPhaiThu);
    }

    public int soDotConLai(HoSoCongNo hoSo) {
        int dem = 0;
        for (DotThanhToan dot : hoSo.dsDot()) {
            if (!daThanhToan(dot)) {
                dem++;
            }
        }
        return dem;
    }

    public double daThu(HoSoCongNo hoSo) {
        double tong = 0;
        for (DotThanhToan dot : hoSo.dsDot()) {
            if (daThanhToan(dot)) {
                tong += dot.getSoTienDot();
            }
        }
        return tong;
    }

    public double conLai(HoSoCongNo hoSo) {
        return hoSo.tongSoTien() - daThu(hoSo);
    }

    public DotThanhToan dotCanThu(HoSoCongNo hoSo) {
        for (DotThanhToan dot : hoSo.dsDot()) {
            if (!daThanhToan(dot)) {
                return dot;
            }
        }
        return null;
    }

    public String trangThai(DotThanhToan dot, LocalDate homNay) {
        if (daThanhToan(dot)) {
            return DOT_DA_THANH_TOAN;
        }
        return dot.getNgayDenHan().isBefore(homNay) ? DOT_QUA_HAN : DOT_CHUA_DEN_HAN;
    }

    public String trangThai(HoSoCongNo hoSo, LocalDate homNay) {
        if (soDotConLai(hoSo) == 0) {
            return HOAN_TAT;
        }
        for (DotThanhToan dot : hoSo.dsDot()) {
            if (DOT_QUA_HAN.equals(trangThai(dot, homNay))) {
                return QUA_HAN;
            }
        }
        return DANG_TRA_GOP;
    }

    public int thuTuDotCanThu(HoSoCongNo hoSo, String maDot) {
        List<DotThanhToan> cacDot = hoSo.dsDot();
        int viTriDot = -1;
        for (int i = 0; i < cacDot.size(); i++) {
            if (cacDot.get(i).getMaDot().equals(maDot)) {
                viTriDot = i;
                break;
            }
        }
        if (viTriDot < 0) {
            throw new IllegalStateException("Đợt thanh toán không còn tồn tại.");
        }
        DotThanhToan dotDuocChon = cacDot.get(viTriDot);
        if (daThanhToan(dotDuocChon)) {
            throw new IllegalStateException("Đợt " + (viTriDot + 1) + " đã được thanh toán.");
        }

        DotThanhToan dotCanThu = dotCanThu(hoSo);
        if (dotDuocChon != dotCanThu) {
            int thuTuDotCanThu = cacDot.indexOf(dotCanThu) + 1;
            throw new IllegalStateException(
                    "Vui lòng thu theo thứ tự: hãy thu đợt " + thuTuDotCanThu + " trước.");
        }
        return viTriDot + 1;
    }

    public KetQuaThanhToan thanhToanDot(String maCongNo, String maDot, LocalDate ngayThanhToan)
            throws SQLException {
        if (ngayThanhToan == null) {
            throw new IllegalArgumentException("Ngày thanh toán không được null.");
        }

        HoSoCongNo hoSo = null;
        for (HoSoCongNo item : layTatCa()) {
            if (item.maCongNo().equals(maCongNo)) {
                hoSo = item;
                break;
            }
        }
        if (hoSo == null) {
            throw new IllegalStateException("Hồ sơ trả góp không còn tồn tại.");
        }

        List<DotThanhToan> cacDot = hoSo.dsDot();
        int thuTuDot = thuTuDotCanThu(hoSo, maDot);
        DotThanhToan dotDuocChon = cacDot.get(thuTuDot - 1);
        if (dotDuocChon == null) {
            throw new IllegalStateException("Đợt này đã được thanh toán hoặc không còn tồn tại.");
        }

        int soDotConLai = soDotConLai(hoSo) - 1;
        double conLai = this.conLai(hoSo) - dotDuocChon.getSoTienDot();
        boolean thanhCong = congNoDAO.thanhToanDot(maDot, ngayThanhToan);
        return new KetQuaThanhToan(thanhCong, hoSo.maCongNo(), hoSo.maHoaDon(),
                hoSo.hoTen(), thuTuDot, dotDuocChon.getSoTienDot(), soDotConLai, conLai);
    }

    public String taoNoiDungNhacNo(HoSoCongNo hoSo, LocalDate homNay) {
        DotThanhToan dot = dotCanThu(hoSo);
        if (dot == null) {
            throw new IllegalStateException("Hồ sơ này đã trả hết, không cần nhắc nợ.");
        }
        if (DOT_QUA_HAN.equals(trangThai(dot, homNay))) {
            return "Phòng khám Muse xin thông báo: đợt thanh toán " + TIEN.format(dot.getSoTienDot())
                    + " đ của hóa đơn " + hoSo.maHoaDon() + " đã quá hạn từ ngày "
                    + dot.getNgayDenHan().format(NGAY)
                    + ". Vui lòng thanh toán sớm. Xin cảm ơn.";
        }
        return "Phòng khám Muse xin nhắc: đợt thanh toán " + TIEN.format(dot.getSoTienDot())
                + " đ của hóa đơn " + hoSo.maHoaDon() + " sẽ đến hạn vào ngày "
                + dot.getNgayDenHan().format(NGAY)
                + ". Xin cảm ơn.";
    }

    private boolean daThanhToan(DotThanhToan dot) {
        return DotThanhToan.DA_THANH_TOAN.equals(dot.getTrangThai());
    }

    public static final class ThongKe {
        private final int dangTraGop;
        private final int dotQuaHan;
        private final int sapDenHan;
        private final double conPhaiThu;

        private ThongKe(int dangTraGop, int dotQuaHan, int sapDenHan, double conPhaiThu) {
            this.dangTraGop = dangTraGop;
            this.dotQuaHan = dotQuaHan;
            this.sapDenHan = sapDenHan;
            this.conPhaiThu = conPhaiThu;
        }

        public int dangTraGop() { return dangTraGop; }
        public int dotQuaHan() { return dotQuaHan; }
        public int sapDenHan() { return sapDenHan; }
        public double conPhaiThu() { return conPhaiThu; }
    }

    public static final class KetQuaThanhToan {
        private final boolean thanhCong;
        private final String maCongNo;
        private final String maHoaDon;
        private final String hoTen;
        private final int thuTuDot;
        private final double soTienDot;
        private final int soDotConLai;
        private final double conLai;

        private KetQuaThanhToan(boolean thanhCong, String maCongNo, String maHoaDon, String hoTen,
                int thuTuDot, double soTienDot, int soDotConLai, double conLai) {
            this.thanhCong = thanhCong;
            this.maCongNo = maCongNo;
            this.maHoaDon = maHoaDon;
            this.hoTen = hoTen;
            this.thuTuDot = thuTuDot;
            this.soTienDot = soTienDot;
            this.soDotConLai = soDotConLai;
            this.conLai = conLai;
        }

        public boolean thanhCong() { return thanhCong; }
        public String maCongNo() { return maCongNo; }
        public String maHoaDon() { return maHoaDon; }
        public String hoTen() { return hoTen; }
        public int thuTuDot() { return thuTuDot; }
        public double soTienDot() { return soTienDot; }
        public int soDotConLai() { return soDotConLai; }
        public double conLai() { return conLai; }
    }
}

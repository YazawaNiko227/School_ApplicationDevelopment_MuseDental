package service;

import dao.LichHenDAO;
import dao.PhieuXuatKhoDAO;
import dao.VatTuDAO;
import dto.ChiTietXuatInfo;
import dto.LichHenInfo;
import dto.PhieuXuatTraCuuInfo;
import entity.ChiTietXuatKho;
import entity.PhieuXuatKho;
import entity.VatTu;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VatTuTieuHaoService {
    private static final String MA_NHAN_VIEN_XUAT = "NV005";

    private final PhieuXuatKhoDAO phieuXuatKhoDAO = new PhieuXuatKhoDAO();
    private final VatTuDAO vatTuDAO = new VatTuDAO();
    private final LichHenDAO lichHenDAO = new LichHenDAO();

    public List<LichHenInfo> layLichHenChoXuatKho() throws SQLException {
        return lichHenDAO.layChoXuatKho();
    }

    public List<VatTu> timVatTu(String tuKhoa, String loaiVatTu) throws SQLException {
        return vatTuDAO.timKiem(tuKhoa, loaiVatTu);
    }

    public List<PhieuXuatTraCuuInfo> timKiemPhieuXuat(String tuKhoa, LocalDate tuNgay, LocalDate denNgay)
            throws SQLException {
        return phieuXuatKhoDAO.timKiemPhieuXuat(tuKhoa, tuNgay, denNgay);
    }

    public List<ChiTietXuatInfo> layChiTietPhieuXuat(String maPhieuXuat) throws SQLException {
        return phieuXuatKhoDAO.layChiTietPhieuXuat(maPhieuXuat);
    }

    public String kiemTraKhoangNgay(LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay != null && denNgay != null && denNgay.isBefore(tuNgay)) {
            return "Ngày kết thúc phải bằng hoặc sau ngày bắt đầu.";
        }
        return null;
    }

    public String kiemTraThemVatTu(VatTu vatTu, int daChon, int soLuong) {
        if (vatTu == null) {
            return "Không tìm thấy vật tư đã chọn.";
        }
        if (soLuong <= 0) {
            return "Số lượng xuất phải lớn hơn 0.";
        }
        if ((long) daChon + soLuong > vatTu.getSoLuongTon()) {
            return "Tổng số lượng xuất (" + ((long) daChon + soLuong)
                    + ") vượt quá tồn kho hiện có (" + vatTu.getSoLuongTon() + ").";
        }
        if (vatTu.getHanSuDung() != null && vatTu.getHanSuDung().isBefore(LocalDate.now())) {
            return "Vật tư " + vatTu.getTenVatTu() + " đã hết hạn sử dụng, không thể xuất dùng!";
        }
        return null;
    }

    public KetQuaLuuPhieu luuPhieuXuat(Map<String, Integer> vatTuDangSoan, String maLichHen)
            throws SQLException {
        if (vatTuDangSoan == null || vatTuDangSoan.isEmpty()) {
            throw new IllegalArgumentException("Phiếu xuất phải có ít nhất một vật tư tiêu hao.");
        }
        String maPhieuMoi = phieuXuatKhoDAO.sinhMa();
        PhieuXuatKho phieu = new PhieuXuatKho(maPhieuMoi, LocalDate.now(), MA_NHAN_VIEN_XUAT, maLichHen);
        List<ChiTietXuatKho> chiTietList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : vatTuDangSoan.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null || entry.getValue() <= 0) {
                throw new IllegalArgumentException("Thông tin vật tư xuất kho không hợp lệ.");
            }
            chiTietList.add(new ChiTietXuatKho(maPhieuMoi, entry.getKey(), entry.getValue()));
        }
        phieuXuatKhoDAO.luuPhieuXuat(phieu, chiTietList);

        List<String> canhBaoTon = new ArrayList<>();
        for (String maVatTu : vatTuDangSoan.keySet()) {
            VatTu vatTu = vatTuDAO.timTheoMa(maVatTu);
            if (vatTu != null && vatTu.getSoLuongTon() <= vatTu.getNguongToiThieu()) {
                canhBaoTon.add(vatTu.getTenVatTu() + " (còn " + vatTu.getSoLuongTon()
                        + " " + vatTu.getDonViTinh() + ")");
            }
        }
        return new KetQuaLuuPhieu(maPhieuMoi, canhBaoTon);
    }

    public static final class KetQuaLuuPhieu {
        private final String maPhieu;
        private final List<String> canhBaoTon;

        private KetQuaLuuPhieu(String maPhieu, List<String> canhBaoTon) {
            this.maPhieu = maPhieu;
            this.canhBaoTon = new ArrayList<>(canhBaoTon);
        }

        public String getMaPhieu() {
            return maPhieu;
        }

        public List<String> getCanhBaoTon() {
            return new ArrayList<>(canhBaoTon);
        }
    }
}

package service;

import dao.ChanDoanDAO;
import dao.DichVuDAO;
import dao.KeHoachDieuTriDAO;
import dao.KeHoachDieuTri_DichVuDAO;
import entity.ChanDoan;
import entity.ChiTietKeHoachDieuTriDichVu;
import entity.DichVu;
import entity.KeHoachDieuTri;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PhacDoDieuTriService {
    private final ChanDoanDAO chanDoanDAO = new ChanDoanDAO();
    private final DichVuDAO dichVuDAO = new DichVuDAO();
    private final KeHoachDieuTriDAO keHoachDieuTriDAO = new KeHoachDieuTriDAO();
    private final KeHoachDieuTri_DichVuDAO chiTietKeHoachDAO = new KeHoachDieuTri_DichVuDAO();

    public List<ChanDoan> layLanKhamDangKham() throws SQLException {
        return chanDoanDAO.layThongTinDangKham();
    }

    public List<DichVu> layDanhSachDichVu() throws SQLException {
        return dichVuDAO.getAllDichVu();
    }

    public KeHoachDieuTri timTheoChanDoan(String maChanDoan) throws SQLException {
        return keHoachDieuTriDAO.timTheoChanDoan(maChanDoan);
    }

    public List<ChiTietKeHoachDieuTriDichVu> layChiTiet(String maKeHoach) throws SQLException {
        return chiTietKeHoachDAO.layTheoKeHoach(maKeHoach);
    }

    public String kiemTraPhacDo(String ten, Integer thoiGian, Map<String, Integer> dichVuDangChon) {
        if (ten == null || ten.trim().isEmpty()) {
            return "Tên phác đồ không được để trống.";
        }
        if (ten.trim().length() > 150) {
            return "Tên phác đồ tối đa 150 ký tự.";
        }
        if (dichVuDangChon == null || dichVuDangChon.isEmpty()) {
            return "Phác đồ cần có ít nhất một dịch vụ.";
        }
        if (thoiGian == null || thoiGian < 1) {
            return "Thời gian dự kiến phải lớn hơn 0 ngày.";
        }
        for (Integer soLuong : dichVuDangChon.values()) {
            if (soLuong == null || soLuong <= 0) {
                return "Số lượng dịch vụ phải lớn hơn 0.";
            }
        }
        return null;
    }

    public int congSoLuongDichVu(int soLuongHienTai, int soLuongThem) {
        if (soLuongThem <= 0) {
            throw new IllegalArgumentException("Số lượng dịch vụ phải lớn hơn 0.");
        }
        return Math.addExact(soLuongHienTai, soLuongThem);
    }

    public String trangThaiTrongCoSoDuLieu(String trangThaiHienThi) {
        if ("Đang thực hiện".equals(trangThaiHienThi)) return KeHoachDieuTri.DANG_THUC_HIEN;
        if ("Hoàn thành".equals(trangThaiHienThi)) return KeHoachDieuTri.HOAN_THANH;
        if ("Hủy".equals(trangThaiHienThi)) return KeHoachDieuTri.HUY;
        throw new IllegalArgumentException("Trạng thái phác đồ không hợp lệ.");
    }

    public String trangThaiHienThi(String trangThai) {
        if ("Dang thuc hien".equals(trangThai)) return "Đang thực hiện";
        if ("Hoan thanh".equals(trangThai)) return "Hoàn thành";
        if ("Huy".equals(trangThai)) return "Hủy";
        return trangThai;
    }

    public String luuPhacDo(ChanDoan lanKham, String ten, Integer thoiGian, String trangThai,
            Map<String, Integer> dichVuDangChon) throws SQLException {
        String loi = kiemTraPhacDo(ten, thoiGian, dichVuDangChon);
        if (loi != null) {
            throw new IllegalArgumentException(loi);
        }
        KeHoachDieuTri keHoachCu = keHoachDieuTriDAO.timTheoChanDoan(lanKham.getMaChanDoan());
        KeHoachDieuTri keHoach = new KeHoachDieuTri();
        if (keHoachCu != null) {
            keHoach.setMaKeHoach(keHoachCu.getMaKeHoach());
        }
        keHoach.setTenPhacDo(ten.trim());
        keHoach.setThoiGianDuKien(thoiGian);
        keHoach.setTrangThai(trangThaiTrongCoSoDuLieu(trangThai));
        keHoach.setMaChanDoan(lanKham.getMaChanDoan());
        keHoach.setMaBenhNhan(lanKham.getMaBenhNhanId());
        List<String[]> chiTiet = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : dichVuDangChon.entrySet()) {
            chiTiet.add(new String[] { entry.getKey(), String.valueOf(entry.getValue()) });
        }
        return keHoachDieuTriDAO.luuPhacDo(keHoach, chiTiet);
    }
}

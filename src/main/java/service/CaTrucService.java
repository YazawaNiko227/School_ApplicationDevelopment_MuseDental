package service;

import dao.CaTrucDAO;
import entity.CaTruc;
import entity.NhanVien;
import entity.PhongKham;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class CaTrucService {
    private static final String PHONG_BAO_TRI = "Bao tri";
    private final CaTrucDAO caTrucDAO = new CaTrucDAO();

    public List<NhanVien> layTatCaNhanVien() throws SQLException {
        return caTrucDAO.layTatCaNhanVien();
    }

    public List<PhongKham> layTatCaPhong() throws SQLException {
        return caTrucDAO.layTatCaPhong();
    }

    public List<CaTruc> layTatCaCaTruc() throws SQLException {
        return caTrucDAO.layTatCa();
    }

    public void themCa(CaTruc ca) throws SQLException {
        String loi = kiemTra(ca, null);
        if (loi != null) throw new IllegalArgumentException(loi);
        caTrucDAO.them(ca);
    }

    public boolean capNhatCa(CaTruc caCu, CaTruc capNhat) throws SQLException {
        if (caCu == null) throw new IllegalArgumentException("Vui lòng chọn một ca trong bảng.");
        if (caCu.getNgay().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Ca của ngày đã qua không được sửa.");
        }
        String loi = kiemTra(capNhat, caCu);
        if (loi != null) throw new IllegalArgumentException(loi);
        capNhat.setMaCa(caCu.getMaCa());
        return caTrucDAO.capNhat(capNhat);
    }

    public boolean xoaCa(CaTruc ca) throws SQLException {
        if (ca == null) throw new IllegalArgumentException("Vui lòng chọn một ca trong bảng.");
        if (ca.getNgay().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Ca của ngày đã qua không được xóa.");
        }
        return caTrucDAO.xoa(ca.getMaCa());
    }

    private String kiemTra(CaTruc ca, CaTruc boQua) throws SQLException {
        if (ca == null) return "Thông tin ca trực không hợp lệ.";
        if (ca.getMaNhanVienId() == null) return "Vui lòng chọn nhân viên.";
        if (ca.getMaPhongId() == null) return "Vui lòng chọn phòng.";
        if (ca.getNgay().isBefore(LocalDate.now())) return "Không thể phân ca cho ngày đã qua.";
        if (!ca.getGioKetThuc().isAfter(ca.getGioBatDau())) return "Giờ kết thúc phải sau giờ bắt đầu.";
        List<PhongKham> dsPhong = caTrucDAO.layTatCaPhong();
        for (PhongKham phong : dsPhong) {
            if (ca.getMaPhongId().equals(phong.getMaPhong())
                    && PHONG_BAO_TRI.equals(phong.getTrangThai())) {
                return "Phòng " + ca.getMaPhongId()
                        + " đang bảo trì, không thể phân ca vào phòng này.";
            }
        }
        if (caTrucDAO.coCaTrung(ca, boQua == null ? null : boQua.getMaCa())) {
            String tenNhanVien = ca.getMaNhanVienId();
            for (NhanVien nhanVien : caTrucDAO.layTatCaNhanVien()) {
                if (ca.getMaNhanVienId().equals(nhanVien.getMaNhanVien())) {
                    tenNhanVien = nhanVien.getHoTen();
                    break;
                }
            }
            return tenNhanVien + " đã có ca trùng giờ trong ngày này.";
        }
        return null;
    }
}

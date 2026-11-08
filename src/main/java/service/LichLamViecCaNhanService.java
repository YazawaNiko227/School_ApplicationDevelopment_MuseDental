package service;

import dao.CaTrucDAO;
import dao.LichHenDAO;
import dao.NhanVienDAO;
import entity.LichHen;
import entity.CaTruc;
import entity.NhanVien;

import java.sql.SQLException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/** Nghiệp vụ tra cứu lịch làm việc cá nhân theo tài khoản và tuần. */
public class LichLamViecCaNhanService {
    private final CaTrucDAO caTrucDAO;
    private final LichHenDAO lichHenDAO;
    private final NhanVienDAO nhanVienDAO;

    public LichLamViecCaNhanService() {
        this(new CaTrucDAO(), new LichHenDAO(), new NhanVienDAO());
    }

    LichLamViecCaNhanService(CaTrucDAO caTrucDAO, LichHenDAO lichHenDAO, NhanVienDAO nhanVienDAO) {
        if (caTrucDAO == null || lichHenDAO == null || nhanVienDAO == null) {
            throw new IllegalArgumentException("Các DAO lịch làm việc không được null.");
        }
        this.caTrucDAO = caTrucDAO;
        this.lichHenDAO = lichHenDAO;
        this.nhanVienDAO = nhanVienDAO;
    }

    public NhanVien timNhanVien(String dinhDanh) throws SQLException {
        if (isBlank(dinhDanh)) {
            return null;
        }
        NhanVien nhanVien = nhanVienDAO.timTheoMa(dinhDanh.trim());
        return nhanVien == null ? nhanVienDAO.timTheoTenDangNhap(dinhDanh.trim()) : nhanVien;
    }

    public boolean laNhaSi(NhanVien nhanVien) {
        if (nhanVien == null || nhanVien.getChuyenMon() == null
                || isBlank(nhanVien.getChuyenMon().getTenChuyenMon())) {
            return false;
        }
        String chuyenMon = Normalizer.normalize(nhanVien.getChuyenMon().getTenChuyenMon(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
        return chuyenMon.contains("nha si") || chuyenMon.contains("bac si");
    }

    public List<CaTruc> layCaTrucTuan(String maNhanVien, LocalDate dauTuan, LocalDate cuoiTuan)
            throws SQLException {
        kiemTraKhoangTuan(maNhanVien, dauTuan, cuoiTuan);
        return caTrucDAO.layTheoTuan(maNhanVien, dauTuan, cuoiTuan);
    }

    public List<LichHen> layLichHenTuan(String maNhaSi, LocalDate dauTuan, LocalDate cuoiTuan)
            throws SQLException {
        kiemTraKhoangTuan(maNhaSi, dauTuan, cuoiTuan);
        return lichHenDAO.layTheoNhaSiTrongTuan(maNhaSi, dauTuan, cuoiTuan);
    }

    private void kiemTraKhoangTuan(String maNhanVien, LocalDate dauTuan, LocalDate cuoiTuan) {
        if (isBlank(maNhanVien)) {
            throw new IllegalArgumentException("Mã nhân viên không được để trống.");
        }
        if (dauTuan == null || cuoiTuan == null || cuoiTuan.isBefore(dauTuan)) {
            throw new IllegalArgumentException("Khoảng tuần không hợp lệ.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

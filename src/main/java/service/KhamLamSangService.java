package service;

import dao.ChanDoanDAO;
import dao.KhamLamSangDAO;
import dao.SoDoRangDAO;
import entity.CaKham;
import entity.ChanDoan;
import entity.SoDoRang;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/** Nghiệp vụ khám lâm sàng, chẩn đoán và cập nhật sơ đồ răng. */
public class KhamLamSangService {
    private static final List<String> TINH_TRANG_RANG = Arrays.asList(
            "Bình thường", "Sâu răng", "Viêm tủy", "Mẻ răng", "Mọc lệch", "Đã trám", "Viêm nướu", "Mất răng");

    private final KhamLamSangDAO khamLamSangDAO;
    private final ChanDoanDAO chanDoanDAO;
    private final SoDoRangDAO soDoRangDAO;

    public KhamLamSangService() {
        this(new KhamLamSangDAO(), new ChanDoanDAO(), new SoDoRangDAO());
    }

    KhamLamSangService(KhamLamSangDAO khamLamSangDAO, ChanDoanDAO chanDoanDAO, SoDoRangDAO soDoRangDAO) {
        if (khamLamSangDAO == null || chanDoanDAO == null || soDoRangDAO == null) {
            throw new IllegalArgumentException("Các DAO khám lâm sàng không được null.");
        }
        this.khamLamSangDAO = khamLamSangDAO;
        this.chanDoanDAO = chanDoanDAO;
        this.soDoRangDAO = soDoRangDAO;
    }

    public List<CaKham> layHangCho() throws SQLException {
        return khamLamSangDAO.layHangCho();
    }

    public ChanDoan vaoKham(String maLichHen, String trangThaiHienThi, String maHoSo,
            String maNhaSi, String maPhong) throws SQLException {
        if (!"Đã check-in".equals(trangThaiHienThi)) {
            throw new IllegalArgumentException("Chỉ vào khám được bệnh nhân đang ở trạng thái \"Đã check-in\".");
        }
        if (isBlank(maHoSo)) {
            throw new IllegalArgumentException("Bệnh nhân chưa có hồ sơ bệnh án nên chưa thể vào khám.");
        }
        return chanDoanDAO.taoNhap(chanDoanDAO.sinhMa(), LocalDate.now(),
                maLichHen, maHoSo, maNhaSi, maPhong);
    }

    public void kiemTraChanDoan(ChanDoan chanDoan, boolean chot) {
        if (chanDoan == null) {
            throw new IllegalArgumentException("Không có thông tin chẩn đoán để lưu.");
        }
        kiemTraNoiDungChanDoan(
                chanDoan.getLyDoKham(), chanDoan.getTrieuChung(), chanDoan.getKetLuanChanDoan(), chot);
    }

    public void kiemTraNoiDungChanDoan(String lyDo, String trieuChung, String ketLuan, boolean chot) {
        kiemTraDoDai(lyDo, 500, "Lý do khám");
        kiemTraDoDai(trieuChung, 1000, "Triệu chứng");
        kiemTraDoDai(ketLuan, 1000, "Kết luận chẩn đoán");
        if (chot && isBlank(ketLuan)) {
            throw new IllegalArgumentException("Kết luận chẩn đoán không được để trống khi hoàn tất khám.");
        }
    }

    public boolean luuNhap(ChanDoan chanDoan) throws SQLException {
        kiemTraChanDoanNoiBo(chanDoan, false);
        return chanDoanDAO.capNhatNhap(chanDoan);
    }

    public boolean hoanTat(ChanDoan chanDoan, String maLichHen, String maPhong) throws SQLException {
        kiemTraChanDoanNoiBo(chanDoan, true);
        return chanDoanDAO.chot(chanDoan, maLichHen, maPhong);
    }

    public List<SoDoRang> laySoDoRang(String maHoSo) throws SQLException {
        if (isBlank(maHoSo)) {
            throw new IllegalArgumentException("Mã hồ sơ bệnh án không được để trống.");
        }
        return soDoRangDAO.layTheoHoSo(maHoSo);
    }

    public boolean luuRang(String maHoSo, int maRang, String tinhTrang, String ghiChu) throws SQLException {
        if (isBlank(maHoSo)) {
            throw new IllegalArgumentException("Bệnh nhân chưa có hồ sơ bệnh án.");
        }
        int nhomRang = maRang / 10;
        int viTriRang = maRang % 10;
        boolean rangVinhVien = nhomRang >= 1 && nhomRang <= 4 && viTriRang >= 1 && viTriRang <= 8;
        boolean rangSua = nhomRang >= 5 && nhomRang <= 8 && viTriRang >= 1 && viTriRang <= 5;
        if (!rangVinhVien && !rangSua) {
            throw new IllegalArgumentException("Mã răng không hợp lệ.");
        }
        if (!TINH_TRANG_RANG.contains(tinhTrang)) {
            throw new IllegalArgumentException("Tình trạng răng không hợp lệ.");
        }
        String ghiChuDaTrim = ghiChu == null ? "" : ghiChu.trim();
        if (ghiChuDaTrim.length() > 500) {
            throw new IllegalArgumentException("Ghi chú răng tối đa 500 ký tự.");
        }
        return soDoRangDAO.luuHoacCapNhat(new SoDoRang(maHoSo, maRang, loaiRang(maRang), tinhTrang, ghiChuDaTrim));
    }

    public boolean xoaRang(String maHoSo, int maRang) throws SQLException {
        if (isBlank(maHoSo)) {
            throw new IllegalArgumentException("Bệnh nhân chưa có hồ sơ bệnh án.");
        }
        return soDoRangDAO.xoa(maHoSo, maRang);
    }

    public String loaiRang(int maRang) {
        if (maRang >= 50) {
            return "Răng sữa";
        }
        int viTri = maRang % 10;
        if (viTri <= 2) {
            return "Răng cửa";
        } else if (viTri == 3) {
            return "Răng nanh";
        } else if (viTri <= 5) {
            return "Răng hàm nhỏ";
        } else if (viTri <= 7) {
            return "Răng hàm";
        }
        return "Răng khôn";
    }

    private void kiemTraChanDoanNoiBo(ChanDoan chanDoan, boolean chot) {
        if (chanDoan == null) {
            throw new IllegalArgumentException("Không có thông tin chẩn đoán để lưu.");
        }
        kiemTraNoiDungChanDoan(
                chanDoan.getLyDoKham(), chanDoan.getTrieuChung(), chanDoan.getKetLuanChanDoan(), chot);
    }

    private void kiemTraDoDai(String value, int max, String field) {
        if (value != null && value.trim().length() > max) {
            throw new IllegalArgumentException(field + " tối đa " + max + " ký tự.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

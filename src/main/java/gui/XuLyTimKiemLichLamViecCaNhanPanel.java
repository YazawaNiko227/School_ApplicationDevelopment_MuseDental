package gui;

import dao.CaTrucDAO;
import dao.LichHenDAO;
import dao.NhanVienDAO;
import dto.LichHenInfo;
import entity.CaTruc;
import entity.NhanVien;
import util.GiaoDienUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Panel "Lịch làm việc cá nhân" (phục vụ mục TÌM KIẾM → "Lịch làm việc cá nhân").
 * Kết nối CSDL trực tiếp qua CaTrucDAO, LichHenDAO và NhanVienDAO.
 *
 * Chức năng chính:
 * 1. Xem lịch trực cá nhân theo tuần (bảng CaTruc).
 * 2. Nha sĩ xem danh sách bệnh nhân hẹn khám với mình trong tuần (bảng LichHen).
 * 3. Hỗ trợ điều hướng chuyển đổi tuần làm việc (Tuần trước, Tuần này, Tuần sau).
 */
public class XuLyTimKiemLichLamViecCaNhanPanel extends JPanel {

    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm");
    private static final String[] THU_TRONG_TUAN = { "Thứ hai", "Thứ ba", "Thứ tư", "Thứ năm", "Thứ sáu", "Thứ bảy", "Chủ nhật" };

    private final CaTrucDAO caTrucDAO = new CaTrucDAO();
    private final LichHenDAO lichHenDAO = new LichHenDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();

    private String maNhanVien;
    private String hoTen = "";
    private String chuyenMon = "";
    private boolean laNhaSi = false;

    // Ngày đầu tuần đang xem (luôn là Thứ hai)
    private LocalDate dauTuan = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

    // Thẻ thống kê
    private final JLabel lblSoCaTruc = new JLabel("0");
    private final JLabel lblSoLichHen = new JLabel("0");

    // Thẻ ca trực
    private final JLabel lblThongTinNhanVien = new JLabel(" ");
    private final DefaultTableModel modelCa = new DefaultTableModel(
            new String[] { "Mã ca", "Ngày", "Thứ", "Bắt đầu", "Kết thúc", "Loại ca", "Phòng" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable bangCa = new JTable(modelCa);

    // Thẻ lịch hẹn bệnh nhân
    private final JTextField txtTimBenhNhan = new JTextField();
    private final DefaultTableModel modelLich = new DefaultTableModel(
            new String[] { "Mã lịch", "Giờ hẹn", "Mã BN", "Bệnh nhân", "Phòng", "Trạng thái" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable bangLich = new JTable(modelLich);
    private final List<LichHenInfo> danhSachLichTuan = new ArrayList<>();

    public XuLyTimKiemLichLamViecCaNhanPanel() {
        this("NV001"); // Mặc định NV001 cho chạy thử
    }

    public XuLyTimKiemLichLamViecCaNhanPanel(String maNhanVien) {
        this.maNhanVien = maNhanVien == null || maNhanVien.trim().isEmpty() ? "NV001" : maNhanVien.trim();

        setLayout(new BorderLayout(0, 18));
        setOpaque(false);

        napThongTinNhanVien();

        add(taoThongKeThe(), BorderLayout.NORTH);

        JPanel vungNoiDung = new JPanel(new BorderLayout(0, 16));
        vungNoiDung.setOpaque(false);
        vungNoiDung.add(taoTheCaTruc(), BorderLayout.NORTH);
        vungNoiDung.add(taoTheLichHen(), BorderLayout.CENTER);
        add(vungNoiDung, BorderLayout.CENTER);

        lamMoiTuan();
    }

    // =========================================================================
    // 1. XÂY DỰNG GIAO DIỆN
    // =========================================================================

    /** Thẻ thống kê nhanh trên cùng. */
    private JPanel taoThongKeThe() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        p.setOpaque(false);
        p.add(taoCardChiSo("CA TRỰC TRONG TUẦN", lblSoCaTruc, GiaoDienUtil.TEAL));
        p.add(taoCardChiSo("LỊCH HẸN BỆNH NHÂN", lblSoLichHen, GiaoDienUtil.BLUE));
        return p;
    }

    private JPanel taoCardChiSo(String tieuDe, JLabel lblGiaTri, Color mauSo) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        GiaoDienUtil.trangTriThe(card);
        card.setPreferredSize(new Dimension(240, 75));

        JLabel caption = GiaoDienUtil.taoNhanPhu(tieuDe);
        lblGiaTri.setFont(new Font("Arial", Font.BOLD, 22));
        lblGiaTri.setForeground(mauSo);

        card.add(caption);
        card.add(Box.createVerticalStrut(4));
        card.add(lblGiaTri);
        return card;
    }

    /** Thẻ hiển thị các ca trực của nhân viên trong tuần. */
    private JPanel taoTheCaTruc() {
        JPanel the = new JPanel(new BorderLayout(0, 12));
        GiaoDienUtil.trangTriThe(the);

        // Thanh tiêu đề + nút chuyển tuần
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.add(GiaoDienUtil.taoTieuDe("Lịch phân công ca trực"));
        titleBox.add(Box.createVerticalStrut(4));

        lblThongTinNhanVien.setFont(GiaoDienUtil.CHU);
        lblThongTinNhanVien.setForeground(GiaoDienUtil.MUTED);
        titleBox.add(lblThongTinNhanVien);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        nav.setOpaque(false);
        JButton btnTruoc = GiaoDienUtil.taoNut("< Tuần trước", false);
        JButton btnNay = GiaoDienUtil.taoNut("Tuần này", false);
        JButton btnSau = GiaoDienUtil.taoNut("Tuần sau >", false);

        btnTruoc.addActionListener(e -> doiTuan(-1));
        btnNay.addActionListener(e -> {
            dauTuan = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            lamMoiTuan();
        });
        btnSau.addActionListener(e -> doiTuan(1));

        nav.add(btnTruoc);
        nav.add(btnNay);
        nav.add(btnSau);

        header.add(titleBox, BorderLayout.WEST);
        header.add(nav, BorderLayout.EAST);
        the.add(header, BorderLayout.NORTH);

        // Bảng ca trực
        GiaoDienUtil.tuyChinhBang(bangCa);
        bangCa.getColumnModel().getColumn(0).setPreferredWidth(60);
        bangCa.getColumnModel().getColumn(1).setPreferredWidth(90);
        bangCa.getColumnModel().getColumn(2).setPreferredWidth(75);
        bangCa.getColumnModel().getColumn(3).setPreferredWidth(60);
        bangCa.getColumnModel().getColumn(4).setPreferredWidth(60);
        bangCa.getColumnModel().getColumn(5).setPreferredWidth(80);
        bangCa.getColumnModel().getColumn(6).setPreferredWidth(55);

        JScrollPane cuonBang = new JScrollPane(bangCa);
        cuonBang.setBorder(BorderFactory.createLineBorder(GiaoDienUtil.LINE));
        cuonBang.getViewport().setBackground(Color.WHITE);
        cuonBang.setPreferredSize(new Dimension(100, 180));
        the.add(cuonBang, BorderLayout.CENTER);

        return the;
    }

    /** Thẻ danh sách bệnh nhân hẹn khám với nha sĩ trong tuần. */
    private JPanel taoTheLichHen() {
        JPanel the = new JPanel(new BorderLayout(0, 12));
        GiaoDienUtil.trangTriThe(the);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(GiaoDienUtil.taoTieuDe("Bệnh nhân hẹn khám trong tuần"), BorderLayout.WEST);

        // Tìm kiếm nhanh bệnh nhân
        JPanel search = new JPanel(new BorderLayout(6, 0));
        search.setOpaque(false);
        GiaoDienUtil.tuyChinhO(txtTimBenhNhan);
        txtTimBenhNhan.setPreferredSize(new Dimension(220, 32));
        txtTimBenhNhan.setToolTipText("Nhập mã lịch, mã BN hoặc tên bệnh nhân");
        txtTimBenhNhan.addActionListener(e -> locBangLichHen());
        JButton btnTim = GiaoDienUtil.taoNut("Tìm", false);
        btnTim.addActionListener(e -> locBangLichHen());

        search.add(txtTimBenhNhan, BorderLayout.CENTER);
        search.add(btnTim, BorderLayout.EAST);
        header.add(search, BorderLayout.EAST);
        the.add(header, BorderLayout.NORTH);

        // Bảng lịch hẹn
        GiaoDienUtil.tuyChinhBang(bangLich);
        bangLich.getColumnModel().getColumn(0).setPreferredWidth(60);
        bangLich.getColumnModel().getColumn(1).setPreferredWidth(95);
        bangLich.getColumnModel().getColumn(2).setPreferredWidth(60);
        bangLich.getColumnModel().getColumn(3).setPreferredWidth(160);
        bangLich.getColumnModel().getColumn(4).setPreferredWidth(55);
        bangLich.getColumnModel().getColumn(5).setPreferredWidth(100);
        bangLich.getColumnModel().getColumn(5).setCellRenderer(new TrangThaiLichRenderer());

        JScrollPane cuonLich = new JScrollPane(bangLich);
        cuonLich.setBorder(BorderFactory.createLineBorder(GiaoDienUtil.LINE));
        cuonLich.getViewport().setBackground(Color.WHITE);
        the.add(cuonLich, BorderLayout.CENTER);

        return the;
    }

    // =========================================================================
    // 2. XỬ LÝ DỮ LIỆU TỪ DAO
    // =========================================================================

    /** Nạp thông tin nhân viên theo mã hoặc tên đăng nhập từ NhanVienDAO. */
    private void napThongTinNhanVien() {
        try {
            NhanVien nv = nhanVienDAO.timTheoMa(maNhanVien);
            if (nv == null) {
                nv = nhanVienDAO.timTheoTenDangNhap(maNhanVien);
            }
            if (nv != null) {
                this.maNhanVien = nv.getMaNhanVien();
                this.hoTen = nv.getHoTen();
                this.chuyenMon = nv.getChuyenMon() == null ? "" : nv.getChuyenMon();
                this.laNhaSi = chuyenMon.toLowerCase().contains("nha sĩ") || chuyenMon.toLowerCase().contains("bác sĩ");
            } else {
                this.hoTen = maNhanVien;
                this.chuyenMon = "Nhân viên";
                this.laNhaSi = true;
            }
        } catch (SQLException e) {
            this.hoTen = maNhanVien;
            this.chuyenMon = "Nhân viên";
        }
    }

    /** Nạp lại bảng ca trực và bảng lịch hẹn theo khoảng ngày của tuần. */
    private void lamMoiTuan() {
        LocalDate cuoiTuan = dauTuan.plusDays(6);
        lblThongTinNhanVien.setText(hoTen + "  ·  " + maNhanVien + " (" + chuyenMon + ")  ·  Tuần "
                + dauTuan.format(DINH_DANG_NGAY) + " - " + cuoiTuan.format(DINH_DANG_NGAY));

        // 1. Nạp ca trực từ CaTrucDAO
        try {
            List<CaTruc> caList = caTrucDAO.layTheoTuan(maNhanVien, dauTuan, cuoiTuan);
            modelCa.setRowCount(0);
            for (CaTruc ca : caList) {
                int thuIdx = ca.getNgay().getDayOfWeek().getValue() - 1;
                modelCa.addRow(new Object[] {
                        ca.getMaCa(),
                        ca.getNgay().format(DINH_DANG_NGAY),
                        THU_TRONG_TUAN[thuIdx],
                        ca.getGioBatDau().format(DINH_DANG_GIO),
                        ca.getGioKetThuc().format(DINH_DANG_GIO),
                        ca.getLoaiCa() == null ? "" : ca.getLoaiCa(),
                        ca.getMaPhong()
                });
            }
            lblSoCaTruc.setText(String.valueOf(caList.size()));
        } catch (SQLException e) {
            GiaoDienUtil.baoLoi(this, e);
        }

        // 2. Nạp lịch hẹn từ LichHenDAO (nếu là nha sĩ)
        try {
            danhSachLichTuan.clear();
            if (laNhaSi) {
                danhSachLichTuan.addAll(lichHenDAO.layTheoNhaSiTrongTuan(maNhanVien, dauTuan, cuoiTuan));
            }
            lblSoLichHen.setText(String.valueOf(danhSachLichTuan.size()));
            locBangLichHen();
        } catch (SQLException e) {
            GiaoDienUtil.baoLoi(this, e);
        }
    }

    /** Lọc bảng lịch hẹn theo từ khóa tìm kiếm (mã lịch, mã BN, tên BN). */
    private void locBangLichHen() {
        String tuKhoa = txtTimBenhNhan.getText().trim().toLowerCase();
        modelLich.setRowCount(0);

        for (LichHenInfo lh : danhSachLichTuan) {
            boolean khop = tuKhoa.isEmpty()
                    || lh.maLichHen().toLowerCase().contains(tuKhoa)
                    || lh.maBenhNhan().toLowerCase().contains(tuKhoa)
                    || (lh.hoTen() != null && lh.hoTen().toLowerCase().contains(tuKhoa));
            if (khop) {
                modelLich.addRow(new Object[] {
                        lh.maLichHen(),
                        GiaoDienUtil.dinhDangNgayGio(lh.thoiGianHen()),
                        lh.maBenhNhan(),
                        lh.hoTen(),
                        lh.maPhong(),
                        GiaoDienUtil.hienThiTrangThai(lh.trangThai())
                });
            }
        }
    }

    private void doiTuan(int soTuan) {
        dauTuan = dauTuan.plusWeeks(soTuan);
        lamMoiTuan();
    }

    // =========================================================================
    // 3. RENDERER TRẠNG THÁI LỊCH HẸN
    // =========================================================================

    private static final class TrangThaiLichRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            setBorder(new EmptyBorder(0, 8, 0, 8));
            setFont(GiaoDienUtil.CHU_DAM);
            if (!isSelected) setBackground(Color.WHITE);

            String tt = value == null ? "" : value.toString();
            switch (tt) {
                case "Đang khám" -> setForeground(GiaoDienUtil.TEAL);
                case "Đã check-in", "Đã xác nhận" -> setForeground(GiaoDienUtil.BLUE);
                case "Chờ xác nhận" -> setForeground(GiaoDienUtil.ORANGE);
                case "Hoàn thành" -> setForeground(new Color(38, 143, 85));
                case "Hủy", "Vắng" -> setForeground(GiaoDienUtil.RED);
                default -> setForeground(GiaoDienUtil.INK);
            }
            return this;
        }
    }
}
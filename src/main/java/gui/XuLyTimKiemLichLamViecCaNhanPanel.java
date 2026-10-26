package gui;

import entity.CaTruc;
import entity.LichHen;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
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
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Panel "Lịch làm việc cá nhân" (luồng nhân sự và ca trực).
 *
 * Nhân viên đang đăng nhập thực hiện:
 * 1. Xem các ca trực của mình theo tuần (bảng CaTruc, lọc theo MaNhanVien).
 * 2. Nha sĩ xem bệnh nhân hẹn khám với mình trong tuần (bảng LichHen, lọc theo
 * MaNhaSiPhuTrach), có ô tìm theo mã / tên bệnh nhân.
 *
 * Hiện tại dùng dữ liệu mẫu trong bộ nhớ. Chỗ cần nối DAO ghi "TODO DAO".
 */
public class XuLyTimKiemLichLamViecCaNhanPanel extends JPanel {
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color LINE = new Color(226, 234, 237);
    private static final Color ORANGE = new Color(177, 125, 47);
    private static final Color RED = new Color(200, 70, 70);
    private static final Color BLUE = new Color(51, 91, 177);

    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter GIO = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter NGAY_GIO = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private static final String[] THU = { "Thứ hai", "Thứ ba", "Thứ tư", "Thứ năm", "Thứ sáu", "Thứ bảy",
            "Chủ nhật" };

    // Trạng thái lịch hẹn (hiển thị có dấu, DB lưu không dấu).
    private static final String CHO_XAC_NHAN = "Chờ xác nhận";
    private static final String DA_XAC_NHAN = "Đã xác nhận";
    private static final String DA_CHECK_IN = "Đã check-in";
    private static final String DANG_KHAM = "Đang khám";
    private static final String HOAN_THANH = "Hoàn thành";
    private static final String VANG = "Vắng";
    private static final String HUY = "Hủy";

    // Loại ca (hiển thị có dấu, DB lưu không dấu).
    private static final String CA_SANG = "Ca sáng";
    private static final String CA_CHIEU = "Ca chiều";

    // TODO DAO: NhanVienDAO.timTheoMa(maNhanVien) thay cho dữ liệu này.
    // {MaNhanVien, HoTen, ChuyenMon}
    private static final String[][] MOCK_NHAN_VIEN = {
            { "NV001", "Nguyễn Văn An", "Nha sĩ - Nhổ răng" },
            { "NV002", "Trần Thị Bình", "Nha sĩ - Chỉnh nha" },
            { "NV003", "Lê Minh Châu", "Lễ tân" },
            { "NV004", "Phạm Quốc Dũng", "Thu ngân" },
            { "NV005", "Hoàng Thị Em", "Thu kho" },
            { "NV006", "Võ Thị Phương", "Y tá" }
    };

    // {MaBenhNhan, HoTen}
    private static final String[][] MOCK_BENH_NHAN = {
            { "BN001", "Võ Thanh Hải" },
            { "BN002", "Nguyễn Thị Lan" },
            { "BN003", "Đặng Quốc Khánh" },
            { "BN004", "Trần Bảo Ngọc" },
            { "BN005", "Phạm Gia Bảo" },
            { "BN006", "Lê Ngọc Hà" }
    };

    private final String maNhanVien;
    private final String hoTen;
    private final String chuyenMon;
    private final boolean laNhaSi;

    // Chỉ chứa dữ liệu của nhân viên đang đăng nhập.
    private final List<CaTruc> dsCa = new ArrayList<>();
    private final List<LichHen> dsLichHen = new ArrayList<>();
    private LocalDate dauTuan = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    private int nextCa = 1;
    private int nextLich = 1;

    // ----- Thẻ thống kê -----
    private final JLabel[] summaryValues = new JLabel[2];

    // ----- Lịch trực -----
    private final JLabel lblThongTin = new JLabel(" ");
    private final DefaultTableModel caModel = new DefaultTableModel(
            new String[] { "Mã ca", "Ngày", "Thứ", "Bắt đầu", "Kết thúc", "Loại ca", "Phòng" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable caTable = new JTable(caModel);

    // ----- Bệnh nhân hẹn khám -----
    private final JTextField txtTimBenhNhan = new JTextField();
    private final JLabel lblLichGoiY = new JLabel(" ");
    private final DefaultTableModel lichModel = new DefaultTableModel(
            new String[] { "Mã lịch", "Giờ hẹn", "Mã BN", "Bệnh nhân", "Phòng", "Trạng thái" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable lichTable = new JTable(lichModel);

    /**
     * Mặc định dùng nhân viên NV001 để thử giao diện.
     * TODO: truyền mã nhân viên của tài khoản đang đăng nhập từ ManHinhChinhFrame
     * (TaiKhoan.TenDangNhap -> TaiKhoan.MaNhanVien) bằng constructor bên dưới.
     */
    public XuLyTimKiemLichLamViecCaNhanPanel() {
        this("NV001");
    }

    public XuLyTimKiemLichLamViecCaNhanPanel(String maNhanVien) {
        this.maNhanVien = maNhanVien;
        String[] nv = timNhanVien(maNhanVien);
        this.hoTen = nv == null ? maNhanVien : nv[1];
        this.chuyenMon = nv == null ? "" : nv[2];
        this.laNhaSi = chuyenMon.startsWith("Nha sĩ");

        setLayout(new BorderLayout(0, 18));
        setOpaque(false);

        taoDuLieuMau();

        add(createSummaryPanel(), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);
        content.add(createCaCard(), BorderLayout.NORTH);
        content.add(createLichHenCard(), BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);

        lamMoiTuan();
    }

    // =====================================================================
    // DỮ LIỆU MẪU
    // =====================================================================

    private void taoDuLieuMau() {
        // TODO DAO: CaTrucDAO.layTheoNhanVien(maNhanVien),
        // LichHenDAO.layTheoNhaSi(maNhanVien) thay cho dữ liệu này.
        themCa(-6, LocalTime.of(8, 0), LocalTime.of(12, 0), CA_SANG, "P01"); // CA001 (đã qua)
        themCa(0, LocalTime.of(8, 0), LocalTime.of(12, 0), CA_SANG, "P01"); // CA002
        themCa(1, LocalTime.of(13, 0), LocalTime.of(17, 0), CA_CHIEU, "P01"); // CA003
        themCa(3, LocalTime.of(8, 0), LocalTime.of(12, 0), CA_SANG, "P01"); // CA004
        themCa(7, LocalTime.of(8, 0), LocalTime.of(12, 0), CA_SANG, "P01"); // CA005
        themCa(8, LocalTime.of(13, 0), LocalTime.of(17, 0), CA_CHIEU, "P01"); // CA006

        if (laNhaSi) {
            themLich(-6, 9, 0, "BN005", HOAN_THANH);
            themLich(0, 8, 30, "BN003", DA_XAC_NHAN);
            themLich(0, 9, 30, "BN001", CHO_XAC_NHAN);
            themLich(1, 14, 0, "BN002", DA_XAC_NHAN);
            themLich(3, 9, 0, "BN004", DA_XAC_NHAN);
            themLich(7, 8, 30, "BN006", DA_XAC_NHAN);
        }

    }

    private void themCa(int ngayTuDauTuan, LocalTime batDau, LocalTime ketThuc, String loai, String maPhong) {
        CaTruc ca = new CaTruc(String.format("CA%03d", nextCa++), dauTuan.plusDays(ngayTuDauTuan),
                batDau, ketThuc, loai, maNhanVien, maPhong);
        dsCa.add(ca);
    }

    private void themLich(int ngayTuDauTuan, int gio, int phut, String maBn, String trangThai) {
        LichHen lh = new LichHen(String.format("LH%03d", nextLich++),
                dauTuan.plusDays(ngayTuDauTuan).atTime(gio, phut), trangThaiLuu(trangThai), LocalDate.now(), null,
                maNhanVien, maNhanVien, maBn, "P01", null);
        dsLichHen.add(lh);
    }

    private String trangThaiLuu(String trangThai) {
        switch (trangThai) {
            case CHO_XAC_NHAN:
                return "Cho xac nhan";
            case DA_XAC_NHAN:
                return "Da xac nhan";
            case DA_CHECK_IN:
                return "Da check-in";
            case DANG_KHAM:
                return "Dang kham";
            case HOAN_THANH:
                return "Hoan thanh";
            case VANG:
                return "Vang";
            case HUY:
                return "Huy";
            default:
                throw new IllegalArgumentException("Trạng thái lịch hẹn không hợp lệ: " + trangThai);
        }
    }

    private String trangThaiHienThi(String trangThai) {
        switch (trangThai) {
            case "Cho xac nhan":
                return CHO_XAC_NHAN;
            case "Da xac nhan":
                return DA_XAC_NHAN;
            case "Da check-in":
                return DA_CHECK_IN;
            case "Dang kham":
                return DANG_KHAM;
            case "Hoan thanh":
                return HOAN_THANH;
            case "Vang":
                return VANG;
            case "Huy":
                return HUY;
            default:
                return trangThai;
        }
    }

    // =====================================================================
    // GIAO DIỆN
    // =====================================================================

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        panel.setOpaque(false);
        panel.add(createSummaryCard(0, "CA TRỰC TRONG TUẦN", TEAL));
        panel.add(createSummaryCard(1, "LỊCH HẸN TRONG TUẦN", BLUE));
        return panel;
    }

    private JPanel createSummaryCard(int index, String title, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(12, 16, 12, 16)));
        card.setPreferredSize(new Dimension(200, 78));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 9));
        titleLabel.setForeground(MUTED);

        summaryValues[index] = new JLabel("0");
        summaryValues[index].setFont(new Font("Arial", Font.BOLD, 24));
        summaryValues[index].setForeground(color);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(summaryValues[index]);
        return card;
    }

    // Lịch trực của tôi theo tuần.
    private JPanel createCaCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(16, 18, 16, 18)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Lịch trực của tôi");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setForeground(INK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblThongTin.setFont(new Font("Arial", Font.PLAIN, 11));
        lblThongTin.setForeground(MUTED);
        lblThongTin.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(3));
        titleBox.add(lblThongTin);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        nav.setOpaque(false);
        JButton btnTruoc = createButton("< Tuần trước", false);
        JButton btnNay = createButton("Tuần này", false);
        JButton btnSau = createButton("Tuần sau >", false);
        btnTruoc.addActionListener(event -> doiTuan(-1));
        btnNay.addActionListener(event -> {
            dauTuan = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            lamMoiTuan();
        });
        btnSau.addActionListener(event -> doiTuan(1));
        nav.add(btnTruoc);
        nav.add(btnNay);
        nav.add(btnSau);
        header.add(titleBox, BorderLayout.WEST);
        header.add(nav, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        styleTable(caTable);
        caTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        caTable.getColumnModel().getColumn(1).setPreferredWidth(90);
        caTable.getColumnModel().getColumn(2).setPreferredWidth(75);
        caTable.getColumnModel().getColumn(3).setPreferredWidth(60);
        caTable.getColumnModel().getColumn(4).setPreferredWidth(60);
        caTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        caTable.getColumnModel().getColumn(6).setPreferredWidth(55);
        JScrollPane scroll = new JScrollPane(caTable);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setPreferredSize(new Dimension(100, 190));
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // Bệnh nhân hẹn khám với tôi trong tuần.
    private JPanel createLichHenCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(16, 18, 16, 18)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Bệnh nhân hẹn khám trong tuần");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setForeground(INK);

        JPanel search = new JPanel(new BorderLayout(6, 0));
        search.setOpaque(false);
        styleField(txtTimBenhNhan);
        txtTimBenhNhan.setPreferredSize(new Dimension(200, 32));
        txtTimBenhNhan.setToolTipText("Nhập mã lịch, mã hoặc tên bệnh nhân");
        txtTimBenhNhan.addActionListener(event -> lamMoiLichHen());
        JButton btnTim = createButton("Tìm", false);
        btnTim.addActionListener(event -> lamMoiLichHen());
        search.add(txtTimBenhNhan, BorderLayout.CENTER);
        search.add(btnTim, BorderLayout.EAST);
        header.add(title, BorderLayout.WEST);
        header.add(search, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        styleTable(lichTable);
        lichTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        lichTable.getColumnModel().getColumn(1).setPreferredWidth(95);
        lichTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        lichTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        lichTable.getColumnModel().getColumn(4).setPreferredWidth(55);
        lichTable.getColumnModel().getColumn(5).setPreferredWidth(95);
        lichTable.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer());
        JScrollPane scroll = new JScrollPane(lichTable);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        lblLichGoiY.setFont(new Font("Arial", Font.PLAIN, 11));
        lblLichGoiY.setForeground(MUTED);
        card.add(lblLichGoiY, BorderLayout.SOUTH);
        return card;
    }

    private void doiTuan(int soTuan) {
        dauTuan = dauTuan.plusWeeks(soTuan);
        lamMoiTuan();
    }

    // =====================================================================
    // ĐỒNG BỘ GIAO DIỆN
    // =====================================================================

    // Nạp lại bảng ca trực, bảng lịch hẹn và thẻ thống kê theo tuần đang xem.
    private void lamMoiTuan() {
        LocalDate cuoiTuan = dauTuan.plusDays(6);

        caModel.setRowCount(0);
        List<CaTruc> trongTuan = new ArrayList<>();
        for (CaTruc ca : dsCa) {
            if (!ca.getNgay().isBefore(dauTuan) && !ca.getNgay().isAfter(cuoiTuan)) {
                trongTuan.add(ca);
            }
        }
        trongTuan.sort(Comparator.comparing(CaTruc::getNgay).thenComparing(CaTruc::getGioBatDau));
        for (CaTruc ca : trongTuan) {
            caModel.addRow(new Object[] {
                    ca.getMaCa(),
                    ca.getNgay().format(NGAY),
                    THU[ca.getNgay().getDayOfWeek().getValue() - 1],
                    ca.getGioBatDau().format(GIO),
                    ca.getGioKetThuc().format(GIO),
                    ca.getLoaiCa(),
                    ca.getMaPhong() });
        }
        lblThongTin.setText(hoTen + "  ·  " + maNhanVien + "  ·  " + chuyenMon + "  ·  Tuần "
                + dauTuan.format(NGAY) + " - " + cuoiTuan.format(NGAY));

        lamMoiLichHen();

        int soLichTuan = 0;
        for (LichHen lh : dsLichHen) {
            if (trongTuan(lh.getThoiGianHen().toLocalDate())) {
                soLichTuan++;
            }
        }
        summaryValues[0].setText(String.valueOf(trongTuan.size()));
        summaryValues[1].setText(String.valueOf(soLichTuan));
    }

    // Nạp lại bảng lịch hẹn của tuần, lọc theo ô tìm kiếm.
    private void lamMoiLichHen() {
        String tuKhoa = txtTimBenhNhan.getText().trim().toLowerCase();
        lichModel.setRowCount(0);

        List<LichHen> ds = new ArrayList<>();
        for (LichHen lh : dsLichHen) {
            if (!maNhanVien.equals(lh.getMaNhaSiPhuTrach())
                    || !trongTuan(lh.getThoiGianHen().toLocalDate())) {
                continue;
            }
            String tenBenhNhan = tenBenhNhan(lh.getMaBenhNhan());
            boolean khop = tuKhoa.isEmpty()
                    || lh.getMaLichHen().toLowerCase().contains(tuKhoa)
                    || lh.getMaBenhNhan().toLowerCase().contains(tuKhoa)
                    || tenBenhNhan.toLowerCase().contains(tuKhoa);
            if (khop) {
                ds.add(lh);
            }
        }
        ds.sort(Comparator.comparing(LichHen::getThoiGianHen));
        for (LichHen lh : ds) {
            lichModel.addRow(new Object[] {
                    lh.getMaLichHen(), lh.getThoiGianHen().format(NGAY_GIO), lh.getMaBenhNhan(),
                    tenBenhNhan(lh.getMaBenhNhan()), lh.getMaPhong(), trangThaiHienThi(lh.getTrangThai()) });
        }

        if (!laNhaSi) {
            lblLichGoiY.setText("Chỉ nha sĩ phụ trách mới có lịch hẹn bệnh nhân.");
        } else if (ds.isEmpty()) {
            lblLichGoiY.setText("Không có lịch hẹn nào trong tuần này" + (tuKhoa.isEmpty() ? "." : " khớp từ khóa."));
        } else {
            lblLichGoiY.setText("Có " + ds.size() + " lịch hẹn trong tuần.");
        }
    }

    // =====================================================================
    // HÀM HỖ TRỢ
    // =====================================================================

    private boolean trongTuan(LocalDate ngay) {
        return !ngay.isBefore(dauTuan) && !ngay.isAfter(dauTuan.plusDays(6));
    }

    private static String tenBenhNhan(String maBenhNhan) {
        for (String[] benhNhan : MOCK_BENH_NHAN) {
            if (benhNhan[0].equals(maBenhNhan)) {
                return benhNhan[1];
            }
        }
        return maBenhNhan;
    }

    private static String[] timNhanVien(String ma) {
        for (String[] nv : MOCK_NHAN_VIEN) {
            if (nv[0].equals(ma)) {
                return nv;
            }
        }
        return null;
    }

    // ----- Tạo thành phần giao diện -----

    private void styleField(JTextField field) {
        field.setFont(new Font("Arial", Font.PLAIN, 12));
        field.setForeground(INK);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(0, 8, 0, 8)));
    }

    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setForeground(INK);
        table.setShowVerticalLines(false);
        table.setGridColor(LINE);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(231, 246, 245));
        table.setSelectionForeground(INK);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        table.getTableHeader().setReorderingAllowed(false);
    }

    private static JButton createButton(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        if (primary) {
            button.setBackground(TEAL);
            button.setForeground(Color.WHITE);
            button.setBorder(new EmptyBorder(8, 16, 8, 16));
            button.setBorderPainted(false);
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(INK);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(LINE), new EmptyBorder(7, 15, 7, 15)));
        }
        return button;
    }

    // Tô màu chữ theo trạng thái lịch hẹn.
    private static final class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 11));
            String text = String.valueOf(value);
            if (DA_XAC_NHAN.equals(text) || HOAN_THANH.equals(text)) {
                label.setForeground(TEAL);
            } else if (CHO_XAC_NHAN.equals(text) || DA_CHECK_IN.equals(text)) {
                label.setForeground(ORANGE);
            } else if (DANG_KHAM.equals(text)) {
                label.setForeground(BLUE);
            } else if (VANG.equals(text) || HUY.equals(text)) {
                label.setForeground(RED);
            } else {
                label.setForeground(MUTED);
            }
            return label;
        }
    }
}
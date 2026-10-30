package gui;

import dao.CaTrucDAO;
import entity.CaTruc;
import entity.NhanVien;
import entity.PhongKham;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Panel "Phân công lịch làm việc và ca trực" (luồng nhân sự và ca trực).
 *
 * Người quản lý thực hiện:
 * 1. Xem các ca trực theo tuần (nút tuần trước / tuần này / tuần sau), lọc theo
 * nhân viên và phòng.
 * 2. Điền form bên phải rồi bấm "Thêm ca" để phân công (bảng CaTruc).
 * 3. Chọn một dòng trong bảng để sửa ("Cập nhật ca") hoặc "Xóa ca".
 *
 * Quy tắc kiểm tra (khớp ràng buộc trong SQL và nghiệp vụ):
 * - Giờ kết thúc phải sau giờ bắt đầu (CK_CaTruc_GioHopLe).
 * - Một nhân viên không được có hai ca trùng giờ trong cùng một ngày.
 * - Không phân ca vào phòng đang "Bảo trì".
 * - Không thêm, sửa, xóa ca của ngày đã qua.
 * - Một phòng được phép có nhiều nhân viên cùng ca (dữ liệu mẫu SQL có CA001
 * và CA003 cùng phòng P01 trùng giờ).
 *
 * Dữ liệu được đọc và cập nhật trực tiếp qua CaTrucDAO.
 */
public class XuLyBangPhanCongCaTrucYTaPanel extends JPanel {
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color LINE = new Color(226, 234, 237);
    private static final Color ORANGE = new Color(177, 125, 47);
    private static final Color BLUE = new Color(51, 91, 177);

    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter GIO = DateTimeFormatter.ofPattern("HH:mm");

    private static final String TAT_CA = "Tất cả";

    // Loại ca (hiển thị có dấu, DB lưu không dấu: 'Ca sang', 'Ca chieu', 'Hanh
    // chinh').
    private static final String CA_SANG = "Ca sáng";
    private static final String CA_CHIEU = "Ca chiều";
    private static final String HANH_CHINH = "Hành chính";
    private static final String[] LOAI_CA = { CA_SANG, CA_CHIEU, HANH_CHINH };

    // Nhân viên được hiển thị có dấu; trạng thái phòng trong entity dùng giá trị
    // chuẩn của schema.
    private static final String PHONG_BAO_TRI = "Bao tri";
    private static final String NV_DANG_LAM = "Đang làm việc";

    private static final String[] THU = { "Thứ hai", "Thứ ba", "Thứ tư", "Thứ năm", "Thứ sáu", "Thứ bảy",
            "Chủ nhật" };

    private final CaTrucDAO caTrucDAO = new CaTrucDAO();
    private final List<CaTruc> dsCa = new ArrayList<>();
    private final List<NhanVien> dsNhanVien = new ArrayList<>();
    private final List<PhongKham> dsPhong = new ArrayList<>();
    // Các ca đang hiển thị trong bảng (cùng thứ tự với dòng của bảng).
    private final List<CaTruc> dangHienThi = new ArrayList<>();
    private LocalDate dauTuan = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    // true khi đang nạp dữ liệu bằng code, để bỏ qua sự kiện của người dùng.
    private boolean dangNap = false;

    // ----- Thẻ thống kê -----
    private final JLabel[] summaryValues = new JLabel[3];

    // ----- Bộ lọc và bảng -----
    private final JLabel lblTuan = new JLabel(" ");
    private final JComboBox<String> cboLocNhanVien = new JComboBox<>();
    private final JComboBox<String> cboLocPhong = new JComboBox<>();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[] { "Mã ca", "Ngày", "Thứ", "Bắt đầu", "Kết thúc", "Loại ca", "Nhân viên", "Chuyên môn",
                    "Phòng" },
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    // ----- Form phân công -----
    private final JLabel lblMaCa = new JLabel("(tự sinh khi lưu)");
    private final JSpinner spnNgay = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
    private final JComboBox<String> cboLoai = new JComboBox<>(LOAI_CA);
    private final JSpinner spnBatDau = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE));
    private final JSpinner spnKetThuc = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE));
    private final JComboBox<String> cboNhanVien = new JComboBox<>();
    private final JComboBox<String> cboPhong = new JComboBox<>();
    private final JLabel lblGoiY = new JLabel(" ");
    private final JButton btnThem = createButton("Thêm ca", true);
    private final JButton btnCapNhat = createButton("Cập nhật ca", false);
    private final JButton btnXoa = createButton("Xóa ca", false);

    public XuLyBangPhanCongCaTrucYTaPanel() {
        setLayout(new BorderLayout(0, 18));
        setOpaque(false);

        try {
            napCombo();
        } catch (SQLException ex) {
            showDatabaseError("Không thể tải danh sách nhân viên và phòng khám.", ex);
        }

        spnNgay.setEditor(new JSpinner.DateEditor(spnNgay, "dd/MM/yyyy"));
        spnBatDau.setEditor(new JSpinner.DateEditor(spnBatDau, "HH:mm"));
        spnKetThuc.setEditor(new JSpinner.DateEditor(spnKetThuc, "HH:mm"));

        add(createSummaryPanel(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(18, 0));
        center.setOpaque(false);
        center.add(createTableCard(), BorderLayout.CENTER);
        center.add(createFormCard(), BorderLayout.EAST);
        add(center, BorderLayout.CENTER);

        lamMoiForm();
        lamMoiBang();
    }

    // =====================================================================
    // DỮ LIỆU BAN ĐẦU
    // =====================================================================

    // Nạp các combo (phải làm trước khi gắn listener).
    private void napCombo() throws SQLException {
        dsNhanVien.clear();
        dsNhanVien.addAll(caTrucDAO.layTatCaNhanVien());
        dsPhong.clear();
        dsPhong.addAll(caTrucDAO.layTatCaPhong());

        cboLocNhanVien.addItem(TAT_CA);
        for (NhanVien nv : dsNhanVien) {
            cboLocNhanVien.addItem(nv.getMaNhanVien() + " - " + nv.getHoTen());
            // Chỉ nhân viên đang làm việc mới được phân ca mới.
            if (NV_DANG_LAM.equals(nv.getTrangThaiLamViec())) {
                cboNhanVien.addItem(nv.getMaNhanVien() + " - " + nv.getHoTen() + " ("
                        + (nv.getChuyenMon() == null ? "" : nv.getChuyenMon()) + ")");
            }
        }
        cboLocPhong.addItem(TAT_CA);
        for (PhongKham phong : dsPhong) {
            cboLocPhong.addItem(phong.getMaPhong() + " - " + phong.getTenPhong());
            cboPhong.addItem(phong.getMaPhong() + " - " + phong.getTenPhong() + " ("
                    + trangThaiPhongHienThi(phong.getTrangThai()) + ")");
        }
    }

    // =====================================================================
    // GIAO DIỆN
    // =====================================================================

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        panel.setOpaque(false);
        panel.add(createSummaryCard(0, "CA TRỰC HÔM NAY", TEAL));
        panel.add(createSummaryCard(1, "CA TRỰC TRONG TUẦN", BLUE));
        panel.add(createSummaryCard(2, "NHÂN VIÊN CÓ CA TRONG TUẦN", ORANGE));
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

    // Bảng ca trực bên trái, kèm điều hướng tuần và bộ lọc.
    private JPanel createTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(16, 18, 16, 18)));

        // Hàng 1: tiêu đề + điều hướng tuần.
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Bảng ca trực");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setForeground(INK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTuan.setFont(new Font("Arial", Font.PLAIN, 11));
        lblTuan.setForeground(MUTED);
        lblTuan.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(3));
        titleBox.add(lblTuan);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        nav.setOpaque(false);
        JButton btnTruoc = createButton("< Tuần trước", false);
        JButton btnNay = createButton("Tuần này", false);
        JButton btnSau = createButton("Tuần sau >", false);
        btnTruoc.addActionListener(event -> doiTuan(-1));
        btnNay.addActionListener(event -> {
            dauTuan = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            lamMoiBang();
        });
        btnSau.addActionListener(event -> doiTuan(1));
        nav.add(btnTruoc);
        nav.add(btnNay);
        nav.add(btnSau);
        header.add(titleBox, BorderLayout.WEST);
        header.add(nav, BorderLayout.EAST);

        // Hàng 2: bộ lọc.
        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filter.setOpaque(false);
        cboLocNhanVien.setFont(new Font("Arial", Font.PLAIN, 12));
        cboLocPhong.setFont(new Font("Arial", Font.PLAIN, 12));
        cboLocNhanVien.addActionListener(event -> lamMoiBang());
        cboLocPhong.addActionListener(event -> lamMoiBang());
        filter.add(createSmallLabel("Nhân viên:"));
        filter.add(cboLocNhanVien);
        filter.add(createSmallLabel("Phòng:"));
        filter.add(cboLocPhong);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        filter.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(header);
        top.add(Box.createVerticalStrut(10));
        top.add(filter);
        card.add(top, BorderLayout.NORTH);

        styleTable(table);
        table.setDefaultRenderer(Object.class, new CaRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(85);
        table.getColumnModel().getColumn(2).setPreferredWidth(70);
        table.getColumnModel().getColumn(3).setPreferredWidth(55);
        table.getColumnModel().getColumn(4).setPreferredWidth(55);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(130);
        table.getColumnModel().getColumn(7).setPreferredWidth(130);
        table.getColumnModel().getColumn(8).setPreferredWidth(50);
        table.getSelectionModel().addListSelectionListener(event -> {
            if (event.getValueIsAdjusting() || dangNap) {
                return;
            }
            CaTruc ca = layCaDangChon();
            if (ca != null) {
                hienThiForm(ca);
            }
            capNhatNut();
        });
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        JLabel hint = new JLabel("Ca của ngày đã qua hiển thị màu xám và không sửa được.");
        hint.setFont(new Font("Arial", Font.PLAIN, 11));
        hint.setForeground(MUTED);
        card.add(hint, BorderLayout.SOUTH);
        return card;
    }

    // Form phân công bên phải.
    private JPanel createFormCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(16, 18, 16, 18)));
        card.setPreferredSize(new Dimension(390, 0));

        JLabel title = new JLabel("Phân công ca trực");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setForeground(INK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(12));

        lblMaCa.setFont(new Font("Arial", Font.BOLD, 12));
        lblMaCa.setForeground(INK);
        card.add(createRow("Mã ca", lblMaCa));
        card.add(createRow("Ngày *", spnNgay));
        card.add(createRow("Loại ca", cboLoai));
        card.add(createRow("Giờ bắt đầu *", spnBatDau));
        card.add(createRow("Giờ kết thúc *", spnKetThuc));
        card.add(createRow("Nhân viên *", cboNhanVien));
        card.add(createRow("Phòng *", cboPhong));

        lblGoiY.setFont(new Font("Arial", Font.ITALIC, 11));
        lblGoiY.setForeground(MUTED);
        lblGoiY.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblGoiY);
        card.add(Box.createVerticalStrut(14));

        // Chọn loại ca thì tự điền giờ chuẩn (vẫn chỉnh tay được).
        cboLoai.addActionListener(event -> {
            if (!dangNap) {
                tuDienGioTheoLoai();
            }
        });
        btnThem.addActionListener(event -> themCa());
        btnCapNhat.addActionListener(event -> capNhatCa());
        btnXoa.addActionListener(event -> xoaCa());
        JButton btnLamMoi = createButton("Làm mới", false);
        btnLamMoi.addActionListener(event -> {
            table.clearSelection();
            lamMoiForm();
            capNhatNut();
        });

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        row1.setOpaque(false);
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);
        row1.add(btnLamMoi);
        row1.add(btnThem);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        row2.setOpaque(false);
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.add(btnXoa);
        row2.add(btnCapNhat);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        card.add(row1);
        card.add(Box.createVerticalStrut(8));
        card.add(row2);
        card.add(Box.createVerticalGlue());

        capNhatNut();
        return card;
    }

    // =====================================================================
    // XỬ LÝ SỰ KIỆN
    // =====================================================================

    private void doiTuan(int soTuan) {
        dauTuan = dauTuan.plusWeeks(soTuan);
        lamMoiBang();
    }

    /** Thêm ca mới sau khi kiểm tra dữ liệu. */
    private void themCa() {
        CaTruc moi = docForm();
        if (moi == null) {
            return;
        }
        try {
            String loi = kiemTra(moi, null);
            if (loi != null) {
                showWarning(loi);
                return;
            }
            caTrucDAO.them(moi);
        } catch (SQLException ex) {
            showDatabaseError("Không thể thêm ca trực.", ex);
            return;
        }

        // Chuyển sang tuần chứa ca vừa thêm để người dùng thấy ngay.
        dauTuan = moi.getNgay().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        lamMoiBang(moi.getMaCa());
        showMessage("Đã thêm ca " + moi.getMaCa() + " cho " + tenNhanVien(moi.getMaNhanVien()) + " ngày "
                + moi.getNgay().format(NGAY) + ".");
    }

    /** Cập nhật ca đang chọn. */
    private void capNhatCa() {
        CaTruc ca = layCaDangChon();
        if (ca == null) {
            showMessage("Vui lòng chọn một ca trong bảng.");
            return;
        }
        if (ca.getNgay().isBefore(LocalDate.now())) {
            showWarning("Ca của ngày đã qua không được sửa.");
            return;
        }
        CaTruc moi = docForm();
        if (moi == null) {
            return;
        }
        try {
            String loi = kiemTra(moi, ca);
            if (loi != null) {
                showWarning(loi);
                return;
            }
            moi.setMaCa(ca.getMaCa());
            if (!caTrucDAO.capNhat(moi)) {
                showWarning("Không tìm thấy ca trực trong cơ sở dữ liệu để cập nhật.");
                return;
            }
        } catch (SQLException ex) {
            showDatabaseError("Không thể cập nhật ca trực.", ex);
            return;
        }

        dauTuan = moi.getNgay().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        lamMoiBang(ca.getMaCa());
        showMessage("Đã cập nhật ca " + ca.getMaCa() + ".");
    }

    /** Xóa ca đang chọn (chỉ ca chưa qua). */
    private void xoaCa() {
        CaTruc ca = layCaDangChon();
        if (ca == null) {
            showMessage("Vui lòng chọn một ca trong bảng.");
            return;
        }
        if (ca.getNgay().isBefore(LocalDate.now())) {
            showWarning("Ca của ngày đã qua không được xóa.");
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Xóa ca " + ca.getMaCa() + " của " + tenNhanVien(ca.getMaNhanVien()) + " ngày "
                        + ca.getNgay().format(NGAY) + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            if (!caTrucDAO.xoa(ca.getMaCa())) {
                showWarning("Không tìm thấy ca trực trong cơ sở dữ liệu để xóa.");
                return;
            }
        } catch (SQLException ex) {
            showDatabaseError("Không thể xóa ca trực.", ex);
            return;
        }
        lamMoiBang();
        lamMoiForm();
    }

    // =====================================================================
    // KIỂM TRA DỮ LIỆU
    // =====================================================================

    /**
     * Trả về thông báo lỗi, hoặc null nếu hợp lệ. boQua là ca đang sửa (không tính
     * trùng với chính nó).
     */
    private String kiemTra(CaTruc moi, CaTruc boQua) throws SQLException {
        if (moi.getMaNhanVien() == null) {
            return "Vui lòng chọn nhân viên.";
        }
        if (moi.getMaPhong() == null) {
            return "Vui lòng chọn phòng.";
        }
        if (moi.getNgay().isBefore(LocalDate.now())) {
            return "Không thể phân ca cho ngày đã qua.";
        }
        if (!moi.getGioKetThuc().isAfter(moi.getGioBatDau())) {
            return "Giờ kết thúc phải sau giờ bắt đầu.";
        }
        dsPhong.clear();
        dsPhong.addAll(caTrucDAO.layTatCaPhong());
        String trangThaiPhong = trangThaiPhong(moi.getMaPhong());
        if (PHONG_BAO_TRI.equals(trangThaiPhong)) {
            return "Phòng " + moi.getMaPhong() + " đang bảo trì, không thể phân ca vào phòng này.";
        }
        if (caTrucDAO.coCaTrung(moi, boQua == null ? null : boQua.getMaCa())) {
            return tenNhanVien(moi.getMaNhanVien())
                    + " đã có ca trùng giờ trong ngày này.";
        }
        return null;
    }

    // =====================================================================
    // ĐỒNG BỘ GIAO DIỆN
    // =====================================================================

    private void lamMoiBang() {
        lamMoiBang(null);
    }

    // Nạp lại bảng theo tuần và bộ lọc, chọn lại ca có mã maCanChon (nếu có).
    private void lamMoiBang(String maCanChon) {
        String maNhanVienLoc = layMaTuCombo(cboLocNhanVien);
        String maPhongLoc = layMaTuCombo(cboLocPhong);
        LocalDate cuoiTuan = dauTuan.plusDays(6);

        try {
            List<CaTruc> caTrucMoi = caTrucDAO.layTatCa();
            dsCa.clear();
            dsCa.addAll(caTrucMoi);
        } catch (SQLException ex) {
            showDatabaseError("Không thể tải danh sách ca trực.", ex);
            return;
        }

        dangNap = true;
        tableModel.setRowCount(0);
        dangHienThi.clear();

        List<CaTruc> trongTuan = new ArrayList<>();
        for (CaTruc ca : dsCa) {
            if (!ca.getNgay().isBefore(dauTuan) && !ca.getNgay().isAfter(cuoiTuan)) {
                trongTuan.add(ca);
            }
        }
        for (CaTruc ca : trongTuan) {
            if (maNhanVienLoc != null && !maNhanVienLoc.equals(ca.getMaNhanVien())) {
                continue;
            }
            if (maPhongLoc != null && !maPhongLoc.equals(ca.getMaPhong())) {
                continue;
            }
            dangHienThi.add(ca);
            NhanVien nv = timNhanVien(ca.getMaNhanVien());
            tableModel.addRow(new Object[] {
                    ca.getMaCa(),
                    ca.getNgay().format(NGAY),
                    THU[ca.getNgay().getDayOfWeek().getValue() - 1],
                    ca.getGioBatDau().format(GIO),
                    ca.getGioKetThuc().format(GIO),
                    ca.getLoaiCa(),
                    nv == null ? ca.getMaNhanVien() : nv.getHoTen(),
                    nv == null || nv.getChuyenMon() == null ? "" : nv.getChuyenMon(),
                    ca.getMaPhong() });
        }

        lblTuan.setText("Tuần " + dauTuan.format(NGAY) + " - " + cuoiTuan.format(NGAY) + "  ·  "
                + dangHienThi.size() + " ca");

        // Thẻ thống kê tính trên cả tuần, không phụ thuộc bộ lọc.
        int homNay = 0;
        Set<String> nhanVienCoCa = new HashSet<>();
        for (CaTruc ca : trongTuan) {
            nhanVienCoCa.add(ca.getMaNhanVien());
        }
        for (CaTruc ca : dsCa) {
            if (ca.getNgay().equals(LocalDate.now())) {
                homNay++;
            }
        }
        summaryValues[0].setText(String.valueOf(homNay));
        summaryValues[1].setText(String.valueOf(trongTuan.size()));
        summaryValues[2].setText(String.valueOf(nhanVienCoCa.size()));

        if (maCanChon != null) {
            for (int i = 0; i < dangHienThi.size(); i++) {
                if (dangHienThi.get(i).getMaCa().equals(maCanChon)) {
                    table.setRowSelectionInterval(i, i);
                    break;
                }
            }
        }
        dangNap = false;

        CaTruc dangChon = layCaDangChon();
        if (dangChon != null) {
            hienThiForm(dangChon);
        } else if (maCanChon == null) {
            table.clearSelection();
        }
        capNhatNut();
    }

    // Bật/tắt các nút theo việc có đang chọn ca và ca đó đã qua hay chưa.
    private void capNhatNut() {
        CaTruc ca = layCaDangChon();
        boolean choPhepSua = ca != null && !ca.getNgay().isBefore(LocalDate.now());
        btnCapNhat.setEnabled(choPhepSua);
        btnXoa.setEnabled(choPhepSua);
        btnThem.setEnabled(ca == null);
        if (ca == null) {
            lblGoiY.setText("Điền thông tin rồi bấm \"Thêm ca\". Chọn một dòng trong bảng để sửa hoặc xóa.");
        } else if (!choPhepSua) {
            lblGoiY.setText("Ca " + ca.getMaCa() + " đã qua nên chỉ xem. Bấm \"Làm mới\" để thêm ca mới.");
        } else {
            lblGoiY.setText("Đang chọn ca " + ca.getMaCa() + ". Bấm \"Làm mới\" nếu muốn thêm ca mới.");
        }
    }

    // Đổ ca đang chọn lên form.
    private void hienThiForm(CaTruc ca) {
        dangNap = true;
        lblMaCa.setText(ca.getMaCa());
        datNgay(ca.getNgay());
        cboLoai.setSelectedItem(ca.getLoaiCa());
        datGio(spnBatDau, ca.getGioBatDau());
        datGio(spnKetThuc, ca.getGioKetThuc());
        chonTheoMa(cboNhanVien, ca.getMaNhanVien());
        chonTheoMa(cboPhong, ca.getMaPhong());
        dangNap = false;
    }

    // Đưa form về trạng thái thêm ca mới.
    private void lamMoiForm() {
        dangNap = true;
        lblMaCa.setText("(tự sinh khi lưu)");
        datNgay(LocalDate.now());
        cboLoai.setSelectedItem(CA_SANG);
        datGio(spnBatDau, LocalTime.of(8, 0));
        datGio(spnKetThuc, LocalTime.of(12, 0));
        if (cboNhanVien.getItemCount() > 0) {
            cboNhanVien.setSelectedIndex(0);
        }
        // Chọn sẵn phòng đầu tiên không bảo trì.
        for (int i = 0; i < dsPhong.size(); i++) {
            if (!PHONG_BAO_TRI.equals(dsPhong.get(i).getTrangThai())) {
                cboPhong.setSelectedIndex(i);
                break;
            }
        }
        dangNap = false;
    }

    private void tuDienGioTheoLoai() {
        String loai = (String) cboLoai.getSelectedItem();
        if (CA_SANG.equals(loai)) {
            datGio(spnBatDau, LocalTime.of(8, 0));
            datGio(spnKetThuc, LocalTime.of(12, 0));
        } else if (CA_CHIEU.equals(loai)) {
            datGio(spnBatDau, LocalTime.of(13, 0));
            datGio(spnKetThuc, LocalTime.of(17, 0));
        } else if (HANH_CHINH.equals(loai)) {
            datGio(spnBatDau, LocalTime.of(8, 0));
            datGio(spnKetThuc, LocalTime.of(17, 0));
        }
    }

    // Đọc dữ liệu form thành một CaTruc (chưa có mã).
    private CaTruc docForm() {
        String maNhanVien = layMaTuCombo(cboNhanVien);
        if (maNhanVien == null) {
            showWarning("Vui lòng chọn nhân viên.");
            return null;
        }
        String maPhong = layMaTuCombo(cboPhong);
        if (maPhong == null) {
            showWarning("Vui lòng chọn phòng.");
            return null;
        }
        LocalTime batDau = layGio(spnBatDau);
        LocalTime ketThuc = layGio(spnKetThuc);
        if (!ketThuc.isAfter(batDau)) {
            showWarning("Giờ kết thúc phải sau giờ bắt đầu.");
            return null;
        }
        return new CaTruc("", layNgay(), batDau, ketThuc, (String) cboLoai.getSelectedItem(), maNhanVien, maPhong);
    }

    // =====================================================================
    // HÀM HỖ TRỢ
    // =====================================================================

    private CaTruc layCaDangChon() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= dangHienThi.size()) {
            return null;
        }
        return dangHienThi.get(row);
    }

    // Lấy mã ở đầu chuỗi "MA - tên ..." của combo; null nếu là "Tất cả" hoặc chưa
    // chọn.
    private String layMaTuCombo(JComboBox<String> combo) {
        String chon = (String) combo.getSelectedItem();
        if (chon == null || TAT_CA.equals(chon)) {
            return null;
        }
        return chon.split(" - ")[0];
    }

    private void chonTheoMa(JComboBox<String> combo, String ma) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).startsWith(ma + " - ")) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        combo.setSelectedIndex(-1);
    }

    private NhanVien timNhanVien(String maNhanVien) {
        for (NhanVien nv : dsNhanVien) {
            if (nv.getMaNhanVien().equals(maNhanVien)) {
                return nv;
            }
        }
        return null;
    }

    private String tenNhanVien(String maNhanVien) {
        NhanVien nv = timNhanVien(maNhanVien);
        return nv == null ? maNhanVien : nv.getHoTen();
    }

    private String trangThaiPhong(String maPhong) {
        for (PhongKham phong : dsPhong) {
            if (phong.getMaPhong().equals(maPhong)) {
                return phong.getTrangThai();
            }
        }
        return "";
    }

    private String trangThaiPhongHienThi(String trangThai) {
        if ("San sang".equals(trangThai)) {
            return "Sẵn sàng";
        }
        if ("Dang kham".equals(trangThai)) {
            return "Đang khám";
        }
        if ("Can ve sinh".equals(trangThai)) {
            return "Cần vệ sinh";
        }
        if (PHONG_BAO_TRI.equals(trangThai)) {
            return "Bảo trì";
        }
        return trangThai;
    }

    private void showDatabaseError(String message, SQLException ex) {
        String detail = ex.getMessage();
        JOptionPane.showMessageDialog(this,
                detail == null || detail.trim().isEmpty() ? message : message + "\n" + detail,
                "Lỗi cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
    }

    private LocalDate layNgay() {
        return ((Date) spnNgay.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private LocalTime layGio(JSpinner spinner) {
        return ((Date) spinner.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
                .withSecond(0).withNano(0);
    }

    private void datNgay(LocalDate ngay) {
        spnNgay.setValue(Date.from(ngay.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    private void datGio(JSpinner spinner, LocalTime gio) {
        spinner.setValue(Date.from(LocalDate.now().atTime(gio).atZone(ZoneId.systemDefault()).toInstant()));
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
    }

    // ----- Tạo thành phần giao diện -----

    private JLabel createSmallLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 11));
        label.setForeground(MUTED);
        return label;
    }

    /**
     * Một hàng "nhãn + ô nhập": nhãn rộng cố định bên trái, ô nhập giãn hết phần
     * còn lại.
     */
    private JPanel createRow(String labelText, Component field) {
        if (!(field instanceof JLabel)) {
            field.setFont(new Font("Arial", Font.PLAIN, 12));
        }
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setForeground(INK);
        label.setPreferredSize(new Dimension(110, 32));

        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(new EmptyBorder(0, 0, 8, 0));
        row.add(label, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        return row;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(32);
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

    // Tô xám các ca của ngày đã qua, ca hôm nay in đậm.
    private final class CaRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            label.setBorder(new EmptyBorder(0, 6, 0, 6));
            CaTruc ca = row < dangHienThi.size() ? dangHienThi.get(row) : null;
            boolean daQua = ca != null && ca.getNgay().isBefore(LocalDate.now());
            boolean homNay = ca != null && ca.getNgay().equals(LocalDate.now());
            label.setForeground(daQua ? MUTED : INK);
            label.setFont(new Font("Arial", homNay ? Font.BOLD : Font.PLAIN, 12));
            return label;
        }
    }
}
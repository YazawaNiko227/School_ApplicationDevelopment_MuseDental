package gui;

import entity.DotThanhToan;
import entity.HoSoCongNo;
import service.CongNoTraGopService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
 * Panel "Công nợ và trả góp theo đợt" (bước 9 trong luồng khám bệnh chính,
 * nối tiếp "Lập hóa đơn" khi bệnh nhân chọn trả góp).
 *
 * Luồng xử lý:
 * 1. Danh sách hồ sơ trả góp (bảng CongNoTraGop) bên trái, có lọc theo trạng
 * thái và tìm theo mã / tên bệnh nhân / mã hóa đơn.
 * 2. Chọn một hồ sơ để xem các đợt thanh toán (bảng DotThanhToan) bên phải.
 * 3. "Thu tiền đợt": thu đợt chưa thanh toán sớm nhất -> đợt = 'Da thanh toan',
 * ghi NgayThanhToanThucTe, giảm SoDotConLai. Thu hết các đợt thì
 * HoaDon chuyển sang 'Da thanh toan'.
 * 4. "Nhắc nợ": soạn nội dung nhắc bệnh nhân về đợt chưa thanh toán gần nhất.
 *
 * Đợt chưa thanh toán mà quá ngày đến hạn được hiển thị là "Quá hạn" mà không
 * cần sửa dữ liệu trạng thái trong CSDL.
 *
 * Nghiệp vụ công nợ được xử lý trong service; DAO phụ trách truy cập CSDL.
 */
public class CapNhatCongNoTraGopPanel extends JPanel {
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color LINE = new Color(226, 234, 237);
    private static final Color ORANGE = new Color(177, 125, 47);
    private static final Color RED = new Color(200, 70, 70);
    private static final Color BLUE = new Color(51, 91, 177);

    private static final DecimalFormat TIEN = new DecimalFormat("#,##0");
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String DOT_DA_THANH_TOAN = CongNoTraGopService.DOT_DA_THANH_TOAN;
    private static final String DOT_QUA_HAN = CongNoTraGopService.DOT_QUA_HAN;
    private static final String DANG_TRA_GOP = CongNoTraGopService.DANG_TRA_GOP;
    private static final String QUA_HAN = CongNoTraGopService.QUA_HAN;
    private static final String HOAN_TAT = CongNoTraGopService.HOAN_TAT;
    private static final String TAT_CA = CongNoTraGopService.TAT_CA;
    private static final int NGAY_SAP_DEN_HAN = CongNoTraGopService.NGAY_SAP_DEN_HAN;

    private final CongNoTraGopService congNoService = new CongNoTraGopService();
    private final List<HoSoCongNo> dsCongNo = new ArrayList<>();
    private boolean dangNapBang = false;

    // ----- Thẻ thống kê -----
    private final JLabel lblDangTraGop = new JLabel("0");
    private final JLabel lblQuaHan = new JLabel("0");
    private final JLabel lblSapDenHan = new JLabel("0");
    private final JLabel lblConPhaiThu = new JLabel("0 đ");

    // ----- Danh sách công nợ -----
    private final JTextField txtTim = new JTextField();
    private final JComboBox<String> cboLoc = new JComboBox<>(
            new String[] { TAT_CA, DANG_TRA_GOP, QUA_HAN, HOAN_TAT });
    private final DefaultTableModel congNoModel = new DefaultTableModel(
            new String[] { "Mã CN", "Hóa đơn", "Bệnh nhân", "Tổng tiền (đ)", "Đã thu (đ)", "Còn lại (đ)",
                    "Đợt còn", "Trạng thái" },
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable congNoTable = new JTable(congNoModel);

    // ----- Chi tiết các đợt -----
    private final JLabel lblBenhNhan = new JLabel("Chưa chọn hồ sơ trả góp");
    private final JLabel lblHoaDon = new JLabel(" ");
    private final JLabel lblTienDo = new JLabel(" ");
    private final DefaultTableModel dotModel = new DefaultTableModel(
            new String[] { "Mã đợt", "Đợt", "Ngày đến hạn", "Số tiền (đ)", "Trạng thái", "Ngày thu" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable dotTable = new JTable(dotModel);
    private final JButton btnNhacNo = createButton("Nhắc nợ", false);
    private final JButton btnThuTien = createButton("Thu tiền đợt", true);

    public CapNhatCongNoTraGopPanel() {
        setLayout(new BorderLayout(0, 18));
        setOpaque(false);

        add(createSummaryPanel(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(18, 0));
        center.setOpaque(false);
        center.add(createDanhSachCard(), BorderLayout.CENTER);
        center.add(createChiTietCard(), BorderLayout.EAST);
        add(center, BorderLayout.CENTER);

        lamMoi(null);
    }

    // =====================================================================
    // GIAO DIỆN
    // =====================================================================

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        panel.setOpaque(false);
        panel.add(createSummaryCard("HỒ SƠ ĐANG TRẢ GÓP", lblDangTraGop, BLUE, 180));
        panel.add(createSummaryCard("ĐỢT QUÁ HẠN", lblQuaHan, RED, 180));
        panel.add(createSummaryCard("SẮP ĐẾN HẠN (" + NGAY_SAP_DEN_HAN + " NGÀY)", lblSapDenHan, ORANGE, 180));
        panel.add(createSummaryCard("CÒN PHẢI THU", lblConPhaiThu, TEAL, 230));
        return panel;
    }

    private JPanel createSummaryCard(String title, JLabel value, Color color, int width) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(12, 16, 12, 16)));
        card.setPreferredSize(new Dimension(width, 78));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 9));
        titleLabel.setForeground(MUTED);

        value.setFont(new Font("Arial", Font.BOLD, 24));
        value.setForeground(color);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(value);
        return card;
    }

    // Bên trái: danh sách hồ sơ trả góp.
    private JPanel createDanhSachCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(16, 18, 16, 18)));

        // Tiêu đề + bộ lọc, ô tìm kiếm.
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel title = new JLabel("Hồ sơ trả góp");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setForeground(INK);
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterPanel.setOpaque(false);
        JLabel filterLabel = new JLabel("Trạng thái:");
        filterLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        filterLabel.setForeground(MUTED);
        cboLoc.setFont(new Font("Arial", Font.PLAIN, 12));
        cboLoc.addActionListener(event -> lamMoi(layMaCongNoDangChon()));
        filterPanel.add(filterLabel);
        filterPanel.add(cboLoc);
        header.add(title, BorderLayout.WEST);
        header.add(filterPanel, BorderLayout.EAST);
        top.add(header);
        top.add(Box.createVerticalStrut(8));

        styleField(txtTim);
        txtTim.setToolTipText("Nhập mã công nợ, mã hóa đơn hoặc tên bệnh nhân");
        txtTim.addActionListener(event -> lamMoi(null));
        JButton btnTim = createButton("Tìm", false);
        btnTim.addActionListener(event -> lamMoi(null));
        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchRow.add(txtTim, BorderLayout.CENTER);
        searchRow.add(btnTim, BorderLayout.EAST);
        searchRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        top.add(searchRow);
        card.add(top, BorderLayout.NORTH);

        styleTable(congNoTable);
        congNoTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        congNoTable.getColumnModel().getColumn(1).setPreferredWidth(65);
        congNoTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        congNoTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        congNoTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        congNoTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        congNoTable.getColumnModel().getColumn(6).setPreferredWidth(55);
        congNoTable.getColumnModel().getColumn(7).setPreferredWidth(90);
        congNoTable.getColumnModel().getColumn(7).setCellRenderer(new StatusRenderer());
        congNoTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && !dangNapBang) {
                hienThiChiTiet();
            }
        });
        JScrollPane scroll = new JScrollPane(congNoTable);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // Bên phải: các đợt của hồ sơ đang chọn.
    private JPanel createChiTietCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(16, 18, 16, 18)));
        card.setPreferredSize(new Dimension(500, 0));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Các đợt thanh toán");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setForeground(INK);
        lblBenhNhan.setFont(new Font("Arial", Font.BOLD, 12));
        lblBenhNhan.setForeground(INK);
        lblHoaDon.setFont(new Font("Arial", Font.PLAIN, 12));
        lblHoaDon.setForeground(MUTED);
        lblTienDo.setFont(new Font("Arial", Font.BOLD, 12));
        lblTienDo.setForeground(TEAL);
        for (JLabel label : new JLabel[] { title, lblBenhNhan, lblHoaDon, lblTienDo }) {
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        top.add(title);
        top.add(Box.createVerticalStrut(10));
        top.add(lblBenhNhan);
        top.add(Box.createVerticalStrut(4));
        top.add(lblHoaDon);
        top.add(Box.createVerticalStrut(4));
        top.add(lblTienDo);
        card.add(top, BorderLayout.NORTH);

        styleTable(dotTable);
        dotTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        dotTable.getColumnModel().getColumn(1).setPreferredWidth(40);
        dotTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        dotTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        dotTable.getColumnModel().getColumn(4).setPreferredWidth(95);
        dotTable.getColumnModel().getColumn(5).setPreferredWidth(85);
        dotTable.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());
        JScrollPane scroll = new JScrollPane(dotTable);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        btnNhacNo.addActionListener(event -> nhacNo());
        btnThuTien.addActionListener(event -> thuTienDot());
        actions.add(btnNhacNo);
        actions.add(btnThuTien);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    // =====================================================================
    // XỬ LÝ SỰ KIỆN
    // =====================================================================

    /** Thu tiền đợt chưa thanh toán sớm nhất của hồ sơ đang chọn. */
    private void thuTienDot() {
        HoSoCongNo hoSo = layCongNoDangChon();
        if (hoSo == null) {
            showMessage("Vui lòng chọn một hồ sơ trả góp.");
            return;
        }
        int row = dotTable.getSelectedRow();
        if (row < 0) {
            showMessage("Vui lòng chọn đợt cần thu trong bảng bên phải.");
            return;
        }
        DotThanhToan dot = hoSo.dsDot().get(row);
        try {
            congNoService.thuTuDotCanThu(hoSo, dot.getMaDot());
        } catch (IllegalStateException exception) {
            showWarning(exception.getMessage());
            return;
        }

        String thongTinQuaHan = DOT_QUA_HAN.equals(congNoService.trangThai(dot, LocalDate.now()))
                ? "\n(Đợt này đã quá hạn.)" : "";
        int choice = JOptionPane.showConfirmDialog(this,
                "Thu đợt " + (row + 1) + " của " + hoSo.hoTen() + ": "
                        + TIEN.format(dot.getSoTienDot()) + " đ?"
                        + thongTinQuaHan,
                "Xác nhận thu tiền", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            CongNoTraGopService.KetQuaThanhToan ketQua =
                    congNoService.thanhToanDot(hoSo.maCongNo(), dot.getMaDot(), LocalDate.now());
            if (!ketQua.thanhCong()) {
                showWarning("Đợt này đã được thanh toán hoặc không còn tồn tại. Danh sách sẽ được tải lại.");
                lamMoi(hoSo.maCongNo());
                return;
            }
            lamMoi(ketQua.maCongNo());
            String thongBao = "Đã thu đợt " + ketQua.thuTuDot() + ": "
                    + TIEN.format(ketQua.soTienDot()) + " đ.";
            if (ketQua.soDotConLai() == 0) {
                thongBao += "\nHồ sơ " + ketQua.maCongNo() + " đã trả hết, hóa đơn "
                        + ketQua.maHoaDon() + " chuyển sang \"Đã thanh toán\".";
            } else {
                thongBao += "\nCòn " + ketQua.soDotConLai() + " đợt, "
                        + TIEN.format(ketQua.conLai()) + " đ.";
            }
            showMessage(thongBao);
        } catch (IllegalStateException exception) {
            showWarning(exception.getMessage());
            lamMoi(hoSo.maCongNo());
        } catch (java.sql.SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "Không thể ghi nhận thanh toán:\n" + exception.getMessage(),
                    "Lỗi cập nhật công nợ", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Soạn nội dung nhắc nợ cho đợt chưa thanh toán gần nhất. */
    private void nhacNo() {
        HoSoCongNo hoSo = layCongNoDangChon();
        if (hoSo == null) {
            showMessage("Vui lòng chọn một hồ sơ trả góp.");
            return;
        }
        try {
            String noiDung = congNoService.taoNoiDungNhacNo(hoSo, LocalDate.now());
            JOptionPane.showMessageDialog(this,
                    "Chưa tích hợp gửi SMS/Zalo; nội dung dưới đây chỉ để tham khảo, chưa được gửi.\n"
                            + "Liên hệ: " + hoSo.soDienThoai() + "\n\n" + noiDung,
                    "Nội dung nhắc nợ", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalStateException exception) {
            showMessage(exception.getMessage());
        }
    }

    // =====================================================================
    // HÀM HỖ TRỢ
    // =====================================================================

    // Nạp lại bảng công nợ theo bộ lọc, cập nhật thẻ thống kê, chọn lại dòng cũ.
    private void lamMoi(String maCanChon) {
        dangNapBang = true;
        String keyword = txtTim.getText().trim().toLowerCase();
        String loc = (String) cboLoc.getSelectedItem();

        dsCongNo.clear();
        try {
            dsCongNo.addAll(congNoService.layTatCa());
        } catch (java.sql.SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "Không thể tải dữ liệu công nợ từ CSDL:\n" + exception.getMessage(),
                    "Lỗi tải công nợ", JOptionPane.ERROR_MESSAGE);
        }
        congNoModel.setRowCount(0);
        for (HoSoCongNo hoSo : congNoService.locDanhSach(dsCongNo, keyword, loc, LocalDate.now())) {
            congNoModel.addRow(new Object[] {
                    hoSo.maCongNo(), hoSo.maHoaDon(), hoSo.hoTen(),
                    TIEN.format(hoSo.tongSoTien()), TIEN.format(congNoService.daThu(hoSo)),
                    TIEN.format(congNoService.conLai(hoSo)), congNoService.soDotConLai(hoSo),
                    congNoService.trangThai(hoSo, LocalDate.now()) });
        }

        capNhatThongKe();

        if (maCanChon != null) {
            for (int i = 0; i < congNoModel.getRowCount(); i++) {
                if (maCanChon.equals(congNoModel.getValueAt(i, 0))) {
                    congNoTable.setRowSelectionInterval(i, i);
                    break;
                }
            }
        }
        dangNapBang = false;
        hienThiChiTiet();
    }

    // Thống kê tính trên toàn bộ hồ sơ (không phụ thuộc bộ lọc).
    private void capNhatThongKe() {
        CongNoTraGopService.ThongKe thongKe = congNoService.thongKe(dsCongNo, LocalDate.now());
        lblDangTraGop.setText(String.valueOf(thongKe.dangTraGop()));
        lblQuaHan.setText(String.valueOf(thongKe.dotQuaHan()));
        lblSapDenHan.setText(String.valueOf(thongKe.sapDenHan()));
        lblConPhaiThu.setText(TIEN.format(thongKe.conPhaiThu()) + " đ");
    }

    // Đổ các đợt của hồ sơ đang chọn lên bảng bên phải.
    private void hienThiChiTiet() {
        HoSoCongNo hoSo = layCongNoDangChon();
        dotModel.setRowCount(0);
        if (hoSo == null) {
            lblBenhNhan.setText("Chưa chọn hồ sơ trả góp");
            lblHoaDon.setText("Chọn một hồ sơ ở danh sách bên trái để xem các đợt.");
            lblTienDo.setText(" ");
            btnNhacNo.setEnabled(false);
            btnThuTien.setEnabled(false);
            return;
        }
        lblBenhNhan.setText(hoSo.hoTen() + "  ·  " + hoSo.maBenhNhan() + "  ·  " + hoSo.soDienThoai());
        lblHoaDon.setText("Hồ sơ " + hoSo.maCongNo() + "  ·  Hóa đơn " + hoSo.maHoaDon()
                + "  ·  Tổng " + TIEN.format(hoSo.tongSoTien()) + " đ");
        lblTienDo.setText("Đã thu " + TIEN.format(congNoService.daThu(hoSo)) + " đ  ·  Còn lại "
                + TIEN.format(congNoService.conLai(hoSo)) + " đ ("
                + congNoService.soDotConLai(hoSo) + " đợt)");

        int stt = 1;
        for (DotThanhToan dot : hoSo.dsDot()) {
            dotModel.addRow(new Object[] {
                    dot.getMaDot(), stt++, dot.getNgayDenHan().format(NGAY),
                    TIEN.format(dot.getSoTienDot()),
                    congNoService.trangThai(dot, LocalDate.now()),
                    dot.getNgayThanhToanThucTe() == null
                            ? "-" : dot.getNgayThanhToanThucTe().format(NGAY) });
        }
        boolean conNo = congNoService.soDotConLai(hoSo) > 0;
        btnNhacNo.setEnabled(conNo);
        btnThuTien.setEnabled(conNo);
    }

    private String layMaCongNoDangChon() {
        int row = congNoTable.getSelectedRow();
        return row < 0 ? null : (String) congNoModel.getValueAt(row, 0);
    }

    private HoSoCongNo layCongNoDangChon() {
        String ma = layMaCongNoDangChon();
        if (ma == null) {
            return null;
        }
        for (HoSoCongNo hoSo : dsCongNo) {
            if (hoSo.maCongNo().equals(ma)) {
                return hoSo;
            }
        }
        return null;
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
    }

    // ----- Tạo thành phần giao diện -----

    private void styleField(JTextField field) {
        field.setFont(new Font("Arial", Font.PLAIN, 12));
        field.setForeground(INK);
        field.setPreferredSize(new Dimension(160, 32));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(0, 8, 0, 8)));
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

    // Tô màu chữ cho cột trạng thái (dùng cho cả hồ sơ và đợt).
    private static final class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 11));
            String text = String.valueOf(value);
            if (HOAN_TAT.equals(text) || DOT_DA_THANH_TOAN.equals(text)) {
                label.setForeground(TEAL);
            } else if (DANG_TRA_GOP.equals(text)) {
                label.setForeground(BLUE);
            } else if (QUA_HAN.equals(text) || DOT_QUA_HAN.equals(text)) {
                label.setForeground(RED);
            } else {
                label.setForeground(MUTED);
            }
            return label;
        }
    }
}
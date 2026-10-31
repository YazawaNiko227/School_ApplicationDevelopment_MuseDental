package gui;

import com.toedter.calendar.JDateChooser;
import util.GiaoDienUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Panel "Tra cứu hóa đơn / giao dịch" (menu TÌM KIẾM).
 *
 * Luồng xử lý:
 * 1. Nhập điều kiện lọc (từ khóa, khoảng ngày lập, phương thức, trạng thái)
 * rồi bấm "Tìm kiếm". Để trống / chọn "Tất cả" nghĩa là không lọc theo mục đó.
 * 2. Chọn một hóa đơn trong bảng, bấm "Xem chi tiết" (hoặc nhấp đúp) để xem
 * dịch vụ (ChiTietHoaDon), thuốc (ChiTietHoaDonThuoc) và các đợt thanh toán
 * (DotThanhToan, chỉ có với hóa đơn trả góp).
 *
 * Hiện tại dùng dữ liệu mẫu trong bộ nhớ (khớp dữ liệu mẫu trong SQL).
 * Chỗ cần nối DAO ghi "TODO DAO".
 */
public class TimKiemTraCuuHoaDonPanel extends JPanel {
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color LINE = new Color(226, 234, 237);
    private static final Color ORANGE = new Color(177, 125, 47);
    private static final Color RED = new Color(200, 70, 70);
    private static final Color BLUE = new Color(51, 91, 177);

    private static final DecimalFormat TIEN = new DecimalFormat("#,##0");
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String TAT_CA = "Tất cả";

    // Phương thức thanh toán (DB lưu không dấu: 'Tien mat', 'Chuyen khoan', 'Tra
    // gop').
    private static final String TIEN_MAT = "Tiền mặt";
    private static final String CHUYEN_KHOAN = "Chuyển khoản";
    private static final String TRA_GOP = "Trả góp";

    // Trạng thái thanh toán (DB: 'Chua thanh toan', 'Da thanh toan', 'Dang tra
    // gop').
    private static final String CHUA_THANH_TOAN = "Chưa thanh toán";
    private static final String DA_THANH_TOAN = "Đã thanh toán";
    private static final String DANG_TRA_GOP = "Đang trả góp";

    /** Một hóa đơn kèm các dòng chi tiết (dịch vụ, thuốc, đợt trả góp). */
    private static final class HoaDon {
        final String ma;
        final LocalDate ngayLap;
        final String maBenhNhan;
        final String tenBenhNhan;
        final String maNhanVien;
        final String tenNhanVien;
        final String phuongThuc; // null nếu chưa thanh toán
        final String trangThai;
        // {maDichVu, tenDichVu, donGia (Double), soLuong (Integer)}
        final Object[][] dichVu;
        // {maVatTu, tenVatTu, donGia (Double), soLuong (Integer)}
        final Object[][] thuoc;
        // {maDot, ngayDenHan, soTien (Double), trangThai, ngayThu}
        final Object[][] dots;

        HoaDon(String ma, LocalDate ngayLap, String maBenhNhan, String tenBenhNhan, String maNhanVien,
                String tenNhanVien, String phuongThuc, String trangThai, Object[][] dichVu, Object[][] thuoc,
                Object[][] dots) {
            this.ma = ma;
            this.ngayLap = ngayLap;
            this.maBenhNhan = maBenhNhan;
            this.tenBenhNhan = tenBenhNhan;
            this.maNhanVien = maNhanVien;
            this.tenNhanVien = tenNhanVien;
            this.phuongThuc = phuongThuc;
            this.trangThai = trangThai;
            this.dichVu = dichVu;
            this.thuoc = thuoc;
            this.dots = dots;
        }

        double tienDichVu() {
            return tong(dichVu);
        }

        double tienThuoc() {
            return tong(thuoc);
        }

        double tongTien() {
            return tienDichVu() + tienThuoc();
        }

        private static double tong(Object[][] dong) {
            double tong = 0;
            for (Object[] d : dong) {
                tong += (Double) d[2] * (Integer) d[3];
            }
            return tong;
        }
    }

    // TODO DAO: HoaDonDAO.layTatCa() / HoaDonDAO.timKiem(...) thay cho dữ liệu mẫu
    // này.
    private final List<HoaDon> dsHoaDon = new ArrayList<>();
    // Các hóa đơn đang hiển thị trong bảng (cùng thứ tự với dòng bảng).
    private final List<HoaDon> dangHienThi = new ArrayList<>();

    // ----- Bộ lọc -----
    private final JTextField txtTuKhoa = new JTextField();
    private final JComboBox<String> cboPhuongThuc = new JComboBox<>(
            new String[] { TAT_CA, TIEN_MAT, CHUYEN_KHOAN, TRA_GOP });
    private final JComboBox<String> cboTrangThai = new JComboBox<>(
            new String[] { TAT_CA, CHUA_THANH_TOAN, DA_THANH_TOAN, DANG_TRA_GOP });
    private final JDateChooser dtTuNgay = new JDateChooser();
    private final JDateChooser dtDenNgay = new JDateChooser();

    // ----- Bảng kết quả -----
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[] { "Mã HĐ", "Ngày lập", "Mã BN", "Bệnh nhân", "Nhân viên lập", "Phương thức",
                    "Trạng thái", "Tổng tiền (đ)" },
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JLabel lblKetQua = new JLabel(" ");

    public TimKiemTraCuuHoaDonPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);

        taoDuLieuMau();

        add(createFilterCard(), BorderLayout.NORTH);
        add(createResultCard(), BorderLayout.CENTER);

        timKiem();
    }

    // =====================================================================
    // DỮ LIỆU MẪU (khớp dữ liệu mẫu trong SQL)
    // =====================================================================

    private void taoDuLieuMau() {
        dsHoaDon.add(new HoaDon("HD001", LocalDate.of(2026, 9, 1), "BN001", "Võ Thanh Hải",
                "NV004", "Phạm Quốc Dũng", TIEN_MAT, DA_THANH_TOAN,
                new Object[][] {
                        { "DV001", "Lấy cao răng", 200000.0, 1 },
                        { "DV002", "Trám răng composite", 300000.0, 1 } },
                new Object[][] {
                        { "VT001", "Amoxicillin 500mg", 5000.0, 15 },
                        { "VT002", "Paracetamol 500mg", 1500.0, 10 } },
                new Object[][] {}));

        dsHoaDon.add(new HoaDon("HD002", LocalDate.of(2026, 9, 5), "BN002", "Nguyễn Thị Lan",
                "NV004", "Phạm Quốc Dũng", CHUYEN_KHOAN, DA_THANH_TOAN,
                new Object[][] { { "DV006", "Tẩy trắng răng", 2000000.0, 1 } },
                new Object[][] {},
                new Object[][] {}));

        dsHoaDon.add(new HoaDon("HD003", LocalDate.of(2026, 9, 10), "BN002", "Nguyễn Thị Lan",
                "NV004", "Phạm Quốc Dũng", TRA_GOP, DANG_TRA_GOP,
                new Object[][] { { "DV004", "Bọc răng sứ", 3500000.0, 2 } },
                new Object[][] {},
                new Object[][] {
                        { "DOT001", "10/09/2026", 3000000.0, "Đã thanh toán", "10/09/2026" },
                        { "DOT002", "10/10/2026", 2000000.0, "Quá hạn", "-" },
                        { "DOT003", "10/11/2026", 2000000.0, "Chưa đến hạn", "-" } }));

        dsHoaDon.add(new HoaDon("HD004", LocalDate.of(2026, 10, 1), "BN004", "Trần Bảo Ngọc",
                "NV004", "Phạm Quốc Dũng", null, CHUA_THANH_TOAN,
                new Object[][] { { "DV002", "Trám răng composite", 300000.0, 1 } },
                new Object[][] {},
                new Object[][] {}));
    }

    // =====================================================================
    // GIAO DIỆN
    // =====================================================================

    private JPanel createFilterCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(16, 18, 14, 18)));

        JLabel title = new JLabel("Bộ lọc tra cứu hóa đơn");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(INK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(12));

        styleField(txtTuKhoa, 260);
        txtTuKhoa.setToolTipText("Nhập mã hóa đơn, mã bệnh nhân hoặc tên bệnh nhân");
        txtTuKhoa.addActionListener(event -> timKiem());
        styleCombo(cboPhuongThuc, 160);
        styleCombo(cboTrangThai, 160);
        styleDate(dtTuNgay);
        styleDate(dtDenNgay);

        // Hàng 1: từ khóa, phương thức, trạng thái.
        JPanel row1 = createFlowRow();
        row1.add(createLabeledField("Từ khóa (mã HĐ, mã BN, tên BN)", txtTuKhoa));
        row1.add(createLabeledField("Phương thức thanh toán", cboPhuongThuc));
        row1.add(createLabeledField("Trạng thái thanh toán", cboTrangThai));
        card.add(row1);
        card.add(Box.createVerticalStrut(10));

        // Hàng 2: khoảng ngày lập + nút.
        JPanel row2 = createFlowRow();
        row2.add(createLabeledField("Từ ngày lập", dtTuNgay));
        row2.add(createLabeledField("Đến ngày lập", dtDenNgay));
        JButton btnTim = createButton("Tìm kiếm", true);
        JButton btnDatLai = createButton("Đặt lại", false);
        btnTim.addActionListener(event -> timKiem());
        btnDatLai.addActionListener(event -> datLai());
        row2.add(createButtonSlot(btnTim, btnDatLai));
        card.add(row2);
        return card;
    }

    private JPanel createResultCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(16, 18, 16, 18)));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JLabel title = new JLabel("Danh sách hóa đơn / giao dịch");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setForeground(INK);
        lblKetQua.setFont(new Font("Arial", Font.PLAIN, 11));
        lblKetQua.setForeground(MUTED);
        heading.add(title, BorderLayout.WEST);
        heading.add(lblKetQua, BorderLayout.EAST);
        card.add(heading, BorderLayout.NORTH);

        styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(65);
        table.getColumnModel().getColumn(1).setPreferredWidth(85);
        table.getColumnModel().getColumn(2).setPreferredWidth(65);
        table.getColumnModel().getColumn(3).setPreferredWidth(140);
        table.getColumnModel().getColumn(4).setPreferredWidth(130);
        table.getColumnModel().getColumn(5).setPreferredWidth(95);
        table.getColumnModel().getColumn(6).setPreferredWidth(110);
        table.getColumnModel().getColumn(7).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusRenderer());
        DefaultTableCellRenderer phai = new DefaultTableCellRenderer();
        phai.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(7).setCellRenderer(phai);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    xemChiTiet();
                }
            }
        });
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton btnChiTiet = createButton("Xem chi tiết", true);
        btnChiTiet.addActionListener(event -> xemChiTiet());
        actions.add(btnChiTiet);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    // =====================================================================
    // XỬ LÝ SỰ KIỆN
    // =====================================================================

    /** Lọc danh sách hóa đơn theo các điều kiện (trống / "Tất cả" = bỏ qua). */
    private void timKiem() {
        LocalDate tuNgay = toLocalDate(dtTuNgay.getDate());
        LocalDate denNgay = toLocalDate(dtDenNgay.getDate());
        if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) {
            JOptionPane.showMessageDialog(this, "Ngày bắt đầu không được sau ngày kết thúc.",
                    "Khoảng ngày không hợp lệ", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tuKhoa = txtTuKhoa.getText().trim().toLowerCase();
        String phuongThuc = (String) cboPhuongThuc.getSelectedItem();
        String trangThai = (String) cboTrangThai.getSelectedItem();

        // TODO DAO: HoaDonDAO.timKiem(tuKhoa, phuongThuc, trangThai, tuNgay, denNgay)
        dangHienThi.clear();
        tableModel.setRowCount(0);
        for (HoaDon hd : dsHoaDon) {
            if (!tuKhoa.isEmpty()
                    && !hd.ma.toLowerCase().contains(tuKhoa)
                    && !hd.maBenhNhan.toLowerCase().contains(tuKhoa)
                    && !hd.tenBenhNhan.toLowerCase().contains(tuKhoa)) {
                continue;
            }
            if (!TAT_CA.equals(phuongThuc) && !phuongThuc.equals(hd.phuongThuc)) {
                continue;
            }
            if (!TAT_CA.equals(trangThai) && !trangThai.equals(hd.trangThai)) {
                continue;
            }
            if (tuNgay != null && hd.ngayLap.isBefore(tuNgay)) {
                continue;
            }
            if (denNgay != null && hd.ngayLap.isAfter(denNgay)) {
                continue;
            }
            dangHienThi.add(hd);
            tableModel.addRow(new Object[] {
                    hd.ma, hd.ngayLap.format(NGAY), hd.maBenhNhan, hd.tenBenhNhan,
                    hd.maNhanVien + " - " + hd.tenNhanVien,
                    hd.phuongThuc == null ? "-" : hd.phuongThuc,
                    hd.trangThai, TIEN.format(hd.tongTien()) });
        }
        lblKetQua.setText(dangHienThi.isEmpty()
                ? "Không tìm thấy hóa đơn phù hợp"
                : "Tìm thấy " + dangHienThi.size() + " hóa đơn");
    }

    private void datLai() {
        txtTuKhoa.setText("");
        cboPhuongThuc.setSelectedIndex(0);
        cboTrangThai.setSelectedIndex(0);
        dtTuNgay.setDate(null);
        dtDenNgay.setDate(null);
        timKiem();
    }

    /** Mở cửa sổ chi tiết của hóa đơn đang chọn. */
    private void xemChiTiet() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn trong danh sách.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        // TODO DAO: ChiTietHoaDonDAO / ChiTietHoaDonThuocDAO / DotThanhToanDAO theo
        // maHoaDon.
        HoaDon hd = dangHienThi.get(row);

        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Chi tiết hóa đơn " + hd.ma, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(820, 640);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Thông tin chung.
        JLabel lblTieuDe = new JLabel("Hóa đơn " + hd.ma + "  ·  Ngày lập " + hd.ngayLap.format(NGAY));
        lblTieuDe.setFont(new Font("Arial", Font.BOLD, 15));
        lblTieuDe.setForeground(INK);
        content.add(left(lblTieuDe));
        content.add(Box.createVerticalStrut(8));
        content.add(left(createInfoLabel("Bệnh nhân: " + hd.tenBenhNhan + " (" + hd.maBenhNhan + ")")));
        content.add(Box.createVerticalStrut(4));
        content.add(left(createInfoLabel("Nhân viên lập: " + hd.tenNhanVien + " (" + hd.maNhanVien + ")")));
        content.add(Box.createVerticalStrut(4));
        content.add(left(createInfoLabel("Phương thức: " + (hd.phuongThuc == null ? "chưa chọn" : hd.phuongThuc)
                + "  ·  Trạng thái: " + hd.trangThai)));
        content.add(Box.createVerticalStrut(14));

        // Dịch vụ.
        content.add(createBangChiTiet("Dịch vụ điều trị",
                new String[] { "Mã", "Tên dịch vụ", "Đơn giá (đ)", "SL", "Thành tiền (đ)" },
                dongTien(hd.dichVu)));
        content.add(Box.createVerticalStrut(12));

        // Thuốc.
        content.add(createBangChiTiet("Thuốc theo đơn",
                new String[] { "Mã", "Tên thuốc", "Đơn giá (đ)", "SL", "Thành tiền (đ)" },
                dongTien(hd.thuoc)));
        content.add(Box.createVerticalStrut(12));

        // Các đợt thanh toán (chỉ có với hóa đơn trả góp).
        if (hd.dots.length > 0) {
            List<Object[]> dong = new ArrayList<>();
            for (Object[] d : hd.dots) {
                dong.add(new Object[] { d[0], d[1], TIEN.format((Double) d[2]), d[3], d[4] });
            }
            content.add(createBangChiTiet("Các đợt thanh toán",
                    new String[] { "Mã đợt", "Ngày đến hạn", "Số tiền (đ)", "Trạng thái", "Ngày thu" }, dong));
            content.add(Box.createVerticalStrut(12));
        }

        // Tổng tiền.
        content.add(createMoneyRow("Cộng tiền dịch vụ", hd.tienDichVu(), 13, INK));
        content.add(Box.createVerticalStrut(4));
        content.add(createMoneyRow("Cộng tiền thuốc", hd.tienThuoc(), 13, INK));
        content.add(Box.createVerticalStrut(6));
        content.add(createMoneyRow("TỔNG GIÁ TRỊ HÓA ĐƠN", hd.tongTien(), 18, TEAL));

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        dialog.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(Color.WHITE);
        JButton btnDong = createButton("Đóng", false);
        btnDong.addActionListener(event -> dialog.dispose());
        bottom.add(btnDong);
        dialog.add(bottom, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    // =====================================================================
    // HÀM HỖ TRỢ
    // =====================================================================

    // Đổi các dòng {mã, tên, đơn giá, số lượng} thành dòng hiển thị có thành tiền.
    private List<Object[]> dongTien(Object[][] dong) {
        List<Object[]> kq = new ArrayList<>();
        for (Object[] d : dong) {
            double donGia = (Double) d[2];
            int soLuong = (Integer) d[3];
            kq.add(new Object[] { d[0], d[1], TIEN.format(donGia), soLuong, TIEN.format(donGia * soLuong) });
        }
        return kq;
    }

    private LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // Tiêu đề + bảng nhỏ trong cửa sổ chi tiết.
    private JPanel createBangChiTiet(String tieuDe, String[] cot, List<Object[]> dong) {
        DefaultTableModel model = new DefaultTableModel(cot, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (Object[] d : dong) {
            model.addRow(d);
        }
        JTable bang = new JTable(model);
        styleTable(bang);
        JScrollPane scroll = new JScrollPane(bang);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setPreferredSize(new Dimension(100, 40 + 30 * Math.max(1, Math.min(dong.size(), 3))));

        JLabel label = new JLabel(tieuDe);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setForeground(INK);

        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40 + 30 * Math.max(1, Math.min(dong.size(), 3)) + 30));
        return panel;
    }

    // Một hàng "nhãn ... số tiền" căn hai đầu.
    private JPanel createMoneyRow(String caption, double soTien, int size, Color color) {
        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(new Font("Arial", Font.BOLD, 11));
        captionLabel.setForeground(MUTED);
        JLabel value = new JLabel(TIEN.format(soTien) + " đ");
        value.setFont(new Font("Arial", Font.BOLD, size));
        value.setForeground(color);

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(captionLabel, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, size + 14));
        return row;
    }

    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setForeground(INK);
        return label;
    }

    private JLabel left(JLabel label) {
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    // ----- Tạo thành phần giao diện -----

    private JPanel createFlowRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        return row;
    }

    // Nhãn nhỏ phía trên + ô nhập.
    private JPanel createLabeledField(String labelText, JComponent field) {
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 10));
        label.setForeground(MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(label);
        wrapper.add(Box.createVerticalStrut(5));
        wrapper.add(field);
        return wrapper;
    }

    // Hai nút nằm ngang hàng với ô nhập (chừa chỗ cho dòng nhãn phía trên).
    private JPanel createButtonSlot(JButton primary, JButton secondary) {
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.add(Box.createVerticalStrut(19));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(primary);
        buttons.add(secondary);
        wrapper.add(buttons);
        return wrapper;
    }

    private void styleField(JTextField field, int width) {
        field.setFont(new Font("Arial", Font.PLAIN, 12));
        field.setForeground(INK);
        field.setPreferredSize(new Dimension(width, 36));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(0, 10, 0, 10)));
    }

    private void styleCombo(JComboBox<String> combo, int width) {
        combo.setFont(new Font("Arial", Font.PLAIN, 12));
        combo.setForeground(INK);
        combo.setBackground(Color.WHITE);
        combo.setPreferredSize(new Dimension(width, 36));
    }

    private void styleDate(JDateChooser chooser) {
        GiaoDienUtil.cauHinhChonNgay(chooser, "dd/MM/yyyy");
        chooser.setPreferredSize(new Dimension(160, 36));
    }

    private void styleTable(JTable bang) {
        bang.setRowHeight(30);
        bang.setFont(new Font("Arial", Font.PLAIN, 12));
        bang.setForeground(INK);
        bang.setShowVerticalLines(false);
        bang.setGridColor(LINE);
        bang.setFillsViewportHeight(true);
        bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bang.setSelectionBackground(new Color(231, 246, 245));
        bang.setSelectionForeground(INK);
        bang.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        bang.getTableHeader().setReorderingAllowed(false);
    }

    private JButton createButton(String text, boolean primary) {
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

    // Tô màu chữ cho cột trạng thái thanh toán.
    private static final class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 11));
            String text = String.valueOf(value);
            if (DA_THANH_TOAN.equals(text)) {
                label.setForeground(TEAL);
            } else if (DANG_TRA_GOP.equals(text)) {
                label.setForeground(BLUE);
            } else if (CHUA_THANH_TOAN.equals(text)) {
                label.setForeground(ORANGE);
            } else if ("Quá hạn".equals(text)) {
                label.setForeground(RED);
            } else {
                label.setForeground(INK);
            }
            return label;
        }
    }
}
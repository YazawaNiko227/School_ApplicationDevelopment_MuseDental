package gui;

import com.toedter.calendar.JDateChooser;
import entity.BenhNhan;
import util.GiaoDienUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Panel quản lý bệnh nhân (CRUD) cho mục DANH MỤC → "Quản lý bệnh nhân".
 * Dùng bảng BenhNhan: thông tin hành chính, CCCD, tiền sử bệnh lý, dị ứng,
 * người bảo hộ.
 *
 * Bên trái: danh sách + tìm kiếm (mã, họ tên, SĐT, CCCD). Bên phải: form thêm /
 * sửa / xóa.
 * Dữ liệu tạm giữ trong bộ nhớ, chỗ cần nối DAO được đánh dấu "TODO DAO".
 */
public class DanhMucQuanLyBenhNhanPanel extends JPanel {
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color LINE = new Color(226, 234, 237);
    private static final Color DANGER = new Color(214, 69, 80);

    private static final String[] GIOI_TINH = { "Nam", "Nu", "Khac" };
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9,10}$");
    private static final Pattern CCCD_PATTERN = Pattern.compile("^\\d{12}$");

    private final List<BenhNhan> danhSach = new ArrayList<>();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[] { "Mã BN", "Họ tên", "Giới tính", "Ngày sinh", "Số điện thoại", "Dị ứng" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);

    private final JTextField txtSearch = new JTextField();
    private final JLabel lblCount = new JLabel();

    private final JTextField txtMa = new JTextField();
    private final JTextField txtHoTen = new JTextField();
    private final JComboBox<String> cbGioiTinh = new JComboBox<>(GIOI_TINH);
    private final JDateChooser chonNgaySinh = new JDateChooser();
    private final JTextField txtSdt = new JTextField();
    private final JTextField txtCccd = new JTextField();
    private final JTextField txtDiaChi = new JTextField();
    private final JTextField txtNguoiBaoHo = new JTextField();
    private final JTextArea txtTienSu = new JTextArea(3, 20);
    private final JTextArea txtDiUng = new JTextArea(3, 20);

    private final JButton btnAdd = createButton("Thêm", TEAL, Color.WHITE);
    private final JButton btnUpdate = createButton("Sửa", new Color(51, 91, 177), Color.WHITE);
    private final JButton btnDelete = createButton("Xóa", DANGER, Color.WHITE);
    private final JButton btnClear = createButton("Làm mới", new Color(236, 241, 243), INK);

    public DanhMucQuanLyBenhNhanPanel() {
        setLayout(new BorderLayout(18, 0));
        setOpaque(false);

        loadData();
        setupTable();
        add(createListCard(), BorderLayout.CENTER);
        add(createFormCard(), BorderLayout.EAST);

        refreshTable();
        clearForm();
    }

    // ---------------------------- Dữ liệu ----------------------------

    /**
     * TODO DAO: thay bằng benhNhanDAO.getAll(). Hiện dùng dữ liệu mẫu theo file
     * SQL.
     */
    private void loadData() {
        danhSach.add(new BenhNhan("BN001", "Vo Thanh Hai", LocalDate.of(1990, 4, 10), "Nam", "0912000001",
                "Quan 7, TP.HCM", "079090000001", "Khong", "Di ung Penicillin", null));
        danhSach.add(new BenhNhan("BN002", "Nguyen Thi Lan", LocalDate.of(1985, 9, 21), "Nu", "0912000002",
                "Quan 10, TP.HCM", "079085000002", "Cao huyet ap", null, null));
        danhSach.add(new BenhNhan("BN003", "Dang Quoc Khanh", LocalDate.of(1978, 1, 15), "Nam", "0912000003",
                "TP. Thu Duc", "079078000003", "Tieu duong type 2", null, null));
        danhSach.add(new BenhNhan("BN004", "Tran Bao Ngoc", LocalDate.of(2015, 6, 30), "Nu", "0912000004",
                "Quan Phu Nhuan", null, "Khong", null, "Tran Van Minh (cha)"));
    }

    private BenhNhan findById(String ma) {
        for (BenhNhan bn : danhSach) {
            if (bn.getMaBenhNhan().equalsIgnoreCase(ma)) {
                return bn;
            }
        }
        return null;
    }

    /** Sinh mã BN### tiếp theo dựa trên mã lớn nhất hiện có. */
    private String nextId() {
        int max = 0;
        for (BenhNhan bn : danhSach) {
            try {
                max = Math.max(max, Integer.parseInt(bn.getMaBenhNhan().substring(2)));
            } catch (RuntimeException ignored) {
                // Bỏ qua mã không theo định dạng BN###.
            }
        }
        return String.format("BN%03d", max + 1);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String emptyToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }

    // ---------------------------- Giao diện ----------------------------

    private void setupTable() {
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setForeground(INK);
        table.setSelectionBackground(new Color(231, 246, 245));
        table.setSelectionForeground(INK);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSorter(sorter);

        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        table.getTableHeader().setForeground(MUTED);
        table.getTableHeader().setBackground(new Color(247, 250, 251));

        CellRenderer renderer = new CellRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(70);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(130);

        table.getSelectionModel().addListSelectionListener(event -> {
            if (event.getValueIsAdjusting() || table.getSelectedRow() < 0) {
                return;
            }
            String ma = (String) tableModel.getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), 0);
            BenhNhan bn = findById(ma);
            if (bn != null) {
                fillForm(bn);
                setEditing(true);
            }
        });
    }

    private JPanel createListCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(18, 18, 18, 18)));

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        styleTextField(txtSearch);
        txtSearch.setPreferredSize(new Dimension(300, 36));
        txtSearch.setToolTipText("Tìm theo mã bệnh nhân, họ tên, số điện thoại hoặc CCCD");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }
        });
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(txtSearch);

        lblCount.setFont(new Font("Arial", Font.BOLD, 11));
        lblCount.setForeground(MUTED);
        toolbar.add(left, BorderLayout.WEST);
        toolbar.add(lblCount, BorderLayout.EAST);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(Color.WHITE);

        card.add(toolbar, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel createFormCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(390, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(18, 18, 18, 18)));

        JLabel heading = new JLabel("Thông tin bệnh nhân");
        heading.setFont(new Font("Arial", Font.BOLD, 15));
        heading.setForeground(INK);
        card.add(heading, BorderLayout.NORTH);

        for (JTextField f : new JTextField[] { txtMa, txtHoTen, txtSdt, txtCccd, txtDiaChi,
                txtNguoiBaoHo }) {
            styleTextField(f);
        }
        GiaoDienUtil.cauHinhChonNgay(chonNgaySinh, "dd/MM/yyyy");
        chonNgaySinh.setPreferredSize(new Dimension(160, 34));
        cbGioiTinh.setFont(new Font("Arial", Font.PLAIN, 12));
        cbGioiTinh.setBackground(Color.WHITE);
        cbGioiTinh.setForeground(INK);
        txtMa.setEditable(false);
        txtMa.setBackground(new Color(244, 247, 250));
        chonNgaySinh.setToolTipText("Chọn ngày sinh từ lịch");
        txtCccd.setToolTipText("12 chữ số. Có thể để trống với trẻ em chưa có CCCD");
        txtNguoiBaoHo.setToolTipText("Bắt buộc với bệnh nhân dưới 18 tuổi");

        JScrollPane tienSuScroll = createTextAreaScroll(txtTienSu);
        JScrollPane diUngScroll = createTextAreaScroll(txtDiUng);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(createRow(field("Mã bệnh nhân", txtMa), field("Giới tính", cbGioiTinh)));
        form.add(Box.createVerticalStrut(12));
        form.add(createRow(field("Họ tên *", txtHoTen)));
        form.add(Box.createVerticalStrut(12));
        form.add(createRow(field("Ngày sinh", chonNgaySinh), field("Số điện thoại *", txtSdt)));
        form.add(Box.createVerticalStrut(12));
        form.add(createRow(field("Số CCCD", txtCccd)));
        form.add(Box.createVerticalStrut(12));
        form.add(createRow(field("Địa chỉ", txtDiaChi)));
        form.add(Box.createVerticalStrut(12));
        form.add(createRow(field("Người bảo hộ (dưới 18 tuổi)", txtNguoiBaoHo)));
        form.add(Box.createVerticalStrut(12));
        form.add(createRow(field("Tiền sử bệnh lý", tienSuScroll, 72)));
        form.add(Box.createVerticalStrut(12));
        form.add(createRow(field("Thông tin dị ứng", diUngScroll, 72)));
        form.add(Box.createVerticalGlue());

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(null);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        formScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        card.add(formScroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        actions.setOpaque(false);
        actions.add(btnAdd);
        actions.add(btnUpdate);
        actions.add(btnDelete);
        actions.add(btnClear);
        card.add(actions, BorderLayout.SOUTH);

        btnAdd.addActionListener(event -> onAdd());
        btnUpdate.addActionListener(event -> onUpdate());
        btnDelete.addActionListener(event -> onDelete());
        btnClear.addActionListener(event -> clearForm());
        return card;
    }

    private JScrollPane createTextAreaScroll(JTextArea area) {
        area.setFont(new Font("Arial", Font.PLAIN, 12));
        area.setForeground(INK);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new EmptyBorder(8, 10, 8, 10));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        return scroll;
    }

    private JPanel field(String label, JComponent input) {
        return field(label, input, 34);
    }

    private JPanel field(String label, JComponent input, int inputHeight) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.BOLD, 10));
        lbl.setForeground(MUTED);
        input.setPreferredSize(new Dimension(100, inputHeight));
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRow(JPanel... fields) {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                row.add(Box.createHorizontalStrut(10));
            }
            row.add(fields[i]);
        }
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Arial", Font.PLAIN, 12));
        field.setForeground(INK);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(0, 10, 0, 10)));
    }

    private static JButton createButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setForeground(foreground);
        button.setBackground(background);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(78, 36));
        return button;
    }

    // ---------------------------- Bảng & form ----------------------------

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (BenhNhan bn : danhSach) {
            tableModel.addRow(new Object[] {
                    bn.getMaBenhNhan(), bn.getHoTen(), nullToEmpty(bn.getGioiTinh()),
                    bn.getNgaySinh() == null ? "" : bn.getNgaySinh().format(DATE_FMT),
                    nullToEmpty(bn.getSoDienThoai()), nullToEmpty(bn.getThongTinDiUng())
            });
        }
        applyFilter();
    }

    /**
     * Lọc theo mã, họ tên, SĐT hoặc CCCD (CCCD không hiện trên bảng nên tra theo
     * entity).
     */
    private void applyFilter() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    for (int col : new int[] { 0, 1, 4 }) {
                        if (entry.getStringValue(col).toLowerCase().contains(keyword)) {
                            return true;
                        }
                    }
                    BenhNhan bn = findById(entry.getStringValue(0));
                    return bn != null && nullToEmpty(bn.getSoCCCD()).contains(keyword);
                }
            });
        }
        lblCount.setText("Hiển thị " + table.getRowCount() + " / " + danhSach.size() + " bệnh nhân");
    }

    private void fillForm(BenhNhan bn) {
        txtMa.setText(bn.getMaBenhNhan());
        txtHoTen.setText(bn.getHoTen());
        cbGioiTinh.setSelectedItem(bn.getGioiTinh());
        GiaoDienUtil.datNgayDuocChon(chonNgaySinh, bn.getNgaySinh());
        txtSdt.setText(nullToEmpty(bn.getSoDienThoai()));
        txtCccd.setText(nullToEmpty(bn.getSoCCCD()));
        txtDiaChi.setText(nullToEmpty(bn.getDiaChi()));
        txtNguoiBaoHo.setText(nullToEmpty(bn.getNguoiBaoHo()));
        txtTienSu.setText(nullToEmpty(bn.getTienSuBenhLy()));
        txtDiUng.setText(nullToEmpty(bn.getThongTinDiUng()));
        txtTienSu.setCaretPosition(0);
        txtDiUng.setCaretPosition(0);
    }

    private void clearForm() {
        table.clearSelection();
        txtMa.setText(nextId());
        txtHoTen.setText("");
        cbGioiTinh.setSelectedIndex(0);
        GiaoDienUtil.datNgayDuocChon(chonNgaySinh, null);
        txtSdt.setText("");
        txtCccd.setText("");
        txtDiaChi.setText("");
        txtNguoiBaoHo.setText("");
        txtTienSu.setText("");
        txtDiUng.setText("");
        setEditing(false);
        txtHoTen.requestFocusInWindow();
    }

    private void setEditing(boolean editing) {
        btnAdd.setEnabled(!editing);
        btnUpdate.setEnabled(editing);
        btnDelete.setEnabled(editing);
    }

    // ---------------------------- CRUD ----------------------------

    private void onAdd() {
        try {
            BenhNhan bn = readForm(txtMa.getText().trim());
            if (findById(bn.getMaBenhNhan()) != null) {
                throw new IllegalArgumentException("Mã bệnh nhân đã tồn tại.");
            }
            // TODO DAO: benhNhanDAO.insert(bn);
            // Lưu ý: HoSoBenhAn và BenhNhan quan hệ 1-1 (UQ_HoSoBenhAn_BenhNhan), nên nếu
            // hệ thống
            // yêu cầu mỗi bệnh nhân có hồ sơ ngay khi tạo thì tạo luôn HoSoBenhAn ở bước
            // này.
            danhSach.add(bn);
            refreshTable();
            clearForm();
            JOptionPane.showMessageDialog(this, "Đã thêm bệnh nhân " + bn.getHoTen() + ".");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dữ liệu không hợp lệ",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onUpdate() {
        try {
            BenhNhan updated = readForm(txtMa.getText().trim());
            BenhNhan current = findById(updated.getMaBenhNhan());
            if (current == null) {
                throw new IllegalArgumentException("Không tìm thấy bệnh nhân cần cập nhật.");
            }
            // TODO DAO: benhNhanDAO.update(updated);
            danhSach.set(danhSach.indexOf(current), updated);
            refreshTable();
            clearForm();
            JOptionPane.showMessageDialog(this, "Đã cập nhật thông tin bệnh nhân.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dữ liệu không hợp lệ",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onDelete() {
        BenhNhan bn = findById(txtMa.getText().trim());
        if (bn == null) {
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Xóa bệnh nhân " + bn.getMaBenhNhan() + " - " + bn.getHoTen() + "?\n"
                        + "Bệnh nhân đã có hồ sơ, lịch hẹn hoặc hóa đơn sẽ không xóa được.",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            // TODO DAO: benhNhanDAO.delete(ma). Bệnh nhân đã có HoSoBenhAn / LichHen /
            // HoaDon /
            // KeHoachDieuTri sẽ vướng khóa ngoại -> bắt SQLException và báo "Bệnh nhân đã
            // có dữ liệu
            // khám chữa bệnh, không thể xóa".
            danhSach.remove(bn);
            refreshTable();
            clearForm();
        }
    }

    // ---------------------------- Kiểm tra dữ liệu ----------------------------

    private BenhNhan readForm(String ma) {
        String hoTen = txtHoTen.getText().trim();
        if (hoTen.isEmpty()) {
            txtHoTen.requestFocusInWindow();
            throw new IllegalArgumentException("Họ tên không được để trống.");
        }
        if (hoTen.length() > 100) {
            throw new IllegalArgumentException("Họ tên tối đa 100 ký tự.");
        }

        LocalDate ngaySinh = GiaoDienUtil.layNgayDaChon(chonNgaySinh);
        if (ngaySinh != null && ngaySinh.isAfter(LocalDate.now())) {
            chonNgaySinh.requestFocusInWindow();
            throw new IllegalArgumentException("Ngày sinh không được sau ngày hiện tại.");
        }

        String sdt = txtSdt.getText().trim();
        if (!PHONE_PATTERN.matcher(sdt).matches()) {
            txtSdt.requestFocusInWindow();
            throw new IllegalArgumentException("Số điện thoại phải bắt đầu bằng 0 và có 10-11 chữ số.");
        }

        String cccd = txtCccd.getText().trim();
        if (!cccd.isEmpty() && !CCCD_PATTERN.matcher(cccd).matches()) {
            txtCccd.requestFocusInWindow();
            throw new IllegalArgumentException("Số CCCD phải gồm đúng 12 chữ số.");
        }

        String diaChi = txtDiaChi.getText().trim();
        if (diaChi.length() > 200) {
            throw new IllegalArgumentException("Địa chỉ tối đa 200 ký tự.");
        }

        String nguoiBaoHo = txtNguoiBaoHo.getText().trim();
        if (ngaySinh != null && Period.between(ngaySinh, LocalDate.now()).getYears() < 18
                && nguoiBaoHo.isEmpty()) {
            txtNguoiBaoHo.requestFocusInWindow();
            throw new IllegalArgumentException("Bệnh nhân dưới 18 tuổi bắt buộc nhập người bảo hộ.");
        }
        if (nguoiBaoHo.length() > 100) {
            throw new IllegalArgumentException("Người bảo hộ tối đa 100 ký tự.");
        }

        String tienSu = txtTienSu.getText().trim();
        if (tienSu.length() > 1000) {
            throw new IllegalArgumentException("Tiền sử bệnh lý tối đa 1000 ký tự.");
        }
        String diUng = txtDiUng.getText().trim();
        if (diUng.length() > 500) {
            throw new IllegalArgumentException("Thông tin dị ứng tối đa 500 ký tự.");
        }

        return new BenhNhan(ma, hoTen, ngaySinh, (String) cbGioiTinh.getSelectedItem(), sdt,
                emptyToNull(diaChi), emptyToNull(cccd), emptyToNull(tienSu), emptyToNull(diUng),
                emptyToNull(nguoiBaoHo));
    }

    // ---------------------------- Renderer ----------------------------

    /**
     * Cột "Dị ứng": để trống thì hiện "Không", có dị ứng thì tô đỏ để dễ nhận biết.
     */
    private static final class CellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                boolean focus, int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, false, row, column);
            setBorder(new EmptyBorder(0, 10, 0, 10));
            setFont(new Font("Arial", Font.PLAIN, 12));
            setForeground(INK);
            if (!selected) {
                setBackground(Color.WHITE);
            }
            if (column == 5) {
                if (value == null || value.toString().isEmpty()) {
                    setText("Không");
                    setForeground(MUTED);
                } else {
                    setForeground(DANGER);
                    setFont(new Font("Arial", Font.BOLD, 12));
                }
            }
            return this;
        }
    }
}
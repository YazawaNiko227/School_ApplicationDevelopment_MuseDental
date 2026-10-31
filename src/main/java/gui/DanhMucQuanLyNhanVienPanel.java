package gui;

import dao.NhanVienDAO;
import entity.NhanVien;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class DanhMucQuanLyNhanVienPanel extends JPanel {
        // Bảng màu dùng chung với ManHinhChinhFrame.
        private static final Color INK = new Color(32, 53, 68);
        private static final Color MUTED = new Color(119, 139, 151);
        private static final Color TEAL = new Color(26, 151, 151);
        private static final Color LINE = new Color(226, 234, 237);
        private static final Color SELECT_BG = new Color(231, 246, 245);
        private static final Color DANGER = new Color(214, 69, 80);
        private static final Color WARN = new Color(177, 125, 47);

        // Giá trị trạng thái / giới tính đúng theo dữ liệu trong CSDL.
        private static final String TT_DANG_LAM = "Dang lam viec";
        private static final String TT_TAM_NGHI = "Tam nghi";
        private static final String TT_DA_NGHI = "Da nghi viec";
        private static final String TAT_CA = "Tất cả trạng thái";
        private static final String[] TRANG_THAI = { TT_DANG_LAM, TT_TAM_NGHI, TT_DA_NGHI };
        private static final String[] GIOI_TINH = { "Nam", "Nu", "Khac" };
        private static final String[] CHUYEN_MON = {
                        "", "Nha si - Nho rang", "Nha si - Chinh nha", "Nha si - Tong quat",
                        "Y ta", "Le tan", "Thu ngan", "Thu kho"
        };

        private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/uuuu")
                        .withResolverStyle(ResolverStyle.STRICT);
        private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9,10}$");
        private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

        // Dữ liệu và bảng.
        private final List<NhanVien> danhSach = new ArrayList<>();
        private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
        private final DefaultTableModel tableModel = new DefaultTableModel(
                        new String[] { "Mã NV", "Họ tên", "Giới tính", "Số điện thoại", "Chuyên môn", "Trạng thái" },
                        0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                        return false;
                }
        };
        private final JTable table = new JTable(tableModel);
        private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);

        // Thanh công cụ.
        private final JTextField txtSearch = new JTextField();
        private final JComboBox<String> cbFilterStatus = new JComboBox<>();
        private final JLabel lblCount = new JLabel();

        // Biểu mẫu.
        private final JTextField txtMa = new JTextField();
        private final JTextField txtHoTen = new JTextField();
        private final JTextField txtNgaySinh = new JTextField();
        private final JComboBox<String> cbGioiTinh = new JComboBox<>(GIOI_TINH);
        private final JTextField txtSdt = new JTextField();
        private final JTextField txtEmail = new JTextField();
        private final JTextField txtDiaChi = new JTextField();
        private final JTextField txtNgayVaoLam = new JTextField();
        private final JComboBox<String> cbTrangThai = new JComboBox<>(TRANG_THAI);
        private final JComboBox<String> cbChuyenMon = new JComboBox<>(CHUYEN_MON);
        private final JTextField txtChungChi = new JTextField();

        private final JButton btnAdd = createButton("Thêm", TEAL, Color.WHITE);
        private final JButton btnUpdate = createButton("Sửa", new Color(51, 91, 177), Color.WHITE);
        private final JButton btnDelete = createButton("Xóa", DANGER, Color.WHITE);
        private final JButton btnClear = createButton("Làm mới", new Color(236, 241, 243), INK);

        public DanhMucQuanLyNhanVienPanel() {
                setLayout(new BorderLayout(18, 0));
                setOpaque(false);

                loadData();
                setupTable();
                add(createListCard(), BorderLayout.CENTER);
                add(createFormCard(), BorderLayout.EAST);

                refreshTable();
                clearForm();
        }

        // ------------------------------------------------------------------
        // Dữ liệu
        // ------------------------------------------------------------------

        private void loadData() {
                danhSach.clear();
                try {
                        danhSach.addAll(nhanVienDAO.layTatCa());
                } catch (java.sql.SQLException exception) {
                        JOptionPane.showMessageDialog(
                                        this,
                                        "Không thể tải danh sách nhân viên từ CSDL:\n" + exception.getMessage(),
                                        "Lỗi CSDL",
                                        JOptionPane.ERROR_MESSAGE);
                }
        }

        private NhanVien findById(String ma) {
                for (NhanVien nv : danhSach) {
                        if (nv.getMaNhanVien().equalsIgnoreCase(ma)) {
                                return nv;
                        }
                }
                return null;
        }

        /** Sinh mã dạng NV### tiếp theo dựa trên mã lớn nhất hiện có. */
        private String nextId() {
                int max = 0;
                for (NhanVien nv : danhSach) {
                        String ma = nv.getMaNhanVien();
                        if (ma != null && ma.toUpperCase().startsWith("NV")) {
                                try {
                                        max = Math.max(max, Integer.parseInt(ma.substring(2)));
                                } catch (NumberFormatException ignored) {
                                        // Bỏ qua mã không theo định dạng NV###.
                                }
                        }
                }
                return String.format("NV%03d", max + 1);
        }

        // ------------------------------------------------------------------
        // Dựng giao diện
        // ------------------------------------------------------------------

        private void setupTable() {
                table.setRowHeight(40);
                table.setShowGrid(false);
                table.setIntercellSpacing(new Dimension(0, 0));
                table.setFont(new Font("Arial", Font.PLAIN, 12));
                table.setForeground(INK);
                table.setSelectionBackground(SELECT_BG);
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
                table.getColumnModel().getColumn(1).setPreferredWidth(170);
                table.getColumnModel().getColumn(2).setPreferredWidth(70);
                table.getColumnModel().getColumn(3).setPreferredWidth(110);
                table.getColumnModel().getColumn(4).setPreferredWidth(150);
                table.getColumnModel().getColumn(5).setPreferredWidth(110);

                table.getSelectionModel().addListSelectionListener(event -> {
                        if (event.getValueIsAdjusting()) {
                                return;
                        }
                        int viewRow = table.getSelectedRow();
                        if (viewRow < 0) {
                                return;
                        }
                        String ma = (String) tableModel.getValueAt(table.convertRowIndexToModel(viewRow), 0);
                        NhanVien nv = findById(ma);
                        if (nv != null) {
                                fillForm(nv);
                                setEditing(true);
                        }
                });
        }

        private JPanel createListCard() {
                JPanel card = new JPanel(new BorderLayout(0, 14));
                card.setBackground(Color.WHITE);
                card.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(LINE),
                                new EmptyBorder(18, 18, 18, 18)));

                // Thanh tìm kiếm + lọc trạng thái + tổng số.
                JPanel toolbar = new JPanel(new BorderLayout());
                toolbar.setOpaque(false);

                JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
                left.setOpaque(false);
                styleTextField(txtSearch);
                txtSearch.setPreferredSize(new Dimension(260, 36));
                txtSearch.setToolTipText("Tìm theo mã, họ tên, số điện thoại hoặc chuyên môn");
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
                cbFilterStatus.addItem(TAT_CA);
                for (String status : TRANG_THAI) {
                        cbFilterStatus.addItem(status);
                }
                styleCombo(cbFilterStatus);
                cbFilterStatus.setPreferredSize(new Dimension(170, 36));
                cbFilterStatus.addActionListener(event -> applyFilter());
                left.add(txtSearch);
                left.add(cbFilterStatus);

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
                                BorderFactory.createLineBorder(LINE),
                                new EmptyBorder(18, 18, 18, 18)));

                JLabel heading = new JLabel("Thông tin nhân viên");
                heading.setFont(new Font("Arial", Font.BOLD, 15));
                heading.setForeground(INK);
                card.add(heading, BorderLayout.NORTH);

                for (JTextField field : new JTextField[] { txtMa, txtHoTen, txtNgaySinh, txtSdt, txtEmail, txtDiaChi,
                                txtNgayVaoLam, txtChungChi }) {
                        styleTextField(field);
                }
                for (JComboBox<String> combo : java.util.Arrays.asList(cbGioiTinh, cbTrangThai, cbChuyenMon)) {
                        styleCombo(combo);
                }
                cbChuyenMon.setEditable(true);
                txtMa.setBackground(new Color(244, 247, 250));
                txtNgaySinh.setToolTipText("Định dạng dd/MM/yyyy");
                txtNgayVaoLam.setToolTipText("Định dạng dd/MM/yyyy");

                JPanel form = new JPanel();
                form.setOpaque(false);
                form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
                form.add(createRow(field("Mã nhân viên", txtMa), field("Giới tính", cbGioiTinh)));
                form.add(Box.createVerticalStrut(12));
                form.add(createRow(field("Họ tên *", txtHoTen)));
                form.add(Box.createVerticalStrut(12));
                form.add(createRow(field("Ngày sinh (dd/MM/yyyy)", txtNgaySinh),
                                field("Ngày vào làm", txtNgayVaoLam)));
                form.add(Box.createVerticalStrut(12));
                form.add(createRow(field("Số điện thoại *", txtSdt), field("Trạng thái", cbTrangThai)));
                form.add(Box.createVerticalStrut(12));
                form.add(createRow(field("Email", txtEmail)));
                form.add(Box.createVerticalStrut(12));
                form.add(createRow(field("Địa chỉ", txtDiaChi)));
                form.add(Box.createVerticalStrut(12));
                form.add(createRow(field("Chuyên môn", cbChuyenMon)));
                form.add(Box.createVerticalStrut(12));
                form.add(createRow(field("Số chứng chỉ hành nghề", txtChungChi)));
                form.add(Box.createVerticalGlue());

                JScrollPane scroll = new JScrollPane(form);
                scroll.setBorder(null);
                scroll.setOpaque(false);
                scroll.getViewport().setOpaque(false);
                scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                scroll.getVerticalScrollBar().setUnitIncrement(16);
                card.add(scroll, BorderLayout.CENTER);

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

        /** Một ô nhập gồm nhãn phía trên và control phía dưới. */
        private JPanel field(String label, JComponent input) {
                JPanel panel = new JPanel(new BorderLayout(0, 5));
                panel.setOpaque(false);
                JLabel lbl = new JLabel(label);
                lbl.setFont(new Font("Arial", Font.BOLD, 10));
                lbl.setForeground(MUTED);
                input.setPreferredSize(new Dimension(100, 34));
                panel.add(lbl, BorderLayout.NORTH);
                panel.add(input, BorderLayout.CENTER);
                return panel;
        }

        /** Ghép 1 hoặc 2 ô nhập thành một hàng ngang bằng BoxLayout. */
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
                                BorderFactory.createLineBorder(LINE),
                                new EmptyBorder(0, 10, 0, 10)));
        }

        private void styleCombo(JComboBox<String> combo) {
                combo.setFont(new Font("Arial", Font.PLAIN, 12));
                combo.setBackground(Color.WHITE);
                combo.setForeground(INK);
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

        // ------------------------------------------------------------------
        // Bảng, lọc, form
        // ------------------------------------------------------------------

        private void refreshTable() {
                tableModel.setRowCount(0);
                for (NhanVien nv : danhSach) {
                        tableModel.addRow(new Object[] {
                                        nv.getMaNhanVien(), nv.getHoTen(), nv.getGioiTinh(), nv.getSoDienThoai(),
                                        nv.getChuyenMon() == null ? "" : nv.getChuyenMon(), nv.getTrangThaiLamViec()
                        });
                }
                applyFilter();
        }

        private void applyFilter() {
                List<RowFilter<DefaultTableModel, Integer>> filters = new ArrayList<>();
                String keyword = txtSearch.getText().trim();
                if (!keyword.isEmpty()) {
                        filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(keyword), 0, 1, 3, 4));
                }
                Object status = cbFilterStatus.getSelectedItem();
                if (status != null && !TAT_CA.equals(status)) {
                        filters.add(RowFilter.regexFilter("^" + Pattern.quote(status.toString()) + "$", 5));
                }
                sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
                lblCount.setText("Hiển thị " + table.getRowCount() + " / " + danhSach.size() + " nhân viên");
        }

        private void fillForm(NhanVien nv) {
                txtMa.setText(nv.getMaNhanVien());
                txtHoTen.setText(nv.getHoTen());
                txtNgaySinh.setText(nv.getNgaySinh() == null ? "" : nv.getNgaySinh().format(DATE_FMT));
                cbGioiTinh.setSelectedItem(nv.getGioiTinh());
                txtSdt.setText(nv.getSoDienThoai());
                txtEmail.setText(nv.getEmail() == null ? "" : nv.getEmail());
                txtDiaChi.setText(nv.getDiaChi() == null ? "" : nv.getDiaChi());
                txtNgayVaoLam.setText(nv.getNgayVaoLam() == null ? "" : nv.getNgayVaoLam().format(DATE_FMT));
                cbTrangThai.setSelectedItem(nv.getTrangThaiLamViec());
                cbChuyenMon.setSelectedItem(nv.getChuyenMon() == null ? "" : nv.getChuyenMon());
                txtChungChi.setText(nv.getSoChungChiHanhNghe() == null ? "" : nv.getSoChungChiHanhNghe());
        }

        /** Đưa form về trạng thái thêm mới. */
        private void clearForm() {
                table.clearSelection();
                txtMa.setText(nextId());
                txtHoTen.setText("");
                txtNgaySinh.setText("");
                cbGioiTinh.setSelectedIndex(0);
                txtSdt.setText("");
                txtEmail.setText("");
                txtDiaChi.setText("");
                txtNgayVaoLam.setText(LocalDate.now().format(DATE_FMT));
                cbTrangThai.setSelectedItem(TT_DANG_LAM);
                cbChuyenMon.setSelectedItem("");
                txtChungChi.setText("");
                setEditing(false);
                txtHoTen.requestFocusInWindow();
        }

        /** editing = true khi đang chọn một nhân viên có sẵn (cho phép Sửa / Xóa). */
        private void setEditing(boolean editing) {
                btnAdd.setEnabled(!editing);
                btnUpdate.setEnabled(editing);
                btnDelete.setEnabled(editing);
        }

        // ------------------------------------------------------------------
        // CRUD
        // ------------------------------------------------------------------

        private void onAdd() {
                try {
                        NhanVien nv = readForm(txtMa.getText().trim());
                        if (findById(nv.getMaNhanVien()) != null) {
                                throw new IllegalArgumentException(
                                                "Mã nhân viên " + nv.getMaNhanVien() + " đã tồn tại.");
                        }
                        if (!nhanVienDAO.them(nv)) {
                                throw new java.sql.SQLException("Không thêm được hồ sơ nhân viên.");
                        }
                        danhSach.add(nv);
                        refreshTable();
                        clearForm();
                        JOptionPane.showMessageDialog(this, "Đã thêm nhân viên " + nv.getHoTen() + ".",
                                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Dữ liệu không hợp lệ",
                                        JOptionPane.WARNING_MESSAGE);
                } catch (java.sql.SQLException ex) {
                        hienThiLoiCSDL("Không thể thêm nhân viên", ex);
                }
        }

        private void onUpdate() {
                try {
                        NhanVien updated = readForm(txtMa.getText().trim());
                        NhanVien current = findById(updated.getMaNhanVien());
                        if (current == null) {
                                throw new IllegalArgumentException("Không tìm thấy nhân viên cần cập nhật.");
                        }
                        if (!nhanVienDAO.capNhat(updated)) {
                                throw new java.sql.SQLException("Không tìm thấy nhân viên cần cập nhật trong CSDL.");
                        }
                        danhSach.set(danhSach.indexOf(current), updated);
                        refreshTable();
                        selectRowById(updated.getMaNhanVien());
                        JOptionPane.showMessageDialog(this, "Đã cập nhật thông tin nhân viên.",
                                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Dữ liệu không hợp lệ",
                                        JOptionPane.WARNING_MESSAGE);
                } catch (java.sql.SQLException ex) {
                        hienThiLoiCSDL("Không thể cập nhật nhân viên", ex);
                }
        }

        /**
         * NhanVien được tham chiếu bởi TaiKhoan, CaTruc, ChanDoan, LichHen, HoaDon...
         * nên xóa cứng dễ vướng khóa ngoại. Vì vậy cho người dùng chọn giữa xóa hẳn
         * và chuyển sang trạng thái "Da nghi viec".
         */
        private void onDelete() {
                NhanVien nv = findById(txtMa.getText().trim());
                if (nv == null) {
                        return;
                }
                Object[] options = { "Xóa hẳn", "Cho nghỉ việc", "Hủy" };
                int choice = JOptionPane.showOptionDialog(this,
                                "Bạn muốn xử lý nhân viên " + nv.getMaNhanVien() + " - " + nv.getHoTen() + " thế nào?\n"
                                                + "• Xóa hẳn: xóa khỏi hệ thống (không thực hiện được nếu nhân viên đã có dữ liệu liên quan).\n"
                                                + "• Cho nghỉ việc: giữ lại hồ sơ, chuyển trạng thái sang \"Da nghi viec\".",
                                "Xác nhận", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                                null, options, options[1]);

                if (choice == 0) {
                        try {
                                if (!nhanVienDAO.xoa(nv.getMaNhanVien())) {
                                        throw new java.sql.SQLException("Không tìm thấy nhân viên trong CSDL.");
                                }
                                danhSach.remove(nv);
                                refreshTable();
                                clearForm();
                        } catch (java.sql.SQLException exception) {
                                hienThiLoiCSDL("Không thể xóa nhân viên", exception);
                        }
                } else if (choice == 1) {
                        try {
                                if (!nhanVienDAO.capNhatTrangThai(nv.getMaNhanVien(), TT_DA_NGHI)) {
                                        throw new java.sql.SQLException("Không tìm thấy nhân viên trong CSDL.");
                                }
                                nv.setTrangThaiLamViec(TT_DA_NGHI);
                                refreshTable();
                                selectRowById(nv.getMaNhanVien());
                        } catch (java.sql.SQLException exception) {
                                hienThiLoiCSDL("Không thể cập nhật trạng thái nhân viên", exception);
                        }
                }
        }

        private void hienThiLoiCSDL(String thaoTac, java.sql.SQLException exception) {
                JOptionPane.showMessageDialog(
                                this,
                                thaoTac + " do lỗi CSDL:\n" + exception.getMessage(),
                                "Lỗi CSDL",
                                JOptionPane.ERROR_MESSAGE);
        }

        private void selectRowById(String ma) {
                for (int i = 0; i < table.getRowCount(); i++) {
                        if (ma.equals(tableModel.getValueAt(table.convertRowIndexToModel(i), 0))) {
                                table.setRowSelectionInterval(i, i);
                                return;
                        }
                }
        }

        // ------------------------------------------------------------------
        // Đọc + kiểm tra dữ liệu form
        // ------------------------------------------------------------------

        private NhanVien readForm(String ma) {
                String hoTen = txtHoTen.getText().trim();
                if (hoTen.isEmpty()) {
                        txtHoTen.requestFocusInWindow();
                        throw new IllegalArgumentException("Họ tên không được để trống.");
                }
                if (hoTen.length() > 100) {
                        throw new IllegalArgumentException("Họ tên tối đa 100 ký tự.");
                }

                LocalDate ngaySinh = parseDate(txtNgaySinh, "Ngày sinh");
                if (ngaySinh != null && !ngaySinh.isBefore(LocalDate.now())) {
                        throw new IllegalArgumentException("Ngày sinh phải nhỏ hơn ngày hiện tại.");
                }
                LocalDate ngayVaoLam = parseDate(txtNgayVaoLam, "Ngày vào làm");
                if (ngaySinh != null && ngayVaoLam != null && ngayVaoLam.isBefore(ngaySinh)) {
                        throw new IllegalArgumentException("Ngày vào làm không được trước ngày sinh.");
                }

                String sdt = txtSdt.getText().trim();
                if (sdt.isEmpty() || !PHONE_PATTERN.matcher(sdt).matches()) {
                        txtSdt.requestFocusInWindow();
                        throw new IllegalArgumentException("Số điện thoại phải bắt đầu bằng 0 và có 10-11 chữ số.");
                }

                String email = txtEmail.getText().trim();
                if (!email.isEmpty() && (email.length() > 100 || !EMAIL_PATTERN.matcher(email).matches())) {
                        txtEmail.requestFocusInWindow();
                        throw new IllegalArgumentException("Email không đúng định dạng.");
                }

                String diaChi = txtDiaChi.getText().trim();
                if (diaChi.length() > 200) {
                        throw new IllegalArgumentException("Địa chỉ tối đa 200 ký tự.");
                }

                Object chuyenMonValue = cbChuyenMon.getSelectedItem();
                String chuyenMon = chuyenMonValue == null ? "" : chuyenMonValue.toString().trim();
                String chungChi = txtChungChi.getText().trim();
                if (chuyenMon.toLowerCase().startsWith("nha si") && chungChi.isEmpty()) {
                        txtChungChi.requestFocusInWindow();
                        throw new IllegalArgumentException("Nha sĩ bắt buộc nhập số chứng chỉ hành nghề.");
                }
                if (chungChi.length() > 50) {
                        throw new IllegalArgumentException("Số chứng chỉ hành nghề tối đa 50 ký tự.");
                }

                return new NhanVien(ma, hoTen, ngaySinh, (String) cbGioiTinh.getSelectedItem(), sdt,
                                emptyToNull(email), emptyToNull(diaChi), ngayVaoLam,
                                (String) cbTrangThai.getSelectedItem(), emptyToNull(chuyenMon), emptyToNull(chungChi));
        }

        private LocalDate parseDate(JTextField field, String name) {
                String text = field.getText().trim();
                if (text.isEmpty()) {
                        return null;
                }
                try {
                        return LocalDate.parse(text, DATE_FMT);
                } catch (DateTimeParseException ex) {
                        field.requestFocusInWindow();
                        throw new IllegalArgumentException(name + " không hợp lệ. Vui lòng nhập theo dạng dd/MM/yyyy.");
                }
        }

        private static String emptyToNull(String value) {
                return value == null || value.isEmpty() ? null : value;
        }

        // ------------------------------------------------------------------
        // Renderer: padding cho ô + tô màu cột trạng thái
        // ------------------------------------------------------------------

        private static final class CellRenderer extends DefaultTableCellRenderer {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                boolean focus, int row, int column) {
                        super.getTableCellRendererComponent(table, value, selected, false, row, column);
                        setBorder(new EmptyBorder(0, 10, 0, 10));
                        setHorizontalAlignment(SwingConstants.LEFT);
                        setFont(new Font("Arial", column == 5 ? Font.BOLD : Font.PLAIN, 12));
                        setForeground(INK);
                        if (column == 5 && value != null) {
                                String status = value.toString();
                                if (TT_DANG_LAM.equals(status)) {
                                        setForeground(TEAL);
                                } else if (TT_TAM_NGHI.equals(status)) {
                                        setForeground(WARN);
                                } else {
                                        setForeground(DANGER);
                                }
                        }
                        if (!selected) {
                                setBackground(Color.WHITE);
                        }
                        return this;
                }
        }
}
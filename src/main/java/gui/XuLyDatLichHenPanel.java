package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;
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
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.border.EmptyBorder;

/**
 * Panel "Đặt lịch hẹn khám".
 *
 * Luồng xử lý:
 * 1. Tìm bệnh nhân (hoặc bấm "Bệnh nhân mới" nếu chưa từng khám).
 * 2. Chọn nha sĩ, phòng, ngày giờ hẹn rồi bấm "Lưu lịch hẹn" -> LichHen = "Chờ
 * xác nhận".
 * 3. Chọn dòng trong bảng, bấm "Xác nhận" -> "Đã xác nhận" hoặc "Hủy lịch" ->
 * "Huỷ".
 *
 * Hiện tại chưa có DAO: dữ liệu là mẫu trong bộ nhớ. Những chỗ cần nối DAO
 * được đánh dấu bằng "TODO DAO".
 */
public final class XuLyDatLichHenPanel extends JPanel {
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color LINE = new Color(226, 234, 237);

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Trạng thái hiển thị trên giao diện (DB lưu dạng không dấu: 'Cho xac nhan',
    // 'Da xac nhan', ...).
    private static final String CHO_XAC_NHAN = "Chờ xác nhận";
    private static final String DA_XAC_NHAN = "Đã xác nhận";
    private static final String DA_CHECK_IN = "Đã check-in";
    private static final String DANG_KHAM = "Đang khám";
    private static final String HOAN_THANH = "Hoàn thành";
    private static final String HUY = "Hủy";
    private static final String VANG = "Vắng";

    // ----- Dữ liệu mẫu (TODO DAO: thay bằng BenhNhanDAO / NhanVienDAO /
    // PhongKhamDAO) -----
    private static final String[][] MOCK_BENH_NHAN = {
            { "BN001", "Võ Thanh Hải", "0912000001", "10/04/1990", "Nam", "Quận 7, TP.HCM" },
            { "BN002", "Nguyễn Thị Lan", "0912000002", "21/09/1985", "Nữ", "Quận 10, TP.HCM" },
            { "BN003", "Đặng Quốc Khánh", "0912000003", "15/01/1978", "Nam", "TP. Thủ Đức" },
            { "BN004", "Trần Bảo Ngọc", "0912000004", "30/06/2015", "Nữ", "Quận Phú Nhuận" }
    };
    private static final String[] MOCK_NHA_SI = {
            "NV001 - Nguyễn Văn An", "NV002 - Trần Thị Bình"
    };
    private static final String[] MOCK_PHONG = {
            "P01 - Phòng khám số 1", "P02 - Phòng khám số 2"
    };

    // ----- Form bệnh nhân -----
    private final JTextField txtTimKiem = new JTextField();
    private final JTextField txtMaBenhNhan = new JTextField();
    private final JTextField txtHoTen = new JTextField();
    private final JTextField txtSoDienThoai = new JTextField();
    private final JTextField txtNgaySinh = new JTextField();
    private final JComboBox<String> cboGioiTinh = new JComboBox<>(new String[] { "Nam", "Nữ" });
    private final JTextField txtDiaChi = new JTextField();

    // ----- Form lịch hẹn -----
    private final JComboBox<String> cboNhaSi = new JComboBox<>(MOCK_NHA_SI);
    private final JComboBox<String> cboPhong = new JComboBox<>(MOCK_PHONG);
    private final JSpinner spnNgayHen = new JSpinner(
            new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
    private final JSpinner spnGioHen = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE));

    // ----- Bảng danh sách -----
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[] { "Mã lịch hẹn", "Thời gian hẹn", "Mã BN", "Bệnh nhân", "Nha sĩ", "Phòng", "Trạng thái" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
    private final JComboBox<String> cboLocTrangThai = new JComboBox<>(new String[] {
            "Tất cả", CHO_XAC_NHAN, DA_XAC_NHAN, DA_CHECK_IN, DANG_KHAM, HOAN_THANH, HUY, VANG });

    // true khi lễ tân bấm "Bệnh nhân mới" (cần tạo BenhNhan + HoSoBenhAn rỗng khi
    // lưu).
    private boolean laBenhNhanMoi = false;
    // Bộ đếm tạm để sinh mã giả; TODO DAO: sinh mã từ DB.
    private int nextLichHen = 6;
    private int nextBenhNhan = 5;

    public XuLyDatLichHenPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);

        add(createFormCard(), BorderLayout.NORTH);
        add(createTableCard(), BorderLayout.CENTER);

        spnNgayHen.setEditor(new JSpinner.DateEditor(spnNgayHen, "dd/MM/yyyy"));
        spnGioHen.setEditor(new JSpinner.DateEditor(spnGioHen, "HH:mm"));

        khoaThongTinBenhNhan(true);
        loadMockAppointments();
    }

    // =====================================================================
    // GIAO DIỆN
    // =====================================================================

    private JPanel createFormCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(16, 18, 16, 18)));

        // Hai cột cạnh nhau: bệnh nhân (trái) và lịch hẹn (phải).
        JPanel columns = new JPanel();
        columns.setOpaque(false);
        columns.setLayout(new BoxLayout(columns, BoxLayout.X_AXIS));
        columns.add(createPatientColumn());
        columns.add(Box.createHorizontalStrut(24));
        columns.add(createAppointmentColumn());
        card.add(columns, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        JButton btnLamMoi = createButton("Làm mới", false);
        JButton btnLuu = createButton("Lưu lịch hẹn", true);
        btnLamMoi.addActionListener(event -> clearForm());
        btnLuu.addActionListener(event -> saveAppointment());
        buttons.add(btnLamMoi);
        buttons.add(btnLuu);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createPatientColumn() {
        JPanel column = createColumn("Thông tin bệnh nhân");

        // Hàng tìm kiếm: ô nhập + nút Tìm + nút Bệnh nhân mới.
        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);
        styleField(txtTimKiem);
        txtTimKiem.setToolTipText("Nhập mã bệnh nhân, họ tên hoặc số điện thoại");
        JPanel searchButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchButtons.setOpaque(false);
        JButton btnTim = createButton("Tìm", true);
        JButton btnMoi = createButton("Bệnh nhân mới", false);
        btnTim.addActionListener(event -> searchPatient());
        txtTimKiem.addActionListener(event -> searchPatient());
        btnMoi.addActionListener(event -> startNewPatient());
        searchButtons.add(btnTim);
        searchButtons.add(btnMoi);
        searchRow.add(createLabel("Tìm bệnh nhân"), BorderLayout.WEST);
        searchRow.add(txtTimKiem, BorderLayout.CENTER);
        searchRow.add(searchButtons, BorderLayout.EAST);
        lockHeight(searchRow);
        column.add(searchRow);
        column.add(Box.createVerticalStrut(8));

        txtMaBenhNhan.setEditable(false);
        column.add(createRow("Mã bệnh nhân", txtMaBenhNhan));
        column.add(createRow("Họ tên", txtHoTen));
        column.add(createRow("Số điện thoại", txtSoDienThoai));
        column.add(createRow("Ngày sinh", txtNgaySinh));
        column.add(createRow("Giới tính", cboGioiTinh));
        column.add(createRow("Địa chỉ", txtDiaChi));
        return column;
    }

    private JPanel createAppointmentColumn() {
        JPanel column = createColumn("Thông tin lịch hẹn");
        column.add(createRow("Nha sĩ phụ trách", cboNhaSi));
        column.add(createRow("Phòng khám", cboPhong));
        column.add(createRow("Ngày hẹn", spnNgayHen));
        column.add(createRow("Giờ hẹn", spnGioHen));

        JLabel hint = new JLabel("<html>Lịch mới được lưu với trạng thái <b>Chờ xác nhận</b>."
                + " Lễ tân xác nhận ở bảng bên dưới.</html>");
        hint.setFont(new Font("Arial", Font.PLAIN, 11));
        hint.setForeground(MUTED);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        column.add(Box.createVerticalStrut(10));
        column.add(hint);
        column.add(Box.createVerticalGlue());
        return column;
    }

    private JPanel createTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(16, 18, 16, 18)));

        // Thanh trên: tiêu đề + bộ lọc trạng thái.
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("Danh sách lịch hẹn");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setForeground(INK);
        JPanel filter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filter.setOpaque(false);
        filter.add(createLabel("Trạng thái:"));
        cboLocTrangThai.setFont(new Font("Arial", Font.PLAIN, 12));
        cboLocTrangThai.addActionListener(event -> applyStatusFilter());
        filter.add(cboLocTrangThai);
        top.add(title, BorderLayout.WEST);
        top.add(filter, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        // Bảng.
        table.setRowSorter(sorter);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.setSelectionBackground(new Color(231, 246, 245));
        table.setSelectionForeground(INK);
        table.setGridColor(LINE);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        card.add(scroll, BorderLayout.CENTER);

        // Nút thao tác trên dòng đang chọn.
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton btnHuy = createButton("Hủy lịch", false);
        JButton btnXacNhan = createButton("Xác nhận lịch hẹn", true);
        btnHuy.addActionListener(event -> cancelSelected());
        btnXacNhan.addActionListener(event -> confirmSelected());
        actions.add(btnHuy);
        actions.add(btnXacNhan);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    // =====================================================================
    // XỬ LÝ SỰ KIỆN
    // =====================================================================

    /** Tìm bệnh nhân theo mã / họ tên / SĐT. */
    private void searchPatient() {
        String keyword = txtTimKiem.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            showMessage("Vui lòng nhập mã, họ tên hoặc số điện thoại để tìm.");
            return;
        }
        // TODO DAO: BenhNhanDAO.timKiem(keyword)
        for (String[] bn : MOCK_BENH_NHAN) {
            if (bn[0].toLowerCase().contains(keyword)
                    || bn[1].toLowerCase().contains(keyword)
                    || bn[2].contains(keyword)) {
                txtMaBenhNhan.setText(bn[0]);
                txtHoTen.setText(bn[1]);
                txtSoDienThoai.setText(bn[2]);
                txtNgaySinh.setText(bn[3]);
                cboGioiTinh.setSelectedItem(bn[4]);
                txtDiaChi.setText(bn[5]);
                laBenhNhanMoi = false;
                khoaThongTinBenhNhan(true);
                return;
            }
        }
        showMessage("Không tìm thấy bệnh nhân. Bấm \"Bệnh nhân mới\" để tạo hồ sơ.");
    }

    /** Chuyển form sang chế độ nhập bệnh nhân mới. */
    private void startNewPatient() {
        clearPatientFields();
        txtMaBenhNhan.setText("(tự sinh khi lưu)");
        laBenhNhanMoi = true;
        khoaThongTinBenhNhan(false);
        txtHoTen.requestFocus();
    }

    /** Kiểm tra dữ liệu rồi lưu lịch hẹn với trạng thái "Chờ xác nhận". */
    private void saveAppointment() {
        boolean daCoBenhNhan = laBenhNhanMoi || txtMaBenhNhan.getText().trim().matches("BN\\d+");
        if (!daCoBenhNhan) {
            showMessage("Vui lòng tìm bệnh nhân hoặc chọn \"Bệnh nhân mới\".");
            return;
        }
        if (laBenhNhanMoi && (txtHoTen.getText().trim().isEmpty() || txtSoDienThoai.getText().trim().isEmpty())) {
            showMessage("Bệnh nhân mới cần nhập tối thiểu họ tên và số điện thoại.");
            return;
        }

        LocalDateTime thoiGianHen = getSelectedDateTime();
        if (thoiGianHen.isBefore(LocalDateTime.now())) {
            showMessage("Thời gian hẹn phải sau thời điểm hiện tại.");
            return;
        }

        // TODO DAO: kiểm tra trùng lịch của nha sĩ / phòng tại thời điểm này.

        String maBenhNhan = txtMaBenhNhan.getText().trim();
        if (laBenhNhanMoi) {
            // TODO DAO: BenhNhanDAO.them(...) + HoSoBenhAnDAO.taoRong(maBenhNhan)
            maBenhNhan = String.format("BN%03d", nextBenhNhan++);
        }

        // TODO DAO: LichHenDAO.them(...) với TrangThai = 'Cho xac nhan', MaNhanVienLap
        // = nhân viên đăng nhập.
        String maLichHen = String.format("LH%03d", nextLichHen++);
        tableModel.addRow(new Object[] {
                maLichHen,
                thoiGianHen.format(DATE_TIME_FORMAT),
                maBenhNhan,
                txtHoTen.getText().trim(),
                (String) cboNhaSi.getSelectedItem(),
                (String) cboPhong.getSelectedItem(),
                CHO_XAC_NHAN
        });
        showMessage("Đã lưu lịch hẹn " + maLichHen + " (Chờ xác nhận).");
        clearForm();
    }

    /** Chuyển lịch đang chọn từ "Chờ xác nhận" sang "Đã xác nhận". */
    private void confirmSelected() {
        int row = getSelectedModelRow();
        if (row < 0) {
            return;
        }
        if (!CHO_XAC_NHAN.equals(tableModel.getValueAt(row, 6))) {
            showMessage("Chỉ xác nhận được lịch đang ở trạng thái \"Chờ xác nhận\".");
            return;
        }
        // TODO DAO: LichHenDAO.capNhatTrangThai(maLichHen, 'Da xac nhan')
        tableModel.setValueAt(DA_XAC_NHAN, row, 6);
    }

    /** Hủy lịch đang chọn (chỉ khi chưa check-in). */
    private void cancelSelected() {
        int row = getSelectedModelRow();
        if (row < 0) {
            return;
        }
        Object status = tableModel.getValueAt(row, 6);
        if (!CHO_XAC_NHAN.equals(status) && !DA_XAC_NHAN.equals(status)) {
            showMessage("Chỉ hủy được lịch \"Chờ xác nhận\" hoặc \"Đã xác nhận\".");
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Hủy lịch hẹn " + tableModel.getValueAt(row, 0) + "?",
                "Xác nhận hủy", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            // TODO DAO: LichHenDAO.capNhatTrangThai(maLichHen, 'Huy')
            tableModel.setValueAt(HUY, row, 6);
        }
    }

    private void applyStatusFilter() {
        String selected = (String) cboLocTrangThai.getSelectedItem();
        if (selected == null || "Tất cả".equals(selected)) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("^" + Pattern.quote(selected) + "$", 6));
        }
    }

    // =====================================================================
    // HÀM HỖ TRỢ
    // =====================================================================

    private void loadMockAppointments() {
        // TODO DAO: LichHenDAO.layDanhSach() thay cho dữ liệu mẫu này.
        tableModel.addRow(new Object[] { "LH003", "19/10/2026 08:30", "BN003", "Đặng Quốc Khánh",
                MOCK_NHA_SI[0], MOCK_PHONG[0], DA_XAC_NHAN });
        tableModel.addRow(new Object[] { "LH004", "19/10/2026 14:00", "BN002", "Nguyễn Thị Lan",
                MOCK_NHA_SI[1], MOCK_PHONG[1], CHO_XAC_NHAN });
        tableModel.addRow(new Object[] { "LH005", "20/10/2026 09:00", "BN004", "Trần Bảo Ngọc",
                MOCK_NHA_SI[1], MOCK_PHONG[1], CHO_XAC_NHAN });
    }

    private LocalDateTime getSelectedDateTime() {
        Calendar day = Calendar.getInstance();
        day.setTime((Date) spnNgayHen.getValue());
        Calendar time = Calendar.getInstance();
        time.setTime((Date) spnGioHen.getValue());
        day.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
        day.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
        day.set(Calendar.SECOND, 0);
        return day.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Trả về chỉ số dòng theo model, hoặc -1 (kèm thông báo) nếu chưa chọn dòng
     * nào.
     */
    private int getSelectedModelRow() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            showMessage("Vui lòng chọn một lịch hẹn trong bảng.");
            return -1;
        }
        return table.convertRowIndexToModel(viewRow);
    }

    /**
     * true = khóa các ô thông tin bệnh nhân (bệnh nhân cũ), false = cho nhập (bệnh
     * nhân mới).
     */
    private void khoaThongTinBenhNhan(boolean khoa) {
        txtHoTen.setEditable(!khoa);
        txtSoDienThoai.setEditable(!khoa);
        txtNgaySinh.setEditable(!khoa);
        txtDiaChi.setEditable(!khoa);
        cboGioiTinh.setEnabled(!khoa);
    }

    private void clearPatientFields() {
        txtMaBenhNhan.setText("");
        txtHoTen.setText("");
        txtSoDienThoai.setText("");
        txtNgaySinh.setText("");
        txtDiaChi.setText("");
        cboGioiTinh.setSelectedIndex(0);
    }

    private void clearForm() {
        txtTimKiem.setText("");
        clearPatientFields();
        laBenhNhanMoi = false;
        khoaThongTinBenhNhan(true);
        cboNhaSi.setSelectedIndex(0);
        cboPhong.setSelectedIndex(0);
        spnNgayHen.setValue(new Date());
        spnGioHen.setValue(new Date());
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    // ----- Tạo thành phần giao diện -----

    private JPanel createColumn(String title) {
        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(title);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setForeground(TEAL);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        column.add(label);
        column.add(Box.createVerticalStrut(10));
        return column;
    }

    /**
     * Một hàng "nhãn + ô nhập": nhãn rộng cố định bên trái, ô nhập giãn hết phần
     * còn lại.
     */
    private JPanel createRow(String labelText, Component field) {
        if (field instanceof JTextField) {
            styleField((JTextField) field);
        } else {
            field.setFont(new Font("Arial", Font.PLAIN, 12));
        }
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(createLabel(labelText), BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        lockHeight(row);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 0, 8, 0));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(row, BorderLayout.CENTER);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        return wrapper;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setForeground(INK);
        label.setPreferredSize(new Dimension(110, 32));
        return label;
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("Arial", Font.PLAIN, 12));
        field.setPreferredSize(new Dimension(160, 32));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(0, 8, 0, 8)));
    }

    private void lockHeight(JPanel panel) {
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
    }

    private JButton createButton(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
        button.setOpaque(true);
        if (primary) {
            button.setBackground(TEAL);
            button.setForeground(Color.WHITE);
            button.setBorderPainted(false);
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(INK);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(LINE),
                    new EmptyBorder(7, 15, 7, 15)));
        }
        return button;
    }
}
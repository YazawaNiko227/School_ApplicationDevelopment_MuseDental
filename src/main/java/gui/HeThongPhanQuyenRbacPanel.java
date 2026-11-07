/*
    Người làm phần này: Phan Đức Toàn
    MSSV: 24741631
*/

package gui;

import entity.VaiTro;
import service.PhanQuyenRbacService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.CellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import util.PhanQuyenMenu;

public final class HeThongPhanQuyenRbacPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(HeThongPhanQuyenRbacPanel.class.getName());
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(102, 119, 130);
    private static final Color LINE = new Color(226, 234, 237);
    private static final String TAT_CA = "Tất cả vai trò";
    private static final int COT_NHOM = 0;
    private static final int COT_CHUC_NANG = 1;
    private static final int COT_VAI_TRO_DAU = 2;

    private final PhanQuyenRbacService phanQuyenService = new PhanQuyenRbacService();
    /** Mã vai trò của tài khoản đang đăng nhập, dùng để chặn thao tác lưu. */
    private final String maVaiTro;
    private List<VaiTro> vaiTros;
    private JComboBox<String> roleSelector;
    private JLabel summary;
    private PermissionTableModel permissionModel;
    private JTable permissionTable;
    private JButton saveButton;

    public HeThongPhanQuyenRbacPanel() {
        this(null);
    }

    public HeThongPhanQuyenRbacPanel(String maVaiTro) {
        this.maVaiTro = maVaiTro == null ? null : maVaiTro.trim();
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        try {
            phanQuyenService.khoiTaoQuyenMacDinhNeuChuaCo();
            vaiTros = phanQuyenService.layDanhSachVaiTro();
            permissionModel = new PermissionTableModel();
            permissionTable = new JTable(permissionModel);
            loadMatrix();
            add(createRoleToolbar(), BorderLayout.NORTH);
            add(createPermissionCard(), BorderLayout.CENTER);
            applyFilter();
            updateSaveButtonState();
        } catch (SQLException | RuntimeException exception) {
            add(createErrorPanel(exception), BorderLayout.CENTER);
        }
    }

    /**
     * Một dòng của ma trận phân quyền: tên nhóm, tên chức năng và quyền tương ứng
     * với từng vai trò (theo đúng thứ tự trong {@link #vaiTros}).
     */
    private static final class QuyenRow {
        private final String nhom;
        private final String chucNang;
        private final boolean[] quyen;

        private QuyenRow(String nhom, String chucNang, boolean[] quyen) {
            this.nhom = nhom;
            this.chucNang = chucNang;
            this.quyen = quyen;
        }
    }

    /**
     * Model giữ toàn bộ dữ liệu phân quyền và một danh sách hiển thị đã lọc theo
     * vai trò. Việc lọc được thực hiện bằng cách đổi danh sách hiển thị thay vì
     * dùng {@code TableRowSorter}, tránh lỗi IndexOutOfBoundsException khi vừa
     * chỉnh sửa ô vừa lọc dữ liệu.
     */
    private final class PermissionTableModel extends AbstractTableModel {
        private final List<QuyenRow> tatCa = new ArrayList<>();
        private final List<QuyenRow> hienThi = new ArrayList<>();
        private int vaiTroDangLoc = -1;

        private void napDuLieu(List<QuyenRow> rows) {
            tatCa.clear();
            tatCa.addAll(rows);
            locTheoVaiTro(vaiTroDangLoc);
        }

        private void locTheoVaiTro(int vaiTroIndex) {
            vaiTroDangLoc = vaiTroIndex;
            hienThi.clear();
            for (QuyenRow row : tatCa) {
                if (vaiTroIndex < 0 || row.quyen[vaiTroIndex]) {
                    hienThi.add(row);
                }
            }
            fireTableDataChanged();
        }

        private List<QuyenRow> getTatCa() {
            return tatCa;
        }

        private int getTongSoDong() {
            return tatCa.size();
        }

        @Override
        public int getRowCount() {
            return hienThi.size();
        }

        @Override
        public int getColumnCount() {
            return COT_VAI_TRO_DAU + vaiTros.size();
        }

        @Override
        public String getColumnName(int column) {
            if (column == COT_NHOM) {
                return "Nhóm";
            }
            if (column == COT_CHUC_NANG) {
                return "Chức năng";
            }
            return vaiTros.get(column - COT_VAI_TRO_DAU).getTenVaiTro();
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex < COT_VAI_TRO_DAU ? String.class : Boolean.class;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column >= COT_VAI_TRO_DAU;
        }

        @Override
        public Object getValueAt(int row, int column) {
            QuyenRow quyenRow = hienThi.get(row);
            if (column == COT_NHOM) {
                return quyenRow.nhom;
            }
            if (column == COT_CHUC_NANG) {
                return quyenRow.chucNang;
            }
            return quyenRow.quyen[column - COT_VAI_TRO_DAU];
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            if (column < COT_VAI_TRO_DAU) {
                return;
            }
            hienThi.get(row).quyen[column - COT_VAI_TRO_DAU] = Boolean.TRUE.equals(value);
            fireTableCellUpdated(row, column);
        }
    }

    private void loadMatrix() throws SQLException {
        Map<String, Set<String>> storedPermissions = phanQuyenService.layTatCaQuyen();
        List<QuyenRow> rows = new ArrayList<>();
        for (Map.Entry<String, List<String>> group : PhanQuyenMenu.nhomMenu().entrySet()) {
            for (String item : group.getValue()) {
                boolean[] quyen = new boolean[vaiTros.size()];
                for (int i = 0; i < vaiTros.size(); i++) {
                    Set<String> allowed = storedPermissions.get(vaiTros.get(i).getMaVaiTro());
                    quyen[i] = allowed != null && allowed.contains(item);
                }
                rows.add(new QuyenRow(group.getKey(), item, quyen));
            }
        }
        permissionModel.napDuLieu(rows);
    }

    private JPanel createRoleToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        JPanel rolePicker = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        rolePicker.setBackground(Color.WHITE);
        rolePicker.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(12, 14, 12, 14)));
        JLabel roleLabel = new JLabel("Lọc theo vai trò:");
        roleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        roleLabel.setForeground(INK);

        roleSelector = new JComboBox<>();
        roleSelector.addItem(TAT_CA);
        for (VaiTro vaiTro : vaiTros) {
            roleSelector.addItem(vaiTro.getTenVaiTro());
        }
        roleSelector.setFont(new Font("Arial", Font.PLAIN, 12));
        roleSelector.setPreferredSize(new Dimension(190, 36));
        roleSelector.addActionListener(event -> applyFilter());
        rolePicker.add(roleLabel);
        rolePicker.add(roleSelector);

        JButton nutLuu = new JButton("Lưu phân quyền");
        nutLuu.setFont(new Font("Arial", Font.BOLD, 11));
        nutLuu.setForeground(Color.WHITE);
        nutLuu.setBackground(new Color(26, 151, 151));
        nutLuu.setBorderPainted(false);
        nutLuu.setFocusPainted(false);
        nutLuu.setPreferredSize(new Dimension(140, 38));
        nutLuu.addActionListener(event -> savePermissions());
        this.saveButton = nutLuu;

        toolbar.add(rolePicker, BorderLayout.WEST);
        toolbar.add(nutLuu, BorderLayout.EAST);
        return toolbar;
    }

    /** Chỉ quản trị viên mới được phép thay đổi phân quyền. */
    private boolean coQuyenLuuPhanQuyen() {
        return phanQuyenService.coQuyenCapNhat(maVaiTro);
    }

    private void updateSaveButtonState() {
        if (saveButton == null) {
            return;
        }
        boolean coQuyen = coQuyenLuuPhanQuyen();
        saveButton.setEnabled(coQuyen);
        saveButton.setToolTipText(coQuyen ? null
                : "Chỉ tài khoản quản trị viên mới được thay đổi phân quyền.");
    }

    private JPanel createPermissionCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(17, 18, 14, 18)));

        JLabel title = new JLabel("Quyền xem chức năng theo vai trò");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setForeground(INK);
        summary = new JLabel();
        summary.setFont(new Font("Arial", Font.PLAIN, 11));
        summary.setForeground(MUTED);
        JPanel heading = new JPanel(new GridLayout(2, 1, 0, 4));
        heading.setOpaque(false);
        heading.add(title);
        heading.add(summary);

        configurePermissionTable();
        JScrollPane scrollPane = new JScrollPane(permissionTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(LINE));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JLabel note = new JLabel(
                "<html>Quyền được lưu trong SQL Server và áp dụng khi người dùng đăng nhập lần tiếp theo. "
                        + "Mục \"Tổng quan\" luôn hiển thị. Phân quyền hiện kiểm soát hiển thị menu, "
                        + "chưa thay thế kiểm tra quyền ở từng thao tác dữ liệu.</html>");
        note.setFont(new Font("Arial", Font.PLAIN, 10));
        note.setForeground(MUTED);

        card.add(heading, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        card.add(note, BorderLayout.SOUTH);
        return card;
    }

    private void configurePermissionTable() {
        permissionTable.setFont(new Font("Arial", Font.PLAIN, 12));
        permissionTable.setForeground(INK);
        permissionTable.setRowHeight(34);
        permissionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        permissionTable.setGridColor(new Color(237, 242, 244));
        permissionTable.setShowVerticalLines(false);
        permissionTable.setFillsViewportHeight(true);
        permissionTable.setRowSelectionAllowed(false);
        // Ghi nhận ngay giá trị ô khi người dùng bấm ra ngoài (ví dụ bấm nút Lưu).
        permissionTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        // Cho phép bật/tắt ô quyền chỉ bằng một lần bấm chuột.
        JCheckBox checkBox = new JCheckBox();
        checkBox.setHorizontalAlignment(SwingConstants.CENTER);
        DefaultCellEditor booleanEditor = new DefaultCellEditor(checkBox);
        booleanEditor.setClickCountToStart(1);
        permissionTable.setDefaultEditor(Boolean.class, booleanEditor);

        JTableHeader header = permissionTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 11));
        header.setForeground(MUTED);
        header.setBackground(new Color(248, 250, 251));
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);
        permissionTable.getColumnModel().getColumn(COT_NHOM).setPreferredWidth(90);
        permissionTable.getColumnModel().getColumn(COT_CHUC_NANG).setPreferredWidth(300);
        for (int column = COT_VAI_TRO_DAU; column < permissionTable.getColumnCount(); column++) {
            permissionTable.getColumnModel().getColumn(column).setPreferredWidth(95);
        }
    }

    private void savePermissions() {
        if (!coQuyenLuuPhanQuyen()) {
            showMessage("Chỉ tài khoản quản trị viên mới được thay đổi phân quyền.",
                    "Không đủ quyền", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!commitDangChinhSua()) {
            return;
        }

        Map<String, Set<String>> permissions = new LinkedHashMap<>();
        for (VaiTro vaiTro : vaiTros) {
            permissions.put(vaiTro.getMaVaiTro(), new LinkedHashSet<>());
        }
        // Đọc từ dữ liệu gốc để không bỏ sót các dòng đang bị ẩn bởi bộ lọc.
        for (QuyenRow row : permissionModel.getTatCa()) {
            for (int i = 0; i < vaiTros.size(); i++) {
                if (row.quyen[i]) {
                    permissions.get(vaiTros.get(i).getMaVaiTro()).add(row.chucNang);
                }
            }
        }

        try {
            phanQuyenService.capNhatTatCaQuyen(permissions);
        } catch (SQLException | RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Không thể lưu phân quyền RBAC.", exception);
            showMessage("Không thể lưu phân quyền:\n" + moTaLoi(exception),
                    "Lỗi phân quyền", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Đọc lại từ CSDL để màn hình phản ánh đúng dữ liệu vừa ghi.
            loadMatrix();
            applyFilter();
        } catch (SQLException | RuntimeException exception) {
            LOGGER.log(Level.WARNING, "Đã lưu phân quyền nhưng không tải lại được bảng.", exception);
        }
        showMessage("Đã lưu phân quyền. Thay đổi sẽ có hiệu lực sau khi tài khoản đăng nhập lại.",
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Ghi nhận giá trị của ô đang chỉnh sửa vào model. Trả về false nếu không thể
     * ghi nhận, kèm thông báo để người dùng biết lý do thay vì im lặng.
     */
    private boolean commitDangChinhSua() {
        if (!permissionTable.isEditing()) {
            return true;
        }
        CellEditor editor = permissionTable.getCellEditor();
        if (editor == null) {
            permissionTable.removeEditor();
            return true;
        }
        try {
            if (editor.stopCellEditing()) {
                return true;
            }
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Không thể ghi nhận ô đang chỉnh sửa.", exception);
        }
        showMessage("Không thể ghi nhận giá trị vừa nhập. Hãy nhấn Enter hoặc bấm ra ngoài ô rồi thử lưu lại.",
                "Chưa thể lưu", JOptionPane.WARNING_MESSAGE);
        return false;
    }

    private void applyFilter() {
        if (permissionModel == null || roleSelector == null || summary == null) {
            return;
        }
        int selected = roleSelector.getSelectedIndex();
        int roleIndex = selected <= 0 ? -1 : selected - 1;
        permissionModel.locTheoVaiTro(roleIndex);
        int total = permissionModel.getTongSoDong();
        if (roleIndex < 0) {
            summary.setText("Hiển thị " + vaiTros.size() + " vai trò × " + total
                    + " chức năng. Chọn ô để cấp hoặc thu hồi quyền xem.");
            return;
        }
        summary.setText(roleSelector.getSelectedItem() + " được cấp quyền xem "
                + permissionModel.getRowCount() + "/" + total + " chức năng.");
    }

    private static String moTaLoi(Throwable loi) {
        String message = loi.getMessage();
        if (message == null || message.trim().isEmpty()) {
            message = loi.getClass().getSimpleName();
        }
        return message;
    }

    private void showMessage(String message, String title, int messageType) {
        java.awt.Window owner = SwingUtilities.getWindowAncestor(this);
        JOptionPane.showMessageDialog(owner == null ? this : owner, message, title, messageType);
    }

    private JPanel createErrorPanel(Exception exception) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(20, 22, 20, 22)));
        JLabel message = new JLabel("<html><b>Không thể tải cấu hình phân quyền từ CSDL.</b><br>"
                + moTaLoi(exception) + "<br>Hãy chạy database/Migration_RBAC.sql trong SSMS.</html>");
        message.setForeground(new Color(184, 55, 55));
        panel.add(message, BorderLayout.NORTH);
        return panel;
    }
}

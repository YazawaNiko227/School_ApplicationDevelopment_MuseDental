/* trinhKiet */
package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class EmployeeSearchPanel extends JPanel {

    // Các thành phần nhập liệu và bảng kết quả của màn hình tìm kiếm nhân viên.
    private JTextField txtSearch;
    private JComboBox<String> cbRoleFilter;
    private JComboBox<String> cbStatusFilter;
    private JButton btnSearch;
    private JButton btnReset;
    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private JLabel lblResultCount;

    // Bảng màu dùng chung; nền nút tìm kiếm được làm sáng để chữ tối dễ đọc.
    private static final Color PRIMARY_COLOR = new Color(218, 232, 250);
    private static final Color BG_LIGHT = new Color(245, 247, 250);
    private static final Color TEXT_DARK = new Color(33, 37, 41);
    private static final Color BORDER_COLOR = new Color(222, 226, 230);

    // Khởi tạo giao diện, sau đó hiển thị danh sách nhân viên mẫu ban đầu.
    public EmployeeSearchPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(BG_LIGHT);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initUI();
        loadMockData(); // Nạp dữ liệu mẫu ban đầu
    }

    // Tạo thanh tìm kiếm, bộ lọc chức vụ/trạng thái và bảng kết quả.
    private void initUI() {
        // --- 1. KHU VỰC THANH CÔNG CỤ TÌM KIẾM ---
        JPanel searchCard = new JPanel();
        searchCard.setLayout(new BoxLayout(searchCard, BoxLayout.Y_AXIS));
        searchCard.setBackground(Color.WHITE);
        searchCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(15, 20, 15, 20)));

        // Dòng nhập từ khóa
        JPanel searchRow = new JPanel(new BorderLayout(10, 0));
        searchRow.setOpaque(false);

        JLabel lblSearch = new JLabel("Từ khóa tìm kiếm:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSearch.setForeground(TEXT_DARK);

        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setPreferredSize(new Dimension(300, 36));
        txtSearch.putClientProperty("JTextField.placeholderText",
                "Nhập mã NV, họ tên, số điện thoại, email hoặc CCCD...");
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performSearch();
                }
            }
        });

        searchRow.add(lblSearch, BorderLayout.WEST);
        searchRow.add(txtSearch, BorderLayout.CENTER);

        // Dòng bộ lọc và nút bấm
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        filterRow.setOpaque(false);

        JLabel lblRole = new JLabel("Chức vụ:");
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbRoleFilter = new JComboBox<>(
                new String[] { "Tất cả chức vụ", "Bác sĩ", "Y tá", "Lễ tân", "Quản lý", "Dược sĩ" });
        cbRoleFilter.setPreferredSize(new Dimension(140, 32));

        JLabel lblStatus = new JLabel("Trạng thái:");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbStatusFilter = new JComboBox<>(
                new String[] { "Tất cả trạng thái", "Đang làm việc", "Nghỉ phép", "Đã nghỉ việc" });
        cbStatusFilter.setPreferredSize(new Dimension(140, 32));

        btnSearch = new JButton("Tìm kiếm");
        btnSearch.setBackground(PRIMARY_COLOR);
        btnSearch.setForeground(TEXT_DARK);
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSearch.setFocusPainted(false);
        btnSearch.setPreferredSize(new Dimension(110, 32));
        btnSearch.addActionListener(e -> performSearch());

        btnReset = new JButton("Xóa lọc");
        btnReset.setBackground(new Color(230, 235, 240));
        btnReset.setForeground(TEXT_DARK);
        btnReset.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnReset.setFocusPainted(false);
        btnReset.setPreferredSize(new Dimension(95, 32));
        btnReset.addActionListener(e -> resetSearch());

        filterRow.add(lblRole);
        filterRow.add(cbRoleFilter);
        filterRow.add(Box.createHorizontalStrut(10));
        filterRow.add(lblStatus);
        filterRow.add(cbStatusFilter);
        filterRow.add(Box.createHorizontalStrut(15));
        filterRow.add(btnSearch);
        filterRow.add(btnReset);

        searchCard.add(searchRow);
        searchCard.add(Box.createVerticalStrut(5));
        searchCard.add(filterRow);

        add(searchCard, BorderLayout.NORTH);

        // --- 2. BẢNG HIỂN THỊ KẾT QUẢ ---
        // Model giữ dữ liệu theo các cột nhân viên và không cho phép sửa trực tiếp trên bảng.
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));

        String[] columns = { "Mã NV", "Họ và tên", "Chức vụ", "Số điện thoại", "Email", "CCCD / CMND", "Trạng thái" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        employeeTable = new JTable(tableModel);
        employeeTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        employeeTable.setRowHeight(32);
        employeeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        employeeTable.setShowGrid(true);
        employeeTable.setGridColor(new Color(235, 238, 242));

        JTableHeader header = employeeTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(240, 243, 246));
        header.setForeground(TEXT_DARK);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));

        // Căn giữa mã nhân viên, chức vụ, số điện thoại và trạng thái để dễ theo dõi.
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        employeeTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        employeeTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        employeeTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        employeeTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(employeeTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        // --- 3. THANH THỐNG KÊ DƯỚI BẢNG ---
        // Hiển thị số dòng hiện có sau mỗi lần tìm kiếm hoặc đặt lại bộ lọc.
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        footerPanel.setBackground(Color.WHITE);
        lblResultCount = new JLabel("Tổng số nhân viên: 0");
        lblResultCount.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblResultCount.setForeground(new Color(108, 117, 125));
        footerPanel.add(lblResultCount);

        tableContainer.add(footerPanel, BorderLayout.SOUTH);
        add(tableContainer, BorderLayout.CENTER);
    }

    // Nạp dữ liệu nhân viên minh họa và cập nhật số lượng hiển thị.
    private void loadMockData() {
        tableModel.setRowCount(0);
        Object[][] data = {
                { "NV001", "Nguyễn Văn Hùng", "Bác sĩ", "0901234567", "hung.nv@musedental.vn", "079201001234",
                        "Đang làm việc" },
                { "NV002", "Trần Thị Mai", "Y tá", "0912345678", "mai.tt@musedental.vn", "079302002345",
                        "Đang làm việc" },
                { "NV003", "Lê Minh Sang", "Lễ tân", "0923456789", "sang.lm@musedental.vn", "079403003456",
                        "Đang làm việc" },
                { "NV004", "Phạm Quốc Toàn", "Quản lý", "0934567890", "toan.pq@musedental.vn", "079104004567",
                        "Đang làm việc" },
                { "NV005", "Võ Thành Tài", "Dược sĩ", "0945678901", "tai.vt@musedental.vn", "079505005678",
                        "Nghỉ phép" },
                { "NV006", "Đỗ Kim Ngân", "Y tá", "0956789012", "ngan.dk@musedental.vn", "079606006789",
                        "Đã nghỉ việc" }
        };

        for (Object[] row : data) {
            tableModel.addRow(row);
        }
        updateCount();
    }

    // Lọc nhân viên theo từ khóa, chức vụ và trạng thái đã chọn.
    private void performSearch() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        String selectedRole = cbRoleFilter.getSelectedItem().toString();
        String selectedStatus = cbStatusFilter.getSelectedItem().toString();

        tableModel.setRowCount(0);
        Object[][] rawData = {
                { "NV001", "Nguyễn Văn Hùng", "Bác sĩ", "0901234567", "hung.nv@musedental.vn", "079201001234",
                        "Đang làm việc" },
                { "NV002", "Trần Thị Mai", "Y tá", "0912345678", "mai.tt@musedental.vn", "079302002345",
                        "Đang làm việc" },
                { "NV003", "Lê Minh Sang", "Lễ tân", "0923456789", "sang.lm@musedental.vn", "079403003456",
                        "Đang làm việc" },
                { "NV004", "Phạm Quốc Toàn", "Quản lý", "0934567890", "toan.pq@musedental.vn", "079104004567",
                        "Đang làm việc" },
                { "NV005", "Võ Thành Tài", "Dược sĩ", "0945678901", "tai.vt@musedental.vn", "079505005678",
                        "Nghỉ phép" },
                { "NV006", "Đỗ Kim Ngân", "Y tá", "0956789012", "ngan.dk@musedental.vn", "079606006789",
                        "Đã nghỉ việc" }
        };

        // Chỉ thêm nhân viên khi đồng thời khớp từ khóa và các bộ lọc đang chọn.
        for (Object[] row : rawData) {
            String id = row[0].toString().toLowerCase();
            String name = row[1].toString().toLowerCase();
            String role = row[2].toString();
            String phone = row[3].toString();
            String email = row[4].toString().toLowerCase();
            String cccd = row[5].toString();
            String status = row[6].toString();

            boolean matchKeyword = keyword.isEmpty() || id.contains(keyword) || name.contains(keyword)
                    || phone.contains(keyword) || email.contains(keyword) || cccd.contains(keyword);

            boolean matchRole = selectedRole.equals("Tất cả chức vụ") || role.equalsIgnoreCase(selectedRole);
            boolean matchStatus = selectedStatus.equals("Tất cả trạng thái") || status.equalsIgnoreCase(selectedStatus);

            if (matchKeyword && matchRole && matchStatus) {
                tableModel.addRow(row);
            }
        }
        updateCount();
    }

    // Xóa từ khóa, khôi phục bộ lọc mặc định và nạp lại toàn bộ danh sách mẫu.
    private void resetSearch() {
        txtSearch.setText("");
        cbRoleFilter.setSelectedIndex(0);
        cbStatusFilter.setSelectedIndex(0);
        loadMockData();
    }

    // Đồng bộ nhãn tổng số nhân viên với số dòng đang có trong bảng.
    private void updateCount() {
        lblResultCount.setText("Tổng số nhân viên: " + tableModel.getRowCount());
    }
}
/* end trinhKiet */
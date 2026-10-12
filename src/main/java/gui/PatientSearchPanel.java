package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

/**
 * Giao diện tra cứu hồ sơ bệnh nhân theo các trường trong sơ đồ CSDL của dự án.
 */
public final class PatientSearchPanel extends JPanel {
    // Bảng màu dùng chung cho chữ, nút và đường viền của màn hình.
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color LINE = new Color(226, 234, 237);

    // Dữ liệu minh họa tại chỗ; thay bằng dữ liệu truy vấn CSDL khi tích hợp JDBC.
    private final List<Patient> patients = createSamplePatients();
    // Điều khiển nhập từ khóa và hiển thị số hồ sơ tìm thấy.
    private final JTextField searchField = new JTextField();
    private final JLabel resultCount = new JLabel();
    // Model chỉ đọc, chứa các cột được hiển thị trong danh sách kết quả.
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[] { "Mã bệnh nhân", "Họ và tên", "Ngày sinh", "Giới tính", "Số điện thoại", "CCCD" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable patientTable = new JTable(tableModel);
    // Cho phép sắp xếp danh sách theo tiêu đề cột.
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);

    // Khởi tạo nền, thanh tìm kiếm và bảng kết quả; ban đầu hiển thị toàn bộ dữ liệu mẫu.
    public PatientSearchPanel() {
        setLayout(new BorderLayout(0, 18));
        setBackground(new Color(244, 248, 249));
        setBorder(new EmptyBorder(0, 0, 0, 0));
        add(createSearchToolbar(), BorderLayout.NORTH);
        add(createResultsPanel(), BorderLayout.CENTER);
        populateTable(patients);
    }

    // Tạo phần tiêu đề, ô nhập từ khóa và các nút thao tác tìm kiếm.
    private JPanel createSearchToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(0, 14));
        toolbar.setOpaque(false);

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new javax.swing.BoxLayout(titleBlock, javax.swing.BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Hồ sơ bệnh nhân");
        title.setFont(new Font("Arial", Font.BOLD, 21));
        title.setForeground(INK);
        JLabel subtitle = new JLabel("Tìm nhanh bệnh nhân theo mã, họ tên, số điện thoại hoặc CCCD.");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitle.setForeground(MUTED);
        titleBlock.add(title);
        titleBlock.add(javax.swing.Box.createVerticalStrut(5));
        titleBlock.add(subtitle);
        heading.add(titleBlock, BorderLayout.WEST);

        JPanel searchBox = new JPanel(new BorderLayout(10, 0));
        searchBox.setBackground(Color.WHITE);
        searchBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(12, 14, 12, 14)));

        JPanel inputGroup = new JPanel(new BorderLayout(10, 0));
        inputGroup.setBackground(Color.WHITE);
        JLabel searchLabel = new JLabel("TÌM KIẾM");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 9));
        searchLabel.setForeground(INK);
        searchField.setFont(new Font("Arial", Font.PLAIN, 13));
        searchField.setForeground(INK);
        searchField.setToolTipText("Nhập mã BN, họ tên, số điện thoại hoặc CCCD");
        searchField.setPreferredSize(new Dimension(200, 38));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(0, 11, 0, 11)));
        // Nhấn Enter trong ô nhập cũng thực hiện tra cứu.
        searchField.addActionListener(event -> searchPatients());
        inputGroup.add(searchLabel, BorderLayout.NORTH);
        inputGroup.add(searchField, BorderLayout.CENTER);
        searchBox.add(inputGroup, BorderLayout.CENTER);

        JPanel actions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton clearButton = new JButton("Xóa lọc");
        styleSecondaryButton(clearButton);
        // Xóa từ khóa, khôi phục danh sách đầy đủ và đưa con trỏ về ô tìm kiếm.
        clearButton.addActionListener(event -> {
            searchField.setText("");
            searchPatients();
            searchField.requestFocusInWindow();
        });

        // Nút tìm kiếm có nền sáng và chữ tối để dễ đọc.
        JButton searchButton = new JButton("Tìm hồ sơ");
        searchButton.setFont(new Font("Arial", Font.BOLD, 12));
        searchButton.setForeground(INK);
        searchButton.setBackground(new Color(218, 241, 239));
        searchButton.setBorder(new EmptyBorder(11, 17, 11, 17));
        searchButton.setFocusPainted(false);
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.addActionListener(event -> searchPatients());
        actions.add(clearButton);
        actions.add(searchButton);
        searchBox.add(actions, BorderLayout.EAST);

        toolbar.add(heading, BorderLayout.NORTH);
        toolbar.add(searchBox, BorderLayout.CENTER);
        return toolbar;
    }

    // Tạo khu vực bảng kết quả, tiêu đề cột và hướng dẫn mở hồ sơ chi tiết.
    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(18, 19, 15, 19)));

        JPanel tableHeading = new JPanel(new BorderLayout());
        tableHeading.setOpaque(false);
        JLabel heading = new JLabel("Danh sách bệnh nhân");
        heading.setFont(new Font("Arial", Font.BOLD, 15));
        heading.setForeground(INK);
        resultCount.setFont(new Font("Arial", Font.PLAIN, 11));
        resultCount.setForeground(MUTED);
        tableHeading.add(heading, BorderLayout.WEST);
        tableHeading.add(resultCount, BorderLayout.EAST);

        // Cấu hình bảng để chọn một hồ sơ, sắp xếp và xem chi tiết bằng nhấp đúp.
        patientTable.setRowSorter(sorter);
        patientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        patientTable.setRowHeight(42);
        patientTable.setFont(new Font("Arial", Font.PLAIN, 12));
        patientTable.setForeground(INK);
        patientTable.setGridColor(new Color(237, 242, 244));
        patientTable.setShowVerticalLines(false);
        patientTable.setFillsViewportHeight(true);
        patientTable.setIntercellSpacing(new Dimension(0, 0));
        patientTable.setSelectionBackground(new Color(231, 246, 245));
        patientTable.setSelectionForeground(INK);

        // Thiết lập kiểu chữ, nền tiêu đề và độ rộng phù hợp cho từng cột.
        JTableHeader header = patientTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 10));
        header.setForeground(MUTED);
        header.setBackground(new Color(248, 250, 251));
        header.setPreferredSize(new Dimension(0, 38));
        patientTable.getColumnModel().getColumn(0).setPreferredWidth(110);
        patientTable.getColumnModel().getColumn(1).setPreferredWidth(210);
        patientTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        patientTable.getColumnModel().getColumn(3).setPreferredWidth(85);
        patientTable.getColumnModel().getColumn(4).setPreferredWidth(130);
        patientTable.getColumnModel().getColumn(5).setPreferredWidth(140);
        patientTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 && patientTable.getSelectedRow() >= 0) {
                    showPatientDetails();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(patientTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(LINE));
        scrollPane.getViewport().setBackground(Color.WHITE);

        // Gợi ý cách mở hồ sơ để xem các trường thông tin còn lại.
        JLabel hint = new JLabel("Nhấp đúp vào một hồ sơ để xem thông tin hành chính và y tế.");
        hint.setFont(new Font("Arial", Font.PLAIN, 10));
        hint.setForeground(MUTED);

        panel.add(tableHeading, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(hint, BorderLayout.SOUTH);
        return panel;
    }

    // Áp dụng giao diện thống nhất cho nút xóa bộ lọc.
    private void styleSecondaryButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setForeground(INK);
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(10, 13, 10, 13)));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // Tra cứu một chuỗi trên đúng bốn trường tìm kiếm được mô tả trong tài liệu yêu cầu.
    private void searchPatients() {
        String query = normalize(searchField.getText().trim());
        List<Patient> matches = new ArrayList<>();
        // Từ khóa rỗng hiển thị toàn bộ hồ sơ; nếu có từ khóa thì so khớp một phần.
        for (Patient patient : patients) {
            if (query.isEmpty()
                    || normalize(patient.maBenhNhan).contains(query)
                    || normalize(patient.hoTen).contains(query)
                    || normalize(patient.soDienThoai).contains(query)
                    || normalize(patient.soCCCD).contains(query)) {
                matches.add(patient);
            }
        }
        populateTable(matches);
    }

    // Nạp danh sách hồ sơ mới vào bảng và cập nhật trạng thái số lượng kết quả.
    private void populateTable(List<Patient> rows) {
        tableModel.setRowCount(0);
        for (Patient patient : rows) {
            tableModel.addRow(new Object[] {
                    patient.maBenhNhan,
                    patient.hoTen,
                    patient.ngaySinh,
                    patient.gioiTinh,
                    patient.soDienThoai,
                    patient.soCCCD == null ? "—" : patient.soCCCD
            });
        }
        resultCount.setText(rows.isEmpty() ? "Không tìm thấy hồ sơ phù hợp" : rows.size() + " hồ sơ");
    }

    // Hiển thị thông tin hành chính và y tế của hồ sơ được chọn trong hộp thoại.
    private void showPatientDetails() {
        int viewRow = patientTable.getSelectedRow();
        if (viewRow < 0) {
            return;
        }
        int modelRow = patientTable.convertRowIndexToModel(viewRow);
        String patientId = tableModel.getValueAt(modelRow, 0).toString();
        Patient patient = findPatient(patientId);
        if (patient == null) {
            return;
        }

        JTextArea details = new JTextArea(
                "Mã bệnh nhân: " + patient.maBenhNhan + "\n"
                        + "Họ và tên: " + patient.hoTen + "\n"
                        + "Ngày sinh: " + patient.ngaySinh + "\n"
                        + "Giới tính: " + patient.gioiTinh + "\n"
                        + "Số điện thoại: " + patient.soDienThoai + "\n"
                        + "CCCD: " + (patient.soCCCD == null ? "Chưa cập nhật" : patient.soCCCD) + "\n"
                        + "Địa chỉ: " + patient.diaChi + "\n"
                        + "Tiền sử bệnh lý: " + patient.tienSuBenhLy + "\n"
                        + "Thông tin dị ứng: " + patient.thongTinDiUng + "\n"
                        + "Người bảo hộ: " + patient.nguoiBaoHo);
        details.setFont(new Font("Arial", Font.PLAIN, 12));
        details.setForeground(INK);
        details.setEditable(false);
        details.setOpaque(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        details.setBorder(new EmptyBorder(8, 8, 8, 8));
        // Hiện thông tin bệnh nhân trong hộp thoại chỉ đọc.
        JOptionPane.showMessageDialog(
                this,
                details,
                "Chi tiết hồ sơ bệnh nhân",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Lấy đối tượng hồ sơ từ mã bệnh nhân đang được chọn trong bảng.
    private Patient findPatient(String patientId) {
        for (Patient patient : patients) {
            if (patient.maBenhNhan.equals(patientId)) {
                return patient;
            }
        }
        return null;
    }

    // Bỏ dấu khi so khớp để người dùng có thể tìm tên dù không gõ dấu tiếng Việt.
    private String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT);
    }

    // Dữ liệu demo tuân thủ tên cột và quy tắc mã bệnh nhân trong tài liệu OOAD.
    private List<Patient> createSamplePatients() {
        List<Patient> sample = new ArrayList<>();
        sample.add(new Patient(
                "BN000001", "Nguyễn Thị Minh Anh", "1990-05-12", "Nữ", "0901234567", "079090123456",
                "12 Nguyễn Trãi, Quận 1", "Không ghi nhận", "Dị ứng penicillin", "Nguyễn Văn An - 0901111222"));
        sample.add(new Patient(
                "BN000002", "Trần Hoàng Nam", "1985-11-03", "Nam", "0912345678", "079085234567",
                "25 Lê Lợi, Quận 3", "Cao huyết áp", "Chưa ghi nhận", "Trần Thị Hoa - 0902222333"));
        sample.add(new Patient(
                "BN000003", "Lê Ngọc Hà", "2001-02-19", "Nữ", "0987654321", "079201345678",
                "8 Pasteur, Quận 1", "Không ghi nhận", "Dị ứng thuốc tê", "Chưa cần"));
        sample.add(new Patient(
                "BN000004", "Phạm Gia Bảo", "2012-07-24", "Nam", "0934567890", "079212456789",
                "42 Võ Văn Tần, Quận 3", "Hen suyễn", "Dị ứng hải sản", "Phạm Thị Mai - 0903333444"));
        sample.add(new Patient(
                "BN000005", "Đỗ Thanh Bình", "1978-09-15", "Khác", "0978123456", null,
                "17 Hai Bà Trưng, Quận 1", "Tiểu đường type 2", "Chưa ghi nhận", "Chưa cập nhật"));
        return sample;
    }

    // Đối tượng dữ liệu đơn giản ánh xạ các trường hồ sơ bệnh nhân dùng trên màn hình.
    private static final class Patient {
        private final String maBenhNhan;
        private final String hoTen;
        private final String ngaySinh;
        private final String gioiTinh;
        private final String soDienThoai;
        private final String soCCCD;
        private final String diaChi;
        private final String tienSuBenhLy;
        private final String thongTinDiUng;
        private final String nguoiBaoHo;

        // Lưu các trường bệnh nhân để dùng cho kết quả tìm kiếm và cửa sổ chi tiết.
        private Patient(
                String maBenhNhan,
                String hoTen,
                String ngaySinh,
                String gioiTinh,
                String soDienThoai,
                String soCCCD,
                String diaChi,
                String tienSuBenhLy,
                String thongTinDiUng,
                String nguoiBaoHo) {
            this.maBenhNhan = maBenhNhan;
            this.hoTen = hoTen;
            this.ngaySinh = ngaySinh;
            this.gioiTinh = gioiTinh;
            this.soDienThoai = soDienThoai;
            this.soCCCD = soCCCD;
            this.diaChi = diaChi;
            this.tienSuBenhLy = tienSuBenhLy;
            this.thongTinDiUng = thongTinDiUng;
            this.nguoiBaoHo = nguoiBaoHo;
        }
    }
}

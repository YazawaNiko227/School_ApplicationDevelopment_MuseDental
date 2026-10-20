package gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Giao diện Khám lâm sàng và Sơ đồ răng tương tác (UC011, UC012) - Phòng khám
 * Nha khoa Muse.
 * Cho phép Nha sĩ ghi nhận triệu chứng lâm sàng, chẩn đoán bệnh lý, tương tác
 * trực quan
 * trên sơ đồ răng chuẩn quốc tế FDI (người lớn & trẻ em), chỉ định điều trị và
 * in phiếu khám.
 */
public class KhamLamSangVaSoDoRangPanel extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;

    // Bảng màu chuẩn Muse Dental
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color TEAL_LIGHT = new Color(231, 246, 245);
    private static final Color CANVAS = new Color(244, 248, 249);
    private static final Color LINE = new Color(226, 234, 237);
    private static final Color DANGER = new Color(214, 48, 49);
    private static final Color SUCCESS = new Color(39, 174, 96);
    private static final Color WARNING = new Color(230, 126, 34);

    // Bảng màu trạng thái răng
    private static final Color COLOR_NORMAL = new Color(245, 248, 250);
    private static final Color COLOR_CARIES = new Color(255, 118, 117); // Sâu răng (Đỏ cam)
    private static final Color COLOR_PULP = new Color(214, 48, 49); // Viêm tủy (Đỏ đậm)
    private static final Color COLOR_FILLING = new Color(52, 152, 219); // Đã trám (Xanh dương)
    private static final Color COLOR_CROWN = new Color(241, 196, 15); // Bọc sứ (Vàng kim)
    private static final Color COLOR_MISSING = new Color(178, 190, 195); // Mất răng (Xám)
    private static final Color COLOR_WISDOM = new Color(155, 89, 182); // Răng khôn lệch (Tím)
    private static final Color COLOR_IMPLANT = new Color(0, 184, 148); // Implant (Xanh ngọc lục)

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Component thông tin bệnh nhân & hồ sơ
    private JComboBox<String> cboBenhNhan;
    private JTextField txtMaBN, txtTenBN, txtTuoi, txtGioiTinh, txtSDT;
    private JTextField txtMaChanDoan, txtNgayKham, txtMaHoSo;
    private JComboBox<String> cboBacSi;
    private JTextField txtTienSuBenh;
    private JTextField txtDiUng;

    // Component khám lâm sàng
    private JTextArea txtLyDoKham;
    private JTextArea txtTrieuChungMoMem;
    private JTextArea txtKetLuanChanDoan;
    private JTextArea txtHuongDieuTri;

    // Component Sơ đồ răng
    private JComboBox<String> cboLoaiSoDo; // Người lớn vs Trẻ em
    private JPanel pTeethGridContainer;
    private final Map<Integer, ToothButton> toothButtons = new HashMap<>();
    private final Map<Integer, ToothStatus> toothData = new LinkedHashMap<>();
    private int selectedToothNumber = -1;

    // Panel chỉnh sửa chi tiết răng đang chọn
    private JLabel lblCurrentToothTitle;
    private JComboBox<String> cboTinhTrangRang;
    private JCheckBox chkMatNhai, chkMatNgoai, chkMatTrong, chkMatGan, chkMatXa;
    private JComboBox<String> cboHuongXuLy;
    private JTextField txtGhiChuRang;
    private JButton btnCapNhatRang, btnXoaTrangThaiRang, btnDatLaiToanHam;

    // Bảng danh sách các răng có bệnh lý
    private JTable tblRangBenhLy;
    private DefaultTableModel modelTableBenhLy;
    private final String[] colsBenhLy = {
            "Răng số", "Tên răng", "Tình trạng bệnh lý", "Mặt tổn thương", "Chỉ định điều trị", "Ghi chú"
    };

    // Nút hành động chính
    private JButton btnLuuKhamBenh, btnInPhieuKham, btnLamMoi;

    public KhamLamSangVaSoDoRangPanel() {
        setLayout(new BorderLayout());
        setBackground(CANVAS);
        initUI();
        napDuLieuMauBanDau();
    }

    private void initUI() {
        JPanel pMain = new JPanel();
        pMain.setLayout(new BoxLayout(pMain, BoxLayout.Y_AXIS));
        pMain.setBackground(CANVAS);
        pMain.setBorder(new EmptyBorder(12, 16, 16, 16));

        // 1. Header Toolbar
        pMain.add(createHeaderPanel());
        pMain.add(Box.createVerticalStrut(12));

        // 2. Thẻ Thông tin hành chính & Bệnh sử
        pMain.add(createPatientProfilePanel());
        pMain.add(Box.createVerticalStrut(12));

        // 3. Khối Sơ đồ răng tương tác (Teeth Chart Section)
        pMain.add(createTeethChartSection());
        pMain.add(Box.createVerticalStrut(12));

        // 4. Khối Ghi chép Khám lâm sàng & Bảng tổng kết răng bệnh lý
        pMain.add(createClinicalDetailsSection());

        JScrollPane scrollPane = new JScrollPane(pMain);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    /** Header tiêu đề trang */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Khám lâm sàng và sơ đồ răng");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(INK);

        JLabel desc = new JLabel("Ghi nhận triệu chứng khám, chẩn đoán bệnh lý và trực quan hóa sơ đồ răng chuẩn FDI");
        desc.setFont(new Font("Arial", Font.PLAIN, 12));
        desc.setForeground(MUTED);

        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(desc);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        btnLamMoi = createButton("Làm mới (F5)", false);
        btnInPhieuKham = createButton("In phiếu khám lâm sàng", false);
        btnLuuKhamBenh = createButton("Lưu chẩn đoán & Sơ đồ", false);

        btnLamMoi.addActionListener(this);
        btnInPhieuKham.addActionListener(this);
        btnLuuKhamBenh.addActionListener(this);

        right.add(btnLamMoi);
        right.add(btnInPhieuKham);
        right.add(btnLuuKhamBenh);

        panel.add(left, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    /** Thẻ thông tin hành chính, bệnh nhân và tiền sử bệnh */
    private JPanel createPatientProfilePanel() {
        JPanel card = createCardPanel("Thông tin hành chính bệnh nhân & Tiền sử y khoa");
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cboBenhNhan = new JComboBox<>(new String[] {
                "BN001 - Nguyễn Văn An (32 tuổi, Nam)",
                "BN002 - Trần Thị Bình (28 tuổi, Nữ)",
                "BN003 - Lê Hoàng Cường (45 tuổi, Nam)",
                "BN004 - Phạm Minh Dũng (8 tuổi, Bé trai)",
                "BN005 - Đỗ Ánh Nguyệt (52 tuổi, Nữ)"
        });
        styleComboBox(cboBenhNhan);
        cboBenhNhan.addActionListener(e -> capNhatThongTinBenhNhan());

        txtMaChanDoan = createTextField(false);
        txtNgayKham = createTextField(false);
        txtNgayKham.setText(LocalDate.now().format(dtf));

        cboBacSi = new JComboBox<>(new String[] {
                "BS001 - ThS.BS Nguyễn Văn Tuấn (Trưởng khoa)",
                "BS002 - BSCKII Trần Mai Linh (Chỉnh nha & Thẩm mỹ)",
                "BS003 - ThS.BS Lê Minh Trí (Implant & Tiểu phẫu)"
        });
        styleComboBox(cboBacSi);

        txtMaBN = createTextField(false);
        txtTenBN = createTextField(false);
        txtTuoi = createTextField(false);
        txtGioiTinh = createTextField(false);
        txtSDT = createTextField(false);
        txtMaHoSo = createTextField(false);

        txtTienSuBenh = createTextField(true);
        txtTienSuBenh.setText("Không có bệnh lý nền mạn tính");
        txtDiUng = createTextField(true);
        txtDiUng.setText("Không ghi nhận dị ứng thuốc gây tê/kháng sinh");

        // Hàng 0: Chọn bệnh nhân & Mã chẩn đoán
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.12;
        card.add(createFieldLabel("Bệnh nhân:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.38;
        card.add(cboBenhNhan, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.12;
        card.add(createFieldLabel("Mã chẩn đoán:"), gbc);
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 0.38;
        card.add(txtMaChanDoan, gbc);

        // Hàng 1: Chi tiết BN
        gbc.gridx = 0;
        gbc.gridy = 1;
        card.add(createFieldLabel("Họ tên:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        card.add(txtTenBN, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        card.add(createFieldLabel("Bác sĩ khám:"), gbc);
        gbc.gridx = 3;
        gbc.gridy = 1;
        card.add(cboBacSi, gbc);

        // Hàng 2: Tuổi, Giới tính, SDT, Mã EMR
        JPanel pSubBN = new JPanel(new GridLayout(1, 4, 8, 0));
        pSubBN.setOpaque(false);
        pSubBN.add(txtTuoi);
        pSubBN.add(txtGioiTinh);
        pSubBN.add(txtSDT);
        pSubBN.add(txtMaHoSo);

        gbc.gridx = 0;
        gbc.gridy = 2;
        card.add(createFieldLabel("Tuổi/GT/SĐT/EMR:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        card.add(pSubBN, gbc);

        gbc.gridx = 2;
        gbc.gridy = 2;
        card.add(createFieldLabel("Ngày khám:"), gbc);
        gbc.gridx = 3;
        gbc.gridy = 2;
        card.add(txtNgayKham, gbc);

        // Hàng 3: Tiền sử & Dị ứng
        gbc.gridx = 0;
        gbc.gridy = 3;
        card.add(createFieldLabel("Tiền sử bệnh:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        card.add(txtTienSuBenh, gbc);

        gbc.gridx = 2;
        gbc.gridy = 3;
        card.add(createFieldLabel("Dị ứng thuốc:"), gbc);
        gbc.gridx = 3;
        gbc.gridy = 3;
        card.add(txtDiUng, gbc);

        return card;
    }

    /** Khối Sơ đồ răng trực quan FDI (Teeth Chart) */
    private JPanel createTeethChartSection() {
        JPanel card = createCardPanel("Sơ đồ răng tương tác (Interactive Teeth Chart FDI)");
        card.setLayout(new BorderLayout(0, 10));

        // Thanh công cụ trên sơ đồ răng: Chế độ răng + Chú thích màu
        JPanel pTopBar = new JPanel(new BorderLayout());
        pTopBar.setOpaque(false);

        JPanel pMode = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pMode.setOpaque(false);
        cboLoaiSoDo = new JComboBox<>(new String[] {
                "Sơ đồ 32 Răng Vĩnh Viễn (Người lớn)",
                "Sơ đồ 20 Răng Sữa (Trẻ em)"
        });
        styleComboBox(cboLoaiSoDo);
        cboLoaiSoDo.addActionListener(e -> thayDoiLoaiSoDo());
        pMode.add(createFieldLabel("Bộ răng:"));
        pMode.add(cboLoaiSoDo);

        // Bảng chú thích màu sắc
        JPanel pLegend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pLegend.setOpaque(false);
        pLegend.add(createLegendBadge("Bình thường", COLOR_NORMAL, INK));
        pLegend.add(createLegendBadge("Sâu răng", COLOR_CARIES, Color.WHITE));
        pLegend.add(createLegendBadge("Viêm tủy", COLOR_PULP, Color.WHITE));
        pLegend.add(createLegendBadge("Đã trám", COLOR_FILLING, Color.WHITE));
        pLegend.add(createLegendBadge("Bọc sứ", COLOR_CROWN, INK));
        pLegend.add(createLegendBadge("Mất răng", COLOR_MISSING, Color.WHITE));
        pLegend.add(createLegendBadge("Răng khôn lệch", COLOR_WISDOM, Color.WHITE));
        pLegend.add(createLegendBadge("Implant", COLOR_IMPLANT, Color.WHITE));

        pTopBar.add(pMode, BorderLayout.WEST);
        pTopBar.add(pLegend, BorderLayout.EAST);

        // Vùng đồ họa hiển thị các hàm răng
        pTeethGridContainer = new JPanel();
        pTeethGridContainer.setBackground(Color.WHITE);
        pTeethGridContainer.setLayout(new BoxLayout(pTeethGridContainer, BoxLayout.Y_AXIS));
        pTeethGridContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(12, 16, 12, 16)));

        xayDungSoDoRangNguoiLon();

        // Vùng điều khiển chi tiết cho răng đang được chọn
        JPanel pToothDetailControl = createToothDetailControlPanel();

        card.add(pTopBar, BorderLayout.NORTH);
        card.add(pTeethGridContainer, BorderLayout.CENTER);
        card.add(pToothDetailControl, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createLegendBadge(String label, Color bg, Color fg) {
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 2));
        badge.setOpaque(false);

        JLabel colorBox = new JLabel("  ");
        colorBox.setOpaque(true);
        colorBox.setBackground(bg);
        colorBox.setBorder(BorderFactory.createLineBorder(new Color(180, 190, 195)));
        colorBox.setPreferredSize(new Dimension(14, 14));

        JLabel text = new JLabel(label);
        text.setFont(new Font("Arial", Font.PLAIN, 10));
        text.setForeground(MUTED);

        badge.add(colorBox);
        badge.add(text);
        return badge;
    }

    /** Xây dựng giao diện 32 răng vĩnh viễn chuẩn FDI */
    private void xayDungSoDoRangNguoiLon() {
        pTeethGridContainer.removeAll();
        toothButtons.clear();

        // 1. Cung hàm trên (Upper Arch: Maxillary)
        JLabel lblUpper = new JLabel("HÀM TRÊN (CUNG 1 & CUNG 2)", SwingConstants.CENTER);
        lblUpper.setFont(new Font("Arial", Font.BOLD, 11));
        lblUpper.setForeground(TEAL);
        lblUpper.setAlignmentX(CENTER_ALIGNMENT);

        JPanel pUpperRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
        pUpperRow.setOpaque(false);

        // Cung 1 (Phải): 18 -> 11
        for (int i = 18; i >= 11; i--) {
            ToothButton tb = new ToothButton(i, getTenRang(i));
            toothButtons.put(i, tb);
            pUpperRow.add(tb);
        }

        // Vạch phân cách hàm trên
        JLabel sepUpper = new JLabel(" | ");
        sepUpper.setFont(new Font("Arial", Font.BOLD, 18));
        sepUpper.setForeground(LINE);
        pUpperRow.add(sepUpper);

        // Cung 2 (Trái): 21 -> 28
        for (int i = 21; i <= 28; i++) {
            ToothButton tb = new ToothButton(i, getTenRang(i));
            toothButtons.put(i, tb);
            pUpperRow.add(tb);
        }

        // Đường ranh giới giữa 2 hàm
        JPanel pDivider = new JPanel();
        pDivider.setBackground(LINE);
        pDivider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // 2. Cung hàm dưới (Lower Arch: Mandibular)
        JLabel lblLower = new JLabel("HÀM DƯỚI (CUNG 4 & CUNG 3)", SwingConstants.CENTER);
        lblLower.setFont(new Font("Arial", Font.BOLD, 11));
        lblLower.setForeground(TEAL);
        lblLower.setAlignmentX(CENTER_ALIGNMENT);

        JPanel pLowerRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
        pLowerRow.setOpaque(false);

        // Cung 4 (Phải): 48 -> 41
        for (int i = 48; i >= 41; i--) {
            ToothButton tb = new ToothButton(i, getTenRang(i));
            toothButtons.put(i, tb);
            pLowerRow.add(tb);
        }

        // Vạch phân cách hàm dưới
        JLabel sepLower = new JLabel(" | ");
        sepLower.setFont(new Font("Arial", Font.BOLD, 18));
        sepLower.setForeground(LINE);
        pLowerRow.add(sepLower);

        // Cung 3 (Trái): 31 -> 38
        for (int i = 31; i <= 38; i++) {
            ToothButton tb = new ToothButton(i, getTenRang(i));
            toothButtons.put(i, tb);
            pLowerRow.add(tb);
        }

        pTeethGridContainer.add(lblUpper);
        pTeethGridContainer.add(Box.createVerticalStrut(4));
        pTeethGridContainer.add(pUpperRow);
        pTeethGridContainer.add(Box.createVerticalStrut(8));
        pTeethGridContainer.add(pDivider);
        pTeethGridContainer.add(Box.createVerticalStrut(8));
        pTeethGridContainer.add(pLowerRow);
        pTeethGridContainer.add(Box.createVerticalStrut(4));
        pTeethGridContainer.add(lblLower);

        dongBoMauTuToothData();
        pTeethGridContainer.revalidate();
        pTeethGridContainer.repaint();
    }

    /** Xây dựng giao diện 20 răng sữa chuẩn FDI */
    private void xayDungSoDoRangTreEm() {
        pTeethGridContainer.removeAll();
        toothButtons.clear();

        JLabel lblUpper = new JLabel("HÀM TRÊN RĂNG SỮA (CUNG 5 & CUNG 6)", SwingConstants.CENTER);
        lblUpper.setFont(new Font("Arial", Font.BOLD, 11));
        lblUpper.setForeground(TEAL);
        lblUpper.setAlignmentX(CENTER_ALIGNMENT);

        JPanel pUpperRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        pUpperRow.setOpaque(false);

        // 55 -> 51
        for (int i = 55; i >= 51; i--) {
            ToothButton tb = new ToothButton(i, getTenRangTreEm(i));
            toothButtons.put(i, tb);
            pUpperRow.add(tb);
        }

        JLabel sepUpper = new JLabel(" | ");
        sepUpper.setFont(new Font("Arial", Font.BOLD, 18));
        sepUpper.setForeground(LINE);
        pUpperRow.add(sepUpper);

        // 61 -> 65
        for (int i = 61; i <= 65; i++) {
            ToothButton tb = new ToothButton(i, getTenRangTreEm(i));
            toothButtons.put(i, tb);
            pUpperRow.add(tb);
        }

        JPanel pDivider = new JPanel();
        pDivider.setBackground(LINE);
        pDivider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        JLabel lblLower = new JLabel("HÀM DƯỚI RĂNG SỮA (CUNG 8 & CUNG 7)", SwingConstants.CENTER);
        lblLower.setFont(new Font("Arial", Font.BOLD, 11));
        lblLower.setForeground(TEAL);
        lblLower.setAlignmentX(CENTER_ALIGNMENT);

        JPanel pLowerRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        pLowerRow.setOpaque(false);

        // 85 -> 81
        for (int i = 85; i >= 81; i--) {
            ToothButton tb = new ToothButton(i, getTenRangTreEm(i));
            toothButtons.put(i, tb);
            pLowerRow.add(tb);
        }

        JLabel sepLower = new JLabel(" | ");
        sepLower.setFont(new Font("Arial", Font.BOLD, 18));
        sepLower.setForeground(LINE);
        pLowerRow.add(sepLower);

        // 71 -> 75
        for (int i = 71; i <= 75; i++) {
            ToothButton tb = new ToothButton(i, getTenRangTreEm(i));
            toothButtons.put(i, tb);
            pLowerRow.add(tb);
        }

        pTeethGridContainer.add(lblUpper);
        pTeethGridContainer.add(Box.createVerticalStrut(4));
        pTeethGridContainer.add(pUpperRow);
        pTeethGridContainer.add(Box.createVerticalStrut(8));
        pTeethGridContainer.add(pDivider);
        pTeethGridContainer.add(Box.createVerticalStrut(8));
        pTeethGridContainer.add(pLowerRow);
        pTeethGridContainer.add(Box.createVerticalStrut(4));
        pTeethGridContainer.add(lblLower);

        dongBoMauTuToothData();
        pTeethGridContainer.revalidate();
        pTeethGridContainer.repaint();
    }

    private void thayDoiLoaiSoDo() {
        if (cboLoaiSoDo.getSelectedIndex() == 0) {
            xayDungSoDoRangNguoiLon();
        } else {
            xayDungSoDoRangTreEm();
        }
    }

    /** Panel chi tiết thao tác chẩn đoán khi click vào 1 răng */
    private JPanel createToothDetailControlPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 6));
        panel.setBackground(new Color(248, 251, 252));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(8, 10, 8, 10)));

        lblCurrentToothTitle = new JLabel(
                "Chưa chọn răng nào trên sơ đồ. Vui lòng bấm vào một răng để cập nhật bệnh lý.");
        lblCurrentToothTitle.setFont(new Font("Arial", Font.BOLD, 12));
        lblCurrentToothTitle.setForeground(TEAL);

        JPanel pForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 3));
        pForm.setOpaque(false);

        cboTinhTrangRang = new JComboBox<>(new String[] {
                "Bình thường (Khỏe mạnh)",
                "Sâu men / Sâu ngà (Caries)",
                "Viêm tủy cấp / mạn (Pulpitis)",
                "Đã trám thẩm mỹ (Restored)",
                "Bọc răng sứ Cercon (Crown)",
                "Mất răng (Missing)",
                "Răng khôn mọc lệch / Ngầm (Impacted)",
                "Cấy ghép trụ Implant"
        });
        styleComboBox(cboTinhTrangRang);
        cboTinhTrangRang.setPreferredSize(new Dimension(145, 28));

        chkMatNhai = new JCheckBox("O");
        chkMatNhai.setToolTipText("Mặt nhai (Occlusal)");
        chkMatNgoai = new JCheckBox("B");
        chkMatNgoai.setToolTipText("Mặt ngoài (Buccal)");
        chkMatTrong = new JCheckBox("L");
        chkMatTrong.setToolTipText("Mặt trong (Lingual)");
        chkMatGan = new JCheckBox("M");
        chkMatGan.setToolTipText("Mặt gần (Mesial)");
        chkMatXa = new JCheckBox("D");
        chkMatXa.setToolTipText("Mặt xa (Distal)");

        for (JCheckBox chk : new JCheckBox[] { chkMatNhai, chkMatNgoai, chkMatTrong, chkMatGan, chkMatXa }) {
            chk.setOpaque(false);
            chk.setFont(new Font("Arial", Font.BOLD, 11));
            chk.setForeground(INK);
            chk.setMargin(new Insets(0, 1, 0, 1));
        }

        cboHuongXuLy = new JComboBox<>(new String[] {
                "Theo dõi định kỳ",
                "Trám răng Composite",
                "Điều trị tủy răng & phục hình",
                "Nhổ răng khôn / Tiểu phẫu",
                "Bọc răng sứ cao cấp",
                "Cấy ghép Implant",
                "Tẩy trắng răng"
        });
        styleComboBox(cboHuongXuLy);
        cboHuongXuLy.setPreferredSize(new Dimension(135, 28));

        txtGhiChuRang = createTextField(true);
        txtGhiChuRang.setPreferredSize(new Dimension(85, 28));

        btnCapNhatRang = new JButton("Áp dụng cho răng");
        btnCapNhatRang.setFont(new Font("Arial", Font.BOLD, 11));
        btnCapNhatRang.setForeground(Color.BLACK);
        btnCapNhatRang.setBackground(new Color(231, 246, 245));
        btnCapNhatRang.setFocusPainted(false);
        btnCapNhatRang.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCapNhatRang.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TEAL, 1),
                new EmptyBorder(5, 8, 5, 8)));
        btnCapNhatRang.addActionListener(e -> capNhatTrangThaiChoRangDangChon());

        btnXoaTrangThaiRang = createButton("Đặt lại", false);
        btnXoaTrangThaiRang.setFont(new Font("Arial", Font.PLAIN, 11));
        btnXoaTrangThaiRang.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(5, 8, 5, 8)));
        btnXoaTrangThaiRang.addActionListener(e -> xoaTrangThaiRangDangChon());

        btnDatLaiToanHam = createButton("Đặt lại toàn hàm", false);
        btnDatLaiToanHam.setFont(new Font("Arial", Font.PLAIN, 11));
        btnDatLaiToanHam.setForeground(DANGER);
        btnDatLaiToanHam.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(5, 8, 5, 8)));
        btnDatLaiToanHam.addActionListener(e -> datLaiToanHamVeBinhThuong());

        pForm.add(createFieldLabel("Tình trạng:"));
        pForm.add(cboTinhTrangRang);
        pForm.add(createFieldLabel("Mặt:"));
        pForm.add(chkMatNhai);
        pForm.add(chkMatNgoai);
        pForm.add(chkMatTrong);
        pForm.add(chkMatGan);
        pForm.add(chkMatXa);
        pForm.add(createFieldLabel("Chỉ định:"));
        pForm.add(cboHuongXuLy);
        pForm.add(createFieldLabel("Ghi chú:"));
        pForm.add(txtGhiChuRang);
        pForm.add(btnCapNhatRang);
        pForm.add(btnXoaTrangThaiRang);
        pForm.add(btnDatLaiToanHam);

        panel.add(lblCurrentToothTitle, BorderLayout.NORTH);
        panel.add(pForm, BorderLayout.CENTER);
        return panel;
    }

    /** Khối Ghi chép Khám lâm sàng & Bảng răng bệnh lý */
    private JPanel createClinicalDetailsSection() {
        JPanel card = createCardPanel("Khám lâm sàng EMR & Danh mục răng bệnh lý cần điều trị");
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // --- CỘT TRÁI: GHI CHÉP KHÁM LÂM SÀNG (62% CHIỀU RỘNG) ---
        JPanel pExamNotes = new JPanel();
        pExamNotes.setOpaque(false);
        pExamNotes.setLayout(new BoxLayout(pExamNotes, BoxLayout.Y_AXIS));

        txtLyDoKham = createTextArea(2);
        txtTrieuChungMoMem = createTextArea(2);
        txtKetLuanChanDoan = createTextArea(2);
        txtHuongDieuTri = createTextArea(2);

        txtLyDoKham.setText("Đau buốt nhức nhiều răng hàm dưới bên trái khi ăn nhai đồ nóng/lạnh.");
        txtTrieuChungMoMem
                .setText("Nướu viền R36 cương sưng nhẹ, sờ đau. Niêm mạc miệng hồng hào, khớp cắn chuẩn Class I.");
        txtKetLuanChanDoan
                .setText("Viêm tủy cấp không hồi phục R36; Sâu ngà sâu R46; Răng khôn R38 mọc lệch nghiêng gần 45 độ.");
        txtHuongDieuTri.setText(
                "1. Chữa tủy R36 (3 ống). 2. Trám thẩm mỹ Composite R46. 3. Chỉ định chụp Panorama & nhổ R38.");

        pExamNotes.add(createFieldLabel("1. Lý do đến khám:"));
        pExamNotes.add(new JScrollPane(txtLyDoKham));
        pExamNotes.add(Box.createVerticalStrut(6));

        pExamNotes.add(createFieldLabel("2. Khám triệu chứng cơ năng & mô mềm (Nướu, niêm mạc, khớp cắn):"));
        pExamNotes.add(new JScrollPane(txtTrieuChungMoMem));
        pExamNotes.add(Box.createVerticalStrut(6));

        pExamNotes.add(createFieldLabel("3. Kết luận chẩn đoán bệnh lý tổng quát:"));
        pExamNotes.add(new JScrollPane(txtKetLuanChanDoan));
        pExamNotes.add(Box.createVerticalStrut(6));

        pExamNotes.add(createFieldLabel("4. Hướng xử lý & Phác đồ điều trị đề xuất:"));
        pExamNotes.add(new JScrollPane(txtHuongDieuTri));

        // --- CỘT PHẢI: BẢNG RĂNG BỆNH LÝ (38% CHIỀU RỘNG - THU NHỎ LẠI) ---
        JPanel pTableContainer = new JPanel(new BorderLayout(0, 6));
        pTableContainer.setOpaque(false);

        JLabel lblTableTitle = new JLabel("Danh mục răng tổn thương / cần can thiệp:");
        lblTableTitle.setFont(new Font("Arial", Font.BOLD, 11));
        lblTableTitle.setForeground(INK);

        modelTableBenhLy = new DefaultTableModel(colsBenhLy, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblRangBenhLy = new JTable(modelTableBenhLy);
        tblRangBenhLy.setRowHeight(28);
        tblRangBenhLy.setFont(new Font("Arial", Font.PLAIN, 12));
        tblRangBenhLy.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblRangBenhLy.setGridColor(LINE);

        JTableHeader th = tblRangBenhLy.getTableHeader();
        th.setFont(new Font("Arial", Font.BOLD, 12));
        th.setBackground(TEAL_LIGHT);
        th.setForeground(INK);

        DefaultTableCellRenderer centerR = new DefaultTableCellRenderer();
        centerR.setHorizontalAlignment(JLabel.CENTER);
        tblRangBenhLy.getColumnModel().getColumn(0).setPreferredWidth(45);
        tblRangBenhLy.getColumnModel().getColumn(0).setCellRenderer(centerR);
        tblRangBenhLy.getColumnModel().getColumn(1).setPreferredWidth(95);
        tblRangBenhLy.getColumnModel().getColumn(2).setPreferredWidth(95);
        tblRangBenhLy.getColumnModel().getColumn(3).setPreferredWidth(45);
        tblRangBenhLy.getColumnModel().getColumn(3).setCellRenderer(centerR);
        tblRangBenhLy.getColumnModel().getColumn(4).setPreferredWidth(105);

        JScrollPane scpTable = new JScrollPane(tblRangBenhLy);
        scpTable.setBorder(BorderFactory.createLineBorder(LINE));

        pTableContainer.add(lblTableTitle, BorderLayout.NORTH);
        pTableContainer.add(scpTable, BorderLayout.CENTER);

        gbc.insets = new Insets(0, 0, 0, 12);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.62;
        card.add(pExamNotes, gbc);

        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.38;
        card.add(pTableContainer, gbc);
        return card;
    }

    // ==========================================
    // LOGIC NGHIỆP VỤ & TƯƠNG TÁC RĂNG
    // ==========================================

    private void napDuLieuMauBanDau() {
        txtMaChanDoan.setText("CD" + (System.currentTimeMillis() % 10000));
        capNhatThongTinBenhNhan();

        // Ghi nhận một vài răng bệnh lý mẫu để sơ đồ trực quan và đẹp mắt
        ToothStatus ts36 = new ToothStatus(36, "Viêm tủy cấp / mạn (Pulpitis)", "Nhai (O), Gần (M)",
                "Điều trị tủy răng & phục hình", "Lộ tủy, đau nhức");
        ToothStatus ts46 = new ToothStatus(46, "Sâu men / Sâu ngà (Caries)", "Nhai (O)", "Trám răng thẩm mỹ Composite",
                "Sâu men mặt nhai");
        ToothStatus ts38 = new ToothStatus(38, "Răng khôn mọc lệch / Ngầm (Impacted)", "Toàn thân",
                "Nhổ răng khôn / Tiểu phẫu", "Lệch gần 45 độ kẹt R37");
        ToothStatus ts24 = new ToothStatus(24, "Mất răng (Missing)", "Toàn răng", "Cấy ghép Implant & Abutment",
                "Mất răng cách 1 năm");

        toothData.put(36, ts36);
        toothData.put(46, ts46);
        toothData.put(38, ts38);
        toothData.put(24, ts24);

        dongBoMauTuToothData();
        capNhatBangRangBenhLy();

        // Mặc định chọn răng 36 để hiện chi tiết
        chonRang(36);
    }

    private void capNhatThongTinBenhNhan() {
        int idx = cboBenhNhan.getSelectedIndex();
        if (idx == 0) {
            txtMaBN.setText("BN001");
            txtTenBN.setText("Nguyễn Văn An");
            txtTuoi.setText("32 tuổi");
            txtGioiTinh.setText("Nam");
            txtSDT.setText("0908123456");
            txtMaHoSo.setText("HS001");
        } else if (idx == 1) {
            txtMaBN.setText("BN002");
            txtTenBN.setText("Trần Thị Bình");
            txtTuoi.setText("28 tuổi");
            txtGioiTinh.setText("Nữ");
            txtSDT.setText("0912345678");
            txtMaHoSo.setText("HS002");
        } else if (idx == 2) {
            txtMaBN.setText("BN003");
            txtTenBN.setText("Lê Hoàng Cường");
            txtTuoi.setText("45 tuổi");
            txtGioiTinh.setText("Nam");
            txtSDT.setText("0987654321");
            txtMaHoSo.setText("HS003");
        } else if (idx == 3) {
            txtMaBN.setText("BN004");
            txtTenBN.setText("Phạm Minh Dũng");
            txtTuoi.setText("8 tuổi");
            txtGioiTinh.setText("Nam");
            txtSDT.setText("0934567890");
            txtMaHoSo.setText("HS004");
            cboLoaiSoDo.setSelectedIndex(1); // Chuyển sang răng trẻ em
        } else {
            txtMaBN.setText("BN005");
            txtTenBN.setText("Đỗ Ánh Nguyệt");
            txtTuoi.setText("52 tuổi");
            txtGioiTinh.setText("Nữ");
            txtSDT.setText("0945678901");
            txtMaHoSo.setText("HS005");
        }
    }

    public void chonRang(int toothNumber) {
        this.selectedToothNumber = toothNumber;

        // Bỏ highlight răng cũ và highlight răng mới
        for (ToothButton tb : toothButtons.values()) {
            tb.setSelected(tb.toothNumber == toothNumber);
        }

        String ten = getTenRang(toothNumber);
        lblCurrentToothTitle.setText("RĂNG ĐANG CHỌN: RĂNG " + toothNumber + " - " + ten);

        ToothStatus ts = toothData.get(toothNumber);
        if (ts != null) {
            cboTinhTrangRang.setSelectedItem(ts.tinhTrang);
            cboHuongXuLy.setSelectedItem(ts.huongXuLy);
            txtGhiChuRang.setText(ts.ghiChu);

            chkMatNhai.setSelected(ts.matRang.contains("Nhai"));
            chkMatNgoai.setSelected(ts.matRang.contains("Ngoài"));
            chkMatTrong.setSelected(ts.matRang.contains("Trong"));
            chkMatGan.setSelected(ts.matRang.contains("Gần"));
            chkMatXa.setSelected(ts.matRang.contains("Xa"));
        } else {
            cboTinhTrangRang.setSelectedIndex(0); // Bình thường
            cboHuongXuLy.setSelectedIndex(0);
            txtGhiChuRang.setText("");
            chkMatNhai.setSelected(false);
            chkMatNgoai.setSelected(false);
            chkMatTrong.setSelected(false);
            chkMatGan.setSelected(false);
            chkMatXa.setSelected(false);
        }
    }

    private void capNhatTrangThaiChoRangDangChon() {
        if (selectedToothNumber == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng bấm chọn một răng trên sơ đồ trước khi áp dụng.",
                    "Chưa chọn răng", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tinhTrang = (String) cboTinhTrangRang.getSelectedItem();
        if (tinhTrang == null || tinhTrang.startsWith("Bình thường")) {
            toothData.remove(selectedToothNumber);
        } else {
            StringBuilder sbMat = new StringBuilder();
            if (chkMatNhai.isSelected())
                sbMat.append("O, ");
            if (chkMatNgoai.isSelected())
                sbMat.append("B, ");
            if (chkMatTrong.isSelected())
                sbMat.append("L, ");
            if (chkMatGan.isSelected())
                sbMat.append("M, ");
            if (chkMatXa.isSelected())
                sbMat.append("D, ");
            String matRang = sbMat.length() > 0 ? sbMat.substring(0, sbMat.length() - 2) : "Toàn răng";

            String huongXuLy = (String) cboHuongXuLy.getSelectedItem();
            String ghiChu = txtGhiChuRang.getText().trim();

            ToothStatus ts = new ToothStatus(selectedToothNumber, tinhTrang, matRang, huongXuLy, ghiChu);
            toothData.put(selectedToothNumber, ts);
        }

        dongBoMauTuToothData();
        capNhatBangRangBenhLy();
    }

    private void xoaTrangThaiRangDangChon() {
        if (selectedToothNumber == -1)
            return;
        toothData.remove(selectedToothNumber);
        chonRang(selectedToothNumber);
        dongBoMauTuToothData();
        capNhatBangRangBenhLy();
    }

    private void datLaiToanHamVeBinhThuong() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn đặt lại toàn bộ sơ đồ răng về trạng thái bình thường?",
                "Xác nhận đặt lại sơ đồ răng", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            toothData.clear();
            dongBoMauTuToothData();
            capNhatBangRangBenhLy();
            if (selectedToothNumber != -1)
                chonRang(selectedToothNumber);
        }
    }

    private void dongBoMauTuToothData() {
        for (Map.Entry<Integer, ToothButton> entry : toothButtons.entrySet()) {
            int num = entry.getKey();
            ToothButton btn = entry.getValue();
            ToothStatus ts = toothData.get(num);

            if (ts == null) {
                btn.setStatusColor(COLOR_NORMAL);
                btn.setSubtitle("BT");
            } else {
                String tt = ts.tinhTrang;
                if (tt.contains("Sâu")) {
                    btn.setStatusColor(COLOR_CARIES);
                    btn.setSubtitle("Sâu");
                } else if (tt.contains("Viêm tủy")) {
                    btn.setStatusColor(COLOR_PULP);
                    btn.setSubtitle("Tủy");
                } else if (tt.contains("Đã trám")) {
                    btn.setStatusColor(COLOR_FILLING);
                    btn.setSubtitle("Trám");
                } else if (tt.contains("Bọc răng sứ")) {
                    btn.setStatusColor(COLOR_CROWN);
                    btn.setSubtitle("Sứ");
                } else if (tt.contains("Mất răng")) {
                    btn.setStatusColor(COLOR_MISSING);
                    btn.setSubtitle("Mất");
                } else if (tt.contains("Răng khôn")) {
                    btn.setStatusColor(COLOR_WISDOM);
                    btn.setSubtitle("Lệch");
                } else if (tt.contains("Implant")) {
                    btn.setStatusColor(COLOR_IMPLANT);
                    btn.setSubtitle("Imp");
                } else {
                    btn.setStatusColor(COLOR_NORMAL);
                    btn.setSubtitle("BT");
                }
            }
        }
    }

    private void capNhatBangRangBenhLy() {
        modelTableBenhLy.setRowCount(0);
        for (ToothStatus ts : toothData.values()) {
            modelTableBenhLy.addRow(new Object[] {
                    "R" + ts.toothNumber,
                    getTenRang(ts.toothNumber),
                    ts.tinhTrang.split("\\(")[0].trim(),
                    ts.matRang,
                    ts.huongXuLy,
                    ts.ghiChu
            });
        }
    }

    private void thucHienLuuKhamBenh() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Lưu kết quả khám lâm sàng & Sơ đồ răng cho bệnh nhân " + txtTenBN.getText() + " (" + txtMaBN.getText()
                        + ")?\n" +
                        "- Bác sĩ: " + cboBacSi.getSelectedItem().toString().split("-")[1].trim() + "\n" +
                        "- Số lượng răng bệnh lý ghi nhận: " + toothData.size() + " răng\n\n" +
                        "Dữ liệu sẽ được cập nhật đồng bộ vào Hồ sơ bệnh án EMR.",
                "Lưu bệnh án lâm sàng", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this,
                    "Đã lưu thành công hồ sơ khám lâm sàng mã chẩn đoán " + txtMaChanDoan.getText() + "!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void moDialogInPhieuKham() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, "In phiếu khám lâm sàng & Sơ đồ răng - Muse Dental",
                JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(560, 720);
        dialog.setLocationRelativeTo(parentWindow);
        dialog.setLayout(new BorderLayout());

        JPanel pDoc = new JPanel();
        pDoc.setBackground(Color.WHITE);
        pDoc.setBorder(new EmptyBorder(24, 28, 24, 28));
        pDoc.setLayout(new BoxLayout(pDoc, BoxLayout.Y_AXIS));

        JLabel h1 = new JLabel("PHÒNG KHÁM CHUYÊN KHOA RĂNG HÀM MẶT MUSE", SwingConstants.CENTER);
        h1.setFont(new Font("Arial", Font.BOLD, 14));
        h1.setForeground(INK);
        h1.setAlignmentX(CENTER_ALIGNMENT);

        JLabel h2 = new JLabel("Địa chỉ: 12 Nguyễn Văn Bảo, Phường 4, Gò Vấp · Hotline: 1900 6868",
                SwingConstants.CENTER);
        h2.setFont(new Font("Arial", Font.PLAIN, 10));
        h2.setForeground(MUTED);
        h2.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sep1 = new JLabel(
                "--------------------------------------------------------------------------------------------------");
        sep1.setForeground(LINE);
        sep1.setAlignmentX(CENTER_ALIGNMENT);

        JLabel titleDoc = new JLabel("PHIẾU KHÁM LÂM SÀNG & SƠ ĐỒ RĂNG ĐIỆN TỬ", SwingConstants.CENTER);
        titleDoc.setFont(new Font("Arial", Font.BOLD, 15));
        titleDoc.setForeground(TEAL);
        titleDoc.setAlignmentX(CENTER_ALIGNMENT);

        pDoc.add(h1);
        pDoc.add(Box.createVerticalStrut(4));
        pDoc.add(h2);
        pDoc.add(Box.createVerticalStrut(8));
        pDoc.add(sep1);
        pDoc.add(Box.createVerticalStrut(8));
        pDoc.add(titleDoc);
        pDoc.add(Box.createVerticalStrut(12));

        // Thông tin hành chính
        JPanel pInfo = new JPanel(new GridLayout(4, 2, 8, 4));
        pInfo.setBackground(Color.WHITE);
        pInfo.add(new JLabel("Mã chẩn đoán: " + txtMaChanDoan.getText()));
        pInfo.add(new JLabel("Ngày khám: " + txtNgayKham.getText()));
        pInfo.add(new JLabel("Bệnh nhân: " + txtTenBN.getText()));
        pInfo.add(new JLabel(
                "Mã BN: " + txtMaBN.getText() + " (" + txtTuoi.getText() + " - " + txtGioiTinh.getText() + ")"));
        pInfo.add(new JLabel("Số điện thoại: " + txtSDT.getText()));
        pInfo.add(new JLabel("Mã EMR: " + txtMaHoSo.getText()));
        pInfo.add(new JLabel("Bác sĩ khám: " + cboBacSi.getSelectedItem().toString().split("-")[1].trim()));
        pInfo.add(new JLabel("Dị ứng: " + txtDiUng.getText()));
        pDoc.add(pInfo);
        pDoc.add(Box.createVerticalStrut(12));

        // Khám lâm sàng
        JPanel pClinical = new JPanel(new GridLayout(4, 1, 0, 4));
        pClinical.setBackground(Color.WHITE);
        pClinical.add(new JLabel("<html><b>1. Lý do khám:</b> " + txtLyDoKham.getText() + "</html>"));
        pClinical.add(new JLabel("<html><b>2. Triệu chứng cơ năng:</b> " + txtTrieuChungMoMem.getText() + "</html>"));
        pClinical.add(new JLabel("<html><b>3. Chẩn đoán:</b> " + txtKetLuanChanDoan.getText() + "</html>"));
        pClinical.add(new JLabel("<html><b>4. Phác đồ:</b> " + txtHuongDieuTri.getText() + "</html>"));
        pDoc.add(pClinical);
        pDoc.add(Box.createVerticalStrut(12));

        // Bảng răng bệnh lý tóm tắt
        JLabel lblChartSum = new JLabel("<html><b>5. Tình trạng tổn thương trên sơ đồ răng:</b></html>");
        pDoc.add(lblChartSum);
        pDoc.add(Box.createVerticalStrut(6));

        String[] pCols = { "Răng", "Tên răng", "Bệnh lý", "Mặt", "Chỉ định" };
        DefaultTableModel pModel = new DefaultTableModel(pCols, 0);
        for (ToothStatus ts : toothData.values()) {
            pModel.addRow(new Object[] {
                    "R" + ts.toothNumber,
                    getTenRang(ts.toothNumber),
                    ts.tinhTrang.split("\\(")[0].trim(),
                    ts.matRang,
                    ts.huongXuLy
            });
        }
        JTable pTable = new JTable(pModel);
        pTable.setFont(new Font("Arial", Font.PLAIN, 11));
        pTable.setRowHeight(22);
        JScrollPane scp = new JScrollPane(pTable);
        scp.setPreferredSize(new Dimension(500, 130));
        pDoc.add(scp);
        pDoc.add(Box.createVerticalStrut(16));

        // Chữ ký
        JPanel sign = new JPanel(new GridLayout(1, 2));
        sign.setBackground(Color.WHITE);
        JLabel s1 = new JLabel(
                "<html><center><b>BỆNH NHÂN / NGƯỜI GIÁM HỘ</b><br><i>(Ký xác nhận thông tin)</i></center></html>",
                SwingConstants.CENTER);
        JLabel s2 = new JLabel(
                "<html><center><b>BÁC SĨ NHA KHOA ĐIỀU TRỊ</b><br><i>(Ký và ghi rõ họ tên)</i></center></html>",
                SwingConstants.CENTER);
        sign.add(s1);
        sign.add(s2);
        pDoc.add(sign);

        // Footer buttons
        JPanel pFoot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnPrint = createButton("In phiếu khám (Print)", true);
        JButton btnClose = createButton("Đóng", false);
        btnPrint.addActionListener(e -> {
            JOptionPane.showMessageDialog(dialog, "Lệnh in phiếu khám đã gửi tới máy in hồ sơ bệnh án!", "Máy in",
                    JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });
        btnClose.addActionListener(e -> dialog.dispose());
        pFoot.add(btnPrint);
        pFoot.add(btnClose);

        dialog.add(new JScrollPane(pDoc), BorderLayout.CENTER);
        dialog.add(pFoot, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void lamMoiForm() {
        txtMaChanDoan.setText("CD" + (System.currentTimeMillis() % 10000));
        cboBenhNhan.setSelectedIndex(0);
        toothData.clear();
        dongBoMauTuToothData();
        capNhatBangRangBenhLy();
        selectedToothNumber = -1;
        lblCurrentToothTitle.setText("Chưa chọn răng nào trên sơ đồ. Vui lòng bấm vào một răng để cập nhật bệnh lý.");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src.equals(btnLamMoi)) {
            lamMoiForm();
        } else if (src.equals(btnInPhieuKham)) {
            moDialogInPhieuKham();
        } else if (src.equals(btnLuuKhamBenh)) {
            thucHienLuuKhamBenh();
        }
    }

    // ==========================================
    // TOOTH BUTTON CUSTOM COMPONENT (NÚT VẼ RĂNG)
    // ==========================================

    private final class ToothButton extends JButton {
        private final int toothNumber;
        private final String toothName;
        private Color statusColor = COLOR_NORMAL;
        private String subtitle = "BT";
        private boolean isCurrentSelected = false;

        public ToothButton(int toothNumber, String toothName) {
            this.toothNumber = toothNumber;
            this.toothName = toothName;

            setPreferredSize(new Dimension(54, 60));
            setToolTipText("Răng " + toothNumber + ": " + toothName);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addActionListener(e -> chonRang(toothNumber));
        }

        public void setStatusColor(Color c) {
            this.statusColor = c;
            repaint();
        }

        public void setSubtitle(String sub) {
            this.subtitle = sub;
            repaint();
        }

        public void setSelected(boolean sel) {
            this.isCurrentSelected = sel;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Nền răng
            g2.setColor(statusColor);
            g2.fillRoundRect(2, 2, w - 5, h - 5, 8, 8);

            // Viền răng (Nếu được chọn thì viền TEAL đậm 2.5px, nếu không thì viền LINE)
            if (isCurrentSelected) {
                g2.setColor(TEAL);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawRoundRect(2, 2, w - 5, h - 5, 8, 8);
            } else {
                g2.setColor(LINE);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(2, 2, w - 5, h - 5, 8, 8);
            }

            // Số hiệu răng (FDI)
            g2.setColor(INK);
            g2.setFont(new Font("Arial", Font.BOLD, 13));
            String strNum = String.valueOf(toothNumber);
            int strW = g2.getFontMetrics().stringWidth(strNum);
            g2.drawString(strNum, (w - strW) / 2, 22);

            // Vẽ hình icon răng nhỏ cách điệu
            g2.setColor(isCurrentSelected ? TEAL : MUTED);
            g2.setStroke(new BasicStroke(1.2f));
            int cx = w / 2;
            int cy = 34;
            g2.drawRoundRect(cx - 7, cy - 6, 14, 12, 4, 4);

            // Chữ viết tắt trạng thái
            g2.setFont(new Font("Arial", Font.BOLD, 9));
            int subW = g2.getFontMetrics().stringWidth(subtitle);
            Color textSubColor = (statusColor.equals(COLOR_CARIES) || statusColor.equals(COLOR_PULP)
                    || statusColor.equals(COLOR_FILLING)) ? Color.WHITE : INK;
            g2.setColor(textSubColor);
            g2.drawString(subtitle, (w - subW) / 2, h - 6);

            g2.dispose();
        }
    }

    // ==========================================
    // CÁC HÀM TIỆN ÍCH DỰNG GIAO DIỆN & TÊN GIẢI PHẪU
    // ==========================================

    private static class ToothStatus {
        final int toothNumber;
        final String tinhTrang;
        final String matRang;
        final String huongXuLy;
        final String ghiChu;

        ToothStatus(int toothNumber, String tinhTrang, String matRang, String huongXuLy, String ghiChu) {
            this.toothNumber = toothNumber;
            this.tinhTrang = tinhTrang;
            this.matRang = matRang;
            this.huongXuLy = huongXuLy;
            this.ghiChu = ghiChu;
        }
    }

    private String getTenRang(int num) {
        int donVi = num % 10;
        switch (donVi) {
            case 1:
                return "Răng cửa giữa";
            case 2:
                return "Răng cửa bên";
            case 3:
                return "Răng nanh";
            case 4:
                return "Răng tiền cối 1";
            case 5:
                return "Răng tiền cối 2";
            case 6:
                return "Răng cối lớn 1";
            case 7:
                return "Răng cối lớn 2";
            case 8:
                return "Răng khôn (R8)";
            default:
                return "Răng " + num;
        }
    }

    private String getTenRangTreEm(int num) {
        int donVi = num % 10;
        switch (donVi) {
            case 1:
                return "Răng cửa giữa sữa";
            case 2:
                return "Răng cửa bên sữa";
            case 3:
                return "Răng nanh sữa";
            case 4:
                return "Răng hàm sữa 1";
            case 5:
                return "Răng hàm sữa 2";
            default:
                return "Răng sữa " + num;
        }
    }

    private JPanel createCardPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(LINE),
                        title,
                        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                        javax.swing.border.TitledBorder.DEFAULT_POSITION,
                        new Font("Arial", Font.BOLD, 12),
                        INK),
                new EmptyBorder(8, 12, 10, 12)));
        return panel;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 11));
        label.setForeground(MUTED);
        return label;
    }

    private JTextField createTextField(boolean editable) {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Arial", Font.PLAIN, 12));
        tf.setForeground(INK);
        tf.setEditable(editable);
        tf.setBackground(editable ? Color.WHITE : new Color(248, 250, 251));
        tf.setPreferredSize(new Dimension(140, 32));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(0, 8, 0, 8)));
        return tf;
    }

    private JTextArea createTextArea(int rows) {
        JTextArea ta = new JTextArea(rows, 20);
        ta.setFont(new Font("Arial", Font.PLAIN, 12));
        ta.setForeground(INK);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(new EmptyBorder(4, 6, 4, 6));
        return ta;
    }

    private void styleComboBox(JComboBox<?> cbo) {
        cbo.setFont(new Font("Arial", Font.PLAIN, 12));
        cbo.setBackground(Color.WHITE);
        cbo.setForeground(INK);
    }

    private JButton createButton(String text, boolean primary) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (primary) {
            btn.setBackground(TEAL);
            btn.setForeground(Color.WHITE);
            btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(INK);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(LINE),
                    new EmptyBorder(7, 14, 7, 14)));
        }
        return btn;
    }
}

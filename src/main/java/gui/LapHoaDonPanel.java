package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Giao diện Lập hóa đơn và thanh toán (UC021, UC022) - Phòng khám Nha khoa
 * Muse.
 * Cho phép thu ngân / lễ tân trích xuất dịch vụ, tính tiền, áp dụng chiết khấu,
 * chọn phương thức thanh toán và in phiếu thu / hóa đơn thanh toán.
 */
public class LapHoaDonPanel extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;

    // Màu sắc quy chuẩn hệ thống Muse Dental
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color TEAL_LIGHT = new Color(231, 246, 245);
    private static final Color CANVAS = new Color(244, 248, 249);
    private static final Color LINE = new Color(226, 234, 237);
    private static final Color DANGER = new Color(214, 48, 49);
    private static final Color SUCCESS = new Color(39, 174, 96);

    private final DecimalFormat df = new DecimalFormat("#,##0 VNĐ");
    private final DecimalFormat dfNumber = new DecimalFormat("#,##0");
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Component thông tin chung
    private JTextField txtMaHD;
    private JTextField txtNgayLap;
    private JComboBox<String> cboThuNgan;
    private JComboBox<String> cboBacSi;
    private JLabel lblTrangThaiBadge;

    // Component thông tin bệnh nhân
    private JComboBox<String> cboBenhNhan;
    private JTextField txtMaBN;
    private JTextField txtTenBN;
    private JTextField txtSoDT;
    private JTextField txtDiaChi;
    private JTextField txtMaHoSo;

    // Component thêm dịch vụ
    private JComboBox<DichVuItem> cboDichVu;
    private JComboBox<String> cboViTriRang;
    private JSpinner spnSoLuong;
    private JTextField txtDonGia;
    private JSpinner spnGiamGia;
    private JButton btnThemDichVu;
    private JButton btnXoaDong;
    private JButton btnXoaTatCa;

    // Bảng chi tiết hóa đơn
    private JTable tblChiTiet;
    private DefaultTableModel modelTable;
    private final String[] cols = {
            "STT", "Mã DV", "Tên dịch vụ / thủ thuật", "Vị trí răng", "ĐVT",
            "Số lượng", "Đơn giá", "Giảm giá (%)", "Thành tiền"
    };

    // Component thanh toán
    private JLabel lblTongTienDichVu;
    private JTextField txtChietKhauVoucher;
    private JTextField txtBaoHiemChiTra;
    private JLabel lblTongThanhToan;
    private JComboBox<String> cboPhuongThucTT;
    private JTextField txtTienKhachDua;
    private JLabel lblTienThoi;
    private JTextArea txtGhiChu;

    // Các nút hành động chính
    private JButton btnLuuThanhToan;
    private JButton btnInHoaDon;
    private JButton btnLamMoi;

    // Dữ liệu dịch vụ mẫu
    private final List<DichVuItem> danhSachDichVuMau = khoiTaoDichVuMau();
    private final List<BenhNhanItem> danhSachBenhNhanMau = khoiTaoBenhNhanMau();

    public LapHoaDonPanel() {
        setLayout(new BorderLayout());
        setBackground(CANVAS);
        initUI();
        napDuLieuMacDinh();
    }

    private void initUI() {
        JPanel pMain = new JPanel();
        pMain.setLayout(new BoxLayout(pMain, BoxLayout.Y_AXIS));
        pMain.setBackground(CANVAS);
        pMain.setBorder(new EmptyBorder(12, 16, 16, 16));

        // 1. Tiêu đề và thanh thông tin đầu
        pMain.add(createHeaderPanel());
        pMain.add(Box.createVerticalStrut(12));

        // 2. Cụm thông tin hóa đơn và bệnh nhân (2 cột)
        pMain.add(createInvoiceAndPatientInfoPanel());
        pMain.add(Box.createVerticalStrut(12));

        // 3. Cụm danh sách dịch vụ chi tiết (Bảng và thanh thêm dịch vụ)
        pMain.add(createServiceListSection());
        pMain.add(Box.createVerticalStrut(12));

        // 4. Cụm thanh toán & Hành động
        pMain.add(createPaymentAndActionSection());

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

        JLabel title = new JLabel("Lập hóa đơn và thanh toán");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(INK);

        JLabel desc = new JLabel(
                "Lập hóa đơn điều trị, tính tiền dịch vụ & đơn thuốc, ghi nhận thanh toán và in biên lai");
        desc.setFont(new Font("Arial", Font.PLAIN, 12));
        desc.setForeground(MUTED);

        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(desc);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        btnLamMoi = createButton("Lập mới (F5)", false);
        btnInHoaDon = createButton("In biên lai / Hóa đơn", false);
        btnLuuThanhToan = createButton("Xác nhận & Thanh toán", false);

        btnLamMoi.addActionListener(this);
        btnInHoaDon.addActionListener(this);
        btnLuuThanhToan.addActionListener(this);

        right.add(btnLamMoi);
        right.add(btnInHoaDon);
        right.add(btnLuuThanhToan);

        panel.add(left, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    /** Khối 2 cột: Thông tin hóa đơn & Thông tin bệnh nhân */
    private JPanel createInvoiceAndPatientInfoPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 14, 0));
        panel.setOpaque(false);

        // --- CỘT TRÁI: THÔNG TIN HÓA ĐƠN ---
        JPanel pHoaDon = createCardPanel("Thông tin phiếu thu / Hóa đơn");
        pHoaDon.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtMaHD = createTextField(false);
        txtNgayLap = createTextField(false);
        txtNgayLap.setText(LocalDate.now().format(dtf));

        cboThuNgan = new JComboBox<>(new String[] {
                "NV001 - Bùi Thu Hương (Thu ngân)",
                "NV002 - Lê Văn Long (Lễ tân)",
                "NV003 - Phan Đức Toàn (Quản trị)"
        });
        styleComboBox(cboThuNgan);

        cboBacSi = new JComboBox<>(new String[] {
                "BS001 - ThS.BS Nguyễn Văn Tuấn",
                "BS002 - BSCKII Trần Mai Linh",
                "BS003 - ThS.BS Lê Minh Trí"
        });
        styleComboBox(cboBacSi);

        lblTrangThaiBadge = new JLabel("Chưa thanh toán", SwingConstants.CENTER);
        lblTrangThaiBadge.setFont(new Font("Arial", Font.BOLD, 11));
        lblTrangThaiBadge.setOpaque(true);
        lblTrangThaiBadge.setBackground(new Color(255, 246, 232));
        lblTrangThaiBadge.setForeground(new Color(177, 125, 47));
        lblTrangThaiBadge.setBorder(new EmptyBorder(5, 10, 5, 10));

        addFormRow(pHoaDon, gbc, 0, "Mã hóa đơn:", txtMaHD, "Trạng thái:", lblTrangThaiBadge);
        addFormRow(pHoaDon, gbc, 1, "Ngày lập:", txtNgayLap, "Bác sĩ khám:", cboBacSi);
        addFormRow(pHoaDon, gbc, 2, "Thu ngân lập:", cboThuNgan, "", new JLabel());

        // --- CỘT PHẢI: THÔNG TIN BỆNH NHÂN ---
        JPanel pBenhNhan = createCardPanel("Thông tin khách hàng / Bệnh nhân");
        pBenhNhan.setLayout(new GridBagLayout());
        GridBagConstraints gbcBN = new GridBagConstraints();
        gbcBN.insets = new Insets(5, 6, 5, 6);
        gbcBN.fill = GridBagConstraints.HORIZONTAL;

        cboBenhNhan = new JComboBox<>();
        cboBenhNhan.addItem("-- Chọn bệnh nhân hoặc tìm kiếm --");
        for (BenhNhanItem bn : danhSachBenhNhanMau) {
            cboBenhNhan.addItem(bn.maBN + " - " + bn.tenBN + " (" + bn.sdt + ")");
        }
        styleComboBox(cboBenhNhan);
        cboBenhNhan.addActionListener(e -> chonBenhNhanTuDanhSach());

        txtMaBN = createTextField(false);
        txtTenBN = createTextField(false);
        txtSoDT = createTextField(false);
        txtDiaChi = createTextField(false);
        txtMaHoSo = createTextField(false);

        // Hàng 0: Chọn nhanh
        gbcBN.gridx = 0;
        gbcBN.gridy = 0;
        gbcBN.gridwidth = 1;
        pBenhNhan.add(createFieldLabel("Chọn nhanh BN:"), gbcBN);
        gbcBN.gridx = 1;
        gbcBN.gridy = 0;
        gbcBN.gridwidth = 3;
        pBenhNhan.add(cboBenhNhan, gbcBN);

        gbcBN.gridwidth = 1;
        addFormRow(pBenhNhan, gbcBN, 1, "Mã bệnh nhân:", txtMaBN, "Tên bệnh nhân:", txtTenBN);
        addFormRow(pBenhNhan, gbcBN, 2, "Số điện thoại:", txtSoDT, "Hồ sơ EMR:", txtMaHoSo);
        addFormRow(pBenhNhan, gbcBN, 3, "Địa chỉ:", txtDiaChi, "", new JLabel());

        panel.add(pHoaDon);
        panel.add(pBenhNhan);
        return panel;
    }

    /** Cụm bảng dịch vụ và thanh công cụ thêm dịch vụ */
    private JPanel createServiceListSection() {
        JPanel card = createCardPanel("Chi tiết dịch vụ điều trị & Đơn thuốc thanh toán");
        card.setLayout(new BorderLayout(0, 10));

        // Thanh thêm dịch vụ nhanh
        JPanel pAdd = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        pAdd.setBackground(new Color(248, 251, 252));
        pAdd.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(6, 10, 6, 10)));

        cboDichVu = new JComboBox<>();
        for (DichVuItem dv : danhSachDichVuMau) {
            cboDichVu.addItem(dv);
        }
        cboDichVu.setPreferredSize(new Dimension(280, 32));
        styleComboBox(cboDichVu);
        cboDichVu.addActionListener(e -> capNhatDonGiaTheoDichVu());

        cboViTriRang = new JComboBox<>(new String[] {
                "Toàn hàm", "Răng 11", "Răng 12", "Răng 13", "Răng 14", "Răng 15", "Răng 16", "Răng 17", "Răng 18",
                "Răng 21", "Răng 22", "Răng 23", "Răng 24", "Răng 25", "Răng 26", "Răng 27", "Răng 28",
                "Răng 31", "Răng 32", "Răng 33", "Răng 34", "Răng 35", "Răng 36", "Răng 37", "Răng 38",
                "Răng 41", "Răng 42", "Răng 43", "Răng 44", "Răng 45", "Răng 46", "Răng 47", "Răng 48"
        });
        cboViTriRang.setPreferredSize(new Dimension(110, 32));
        styleComboBox(cboViTriRang);

        spnSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        spnSoLuong.setPreferredSize(new Dimension(55, 32));

        txtDonGia = createTextField(false);
        txtDonGia.setPreferredSize(new Dimension(120, 32));

        spnGiamGia = new JSpinner(new SpinnerNumberModel(0, 0, 100, 5));
        spnGiamGia.setPreferredSize(new Dimension(55, 32));

        btnThemDichVu = createButton("+ Thêm dịch vụ", true);
        btnThemDichVu.setPreferredSize(new Dimension(135, 32));
        btnThemDichVu.addActionListener(e -> themDichVuVaoBang());

        btnXoaDong = createButton("Xóa dòng", false);
        btnXoaDong.setForeground(DANGER);
        btnXoaDong.addActionListener(e -> xoaDongDaChon());

        btnXoaTatCa = createButton("Xóa hết", false);
        btnXoaTatCa.addActionListener(e -> xoaTatCaDichVu());

        pAdd.add(createFieldLabel("Dịch vụ / Thuốc:"));
        pAdd.add(cboDichVu);
        pAdd.add(createFieldLabel("Răng:"));
        pAdd.add(cboViTriRang);
        pAdd.add(createFieldLabel("SL:"));
        pAdd.add(spnSoLuong);
        pAdd.add(createFieldLabel("Đơn giá:"));
        pAdd.add(txtDonGia);
        pAdd.add(createFieldLabel("Giảm (%):"));
        pAdd.add(spnGiamGia);
        pAdd.add(btnThemDichVu);
        pAdd.add(btnXoaDong);
        pAdd.add(btnXoaTatCa);

        // Bảng dữ liệu dịch vụ
        modelTable = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblChiTiet = new JTable(modelTable);
        tblChiTiet.setRowHeight(30);
        tblChiTiet.setFont(new Font("Arial", Font.PLAIN, 12));
        tblChiTiet.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblChiTiet.setGridColor(LINE);

        JTableHeader th = tblChiTiet.getTableHeader();
        th.setFont(new Font("Arial", Font.BOLD, 12));
        th.setBackground(TEAL_LIGHT);
        th.setForeground(INK);
        th.setPreferredSize(new Dimension(0, 32));

        // Format căn lề cho các cột
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tblChiTiet.getColumnModel().getColumn(0).setPreferredWidth(45); // STT
        tblChiTiet.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tblChiTiet.getColumnModel().getColumn(1).setPreferredWidth(70); // Mã
        tblChiTiet.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        tblChiTiet.getColumnModel().getColumn(2).setPreferredWidth(260); // Tên
        tblChiTiet.getColumnModel().getColumn(3).setPreferredWidth(90); // Răng
        tblChiTiet.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tblChiTiet.getColumnModel().getColumn(4).setPreferredWidth(60); // ĐVT
        tblChiTiet.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        tblChiTiet.getColumnModel().getColumn(5).setPreferredWidth(60); // SL
        tblChiTiet.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        tblChiTiet.getColumnModel().getColumn(6).setCellRenderer(rightRenderer); // Đơn giá
        tblChiTiet.getColumnModel().getColumn(7).setCellRenderer(centerRenderer); // Giảm giá
        tblChiTiet.getColumnModel().getColumn(8).setCellRenderer(rightRenderer); // Thành tiền

        JScrollPane scrollTable = new JScrollPane(tblChiTiet);
        scrollTable.setPreferredSize(new Dimension(1000, 200));
        scrollTable.setBorder(BorderFactory.createLineBorder(LINE));

        card.add(pAdd, BorderLayout.NORTH);
        card.add(scrollTable, BorderLayout.CENTER);
        return card;
    }

    /** Cụm tính tiền, phương thức thanh toán và tổng tiền phải trả */
    private JPanel createPaymentAndActionSection() {
        JPanel card = createCardPanel("Xác nhận thanh toán & Tổng hợp chi phí");
        card.setLayout(new GridLayout(1, 2, 20, 0));

        // --- CỘT TRÁI: HÌNH THỨC THANH TOÁN & GHI CHÚ ---
        JPanel pLeft = new JPanel();
        pLeft.setOpaque(false);
        pLeft.setLayout(new BoxLayout(pLeft, BoxLayout.Y_AXIS));

        JPanel pPT = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        pPT.setOpaque(false);
        cboPhuongThucTT = new JComboBox<>(new String[] {
                "Tiền mặt (Cash)",
                "Chuyển khoản ngân hàng (QR Code)",
                "Thẻ ngân hàng / POS",
                "Chuyển hồ sơ trả góp theo đợt"
        });
        styleComboBox(cboPhuongThucTT);
        cboPhuongThucTT.setPreferredSize(new Dimension(240, 34));
        cboPhuongThucTT.addActionListener(e -> tinhToanTienThoi());
        pPT.add(createFieldLabel("Phương thức thanh toán:"));
        pPT.add(cboPhuongThucTT);

        JPanel pCash = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        pCash.setOpaque(false);
        txtTienKhachDua = createTextField(true);
        txtTienKhachDua.setPreferredSize(new Dimension(160, 34));
        txtTienKhachDua.setText("0");
        txtTienKhachDua.addActionListener(e -> tinhToanTienThoi());

        lblTienThoi = new JLabel("0 VNĐ");
        lblTienThoi.setFont(new Font("Arial", Font.BOLD, 14));
        lblTienThoi.setForeground(SUCCESS);

        pCash.add(createFieldLabel("Tiền khách đưa:"));
        pCash.add(txtTienKhachDua);
        pCash.add(createFieldLabel("Tiền thừa lại:"));
        pCash.add(lblTienThoi);

        JPanel pNote = new JPanel(new BorderLayout(0, 4));
        pNote.setOpaque(false);
        pNote.setBorder(new EmptyBorder(6, 6, 6, 6));
        pNote.add(createFieldLabel("Ghi chú hóa đơn (Thông tin xuất VAT / Mã số thuế / dặn dò):"), BorderLayout.NORTH);
        txtGhiChu = new JTextArea(3, 20);
        txtGhiChu.setFont(new Font("Arial", Font.PLAIN, 12));
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(6, 8, 6, 8)));
        pNote.add(new JScrollPane(txtGhiChu), BorderLayout.CENTER);

        pLeft.add(pPT);
        pLeft.add(Box.createVerticalStrut(4));
        pLeft.add(pCash);
        pLeft.add(Box.createVerticalStrut(4));
        pLeft.add(pNote);

        // --- CỘT PHẢI: BẢNG TÍNH TIỀN TỔNG HỢP ---
        JPanel pRight = new JPanel();
        pRight.setBackground(new Color(248, 251, 252));
        pRight.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(14, 20, 14, 20)));
        pRight.setLayout(new GridLayout(4, 2, 10, 10));

        lblTongTienDichVu = new JLabel("0 VNĐ", SwingConstants.RIGHT);
        lblTongTienDichVu.setFont(new Font("Arial", Font.BOLD, 13));
        lblTongTienDichVu.setForeground(INK);

        txtChietKhauVoucher = createTextField(true);
        txtChietKhauVoucher.setText("0");
        txtChietKhauVoucher.setHorizontalAlignment(JTextField.RIGHT);
        txtChietKhauVoucher.addActionListener(e -> capNhatTongTienVaThanhToan());

        txtBaoHiemChiTra = createTextField(true);
        txtBaoHiemChiTra.setText("0");
        txtBaoHiemChiTra.setHorizontalAlignment(JTextField.RIGHT);
        txtBaoHiemChiTra.addActionListener(e -> capNhatTongTienVaThanhToan());

        lblTongThanhToan = new JLabel("0 VNĐ", SwingConstants.RIGHT);
        lblTongThanhToan.setFont(new Font("Arial", Font.BOLD, 20));
        lblTongThanhToan.setForeground(TEAL);

        JLabel l1 = createFieldLabel("Tổng tiền dịch vụ:");
        JLabel l2 = createFieldLabel("Chiết khấu / Ưu đãi (VNĐ):");
        JLabel l3 = createFieldLabel("Bảo hiểm bảo lãnh (VNĐ):");
        JLabel l4 = new JLabel("TỔNG TIỀN PHẢI TRẢ:");
        l4.setFont(new Font("Arial", Font.BOLD, 14));
        l4.setForeground(INK);

        pRight.add(l1);
        pRight.add(lblTongTienDichVu);
        pRight.add(l2);
        pRight.add(txtChietKhauVoucher);
        pRight.add(l3);
        pRight.add(txtBaoHiemChiTra);
        pRight.add(l4);
        pRight.add(lblTongThanhToan);

        card.add(pLeft);
        card.add(pRight);
        return card;
    }

    // ==========================================
    // LOGIC NGHIỆP VỤ & TÍNH TOÁN
    // ==========================================

    private void napDuLieuMacDinh() {
        txtMaHD.setText(sinhMaHoaDonMoi());
        cboBenhNhan.setSelectedIndex(1); // Chọn BN001
        capNhatDonGiaTheoDichVu();

        // Thêm 2 dịch vụ mẫu phổ biến để màn hình sinh động
        themDichVuVaoBangManual(danhSachDichVuMau.get(0), "Toàn hàm", 1, 0);
        themDichVuVaoBangManual(danhSachDichVuMau.get(1), "Răng 36", 1, 10);
    }

    private String sinhMaHoaDonMoi() {
        long timestamp = System.currentTimeMillis() % 10000;
        return "HD" + String.format("%04d", timestamp);
    }

    private void capNhatDonGiaTheoDichVu() {
        DichVuItem item = (DichVuItem) cboDichVu.getSelectedItem();
        if (item != null) {
            txtDonGia.setText(dfNumber.format(item.donGia));
        }
    }

    private void chonBenhNhanTuDanhSach() {
        int idx = cboBenhNhan.getSelectedIndex();
        if (idx <= 0) {
            txtMaBN.setText("");
            txtTenBN.setText("");
            txtSoDT.setText("");
            txtDiaChi.setText("");
            txtMaHoSo.setText("");
            return;
        }
        BenhNhanItem bn = danhSachBenhNhanMau.get(idx - 1);
        txtMaBN.setText(bn.maBN);
        txtTenBN.setText(bn.tenBN);
        txtSoDT.setText(bn.sdt);
        txtDiaChi.setText(bn.diaChi);
        txtMaHoSo.setText(bn.maHoSo);
    }

    private void themDichVuVaoBang() {
        DichVuItem dv = (DichVuItem) cboDichVu.getSelectedItem();
        if (dv == null)
            return;

        String viTri = (String) cboViTriRang.getSelectedItem();
        int sl = (Integer) spnSoLuong.getValue();
        int giam = (Integer) spnGiamGia.getValue();

        themDichVuVaoBangManual(dv, viTri, sl, giam);
    }

    private void themDichVuVaoBangManual(DichVuItem dv, String viTri, int sl, int giam) {
        double thanhTien = dv.donGia * sl * (100 - giam) / 100.0;
        int stt = modelTable.getRowCount() + 1;

        modelTable.addRow(new Object[] {
                stt,
                dv.maDV,
                dv.tenDV,
                viTri,
                dv.dvt,
                sl,
                df.format(dv.donGia),
                giam + "%",
                df.format(thanhTien)
        });

        capNhatTongTienVaThanhToan();
    }

    private void xoaDongDaChon() {
        int row = tblChiTiet.getSelectedRow();
        if (row >= 0) {
            modelTable.removeRow(row);
            // Đánh lại số thứ tự
            for (int i = 0; i < modelTable.getRowCount(); i++) {
                modelTable.setValueAt(i + 1, i, 0);
            }
            capNhatTongTienVaThanhToan();
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng dịch vụ cần xóa khỏi hóa đơn.", "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void xoaTatCaDichVu() {
        if (modelTable.getRowCount() == 0)
            return;
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa tất cả dịch vụ trong bảng?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            modelTable.setRowCount(0);
            capNhatTongTienVaThanhToan();
        }
    }

    private double getTongTienDichVuRaw() {
        double total = 0;
        for (int i = 0; i < modelTable.getRowCount(); i++) {
            String str = modelTable.getValueAt(i, 8).toString().replace(" VNĐ", "").replace(".", "").replace(",", "");
            try {
                total += Double.parseDouble(str);
            } catch (Exception ignored) {
            }
        }
        return total;
    }

    private void capNhatTongTienVaThanhToan() {
        double tongDichVu = getTongTienDichVuRaw();
        lblTongTienDichVu.setText(df.format(tongDichVu));

        double chietKhau = parseDoubleSafely(txtChietKhauVoucher.getText());
        double baoHiem = parseDoubleSafely(txtBaoHiemChiTra.getText());

        double tongPhaiTra = tongDichVu - chietKhau - baoHiem;
        if (tongPhaiTra < 0)
            tongPhaiTra = 0;

        lblTongThanhToan.setText(df.format(tongPhaiTra));
        txtTienKhachDua.setText(dfNumber.format((long) tongPhaiTra));
        tinhToanTienThoi();
    }

    private void tinhToanTienThoi() {
        try {
            String strTong = lblTongThanhToan.getText().replace(" VNĐ", "").replace(".", "").replace(",", "");
            double tongPhaiTra = Double.parseDouble(strTong);

            String strKhachDua = txtTienKhachDua.getText().replace(".", "").replace(",", "").trim();
            double khachDua = strKhachDua.isEmpty() ? 0 : Double.parseDouble(strKhachDua);

            double tienThoi = khachDua - tongPhaiTra;
            if (tienThoi < 0) {
                lblTienThoi.setText("Còn thiếu: " + df.format(-tienThoi));
                lblTienThoi.setForeground(DANGER);
            } else {
                lblTienThoi.setText(df.format(tienThoi));
                lblTienThoi.setForeground(SUCCESS);
            }
        } catch (Exception e) {
            lblTienThoi.setText("0 VNĐ");
        }
    }

    private double parseDoubleSafely(String text) {
        if (text == null)
            return 0;
        String clean = text.replace(".", "").replace(",", "").trim();
        if (clean.isEmpty())
            return 0;
        try {
            return Double.parseDouble(clean);
        } catch (Exception e) {
            return 0;
        }
    }

    private void thucHienLuuThanhToan() {
        if (txtMaBN.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn thông tin bệnh nhân trước khi thanh toán.",
                    "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (modelTable.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Hóa đơn phải có ít nhất 1 dịch vụ để thanh toán.", "Chưa có dịch vụ",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String pt = (String) cboPhuongThucTT.getSelectedItem();
        String tongTien = lblTongThanhToan.getText();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận thanh toán cho hóa đơn " + txtMaHD.getText() + ":\n" +
                        "- Bệnh nhân: " + txtTenBN.getText() + " (" + txtMaBN.getText() + ")\n" +
                        "- Tổng số tiền: " + tongTien + "\n" +
                        "- Phương thức: " + pt + "\n\nTiếp tục hoàn tất giao dịch?",
                "Xác nhận thanh toán", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            lblTrangThaiBadge.setText("Đã thanh toán");
            lblTrangThaiBadge.setBackground(new Color(231, 246, 245));
            lblTrangThaiBadge.setForeground(TEAL);

            int in = JOptionPane.showConfirmDialog(this,
                    "Thanh toán thành công hóa đơn " + txtMaHD.getText()
                            + "!\nBạn có muốn mở xem và in hóa đơn biên lai không?",
                    "Thành công", JOptionPane.YES_NO_OPTION);
            if (in == JOptionPane.YES_OPTION) {
                moDialogInHoaDon();
            }
        }
    }

    private void moDialogInHoaDon() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, "Xem trước biên lai thanh toán - Nha khoa Muse",
                JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(520, 680);
        dialog.setLocationRelativeTo(parentWindow);
        dialog.setLayout(new BorderLayout());

        JPanel pReceipt = new JPanel();
        pReceipt.setBackground(Color.WHITE);
        pReceipt.setBorder(new EmptyBorder(24, 28, 24, 28));
        pReceipt.setLayout(new BoxLayout(pReceipt, BoxLayout.Y_AXIS));

        // Header Biên lai
        JLabel h1 = new JLabel("NHA KHOA QUỐC TẾ MUSE DENTAL", SwingConstants.CENTER);
        h1.setFont(new Font("Arial", Font.BOLD, 15));
        h1.setForeground(INK);
        h1.setAlignmentX(CENTER_ALIGNMENT);

        JLabel h2 = new JLabel("12 Nguyễn Văn Bảo, Phường 4, Q. Gò Vấp, TP. Hồ Chí Minh", SwingConstants.CENTER);
        h2.setFont(new Font("Arial", Font.PLAIN, 10));
        h2.setForeground(MUTED);
        h2.setAlignmentX(CENTER_ALIGNMENT);

        JLabel h3 = new JLabel("Hotline: 1900 6868 · Website: www.musedental.vn", SwingConstants.CENTER);
        h3.setFont(new Font("Arial", Font.PLAIN, 10));
        h3.setForeground(MUTED);
        h3.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sep1 = new JLabel(
                "--------------------------------------------------------------------------------------------------");
        sep1.setForeground(LINE);
        sep1.setAlignmentX(CENTER_ALIGNMENT);

        JLabel titleBill = new JLabel("HÓA ĐƠN THANH TOÁN DỊCH VỤ", SwingConstants.CENTER);
        titleBill.setFont(new Font("Arial", Font.BOLD, 14));
        titleBill.setForeground(TEAL);
        titleBill.setAlignmentX(CENTER_ALIGNMENT);

        pReceipt.add(h1);
        pReceipt.add(Box.createVerticalStrut(4));
        pReceipt.add(h2);
        pReceipt.add(Box.createVerticalStrut(2));
        pReceipt.add(h3);
        pReceipt.add(Box.createVerticalStrut(8));
        pReceipt.add(sep1);
        pReceipt.add(Box.createVerticalStrut(8));
        pReceipt.add(titleBill);
        pReceipt.add(Box.createVerticalStrut(10));

        // Thông tin người nhận
        JPanel info = new JPanel(new GridLayout(4, 2, 8, 4));
        info.setBackground(Color.WHITE);
        info.add(new JLabel("Mã hóa đơn: " + txtMaHD.getText()));
        info.add(new JLabel("Ngày lập: " + txtNgayLap.getText()));
        info.add(new JLabel("Bệnh nhân: " + txtTenBN.getText()));
        info.add(new JLabel("Mã BN: " + txtMaBN.getText()));
        info.add(new JLabel("Số điện thoại: " + txtSoDT.getText()));
        info.add(new JLabel("Bác sĩ: " + cboBacSi.getSelectedItem().toString().split("-")[1].trim()));
        info.add(new JLabel("Thu ngân: " + cboThuNgan.getSelectedItem().toString().split("-")[1].trim()));
        info.add(new JLabel("PT Thanh toán: " + cboPhuongThucTT.getSelectedItem().toString().split("\\(")[0].trim()));
        pReceipt.add(info);
        pReceipt.add(Box.createVerticalStrut(12));

        // Bảng dịch vụ rút gọn
        String[] bCols = { "Dịch vụ", "Răng", "SL", "Đơn giá", "T.Tiền" };
        DefaultTableModel bModel = new DefaultTableModel(bCols, 0);
        for (int i = 0; i < modelTable.getRowCount(); i++) {
            bModel.addRow(new Object[] {
                    modelTable.getValueAt(i, 2),
                    modelTable.getValueAt(i, 3),
                    modelTable.getValueAt(i, 5),
                    modelTable.getValueAt(i, 6),
                    modelTable.getValueAt(i, 8)
            });
        }
        JTable bTable = new JTable(bModel);
        bTable.setFont(new Font("Arial", Font.PLAIN, 11));
        bTable.setRowHeight(22);
        JScrollPane scpB = new JScrollPane(bTable);
        scpB.setPreferredSize(new Dimension(460, 140));
        pReceipt.add(scpB);
        pReceipt.add(Box.createVerticalStrut(10));

        // Tổng kết
        JPanel summary = new JPanel(new GridLayout(3, 2, 8, 4));
        summary.setBackground(Color.WHITE);
        summary.add(new JLabel("Tổng tiền dịch vụ:"));
        summary.add(new JLabel(lblTongTienDichVu.getText(), SwingConstants.RIGHT));
        summary.add(new JLabel("Chiết khấu / Ưu đãi:"));
        summary.add(new JLabel(df.format(parseDoubleSafely(txtChietKhauVoucher.getText())), SwingConstants.RIGHT));

        JLabel sumTotal = new JLabel("TỔNG TIỀN THANH TOÁN:");
        sumTotal.setFont(new Font("Arial", Font.BOLD, 12));
        sumTotal.setForeground(INK);
        JLabel valTotal = new JLabel(lblTongThanhToan.getText(), SwingConstants.RIGHT);
        valTotal.setFont(new Font("Arial", Font.BOLD, 14));
        valTotal.setForeground(TEAL);

        summary.add(sumTotal);
        summary.add(valTotal);
        pReceipt.add(summary);
        pReceipt.add(Box.createVerticalStrut(16));

        // Chữ ký
        JPanel sign = new JPanel(new GridLayout(1, 2));
        sign.setBackground(Color.WHITE);
        JLabel s1 = new JLabel("<html><center><b>BỆNH NHÂN</b><br><i>(Ký, ghi rõ họ tên)</i></center></html>",
                SwingConstants.CENTER);
        JLabel s2 = new JLabel("<html><center><b>NGƯỜI THU TIỀN</b><br><i>(Ký, ghi rõ họ tên)</i></center></html>",
                SwingConstants.CENTER);
        sign.add(s1);
        sign.add(s2);
        pReceipt.add(sign);

        // Nút in ở footer dialog
        JPanel pFoot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnPrint = createButton("In biên lai (Print)", true);
        JButton btnClose = createButton("Đóng", false);
        btnPrint.addActionListener(e -> {
            JOptionPane.showMessageDialog(dialog, "Lệnh in đã được gửi tới máy in nhiệt hóa đơn!", "Máy in",
                    JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });
        btnClose.addActionListener(e -> dialog.dispose());
        pFoot.add(btnPrint);
        pFoot.add(btnClose);

        dialog.add(new JScrollPane(pReceipt), BorderLayout.CENTER);
        dialog.add(pFoot, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void lamMoiForm() {
        txtMaHD.setText(sinhMaHoaDonMoi());
        lblTrangThaiBadge.setText("Chưa thanh toán");
        lblTrangThaiBadge.setBackground(new Color(255, 246, 232));
        lblTrangThaiBadge.setForeground(new Color(177, 125, 47));
        modelTable.setRowCount(0);
        cboBenhNhan.setSelectedIndex(0);
        txtChietKhauVoucher.setText("0");
        txtBaoHiemChiTra.setText("0");
        txtGhiChu.setText("");
        capNhatTongTienVaThanhToan();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src.equals(btnLamMoi)) {
            lamMoiForm();
        } else if (src.equals(btnInHoaDon)) {
            moDialogInHoaDon();
        } else if (src.equals(btnLuuThanhToan)) {
            thucHienLuuThanhToan();
        }
    }

    // ==========================================
    // CÁC HÀM TIỆN ÍCH DỰNG GIAO DIỆN
    // ==========================================

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

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row,
            String label1, Component comp1,
            String label2, Component comp2) {
        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.weightx = 0.15;
        panel.add(createFieldLabel(label1), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.35;
        panel.add(comp1, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.15;
        panel.add(createFieldLabel(label2), gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.35;
        panel.add(comp2, gbc);
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

    // ==========================================
    // DỮ LIỆU MẪU ĐỂ TEST UI
    // ==========================================

    private static class DichVuItem {
        final String maDV;
        final String tenDV;
        final String dvt;
        final double donGia;

        DichVuItem(String maDV, String tenDV, String dvt, double donGia) {
            this.maDV = maDV;
            this.tenDV = tenDV;
            this.dvt = dvt;
            this.donGia = donGia;
        }

        @Override
        public String toString() {
            return maDV + " - " + tenDV;
        }
    }

    private static class BenhNhanItem {
        final String maBN;
        final String tenBN;
        final String sdt;
        final String diaChi;
        final String maHoSo;

        BenhNhanItem(String maBN, String tenBN, String sdt, String diaChi, String maHoSo) {
            this.maBN = maBN;
            this.tenBN = tenBN;
            this.sdt = sdt;
            this.diaChi = diaChi;
            this.maHoSo = maHoSo;
        }
    }

    private List<DichVuItem> khoiTaoDichVuMau() {
        List<DichVuItem> list = new ArrayList<>();
        list.add(new DichVuItem("DV001", "Cạo vôi răng & Đánh bóng 2 hàm", "Lần", 250000));
        list.add(new DichVuItem("DV002", "Trám răng thẩm mỹ Composite", "Răng", 350000));
        list.add(new DichVuItem("DV003", "Điều trị tủy răng 1 ống", "Răng", 800000));
        list.add(new DichVuItem("DV004", "Điều trị tủy răng nhiều ống", "Răng", 1500000));
        list.add(new DichVuItem("DV005", "Nhổ răng khôn mọc lệch hàm dưới (Tiểu phẫu)", "Răng", 1800000));
        list.add(new DichVuItem("DV006", "Bọc răng sứ cao cấp Cercon HT", "Răng", 4500000));
        list.add(new DichVuItem("DV007", "Tẩy trắng răng công nghệ Laser Whitening", "Liệu trình", 2200000));
        list.add(new DichVuItem("DV008", "Cấy ghép Implant Straumann Thụy Sĩ", "Trụ", 25000000));
        list.add(new DichVuItem("DV009", "Chụp phim X-quang Panorama toàn hàm", "Lần", 150000));
        list.add(new DichVuItem("TH001", "Đơn thuốc kháng sinh & giảm đau hậu phẫu", "Toa", 280000));
        return list;
    }

    private List<BenhNhanItem> khoiTaoBenhNhanMau() {
        List<BenhNhanItem> list = new ArrayList<>();
        list.add(new BenhNhanItem("BN001", "Nguyễn Văn An", "0908123456", "124 Quang Trung, Gò Vấp, TP.HCM", "HS001"));
        list.add(new BenhNhanItem("BN002", "Trần Thị Bình", "0912345678", "56 Phan Đăng Lưu, Phú Nhuận, TP.HCM",
                "HS002"));
        list.add(new BenhNhanItem("BN003", "Lê Hoàng Cường", "0987654321", "89 Lê Lợi, Quận 1, TP.HCM", "HS003"));
        list.add(new BenhNhanItem("BN004", "Phạm Minh Dũng", "0934567890", "12 Hoàng Văn Thụ, Tân Bình, TP.HCM",
                "HS004"));
        list.add(new BenhNhanItem("BN005", "Đỗ Ánh Nguyệt", "0945678901", "78 Nguyễn Trãi, Quận 5, TP.HCM", "HS005"));
        return list;
    }
}

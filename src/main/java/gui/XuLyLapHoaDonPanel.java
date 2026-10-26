package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import util.HoaDonPdfExporter;

/**
 * Panel "Lập hóa đơn" (bước 9 trong luồng khám bệnh chính).
 *
 * Luồng xử lý:
 * 1. Thu ngân chọn lần khám đã chốt (ChanDoan = 'Da chot') chưa có hóa đơn.
 * 2. Hệ thống tự nạp tiền dịch vụ (từ phác đồ: KeHoachDieuTri_DichVu ->
 * ChiTietHoaDon) và tiền thuốc (từ đơn thuốc: ChiTietDonThuoc ->
 * ChiTietHoaDonThuoc, giá = VatTu.DonGiaBan).
 * 3. Chọn phương thức thanh toán:
 * - Tiền mặt / Chuyển khoản -> HoaDon = 'Da thanh toan'.
 * - Trả góp -> chọn số đợt, tạo CongNoTraGop + DotThanhToan,
 * HoaDon = 'Dang tra gop'. Đợt 1 thu ngay khi lập hóa đơn.
 * 4. Bấm "Lập hóa đơn".
 *
 * Hiện tại dùng dữ liệu mẫu trong bộ nhớ. Chỗ cần nối DAO ghi "TODO DAO".
 */
public class XuLyLapHoaDonPanel extends JPanel {
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color LINE = new Color(226, 234, 237);

    private static final DecimalFormat TIEN = new DecimalFormat("#,##0");
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Phương thức thanh toán (DB lưu không dấu: 'Tien mat', 'Chuyen khoan', 'Tra
    // gop').
    private static final String TIEN_MAT = "Tiền mặt";
    private static final String CHUYEN_KHOAN = "Chuyển khoản";
    private static final String TRA_GOP = "Trả góp";

    // Trạng thái đợt thanh toán.
    private static final String DOT_DA_THANH_TOAN = "Đã thanh toán";
    private static final String DOT_CHUA_DEN_HAN = "Chưa đến hạn";

    // ----- Dữ liệu mẫu -----
    // TODO DAO: ChanDoanDAO.layLanKhamDaChotChuaCoHoaDon() thay cho dữ liệu này.
    // {MaChanDoan, MaBenhNhan, HoTen, KetLuanChanDoan}
    private final List<String[]> dsLanKham = new ArrayList<>();

    // TODO DAO: DichVuDAO.layTatCa()
    // {MaDichVu, TenDichVu, DonGia}
    private static final Object[][] MOCK_DICH_VU = {
            { "DV001", "Lấy cao răng", 200000.0 },
            { "DV002", "Trám răng composite", 300000.0 },
            { "DV003", "Nhổ răng khôn", 1500000.0 },
            { "DV004", "Bọc răng sứ", 3500000.0 },
            { "DV005", "Điều trị tủy răng", 1200000.0 },
            { "DV006", "Tẩy trắng răng", 2000000.0 }
    };

    // TODO DAO: VatTuDAO.layTatCa()
    // {MaVatTu, TenVatTu, DonViTinh, DonGiaBan}
    private static final Object[][] MOCK_THUOC = {
            { "VT001", "Amoxicillin 500mg", "Viên", 5000.0 },
            { "VT002", "Paracetamol 500mg", "Viên", 1500.0 }
    };

    // maChanDoan -> các dòng dịch vụ trong phác đồ {maDichVu, soLuong}.
    // TODO DAO: KeHoachDieuTri_DichVuDAO.timTheoChanDoan(maChanDoan)
    private final Map<String, Object[][]> dichVuTheoChanDoan = new HashMap<>();
    // maChanDoan -> các dòng thuốc trong đơn {maVatTu, soLuong}.
    // TODO DAO: ChiTietDonThuocDAO.timTheoChanDoan(maChanDoan)
    private final Map<String, Object[][]> thuocTheoChanDoan = new HashMap<>();

    private int nextHoaDon = 5;
    private int nextCongNo = 2;
    private int nextDot = 4;

    // Tổng tiền của lần khám đang chọn.
    private double tienDichVu = 0;
    private double tienThuoc = 0;
    private boolean dangNapCombo = false;

    // ----- Chọn lần khám -----
    private final JComboBox<String> cboLanKham = new JComboBox<>();
    private final JLabel lblBenhNhan = new JLabel(" ");
    private final JLabel lblKetLuan = new JLabel(" ");
    private final JLabel lblNgayLap = new JLabel(LocalDate.now().format(NGAY));

    // ----- Chi tiết tiền -----
    private final DefaultTableModel dichVuModel = new DefaultTableModel(
            new String[] { "Mã", "Tên dịch vụ", "Đơn giá (đ)", "SL", "Thành tiền (đ)" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable dichVuTable = new JTable(dichVuModel);
    private final DefaultTableModel thuocModel = new DefaultTableModel(
            new String[] { "Mã", "Tên thuốc", "Đơn giá (đ)", "SL", "Thành tiền (đ)" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable thuocTable = new JTable(thuocModel);
    private final JLabel lblTienDichVu = new JLabel("0 đ");
    private final JLabel lblTienThuoc = new JLabel("0 đ");
    private final JLabel lblTongTien = new JLabel("0 đ");

    // ----- Thanh toán -----
    private final JComboBox<String> cboPhuongThuc = new JComboBox<>(
            new String[] { TIEN_MAT, CHUYEN_KHOAN, TRA_GOP });
    private final JSpinner spnSoDot = new JSpinner(new SpinnerNumberModel(3, 2, 12, 1));
    private final DefaultTableModel dotModel = new DefaultTableModel(
            new String[] { "Đợt", "Ngày đến hạn", "Số tiền (đ)", "Trạng thái" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable dotTable = new JTable(dotModel);
    private final JLabel lblGhiChuThanhToan = new JLabel(" ");
    private final JButton btnLap = createButton("Lập hóa đơn", true);

    public XuLyLapHoaDonPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);

        taoDuLieuMau();

        add(createLanKhamCard(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(16, 0));
        center.setOpaque(false);
        center.add(createChiTietCard(), BorderLayout.CENTER);
        center.add(createThanhToanCard(), BorderLayout.EAST);
        add(center, BorderLayout.CENTER);

        napComboLanKham();
        // Gắn listener sau khi đã nạp item để không bị kích hoạt sớm.
        cboLanKham.addActionListener(event -> {
            if (!dangNapCombo) {
                chonLanKham();
            }
        });
        cboPhuongThuc.addActionListener(event -> capNhatPhuongThuc());
        spnSoDot.addChangeListener(event -> loadBangDot());
        chonLanKham();
    }

    // =====================================================================
    // DỮ LIỆU MẪU
    // =====================================================================

    private void taoDuLieuMau() {
        // Khớp với các lần khám mẫu ở panel Kê đơn / Phác đồ.
        dsLanKham.add(new String[] { "CD005", "BN003", "Đặng Quốc Khánh", "Viêm tủy răng số 46" });
        dsLanKham.add(new String[] { "CD006", "BN001", "Võ Thanh Hải", "Sâu răng số 16, cao răng nhiều" });
        dsLanKham.add(new String[] { "CD007", "BN004", "Trần Bảo Ngọc", "Sâu răng sữa số 55 mức độ nhẹ" });

        dichVuTheoChanDoan.put("CD005", new Object[][] { { "DV005", 1 }, { "DV002", 1 } });
        dichVuTheoChanDoan.put("CD006", new Object[][] { { "DV001", 1 }, { "DV002", 1 } });
        dichVuTheoChanDoan.put("CD007", new Object[][] { { "DV002", 1 } });

        thuocTheoChanDoan.put("CD005", new Object[][] { { "VT001", 14 }, { "VT002", 10 } });
        thuocTheoChanDoan.put("CD007", new Object[][] { { "VT002", 6 } });
        // CD006 không có đơn thuốc nên không có dòng thuốc.
    }

    // =====================================================================
    // GIAO DIỆN
    // =====================================================================

    private JPanel createLanKhamCard() {
        JPanel card = createCard();
        card.add(createCardTitle("Lần khám cần lập hóa đơn"));
        card.add(Box.createVerticalStrut(10));

        cboLanKham.setFont(new Font("Arial", Font.PLAIN, 12));
        card.add(createRow("Lần khám (đã chốt)", cboLanKham));

        lblBenhNhan.setFont(new Font("Arial", Font.BOLD, 12));
        lblBenhNhan.setForeground(INK);
        lblKetLuan.setFont(new Font("Arial", Font.PLAIN, 12));
        lblKetLuan.setForeground(INK);
        lblNgayLap.setFont(new Font("Arial", Font.PLAIN, 12));
        lblNgayLap.setForeground(MUTED);
        for (JLabel label : new JLabel[] { lblBenhNhan, lblKetLuan }) {
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(label);
            card.add(Box.createVerticalStrut(4));
        }
        JLabel lblThongTin = new JLabel();
        lblThongTin.setFont(new Font("Arial", Font.PLAIN, 12));
        lblThongTin.setForeground(MUTED);
        lblThongTin.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblThongTin.setText("Ngày lập: " + LocalDate.now().format(NGAY)
                + "  ·  Thu ngân: nhân viên đang đăng nhập");
        card.add(lblThongTin);
        return card;
    }

    // Bên trái: chi tiết tiền dịch vụ và tiền thuốc.
    private JPanel createChiTietCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(16, 18, 16, 18)));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(createCardTitle("Tiền dịch vụ (theo phác đồ)"));
        content.add(Box.createVerticalStrut(8));
        styleTable(dichVuTable);
        dichVuTable.getColumnModel().getColumn(0).setPreferredWidth(55);
        dichVuTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        dichVuTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        dichVuTable.getColumnModel().getColumn(3).setPreferredWidth(40);
        dichVuTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        content.add(createScroll(dichVuTable));
        content.add(Box.createVerticalStrut(6));
        content.add(createMoneyRow("Cộng tiền dịch vụ", lblTienDichVu, 13, INK));
        content.add(Box.createVerticalStrut(14));

        content.add(createCardTitle("Tiền thuốc (theo đơn thuốc)"));
        content.add(Box.createVerticalStrut(8));
        styleTable(thuocTable);
        thuocTable.getColumnModel().getColumn(0).setPreferredWidth(55);
        thuocTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        thuocTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        thuocTable.getColumnModel().getColumn(3).setPreferredWidth(40);
        thuocTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        content.add(createScroll(thuocTable));
        content.add(Box.createVerticalStrut(6));
        content.add(createMoneyRow("Cộng tiền thuốc", lblTienThuoc, 13, INK));
        content.add(Box.createVerticalStrut(14));

        content.add(createMoneyRow("TỔNG THANH TOÁN", lblTongTien, 20, TEAL));

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    // Bên phải: phương thức thanh toán + bảng chia đợt.
    private JPanel createThanhToanCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(16, 18, 16, 18)));
        card.setPreferredSize(new Dimension(450, 0));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(createCardTitle("Thanh toán"));
        top.add(Box.createVerticalStrut(10));
        top.add(createRow("Phương thức", cboPhuongThuc));
        top.add(createRow("Số đợt trả góp", spnSoDot));

        lblGhiChuThanhToan.setFont(new Font("Arial", Font.ITALIC, 11));
        lblGhiChuThanhToan.setForeground(MUTED);
        lblGhiChuThanhToan.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(lblGhiChuThanhToan);
        top.add(Box.createVerticalStrut(8));
        JLabel dotTitle = new JLabel("Lịch các đợt thanh toán");
        dotTitle.setFont(new Font("Arial", Font.BOLD, 11));
        dotTitle.setForeground(MUTED);
        dotTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(dotTitle);
        card.add(top, BorderLayout.NORTH);

        styleTable(dotTable);
        dotTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        dotTable.getColumnModel().getColumn(1).setPreferredWidth(95);
        dotTable.getColumnModel().getColumn(2).setPreferredWidth(95);
        dotTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        JScrollPane scroll = new JScrollPane(dotTable);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton btnLamMoi = createButton("Làm mới", false);
        btnLamMoi.addActionListener(event -> chonLanKham());
        btnLap.addActionListener(event -> lapHoaDon());
        actions.add(btnLamMoi);
        actions.add(btnLap);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    // =====================================================================
    // XỬ LÝ SỰ KIỆN
    // =====================================================================

    /** Chọn lần khám: nạp tiền dịch vụ, tiền thuốc và tính tổng. */
    private void chonLanKham() {
        int index = cboLanKham.getSelectedIndex();
        dichVuModel.setRowCount(0);
        thuocModel.setRowCount(0);
        tienDichVu = 0;
        tienThuoc = 0;

        if (index < 0) {
            lblBenhNhan.setText("Không còn lần khám nào chờ lập hóa đơn.");
            lblKetLuan.setText(" ");
            btnLap.setEnabled(false);
            capNhatTongTien();
            return;
        }
        btnLap.setEnabled(true);
        String[] lanKham = dsLanKham.get(index);
        lblBenhNhan.setText("Bệnh nhân: " + lanKham[2] + "  ·  " + lanKham[1]);
        lblKetLuan.setText("Kết luận chẩn đoán: " + lanKham[3]);

        // TODO DAO: lấy dịch vụ từ KeHoachDieuTri_DichVu, thuốc từ ChiTietDonThuoc.
        Object[][] dichVu = dichVuTheoChanDoan.get(lanKham[0]);
        if (dichVu != null) {
            for (Object[] dong : dichVu) {
                Object[] dv = timDichVu((String) dong[0]);
                int soLuong = (Integer) dong[1];
                double donGia = (Double) dv[2];
                tienDichVu += donGia * soLuong;
                dichVuModel.addRow(new Object[] { dv[0], dv[1], TIEN.format(donGia), soLuong,
                        TIEN.format(donGia * soLuong) });
            }
        }
        Object[][] thuoc = thuocTheoChanDoan.get(lanKham[0]);
        if (thuoc != null) {
            for (Object[] dong : thuoc) {
                Object[] t = timThuoc((String) dong[0]);
                int soLuong = (Integer) dong[1];
                double donGia = (Double) t[3];
                tienThuoc += donGia * soLuong;
                thuocModel.addRow(new Object[] { t[0], t[1], TIEN.format(donGia), soLuong,
                        TIEN.format(donGia * soLuong) });
            }
        }
        capNhatTongTien();
    }

    /** Bật/tắt phần trả góp theo phương thức thanh toán. */
    private void capNhatPhuongThuc() {
        boolean traGop = TRA_GOP.equals(cboPhuongThuc.getSelectedItem());
        spnSoDot.setEnabled(traGop);
        if (traGop) {
            lblGhiChuThanhToan.setText("Đợt 1 thu ngay khi lập hóa đơn, các đợt sau cách nhau 1 tháng.");
        } else {
            lblGhiChuThanhToan.setText("Thanh toán một lần, hóa đơn ghi nhận \"Đã thanh toán\".");
        }
        loadBangDot();
    }

    /** Kiểm tra rồi lập hóa đơn (và công nợ nếu trả góp). */
    private void lapHoaDon() {
        int index = cboLanKham.getSelectedIndex();
        if (index < 0) {
            showMessage("Vui lòng chọn lần khám cần lập hóa đơn.");
            return;
        }
        double tong = tienDichVu + tienThuoc;
        if (tong <= 0) {
            showWarning("Lần khám này không có khoản nào để tính tiền.");
            return;
        }
        boolean traGop = TRA_GOP.equals(cboPhuongThuc.getSelectedItem());
        String phuongThuc = (String) cboPhuongThuc.getSelectedItem();

        int choice = JOptionPane.showConfirmDialog(this,
                "Lập hóa đơn " + TIEN.format(tong) + " đ cho " + dsLanKham.get(index)[2] + "\n"
                        + "Phương thức: " + phuongThuc + "?",
                "Xác nhận lập hóa đơn", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        String[] lanKham = dsLanKham.get(index);
        // TODO DAO: sinh mã từ DB.
        String maHoaDon = String.format("HD%03d", nextHoaDon++);
        String trangThai = traGop ? "Dang tra gop" : "Da thanh toan";

        // TODO DAO (một transaction):
        // 1. HoaDonDAO.them(maHoaDon, ngayLap, trangThai, phuongThuc,
        // maNhanVien (thu ngân đăng nhập), maBenhNhan, maChanDoan)
        // 2. ChiTietHoaDonDAO.them(maHoaDon, maDichVu, soLuong, donGiaTaiThoiDiem)
        // cho từng dòng dịch vụ (đơn giá lấy từ DichVu.DonGia tại thời điểm lập)
        // 3. ChiTietHoaDonThuocDAO.them(maHoaDon, maVatTu, soLuong, donGiaTaiThoiDiem)
        // cho từng dòng thuốc (đơn giá lấy từ VatTu.DonGiaBan)
        // 4. Nếu trả góp:
        // CongNoTraGopDAO.them(maCongNo, tong, soDotConLai = soDot - 1, maHoaDon)
        // DotThanhToanDAO.them(...) cho từng đợt (đợt 1 đã thanh toán)
        String thongBao = "Đã lập hóa đơn " + maHoaDon + ". Tổng tiền: " + TIEN.format(tong) + " đ.";
        if (traGop) {
            int soDot = (Integer) spnSoDot.getValue();
            String maCongNo = String.format("CN%03d", nextCongNo++);
            nextDot += soDot;
            thongBao += "\nĐã tạo công nợ " + maCongNo + " gồm " + soDot
                    + " đợt. Còn lại " + (soDot - 1) + " đợt cần thu.";
        }

        // Lần khám này đã có hóa đơn nên bỏ khỏi danh sách chờ.
        dsLanKham.remove(index);
        napComboLanKham();
        chonLanKham();
        showMessage(thongBao);

        int exportChoice = JOptionPane.showConfirmDialog(this,
                "Bạn có muốn xuất hóa đơn " + maHoaDon + " ra file PDF không?",
                "Xuất hóa đơn PDF", JOptionPane.YES_NO_OPTION);
        if (exportChoice == JOptionPane.YES_OPTION) {
            xuatHoaDonPdf(maHoaDon, lanKham, phuongThuc, trangThai, tong);
        }
    }

    // =====================================================================
    // HÀM HỖ TRỢ
    // =====================================================================

    private void xuatHoaDonPdf(String maHoaDon, String[] lanKham, String phuongThuc,
            String trangThai, double tong) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu hóa đơn PDF");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Tệp PDF (*.pdf)", "pdf"));
        fileChooser.setSelectedFile(new File("HoaDon_" + maHoaDon + ".pdf"));
        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = fileChooser.getSelectedFile();
        String fileName = selectedFile.getName();
        if (!fileName.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            selectedFile = new File(selectedFile.getParentFile(), fileName + ".pdf");
        }
        if (selectedFile.exists()) {
            int overwriteChoice = JOptionPane.showConfirmDialog(this,
                    "Tệp đã tồn tại. Bạn có muốn ghi đè không?",
                    "Xác nhận ghi đè", JOptionPane.YES_NO_OPTION);
            if (overwriteChoice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            HoaDonPdfExporter.export(selectedFile.toPath(), maHoaDon, LocalDate.now(),
                    lanKham[1], lanKham[2], lanKham[0], lanKham[3], phuongThuc, trangThai,
                    layDongBang(dichVuModel), layDongBang(thuocModel),
                    tienDichVu, tienThuoc, tong);
            showMessage("Đã xuất hóa đơn PDF:\n" + selectedFile.getAbsolutePath());
        } catch (IOException | SecurityException exception) {
            JOptionPane.showMessageDialog(this,
                    "Không thể xuất hóa đơn PDF:\n" + exception.getMessage(),
                    "Lỗi xuất hóa đơn", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<String[]> layDongBang(DefaultTableModel model) {
        List<String[]> rows = new ArrayList<>();
        for (int row = 0; row < model.getRowCount(); row++) {
            String[] values = new String[model.getColumnCount()];
            for (int column = 0; column < model.getColumnCount(); column++) {
                values[column] = String.valueOf(model.getValueAt(row, column));
            }
            rows.add(values);
        }
        return rows;
    }

    // Nạp lại combo các lần khám chờ lập hóa đơn.
    private void napComboLanKham() {
        dangNapCombo = true;
        cboLanKham.removeAllItems();
        for (String[] lk : dsLanKham) {
            cboLanKham.addItem(lk[0] + " - " + lk[2] + " (" + lk[1] + ")");
        }
        dangNapCombo = false;
    }

    private void capNhatTongTien() {
        lblTienDichVu.setText(TIEN.format(tienDichVu) + " đ");
        lblTienThuoc.setText(TIEN.format(tienThuoc) + " đ");
        lblTongTien.setText(TIEN.format(tienDichVu + tienThuoc) + " đ");
        capNhatPhuongThuc();
    }

    // Hiển thị lịch chia đợt khi chọn trả góp (chia đều, đợt cuối nhận phần dư).
    private void loadBangDot() {
        dotModel.setRowCount(0);
        if (!TRA_GOP.equals(cboPhuongThuc.getSelectedItem())) {
            return;
        }
        double tong = tienDichVu + tienThuoc;
        if (tong <= 0) {
            return;
        }
        int soDot = (Integer) spnSoDot.getValue();
        double moiDot = Math.floor(tong / soDot);
        for (int i = 0; i < soDot; i++) {
            double soTien = (i == soDot - 1) ? tong - moiDot * (soDot - 1) : moiDot;
            dotModel.addRow(new Object[] {
                    i + 1,
                    LocalDate.now().plusMonths(i).format(NGAY),
                    TIEN.format(soTien),
                    i == 0 ? DOT_DA_THANH_TOAN : DOT_CHUA_DEN_HAN });
        }
    }

    private Object[] timDichVu(String maDichVu) {
        for (Object[] dv : MOCK_DICH_VU) {
            if (dv[0].equals(maDichVu)) {
                return dv;
            }
        }
        return null;
    }

    private Object[] timThuoc(String maVatTu) {
        for (Object[] t : MOCK_THUOC) {
            if (t[0].equals(maVatTu)) {
                return t;
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

    private JPanel createCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(16, 18, 16, 18)));
        return card;
    }

    private JLabel createCardTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 15));
        label.setForeground(INK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    // Một hàng "nhãn ... số tiền" căn hai đầu.
    private JPanel createMoneyRow(String caption, JLabel value, int size, Color color) {
        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(new Font("Arial", Font.BOLD, 11));
        captionLabel.setForeground(MUTED);
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

    private JScrollPane createScroll(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        scroll.setPreferredSize(new Dimension(100, 130));
        return scroll;
    }

    /**
     * Một hàng "nhãn + ô nhập": nhãn rộng cố định bên trái, ô nhập giãn hết phần
     * còn lại.
     */
    private JPanel createRow(String labelText, Component field) {
        field.setFont(new Font("Arial", Font.PLAIN, 12));
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setForeground(INK);
        label.setPreferredSize(new Dimension(120, 32));

        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(new EmptyBorder(0, 0, 8, 0));
        row.add(label, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        return row;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(30);
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
}
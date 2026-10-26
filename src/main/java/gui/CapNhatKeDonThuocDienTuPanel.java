package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Panel "Kê đơn thuốc điện tử" (bước 6 trong luồng khám bệnh chính).
 *
 * Luồng xử lý:
 * 1. Chọn lần khám (ChanDoan nháp) đang khám ở ô trên cùng.
 * 2. Chọn thuốc trong danh mục (VatTu có LoaiVatTu = 'Thuốc'), nhập liều dùng,
 * đường dùng, số lượng rồi bấm "Thêm vào đơn" (bảng ChiTietDonThuoc).
 * 3. Nhập lời dặn chăm sóc (DonThuoc.LoiDanChamSoc), bấm "Lưu đơn thuốc".
 * Mỗi lần khám chỉ có tối đa 1 đơn thuốc (UQ_DonThuoc_ChanDoan), lưu lần nữa
 * là cập nhật. Một thuốc chỉ xuất hiện 1 dòng trong đơn (khóa chính
 * (MaDonThuoc, MaVatTu)), thêm lại cùng thuốc sẽ ghi đè dòng cũ.
 *
 * Kê đơn không trừ tồn kho; việc xuất kho làm ở menu "Ghi nhận vật tư tiêu
 * hao".
 *
 * Hiện tại dùng dữ liệu mẫu trong bộ nhớ. Chỗ cần nối DAO ghi "TODO DAO".
 */
public class CapNhatKeDonThuocDienTuPanel extends JPanel {
        private static final Color INK = new Color(32, 53, 68);
        private static final Color MUTED = new Color(119, 139, 151);
        private static final Color TEAL = new Color(26, 151, 151);
        private static final Color LINE = new Color(226, 234, 237);
        private static final Color RED = new Color(200, 70, 70);

        private static final DecimalFormat TIEN = new DecimalFormat("#,##0");
        private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        private static final String[] DUONG_DUNG = { "Uống", "Tiêm", "Bôi", "Ngậm", "Súc miệng" };

        // ----- Dữ liệu mẫu -----
        // TODO DAO: ChanDoanDAO.layDanhSachDangKham() + BenhNhanDAO thay cho dữ liệu
        // này.
        // {MaChanDoan, MaBenhNhan, HoTen, KetLuanChanDoan, ThongTinDiUng ("" nếu không
        // có)}
        private static final String[][] MOCK_LAN_KHAM = {
                        { "CD005", "BN003", "Đặng Quốc Khánh", "Viêm tủy răng số 46", "" },
                        { "CD006", "BN001", "Võ Thanh Hải", "Sâu răng số 16, cao răng nhiều", "Dị ứng Penicillin" },
                        { "CD007", "BN004", "Trần Bảo Ngọc", "Sâu răng sữa số 55 mức độ nhẹ", "" }
        };

        // TODO DAO: VatTuDAO.timThuoc(keyword) (LoaiVatTu = 'Thuoc') thay cho dữ liệu
        // này.
        // {MaVatTu, TenVatTu, DonViTinh, SoLuongTon, DonGiaBan, HanSuDung}
        private static final Object[][] MOCK_THUOC = {
                        { "VT001", "Amoxicillin 500mg", "Viên", 200, 5000.0, LocalDate.of(2027, 6, 30) },
                        { "VT002", "Paracetamol 500mg", "Viên", 500, 1500.0, LocalDate.of(2027, 12, 31) },
                        { "VT005", "Thuốc tê Lidocaine 2%", "Ống", 60, 0.0, LocalDate.of(2027, 2, 28) }
        };

        /** Một dòng thuốc trong đơn (ChiTietDonThuoc). */
        private static final class DongThuoc {
                String lieuDung;
                String duongDung;
                int soLuong;

                DongThuoc(String lieuDung, String duongDung, int soLuong) {
                        this.lieuDung = lieuDung;
                        this.duongDung = duongDung;
                        this.soLuong = soLuong;
                }
        }

        /** Một đơn thuốc (DonThuoc) kèm các dòng thuốc (maVatTu -> dòng). */
        private static final class DonThuoc {
                String ma;
                LocalDate ngayKe;
                String loiDan;
                final Map<String, DongThuoc> dong = new LinkedHashMap<>();
        }

        // maChanDoan -> đơn thuốc đã lưu (mỗi lần khám tối đa 1 đơn).
        private final Map<String, DonThuoc> donTheoChanDoan = new HashMap<>();
        // Các dòng thuốc đang soạn trên màn hình (chưa lưu): maVatTu -> dòng.
        private final Map<String, DongThuoc> dongDangSoan = new LinkedHashMap<>();
        private int nextDonThuoc = 5;

        // ----- Chọn lần khám -----
        private final JComboBox<String> cboLanKham = new JComboBox<>();
        private final JLabel lblBenhNhan = new JLabel(" ");
        private final JLabel lblKetLuan = new JLabel(" ");
        private final JLabel lblDiUng = new JLabel(" ");
        private final JLabel lblCoDon = new JLabel(" ");

        // ----- Danh mục thuốc -----
        private final JTextField txtTimThuoc = new JTextField();
        private final DefaultTableModel thuocModel = new DefaultTableModel(
                        new String[] { "Mã", "Tên thuốc", "ĐVT", "Tồn", "Giá bán (đ)" }, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                        return false;
                }
        };
        private final JTable thuocTable = new JTable(thuocModel);
        private final JTextField txtLieuDung = new JTextField();
        private final JComboBox<String> cboDuongDung = new JComboBox<>(DUONG_DUNG);
        private final JSpinner spnSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));

        // ----- Đơn thuốc -----
        private final JLabel lblNgayKe = new JLabel(LocalDate.now().format(NGAY));
        private final JTextArea txtLoiDan = new JTextArea(3, 20);
        private final DefaultTableModel donModel = new DefaultTableModel(
                        new String[] { "Mã", "Tên thuốc", "Liều dùng", "Đường dùng", "SL", "Thành tiền (đ)" }, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                        return false;
                }
        };
        private final JTable donTable = new JTable(donModel);
        private final JLabel lblTongTien = new JLabel("0 đ");

        public CapNhatKeDonThuocDienTuPanel() {
                setLayout(new BorderLayout(0, 16));
                setOpaque(false);

                taoDuLieuMau();

                add(createLanKhamCard(), BorderLayout.NORTH);

                JPanel center = new JPanel(new BorderLayout(16, 0));
                center.setOpaque(false);
                center.add(createThuocCard(), BorderLayout.WEST);
                center.add(createDonThuocCard(), BorderLayout.CENTER);
                add(center, BorderLayout.CENTER);

                loadThuoc();
                for (String[] lk : MOCK_LAN_KHAM) {
                        cboLanKham.addItem(lk[0] + " - " + lk[2] + " (" + lk[1] + ")");
                }
                // Gắn listener sau khi đã thêm item để không bị kích hoạt sớm.
                cboLanKham.addActionListener(event -> chonLanKham());
                chonLanKham();
        }

        // =====================================================================
        // DỮ LIỆU MẪU
        // =====================================================================

        private void taoDuLieuMau() {
                // Lần khám CD007 đã có sẵn đơn thuốc để thử chức năng sửa.
                DonThuoc don = new DonThuoc();
                don.ma = "DT004";
                don.ngayKe = LocalDate.now();
                don.loiDan = "Vệ sinh răng miệng nhẹ nhàng";
                don.dong.put("VT002", new DongThuoc("1 viên khi đau", "Uống", 6));
                donTheoChanDoan.put("CD007", don);
        }

        // =====================================================================
        // GIAO DIỆN
        // =====================================================================

        private JPanel createLanKhamCard() {
                JPanel card = createCard();
                card.add(createCardTitle("Lần khám cần kê đơn"));
                card.add(Box.createVerticalStrut(10));

                cboLanKham.setFont(new Font("Arial", Font.PLAIN, 12));
                card.add(createRow("Lần khám (nháp)", cboLanKham));

                lblBenhNhan.setFont(new Font("Arial", Font.BOLD, 12));
                lblBenhNhan.setForeground(INK);
                lblKetLuan.setFont(new Font("Arial", Font.PLAIN, 12));
                lblKetLuan.setForeground(INK);
                lblDiUng.setFont(new Font("Arial", Font.BOLD, 12));
                lblCoDon.setFont(new Font("Arial", Font.ITALIC, 11));
                lblCoDon.setForeground(MUTED);
                for (JLabel label : new JLabel[] { lblBenhNhan, lblKetLuan, lblDiUng, lblCoDon }) {
                        label.setAlignmentX(Component.LEFT_ALIGNMENT);
                        card.add(label);
                        card.add(Box.createVerticalStrut(4));
                }
                return card;
        }

        // Danh mục thuốc bên trái.
        private JPanel createThuocCard() {
                JPanel card = new JPanel(new BorderLayout(0, 12));
                card.setBackground(Color.WHITE);
                card.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(LINE), new EmptyBorder(16, 18, 16, 18)));
                card.setPreferredSize(new Dimension(470, 0));

                // Tiêu đề + ô tìm kiếm.
                JPanel top = new JPanel();
                top.setOpaque(false);
                top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
                top.add(createCardTitle("Danh mục thuốc"));
                top.add(Box.createVerticalStrut(8));

                styleField(txtTimThuoc);
                txtTimThuoc.setToolTipText("Nhập mã hoặc tên thuốc");
                txtTimThuoc.addActionListener(event -> loadThuoc());
                JButton btnTim = createButton("Tìm", false);
                btnTim.addActionListener(event -> loadThuoc());
                JPanel searchRow = new JPanel(new BorderLayout(8, 0));
                searchRow.setOpaque(false);
                searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
                searchRow.add(txtTimThuoc, BorderLayout.CENTER);
                searchRow.add(btnTim, BorderLayout.EAST);
                searchRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
                top.add(searchRow);
                card.add(top, BorderLayout.NORTH);

                styleTable(thuocTable);
                thuocTable.getColumnModel().getColumn(0).setPreferredWidth(55);
                thuocTable.getColumnModel().getColumn(1).setPreferredWidth(170);
                thuocTable.getColumnModel().getColumn(2).setPreferredWidth(45);
                thuocTable.getColumnModel().getColumn(3).setPreferredWidth(45);
                thuocTable.getColumnModel().getColumn(4).setPreferredWidth(80);
                JScrollPane scroll = new JScrollPane(thuocTable);
                scroll.setBorder(BorderFactory.createLineBorder(LINE));
                scroll.getViewport().setBackground(Color.WHITE);
                card.add(scroll, BorderLayout.CENTER);

                // Liều dùng, đường dùng, số lượng + nút thêm.
                JPanel bottom = new JPanel();
                bottom.setOpaque(false);
                bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
                bottom.add(createRow("Liều dùng *", txtLieuDung));
                bottom.add(createRow("Đường dùng", cboDuongDung));
                bottom.add(createRow("Số lượng", spnSoLuong));
                JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
                actions.setOpaque(false);
                actions.setAlignmentX(Component.LEFT_ALIGNMENT);
                JButton btnThem = createButton("Thêm vào đơn", true);
                btnThem.addActionListener(event -> themThuoc());
                actions.add(btnThem);
                actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                bottom.add(actions);
                card.add(bottom, BorderLayout.SOUTH);
                return card;
        }

        // Đơn thuốc bên phải.
        private JPanel createDonThuocCard() {
                JPanel card = new JPanel(new BorderLayout(0, 12));
                card.setBackground(Color.WHITE);
                card.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(LINE), new EmptyBorder(16, 18, 16, 18)));

                // Phần đầu: ngày kê + lời dặn.
                JPanel form = new JPanel();
                form.setOpaque(false);
                form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
                form.add(createCardTitle("Đơn thuốc"));
                form.add(Box.createVerticalStrut(10));

                lblNgayKe.setFont(new Font("Arial", Font.BOLD, 12));
                lblNgayKe.setForeground(INK);
                form.add(createRow("Ngày kê", lblNgayKe));

                txtLoiDan.setFont(new Font("Arial", Font.PLAIN, 12));
                txtLoiDan.setForeground(INK);
                txtLoiDan.setLineWrap(true);
                txtLoiDan.setWrapStyleWord(true);
                txtLoiDan.setBorder(new EmptyBorder(6, 8, 6, 8));
                JScrollPane loiDanScroll = new JScrollPane(txtLoiDan);
                loiDanScroll.setBorder(BorderFactory.createLineBorder(LINE));
                loiDanScroll.setPreferredSize(new Dimension(100, 66));
                form.add(createRow("Lời dặn", loiDanScroll));
                form.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

                JLabel dsTitle = createSmallLabel("Thuốc trong đơn");
                dsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
                form.add(dsTitle);
                card.add(form, BorderLayout.NORTH);

                // Bảng thuốc trong đơn.
                styleTable(donTable);
                donTable.getColumnModel().getColumn(0).setPreferredWidth(50);
                donTable.getColumnModel().getColumn(1).setPreferredWidth(140);
                donTable.getColumnModel().getColumn(2).setPreferredWidth(110);
                donTable.getColumnModel().getColumn(3).setPreferredWidth(70);
                donTable.getColumnModel().getColumn(4).setPreferredWidth(40);
                donTable.getColumnModel().getColumn(5).setPreferredWidth(90);
                JScrollPane scroll = new JScrollPane(donTable);
                scroll.setBorder(BorderFactory.createLineBorder(LINE));
                scroll.getViewport().setBackground(Color.WHITE);
                card.add(scroll, BorderLayout.CENTER);

                // Tổng tiền thuốc + nút thao tác.
                JPanel bottom = new JPanel(new BorderLayout(0, 10));
                bottom.setOpaque(false);

                JPanel totalRow = new JPanel(new BorderLayout());
                totalRow.setOpaque(false);
                JLabel totalCaption = new JLabel("TỔNG TIỀN THUỐC");
                totalCaption.setFont(new Font("Arial", Font.BOLD, 11));
                totalCaption.setForeground(MUTED);
                lblTongTien.setFont(new Font("Arial", Font.BOLD, 20));
                lblTongTien.setForeground(TEAL);
                totalRow.add(totalCaption, BorderLayout.WEST);
                totalRow.add(lblTongTien, BorderLayout.EAST);
                bottom.add(totalRow, BorderLayout.NORTH);

                JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
                actions.setOpaque(false);
                JButton btnXoa = createButton("Xóa dòng thuốc", false);
                JButton btnLamMoi = createButton("Làm mới", false);
                JButton btnLuu = createButton("Lưu đơn thuốc", true);
                btnXoa.addActionListener(event -> xoaThuoc());
                btnLamMoi.addActionListener(event -> chonLanKham());
                btnLuu.addActionListener(event -> luuDonThuoc());
                actions.add(btnXoa);
                actions.add(btnLamMoi);
                actions.add(btnLuu);
                bottom.add(actions, BorderLayout.SOUTH);
                card.add(bottom, BorderLayout.SOUTH);
                return card;
        }

        // =====================================================================
        // XỬ LÝ SỰ KIỆN
        // =====================================================================

        /** Chọn lần khám: nạp đơn đã lưu (nếu có) hoặc xóa trắng form. */
        private void chonLanKham() {
                int index = cboLanKham.getSelectedIndex();
                if (index < 0) {
                        return;
                }
                String[] lanKham = MOCK_LAN_KHAM[index];
                lblBenhNhan.setText("Bệnh nhân: " + lanKham[2] + "  ·  " + lanKham[1]);
                lblKetLuan.setText("Kết luận chẩn đoán: " + lanKham[3]);
                if (lanKham[4].isEmpty()) {
                        lblDiUng.setText("Dị ứng: không ghi nhận");
                        lblDiUng.setForeground(MUTED);
                } else {
                        lblDiUng.setText("CẢNH BÁO DỊ ỨNG: " + lanKham[4]);
                        lblDiUng.setForeground(RED);
                }

                dongDangSoan.clear();
                txtLieuDung.setText("");
                cboDuongDung.setSelectedIndex(0);
                spnSoLuong.setValue(1);

                // TODO DAO: DonThuocDAO.timTheoChanDoan(maChanDoan) + ChiTietDonThuocDAO
                DonThuoc don = donTheoChanDoan.get(lanKham[0]);
                if (don == null) {
                        lblCoDon.setText("Lần khám này chưa có đơn thuốc. Lưu sẽ tạo đơn mới.");
                        lblNgayKe.setText(LocalDate.now().format(NGAY));
                        txtLoiDan.setText("");
                } else {
                        lblCoDon.setText("Đã có đơn thuốc " + don.ma + ". Lưu sẽ cập nhật đơn này.");
                        lblNgayKe.setText(don.ngayKe.format(NGAY));
                        txtLoiDan.setText(don.loiDan);
                        for (Map.Entry<String, DongThuoc> entry : don.dong.entrySet()) {
                                DongThuoc d = entry.getValue();
                                dongDangSoan.put(entry.getKey(), new DongThuoc(d.lieuDung, d.duongDung, d.soLuong));
                        }
                }
                loadDonThuoc();
        }

        /** Thêm thuốc đang chọn vào đơn (ghi đè nếu thuốc đã có trong đơn). */
        private void themThuoc() {
                int row = thuocTable.getSelectedRow();
                if (row < 0) {
                        showMessage("Vui lòng chọn một loại thuốc trong danh mục.");
                        return;
                }
                String maVatTu = (String) thuocModel.getValueAt(row, 0);
                Object[] thuoc = timThuoc(maVatTu);

                String lieuDung = txtLieuDung.getText().trim();
                if (lieuDung.isEmpty()) {
                        showWarning("Liều dùng không được để trống.");
                        txtLieuDung.requestFocusInWindow();
                        return;
                }
                if (lieuDung.length() > 100) {
                        showWarning("Liều dùng tối đa 100 ký tự.");
                        txtLieuDung.requestFocusInWindow();
                        return;
                }

                int soLuong = (Integer) spnSoLuong.getValue();
                int ton = (Integer) thuoc[3];
                if (soLuong > ton) {
                        showWarning("Số lượng kê (" + soLuong + ") vượt quá tồn kho (" + ton + ").");
                        return;
                }
                LocalDate hanSuDung = (LocalDate) thuoc[5];
                if (hanSuDung != null && hanSuDung.isBefore(LocalDate.now())) {
                        showWarning("Thuốc " + thuoc[1] + " đã hết hạn sử dụng.");
                        return;
                }

                dongDangSoan.put(maVatTu, new DongThuoc(lieuDung, (String) cboDuongDung.getSelectedItem(), soLuong));
                txtLieuDung.setText("");
                spnSoLuong.setValue(1);
                loadDonThuoc();
        }

        /** Bỏ dòng thuốc đang chọn ra khỏi đơn. */
        private void xoaThuoc() {
                int row = donTable.getSelectedRow();
                if (row < 0) {
                        showMessage("Vui lòng chọn một dòng thuốc trong đơn để xóa.");
                        return;
                }
                dongDangSoan.remove((String) donModel.getValueAt(row, 0));
                loadDonThuoc();
        }

        /** Kiểm tra dữ liệu rồi lưu đơn thuốc cho lần khám đang chọn. */
        private void luuDonThuoc() {
                int index = cboLanKham.getSelectedIndex();
                if (index < 0) {
                        showMessage("Vui lòng chọn lần khám.");
                        return;
                }
                if (dongDangSoan.isEmpty()) {
                        showWarning("Đơn thuốc cần có ít nhất một loại thuốc.");
                        return;
                }
                String loiDan = txtLoiDan.getText().trim();
                if (loiDan.length() > 500) {
                        showWarning("Lời dặn chăm sóc tối đa 500 ký tự.");
                        txtLoiDan.requestFocusInWindow();
                        return;
                }

                // Có dị ứng thì nhắc nha sĩ kiểm tra lại trước khi lưu.
                String diUng = MOCK_LAN_KHAM[index][4];
                if (!diUng.isEmpty()) {
                        int choice = JOptionPane.showConfirmDialog(this,
                                        "Bệnh nhân có ghi nhận: " + diUng
                                                        + ".\nBạn đã kiểm tra các thuốc trong đơn chưa?",
                                        "Cảnh báo dị ứng", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (choice != JOptionPane.YES_OPTION) {
                                return;
                        }
                }

                String maChanDoan = MOCK_LAN_KHAM[index][0];
                DonThuoc don = donTheoChanDoan.get(maChanDoan);
                boolean taoMoi = don == null;
                if (taoMoi) {
                        don = new DonThuoc();
                        // TODO DAO: sinh mã từ DB.
                        don.ma = String.format("DT%03d", nextDonThuoc++);
                        don.ngayKe = LocalDate.now();
                }
                don.loiDan = loiDan;
                don.dong.clear();
                for (Map.Entry<String, DongThuoc> entry : dongDangSoan.entrySet()) {
                        DongThuoc d = entry.getValue();
                        don.dong.put(entry.getKey(), new DongThuoc(d.lieuDung, d.duongDung, d.soLuong));
                }

                // TODO DAO (một transaction):
                // 1. Tạo mới: DonThuocDAO.them(maDonThuoc, ngayKe, loiDan, maChanDoan)
                // Cập nhật: DonThuocDAO.capNhatLoiDan(maDonThuoc, loiDan)
                // 2. ChiTietDonThuocDAO.xoaTheoDon(maDonThuoc) rồi them lại từng dòng
                // (maDonThuoc, maVatTu, lieuDung, duongDung, soLuong)
                donTheoChanDoan.put(maChanDoan, don);

                lblCoDon.setText("Đã có đơn thuốc " + don.ma + ". Lưu sẽ cập nhật đơn này.");
                showMessage((taoMoi ? "Đã tạo đơn thuốc " : "Đã cập nhật đơn thuốc ") + don.ma
                                + ". Tổng tiền thuốc: " + lblTongTien.getText() + ".");
        }

        // =====================================================================
        // HÀM HỖ TRỢ
        // =====================================================================

        // Nạp danh mục thuốc, lọc theo từ khóa (mã hoặc tên).
        private void loadThuoc() {
                String keyword = txtTimThuoc.getText().trim().toLowerCase();
                thuocModel.setRowCount(0);
                for (Object[] t : MOCK_THUOC) {
                        String ma = ((String) t[0]).toLowerCase();
                        String ten = ((String) t[1]).toLowerCase();
                        if (keyword.isEmpty() || ma.contains(keyword) || ten.contains(keyword)) {
                                thuocModel.addRow(new Object[] { t[0], t[1], t[2], t[3], TIEN.format((Double) t[4]) });
                        }
                }
        }

        // Nạp lại bảng thuốc trong đơn và tính tổng tiền.
        private void loadDonThuoc() {
                donModel.setRowCount(0);
                double tong = 0;
                for (Map.Entry<String, DongThuoc> entry : dongDangSoan.entrySet()) {
                        Object[] thuoc = timThuoc(entry.getKey());
                        if (thuoc == null) {
                                continue;
                        }
                        DongThuoc d = entry.getValue();
                        double thanhTien = (Double) thuoc[4] * d.soLuong;
                        tong += thanhTien;
                        donModel.addRow(new Object[] {
                                        thuoc[0], thuoc[1], d.lieuDung, d.duongDung, d.soLuong,
                                        TIEN.format(thanhTien) });
                }
                lblTongTien.setText(TIEN.format(tong) + " đ");
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

        private JLabel createSmallLabel(String text) {
                JLabel label = new JLabel(text);
                label.setFont(new Font("Arial", Font.BOLD, 11));
                label.setForeground(MUTED);
                return label;
        }

        /**
         * Một hàng "nhãn + ô nhập": nhãn rộng cố định bên trái, ô nhập giãn hết phần
         * còn lại.
         */
        private JPanel createRow(String labelText, Component field) {
                if (field instanceof JTextField) {
                        styleField((JTextField) field);
                } else if (!(field instanceof JScrollPane)) {
                        field.setFont(new Font("Arial", Font.PLAIN, 12));
                }
                JLabel label = new JLabel(labelText);
                label.setFont(new Font("Arial", Font.PLAIN, 12));
                label.setForeground(INK);
                label.setPreferredSize(new Dimension(110, 32));

                JPanel row = new JPanel(new BorderLayout(8, 0));
                row.setOpaque(false);
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                row.setBorder(new EmptyBorder(0, 0, 8, 0));
                row.add(label, BorderLayout.WEST);
                row.add(field, BorderLayout.CENTER);
                int cao = field instanceof JScrollPane ? 82 : 42;
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, cao));
                return row;
        }

        private void styleField(JTextField field) {
                field.setFont(new Font("Arial", Font.PLAIN, 12));
                field.setForeground(INK);
                field.setPreferredSize(new Dimension(160, 32));
                field.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(LINE), new EmptyBorder(0, 8, 0, 8)));
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
}
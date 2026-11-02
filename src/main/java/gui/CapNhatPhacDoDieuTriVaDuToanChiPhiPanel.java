package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import dao.ChanDoanDAO;
import dao.DichVuDAO;
import dao.KeHoachDieuTriDAO;
import dao.KeHoachDieuTri_DichVuDAO;
import dto.LanKham;
import entity.ChiTietKeHoachDieuTriDichVu;
import entity.DichVu;
import entity.KeHoachDieuTri;

/**
 * Panel "Phác đồ điều trị và dự toán chi phí" (bước 6 trong luồng khám bệnh
 * chính).
 *
 * Luồng xử lý:
 * 1. Chọn lần khám (ChanDoan nháp) đang khám ở ô trên cùng.
 * 2. Nhập tên phác đồ, thời gian dự kiến, trạng thái (bảng KeHoachDieuTri).
 * 3. Chọn dịch vụ ở danh mục bên trái, nhập số lượng, bấm "Thêm vào phác đồ"
 * (bảng KeHoachDieuTri_DichVu). Tổng dự toán = tổng (đơn giá x số lượng).
 * 4. Bấm "Lưu phác đồ". Mỗi lần khám chỉ có tối đa 1 phác đồ
 * (UQ_KeHoachDieuTri_ChanDoan), lưu lại lần nữa là cập nhật.
 *
 * Lần khám, dịch vụ và phác đồ được đọc/ghi trực tiếp qua DAO.
 */
public class CapNhatPhacDoDieuTriVaDuToanChiPhiPanel extends JPanel {
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color LINE = new Color(226, 234, 237);
    private static final DecimalFormat TIEN = new DecimalFormat("#,##0");

    // Trạng thái hiển thị và trạng thái lưu trong CSDL.
    private static final String DANG_THUC_HIEN = "Đang thực hiện";
    private static final String HOAN_THANH = "Hoàn thành";
    private static final String HUY = "Hủy";

    private final ChanDoanDAO chanDoanDAO = new ChanDoanDAO();
    private final DichVuDAO dichVuDAO = new DichVuDAO();
    private final KeHoachDieuTriDAO keHoachDieuTriDAO = new KeHoachDieuTriDAO();
    private final KeHoachDieuTri_DichVuDAO chiTietKeHoachDAO = new KeHoachDieuTri_DichVuDAO();
    private final List<LanKham> danhSachLanKham = new ArrayList<>();
    private final Map<String, DichVu> danhMucDichVu = new LinkedHashMap<>();
    // Danh sách dịch vụ đang chọn trên màn hình (chưa lưu): maDichVu -> số lượng.
    private final Map<String, Integer> dichVuDangChon = new LinkedHashMap<>();

    // ----- Chọn lần khám -----
    private final JComboBox<String> cboLanKham = new JComboBox<>();
    private final JLabel lblBenhNhan = new JLabel(" ");
    private final JLabel lblKetLuan = new JLabel(" ");
    private final JLabel lblCoPhacDo = new JLabel(" ");

    // ----- Danh mục dịch vụ -----
    private final JTextField txtTimDichVu = new JTextField();
    private final DefaultTableModel dichVuModel = new DefaultTableModel(
            new String[] { "Mã", "Tên dịch vụ", "Đơn giá (đ)", "Bảo hành (tháng)" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable dichVuTable = new JTable(dichVuModel);
    private final JSpinner spnSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));

    // ----- Phác đồ -----
    private final JTextField txtTenPhacDo = new JTextField();
    private final JSpinner spnThoiGian = new JSpinner(new SpinnerNumberModel(7, 1, 365, 1));
    private final JComboBox<String> cboTrangThai = new JComboBox<>(new String[] { DANG_THUC_HIEN, HOAN_THANH, HUY });
    private final DefaultTableModel chonModel = new DefaultTableModel(
            new String[] { "Mã", "Tên dịch vụ", "Đơn giá (đ)", "SL", "Thành tiền (đ)" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable chonTable = new JTable(chonModel);
    private final JLabel lblTongTien = new JLabel("0 đ");
    private boolean dangNapLanKham;

    public CapNhatPhacDoDieuTriVaDuToanChiPhiPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);

        add(createLanKhamCard(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(16, 0));
        center.setOpaque(false);
        center.add(createDichVuCard(), BorderLayout.WEST);
        center.add(createPhacDoCard(), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        loadDichVu();
        taiDanhSachLanKham();
        // Gắn listener sau khi đã thêm item để không bị kích hoạt sớm.
        cboLanKham.addActionListener(event -> {
            if (!dangNapLanKham) {
                chonLanKham();
            }
        });
        chonLanKham();
    }

    // =====================================================================
    // GIAO DIỆN
    // =====================================================================

    private JPanel createLanKhamCard() {
        JPanel card = createCard();
        card.add(createCardTitle("Lần khám cần lập phác đồ"));
        card.add(Box.createVerticalStrut(10));

        cboLanKham.setFont(new Font("Arial", Font.PLAIN, 12));
        card.add(createRow("Lần khám (nháp)", cboLanKham));

        lblBenhNhan.setFont(new Font("Arial", Font.BOLD, 12));
        lblBenhNhan.setForeground(INK);
        lblKetLuan.setFont(new Font("Arial", Font.PLAIN, 12));
        lblKetLuan.setForeground(INK);
        lblCoPhacDo.setFont(new Font("Arial", Font.ITALIC, 11));
        lblCoPhacDo.setForeground(MUTED);
        for (JLabel label : new JLabel[] { lblBenhNhan, lblKetLuan, lblCoPhacDo }) {
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(label);
            card.add(Box.createVerticalStrut(4));
        }
        return card;
    }

    // Danh mục dịch vụ bên trái.
    private JPanel createDichVuCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(16, 18, 16, 18)));
        card.setPreferredSize(new Dimension(470, 0));

        // Tiêu đề + ô tìm kiếm.
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(createCardTitle("Danh mục dịch vụ"));
        top.add(Box.createVerticalStrut(8));

        styleField(txtTimDichVu);
        txtTimDichVu.setToolTipText("Nhập mã hoặc tên dịch vụ");
        txtTimDichVu.addActionListener(event -> loadDichVu());
        JButton btnTim = createButton("Tìm", false);
        btnTim.addActionListener(event -> loadDichVu());
        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchRow.add(txtTimDichVu, BorderLayout.CENTER);
        searchRow.add(btnTim, BorderLayout.EAST);
        searchRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        top.add(searchRow);
        card.add(top, BorderLayout.NORTH);

        styleTable(dichVuTable);
        dichVuTable.getColumnModel().getColumn(0).setPreferredWidth(55);
        dichVuTable.getColumnModel().getColumn(1).setPreferredWidth(170);
        dichVuTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        dichVuTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        JScrollPane scroll = new JScrollPane(dichVuTable);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        // Số lượng + nút thêm.
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        spnSoLuong.setFont(new Font("Arial", Font.PLAIN, 12));
        spnSoLuong.setPreferredSize(new Dimension(70, 32));
        JButton btnThem = createButton("Thêm vào phác đồ", true);
        btnThem.addActionListener(event -> themDichVu());
        actions.add(createSmallLabel("Số lượng:"));
        actions.add(spnSoLuong);
        actions.add(btnThem);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    // Phác đồ + dịch vụ đã chọn + dự toán bên phải.
    private JPanel createPhacDoCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(16, 18, 16, 18)));

        // Phần form phác đồ.
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(createCardTitle("Phác đồ điều trị"));
        form.add(Box.createVerticalStrut(10));
        form.add(createRow("Tên phác đồ *", txtTenPhacDo));
        form.add(createRow("Thời gian (ngày)", spnThoiGian));
        form.add(createRow("Trạng thái", cboTrangThai));
        JLabel dsTitle = createSmallLabel("Dịch vụ trong phác đồ");
        dsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(dsTitle);
        card.add(form, BorderLayout.NORTH);

        // Bảng dịch vụ đã chọn.
        styleTable(chonTable);
        chonTable.getColumnModel().getColumn(0).setPreferredWidth(55);
        chonTable.getColumnModel().getColumn(1).setPreferredWidth(170);
        chonTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        chonTable.getColumnModel().getColumn(3).setPreferredWidth(40);
        chonTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        JScrollPane scroll = new JScrollPane(chonTable);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        // Tổng dự toán + nút thao tác.
        JPanel bottom = new JPanel(new BorderLayout(0, 10));
        bottom.setOpaque(false);

        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        JLabel totalCaption = new JLabel("TỔNG DỰ TOÁN CHI PHÍ");
        totalCaption.setFont(new Font("Arial", Font.BOLD, 11));
        totalCaption.setForeground(MUTED);
        lblTongTien.setFont(new Font("Arial", Font.BOLD, 20));
        lblTongTien.setForeground(TEAL);
        totalRow.add(totalCaption, BorderLayout.WEST);
        totalRow.add(lblTongTien, BorderLayout.EAST);
        bottom.add(totalRow, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton btnXoa = createButton("Xóa dịch vụ", false);
        JButton btnXoaPhacDo = createButton("Xóa phác đồ", false);
        JButton btnLamMoi = createButton("Làm mới", false);
        JButton btnLuu = createButton("Lưu phác đồ", true);
        btnXoa.addActionListener(event -> xoaDichVu());
        btnLamMoi.addActionListener(event -> taiDanhSachLanKham());
        btnLuu.addActionListener(event -> luuPhacDo());
        actions.add(btnXoa);
        actions.add(btnXoaPhacDo);
        actions.add(btnLamMoi);
        actions.add(btnLuu);
        bottom.add(actions, BorderLayout.SOUTH);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    // =====================================================================
    // XỬ LÝ SỰ KIỆN
    // =====================================================================

    private void taiDanhSachLanKham() {
        String maDangChon = layLanKhamDangChon() == null ? null : layLanKhamDangChon().maChanDoan();
        dangNapLanKham = true;
        cboLanKham.removeAllItems();
        danhSachLanKham.clear();
        try {
            danhSachLanKham.addAll(chanDoanDAO.layThongTinDangKham());
            for (LanKham lanKham : danhSachLanKham) {
                cboLanKham.addItem(lanKham.maChanDoan() + " - " + lanKham.hoTen()
                        + " (" + lanKham.maBenhNhan() + ")");
            }
            if (maDangChon != null) {
                for (int i = 0; i < danhSachLanKham.size(); i++) {
                    if (maDangChon.equals(danhSachLanKham.get(i).maChanDoan())) {
                        cboLanKham.setSelectedIndex(i);
                        break;
                    }
                }
            }
            if (cboLanKham.getSelectedIndex() < 0 && !danhSachLanKham.isEmpty()) {
                cboLanKham.setSelectedIndex(0);
            }
        } catch (SQLException | IllegalStateException ex) {
            danhSachLanKham.clear();
            showError("Không thể tải danh sách lần khám", ex);
        } finally {
            dangNapLanKham = false;
        }
        if (danhSachLanKham.isEmpty()) {
            hienThiKhongCoLanKham();
        } else {
            chonLanKham();
        }
    }

    /** Chọn lần khám: nạp phác đồ đã lưu (nếu có) hoặc xóa trắng form. */
    private void chonLanKham() {
        int index = cboLanKham.getSelectedIndex();
        if (index < 0 || index >= danhSachLanKham.size()) {
            hienThiKhongCoLanKham();
            return;
        }
        LanKham lanKham = danhSachLanKham.get(index);
        lblBenhNhan.setText("Bệnh nhân: " + lanKham.hoTen() + "  ·  " + lanKham.maBenhNhan());
        String ketLuan = lanKham.ketLuan();
        lblKetLuan.setText("Kết luận chẩn đoán: "
                + (ketLuan == null || ketLuan.trim().isEmpty() ? "Chưa nhập" : ketLuan));

        dichVuDangChon.clear();
        try {
            KeHoachDieuTri keHoach = keHoachDieuTriDAO.timTheoChanDoan(lanKham.maChanDoan());
            if (keHoach == null) {
                lblCoPhacDo.setText("Lần khám này chưa có phác đồ. Lưu sẽ tạo phác đồ mới.");
                txtTenPhacDo.setText("");
                spnThoiGian.setValue(7);
                cboTrangThai.setSelectedItem(DANG_THUC_HIEN);
            } else {
                lblCoPhacDo.setText("Đã có phác đồ " + keHoach.getMaKeHoach()
                        + ". Lưu sẽ cập nhật phác đồ này.");
                txtTenPhacDo.setText(keHoach.getTenPhacDo());
                if (keHoach.getThoiGianDuKien() != null) {
                    spnThoiGian.setValue(keHoach.getThoiGianDuKien());
                }
                cboTrangThai.setSelectedItem(hienThiTrangThai(keHoach.getTrangThai()));
                for (ChiTietKeHoachDieuTriDichVu chiTiet
                        : chiTietKeHoachDAO.layTheoKeHoach(keHoach.getMaKeHoach())) {
                    dichVuDangChon.put(chiTiet.getMaDichVu(), chiTiet.getSoLuong());
                }
            }
        } catch (SQLException | IllegalStateException ex) {
            showError("Không thể tải phác đồ của lần khám", ex);
        }
        loadDichVuDaChon();
    }

    /** Thêm dịch vụ đang chọn ở danh mục vào phác đồ (cộng dồn nếu đã có). */
    private void themDichVu() {
        int row = dichVuTable.getSelectedRow();
        if (row < 0) {
            showMessage("Vui lòng chọn một dịch vụ trong danh mục.");
            return;
        }
        String maDichVu = (String) dichVuModel.getValueAt(row, 0);
        int soLuong = (Integer) spnSoLuong.getValue();
        try {
            dichVuDangChon.merge(maDichVu, soLuong, Math::addExact);
        } catch (ArithmeticException e) {
            showWarning("Tổng số lượng dịch vụ vượt giới hạn cho phép.");
            return;
        }
        spnSoLuong.setValue(1);
        loadDichVuDaChon();
    }

    /** Bỏ dịch vụ đang chọn ra khỏi phác đồ. */
    private void xoaDichVu() {
        int row = chonTable.getSelectedRow();
        if (row < 0) {
            showMessage("Vui lòng chọn một dịch vụ trong phác đồ để xóa.");
            return;
        }
        dichVuDangChon.remove((String) chonModel.getValueAt(row, 0));
        loadDichVuDaChon();
    }

    /** Kiểm tra dữ liệu rồi lưu phác đồ cho lần khám đang chọn. */
    private void luuPhacDo() {
        int index = cboLanKham.getSelectedIndex();
        if (index < 0 || index >= danhSachLanKham.size()) {
            showMessage("Vui lòng chọn lần khám.");
            return;
        }
        String ten = txtTenPhacDo.getText().trim();
        if (ten.isEmpty()) {
            showWarning("Tên phác đồ không được để trống.");
            txtTenPhacDo.requestFocusInWindow();
            return;
        }
        if (ten.length() > 150) {
            showWarning("Tên phác đồ tối đa 150 ký tự.");
            txtTenPhacDo.requestFocusInWindow();
            return;
        }
        if (dichVuDangChon.isEmpty()) {
            showWarning("Phác đồ cần có ít nhất một dịch vụ.");
            return;
        }
        if ((Integer) spnThoiGian.getValue() < 1) {
            showWarning("Thời gian dự kiến phải lớn hơn 0 ngày.");
            return;
        }

        LanKham lanKham = danhSachLanKham.get(index);
        try {
            KeHoachDieuTri keHoachCu = keHoachDieuTriDAO.timTheoChanDoan(lanKham.maChanDoan());
            KeHoachDieuTri keHoach = new KeHoachDieuTri();
            if (keHoachCu != null) {
                keHoach.setMaKeHoach(keHoachCu.getMaKeHoach());
            }
            keHoach.setTenPhacDo(ten);
            keHoach.setThoiGianDuKien((Integer) spnThoiGian.getValue());
            keHoach.setTrangThai(trangThaiTrongCoSoDuLieu((String) cboTrangThai.getSelectedItem()));
            keHoach.setMaChanDoan(lanKham.maChanDoan());
            keHoach.setMaBenhNhan(lanKham.maBenhNhan());
            List<String[]> chiTiet = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : dichVuDangChon.entrySet()) {
                chiTiet.add(new String[] { entry.getKey(), String.valueOf(entry.getValue()) });
            }
            String maKeHoach = keHoachDieuTriDAO.luuPhacDo(keHoach, chiTiet);
            lblCoPhacDo.setText("Đã có phác đồ " + maKeHoach + ". Lưu sẽ cập nhật phác đồ này.");
            showMessage((keHoachCu == null ? "Đã tạo phác đồ " : "Đã cập nhật phác đồ ") + maKeHoach
                    + ". Tổng dự toán: " + lblTongTien.getText() + ".");
        } catch (SQLException | IllegalStateException ex) {
            showError("Không thể lưu phác đồ", ex);
        }
    }

    // =====================================================================
    // HÀM HỖ TRỢ
    // =====================================================================

    // Lọc danh mục đã tải từ CSDL theo từ khóa (mã hoặc tên).
    private void loadDichVu() {
        String keyword = txtTimDichVu.getText().trim().toLowerCase();
        dichVuModel.setRowCount(0);
        try {
            danhMucDichVu.clear();
            for (DichVu dichVu : dichVuDAO.getAllDichVu()) {
                danhMucDichVu.put(dichVu.getMaDichVu(), dichVu);
                String ma = dichVu.getMaDichVu().toLowerCase();
                String ten = dichVu.getTenDichVu().toLowerCase();
                if (keyword.isEmpty() || ma.contains(keyword) || ten.contains(keyword)) {
                    dichVuModel.addRow(new Object[] {
                            dichVu.getMaDichVu(),
                            dichVu.getTenDichVu(),
                            TIEN.format(dichVu.getDonGia()),
                            dichVu.getThoiGianBaoHanh() == null ? "" : dichVu.getThoiGianBaoHanh()
                    });
                }
            }
        } catch (SQLException | IllegalStateException ex) {
            showError("Không thể tải danh mục dịch vụ", ex);
        }
    }

    // Nạp lại bảng dịch vụ đã chọn và tính tổng dự toán.
    private void loadDichVuDaChon() {
        chonModel.setRowCount(0);
        double tong = 0;
        for (Map.Entry<String, Integer> entry : dichVuDangChon.entrySet()) {
            DichVu dv = timDichVu(entry.getKey());
            if (dv == null) {
                continue;
            }
            double donGia = dv.getDonGia();
            int soLuong = entry.getValue();
            double thanhTien = donGia * soLuong;
            tong += thanhTien;
            chonModel.addRow(new Object[] {
                    dv.getMaDichVu(), dv.getTenDichVu(), TIEN.format(donGia), soLuong, TIEN.format(thanhTien) });
        }
        lblTongTien.setText(TIEN.format(tong) + " đ");
    }

    private DichVu timDichVu(String maDichVu) {
        return danhMucDichVu.get(maDichVu);
    }

    private LanKham layLanKhamDangChon() {
        int index = cboLanKham.getSelectedIndex();
        return index < 0 || index >= danhSachLanKham.size() ? null : danhSachLanKham.get(index);
    }

    private void hienThiKhongCoLanKham() {
        lblBenhNhan.setText("Không có bệnh nhân đang khám.");
        lblKetLuan.setText("");
        lblCoPhacDo.setText("");
        txtTenPhacDo.setText("");
        spnThoiGian.setValue(7);
        cboTrangThai.setSelectedItem(DANG_THUC_HIEN);
        dichVuDangChon.clear();
        loadDichVuDaChon();
    }

    private static String hienThiTrangThai(String trangThai) {
        if ("Dang thuc hien".equals(trangThai)) return DANG_THUC_HIEN;
        if ("Hoan thanh".equals(trangThai)) return HOAN_THANH;
        if ("Huy".equals(trangThai)) return HUY;
        return trangThai;
    }

    private static String trangThaiTrongCoSoDuLieu(String trangThai) {
        if (DANG_THUC_HIEN.equals(trangThai)) return KeHoachDieuTri.DANG_THUC_HIEN;
        if (HOAN_THANH.equals(trangThai)) return KeHoachDieuTri.HOAN_THANH;
        if (HUY.equals(trangThai)) return KeHoachDieuTri.HUY;
        throw new IllegalArgumentException("Trạng thái phác đồ không hợp lệ.");
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String action, Exception exception) {
        String detail = exception.getMessage();
        JOptionPane.showMessageDialog(this,
                action + ":\n" + (detail == null || detail.trim().isEmpty()
                        ? exception.getClass().getSimpleName()
                        : detail),
                "Lỗi cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
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
        } else {
            field.setFont(new Font("Arial", Font.PLAIN, 12));
        }
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
package gui;

import dao.VatTuDAO;
import entity.VatTu;
import util.GiaoDienUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Panel Quản lý thuốc (CRUD) phục vụ mục DANH MỤC → "Quản lý thuốc".
 * Kết nối trực tiếp với CSDL qua VatTuDAO (bảng VatTu, LoaiVatTu = 'Thuoc').
 *
 * Thiết kế Clean Code:
 * - Bên trái: Danh sách thuốc dạng bảng kèm bộ lọc tìm kiếm tức thì.
 * - Bên phải: Biểu mẫu thông tin thuốc để Thêm / Sửa / Xóa / Làm mới.
 * - Kiểm tra dữ liệu (validation) chặt chẽ trước khi lưu vào CSDL.
 */
public class DanhMucQuanLyThuocPanel extends JPanel {

    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat DINH_DANG_TIEN = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"));

    private final VatTuDAO vatTuDAO = new VatTuDAO();

    // Bảng và mô hình dữ liệu bảng
    private final DefaultTableModel modelBang = new DefaultTableModel(
            new String[] { "Mã thuốc", "Tên thuốc", "Đơn vị", "Tồn kho", "Ngưỡng tối thiểu", "Giá bán", "Hạn sử dụng" },
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            if (column == 5) return Double.class;
            if (column == 3 || column == 4) return Integer.class;
            return String.class;
        }
    };
    private final JTable bangThuoc = new JTable(modelBang);

    // Thành phần tìm kiếm
    private final JTextField txtTimKiem = new JTextField();
    private final JLabel lblSoLuong = new JLabel();

    // Các ô nhập liệu trên form
    private final JTextField txtMa = new JTextField();
    private final JTextField txtTen = new JTextField();
    private final JTextField txtDonVi = new JTextField();
    private final JTextField txtTon = new JTextField();
    private final JTextField txtNguong = new JTextField();
    private final JTextField txtGiaBan = new JTextField();
    private final JTextField txtHan = new JTextField();

    // Các nút chức năng
    private final JButton btnThem = GiaoDienUtil.taoNut("Thêm", true);
    private final JButton btnSua = GiaoDienUtil.taoNut("Sửa", false);
    private final JButton btnXoa = GiaoDienUtil.taoNut("Xóa", false);
    private final JButton btnLamMoi = GiaoDienUtil.taoNut("Làm mới", false);

    public DanhMucQuanLyThuocPanel() {
        setLayout(new BorderLayout(18, 0));
        setOpaque(false);

        thietLapBang();
        add(taoDanhSachThe(), BorderLayout.CENTER);
        add(taoBieuMauThe(), BorderLayout.EAST);

        taiDuLieu();
        lamMoiBieuMau();
    }

    // =========================================================================
    // 1. XÂY DỰNG GIAO DIỆN
    // =========================================================================

    /** Tạo thẻ danh sách bên trái: ô tìm kiếm + bảng danh sách thuốc. */
    private JPanel taoDanhSachThe() {
        JPanel the = new JPanel(new BorderLayout(0, 14));
        GiaoDienUtil.trangTriThe(the);

        // Thanh công cụ tìm kiếm
        JPanel thanhCongCu = new JPanel(new BorderLayout(10, 0));
        thanhCongCu.setOpaque(false);

        txtTimKiem.setPreferredSize(new Dimension(300, 34));
        txtTimKiem.setToolTipText("Nhập mã hoặc tên thuốc để lọc tức thì");
        GiaoDienUtil.tuyChinhO(txtTimKiem);
        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { taiDuLieu(); }
            @Override
            public void removeUpdate(DocumentEvent e) { taiDuLieu(); }
            @Override
            public void changedUpdate(DocumentEvent e) { taiDuLieu(); }
        });

        lblSoLuong.setFont(GiaoDienUtil.NHO_DAM);
        lblSoLuong.setForeground(GiaoDienUtil.MUTED);

        thanhCongCu.add(txtTimKiem, BorderLayout.WEST);
        thanhCongCu.add(lblSoLuong, BorderLayout.EAST);

        // Bảng dữ liệu có cuộn
        JScrollPane cuonBang = new JScrollPane(bangThuoc);
        cuonBang.setBorder(BorderFactory.createLineBorder(GiaoDienUtil.LINE));
        cuonBang.getViewport().setBackground(Color.WHITE);

        the.add(thanhCongCu, BorderLayout.NORTH);
        the.add(cuonBang, BorderLayout.CENTER);
        return the;
    }

    /** Tạo thẻ form thông tin thuốc bên phải: các trường nhập liệu và nút CRUD. */
    private JPanel taoBieuMauThe() {
        JPanel the = new JPanel(new BorderLayout(0, 14));
        GiaoDienUtil.trangTriThe(the);
        the.setPreferredSize(new Dimension(350, 0));

        the.add(GiaoDienUtil.taoTieuDe("Thông tin thuốc"), BorderLayout.NORTH);

        txtMa.setEditable(false);
        txtMa.setBackground(new Color(244, 247, 250));
        txtHan.setToolTipText("Định dạng dd/MM/yyyy (ví dụ: 30/12/2027)");
        txtGiaBan.setToolTipText("Giá bán cho bệnh nhân. Nhập 0 nếu chỉ dùng nội bộ");

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        form.add(taoHangDoi(taoO("Mã thuốc", txtMa), taoO("Đơn vị tính", txtDonVi)));
        form.add(Box.createVerticalStrut(10));
        form.add(taoO("Tên thuốc (*)", txtTen));
        form.add(Box.createVerticalStrut(10));
        form.add(taoHangDoi(taoO("Số lượng tồn", txtTon), taoO("Ngưỡng tối thiểu", txtNguong)));
        form.add(Box.createVerticalStrut(10));
        form.add(taoO("Giá bán (VNĐ)", txtGiaBan));
        form.add(Box.createVerticalStrut(10));
        form.add(taoO("Hạn sử dụng (dd/MM/yyyy)", txtHan));
        form.add(Box.createVerticalGlue());
        the.add(form, BorderLayout.CENTER);

        // Cụm nút thao tác
        JPanel thanhNut = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        thanhNut.setOpaque(false);
        thanhNut.add(btnThem);
        thanhNut.add(btnSua);
        thanhNut.add(btnXoa);
        thanhNut.add(btnLamMoi);
        the.add(thanhNut, BorderLayout.SOUTH);

        btnThem.addActionListener(e -> themThuoc());
        btnSua.addActionListener(e -> suaThuoc());
        btnXoa.addActionListener(e -> xoaThuoc());
        btnLamMoi.addActionListener(e -> lamMoiBieuMau());

        return the;
    }

    private JPanel taoO(String tieuDe, JComponent input) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel lbl = GiaoDienUtil.taoNhanPhu(tieuDe);
        input.setPreferredSize(new Dimension(100, 32));
        GiaoDienUtil.tuyChinhO(input);
        p.add(lbl, BorderLayout.NORTH);
        p.add(input, BorderLayout.CENTER);
        return p;
    }

    private JPanel taoHangDoi(JPanel o1, JPanel o2) {
        JPanel hang = new JPanel();
        hang.setOpaque(false);
        hang.setLayout(new BoxLayout(hang, BoxLayout.X_AXIS));
        hang.add(o1);
        hang.add(Box.createHorizontalStrut(10));
        hang.add(o2);
        hang.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        return hang;
    }

    private void thietLapBang() {
        GiaoDienUtil.tuyChinhBang(bangThuoc);
        bangThuoc.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Renderer làm nổi bật tồn kho thấp và hạn dùng
        DinhDangCotRenderer renderer = new DinhDangCotRenderer();
        for (int i = 0; i < bangThuoc.getColumnCount(); i++) {
            bangThuoc.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        bangThuoc.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || bangThuoc.getSelectedRow() < 0) return;
            int row = bangThuoc.getSelectedRow();
            String ma = (String) modelBang.getValueAt(row, 0);
            try {
                VatTu vt = vatTuDAO.timTheoMa(ma);
                if (vt != null) {
                    doLenForm(vt);
                    chuyenCheDo(true);
                }
            } catch (SQLException ex) {
                GiaoDienUtil.baoLoi(this, ex);
            }
        });
    }

    // =========================================================================
    // 2. XỬ LÝ DỮ LIỆU & DAO
    // =========================================================================

    /** Nạp danh sách thuốc từ CSDL qua VatTuDAO, có hỗ trợ từ khóa tìm kiếm. */
    private void taiDuLieu() {
        String tuKhoa = txtTimKiem.getText().trim();
        try {
            List<VatTu> danhSach = vatTuDAO.timKiem(tuKhoa, VatTuDAO.LOAI_THUOC);
            modelBang.setRowCount(0);
            for (VatTu vt : danhSach) {
                modelBang.addRow(new Object[] {
                        vt.getMaVatTu(),
                        vt.getTenVatTu(),
                        vt.getDonViTinh() == null ? "" : vt.getDonViTinh(),
                        vt.getSoLuongTon(),
                        vt.getNguongToiThieu(),
                        vt.getDonGiaBan(),
                        vt.getHanSuDung() == null ? "" : vt.getHanSuDung().format(DINH_DANG_NGAY)
                });
            }
            lblSoLuong.setText("Tổng số thuốc: " + danhSach.size());
        } catch (SQLException e) {
            GiaoDienUtil.baoLoi(this, e);
        }
    }

    /** Đổ thông tin thuốc lên các trường nhập liệu. */
    private void doLenForm(VatTu vt) {
        txtMa.setText(vt.getMaVatTu());
        txtTen.setText(vt.getTenVatTu());
        txtDonVi.setText(vt.getDonViTinh() == null ? "" : vt.getDonViTinh());
        txtTon.setText(String.valueOf(vt.getSoLuongTon()));
        txtNguong.setText(String.valueOf(vt.getNguongToiThieu()));
        txtGiaBan.setText(DINH_DANG_TIEN.format(vt.getDonGiaBan()));
        txtHan.setText(vt.getHanSuDung() == null ? "" : vt.getHanSuDung().format(DINH_DANG_NGAY));
    }

    /** Xóa trắng form, sinh mã thuốc tiếp theo và trả về chế độ thêm mới. */
    private void lamMoiBieuMau() {
        bangThuoc.clearSelection();
        try {
            txtMa.setText(vatTuDAO.sinhMa());
        } catch (SQLException e) {
            txtMa.setText("VT001");
        }
        txtTen.setText("");
        txtDonVi.setText("");
        txtTon.setText("0");
        txtNguong.setText("0");
        txtGiaBan.setText("0");
        txtHan.setText("");
        chuyenCheDo(false);
        txtTen.requestFocusInWindow();
    }

    private void chuyenCheDo(boolean dangSua) {
        btnThem.setEnabled(!dangSua);
        btnSua.setEnabled(dangSua);
        btnXoa.setEnabled(dangSua);
    }

    // =========================================================================
    // 3. THAO TÁC CRUD
    // =========================================================================

    /** Thêm thuốc mới vào CSDL. */
    private void themThuoc() {
        try {
            VatTu vt = docTuForm(txtMa.getText().trim());
            if (vatTuDAO.timTheoMa(vt.getMaVatTu()) != null) {
                GiaoDienUtil.canhBao(this, "Mã thuốc đã tồn tại trong hệ thống.");
                return;
            }
            if (vatTuDAO.them(vt)) {
                GiaoDienUtil.thongBao(this, "Đã thêm thuốc thành công: " + vt.getTenVatTu());
                taiDuLieu();
                lamMoiBieuMau();
            }
        } catch (IllegalArgumentException ex) {
            GiaoDienUtil.canhBao(this, ex.getMessage());
        } catch (SQLException ex) {
            GiaoDienUtil.baoLoi(this, ex);
        }
    }

    /** Cập nhật thông tin thuốc đã chọn. */
    private void suaThuoc() {
        try {
            VatTu vt = docTuForm(txtMa.getText().trim());
            if (vatTuDAO.capNhat(vt)) {
                GiaoDienUtil.thongBao(this, "Đã cập nhật thông tin thuốc: " + vt.getTenVatTu());
                taiDuLieu();
                lamMoiBieuMau();
            }
        } catch (IllegalArgumentException ex) {
            GiaoDienUtil.canhBao(this, ex.getMessage());
        } catch (SQLException ex) {
            GiaoDienUtil.baoLoi(this, ex);
        }
    }

    /** Xóa thuốc theo mã (bảo đảm an toàn dữ liệu qua ràng buộc khóa ngoại). */
    private void xoaThuoc() {
        String ma = txtMa.getText().trim();
        String ten = txtTen.getText().trim();
        if (ma.isEmpty()) return;

        int xacNhan = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa thuốc " + ma + " - " + ten + "?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (xacNhan != JOptionPane.YES_OPTION) return;

        try {
            if (vatTuDAO.xoa(ma)) {
                GiaoDienUtil.thongBao(this, "Đã xóa thuốc " + ma + " thành công.");
                taiDuLieu();
                lamMoiBieuMau();
            } else {
                GiaoDienUtil.canhBao(this, "Không tìm thấy thuốc cần xóa.");
            }
        } catch (SQLException ex) {
            // Bắt lỗi ràng buộc khóa ngoại (thuốc đã phát sinh trong đơn hoặc phiếu xuất)
            GiaoDienUtil.canhBao(this,
                    "Không thể xóa thuốc này do đã được kê trong đơn thuốc hoặc có trong phiếu xuất kho / hóa đơn.");
        }
    }

    // =========================================================================
    // 4. KIỂM TRA DỮ LIỆU ĐẦU VÀO (VALIDATION)
    // =========================================================================

    private VatTu docTuForm(String ma) {
        String ten = txtTen.getText().trim();
        if (ten.isEmpty()) {
            txtTen.requestFocusInWindow();
            throw new IllegalArgumentException("Tên thuốc không được để trống.");
        }

        int ton = docSoNguyenKhongAm(txtTon, "Số lượng tồn");
        int nguong = docSoNguyenKhongAm(txtNguong, "Ngưỡng tối thiểu");
        double giaBan = docSoTien(txtGiaBan, "Giá bán");

        LocalDate han = null;
        String hanStr = txtHan.getText().trim();
        if (!hanStr.isEmpty()) {
            try {
                han = LocalDate.parse(hanStr, DINH_DANG_NGAY);
            } catch (DateTimeParseException ex) {
                txtHan.requestFocusInWindow();
                throw new IllegalArgumentException("Hạn sử dụng không đúng định dạng dd/MM/yyyy.");
            }
        }

        String donVi = txtDonVi.getText().trim();
        return new VatTu(ma, ten, VatTuDAO.LOAI_THUOC, donVi.isEmpty() ? null : donVi, ton, nguong, han, giaBan);
    }

    private int docSoNguyenKhongAm(JTextField field, String tenTruong) {
        try {
            int val = Integer.parseInt(field.getText().trim());
            if (val < 0) throw new NumberFormatException();
            return val;
        } catch (NumberFormatException e) {
            field.requestFocusInWindow();
            throw new IllegalArgumentException(tenTruong + " phải là số nguyên không âm.");
        }
    }

    private double docSoTien(JTextField field, String tenTruong) {
        String text = field.getText().trim();
        if (text.isEmpty()) return 0;
        String so = text.replaceAll("[^0-9]", "");
        if (text.contains("-") || so.isEmpty()) {
            field.requestFocusInWindow();
            throw new IllegalArgumentException(tenTruong + " phải là số tiền không âm.");
        }
        return Double.parseDouble(so);
    }

    // =========================================================================
    // 5. CELL RENDERER ĐỊNH DẠNG BẢNG
    // =========================================================================

    private static final class DinhDangCotRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            setBorder(new EmptyBorder(0, 8, 0, 8));
            setFont(GiaoDienUtil.CHU);
            setForeground(GiaoDienUtil.INK);
            if (!isSelected) setBackground(Color.WHITE);

            setHorizontalAlignment(SwingConstants.LEFT);

            int modelRow = table.convertRowIndexToModel(row);
            // Cột 3: Tồn kho - cảnh báo nếu chạm ngưỡng tối thiểu
            if (column == 3) {
                int ton = (Integer) table.getModel().getValueAt(modelRow, 3);
                int nguong = (Integer) table.getModel().getValueAt(modelRow, 4);
                if (ton <= nguong) {
                    setForeground(GiaoDienUtil.DANGER);
                    setFont(GiaoDienUtil.CHU_DAM);
                }
            }
            // Cột 5: Giá bán
            else if (column == 5 && value instanceof Number num) {
                double gia = num.doubleValue();
                if (gia == 0) {
                    setText("Nội bộ");
                    setForeground(GiaoDienUtil.MUTED);
                } else {
                    setText(DINH_DANG_TIEN.format(gia) + " đ");
                    setForeground(GiaoDienUtil.TEAL);
                    setFont(GiaoDienUtil.CHU_DAM);
                }
                setHorizontalAlignment(SwingConstants.RIGHT);
            }
            // Cột 6: Hạn sử dụng - cảnh báo nếu đã quá hạn
            else if (column == 6 && value != null && !value.toString().isEmpty()) {
                try {
                    LocalDate hsd = LocalDate.parse(value.toString(), DINH_DANG_NGAY);
                    if (hsd.isBefore(LocalDate.now())) {
                        setForeground(GiaoDienUtil.DANGER);
                        setFont(GiaoDienUtil.CHU_DAM);
                    }
                } catch (DateTimeParseException ignored) {
                }
            }
            return this;
        }
    }
}
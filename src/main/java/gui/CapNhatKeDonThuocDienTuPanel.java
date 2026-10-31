package gui;

import dao.DonThuocDAO;
import dao.VatTuDAO;
import dto.LanKham;
import entity.ChiTietDonThuoc;
import entity.DonThuoc;
import entity.VatTu;
import util.GiaoDienUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Panel "Kê đơn thuốc điện tử" (bước kê đơn trong luồng khám bệnh).
 * Kết nối CSDL trực tiếp qua DonThuocDAO và VatTuDAO.
 *
 * Quy tắc nghiệp vụ chuẩn y khoa & CSDL:
 * - Mỗi lần khám (ChanDoan) có tối đa 1 đơn thuốc (ràng buộc UQ_DonThuoc_ChanDoan).
 * - Một thuốc chỉ xuất hiện 1 dòng trong một đơn (khóa chính (MaDonThuoc, MaVatTu)).
 * - Thao tác "Lưu đơn thuốc" thực thi trong một Transaction duy nhất (All or Nothing).
 */
public class CapNhatKeDonThuocDienTuPanel extends JPanel {

    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] DUONG_DUNG_MAC_DINH = { "Uống", "Tiêm", "Bôi", "Ngậm", "Súc miệng" };

    private final DonThuocDAO donThuocDAO = new DonThuocDAO();
    private final VatTuDAO vatTuDAO = new VatTuDAO();

    // Dữ liệu lần khám và thuốc trong bộ nhớ làm việc
    private final List<LanKham> danhSachLanKham = new ArrayList<>();
    // Dòng thuốc đang soạn trên giao diện: mã vật tư -> ChiTietDonThuoc
    private final Map<String, ChiTietDonThuoc> mapChiTietDangSoan = new LinkedHashMap<>();
    // Bản lưu tạm thông tin thuốc để tính thành tiền và kiểm tra tồn: mã -> VatTu
    private final Map<String, VatTu> mapCacheVatTu = new LinkedHashMap<>();

    private DonThuoc donThuocHienTai;

    // ----- 1. Thẻ chọn lần khám -----
    private final JComboBox<String> cboLanKham = new JComboBox<>();
    private final JLabel lblBenhNhan = new JLabel(" ");
    private final JLabel lblKetLuan = new JLabel(" ");
    private final JLabel lblDiUng = new JLabel(" ");
    private final JLabel lblTrangThaiDon = new JLabel(" ");

    // ----- 2. Thẻ danh mục thuốc -----
    private final JTextField txtTimThuoc = new JTextField();
    private final DefaultTableModel modelThuoc = new DefaultTableModel(
            new String[] { "Mã", "Tên thuốc", "ĐVT", "Tồn", "Giá bán (đ)" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable bangThuoc = new JTable(modelThuoc);
    private final JTextField txtLieuDung = new JTextField();
    private final JComboBox<String> cboDuongDung = new JComboBox<>(DUONG_DUNG_MAC_DINH);
    private final JSpinner spnSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));

    // ----- 3. Thẻ đơn thuốc bên phải -----
    private final JLabel lblNgayKe = new JLabel(LocalDate.now().format(DINH_DANG_NGAY));
    private final JTextArea txtLoiDan = new JTextArea(3, 20);
    private final DefaultTableModel modelDon = new DefaultTableModel(
            new String[] { "Mã", "Tên thuốc", "Liều dùng", "Đường dùng", "SL", "Thành tiền (đ)" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable bangDon = new JTable(modelDon);
    private final JLabel lblTongTien = new JLabel("0 đ");

    public CapNhatKeDonThuocDienTuPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);

        add(taoTheLanKham(), BorderLayout.NORTH);

        JPanel vungGiua = new JPanel(new BorderLayout(16, 0));
        vungGiua.setOpaque(false);
        vungGiua.add(taoTheDanhMucThuoc(), BorderLayout.WEST);
        vungGiua.add(taoTheDonThuoc(), BorderLayout.CENTER);
        add(vungGiua, BorderLayout.CENTER);

        // Nạp dữ liệu lần khám và danh mục thuốc ban đầu
        taiDanhSachLanKham();
        taiDanhSachThuoc();
    }

    // =========================================================================
    // 1. XÂY DỰNG GIAO DIỆN
    // =========================================================================

    /** Thẻ hiển thị lần khám: người dùng chọn lần khám của bệnh nhân cần kê đơn. */
    private JPanel taoTheLanKham() {
        JPanel the = GiaoDienUtil.taoThe();
        the.add(GiaoDienUtil.taoTieuDe("Lần khám cần kê đơn thuốc"));
        the.add(Box.createVerticalStrut(10));

        cboLanKham.setFont(GiaoDienUtil.CHU);
        the.add(GiaoDienUtil.taoHang("Lần khám (chờ kê)", cboLanKham));

        lblBenhNhan.setFont(GiaoDienUtil.CHU_DAM);
        lblBenhNhan.setForeground(GiaoDienUtil.INK);
        lblKetLuan.setFont(GiaoDienUtil.CHU);
        lblKetLuan.setForeground(GiaoDienUtil.INK);
        lblDiUng.setFont(GiaoDienUtil.CHU_DAM);
        lblTrangThaiDon.setFont(new Font("Arial", Font.ITALIC, 11));
        lblTrangThaiDon.setForeground(GiaoDienUtil.MUTED);

        for (JLabel label : new JLabel[] { lblBenhNhan, lblKetLuan, lblDiUng, lblTrangThaiDon }) {
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            the.add(label);
            the.add(Box.createVerticalStrut(3));
        }

        cboLanKham.addActionListener(e -> chonLanKham());
        return the;
    }

    /** Thẻ danh mục thuốc bên trái: tìm kiếm thuốc và chọn liều dùng để thêm vào đơn. */
    private JPanel taoTheDanhMucThuoc() {
        JPanel the = new JPanel(new BorderLayout(0, 12));
        GiaoDienUtil.trangTriThe(the);
        the.setPreferredSize(new Dimension(480, 0));

        // Thanh tìm kiếm
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(GiaoDienUtil.taoTieuDe("Danh mục thuốc"));
        top.add(Box.createVerticalStrut(8));

        GiaoDienUtil.tuyChinhO(txtTimThuoc);
        txtTimThuoc.setToolTipText("Nhập mã hoặc tên thuốc");
        txtTimThuoc.addActionListener(e -> taiDanhSachThuoc());
        JButton btnTim = GiaoDienUtil.taoNut("Tìm", false);
        btnTim.addActionListener(e -> taiDanhSachThuoc());

        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchRow.add(txtTimThuoc, BorderLayout.CENTER);
        searchRow.add(btnTim, BorderLayout.EAST);
        searchRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        top.add(searchRow);
        the.add(top, BorderLayout.NORTH);

        // Bảng thuốc
        GiaoDienUtil.tuyChinhBang(bangThuoc);
        bangThuoc.getColumnModel().getColumn(0).setPreferredWidth(55);
        bangThuoc.getColumnModel().getColumn(1).setPreferredWidth(170);
        bangThuoc.getColumnModel().getColumn(2).setPreferredWidth(45);
        bangThuoc.getColumnModel().getColumn(3).setPreferredWidth(45);
        bangThuoc.getColumnModel().getColumn(4).setPreferredWidth(85);

        JScrollPane cuonBang = new JScrollPane(bangThuoc);
        cuonBang.setBorder(BorderFactory.createLineBorder(GiaoDienUtil.LINE));
        cuonBang.getViewport().setBackground(Color.WHITE);
        the.add(cuonBang, BorderLayout.CENTER);

        // Phần nhập liều dùng, đường dùng, số lượng
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.add(GiaoDienUtil.taoHang("Liều dùng (*)", txtLieuDung));
        bottom.add(GiaoDienUtil.taoHang("Đường dùng", cboDuongDung));
        bottom.add(GiaoDienUtil.taoHang("Số lượng", spnSoLuong));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton btnThem = GiaoDienUtil.taoNut("Thêm vào đơn", true);
        btnThem.addActionListener(e -> themThuocVaoDon());
        actions.add(btnThem);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        bottom.add(actions);

        the.add(bottom, BorderLayout.SOUTH);
        return the;
    }

    /** Thẻ đơn thuốc bên phải: lời dặn, bảng dòng thuốc trong đơn và tổng tiền. */
    private JPanel taoTheDonThuoc() {
        JPanel the = new JPanel(new BorderLayout(0, 12));
        GiaoDienUtil.trangTriThe(the);

        // Lời dặn chăm sóc và ngày kê
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(GiaoDienUtil.taoTieuDe("Đơn thuốc điện tử"));
        form.add(Box.createVerticalStrut(10));

        lblNgayKe.setFont(GiaoDienUtil.CHU_DAM);
        lblNgayKe.setForeground(GiaoDienUtil.INK);
        form.add(GiaoDienUtil.taoHang("Ngày kê", lblNgayKe));

        txtLoiDan.setFont(GiaoDienUtil.CHU);
        txtLoiDan.setForeground(GiaoDienUtil.INK);
        txtLoiDan.setLineWrap(true);
        txtLoiDan.setWrapStyleWord(true);
        txtLoiDan.setBorder(new EmptyBorder(6, 8, 6, 8));
        JScrollPane cuonLoiDan = new JScrollPane(txtLoiDan);
        cuonLoiDan.setBorder(BorderFactory.createLineBorder(GiaoDienUtil.LINE));
        cuonLoiDan.setPreferredSize(new Dimension(100, 64));
        form.add(GiaoDienUtil.taoHang("Lời dặn", cuonLoiDan));

        JLabel lblDanhSachTitle = GiaoDienUtil.taoNhanPhu("Thuốc trong đơn");
        lblDanhSachTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblDanhSachTitle);
        the.add(form, BorderLayout.NORTH);

        // Bảng thuốc trong đơn
        GiaoDienUtil.tuyChinhBang(bangDon);
        bangDon.getColumnModel().getColumn(0).setPreferredWidth(50);
        bangDon.getColumnModel().getColumn(1).setPreferredWidth(140);
        bangDon.getColumnModel().getColumn(2).setPreferredWidth(110);
        bangDon.getColumnModel().getColumn(3).setPreferredWidth(70);
        bangDon.getColumnModel().getColumn(4).setPreferredWidth(40);
        bangDon.getColumnModel().getColumn(5).setPreferredWidth(90);

        JScrollPane cuonBangDon = new JScrollPane(bangDon);
        cuonBangDon.setBorder(BorderFactory.createLineBorder(GiaoDienUtil.LINE));
        cuonBangDon.getViewport().setBackground(Color.WHITE);
        the.add(cuonBangDon, BorderLayout.CENTER);

        // Phần tổng tiền và cụm nút thao tác
        JPanel bottom = new JPanel(new BorderLayout(0, 10));
        bottom.setOpaque(false);

        JPanel rowTong = new JPanel(new BorderLayout());
        rowTong.setOpaque(false);
        JLabel lblCaption = new JLabel("TỔNG TIỀN THUỐC");
        lblCaption.setFont(GiaoDienUtil.NHO_DAM);
        lblCaption.setForeground(GiaoDienUtil.MUTED);
        lblTongTien.setFont(new Font("Arial", Font.BOLD, 18));
        lblTongTien.setForeground(GiaoDienUtil.TEAL);
        rowTong.add(lblCaption, BorderLayout.WEST);
        rowTong.add(lblTongTien, BorderLayout.EAST);
        bottom.add(rowTong, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton btnXoaDong = GiaoDienUtil.taoNut("Xóa dòng thuốc", false);
        JButton btnLamMoi = GiaoDienUtil.taoNut("Làm mới", false);
        JButton btnLuu = GiaoDienUtil.taoNut("Lưu đơn thuốc", true);

        btnXoaDong.addActionListener(e -> xoaThuocKhoiDon());
        btnLamMoi.addActionListener(e -> chonLanKham());
        btnLuu.addActionListener(e -> luuDonThuoc());

        actions.add(btnXoaDong);
        actions.add(btnLamMoi);
        actions.add(btnLuu);
        bottom.add(actions, BorderLayout.SOUTH);

        the.add(bottom, BorderLayout.SOUTH);
        return the;
    }

    // =========================================================================
    // 2. TẢI DỮ LIỆU TỪ DAO
    // =========================================================================

    /** Nạp danh sách các lần khám đang chờ kê đơn (TrangThai = 'Nhap'). */
    public void taiDanhSachLanKham() {
        try {
            danhSachLanKham.clear();
            danhSachLanKham.addAll(donThuocDAO.layLanKhamChoKeDon());
            cboLanKham.removeAllItems();
            for (LanKham lk : danhSachLanKham) {
                cboLanKham.addItem(lk.maChanDoan() + " - " + lk.hoTen() + " (" + lk.maBenhNhan() + ")");
            }
            if (!danhSachLanKham.isEmpty()) {
                chonLanKham();
            } else {
                lblBenhNhan.setText("Hiện không có lần khám nào đang chờ kê đơn.");
                lblKetLuan.setText("");
                lblDiUng.setText("");
                lblTrangThaiDon.setText("");
            }
        } catch (SQLException e) {
            GiaoDienUtil.baoLoi(this, e);
        }
    }

    /** Nạp danh mục thuốc từ VatTuDAO (lọc theo từ khóa). */
    public void taiDanhSachThuoc() {
        String tuKhoa = txtTimThuoc.getText().trim();
        try {
            List<VatTu> danhSach = vatTuDAO.timKiem(tuKhoa, VatTuDAO.LOAI_THUOC);
            modelThuoc.setRowCount(0);
            for (VatTu vt : danhSach) {
                mapCacheVatTu.put(vt.getMaVatTu(), vt);
                modelThuoc.addRow(new Object[] {
                        vt.getMaVatTu(),
                        vt.getTenVatTu(),
                        vt.getDonViTinh() == null ? "" : vt.getDonViTinh(),
                        vt.getSoLuongTon(),
                        DINH_DANG_TIEN.format(vt.getDonGiaBan())
                });
            }
        } catch (SQLException e) {
            GiaoDienUtil.baoLoi(this, e);
        }
    }

    // =========================================================================
    // 3. XỬ LÝ NGHIỆP VỤ KÊ ĐƠN
    // =========================================================================

    /** Khi chọn lần khám: tải đơn thuốc đã lưu (nếu có) hoặc chuẩn bị tạo đơn mới. */
    private void chonLanKham() {
        int index = cboLanKham.getSelectedIndex();
        if (index < 0 || index >= danhSachLanKham.size()) return;

        LanKham lk = danhSachLanKham.get(index);
        lblBenhNhan.setText("Bệnh nhân: " + lk.hoTen() + "  ·  Mã: " + lk.maBenhNhan());
        lblKetLuan.setText("Kết luận chẩn đoán: " + lk.ketLuan());

        if (lk.thongTinDiUng() != null && !lk.thongTinDiUng().trim().isEmpty()) {
            lblDiUng.setText("CẢNH BÁO DỊ ỨNG: " + lk.thongTinDiUng());
            lblDiUng.setForeground(GiaoDienUtil.RED);
        } else {
            lblDiUng.setText("Dị ứng: Không ghi nhận");
            lblDiUng.setForeground(GiaoDienUtil.MUTED);
        }

        mapChiTietDangSoan.clear();
        txtLieuDung.setText("");
        cboDuongDung.setSelectedIndex(0);
        spnSoLuong.setValue(1);

        try {
            // Kiểm tra xem lần khám này đã có đơn thuốc chưa
            donThuocHienTai = donThuocDAO.timTheoChanDoan(lk.maChanDoan());
            if (donThuocHienTai == null) {
                String maMoi = donThuocDAO.sinhMa();
                donThuocHienTai = new DonThuoc(maMoi, LocalDate.now(), "", lk.maChanDoan());
                lblTrangThaiDon.setText("Lần khám này chưa có đơn thuốc. Sẽ tạo mới mã " + maMoi);
                lblNgayKe.setText(LocalDate.now().format(DINH_DANG_NGAY));
                txtLoiDan.setText("");
            } else {
                lblTrangThaiDon.setText("Đã có đơn thuốc " + donThuocHienTai.getMaDonThuoc() + ". Lưu sẽ cập nhật đơn này.");
                lblNgayKe.setText(donThuocHienTai.getNgayKe().format(DINH_DANG_NGAY));
                txtLoiDan.setText(donThuocHienTai.getLoiDanChamSoc() == null ? "" : donThuocHienTai.getLoiDanChamSoc());

                List<ChiTietDonThuoc> chiTietList = donThuocDAO.layChiTiet(donThuocHienTai.getMaDonThuoc());
                for (ChiTietDonThuoc ct : chiTietList) {
                    mapChiTietDangSoan.put(ct.getMaVatTu(), ct);
                }
            }
            capNhatBangDonThuoc();
        } catch (SQLException e) {
            GiaoDienUtil.baoLoi(this, e);
        }
    }

    /** Thêm thuốc từ danh mục vào đơn đang soạn (ghi đè nếu thuốc đã tồn tại trong đơn). */
    private void themThuocVaoDon() {
        int row = bangThuoc.getSelectedRow();
        if (row < 0) {
            GiaoDienUtil.canhBao(this, "Vui lòng chọn một loại thuốc trong danh mục.");
            return;
        }

        String maVatTu = (String) modelThuoc.getValueAt(row, 0);
        VatTu vt = mapCacheVatTu.get(maVatTu);
        if (vt == null) return;

        String lieuDung = txtLieuDung.getText().trim();
        if (lieuDung.isEmpty()) {
            GiaoDienUtil.canhBao(this, "Liều dùng không được để trống (ví dụ: 1 viên x 2 lần/ngày).");
            txtLieuDung.requestFocusInWindow();
            return;
        }

        int soLuong = (Integer) spnSoLuong.getValue();
        if (soLuong > vt.getSoLuongTon()) {
            GiaoDienUtil.canhBao(this, "Số lượng kê (" + soLuong + ") vượt quá tồn kho hiện có (" + vt.getSoLuongTon() + ").");
            return;
        }

        if (vt.getHanSuDung() != null && vt.getHanSuDung().isBefore(LocalDate.now())) {
            GiaoDienUtil.canhBao(this, "Thuốc " + vt.getTenVatTu() + " đã hết hạn sử dụng, không thể kê đơn!");
            return;
        }

        String duongDung = (String) cboDuongDung.getSelectedItem();
        ChiTietDonThuoc ct = new ChiTietDonThuoc(donThuocHienTai.getMaDonThuoc(), maVatTu, lieuDung, duongDung, soLuong);
        mapChiTietDangSoan.put(maVatTu, ct);

        txtLieuDung.setText("");
        spnSoLuong.setValue(1);
        capNhatBangDonThuoc();
    }

    /** Xóa dòng thuốc đang chọn khỏi đơn. */
    private void xoaThuocKhoiDon() {
        int row = bangDon.getSelectedRow();
        if (row < 0) {
            GiaoDienUtil.canhBao(this, "Vui lòng chọn dòng thuốc trong đơn cần xóa.");
            return;
        }
        String maVatTu = (String) modelDon.getValueAt(row, 0);
        mapChiTietDangSoan.remove(maVatTu);
        capNhatBangDonThuoc();
    }

    /** Làm mới bảng đơn thuốc trên màn hình và tính lại tổng tiền. */
    private void capNhatBangDonThuoc() {
        modelDon.setRowCount(0);
        double tongTien = 0;

        for (ChiTietDonThuoc ct : mapChiTietDangSoan.values()) {
            VatTu vt = mapCacheVatTu.get(ct.getMaVatTu());
            String tenThuoc = vt != null ? vt.getTenVatTu() : ct.getMaVatTu();
            double giaBan = vt != null ? vt.getDonGiaBan() : 0;
            double thanhTien = giaBan * ct.getSoLuong();
            tongTien += thanhTien;

            modelDon.addRow(new Object[] {
                    ct.getMaVatTu(),
                    tenThuoc,
                    ct.getLieuDung(),
                    ct.getDuongDung(),
                    ct.getSoLuong(),
                    DINH_DANG_TIEN.format(thanhTien)
            });
        }
        lblTongTien.setText(DINH_DANG_TIEN.format(tongTien) + " đ");
    }

    /** Lưu đơn thuốc và chi tiết vào CSDL bằng Transaction. */
    private void luuDonThuoc() {
        int index = cboLanKham.getSelectedIndex();
        if (index < 0 || donThuocHienTai == null) {
            GiaoDienUtil.canhBao(this, "Vui lòng chọn lần khám cần kê đơn.");
            return;
        }

        if (mapChiTietDangSoan.isEmpty()) {
            GiaoDienUtil.canhBao(this, "Đơn thuốc phải có ít nhất một loại thuốc.");
            return;
        }

        String loiDan = txtLoiDan.getText().trim();
        if (loiDan.length() > 500) {
            GiaoDienUtil.canhBao(this, "Lời dặn chăm sóc tối đa 500 ký tự.");
            txtLoiDan.requestFocusInWindow();
            return;
        }

        // Cảnh báo nếu bệnh nhân có tiền sử dị ứng
        LanKham lk = danhSachLanKham.get(index);
        if (lk.thongTinDiUng() != null && !lk.thongTinDiUng().trim().isEmpty()) {
            int xacNhan = JOptionPane.showConfirmDialog(
                    this,
                    "Bệnh nhân có thông tin dị ứng: " + lk.thongTinDiUng()
                            + ".\nBạn đã kiểm tra tương tác thuốc chưa?",
                    "Cảnh báo dị ứng",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (xacNhan != JOptionPane.YES_OPTION) return;
        }

        donThuocHienTai.setLoiDanChamSoc(loiDan.isEmpty() ? null : loiDan);
        List<ChiTietDonThuoc> chiTietList = new ArrayList<>(mapChiTietDangSoan.values());

        try {
            donThuocDAO.luuDonThuoc(donThuocHienTai, chiTietList);
            GiaoDienUtil.thongBao(this, "Đã lưu đơn thuốc " + donThuocHienTai.getMaDonThuoc() + " thành công!");
            chonLanKham();
        } catch (SQLException e) {
            GiaoDienUtil.baoLoi(this, e);
        }
    }
}
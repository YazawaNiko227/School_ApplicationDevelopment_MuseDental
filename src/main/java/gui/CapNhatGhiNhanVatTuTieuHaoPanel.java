package gui;

import dao.LichHenDAO;
import dao.PhieuXuatKhoDAO;
import dao.VatTuDAO;
import dto.ChiTietXuatInfo;
import dto.LichHenInfo;
import entity.ChiTietXuatKho;
import entity.PhieuXuatKho;
import entity.VatTu;
import util.GiaoDienUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.time.LocalDate;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

/**
 * Panel "Ghi nhận vật tư tiêu hao" (xuất kho dùng cho lần khám hoặc phòng
 * khám).
 * Kết nối CSDL trực tiếp qua PhieuXuatKhoDAO, VatTuDAO và LichHenDAO.
 *
 * Quy tắc nghiệp vụ:
 * - Chọn lịch hẹn đang khám / hoàn thành, hoặc chọn "Xuất dùng chung".
 * - Khi lưu phiếu xuất, hệ thống tạo PhieuXuatKho, ChiTietXuatKho và tự động
 * trừ số lượng tồn kho của VatTu trong một Transaction duy nhất.
 */
public class CapNhatGhiNhanVatTuTieuHaoPanel extends JPanel {

    private static final String TAT_CA = "Tất cả";
    private static final String XUAT_DUNG_CHUNG = "Xuất dùng chung (không gắn lịch hẹn)";
    private static final String MA_NHAN_VIEN_XUAT = "NV005"; // Mã thủ kho mặc định

    private final PhieuXuatKhoDAO phieuXuatKhoDAO = new PhieuXuatKhoDAO();
    private final VatTuDAO vatTuDAO = new VatTuDAO();
    private final LichHenDAO lichHenDAO = new LichHenDAO();

    // Danh sách lịch hẹn nạp từ CSDL
    private final List<LichHenInfo> danhSachLichHen = new ArrayList<>();
    // Dòng vật tư đang soạn trên phiếu: mã vật tư -> số lượng sử dụng
    private final Map<String, Integer> mapVatTuDangSoan = new LinkedHashMap<>();
    // Cache danh mục vật tư hiện hành: mã -> VatTu
    private final Map<String, VatTu> mapCacheVatTu = new LinkedHashMap<>();

    // ----- 1. Thẻ chọn lịch hẹn -----
    private final JComboBox<String> cboLichHen = new JComboBox<>();
    private final JLabel lblBenhNhan = new JLabel(" ");

    // ----- 2. Thẻ danh mục vật tư bên trái -----
    private final JTextField txtTimVatTu = new JTextField();
    private final JComboBox<String> cboLoai = new JComboBox<>(
            new String[] { TAT_CA, "Thuốc", "Tiêu hao", "Vật liệu" });
    private final DefaultTableModel modelVatTu = new DefaultTableModel(
            new String[] { "Mã", "Tên vật tư", "Loại", "ĐVT", "Tồn" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bangVatTu = new JTable(modelVatTu);
    private final JSpinner spnSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));

    // ----- 3. Thẻ phiếu xuất và lịch sử bên phải -----
    private final DefaultTableModel modelPhieu = new DefaultTableModel(
            new String[] { "Mã", "Tên vật tư", "ĐVT", "Số lượng dùng" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bangPhieu = new JTable(modelPhieu);

    private final DefaultTableModel modelLichSu = new DefaultTableModel(
            new String[] { "Mã phiếu", "Mã VT", "Tên vật tư", "ĐVT", "Số lượng" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bangLichSu = new JTable(modelLichSu);

    public CapNhatGhiNhanVatTuTieuHaoPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);

        add(taoTheLichHen(), BorderLayout.NORTH);

        JPanel vungGiua = new JPanel(new BorderLayout(16, 0));
        vungGiua.setOpaque(false);
        vungGiua.add(taoTheVatTu(), BorderLayout.WEST);
        vungGiua.add(taoThePhieuVaLichSu(), BorderLayout.CENTER);
        add(vungGiua, BorderLayout.CENTER);

        // Nạp dữ liệu ban đầu
        taiDanhSachLichHen();
        taiDanhSachVatTu();
    }

    // =========================================================================
    // 1. XÂY DỰNG GIAO DIỆN
    // =========================================================================

    /** Thẻ chọn lịch hẹn / lần khám để xuất vật tư. */
    private JPanel taoTheLichHen() {
        JPanel the = GiaoDienUtil.taoThe();
        the.add(GiaoDienUtil.taoTieuDe("Lần khám / Lịch hẹn sử dụng vật tư"));
        the.add(Box.createVerticalStrut(10));

        cboLichHen.setFont(GiaoDienUtil.CHU);
        the.add(GiaoDienUtil.taoHang("Lịch hẹn", cboLichHen));

        lblBenhNhan.setFont(GiaoDienUtil.CHU_DAM);
        lblBenhNhan.setForeground(GiaoDienUtil.INK);
        lblBenhNhan.setAlignmentX(Component.LEFT_ALIGNMENT);
        the.add(lblBenhNhan);

        cboLichHen.addActionListener(e -> chonLichHen());
        return the;
    }

    /** Thẻ danh mục vật tư bên trái: tìm kiếm, lọc theo loại, chọn số lượng. */
    private JPanel taoTheVatTu() {
        JPanel the = new JPanel(new BorderLayout(0, 12));
        GiaoDienUtil.trangTriThe(the);
        the.setPreferredSize(new Dimension(480, 0));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(GiaoDienUtil.taoTieuDe("Danh mục vật tư y tế"));
        top.add(Box.createVerticalStrut(8));

        // Thanh tìm kiếm và bộ lọc loại vật tư
        GiaoDienUtil.tuyChinhO(txtTimVatTu);
        txtTimVatTu.setToolTipText("Nhập mã hoặc tên vật tư");
        txtTimVatTu.addActionListener(e -> taiDanhSachVatTu());

        cboLoai.setFont(GiaoDienUtil.CHU);
        cboLoai.setPreferredSize(new Dimension(100, 32));
        cboLoai.addActionListener(e -> taiDanhSachVatTu());

        JButton btnTim = GiaoDienUtil.taoNut("Tìm", false);
        btnTim.addActionListener(e -> taiDanhSachVatTu());

        JPanel filterRow = new JPanel(new BorderLayout(8, 0));
        filterRow.setOpaque(false);
        filterRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        filterRow.add(txtTimVatTu, BorderLayout.CENTER);
        filterRow.add(cboLoai, BorderLayout.EAST);
        filterRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        top.add(filterRow);
        the.add(top, BorderLayout.NORTH);

        // Bảng vật tư
        GiaoDienUtil.tuyChinhBang(bangVatTu);
        bangVatTu.getColumnModel().getColumn(0).setPreferredWidth(55);
        bangVatTu.getColumnModel().getColumn(1).setPreferredWidth(175);
        bangVatTu.getColumnModel().getColumn(2).setPreferredWidth(70);
        bangVatTu.getColumnModel().getColumn(3).setPreferredWidth(45);
        bangVatTu.getColumnModel().getColumn(4).setPreferredWidth(45);

        JScrollPane cuonBang = new JScrollPane(bangVatTu);
        cuonBang.setBorder(BorderFactory.createLineBorder(GiaoDienUtil.LINE));
        cuonBang.getViewport().setBackground(Color.WHITE);
        the.add(cuonBang, BorderLayout.CENTER);

        // Chọn số lượng và nút thêm
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.add(GiaoDienUtil.taoHang("Số lượng xuất", spnSoLuong));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton btnThem = GiaoDienUtil.taoNut("Thêm vào phiếu", true);
        btnThem.addActionListener(e -> themVatTu());
        actions.add(btnThem);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        bottom.add(actions);

        the.add(bottom, BorderLayout.SOUTH);
        return the;
    }

    /** Thẻ bên phải: danh sách vật tư trong phiếu đang tạo và lịch sử đã xuất. */
    private JPanel taoThePhieuVaLichSu() {
        JPanel the = new JPanel(new BorderLayout(0, 12));
        GiaoDienUtil.trangTriThe(the);

        JPanel vungNoiDung = new JPanel();
        vungNoiDung.setOpaque(false);
        vungNoiDung.setLayout(new BoxLayout(vungNoiDung, BoxLayout.Y_AXIS));

        // 1. Phiếu xuất đang soạn
        vungNoiDung.add(GiaoDienUtil.taoTieuDe("Phiếu xuất đang soạn"));
        vungNoiDung.add(Box.createVerticalStrut(8));

        GiaoDienUtil.tuyChinhBang(bangPhieu);
        JScrollPane cuonPhieu = new JScrollPane(bangPhieu);
        cuonPhieu.setBorder(BorderFactory.createLineBorder(GiaoDienUtil.LINE));
        cuonPhieu.getViewport().setBackground(Color.WHITE);
        cuonPhieu.setPreferredSize(new Dimension(100, 160));
        cuonPhieu.setAlignmentX(Component.LEFT_ALIGNMENT);
        vungNoiDung.add(cuonPhieu);

        // Cụm nút phiếu xuất
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton btnXoa = GiaoDienUtil.taoNut("Xóa dòng", false);
        JButton btnLamMoi = GiaoDienUtil.taoNut("Làm mới", false);
        JButton btnLuu = GiaoDienUtil.taoNut("Lưu phiếu xuất", true);

        btnXoa.addActionListener(e -> xoaVatTu());
        btnLamMoi.addActionListener(e -> chonLichHen());
        btnLuu.addActionListener(e -> luuPhieuXuat());

        actions.add(btnXoa);
        actions.add(btnLamMoi);
        actions.add(btnLuu);
        vungNoiDung.add(actions);

        // 2. Lịch sử đã xuất của lần khám này
        vungNoiDung.add(Box.createVerticalStrut(10));
        JLabel lblLichSuTitle = GiaoDienUtil.taoTieuDe("Lịch sử vật tư đã xuất cho lần này");
        lblLichSuTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        vungNoiDung.add(lblLichSuTitle);
        vungNoiDung.add(Box.createVerticalStrut(8));

        GiaoDienUtil.tuyChinhBang(bangLichSu);
        JScrollPane cuonLichSu = new JScrollPane(bangLichSu);
        cuonLichSu.setBorder(BorderFactory.createLineBorder(GiaoDienUtil.LINE));
        cuonLichSu.getViewport().setBackground(Color.WHITE);
        cuonLichSu.setPreferredSize(new Dimension(100, 180));
        cuonLichSu.setAlignmentX(Component.LEFT_ALIGNMENT);
        vungNoiDung.add(cuonLichSu);

        the.add(vungNoiDung, BorderLayout.CENTER);
        return the;
    }

    // =========================================================================
    // 2. TẢI DỮ LIỆU TỪ DAO
    // =========================================================================

    /** Nạp các lịch hẹn đang khám hoặc vừa khám xong từ LichHenDAO. */
    public void taiDanhSachLichHen() {
        try {
            danhSachLichHen.clear();
            danhSachLichHen.addAll(lichHenDAO.layChoXuatKho());

            cboLichHen.removeAllItems();
            cboLichHen.addItem(XUAT_DUNG_CHUNG);
            for (LichHenInfo lh : danhSachLichHen) {
                cboLichHen.addItem(
                        lh.maLichHen() + " - " + lh.hoTen() + " (" + lh.trangThai() + ", P." + lh.maPhong() + ")");
            }
            chonLichHen();
        } catch (SQLException e) {
            GiaoDienUtil.baoLoi(this, e);
        }
    }

    /** Nạp danh mục vật tư y tế theo từ khóa và loại. */
    public void taiDanhSachVatTu() {
        String tuKhoa = txtTimVatTu.getText().trim();
        String loai = (String) cboLoai.getSelectedItem();
        String loaiLoc = TAT_CA.equals(loai) ? null : loai;

        try {
            List<VatTu> danhSach = vatTuDAO.timKiem(tuKhoa, loaiLoc);
            modelVatTu.setRowCount(0);
            for (VatTu vt : danhSach) {
                mapCacheVatTu.put(vt.getMaVatTu(), vt);
                modelVatTu.addRow(new Object[] {
                        vt.getMaVatTu(),
                        vt.getTenVatTu(),
                        vt.getLoaiVatTu(),
                        vt.getDonViTinh() == null ? "" : vt.getDonViTinh(),
                        vt.getSoLuongTon()
                });
            }
        } catch (SQLException e) {
            GiaoDienUtil.baoLoi(this, e);
        }
    }

    // =========================================================================
    // 3. XỬ LÝ NGHIỆP VỤ XUẤT KHO
    // =========================================================================

    /**
     * Khi thay đổi lựa chọn lịch hẹn: xóa phiếu đang soạn và tải lại lịch sử xuất.
     */
    private void chonLichHen() {
        int index = cboLichHen.getSelectedIndex();
        if (index < 0)
            return;

        if (index == 0) {
            lblBenhNhan.setText("Xuất dùng chung cho toàn phòng khám (không gắn với bệnh nhân nào).");
        } else {
            LichHenInfo lh = danhSachLichHen.get(index - 1);
            lblBenhNhan.setText(
                    "Bệnh nhân: " + lh.hoTen() + "  ·  Mã BN: " + lh.maBenhNhan() + "  ·  Phòng: " + lh.maPhong());
        }

        mapVatTuDangSoan.clear();
        spnSoLuong.setValue(1);
        capNhatBangPhieu();
        taiLichSuXuat();
    }

    /** Nạp danh sách vật tư đã xuất của lần khám này từ CSDL. */
    private void taiLichSuXuat() {
        int index = cboLichHen.getSelectedIndex();
        String maLichHen = (index <= 0) ? null : danhSachLichHen.get(index - 1).maLichHen();

        try {
            List<ChiTietXuatInfo> lichSu = phieuXuatKhoDAO.layLichSuXuatTheoLichHen(maLichHen);
            modelLichSu.setRowCount(0);
            for (ChiTietXuatInfo info : lichSu) {
                modelLichSu.addRow(new Object[] {
                        info.maPhieuXuat(),
                        info.maVatTu(),
                        info.tenVatTu(),
                        info.donViTinh() == null ? "" : info.donViTinh(),
                        info.soLuongSuDung()
                });
            }
        } catch (SQLException e) {
            GiaoDienUtil.baoLoi(this, e);
        }
    }

    /** Thêm vật tư vào phiếu đang soạn (cộng dồn nếu đã có trong phiếu). */
    private void themVatTu() {
        int row = bangVatTu.getSelectedRow();
        if (row < 0) {
            GiaoDienUtil.canhBao(this, "Vui lòng chọn một vật tư trong danh mục.");
            return;
        }

        String maVatTu = (String) modelVatTu.getValueAt(row, 0);
        VatTu vt = mapCacheVatTu.get(maVatTu);
        if (vt == null)
            return;

        int soLuong = (Integer) spnSoLuong.getValue();
        int daChon = mapVatTuDangSoan.getOrDefault(maVatTu, 0);

        if (daChon + soLuong > vt.getSoLuongTon()) {
            GiaoDienUtil.canhBao(this, "Tổng số lượng xuất (" + (daChon + soLuong) + ") vượt quá tồn kho hiện có ("
                    + vt.getSoLuongTon() + ").");
            return;
        }

        if (vt.getHanSuDung() != null && vt.getHanSuDung().isBefore(LocalDate.now())) {
            GiaoDienUtil.canhBao(this, "Vật tư " + vt.getTenVatTu() + " đã hết hạn sử dụng, không thể xuất dùng!");
            return;
        }

        mapVatTuDangSoan.put(maVatTu, daChon + soLuong);
        spnSoLuong.setValue(1);
        capNhatBangPhieu();
    }

    /** Xóa vật tư đang chọn khỏi phiếu. */
    private void xoaVatTu() {
        int row = bangPhieu.getSelectedRow();
        if (row < 0) {
            GiaoDienUtil.canhBao(this, "Vui lòng chọn dòng vật tư cần xóa khỏi phiếu.");
            return;
        }
        String maVatTu = (String) modelPhieu.getValueAt(row, 0);
        mapVatTuDangSoan.remove(maVatTu);
        capNhatBangPhieu();
    }

    /** Cập nhật bảng phiếu xuất đang soạn. */
    private void capNhatBangPhieu() {
        modelPhieu.setRowCount(0);
        for (Map.Entry<String, Integer> entry : mapVatTuDangSoan.entrySet()) {
            VatTu vt = mapCacheVatTu.get(entry.getKey());
            String ten = vt != null ? vt.getTenVatTu() : entry.getKey();
            String dvt = (vt != null && vt.getDonViTinh() != null) ? vt.getDonViTinh() : "";
            modelPhieu.addRow(new Object[] { entry.getKey(), ten, dvt, entry.getValue() });
        }
    }

    /**
     * Lưu phiếu xuất vào CSDL qua Transaction (tạo phiếu, ghi chi tiết, trừ tồn).
     */
    private void luuPhieuXuat() {
        if (mapVatTuDangSoan.isEmpty()) {
            GiaoDienUtil.canhBao(this, "Phiếu xuất phải có ít nhất một vật tư tiêu hao.");
            return;
        }

        int index = cboLichHen.getSelectedIndex();
        String maLichHen = (index <= 0) ? null : danhSachLichHen.get(index - 1).maLichHen();

        try {
            String maPhieuMoi = phieuXuatKhoDAO.sinhMa();
            PhieuXuatKho phieu = new PhieuXuatKho(maPhieuMoi, LocalDate.now(), MA_NHAN_VIEN_XUAT, maLichHen);

            List<ChiTietXuatKho> chiTietList = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : mapVatTuDangSoan.entrySet()) {
                chiTietList.add(new ChiTietXuatKho(maPhieuMoi, entry.getKey(), entry.getValue()));
            }

            phieuXuatKhoDAO.luuPhieuXuat(phieu, chiTietList);

            // Kiểm tra các vật tư chạm ngưỡng tối thiểu để nhắc nhở
            List<String> canhBaoTon = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : mapVatTuDangSoan.entrySet()) {
                VatTu vt = vatTuDAO.timTheoMa(entry.getKey());
                if (vt != null && vt.getSoLuongTon() <= vt.getNguongToiThieu()) {
                    canhBaoTon.add(vt.getTenVatTu() + " (còn " + vt.getSoLuongTon() + " " + vt.getDonViTinh() + ")");
                }
            }

            String msg = "Đã lưu phiếu xuất " + maPhieuMoi + " thành công và tự động trừ tồn kho!";
            if (!canhBaoTon.isEmpty()) {
                msg += "\n\nLƯU Ý: Các vật tư sau đã chạm ngưỡng tối thiểu:\n- " + String.join("\n- ", canhBaoTon);
            }
            GiaoDienUtil.thongBao(this, msg);

            // Làm mới giao diện
            mapVatTuDangSoan.clear();
            capNhatBangPhieu();
            taiDanhSachVatTu();
            taiLichSuXuat();
        } catch (SQLException e) {
            GiaoDienUtil.baoLoi(this, e);
        }
    }
}
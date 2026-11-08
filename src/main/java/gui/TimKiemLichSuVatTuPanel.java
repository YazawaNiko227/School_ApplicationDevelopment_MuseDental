package gui;

import com.toedter.calendar.JDateChooser;
import entity.BenhNhan;
import entity.ChiTietXuatKho;
import entity.LichHen;
import entity.PhieuXuatKho;
import service.VatTuTieuHaoService;
import util.GiaoDienUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/** Tra cứu các phiếu xuất vật tư và xem chi tiết từng lần xuất. */
public class TimKiemLichSuVatTuPanel extends JPanel {
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final VatTuTieuHaoService service = new VatTuTieuHaoService();
    private final List<PhieuXuatKho> ketQuaDangHienThi = new ArrayList<>();

    private final JTextField txtTuKhoa = new JTextField();
    private final JDateChooser dtTuNgay = new JDateChooser();
    private final JDateChooser dtDenNgay = new JDateChooser();
    private final JLabel lblKetQua = new JLabel(" ");
    private final DefaultTableModel model = new DefaultTableModel(
            new String[] { "Mã phiếu", "Ngày xuất", "Mã lịch hẹn", "Mã BN", "Bệnh nhân", "Số loại vật tư", "Tổng SL" },
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bang = new JTable(model);

    public TimKiemLichSuVatTuPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        add(taoTheBoLoc(), BorderLayout.NORTH);
        add(taoTheKetQua(), BorderLayout.CENTER);
        timKiem();
    }

    private JPanel taoTheBoLoc() {
        JPanel the = taoThe();
        the.add(GiaoDienUtil.taoTieuDe("Bộ lọc tra cứu vật tư đã xuất"));
        the.add(Box.createVerticalStrut(12));

        GiaoDienUtil.tuyChinhO(txtTuKhoa);
        txtTuKhoa.setToolTipText("Mã phiếu, lịch hẹn, bệnh nhân hoặc vật tư");
        txtTuKhoa.addActionListener(event -> timKiem());
        GiaoDienUtil.cauHinhChonNgay(dtTuNgay, "dd/MM/yyyy");
        GiaoDienUtil.cauHinhChonNgay(dtDenNgay, "dd/MM/yyyy");

        JPanel hang = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        hang.setOpaque(false);
        hang.setAlignmentX(Component.LEFT_ALIGNMENT);
        hang.add(taoTruongLoc("Từ khóa", txtTuKhoa));
        hang.add(taoTruongLoc("Từ ngày xuất", dtTuNgay));
        hang.add(taoTruongLoc("Đến ngày xuất", dtDenNgay));

        JButton btnTim = GiaoDienUtil.taoNut("Tìm kiếm", true);
        btnTim.addActionListener(event -> timKiem());
        JButton btnDatLai = GiaoDienUtil.taoNut("Đặt lại", false);
        btnDatLai.addActionListener(event -> datLai());
        JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        nut.setOpaque(false);
        nut.add(btnTim);
        nut.add(btnDatLai);
        hang.add(nut);
        the.add(hang);
        return the;
    }

    private JPanel taoTheKetQua() {
        JPanel the = new JPanel(new BorderLayout(0, 12));
        GiaoDienUtil.trangTriThe(the);
        JPanel tieuDe = new JPanel(new BorderLayout());
        tieuDe.setOpaque(false);
        tieuDe.add(GiaoDienUtil.taoTieuDe("Danh sách phiếu xuất vật tư"), BorderLayout.WEST);
        lblKetQua.setFont(GiaoDienUtil.NHO);
        lblKetQua.setForeground(GiaoDienUtil.MUTED);
        tieuDe.add(lblKetQua, BorderLayout.EAST);
        the.add(tieuDe, BorderLayout.NORTH);

        GiaoDienUtil.tuyChinhBang(bang);
        bang.getColumnModel().getColumn(0).setPreferredWidth(85);
        bang.getColumnModel().getColumn(1).setPreferredWidth(90);
        bang.getColumnModel().getColumn(2).setPreferredWidth(95);
        bang.getColumnModel().getColumn(3).setPreferredWidth(75);
        bang.getColumnModel().getColumn(4).setPreferredWidth(150);
        bang.getColumnModel().getColumn(5).setPreferredWidth(110);
        bang.getColumnModel().getColumn(6).setPreferredWidth(80);
        bang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 && bang.getSelectedRow() >= 0) {
                    xemChiTiet();
                }
            }
        });
        JScrollPane cuon = new JScrollPane(bang);
        cuon.setBorder(BorderFactory.createLineBorder(GiaoDienUtil.LINE));
        cuon.getViewport().setBackground(Color.WHITE);
        the.add(cuon, BorderLayout.CENTER);

        JButton btnChiTiet = GiaoDienUtil.taoNut("Xem chi tiết", true);
        btnChiTiet.addActionListener(event -> xemChiTiet());
        JPanel hanhDong = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        hanhDong.setOpaque(false);
        hanhDong.add(btnChiTiet);
        the.add(hanhDong, BorderLayout.SOUTH);
        return the;
    }

    private void timKiem() {
        LocalDate tuNgay = GiaoDienUtil.layNgayDaChon(dtTuNgay);
        LocalDate denNgay = GiaoDienUtil.layNgayDaChon(dtDenNgay);
        String loiKhoangNgay = service.kiemTraKhoangNgay(tuNgay, denNgay);
        if (loiKhoangNgay != null) {
            JOptionPane.showMessageDialog(this, loiKhoangNgay,
                    "Khoảng ngày không hợp lệ", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ketQuaDangHienThi.clear();
        model.setRowCount(0);
        try {
            List<PhieuXuatKho> ketQua = service.timKiemPhieuXuat(txtTuKhoa.getText().trim(), tuNgay, denNgay);
            for (PhieuXuatKho phieu : ketQua) {
                ketQuaDangHienThi.add(phieu);
                LichHen lichHen = phieu.getLichHen();
                BenhNhan benhNhan = lichHen == null ? null : lichHen.getBenhNhan();
                model.addRow(new Object[] {
                        phieu.getMaPhieuXuat(),
                        phieu.getNgayXuat().format(NGAY),
                        phieu.getMaLichHenId() == null ? "Dùng chung" : phieu.getMaLichHenId(),
                        benhNhan == null ? "" : benhNhan.getMaBenhNhan(),
                        benhNhan == null ? "Xuất dùng chung" : benhNhan.getHoTen(),
                        phieu.getSoLoaiVatTu(),
                        phieu.getTongSoLuong()
                });
            }
            lblKetQua.setText(ketQua.isEmpty()
                    ? "Không tìm thấy phiếu xuất phù hợp"
                    : "Tìm thấy " + ketQua.size() + " phiếu xuất");
        } catch (java.sql.SQLException exception) {
            lblKetQua.setText("Không thể tải kết quả");
            JOptionPane.showMessageDialog(this,
                    "Không thể tra cứu lịch sử vật tư từ CSDL:\n" + exception.getMessage(),
                    "Lỗi tra cứu vật tư", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void datLai() {
        txtTuKhoa.setText("");
        dtTuNgay.setDate(null);
        dtDenNgay.setDate(null);
        timKiem();
    }

    private void xemChiTiet() {
        int row = bang.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu xuất trong danh sách.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        PhieuXuatKho phieu = ketQuaDangHienThi.get(bang.convertRowIndexToModel(row));
        List<ChiTietXuatKho> chiTiet;
        try {
            chiTiet = service.layChiTietPhieuXuat(phieu.getMaPhieuXuat());
        } catch (java.sql.SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "Không thể tải chi tiết phiếu xuất " + phieu.getMaPhieuXuat() + ":\n" + exception.getMessage(),
                    "Lỗi tải chi tiết", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Chi tiết phiếu xuất " + phieu.getMaPhieuXuat(),
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(0, 12));
        JPanel noiDung = new JPanel(new BorderLayout(0, 12));
        noiDung.setBackground(Color.WHITE);
        noiDung.setBorder(new EmptyBorder(16, 18, 16, 18));
        noiDung.add(new JLabel("Phiếu " + phieu.getMaPhieuXuat() + "  ·  Ngày xuất "
                + phieu.getNgayXuat().format(NGAY) + "  ·  Lịch hẹn "
                + (phieu.getMaLichHenId() == null ? "dùng chung" : phieu.getMaLichHenId())), BorderLayout.NORTH);

        DefaultTableModel modelChiTiet = new DefaultTableModel(
                new String[] { "Mã vật tư", "Tên vật tư", "ĐVT", "Số lượng xuất" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (ChiTietXuatKho dong : chiTiet) {
            modelChiTiet.addRow(new Object[] {
                    dong.getMaVatTuId(), dong.getVatTu().getTenVatTu(),
                    dong.getVatTu().getDonViTinh() == null ? "" : dong.getVatTu().getDonViTinh(),
                    dong.getSoLuongSuDung()
            });
        }
        JTable bangChiTiet = new JTable(modelChiTiet);
        GiaoDienUtil.tuyChinhBang(bangChiTiet);
        JScrollPane cuon = new JScrollPane(bangChiTiet);
        cuon.setPreferredSize(new Dimension(620, 280));
        noiDung.add(cuon, BorderLayout.CENTER);
        dialog.add(noiDung, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JPanel taoThe() {
        JPanel the = new JPanel();
        the.setLayout(new BoxLayout(the, BoxLayout.Y_AXIS));
        GiaoDienUtil.trangTriThe(the);
        return the;
    }

    private JPanel taoTruongLoc(String ten, Component truong) {
        JPanel cot = new JPanel();
        cot.setOpaque(false);
        cot.setLayout(new BoxLayout(cot, BoxLayout.Y_AXIS));
        JLabel nhan = new JLabel(ten);
        nhan.setFont(GiaoDienUtil.NHO_DAM);
        nhan.setForeground(GiaoDienUtil.INK);
        nhan.setAlignmentX(Component.LEFT_ALIGNMENT);
        truong.setPreferredSize(new Dimension(180, 32));
        truong.setMaximumSize(new Dimension(220, 32));
        cot.add(nhan);
        cot.add(Box.createVerticalStrut(4));
        cot.add(truong);
        return cot;
    }
}

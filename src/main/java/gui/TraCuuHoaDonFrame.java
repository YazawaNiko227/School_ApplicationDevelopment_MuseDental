package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class TraCuuHoaDonFrame extends JFrame implements ActionListener, MouseListener {
    private static final long serialVersionUID = 1L;

    private JTextField txtMaHD, txtNgayLap, txtMaBN, txtTenBN, txtMaNV, txtTongTien;
    private JComboBox<String> cboPhuongThuc, cboTrangThai;
    private JButton btnChonNgay;
    private JLabel lbShowMessage;
    private JButton btnTimKiem, btnXemChiTiet, btnLamMoi, btnXoaRong;
    private final String[] cols = {
            "Mã HĐ", "Ngày lập", "Mã bệnh nhân", "Tên bệnh nhân", "Mã nhân viên",
            "Phương thức TT", "Trạng thái", "Tổng tiền (VNĐ)"
    };

    private DefaultTableModel modelTable;
    private JTable table;
    private JComboBox<String> cboHoaDon;
    private JPanel pMain;
    private final DecimalFormat df = new DecimalFormat("#,##0 VNĐ");
    private final DecimalFormat dfRaw = new DecimalFormat("#,##0");

    public TraCuuHoaDonFrame() {
        setTitle("Tra Cứu Hóa Đơn / Giao Dịch - Muse Dental");
        setSize(1220, 720);
        setResizable(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        createGUI();
    }

    public void createGUI() {
        pMain = new JPanel();
        pMain.setLayout(new BoxLayout(pMain, BoxLayout.Y_AXIS));
        pMain.setBackground(new Color(244, 248, 249));
        pMain.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JPanel pTren = new JPanel();
        JPanel pGiua = new JPanel();
        JPanel pDuoi = new JPanel();

        pTren.setLayout(new GridLayout(5, 4, 12, 8));
        pTren.setBackground(Color.WHITE);
        pTren.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(180, 205, 215)),
                        "Thông tin tra cứu / Chi tiết hóa đơn",
                        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                        javax.swing.border.TitledBorder.DEFAULT_POSITION,
                        new Font("Arial", Font.BOLD, 13),
                        new Color(32, 53, 68)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        JLabel lbMaHD = new JLabel("Mã hóa đơn (HDxxx):");
        JLabel lbNgayLap = new JLabel("Ngày lập (yyyy-MM-dd):");
        JLabel lbMaBN = new JLabel("Mã bệnh nhân (BNxxx):");
        JLabel lbTenBN = new JLabel("Tên bệnh nhân:");
        JLabel lbMaNV = new JLabel("Mã nhân viên lập:");
        JLabel lbPhuongThuc = new JLabel("Phương thức thanh toán:");
        JLabel lbTrangThai = new JLabel("Trạng thái thanh toán:");
        JLabel lbTongTien = new JLabel("Tổng tiền (VNĐ):");

        Font labelFont = new Font("Arial", Font.PLAIN, 12);
        Color labelColor = new Color(32, 53, 68);
        for (JLabel label : new JLabel[] { lbMaHD, lbNgayLap, lbMaBN, lbTenBN, lbMaNV, lbPhuongThuc, lbTrangThai, lbTongTien }) {
            label.setFont(labelFont);
            label.setForeground(labelColor);
        }

        txtMaHD = new JTextField(15);
        txtMaBN = new JTextField(15);
        txtTenBN = new JTextField(15);
        txtMaNV = new JTextField(15);
        txtTongTien = new JTextField(15);

        JPanel pNgay = new JPanel(new BorderLayout(4, 0));
        pNgay.setBackground(Color.WHITE);
        txtNgayLap = new JTextField(10);
        txtNgayLap.setText(LocalDate.now().toString());
        txtNgayLap.setEditable(false);
        txtNgayLap.setFocusable(false);
        txtNgayLap.setBackground(new Color(245, 245, 245));

        btnChonNgay = new JButton("Chọn ngày");
        btnChonNgay.setFont(new Font("Arial", Font.BOLD, 11));
        btnChonNgay.setBackground(Color.BLACK);
        btnChonNgay.setForeground(Color.WHITE);
        btnChonNgay.setFocusPainted(false);
        btnChonNgay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnChonNgay.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        btnChonNgay.setToolTipText("Mở bảng lịch chọn ngày, tháng, năm");
        btnChonNgay.addActionListener(e -> hienThiBangChonNgay(txtNgayLap));
        pNgay.add(txtNgayLap, BorderLayout.CENTER);
        pNgay.add(btnChonNgay, BorderLayout.EAST);

        cboPhuongThuc = new JComboBox<>(new String[] { "Tiền mặt", "Chuyển khoản", "Trả góp" });
        cboPhuongThuc.setBackground(Color.WHITE);
        cboTrangThai = new JComboBox<>(new String[] { "Chưa thanh toán", "Đã thanh toán", "Đang trả góp" });
        cboTrangThai.setBackground(Color.WHITE);

        lbShowMessage = new JLabel("");
        lbShowMessage.setForeground(new Color(214, 48, 49));
        lbShowMessage.setFont(new Font("Arial", Font.ITALIC, 12));

        pTren.add(lbMaHD);
        pTren.add(txtMaHD);
        pTren.add(lbNgayLap);
        pTren.add(pNgay);

        pTren.add(lbMaBN);
        pTren.add(txtMaBN);
        pTren.add(lbTenBN);
        pTren.add(txtTenBN);

        pTren.add(lbMaNV);
        pTren.add(txtMaNV);
        pTren.add(lbPhuongThuc);
        pTren.add(cboPhuongThuc);

        pTren.add(lbTrangThai);
        pTren.add(cboTrangThai);
        pTren.add(lbTongTien);
        pTren.add(txtTongTien);

        pTren.add(new JLabel("Thông báo:"));
        pTren.add(lbShowMessage);
        pTren.add(new JLabel(""));
        pTren.add(new JLabel(""));

        pGiua.setLayout(new BoxLayout(pGiua, BoxLayout.X_AXIS));
        pGiua.setOpaque(false);
        pGiua.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        btnTimKiem = new JButton("Tìm kiếm");
        btnXemChiTiet = new JButton("Xem Chi Tiết");
        btnLamMoi = new JButton("Làm Mới");
        btnXoaRong = new JButton("Xóa Rỗng");

        JLabel lbTimKiem = new JLabel("Tìm theo mã HĐ:");
        lbTimKiem.setFont(new Font("Arial", Font.BOLD, 12));
        lbTimKiem.setForeground(new Color(32, 53, 68));

        cboHoaDon = new JComboBox<>();
        cboHoaDon.addItem("None");
        cboHoaDon.setPreferredSize(new Dimension(115, 28));
        cboHoaDon.setMaximumSize(new Dimension(120, 28));

        styleButton(btnTimKiem, new Color(26, 151, 151), Color.WHITE);
        styleButton(btnXemChiTiet, new Color(39, 110, 144), Color.WHITE);
        styleButton(btnLamMoi, new Color(240, 243, 245), new Color(32, 53, 68));
        styleButton(btnXoaRong, new Color(240, 243, 245), new Color(32, 53, 68));

        pGiua.add(Box.createHorizontalGlue());
        pGiua.add(btnTimKiem);
        pGiua.add(Box.createHorizontalStrut(8));
        pGiua.add(btnXemChiTiet);
        pGiua.add(Box.createHorizontalStrut(8));
        pGiua.add(btnLamMoi);
        pGiua.add(Box.createHorizontalStrut(8));
        pGiua.add(btnXoaRong);
        pGiua.add(Box.createHorizontalStrut(18));
        pGiua.add(lbTimKiem);
        pGiua.add(cboHoaDon);
        pGiua.add(Box.createHorizontalGlue());

        pDuoi.setBackground(Color.WHITE);
        pDuoi.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(180, 205, 215)),
                        "Danh sách hóa đơn / giao dịch điều trị",
                        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                        javax.swing.border.TitledBorder.DEFAULT_POSITION,
                        new Font("Arial", Font.BOLD, 13),
                        new Color(32, 53, 68)),
                BorderFactory.createEmptyBorder(6, 10, 10, 10)));
        pDuoi.setLayout(new BoxLayout(pDuoi, BoxLayout.X_AXIS));

        modelTable = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(modelTable);
        table.setRowHeight(28);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(235, 243, 245));
        table.getTableHeader().setForeground(new Color(32, 53, 68));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(230, 235, 238));

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);

        JScrollPane scp = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scp.setPreferredSize(new Dimension(1150, 300));
        pDuoi.add(scp);

        taiDuLieuVaoBang();

        pMain.add(pTren);
        pMain.add(Box.createVerticalStrut(6));
        pMain.add(pGiua);
        pMain.add(Box.createVerticalStrut(6));
        pMain.add(pDuoi);

        add(pMain);

        btnTimKiem.addActionListener(this);
        btnXemChiTiet.addActionListener(this);
        btnLamMoi.addActionListener(this);
        btnXoaRong.addActionListener(this);
        cboHoaDon.addActionListener(this);
        table.addMouseListener(this);
    }

    private void taiDuLieuVaoBang() {
        modelTable.setRowCount(0);
        cboHoaDon.removeAllItems();
        cboHoaDon.addItem("None");

        Object[][] data = {
                { "HD001", "2026-10-01", "BN001", "Nguyễn Văn A", "NV001", "Tiền mặt", "Đã thanh toán", 1500000 },
                { "HD002", "2026-10-02", "BN002", "Trần Thị B", "NV003", "Chuyển khoản", "Đã thanh toán", 3200000 },
                { "HD003", "2026-10-03", "BN003", "Lê Hoàng C", "NV005", "Trả góp", "Đang trả góp", 4800000 },
                { "HD004", "2026-10-04", "BN004", "Phạm Minh D", "NV002", "Tiền mặt", "Chưa thanh toán", 2100000 },
                { "HD005", "2026-10-05", "BN005", "Đỗ Ánh E", "NV004", "Chuyển khoản", "Đã thanh toán", 5600000 }
        };

        for (Object[] row : data) {
            modelTable.addRow(row);
            cboHoaDon.addItem(String.valueOf(row[0]));
        }
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 215, 220)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source.equals(btnTimKiem)) {
            thucHienTraCuu();
        } else if (source.equals(btnXemChiTiet)) {
            xemChiTietHoaDonVaGiaoDich();
        } else if (source.equals(btnLamMoi)) {
            taiDuLieuVaoBang();
            xoaRongForm();
            lbShowMessage.setText("Đã làm mới danh sách hóa đơn.");
        } else if (source.equals(btnXoaRong)) {
            xoaRongForm();
            lbShowMessage.setText("");
        } else if (source.equals(cboHoaDon)) {
            Object selected = cboHoaDon.getSelectedItem();
            if (selected != null) {
                String ma = selected.toString();
                timHoaDon(ma);
                int row = -1;
                for (int i = 0; i < table.getRowCount(); i++) {
                    if (table.getValueAt(i, 0).toString().equals(ma)) {
                        row = i;
                        break;
                    }
                }
                if (row >= 0) {
                    chonDong(row);
                }
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int row = table.getSelectedRow();
        if (row >= 0) {
            chonDong(row);
            if (e.getClickCount() == 2) {
                xemChiTietHoaDonVaGiaoDich();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    private void thucHienTraCuu() {
        String maHD = txtMaHD.getText().trim().toLowerCase();
        String ngayLap = txtNgayLap.getText().trim();
        String maBN = txtMaBN.getText().trim().toLowerCase();
        String tenBN = txtTenBN.getText().trim().toLowerCase();
        String maNV = txtMaNV.getText().trim().toLowerCase();
        String pt = cboPhuongThuc.getSelectedItem() != null ? cboPhuongThuc.getSelectedItem().toString() : "";
        String tt = cboTrangThai.getSelectedItem() != null ? cboTrangThai.getSelectedItem().toString() : "";

        if (!kiemTraData()) {
            return;
        }

        DefaultTableModel filteredModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (int i = 0; i < modelTable.getRowCount(); i++) {
            String rowMaHD = String.valueOf(modelTable.getValueAt(i, 0)).toLowerCase();
            String rowNgay = String.valueOf(modelTable.getValueAt(i, 1));
            String rowMaBN = String.valueOf(modelTable.getValueAt(i, 2)).toLowerCase();
            String rowTenBN = String.valueOf(modelTable.getValueAt(i, 3)).toLowerCase();
            String rowMaNV = String.valueOf(modelTable.getValueAt(i, 4)).toLowerCase();
            String rowPt = String.valueOf(modelTable.getValueAt(i, 5));
            String rowTt = String.valueOf(modelTable.getValueAt(i, 6));
            String rowTong = String.valueOf(modelTable.getValueAt(i, 7));

            boolean match = true;
            if (!maHD.isEmpty() && !rowMaHD.contains(maHD)) match = false;
            if (!ngayLap.isEmpty() && !rowNgay.equals(ngayLap)) match = false;
            if (!maBN.isEmpty() && !rowMaBN.contains(maBN)) match = false;
            if (!tenBN.isEmpty() && !rowTenBN.contains(tenBN)) match = false;
            if (!maNV.isEmpty() && !rowMaNV.contains(maNV)) match = false;
            if (!pt.isEmpty() && !pt.equals("") && !rowPt.equals(pt)) match = false;
            if (!tt.isEmpty() && !tt.equals("") && !rowTt.equals(tt)) match = false;
            if (!txtTongTien.getText().trim().isEmpty() && !rowTong.contains(txtTongTien.getText().trim().replace(".", ""))) match = false;

            if (match) {
                filteredModel.addRow(new Object[] {
                        modelTable.getValueAt(i, 0),
                        modelTable.getValueAt(i, 1),
                        modelTable.getValueAt(i, 2),
                        modelTable.getValueAt(i, 3),
                        modelTable.getValueAt(i, 4),
                        modelTable.getValueAt(i, 5),
                        modelTable.getValueAt(i, 6),
                        modelTable.getValueAt(i, 7)
                });
            }
        }

        table.setModel(filteredModel);
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {{ setHorizontalAlignment(JLabel.RIGHT); }});
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.setRowHeight(28);

        if (filteredModel.getRowCount() == 0) {
            lbShowMessage.setText("Không tìm thấy hóa đơn phù hợp.");
        } else {
            lbShowMessage.setText("Tìm thấy " + filteredModel.getRowCount() + " hóa đơn.");
        }
    }

    private void xemChiTietHoaDonVaGiaoDich() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 hóa đơn trong danh sách để xem chi tiết!", "Nhắc nhở",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String maHD = table.getValueAt(row, 0).toString();
        String ngayLap = table.getValueAt(row, 1).toString();
        String maBN = table.getValueAt(row, 2).toString();
        String tenBN = table.getValueAt(row, 3).toString();
        String maNV = table.getValueAt(row, 4).toString();
        String pt = table.getValueAt(row, 5).toString();
        String tt = table.getValueAt(row, 6).toString();
        String tongTien = table.getValueAt(row, 7).toString();

        JDialog dialog = new JDialog(this, "Chi tiết hóa đơn & Lịch sử giao dịch: " + maHD, true);
        dialog.setSize(860, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel pContent = new JPanel();
        pContent.setLayout(new BoxLayout(pContent, BoxLayout.Y_AXIS));
        pContent.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        pContent.setBackground(Color.WHITE);

        JPanel pThongTin = new JPanel(new GridLayout(4, 2, 8, 4));
        pThongTin.setBackground(Color.WHITE);
        pThongTin.setBorder(BorderFactory.createTitledBorder("Thông tin chung"));
        pThongTin.add(new JLabel("Mã HĐ: " + maHD));
        pThongTin.add(new JLabel("Ngày lập: " + ngayLap));
        pThongTin.add(new JLabel("Bệnh nhân: " + tenBN + " (" + maBN + ")"));
        pThongTin.add(new JLabel("Mã nhân viên: " + maNV));
        pThongTin.add(new JLabel("Phương thức: " + pt));
        pThongTin.add(new JLabel("Trạng thái: " + tt));
        pThongTin.add(new JLabel("Tổng tiền: " + df.format(Double.parseDouble(tongTien.replace(",", "")))));
        pThongTin.add(new JLabel(""));

        String[] colsDichVu = { "Mã DV", "Tên dịch vụ", "Số lượng", "Đơn giá", "Thành tiền" };
        DefaultTableModel modelDV = new DefaultTableModel(colsDichVu, 0);
        modelDV.addRow(new Object[] { "DV001", "Chụp X-Quang", 1, "300000", "300000" });
        modelDV.addRow(new Object[] { "DV010", "Niềng răng", 1, "4500000", "4500000" });
        JTable tableDV = new JTable(modelDV);
        tableDV.setRowHeight(24);
        JScrollPane scpDV = new JScrollPane(tableDV);
        scpDV.setBorder(BorderFactory.createTitledBorder("Chi tiết dịch vụ điều trị"));
        scpDV.setPreferredSize(new Dimension(820, 160));

        String[] colsDot = { "Mã đợt", "Ngày đến hạn", "Số tiền", "Trạng thái", "Ghi chú" };
        DefaultTableModel modelDot = new DefaultTableModel(colsDot, 0);
        modelDot.addRow(new Object[] { "DT01", "2026-10-15", "1500000", "Đã thanh toán", "Thanh toán lần 1" });
        modelDot.addRow(new Object[] { "DT02", "2026-11-15", "1500000", "Chưa thanh toán", "Thanh toán còn lại" });
        JTable tableDot = new JTable(modelDot);
        tableDot.setRowHeight(24);
        JScrollPane scpDot = new JScrollPane(tableDot);
        scpDot.setBorder(BorderFactory.createTitledBorder("Lịch sử giao dịch / Đợt thanh toán"));
        scpDot.setPreferredSize(new Dimension(820, 140));

        JPanel pBottom = new JPanel(new BorderLayout());
        pBottom.setBackground(Color.WHITE);
        JLabel lbTong = new JLabel("TỔNG GIÁ TRỊ HÓA ĐƠN: " + df.format(Double.parseDouble(tongTien.replace(",", ""))) + "   ");
        lbTong.setFont(new Font("Arial", Font.BOLD, 14));
        lbTong.setForeground(new Color(26, 151, 151));

        JButton btnDong = new JButton("Đóng");
        btnDong.addActionListener(evt -> dialog.dispose());
        pBottom.add(lbTong, BorderLayout.WEST);
        pBottom.add(btnDong, BorderLayout.EAST);

        pContent.add(pThongTin);
        pContent.add(Box.createVerticalStrut(8));
        pContent.add(scpDV);
        pContent.add(Box.createVerticalStrut(8));
        pContent.add(scpDot);
        pContent.add(Box.createVerticalStrut(8));
        pContent.add(pBottom);

        dialog.add(pContent);
        dialog.setVisible(true);
    }

    private void timHoaDon(String maHD) {
        if ("None".equals(maHD)) {
            table.clearSelection();
            if (table.getRowCount() > 0) {
                Rectangle cellRect = table.getCellRect(0, 0, true);
                table.scrollRectToVisible(cellRect);
            }
        } else {
            for (int i = 0; i < table.getRowCount(); i++) {
                if (table.getValueAt(i, 0).toString().equals(maHD)) {
                    table.setRowSelectionInterval(i, i);
                    Rectangle cellRect = table.getCellRect(i, 0, true);
                    table.scrollRectToVisible(cellRect);
                    return;
                }
            }
        }
    }

    private void chonDong(int row) {
        if (row >= 0 && row < table.getRowCount()) {
            txtMaHD.setText(table.getValueAt(row, 0).toString());
            txtNgayLap.setText(table.getValueAt(row, 1).toString());
            txtMaBN.setText(table.getValueAt(row, 2).toString());
            txtTenBN.setText(table.getValueAt(row, 3).toString());
            txtMaNV.setText(table.getValueAt(row, 4).toString());
            cboPhuongThuc.setSelectedItem(table.getValueAt(row, 5).toString());
            cboTrangThai.setSelectedItem(table.getValueAt(row, 6).toString());
            txtTongTien.setText(table.getValueAt(row, 7).toString());
        }
    }

    private void xoaRongForm() {
        txtMaHD.setText("");
        txtNgayLap.setText(LocalDate.now().toString());
        txtMaBN.setText("");
        txtTenBN.setText("");
        txtMaNV.setText("");
        cboPhuongThuc.setSelectedIndex(0);
        cboTrangThai.setSelectedIndex(0);
        txtTongTien.setText("0");
        cboHoaDon.setSelectedIndex(0);
        table.clearSelection();
        txtMaHD.requestFocus();
    }

    private void showMessage(String message, JTextField txt) {
        lbShowMessage.setText(message);
        txt.requestFocus();
    }

    private void hienThiBangChonNgay(JTextField targetTxt) {
        JDialog dlgCalendar = new JDialog(this, "Bảng chọn ngày, tháng, năm", true);
        dlgCalendar.setSize(390, 360);
        dlgCalendar.setLocationRelativeTo(this);
        dlgCalendar.setLayout(new BorderLayout(8, 8));

        int[] initDate = layNgayThangNam(targetTxt.getText().trim());
        int currYear = initDate[0];
        int currMonth = initDate[1];

        JPanel pTop = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        pTop.setBackground(new Color(244, 248, 249));

        JButton btnPrevMonth = new JButton("◀");
        JButton btnNextMonth = new JButton("▶");
        JLabel lbThang = new JLabel("Tháng:");
        JComboBox<Integer> cboThang = new JComboBox<>();
        for (int m = 1; m <= 12; m++) {
            cboThang.addItem(m);
        }
        cboThang.setSelectedItem(currMonth);

        JLabel lbNam = new JLabel("Năm:");
        JComboBox<Integer> cboNam = new JComboBox<>();
        for (int y = 1970; y <= 2040; y++) {
            cboNam.addItem(y);
        }
        cboNam.setSelectedItem(currYear);

        pTop.add(btnPrevMonth);
        pTop.add(lbThang);
        pTop.add(cboThang);
        pTop.add(lbNam);
        pTop.add(cboNam);
        pTop.add(btnNextMonth);

        String[] headerCols = { "T2", "T3", "T4", "T5", "T6", "T7", "CN" };
        DefaultTableModel modelCal = new DefaultTableModel(headerCols, 6) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable tableCal = new JTable(modelCal);
        tableCal.setRowHeight(32);
        tableCal.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableCal.setCellSelectionEnabled(true);
        tableCal.setFont(new Font("Arial", Font.PLAIN, 13));
        tableCal.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tableCal.getTableHeader().setBackground(new Color(220, 235, 238));

        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(JLabel.CENTER);
        for (int c = 0; c < 7; c++) {
            tableCal.getColumnModel().getColumn(c).setCellRenderer(centerRender);
        }

        Runnable capNhatLich = () -> {
            int selYear = (Integer) cboNam.getSelectedItem();
            int selMonth = (Integer) cboThang.getSelectedItem();
            for (int r = 0; r < 6; r++) {
                for (int c = 0; c < 7; c++) {
                    modelCal.setValueAt("", r, c);
                }
            }

            YearMonth ym = YearMonth.of(selYear, selMonth);
            LocalDate firstDay = ym.atDay(1);
            int startCol = firstDay.getDayOfWeek().getValue() - 1;
            int daysInMonth = ym.lengthOfMonth();

            int r = 0;
            int c = startCol;
            for (int d = 1; d <= daysInMonth; d++) {
                modelCal.setValueAt(d, r, c);
                c++;
                if (c == 7) {
                    c = 0;
                    r++;
                }
            }
        };

        capNhatLich.run();
        cboThang.addActionListener(e -> capNhatLich.run());
        cboNam.addActionListener(e -> capNhatLich.run());

        btnPrevMonth.addActionListener(e -> {
            int m = (Integer) cboThang.getSelectedItem();
            int y = (Integer) cboNam.getSelectedItem();
            if (m == 1) {
                cboThang.setSelectedItem(12);
                cboNam.setSelectedItem(y - 1);
            } else {
                cboThang.setSelectedItem(m - 1);
            }
        });

        btnNextMonth.addActionListener(e -> {
            int m = (Integer) cboThang.getSelectedItem();
            int y = (Integer) cboNam.getSelectedItem();
            if (m == 12) {
                cboThang.setSelectedItem(1);
                cboNam.setSelectedItem(y + 1);
            } else {
                cboThang.setSelectedItem(m + 1);
            }
        });

        tableCal.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tableCal.getSelectedRow();
                int col = tableCal.getSelectedColumn();
                if (row >= 0 && col >= 0) {
                    Object val = tableCal.getValueAt(row, col);
                    if (val != null && !val.toString().trim().isEmpty()) {
                        int day = Integer.parseInt(val.toString());
                        int selYear = (Integer) cboNam.getSelectedItem();
                        int selMonth = (Integer) cboThang.getSelectedItem();
                        String dateStr = String.format("%04d-%02d-%02d", selYear, selMonth, day);
                        targetTxt.setText(dateStr);
                        dlgCalendar.dispose();
                    }
                }
            }
        });

        JPanel pBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        JButton btnXoaNgay = new JButton("Xóa ngày");
        btnXoaNgay.addActionListener(e -> {
            targetTxt.setText("");
            dlgCalendar.dispose();
        });
        JButton btnHomNay = new JButton("Hôm nay");
        btnHomNay.addActionListener(e -> {
            targetTxt.setText(LocalDate.now().toString());
            dlgCalendar.dispose();
        });
        JButton btnHuy = new JButton("Đóng");
        btnHuy.addActionListener(e -> dlgCalendar.dispose());
        pBottom.add(btnXoaNgay);
        pBottom.add(btnHomNay);
        pBottom.add(btnHuy);

        dlgCalendar.add(pTop, BorderLayout.NORTH);
        dlgCalendar.add(new JScrollPane(tableCal), BorderLayout.CENTER);
        dlgCalendar.add(pBottom, BorderLayout.SOUTH);
        dlgCalendar.setVisible(true);
    }

    private int[] layNgayThangNam(String text) {
        LocalDate now = LocalDate.now();
        if (text != null && text.matches("\\d{4}-\\d{2}-\\d{2}")) {
            try {
                LocalDate parsed = LocalDate.parse(text);
                return new int[] { parsed.getYear(), parsed.getMonthValue(), parsed.getDayOfMonth() };
            } catch (Exception ignored) {
            }
        }
        return new int[] { now.getYear(), now.getMonthValue(), now.getDayOfMonth() };
    }

    private boolean kiemTraData() {
        String maHD = txtMaHD.getText().trim();
        String ngayLap = txtNgayLap.getText().trim();
        String maBN = txtMaBN.getText().trim();
        String maNV = txtMaNV.getText().trim();
        String tongTienStr = txtTongTien.getText().trim().replace(",", "").replace(".", "").replace(" VNĐ", "").replace("VND", "");

        if (maHD.length() > 0 && !maHD.matches("(?i)HD\\d{3}")) {
            showMessage("Error: Mã hóa đơn phải theo mẫu: HDxxx (ví dụ: HD001)", txtMaHD);
            return false;
        }

        if (ngayLap.length() > 0 && !ngayLap.matches("\\d{4}-\\d{2}-\\d{2}")) {
            showMessage("Error: Ngày lập phải theo định dạng yyyy-MM-dd (ví dụ 2026-10-13)", txtNgayLap);
            return false;
        }

        if (maBN.length() > 0 && !maBN.matches("(?i)BN\\d{3}")) {
            showMessage("Error: Mã bệnh nhân phải theo mẫu: BNxxx (ví dụ: BN001)", txtMaBN);
            return false;
        }

        if (maNV.length() > 0 && !maNV.matches("(?i)NV\\d{1,10}")) {
            showMessage("Error: Mã nhân viên phải theo mẫu: NVxxx (ví dụ NV001)", txtMaNV);
            return false;
        }

        if (tongTienStr.length() > 0) {
            try {
                double num = Double.parseDouble(tongTienStr);
                if (num < 0) {
                    showMessage("Error: Tổng tiền thanh toán phải >= 0.", txtTongTien);
                    return false;
                }
            } catch (NumberFormatException ex) {
                showMessage("Error: Tổng tiền thanh toán phải là số hợp lệ.", txtTongTien);
                return false;
            }
        }

        return true;
    }

    public JPanel getMainPanel() {
        return pMain;
    }

}

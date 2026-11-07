package gui;

import dto.DonThuocDaLuu;
import dto.ThuocDonThuocDaLuu;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import service.KeDonThuocService;

public class TimKiemDonThuocDaLuuPanel extends JPanel {
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color LINE = new Color(226, 234, 237);
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final KeDonThuocService service = new KeDonThuocService();
    private final List<DonThuocDaLuu> prescriptions = new ArrayList<>();
    private final JTextField searchField = new JTextField();
    private final JLabel prescriptionInfo = new JLabel("Chọn một đơn thuốc để xem chi tiết.");
    private final JLabel diagnosisInfo = new JLabel(" ");
    private final JLabel careInfo = new JLabel(" ");

    private final DefaultTableModel prescriptionModel = new DefaultTableModel(
            new String[] { "Mã đơn thuốc", "Ngày kê", "Mã bệnh nhân", "Bệnh nhân", "Mã lần khám" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable prescriptionTable = new JTable(prescriptionModel);
    private final TableRowSorter<DefaultTableModel> prescriptionSorter = new TableRowSorter<>(prescriptionModel);

    private final DefaultTableModel medicineModel = new DefaultTableModel(
            new String[] { "Mã thuốc", "Tên thuốc", "ĐVT", "Liều dùng", "Đường dùng", "Số lượng" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable medicineTable = new JTable(medicineModel);

    public TimKiemDonThuocDaLuuPanel() {
        setLayout(new BorderLayout(0, 14));
        setOpaque(false);
        add(createToolbar(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        prescriptionTable.setRowSorter(prescriptionSorter);
        prescriptionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        prescriptionTable.setRowHeight(30);
        prescriptionTable.getTableHeader().setReorderingAllowed(false);
        prescriptionTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                hienThiChiTietDaChon();
            }
        });
        medicineTable.setRowHeight(28);
        medicineTable.getTableHeader().setReorderingAllowed(false);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                locDanhSach();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                locDanhSach();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                locDanhSach();
            }
        });
        taiDanhSach();
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        JLabel heading = new JLabel("Đơn thuốc đã lưu");
        heading.setFont(new Font("Arial", Font.BOLD, 18));
        heading.setForeground(INK);
        toolbar.add(heading, BorderLayout.WEST);

        JPanel controls = new JPanel(new BorderLayout(8, 0));
        controls.setOpaque(false);
        searchField.setFont(new Font("Arial", Font.PLAIN, 12));
        searchField.setToolTipText("Tìm theo mã đơn thuốc, mã lần khám hoặc bệnh nhân");
        searchField.setPreferredSize(new Dimension(300, 36));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(6, 9, 6, 9)));
        JButton refreshButton = new JButton("Tải lại");
        refreshButton.setFont(new Font("Arial", Font.BOLD, 12));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setBackground(TEAL);
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(event -> taiDanhSach());
        controls.add(searchField, BorderLayout.CENTER);
        controls.add(refreshButton, BorderLayout.EAST);
        toolbar.add(controls, BorderLayout.EAST);
        return toolbar;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setOpaque(false);

        JPanel listCard = createCard();
        listCard.setLayout(new BorderLayout());
        styleTable(prescriptionTable);
        JScrollPane prescriptionsScroll = new JScrollPane(prescriptionTable);
        prescriptionsScroll.setBorder(BorderFactory.createEmptyBorder());
        listCard.add(prescriptionsScroll, BorderLayout.CENTER);
        content.add(listCard, BorderLayout.CENTER);

        JPanel detailsCard = createCard();
        detailsCard.setLayout(new BorderLayout(0, 8));
        detailsCard.setPreferredSize(new Dimension(0, 240));
        JPanel detailsHeader = new JPanel(new BorderLayout(0, 4));
        detailsHeader.setOpaque(false);
        prescriptionInfo.setFont(new Font("Arial", Font.BOLD, 13));
        prescriptionInfo.setForeground(INK);
        diagnosisInfo.setFont(new Font("Arial", Font.PLAIN, 11));
        diagnosisInfo.setForeground(MUTED);
        careInfo.setFont(new Font("Arial", Font.PLAIN, 11));
        careInfo.setForeground(MUTED);
        JPanel headingLines = new JPanel(new BorderLayout(0, 3));
        headingLines.setOpaque(false);
        headingLines.add(diagnosisInfo, BorderLayout.NORTH);
        headingLines.add(careInfo, BorderLayout.CENTER);
        detailsHeader.add(prescriptionInfo, BorderLayout.NORTH);
        detailsHeader.add(headingLines, BorderLayout.CENTER);
        detailsCard.add(detailsHeader, BorderLayout.NORTH);

        styleTable(medicineTable);
        JScrollPane medicinesScroll = new JScrollPane(medicineTable);
        medicinesScroll.setBorder(BorderFactory.createLineBorder(LINE));
        detailsCard.add(medicinesScroll, BorderLayout.CENTER);
        content.add(detailsCard, BorderLayout.SOUTH);
        return content;
    }

    private JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(12, 14, 12, 14)));
        return card;
    }

    private void taiDanhSach() {
        try {
            List<DonThuocDaLuu> loadedPrescriptions = service.layDanhSachDonThuocDaLuu();
            prescriptions.clear();
            prescriptions.addAll(loadedPrescriptions);
            prescriptionModel.setRowCount(0);
            for (DonThuocDaLuu prescription : prescriptions) {
                prescriptionModel.addRow(new Object[] {
                        prescription.getMaDonThuoc(),
                        prescription.getNgayKe() == null ? "" : NGAY.format(prescription.getNgayKe()),
                        prescription.getMaBenhNhan(),
                        prescription.getTenBenhNhan(),
                        prescription.getMaChanDoan()
                });
            }
            locDanhSach();
            xoaChiTiet();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "Không thể tải danh sách đơn thuốc đã lưu:\n" + exception.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void locDanhSach() {
        String keyword = searchField.getText().trim();
        prescriptionSorter.setRowFilter(keyword.isEmpty()
                ? null
                : RowFilter.regexFilter("(?i)" + Pattern.quote(keyword), 0, 2, 3, 4));
    }

    private void hienThiChiTietDaChon() {
        int selectedRow = prescriptionTable.getSelectedRow();
        if (selectedRow < 0) {
            xoaChiTiet();
            return;
        }
        int modelRow = prescriptionTable.convertRowIndexToModel(selectedRow);
        String prescriptionId = (String) prescriptionModel.getValueAt(modelRow, 0);
        DonThuocDaLuu prescription = timDonThuoc(prescriptionId);
        if (prescription == null) {
            xoaChiTiet();
            return;
        }

        prescriptionInfo.setText(prescriptionId + " · Bệnh nhân: " + prescription.getTenBenhNhan());
        String diagnosis = prescription.getKetLuanChanDoan() == null
                || prescription.getKetLuanChanDoan().trim().isEmpty()
                        ? "Chưa có kết luận chẩn đoán"
                        : prescription.getKetLuanChanDoan();
        diagnosisInfo.setText("Lần khám " + prescription.getMaChanDoan() + ": " + diagnosis);
        String careAdvice = prescription.getLoiDanChamSoc() == null
                || prescription.getLoiDanChamSoc().trim().isEmpty()
                        ? "Không có lời dặn chăm sóc"
                        : prescription.getLoiDanChamSoc();
        careInfo.setText("Lời dặn: " + careAdvice);

        try {
            List<ThuocDonThuocDaLuu> medicines = service.layThuocDaKe(prescriptionId);
            medicineModel.setRowCount(0);
            for (ThuocDonThuocDaLuu medicine : medicines) {
                medicineModel.addRow(new Object[] {
                        medicine.getMaThuoc(),
                        medicine.getTenThuoc(),
                        medicine.getDonViTinh(),
                        medicine.getLieuDung(),
                        medicine.getDuongDung(),
                        medicine.getSoLuong()
                });
            }
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "Không thể tải thuốc trong đơn " + prescriptionId + ":\n" + exception.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            medicineModel.setRowCount(0);
        }
    }

    private DonThuocDaLuu timDonThuoc(String maDonThuoc) {
        for (DonThuocDaLuu prescription : prescriptions) {
            if (prescription.getMaDonThuoc().equals(maDonThuoc)) {
                return prescription;
            }
        }
        return null;
    }

    private void xoaChiTiet() {
        prescriptionInfo.setText("Chọn một đơn thuốc để xem chi tiết.");
        diagnosisInfo.setText(" ");
        careInfo.setText(" ");
        medicineModel.setRowCount(0);
    }

    private static void styleTable(JTable table) {
        table.setFont(new Font("Arial", Font.PLAIN, 11));
        table.setForeground(INK);
        table.setSelectionBackground(new Color(231, 246, 245));
        table.setSelectionForeground(INK);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(238, 242, 244));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        table.getTableHeader().setForeground(INK);
        table.getTableHeader().setBackground(new Color(244, 247, 250));
        table.getTableHeader().setPreferredSize(new Dimension(0, 32));
    }
}

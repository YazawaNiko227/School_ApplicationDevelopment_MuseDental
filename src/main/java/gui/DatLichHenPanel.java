/*
    Người làm phần này: Phan Đức Toàn
    MSSV: 24741631
*/

package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Màn hình đặt lịch và theo dõi nhắc lịch, sử dụng dữ liệu demo trong bộ nhớ.
 */
public final class DatLichHenPanel extends JPanel {
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(102, 119, 130);
    private static final Color ACCENT = new Color(218, 241, 239);
    private static final Color LINE = new Color(226, 234, 237);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final String[] STATUSES = {
            "Chưa xác nhận", "Đã xác nhận", "Đã hủy", "Hoàn thành"
    };

    // Danh sách mẫu trong bộ nhớ; thay bằng truy vấn bảng LichHen khi tích hợp CSDL.
    private final List<Appointment> appointments = createSampleAppointments();
    private final JTextField searchField = new JTextField();
    private final JComboBox<String> statusFilter = new JComboBox<>(
            new String[] { "Tất cả trạng thái", "Chưa xác nhận", "Đã xác nhận", "Đã hủy", "Hoàn thành" });
    private final JLabel countLabel = new JLabel();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[] { "Mã lịch hẹn", "Ngày", "Giờ", "Mã bệnh nhân", "Bệnh nhân",
                    "Nha sĩ", "Phòng", "Trạng thái", "Nhắc lịch" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable appointmentTable = new JTable(tableModel);

    public DatLichHenPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        add(createToolbar(), BorderLayout.NORTH);
        add(createAppointmentList(), BorderLayout.CENTER);
        refreshTable();
    }

    // Thanh tìm kiếm, bộ lọc trạng thái và các thao tác nghiệp vụ lịch hẹn.
    private JPanel createToolbar() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 14));
        wrapper.setOpaque(false);

        JPanel titleBlock = new JPanel(new BorderLayout());
        titleBlock.setOpaque(false);
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 5));
        titles.setOpaque(false);
        JLabel title = new JLabel("Lịch hẹn khám");
        title.setFont(new Font("Arial", Font.BOLD, 21));
        title.setForeground(INK);
        JLabel subtitle = new JLabel("Tạo lịch hẹn, theo dõi xác nhận và ghi nhận nhắc lịch bệnh nhân.");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitle.setForeground(MUTED);
        titles.add(title);
        titles.add(subtitle);

        JButton addButton = createButton("Đặt lịch hẹn", true);
        addButton.addActionListener(event -> openCreateAppointmentDialog());
        titleBlock.add(titles, BorderLayout.WEST);
        titleBlock.add(addButton, BorderLayout.EAST);

        JPanel filterCard = new JPanel(new BorderLayout(10, 0));
        filterCard.setBackground(Color.WHITE);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(12, 14, 12, 14)));

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 0));
        filters.setOpaque(false);
        JLabel searchLabel = new JLabel("Tìm lịch:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 11));
        searchLabel.setForeground(INK);
        searchField.setFont(new Font("Arial", Font.PLAIN, 12));
        searchField.setToolTipText("Tìm theo mã lịch hẹn, mã bệnh nhân, tên bệnh nhân hoặc nha sĩ");
        searchField.setPreferredSize(new Dimension(290, 34));
        searchField.addActionListener(event -> refreshTable());
        statusFilter.setFont(new Font("Arial", Font.PLAIN, 11));
        statusFilter.setPreferredSize(new Dimension(155, 34));
        statusFilter.addActionListener(event -> refreshTable());
        filters.add(searchLabel);
        filters.add(searchField);
        filters.add(statusFilter);

        JButton resetButton = createButton("Xóa lọc", false);
        resetButton.addActionListener(event -> {
            searchField.setText("");
            statusFilter.setSelectedIndex(0);
            refreshTable();
        });
        filterCard.add(filters, BorderLayout.CENTER);
        filterCard.add(resetButton, BorderLayout.EAST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton remindButton = createButton("Đánh dấu đã nhắc", false);
        remindButton.addActionListener(event -> markReminderSent());
        JButton cancelButton = createButton("Hủy lịch hẹn", false);
        cancelButton.addActionListener(event -> cancelSelectedAppointment());
        actions.add(remindButton);
        actions.add(cancelButton);

        JPanel lowerRow = new JPanel(new BorderLayout(0, 10));
        lowerRow.setOpaque(false);
        lowerRow.add(filterCard, BorderLayout.NORTH);
        lowerRow.add(actions, BorderLayout.SOUTH);
        wrapper.add(titleBlock, BorderLayout.NORTH);
        wrapper.add(lowerRow, BorderLayout.CENTER);
        return wrapper;
    }

    // Danh sách lịch hẹn và gợi ý thao tác với lịch đang chọn.
    private JPanel createAppointmentList() {
        JPanel panel = new JPanel(new BorderLayout(0, 11));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(16, 17, 13, 17)));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JLabel title = new JLabel("Danh sách lịch hẹn");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(INK);
        countLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        countLabel.setForeground(MUTED);
        heading.add(title, BorderLayout.WEST);
        heading.add(countLabel, BorderLayout.EAST);

        appointmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appointmentTable.setRowHeight(38);
        appointmentTable.setFont(new Font("Arial", Font.PLAIN, 11));
        appointmentTable.setForeground(INK);
        appointmentTable.setGridColor(new Color(237, 242, 244));
        appointmentTable.setShowVerticalLines(false);
        appointmentTable.setFillsViewportHeight(true);
        appointmentTable.setSelectionBackground(new Color(231, 246, 245));
        appointmentTable.setSelectionForeground(INK);
        JTableHeader tableHeader = appointmentTable.getTableHeader();
        tableHeader.setFont(new Font("Arial", Font.BOLD, 10));
        tableHeader.setForeground(MUTED);
        tableHeader.setBackground(new Color(248, 250, 251));
        tableHeader.setPreferredSize(new Dimension(0, 36));
        int[] widths = { 90, 90, 55, 95, 155, 100, 55, 105, 85 };
        for (int column = 0; column < widths.length; column++) {
            appointmentTable.getColumnModel().getColumn(column).setPreferredWidth(widths[column]);
        }

        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(LINE));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JLabel hint = new JLabel("Chọn một lịch hẹn để đánh dấu đã nhắc hoặc hủy lịch.");
        hint.setFont(new Font("Arial", Font.PLAIN, 10));
        hint.setForeground(MUTED);
        panel.add(heading, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(hint, BorderLayout.SOUTH);
        return panel;
    }

    // Đồng bộ kiểu nút và màu chữ có độ tương phản rõ ràng.
    private JButton createButton(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setForeground(INK);
        button.setBackground(primary ? ACCENT : Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primary ? ACCENT : LINE),
                new EmptyBorder(9, 12, 9, 12)));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    // Mở biểu mẫu nhập các mã liên kết và thời gian theo cấu trúc bảng LichHen.
    private void openCreateAppointmentDialog() {
        JTextField patientCode = new JTextField();
        DatePickerField date = new DatePickerField(LocalDate.now().plusDays(1));
        TimePickerField time = new TimePickerField(9, 0);
        JTextField dentistCode = new JTextField();
        JTextField roomCode = new JTextField();
        JTextField planCode = new JTextField();
        JComboBox<String> status = new JComboBox<>(new String[] { "Chưa xác nhận", "Đã xác nhận" });

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 8));
        form.setPreferredSize(new Dimension(410, 265));
        addFormRow(form, "Mã bệnh nhân *", patientCode);
        addFormRow(form, "Ngày hẹn *", date);
        addFormRow(form, "Giờ hẹn *", time);
        addFormRow(form, "Mã nha sĩ phụ trách *", dentistCode);
        addFormRow(form, "Mã phòng *", roomCode);
        addFormRow(form, "Mã kế hoạch (nếu có)", planCode);
        addFormRow(form, "Trạng thái", status);

        int result = JOptionPane.showConfirmDialog(
                this, form, "Đặt lịch hẹn khám",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            LocalDate appointmentDate = date.getSelectedDate();
            LocalTime appointmentTime = time.getSelectedTime();
            if (!appointmentDate.atTime(appointmentTime).isAfter(LocalDateTime.now())) {
                showError("Thời gian hẹn phải ở tương lai.");
                return;
            }
            String patientId = requiredValue(patientCode, "Mã bệnh nhân");
            String dentistId = requiredValue(dentistCode, "Mã nha sĩ phụ trách");
            String roomId = requiredValue(roomCode, "Mã phòng");
            String planId = planCode.getText().trim();
            String nextId = String.format(Locale.ROOT, "LH%06d", appointments.size() + 1);
            Appointment appointment = new Appointment(
                    nextId, appointmentDate, appointmentTime, patientId, resolvePatientName(patientId),
                    dentistId, roomId, planId, "NV001",
                    (String) status.getSelectedItem(), false);
            appointments.add(appointment);
            refreshTable();
            JOptionPane.showMessageDialog(
                    this,
                    "Đã thêm lịch hẹn " + nextId + ".\n"
                            + "Thông tin hiện được lưu trong bộ nhớ của phiên chạy.",
                    "Đặt lịch thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());
        }
    }

    // Thêm nhãn và ô nhập tương ứng vào biểu mẫu đặt lịch.
    private void addFormRow(JPanel form, String label, Component input) {
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        fieldLabel.setForeground(INK);
        form.add(fieldLabel);
        form.add(input);
    }

    // Kiểm tra các mã bắt buộc trước khi tạo bản ghi lịch hẹn.
    private String requiredValue(JTextField field, String label) {
        String value = field.getText().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập " + label + ".");
        }
        return value;
    }

    // Lọc danh sách theo từ khóa và trạng thái, rồi dựng lại các dòng trong bảng.
    private void refreshTable() {
        String keyword = searchField.getText().trim().toLowerCase(Locale.ROOT);
        String selectedStatus = (String) statusFilter.getSelectedItem();
        tableModel.setRowCount(0);
        int visibleCount = 0;
        for (Appointment appointment : appointments) {
            boolean matchesKeyword = keyword.isEmpty()
                    || appointment.id.toLowerCase(Locale.ROOT).contains(keyword)
                    || appointment.patientId.toLowerCase(Locale.ROOT).contains(keyword)
                    || appointment.patientName.toLowerCase(Locale.ROOT).contains(keyword)
                    || appointment.dentistId.toLowerCase(Locale.ROOT).contains(keyword);
            boolean matchesStatus = "Tất cả trạng thái".equals(selectedStatus)
                    || appointment.status.equals(selectedStatus);
            if (matchesKeyword && matchesStatus) {
                tableModel.addRow(new Object[] {
                        appointment.id,
                        appointment.date.format(DATE_FORMAT),
                        appointment.time.format(TIME_FORMAT),
                        appointment.patientId,
                        appointment.patientName,
                        appointment.dentistId,
                        appointment.roomId,
                        appointment.status,
                        appointment.reminderSent ? "Đã nhắc" : "Chưa nhắc"
                });
                visibleCount++;
            }
        }
        countLabel.setText(visibleCount + " lịch hẹn");
    }

    // Đánh dấu lịch hẹn đang chọn là đã được nhân viên nhắc; không gửi SMS thật.
    private void markReminderSent() {
        Appointment appointment = getSelectedAppointment();
        if (appointment == null) {
            showError("Vui lòng chọn lịch hẹn cần nhắc.");
            return;
        }
        if ("Đã hủy".equals(appointment.status)) {
            showError("Không thể nhắc lịch đã hủy.");
            return;
        }
        appointment.reminderSent = true;
        refreshTable();
        JOptionPane.showMessageDialog(
                this,
                "Đã đánh dấu lịch " + appointment.id + " là đã nhắc.\n"
                        + "Việc gửi SMS hoặc thông báo tự động chưa được tích hợp.",
                "Cập nhật nhắc lịch",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Chuyển trạng thái lịch được chọn sang đã hủy.
    private void cancelSelectedAppointment() {
        Appointment appointment = getSelectedAppointment();
        if (appointment == null) {
            showError("Vui lòng chọn lịch hẹn cần hủy.");
            return;
        }
        if ("Hoàn thành".equals(appointment.status)) {
            showError("Không thể hủy lịch hẹn đã hoàn thành.");
            return;
        }
        appointment.status = "Đã hủy";
        refreshTable();
    }

    // Lấy lịch hẹn theo dòng hiện tại trong bảng.
    private Appointment getSelectedAppointment() {
        int row = appointmentTable.getSelectedRow();
        if (row < 0) {
            return null;
        }
        String appointmentId = tableModel.getValueAt(row, 0).toString();
        for (Appointment appointment : appointments) {
            if (appointment.id.equals(appointmentId)) {
                return appointment;
            }
        }
        return null;
    }

    // Hiển thị thông báo lỗi cho thao tác nhập hoặc xử lý lịch hẹn.
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Không thể thực hiện", JOptionPane.WARNING_MESSAGE);
    }

    // Ô chọn ngày mở lịch tháng; người dùng chọn ngày trực tiếp thay vì nhập chuỗi.
    private final class DatePickerField extends JPanel {
        private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        private LocalDate selectedDate;
        private final JTextField dateDisplay = new JTextField();

        private DatePickerField(LocalDate initialDate) {
            super(new BorderLayout(6, 0));
            selectedDate = initialDate;
            dateDisplay.setEditable(false);
            dateDisplay.setFocusable(false);
            dateDisplay.setFont(new Font("Arial", Font.PLAIN, 12));
            dateDisplay.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(LINE),
                    new EmptyBorder(0, 9, 0, 9)));
            updateDateDisplay();

            JButton chooseButton = createButton("Chọn ngày", false);
            chooseButton.addActionListener(event -> openCalendar());
            add(dateDisplay, BorderLayout.CENTER);
            add(chooseButton, BorderLayout.EAST);
        }

        private LocalDate getSelectedDate() {
            return selectedDate;
        }

        private void updateDateDisplay() {
            dateDisplay.setText(selectedDate.format(DISPLAY_FORMAT));
        }

        // Hiện lịch dạng tháng với các nút ngày và điều hướng tháng trước/sau.
        private void openCalendar() {
            Window owner = javax.swing.SwingUtilities.getWindowAncestor(this);
            JDialog dialog = new JDialog(owner, "Chọn ngày hẹn", JDialog.ModalityType.APPLICATION_MODAL);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setContentPane(new CalendarPopup(dialog));
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        }

        private final class CalendarPopup extends JPanel {
            private final JDialog dialog;
            private final JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
            private final JPanel daysPanel = new JPanel(new GridLayout(6, 7, 4, 4));
            private YearMonth displayedMonth = YearMonth.from(selectedDate);
            private static final String[] WEEKDAYS = { "T2", "T3", "T4", "T5", "T6", "T7", "CN" };

            private CalendarPopup(JDialog dialog) {
                super(new BorderLayout(8, 10));
                this.dialog = dialog;
                setBackground(Color.WHITE);
                setBorder(new EmptyBorder(14, 14, 12, 14));

                JPanel monthNavigation = new JPanel(new BorderLayout(8, 0));
                monthNavigation.setOpaque(false);
                JButton previousMonth = createButton("<", false);
                JButton nextMonth = createButton(">", false);
                monthLabel.setFont(new Font("Arial", Font.BOLD, 14));
                monthLabel.setForeground(INK);
                previousMonth.addActionListener(event -> {
                    displayedMonth = displayedMonth.minusMonths(1);
                    refreshDays();
                });
                nextMonth.addActionListener(event -> {
                    displayedMonth = displayedMonth.plusMonths(1);
                    refreshDays();
                });
                monthNavigation.add(previousMonth, BorderLayout.WEST);
                monthNavigation.add(monthLabel, BorderLayout.CENTER);
                monthNavigation.add(nextMonth, BorderLayout.EAST);

                JPanel calendar = new JPanel(new BorderLayout(0, 7));
                calendar.setOpaque(false);
                JPanel weekdayHeader = new JPanel(new GridLayout(1, 7, 4, 0));
                weekdayHeader.setOpaque(false);
                for (String weekday : WEEKDAYS) {
                    JLabel label = new JLabel(weekday, SwingConstants.CENTER);
                    label.setFont(new Font("Arial", Font.BOLD, 10));
                    label.setForeground(MUTED);
                    weekdayHeader.add(label);
                }
                daysPanel.setOpaque(false);
                calendar.add(weekdayHeader, BorderLayout.NORTH);
                calendar.add(daysPanel, BorderLayout.CENTER);

                JLabel instruction = new JLabel("Chọn một ngày từ hôm nay trở đi.");
                instruction.setFont(new Font("Arial", Font.PLAIN, 10));
                instruction.setForeground(MUTED);
                add(monthNavigation, BorderLayout.NORTH);
                add(calendar, BorderLayout.CENTER);
                add(instruction, BorderLayout.SOUTH);
                refreshDays();
            }

            // Vẽ lại lưới ngày khi đổi tháng hoặc đổi ngày đã chọn.
            private void refreshDays() {
                monthLabel.setText(displayedMonth.format(
                        DateTimeFormatter.ofPattern("'Tháng' MM yyyy", Locale.forLanguageTag("vi"))));
                daysPanel.removeAll();
                LocalDate firstDay = displayedMonth.atDay(1);
                int offset = firstDay.getDayOfWeek().getValue() - 1;
                int daysInMonth = displayedMonth.lengthOfMonth();
                for (int cell = 0; cell < 42; cell++) {
                    int dayOfMonth = cell - offset + 1;
                    if (dayOfMonth < 1 || dayOfMonth > daysInMonth) {
                        daysPanel.add(new JLabel(""));
                        continue;
                    }
                    LocalDate date = displayedMonth.atDay(dayOfMonth);
                    JButton dayButton = new JButton(Integer.toString(dayOfMonth));
                    dayButton.setFont(new Font("Arial", Font.PLAIN, 11));
                    dayButton.setForeground(INK);
                    dayButton.setBackground(date.equals(selectedDate) ? ACCENT : Color.WHITE);
                    dayButton.setBorder(BorderFactory.createLineBorder(LINE));
                    dayButton.setFocusPainted(false);
                    dayButton.setEnabled(!date.isBefore(LocalDate.now()));
                    dayButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    dayButton.addActionListener(event -> {
                        selectedDate = date;
                        updateDateDisplay();
                        dialog.dispose();
                    });
                    daysPanel.add(dayButton);
                }
                daysPanel.revalidate();
                daysPanel.repaint();
            }
        }
    }

    // Ô chọn giờ mở bộ chọn giờ 24h dạng đồng hồ báo thức với giờ và phút riêng biệt.
    private final class TimePickerField extends JPanel {
        private LocalTime selectedTime;
        private final JTextField timeDisplay = new JTextField();

        private TimePickerField(int initialHour, int initialMinute) {
            super(new BorderLayout(6, 0));
            selectedTime = LocalTime.of(initialHour, initialMinute);
            timeDisplay.setEditable(false);
            timeDisplay.setFocusable(false);
            timeDisplay.setFont(new Font("Arial", Font.PLAIN, 12));
            timeDisplay.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(LINE),
                    new EmptyBorder(0, 9, 0, 9)));
            updateTimeDisplay();

            JButton chooseButton = createButton("Chọn giờ", false);
            chooseButton.addActionListener(event -> openTimePicker());
            add(timeDisplay, BorderLayout.CENTER);
            add(chooseButton, BorderLayout.EAST);
        }

        private LocalTime getSelectedTime() {
            return selectedTime;
        }

        private void updateTimeDisplay() {
            timeDisplay.setText(selectedTime.format(TIME_FORMAT));
        }

        // Cho phép chọn giờ và phút riêng, có đồng hồ lớn xem trước như báo thức.
        private void openTimePicker() {
            Window owner = javax.swing.SwingUtilities.getWindowAncestor(this);
            JDialog dialog = new JDialog(owner, "Chọn giờ hẹn", JDialog.ModalityType.APPLICATION_MODAL);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            JSpinner hourSpinner = new JSpinner(
                    new javax.swing.SpinnerNumberModel(selectedTime.getHour(), 0, 23, 1));
            JSpinner minuteSpinner = new JSpinner(
                    new javax.swing.SpinnerNumberModel(selectedTime.getMinute(), 0, 59, 1));
            hourSpinner.setEditor(new JSpinner.NumberEditor(hourSpinner, "00"));
            minuteSpinner.setEditor(new JSpinner.NumberEditor(minuteSpinner, "00"));
            hourSpinner.setPreferredSize(new Dimension(82, 48));
            minuteSpinner.setPreferredSize(new Dimension(82, 48));

            JLabel digitalClock = new JLabel("", SwingConstants.CENTER);
            digitalClock.setFont(new Font("Arial", Font.BOLD, 42));
            digitalClock.setForeground(INK);
            Runnable updateClock = () -> digitalClock.setText(String.format(
                    Locale.ROOT, "%02d:%02d",
                    ((Number) hourSpinner.getValue()).intValue(),
                    ((Number) minuteSpinner.getValue()).intValue()));
            ChangeListener clockUpdater = event -> updateClock.run();
            hourSpinner.addChangeListener(clockUpdater);
            minuteSpinner.addChangeListener(clockUpdater);
            updateClock.run();

            JPanel timeSelectors = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
            timeSelectors.setOpaque(false);
            timeSelectors.add(createSpinnerGroup("GIỜ", hourSpinner));
            JLabel separator = new JLabel(":", SwingConstants.CENTER);
            separator.setFont(new Font("Arial", Font.BOLD, 23));
            separator.setForeground(MUTED);
            timeSelectors.add(separator);
            timeSelectors.add(createSpinnerGroup("PHÚT", minuteSpinner));

            JPanel content = new JPanel(new BorderLayout(0, 14));
            content.setBackground(Color.WHITE);
            content.setBorder(new EmptyBorder(18, 22, 14, 22));
            JLabel instruction = new JLabel("Chọn thời gian theo định dạng 24 giờ", SwingConstants.CENTER);
            instruction.setFont(new Font("Arial", Font.PLAIN, 11));
            instruction.setForeground(MUTED);
            content.add(instruction, BorderLayout.NORTH);
            content.add(digitalClock, BorderLayout.CENTER);
            content.add(timeSelectors, BorderLayout.SOUTH);

            JButton confirmButton = createButton("Chọn giờ này", true);
            confirmButton.addActionListener(event -> {
                selectedTime = LocalTime.of(
                        ((Number) hourSpinner.getValue()).intValue(),
                        ((Number) minuteSpinner.getValue()).intValue());
                updateTimeDisplay();
                dialog.dispose();
            });
            JButton cancelButton = createButton("Hủy", false);
            cancelButton.addActionListener(event -> dialog.dispose());
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
            actions.setBackground(Color.WHITE);
            actions.add(cancelButton);
            actions.add(confirmButton);

            JPanel dialogContent = new JPanel(new BorderLayout());
            dialogContent.add(content, BorderLayout.CENTER);
            dialogContent.add(actions, BorderLayout.SOUTH);
            dialog.setContentPane(dialogContent);
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        }

        // Nhóm nhãn và bộ chọn số có nút tăng/giảm như đồng hồ báo thức.
        private JPanel createSpinnerGroup(String labelText, JSpinner spinner) {
            JPanel group = new JPanel(new BorderLayout(0, 4));
            group.setOpaque(false);
            JLabel label = new JLabel(labelText, SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 9));
            label.setForeground(MUTED);
            spinner.setFont(new Font("Arial", Font.BOLD, 18));
            group.add(label, BorderLayout.NORTH);
            group.add(spinner, BorderLayout.CENTER);
            return group;
        }
    }

    // Tra tên từ bộ dữ liệu minh họa; bệnh nhân khác cần được tra từ CSDL khi kết nối.
    private String resolvePatientName(String patientId) {
        for (Appointment appointment : appointments) {
            if (appointment.patientId.equalsIgnoreCase(patientId)) {
                return appointment.patientName;
            }
        }
        return "Chưa có tên liên kết";
    }

    // Tạo lịch mẫu tương đối với ngày chạy ứng dụng để tiện thử các thao tác.
    private List<Appointment> createSampleAppointments() {
        LocalDate today = LocalDate.now();
        List<Appointment> sample = new ArrayList<>();
        sample.add(new Appointment("LH000001", today, LocalTime.of(9, 0), "BN000001",
                "Nguyễn Thị Minh Anh", "NV002", "P01", "", "NV001", "Chưa xác nhận", false));
        sample.add(new Appointment("LH000002", today, LocalTime.of(9, 30), "BN000002",
                "Trần Hoàng Nam", "NV003", "P02", "", "NV001", "Đã xác nhận", false));
        sample.add(new Appointment("LH000003", today.plusDays(1), LocalTime.of(10, 0), "BN000003",
                "Lê Ngọc Hà", "NV002", "P01", "KH001", "NV001", "Đã xác nhận", true));
        sample.add(new Appointment("LH000004", today.plusDays(1), LocalTime.of(10, 30), "BN000004",
                "Phạm Gia Bảo", "NV004", "P03", "", "NV001", "Chưa xác nhận", false));
        return sample;
    }

    // Đại diện một lịch hẹn, với các khóa liên kết tương ứng sơ đồ LichHen.
    private static final class Appointment {
        private final String id;
        private final LocalDate date;
        private final LocalTime time;
        private final String patientId;
        private final String patientName;
        private final String dentistId;
        private final String roomId;
        private final String planId;
        private final String createdBy;
        private String status;
        private boolean reminderSent;

        private Appointment(
                String id, LocalDate date, LocalTime time, String patientId, String patientName,
                String dentistId, String roomId, String planId, String createdBy,
                String status, boolean reminderSent) {
            this.id = id;
            this.date = date;
            this.time = time;
            this.patientId = patientId;
            this.patientName = patientName;
            this.dentistId = dentistId;
            this.roomId = roomId;
            this.planId = planId;
            this.createdBy = createdBy;
            this.status = status;
            this.reminderSent = reminderSent;
        }
    }
}

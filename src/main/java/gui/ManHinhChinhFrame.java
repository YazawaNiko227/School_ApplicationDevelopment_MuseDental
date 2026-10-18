/*
    Người làm phần này: Phan Đức Toàn
    MSSV: 24741631
*/

package gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

/**
 * ManHinhChinhFrame là màn hình chính của hệ thống quản lý phòng khám Muse Dental.
 *
 * Giao diện này chứa ba phần cốt lõi: thanh điều hướng bên trái để chọn chức
 * năng,
 * header ở trên cùng để hiển thị tiêu đề và tìm kiếm nhanh, và khu vực nội dung
 * ở giữa
 * để render các màn hình con như dashboard, tra cứu bệnh nhân hoặc các module
 * nghiệp vụ.
 *
 * Mỗi mục trong menu đều gọi đến một phương thức khác nhau để thay đổi nội dung
 * của
 * `contentPanel`, nên người dùng có cảm giác di chuyển giữa các màn hình mà
 * không cần
 * khởi động lại ứng dụng hay mở thêm cửa sổ mới.
 */
public final class ManHinhChinhFrame extends JFrame {
    // Màu giao diện dùng chung cho thanh điều hướng và vùng nội dung.
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color CANVAS = new Color(244, 248, 249);
    private static final Color LINE = new Color(226, 234, 237);

    // Tên hiển thị trong thanh đầu trang và lời chào dashboard.
    private final String username;
    // Các thành phần này được giữ lại để cập nhật trang khi người dùng chọn menu.
    private final JPanel contentPanel = new JPanel(new BorderLayout());
    private final JLabel pageTitle = new JLabel("Tổng quan");
    private final JLabel pageDescription = new JLabel("Dữ liệu minh họa · Tình hình hoạt động phòng khám hôm nay");
    private JButton selectedNavigationButton;
    private JButton expandedSectionButton;
    private JPanel expandedSectionItems;

    public ManHinhChinhFrame(String username) {
        super("Nha khoa Muse | Hệ thống quản lý phòng khám");
        this.username = username == null || username.trim().isEmpty() ? "Nhân viên" : username.trim();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1080, 700));
        setSize(1360, 850);
        setLocationRelativeTo(null);
        setContentPane(createLayout());
        showDashboard();
    }

    /**
     * Tạo bố cục tổng thể của ứng dụng: bên trái là menu chức năng, bên phải là
     * vùng làm việc chính.
     * Phương thức này đóng vai trò như "container" gốc, giúp toàn bộ giao diện có
     * cấu trúc rõ ràng
     * và dễ mở rộng khi thêm chức năng mới vào hệ thống quản lý phòng khám.
     */
    private JPanel createLayout() {
        // Khung ứng dụng gồm thanh menu cố định và vùng làm việc thay đổi theo chức
        // năng.
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CANVAS);
        root.add(createSidebar(), BorderLayout.WEST);
        root.add(createWorkspace(), BorderLayout.CENTER);
        return root;
    }

    /**
     * Xây dựng sidebar chứa logo, danh mục chức năng theo nhóm và thông tin tài
     * khoản.
     * Đây là phần tương tác quan trọng nhất của giao diện vì người dùng sẽ chọn các
     * module
     * nghiệp vụ từ đây, ví dụ: Tổng quan, Cập nhật hồ sơ bệnh nhân, Tìm kiếm, Báo
     * cáo, v.v.
     */
    private JPanel createSidebar() {
        // Tạo logo, các nhóm menu có thể thu gọn và vùng đăng xuất.
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(264, 0));
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, LINE));

        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setBackground(Color.WHITE);
        top.setBorder(new EmptyBorder(24, 20, 20, 18));
        top.add(createLogo(), BorderLayout.WEST);

        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        JLabel brandName = new JLabel("Nha khoa Muse");
        brandName.setFont(new Font("Arial", Font.BOLD, 16));
        brandName.setForeground(INK);
        JLabel brandSubtitle = new JLabel("Phần mềm quản lí nha khoa");
        brandSubtitle.setFont(new Font("Arial", Font.BOLD, 8));
        brandSubtitle.setForeground(MUTED);
        brand.add(brandName);
        brand.add(Box.createVerticalStrut(4));
        brand.add(brandSubtitle);
        top.add(brand, BorderLayout.CENTER);
        sidebar.add(top, BorderLayout.NORTH);

        JPanel navigation = new JPanel();
        navigation.setBackground(Color.WHITE);
        navigation.setBorder(new EmptyBorder(6, 12, 18, 12));
        navigation.setLayout(new BoxLayout(navigation, BoxLayout.Y_AXIS));
        navigation.add(createSectionLabel("TỔNG QUAN"));
        addNavigationItem(navigation, "Tổng quan", event -> showDashboard(), true);

        addCollapsibleSection(navigation, "HỆ THỐNG", new String[] {
                "Đổi mật khẩu",
                "Phân quyền hệ thống RBAC"
        });
        addCollapsibleSection(navigation, "DANH MỤC", new String[] {
                "Hồ sơ nhân viên",
                "Hồ sơ bệnh nhân",
                "Thuốc và vật tư y tế",
                "Danh mục dịch vụ và bảng giá"
        });
        addCollapsibleSection(navigation, "CẬP NHẬT", new String[] {
                "Tiếp đón và check-in",
                "Hồ sơ bệnh án điện tử EMR",
                "Kê đơn thuốc điện tử",
                "Phác đồ điều trị và dự toán chi phí",
                "Ghi nhận vật tư tiêu hao",
                "Công nợ và trả góp theo đợt"
        });
        addCollapsibleSection(navigation, "TÌM KIẾM", new String[] {
                "Tìm hồ sơ bệnh nhân",
                "Tìm hồ sơ nhân viên",
                "Tìm hồ sơ bệnh án EMR",
                "Tra cứu hóa đơn / giao dịch",
                "Tra cứu tồn kho thuốc / dụng cụ"
        });
        addCollapsibleSection(navigation, "XỬ LÝ", new String[] {
                "Dashboard điều phối phòng khám",
                "Lịch làm việc cá nhân và báo bận",
                "Dashboard phân công ca trực Y tá",
                "Chuẩn bị dụng cụ và cập nhật trạng thái phòng",
                "Phân công lịch làm việc và ca trực",
                "Lập hóa đơn",
                "Khám lâm sàng và sơ đồ răng",
                "Đặt lịch hẹn khám và nhắc lịch"
        });
        addCollapsibleSection(navigation, "THỐNG KÊ", new String[] {
                "Báo cáo doanh thu",
                "Báo cáo hiệu suất nhân sự",
                "Thống kê lượt khám và lịch hẹn",
                "Báo cáo xuất / nhập / tồn kho",
                "Báo cáo thống kê và KPI nha sĩ"
        });
        navigation.add(Box.createVerticalGlue());

        JScrollPane navigationScroll = new JScrollPane(navigation);
        navigationScroll.setBorder(null);
        navigationScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        navigationScroll.getVerticalScrollBar().setUnitIncrement(16);
        sidebar.add(navigationScroll, BorderLayout.CENTER);

        sidebar.add(createAccountFooter(), BorderLayout.SOUTH);
        return sidebar;
    }

    /**
     * Tạo footer của sidebar để hiển thị tài khoản đang đăng nhập và nút đăng xuất.
     * Phần này cung cấp thông tin nhận diện người dùng và cho phép người dùng thoát
     * khỏi phiên
     * làm việc hiện tại, sau đó quay lại màn hình đăng nhập an toàn hơn.
     */
    private JPanel createAccountFooter() {
        JPanel footer = new JPanel(new BorderLayout(0, 4));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, LINE),
                new EmptyBorder(12, 12, 12, 12)));

        JPanel accountCard = new JPanel(new BorderLayout(10, 0));
        accountCard.setBackground(new Color(244, 247, 250));
        accountCard.setBorder(new EmptyBorder(10, 10, 10, 8));
        accountCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        accountCard.add(new AccountAvatar(username), BorderLayout.WEST);

        JPanel accountDetails = new JPanel();
        accountDetails.setOpaque(false);
        accountDetails.setLayout(new BoxLayout(accountDetails, BoxLayout.Y_AXIS));
        JLabel accountName = new JLabel(username);
        accountName.setFont(new Font("Arial", Font.BOLD, 11));
        accountName.setForeground(INK);
        accountName.setAlignmentX(LEFT_ALIGNMENT);
        JLabel accountRole = new JLabel("Nhân viên phòng khám");
        accountRole.setFont(new Font("Arial", Font.PLAIN, 9));
        accountRole.setForeground(MUTED);
        accountRole.setAlignmentX(LEFT_ALIGNMENT);
        accountDetails.add(accountName);
        accountDetails.add(Box.createVerticalStrut(3));
        accountDetails.add(accountRole);
        accountCard.add(accountDetails, BorderLayout.CENTER);
        footer.add(accountCard, BorderLayout.NORTH);

        JButton logoutButton = new JButton("Đăng xuất", new LogoutIcon());
        logoutButton.setHorizontalAlignment(SwingConstants.LEFT);
        logoutButton.setIconTextGap(9);
        logoutButton.setFont(new Font("Arial", Font.PLAIN, 11));
        logoutButton.setForeground(MUTED);
        logoutButton.setBackground(new Color(244, 247, 250));
        logoutButton.setBorder(new EmptyBorder(10, 9, 5, 5));
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setContentAreaFilled(true);
        logoutButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutButton.addActionListener(event -> logout());
        // BorderLayout.CENTER kéo nút phủ hết chiều ngang bên trong footer.
        footer.add(logoutButton, BorderLayout.CENTER);
        return footer;
    }

    /**
     * Thêm một nhóm menu dạng accordion cho từng mục nghiệp vụ.
     * Khi nhấn vào tiêu đề nhóm, các chức năng con sẽ hiện hoặc ẩn ra, đồng thời
     * chỉ một nhóm
     * được mở cùng lúc để thanh điều hướng không bị quá dày và người dùng dễ theo
     * dõi.
     */
    private void addCollapsibleSection(JPanel navigation, String title, String[] items) {
        JButton sectionButton = new JButton(title, new SectionToggleIcon(false));
        sectionButton.setHorizontalAlignment(SwingConstants.LEFT);
        sectionButton.setIconTextGap(10);
        sectionButton.setFont(new Font("Arial", Font.BOLD, 11));
        sectionButton.setForeground(MUTED);
        sectionButton.setBackground(Color.WHITE);
        sectionButton.setBorder(new EmptyBorder(11, 13, 11, 10));
        sectionButton.setFocusPainted(false);
        sectionButton.setBorderPainted(false);
        sectionButton.setContentAreaFilled(false);
        sectionButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sectionButton.setAlignmentX(LEFT_ALIGNMENT);
        sectionButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JPanel itemPanel = new JPanel();
        itemPanel.setBackground(Color.WHITE);
        itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
        itemPanel.setVisible(false);

        for (String item : items) {
            JButton itemButton = createNavigationButton(item);
            itemButton.setBorder(new EmptyBorder(9, 22, 9, 8));
            itemButton.setAlignmentX(LEFT_ALIGNMENT);
            itemButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            itemButton.addActionListener(event -> {
                setSelectedNavigationButton(itemButton);
                if ("Tìm hồ sơ bệnh nhân".equals(item)) {
                    showPatientSearch();
                    /* trinhKiet */
                } else if ("Tìm hồ sơ nhân viên".equals(item)) {
                    showEmployeeSearch();
                    /* end trinhKiet */
                } else if ("Hồ sơ bệnh nhân".equals(item)) {
                    showPatientSearch();
                } else if ("Hồ sơ nhân viên".equals(item)) {
                    showEmployeeSearch();
                } else if ("Hồ sơ bệnh án điện tử EMR".equals(item)) {
                    showEMRSearch();
                } else if ("Đặt lịch hẹn khám và nhắc lịch".equals(item)) {
                    showAppointmentManagement();
                } else if ("Tìm hồ sơ bệnh án EMR".equals(item)) {
                    showEMRSearch();
                } else if ("Chuẩn bị dụng cụ và cập nhật trạng thái phòng".equals(item)) {
                    showRoomPreparationPanel();
                } else if ("Tiếp đón và check-in".equals(item)
                        || "Dashboard điều phối phòng khám".equals(item)) {
                    showClinicDashboard(item);
                } else if ("Dashboard phân công ca trực Y tá".equals(item)
                        || "Lịch làm việc cá nhân và báo bận".equals(item)
                        || "Phân công lịch làm việc và ca trực".equals(item)) {
                    showNurseShiftDashboard(item);
                } else if ("Thuốc và vật tư y tế".equals(item)
                        || "Tra cứu tồn kho thuốc / dụng cụ".equals(item)) {
                    showMedicineInventory(item);
                } else if ("Tra cứu hóa đơn / giao dịch".equals(item)) {
                    showInvoiceLookup();
                } else {
                    showModule(item, title);
                }
            });
            itemPanel.add(itemButton);
        }

        sectionButton.addActionListener(event -> {
            boolean shouldExpand = !itemPanel.isVisible();
            if (expandedSectionItems != null && expandedSectionItems != itemPanel) {
                expandedSectionItems.setVisible(false);
                expandedSectionButton.setIcon(new SectionToggleIcon(false));
            }

            itemPanel.setVisible(shouldExpand);
            sectionButton.setIcon(new SectionToggleIcon(shouldExpand));
            expandedSectionItems = shouldExpand ? itemPanel : null;
            expandedSectionButton = shouldExpand ? sectionButton : null;
            navigation.revalidate();
            navigation.repaint();
        });

        navigation.add(sectionButton);
        navigation.add(itemPanel);
    }

    /**
     * Đăng xuất khỏi phiên hiện tại và trả người dùng về màn hình đăng nhập.
     * Đây là hành động rời khỏi trạng thái làm việc của nhân viên và cần đóng cửa
     * sổ dashboard
     * hiện tại để tránh giữ state cũ trong bộ nhớ.
     */
    private void logout() {
        dispose();
        new DangNhapFrame().setVisible(true);
    }

    // Đọc logo từ resources để hiển thị nhận diện Muse ở đầu thanh điều hướng.
    private JLabel createLogo() {
        URL logoResource = getClass().getResource("/images/Logo.png");
        if (logoResource == null) {
            throw new IllegalStateException("Không tìm thấy logo tại resources/images/Logo.png.");
        }
        ImageIcon icon = new ImageIcon(logoResource);
        JLabel logo = new JLabel(new ImageIcon(icon.getImage().getScaledInstance(42, 42, java.awt.Image.SCALE_SMOOTH)));
        logo.setPreferredSize(new Dimension(42, 42));
        return logo;

    }

    /**
     * Tạo vùng nội dung chính của ứng dụng, nơi header và page content được đặt
     * cạnh nhau.
     * Vùng này không chứa menu, nên mọi thay đổi của màn hình khi chọn mục trong
     * sidebar
     * chỉ ảnh hưởng đến `contentPanel` mà không làm xô lệch cấu trúc tổng thể của
     * frame.
     */
    private JPanel createWorkspace() {
        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setBackground(CANVAS);
        workspace.add(createTopBar(), BorderLayout.NORTH);

        contentPanel.setBackground(CANVAS);
        contentPanel.setBorder(new EmptyBorder(28, 34, 34, 34));
        workspace.add(contentPanel, BorderLayout.CENTER);
        return workspace;

    }

    /**
     * Tạo top bar phía trên nội dung chính. Đây là khu vực hiển thị tiêu đề trang,
     * mô tả ngắn
     * và ô tìm kiếm nhanh giúp người dùng tra cứu trong hàng loạt chức năng mà
     * không cần rời khỏi
     * màn hình hiện tại.
     */
    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, LINE),
                new EmptyBorder(15, 30, 15, 30)));

        JPanel pageHeading = new JPanel();
        pageHeading.setOpaque(false);
        pageHeading.setLayout(new BoxLayout(pageHeading, BoxLayout.Y_AXIS));
        pageTitle.setFont(new Font("Arial", Font.BOLD, 19));
        pageTitle.setForeground(INK);
        pageDescription.setFont(new Font("Arial", Font.PLAIN, 11));
        pageDescription.setForeground(MUTED);
        pageHeading.add(pageTitle);
        pageHeading.add(Box.createVerticalStrut(4));
        pageHeading.add(pageDescription);
        topBar.add(pageHeading, BorderLayout.WEST);

        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        JTextField search = new JTextField();
        search.setFont(new Font("Arial", Font.PLAIN, 12));
        search.setToolTipText("Tìm nhanh bệnh nhân, lịch hẹn...");
        search.setPreferredSize(new Dimension(230, 36));
        search.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(0, 11, 0, 11)));
        right.add(search, BorderLayout.EAST);
        topBar.add(right, BorderLayout.EAST);
        return topBar;
    }

    // Thêm tiêu đề nhóm, ví dụ HỆ THỐNG hoặc CẬP NHẬT, vào thanh điều hướng.
    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 9));
        label.setForeground(new Color(151, 169, 177));
        label.setBorder(new EmptyBorder(19, 12, 8, 0));
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    // Tạo nút điều hướng độc lập, dùng cho trang tổng quan.
    private void addNavigationItem(
            JPanel navigation,
            String title,
            java.util.function.Consumer<ActionEvent> action,
            boolean selected) {
        JButton button = createNavigationButton(title);
        button.setAlignmentX(LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        button.addActionListener(event -> {
            setSelectedNavigationButton(button);
            action.accept(event);
        });
        navigation.add(button);
        if (selected) {
            setSelectedNavigationButton(button);
        }
    }
    //

    // Dùng chung kiểu nút cho menu chính và các mục con để giữ giao diện đồng nhất.
    private JButton createNavigationButton(String title) {
        // Dùng icon tự vẽ với chữ viết tắt, tránh phụ thuộc ký tự biểu tượng của hệ
        // điều hành.
        JButton button = new JButton(title, new MenuBadgeIcon(title));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(10);
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setForeground(MUTED);
        button.setBackground(Color.WHITE);
        button.setBorder(new EmptyBorder(11, 13, 11, 10));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    // Đánh dấu mục vừa chọn để người dùng nhận biết vị trí hiện tại.
    private void setSelectedNavigationButton(JButton button) {
        if (selectedNavigationButton != null) {
            selectedNavigationButton.setForeground(MUTED);
            selectedNavigationButton.setBackground(Color.WHITE);
            selectedNavigationButton.setContentAreaFilled(false);
        }
        selectedNavigationButton = button;
        selectedNavigationButton.setForeground(TEAL);
        selectedNavigationButton.setBackground(new Color(231, 246, 245));
        selectedNavigationButton.setContentAreaFilled(true);
    }

    /**
     * Hiển thị màn hình tổng quan của phòng khám với các chỉ số hoạt động chính.
     * Dữ liệu đang dùng ở đây là mock data để minh họa UI; khi tích hợp backend,
     * phần
     * // này sẽ
     * được thay bằng dữ liệu thật từ cơ sở dữ liệu hoặc API của hệ thống.
     */
    private void showDashboard() {
        // Hiển thị trang tổng quan; số liệu hiện tại là dữ liệu minh họa, chưa lấy từ
        // CSDL.
        pageTitle.setText("Tổng quan");
        pageDescription.setText("Dữ liệu minh họa · Tình hình hoạt động phòng khám hôm nay");
        contentPanel.removeAll();
        contentPanel.add(createDashboardContent(), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * Chuyển sang màn hình tìm kiếm hồ sơ bệnh nhân.
     * Khi người dùng chọn module "Tìm hồ sơ bệnh nhân" từ sidebar, frame sẽ thay
     * đổi tiêu đề
     * và render `TimKiemBenhNhanPanel` vào `contentPanel` để thao tác tìm kiếm trực
     * tiếp.
     */
    private void showPatientSearch() {
        showContentPanel(
                "Tìm hồ sơ bệnh nhân",
                "Tra cứu theo mã bệnh nhân, họ tên, số điện thoại hoặc CCCD",
                new TimKiemBenhNhanPanel());
    }

    // Mở màn hình tạo và theo dõi lịch hẹn khám.
    private void showAppointmentManagement() {
        showContentPanel(
                "Đặt lịch hẹn khám và nhắc lịch",
                "Quản lý lịch hẹn, trạng thái xác nhận và nhắc lịch bệnh nhân",
                new DatLichHenPanel());
    }

    /* trinhKiet */
    private void showEmployeeSearch() {
        showContentPanel(
                "Tìm hồ sơ nhân viên",
                "Tra cứu theo mã nhân viên, họ tên, chức vụ hoặc số điện thoại",
                new TimKiemNhanVienPanel());
    }

    // Mở màn hình tra cứu bệnh án điện tử EMR trong vùng nội dung chính.
    private void showEMRSearch() {
        showContentPanel(
                "Tra cứu hồ sơ bệnh án",
                "Tìm kiếm lần khám, chẩn đoán và nội dung bệnh án điện tử",
                new TraCuuBenhAnPanel());
    }

    // Hiển thị bảng theo dõi tiếp đón, hàng chờ và trạng thái các phòng khám.
    private void showClinicDashboard(String menuTitle) {
        showContentPanel(
                menuTitle,
                "Theo dõi tiếp đón, hàng chờ bệnh nhân và tình trạng phòng",
                new DieuPhoiPhongKhamPanel());
    }

    // Mở bảng ca trực và lịch làm việc nhân viên y tế.
    private void showNurseShiftDashboard(String menuTitle) {
        showContentPanel(
                menuTitle,
                "Theo dõi, lọc và phân công ca trực nhân viên",
                new BangPhanCongCaTrucYTaPanel());
    }

    // Mở quản lý tồn kho và phiếu nhập thuốc, vật tư y tế.
    private void showMedicineInventory(String menuTitle) {
        showContentPanel(
                menuTitle,
                "Tra cứu tồn kho và lập phiếu nhập thuốc, vật tư",
                new QuanLyKhoThuocPanel());
    }

    // Màn hình hóa đơn hiện được thiết kế thành cửa sổ riêng, nên mở JFrame độc lập.
    private void showInvoiceLookup() {
        new TraCuuHoaDonFrame().setVisible(true);
    }

    // Thay nội dung trung tâm bằng một panel nghiệp vụ và đồng bộ tiêu đề trang.
    private void showContentPanel(String title, String description, JPanel panel) {
        pageTitle.setText(title);
        pageDescription.setText(description);
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }


    private void showRoomPreparationPanel() {
        showContentPanel(
                "Chuẩn bị dụng cụ và cập nhật trạng thái phòng",
                "Chuẩn bị dụng cụ và cập nhật trạng thái sử dụng của phòng",
                new ChuanBiPhongKhamPanel());
    }

    /**
     * Kết hợp các thành phần chính của dashboard thành một bố cục thống nhất.
     * Nội dung gồm lời chào, các thẻ thống kê tổng quan và bảng lịch hẹn sắp tới
     * giúp nhân viên
     * nắm nhanh tình hình hoạt động trong ngày mà không cần đọc từng chi tiết riêng
     * lẻ.
     */
    private JPanel createDashboardContent() {
        JPanel dashboard = new JPanel(new BorderLayout(0, 22));
        dashboard.setOpaque(false);
        dashboard.add(createGreeting(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 22));
        center.setOpaque(false);
        center.add(createSummaryCards(), BorderLayout.NORTH);
        center.add(createActivityPanel(), BorderLayout.CENTER);
        dashboard.add(center, BorderLayout.CENTER);
        return dashboard;
    }

    /**
     * Thêm phần lời chào trên đầu dashboard dựa trên tên người dùng đang đăng nhập.
     * Thông tin ngày tháng cũng được hiển thị để tạo cảm giác thân thiện và làm rõ
     * thời điểm
     * dữ liệu trong màn hình hiện tại đang tương ứng với ngày nào.
     */
    private JPanel createGreeting() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel greeting = new JLabel("Xin chào, " + username);
        greeting.setFont(new Font("Arial", Font.BOLD, 23));
        greeting.setForeground(INK);
        JLabel description = new JLabel("Chúc bạn một ngày làm việc hiệu quả tại nha khoa Muse.");
        description.setFont(new Font("Arial", Font.PLAIN, 12));
        description.setForeground(MUTED);

        text.add(description);
        panel.add(text, BorderLayout.WEST);

        String date = LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", Locale.forLanguageTag("vi-VN")));
        JLabel dateLabel = new JLabel(date);
        dateLabel.setFont(new Font("Arial", Font.BOLD, 11));
        dateLabel.setForeground(MUTED);
        panel.add(dateLabel, BorderLayout.EAST);
        return panel;

    }

    /**
     * Tạo bộ bốn thẻ thống kê nhanh trên dashboard: lịch hẹn, bệnh nhân mới, chờ
     * tiếp đón, doanh thu.
     * Mỗi thẻ tập trung vào một chỉ số cốt lõi của phòng khám, giúp người quản lý
     * nhìn tổng quan
     * tình trạng hoạt động trong ngày ngay tại trang đầu tiên.
     */
    private JPanel createSummaryCards() {
        // Các thẻ tóm tắt những chỉ số vận hành chính theo menu dự án.
        JPanel cards = new JPanel(new GridLayout(1, 4, 14, 0));
        cards.setOpaque(false);

        cards.add(createSummaryCard("LỊCH HẸN HÔM NAY", "24", "+ 4 so với hôm qua", "LH", new Color(231, 246, 245)));
        cards.add(createSummaryCard("BỆNH NHÂN MỚI", "08", "Trong ngày hôm nay", "BN", new Color(237, 242, 255)));
        cards.add(createSummaryCard("CHỜ TIẾP ĐÓN", "05", "Cần được hỗ trợ", "TD", new Color(255, 246, 232)));
        cards.add(createSummaryCard("DOANH THU HÔM NAY", "12,8 tr", "Tạm tính trong ngày", "DT",
                new Color(253, 237, 244)));
        return cards;
    }

    // Dựng một thẻ thống kê gồm nhãn, giá trị, ghi chú và biểu tượng.
    private JPanel createSummaryCard(String title, String value, String note, String icon, Color iconBackground) {
        JPanel card = new JPanel(new BorderLayout(0, 17));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(18, 18, 17, 18)));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 9));
        titleLabel.setForeground(MUTED);
        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Arial", Font.BOLD, 15));
        iconLabel.setForeground(TEAL);
        iconLabel.setOpaque(true);
        iconLabel.setBackground(iconBackground);
        iconLabel.setPreferredSize(new Dimension(34, 34));
        top.add(titleLabel, BorderLayout.WEST);
        top.add(iconLabel, BorderLayout.EAST);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 25));
        valueLabel.setForeground(INK);
        JLabel noteLabel = new JLabel(note);
        noteLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        noteLabel.setForeground(MUTED);
        bottom.add(valueLabel);
        bottom.add(Box.createVerticalStrut(5));
        bottom.add(noteLabel);
        card.add(top, BorderLayout.NORTH);
        card.add(bottom, BorderLayout.CENTER);
        return card;
    }

    /**
     * Tạo khu vực lịch hẹn sắp tới ở phần dưới dashboard.
     * Mỗi hàng trong danh sách hiển thị giờ, tên bệnh nhân, nội dung khám và trạng
     * thái xác nhận,
     * giúp nhân viên chuẩn bị ca làm và làm việc hiệu quả hơn.
     */
    private JPanel createActivityPanel() {
        // Danh sách lịch hẹn mẫu để minh họa khu vực điều phối trong dashboard.
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(20, 21, 18, 21)));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JLabel title = new JLabel("Lịch hẹn sắp tới");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setForeground(INK);
        JButton viewAll = new JButton("Xem tất cả");
        viewAll.setFont(new Font("Arial", Font.BOLD, 11));
        viewAll.setForeground(TEAL);
        viewAll.setBorderPainted(false);
        viewAll.setContentAreaFilled(false);
        viewAll.setFocusPainted(false);
        viewAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewAll.addActionListener(event -> showAppointmentManagement());
        heading.add(title, BorderLayout.WEST);
        heading.add(viewAll, BorderLayout.EAST);

        JPanel appointmentList = new JPanel(new GridLayout(4, 1, 0, 0));

        appointmentList.setOpaque(false);

        appointmentList.add(createAppointmentRow("09:00", "Nguyễn Minh Anh", "Khám tổng quát · BS. Tuấn", "Đang chờ",
                new Color(255, 246, 232)));
        appointmentList.add(createAppointmentRow("09:30", "Trần Hoàng Nam", "Lấy cao răng · BS. Linh", "Đã xác nhận",
                new Color(231, 246, 245)));
        appointmentList.add(createAppointmentRow("10:00", "Lê Ngọc Hà", "Tái khám · BS. Tuấn", "Đã xác nhận",
                new Color(231, 246, 245)));
        appointmentList.add(createAppointmentRow("10:30", "Phạm Gia Bảo", "Tư vấn điều trị · BS. Minh", "Đang chờ",
                new Color(255, 246, 232)));

        panel.add(heading, BorderLayout.NORTH);
        panel.add(appointmentList, BorderLayout.CENTER);
        return panel;

    }

    // Dựng một dòng lịch hẹn với giờ, thông tin bệnh nhân và trạng thái.
    private JPanel createAppointmentRow(String time, String patient, String detail, String status,
            Color statusBackground) {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, LINE));

        JLabel timeLabel = new JLabel(time, SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        timeLabel.setForeground(TEAL);
        timeLabel.setPreferredSize(new Dimension(62, 50));

        JPanel patientInfo = new JPanel();
        patientInfo.setOpaque(false);
        patientInfo.setLayout(new BoxLayout(patientInfo, BoxLayout.Y_AXIS));
        JLabel patientLabel = new JLabel(patient);
        patientLabel.setFont(new Font("Arial", Font.BOLD, 12));
        patientLabel.setForeground(INK);
        JLabel detailLabel = new JLabel(detail);
        detailLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        detailLabel.setForeground(MUTED);
        patientInfo.add(patientLabel);
        patientInfo.add(Box.createVerticalStrut(5));
        patientInfo.add(detailLabel);

        JLabel statusLabel = new JLabel(status, SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 9));
        statusLabel.setForeground(statusBackground.equals(new Color(231, 246, 245)) ? TEAL : new Color(177, 125, 47));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(statusBackground);
        statusLabel.setBorder(new EmptyBorder(6, 9, 6, 9));

        row.add(timeLabel, BorderLayout.WEST);
        row.add(patientInfo, BorderLayout.CENTER);
        row.add(statusLabel, BorderLayout.EAST);
        //
        return row;
    }
    //

    // Thông báo rõ các màn hình nghiệp vụ chưa được nối với xử lý hoặc cơ sở dữ
    // liệu.
    private void showModule(String title, String category) {
        // Các chức năng nghiệp vụ chưa triển khai được báo rõ thay vì hiển thị dữ liệu
        // giả.
        pageTitle.setText(title);
        pageDescription.setText(category + " · Hệ thống quản lý phòng khám nha khoa Muse");
        JPanel placeholder = new JPanel(new GridBagLayout());
        placeholder.setBackground(Color.WHITE);
        placeholder.setBorder(BorderFactory.createLineBorder(LINE));
        JPanel message = new JPanel();
        message.setOpaque(false);
        message.setLayout(new BoxLayout(message, BoxLayout.Y_AXIS));
        JLabel icon = new JLabel("i", SwingConstants.CENTER);
        icon.setAlignmentX(CENTER_ALIGNMENT);
        icon.setFont(new Font("Arial", Font.PLAIN, 32));
        icon.setForeground(TEAL);
        JLabel heading = new JLabel("Đang xây dựng chức năng");
        heading.setAlignmentX(CENTER_ALIGNMENT);
        heading.setFont(new Font("Arial", Font.BOLD, 18));
        heading.setForeground(INK);
        JLabel description = new JLabel("Mục \"" + title + "\" sẽ được triển khai theo menu dự án.");
        description.setAlignmentX(CENTER_ALIGNMENT);
        description.setFont(new Font("Arial", Font.PLAIN, 12));
        description.setForeground(MUTED);
        message.add(icon);
        message.add(Box.createVerticalStrut(12));
        message.add(heading);
        message.add(Box.createVerticalStrut(8));
        message.add(description);
        placeholder.add(message);

        contentPanel.removeAll();
        contentPanel.add(placeholder, BorderLayout.CENTER);
        contentPanel.revalidate();
        //
        contentPanel.repaint();
    }

    // Avatar dạng vòng tròn với chữ cái đầu tài khoản, không cần phụ thuộc icon
    // font.
    private static final class AccountAvatar extends JPanel {
        private final String initials;

        private AccountAvatar(String accountName) {
            setOpaque(false);
            setPreferredSize(new Dimension(36, 36));
            setMinimumSize(new Dimension(36, 36));
            String trimmedName = accountName.trim();
            initials = trimmedName.isEmpty()
                    ? "NV"
                    : trimmedName.substring(0, Math.min(2, trimmedName.length())).toUpperCase(Locale.ROOT);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(51, 91, 177));
            g.fillOval(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 10));
            FontMetrics metrics = g.getFontMetrics();
            int textX = (getWidth() - metrics.stringWidth(initials)) / 2;
            int textY = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            g.drawString(initials, textX, textY);
            g.dispose();
            //
        }
    }

    // Icon đăng xuất vẽ bằng nét vector để hiển thị nhất quán trên các font hệ
    // thống.
    private static final class LogoutIcon implements Icon {
        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }

        @Override
        public void paintIcon(java.awt.Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(MUTED);
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(x + 2, y + 2, x + 2, y + 14);
            g.drawLine(x + 2, y + 2, x + 7, y + 2);
            g.drawLine(x + 2, y + 14, x + 7, y + 14);
            g.drawLine(x + 5, y + 8, x + 14, y + 8);
            g.drawLine(x + 10, y + 4, x + 14, y + 8);
            g.drawLine(x + 10, y + 12, x + 14, y + 8);
            g.dispose();
        }

    }

    /**
     * Icon menu vẽ chữ viết tắt ASCII trong huy hiệu màu, không phụ thuộc font
     * icon.
     */
    private static final class MenuBadgeIcon implements Icon {
        private final String abbreviation;

        private MenuBadgeIcon(String title) {
            String normalized = Normalizer.normalize(title, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "")
                    .replace('đ', 'd')
                    .replace('Đ', 'D');
            String[] words = normalized.split("[^A-Za-z0-9]+");
            StringBuilder initials = new StringBuilder(2);
            for (String word : words) {
                if (!word.isEmpty()) {
                    initials.append(Character.toUpperCase(word.charAt(0)));
                    if (initials.length() == 2) {
                        break;
                    }
                }
            }
            abbreviation = initials.length() == 0 ? "MD" : initials.toString();
        }

        @Override
        public int getIconWidth() {
            return 26;
        }

        @Override
        public int getIconHeight() {
            return 26;
        }

        @Override
        public void paintIcon(java.awt.Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(231, 246, 245));
            g.fillRoundRect(x, y, getIconWidth(), getIconHeight(), 8, 8);
            g.setColor(TEAL);
            g.setFont(new Font(Font.DIALOG, Font.BOLD, 9));
            FontMetrics metrics = g.getFontMetrics();
            int textX = x + (getIconWidth() - metrics.stringWidth(abbreviation)) / 2;
            int textY = y + (getIconHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            g.drawString(abbreviation, textX, textY);
            g.dispose();
        }
    }

    /**
     * Mở rộng/thu gọn được vẽ bằng nét ngang và dọc thay ký tự mũi tên dễ lỗi font.
     */
    private static final class SectionToggleIcon implements Icon {
        private final boolean expanded;

        private SectionToggleIcon(boolean expanded) {
            this.expanded = expanded;
        }

        @Override
        public int getIconWidth() {
            return 14;
        }

        @Override
        public int getIconHeight() {
            return 14;
        }

        @Override
        public void paintIcon(java.awt.Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(MUTED);
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int centerX = x + getIconWidth() / 2;
            int centerY = y + getIconHeight() / 2;
            g.drawLine(centerX - 4, centerY, centerX + 4, centerY);
            if (!expanded) {
                g.drawLine(centerX, centerY - 4, centerX, centerY + 4);
            }
            g.dispose();
            //
        }
    }

    // Điểm khởi chạy độc lập để có thể mở trực tiếp dashboard khi phát triển giao
    // diện.
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception exception) {
                System.err.println("Không thể áp dụng giao diện hệ điều hành: " + exception.getMessage());
            }
            new ManHinhChinhFrame("Nhân viên").setVisible(true);
        });
    }
}

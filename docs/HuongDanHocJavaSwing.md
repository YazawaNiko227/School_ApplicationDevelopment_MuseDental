# Học làm giao diện Java Swing qua dự án Muse Dental

Tài liệu này hướng dẫn các kiến thức cốt lõi để đọc, xây dựng và kiểm tra giao
diện desktop bằng Java Swing. Ví dụ và tên lớp được lấy từ dự án Muse Dental.

> **Phân biệt quan trọng:** `CapNhatCongNoTraGopPanel` là ví dụ tốt để học bố
> cục, JTable, lọc dữ liệu và sự kiện. Tuy nhiên, panel này hiện tạo dữ liệu
> mẫu trong bộ nhớ; nút thu tiền chưa ghi SQL và nút nhắc nợ chưa gửi tin nhắn
> thật. Giao diện trông hoạt động được không có nghĩa dữ liệu đã được lưu.

## 1. Java Swing là gì?

Swing là thư viện giao diện desktop có sẵn trong Java. Một ứng dụng Swing thường
có:

- **Cửa sổ:** `JFrame`, ví dụ màn hình đăng nhập và màn hình chính.
- **Vùng giao diện con:** `JPanel`, dùng để nhóm và bố trí thành phần.
- **Thành phần:** `JButton`, `JLabel`, `JTextField`, `JComboBox`, `JTable`...
- **Layout manager:** quyết định vị trí và kích thước các thành phần.
- **Model:** giữ dữ liệu hiển thị trong bảng, danh sách và lựa chọn.
- **Listener:** nhận sự kiện bấm nút, chọn dòng, nhập liệu.

Trong dự án, các lớp giao diện nằm trong `src/main/java/gui/`; tiện ích giao
diện dùng chung nằm trong `src/main/java/util/GiaoDienUtil.java`.

### Quan hệ cửa sổ và panel

- Dùng `JFrame` làm cửa sổ cấp cao nhất.
- Dùng `JPanel` làm nội dung có thể thay đổi bên trong cửa sổ.
- Một panel có thể chứa nhiều panel con. Đây là cách chia giao diện thành các
  khu vực nhỏ, dễ đọc và dễ tái sử dụng.

Ví dụ tối thiểu tạo cửa sổ và đặt một panel vào giữa. Tạo giao diện trên
**Event Dispatch Thread (EDT)**:

```java
import java.awt.BorderLayout;
import javax.swing.*;

SwingUtilities.invokeLater(() -> {
    JFrame frame = new JFrame("Ứng dụng");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    JPanel content = new JPanel(new BorderLayout());
    content.add(new JLabel("Nội dung màn hình"), BorderLayout.CENTER);
    frame.setContentPane(content);
    frame.setSize(900, 600);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
});
```

## 2. Cấu trúc một màn hình Swing

Một panel nghiệp vụ thường nên có:

1. Hằng số màu, trạng thái và định dạng.
2. DAO hoặc service cần sử dụng.
3. Các component và model của component.
4. Constructor thiết lập layout, tạo các vùng, đăng ký listener và nạp dữ liệu.
5. Các phương thức nhỏ để tạo giao diện từng vùng.
6. Các phương thức xử lý nghiệp vụ và cập nhật giao diện.

Khung đơn giản:

```java
public final class ViDuPanel extends JPanel {
    private final JTextField txtTuKhoa = new JTextField();
    private final JButton btnTim = new JButton("Tìm");

    public ViDuPanel() {
        setLayout(new BorderLayout(0, 12));
        add(taoThanhTimKiem(), BorderLayout.NORTH);
        add(taoNoiDung(), BorderLayout.CENTER);
        btnTim.addActionListener(event -> timDuLieu());
    }

    private JPanel taoThanhTimKiem() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.add(txtTuKhoa, BorderLayout.CENTER);
        panel.add(btnTim, BorderLayout.EAST);
        return panel;
    }

    private JPanel taoNoiDung() {
        return new JPanel(new BorderLayout());
    }

    private void timDuLieu() {
        String tuKhoa = txtTuKhoa.getText().trim();
        // Kiểm tra đầu vào rồi gọi tầng xử lý dữ liệu.
    }
}
```

Hãy chia phương thức theo **khu vực giao diện** và **hành động**, thay vì viết
toàn bộ component và nghiệp vụ vào một constructor dài.

## 3. Layout manager: bố trí giao diện

Không nên đặt tọa độ cố định cho từng component bằng `setBounds`. Cách đó dễ vỡ
khi đổi kích thước cửa sổ, đổi font hoặc chạy trên màn hình khác. Dùng layout
manager để Swing sắp xếp thành phần.

### `BorderLayout`

Chia vùng thành `NORTH`, `SOUTH`, `EAST`, `WEST`, `CENTER`. Phù hợp cho khung
tổng thể: menu bên trái, header phía trên, nội dung ở giữa.

```java
JPanel root = new JPanel(new BorderLayout(16, 16));
root.add(menu, BorderLayout.WEST);
root.add(header, BorderLayout.NORTH);
root.add(content, BorderLayout.CENTER);
```

Chỉ nên thêm một component cho mỗi vị trí. Nếu một vùng cần nhiều thành phần,
hãy tạo một `JPanel` con.

### `FlowLayout`

Đặt các component lần lượt theo hàng. Hợp với thanh nút, nhóm thẻ thống kê,
hoặc các lựa chọn ngắn.

```java
JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
actions.add(btnHuy);
actions.add(btnLuu);
```

### `BoxLayout`

Xếp thành phần theo một trục. `BoxLayout.Y_AXIS` thường dùng để xếp dọc nhãn,
ô nhập và khoảng cách; `X_AXIS` dùng để xếp ngang.

```java
panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
panel.add(title);
panel.add(Box.createVerticalStrut(8));
panel.add(form);
```

Khi dùng `BoxLayout`, để ý alignment và kích thước tối đa của component; nếu
không, thành phần có thể bị kéo giãn hoặc lệch.

### Layout khác

- `GridLayout`: lưới các ô bằng nhau.
- `GridBagLayout`: bố cục biểu mẫu linh hoạt nhưng cần cấu hình nhiều hơn.
- Có thể lồng các panel dùng layout khác nhau. Đây là cách phổ biến để tạo
  màn hình lớn mà vẫn dễ bảo trì.

## 4. Component thường dùng

| Component | Công dụng | Ghi chú |
|---|---|---|
| `JLabel` | Hiển thị chữ hoặc icon | Không nhập liệu. |
| `JButton` | Thực hiện hành động | Gắn `ActionListener`. |
| `JTextField` | Nhập một dòng | Dùng `getText().trim()` trước khi xử lý. |
| `JPasswordField` | Nhập mật khẩu | Đọc bằng `getPassword()`, không log mật khẩu. |
| `JComboBox<T>` | Chọn một mục | Dùng cho trạng thái, vai trò, bác sĩ/phòng. |
| `JTable` | Hiển thị dữ liệu dạng hàng/cột | Luôn bọc trong `JScrollPane`. |
| `JScrollPane` | Tạo vùng cuộn | Cần cho bảng hoặc nội dung dài. |
| `JPanel` | Nhóm và sắp xếp component | Có thể đặt panel con vào panel cha. |
| `JOptionPane` | Thông báo/xác nhận nhanh | Không dùng thay cho màn hình nhập liệu phức tạp. |
| `JSpinner` | Chọn giá trị theo bước | Có thể cấu hình để chọn giờ/phút. |
| `JDateChooser` | Chọn ngày từ lịch | Thư viện JCalendar trong `lib/`. |

Component thường được khai báo thành field của panel khi cần truy cập lại trong
listener hoặc khi cập nhật giao diện. Component chỉ dùng một lần trong một
phương thức có thể khai báo biến cục bộ.

## 5. JTable và dữ liệu dạng bảng

`JTable` hiển thị dữ liệu; dữ liệu thực sự được quản lý bởi một **table model**.
Trong dự án thường dùng `DefaultTableModel`.

```java
DefaultTableModel model = new DefaultTableModel(
        new String[] { "Mã", "Tên", "Trạng thái" }, 0) {
    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
};

JTable table = new JTable(model);
table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
JScrollPane scroll = new JScrollPane(table);
```

Thêm, xóa và làm mới dòng:

```java
model.addRow(new Object[] { "CN001", "Nguyễn An", "Đang xử lý" });
model.setRowCount(0); // xóa các dòng hiển thị, không xóa dữ liệu database
```

Điểm cần nhớ:

- Sắp thứ tự cột nhất quán giữa tên cột, dữ liệu đưa vào và xử lý sự kiện.
- `setRowCount(0)` chỉ xóa dòng khỏi model đang hiển thị; không phải lệnh xóa
  SQL.
- Có thể dùng `setPreferredWidth` để gợi ý độ rộng cột. Tổng chiều rộng lớn
  hơn vùng nhìn sẽ tạo cuộn ngang.
- Dùng `ListSelectionListener` để cập nhật panel chi tiết khi người dùng chọn
  một dòng.
- Nếu gắn `TableRowSorter`, dòng đang thấy có thể khác vị trí trong model.
  Trước khi lấy dữ liệu gốc cần chuyển:

```java
int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
```

Trong `CapNhatCongNoTraGopPanel`, bảng hồ sơ và bảng đợt dùng hai
`DefaultTableModel` không cho sửa trực tiếp. Khi chọn hồ sơ, phương thức
`hienThiChiTiet()` cập nhật bảng đợt bên phải.

### Tô màu ô bằng renderer

Renderer quyết định cách một ô được vẽ. Nó không phải component độc lập cho
mỗi ô, mà thường được Swing dùng lại nhiều lần. Renderer nên chỉ định dạng dựa
trên giá trị nhận vào và cần giữ màu nền/chữ khi dòng đang được chọn.

`StatusRenderer` trong panel công nợ đổi màu chữ trạng thái: hoàn tất màu xanh,
quá hạn màu đỏ, đang trả góp màu xanh dương.

## 6. Sự kiện và listener

Giao diện hướng sự kiện: chương trình phản ứng với hành động của người dùng.

```java
button.addActionListener(event -> xuLyNut());

comboBox.addActionListener(event -> locDuLieu());

table.getSelectionModel().addListSelectionListener(event -> {
    if (!event.getValueIsAdjusting()) {
        hienThiChiTiet();
    }
});
```

Các sự kiện phổ biến:

- `ActionListener`: bấm nút, chọn mục combo, nhấn Enter trong ô nhập.
- `ListSelectionListener`: chọn dòng trong bảng.
- `DocumentListener`: phản ứng tức thời khi nội dung ô nhập thay đổi.
- `MouseListener`/`MouseAdapter`: nhấn chuột, nhấp đúp, rê chuột.

Listener nên gọi một phương thức có tên mô tả hành động, ví dụ
`thuTienDot()` hoặc `lamMoi()`. Cách này dễ đọc và kiểm thử hơn việc viết toàn
bộ xử lý dài trong lambda.

## 7. Dữ liệu trạng thái và đồng bộ giao diện

GUI thường có hai loại dữ liệu:

1. **Dữ liệu nghiệp vụ**: công nợ, đợt thanh toán, nhân viên, lịch hẹn. Nên
   thuộc model/entity hoặc do service/DAO nạp.
2. **Trạng thái giao diện**: dòng đang chọn, từ khóa lọc, nút đang bật/tắt,
   trạng thái đang tải.

Trong panel công nợ:

- `CongNo` đại diện hồ sơ và chứa danh sách `Dot`.
- `Dot.trangThai()` tính trạng thái dựa trên đã thanh toán hay ngày đến hạn.
- `lamMoi()` lọc hồ sơ, đổ lại model, cập nhật thẻ và khôi phục lựa chọn.
- `hienThiChiTiet()` đồng bộ thông tin hồ sơ, danh sách đợt và trạng thái nút.

Một luồng cập nhật giao diện tốt thường là:

1. Xác định dữ liệu mới.
2. Cập nhật model.
3. Cập nhật label/tổng số liệu.
4. Bật/tắt hành động không hợp lệ.
5. Thông báo kết quả cho người dùng.

Không nên để dữ liệu bảng và dữ liệu nghiệp vụ thay đổi riêng rẽ mà không có
một bước làm mới đồng bộ.

## 8. Kiểm tra dữ liệu, xác nhận và thông báo

Kiểm tra đầu vào ở GUI giúp người dùng sửa lỗi sớm; tầng nghiệp vụ/DAO vẫn cần
kiểm tra lại các điều kiện quan trọng.

Ví dụ:

```java
String ma = txtMa.getText().trim();
if (ma.isEmpty()) {
    JOptionPane.showMessageDialog(
            this, "Vui lòng nhập mã.", "Thiếu dữ liệu",
            JOptionPane.WARNING_MESSAGE);
    txtMa.requestFocusInWindow();
    return;
}
```

Với hành động có ảnh hưởng dữ liệu, hãy xác nhận trước:

```java
int result = JOptionPane.showConfirmDialog(
        this, "Bạn có chắc muốn tiếp tục?", "Xác nhận",
        JOptionPane.YES_NO_OPTION);
if (result != JOptionPane.YES_OPTION) {
    return;
}
```

Các loại phản hồi nên phân biệt:

- Thông tin thành công.
- Cảnh báo dữ liệu chưa hợp lệ.
- Lỗi kết nối/lưu dữ liệu.
- Trạng thái chưa có dòng nào hoặc chưa chọn hồ sơ.

Không hiển thị thông báo “đã lưu” nếu dữ liệu chỉ mới đổi trong bộ nhớ.

## 9. Ngày, giờ và định dạng tiền

Lưu ngày nghiệp vụ bằng `LocalDate`, thời điểm bằng `LocalDateTime`, giờ trong
ngày bằng `LocalTime`. Chỉ định dạng thành chuỗi khi hiển thị.

```java
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
String text = date.format(formatter);
```

Trong dự án, `GiaoDienUtil` cấu hình `JDateChooser` và có thể khóa nhập ngày
bằng bàn phím. `XuLyDatLichHenPanel` còn dùng `JSpinner` với editor `HH:mm`
cho giờ hẹn.

Đối với tiền:

- Dùng kiểu số cho phép tính toán; không cộng/trừ chuỗi đã định dạng.
- Chỉ áp dụng dấu phân cách hàng nghìn khi hiển thị.
- Trong hệ thống tài chính thực tế, cân nhắc `BigDecimal` thay vì `double` để
  tránh sai số số thực.

## 10. Kết nối giao diện với dữ liệu

Nên tách trách nhiệm:

```text
GUI (JPanel/JFrame)
    -> DAO/service
        -> SQL Server
```

- GUI thu thập đầu vào, hiển thị kết quả và phản hồi thao tác.
- Service thực hiện quy tắc nghiệp vụ nếu nghiệp vụ có nhiều bước.
- DAO chứa truy vấn SQL và ánh xạ kết quả sang entity/DTO.
- Database lưu dữ liệu bền vững.

Không đặt SQL dài hoặc thao tác JDBC trực tiếp vào listener. Không dùng dữ liệu
mẫu nhưng hiển thị như thể đã lưu database.

### Không làm treo giao diện

Các listener Swing chạy trên EDT. Nếu truy vấn SQL mất thời gian mà chạy trực
tiếp trong listener, cửa sổ có thể đứng yên. Dùng `SwingWorker` cho tác vụ nền:

```java
new SwingWorker<List<String>, Void>() {
    @Override
    protected List<String> doInBackground() throws Exception {
        return dao.layDanhSach();
    }

    @Override
    protected void done() {
        try {
            List<String> ketQua = get();
            capNhatBang(ketQua);
        } catch (Exception ex) {
            hienThiLoi("Không thể tải dữ liệu. Vui lòng thử lại.");
            // Ghi exception vào logger của ứng dụng để chẩn đoán.
        }
    }
}.execute();
```

`doInBackground()` chạy ngoài EDT; cập nhật Swing component trong `done()`.
Trong ứng dụng hoàn chỉnh nên phân loại lỗi rõ ràng, ghi log phù hợp và không
hiển thị thông tin nhạy cảm từ exception cho người dùng cuối.

## 11. Điều hướng giữa các màn hình

Với ứng dụng có sidebar, giữ một `JFrame` chính và thay panel nội dung sẽ tạo
cảm giác tích hợp trong một cửa sổ. Màn hình chính của dự án dùng
`showContentPanel(title, description, panel)` để thay nội dung theo menu.

Khi thêm màn hình mới:

1. Tạo lớp `JPanel` trong package `gui`.
2. Tạo constructor nhận các thông tin cần thiết, ví dụ tên đăng nhập nếu cần.
3. Thêm một phương thức trong `ManHinhChinhFrame` để tạo panel và gọi
   `showContentPanel`.
4. Gắn mục menu tới phương thức đó.
5. Kiểm tra quyền hiển thị mục menu và hành vi quay lại/đăng xuất.

Không tạo `JFrame` mới cho mọi mục menu nếu yêu cầu là nội dung tích hợp trong
màn hình chính.

## 12. Đồng bộ giao diện và tái sử dụng style

Ứng dụng thường định nghĩa palette màu, font, khoảng cách và kiểu nút nhất
quán. Dự án có `GiaoDienUtil` để tái sử dụng một số quy ước như:

- Màu chữ chính/phụ và màu nhấn.
- Tạo card, nút, hàng nhãn + ô nhập.
- Chuẩn hóa bảng và trường nhập.
- Định dạng ngày/giờ/tiền, cấu hình date chooser.

Trước khi viết helper mới, hãy tìm xem dự án đã có tiện ích tương ứng chưa.
Nếu nhiều panel cùng lặp lại một kiểu giao diện, đưa style chung vào helper;
nếu cách bố trí chỉ thuộc một màn hình, giữ logic đó trong panel.

## 13. Tác vụ bất đồng bộ và an toàn dữ liệu

- Không truy vấn database dài trực tiếp trên EDT.
- Khi nạp dữ liệu, cân nhắc vô hiệu hóa nút, hiển thị trạng thái đang tải rồi
  bật lại sau khi hoàn tất.
- Với ghi nhiều bảng, dùng transaction ở service/DAO để bảo đảm thành công
  toàn bộ hoặc rollback toàn bộ.
- Phân biệt lỗi kết nối, dữ liệu không hợp lệ và lỗi nghiệp vụ.
- Không để nút bấm thay đổi dữ liệu cục bộ rồi báo đã lưu nếu thao tác SQL chưa
  thành công.
- Chỉ kiểm thử thao tác ghi trên database được phép dùng để thử nghiệm; tránh
  dữ liệu bệnh nhân thật.

## 14. Tài nguyên giao diện

Ảnh, icon và font đóng gói cùng ứng dụng nên đặt trong resources và nạp từ
classpath, thay vì dựa vào đường dẫn tuyệt đối trên máy lập trình viên.

```java
URL resource = getClass().getResource("/images/logo.png");
if (resource == null) {
    throw new IllegalStateException("Không tìm thấy logo.");
}
ImageIcon icon = new ImageIcon(resource);
```

`build.bat` biên dịch mã vào `out/` và sao chép resources vào thư mục build.
Nếu thêm ảnh mà chạy không thấy ảnh, kiểm tra đường dẫn resource, chữ hoa/thường
và việc copy resource.

## 15. Quy trình tạo một màn hình mới

1. **Mô tả luồng:** người dùng bắt đầu từ đâu, nhập gì, kết quả mong đợi là gì.
2. **Phác bố cục:** chia thành header, form, bộ lọc, bảng, chi tiết, hành động.
3. **Chọn component:** nhập văn bản, chọn danh sách, chọn ngày hay bảng.
4. **Chọn layout:** ưu tiên các layout manager; không đặt tọa độ cố định.
5. **Tạo model dữ liệu:** xác định entity/DTO và trạng thái giao diện.
6. **Tạo panel theo vùng:** dùng các phương thức nhỏ, tên rõ nghĩa.
7. **Đăng ký listener:** mỗi listener gọi một hành động có tên.
8. **Kiểm tra đầu vào:** bắt buộc, định dạng, miền giá trị và lựa chọn hiện tại.
9. **Nối service/DAO:** nạp và lưu thật; hiển thị lỗi rõ ràng.
10. **Kiểm thử:** cửa sổ nhỏ/lớn, không có dữ liệu, chuỗi dài, lỗi SQL, chọn
    dòng, lọc, bấm liên tục và hủy xác nhận.

## 16. Đọc `CapNhatCongNoTraGopPanel` như một bài học

Mở [CapNhatCongNoTraGopPanel.java](../src/main/java/gui/CapNhatCongNoTraGopPanel.java)
và lần lượt tìm các phần:

1. **Hằng số và model:** màu, định dạng ngày/tiền, class lồng `Dot` và
   `CongNo`.
2. **Component:** thẻ thống kê, bộ lọc, hai bảng và nút thao tác.
3. **Constructor:** chọn `BorderLayout`, dựng vùng thống kê, danh sách và chi
   tiết.
4. **Phương thức tạo UI:** `createSummaryPanel`, `createDanhSachCard`,
   `createChiTietCard`.
5. **Sự kiện:** chọn hồ sơ, tìm kiếm, chọn trạng thái, thu tiền, nhắc nợ.
6. **Cập nhật model/UI:** `lamMoi`, `capNhatThongKe`, `hienThiChiTiet`.
7. **Style/renderer:** `styleTable`, `createButton`, `StatusRenderer`.
8. **TODO DAO:** xác định chỗ dữ liệu thật và transaction cần được nối vào.

Một ví dụ quan trọng về quy tắc giao diện là nút thu tiền chỉ cho thu đợt chưa
thanh toán sớm nhất. Ví dụ cũng minh họa kiểm tra chưa chọn hồ sơ, xác nhận
trước hành động và làm mới bảng sau khi thay đổi trạng thái.

## 17. Bài tập tự học

Thực hiện lần lượt trên một nhánh làm việc hoặc dữ liệu giả:

1. Tạo `JFrame` có tiêu đề, kích thước tối thiểu và căn giữa.
2. Tạo panel có tiêu đề, ô tìm kiếm và nút bằng `BorderLayout`.
3. Thêm bảng với ba cột, không cho sửa trực tiếp và bọc bằng `JScrollPane`.
4. Thêm bộ lọc `JComboBox` và cập nhật danh sách khi lựa chọn thay đổi.
5. Khi chọn dòng, hiển thị chi tiết ở panel bên cạnh.
6. Thêm kiểm tra đầu vào và hộp thoại xác nhận.
7. Thêm renderer để tô màu trạng thái.
8. Thay dữ liệu mẫu bằng DAO, xử lý lỗi và tải bằng `SwingWorker`.
9. Kiểm tra rằng nút “Lưu” chỉ báo thành công sau khi DAO xác nhận lưu thành
   công.

## 18. Chạy và kiểm tra dự án

Từ PowerShell tại thư mục gốc:

```powershell
.\build.bat
```

Để chạy ứng dụng cần cấu hình SQL Server theo [README.md](../README.md), sau đó:

```powershell
.\run.bat
```

Build thành công chỉ chứng minh mã biên dịch được. Nó không chứng minh layout
đúng ở mọi kích thước, mọi nút hoạt động, hoặc dữ liệu đã lưu SQL. Hãy kiểm tra
riêng:

- **Khởi tạo:** panel dựng được mà không ném exception.
- **Giao diện:** nhãn, bảng, nút và cuộn hiển thị đúng.
- **Tương tác:** chọn, lọc, tìm kiếm, xác nhận, hủy.
- **Nghiệp vụ:** quy tắc đúng với các trường hợp hợp lệ/không hợp lệ.
- **Tích hợp:** đọc/ghi database đúng và xử lý lỗi kết nối.

## 19. Bản đồ mã nguồn GUI

| Nội dung muốn học | Tệp gợi ý |
|---|---|
| Cửa sổ đăng nhập | `gui/DangNhapFrame.java` |
| Khung chính và điều hướng panel | `gui/ManHinhChinhFrame.java` |
| Form đặt lịch, ngày/giờ, bảng và DAO | `gui/XuLyDatLichHenPanel.java` |
| Bảng nhiều khu vực, thống kê, trạng thái | `gui/CapNhatCongNoTraGopPanel.java` |
| Style và date chooser dùng chung | `util/GiaoDienUtil.java` |

Đường dẫn đầy đủ tính từ thư mục gốc là `src/main/java/` cộng với đường dẫn
trong bảng.

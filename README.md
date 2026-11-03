# Muse Dental — Ứng dụng quản lý phòng khám nha khoa

Muse Dental là ứng dụng desktop hỗ trợ vận hành phòng khám nha khoa: tiếp nhận
bệnh nhân, quản lý lịch và ca trực, hỗ trợ quy trình khám, quản lý thuốc/vật tư
và theo dõi thanh toán. Ứng dụng được xây dựng bằng Java Swing, kết nối SQL
Server qua JDBC.

> **Tình trạng dự án:** Các luồng nghiệp vụ chính đã được nối với SQL Server.
> Đây vẫn là đồ án đang phát triển; hãy xem [Tình trạng chức năng](#tình-trạng-chức-năng)
> và chỉ thử các thao tác ghi trên database kiểm thử riêng.

## Công nghệ

- Java và Java Swing.
- Microsoft SQL Server.
- Microsoft JDBC Driver for SQL Server.
- JCalendar cho các trường chọn ngày.
- Apache PDFBox để xuất hóa đơn ra PDF.

Các thư viện cần thiết đã được đặt trong `lib/`. Dự án hiện biên dịch bằng các
script Windows, chưa dùng Maven hoặc Gradle.

## Chức năng và vai trò

Sidebar được tổ chức theo các nhóm Hệ thống, Danh mục, Cập nhật, Tìm kiếm, Xử
lý và Thống kê. Ứng dụng có sáu vai trò mẫu:

| Mã vai trò | Vai trò       |
| ---------- | ------------- |
| `QUAN_TRI` | Quản trị viên |
| `BAC_SI`   | Bác sĩ        |
| `Y_TA`     | Y tá          |
| `LE_TAN`   | Lễ tân        |
| `THU_NGAN` | Thu ngân      |
| `THU_KHO`  | Nhân viên kho |

Các nhóm nghiệp vụ trên giao diện gồm:

- Đăng nhập, tổng quan, đổi mật khẩu và phân quyền menu.
- Quản lý nhân viên, bệnh nhân, dịch vụ, thuốc và vật tư y tế.
- Đặt lịch, phân công ca trực, xem lịch cá nhân, điều phối và chuẩn bị phòng.
- Khám lâm sàng, sơ đồ răng, phác đồ, kê đơn và ghi nhận vật tư tiêu hao.
- Lập/tra cứu hóa đơn, công nợ trả góp, hồ sơ bệnh án và báo cáo doanh thu.

## Yêu cầu

- Windows.
- JDK tương thích với mã nguồn và các thư viện trong `lib/`; cần có cả `java`
  và `javac` trong `PATH`.
- SQL Server, bật kết nối TCP/IP. Cổng SQL Server phải khớp với chuỗi JDBC.
- SQL login có quyền đọc/ghi trên database ứng dụng.

## Cài đặt database

### Tạo database mới

1. Mở SQL Server Management Studio (SSMS) và kết nối đến SQL Server của bạn.
2. Mở và chạy script `database/QuanLyPhongKhamMuse_Schema .sql` trong SSMS.
   Tên file có một dấu cách trước phần mở rộng `.sql`. Script tạo database
   `QuanLyPhongKhamMuse`, các bảng và dữ liệu mẫu đầy đủ (bao gồm cả bộ dữ
   liệu mở rộng `SEED_FULL_V1`).
3. Nếu dùng SQL Server Express, instance thường có tên
   `localhost\SQLEXPRESS`. Bật TCP/IP trong SQL Server Configuration Manager
   và khởi động lại dịch vụ SQL Server sau khi thay đổi cấu hình.
4. Cấu hình SQL Server cho phép đăng nhập SQL Server (SQL Server Authentication)
   và bật TCP/IP trên cổng `1433`. Kết nối mặc định trong `ConnectDB` dùng
   `localhost`, database `QuanLyPhongKhamMuse` và tài khoản `sa`. Nếu máy bạn
   dùng thông tin khác, sửa các hằng `URL`, `USER` và `PASSWORD` trong
   `src/main/java/connectDB/ConnectDB.java`.

### Database đã tồn tại

Nếu database cũ chưa có các bảng RBAC, mở và chạy
`database/Migration_RBAC.sql` trong SSMS. Không chạy script schema tạo mới
trên database đang có dữ liệu nếu chưa sao lưu và hiểu rõ ảnh hưởng.

Nếu database đã được tạo trước khi cập nhật cơ chế mật khẩu mẫu, chạy thêm
`database/Migration_MatKhauMau.sql` trong SSMS để đổi tên cột mật khẩu và đặt
mật khẩu `Muse@12345` cho các tài khoản mẫu vẫn còn giá trị placeholder.

Script `database/QuanLyPhongKhamMuse_Schema .sql` đã bao gồm phần chuyển dữ
liệu sang tiếng Việt có dấu ở cuối file. Nếu database đã tồn tại và chỉ muốn
chuyển đổi dữ liệu, chạy riêng đoạn từ mục
`CHUYEN DU LIEU MAU SANG TIENG VIET CO DAU` đến hết file. Các mã trạng thái nội
bộ (`Dang lam viec`, `Cho xac nhan`, `Da thanh toan`...) vẫn giữ không dấu vì
được dùng trong điều kiện truy vấn; giao diện sẽ tự hiển thị các trạng thái này
bằng tiếng Việt có dấu.

Các script SQL có lệnh `GO`; hãy chạy bằng SSMS hoặc công cụ hiểu cú pháp `GO`,
không gửi toàn bộ file như một câu lệnh JDBC duy nhất.

## Cấu hình kết nối

Ứng dụng hiện dùng cấu hình SQL Server mặc định khai báo trong
`src/main/java/connectDB/ConnectDB.java`; không cần tạo biến môi trường trước
khi chạy. Máy cần có SQL Server đang chạy, database `QuanLyPhongKhamMuse`,
TCP/IP trên cổng `1433` và đăng nhập SQL Server được bật. Đây là cấu hình tiện
cho môi trường phát triển cục bộ; không dùng tài khoản `sa` cho môi trường
thực tế hoặc chia sẻ mật khẩu trong mã nguồn.

Mật khẩu tài khoản Muse được lưu dạng văn bản thuần theo yêu cầu demo; cách này
không an toàn cho dữ liệu thật hoặc môi trường triển khai.

### Kiểm tra SQL Server không kết nối được

```powershell
Test-NetConnection localhost -Port 1433
```

Nếu `TcpTestSucceeded` là `False`, hãy kiểm tra dịch vụ SQL Server, cấu hình
TCP/IP và cổng đang lắng nghe. Nếu đã kết nối được TCP nhưng gặp lỗi đăng nhập
(ví dụ SQL Server 18456), hãy kiểm tra SQL login, mật khẩu, chế độ xác thực và
quyền truy cập database. Tài khoản SQL dùng cho ứng dụng cần quyền đọc/ghi;
tài khoản chạy script tạo/cập nhật cấu trúc cần thêm quyền DDL.

## Biên dịch và chạy

Từ thư mục gốc dự án:

```powershell
.\build.bat
.\run.bat
```

`build.bat` biên dịch mã Java vào `out/`; `run.bat` build lại rồi mở màn hình
đăng nhập. Cũng có thể chạy từng bước:

```powershell
.\build.bat
java -cp "out;lib\*" gui.DangNhapFrame
```

## Tài khoản mẫu

Các tài khoản mẫu dưới đây đều dùng chung mật khẩu `Muse@12345`:

| Tên đăng nhập | Vai trò       |
| ------------- | ------------- |
| `nv001`       | Bác sĩ        |
| `nv002`       | Bác sĩ        |
| `nv003`       | Lễ tân        |
| `nv004`       | Thu ngân      |
| `nv005`       | Nhân viên kho |
| `nv006`       | Y tá          |
| `admin`       | Quản trị viên |

Các mật khẩu được lưu dạng văn bản thuần để tiện chạy đồ án cục bộ; không dùng
cách này cho dữ liệu thật hoặc môi trường triển khai.

## Tình trạng chức năng

### Các luồng đã nối với SQL Server

- Đăng nhập, tổng quan và đổi mật khẩu.
- Phân quyền menu theo vai trò.
- Quản lý nhân viên.
- Quản lý bệnh nhân; thêm bệnh nhân sẽ tạo hồ sơ bệnh án tương ứng.
- Quản lý dịch vụ và bảng giá.
- Quản lý thuốc/vật tư.
- Đặt lịch hẹn, phân ca và xem lịch cá nhân.
- Điều phối, chuẩn bị phòng, khám lâm sàng và sơ đồ răng.
- Kê đơn thuốc, ghi nhận xuất vật tư.
- Lập hóa đơn cho lần khám đã chốt; chi tiết dịch vụ và thuốc lấy từ phác đồ/đơn thuốc.
- Công nợ trả góp: tải hồ sơ và các đợt từ SQL, ghi nhận thu tiền theo transaction.
- Tra cứu hóa đơn/chi tiết giao dịch và tra cứu bệnh án từ SQL.
- Báo cáo doanh thu: tổng hợp hóa đơn đã thanh toán/đang trả góp từ các dòng
  dịch vụ và thuốc trong SQL Server.

Các thao tác lập hóa đơn và thu từng đợt được xử lý trong transaction. Lập hóa
đơn lưu giá dịch vụ/thuốc tại thời điểm lập; với trả góp, đợt đầu được ghi nhận
đã thu ngay khi lập hóa đơn.

### Giới hạn chức năng

- Quản lý bệnh nhân và quản lý dịch vụ.

Một số DAO cho các nghiệp vụ trên đã tồn tại, nhưng giao diện chưa sử dụng
chúng đầy đủ; DAO dịch vụ hiện mới hỗ trợ đọc/thêm. Vì vậy, dữ liệu nhập trên
các màn hình quản lý này có thể chỉ nằm trong bộ nhớ và không được lưu vào SQL.

Lập hóa đơn chỉ nhận lần khám đã chốt và chưa có hóa đơn. Khi chọn trả góp,
đợt đầu được ghi nhận đã thu ngay lúc lập hóa đơn; các đợt còn lại được theo dõi
ở màn Công nợ và trả góp theo đợt. Chẩn đoán đang ở trạng thái nháp chưa đủ
điều kiện để lập hóa đơn.

### Giới hạn phân quyền

RBAC hiện điều khiển các mục menu được hiển thị theo vai trò. Đây chưa phải cơ
chế phân quyền bảo mật ở tầng database hoặc kiểm tra quyền cho từng thao tác
ghi. Không dùng phân quyền giao diện thay cho bảo vệ dữ liệu thật; hãy giới
hạn quyền SQL login và chỉ dùng dữ liệu phù hợp trong môi trường phát triển.

## Kiểm thử

Các kiểm tra gần nhất:

- `build.bat` biên dịch toàn bộ mã nguồn thành công.
- Các truy vấn đọc cho bệnh nhân, dịch vụ, chẩn đoán, phác đồ, bệnh án, hóa đơn,
  công nợ và báo cáo đã chạy được trên SQL Server đang cấu hình.
- Không chạy thao tác ghi trên database đang cấu hình. Trong lần kiểm tra này,
  SQL login không có quyền `CREATE DATABASE`, nên không thể dựng database tạm
  để xác minh các thao tác ghi.

Các kiểm tra trên không mô phỏng thao tác người dùng trên toàn bộ nút và màn
hình. Dự án hiện chưa có bộ kiểm thử tự động end-to-end đầy đủ; cần kiểm thử
các luồng ghi trên database riêng trước khi triển khai sử dụng thật.

## Cấu trúc thư mục

```text
database/   Script schema và migration SQL
docs/       Tài liệu yêu cầu, thiết kế và luồng nghiệp vụ
lib/        Thư viện Java/JDBC
src/main/java/
  connectDB/ Quản lý kết nối SQL Server
  dao/       Truy vấn và thao tác dữ liệu
  dto/       Đối tượng truyền dữ liệu
  entity/    Mô hình dữ liệu
  gui/       Giao diện Swing
  security/  Băm mật khẩu
  util/      Tiện ích, phân quyền menu, xuất PDF
build.bat   Biên dịch dự án
run.bat     Build và mở màn hình đăng nhập
```

## Tài liệu dự án

Tài liệu phân tích yêu cầu, thiết kế và luồng nghiệp vụ nằm trong `docs/`.
Luồng nghiệp vụ chính được mô tả trong `docs/Luồng khám bệnh chính.txt`.

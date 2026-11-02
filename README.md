# Muse Dental — Ứng dụng quản lý phòng khám nha khoa

Muse Dental là ứng dụng desktop hỗ trợ vận hành phòng khám nha khoa: tiếp nhận
bệnh nhân, quản lý lịch và ca trực, hỗ trợ quy trình khám, quản lý thuốc/vật tư
và theo dõi thanh toán. Ứng dụng được xây dựng bằng Java Swing, kết nối SQL
Server qua JDBC.

> **Tình trạng dự án:** Đây là đồ án đang phát triển. Một số màn hình đã đọc/ghi
> SQL Server; một số màn hình vẫn dùng dữ liệu mẫu và chưa hoàn tất tích hợp.
> Xem [Tình trạng chức năng](#tình-trạng-chức-năng) trước khi dùng dữ liệu thật.

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
   `QuanLyPhongKhamMuse`, các bảng và dữ liệu mẫu.
3. Nếu dùng SQL Server Express, instance thường có tên
   `localhost\SQLEXPRESS`. Bật TCP/IP trong SQL Server Configuration Manager
   và khởi động lại dịch vụ SQL Server sau khi thay đổi cấu hình.
4. Tạo hoặc chọn một SQL login dành riêng cho ứng dụng; cấp quyền đọc và ghi
   trên database `QuanLyPhongKhamMuse`. Không nên dùng tài khoản `sa` cho ứng
   dụng thường ngày.

### Database đã tồn tại

Nếu database cũ chưa có các bảng RBAC, mở và chạy
`database/Migration_RBAC.sql` trong SSMS. Không chạy script schema tạo mới
trên database đang có dữ liệu nếu chưa sao lưu và hiểu rõ ảnh hưởng.

Các script SQL có lệnh `GO`; hãy chạy bằng SSMS hoặc công cụ hiểu cú pháp `GO`,
không gửi toàn bộ file như một câu lệnh JDBC duy nhất.

## Cấu hình kết nối

Ứng dụng đọc cấu hình từ biến môi trường:

| Biến               | Bắt buộc | Mô tả                                                                       |
| ------------------ | -------: | --------------------------------------------------------------------------- |
| `MUSE_DB_USER`     |       Có | SQL login dùng để kết nối.                                                  |
| `MUSE_DB_PASSWORD` |       Có | Mật khẩu của SQL login.                                                     |
| `MUSE_DB_URL`      |    Không | JDBC URL; mặc định dùng `localhost:1433` và database `QuanLyPhongKhamMuse`. |

Ví dụ cấu hình tạm thời trong PowerShell. Thay URL nếu SQL Server của bạn dùng
cổng khác:

```powershell
$env:MUSE_DB_USER = "muse_app"
$secure = Read-Host "Nhập mật khẩu SQL Server" -AsSecureString
$ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
try {
    $env:MUSE_DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr)
} finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)
}
$env:MUSE_DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyPhongKhamMuse;encrypt=true;trustServerCertificate=true"
```

Các biến trên chỉ có hiệu lực trong cửa sổ PowerShell hiện tại. Hãy chạy lệnh
build/chạy ứng dụng trong cùng cửa sổ đó. Không ghi mật khẩu vào mã nguồn,
README hoặc Git.

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

Từ thư mục gốc dự án, trong cửa sổ PowerShell đã cấu hình kết nối:

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

Schema tạo các tên đăng nhập mẫu sau. Mật khẩu trong script là hash placeholder
và **không thể dùng để đăng nhập**:

| Tên đăng nhập | Vai trò       |
| ------------- | ------------- |
| `nv001`       | Bác sĩ        |
| `nv002`       | Bác sĩ        |
| `nv003`       | Lễ tân        |
| `nv004`       | Thu ngân      |
| `nv005`       | Nhân viên kho |
| `nv006`       | Y tá          |
| `admin`       | Quản trị viên |

Sau khi build và cấu hình database, khởi tạo mật khẩu cho từng tài khoản mẫu
trong PowerShell hoặc Command Prompt tương tác:

```powershell
java -cp "out;lib\*" util.TaoMatKhauBanDau
```

Công cụ yêu cầu mật khẩu mới (ít nhất 8 ký tự), xác nhận mật khẩu và tên đăng
nhập. Chạy lại cho từng tài khoản bạn muốn sử dụng. Mật khẩu được băm bằng
PBKDF2-HMAC-SHA256; công cụ chỉ cập nhật tài khoản còn hash placeholder, không
đổi mật khẩu của tài khoản đã khởi tạo. Không chia sẻ thông tin đăng nhập.

## Tình trạng chức năng

### Đã có luồng đọc/ghi SQL qua DAO

- Đăng nhập, tổng quan và đổi mật khẩu.
- Phân quyền menu theo vai trò.
- Quản lý nhân viên.
- Quản lý thuốc/vật tư.
- Đặt lịch hẹn, phân ca và xem lịch cá nhân.
- Kê đơn thuốc, ghi nhận xuất vật tư.
- Chuẩn bị phòng và điều phối lịch khám.

Một số DAO ghi đã được kiểm thử trên database tạm với dữ liệu tổng hợp, bao
gồm cả lịch hẹn, chẩn đoán, sơ đồ răng, phác đồ, đơn thuốc, xuất kho, hóa đơn
và thanh toán trả góp. Điều này xác nhận các thao tác DAO được thử có thể chạy
với schema hiện tại; không đồng nghĩa mọi màn hình giao diện đã được nối tới
các DAO đó.

### Còn dùng dữ liệu mẫu hoặc chưa nối SQL end-to-end trên giao diện

- Quản lý bệnh nhân và quản lý dịch vụ.
- Công nợ/trả góp, phác đồ điều trị và báo cáo doanh thu.
- Tra cứu hóa đơn, tra cứu hồ sơ bệnh án/EMR.
- Khám lâm sàng/sơ đồ răng và lập hóa đơn.

Một số DAO cho các nghiệp vụ trên đã tồn tại, nhưng giao diện chưa sử dụng
chúng đầy đủ; DAO dịch vụ hiện mới hỗ trợ đọc/thêm. Vì vậy, dữ liệu nhập trên
các màn hình này có thể chỉ nằm trong bộ nhớ và không được lưu vào SQL.

### Giới hạn phân quyền

RBAC hiện điều khiển các mục menu được hiển thị theo vai trò. Đây chưa phải cơ
chế phân quyền bảo mật ở tầng database hoặc kiểm tra quyền cho từng thao tác
ghi. Không dùng phân quyền giao diện thay cho bảo vệ dữ liệu thật; hãy giới
hạn quyền SQL login và chỉ dùng dữ liệu phù hợp trong môi trường phát triển.

## Kiểm thử

Các kiểm tra đã thực hiện trong quá trình phát triển:

- `build.bat` biên dịch thành công.
- 21/21 panel được kiểm tra khởi tạo thành công ở lượt smoke test.
- 28 luồng đọc DAO được chạy thành công ở lượt kiểm tra SQL trước đó.
- Một số luồng ghi DAO được thử trên database kiểm thử riêng; database tạm đã
  được xóa sau kiểm tra.

Các kiểm tra trên không mô phỏng thao tác người dùng trên toàn bộ nút và màn
hình. Dự án hiện chưa có bộ kiểm thử tự động end-to-end đầy đủ.

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

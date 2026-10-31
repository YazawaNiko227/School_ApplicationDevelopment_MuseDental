# Ứng dụng quản lý nha khoa Muse Dental

## Biên dịch và chạy ứng dụng

Chạy `build.bat` để biên dịch toàn bộ mã nguồn Java trong
`src/main/java`. Sau khi biên dịch thành công, chạy `run.bat` để mở ứng dụng.

Sau khi xác nhận hóa đơn ở màn hình thanh toán, bạn có thể xuất hóa đơn thành
tệp PDF và chọn thư mục lưu.

## Kết nối SQL Server

Ứng dụng đọc thông tin kết nối từ các biến môi trường sau:

- `MUSE_DB_USER` (bắt buộc): tên đăng nhập SQL Server.
- `MUSE_DB_PASSWORD` (bắt buộc): mật khẩu của tài khoản SQL Server.
- `MUSE_DB_URL` (không bắt buộc): chuỗi kết nối JDBC. Mặc định ứng dụng kết nối
  đến database `QuanLyPhongKhamMuse` trên `localhost:1433`.

Thư viện JDBC của Microsoft SQL Server đã có trong thư mục `lib`. Hãy cấu hình
TCP/IP cho SQL Server và tạo database bằng tệp
`database/QuanLyPhongKhamMuse_Schema .sql`.

Nếu chưa cấu hình biến môi trường, có thể thiết lập chúng trong PowerShell
trước khi chạy ứng dụng:

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
.\run.bat
```

Tài khoản SQL Server dùng cho ứng dụng cần có quyền đọc và ghi trên các bảng
ứng dụng. Tài khoản dùng để chạy các tệp tạo cấu trúc hoặc cập nhật database
cần có thêm quyền DDL.

### Lỗi SQL Server 18456: đăng nhập thất bại

Nếu ứng dụng báo `Login failed for user 'sa'` hoặc lỗi SQL Server 18456, kết
nối TCP/IP đã hoạt động nhưng SQL Server từ chối thông tin đăng nhập. Không sử
dụng mật khẩu mặc định hoặc đưa mật khẩu vào mã nguồn.

1. Trong SSMS, kết nối đến `localhost\SQLEXPRESS` bằng **Windows
   Authentication** với tài khoản Windows có quyền quản trị SQL Server.
2. Nên tạo một SQL login riêng cho ứng dụng thay vì dùng `sa`. Trong Object
   Explorer, chọn **Security > Logins > New Login...**, chọn **SQL Server
   authentication**, đặt mật khẩu mạnh và cấp quyền database tối thiểu cần
   thiết trên `QuanLyPhongKhamMuse`.
3. Nếu cần dùng `sa`, mở **Server Properties > Security**, chọn **SQL Server
   and Windows Authentication mode**, rồi khởi động lại dịch vụ
   `SQL Server (SQLEXPRESS)`. Tiếp theo, mở **Security > Logins > sa >
   Properties**, bật tài khoản trong mục **Status** và đặt lại mật khẩu trong
   mục **General**.
4. Trong cửa sổ PowerShell chạy ứng dụng, đặt `MUSE_DB_USER` và
   `MUSE_DB_PASSWORD` theo SQL login đã cấu hình, sau đó chạy lại `.\run.bat`.
   Không chia sẻ mật khẩu qua tin nhắn hoặc commit mật khẩu vào mã nguồn.

Nếu database đã tồn tại, chạy `database/Migration_RBAC.sql` một lần trong SSMS
trước khi đăng nhập. Sau khi đăng nhập database thành công, ứng dụng sẽ tạo
các quyền menu mặc định nếu chưa có.

### Khởi tạo mật khẩu cho tài khoản mẫu

Các bản ghi `TaiKhoan` trong tệp schema dùng chuỗi hash placeholder, vì vậy
chưa thể dùng để đăng nhập. Sau khi biên dịch và cấu hình kết nối database,
chạy công cụ sau trong PowerShell hoặc Command Prompt:

```powershell
java -cp "out;lib\*" util.TaoMatKhauBanDau
```

Công cụ yêu cầu nhập mật khẩu mới hai lần và tên đăng nhập. Mật khẩu không
hiển thị khi nhập. Công cụ chỉ cập nhật tài khoản mẫu còn hash placeholder;
nó không thay đổi mật khẩu của tài khoản đã được khởi tạo.

## Các màn hình sử dụng dữ liệu từ database

- **Đăng nhập:** xác thực tài khoản đang hoạt động bằng hash
  PBKDF2-HMAC-SHA256 và tải thông tin nhân viên, vai trò.
- **Tổng quan:** đọc số lịch hẹn hôm nay, hóa đơn đã thanh toán, số bệnh nhân
  và các lịch hẹn sắp tới từ SQL Server.
- **Đổi mật khẩu:** xác minh mật khẩu hiện tại và lưu mật khẩu mới đã được
  thêm salt, băm.
- **Phân quyền RBAC:** lưu quyền xem menu theo vai trò trong `QuyenVaiTro`.
  Thay đổi có hiệu lực khi người dùng đăng nhập lại.
- **Quản lý nhân viên:** đọc danh sách và thực hiện các thao tác CRUD trên
  bảng `NhanVien`.

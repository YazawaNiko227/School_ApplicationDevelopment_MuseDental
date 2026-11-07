/*
    QuanLyPhongKhamMuse - schema + dữ liệu mẫu rút gọn (mỗi INSERT tối đa 4 dòng).
    Chạy bằng SSMS hoặc sqlcmd với -f 65001 để giữ đúng tiếng Việt.
*/

SET ANSI_NULLS ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET QUOTED_IDENTIFIER ON;
SET NUMERIC_ROUNDABORT OFF;
GO

IF DB_ID(N'QuanLyPhongKhamMuse') IS NULL
    CREATE DATABASE QuanLyPhongKhamMuse;
GO

USE QuanLyPhongKhamMuse;
GO

/* ===================== 1. QUẢN TRỊ HỆ THỐNG & NHÂN SỰ ===================== */

CREATE TABLE PhongKham (
    MaPhong     NVARCHAR(20)  NOT NULL,
    TenPhong    NVARCHAR(100) NOT NULL,
    TrangThai   NVARCHAR(30)  NOT NULL DEFAULT (N'San sang'),
    CONSTRAINT PK_PhongKham PRIMARY KEY (MaPhong),
    CONSTRAINT CK_PhongKham_TrangThai CHECK (TrangThai IN (N'San sang', N'Dang kham', N'Can ve sinh', N'Bao tri'))
);

CREATE TABLE ChuyenMon (
    MaChuyenMon    NVARCHAR(20)  NOT NULL,
    TenChuyenMon   NVARCHAR(100) NOT NULL,
    CONSTRAINT PK_ChuyenMon PRIMARY KEY (MaChuyenMon),
    CONSTRAINT UQ_ChuyenMon_Ten UNIQUE (TenChuyenMon)
);

CREATE TABLE NhanVien (
    MaNhanVien           NVARCHAR(20)  NOT NULL,
    HoTen                NVARCHAR(100) NOT NULL,
    NgaySinh             DATE          NULL,
    GioiTinh             NVARCHAR(10)  NULL,
    SoDienThoai          NVARCHAR(15)  NULL,
    Email                NVARCHAR(100) NULL,
    DiaChi               NVARCHAR(200) NULL,
    NgayVaoLam           DATE          NULL,
    TrangThaiLamViec     NVARCHAR(30)  NOT NULL DEFAULT (N'Dang lam viec'),
    MaChuyenMon          NVARCHAR(20)  NULL,
    SoChungChiHanhNghe   NVARCHAR(50)  NULL,
    CONSTRAINT PK_NhanVien PRIMARY KEY (MaNhanVien),
    CONSTRAINT FK_NhanVien_ChuyenMon FOREIGN KEY (MaChuyenMon) REFERENCES ChuyenMon(MaChuyenMon)
);

CREATE TABLE VaiTro (
    MaVaiTro    NVARCHAR(20)  NOT NULL,
    TenVaiTro   NVARCHAR(50)  NOT NULL,
    CONSTRAINT PK_VaiTro PRIMARY KEY (MaVaiTro)
);

CREATE TABLE QuyenVaiTro (
    MaVaiTro       NVARCHAR(20)  NOT NULL,
    TenChucNang    NVARCHAR(150) NOT NULL,
    CONSTRAINT PK_QuyenVaiTro PRIMARY KEY (MaVaiTro, TenChucNang),
    CONSTRAINT FK_QuyenVaiTro_VaiTro FOREIGN KEY (MaVaiTro) REFERENCES VaiTro(MaVaiTro)
);

CREATE TABLE CauHinhHeThong (
    MaCauHinh    NVARCHAR(50)  NOT NULL,
    GiaTri       NVARCHAR(200) NOT NULL,
    CONSTRAINT PK_CauHinhHeThong PRIMARY KEY (MaCauHinh)
);

CREATE TABLE TaiKhoan (
    TenDangNhap        NVARCHAR(50)   NOT NULL,
    MatKhau            NVARCHAR(255)  NOT NULL,
    TrangThaiTaiKhoan  NVARCHAR(20)   NOT NULL DEFAULT (N'Kich hoat'),
    LanDangNhapCuoi    DATETIME2(0)   NULL,
    MaNhanVien         NVARCHAR(20)   NOT NULL,
    MaVaiTro           NVARCHAR(20)   NOT NULL,
    CONSTRAINT PK_TaiKhoan PRIMARY KEY (TenDangNhap),
    CONSTRAINT UQ_TaiKhoan_NhanVien UNIQUE (MaNhanVien),
    CONSTRAINT FK_TaiKhoan_NhanVien FOREIGN KEY (MaNhanVien) REFERENCES NhanVien(MaNhanVien),
    CONSTRAINT FK_TaiKhoan_VaiTro FOREIGN KEY (MaVaiTro) REFERENCES VaiTro(MaVaiTro)
);

CREATE TABLE CaTruc (
    MaCa         NVARCHAR(20) NOT NULL,
    Ngay         DATE         NOT NULL,
    GioBatDau    TIME(0)      NOT NULL,
    GioKetThuc   TIME(0)      NOT NULL,
    LoaiCa       NVARCHAR(30) NULL,
    MaNhanVien   NVARCHAR(20) NOT NULL,
    MaPhong      NVARCHAR(20) NOT NULL,
    CONSTRAINT PK_CaTruc PRIMARY KEY (MaCa),
    CONSTRAINT FK_CaTruc_NhanVien FOREIGN KEY (MaNhanVien) REFERENCES NhanVien(MaNhanVien),
    CONSTRAINT FK_CaTruc_PhongKham FOREIGN KEY (MaPhong) REFERENCES PhongKham(MaPhong),
    CONSTRAINT CK_CaTruc_GioHopLe CHECK (GioKetThuc > GioBatDau)
);

/* ===================== 2. BỆNH NHÂN & BỆNH ÁN ĐIỆN TỬ ===================== */

CREATE TABLE BenhNhan (
    MaBenhNhan       NVARCHAR(20)  NOT NULL,
    HoTen            NVARCHAR(100) NOT NULL,
    NgaySinh         DATE          NULL,
    GioiTinh         NVARCHAR(10)  NULL,
    SoDienThoai      NVARCHAR(15)  NULL,
    DiaChi           NVARCHAR(200) NULL,
    SoCCCD           NVARCHAR(20)  NULL,
    TienSuBenhLy     NVARCHAR(1000) NULL,
    ThongTinDiUng    NVARCHAR(500) NULL,
    NguoiBaoHo       NVARCHAR(100) NULL,
    CONSTRAINT PK_BenhNhan PRIMARY KEY (MaBenhNhan)
);

CREATE TABLE HoSoBenhAn (
    MaHoSo       NVARCHAR(20) NOT NULL,
    NgayTao      DATE         NOT NULL DEFAULT (CAST(GETDATE() AS DATE)),
    MaBenhNhan   NVARCHAR(20) NOT NULL,
    CONSTRAINT PK_HoSoBenhAn PRIMARY KEY (MaHoSo),
    CONSTRAINT UQ_HoSoBenhAn_BenhNhan UNIQUE (MaBenhNhan),
    CONSTRAINT FK_HoSoBenhAn_BenhNhan FOREIGN KEY (MaBenhNhan) REFERENCES BenhNhan(MaBenhNhan)
);

CREATE TABLE SoDoRang (
    MaHoSo             NVARCHAR(20) NOT NULL,
    MaRang             INT          NOT NULL,
    LoaiRang           NVARCHAR(20) NULL,
    TinhTrangBenhLy    NVARCHAR(100) NULL,
    GhiChu             NVARCHAR(500) NULL,
    CONSTRAINT PK_SoDoRang PRIMARY KEY (MaHoSo, MaRang),
    CONSTRAINT FK_SoDoRang_HoSoBenhAn FOREIGN KEY (MaHoSo) REFERENCES HoSoBenhAn(MaHoSo)
);

/* TrangThai: 'Nhap' khi bấm "Vào khám", 'Da chot' khi bấm "Hoàn tất".
   MaLichHen: lịch hẹn sinh ra lần khám này (NULL nếu khám không qua lịch hẹn).
   FK của MaLichHen được thêm bằng ALTER TABLE sau khi tạo LichHen (tránh vòng phụ thuộc). */
CREATE TABLE ChanDoan (
    MaChanDoan          NVARCHAR(20)  NOT NULL,
    NgayKham            DATE          NOT NULL,
    LyDoKham            NVARCHAR(500) NULL,
    TrieuChung          NVARCHAR(1000) NULL,
    KetLuanChanDoan     NVARCHAR(1000) NULL,
    TrangThai           NVARCHAR(20)  NOT NULL DEFAULT (N'Nhap'),
    MaLichHen           NVARCHAR(20)  NULL,
    MaHoSo              NVARCHAR(20)  NOT NULL,
    MaNhaSi             NVARCHAR(20)  NOT NULL,
    CONSTRAINT PK_ChanDoan PRIMARY KEY (MaChanDoan),
    CONSTRAINT CK_ChanDoan_TrangThai CHECK (TrangThai IN (N'Nhap', N'Da chot')),
    CONSTRAINT FK_ChanDoan_HoSoBenhAn FOREIGN KEY (MaHoSo) REFERENCES HoSoBenhAn(MaHoSo),
    CONSTRAINT FK_ChanDoan_NhanVien FOREIGN KEY (MaNhaSi) REFERENCES NhanVien(MaNhanVien)
);

/* ===================== 3. ĐƠN THUỐC ===================== */

CREATE TABLE DonThuoc (
    MaDonThuoc        NVARCHAR(20)  NOT NULL,
    NgayKe            DATE          NOT NULL,
    LoiDanChamSoc     NVARCHAR(500) NULL,
    MaChanDoan        NVARCHAR(20)  NOT NULL,
    CONSTRAINT PK_DonThuoc PRIMARY KEY (MaDonThuoc),
    CONSTRAINT UQ_DonThuoc_ChanDoan UNIQUE (MaChanDoan),
    CONSTRAINT FK_DonThuoc_ChanDoan FOREIGN KEY (MaChanDoan) REFERENCES ChanDoan(MaChanDoan)
);

/* ===================== 4. KẾ HOẠCH ĐIỀU TRỊ & DỊCH VỤ ===================== */

CREATE TABLE DichVu (
    MaDichVu           NVARCHAR(20)  NOT NULL,
    TenDichVu          NVARCHAR(150) NOT NULL,
    DonGia             FLOAT         NOT NULL CHECK (DonGia >= 0),
    ThoiGianBaoHanh    INT           NULL,
    MoTa               NVARCHAR(500) NULL,
    CONSTRAINT PK_DichVu PRIMARY KEY (MaDichVu)
);

CREATE TABLE KeHoachDieuTri (
    MaKeHoach          NVARCHAR(20)  NOT NULL,
    TenPhacDo          NVARCHAR(150) NOT NULL,
    ThoiGianDuKien     INT           NULL,
    TrangThai          NVARCHAR(30)  NOT NULL DEFAULT (N'Dang thuc hien'),
    MaChanDoan         NVARCHAR(20)  NOT NULL,
    MaBenhNhan         NVARCHAR(20)  NOT NULL,
    CONSTRAINT PK_KeHoachDieuTri PRIMARY KEY (MaKeHoach),
    CONSTRAINT UQ_KeHoachDieuTri_ChanDoan UNIQUE (MaChanDoan),
    CONSTRAINT FK_KeHoachDieuTri_ChanDoan FOREIGN KEY (MaChanDoan) REFERENCES ChanDoan(MaChanDoan),
    CONSTRAINT FK_KeHoachDieuTri_BenhNhan FOREIGN KEY (MaBenhNhan) REFERENCES BenhNhan(MaBenhNhan)
);

/* Bảng trung gian N-N: một kế hoạch gồm nhiều dịch vụ, một dịch vụ dùng lại ở nhiều kế hoạch. */
CREATE TABLE KeHoachDieuTri_DichVu (
    MaKeHoach   NVARCHAR(20) NOT NULL,
    MaDichVu    NVARCHAR(20) NOT NULL,
    SoLuong     INT          NOT NULL DEFAULT (1),
    CONSTRAINT PK_KeHoachDieuTri_DichVu PRIMARY KEY (MaKeHoach, MaDichVu),
    CONSTRAINT FK_KHDTDV_KeHoachDieuTri FOREIGN KEY (MaKeHoach) REFERENCES KeHoachDieuTri(MaKeHoach),
    CONSTRAINT FK_KHDTDV_DichVu FOREIGN KEY (MaDichVu) REFERENCES DichVu(MaDichVu)
);

/* ===================== 5. LỊCH HẸN & TIẾP ĐÓN ===================== */

CREATE TABLE LichHen (
    MaLichHen          NVARCHAR(20)  NOT NULL,
    ThoiGianHen        DATETIME2(0)  NOT NULL,
    TrangThai          NVARCHAR(30)  NOT NULL DEFAULT (N'Cho xac nhan'),
    NgayTao            DATE          NOT NULL DEFAULT (CAST(GETDATE() AS DATE)),
    ThoiGianCheckIn    DATETIME2(0)  NULL,
    MaNhanVienLap      NVARCHAR(20)  NOT NULL,
    MaNhaSiPhuTrach    NVARCHAR(20)  NOT NULL,
    MaBenhNhan         NVARCHAR(20)  NOT NULL,
    MaPhong            NVARCHAR(20)  NOT NULL,
    MaKeHoach          NVARCHAR(20)  NULL,
    CONSTRAINT PK_LichHen PRIMARY KEY (MaLichHen),
    CONSTRAINT CK_LichHen_TrangThai CHECK (TrangThai IN
        (N'Cho xac nhan', N'Da xac nhan', N'Da check-in', N'Dang kham', N'Hoan thanh', N'Huy', N'Vang')),
    CONSTRAINT FK_LichHen_NhanVienLap FOREIGN KEY (MaNhanVienLap) REFERENCES NhanVien(MaNhanVien),
    CONSTRAINT FK_LichHen_NhaSiPhuTrach FOREIGN KEY (MaNhaSiPhuTrach) REFERENCES NhanVien(MaNhanVien),
    CONSTRAINT FK_LichHen_BenhNhan FOREIGN KEY (MaBenhNhan) REFERENCES BenhNhan(MaBenhNhan),
    CONSTRAINT FK_LichHen_PhongKham FOREIGN KEY (MaPhong) REFERENCES PhongKham(MaPhong),
    CONSTRAINT FK_LichHen_KeHoachDieuTri FOREIGN KEY (MaKeHoach) REFERENCES KeHoachDieuTri(MaKeHoach)
);

ALTER TABLE ChanDoan
    ADD CONSTRAINT FK_ChanDoan_LichHen FOREIGN KEY (MaLichHen) REFERENCES LichHen(MaLichHen);

/* Một lịch hẹn chỉ sinh ra tối đa 1 lần khám (bỏ qua NULL). */
CREATE UNIQUE INDEX UQ_ChanDoan_LichHen ON ChanDoan(MaLichHen) WHERE MaLichHen IS NOT NULL;

/* ===================== 6. HÓA ĐƠN & CÔNG NỢ TRẢ GÓP ===================== */

CREATE TABLE HoaDon (
    MaHoaDon               NVARCHAR(20)  NOT NULL,
    NgayLap                DATE          NOT NULL DEFAULT (CAST(GETDATE() AS DATE)),
    TrangThaiThanhToan     NVARCHAR(30)  NOT NULL DEFAULT (N'Chua thanh toan'),
    PhuongThucThanhToan    NVARCHAR(30)  NULL,
    MaNhanVien             NVARCHAR(20)  NOT NULL,
    MaBenhNhan             NVARCHAR(20)  NOT NULL,
    MaChanDoan             NVARCHAR(20)  NULL,
    CONSTRAINT PK_HoaDon PRIMARY KEY (MaHoaDon),
    CONSTRAINT FK_HoaDon_NhanVien FOREIGN KEY (MaNhanVien) REFERENCES NhanVien(MaNhanVien),
    CONSTRAINT FK_HoaDon_BenhNhan FOREIGN KEY (MaBenhNhan) REFERENCES BenhNhan(MaBenhNhan),
    CONSTRAINT FK_HoaDon_ChanDoan FOREIGN KEY (MaChanDoan) REFERENCES ChanDoan(MaChanDoan)
);

CREATE TABLE ChiTietHoaDon (
    MaHoaDon             NVARCHAR(20)  NOT NULL,
    MaDichVu             NVARCHAR(20)  NOT NULL,
    SoLuong              INT           NOT NULL CHECK (SoLuong > 0),
    DonGiaTaiThoiDiem    FLOAT         NOT NULL CHECK (DonGiaTaiThoiDiem >= 0),
    CONSTRAINT PK_ChiTietHoaDon PRIMARY KEY (MaHoaDon, MaDichVu),
    CONSTRAINT FK_ChiTietHoaDon_HoaDon FOREIGN KEY (MaHoaDon) REFERENCES HoaDon(MaHoaDon),
    CONSTRAINT FK_ChiTietHoaDon_DichVu FOREIGN KEY (MaDichVu) REFERENCES DichVu(MaDichVu)
);

CREATE TABLE CongNoTraGop (
    MaCongNo        NVARCHAR(20)  NOT NULL,
    TongSoTien      FLOAT         NOT NULL CHECK (TongSoTien >= 0),
    SoDotConLai     INT           NOT NULL DEFAULT (0),
    MaHoaDon        NVARCHAR(20)  NOT NULL,
    CONSTRAINT PK_CongNoTraGop PRIMARY KEY (MaCongNo),
    CONSTRAINT UQ_CongNoTraGop_HoaDon UNIQUE (MaHoaDon),
    CONSTRAINT FK_CongNoTraGop_HoaDon FOREIGN KEY (MaHoaDon) REFERENCES HoaDon(MaHoaDon)
);

CREATE TABLE DotThanhToan (
    MaDot                   NVARCHAR(20)  NOT NULL,
    NgayDenHan              DATE          NOT NULL,
    SoTienDot               FLOAT         NOT NULL CHECK (SoTienDot >= 0),
    TrangThai               NVARCHAR(30)  NOT NULL DEFAULT (N'Chua den han'),
    NgayThanhToanThucTe     DATE          NULL,
    MaCongNo                NVARCHAR(20)  NOT NULL,
    CONSTRAINT PK_DotThanhToan PRIMARY KEY (MaDot),
    CONSTRAINT FK_DotThanhToan_CongNoTraGop FOREIGN KEY (MaCongNo) REFERENCES CongNoTraGop(MaCongNo)
);

/* ===================== 7. KHO VẬT TƯ Y TẾ ===================== */

/* DonGiaBan: giá bán cho bệnh nhân (0 với vật tư dùng nội bộ, không tính tiền riêng). */
CREATE TABLE VatTu (
    MaVatTu           NVARCHAR(20)  NOT NULL,
    TenVatTu          NVARCHAR(150) NOT NULL,
    LoaiVatTu         NVARCHAR(50)  NULL,
    DonViTinh         NVARCHAR(20)  NULL,
    SoLuongTon        INT           NOT NULL DEFAULT (0) CHECK (SoLuongTon >= 0),
    NguongToiThieu    INT           NOT NULL DEFAULT (0),
    HanSuDung         DATE          NULL,
    DonGiaBan         FLOAT         NOT NULL DEFAULT (0) CHECK (DonGiaBan >= 0),
    CONSTRAINT PK_VatTu PRIMARY KEY (MaVatTu)
);

CREATE TABLE ChiTietDonThuoc (
    MaDonThuoc    NVARCHAR(20) NOT NULL,
    MaVatTu       NVARCHAR(20) NOT NULL,
    LieuDung      NVARCHAR(100) NULL,
    DuongDung     NVARCHAR(50) NULL,
    SoLuong       INT NOT NULL CHECK (SoLuong > 0),
    CONSTRAINT PK_ChiTietDonThuoc PRIMARY KEY (MaDonThuoc, MaVatTu),
    CONSTRAINT FK_ChiTietDonThuoc_DonThuoc FOREIGN KEY (MaDonThuoc) REFERENCES DonThuoc(MaDonThuoc),
    CONSTRAINT FK_ChiTietDonThuoc_VatTu FOREIGN KEY (MaVatTu) REFERENCES VatTu(MaVatTu)
);

/* Tiền thuốc trên hóa đơn (dịch vụ nằm ở ChiTietHoaDon, thuốc nằm ở đây). */
CREATE TABLE ChiTietHoaDonThuoc (
    MaHoaDon             NVARCHAR(20)  NOT NULL,
    MaVatTu              NVARCHAR(20)  NOT NULL,
    SoLuong              INT           NOT NULL CHECK (SoLuong > 0),
    DonGiaTaiThoiDiem    FLOAT         NOT NULL CHECK (DonGiaTaiThoiDiem >= 0),
    CONSTRAINT PK_ChiTietHoaDonThuoc PRIMARY KEY (MaHoaDon, MaVatTu),
    CONSTRAINT FK_CTHDThuoc_HoaDon FOREIGN KEY (MaHoaDon) REFERENCES HoaDon(MaHoaDon),
    CONSTRAINT FK_CTHDThuoc_VatTu FOREIGN KEY (MaVatTu) REFERENCES VatTu(MaVatTu)
);

CREATE TABLE PhieuNhapKho (
    MaPhieuNhap    NVARCHAR(20)  NOT NULL,
    NgayNhap       DATE          NOT NULL DEFAULT (CAST(GETDATE() AS DATE)),
    NhaCungCap     NVARCHAR(150) NULL,
    MaNhanVien     NVARCHAR(20)  NOT NULL,
    CONSTRAINT PK_PhieuNhapKho PRIMARY KEY (MaPhieuNhap),
    CONSTRAINT FK_PhieuNhapKho_NhanVien FOREIGN KEY (MaNhanVien) REFERENCES NhanVien(MaNhanVien)
);

CREATE TABLE ChiTietNhapKho (
    MaPhieuNhap     NVARCHAR(20)  NOT NULL,
    MaVatTu         NVARCHAR(20)  NOT NULL,
    SoLuong         INT           NOT NULL CHECK (SoLuong > 0),
    DonGiaNhap      FLOAT         NOT NULL CHECK (DonGiaNhap >= 0),
    CONSTRAINT PK_ChiTietNhapKho PRIMARY KEY (MaPhieuNhap, MaVatTu),
    CONSTRAINT FK_ChiTietNhapKho_PhieuNhapKho FOREIGN KEY (MaPhieuNhap) REFERENCES PhieuNhapKho(MaPhieuNhap),
    CONSTRAINT FK_ChiTietNhapKho_VatTu FOREIGN KEY (MaVatTu) REFERENCES VatTu(MaVatTu)
);

CREATE TABLE PhieuXuatKho (
    MaPhieuXuat   NVARCHAR(20) NOT NULL,
    NgayXuat      DATE         NOT NULL DEFAULT (CAST(GETDATE() AS DATE)),
    MaNhanVien    NVARCHAR(20) NOT NULL,
    MaLichHen     NVARCHAR(20) NULL,
    CONSTRAINT PK_PhieuXuatKho PRIMARY KEY (MaPhieuXuat),
    CONSTRAINT FK_PhieuXuatKho_NhanVien FOREIGN KEY (MaNhanVien) REFERENCES NhanVien(MaNhanVien),
    CONSTRAINT FK_PhieuXuatKho_LichHen FOREIGN KEY (MaLichHen) REFERENCES LichHen(MaLichHen)
);

CREATE TABLE ChiTietXuatKho (
    MaPhieuXuat      NVARCHAR(20) NOT NULL,
    MaVatTu          NVARCHAR(20) NOT NULL,
    SoLuongSuDung    INT NOT NULL CHECK (SoLuongSuDung > 0),
    CONSTRAINT PK_ChiTietXuatKho PRIMARY KEY (MaPhieuXuat, MaVatTu),
    CONSTRAINT FK_ChiTietXuatKho_PhieuXuatKho FOREIGN KEY (MaPhieuXuat) REFERENCES PhieuXuatKho(MaPhieuXuat),
    CONSTRAINT FK_ChiTietXuatKho_VatTu FOREIGN KEY (MaVatTu) REFERENCES VatTu(MaVatTu)
);
GO

/* =====================================================================
   DỮ LIỆU MẪU (tiếng Việt có dấu; các mã trạng thái nội bộ giữ không dấu
   vì được dùng trong CHECK / WHERE). Thứ tự INSERT theo khóa ngoại.
   ===================================================================== */

/* ---------- Quản trị hệ thống & nhân sự ---------- */
INSERT INTO PhongKham (MaPhong, TenPhong, TrangThai) VALUES
(N'P01', N'Phòng khám số 1',  N'San sang'),
(N'P02', N'Phòng khám số 2',  N'San sang'),
(N'P03', N'Phòng tiểu phẫu',  N'Bao tri'),
(N'P04', N'Phòng X-quang',    N'Can ve sinh');

INSERT INTO PhongKham (MaPhong, TenPhong, TrangThai) VALUES
(N'P05', N'Phòng khám số 3',  N'San sang');

INSERT INTO ChuyenMon (MaChuyenMon, TenChuyenMon) VALUES
(N'CM001', N'Nha sĩ - Tổng quát'),
(N'CM002', N'Lễ tân'),
(N'CM003', N'Thu ngân'),
(N'CM004', N'Thủ kho');

INSERT INTO ChuyenMon (MaChuyenMon, TenChuyenMon) VALUES
(N'CM005', N'Y tá'),
(N'CM006', N'Quản trị hệ thống');

INSERT INTO ChuyenMon (MaChuyenMon, TenChuyenMon) VALUES
(N'CM007', N'Nha sĩ - Chỉnh nha');

INSERT INTO NhanVien (MaNhanVien, HoTen, NgaySinh, GioiTinh, SoDienThoai, Email, DiaChi, NgayVaoLam, TrangThaiLamViec, MaChuyenMon, SoChungChiHanhNghe) VALUES
(N'NV001', N'Nguyễn Văn An',  '1985-03-12', N'Nam', N'0901000001', N'an.nguyen@muse.vn',  N'Quận 1, TP.HCM',   '2020-01-05', N'Dang lam viec', N'CM001', N'CCHN-001'),
(N'NV003', N'Lê Minh Châu',   '1996-11-02', N'Nữ',  N'0901000003', N'chau.le@muse.vn',    N'Quận Bình Thạnh',  '2022-06-01', N'Dang lam viec', N'CM002', NULL),
(N'NV004', N'Phạm Quốc Dũng', '1994-05-18', N'Nam', N'0901000004', N'dung.pham@muse.vn',  N'Quận Gò Vấp',      '2022-09-15', N'Dang lam viec', N'CM003', NULL),
(N'NV005', N'Hoàng Thị Em',   '1992-12-30', N'Nữ',  N'0901000005', N'em.hoang@muse.vn',   N'Quận Tân Bình',    '2023-03-20', N'Dang lam viec', N'CM004', NULL);

INSERT INTO NhanVien (MaNhanVien, HoTen, NgaySinh, GioiTinh, SoDienThoai, Email, DiaChi, NgayVaoLam, TrangThaiLamViec, MaChuyenMon, SoChungChiHanhNghe) VALUES
(N'NV006', N'Đỗ Thị Phương',  '1997-02-14', N'Nữ',  N'0901000006', N'phuong.do@muse.vn',  N'Quận Phú Nhuận',   '2023-08-01', N'Dang lam viec', N'CM005', NULL),
(N'NV007', N'Bùi Văn Giang',  '1990-09-09', N'Nam', N'0901000007', N'giang.bui@muse.vn',  N'Quận 5, TP.HCM',   '2019-05-10', N'Dang lam viec', N'CM006', NULL);

INSERT INTO NhanVien (MaNhanVien, HoTen, NgaySinh, GioiTinh, SoDienThoai, Email, DiaChi, NgayVaoLam, TrangThaiLamViec, MaChuyenMon, SoChungChiHanhNghe) VALUES
(N'NV002', N'Trần Thị Bình',  '1988-07-22', N'Nữ',  N'0901000002', N'binh.tran@muse.vn',  N'Quận 3, TP.HCM',   '2021-02-15', N'Dang lam viec', N'CM007', N'CCHN-002'),
(N'NV008', N'Lý Minh Khoa',   '1987-10-05', N'Nam', N'0901000008', N'khoa.ly@muse.vn',    N'Quận 10, TP.HCM',  '2024-01-08', N'Dang lam viec', N'CM001', N'CCHN-003'),
(N'NV009', N'Phan Thị Mai',   '1999-04-25', N'Nữ',  N'0901000009', N'mai.phan@muse.vn',   N'TP. Thủ Đức',      '2024-03-11', N'Dang lam viec', N'CM005', NULL);

INSERT INTO VaiTro (MaVaiTro, TenVaiTro) VALUES
(N'QUAN_TRI', N'Quản trị viên'),
(N'BAC_SI',   N'Bác sĩ'),
(N'Y_TA',     N'Y tá'),
(N'LE_TAN',   N'Lễ tân');

INSERT INTO VaiTro (MaVaiTro, TenVaiTro) VALUES
(N'THU_NGAN', N'Thu ngân'),
(N'THU_KHO',  N'Nhân viên kho');

INSERT INTO QuyenVaiTro (MaVaiTro, TenChucNang) VALUES
(N'QUAN_TRI', N'Quản lý tài khoản'),
(N'QUAN_TRI', N'Phân quyền vai trò'),
(N'QUAN_TRI', N'Quản lý nhân viên & ca trực'),
(N'QUAN_TRI', N'Cấu hình hệ thống');

INSERT INTO QuyenVaiTro (MaVaiTro, TenChucNang) VALUES
(N'BAC_SI', N'Xem lịch khám cá nhân'),
(N'BAC_SI', N'Khám bệnh & chẩn đoán (EMR)'),
(N'BAC_SI', N'Kê đơn thuốc'),
(N'BAC_SI', N'Lập kế hoạch điều trị');

INSERT INTO QuyenVaiTro (MaVaiTro, TenChucNang) VALUES
(N'Y_TA', N'Xem hồ sơ bệnh nhân'),
(N'Y_TA', N'Cập nhật sơ đồ răng'),
(N'Y_TA', N'Cập nhật trạng thái phòng khám'),
(N'Y_TA', N'Chuẩn bị vật tư cho ca khám');

INSERT INTO QuyenVaiTro (MaVaiTro, TenChucNang) VALUES
(N'LE_TAN', N'Quản lý bệnh nhân'),
(N'LE_TAN', N'Đặt & điều phối lịch hẹn'),
(N'LE_TAN', N'Check-in bệnh nhân'),
(N'LE_TAN', N'Xem lịch khám bác sĩ');

INSERT INTO QuyenVaiTro (MaVaiTro, TenChucNang) VALUES
(N'THU_NGAN', N'Lập hóa đơn'),
(N'THU_NGAN', N'Thu tiền & in biên lai'),
(N'THU_NGAN', N'Quản lý công nợ trả góp'),
(N'THU_NGAN', N'Xem lịch sử thanh toán');

INSERT INTO QuyenVaiTro (MaVaiTro, TenChucNang) VALUES
(N'THU_KHO', N'Quản lý danh mục vật tư'),
(N'THU_KHO', N'Lập phiếu nhập kho'),
(N'THU_KHO', N'Lập phiếu xuất kho'),
(N'THU_KHO', N'Xem cảnh báo tồn kho');

INSERT INTO TaiKhoan (TenDangNhap, MatKhau, TrangThaiTaiKhoan, LanDangNhapCuoi, MaNhanVien, MaVaiTro) VALUES
(N'nv001', N'123456789', N'Kich hoat', '2026-11-05 08:15:00', N'NV001', N'BAC_SI'),
(N'nv003', N'123456789', N'Kich hoat', '2026-11-05 07:50:00', N'NV003', N'LE_TAN'),
(N'nv004', N'123456789', N'Kich hoat', '2026-11-05 07:55:00', N'NV004', N'THU_NGAN'),
(N'nv005', N'123456789', N'Kich hoat', NULL,                  N'NV005', N'THU_KHO');

INSERT INTO TaiKhoan (TenDangNhap, MatKhau, TrangThaiTaiKhoan, LanDangNhapCuoi, MaNhanVien, MaVaiTro) VALUES
(N'nv006', N'123456789', N'Kich hoat', NULL, N'NV006', N'Y_TA'),
(N'admin', N'123456789', N'Kich hoat', NULL, N'NV007', N'QUAN_TRI');

INSERT INTO TaiKhoan (TenDangNhap, MatKhau, TrangThaiTaiKhoan, LanDangNhapCuoi, MaNhanVien, MaVaiTro) VALUES
(N'nv002', N'123456789', N'Kich hoat', '2026-11-06 07:45:00', N'NV002', N'BAC_SI'),
(N'nv008', N'123456789', N'Kich hoat', '2026-11-06 12:55:00', N'NV008', N'BAC_SI'),
(N'nv009', N'123456789', N'Kich hoat', NULL,                  N'NV009', N'Y_TA');

INSERT INTO CaTruc (MaCa, Ngay, GioBatDau, GioKetThuc, LoaiCa, MaNhanVien, MaPhong) VALUES
(N'CA001', '2026-11-06', '08:00', '12:00', N'Ca sang',    N'NV001', N'P01'),
(N'CA002', '2026-11-06', '13:00', '17:00', N'Ca chieu',   N'NV001', N'P02'),
(N'CA003', '2026-11-06', '08:00', '17:00', N'Hanh chinh', N'NV003', N'P01'),
(N'CA004', '2026-11-09', '08:00', '12:00', N'Ca sang',    N'NV001', N'P01');

INSERT INTO CaTruc (MaCa, Ngay, GioBatDau, GioKetThuc, LoaiCa, MaNhanVien, MaPhong) VALUES
(N'CA005', '2026-11-06', '08:00', '12:00', N'Ca sang',    N'NV002', N'P02'),
(N'CA006', '2026-11-06', '13:00', '17:00', N'Ca chieu',   N'NV008', N'P01'),
(N'CA007', '2026-11-06', '08:00', '17:00', N'Hanh chinh', N'NV006', N'P01'),
(N'CA008', '2026-11-09', '13:00', '17:00', N'Ca chieu',   N'NV002', N'P02');

INSERT INTO CaTruc (MaCa, Ngay, GioBatDau, GioKetThuc, LoaiCa, MaNhanVien, MaPhong) VALUES
(N'CA009', '2026-11-09', '08:00', '17:00', N'Hanh chinh', N'NV003', N'P01'),
(N'CA010', '2026-11-09', '08:00', '17:00', N'Hanh chinh', N'NV009', N'P02');

INSERT INTO CauHinhHeThong (MaCauHinh, GiaTri) VALUES
(N'TEN_PHONG_KHAM', N'Nha khoa Muse'),
(N'DIA_CHI',        N'123 Nguyễn Văn Cừ, Quận 5, TP.HCM'),
(N'DIEN_THOAI',     N'028 3838 6868'),
(N'PHI_TAI_KHAM',   N'100000');

INSERT INTO CauHinhHeThong (MaCauHinh, GiaTri) VALUES
(N'GIO_MO_CUA',        N'08:00'),
(N'GIO_DONG_CUA',      N'17:00'),
(N'EMAIL_LIEN_HE',     N'lienhe@muse.vn'),
(N'SO_NGAY_NHAC_HEN',  N'1');

/* ---------- Bệnh nhân & EMR ---------- */
INSERT INTO BenhNhan (MaBenhNhan, HoTen, NgaySinh, GioiTinh, SoDienThoai, DiaChi, SoCCCD, TienSuBenhLy, ThongTinDiUng, NguoiBaoHo) VALUES
(N'BN001', N'Võ Thanh Hải',    '1990-04-10', N'Nam', N'0912000001', N'Quận 7, TP.HCM',    N'079090000001', N'Không',            N'Dị ứng Penicillin', NULL),
(N'BN002', N'Nguyễn Thị Lan',  '1985-09-21', N'Nữ',  N'0912000002', N'Quận 10, TP.HCM',   N'079085000002', N'Cao huyết áp',     NULL,                 NULL),
(N'BN003', N'Đặng Quốc Khánh', '1978-01-15', N'Nam', N'0912000003', N'TP. Thủ Đức',       N'079078000003', N'Tiểu đường type 2', NULL,                NULL),
(N'BN004', N'Trần Bảo Ngọc',   '2015-06-30', N'Nữ',  N'0912000004', N'Quận Phú Nhuận',    NULL,            N'Không',            NULL,                 N'Trần Văn Minh (cha)');

INSERT INTO BenhNhan (MaBenhNhan, HoTen, NgaySinh, GioiTinh, SoDienThoai, DiaChi, SoCCCD, TienSuBenhLy, ThongTinDiUng, NguoiBaoHo) VALUES
(N'BN005', N'Lê Hoàng Phúc',     '1995-02-18', N'Nam', N'0912000005', N'Quận 3, TP.HCM',   N'079095000005', N'Không',         NULL,                NULL),
(N'BN006', N'Phạm Thu Hà',      '1999-12-05', N'Nữ',  N'0912000006', N'Quận Bình Tân',    N'079099000006', N'Không',         N'Dị ứng Ibuprofen', NULL),
(N'BN007', N'Nguyễn Minh Quân', '2018-08-14', N'Nam', N'0912000007', N'Quận 7, TP.HCM',   NULL,            N'Không',         NULL,                N'Nguyễn Thị Hoa (mẹ)'),
(N'BN008', N'Trương Văn Sơn',   '1969-05-28', N'Nam', N'0912000008', N'TP. Thủ Đức',      N'079069000008', N'Bệnh tim mạch', NULL,                NULL);

INSERT INTO HoSoBenhAn (MaHoSo, NgayTao, MaBenhNhan) VALUES
(N'HS001', '2026-09-01', N'BN001'),
(N'HS002', '2026-09-05', N'BN002'),
(N'HS003', '2026-09-10', N'BN003'),
(N'HS004', '2026-10-01', N'BN004');

INSERT INTO HoSoBenhAn (MaHoSo, NgayTao, MaBenhNhan) VALUES
(N'HS005', '2026-10-20', N'BN005'),
(N'HS006', '2026-10-28', N'BN006'),
(N'HS007', '2026-11-01', N'BN007'),
(N'HS008', '2026-11-05', N'BN008');

INSERT INTO SoDoRang (MaHoSo, MaRang, LoaiRang, TinhTrangBenhLy, GhiChu) VALUES
(N'HS001', 16, N'Răng hàm', N'Sâu răng', N'Cần trám composite'),
(N'HS002', 11, N'Răng cửa', N'Mẻ răng',  N'Dự kiến bọc sứ'),
(N'HS003', 46, N'Răng hàm', N'Viêm tủy', N'Cần điều trị tủy'),
(N'HS004', 55, N'Răng sữa', N'Sâu răng', NULL);

INSERT INTO SoDoRang (MaHoSo, MaRang, LoaiRang, TinhTrangBenhLy, GhiChu) VALUES
(N'HS005', 36, N'Răng hàm', N'Sâu răng',           N'Sâu mặt nhai, cần trám composite'),
(N'HS006', 14, N'Răng hàm', N'Răng mọc lệch',      N'Chỉ định chỉnh nha'),
(N'HS007', 75, N'Răng sữa', N'Sâu răng',           N'Cần trám răng sữa'),
(N'HS008', 48, N'Răng hàm', N'Răng khôn mọc lệch', N'Chỉ định nhổ');

INSERT INTO SoDoRang (MaHoSo, MaRang, LoaiRang, TinhTrangBenhLy, GhiChu) VALUES
(N'HS001', 46, N'Răng hàm', N'Cao răng', NULL),
(N'HS003', 47, N'Răng hàm', N'Sâu răng', N'Theo dõi sau điều trị tủy');

/* MaLichHen của ChanDoan được gán bằng UPDATE sau khi chèn LichHen. */
INSERT INTO ChanDoan (MaChanDoan, NgayKham, LyDoKham, TrieuChung, KetLuanChanDoan, TrangThai, MaHoSo, MaNhaSi) VALUES
(N'CD001', '2026-11-02', N'Đau nhức răng hàm dưới', N'Đau khi ăn nhai, ê buốt', N'Sâu răng số 16, cao răng nhiều',      N'Da chot', N'HS001', N'NV001'),
(N'CD002', '2026-11-03', N'Răng cửa bị mẻ',         N'Mẻ răng, mất thẩm mỹ',    N'Mẻ răng cửa số 11, cần bọc sứ',      N'Da chot', N'HS002', N'NV001'),
(N'CD003', '2026-11-06', N'Đau răng đêm',           N'Đau âm ỉ, sưng lợi',      N'Viêm tủy răng số 46',                N'Da chot', N'HS003', N'NV001'),
(N'CD004', '2026-11-04', N'Khám định kỳ cho bé',    N'Không',                   N'Sâu răng sữa số 55 mức độ nhẹ',      N'Da chot', N'HS004', N'NV001');

INSERT INTO ChanDoan (MaChanDoan, NgayKham, LyDoKham, TrieuChung, KetLuanChanDoan, TrangThai, MaHoSo, MaNhaSi) VALUES
(N'CD005', '2026-11-04', N'Ê buốt khi ăn ngọt',   N'Ê buốt khi ăn đồ ngọt, lạnh',     N'Sâu răng số 36 mức độ vừa',                         N'Da chot', N'HS005', N'NV008'),
(N'CD006', '2026-11-05', N'Muốn niềng răng',       N'Răng chen chúc, hô nhẹ',          N'Sai khớp cắn nhẹ, cần chỉnh nha mắc cài kim loại',  N'Da chot', N'HS006', N'NV002'),
(N'CD007', '2026-11-05', N'Bé đau răng khi nhai',  N'Đau khi nhai, răng ngả đen',      N'Sâu răng sữa số 75',                                N'Da chot', N'HS007', N'NV008'),
(N'CD008', '2026-11-06', N'Đau răng khôn',         N'Sưng đau góc hàm, khó há miệng',  N'Răng khôn số 48 mọc lệch, viêm quanh thân răng',    N'Da chot', N'HS008', N'NV008');

INSERT INTO DonThuoc (MaDonThuoc, NgayKe, LoiDanChamSoc, MaChanDoan) VALUES
(N'DT001', '2026-11-02', N'Uống sau ăn, không ăn đồ cứng trong 24h', N'CD001'),
(N'DT002', '2026-11-03', N'Vệ sinh răng miệng nhẹ nhàng',            N'CD002'),
(N'DT003', '2026-11-06', N'Tái khám sau 3 ngày',                      N'CD003');

INSERT INTO DonThuoc (MaDonThuoc, NgayKe, LoiDanChamSoc, MaChanDoan) VALUES
(N'DT004', '2026-11-04', N'Không ăn đồ cứng trong 24h',                 N'CD005'),
(N'DT005', '2026-11-05', N'Cho bé uống sau ăn, súc miệng nhẹ nhàng',    N'CD007'),
(N'DT006', '2026-11-06', N'Chườm lạnh, ăn đồ mềm, tái khám sau 3 ngày', N'CD008');

/* ---------- Dịch vụ & kế hoạch điều trị ---------- */
INSERT INTO DichVu (MaDichVu, TenDichVu, DonGia, ThoiGianBaoHanh, MoTa) VALUES
(N'DV001', N'Lấy cao răng',        200000,  0,  N'Lấy cao và đánh bóng'),
(N'DV002', N'Trám răng composite', 300000,  6,  N'Trám răng sâu bằng composite'),
(N'DV004', N'Bọc răng sứ',         3500000, 60, N'Bọc răng sứ thẩm mỹ / 1 răng'),
(N'DV005', N'Điều trị tủy răng',   1200000, 12, N'Lấy tủy và trám bít ống tủy');

INSERT INTO DichVu (MaDichVu, TenDichVu, DonGia, ThoiGianBaoHanh, MoTa) VALUES
(N'DV003', N'Nhổ răng khôn',               1500000,  0,  N'Nhổ răng khôn mọc lệch / 1 răng'),
(N'DV006', N'Niềng răng mắc cài kim loại', 30000000, 24, N'Chỉnh nha trọn gói mắc cài kim loại'),
(N'DV007', N'Tẩy trắng răng',              2500000,  12, N'Tẩy trắng răng tại phòng khám'),
(N'DV008', N'Chụp X-quang răng',           250000,   0,  N'Chụp phim X-quang toàn cảnh');

INSERT INTO KeHoachDieuTri (MaKeHoach, TenPhacDo, ThoiGianDuKien, TrangThai, MaChanDoan, MaBenhNhan) VALUES
(N'KH001', N'Trám răng 16',           7,  N'Hoan thanh',     N'CD001', N'BN001'),
(N'KH002', N'Bọc sứ răng cửa 11',     30, N'Dang thuc hien', N'CD002', N'BN002'),
(N'KH003', N'Điều trị tủy răng 46',   21, N'Dang thuc hien', N'CD003', N'BN003'),
(N'KH004', N'Trám răng sữa 55',       7,  N'Dang thuc hien', N'CD004', N'BN004');

INSERT INTO KeHoachDieuTri (MaKeHoach, TenPhacDo, ThoiGianDuKien, TrangThai, MaChanDoan, MaBenhNhan) VALUES
(N'KH005', N'Trám răng 36',               7,   N'Hoan thanh',     N'CD005', N'BN005'),
(N'KH006', N'Chỉnh nha mắc cài kim loại', 540, N'Dang thuc hien', N'CD006', N'BN006'),
(N'KH007', N'Trám răng sữa 75',           7,   N'Dang thuc hien', N'CD007', N'BN007'),
(N'KH008', N'Nhổ răng khôn 48',           14,  N'Dang thuc hien', N'CD008', N'BN008');

INSERT INTO KeHoachDieuTri_DichVu (MaKeHoach, MaDichVu, SoLuong) VALUES
(N'KH001', N'DV002', 1),
(N'KH002', N'DV004', 2),
(N'KH003', N'DV005', 1),
(N'KH004', N'DV002', 1);

INSERT INTO KeHoachDieuTri_DichVu (MaKeHoach, MaDichVu, SoLuong) VALUES
(N'KH005', N'DV002', 1),
(N'KH006', N'DV006', 1),
(N'KH007', N'DV002', 1),
(N'KH008', N'DV003', 1);

INSERT INTO KeHoachDieuTri_DichVu (MaKeHoach, MaDichVu, SoLuong) VALUES
(N'KH003', N'DV008', 1),
(N'KH006', N'DV008', 1),
(N'KH008', N'DV008', 1);

/* ---------- Lịch hẹn ---------- */
INSERT INTO LichHen (MaLichHen, ThoiGianHen, TrangThai, NgayTao, ThoiGianCheckIn, MaNhanVienLap, MaNhaSiPhuTrach, MaBenhNhan, MaPhong, MaKeHoach) VALUES
(N'LH001', '2026-11-02 09:00:00', N'Hoan thanh', '2026-10-30', '2026-11-02 08:50:00', N'NV003', N'NV001', N'BN001', N'P01', N'KH001'),
(N'LH002', '2026-11-03 14:00:00', N'Hoan thanh', '2026-10-31', '2026-11-03 13:50:00', N'NV003', N'NV001', N'BN002', N'P02', N'KH002'),
(N'LH003', '2026-11-06 09:00:00', N'Hoan thanh','2026-11-03', '2026-11-06 08:50:00', N'NV003', N'NV001', N'BN003', N'P01', N'KH003'),
(N'LH004', '2026-11-06 13:00:00', N'Da check-in','2026-11-04', '2026-11-06 12:50:00', N'NV003', N'NV001', N'BN004', N'P02', N'KH004');

INSERT INTO LichHen (MaLichHen, ThoiGianHen, TrangThai, NgayTao, ThoiGianCheckIn, MaNhanVienLap, MaNhaSiPhuTrach, MaBenhNhan, MaPhong, MaKeHoach) VALUES
(N'LH005', '2026-11-04 10:00:00', N'Hoan thanh', '2026-11-02', '2026-11-04 09:50:00', N'NV003', N'NV008', N'BN005', N'P01', N'KH005'),
(N'LH006', '2026-11-05 15:00:00', N'Hoan thanh', '2026-11-03', '2026-11-05 14:45:00', N'NV003', N'NV002', N'BN006', N'P02', N'KH006'),
(N'LH007', '2026-11-05 16:00:00', N'Hoan thanh', '2026-11-04', '2026-11-05 15:50:00', N'NV003', N'NV008', N'BN007', N'P01', N'KH007'),
(N'LH008', '2026-11-06 14:00:00', N'Hoan thanh','2026-11-05', '2026-11-06 13:50:00', N'NV003', N'NV008', N'BN008', N'P01', N'KH008');

INSERT INTO LichHen (MaLichHen, ThoiGianHen, TrangThai, NgayTao, ThoiGianCheckIn, MaNhanVienLap, MaNhaSiPhuTrach, MaBenhNhan, MaPhong, MaKeHoach) VALUES
(N'LH009', '2026-11-09 09:00:00', N'Da xac nhan',  '2026-11-05', NULL, N'NV003', N'NV001', N'BN001', N'P01', NULL),
(N'LH010', '2026-11-09 14:00:00', N'Cho xac nhan', '2026-11-06', NULL, N'NV003', N'NV002', N'BN006', N'P02', N'KH006'),
(N'LH011', '2026-11-04 08:30:00', N'Huy',          '2026-11-01', NULL, N'NV003', N'NV001', N'BN003', N'P01', N'KH003'),
(N'LH012', '2026-11-05 09:30:00', N'Vang',         '2026-11-02', NULL, N'NV003', N'NV001', N'BN008', N'P01', NULL);

/* Gán lịch hẹn đã sinh ra lần khám (CD004 là lần khám không qua lịch hẹn). */
UPDATE ChanDoan
SET MaLichHen = CASE MaChanDoan
                    WHEN N'CD001' THEN N'LH001'
                    WHEN N'CD002' THEN N'LH002'
                    WHEN N'CD003' THEN N'LH003'
                    WHEN N'CD005' THEN N'LH005'
                    WHEN N'CD006' THEN N'LH006'
                    WHEN N'CD007' THEN N'LH007'
                    WHEN N'CD008' THEN N'LH008'
                END
WHERE MaChanDoan IN (N'CD001', N'CD002', N'CD003', N'CD005', N'CD006', N'CD007', N'CD008');

/* ---------- Hóa đơn & công nợ trả góp ---------- */
INSERT INTO HoaDon (MaHoaDon, NgayLap, TrangThaiThanhToan, PhuongThucThanhToan, MaNhanVien, MaBenhNhan, MaChanDoan) VALUES
(N'HD001', '2026-11-02', N'Da thanh toan',   N'Tien mat',     N'NV004', N'BN001', N'CD001'),
(N'HD002', '2026-11-03', N'Dang tra gop',    N'Tra gop',      N'NV004', N'BN002', N'CD002'),
(N'HD003', '2026-11-04', N'Da thanh toan',   N'Chuyen khoan', N'NV004', N'BN003', NULL),
(N'HD004', '2026-11-04', N'Chua thanh toan', NULL,            N'NV004', N'BN004', N'CD004');

INSERT INTO HoaDon (MaHoaDon, NgayLap, TrangThaiThanhToan, PhuongThucThanhToan, MaNhanVien, MaBenhNhan, MaChanDoan) VALUES
(N'HD005', '2026-11-04', N'Da thanh toan',   N'Tien mat',     N'NV004', N'BN005', N'CD005'),
(N'HD006', '2026-11-05', N'Dang tra gop',    N'Tra gop',      N'NV004', N'BN006', N'CD006'),
(N'HD007', '2026-11-05', N'Da thanh toan',   N'Chuyen khoan', N'NV004', N'BN007', N'CD007'),
(N'HD008', '2026-11-06', N'Chua thanh toan', NULL,            N'NV004', N'BN008', NULL);

INSERT INTO ChiTietHoaDon (MaHoaDon, MaDichVu, SoLuong, DonGiaTaiThoiDiem) VALUES
(N'HD001', N'DV002', 1, 300000),
(N'HD002', N'DV004', 2, 3500000),
(N'HD003', N'DV001', 1, 200000),
(N'HD004', N'DV002', 1, 300000);

INSERT INTO ChiTietHoaDon (MaHoaDon, MaDichVu, SoLuong, DonGiaTaiThoiDiem) VALUES
(N'HD005', N'DV002', 1, 300000),
(N'HD006', N'DV006', 1, 30000000),
(N'HD007', N'DV002', 1, 300000),
(N'HD008', N'DV008', 1, 250000);

INSERT INTO CongNoTraGop (MaCongNo, TongSoTien, SoDotConLai, MaHoaDon) VALUES
(N'CN001', 7000000, 2, N'HD002');

INSERT INTO CongNoTraGop (MaCongNo, TongSoTien, SoDotConLai, MaHoaDon) VALUES
(N'CN002', 30000000, 2, N'HD006');

INSERT INTO DotThanhToan (MaDot, NgayDenHan, SoTienDot, TrangThai, NgayThanhToanThucTe, MaCongNo) VALUES
(N'DOT001', '2026-11-03', 3000000, N'Da thanh toan', '2026-11-03', N'CN001'),
(N'DOT002', '2026-11-05', 2000000, N'Qua han',       NULL,         N'CN001'),
(N'DOT003', '2026-12-05', 2000000, N'Chua den han',  NULL,         N'CN001');

INSERT INTO DotThanhToan (MaDot, NgayDenHan, SoTienDot, TrangThai, NgayThanhToanThucTe, MaCongNo) VALUES
(N'DOT004', '2026-11-05', 10000000, N'Da thanh toan', '2026-11-05', N'CN002'),
(N'DOT005', '2026-12-05', 10000000, N'Chua den han',  NULL,         N'CN002'),
(N'DOT006', '2027-01-05', 10000000, N'Chua den han',  NULL,         N'CN002');

/* ---------- Kho vật tư ---------- */
INSERT INTO VatTu (MaVatTu, TenVatTu, LoaiVatTu, DonViTinh, SoLuongTon, NguongToiThieu, HanSuDung, DonGiaBan) VALUES
(N'VT001', N'Amoxicillin 500mg',     N'Thuốc',    N'Viên', 200, 50,  '2027-06-30', 5000),
(N'VT002', N'Paracetamol 500mg',     N'Thuốc',    N'Viên', 500, 100, '2027-12-31', 1500),
(N'VT004', N'Composite trám răng',   N'Vật liệu', N'Tuýp', 25,  5,   '2027-03-15', 0),
(N'VT005', N'Thuốc tê Lidocaine 2%', N'Thuốc',    N'Ống',  60,  20,  '2027-02-28', 0);

INSERT INTO VatTu (MaVatTu, TenVatTu, LoaiVatTu, DonViTinh, SoLuongTon, NguongToiThieu, HanSuDung, DonGiaBan) VALUES
(N'VT003', N'Ibuprofen 400mg',   N'Thuốc',    N'Viên', 300, 60, '2027-09-30', 3000),
(N'VT006', N'Găng tay y tế',     N'Vật liệu', N'Hộp',  38,  10, '2028-01-31', 0),
(N'VT007', N'Mắc cài kim loại',  N'Vật liệu', N'Bộ',   14,  5,  '2029-06-30', 0),
(N'VT008', N'Gạc vô trùng',      N'Vật liệu', N'Gói',  25,  30, '2028-05-31', 0);

INSERT INTO ChiTietDonThuoc (MaDonThuoc, MaVatTu, LieuDung, DuongDung, SoLuong) VALUES
(N'DT001', N'VT002', N'1 viên khi đau',      N'Uống', 10),
(N'DT002', N'VT002', N'1 viên khi đau',      N'Uống', 6),
(N'DT003', N'VT001', N'1 viên x 2 lần/ngày', N'Uống', 14),
(N'DT003', N'VT002', N'1 viên khi đau',      N'Uống', 10);

INSERT INTO ChiTietDonThuoc (MaDonThuoc, MaVatTu, LieuDung, DuongDung, SoLuong) VALUES
(N'DT004', N'VT003', N'1 viên x 2 lần/ngày', N'Uống', 10),
(N'DT005', N'VT002', N'1/2 viên khi đau',    N'Uống', 4),
(N'DT006', N'VT001', N'1 viên x 2 lần/ngày', N'Uống', 14),
(N'DT006', N'VT002', N'1 viên khi đau',      N'Uống', 10);

INSERT INTO ChiTietHoaDonThuoc (MaHoaDon, MaVatTu, SoLuong, DonGiaTaiThoiDiem) VALUES
(N'HD001', N'VT002', 10, 1500),
(N'HD002', N'VT002', 6,  1500);

INSERT INTO ChiTietHoaDonThuoc (MaHoaDon, MaVatTu, SoLuong, DonGiaTaiThoiDiem) VALUES
(N'HD005', N'VT003', 10, 3000),
(N'HD007', N'VT002', 4,  1500);

INSERT INTO PhieuNhapKho (MaPhieuNhap, NgayNhap, NhaCungCap, MaNhanVien) VALUES
(N'PN001', '2026-08-20', N'Công ty Dược phẩm An Khang',   N'NV005'),
(N'PN002', '2026-09-15', N'Công ty Vật tư Nha khoa Việt', N'NV005');

INSERT INTO PhieuNhapKho (MaPhieuNhap, NgayNhap, NhaCungCap, MaNhanVien) VALUES
(N'PN003', '2026-10-20', N'Công ty Dược phẩm An Khang',   N'NV005'),
(N'PN004', '2026-10-28', N'Công ty Vật tư Nha khoa Việt', N'NV005');

INSERT INTO ChiTietNhapKho (MaPhieuNhap, MaVatTu, SoLuong, DonGiaNhap) VALUES
(N'PN001', N'VT001', 200, 3500),
(N'PN001', N'VT002', 500, 800),
(N'PN001', N'VT005', 60,  12000),
(N'PN002', N'VT004', 25,  210000);

INSERT INTO ChiTietNhapKho (MaPhieuNhap, MaVatTu, SoLuong, DonGiaNhap) VALUES
(N'PN003', N'VT003', 300, 2000),
(N'PN004', N'VT006', 40,  85000),
(N'PN004', N'VT007', 15,  450000),
(N'PN004', N'VT008', 40,  12000);

INSERT INTO PhieuXuatKho (MaPhieuXuat, NgayXuat, MaNhanVien, MaLichHen) VALUES
(N'PX001', '2026-11-02', N'NV005', N'LH001'),
(N'PX002', '2026-11-03', N'NV005', N'LH002'),
(N'PX003', '2026-11-04', N'NV005', NULL);

INSERT INTO PhieuXuatKho (MaPhieuXuat, NgayXuat, MaNhanVien, MaLichHen) VALUES
(N'PX004', '2026-11-04', N'NV005', N'LH005'),
(N'PX005', '2026-11-05', N'NV005', N'LH006'),
(N'PX006', '2026-11-05', N'NV005', N'LH007'),
(N'PX007', '2026-11-06', N'NV005', N'LH008');

INSERT INTO ChiTietXuatKho (MaPhieuXuat, MaVatTu, SoLuongSuDung) VALUES
(N'PX001', N'VT004', 1),
(N'PX001', N'VT005', 1),
(N'PX002', N'VT005', 2),
(N'PX003', N'VT004', 1);

INSERT INTO ChiTietXuatKho (MaPhieuXuat, MaVatTu, SoLuongSuDung) VALUES
(N'PX004', N'VT004', 1),
(N'PX004', N'VT005', 1),
(N'PX004', N'VT008', 5),
(N'PX005', N'VT006', 1);

INSERT INTO ChiTietXuatKho (MaPhieuXuat, MaVatTu, SoLuongSuDung) VALUES
(N'PX005', N'VT007', 1),
(N'PX006', N'VT004', 1),
(N'PX006', N'VT005', 1),
(N'PX006', N'VT008', 5);

INSERT INTO ChiTietXuatKho (MaPhieuXuat, MaVatTu, SoLuongSuDung) VALUES
(N'PX007', N'VT005', 2),
(N'PX007', N'VT006', 1),
(N'PX007', N'VT008', 5);
GO
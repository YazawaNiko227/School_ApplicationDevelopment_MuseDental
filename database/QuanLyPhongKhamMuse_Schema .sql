/*
    Script tao database, schema va toan bo du lieu mau cho QuanLyPhongKhamMuse.
    Chay bang SSMS hoac sqlcmd voi -f 65001 (de giu dung tieng Viet).
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
BEGIN
    CREATE DATABASE QuanLyPhongKhamMuse;
END
GO

USE QuanLyPhongKhamMuse;
GO

/* =====================================================================
   1. PHAN HE QUAN TRI HE THONG & NHAN SU
   (UC001, UC024-UC026)
   ===================================================================== */

CREATE TABLE PhongKham (
    MaPhong     NVARCHAR(20)  NOT NULL,
    TenPhong    NVARCHAR(100) NOT NULL,
    TrangThai   NVARCHAR(30)  NOT NULL DEFAULT (N'San sang'),
    CONSTRAINT PK_PhongKham PRIMARY KEY (MaPhong),
    /* [MOI] rang buoc cac trang thai phong theo luong kham */
    CONSTRAINT CK_PhongKham_TrangThai CHECK (TrangThai IN (N'San sang', N'Dang kham', N'Can ve sinh', N'Bao tri'))
);
GO

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
    ChuyenMon            NVARCHAR(100) NULL,
    SoChungChiHanhNghe   NVARCHAR(50)  NULL,
    CONSTRAINT PK_NhanVien PRIMARY KEY (MaNhanVien)
);
GO

/* Danh sach vai tro va quyen xem menu theo vai tro. */
CREATE TABLE VaiTro (
    MaVaiTro    NVARCHAR(20)  NOT NULL,
    TenVaiTro   NVARCHAR(50)  NOT NULL,
    CONSTRAINT PK_VaiTro PRIMARY KEY (MaVaiTro)
);
GO

CREATE TABLE QuyenVaiTro (
    MaVaiTro       NVARCHAR(20)  NOT NULL,
    TenChucNang    NVARCHAR(150) NOT NULL,
    CONSTRAINT PK_QuyenVaiTro PRIMARY KEY (MaVaiTro, TenChucNang),
    CONSTRAINT FK_QuyenVaiTro_VaiTro FOREIGN KEY (MaVaiTro) REFERENCES VaiTro(MaVaiTro)
);
GO

CREATE TABLE CauHinhHeThong (
    MaCauHinh    NVARCHAR(50)  NOT NULL,
    GiaTri       NVARCHAR(200) NOT NULL,
    CONSTRAINT PK_CauHinhHeThong PRIMARY KEY (MaCauHinh)
);
GO

/* [MOI] MaVaiTro: vai tro cua tai khoan, dung de an/hien menu. */
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
GO

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
GO

/* =====================================================================
   2. PHAN HE HO SO BENH NHAN & BENH AN DIEN TU (EMR)
   (UC007, UC008, UC010-UC013)
   ===================================================================== */

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
GO

CREATE TABLE HoSoBenhAn (
    MaHoSo       NVARCHAR(20) NOT NULL,
    NgayTao      DATE         NOT NULL DEFAULT (CAST(GETDATE() AS DATE)),
    MaBenhNhan   NVARCHAR(20) NOT NULL,
    CONSTRAINT PK_HoSoBenhAn PRIMARY KEY (MaHoSo),
    CONSTRAINT UQ_HoSoBenhAn_BenhNhan UNIQUE (MaBenhNhan),
    CONSTRAINT FK_HoSoBenhAn_BenhNhan FOREIGN KEY (MaBenhNhan) REFERENCES BenhNhan(MaBenhNhan)
);
GO

CREATE TABLE SoDoRang (
    MaHoSo             NVARCHAR(20) NOT NULL,
    MaRang             INT          NOT NULL,
    LoaiRang           NVARCHAR(20) NULL,
    TinhTrangBenhLy    NVARCHAR(100) NULL,
    GhiChu             NVARCHAR(500) NULL,
    CONSTRAINT PK_SoDoRang PRIMARY KEY (MaHoSo, MaRang),
    CONSTRAINT FK_SoDoRang_HoSoBenhAn FOREIGN KEY (MaHoSo) REFERENCES HoSoBenhAn(MaHoSo)
);
GO

/* [MOI] TrangThai: 'Nhap' khi nha si bam "Vao kham", 'Da chot' khi bam "Hoan tat".
   [MOI] MaLichHen: lich hen sinh ra lan kham nay (NULL cho lan kham khong qua lich hen).
   FK cua MaLichHen duoc them bang ALTER TABLE sau khi tao bang LichHen
   (tranh vong phu thuoc ChanDoan -> LichHen -> KeHoachDieuTri -> ChanDoan). */
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
GO

/* =====================================================================
   3. PHAN HE DON THUOC DIEN TU
   (UC013)
   ===================================================================== */

CREATE TABLE DonThuoc (
    MaDonThuoc        NVARCHAR(20)  NOT NULL,
    NgayKe            DATE          NOT NULL,
    LoiDanChamSoc     NVARCHAR(500) NULL,
    MaChanDoan        NVARCHAR(20)  NOT NULL,
    CONSTRAINT PK_DonThuoc PRIMARY KEY (MaDonThuoc),
    CONSTRAINT UQ_DonThuoc_ChanDoan UNIQUE (MaChanDoan),
    CONSTRAINT FK_DonThuoc_ChanDoan FOREIGN KEY (MaChanDoan) REFERENCES ChanDoan(MaChanDoan)
);
GO

/* =====================================================================
   4. PHAN HE KE HOACH DIEU TRI & DANH MUC DICH VU
   (UC014, UC015, UC016, UC027)
   ===================================================================== */

CREATE TABLE DichVu (
    MaDichVu           NVARCHAR(20)  NOT NULL,
    TenDichVu          NVARCHAR(150) NOT NULL,
    DonGia             FLOAT         NOT NULL CHECK (DonGia >= 0),
    ThoiGianBaoHanh    INT           NULL,
    MoTa               NVARCHAR(500) NULL,
    CONSTRAINT PK_DichVu PRIMARY KEY (MaDichVu)
);
GO

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
GO

/* Bang trung gian N-N: mot ke hoach dieu tri gom nhieu dich vu,
   va moi dich vu trong danh muc co the duoc dung lai o nhieu ke hoach
   khac nhau (xem ghi chu thiet ke trong tai lieu ERD). */
CREATE TABLE KeHoachDieuTri_DichVu (
    MaKeHoach   NVARCHAR(20) NOT NULL,
    MaDichVu    NVARCHAR(20) NOT NULL,
    SoLuong     INT          NOT NULL DEFAULT (1),
    CONSTRAINT PK_KeHoachDieuTri_DichVu PRIMARY KEY (MaKeHoach, MaDichVu),
    CONSTRAINT FK_KHDTDV_KeHoachDieuTri FOREIGN KEY (MaKeHoach) REFERENCES KeHoachDieuTri(MaKeHoach),
    CONSTRAINT FK_KHDTDV_DichVu FOREIGN KEY (MaDichVu) REFERENCES DichVu(MaDichVu)
);
GO

/* =====================================================================
   5. PHAN HE LICH HEN & TIEP DON
   (UC003-UC006)
   ===================================================================== */

/* [MOI] TrangThai: them 'Da check-in', 'Dang kham', 'Huy', 'Vang' (rang buoc CHECK).
   [MOI] ThoiGianCheckIn: gio benh nhan den, de dashboard dieu phoi tinh thoi gian cho. */
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
GO

/* [MOI] Noi ChanDoan -> LichHen (them sau khi LichHen da ton tai).
   Mot lich hen chi sinh ra toi da 1 lan kham -> unique co dieu kien (bo qua NULL). */
ALTER TABLE ChanDoan
    ADD CONSTRAINT FK_ChanDoan_LichHen FOREIGN KEY (MaLichHen) REFERENCES LichHen(MaLichHen);
GO

CREATE UNIQUE INDEX UQ_ChanDoan_LichHen ON ChanDoan(MaLichHen) WHERE MaLichHen IS NOT NULL;
GO

/* =====================================================================
   6. PHAN HE TAI CHINH, HOA DON & CONG NO TRA GOP
   (UC021-UC023)
   ===================================================================== */

/* [MOI] MaChanDoan: hoa don thuoc lan kham nao (NULL cho hoa don le khong qua kham).
   Khong rang buoc unique vi mot lan kham co the lap nhieu hoa don (vd. thanh toan tach). */
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
GO

/* Khoa chinh phuc hop: 1 hoa don khong the co 2 dong chi tiet
   cung 1 dich vu -> dung luon (MaHoaDon, MaDichVu) lam khoa chinh,
   khong can sinh them khoa nhan tao MaChiTietHD. */
CREATE TABLE ChiTietHoaDon (
    MaHoaDon             NVARCHAR(20)  NOT NULL,
    MaDichVu             NVARCHAR(20)  NOT NULL,
    SoLuong              INT           NOT NULL CHECK (SoLuong > 0),
    DonGiaTaiThoiDiem    FLOAT         NOT NULL CHECK (DonGiaTaiThoiDiem >= 0),
    CONSTRAINT PK_ChiTietHoaDon PRIMARY KEY (MaHoaDon, MaDichVu),
    CONSTRAINT FK_ChiTietHoaDon_HoaDon FOREIGN KEY (MaHoaDon) REFERENCES HoaDon(MaHoaDon),
    CONSTRAINT FK_ChiTietHoaDon_DichVu FOREIGN KEY (MaDichVu) REFERENCES DichVu(MaDichVu)
);
GO

CREATE TABLE CongNoTraGop (
    MaCongNo        NVARCHAR(20)  NOT NULL,
    TongSoTien      FLOAT         NOT NULL CHECK (TongSoTien >= 0),
    SoDotConLai     INT           NOT NULL DEFAULT (0),
    MaHoaDon        NVARCHAR(20)  NOT NULL,
    CONSTRAINT PK_CongNoTraGop PRIMARY KEY (MaCongNo),
    CONSTRAINT UQ_CongNoTraGop_HoaDon UNIQUE (MaHoaDon),
    CONSTRAINT FK_CongNoTraGop_HoaDon FOREIGN KEY (MaHoaDon) REFERENCES HoaDon(MaHoaDon)
);
GO

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
GO

/* =====================================================================
   7. PHAN HE QUAN LY KHO VAT TU Y TE
   (UC019, UC020, UC028)
   ===================================================================== */

/* [MOI] DonGiaBan: gia ban cho benh nhan, dung khi tinh tien thuoc tren hoa don
   (0 voi vat tu chi dung noi bo, khong tinh tien rieng). */
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
GO

/* Khoa chinh phuc hop: 1 don thuoc khong the ke 2 dong cung 1 vat tu
   -> dung luon (MaDonThuoc, MaVatTu) lam khoa chinh. */
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
GO

/* [MOI] Tien thuoc tren hoa don (dich vu nam o ChiTietHoaDon, thuoc nam o day).
   Khoa chinh phuc hop (MaHoaDon, MaVatTu) giong cac bang chi tiet khac. */
CREATE TABLE ChiTietHoaDonThuoc (
    MaHoaDon             NVARCHAR(20)  NOT NULL,
    MaVatTu              NVARCHAR(20)  NOT NULL,
    SoLuong              INT           NOT NULL CHECK (SoLuong > 0),
    DonGiaTaiThoiDiem    FLOAT         NOT NULL CHECK (DonGiaTaiThoiDiem >= 0),
    CONSTRAINT PK_ChiTietHoaDonThuoc PRIMARY KEY (MaHoaDon, MaVatTu),
    CONSTRAINT FK_CTHDThuoc_HoaDon FOREIGN KEY (MaHoaDon) REFERENCES HoaDon(MaHoaDon),
    CONSTRAINT FK_CTHDThuoc_VatTu FOREIGN KEY (MaVatTu) REFERENCES VatTu(MaVatTu)
);
GO

CREATE TABLE PhieuNhapKho (
    MaPhieuNhap    NVARCHAR(20)  NOT NULL,
    NgayNhap       DATE          NOT NULL DEFAULT (CAST(GETDATE() AS DATE)),
    NhaCungCap     NVARCHAR(150) NULL,
    MaNhanVien     NVARCHAR(20)  NOT NULL,
    CONSTRAINT PK_PhieuNhapKho PRIMARY KEY (MaPhieuNhap),
    CONSTRAINT FK_PhieuNhapKho_NhanVien FOREIGN KEY (MaNhanVien) REFERENCES NhanVien(MaNhanVien)
);
GO

/* Khoa chinh phuc hop: 1 phieu nhap khong the co 2 dong cung 1 vat tu
   -> dung luon (MaPhieuNhap, MaVatTu) lam khoa chinh. */
CREATE TABLE ChiTietNhapKho (
    MaPhieuNhap     NVARCHAR(20)  NOT NULL,
    MaVatTu         NVARCHAR(20)  NOT NULL,
    SoLuong         INT           NOT NULL CHECK (SoLuong > 0),
    DonGiaNhap      FLOAT         NOT NULL CHECK (DonGiaNhap >= 0),
    CONSTRAINT PK_ChiTietNhapKho PRIMARY KEY (MaPhieuNhap, MaVatTu),
    CONSTRAINT FK_ChiTietNhapKho_PhieuNhapKho FOREIGN KEY (MaPhieuNhap) REFERENCES PhieuNhapKho(MaPhieuNhap),
    CONSTRAINT FK_ChiTietNhapKho_VatTu FOREIGN KEY (MaVatTu) REFERENCES VatTu(MaVatTu)
);
GO

CREATE TABLE PhieuXuatKho (
    MaPhieuXuat   NVARCHAR(20) NOT NULL,
    NgayXuat      DATE         NOT NULL DEFAULT (CAST(GETDATE() AS DATE)),
    MaNhanVien    NVARCHAR(20) NOT NULL,
    MaLichHen     NVARCHAR(20) NULL,
    CONSTRAINT PK_PhieuXuatKho PRIMARY KEY (MaPhieuXuat),
    CONSTRAINT FK_PhieuXuatKho_NhanVien FOREIGN KEY (MaNhanVien) REFERENCES NhanVien(MaNhanVien),
    CONSTRAINT FK_PhieuXuatKho_LichHen FOREIGN KEY (MaLichHen) REFERENCES LichHen(MaLichHen)
);
GO

/* Khoa chinh phuc hop: 1 phieu xuat khong the co 2 dong cung 1 vat tu
   -> dung luon (MaPhieuXuat, MaVatTu) lam khoa chinh. */
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
   DU LIEU MAU
   (chay sau khi tao xong tat ca cac bang, thu tu INSERT theo khoa ngoai)
   ===================================================================== */

/* ---------- 1. Quan tri he thong & nhan su ---------- */
INSERT INTO PhongKham (MaPhong, TenPhong, TrangThai) VALUES
(N'P01', N'Phong kham so 1', N'San sang'),
(N'P02', N'Phong kham so 2', N'San sang'),
(N'P03', N'Phong tieu phau', N'Bao tri');
GO

/* [MOI] Them NV006 (Y ta) va NV007 (Quan tri) de test du 6 vai tro. */
INSERT INTO NhanVien (MaNhanVien, HoTen, NgaySinh, GioiTinh, SoDienThoai, Email, DiaChi, NgayVaoLam, TrangThaiLamViec, ChuyenMon, SoChungChiHanhNghe) VALUES
(N'NV001', N'Nguyen Van An',   '1985-03-12', N'Nam', N'0901000001', N'an.nguyen@muse.vn',   N'Quan 1, TP.HCM',       '2020-01-05', N'Dang lam viec', N'Nha si - Nho rang',        N'CCHN-001'),
(N'NV002', N'Tran Thi Binh',   '1988-07-25', N'Nu',  N'0901000002', N'binh.tran@muse.vn',   N'Quan 3, TP.HCM',       '2021-02-10', N'Dang lam viec', N'Nha si - Chinh nha',       N'CCHN-002'),
(N'NV003', N'Le Minh Chau',    '1996-11-02', N'Nu',  N'0901000003', N'chau.le@muse.vn',     N'Quan Binh Thanh',      '2022-06-01', N'Dang lam viec', N'Le tan',                   NULL),
(N'NV004', N'Pham Quoc Dung',  '1994-05-18', N'Nam', N'0901000004', N'dung.pham@muse.vn',   N'Quan Go Vap',          '2022-09-15', N'Dang lam viec', N'Thu ngan',                 NULL),
(N'NV005', N'Hoang Thi Em',    '1992-12-30', N'Nu',  N'0901000005', N'em.hoang@muse.vn',    N'Quan Tan Binh',        '2023-03-20', N'Dang lam viec', N'Thu kho',                  NULL),
(N'NV006', N'Do Thi Phuong',   '1997-02-14', N'Nu',  N'0901000006', N'phuong.do@muse.vn',   N'Quan Phu Nhuan',       '2023-08-01', N'Dang lam viec', N'Y ta',                     NULL),
(N'NV007', N'Bui Van Giang',   '1990-09-09', N'Nam', N'0901000007', N'giang.bui@muse.vn',   N'Quan 5, TP.HCM',       '2019-05-10', N'Dang lam viec', N'Quan tri he thong',        NULL);
GO

/* [MOI] Danh sach vai tro (chen truoc TaiKhoan do khoa ngoai). */
INSERT INTO VaiTro (MaVaiTro, TenVaiTro) VALUES
(N'QUAN_TRI', N'Quan tri vien'),
(N'BAC_SI',   N'Bac si'),
(N'Y_TA',     N'Y ta'),
(N'LE_TAN',   N'Le tan'),
(N'THU_NGAN', N'Thu ngan'),
(N'THU_KHO',  N'Nhan vien kho');
GO

INSERT INTO TaiKhoan (TenDangNhap, MatKhau, TrangThaiTaiKhoan, LanDangNhapCuoi, MaNhanVien, MaVaiTro) VALUES
(N'nv001', N'123456789', N'Kich hoat', '2026-10-16 08:15:00', N'NV001', N'BAC_SI'),
(N'nv002', N'123456789', N'Kich hoat', '2026-10-16 08:30:00', N'NV002', N'BAC_SI'),
(N'nv003', N'123456789', N'Kich hoat', '2026-10-17 07:50:00', N'NV003', N'LE_TAN'),
(N'nv004', N'123456789', N'Kich hoat', '2026-10-17 07:55:00', N'NV004', N'THU_NGAN'),
(N'nv005', N'123456789', N'Kich hoat', NULL,                  N'NV005', N'THU_KHO'),
(N'nv006', N'123456789', N'Kich hoat', NULL,                  N'NV006', N'Y_TA'),
(N'admin', N'123456789', N'Kich hoat', NULL,                  N'NV007', N'QUAN_TRI');
GO

INSERT INTO CaTruc (MaCa, Ngay, GioBatDau, GioKetThuc, LoaiCa, MaNhanVien, MaPhong) VALUES
(N'CA001', '2026-10-19', '08:00', '12:00', N'Ca sang',  N'NV001', N'P01'),
(N'CA002', '2026-10-19', '13:00', '17:00', N'Ca chieu', N'NV002', N'P02'),
(N'CA003', '2026-10-19', '08:00', '17:00', N'Hanh chinh', N'NV003', N'P01'),
(N'CA004', '2026-10-20', '08:00', '12:00', N'Ca sang',  N'NV002', N'P02'),
(N'CA005', '2026-10-20', '13:00', '17:00', N'Ca chieu', N'NV001', N'P01');
GO

/* ---------- 2. Benh nhan & EMR ---------- */
INSERT INTO BenhNhan (MaBenhNhan, HoTen, NgaySinh, GioiTinh, SoDienThoai, DiaChi, SoCCCD, TienSuBenhLy, ThongTinDiUng, NguoiBaoHo) VALUES
(N'BN001', N'Vo Thanh Hai',    '1990-04-10', N'Nam', N'0912000001', N'Quan 7, TP.HCM',     N'079090000001', N'Khong',                N'Di ung Penicillin', NULL),
(N'BN002', N'Nguyen Thi Lan',  '1985-09-21', N'Nu',  N'0912000002', N'Quan 10, TP.HCM',    N'079085000002', N'Cao huyet ap',         NULL,                 NULL),
(N'BN003', N'Dang Quoc Khanh', '1978-01-15', N'Nam', N'0912000003', N'TP. Thu Duc',        N'079078000003', N'Tieu duong type 2',    NULL,                 NULL),
(N'BN004', N'Tran Bao Ngoc',   '2015-06-30', N'Nu',  N'0912000004', N'Quan Phu Nhuan',     NULL,            N'Khong',                NULL,                 N'Tran Van Minh (cha)');
GO

INSERT INTO HoSoBenhAn (MaHoSo, NgayTao, MaBenhNhan) VALUES
(N'HS001', '2026-09-01', N'BN001'),
(N'HS002', '2026-09-05', N'BN002'),
(N'HS003', '2026-09-10', N'BN003'),
(N'HS004', '2026-10-01', N'BN004');
GO

INSERT INTO SoDoRang (MaHoSo, MaRang, LoaiRang, TinhTrangBenhLy, GhiChu) VALUES
(N'HS001', 16, N'Rang hang',   N'Sau rang',        N'Can tram composite'),
(N'HS001', 26, N'Rang hang',   N'Binh thuong',     NULL),
(N'HS001', 38, N'Rang khon',   N'Moc lech',        N'Chi dinh nho'),
(N'HS002', 11, N'Rang cua',    N'Me rang',         N'Du kien boc su'),
(N'HS003', 46, N'Rang hang',   N'Viem tuy',        N'Can dieu tri tuy'),
(N'HS004', 55, N'Rang sua',    N'Sau rang',        NULL);
GO

/* MaLichHen cua ChanDoan duoc gan bang UPDATE sau khi chen LichHen (xem muc 5). */
INSERT INTO ChanDoan (MaChanDoan, NgayKham, LyDoKham, TrieuChung, KetLuanChanDoan, TrangThai, MaHoSo, MaNhaSi) VALUES
(N'CD001', '2026-09-01', N'Dau nhuc rang ham duoi', N'Dau khi an nhai, e buot', N'Sau rang so 16, cao rang nhieu',       N'Da chot', N'HS001', N'NV001'),
(N'CD002', '2026-09-05', N'Rang cua bi me',         N'Me rang, mat tham my',    N'Me rang cua so 11, can boc su',        N'Da chot', N'HS002', N'NV002'),
(N'CD003', '2026-09-10', N'Dau rang dem',           N'Dau am i, sung loi',      N'Viem tuy rang so 46',                  N'Da chot', N'HS003', N'NV001'),
(N'CD004', '2026-10-01', N'Kham dinh ky cho be',    N'Khong',                   N'Sau rang sua so 55 muc do nhe',        N'Da chot', N'HS004', N'NV002');
GO

/* ---------- 3. Don thuoc ---------- */
INSERT INTO DonThuoc (MaDonThuoc, NgayKe, LoiDanChamSoc, MaChanDoan) VALUES
(N'DT001', '2026-09-01', N'Uong sau an, khong an do cung trong 24h', N'CD001'),
(N'DT002', '2026-09-05', N'Ve sinh rang mieng nhe nhang',           N'CD002'),
(N'DT003', '2026-09-10', N'Tai kham sau 3 ngay',                     N'CD003');
GO

/* ---------- 4. Dich vu & ke hoach dieu tri ---------- */
INSERT INTO DichVu (MaDichVu, TenDichVu, DonGia, ThoiGianBaoHanh, MoTa) VALUES
(N'DV001', N'Lay cao rang',              200000,  0,  N'Lay cao va danh bong'),
(N'DV002', N'Tram rang composite',       300000,  6,  N'Tram rang sau bang composite'),
(N'DV003', N'Nho rang khon',             1500000, 0,  N'Tieu phau nho rang khon moc lech'),
(N'DV004', N'Boc rang su',               3500000, 60, N'Boc rang su tham my / 1 rang'),
(N'DV005', N'Dieu tri tuy rang',         1200000, 12, N'Lay tuy va tram bit ong tuy'),
(N'DV006', N'Tay trang rang',            2000000, 12, N'Tay trang tai phong kham');
GO

INSERT INTO KeHoachDieuTri (MaKeHoach, TenPhacDo, ThoiGianDuKien, TrangThai, MaChanDoan, MaBenhNhan) VALUES
(N'KH001', N'Lay cao & tram rang 16',     14,  N'Hoan thanh',     N'CD001', N'BN001'),
(N'KH002', N'Boc su rang cua 11',         30,  N'Dang thuc hien', N'CD002', N'BN002'),
(N'KH003', N'Dieu tri tuy rang 46',       21,  N'Dang thuc hien', N'CD003', N'BN003'),
(N'KH004', N'Tram rang sua 55',           7,   N'Dang thuc hien', N'CD004', N'BN004');
GO

INSERT INTO KeHoachDieuTri_DichVu (MaKeHoach, MaDichVu, SoLuong) VALUES
(N'KH001', N'DV001', 1),
(N'KH001', N'DV002', 1),
(N'KH002', N'DV004', 2),
(N'KH003', N'DV005', 1),
(N'KH003', N'DV002', 1),
(N'KH004', N'DV002', 1);
GO

/* ---------- 5. Lich hen ---------- */
INSERT INTO LichHen (MaLichHen, ThoiGianHen, TrangThai, NgayTao, ThoiGianCheckIn, MaNhanVienLap, MaNhaSiPhuTrach, MaBenhNhan, MaPhong, MaKeHoach) VALUES
(N'LH001', '2026-09-01 09:00:00', N'Hoan thanh',   '2026-08-28', '2026-09-01 08:50:00', N'NV003', N'NV001', N'BN001', N'P01', N'KH001'),
(N'LH002', '2026-09-05 14:00:00', N'Hoan thanh',   '2026-09-02', '2026-09-05 13:50:00', N'NV003', N'NV002', N'BN002', N'P02', N'KH002'),
(N'LH003', '2026-10-19 08:30:00', N'Da xac nhan',  '2026-10-15', NULL,                  N'NV003', N'NV001', N'BN003', N'P01', N'KH003'),
(N'LH004', '2026-10-19 14:00:00', N'Cho xac nhan', '2026-10-16', NULL,                  N'NV003', N'NV002', N'BN002', N'P02', N'KH002'),
(N'LH005', '2026-10-20 09:00:00', N'Cho xac nhan', '2026-10-17', NULL,                  N'NV003', N'NV002', N'BN004', N'P02', N'KH004');
GO

/* Gan lich hen da sinh ra lan kham (chi 2 lan kham dau co lich hen tuong ung). */
UPDATE ChanDoan SET MaLichHen = N'LH001' WHERE MaChanDoan = N'CD001';
UPDATE ChanDoan SET MaLichHen = N'LH002' WHERE MaChanDoan = N'CD002';
GO

/* ---------- 6. Hoa don & cong no tra gop ---------- */
INSERT INTO HoaDon (MaHoaDon, NgayLap, TrangThaiThanhToan, PhuongThucThanhToan, MaNhanVien, MaBenhNhan, MaChanDoan) VALUES
(N'HD001', '2026-09-01', N'Da thanh toan',   N'Tien mat',     N'NV004', N'BN001', N'CD001'),
(N'HD002', '2026-09-05', N'Da thanh toan',   N'Chuyen khoan', N'NV004', N'BN002', N'CD002'),
(N'HD003', '2026-09-10', N'Dang tra gop',    N'Tra gop',      N'NV004', N'BN002', N'CD002'),
(N'HD004', '2026-10-01', N'Chua thanh toan', NULL,            N'NV004', N'BN004', N'CD004');
GO

INSERT INTO ChiTietHoaDon (MaHoaDon, MaDichVu, SoLuong, DonGiaTaiThoiDiem) VALUES
(N'HD001', N'DV001', 1, 200000),
(N'HD001', N'DV002', 1, 300000),
(N'HD002', N'DV006', 1, 2000000),
(N'HD003', N'DV004', 2, 3500000),
(N'HD004', N'DV002', 1, 300000);
GO

INSERT INTO CongNoTraGop (MaCongNo, TongSoTien, SoDotConLai, MaHoaDon) VALUES
(N'CN001', 7000000, 2, N'HD003');
GO

INSERT INTO DotThanhToan (MaDot, NgayDenHan, SoTienDot, TrangThai, NgayThanhToanThucTe, MaCongNo) VALUES
(N'DOT001', '2026-09-10', 3000000, N'Da thanh toan',  '2026-09-10', N'CN001'),
(N'DOT002', '2026-10-10', 2000000, N'Qua han',        NULL,         N'CN001'),
(N'DOT003', '2026-11-10', 2000000, N'Chua den han',   NULL,         N'CN001');
GO

/* ---------- 7. Kho vat tu ---------- */
INSERT INTO VatTu (MaVatTu, TenVatTu, LoaiVatTu, DonViTinh, SoLuongTon, NguongToiThieu, HanSuDung, DonGiaBan) VALUES
(N'VT001', N'Amoxicillin 500mg',        N'Thuoc',    N'Vien', 200, 50,  '2027-06-30', 5000),
(N'VT002', N'Paracetamol 500mg',        N'Thuoc',    N'Vien', 500, 100, '2027-12-31', 1500),
(N'VT003', N'Gang tay y te',            N'Tieu hao', N'Hop',  40,  10,  '2028-01-01', 0),
(N'VT004', N'Composite tram rang',      N'Vat lieu', N'Tuyp', 25,  5,   '2027-03-15', 0),
(N'VT005', N'Thuoc te Lidocaine 2%',    N'Thuoc',    N'Ong',  60,  20,  '2027-02-28', 0),
(N'VT006', N'Mu su (rang su)',          N'Vat lieu', N'Cai',  15,  5,   NULL,         0);
GO

INSERT INTO ChiTietDonThuoc (MaDonThuoc, MaVatTu, LieuDung, DuongDung, SoLuong) VALUES
(N'DT001', N'VT001', N'1 vien x 3 lan/ngay', N'Uong', 15),
(N'DT001', N'VT002', N'1 vien khi dau',      N'Uong', 10),
(N'DT002', N'VT002', N'1 vien khi dau',      N'Uong', 6),
(N'DT003', N'VT001', N'1 vien x 2 lan/ngay', N'Uong', 14),
(N'DT003', N'VT002', N'1 vien khi dau',      N'Uong', 10);
GO

/* Tien thuoc cua HD001 theo don thuoc DT001 (mau). */
INSERT INTO ChiTietHoaDonThuoc (MaHoaDon, MaVatTu, SoLuong, DonGiaTaiThoiDiem) VALUES
(N'HD001', N'VT001', 15, 5000),
(N'HD001', N'VT002', 10, 1500);
GO

INSERT INTO PhieuNhapKho (MaPhieuNhap, NgayNhap, NhaCungCap, MaNhanVien) VALUES
(N'PN001', '2026-08-20', N'Cong ty Duoc pham An Khang', N'NV005'),
(N'PN002', '2026-09-15', N'Cong ty Vat tu Nha khoa Viet', N'NV005');
GO

INSERT INTO ChiTietNhapKho (MaPhieuNhap, MaVatTu, SoLuong, DonGiaNhap) VALUES
(N'PN001', N'VT001', 200, 3500),
(N'PN001', N'VT002', 500, 800),
(N'PN001', N'VT005', 60,  12000),
(N'PN002', N'VT003', 40,  85000),
(N'PN002', N'VT004', 25,  210000),
(N'PN002', N'VT006', 15,  450000);
GO

INSERT INTO PhieuXuatKho (MaPhieuXuat, NgayXuat, MaNhanVien, MaLichHen) VALUES
(N'PX001', '2026-09-01', N'NV005', N'LH001'),
(N'PX002', '2026-09-05', N'NV005', N'LH002'),
(N'PX003', '2026-09-30', N'NV005', NULL);
GO

INSERT INTO ChiTietXuatKho (MaPhieuXuat, MaVatTu, SoLuongSuDung) VALUES
(N'PX001', N'VT004', 1),
(N'PX001', N'VT005', 1),
(N'PX002', N'VT006', 2),
(N'PX002', N'VT005', 2),
(N'PX003', N'VT003', 2);
GO


/* =====================================================================
   DU LIEU MAU MO RONG (SEED_FULL_V1)
   ===================================================================== */
SET NOCOUNT ON;
SET XACT_ABORT ON;
SET ANSI_NULLS ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET QUOTED_IDENTIFIER ON;
SET NUMERIC_ROUNDABORT OFF;

IF EXISTS (SELECT 1 FROM CauHinhHeThong WHERE MaCauHinh = N'SEED_FULL_V1')
BEGIN
    PRINT N'Du lieu mo rong SEED_FULL_V1 da duoc nap truoc do, bo qua.';
END
ELSE
BEGIN
    BEGIN TRY
        BEGIN TRAN;

        /* =================================================================
           1. PHONG KHAM
           ================================================================= */
        INSERT INTO PhongKham (MaPhong, TenPhong, TrangThai) VALUES
        (N'P04', N'Phong kham so 3',  N'San sang'),
        (N'P05', N'Phong chinh nha',  N'San sang'),
        (N'P06', N'Phong X-quang',    N'San sang'),
        (N'P07', N'Phong tieu phau 2', N'Bao tri');

        /* =================================================================
           2. NHAN VIEN & TAI KHOAN
           ================================================================= */
        INSERT INTO NhanVien (MaNhanVien, HoTen, NgaySinh, GioiTinh, SoDienThoai, Email, DiaChi, NgayVaoLam, TrangThaiLamViec, ChuyenMon, SoChungChiHanhNghe) VALUES
        (N'NV009', N'Ngo Quang Huy',        '1982-02-14', N'Nam', N'0902000008', N'huy.ngo@muse.vn',      N'Quan 3, TP.HCM',       '2019-06-01', N'Dang lam viec', N'Nha si - Phuc hinh',    N'CCHN-008'),
        (N'NV010', N'Dang Thu Ha',          '1990-08-19', N'Nu',  N'0902000009', N'ha.dang@muse.vn',      N'Quan 7, TP.HCM',       '2020-09-15', N'Dang lam viec', N'Nha si - Nho rang',     N'CCHN-009'),
        (N'NV011', N'Phan Minh Tuan',       '1986-12-05', N'Nam', N'0902000010', N'tuan.phan@muse.vn',    N'Quan Tan Binh',        '2018-03-20', N'Dang lam viec', N'Nha si - Chinh nha',    N'CCHN-010'),
        (N'NV012', N'Nguyen Thi Mai',       '1999-04-22', N'Nu',  N'0902000011', N'mai.nguyen@muse.vn',   N'Quan Binh Thanh',      '2023-01-10', N'Dang lam viec', N'Y ta',                  NULL),
        (N'NV013', N'Tran Thi Thu',         '1998-11-30', N'Nu',  N'0902000012', N'thu.tran@muse.vn',     N'Quan Go Vap',          '2023-05-05', N'Dang lam viec', N'Y ta',                  NULL),
        (N'NV014', N'Vo Thi Kim',           '2000-06-18', N'Nu',  N'0902000013', N'kim.vo@muse.vn',       N'Quan 10, TP.HCM',      '2024-02-19', N'Dang lam viec', N'Le tan',                NULL),
        (N'NV015', N'Do Minh Quan',         '1997-09-09', N'Nam', N'0902000014', N'quan.do@muse.vn',      N'TP. Thu Duc',          '2022-11-11', N'Dang lam viec', N'Le tan',                NULL),
        (N'NV016', N'Huynh Thi Ngoc',       '1995-03-03', N'Nu',  N'0902000015', N'ngoc.huynh@muse.vn',   N'Quan 1, TP.HCM',       '2021-07-01', N'Dang lam viec', N'Thu ngan',              NULL),
        (N'NV017', N'Nguyen Van Tung',      '1993-10-27', N'Nam', N'0902000016', N'tung.nguyen@muse.vn',  N'Quan 12, TP.HCM',      '2020-12-12', N'Dang lam viec', N'Thu kho',               NULL),
        (N'NV018', N'Le Thi Hoa',           '1991-01-08', N'Nu',  N'0902000017', N'hoa.le@muse.vn',       N'Quan 4, TP.HCM',       '2022-04-04', N'Dang lam viec', N'Thu kho',               NULL),
        (N'NV019', N'Bui Thanh Nam',        '1989-05-16', N'Nam', N'0902000018', N'nam.bui@muse.vn',      N'Quan 5, TP.HCM',       '2017-08-08', N'Dang lam viec', N'Quan tri he thong',     NULL),
        (N'NV020', N'Hoang Gia Bao',        '1994-07-07', N'Nam', N'0902000019', N'bao.hoang@muse.vn',    N'Quan Tan Phu',         '2024-09-01', N'Dang lam viec', N'Nha si - Tong quat',    N'CCHN-019'),
        (N'NV021', N'Truong My Linh',       '1996-02-25', N'Nu',  N'0902000020', N'linh.truong@muse.vn',  N'Quan Phu Nhuan',       '2025-01-15', N'Dang lam viec', N'Le tan',                NULL);

        INSERT INTO TaiKhoan (TenDangNhap, MatKhau, TrangThaiTaiKhoan, LanDangNhapCuoi, MaNhanVien, MaVaiTro) VALUES
        (N'nv009', N'123456789', N'Kich hoat', '2026-11-01 07:45:00', N'NV009', N'BAC_SI'),
        (N'nv010', N'123456789', N'Kich hoat', '2026-11-01 08:00:00', N'NV010', N'BAC_SI'),
        (N'nv011', N'123456789', N'Kich hoat', '2026-11-01 08:10:00', N'NV011', N'BAC_SI'),
        (N'nv012', N'123456789', N'Kich hoat', '2026-11-02 07:30:00', N'NV012', N'Y_TA'),
        (N'nv013', N'123456789', N'Kich hoat', '2026-11-02 07:35:00', N'NV013', N'Y_TA'),
        (N'nv014', N'123456789', N'Kich hoat', '2026-11-03 07:20:00', N'NV014', N'LE_TAN'),
        (N'nv015', N'123456789', N'Kich hoat', '2026-11-03 07:25:00', N'NV015', N'LE_TAN'),
        (N'nv016', N'123456789', N'Kich hoat', '2026-11-03 07:40:00', N'NV016', N'THU_NGAN'),
        (N'nv017', N'123456789', N'Kich hoat', '2026-11-01 08:20:00', N'NV017', N'THU_KHO'),
        (N'nv018', N'123456789', N'Kich hoat', '2026-11-02 08:00:00', N'NV018', N'THU_KHO'),
        (N'nv019', N'123456789', N'Kich hoat', '2026-11-01 09:00:00', N'NV019', N'QUAN_TRI'),
        (N'nv020', N'123456789', N'Kich hoat', '2026-11-02 08:30:00', N'NV020', N'BAC_SI'),
        (N'nv021', N'123456789', N'Kich hoat', '2026-11-03 07:50:00', N'NV021', N'LE_TAN');

        /* =================================================================
           3. CA TRUC (tuan 02/11 - 15/11/2026)
           ================================================================= */
        INSERT INTO CaTruc (MaCa, Ngay, GioBatDau, GioKetThuc, LoaiCa, MaNhanVien, MaPhong) VALUES
        (N'CA006', '2026-11-02', '08:00', '17:00', N'Hanh chinh', N'NV007', N'P01'),
        (N'CA007', '2026-11-02', '08:00', '12:00', N'Ca sang',    N'NV001', N'P02'),
        (N'CA008', '2026-11-02', '13:00', '17:00', N'Ca chieu',   N'NV009', N'P04'),
        (N'CA009', '2026-11-03', '08:00', '17:00', N'Hanh chinh', N'NV007', N'P01'),
        (N'CA010', '2026-11-03', '08:00', '12:00', N'Ca sang',    N'NV009', N'P01'),
        (N'CA011', '2026-11-03', '13:00', '17:00', N'Ca chieu',   N'NV010', N'P02'),
        (N'CA012', '2026-11-03', '08:00', '17:00', N'Hanh chinh', N'NV014', N'P04'),
        (N'CA013', '2026-11-03', '08:00', '17:00', N'Hanh chinh', N'NV016', N'P05'),
        (N'CA014', '2026-11-04', '08:00', '12:00', N'Ca sang',    N'NV011', N'P05'),
        (N'CA015', '2026-11-04', '13:00', '17:00', N'Ca chieu',   N'NV020', N'P01'),
        (N'CA016', '2026-11-04', '08:00', '17:00', N'Hanh chinh', N'NV012', N'P02'),
        (N'CA017', '2026-11-04', '08:00', '17:00', N'Hanh chinh', N'NV015', N'P04'),
        (N'CA018', '2026-11-05', '08:00', '12:00', N'Ca sang',    N'NV001', N'P02'),
        (N'CA019', '2026-11-05', '13:00', '17:00', N'Ca chieu',   N'NV009', N'P04'),
        (N'CA020', '2026-11-05', '08:00', '17:00', N'Hanh chinh', N'NV013', N'P01'),
        (N'CA021', '2026-11-05', '08:00', '17:00', N'Hanh chinh', N'NV017', N'P06'),
        (N'CA022', '2026-11-06', '08:00', '12:00', N'Ca sang',    N'NV010', N'P02'),
        (N'CA023', '2026-11-06', '13:00', '17:00', N'Ca chieu',   N'NV011', N'P05'),
        (N'CA024', '2026-11-06', '08:00', '17:00', N'Hanh chinh', N'NV021', N'P04'),
        (N'CA025', '2026-11-07', '08:00', '12:00', N'Ca sang',    N'NV007', N'P01'),
        (N'CA026', '2026-11-07', '13:00', '17:00', N'Ca chieu',   N'NV020', N'P04'),
        (N'CA027', '2026-11-08', '08:00', '17:00', N'Truc cuoi tuan', N'NV009', N'P01'),
        (N'CA028', '2026-11-09', '08:00', '12:00', N'Ca sang',    N'NV011', N'P05'),
        (N'CA029', '2026-11-09', '13:00', '17:00', N'Ca chieu',   N'NV001', N'P02'),
        (N'CA030', '2026-11-10', '08:00', '12:00', N'Ca sang',    N'NV009', N'P01'),
        (N'CA031', '2026-11-10', '13:00', '17:00', N'Ca chieu',   N'NV010', N'P02'),
        (N'CA032', '2026-11-11', '08:00', '17:00', N'Hanh chinh', N'NV011', N'P05'),
        (N'CA033', '2026-11-12', '08:00', '17:00', N'Hanh chinh', N'NV020', N'P04'),
        (N'CA034', '2026-11-13', '08:00', '12:00', N'Ca sang',    N'NV009', N'P01'),
        (N'CA035', '2026-11-13', '13:00', '17:00', N'Ca chieu',   N'NV001', N'P02'),
        (N'CA036', '2026-11-14', '08:00', '12:00', N'Ca sang',    N'NV010', N'P02'),
        (N'CA037', '2026-11-14', '13:00', '17:00', N'Ca chieu',   N'NV011', N'P05'),
        (N'CA038', '2026-11-15', '08:00', '17:00', N'Truc cuoi tuan', N'NV020', N'P04'),
        (N'CA039', '2026-11-15', '08:00', '17:00', N'Truc cuoi tuan', N'NV009', N'P01');

        /* =================================================================
           4. BENH NHAN & HO SO BENH AN
           ================================================================= */
        INSERT INTO BenhNhan (MaBenhNhan, HoTen, NgaySinh, GioiTinh, SoDienThoai, DiaChi, SoCCCD, TienSuBenhLy, ThongTinDiUng, NguoiBaoHo) VALUES
        (N'BN005', N'Le Van Hung',          '1965-03-14', N'Nam', N'0912000005', N'Quan 3, TP.HCM',       N'079065000005', N'Cao huyet ap, thoai hoa cot song',       N'Di ung Aspirin',            NULL),
        (N'BN006', N'Pham Thi Hoa',         '1972-07-08', N'Nu',  N'0912000006', N'Quan 5, TP.HCM',       N'079072000006', N'Viem da day',                            N'Khong',                     NULL),
        (N'BN007', N'Nguyen Duc Long',      '1988-11-23', N'Nam', N'0912000007', N'Quan Binh Thanh',      N'079088000007', N'Khoe manh',                              N'Khong',                     NULL),
        (N'BN008', N'Tran Ngoc Diep',       '1995-02-17', N'Nu',  N'0912000008', N'Quan 7, TP.HCM',       N'079095000008', N'Khoe manh',                              N'Di ung hai phan hoa',       NULL),
        (N'BN009', N'Hoang Minh Tri',       '2001-09-05', N'Nam', N'0912000009', N'TP. Thu Duc',          N'079001000009', N'Khoe manh',                              N'Khong',                     NULL),
        (N'BN010', N'Vo Thi Thanh',         '1958-12-01', N'Nu',  N'0912000010', N'Quan 10, TP.HCM',      N'079058000010', N'Tieu duong type 2',                      N'Di ung Sulfonamide',        NULL),
        (N'BN011', N'Dang Van Tai',         '1983-04-29', N'Nam', N'0912000011', N'Quan Tan Binh',        N'079083000011', N'Khoe manh',                              N'Khong',                     NULL),
        (N'BN012', N'Bui Thi Lan',          '1992-06-11', N'Nu',  N'0912000012', N'Quan Go Vap',          N'079092000012', N'Thieu mau nhe',                          N'Khong',                     NULL),
        (N'BN013', N'Ngo Quoc Bao',         '2012-08-20', N'Nam', N'0912000013', N'Quan 1, TP.HCM',       NULL,            N'Khoe manh',                              N'Khong',                     N'Ngo Van Thanh (bo)'),
        (N'BN014', N'Huynh Thi Ngoc Anh',   '2008-01-30', N'Nu',  N'0912000014', N'Nha Be, TP.HCM',       NULL,            N'Viem phe quan man',                      N'Di ung bui nha',            N'Huynh Van Duc (bo)'),
        (N'BN015', N'Phan Thanh Son',       '1976-10-10', N'Nam', N'0912000015', N'Quan 4, TP.HCM',       N'079076000015', N'Khoe manh',                              N'Khong',                     NULL),
        (N'BN016', N'Le Thi Kim Ngan',      '1999-03-03', N'Nu',  N'0912000016', N'Quan Binh Tan',        N'079099000016', N'Khoe manh',                              N'Di ung Penicillin',         NULL),
        (N'BN017', N'Truong Van Phuc',      '1968-05-19', N'Nam', N'0912000017', N'Quan 6, TP.HCM',       N'079068000017', N'Roi loan lo au',                         N'Khong',                     NULL),
        (N'BN018', N'Nguyen Thi Thu Thao',  '1987-09-27', N'Nu',  N'0912000018', N'Quan 8, TP.HCM',       N'079087000018', N'Khoe manh',                              N'Khong',                     NULL),
        (N'BN019', N'Ly Minh Khang',        '2016-11-11', N'Nam', N'0912000019', N'Quan 11, TP.HCM',      NULL,            N'Khoe manh',                              N'Khong',                     N'Ly Van Hung (bo)'),
        (N'BN020', N'Dao Thi Bich',         '1980-02-02', N'Nu',  N'0912000020', N'Quan Tan Phu',         N'079080000020', N'Viem khop dang thap',                    N'Di ung NSAID',              NULL),
        (N'BN021', N'Mai Van Duong',        '1991-07-15', N'Nam', N'0912000021', N'TP. Thu Duc',          N'079091000021', N'Khoe manh',                              N'Khong',                     NULL),
        (N'BN022', N'Ton Nu Bao Tram',      '1997-12-24', N'Nu',  N'0912000022', N'Quan 3, TP.HCM',       N'079097000022', N'Khoe manh',                              N'Khong',                     NULL),
        (N'BN023', N'Dinh Cong Thanh',      '1952-04-05', N'Nam', N'0912000023', N'Quan 5, TP.HCM',       N'079052000023', N'Benh tim mach',                          N'Khong',                     NULL),
        (N'BN024', N'Pham Thi My Duyen',    '2005-06-21', N'Nu',  N'0912000024', N'Binh Chanh, TP.HCM',   NULL,            N'Khoe manh',                              N'Khong',                     N'Pham Van Luong (bo)'),
        (N'BN025', N'Nguyen Hoang Phuc',    '1985-08-08', N'Nam', N'0912000025', N'Quan 9, TP.HCM',       N'079085000025', N'Gan nhiem mo',                           N'Khong',                     NULL),
        (N'BN026', N'Tran Thi Xuan',        '1970-01-01', N'Nu',  N'0912000026', N'Quan 2, TP.HCM',       N'079070000026', N'Loang xuong',                            N'Di ung Calcium',            NULL),
        (N'BN027', N'Le Quang Vinh',        '1993-05-30', N'Nam', N'0912000027', N'Quan 12, TP.HCM',      N'079093000027', N'Khoe manh',                              N'Khong',                     NULL),
        (N'BN028', N'Vo Ngoc Mai',          '2020-10-12', N'Nu',  N'0912000028', N'Quan Binh Thanh',      NULL,            N'Khoe manh',                              N'Khong',                     N'Vo Van Hau (bo)');

        INSERT INTO HoSoBenhAn (MaHoSo, NgayTao, MaBenhNhan) VALUES
        (N'HS005', '2026-10-25', N'BN005'),
        (N'HS006', '2026-10-15', N'BN006'),
        (N'HS007', '2026-10-28', N'BN007'),
        (N'HS008', '2026-10-15', N'BN008'),
        (N'HS009', '2026-10-18', N'BN009'),
        (N'HS010', '2026-10-19', N'BN010'),
        (N'HS011', '2026-10-20', N'BN011'),
        (N'HS012', '2026-10-21', N'BN012'),
        (N'HS013', '2026-10-22', N'BN013'),
        (N'HS014', '2026-10-23', N'BN014'),
        (N'HS015', '2026-10-24', N'BN015'),
        (N'HS016', '2026-10-15', N'BN016'),
        (N'HS017', '2026-10-16', N'BN017'),
        (N'HS018', '2026-10-17', N'BN018'),
        (N'HS019', '2026-10-18', N'BN019'),
        (N'HS020', '2026-10-19', N'BN020'),
        (N'HS021', '2026-10-21', N'BN021'),
        (N'HS022', '2026-10-22', N'BN022'),
        (N'HS023', '2026-10-23', N'BN023'),
        (N'HS024', '2026-10-24', N'BN024'),
        (N'HS025', '2026-10-26', N'BN025'),
        (N'HS026', '2026-10-27', N'BN026'),
        (N'HS027', '2026-10-29', N'BN027'),
        (N'HS028', '2026-10-30', N'BN028');

        /* =================================================================
           5. SO DO RANG
           ================================================================= */
        INSERT INTO SoDoRang (MaHoSo, MaRang, LoaiRang, TinhTrangBenhLy, GhiChu) VALUES
        (N'HS005', 16, N'Rang hang', N'Sau rang',          N'Can tram composite'),
        (N'HS005', 17, N'Rang hang', N'Binh thuong',       NULL),
        (N'HS005', 38, N'Rang khon', N'Moc lech',          N'Chi dinh nho rang khon'),
        (N'HS005', 46, N'Rang hang', N'Cao rang',          N'Can lay cao'),
        (N'HS006', 11, N'Rang cua',  N'Viem loi',          N'Can dieu tri nha chu'),
        (N'HS006', 26, N'Rang hang', N'Viem loi',          N'Can ve sinh rang mieng'),
        (N'HS007', 11, N'Rang cua',  N'Me rang',           N'Du kien boc su'),
        (N'HS007', 21, N'Rang cua',  N'Binh thuong',       NULL),
        (N'HS007', 36, N'Rang hang', N'Sau rang',          N'Can tram rang'),
        (N'HS008', 11, N'Rang cua',  N'Me rang',           N'Du kien tram rang'),
        (N'HS008', 12, N'Rang cua',  N'Binh thuong',       NULL),
        (N'HS008', 46, N'Rang hang', N'Viem tuy',          N'Can dieu tri tuy'),
        (N'HS009', 16, N'Rang hang', N'Cao rang',          N'Can lay cao'),
        (N'HS009', 26, N'Rang hang', N'Cao rang',          N'Can lay cao'),
        (N'HS010', 24, N'Rang hang', N'Mat rang',          N'Can phuc hinh'),
        (N'HS010', 25, N'Rang hang', N'Binh thuong',       NULL),
        (N'HS010', 36, N'Rang hang', N'Sau rang',          N'Can tram rang'),
        (N'HS011', 11, N'Rang cua',  N'Chen chuc',         N'Chi dinh chinh nha'),
        (N'HS011', 21, N'Rang cua',  N'Chen chuc',         N'Chi dinh chinh nha'),
        (N'HS011', 31, N'Rang cua',  N'Binh thuong',       NULL),
        (N'HS012', 46, N'Rang hang', N'Viem tuy',          N'Can dieu tri tuy'),
        (N'HS012', 47, N'Rang hang', N'Binh thuong',       NULL),
        (N'HS012', 38, N'Rang khon', N'Moc lech',          N'Can nho'),
        (N'HS013', 55, N'Rang sua',  N'Sau rang',          N'Can tram rang'),
        (N'HS013', 54, N'Rang sua',  N'Binh thuong',       NULL),
        (N'HS014', 26, N'Rang hang', N'Viem loi',          N'Can dieu tri nha chu'),
        (N'HS014', 36, N'Rang hang', N'Cao rang',          N'Can lay cao'),
        (N'HS015', 18, N'Rang khon', N'Moc lech',          N'Can nho rang khon'),
        (N'HS015', 17, N'Rang hang', N'Binh thuong',       NULL),
        (N'HS015', 46, N'Rang hang', N'Cao rang',          N'Can lay cao'),
        (N'HS016', 48, N'Rang khon', N'Moc lech',          N'Can nho rang khon'),
        (N'HS016', 47, N'Rang hang', N'Binh thuong',       NULL),
        (N'HS017', 11, N'Rang cua',  N'Rang nhiem mau',    N'Can tay trang'),
        (N'HS017', 21, N'Rang cua',  N'Rang nhiem mau',    N'Can tay trang'),
        (N'HS017', 33, N'Rang nanh', N'Binh thuong',       NULL),
        (N'HS018', 36, N'Rang hang', N'Mat rang',          N'Can cay ghep Implant'),
        (N'HS018', 35, N'Rang hang', N'Binh thuong',       NULL),
        (N'HS018', 37, N'Rang hang', N'Binh thuong',       NULL),
        (N'HS019', 54, N'Rang sua',  N'Sau rang',          N'Can tram rang'),
        (N'HS019', 55, N'Rang sua',  N'Binh thuong',       NULL),
        (N'HS019', 64, N'Rang sua',  N'Sau rang',          N'Can tram rang'),
        (N'HS020', 11, N'Rang cua',  N'Mat rang',          N'Can phuc hinh'),
        (N'HS020', 12, N'Rang cua',  N'Binh thuong',       NULL),
        (N'HS020', 21, N'Rang cua',  N'Binh thuong',       NULL),
        (N'HS021', 16, N'Rang hang', N'Cao rang',          N'Can lay cao'),
        (N'HS021', 26, N'Rang hang', N'Cao rang',          N'Can lay cao'),
        (N'HS021', 36, N'Rang hang', N'Viem loi',          N'Can dieu tri nha chu'),
        (N'HS022', 26, N'Rang hang', N'Sau rang',          N'Can tram rang'),
        (N'HS022', 27, N'Rang hang', N'Binh thuong',       NULL),
        (N'HS022', 37, N'Rang hang', N'Cao rang',          N'Can lay cao'),
        (N'HS023', 16, N'Rang hang', N'Viem nha chu',      N'Can dieu tri nha chu'),
        (N'HS023', 26, N'Rang hang', N'Viem nha chu',      N'Can dieu tri nha chu'),
        (N'HS023', 36, N'Rang hang', N'Viem nha chu',      N'Can dieu tri nha chu'),
        (N'HS024', 11, N'Rang cua',  N'Chen chuc',         N'Chi dinh chinh nha'),
        (N'HS024', 12, N'Rang cua',  N'Chen chuc',         N'Chi dinh chinh nha'),
        (N'HS024', 21, N'Rang cua',  N'Chen chuc',         N'Chi dinh chinh nha'),
        (N'HS025', 16, N'Rang hang', N'Cao rang',          N'Can lay cao'),
        (N'HS025', 17, N'Rang hang', N'Binh thuong',       NULL),
        (N'HS025', 26, N'Rang hang', N'Cao rang',          N'Can lay cao'),
        (N'HS026', 18, N'Rang khon', N'Moc lech',          N'Can nho rang khon'),
        (N'HS026', 17, N'Rang hang', N'Binh thuong',       NULL),
        (N'HS026', 27, N'Rang hang', N'Cao rang',          N'Can lay cao'),
        (N'HS027', 46, N'Rang hang', N'Mat rang',          N'Can cay ghep Implant'),
        (N'HS027', 45, N'Rang hang', N'Binh thuong',       NULL),
        (N'HS027', 47, N'Rang hang', N'Binh thuong',       NULL),
        (N'HS028', 64, N'Rang sua',  N'Sau rang',          N'Can tram rang'),
        (N'HS028', 65, N'Rang sua',  N'Binh thuong',       NULL),
        (N'HS028', 74, N'Rang sua',  N'Binh thuong',       NULL);

        /* =================================================================
           6. DANH MUC DICH VU
           ================================================================= */
        INSERT INTO DichVu (MaDichVu, TenDichVu, DonGia, ThoiGianBaoHanh, MoTa) VALUES
        (N'DV007', N'Nho rang thuong',              800000,   0,  N'Nho rang vinh vien khong phau thuat'),
        (N'DV008', N'Nho rang khon moc lech',       2500000,  0,  N'Tieu phau nho rang khon moc lech'),
        (N'DV009', N'Cay ghep Implant',             25000000, 120, N'Cay ghep tru Implant va phuc hinh'),
        (N'DV010', N'Han tram rang',                1200000,  24, N'Han tram phuc hinh than rang'),
        (N'DV011', N'Tay trang rang bang may',      3500000,  12, N'Tay trang rang bang den LED'),
        (N'DV012', N'Cao voi rang sieu am',         400000,   0,  N'Lay cao va danh bong bang may sieu am'),
        (N'DV013', N'Chup X-quang rang toan ham',   250000,   0,  N'Phim panorama rang toan ham'),
        (N'DV014', N'Chup CT Cone Beam',            900000,   0,  N'Chup CT 3D cho cay ghep / phau thuat'),
        (N'DV015', N'Diep rang',                    150000,   0,  N'Diep rang bang chi nha khoa'),
        (N'DV016', N'Nha rang su kim loai',         5000000,  60, N'Phuc hinh rang su kim loai'),
        (N'DV017', N'Nha rang toan su',             7000000,  60, N'Phuc hinh rang toan su tham my'),
        (N'DV018', N'Ham duy tri',                  2500000,  0,  N'Ham duy tri sau chinh nha'),
        (N'DV019', N'Chinh nha mac cai',            35000000, 24, N'Dieu tri chinh nha bang mac cai kim loai'),
        (N'DV020', N'Nho rang su',                  300000,   0,  N'Nho rang su cho tre em');

        /* =================================================================
           7. DANH MUC VAT TU
           ================================================================= */
        INSERT INTO VatTu (MaVatTu, TenVatTu, LoaiVatTu, DonViTinh, SoLuongTon, NguongToiThieu, HanSuDung, DonGiaBan) VALUES
        (N'VT007', N'Ibuprofen 400mg',              N'Thuoc',    N'Vien', 300, 80,  '2028-05-31', 3000),
        (N'VT008', N'Augmentin 625mg',              N'Thuoc',    N'Vien', 150, 40,  '2027-09-30', 12000),
        (N'VT009', N'Metronidazole 250mg',          N'Thuoc',    N'Vien', 220, 50,  '2027-11-30', 2500),
        (N'VT010', N'Chlorhexidine 0.12%',          N'Thuoc',    N'Chai', 35,  10,  '2027-04-30', 45000),
        (N'VT011', N'Vet bond',                     N'Vat lieu', N'Ong',  20,  5,   '2027-08-31', 0),
        (N'VT012', N'Xi mang tram rang',            N'Vat lieu', N'Hop',  18,  5,   '2027-06-30', 0),
        (N'VT013', N'Kim tiem nha khoa',            N'Tieu hao', N'Hop',  50,  15,  '2027-10-31', 0),
        (N'VT014', N'Chi khau phau thuat',          N'Tieu hao', N'Goi',  30,  10,  '2027-12-31', 0),
        (N'VT015', N'Gau vo trung',                 N'Tieu hao', N'Goi',  100, 25,  '2028-02-28', 0),
        (N'VT016', N'Nuoc suc mieng Chlorhexidine', N'Tieu hao', N'Chai', 45,  15,  '2027-07-31', 0),
        (N'VT017', N'Bo dung cu kham rang',         N'Dung cu',  N'Bo',   12,  3,   '2028-12-31', 0),
        (N'VT018', N'Bo dung cu nho rang',          N'Dung cu',  N'Bo',   8,   2,   '2028-12-31', 0),
        (N'VT019', N'Dau kim tram rang',            N'Dung cu',  N'Bo',   6,   2,   '2028-01-31', 0),
        (N'VT020', N'Nap rang su',                  N'Vat lieu', N'Cai',  12,  3,   '2027-12-31', 0),
        (N'VT021', N'Cap rang su',                  N'Vat lieu', N'Cap',  10,  3,   '2027-12-31', 0),
        (N'VT022', N'Thuoc te Articaine 4%',        N'Thuoc',    N'Ong',  80,  20,  '2027-10-31', 0),
        (N'VT023', N'Paracetamol 325mg',            N'Thuoc',    N'Vien', 400, 100, '2028-03-31', 2000),
        (N'VT024', N'Amoxicillin 250mg',            N'Thuoc',    N'Vien', 250, 60,  '2027-08-31', 4000);

        /* =================================================================
           8. LICH HEN
           ================================================================= */
        INSERT INTO LichHen (MaLichHen, ThoiGianHen, TrangThai, NgayTao, ThoiGianCheckIn, MaNhanVienLap, MaNhaSiPhuTrach, MaBenhNhan, MaPhong, MaKeHoach) VALUES
        (N'LH006', '2026-11-03 08:00:00', N'Hoan thanh',   '2026-10-30', '2026-11-03 07:50:00', N'NV014', N'NV009', N'BN005', N'P01', NULL),
        (N'LH007', '2026-11-03 08:30:00', N'Da check-in',  '2026-10-30', '2026-11-03 08:20:00', N'NV014', N'NV010', N'BN006', N'P02', NULL),
        (N'LH008', '2026-11-03 09:00:00', N'Da xac nhan',  '2026-10-31', NULL,                  N'NV014', N'NV011', N'BN007', N'P05', NULL),
        (N'LH009', '2026-11-03 09:30:00', N'Dang kham',    '2026-10-31', '2026-11-03 09:20:00', N'NV015', N'NV009', N'BN008', N'P01', NULL),
        (N'LH010', '2026-11-03 10:00:00', N'Cho xac nhan', '2026-11-01', NULL,                  N'NV015', N'NV020', N'BN009', N'P04', NULL),
        (N'LH011', '2026-11-03 10:30:00', N'Da xac nhan',  '2026-11-01', NULL,                  N'NV014', N'NV010', N'BN010', N'P02', NULL),
        (N'LH012', '2026-11-03 13:30:00', N'Cho xac nhan', '2026-11-01', NULL,                  N'NV015', N'NV011', N'BN011', N'P05', NULL),
        (N'LH013', '2026-11-03 14:00:00', N'Da check-in',  '2026-11-02', '2026-11-03 13:50:00', N'NV015', N'NV009', N'BN012', N'P01', NULL),
        (N'LH014', '2026-11-03 15:00:00', N'Da xac nhan',  '2026-11-02', NULL,                  N'NV014', N'NV020', N'BN013', N'P04', NULL),
        (N'LH015', '2026-11-03 16:00:00', N'Cho xac nhan', '2026-11-02', NULL,                  N'NV015', N'NV010', N'BN014', N'P02', NULL),
        (N'LH016', '2026-11-03 16:30:00', N'Da xac nhan',  '2026-11-02', NULL,                  N'NV014', N'NV011', N'BN015', N'P05', NULL),
        (N'LH017', '2026-11-04 08:00:00', N'Da xac nhan',  '2026-11-01', NULL,                  N'NV014', N'NV009', N'BN016', N'P01', NULL),
        (N'LH018', '2026-11-04 09:00:00', N'Cho xac nhan', '2026-11-01', NULL,                  N'NV015', N'NV010', N'BN017', N'P02', NULL),
        (N'LH019', '2026-11-04 10:00:00', N'Da xac nhan',  '2026-11-02', NULL,                  N'NV014', N'NV011', N'BN018', N'P05', NULL),
        (N'LH020', '2026-11-04 14:00:00', N'Cho xac nhan', '2026-11-02', NULL,                  N'NV015', N'NV020', N'BN019', N'P04', NULL),
        (N'LH021', '2026-11-04 15:00:00', N'Da xac nhan',  '2026-11-02', NULL,                  N'NV014', N'NV009', N'BN020', N'P01', NULL),
        (N'LH022', '2026-11-05 08:00:00', N'Cho xac nhan', '2026-11-02', NULL,                  N'NV015', N'NV010', N'BN021', N'P02', NULL),
        (N'LH023', '2026-11-05 09:30:00', N'Cho xac nhan', '2026-11-03', NULL,                  N'NV014', N'NV011', N'BN022', N'P05', NULL),
        (N'LH024', '2026-11-05 13:30:00', N'Da xac nhan',  '2026-11-03', NULL,                  N'NV015', N'NV009', N'BN023', N'P01', NULL),
        (N'LH025', '2026-11-05 15:00:00', N'Cho xac nhan', '2026-11-03', NULL,                  N'NV014', N'NV020', N'BN024', N'P04', NULL),
        (N'LH026', '2026-11-06 08:00:00', N'Da xac nhan',  '2026-11-03', NULL,                  N'NV015', N'NV010', N'BN025', N'P02', NULL),
        (N'LH027', '2026-11-06 09:00:00', N'Cho xac nhan', '2026-11-03', NULL,                  N'NV014', N'NV011', N'BN026', N'P05', NULL),
        (N'LH028', '2026-11-06 14:00:00', N'Cho xac nhan', '2026-11-03', NULL,                  N'NV015', N'NV009', N'BN027', N'P01', NULL),
        (N'LH029', '2026-11-06 15:30:00', N'Cho xac nhan', '2026-11-03', NULL,                  N'NV014', N'NV020', N'BN028', N'P04', NULL),
        (N'LH030', '2026-11-09 08:00:00', N'Da xac nhan',  '2026-11-03', NULL,                  N'NV014', N'NV009', N'BN005', N'P01', NULL),
        (N'LH031', '2026-11-09 09:00:00', N'Cho xac nhan', '2026-11-03', NULL,                  N'NV015', N'NV010', N'BN006', N'P02', NULL),
        (N'LH032', '2026-11-09 10:30:00', N'Cho xac nhan', '2026-11-03', NULL,                  N'NV014', N'NV011', N'BN007', N'P05', NULL),
        (N'LH033', '2026-11-10 08:30:00', N'Cho xac nhan', '2026-11-03', NULL,                  N'NV015', N'NV009', N'BN008', N'P01', NULL),
        (N'LH034', '2026-11-10 14:00:00', N'Da xac nhan',  '2026-11-03', NULL,                  N'NV014', N'NV020', N'BN009', N'P04', NULL),
        (N'LH035', '2026-11-12 09:00:00', N'Cho xac nhan', '2026-11-03', NULL,                  N'NV015', N'NV010', N'BN010', N'P02', NULL),
        (N'LH036', '2026-11-12 15:00:00', N'Cho xac nhan', '2026-11-03', NULL,                  N'NV014', N'NV011', N'BN011', N'P05', NULL),
        (N'LH037', '2026-11-16 08:00:00', N'Cho xac nhan', '2026-11-03', NULL,                  N'NV015', N'NV009', N'BN012', N'P01', NULL),
        (N'LH038', '2026-11-16 09:30:00', N'Cho xac nhan', '2026-11-03', NULL,                  N'NV014', N'NV020', N'BN013', N'P04', NULL),
        (N'LH039', '2026-11-20 08:00:00', N'Cho xac nhan', '2026-11-03', NULL,                  N'NV015', N'NV010', N'BN014', N'P02', NULL),
        (N'LH040', '2026-11-20 10:00:00', N'Cho xac nhan', '2026-11-03', NULL,                  N'NV014', N'NV011', N'BN015', N'P05', NULL),
        (N'LH041', '2026-10-05 09:00:00', N'Hoan thanh',   '2026-10-01', '2026-10-05 08:50:00', N'NV003', N'NV002', N'BN002', N'P02', NULL),
        (N'LH042', '2026-10-12 10:00:00', N'Hoan thanh',   '2026-10-08', '2026-10-12 09:50:00', N'NV003', N'NV001', N'BN003', N'P01', NULL),
        (N'LH043', '2026-10-20 08:30:00', N'Hoan thanh',   '2026-10-16', '2026-10-20 08:20:00', N'NV003', N'NV002', N'BN004', N'P02', NULL),
        (N'LH044', '2026-10-25 14:00:00', N'Hoan thanh',   '2026-10-21', '2026-10-25 13:50:00', N'NV003', N'NV009', N'BN005', N'P01', NULL),
        (N'LH045', '2026-10-28 09:30:00', N'Hoan thanh',   '2026-10-24', '2026-10-28 09:20:00', N'NV003', N'NV010', N'BN007', N'P02', NULL),
        (N'LH046', '2026-10-30 11:00:00', N'Huy',          '2026-10-27', NULL,                  N'NV003', N'NV009', N'BN008', N'P01', NULL),
        (N'LH047', '2026-11-02 09:00:00', N'Vang',         '2026-10-28', NULL,                  N'NV003', N'NV011', N'BN009', N'P05', NULL),
        (N'LH048', '2026-11-02 15:00:00', N'Hoan thanh',   '2026-10-29', '2026-11-02 14:50:00', N'NV014', N'NV010', N'BN010', N'P02', NULL),
        (N'LH049', '2026-11-01 10:00:00', N'Hoan thanh',   '2026-10-28', '2026-11-01 09:50:00', N'NV015', N'NV020', N'BN011', N'P04', NULL),
        (N'LH050', '2026-11-04 16:30:00', N'Huy',          '2026-11-02', NULL,                  N'NV014', N'NV009', N'BN012', N'P01', NULL);

        /* =================================================================
           9. CHAN DOAN
           ================================================================= */
        INSERT INTO ChanDoan (MaChanDoan, NgayKham, LyDoKham, TrieuChung, KetLuanChanDoan, TrangThai, MaLichHen, MaHoSo, MaNhaSi) VALUES
        (N'CD005', '2026-11-03', N'Dau nhuc rang ham duoi',        N'Dau khi an nhai, e buot',        N'Sau rang 16, cao rang nhieu',            N'Da chot', N'LH006', N'HS005', N'NV009'),
        (N'CD006', '2026-11-03', N'Chay mau chan rang',            N'Sung nhe, de chay mau',           N'Viem loi nhe, can ve sinh rang mieng',   N'Nhap',    N'LH009', N'HS008', N'NV009'),
        (N'CD007', '2026-11-03', N'Dau rang ham duoi',             N'Dau am i khi an nong',            N'Viem tuy rang 46',                       N'Nhap',    N'LH013', N'HS012', N'NV009'),
        (N'CD008', '2026-11-03', N'Sung loi rang cua',             N'Loi sung do, de chay mau',        N'Viem loi, can dieu tri nha chu',         N'Nhap',    N'LH007', N'HS006', N'NV010'),
        (N'CD009', '2026-10-05', N'Rang nhiem mau',                 N'Rang vang, khong dau',             N'Rang nhiem mau, chi dinh tay trang',     N'Da chot', N'LH041', N'HS002', N'NV002'),
        (N'CD010', '2026-10-12', N'Dau rang dem',                  N'Dau am i, sung loi',               N'Viem tuy rang 46',                       N'Da chot', N'LH042', N'HS003', N'NV001'),
        (N'CD011', '2026-10-20', N'Kham dinh ky cho be',           N'Khong',                            N'Sau rang sua so 55 muc do nhe',          N'Da chot', N'LH043', N'HS004', N'NV002'),
        (N'CD012', '2026-10-25', N'Dau rang khon',                 N'Sung, kho mo mieng',               N'Rang khon 38 moc lech, can nho',         N'Da chot', N'LH044', N'HS005', N'NV009'),
        (N'CD013', '2026-10-28', N'Cao rang, chay mau loi',        N'Cao rang vang, loi sung',          N'Cao rang, viem nha chu nhe',             N'Da chot', N'LH045', N'HS007', N'NV010'),
        (N'CD014', '2026-11-02', N'Mat rang so 24',                N'Kho an nhai, mat tham my',         N'Mat rang 24, can phuc hinh',             N'Da chot', N'LH048', N'HS010', N'NV010'),
        (N'CD015', '2026-11-01', N'Rang chen chuc',                N'Rang moi chen chuc, kho ve sinh',  N'Rang chen chuc, chi dinh chinh nha',     N'Da chot', N'LH049', N'HS011', N'NV020'),
        (N'CD016', '2026-10-15', N'Dau rang khon',                 N'Sung, kho mo mieng',               N'Rang khon 48 moc lech, can nho',         N'Da chot', NULL, N'HS016', N'NV011'),
        (N'CD017', '2026-10-16', N'Rang vang',                     N'Rang nhiem mau, khong dau',        N'Rang nhiem mau, chi dinh tay trang',     N'Da chot', NULL, N'HS017', N'NV010'),
        (N'CD018', '2026-10-17', N'Mat rang ham duoi',             N'Kho an nhai',                      N'Mat rang 36, chi dinh cay ghep',         N'Da chot', NULL, N'HS018', N'NV009'),
        (N'CD019', '2026-10-18', N'Kham dinh ky cho be',           N'Khong',                            N'Sau rang sua 54 va 64',                  N'Da chot', NULL, N'HS019', N'NV020'),
        (N'CD020', '2026-10-19', N'Mat rang cua',                  N'Mat rang 11, kho phat am',         N'Mat rang 11, can phuc hinh',             N'Da chot', NULL, N'HS020', N'NV011'),
        (N'CD021', '2026-10-21', N'Cao rang',                      N'Cao rang vang, hoi tho',           N'Cao rang, viem loi nhe',                 N'Da chot', NULL, N'HS021', N'NV010'),
        (N'CD022', '2026-10-22', N'Sau rang so 26',                N'Lo rang, kho an nhai',             N'Sau rang 26, can tram rang',             N'Da chot', NULL, N'HS022', N'NV009'),
        (N'CD023', '2026-10-23', N'Chay mau chan rang',            N'Loi sung, chay mau khi danh rang', N'Viem nha chu, can dieu tri',             N'Da chot', NULL, N'HS023', N'NV011'),
        (N'CD024', '2026-10-24', N'Rang chen chuc',                N'Rang moi chen chuc',               N'Rang chen chuc, chi dinh chinh nha',     N'Da chot', NULL, N'HS024', N'NV020'),
        (N'CD025', '2026-10-26', N'Cao rang',                      N'Cao rang, hoi tho',                N'Cao rang, can lay cao',                  N'Da chot', NULL, N'HS025', N'NV010'),
        (N'CD026', '2026-10-27', N'Dau rang khon',                 N'Sung, kho mo mieng',               N'Rang khon 18 moc lech, can nho',         N'Da chot', NULL, N'HS026', N'NV009'),
        (N'CD027', '2026-10-29', N'Mat rang ham duoi',             N'Kho an nhai',                      N'Mat rang 46, chi dinh Implant',          N'Da chot', NULL, N'HS027', N'NV011'),
        (N'CD028', '2026-10-30', N'Sau rang sua',                  N'Lo rang, kho an nhai',             N'Sau rang 64, can tram rang',             N'Da chot', NULL, N'HS028', N'NV020'),
        (N'CD029', '2026-10-31', N'Rang vang',                     N'Rang nhiem mau',                   N'Rang nhiem mau, chi dinh tay trang',     N'Da chot', NULL, N'HS015', N'NV002'),
        (N'CD030', '2026-10-30', N'Cao rang, chay mau loi',        N'Cao rang, loi sung',               N'Cao rang, viem loi nhe',                 N'Da chot', NULL, N'HS014', N'NV001'),
        (N'CD031', '2026-10-29', N'Sau rang sua',                  N'Lo rang',                          N'Sau rang 55, can tram rang',             N'Da chot', NULL, N'HS013', N'NV002'),
        (N'CD032', '2026-11-01', N'Dau rang khon',                 N'Sung, kho mo mieng',               N'Rang khon 38 moc lech, can nho',         N'Da chot', NULL, N'HS012', N'NV009'),
        (N'CD033', '2026-11-02', N'Tai kham sau dieu tri',         N'Khong dau',                        N'On dinh, tiep tuc theo doi',             N'Da chot', NULL, N'HS005', N'NV009'),
        (N'CD034', '2026-11-02', N'Rang vang',                     N'Rang nhiem mau',                   N'Rang nhiem mau, chi dinh tay trang',     N'Da chot', NULL, N'HS009', N'NV020'),
        (N'CD035', '2026-11-02', N'Kiem tra sau dieu tri nha chu',  N'Loi do, khong chay mau',           N'Viem loi da giam, tiep tuc ve sinh',     N'Da chot', NULL, N'HS006', N'NV010');

        /* =================================================================
           10. DON THUOC
           ================================================================= */
        INSERT INTO DonThuoc (MaDonThuoc, NgayKe, LoiDanChamSoc, MaChanDoan) VALUES
        (N'DT004', '2026-11-03', N'Uong sau an, khong an do cung trong 24h', N'CD005'),
        (N'DT005', '2026-10-05', N'Ve sinh rang mieng nhe nhang',             N'CD009'),
        (N'DT006', '2026-10-12', N'Uong thuoc dung lieu, tai kham sau 3 ngay', N'CD010'),
        (N'DT007', '2026-10-20', N'Chi dung thuoc khi tre dau',               N'CD011'),
        (N'DT008', '2026-10-25', N'Chuom lanh 15 phut moi ngay',              N'CD012'),
        (N'DT009', '2026-10-28', N'Suc mieng 2 lan/ngay',                     N'CD013'),
        (N'DT010', '2026-11-02', N'Tai kham sau 1 tuan',                      N'CD014'),
        (N'DT011', '2026-11-01', N'Ve sinh rang mieng ky',                     N'CD015'),
        (N'DT012', '2026-10-15', N'Uong nhieu nuoc, nghi ngoi',               N'CD016'),
        (N'DT013', '2026-10-16', N'Tranh do an co mau trong 24h',             N'CD017'),
        (N'DT014', '2026-10-17', N'Cham soc sau cay ghep theo huong dan',     N'CD018'),
        (N'DT015', '2026-10-18', N'Chi dung thuoc khi tre dau',               N'CD019'),
        (N'DT016', '2026-10-19', N'Ve sinh rang mieng nhe nhang',             N'CD020'),
        (N'DT017', '2026-10-21', N'Thay ban chai sau 3 thang',                N'CD021'),
        (N'DT018', '2026-10-22', N'Khong an do cung trong 24h',               N'CD022'),
        (N'DT019', '2026-10-23', N'Suc mieng bang nuoc muoi loang',           N'CD023'),
        (N'DT020', '2026-10-24', N'Chai rang dung cach, luc 2 phut',          N'CD024'),
        (N'DT021', '2026-10-26', N'Ve sinh rang mieng 2 lan/ngay',            N'CD025'),
        (N'DT022', '2026-10-27', N'Uong thuoc dung lieu, tai kham sau 3 ngay', N'CD026');

        INSERT INTO ChiTietDonThuoc (MaDonThuoc, MaVatTu, LieuDung, DuongDung, SoLuong) VALUES
        (N'DT004', N'VT007', N'1 vien x 2 lan/ngay',  N'Uong', 10),
        (N'DT004', N'VT008', N'1 vien x 2 lan/ngay',  N'Uong', 14),
        (N'DT005', N'VT002', N'1 vien khi dau',        N'Uong', 10),
        (N'DT006', N'VT001', N'1 vien x 3 lan/ngay',  N'Uong', 15),
        (N'DT006', N'VT002', N'1 vien khi dau',        N'Uong', 10),
        (N'DT007', N'VT023', N'1 vien khi dau',        N'Uong', 6),
        (N'DT008', N'VT007', N'1 vien x 2 lan/ngay',  N'Uong', 10),
        (N'DT008', N'VT009', N'1 vien x 2 lan/ngay',  N'Uong', 10),
        (N'DT009', N'VT010', N'Suc mieng 2 lan/ngay', N'Suc mieng', 1),
        (N'DT010', N'VT007', N'1 vien x 2 lan/ngay',  N'Uong', 10),
        (N'DT011', N'VT002', N'1 vien khi dau',        N'Uong', 10),
        (N'DT012', N'VT008', N'1 vien x 2 lan/ngay',  N'Uong', 14),
        (N'DT013', N'VT023', N'1 vien khi dau',        N'Uong', 6),
        (N'DT014', N'VT008', N'1 vien x 2 lan/ngay',  N'Uong', 14),
        (N'DT014', N'VT007', N'1 vien x 2 lan/ngay',  N'Uong', 10),
        (N'DT015', N'VT023', N'1 vien khi dau',        N'Uong', 6),
        (N'DT016', N'VT024', N'1 vien x 3 lan/ngay',  N'Uong', 15),
        (N'DT017', N'VT010', N'Suc mieng 2 lan/ngay', N'Suc mieng', 1),
        (N'DT018', N'VT001', N'1 vien x 3 lan/ngay',  N'Uong', 15),
        (N'DT019', N'VT009', N'1 vien x 2 lan/ngay',  N'Uong', 10),
        (N'DT020', N'VT023', N'1 vien khi dau',        N'Uong', 6),
        (N'DT021', N'VT002', N'1 vien khi dau',        N'Uong', 10),
        (N'DT022', N'VT008', N'1 vien x 2 lan/ngay',  N'Uong', 14);

        /* =================================================================
           11. KE HOACH DIEU TRI
           ================================================================= */
        INSERT INTO KeHoachDieuTri (MaKeHoach, TenPhacDo, ThoiGianDuKien, TrangThai, MaChanDoan, MaBenhNhan) VALUES
        (N'KH005', N'Lay cao & tram rang 16',        14,  N'Dang thuc hien', N'CD005', N'BN005'),
        (N'KH006', N'Tay trang rang',                 1,  N'Hoan thanh',     N'CD009', N'BN002'),
        (N'KH007', N'Dieu tri tuy rang 46',          21,  N'Dang thuc hien', N'CD010', N'BN003'),
        (N'KH008', N'Tram rang sua 55',               7,  N'Hoan thanh',     N'CD011', N'BN004'),
        (N'KH009', N'Nho rang khon 38',               1,  N'Hoan thanh',     N'CD012', N'BN005'),
        (N'KH010', N'Cao voi & dieu tri nha chu',     3,  N'Dang thuc hien', N'CD013', N'BN007'),
        (N'KH011', N'Phuc hinh rang 24',             30,  N'Dang thuc hien', N'CD014', N'BN010'),
        (N'KH012', N'Chinh nha mac cai',             24,  N'Dang thuc hien', N'CD015', N'BN011'),
        (N'KH013', N'Nho rang khon 48',               1,  N'Hoan thanh',     N'CD016', N'BN016'),
        (N'KH014', N'Tay trang rang',                 1,  N'Hoan thanh',     N'CD017', N'BN017'),
        (N'KH015', N'Cay ghep Implant 36',           90,  N'Dang thuc hien', N'CD018', N'BN018'),
        (N'KH016', N'Tram rang sua 54 & 64',          7,  N'Hoan thanh',     N'CD019', N'BN019'),
        (N'KH017', N'Nha rang toan su 11',           30,  N'Dang thuc hien', N'CD020', N'BN020'),
        (N'KH018', N'Cao voi rang',                   1,  N'Hoan thanh',     N'CD021', N'BN021'),
        (N'KH019', N'Han tram rang 26',              14,  N'Dang thuc hien', N'CD022', N'BN022'),
        (N'KH020', N'Dieu tri nha chu',              30,  N'Dang thuc hien', N'CD023', N'BN023'),
        (N'KH021', N'Chinh nha mac cai',             24,  N'Dang thuc hien', N'CD024', N'BN024'),
        (N'KH022', N'Lay cao & tram rang',            7,  N'Hoan thanh',     N'CD025', N'BN025'),
        (N'KH023', N'Nho rang khon 18',               1,  N'Hoan thanh',     N'CD026', N'BN026'),
        (N'KH024', N'Cay ghep Implant 46',           90,  N'Dang thuc hien', N'CD027', N'BN027'),
        (N'KH025', N'Tram rang sua 64',               7,  N'Dang thuc hien', N'CD028', N'BN028'),
        (N'KH026', N'Tay trang rang',                 1,  N'Hoan thanh',     N'CD029', N'BN015'),
        (N'KH027', N'Chup X-quang & diep rang',       3,  N'Hoan thanh',     N'CD030', N'BN014'),
        (N'KH028', N'Han tram rang 55',              14,  N'Dang thuc hien', N'CD031', N'BN013'),
        (N'KH029', N'Nho rang khon 38',               1,  N'Hoan thanh',     N'CD032', N'BN012'),
        (N'KH030', N'Tay trang rang',                 1,  N'Hoan thanh',     N'CD033', N'BN005');

        INSERT INTO KeHoachDieuTri_DichVu (MaKeHoach, MaDichVu, SoLuong) VALUES
        (N'KH005', N'DV001', 1), (N'KH005', N'DV002', 1),
        (N'KH006', N'DV006', 1),
        (N'KH007', N'DV005', 1), (N'KH007', N'DV002', 1),
        (N'KH008', N'DV002', 1),
        (N'KH009', N'DV008', 1), (N'KH009', N'DV001', 1),
        (N'KH010', N'DV012', 1), (N'KH010', N'DV015', 1),
        (N'KH011', N'DV004', 1), (N'KH011', N'DV014', 1),
        (N'KH012', N'DV019', 1),
        (N'KH013', N'DV008', 1),
        (N'KH014', N'DV006', 1),
        (N'KH015', N'DV009', 1), (N'KH015', N'DV014', 1),
        (N'KH016', N'DV002', 1),
        (N'KH017', N'DV017', 1),
        (N'KH018', N'DV012', 1),
        (N'KH019', N'DV010', 1),
        (N'KH020', N'DV012', 1), (N'KH020', N'DV015', 1),
        (N'KH021', N'DV019', 1),
        (N'KH022', N'DV001', 1), (N'KH022', N'DV002', 1),
        (N'KH023', N'DV008', 1),
        (N'KH024', N'DV009', 1), (N'KH024', N'DV014', 1),
        (N'KH025', N'DV002', 1),
        (N'KH026', N'DV006', 1),
        (N'KH027', N'DV013', 1), (N'KH027', N'DV015', 1),
        (N'KH028', N'DV010', 1), (N'KH028', N'DV002', 1),
        (N'KH029', N'DV008', 1),
        (N'KH030', N'DV006', 1);

        UPDATE LichHen SET MaKeHoach = N'KH005' WHERE MaLichHen = N'LH006';
        UPDATE LichHen SET MaKeHoach = N'KH006' WHERE MaLichHen = N'LH041';
        UPDATE LichHen SET MaKeHoach = N'KH007' WHERE MaLichHen = N'LH042';
        UPDATE LichHen SET MaKeHoach = N'KH008' WHERE MaLichHen = N'LH043';
        UPDATE LichHen SET MaKeHoach = N'KH009' WHERE MaLichHen = N'LH044';
        UPDATE LichHen SET MaKeHoach = N'KH010' WHERE MaLichHen = N'LH045';
        UPDATE LichHen SET MaKeHoach = N'KH011' WHERE MaLichHen = N'LH048';
        UPDATE LichHen SET MaKeHoach = N'KH012' WHERE MaLichHen = N'LH049';

        /* =================================================================
           12. HOA DON & CHI TIET
           ================================================================= */
        INSERT INTO HoaDon (MaHoaDon, NgayLap, TrangThaiThanhToan, PhuongThucThanhToan, MaNhanVien, MaBenhNhan, MaChanDoan) VALUES
        (N'HD005', '2026-11-03', N'Da thanh toan',   N'Tien mat',     N'NV016', N'BN005', N'CD005'),
        (N'HD006', '2026-11-03', N'Da thanh toan',   N'Chuyen khoan', N'NV016', N'BN015', NULL),
        (N'HD007', '2026-11-03', N'Da thanh toan',   N'Tien mat',     N'NV004', N'BN016', NULL),
        (N'HD008', '2026-11-03', N'Da thanh toan',   N'Tien mat',     N'NV016', N'BN017', NULL),
        (N'HD009', '2026-10-05', N'Da thanh toan',   N'Chuyen khoan', N'NV004', N'BN002', N'CD009'),
        (N'HD010', '2026-10-05', N'Da thanh toan',   N'Tien mat',     N'NV004', N'BN003', NULL),
        (N'HD011', '2026-10-12', N'Da thanh toan',   N'Tien mat',     N'NV004', N'BN003', N'CD010'),
        (N'HD012', '2026-10-15', N'Da thanh toan',   N'Chuyen khoan', N'NV016', N'BN016', N'CD016'),
        (N'HD013', '2026-10-16', N'Da thanh toan',   N'Tien mat',     N'NV004', N'BN017', N'CD017'),
        (N'HD014', '2026-10-18', N'Da thanh toan',   N'Tien mat',     N'NV016', N'BN019', N'CD019'),
        (N'HD015', '2026-10-20', N'Da thanh toan',   N'Chuyen khoan', N'NV004', N'BN004', N'CD011'),
        (N'HD016', '2026-10-22', N'Da thanh toan',   N'Tien mat',     N'NV016', N'BN022', N'CD022'),
        (N'HD017', '2026-10-25', N'Da thanh toan',   N'Tien mat',     N'NV004', N'BN005', N'CD012'),
        (N'HD018', '2026-10-28', N'Da thanh toan',   N'Chuyen khoan', N'NV016', N'BN007', N'CD013'),
        (N'HD019', '2026-10-30', N'Chua thanh toan', NULL,            N'NV004', N'BN028', N'CD028'),
        (N'HD020', '2026-11-01', N'Dang tra gop',    N'Tra gop',      N'NV016', N'BN011', N'CD015'),
        (N'HD021', '2026-11-02', N'Da thanh toan',   N'Tien mat',     N'NV004', N'BN010', N'CD014'),
        (N'HD022', '2026-11-02', N'Da thanh toan',   N'Tien mat',     N'NV016', N'BN007', NULL),
        (N'HD023', '2026-10-29', N'Dang tra gop',    N'Tra gop',      N'NV004', N'BN027', N'CD027'),
        (N'HD024', '2026-10-31', N'Da thanh toan',   N'Tien mat',     N'NV016', N'BN015', N'CD029'),
        (N'HD025', '2026-10-30', N'Da thanh toan',   N'Chuyen khoan', N'NV004', N'BN014', N'CD030'),
        (N'HD026', '2026-10-29', N'Da thanh toan',   N'Tien mat',     N'NV016', N'BN013', N'CD031'),
        (N'HD027', '2026-11-01', N'Da thanh toan',   N'Tien mat',     N'NV004', N'BN012', N'CD032'),
        (N'HD028', '2026-11-02', N'Da thanh toan',   N'Chuyen khoan', N'NV016', N'BN005', N'CD033'),
        (N'HD029', '2026-11-02', N'Da thanh toan',   N'Tien mat',     N'NV004', N'BN009', N'CD034'),
        (N'HD030', '2026-11-03', N'Chua thanh toan', NULL,            N'NV016', N'BN018', NULL),
        (N'HD031', '2026-10-26', N'Dang tra gop',    N'Tra gop',      N'NV004', N'BN020', N'CD020'),
        (N'HD032', '2026-10-24', N'Dang tra gop',    N'Tra gop',      N'NV016', N'BN024', N'CD024');

        INSERT INTO ChiTietHoaDon (MaHoaDon, MaDichVu, SoLuong, DonGiaTaiThoiDiem) VALUES
        (N'HD005', N'DV001', 1, 200000), (N'HD005', N'DV002', 1, 300000),
        (N'HD006', N'DV012', 1, 400000), (N'HD006', N'DV014', 1, 900000),
        (N'HD007', N'DV011', 1, 3500000),
        (N'HD008', N'DV015', 1, 150000), (N'HD008', N'DV013', 1, 250000),
        (N'HD009', N'DV006', 1, 2000000),
        (N'HD010', N'DV001', 1, 200000),
        (N'HD011', N'DV005', 1, 1200000), (N'HD011', N'DV002', 1, 300000),
        (N'HD012', N'DV008', 1, 2500000),
        (N'HD013', N'DV006', 1, 2000000),
        (N'HD014', N'DV002', 1, 300000), (N'HD014', N'DV001', 1, 200000),
        (N'HD015', N'DV002', 1, 300000),
        (N'HD016', N'DV010', 1, 1200000), (N'HD016', N'DV015', 1, 150000),
        (N'HD017', N'DV003', 1, 1500000), (N'HD017', N'DV001', 1, 200000),
        (N'HD018', N'DV001', 1, 200000), (N'HD018', N'DV012', 1, 400000),
        (N'HD019', N'DV002', 1, 300000), (N'HD019', N'DV010', 1, 1200000),
        (N'HD020', N'DV019', 1, 35000000),
        (N'HD021', N'DV004', 1, 3500000), (N'HD021', N'DV014', 1, 900000),
        (N'HD022', N'DV006', 1, 2000000),
        (N'HD023', N'DV009', 1, 25000000), (N'HD023', N'DV014', 1, 900000), (N'HD023', N'DV013', 1, 250000),
        (N'HD024', N'DV006', 1, 2000000),
        (N'HD025', N'DV013', 1, 250000), (N'HD025', N'DV015', 1, 150000),
        (N'HD026', N'DV010', 1, 1200000), (N'HD026', N'DV002', 1, 300000),
        (N'HD027', N'DV008', 1, 2500000),
        (N'HD028', N'DV006', 1, 2000000),
        (N'HD029', N'DV006', 1, 2000000),
        (N'HD030', N'DV012', 1, 400000),
        (N'HD031', N'DV016', 1, 5000000), (N'HD031', N'DV010', 1, 1200000),
        (N'HD032', N'DV019', 1, 35000000);

        INSERT INTO ChiTietHoaDonThuoc (MaHoaDon, MaVatTu, SoLuong, DonGiaTaiThoiDiem) VALUES
        (N'HD005', N'VT007', 10, 3000), (N'HD005', N'VT008', 14, 12000),
        (N'HD011', N'VT001', 15, 5000), (N'HD011', N'VT002', 10, 1500),
        (N'HD013', N'VT007', 10, 3000),
        (N'HD017', N'VT001', 15, 5000), (N'HD017', N'VT002', 10, 1500),
        (N'HD021', N'VT023', 6, 2000),
        (N'HD022', N'VT024', 15, 4000),
        (N'HD028', N'VT010', 1, 45000),
        (N'HD029', N'VT024', 15, 4000);

        /* =================================================================
           13. CONG NO TRA GOP
           ================================================================= */
        INSERT INTO CongNoTraGop (MaCongNo, TongSoTien, SoDotConLai, MaHoaDon) VALUES
        (N'CN002', 35000000, 2, N'HD020'),
        (N'CN003', 26150000, 1, N'HD023'),
        (N'CN004', 6200000,  1, N'HD031'),
        (N'CN005', 35000000, 2, N'HD032');

        INSERT INTO DotThanhToan (MaDot, NgayDenHan, SoTienDot, TrangThai, NgayThanhToanThucTe, MaCongNo) VALUES
        (N'DOT004', '2026-11-01', 15000000, N'Da thanh toan', '2026-11-01', N'CN002'),
        (N'DOT005', '2026-11-01', 10000000, N'Qua han',       NULL,         N'CN002'),
        (N'DOT006', '2026-12-01', 10000000, N'Chua den han',  NULL,         N'CN002'),
        (N'DOT007', '2026-10-29', 13075000, N'Da thanh toan', '2026-10-29', N'CN003'),
        (N'DOT008', '2026-11-29', 13075000, N'Chua den han',  NULL,         N'CN003'),
        (N'DOT009', '2026-10-26', 3100000,  N'Da thanh toan', '2026-10-26', N'CN004'),
        (N'DOT010', '2026-11-26', 3100000,  N'Chua den han',  NULL,         N'CN004'),
        (N'DOT011', '2026-10-24', 11000000, N'Da thanh toan', '2026-10-24', N'CN005'),
        (N'DOT012', '2026-11-01', 12000000, N'Qua han',       NULL,         N'CN005'),
        (N'DOT013', '2026-12-01', 12000000, N'Chua den han',  NULL,         N'CN005');

        /* =================================================================
           14. PHIEU NHAP KHO
           ================================================================= */
        INSERT INTO PhieuNhapKho (MaPhieuNhap, NgayNhap, NhaCungCap, MaNhanVien) VALUES
        (N'PN003', '2026-09-25', N'Duoc pham Minh Chau',              N'NV017'),
        (N'PN004', '2026-10-05', N'Vat tu Nha khoa Sai Gon',          N'NV018'),
        (N'PN005', '2026-10-15', N'Thiet bi Y te Viet',               N'NV017'),
        (N'PN006', '2026-10-25', N'Duoc pham An Khang',               N'NV005'),
        (N'PN007', '2026-11-01', N'Vat tu Nha khoa Viet',             N'NV018'),
        (N'PN008', '2026-11-02', N'Thiet bi y te Hoang Long',         N'NV017');

        INSERT INTO ChiTietNhapKho (MaPhieuNhap, MaVatTu, SoLuong, DonGiaNhap) VALUES
        (N'PN003', N'VT007', 300, 2000), (N'PN003', N'VT008', 150, 9000), (N'PN003', N'VT009', 220, 1800),
        (N'PN004', N'VT011', 20, 650000), (N'PN004', N'VT012', 18, 480000), (N'PN004', N'VT020', 12, 900000),
        (N'PN005', N'VT013', 50, 120000), (N'PN005', N'VT014', 30, 85000), (N'PN005', N'VT015', 100, 45000),
        (N'PN006', N'VT010', 35, 32000), (N'PN006', N'VT022', 80, 15000), (N'PN006', N'VT023', 400, 1200),
        (N'PN007', N'VT016', 45, 28000), (N'PN007', N'VT017', 12, 1250000), (N'PN007', N'VT018', 8, 1850000),
        (N'PN008', N'VT019', 6, 2400000), (N'PN008', N'VT021', 10, 1600000), (N'PN008', N'VT024', 250, 2800);

        /* =================================================================
           15. PHIEU XUAT KHO
           ================================================================= */
        INSERT INTO PhieuXuatKho (MaPhieuXuat, NgayXuat, MaNhanVien, MaLichHen) VALUES
        (N'PX004', '2026-10-05', N'NV017', N'LH041'),
        (N'PX005', '2026-10-12', N'NV018', N'LH042'),
        (N'PX006', '2026-10-20', N'NV005', N'LH043'),
        (N'PX007', '2026-10-25', N'NV017', N'LH044'),
        (N'PX008', '2026-10-28', N'NV018', N'LH045'),
        (N'PX009', '2026-11-01', N'NV017', N'LH049'),
        (N'PX010', '2026-11-02', N'NV005', N'LH048'),
        (N'PX011', '2026-11-03', N'NV017', N'LH006'),
        (N'PX012', '2026-11-03', N'NV018', NULL);

        INSERT INTO ChiTietXuatKho (MaPhieuXuat, MaVatTu, SoLuongSuDung) VALUES
        (N'PX004', N'VT002', 10),
        (N'PX005', N'VT001', 15), (N'PX005', N'VT002', 10),
        (N'PX006', N'VT012', 1), (N'PX006', N'VT019', 1),
        (N'PX007', N'VT018', 1), (N'PX007', N'VT022', 1),
        (N'PX008', N'VT010', 1), (N'PX008', N'VT015', 2),
        (N'PX009', N'VT006', 2), (N'PX009', N'VT013', 1),
        (N'PX010', N'VT004', 2), (N'PX010', N'VT014', 1),
        (N'PX011', N'VT007', 10), (N'PX011', N'VT008', 14),
        (N'PX012', N'VT015', 5), (N'PX012', N'VT016', 2);

        /* =================================================================
           16. CAU HINH HE THONG
           ================================================================= */
        INSERT INTO CauHinhHeThong (MaCauHinh, GiaTri) VALUES
        (N'TEN_PHONG_KHAM',  N'Nha khoa Muse'),
        (N'DIA_CHI',         N'123 Nguyen Van Cu, Quan 5, TP.HCM'),
        (N'DIEN_THOAI',      N'028 3838 6868'),
        (N'EMAIL',           N'lienhe@muse.vn'),
        (N'MA_SO_THUE',      N'0312345678'),
        (N'GIO_MO_CUA',      N'08:00'),
        (N'GIO_DONG_CUA',    N'17:00'),
        (N'TIEN_TO_HOA_DON', N'HD'),
        (N'PHI_TAI_KHAM',    N'100000');

        /* Bo sung quyen mac dinh con thieu cho Bac si (sao chep tu Quan tri). */
        INSERT INTO QuyenVaiTro (MaVaiTro, TenChucNang)
        SELECT N'BAC_SI', q.TenChucNang
        FROM QuyenVaiTro q
        WHERE q.MaVaiTro = N'QUAN_TRI'
          AND q.TenChucNang LIKE N'%i m_t kh_u%'
          AND NOT EXISTS (
              SELECT 1 FROM QuyenVaiTro x
              WHERE x.MaVaiTro = N'BAC_SI' AND x.TenChucNang = q.TenChucNang
          );

        INSERT INTO CauHinhHeThong (MaCauHinh, GiaTri) VALUES (N'SEED_FULL_V1', N'2026-11-03');

        COMMIT;
        PRINT N'Da nap xong du lieu mo rong SEED_FULL_V1.';
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK;
        THROW;
    END CATCH
END
GO


/* =====================================================================
   CHUYEN DU LIEU MAU SANG TIENG VIET CO DAU
   ===================================================================== */
/*
    Chuyen du lieu van ban trong CSDL sang tieng Viet co dau.
    - Chi cap nhat cac cot hien thi (ten, dia chi, mo ta, chan doan...).
    - Khong doi cac ma trang thai noi bo (Dang lam viec, Cho xac nhan,
      Da thanh toan...) vi cac trang thai nay duoc dung trong WHERE/CHECK.
    - Script chay lai nhieu lan khong bi doi du lieu (WHERE theo gia tri cu).
    - Phan nay da duoc gop vao cuoi file schema; khi tao database moi chi can
      chay file nay. Neu database da ton tai va chi muon chuyen doi du lieu,
      chay rieng doan tu muc nay den het file.
*/

USE QuanLyPhongKhamMuse;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;
SET QUOTED_IDENTIFIER ON;

BEGIN TRY
    BEGIN TRAN;

    /* ============================ NHAN VIEN ============================ */
    UPDATE NhanVien SET HoTen = N'Nguyễn Văn An'       WHERE HoTen = N'Nguyen Van An';
    UPDATE NhanVien SET HoTen = N'Trần Thị Bình'       WHERE HoTen = N'Tran Thi Binh';
    UPDATE NhanVien SET HoTen = N'Lê Minh Châu'        WHERE HoTen = N'Le Minh Chau';
    UPDATE NhanVien SET HoTen = N'Phạm Quốc Dũng'      WHERE HoTen = N'Pham Quoc Dung';
    UPDATE NhanVien SET HoTen = N'Hoàng Thị Em'        WHERE HoTen = N'Hoang Thi Em';
    UPDATE NhanVien SET HoTen = N'Đỗ Thị Phương'       WHERE HoTen = N'Do Thi Phuong';
    UPDATE NhanVien SET HoTen = N'Bùi Văn Giang'       WHERE HoTen = N'Bui Van Giang';
    UPDATE NhanVien SET HoTen = N'Trà Thanh Nhựt'      WHERE HoTen = N'tra thanh nhut';
    UPDATE NhanVien SET HoTen = N'Ngô Quang Huy'       WHERE HoTen = N'Ngo Quang Huy';
    UPDATE NhanVien SET HoTen = N'Đặng Thu Hà'         WHERE HoTen = N'Dang Thu Ha';
    UPDATE NhanVien SET HoTen = N'Phan Minh Tuấn'      WHERE HoTen = N'Phan Minh Tuan';
    UPDATE NhanVien SET HoTen = N'Nguyễn Thị Mai'      WHERE HoTen = N'Nguyen Thi Mai';
    UPDATE NhanVien SET HoTen = N'Trần Thị Thu'        WHERE HoTen = N'Tran Thi Thu';
    UPDATE NhanVien SET HoTen = N'Võ Thị Kim'          WHERE HoTen = N'Vo Thi Kim';
    UPDATE NhanVien SET HoTen = N'Đỗ Minh Quân'        WHERE HoTen = N'Do Minh Quan';
    UPDATE NhanVien SET HoTen = N'Huỳnh Thị Ngọc'      WHERE HoTen = N'Huynh Thi Ngoc';
    UPDATE NhanVien SET HoTen = N'Nguyễn Văn Tùng'     WHERE HoTen = N'Nguyen Van Tung';
    UPDATE NhanVien SET HoTen = N'Lê Thị Hoa'          WHERE HoTen = N'Le Thi Hoa';
    UPDATE NhanVien SET HoTen = N'Bùi Thanh Nam'       WHERE HoTen = N'Bui Thanh Nam';
    UPDATE NhanVien SET HoTen = N'Hoàng Gia Bảo'       WHERE HoTen = N'Hoang Gia Bao';
    UPDATE NhanVien SET HoTen = N'Trương Mỹ Linh'      WHERE HoTen = N'Truong My Linh';

    UPDATE NhanVien SET ChuyenMon = N'Nha sĩ - Nhổ răng'     WHERE ChuyenMon = N'Nha si - Nho rang';
    UPDATE NhanVien SET ChuyenMon = N'Nha sĩ - Chỉnh nha'    WHERE ChuyenMon = N'Nha si - Chinh nha';
    UPDATE NhanVien SET ChuyenMon = N'Nha sĩ - Phục hình'    WHERE ChuyenMon = N'Nha si - Phuc hinh';
    UPDATE NhanVien SET ChuyenMon = N'Nha sĩ - Tổng quát'    WHERE ChuyenMon = N'Nha si - Tong quat';
    UPDATE NhanVien SET ChuyenMon = N'Y tá'                  WHERE ChuyenMon = N'Y ta';
    UPDATE NhanVien SET ChuyenMon = N'Lễ tân'                WHERE ChuyenMon = N'Le tan';
    UPDATE NhanVien SET ChuyenMon = N'Thu ngân'              WHERE ChuyenMon = N'Thu ngan';
    UPDATE NhanVien SET ChuyenMon = N'Thủ kho'               WHERE ChuyenMon = N'Thu kho';
    UPDATE NhanVien SET ChuyenMon = N'Quản trị hệ thống'     WHERE ChuyenMon = N'Quan tri he thong';

    UPDATE NhanVien SET GioiTinh = N'Nữ' WHERE GioiTinh = N'Nu';

    /* ============================ BENH NHAN ============================ */
    UPDATE BenhNhan SET HoTen = N'Võ Thanh Hải'          WHERE HoTen = N'Vo Thanh Hai';
    UPDATE BenhNhan SET HoTen = N'Nguyễn Thị Lan'        WHERE HoTen = N'Nguyen Thi Lan';
    UPDATE BenhNhan SET HoTen = N'Đặng Quốc Khánh'       WHERE HoTen = N'Dang Quoc Khanh';
    UPDATE BenhNhan SET HoTen = N'Trần Bảo Ngọc'         WHERE HoTen = N'Tran Bao Ngoc';
    UPDATE BenhNhan SET HoTen = N'Lê Văn Hùng'           WHERE HoTen = N'Le Van Hung';
    UPDATE BenhNhan SET HoTen = N'Phạm Thị Hoa'          WHERE HoTen = N'Pham Thi Hoa';
    UPDATE BenhNhan SET HoTen = N'Nguyễn Đức Long'       WHERE HoTen = N'Nguyen Duc Long';
    UPDATE BenhNhan SET HoTen = N'Trần Ngọc Diệp'        WHERE HoTen = N'Tran Ngoc Diep';
    UPDATE BenhNhan SET HoTen = N'Hoàng Minh Trí'        WHERE HoTen = N'Hoang Minh Tri';
    UPDATE BenhNhan SET HoTen = N'Võ Thị Thanh'          WHERE HoTen = N'Vo Thi Thanh';
    UPDATE BenhNhan SET HoTen = N'Đặng Văn Tài'          WHERE HoTen = N'Dang Van Tai';
    UPDATE BenhNhan SET HoTen = N'Bùi Thị Lan'           WHERE HoTen = N'Bui Thi Lan';
    UPDATE BenhNhan SET HoTen = N'Ngô Quốc Bảo'          WHERE HoTen = N'Ngo Quoc Bao';
    UPDATE BenhNhan SET HoTen = N'Huỳnh Thị Ngọc Ánh'    WHERE HoTen = N'Huynh Thi Ngoc Anh';
    UPDATE BenhNhan SET HoTen = N'Phan Thanh Sơn'        WHERE HoTen = N'Phan Thanh Son';
    UPDATE BenhNhan SET HoTen = N'Lê Thị Kim Ngân'       WHERE HoTen = N'Le Thi Kim Ngan';
    UPDATE BenhNhan SET HoTen = N'Trương Văn Phúc'       WHERE HoTen = N'Truong Van Phuc';
    UPDATE BenhNhan SET HoTen = N'Nguyễn Thị Thu Thảo'   WHERE HoTen = N'Nguyen Thi Thu Thao';
    UPDATE BenhNhan SET HoTen = N'Lý Minh Khang'         WHERE HoTen = N'Ly Minh Khang';
    UPDATE BenhNhan SET HoTen = N'Đào Thị Bích'          WHERE HoTen = N'Dao Thi Bich';
    UPDATE BenhNhan SET HoTen = N'Mai Văn Dương'         WHERE HoTen = N'Mai Van Duong';
    UPDATE BenhNhan SET HoTen = N'Tôn Nữ Bảo Trâm'       WHERE HoTen = N'Ton Nu Bao Tram';
    UPDATE BenhNhan SET HoTen = N'Đinh Công Thành'       WHERE HoTen = N'Dinh Cong Thanh';
    UPDATE BenhNhan SET HoTen = N'Phạm Thị Mỹ Duyên'     WHERE HoTen = N'Pham Thi My Duyen';
    UPDATE BenhNhan SET HoTen = N'Nguyễn Hoàng Phúc'     WHERE HoTen = N'Nguyen Hoang Phuc';
    UPDATE BenhNhan SET HoTen = N'Trần Thị Xuân'         WHERE HoTen = N'Tran Thi Xuan';
    UPDATE BenhNhan SET HoTen = N'Lê Quang Vinh'         WHERE HoTen = N'Le Quang Vinh';
    UPDATE BenhNhan SET HoTen = N'Võ Ngọc Mai'           WHERE HoTen = N'Vo Ngoc Mai';
    UPDATE BenhNhan SET HoTen = N'Nguyễn Văn Anh'        WHERE HoTen = N'Nguyen van Anh';
    UPDATE BenhNhan SET HoTen = N'Trần Thanh Nhựt'       WHERE HoTen = N'Tran Thanh Nhut';
    UPDATE BenhNhan SET HoTen = N'Nhựt'                  WHERE HoTen = N'nhut';
    UPDATE BenhNhan SET HoTen = N'Nguyễn Văn B'          WHERE HoTen = N'Nguyen van B';

    UPDATE BenhNhan SET GioiTinh = N'Nữ' WHERE GioiTinh = N'Nu';

    UPDATE BenhNhan SET DiaChi = N'Quận 1, TP.HCM'      WHERE DiaChi = N'Quan 1, TP.HCM';
    UPDATE BenhNhan SET DiaChi = N'Quận 2, TP.HCM'      WHERE DiaChi = N'Quan 2, TP.HCM';
    UPDATE BenhNhan SET DiaChi = N'Quận 3, TP.HCM'      WHERE DiaChi = N'Quan 3, TP.HCM';
    UPDATE BenhNhan SET DiaChi = N'Quận 4, TP.HCM'      WHERE DiaChi = N'Quan 4, TP.HCM';
    UPDATE BenhNhan SET DiaChi = N'Quận 5, TP.HCM'      WHERE DiaChi = N'Quan 5, TP.HCM';
    UPDATE BenhNhan SET DiaChi = N'Quận 6, TP.HCM'      WHERE DiaChi = N'Quan 6, TP.HCM';
    UPDATE BenhNhan SET DiaChi = N'Quận 7, TP.HCM'      WHERE DiaChi = N'Quan 7, TP.HCM';
    UPDATE BenhNhan SET DiaChi = N'Quận 8, TP.HCM'      WHERE DiaChi = N'Quan 8, TP.HCM';
    UPDATE BenhNhan SET DiaChi = N'Quận 9, TP.HCM'      WHERE DiaChi = N'Quan 9, TP.HCM';
    UPDATE BenhNhan SET DiaChi = N'Quận 10, TP.HCM'     WHERE DiaChi = N'Quan 10, TP.HCM';
    UPDATE BenhNhan SET DiaChi = N'Quận 11, TP.HCM'     WHERE DiaChi = N'Quan 11, TP.HCM';
    UPDATE BenhNhan SET DiaChi = N'Quận 12, TP.HCM'     WHERE DiaChi = N'Quan 12, TP.HCM';
    UPDATE BenhNhan SET DiaChi = N'Quận Bình Tân'       WHERE DiaChi = N'Quan Binh Tan';
    UPDATE BenhNhan SET DiaChi = N'Quận Bình Thạnh'     WHERE DiaChi = N'Quan Binh Thanh';
    UPDATE BenhNhan SET DiaChi = N'Quận Gò Vấp'         WHERE DiaChi = N'Quan Go Vap';
    UPDATE BenhNhan SET DiaChi = N'Quận Phú Nhuận'      WHERE DiaChi = N'Quan Phu Nhuan';
    UPDATE BenhNhan SET DiaChi = N'Quận Tân Bình'       WHERE DiaChi = N'Quan Tan Binh';
    UPDATE BenhNhan SET DiaChi = N'Quận Tân Phú'        WHERE DiaChi = N'Quan Tan Phu';
    UPDATE BenhNhan SET DiaChi = N'TP. Thủ Đức'         WHERE DiaChi = N'TP. Thu Duc';
    UPDATE BenhNhan SET DiaChi = N'Bình Chánh, TP.HCM'  WHERE DiaChi = N'Binh Chanh, TP.HCM';
    UPDATE BenhNhan SET DiaChi = N'Nhà Bè, TP.HCM'      WHERE DiaChi = N'Nha Be, TP.HCM';
    UPDATE BenhNhan SET DiaChi = N'Long An'             WHERE DiaChi = N'long an';

    UPDATE NhanVien SET DiaChi = N'Quận 1, TP.HCM'      WHERE DiaChi = N'Quan 1, TP.HCM';
    UPDATE NhanVien SET DiaChi = N'Quận 3, TP.HCM'      WHERE DiaChi = N'Quan 3, TP.HCM';
    UPDATE NhanVien SET DiaChi = N'Quận 4, TP.HCM'      WHERE DiaChi = N'Quan 4, TP.HCM';
    UPDATE NhanVien SET DiaChi = N'Quận 5, TP.HCM'      WHERE DiaChi = N'Quan 5, TP.HCM';
    UPDATE NhanVien SET DiaChi = N'Quận 7, TP.HCM'      WHERE DiaChi = N'Quan 7, TP.HCM';
    UPDATE NhanVien SET DiaChi = N'Quận 10, TP.HCM'     WHERE DiaChi = N'Quan 10, TP.HCM';
    UPDATE NhanVien SET DiaChi = N'Quận 12, TP.HCM'     WHERE DiaChi = N'Quan 12, TP.HCM';
    UPDATE NhanVien SET DiaChi = N'Quận Bình Thạnh'     WHERE DiaChi = N'Quan Binh Thanh';
    UPDATE NhanVien SET DiaChi = N'Quận Gò Vấp'         WHERE DiaChi = N'Quan Go Vap';
    UPDATE NhanVien SET DiaChi = N'Quận Phú Nhuận'      WHERE DiaChi = N'Quan Phu Nhuan';
    UPDATE NhanVien SET DiaChi = N'Quận Tân Bình'       WHERE DiaChi = N'Quan Tan Binh';
    UPDATE NhanVien SET DiaChi = N'Quận Tân Phú'        WHERE DiaChi = N'Quan Tan Phu';
    UPDATE NhanVien SET DiaChi = N'TP. Thủ Đức'         WHERE DiaChi = N'TP. Thu Duc';
    UPDATE NhanVien SET DiaChi = N'Long An'             WHERE DiaChi = N'long an';

    UPDATE BenhNhan SET TienSuBenhLy = N'Khỏe mạnh'                          WHERE TienSuBenhLy = N'Khoe manh';
    UPDATE BenhNhan SET TienSuBenhLy = N'Không'                              WHERE TienSuBenhLy = N'Khong';
    UPDATE BenhNhan SET TienSuBenhLy = N'Cao huyết áp'                       WHERE TienSuBenhLy = N'Cao huyet ap';
    UPDATE BenhNhan SET TienSuBenhLy = N'Cao huyết áp, thoái hóa cột sống'   WHERE TienSuBenhLy = N'Cao huyet ap, thoai hoa cot song';
    UPDATE BenhNhan SET TienSuBenhLy = N'Viêm dạ dày'                        WHERE TienSuBenhLy = N'Viem da day';
    UPDATE BenhNhan SET TienSuBenhLy = N'Tiểu đường type 2'                  WHERE TienSuBenhLy = N'Tieu duong type 2';
    UPDATE BenhNhan SET TienSuBenhLy = N'Thiếu máu nhẹ'                      WHERE TienSuBenhLy = N'Thieu mau nhe';
    UPDATE BenhNhan SET TienSuBenhLy = N'Viêm phế quản mạn'                  WHERE TienSuBenhLy = N'Viem phe quan man';
    UPDATE BenhNhan SET TienSuBenhLy = N'Rối loạn lo âu'                     WHERE TienSuBenhLy = N'Roi loan lo au';
    UPDATE BenhNhan SET TienSuBenhLy = N'Viêm khớp dạng thấp'                WHERE TienSuBenhLy = N'Viem khop dang thap';
    UPDATE BenhNhan SET TienSuBenhLy = N'Bệnh tim mạch'                      WHERE TienSuBenhLy = N'Benh tim mach';
    UPDATE BenhNhan SET TienSuBenhLy = N'Gan nhiễm mỡ'                       WHERE TienSuBenhLy = N'Gan nhiem mo';
    UPDATE BenhNhan SET TienSuBenhLy = N'Loãng xương'                        WHERE TienSuBenhLy = N'Loang xuong';

    UPDATE BenhNhan SET ThongTinDiUng = N'Không'                    WHERE ThongTinDiUng = N'Khong';
    UPDATE BenhNhan SET ThongTinDiUng = N'Dị ứng Penicillin'       WHERE ThongTinDiUng = N'Di ung Penicillin';
    UPDATE BenhNhan SET ThongTinDiUng = N'Dị ứng Aspirin'          WHERE ThongTinDiUng = N'Di ung Aspirin';
    UPDATE BenhNhan SET ThongTinDiUng = N'Dị ứng Sulfonamide'      WHERE ThongTinDiUng = N'Di ung Sulfonamide';
    UPDATE BenhNhan SET ThongTinDiUng = N'Dị ứng bụi nhà'          WHERE ThongTinDiUng = N'Di ung bui nha';
    UPDATE BenhNhan SET ThongTinDiUng = N'Dị ứng Calcium'          WHERE ThongTinDiUng = N'Di ung Calcium';
    UPDATE BenhNhan SET ThongTinDiUng = N'Dị ứng hải sản, phấn hoa' WHERE ThongTinDiUng = N'Di ung hai phan hoa';
    UPDATE BenhNhan SET ThongTinDiUng = N'Dị ứng NSAID'            WHERE ThongTinDiUng = N'Di ung NSAID';

    UPDATE BenhNhan SET NguoiBaoHo = N'Huỳnh Văn Đức (bố)'   WHERE NguoiBaoHo = N'Huynh Van Duc (bo)';
    UPDATE BenhNhan SET NguoiBaoHo = N'Lý Văn Hùng (bố)'     WHERE NguoiBaoHo = N'Ly Van Hung (bo)';
    UPDATE BenhNhan SET NguoiBaoHo = N'Ngô Văn Thành (bố)'   WHERE NguoiBaoHo = N'Ngo Van Thanh (bo)';
    UPDATE BenhNhan SET NguoiBaoHo = N'Phạm Văn Lương (bố)'  WHERE NguoiBaoHo = N'Pham Van Luong (bo)';
    UPDATE BenhNhan SET NguoiBaoHo = N'Trần Văn Minh (cha)'  WHERE NguoiBaoHo = N'Tran Van Minh (cha)';
    UPDATE BenhNhan SET NguoiBaoHo = N'Võ Văn Hậu (bố)'      WHERE NguoiBaoHo = N'Vo Van Hau (bo)';
    UPDATE BenhNhan SET NguoiBaoHo = N'Nguyễn Văn B'         WHERE NguoiBaoHo = N'Nguyen van B';

    /* ============================ DANH MUC DICH VU ============================ */
    UPDATE DichVu SET TenDichVu = N'Lấy cao răng'              WHERE TenDichVu = N'Lay cao rang';
    UPDATE DichVu SET TenDichVu = N'Trám răng composite'       WHERE TenDichVu = N'Tram rang composite';
    UPDATE DichVu SET TenDichVu = N'Nhổ răng khôn'             WHERE TenDichVu = N'Nho rang khon';
    UPDATE DichVu SET TenDichVu = N'Bọc răng sứ'               WHERE TenDichVu = N'Boc rang su';
    UPDATE DichVu SET TenDichVu = N'Điều trị tủy răng'         WHERE TenDichVu = N'Dieu tri tuy rang';
    UPDATE DichVu SET TenDichVu = N'Tẩy trắng răng'            WHERE TenDichVu = N'Tay trang rang';
    UPDATE DichVu SET TenDichVu = N'Nhổ răng thường'           WHERE TenDichVu = N'Nho rang thuong';
    UPDATE DichVu SET TenDichVu = N'Nhổ răng khôn mọc lệch'    WHERE TenDichVu = N'Nho rang khon moc lech';
    UPDATE DichVu SET TenDichVu = N'Cấy ghép Implant'          WHERE TenDichVu = N'Cay ghep Implant';
    UPDATE DichVu SET TenDichVu = N'Hàn trám răng'             WHERE TenDichVu = N'Han tram rang';
    UPDATE DichVu SET TenDichVu = N'Tẩy trắng răng bằng máy'    WHERE TenDichVu = N'Tay trang rang bang may';
    UPDATE DichVu SET TenDichVu = N'Cạo vôi răng siêu âm'      WHERE TenDichVu = N'Cao voi rang sieu am';
    UPDATE DichVu SET TenDichVu = N'Chụp X-quang răng toàn hàm' WHERE TenDichVu = N'Chup X-quang rang toan ham';
    UPDATE DichVu SET TenDichVu = N'Chụp CT Cone Beam'          WHERE TenDichVu = N'Chup CT Cone Beam';
    UPDATE DichVu SET TenDichVu = N'Điệp răng'                 WHERE TenDichVu = N'Diep rang';
    UPDATE DichVu SET TenDichVu = N'Răng sứ kim loại'          WHERE TenDichVu = N'Nha rang su kim loai';
    UPDATE DichVu SET TenDichVu = N'Răng toàn sứ'              WHERE TenDichVu = N'Nha rang toan su';
    UPDATE DichVu SET TenDichVu = N'Hàm duy trì'               WHERE TenDichVu = N'Ham duy tri';
    UPDATE DichVu SET TenDichVu = N'Chỉnh nha mắc cài'         WHERE TenDichVu = N'Chinh nha mac cai';
    UPDATE DichVu SET TenDichVu = N'Nhổ răng sữa'              WHERE TenDichVu = N'Nho rang su';

    UPDATE DichVu SET MoTa = N'Lấy cao và đánh bóng'                         WHERE MoTa = N'Lay cao va danh bong';
    UPDATE DichVu SET MoTa = N'Trám răng sâu bằng composite'                 WHERE MoTa = N'Tram rang sau bang composite';
    UPDATE DichVu SET MoTa = N'Tiểu phẫu nhổ răng khôn mọc lệch'             WHERE MoTa = N'Tieu phau nho rang khon moc lech';
    UPDATE DichVu SET MoTa = N'Bọc răng sứ thẩm mỹ / 1 răng'                 WHERE MoTa = N'Boc rang su tham my / 1 rang';
    UPDATE DichVu SET MoTa = N'Lấy tủy và trám bít ống tủy'                  WHERE MoTa = N'Lay tuy va tram bit ong tuy';
    UPDATE DichVu SET MoTa = N'Tẩy trắng tại phòng khám'                     WHERE MoTa = N'Tay trang tai phong kham';
    UPDATE DichVu SET MoTa = N'Nhổ răng vĩnh viễn không phẫu thuật'          WHERE MoTa = N'Nho rang vinh vien khong phau thuat';
    UPDATE DichVu SET MoTa = N'Cấy ghép trụ Implant và phục hình'            WHERE MoTa = N'Cay ghep tru Implant va phuc hinh';
    UPDATE DichVu SET MoTa = N'Hàn trám phục hình thân răng'                 WHERE MoTa = N'Han tram phuc hinh than rang';
    UPDATE DichVu SET MoTa = N'Tẩy trắng răng bằng đèn LED'                  WHERE MoTa = N'Tay trang rang bang den LED';
    UPDATE DichVu SET MoTa = N'Lấy cao và đánh bóng bằng máy siêu âm'        WHERE MoTa = N'Lay cao va danh bong bang may sieu am';
    UPDATE DichVu SET MoTa = N'Phim panorama răng toàn hàm'                  WHERE MoTa = N'Phim panorama rang toan ham';
    UPDATE DichVu SET MoTa = N'Chụp CT 3D cho cấy ghép / phẫu thuật'         WHERE MoTa = N'Chup CT 3D cho cay ghep / phau thuat';
    UPDATE DichVu SET MoTa = N'Điệp răng bằng chỉ nha khoa'                  WHERE MoTa = N'Diep rang bang chi nha khoa';
    UPDATE DichVu SET MoTa = N'Phục hình răng sứ kim loại'                   WHERE MoTa = N'Phuc hinh rang su kim loai';
    UPDATE DichVu SET MoTa = N'Phục hình răng toàn sứ thẩm mỹ'               WHERE MoTa = N'Phuc hinh rang toan su tham my';
    UPDATE DichVu SET MoTa = N'Hàm duy trì sau chỉnh nha'                    WHERE MoTa = N'Ham duy tri sau chinh nha';
    UPDATE DichVu SET MoTa = N'Điều trị chỉnh nha bằng mắc cài kim loại'     WHERE MoTa = N'Dieu tri chinh nha bang mac cai kim loai';
    UPDATE DichVu SET MoTa = N'Nhổ răng sữa cho trẻ em'                      WHERE MoTa = N'Nho rang su cho tre em';

    /* ============================ VAT TU ============================ */
    UPDATE VatTu SET TenVatTu = N'Găng tay y tế'                    WHERE TenVatTu = N'Gang tay y te';
    UPDATE VatTu SET TenVatTu = N'Composite trám răng'              WHERE TenVatTu = N'Composite tram rang';
    UPDATE VatTu SET TenVatTu = N'Thuốc tê Lidocaine 2%'            WHERE TenVatTu = N'Thuoc te Lidocaine 2%';
    UPDATE VatTu SET TenVatTu = N'Mũ sứ (răng sứ)'                  WHERE TenVatTu = N'Mu su (rang su)';
    UPDATE VatTu SET TenVatTu = N'Xi măng trám răng'                WHERE TenVatTu = N'Xi mang tram rang';
    UPDATE VatTu SET TenVatTu = N'Kim tiêm nha khoa'                WHERE TenVatTu = N'Kim tiem nha khoa';
    UPDATE VatTu SET TenVatTu = N'Chỉ khâu phẫu thuật'              WHERE TenVatTu = N'Chi khau phau thuat';
    UPDATE VatTu SET TenVatTu = N'Gạc vô trùng'                     WHERE TenVatTu = N'Gau vo trung';
    UPDATE VatTu SET TenVatTu = N'Nước súc miệng Chlorhexidine'     WHERE TenVatTu = N'Nuoc suc mieng Chlorhexidine';
    UPDATE VatTu SET TenVatTu = N'Bộ dụng cụ khám răng'             WHERE TenVatTu = N'Bo dung cu kham rang';
    UPDATE VatTu SET TenVatTu = N'Bộ dụng cụ nhổ răng'              WHERE TenVatTu = N'Bo dung cu nho rang';
    UPDATE VatTu SET TenVatTu = N'Đầu kim trám răng'                WHERE TenVatTu = N'Dau kim tram rang';
    UPDATE VatTu SET TenVatTu = N'Nắp răng sứ'                      WHERE TenVatTu = N'Nap rang su';
    UPDATE VatTu SET TenVatTu = N'Cặp răng sứ'                      WHERE TenVatTu = N'Cap rang su';
    UPDATE VatTu SET TenVatTu = N'Thuốc tê Articaine 4%'            WHERE TenVatTu = N'Thuoc te Articaine 4%';

    UPDATE VatTu SET LoaiVatTu = N'Thuốc'      WHERE LoaiVatTu = N'Thuoc';
    UPDATE VatTu SET LoaiVatTu = N'Tiêu hao'   WHERE LoaiVatTu = N'Tieu hao';
    UPDATE VatTu SET LoaiVatTu = N'Vật liệu'   WHERE LoaiVatTu = N'Vat lieu';
    UPDATE VatTu SET LoaiVatTu = N'Dụng cụ'    WHERE LoaiVatTu = N'Dung cu';

    UPDATE VatTu SET DonViTinh = N'Viên' WHERE DonViTinh = N'Vien';
    UPDATE VatTu SET DonViTinh = N'Hộp'  WHERE DonViTinh = N'Hop';
    UPDATE VatTu SET DonViTinh = N'Tuýp' WHERE DonViTinh = N'Tuyp';
    UPDATE VatTu SET DonViTinh = N'Cái'  WHERE DonViTinh = N'Cai';
    UPDATE VatTu SET DonViTinh = N'Ống'  WHERE DonViTinh IN (N'Ong', N'ong');
    UPDATE VatTu SET DonViTinh = N'Gói'  WHERE DonViTinh = N'Goi';
    UPDATE VatTu SET DonViTinh = N'Bộ'   WHERE DonViTinh = N'Bo';
    UPDATE VatTu SET DonViTinh = N'Cặp'  WHERE DonViTinh = N'Cap';

    /* ============================ PHONG KHAM / VAI TRO ============================ */
    UPDATE PhongKham SET TenPhong = N'Phòng khám số 1'     WHERE TenPhong = N'Phong kham so 1';
    UPDATE PhongKham SET TenPhong = N'Phòng khám số 2'     WHERE TenPhong = N'Phong kham so 2';
    UPDATE PhongKham SET TenPhong = N'Phòng tiểu phẫu'     WHERE TenPhong = N'Phong tieu phau';
    UPDATE PhongKham SET TenPhong = N'Phòng khám số 3'     WHERE TenPhong = N'Phong kham so 3';
    UPDATE PhongKham SET TenPhong = N'Phòng chỉnh nha'     WHERE TenPhong = N'Phong chinh nha';
    UPDATE PhongKham SET TenPhong = N'Phòng X-quang'       WHERE TenPhong = N'Phong X-quang';
    UPDATE PhongKham SET TenPhong = N'Phòng tiểu phẫu 2'   WHERE TenPhong = N'Phong tieu phau 2';

    UPDATE VaiTro SET TenVaiTro = N'Quản trị viên'   WHERE TenVaiTro = N'Quan tri vien';
    UPDATE VaiTro SET TenVaiTro = N'Bác sĩ'          WHERE TenVaiTro = N'Bac si';
    UPDATE VaiTro SET TenVaiTro = N'Y tá'            WHERE TenVaiTro = N'Y ta';
    UPDATE VaiTro SET TenVaiTro = N'Lễ tân'          WHERE TenVaiTro = N'Le tan';
    UPDATE VaiTro SET TenVaiTro = N'Thu ngân'        WHERE TenVaiTro = N'Thu ngan';
    UPDATE VaiTro SET TenVaiTro = N'Nhân viên kho'   WHERE TenVaiTro = N'Nhan vien kho';

    /* ============================ NHA CUNG CAP ============================ */
    UPDATE PhieuNhapKho SET NhaCungCap = N'Công ty Dược phẩm An Khang'     WHERE NhaCungCap = N'Cong ty Duoc pham An Khang';
    UPDATE PhieuNhapKho SET NhaCungCap = N'Công ty Vật tư Nha khoa Việt'   WHERE NhaCungCap = N'Cong ty Vat tu Nha khoa Viet';
    UPDATE PhieuNhapKho SET NhaCungCap = N'Dược phẩm An Khang'             WHERE NhaCungCap = N'Duoc pham An Khang';
    UPDATE PhieuNhapKho SET NhaCungCap = N'Dược phẩm Minh Châu'            WHERE NhaCungCap = N'Duoc pham Minh Chau';
    UPDATE PhieuNhapKho SET NhaCungCap = N'Thiết bị y tế Hoàng Long'       WHERE NhaCungCap = N'Thiet bi y te Hoang Long';
    UPDATE PhieuNhapKho SET NhaCungCap = N'Thiết bị y tế Việt'             WHERE NhaCungCap = N'Thiet bi Y te Viet';
    UPDATE PhieuNhapKho SET NhaCungCap = N'Vật tư Nha khoa Sài Gòn'        WHERE NhaCungCap = N'Vat tu Nha khoa Sai Gon';
    UPDATE PhieuNhapKho SET NhaCungCap = N'Vật tư Nha khoa Việt'           WHERE NhaCungCap = N'Vat tu Nha khoa Viet';

    /* ============================ SO DO RANG ============================ */
    UPDATE SoDoRang SET LoaiRang = N'Răng cửa'   WHERE LoaiRang = N'Rang cua';
    UPDATE SoDoRang SET LoaiRang = N'Răng hàm'   WHERE LoaiRang = N'Rang hang';
    UPDATE SoDoRang SET LoaiRang = N'Răng khôn'  WHERE LoaiRang = N'Rang khon';
    UPDATE SoDoRang SET LoaiRang = N'Răng nanh'  WHERE LoaiRang = N'Rang nanh';
    UPDATE SoDoRang SET LoaiRang = N'Răng sữa'   WHERE LoaiRang LIKE N'Rang s%a';

    UPDATE SoDoRang SET TinhTrangBenhLy = N'Bình thường'     WHERE TinhTrangBenhLy = N'Binh thuong';
    UPDATE SoDoRang SET TinhTrangBenhLy = N'Cao răng'        WHERE TinhTrangBenhLy = N'Cao rang';
    UPDATE SoDoRang SET TinhTrangBenhLy = N'Chen chúc'       WHERE TinhTrangBenhLy = N'Chen chuc';
    UPDATE SoDoRang SET TinhTrangBenhLy = N'Mất răng'        WHERE TinhTrangBenhLy = N'Mat rang';
    UPDATE SoDoRang SET TinhTrangBenhLy = N'Mẻ răng'         WHERE TinhTrangBenhLy = N'Me rang';
    UPDATE SoDoRang SET TinhTrangBenhLy = N'Mọc lệch'        WHERE TinhTrangBenhLy = N'Moc lech';
    UPDATE SoDoRang SET TinhTrangBenhLy = N'Răng nhiễm màu'  WHERE TinhTrangBenhLy = N'Rang nhiem mau';
    UPDATE SoDoRang SET TinhTrangBenhLy = N'Sâu răng'        WHERE TinhTrangBenhLy LIKE N'S%u rang';
    UPDATE SoDoRang SET TinhTrangBenhLy = N'Viêm lợi'        WHERE TinhTrangBenhLy = N'Viem loi';
    UPDATE SoDoRang SET TinhTrangBenhLy = N'Viêm nha chu'    WHERE TinhTrangBenhLy = N'Viem nha chu';
    UPDATE SoDoRang SET TinhTrangBenhLy = N'Viêm tủy'        WHERE TinhTrangBenhLy = N'Viem tuy';

    UPDATE SoDoRang SET GhiChu = N'Cần cấy ghép Implant'          WHERE GhiChu = N'Can cay ghep Implant';
    UPDATE SoDoRang SET GhiChu = N'Cần điều trị nha chu'          WHERE GhiChu = N'Can dieu tri nha chu';
    UPDATE SoDoRang SET GhiChu = N'Cần điều trị tủy'              WHERE GhiChu = N'Can dieu tri tuy';
    UPDATE SoDoRang SET GhiChu = N'Cần lấy cao'                   WHERE GhiChu = N'Can lay cao';
    UPDATE SoDoRang SET GhiChu = N'Cần nhổ'                       WHERE GhiChu = N'Can nho';
    UPDATE SoDoRang SET GhiChu = N'Cần nhổ răng khôn'             WHERE GhiChu = N'Can nho rang khon';
    UPDATE SoDoRang SET GhiChu = N'Cần phục hình'                 WHERE GhiChu = N'Can phuc hinh';
    UPDATE SoDoRang SET GhiChu = N'Cần tẩy trắng'                 WHERE GhiChu = N'Can tay trang';
    UPDATE SoDoRang SET GhiChu = N'Cần trám composite'            WHERE GhiChu = N'Can tram composite';
    UPDATE SoDoRang SET GhiChu = N'Cần trám răng'                 WHERE GhiChu = N'Can tram rang';
    UPDATE SoDoRang SET GhiChu = N'Cần vệ sinh răng miệng'        WHERE GhiChu = N'Can ve sinh rang mieng';
    UPDATE SoDoRang SET GhiChu = N'Chỉ định chỉnh nha'            WHERE GhiChu = N'Chi dinh chinh nha';
    UPDATE SoDoRang SET GhiChu = N'Chỉ định nhổ'                  WHERE GhiChu = N'Chi dinh nho';
    UPDATE SoDoRang SET GhiChu = N'Chỉ định nhổ răng khôn'        WHERE GhiChu = N'Chi dinh nho rang khon';
    UPDATE SoDoRang SET GhiChu = N'Dự kiến bọc sứ'                WHERE GhiChu = N'Du kien boc su';
    UPDATE SoDoRang SET GhiChu = N'Dự kiến trám răng'             WHERE GhiChu = N'Du kien tram rang';
    UPDATE SoDoRang SET GhiChu = N'Sâu khó điều trị'              WHERE GhiChu LIKE N'S%u kh% t%';

    /* ============================ CHAN DOAN ============================ */
    UPDATE ChanDoan SET LyDoKham = N'Đau nhức răng hàm dưới'       WHERE LyDoKham = N'Dau nhuc rang ham duoi';
    UPDATE ChanDoan SET LyDoKham = N'Đau răng'                     WHERE LyDoKham = N'Dau rang';
    UPDATE ChanDoan SET LyDoKham = N'Đau răng đêm'                 WHERE LyDoKham = N'Dau rang dem';
    UPDATE ChanDoan SET LyDoKham = N'Đau răng hàm dưới'            WHERE LyDoKham = N'Dau rang ham duoi';
    UPDATE ChanDoan SET LyDoKham = N'Đau răng khôn'                WHERE LyDoKham = N'Dau rang khon';
    UPDATE ChanDoan SET LyDoKham = N'Cao răng'                     WHERE LyDoKham = N'Cao rang';
    UPDATE ChanDoan SET LyDoKham = N'Cao răng, chảy máu lợi'       WHERE LyDoKham = N'Cao rang, chay mau loi';
    UPDATE ChanDoan SET LyDoKham = N'Chảy máu chân răng'           WHERE LyDoKham = N'Chay mau chan rang';
    UPDATE ChanDoan SET LyDoKham = N'Khám định kỳ cho bé'          WHERE LyDoKham = N'Kham dinh ky cho be';
    UPDATE ChanDoan SET LyDoKham = N'Kiểm tra sau điều trị nha chu' WHERE LyDoKham = N'Kiem tra sau dieu tri nha chu';
    UPDATE ChanDoan SET LyDoKham = N'Mất răng cửa'                 WHERE LyDoKham = N'Mat rang cua';
    UPDATE ChanDoan SET LyDoKham = N'Mất răng hàm dưới'            WHERE LyDoKham = N'Mat rang ham duoi';
    UPDATE ChanDoan SET LyDoKham = N'Mất răng số 24'               WHERE LyDoKham = N'Mat rang so 24';
    UPDATE ChanDoan SET LyDoKham = N'Răng chen chúc'               WHERE LyDoKham = N'Rang chen chuc';
    UPDATE ChanDoan SET LyDoKham = N'Răng cửa bị mẻ'               WHERE LyDoKham = N'Rang cua bi me';
    UPDATE ChanDoan SET LyDoKham = N'Răng nhiễm màu'               WHERE LyDoKham = N'Rang nhiem mau';
    UPDATE ChanDoan SET LyDoKham = N'Răng vàng'                    WHERE LyDoKham = N'Rang vang';
    UPDATE ChanDoan SET LyDoKham = N'Sâu răng số 26'               WHERE LyDoKham = N'Sau rang so 26';
    UPDATE ChanDoan SET LyDoKham = N'Sâu răng sữa'                 WHERE LyDoKham = N'Sau rang sua';
    UPDATE ChanDoan SET LyDoKham = N'Sưng lợi răng cửa'            WHERE LyDoKham = N'Sung loi rang cua';
    UPDATE ChanDoan SET LyDoKham = N'Tái khám sau điều trị'        WHERE LyDoKham = N'Tai kham sau dieu tri';

    UPDATE ChanDoan SET TrieuChung = N'Đau khi ăn nhai, ê buốt'          WHERE TrieuChung = N'Dau khi an nhai, e buot';
    UPDATE ChanDoan SET TrieuChung = N'Đau âm ỉ, sưng lợi'               WHERE TrieuChung = N'Dau am i, sung loi';
    UPDATE ChanDoan SET TrieuChung = N'Đau âm ỉ khi ăn nóng'             WHERE TrieuChung = N'Dau am i khi an nong';
    UPDATE ChanDoan SET TrieuChung = N'Sưng nhẹ, dễ chảy máu'            WHERE TrieuChung = N'Sung nhe, de chay mau';
    UPDATE ChanDoan SET TrieuChung = N'Lợi sưng đỏ, dễ chảy máu'         WHERE TrieuChung = N'Loi sung do, de chay mau';
    UPDATE ChanDoan SET TrieuChung = N'Sưng, khó mở miệng'               WHERE TrieuChung = N'Sung, kho mo mieng';
    UPDATE ChanDoan SET TrieuChung = N'Cao răng vàng, lợi sưng'          WHERE TrieuChung = N'Cao rang vang, loi sung';
    UPDATE ChanDoan SET TrieuChung = N'Cao răng vàng, hôi thở'           WHERE TrieuChung = N'Cao rang vang, hoi tho';
    UPDATE ChanDoan SET TrieuChung = N'Cao răng, hôi thở'                WHERE TrieuChung = N'Cao rang, hoi tho';
    UPDATE ChanDoan SET TrieuChung = N'Cao răng, lợi sưng'               WHERE TrieuChung = N'Cao rang, loi sung';
    UPDATE ChanDoan SET TrieuChung = N'Đau, răng bên trái'               WHERE TrieuChung = N'Dau, rang ben trai';
    UPDATE ChanDoan SET TrieuChung = N'Đau nhức răng bên trái'           WHERE TrieuChung LIKE N'Dau nh%t rang b%n tr%i';
    UPDATE ChanDoan SET TrieuChung = N'Khó ăn nhai'                       WHERE TrieuChung = N'Kho an nhai';
    UPDATE ChanDoan SET TrieuChung = N'Khó ăn nhai, mất thẩm mỹ'          WHERE TrieuChung = N'Kho an nhai, mat tham my';
    UPDATE ChanDoan SET TrieuChung = N'Không'                             WHERE TrieuChung = N'Khong';
    UPDATE ChanDoan SET TrieuChung = N'Không đau'                         WHERE TrieuChung = N'Khong dau';
    UPDATE ChanDoan SET TrieuChung = N'Lở răng'                           WHERE TrieuChung = N'Lo rang';
    UPDATE ChanDoan SET TrieuChung = N'Lở răng, khó ăn nhai'              WHERE TrieuChung = N'Lo rang, kho an nhai';
    UPDATE ChanDoan SET TrieuChung = N'Lợi đỏ, không chảy máu'            WHERE TrieuChung = N'Loi do, khong chay mau';
    UPDATE ChanDoan SET TrieuChung = N'Lợi sưng, chảy máu khi đánh răng'  WHERE TrieuChung = N'Loi sung, chay mau khi danh rang';
    UPDATE ChanDoan SET TrieuChung = N'Mất răng 11, khó phát âm'          WHERE TrieuChung = N'Mat rang 11, kho phat am';
    UPDATE ChanDoan SET TrieuChung = N'Mẻ răng, mất thẩm mỹ'              WHERE TrieuChung = N'Me rang, mat tham my';
    UPDATE ChanDoan SET TrieuChung = N'Răng mới chen chúc'                WHERE TrieuChung = N'Rang moi chen chuc';
    UPDATE ChanDoan SET TrieuChung = N'Răng mới chen chúc, khó vệ sinh'   WHERE TrieuChung = N'Rang moi chen chuc, kho ve sinh';
    UPDATE ChanDoan SET TrieuChung = N'Răng nhiễm màu'                    WHERE TrieuChung = N'Rang nhiem mau';
    UPDATE ChanDoan SET TrieuChung = N'Răng nhiễm màu, không đau'         WHERE TrieuChung = N'Rang nhiem mau, khong dau';
    UPDATE ChanDoan SET TrieuChung = N'Răng vàng, không đau'              WHERE TrieuChung = N'Rang vang, khong dau';

    UPDATE ChanDoan SET KetLuanChanDoan = N'Sâu răng 16, cao răng nhiều'          WHERE KetLuanChanDoan = N'Sau rang 16, cao rang nhieu';
    UPDATE ChanDoan SET KetLuanChanDoan = N'Sâu răng số 16, cao răng nhiều'       WHERE KetLuanChanDoan = N'Sau rang so 16, cao rang nhieu';
    UPDATE ChanDoan SET KetLuanChanDoan = N'Viêm lợi nhẹ, cần vệ sinh răng miệng' WHERE KetLuanChanDoan = N'Viem loi nhe, can ve sinh rang mieng';
    UPDATE ChanDoan SET KetLuanChanDoan = N'Viêm tủy răng 46'                     WHERE KetLuanChanDoan = N'Viem tuy rang 46';
    UPDATE ChanDoan SET KetLuanChanDoan = N'Viêm tủy răng số 46'                  WHERE KetLuanChanDoan = N'Viem tuy rang so 46';
    UPDATE ChanDoan SET KetLuanChanDoan = N'Viêm lợi, cần điều trị nha chu'       WHERE KetLuanChanDoan = N'Viem loi, can dieu tri nha chu';
    UPDATE ChanDoan SET KetLuanChanDoan = N'Răng nhiễm màu, chỉ định tẩy trắng'   WHERE KetLuanChanDoan = N'Rang nhiem mau, chi dinh tay trang';
    UPDATE ChanDoan SET KetLuanChanDoan = N'Sâu răng sữa số 55 mức độ nhẹ'        WHERE KetLuanChanDoan = N'Sau rang sua so 55 muc do nhe';
    UPDATE ChanDoan SET KetLuanChanDoan = N'Răng khôn 38 mọc lệch, cần nhổ'       WHERE KetLuanChanDoan = N'Rang khon 38 moc lech, can nho';
    UPDATE ChanDoan SET KetLuanChanDoan = N'Cao răng, viêm nha chu nhẹ'           WHERE KetLuanChanDoan = N'Cao rang, viem nha chu nhe';
    UPDATE ChanDoan SET KetLuanChanDoan = N'Mất răng 24, cần phục hình'           WHERE KetLuanChanDoan = N'Mat rang 24, can phuc hinh';
    UPDATE ChanDoan SET KetLuanChanDoan = N'Răng chen chúc, chỉ định chỉnh nha'   WHERE KetLuanChanDoan = N'Rang chen chuc, chi dinh chinh nha';
    UPDATE ChanDoan SET KetLuanChanDoan = N'Răng khôn 48 mọc lệch, cần nhổ'       WHERE KetLuanChanDoan = N'Rang khon 48 moc lech, can nho';
    UPDATE ChanDoan SET KetLuanChanDoan = N'Mất răng 36, chỉ định cấy ghép'       WHERE KetLuanChanDoan = N'Mat rang 36, chi dinh cay ghep';
    UPDATE ChanDoan SET KetLuanChanDoan = N'Sâu răng sữa 54 và 64'                WHERE KetLuanChanDoan = N'Sau rang sua 54 va 64';
    UPDATE ChanDoan SET KetLuanChanDoan = N'Mất răng 11, cần phục hình'           WHERE KetLuanChanDoan = N'Mat rang 11, can phuc hinh';
    UPDATE ChanDoan SET KetLuanChanDoan = N'Cao răng, viêm lợi nhẹ'               WHERE KetLuanChanDoan = N'Cao rang, viem loi nhe';
    UPDATE ChanDoan SET KetLuanChanDoan = N'Sâu răng 26, cần trám răng'           WHERE KetLuanChanDoan = N'Sau rang 26, can tram rang';
    UPDATE ChanDoan SET KetLuanChanDoan = N'Viêm nha chu, cần điều trị'           WHERE KetLuanChanDoan = N'Viem nha chu, can dieu tri';
    UPDATE ChanDoan SET KetLuanChanDoan = N'Cao răng, cần lấy cao'                WHERE KetLuanChanDoan = N'Cao rang, can lay cao';
    UPDATE ChanDoan SET KetLuanChanDoan = N'Răng khôn 18 mọc lệch, cần nhổ'       WHERE KetLuanChanDoan = N'Rang khon 18 moc lech, can nho';
    UPDATE ChanDoan SET KetLuanChanDoan = N'Mất răng 46, chỉ định Implant'        WHERE KetLuanChanDoan = N'Mat rang 46, chi dinh Implant';
    UPDATE ChanDoan SET KetLuanChanDoan = N'Sâu răng 64, cần trám răng'           WHERE KetLuanChanDoan = N'Sau rang 64, can tram rang';
    UPDATE ChanDoan SET KetLuanChanDoan = N'Ổn định, tiếp tục theo dõi'           WHERE KetLuanChanDoan = N'On dinh, tiep tuc theo doi';
    UPDATE ChanDoan SET KetLuanChanDoan = N'Viêm lợi đã giảm, tiếp tục vệ sinh'   WHERE KetLuanChanDoan = N'Viem loi da giam, tiep tuc ve sinh';
    UPDATE ChanDoan SET KetLuanChanDoan = N'Sâu răng 55, cần trám răng'           WHERE KetLuanChanDoan = N'Sau rang 55, can tram rang';
    UPDATE ChanDoan SET KetLuanChanDoan = N'Mẻ răng cửa số 11, cần bọc sứ'        WHERE KetLuanChanDoan = N'Me rang cua so 11, can boc su';
    UPDATE ChanDoan SET KetLuanChanDoan = N'Sâu răng sữa bên trái'                WHERE KetLuanChanDoan LIKE N'S%u rang s%a b%n%tr%i';
    UPDATE ChanDoan SET KetLuanChanDoan = N'Sâu răng sữa bên trái'                WHERE KetLuanChanDoan = N'Sau rang sua ben trai';

    /* ============================ KE HOACH DIEU TRI ============================ */
    UPDATE KeHoachDieuTri SET TenPhacDo = N'Lấy cao & trám răng 16'     WHERE TenPhacDo = N'Lay cao & tram rang 16';
    UPDATE KeHoachDieuTri SET TenPhacDo = N'Bọc sứ răng cửa 11'         WHERE TenPhacDo = N'Boc su rang cua 11';
    UPDATE KeHoachDieuTri SET TenPhacDo = N'Điều trị tủy răng 46'       WHERE TenPhacDo = N'Dieu tri tuy rang 46';
    UPDATE KeHoachDieuTri SET TenPhacDo = N'Trám răng sữa 55'           WHERE TenPhacDo = N'Tram rang sua 55';
    UPDATE KeHoachDieuTri SET TenPhacDo = N'Nhổ răng khôn 38'           WHERE TenPhacDo = N'Nho rang khon 38';
    UPDATE KeHoachDieuTri SET TenPhacDo = N'Cạo vôi & điều trị nha chu' WHERE TenPhacDo = N'Cao voi & dieu tri nha chu';
    UPDATE KeHoachDieuTri SET TenPhacDo = N'Phục hình răng 24'          WHERE TenPhacDo = N'Phuc hinh rang 24';
    UPDATE KeHoachDieuTri SET TenPhacDo = N'Chỉnh nha mắc cài'          WHERE TenPhacDo = N'Chinh nha mac cai';
    UPDATE KeHoachDieuTri SET TenPhacDo = N'Nhổ răng khôn 48'           WHERE TenPhacDo = N'Nho rang khon 48';
    UPDATE KeHoachDieuTri SET TenPhacDo = N'Tẩy trắng răng'             WHERE TenPhacDo = N'Tay trang rang';
    UPDATE KeHoachDieuTri SET TenPhacDo = N'Cấy ghép Implant 36'        WHERE TenPhacDo = N'Cay ghep Implant 36';
    UPDATE KeHoachDieuTri SET TenPhacDo = N'Trám răng sữa 54 & 64'      WHERE TenPhacDo = N'Tram rang sua 54 & 64';
    UPDATE KeHoachDieuTri SET TenPhacDo = N'Răng toàn sứ 11'            WHERE TenPhacDo = N'Nha rang toan su 11';
    UPDATE KeHoachDieuTri SET TenPhacDo = N'Cạo vôi răng'               WHERE TenPhacDo = N'Cao voi rang';
    UPDATE KeHoachDieuTri SET TenPhacDo = N'Hàn trám răng 26'           WHERE TenPhacDo = N'Han tram rang 26';
    UPDATE KeHoachDieuTri SET TenPhacDo = N'Điều trị nha chu'           WHERE TenPhacDo = N'Dieu tri nha chu';
    UPDATE KeHoachDieuTri SET TenPhacDo = N'Trám răng sữa 64'           WHERE TenPhacDo = N'Tram rang sua 64';
    UPDATE KeHoachDieuTri SET TenPhacDo = N'Nhổ răng khôn 18'           WHERE TenPhacDo = N'Nho rang khon 18';
    UPDATE KeHoachDieuTri SET TenPhacDo = N'Cấy ghép Implant 46'        WHERE TenPhacDo = N'Cay ghep Implant 46';
    UPDATE KeHoachDieuTri SET TenPhacDo = N'Lấy cao & trám răng'        WHERE TenPhacDo = N'Lay cao & tram rang';
    UPDATE KeHoachDieuTri SET TenPhacDo = N'Chụp X-quang & điệp răng'   WHERE TenPhacDo = N'Chup X-quang & diep rang';
    UPDATE KeHoachDieuTri SET TenPhacDo = N'Nhổ răng'                   WHERE TenPhacDo = N'Nho rang';
    UPDATE KeHoachDieuTri SET TenPhacDo = N'Nhổ răng thường'            WHERE TenPhacDo LIKE N'Nh% rang thu%ng';

    /* ============================ DON THUOC ============================ */
    UPDATE DonThuoc SET LoiDanChamSoc = N'Uống sau ăn, không ăn đồ cứng trong 24h' WHERE LoiDanChamSoc = N'Uong sau an, khong an do cung trong 24h';
    UPDATE DonThuoc SET LoiDanChamSoc = N'Vệ sinh răng miệng nhẹ nhàng'            WHERE LoiDanChamSoc = N'Ve sinh rang mieng nhe nhang';
    UPDATE DonThuoc SET LoiDanChamSoc = N'Tái khám sau 3 ngày'                     WHERE LoiDanChamSoc = N'Tai kham sau 3 ngay';
    UPDATE DonThuoc SET LoiDanChamSoc = N'Chỉ dùng thuốc khi trẻ đau'              WHERE LoiDanChamSoc = N'Chi dung thuoc khi tre dau';
    UPDATE DonThuoc SET LoiDanChamSoc = N'Chườm lạnh 15 phút mỗi ngày'             WHERE LoiDanChamSoc = N'Chuom lanh 15 phut moi ngay';
    UPDATE DonThuoc SET LoiDanChamSoc = N'Súc miệng 2 lần/ngày'                    WHERE LoiDanChamSoc = N'Suc mieng 2 lan/ngay';
    UPDATE DonThuoc SET LoiDanChamSoc = N'Tái khám sau 1 tuần'                     WHERE LoiDanChamSoc = N'Tai kham sau 1 tuan';
    UPDATE DonThuoc SET LoiDanChamSoc = N'Vệ sinh răng miệng kỹ'                   WHERE LoiDanChamSoc = N'Ve sinh rang mieng ky';
    UPDATE DonThuoc SET LoiDanChamSoc = N'Uống nhiều nước, nghỉ ngơi'              WHERE LoiDanChamSoc = N'Uong nhieu nuoc, nghi ngoi';
    UPDATE DonThuoc SET LoiDanChamSoc = N'Tránh đồ ăn có màu trong 24h'            WHERE LoiDanChamSoc = N'Tranh do an co mau trong 24h';
    UPDATE DonThuoc SET LoiDanChamSoc = N'Chăm sóc sau cấy ghép theo hướng dẫn'   WHERE LoiDanChamSoc = N'Cham soc sau cay ghep theo huong dan';
    UPDATE DonThuoc SET LoiDanChamSoc = N'Thay bàn chải sau 3 tháng'               WHERE LoiDanChamSoc = N'Thay ban chai sau 3 thang';
    UPDATE DonThuoc SET LoiDanChamSoc = N'Không ăn đồ cứng trong 24h'              WHERE LoiDanChamSoc = N'Khong an do cung trong 24h';
    UPDATE DonThuoc SET LoiDanChamSoc = N'Súc miệng bằng nước muối loãng'          WHERE LoiDanChamSoc = N'Suc mieng bang nuoc muoi loang';
    UPDATE DonThuoc SET LoiDanChamSoc = N'Chải răng đúng cách, lực 2 phút'         WHERE LoiDanChamSoc = N'Chai rang dung cach, luc 2 phut';
    UPDATE DonThuoc SET LoiDanChamSoc = N'Uống thuốc đúng liều, tái khám sau 3 ngày' WHERE LoiDanChamSoc = N'Uong thuoc dung lieu, tai kham sau 3 ngay';
    UPDATE DonThuoc SET LoiDanChamSoc = N'Vệ sinh răng miệng 2 lần/ngày'           WHERE LoiDanChamSoc = N'Ve sinh rang mieng 2 lan/ngay';
    UPDATE DonThuoc SET LoiDanChamSoc = N'Uống sau khi ăn'                         WHERE LoiDanChamSoc LIKE N'u%ng sau khi an';

    UPDATE ChiTietDonThuoc SET LieuDung = N'1 viên khi đau'       WHERE LieuDung = N'1 vien khi dau';
    UPDATE ChiTietDonThuoc SET LieuDung = N'1 viên x 2 lần/ngày'  WHERE LieuDung = N'1 vien x 2 lan/ngay';
    UPDATE ChiTietDonThuoc SET LieuDung = N'1 viên x 3 lần/ngày'  WHERE LieuDung = N'1 vien x 3 lan/ngay';
    UPDATE ChiTietDonThuoc SET LieuDung = N'Súc miệng 2 lần/ngày' WHERE LieuDung = N'Suc mieng 2 lan/ngay';
    UPDATE ChiTietDonThuoc SET LieuDung = N'1 Ngày'               WHERE LieuDung = N'1 Ngay';

    UPDATE ChiTietDonThuoc SET DuongDung = N'Uống'       WHERE DuongDung LIKE N'U%ng';
    UPDATE ChiTietDonThuoc SET DuongDung = N'Súc miệng'  WHERE DuongDung = N'Suc mieng';
    UPDATE ChiTietDonThuoc SET DuongDung = N'Tiêm'       WHERE DuongDung LIKE N'Ti%m';

    /* ============================ CAU HINH ============================ */
    UPDATE CauHinhHeThong SET GiaTri = N'123 Nguyễn Văn Cừ, Quận 5, TP.HCM'
        WHERE MaCauHinh = N'DIA_CHI';

    COMMIT;
    PRINT N'Da cap nhat du lieu sang tieng Viet co dau.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK;
    THROW;
END CATCH
GO

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
    CONSTRAINT PK_PhongKham PRIMARY KEY (MaPhong)
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

CREATE TABLE TaiKhoan (
    TenDangNhap        NVARCHAR(50)   NOT NULL,
    MatKhauMaHoa       NVARCHAR(255)  NOT NULL,
    TrangThaiTaiKhoan  NVARCHAR(20)   NOT NULL DEFAULT (N'Kich hoat'),
    LanDangNhapCuoi    DATETIME2(0)   NULL,
    MaNhanVien         NVARCHAR(20)   NOT NULL,
    CONSTRAINT PK_TaiKhoan PRIMARY KEY (TenDangNhap),
    CONSTRAINT UQ_TaiKhoan_NhanVien UNIQUE (MaNhanVien),
    CONSTRAINT FK_TaiKhoan_NhanVien FOREIGN KEY (MaNhanVien) REFERENCES NhanVien(MaNhanVien)
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

CREATE TABLE ChanDoan (
    MaChanDoan          NVARCHAR(20)  NOT NULL,
    NgayKham            DATE          NOT NULL,
    LyDoKham            NVARCHAR(500) NULL,
    TrieuChung          NVARCHAR(1000) NULL,
    KetLuanChanDoan     NVARCHAR(1000) NULL,
    MaHoSo              NVARCHAR(20)  NOT NULL,
    MaNhaSi             NVARCHAR(20)  NOT NULL,
    CONSTRAINT PK_ChanDoan PRIMARY KEY (MaChanDoan),
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

CREATE TABLE LichHen (
    MaLichHen          NVARCHAR(20)  NOT NULL,
    ThoiGianHen        DATETIME2(0)  NOT NULL,
    TrangThai          NVARCHAR(30)  NOT NULL DEFAULT (N'Cho xac nhan'),
    NgayTao            DATE          NOT NULL DEFAULT (CAST(GETDATE() AS DATE)),
    MaNhanVienLap      NVARCHAR(20)  NOT NULL,
    MaNhaSiPhuTrach    NVARCHAR(20)  NOT NULL,
    MaBenhNhan         NVARCHAR(20)  NOT NULL,
    MaPhong            NVARCHAR(20)  NOT NULL,
    MaKeHoach          NVARCHAR(20)  NULL,
    CONSTRAINT PK_LichHen PRIMARY KEY (MaLichHen),
    CONSTRAINT FK_LichHen_NhanVienLap FOREIGN KEY (MaNhanVienLap) REFERENCES NhanVien(MaNhanVien),
    CONSTRAINT FK_LichHen_NhaSiPhuTrach FOREIGN KEY (MaNhaSiPhuTrach) REFERENCES NhanVien(MaNhanVien),
    CONSTRAINT FK_LichHen_BenhNhan FOREIGN KEY (MaBenhNhan) REFERENCES BenhNhan(MaBenhNhan),
    CONSTRAINT FK_LichHen_PhongKham FOREIGN KEY (MaPhong) REFERENCES PhongKham(MaPhong),
    CONSTRAINT FK_LichHen_KeHoachDieuTri FOREIGN KEY (MaKeHoach) REFERENCES KeHoachDieuTri(MaKeHoach)
);
GO

/* =====================================================================
   6. PHAN HE TAI CHINH, HOA DON & CONG NO TRA GOP
   (UC021-UC023)
   ===================================================================== */

CREATE TABLE HoaDon (
    MaHoaDon               NVARCHAR(20)  NOT NULL,
    NgayLap                DATE          NOT NULL DEFAULT (CAST(GETDATE() AS DATE)),
    TrangThaiThanhToan     NVARCHAR(30)  NOT NULL DEFAULT (N'Chua thanh toan'),
    PhuongThucThanhToan    NVARCHAR(30)  NULL,
    MaNhanVien             NVARCHAR(20)  NOT NULL,
    MaBenhNhan             NVARCHAR(20)  NOT NULL,
    CONSTRAINT PK_HoaDon PRIMARY KEY (MaHoaDon),
    CONSTRAINT FK_HoaDon_NhanVien FOREIGN KEY (MaNhanVien) REFERENCES NhanVien(MaNhanVien),
    CONSTRAINT FK_HoaDon_BenhNhan FOREIGN KEY (MaBenhNhan) REFERENCES BenhNhan(MaBenhNhan)
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

CREATE TABLE VatTu (
    MaVatTu           NVARCHAR(20)  NOT NULL,
    TenVatTu          NVARCHAR(150) NOT NULL,
    LoaiVatTu         NVARCHAR(50)  NULL,
    DonViTinh         NVARCHAR(20)  NULL,
    SoLuongTon        INT           NOT NULL DEFAULT (0) CHECK (SoLuongTon >= 0),
    NguongToiThieu    INT           NOT NULL DEFAULT (0),
    HanSuDung         DATE          NULL,
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

INSERT INTO NhanVien (MaNhanVien, HoTen, NgaySinh, GioiTinh, SoDienThoai, Email, DiaChi, NgayVaoLam, TrangThaiLamViec, ChuyenMon, SoChungChiHanhNghe) VALUES
(N'NV001', N'Nguyen Van An',   '1985-03-12', N'Nam', N'0901000001', N'an.nguyen@muse.vn',   N'Quan 1, TP.HCM',       '2020-01-05', N'Dang lam viec', N'Nha si - Nho rang',        N'CCHN-001'),
(N'NV002', N'Tran Thi Binh',   '1988-07-25', N'Nu',  N'0901000002', N'binh.tran@muse.vn',   N'Quan 3, TP.HCM',       '2021-02-10', N'Dang lam viec', N'Nha si - Chinh nha',       N'CCHN-002'),
(N'NV003', N'Le Minh Chau',    '1996-11-02', N'Nu',  N'0901000003', N'chau.le@muse.vn',     N'Quan Binh Thanh',      '2022-06-01', N'Dang lam viec', N'Le tan',                   NULL),
(N'NV004', N'Pham Quoc Dung',  '1994-05-18', N'Nam', N'0901000004', N'dung.pham@muse.vn',   N'Quan Go Vap',          '2022-09-15', N'Dang lam viec', N'Thu ngan',                 NULL),
(N'NV005', N'Hoang Thi Em',    '1992-12-30', N'Nu',  N'0901000005', N'em.hoang@muse.vn',    N'Quan Tan Binh',        '2023-03-20', N'Dang lam viec', N'Thu kho',                  NULL);
GO

INSERT INTO TaiKhoan (TenDangNhap, MatKhauMaHoa, TrangThaiTaiKhoan, LanDangNhapCuoi, MaNhanVien) VALUES
(N'nv001', N'$2a$10$mauMaHoaBcryptNV001xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', N'Kich hoat', '2026-10-16 08:15:00', N'NV001'),
(N'nv002', N'$2a$10$mauMaHoaBcryptNV002xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', N'Kich hoat', '2026-10-16 08:30:00', N'NV002'),
(N'nv003', N'$2a$10$mauMaHoaBcryptNV003xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', N'Kich hoat', '2026-10-17 07:50:00', N'NV003'),
(N'nv004', N'$2a$10$mauMaHoaBcryptNV004xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', N'Kich hoat', '2026-10-17 07:55:00', N'NV004'),
(N'nv005', N'$2a$10$mauMaHoaBcryptNV005xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', N'Kich hoat', NULL,                  N'NV005');
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

INSERT INTO ChanDoan (MaChanDoan, NgayKham, LyDoKham, TrieuChung, KetLuanChanDoan, MaHoSo, MaNhaSi) VALUES
(N'CD001', '2026-09-01', N'Dau nhuc rang ham duoi', N'Dau khi an nhai, e buot', N'Sau rang so 16, cao rang nhieu',       N'HS001', N'NV001'),
(N'CD002', '2026-09-05', N'Rang cua bi me',         N'Me rang, mat tham my',    N'Me rang cua so 11, can boc su',        N'HS002', N'NV002'),
(N'CD003', '2026-09-10', N'Dau rang dem',           N'Dau am i, sung loi',      N'Viem tuy rang so 46',                  N'HS003', N'NV001'),
(N'CD004', '2026-10-01', N'Kham dinh ky cho be',    N'Khong',                   N'Sau rang sua so 55 muc do nhe',        N'HS004', N'NV002');
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
INSERT INTO LichHen (MaLichHen, ThoiGianHen, TrangThai, NgayTao, MaNhanVienLap, MaNhaSiPhuTrach, MaBenhNhan, MaPhong, MaKeHoach) VALUES
(N'LH001', '2026-09-01 09:00:00', N'Hoan thanh',   '2026-08-28', N'NV003', N'NV001', N'BN001', N'P01', N'KH001'),
(N'LH002', '2026-09-05 14:00:00', N'Hoan thanh',   '2026-09-02', N'NV003', N'NV002', N'BN002', N'P02', N'KH002'),
(N'LH003', '2026-10-19 08:30:00', N'Da xac nhan',  '2026-10-15', N'NV003', N'NV001', N'BN003', N'P01', N'KH003'),
(N'LH004', '2026-10-19 14:00:00', N'Cho xac nhan', '2026-10-16', N'NV003', N'NV002', N'BN002', N'P02', N'KH002'),
(N'LH005', '2026-10-20 09:00:00', N'Cho xac nhan', '2026-10-17', N'NV003', N'NV002', N'BN004', N'P02', N'KH004');
GO

/* ---------- 6. Hoa don & cong no tra gop ---------- */
INSERT INTO HoaDon (MaHoaDon, NgayLap, TrangThaiThanhToan, PhuongThucThanhToan, MaNhanVien, MaBenhNhan) VALUES
(N'HD001', '2026-09-01', N'Da thanh toan',   N'Tien mat',     N'NV004', N'BN001'),
(N'HD002', '2026-09-05', N'Da thanh toan',   N'Chuyen khoan', N'NV004', N'BN002'),
(N'HD003', '2026-09-10', N'Dang tra gop',    N'Tra gop',      N'NV004', N'BN002'),
(N'HD004', '2026-10-01', N'Chua thanh toan', NULL,            N'NV004', N'BN004');
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
INSERT INTO VatTu (MaVatTu, TenVatTu, LoaiVatTu, DonViTinh, SoLuongTon, NguongToiThieu, HanSuDung) VALUES
(N'VT001', N'Amoxicillin 500mg',        N'Thuoc',    N'Vien', 200, 50,  '2027-06-30'),
(N'VT002', N'Paracetamol 500mg',        N'Thuoc',    N'Vien', 500, 100, '2027-12-31'),
(N'VT003', N'Gang tay y te',            N'Tieu hao', N'Hop',  40,  10,  '2028-01-01'),
(N'VT004', N'Composite tram rang',      N'Vat lieu', N'Tuyp', 25,  5,   '2027-03-15'),
(N'VT005', N'Thuoc te Lidocaine 2%',    N'Thuoc',    N'Ong',  60,  20,  '2027-02-28'),
(N'VT006', N'Mu su (rang su)',          N'Vat lieu', N'Cai',  15,  5,   NULL);
GO

INSERT INTO ChiTietDonThuoc (MaDonThuoc, MaVatTu, LieuDung, DuongDung, SoLuong) VALUES
(N'DT001', N'VT001', N'1 vien x 3 lan/ngay', N'Uong', 15),
(N'DT001', N'VT002', N'1 vien khi dau',      N'Uong', 10),
(N'DT002', N'VT002', N'1 vien khi dau',      N'Uong', 6),
(N'DT003', N'VT001', N'1 vien x 2 lan/ngay', N'Uong', 14),
(N'DT003', N'VT002', N'1 vien khi dau',      N'Uong', 10);
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

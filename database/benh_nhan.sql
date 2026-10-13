/*
    Cấu trúc và dữ liệu mẫu cho bảng BenhNhan. 
    Tên các cột tuân theo mục 2.1.2 của tài liệu N01_6_ApplicationDevelopment_ScreenFlow_DB. 
    Các dòng dữ liệu mẫu chỉ được chèn vào khi mã bệnh nhân tương ứng chưa tồn tại. 
*/

SET NOCOUNT ON;
SET XACT_ABORT ON;

IF OBJECT_ID(N'dbo.BenhNhan', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.BenhNhan
    (
        MaBenhNhan    VARCHAR(8)    NOT NULL,
        HoTen         NVARCHAR(100) NOT NULL,
        NgaySinh      DATE          NOT NULL,
        GioiTinh      NVARCHAR(10)  NOT NULL,
        SoDienThoai   VARCHAR(10)   NOT NULL,
        DiaChi        NVARCHAR(250) NULL,
        SoCCCD        CHAR(12)      NULL,
        TienSuBenhLy  NVARCHAR(500) NULL,
        ThongTinDiUng NVARCHAR(500) NULL,
        NguoiBaoHo    NVARCHAR(200) NULL,

        CONSTRAINT PK_BenhNhan PRIMARY KEY (MaBenhNhan),
        CONSTRAINT CK_BenhNhan_MaBenhNhan
            CHECK (MaBenhNhan LIKE 'BN[0-9][0-9][0-9][0-9][0-9][0-9]'),
        CONSTRAINT CK_BenhNhan_HoTen
            CHECK (LEN(LTRIM(RTRIM(HoTen))) BETWEEN 2 AND 100),
        CONSTRAINT CK_BenhNhan_NgaySinh
            CHECK (NgaySinh <= CONVERT(DATE, GETDATE())),
        CONSTRAINT CK_BenhNhan_GioiTinh
            CHECK (GioiTinh IN (N'Nam', N'Nữ', N'Khác')),
        CONSTRAINT CK_BenhNhan_SoDienThoai
            CHECK (SoDienThoai LIKE '0[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]'),
        CONSTRAINT CK_BenhNhan_SoCCCD
            CHECK (SoCCCD IS NULL OR SoCCCD LIKE '[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]')
    );
END;
GO

INSERT INTO dbo.BenhNhan
    (MaBenhNhan, HoTen, NgaySinh, GioiTinh, SoDienThoai, DiaChi, SoCCCD,
     TienSuBenhLy, ThongTinDiUng, NguoiBaoHo)
SELECT
    mau.MaBenhNhan, mau.HoTen, mau.NgaySinh, mau.GioiTinh, mau.SoDienThoai,
    mau.DiaChi, mau.SoCCCD, mau.TienSuBenhLy, mau.ThongTinDiUng, mau.NguoiBaoHo
FROM (VALUES
    ('BN000001', N'Nguyễn Thị Minh Anh', CONVERT(DATE, '19900512'), N'Nữ', '0901234567',
     N'12 Nguyễn Trãi, Quận 1', '079090123456', N'Không ghi nhận',
     N'Dị ứng penicillin', N'Nguyễn Văn An - 0901111222'),
    ('BN000002', N'Trần Hoàng Nam', CONVERT(DATE, '19851103'), N'Nam', '0912345678',
     N'25 Lê Lợi, Quận 3', '079085234567', N'Cao huyết áp',
     N'Chưa ghi nhận', N'Trần Thị Hoa - 0902222333'),
    ('BN000003', N'Lê Ngọc Hà', CONVERT(DATE, '20010219'), N'Nữ', '0987654321',
     N'8 Pasteur, Quận 1', '079201345678', N'Không ghi nhận',
     N'Dị ứng thuốc tê', NULL),
    ('BN000004', N'Phạm Gia Bảo', CONVERT(DATE, '20120724'), N'Nam', '0934567890',
     N'42 Võ Văn Tần, Quận 3', '079212456789', N'Hen suyễn',
     N'Dị ứng hải sản', N'Phạm Thị Mai - 0903333444'),
    ('BN000005', N'Đỗ Thanh Bình', CONVERT(DATE, '19780915'), N'Khác', '0978123456',
     N'17 Hai Bà Trưng, Quận 1', NULL, N'Tiểu đường type 2',
     N'Chưa ghi nhận', NULL)
) AS mau
(
    MaBenhNhan, HoTen, NgaySinh, GioiTinh, SoDienThoai, DiaChi, SoCCCD,
    TienSuBenhLy, ThongTinDiUng, NguoiBaoHo
)
WHERE NOT EXISTS
(
    SELECT 1
    FROM dbo.BenhNhan AS bn
    WHERE bn.MaBenhNhan = mau.MaBenhNhan
);
GO

-- Kiem tra tim kiem theo ma benh nhan, ho ten, so dien thoai va CCCD.
DECLARE @TestCases TABLE
(
    TenKiemThu      NVARCHAR(60)  NOT NULL,
    TuKhoa          NVARCHAR(100) NOT NULL,
    SoKetQuaMongDoi INT           NOT NULL
);

INSERT INTO @TestCases (TenKiemThu, TuKhoa, SoKetQuaMongDoi)
VALUES
    (N'Tìm theo mã bệnh nhân', N'BN000001', 1),
    (N'Tìm theo họ tên', N'Minh Anh', 1),
    (N'Tìm theo số điện thoại', N'0912345678', 1),
    (N'Tìm theo CCCD', N'079201345678', 1),
    (N'Từ khóa không có kết quả', N'KHONGTONTAI', 0);

;WITH KetQua AS
(
    SELECT
        tc.TenKiemThu,
        tc.TuKhoa,
        tc.SoKetQuaMongDoi,
        bn.MaBenhNhan,
        bn.HoTen,
        bn.NgaySinh,
        bn.GioiTinh,
        bn.SoDienThoai,
        bn.SoCCCD,
        COUNT(bn.MaBenhNhan) OVER (PARTITION BY tc.TenKiemThu) AS SoKetQuaThucTe
    FROM @TestCases AS tc
    LEFT JOIN dbo.BenhNhan AS bn
        ON bn.MaBenhNhan LIKE N'%' + tc.TuKhoa + N'%'
        OR bn.HoTen LIKE N'%' + tc.TuKhoa + N'%'
        OR bn.SoDienThoai LIKE N'%' + tc.TuKhoa + N'%'
        OR bn.SoCCCD LIKE N'%' + tc.TuKhoa + N'%'
)
SELECT
    TenKiemThu,
    TuKhoa,
    SoKetQuaMongDoi,
    MAX(SoKetQuaThucTe) AS SoKetQuaThucTe,
    CASE
        WHEN MAX(SoKetQuaThucTe) = SoKetQuaMongDoi THEN N'ĐẠT'
        ELSE N'KHÔNG ĐẠT'
    END AS KetQuaKiemThu,
    MaBenhNhan,
    HoTen,
    NgaySinh,
    GioiTinh,
    SoDienThoai,
    SoCCCD
FROM KetQua
GROUP BY
    TenKiemThu,
    TuKhoa,
    SoKetQuaMongDoi,
    MaBenhNhan,
    HoTen,
    NgaySinh,
    GioiTinh,
    SoDienThoai,
    SoCCCD
ORDER BY TenKiemThu, MaBenhNhan;
GO

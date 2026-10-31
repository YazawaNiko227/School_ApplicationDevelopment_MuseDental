USE QuanLyPhongKhamMuse;
GO

IF OBJECT_ID(N'dbo.QuyenVaiTro', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.QuyenVaiTro (
        MaVaiTro       NVARCHAR(20)  NOT NULL,
        TenChucNang    NVARCHAR(150) NOT NULL,
        CONSTRAINT PK_QuyenVaiTro PRIMARY KEY (MaVaiTro, TenChucNang),
        CONSTRAINT FK_QuyenVaiTro_VaiTro FOREIGN KEY (MaVaiTro) REFERENCES dbo.VaiTro(MaVaiTro)
    );
END
GO

IF OBJECT_ID(N'dbo.CauHinhHeThong', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.CauHinhHeThong (
        MaCauHinh    NVARCHAR(50)  NOT NULL,
        GiaTri       NVARCHAR(200) NOT NULL,
        CONSTRAINT PK_CauHinhHeThong PRIMARY KEY (MaCauHinh)
    );
END
GO

/*
    Chuyen du lieu van ban trong CSDL sang tieng Viet co dau.
    - Chi cap nhat cac cot hien thi (ten, dia chi, mo ta, chan doan...).
    - Khong doi cac ma trang thai noi bo (Dang lam viec, Cho xac nhan,
      Da thanh toan...) vi cac trang thai nay duoc dung trong WHERE/CHECK.
    - Script chay lai nhieu lan khong bi doi du lieu (WHERE theo gia tri cu).

    Chay bang SSMS hoac:
        sqlcmd -S localhost,1433 -U sa -P 123456789 -d QuanLyPhongKhamMuse -i database\Migration_TiengViet.sql -f 65001
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

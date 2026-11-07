package entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LichHenKhamInfo(String maLichHen, LocalDateTime thoiGianHen, String trangThai,
        String maBenhNhan, String hoTen, String gioiTinh, LocalDate ngaySinh, String tienSuBenhLy,
        String thongTinDiUng, String nguoiBaoHo, String maHoSo, String maNhaSi, String tenNhaSi,
        String maPhong, String trangThaiPhong, LocalDateTime thoiGianCheckIn) {
}

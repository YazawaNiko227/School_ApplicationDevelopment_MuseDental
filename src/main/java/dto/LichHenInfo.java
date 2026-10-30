package dto;

import java.time.LocalDateTime;

/**
 * Một lịch hẹn kèm thông tin bệnh nhân, dùng chung cho:
 * "Lịch làm việc cá nhân" (nha sĩ xem lịch hẹn của mình) và
 * "Ghi nhận vật tư tiêu hao" (chọn lần khám cần xuất kho).
 */
public record LichHenInfo(String maLichHen, LocalDateTime thoiGianHen, String trangThai,
        String maBenhNhan, String hoTen, String maPhong) {
}

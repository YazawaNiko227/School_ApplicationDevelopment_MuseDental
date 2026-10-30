package dto;

/**
 * Một lần khám (ChanDoan) đang chờ kê đơn, kèm thông tin bệnh nhân.
 * DTO phục vụ chức năng "Kê đơn thuốc điện tử".
 *
 * @param maChanDoan     mã lần khám
 * @param maBenhNhan     mã bệnh nhân
 * @param hoTen          họ tên bệnh nhân
 * @param ketLuan        kết luận chẩn đoán
 * @param thongTinDiUng  thông tin dị ứng của bệnh nhân (null / rỗng nếu không có)
 */
public record LanKham(String maChanDoan, String maBenhNhan, String hoTen,
        String ketLuan, String thongTinDiUng) {

    public String diUngHienThi() {
        return thongTinDiUng == null || thongTinDiUng.trim().isEmpty() ? "không ghi nhận" : thongTinDiUng;
    }
}

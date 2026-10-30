package dto;

/**
 * DTO chứa thông tin một dòng vật tư đã xuất kho kèm tên và đơn vị tính,
 * phục vụ hiển thị bảng lịch sử xuất kho trong panel "Ghi nhận vật tư tiêu hao".
 */
public record ChiTietXuatInfo(
        String maPhieuXuat,
        String maVatTu,
        String tenVatTu,
        String donViTinh,
        int soLuongSuDung) {
}

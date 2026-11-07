package dto;

/**
 * DTO chứa thông tin một dòng vật tư đã xuất kho kèm tên và đơn vị tính,
 * phục vụ hiển thị chi tiết một phiếu trong panel tra cứu lịch sử vật tư.
 */
public record ChiTietXuatInfo(
        String maPhieuXuat,
        String maVatTu,
        String tenVatTu,
        String donViTinh,
        int soLuongSuDung) {
}

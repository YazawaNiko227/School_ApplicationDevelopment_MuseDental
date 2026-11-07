package dto;

import java.time.LocalDate;

/** Thông tin tổng hợp một phiếu xuất kho dùng trong màn hình tra cứu. */
public record PhieuXuatTraCuuInfo(
        String maPhieuXuat,
        LocalDate ngayXuat,
        String maLichHen,
        String maBenhNhan,
        String tenBenhNhan,
        int soLoaiVatTu,
        int tongSoLuong) {
}

package util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Vẽ mã QR "tượng trưng" để minh họa trên giao diện.
 *
 * Mã này chỉ có hình thức giống QR (có 3 ô định vị và các ô đen/trắng), không
 * mã hóa dữ liệu thật và không dùng để quét. Nếu cần QR thật, nên tích hợp thư
 * viện ZXing và thay thế lớp này.
 */
public final class QrCodeUtil {
    private QrCodeUtil() {
    }

    public static BufferedImage taoMaQrTuongTrung(String noiDung, int kichThuoc) {
        int soOMoiChieu = 29;
        int vienTrang = 2;
        int tongO = soOMoiChieu + vienTrang * 2;
        int cell = Math.max(2, Math.max(1, kichThuoc) / tongO);
        int size = cell * tongO;

        boolean[][] oDen = new boolean[soOMoiChieu][soOMoiChieu];
        long seed = 0x5DEECE66DL;
        if (noiDung != null) {
            for (int i = 0; i < noiDung.length(); i++) {
                seed = seed * 31 + noiDung.charAt(i);
            }
        }
        Random random = new Random(seed);
        for (int row = 0; row < soOMoiChieu; row++) {
            for (int col = 0; col < soOMoiChieu; col++) {
                oDen[row][col] = random.nextBoolean();
            }
        }

        veODinhVi(oDen, 0, 0);
        veODinhVi(oDen, soOMoiChieu - 7, 0);
        veODinhVi(oDen, 0, soOMoiChieu - 7);
        for (int i = 8; i < soOMoiChieu - 8; i++) {
            oDen[6][i] = i % 2 == 0;
            oDen[i][6] = i % 2 == 0;
        }

        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, size, size);
        g.setColor(new Color(24, 24, 24));
        for (int row = 0; row < soOMoiChieu; row++) {
            for (int col = 0; col < soOMoiChieu; col++) {
                if (oDen[row][col]) {
                    g.fillRect((col + vienTrang) * cell, (row + vienTrang) * cell, cell, cell);
                }
            }
        }
        g.dispose();
        return image;
    }

    private static void veODinhVi(boolean[][] oDen, int startCol, int startRow) {
        int n = oDen.length;
        for (int row = startRow - 1; row <= startRow + 7; row++) {
            for (int col = startCol - 1; col <= startCol + 7; col++) {
                if (row >= 0 && row < n && col >= 0 && col < n) {
                    oDen[row][col] = false;
                }
            }
        }
        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 7; col++) {
                boolean vien = row == 0 || row == 6 || col == 0 || col == 6;
                boolean tam = row >= 2 && row <= 4 && col >= 2 && col <= 4;
                oDen[startRow + row][startCol + col] = vien || tam;
            }
        }
    }
}

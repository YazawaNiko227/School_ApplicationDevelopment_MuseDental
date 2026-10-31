package dao;

import connectDB.ConnectDB;
import entity.VaiTro;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import util.PhanQuyenMenu;

public class PhanQuyenDAO {
    public List<VaiTro> layDanhSachVaiTro() throws SQLException {
        List<VaiTro> vaiTros = new ArrayList<>();
        String sql = "SELECT MaVaiTro, TenVaiTro FROM VaiTro ORDER BY TenVaiTro";
        try (PreparedStatement statement = ConnectDB.getConnection().prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                vaiTros.add(new VaiTro(result.getString("MaVaiTro"), result.getString("TenVaiTro")));
            }
        }
        return vaiTros;
    }

    public Set<String> layChucNangDuocXem(String maVaiTro) throws SQLException {
        Set<String> chucNang = new LinkedHashSet<>();
        String sql = "SELECT TenChucNang FROM QuyenVaiTro WHERE MaVaiTro = ? ORDER BY TenChucNang";
        try (PreparedStatement statement = ConnectDB.getConnection().prepareStatement(sql)) {
            statement.setString(1, maVaiTro);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    chucNang.add(result.getString("TenChucNang"));
                }
            }
        }
        return chucNang;
    }

    public Map<String, Set<String>> layTatCaQuyen() throws SQLException {
        Map<String, Set<String>> permissions = new LinkedHashMap<>();
        for (VaiTro role : layDanhSachVaiTro()) {
            permissions.put(role.getMaVaiTro(), new LinkedHashSet<>());
        }
        String sql = "SELECT MaVaiTro, TenChucNang FROM QuyenVaiTro ORDER BY MaVaiTro, TenChucNang";
        try (PreparedStatement statement = ConnectDB.getConnection().prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                Set<String> allowed = permissions.get(result.getString("MaVaiTro"));
                if (allowed != null) {
                    allowed.add(result.getString("TenChucNang"));
                }
            }
        }
        return permissions;
    }

    public void khoiTaoQuyenMacDinhNeuChuaCo() throws SQLException {
        Connection connection = ConnectDB.getConnection();
        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            String checkSql = "SELECT GiaTri FROM CauHinhHeThong WITH (UPDLOCK, HOLDLOCK) "
                    + "WHERE MaCauHinh = N'RBAC_INITIALIZED'";
            boolean initialized;
            try (PreparedStatement statement = connection.prepareStatement(checkSql);
                    ResultSet result = statement.executeQuery()) {
                initialized = result.next();
            }
            if (!initialized) {
                String insertPermission = "INSERT INTO QuyenVaiTro (MaVaiTro, TenChucNang) VALUES (?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(insertPermission)) {
                    for (Map.Entry<String, Set<String>> role : PhanQuyenMenu.quyenMacDinhTheoVaiTro().entrySet()) {
                        for (String menu : role.getValue()) {
                            statement.setString(1, role.getKey());
                            statement.setString(2, menu);
                            statement.addBatch();
                        }
                    }
                    statement.executeBatch();
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO CauHinhHeThong (MaCauHinh, GiaTri) VALUES (N'RBAC_INITIALIZED', N'1')")) {
                    statement.executeUpdate();
                }
            }
            connection.commit();
        } catch (SQLException | RuntimeException exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    public void capNhatTatCaQuyen(Map<String, Set<String>> permissions) throws SQLException {
        Set<String> adminPermissions = permissions == null ? null : permissions.get(PhanQuyenMenu.QUAN_TRI);
        if (adminPermissions == null || !adminPermissions.contains("Phân quyền hệ thống RBAC")) {
            throw new IllegalArgumentException("Quản trị viên phải giữ quyền truy cập chức năng phân quyền.");
        }

        Set<String> roleIds = new LinkedHashSet<>();
        for (VaiTro role : layDanhSachVaiTro()) {
            roleIds.add(role.getMaVaiTro());
        }
        if (!roleIds.equals(permissions.keySet())) {
            throw new IllegalArgumentException("Danh sách vai trò gửi lên không khớp với dữ liệu trong CSDL.");
        }

        Set<String> allowedMenus = new LinkedHashSet<>();
        for (List<String> group : PhanQuyenMenu.nhomMenu().values()) {
            allowedMenus.addAll(group);
        }
        for (Map.Entry<String, Set<String>> role : permissions.entrySet()) {
            if (role.getKey() == null || role.getValue() == null) {
                throw new IllegalArgumentException("Danh sách vai trò gửi lên không hợp lệ.");
            }
            if (!allowedMenus.containsAll(role.getValue())) {
                throw new IllegalArgumentException("Quyền chứa chức năng không tồn tại trong menu.");
            }
        }

        Connection connection = ConnectDB.getConnection();
        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            try (PreparedStatement delete = connection.prepareStatement("DELETE FROM QuyenVaiTro")) {
                delete.executeUpdate();
            }
            String insertSql = "INSERT INTO QuyenVaiTro (MaVaiTro, TenChucNang) VALUES (?, ?)";
            try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
                for (Map.Entry<String, Set<String>> role : permissions.entrySet()) {
                    for (String menu : role.getValue()) {
                        insert.setString(1, role.getKey());
                        insert.setString(2, menu);
                        insert.addBatch();
                    }
                }
                insert.executeBatch();
            }
            connection.commit();
        } catch (SQLException | RuntimeException exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }
}

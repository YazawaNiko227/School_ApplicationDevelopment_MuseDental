package dao;

import connectDB.ConnectDB;
import entity.VaiTro;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import util.PhanQuyenMenu;

public class PhanQuyenDAO {
    public void khoiTaoQuyenMacDinhNeuChuaCo() throws SQLException {
        Connection connection = ConnectDB.getConnection();
        boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            boolean daCoQuyen;
            try (PreparedStatement statement = connection
                    .prepareStatement("SELECT TOP (1) 1 FROM QuyenVaiTro");
                    ResultSet result = statement.executeQuery()) {
                daCoQuyen = result.next();
            }

            if (!daCoQuyen) {
                String sql = "INSERT INTO QuyenVaiTro (MaVaiTro, TenChucNang) VALUES (?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    for (Map.Entry<String, Set<String>> role : PhanQuyenMenu.quyenMacDinhTheoVaiTro().entrySet()) {
                        for (String function : role.getValue()) {
                            statement.setString(1, role.getKey());
                            statement.setString(2, function);
                            statement.addBatch();
                        }
                    }
                    statement.executeBatch();
                }
            }

            Set<String> validMenus = new LinkedHashSet<>();
            for (List<String> functions : PhanQuyenMenu.nhomMenu().values()) {
                validMenus.addAll(functions);
            }
            Set<String> adminPermissions = new LinkedHashSet<>();
            try (PreparedStatement statement = connection
                    .prepareStatement("SELECT TenChucNang FROM QuyenVaiTro WHERE MaVaiTro = ?")) {
                statement.setString(1, PhanQuyenMenu.QUAN_TRI);
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        adminPermissions.add(result.getString("TenChucNang"));
                    }
                }
            }
            if (Collections.disjoint(adminPermissions, validMenus)) {
                try (PreparedStatement delete = connection
                        .prepareStatement("DELETE FROM QuyenVaiTro WHERE MaVaiTro = ?")) {
                    delete.setString(1, PhanQuyenMenu.QUAN_TRI);
                    delete.executeUpdate();
                }
                String sql = "INSERT INTO QuyenVaiTro (MaVaiTro, TenChucNang) VALUES (?, ?)";
                try (PreparedStatement insert = connection.prepareStatement(sql)) {
                    for (String function : PhanQuyenMenu.quyenMacDinhTheoVaiTro().get(PhanQuyenMenu.QUAN_TRI)) {
                        insert.setString(1, PhanQuyenMenu.QUAN_TRI);
                        insert.setString(2, function);
                        insert.addBatch();
                    }
                    insert.executeBatch();
                }
            }
            connection.commit();
        } catch (SQLException exception) {
            rollback(connection, exception);
            throw exception;
        } finally {
            connection.setAutoCommit(autoCommit);
        }
    }

    public List<VaiTro> layDanhSachVaiTro() throws SQLException {
        String sql = "SELECT MaVaiTro, TenVaiTro FROM VaiTro "
                + "ORDER BY CASE MaVaiTro "
                + "WHEN N'QUAN_TRI' THEN 1 WHEN N'BAC_SI' THEN 2 WHEN N'Y_TA' THEN 3 "
                + "WHEN N'LE_TAN' THEN 4 WHEN N'THU_NGAN' THEN 5 WHEN N'THU_KHO' THEN 6 "
                + "ELSE 7 END, TenVaiTro";
        List<VaiTro> vaiTros = new ArrayList<>();
        try (PreparedStatement statement = ConnectDB.getConnection().prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                vaiTros.add(new VaiTro(result.getString("MaVaiTro"), result.getString("TenVaiTro")));
            }
        }
        return vaiTros;
    }

    public Map<String, Set<String>> layTatCaQuyen() throws SQLException {
        Map<String, Set<String>> permissions = new LinkedHashMap<>();
        for (VaiTro vaiTro : layDanhSachVaiTro()) {
            permissions.put(vaiTro.getMaVaiTro(), new LinkedHashSet<>());
        }

        String sql = "SELECT MaVaiTro, TenChucNang FROM QuyenVaiTro ORDER BY MaVaiTro, TenChucNang";
        try (PreparedStatement statement = ConnectDB.getConnection().prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                Set<String> rolePermissions = permissions.get(result.getString("MaVaiTro"));
                if (rolePermissions != null) {
                    rolePermissions.add(result.getString("TenChucNang"));
                }
            }
        }
        return permissions;
    }

    public Set<String> layChucNangDuocXem(String maVaiTro) throws SQLException {
        if (maVaiTro == null || maVaiTro.trim().isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> permissions = new LinkedHashSet<>();
        String sql = "SELECT TenChucNang FROM QuyenVaiTro WHERE MaVaiTro = ? ORDER BY TenChucNang";
        try (PreparedStatement statement = ConnectDB.getConnection().prepareStatement(sql)) {
            statement.setString(1, maVaiTro.trim());
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    permissions.add(result.getString("TenChucNang"));
                }
            }
        }
        return Collections.unmodifiableSet(permissions);
    }

    public void capNhatTatCaQuyen(Map<String, Set<String>> permissions) throws SQLException {
        if (permissions == null) {
            throw new IllegalArgumentException("Danh sách phân quyền không được để trống.");
        }

        Set<String> roleCodes = new LinkedHashSet<>();
        for (VaiTro vaiTro : layDanhSachVaiTro()) {
            roleCodes.add(vaiTro.getMaVaiTro());
        }
        if (!permissions.keySet().equals(roleCodes)) {
            throw new IllegalArgumentException("Danh sách phân quyền phải có đủ các vai trò trong CSDL.");
        }

        Set<String> validFunctions = new LinkedHashSet<>();
        for (List<String> functions : PhanQuyenMenu.nhomMenu().values()) {
            validFunctions.addAll(functions);
        }
        for (Map.Entry<String, Set<String>> role : permissions.entrySet()) {
            if (role.getValue() == null || !validFunctions.containsAll(role.getValue())) {
                throw new IllegalArgumentException("Danh sách chức năng phân quyền không hợp lệ.");
            }
        }

        Connection connection = ConnectDB.getConnection();
        boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            try (PreparedStatement delete = connection.prepareStatement("DELETE FROM QuyenVaiTro")) {
                delete.executeUpdate();
            }

            String sql = "INSERT INTO QuyenVaiTro (MaVaiTro, TenChucNang) VALUES (?, ?)";
            try (PreparedStatement insert = connection.prepareStatement(sql)) {
                for (Map.Entry<String, Set<String>> role : permissions.entrySet()) {
                    for (String function : role.getValue()) {
                        insert.setString(1, role.getKey());
                        insert.setString(2, function);
                        insert.addBatch();
                    }
                }
                insert.executeBatch();
            }
            connection.commit();
        } catch (SQLException exception) {
            rollback(connection, exception);
            throw exception;
        } finally {
            connection.setAutoCommit(autoCommit);
        }
    }

    private static void rollback(Connection connection, SQLException originalException) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            originalException.addSuppressed(rollbackException);
        }
    }
}

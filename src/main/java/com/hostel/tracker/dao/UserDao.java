package com.hostel.tracker.dao;

import com.hostel.tracker.model.Role;
import com.hostel.tracker.model.User;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit 2.1: plain JDBC.
 * Uses Connection, PreparedStatement, Statement, ResultSet and ResultSetMetaData directly.
 * No ORM here on purpose, so JDBC and Hibernate (ComplaintDao) can be compared side by side.
 */
@Repository
public class UserDao {

    private final DataSource dataSource;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** CREATE: INSERT with a PreparedStatement and read back the generated id. */
    public long insert(User u) throws SQLException {
        String sql = "INSERT INTO users (name, email, password_hash, role, room_no, phone, speciality, created_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"})) {
            ps.setString(1, u.getName());
            ps.setString(2, u.getEmail().toLowerCase().trim());
            ps.setString(3, u.getPasswordHash());
            ps.setString(4, u.getRole().name());
            ps.setString(5, u.getRoomNo());
            ps.setString(6, u.getPhone());
            ps.setString(7, u.getSpeciality());
            ps.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    /** READ: used by login. executeQuery returns a ResultSet. */
    public User findByEmail(String email) throws SQLException {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE email = ?")) {
            ps.setString(1, email.toLowerCase().trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public User findById(long id) throws SQLException {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<User> findByRole(Role role) throws SQLException {
        List<User> list = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE role = ? ORDER BY name")) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /** UPDATE: profile details. */
    public void updateProfile(long id, String name, String phone, String roomNo) throws SQLException {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE users SET name = ?, phone = ?, room_no = ? WHERE id = ?")) {
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setString(3, roomNo);
            ps.setLong(4, id);
            ps.executeUpdate();
        }
    }

    /**
     * DELETE: returns false if the user still has complaints
     * (the foreign key makes the database refuse the delete).
     */
    public boolean delete(long id) throws SQLException {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM users WHERE id = ? AND role <> 'WARDEN'")) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false;
        } catch (SQLException e) {
            // PostgreSQL reports FK violations with SQLState 23503 instead of the subclass above
            if ("23503".equals(e.getSQLState())) return false;
            throw e;
        }
    }

    /**
     * Uses a plain Statement plus ResultSetMetaData to print ANY query as a table
     * without knowing its columns in advance. Only called with fixed SQL, never user input.
     */
    public TableData runReport(String sql) throws SQLException {
        try (Connection con = dataSource.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            ResultSetMetaData md = rs.getMetaData();
            TableData t = new TableData();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                t.columns.add(md.getColumnLabel(i));
                t.types.add(md.getColumnTypeName(i));
            }
            while (rs.next()) {
                List<String> row = new ArrayList<>();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    Object v = rs.getObject(i);
                    row.add(v == null ? "-" : v.toString());
                }
                t.rows.add(row);
            }
            return t;
        }
    }

    /** Converts the current ResultSet row into a User object. */
    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setRoomNo(rs.getString("room_no"));
        u.setPhone(rs.getString("phone"));
        u.setSpeciality(rs.getString("speciality"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) u.setCreatedAt(ts.toLocalDateTime());
        return u;
    }

    /** Column names, column types and rows produced by runReport(). */
    public static class TableData {
        private final List<String> columns = new ArrayList<>();
        private final List<String> types = new ArrayList<>();
        private final List<List<String>> rows = new ArrayList<>();
        public List<String> getColumns() { return columns; }
        public List<String> getTypes() { return types; }
        public List<List<String>> getRows() { return rows; }
    }
}

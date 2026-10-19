
package com.lebogang.taskmanager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class TaskRepository {

    private final JdbcTemplate jdbcTemplate;

    public TaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Task> taskRowMapper = new RowMapper<Task>() {
        @Override
        public Task mapRow(ResultSet rs, int rowNum) throws SQLException {

            java.sql.Date dueDate = rs.getDate("due_date");
            Timestamp createdAt = rs.getTimestamp("created_at");

            return new Task(
                    rs.getLong("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    dueDate != null ? dueDate.toLocalDate() : null,
                    rs.getBoolean("completed"),
                    createdAt != null ? createdAt.toLocalDateTime() : null
            );
        }
    };

    public List<Task> findAll() {
        String sql = """
                SELECT id, title, description, due_date, completed, created_at
                FROM tasks
                ORDER BY created_at DESC
                """;

        return jdbcTemplate.query(sql, taskRowMapper);
    }

    public Optional<Task> findById(Long id) {
        String sql = """
                SELECT id, title, description, due_date, completed, created_at
                FROM tasks
                WHERE id = ?
                """;

        List<Task> tasks = jdbcTemplate.query(sql, taskRowMapper, id);

        return tasks.stream().findFirst();
    }
}

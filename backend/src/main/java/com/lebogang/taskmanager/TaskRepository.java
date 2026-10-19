
package com.lebogang.taskmanager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
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

    public Task create(CreateTaskRequest request) {

        String sql = """
                INSERT INTO tasks (title, description, due_date, completed)
                VALUES (?, ?, ?, FALSE)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {

            PreparedStatement statement = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            statement.setString(1, request.getTitle());
            statement.setString(2, request.getDescription());

            if (request.getDueDate() != null) {
                statement.setDate(
                        3,
                        java.sql.Date.valueOf(request.getDueDate())
                );
            } else {
                statement.setNull(3, java.sql.Types.DATE);
            }

            return statement;

        }, keyHolder);

        Number generatedId = keyHolder.getKey();

        if (generatedId == null) {
            throw new IllegalStateException(
                    "The database did not return the new task ID."
            );
        }

        return findById(generatedId.longValue())
                .orElseThrow(() -> new IllegalStateException(
                        "The task was saved but could not be retrieved."
                ));
    }

    public Task update(Long id, UpdateTaskRequest request) {

        String sql = """
                UPDATE tasks
                SET title = ?,
                    description = ?,
                    due_date = ?,
                    completed = ?
                WHERE id = ?
                """;

        int rowsUpdated = jdbcTemplate.update(
                sql,
                request.getTitle(),
                request.getDescription(),
                request.getDueDate() != null
                        ? java.sql.Date.valueOf(request.getDueDate())
                        : null,
                request.getCompleted(),
                id
        );

        if (rowsUpdated == 0) {
            return null;
        }

        return findById(id)
                .orElseThrow(() -> new IllegalStateException(
                        "The task was updated but could not be retrieved."
                ));
    }
}

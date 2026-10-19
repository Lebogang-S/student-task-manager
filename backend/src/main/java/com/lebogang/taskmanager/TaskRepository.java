
package com.lebogang.taskmanager;

// Import classes for working with SQL statements and database results.
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

// Import Spring JDBC classes for communicating with MySQL.
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class TaskRepository {

    // 1. DATABASE CONNECTION
    // JdbcTemplate helps us execute SQL statements against MySQL.
    private final JdbcTemplate jdbcTemplate;

    public TaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 2. CONVERT DATABASE ROWS INTO TASK OBJECTS
    // This mapper reads a database row and converts it into a Task.
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
                    createdAt != null
                            ? createdAt.toLocalDateTime()
                            : null
            );
        }
    };

    // 3. FIND ALL TASKS
    // Retrieves every task, with the newest tasks appearing first.
    public List<Task> findAll() {

        String sql = """
                SELECT id, title, description, due_date, completed, created_at
                FROM tasks
                ORDER BY created_at DESC
                """;

        return jdbcTemplate.query(sql, taskRowMapper);
    }

    // 4. FIND ONE TASK BY ID
    // Returns an Optional containing the task if it exists.
    public Optional<Task> findById(Long id) {

        String sql = """
                SELECT id, title, description, due_date, completed, created_at
                FROM tasks
                WHERE id = ?
                """;

        List<Task> tasks = jdbcTemplate.query(sql, taskRowMapper, id);

        return tasks.stream().findFirst();
    }

    // 5. CREATE A NEW TASK
    // Saves a new task and retrieves its generated database ID.
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

    // 6. UPDATE AN EXISTING TASK
    // Changes the task details and returns the updated task.
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

    // 7. DELETE A TASK BY ID
    // Deletes the matching task and reports whether a row was deleted.
    public boolean deleteById(Long id) {

        String sql = """
                DELETE FROM tasks
                WHERE id = ?
                """;

        int rowsDeleted = jdbcTemplate.update(sql, id);

        return rowsDeleted > 0;
    }
}

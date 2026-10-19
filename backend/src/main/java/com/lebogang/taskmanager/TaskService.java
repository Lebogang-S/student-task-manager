
package com.lebogang.taskmanager;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class TaskService {

    // 1. REPOSITORY CONNECTION
    // The service uses TaskRepository to communicate with the database.
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // 2. GET ALL TASKS
    // Retrieves all tasks from the database.
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    // 3. GET ONE TASK
    // Finds a task using its ID.
    // Optional allows the task to be absent if the ID does not exist.
    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    // 4. CREATE A TASK
    // Validates the information before saving a new task.
    public Task createTask(CreateTaskRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Task information is required."
            );
        }

        validateTitle(request.getTitle());

        if (request.getDescription() != null
                && request.getDescription().length() > 1000) {
            throw new IllegalArgumentException(
                    "Description must not exceed 1000 characters."
            );
        }

        return taskRepository.create(request);
    }

    // 5. UPDATE A TASK
    // Validates the task ID and information before updating.
    // Returns null when the requested task does not exist.
    public Task updateTask(Long id, UpdateTaskRequest request) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Task ID must be a positive number."
            );
        }

        if (request == null) {
            throw new IllegalArgumentException(
                    "Task information is required."
            );
        }

        validateTitle(request.getTitle());

        if (request.getDescription() != null
                && request.getDescription().length() > 1000) {
            throw new IllegalArgumentException(
                    "Description must not exceed 1000 characters."
            );
        }

        if (request.getCompleted() == null) {
            throw new IllegalArgumentException(
                    "Completed status must be provided."
            );
        }

        return taskRepository.findById(id)
                .map(existingTask -> taskRepository.update(id, request))
                .orElse(null);
    }

    // 6. DELETE A TASK
    // Checks that the ID is valid, then asks the repository to delete it.
    // Returns true if a task was deleted, or false if it was not found.
    public boolean deleteTask(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Task ID must be a positive number."
            );
        }

        return taskRepository.deleteById(id);
    }

    // 7. VALIDATE THE TASK TITLE
    // A title is required and cannot exceed 150 characters.
    // This method is reused by createTask() and updateTask().
    private void validateTitle(String title) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Task title is required."
            );
        }

        if (title.length() > 150) {
            throw new IllegalArgumentException(
                    "Title must not exceed 150 characters."
            );
        }
    }
}

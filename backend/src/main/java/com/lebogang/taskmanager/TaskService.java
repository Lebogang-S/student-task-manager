
package com.lebogang.taskmanager;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

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

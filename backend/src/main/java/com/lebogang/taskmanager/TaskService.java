
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
                    "Task details are required."
            );
        }

        if (request.getTitle() == null
                || request.getTitle().isBlank()) {
            throw new IllegalArgumentException(
                    "Task title is required."
            );
        }

        String title = request.getTitle().trim();

        if (title.length() > 150) {
            throw new IllegalArgumentException(
                    "Task title must not exceed 150 characters."
            );
        }

        if (request.getDescription() != null
                && request.getDescription().length() > 1000) {
            throw new IllegalArgumentException(
                    "Task description must not exceed 1000 characters."
            );
        }

        return taskRepository.create(request);
    }
}

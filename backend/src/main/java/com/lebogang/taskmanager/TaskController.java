
package com.lebogang.taskmanager;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TaskController {

    @GetMapping("/api/hello")
    public String hello() {
        return "Welcome to Student Task Manager!";
    }

    @GetMapping("/api/tasks/demo")
    public Task getDemoTask() {
        return new Task(
                1L,
                "Study Java",
                "Practise building REST APIs",
                false);
    }

}


/* =========================================
   1. GET HTML ELEMENTS
   Find the form, messages, and task list.
========================================= */

const taskForm = document.getElementById("task-form");
const titleInput = document.getElementById("title");
const descriptionInput = document.getElementById("description");
const dueDateInput = document.getElementById("due-date");

const formMessage = document.getElementById("form-message");
const tasksMessage = document.getElementById("tasks-message");
const taskList = document.getElementById("task-list");
const refreshButton = document.getElementById("refresh-tasks");

/* =========================================
   2. DISPLAY STATUS MESSAGES
   Show success and error messages to the user.
========================================= */

function showMessage(element, message, type = "") {
    element.textContent = message;
    element.classList.remove("message-success", "message-error");

    if (type === "success") {
        element.classList.add("message-success");
    } else if (type === "error") {
        element.classList.add("message-error");
    }
}

/* =========================================
   3. CREATE SAFE HTML ELEMENTS
   Use textContent for user-provided text to
   avoid interpreting task text as HTML.
========================================= */

function createElement(tag, className, text = "") {
    const element = document.createElement(tag);

    if (className) {
        element.className = className;
    }

    element.textContent = text;

    return element;
}

/* =========================================
   4. FORMAT TASK DATES
   Convert dates into a readable format.
========================================= */

function formatDueDate(dateString) {
    if (!dateString) {
        return "No due date";
    }

    const date = new Date(`${dateString}T00:00:00`);

    if (Number.isNaN(date.getTime())) {
        return "Invalid date";
    }

    return date.toLocaleDateString(undefined, {
        day: "numeric",
        month: "short",
        year: "numeric"
    });
}

/* =========================================
   5. BUILD A TASK CARD
   Display one task and its action buttons.
========================================= */

function createTaskCard(task) {
    const card = createElement(
        "article",
        `task-card${task.completed ? " is-completed" : ""}`
    );

    const heading = createElement("h3", "", task.title);

    const description = createElement(
        "p",
        "task-description",
        task.description || "No description provided."
    );

    const metadata = createElement("div", "task-meta");

    metadata.append(
        createElement("span", "", `Task #${task.id}`),
        createElement("span", "", `Due: ${formatDueDate(task.dueDate)}`)
    );

    const status = createElement(
        "span",
        `task-status${task.completed ? " completed" : ""}`,
        task.completed ? "Completed" : "Pending"
    );

    const actions = createElement("div", "task-actions");

    // Toggle whether the task is completed.
    const completeButton = createElement(
        "button",
        "complete-button",
        task.completed ? "Mark pending" : "Mark completed"
    );

    completeButton.type = "button";

    completeButton.addEventListener("click", async () => {
        completeButton.disabled = true;

        try {
            await TaskAPI.update(task.id, {
                title: task.title,
                description: task.description,
                dueDate: task.dueDate,
                completed: !task.completed
            });

            await loadTasks();
            showMessage(
                tasksMessage,
                "Task status updated successfully.",
                "success"
            );
        } catch (error) {
            showMessage(
                tasksMessage,
                `Could not update task: ${error.message}`,
                "error"
            );
            completeButton.disabled = false;
        }
    });

    // Edit the task details using prompts.
    const editButton = createElement("button", "secondary-button", "Edit");
    editButton.type = "button";

    editButton.addEventListener("click", async () => {
        const newTitle = window.prompt("Task title:", task.title);

        if (newTitle === null) {
            return;
        }

        if (!newTitle.trim()) {
            window.alert("The task title cannot be empty.");
            return;
        }

        const newDescription = window.prompt(
            "Task description:",
            task.description || ""
        );

        if (newDescription === null) {
            return;
        }

        const newDueDate = window.prompt(
            "Due date (YYYY-MM-DD), or leave empty:",
            task.dueDate || ""
        );

        if (newDueDate === null) {
            return;
        }

        if (
            newDueDate.trim() !== "" &&
            !/^\d{4}-\d{2}-\d{2}$/.test(newDueDate.trim())
        ) {
            window.alert("Enter the date in YYYY-MM-DD format.");
            return;
        }

        editButton.disabled = true;

        try {
            await TaskAPI.update(task.id, {
                title: newTitle.trim(),
                description: newDescription,
                dueDate: newDueDate.trim() || null,
                completed: task.completed
            });

            await loadTasks();
            showMessage(
                tasksMessage,
                "Task updated successfully.",
                "success"
            );
        } catch (error) {
            showMessage(
                tasksMessage,
                `Could not edit task: ${error.message}`,
                "error"
            );
        } finally {
            editButton.disabled = false;
        }
    });

    // Delete the task after confirmation.
    const deleteButton = createElement("button", "delete-button", "Delete");
    deleteButton.type = "button";

    deleteButton.addEventListener("click", async () => {
        const confirmed = window.confirm(
            `Are you sure you want to delete "${task.title}"?`
        );

        if (!confirmed) {
            return;
        }

        deleteButton.disabled = true;

        try {
            await TaskAPI.delete(task.id);
            await loadTasks();

            showMessage(
                tasksMessage,
                "Task deleted successfully.",
                "success"
            );
        } catch (error) {
            showMessage(
                tasksMessage,
                `Could not delete task: ${error.message}`,
                "error"
            );
            deleteButton.disabled = false;
        }
    });

    actions.append(completeButton, editButton, deleteButton);

    card.append(heading, description, metadata, status, actions);

    return card;
}

/* =========================================
   6. LOAD TASKS FROM THE BACKEND
   Retrieve the current tasks and display them.
========================================= */

async function loadTasks() {
    refreshButton.disabled = true;
    showMessage(tasksMessage, "Loading tasks...");
    taskList.replaceChildren();

    try {
        const tasks = await TaskAPI.getAll();

        if (!Array.isArray(tasks)) {
            throw new Error("The server returned an unexpected response.");
        }

        if (tasks.length === 0) {
            showMessage(tasksMessage, "You have no tasks yet.");
            return;
        }

        const fragment = document.createDocumentFragment();

        tasks.forEach(task => {
            fragment.appendChild(createTaskCard(task));
        });

        taskList.appendChild(fragment);

        showMessage(
            tasksMessage,
            `${tasks.length} task${tasks.length === 1 ? "" : "s"} found.`
        );
    } catch (error) {
        showMessage(
            tasksMessage,
            `Could not load tasks. ${error.message}`,
            "error"
        );
    } finally {
        refreshButton.disabled = false;
    }
}

/* =========================================
   7. HANDLE THE CREATE TASK FORM
   Send new task details to the backend.
========================================= */

taskForm.addEventListener("submit", async event => {
    event.preventDefault();

    const task = {
        title: titleInput.value.trim(),
        description: descriptionInput.value.trim(),
        dueDate: dueDateInput.value || null
    };

    if (!task.title) {
        showMessage(formMessage, "Please enter a task title.", "error");
        titleInput.focus();
        return;
    }

    const submitButton = taskForm.querySelector(
        'button[type="submit"]'
    );

    submitButton.disabled = true;
    showMessage(formMessage, "Saving your task...");

    try {
        await TaskAPI.create(task);

        taskForm.reset();

        showMessage(
            formMessage,
            "Task created successfully!",
            "success"
        );

        await loadTasks();
    } catch (error) {
        showMessage(
            formMessage,
            `Could not create task: ${error.message}`,
            "error"
        );
    } finally {
        submitButton.disabled = false;
    }
});

/* =========================================
   8. REFRESH BUTTON
   Reload the task list when clicked.
========================================= */

refreshButton.addEventListener("click", loadTasks);

/* =========================================
   9. INITIAL PAGE LOAD
   Load tasks automatically when the page opens.
========================================= */

loadTasks();

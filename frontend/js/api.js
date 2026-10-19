
/* =========================================
   1. API CONFIGURATION
   This is the address of our Spring Boot API.
========================================= */

const API_BASE_URL = "http://localhost:8080/api";

/* =========================================
   2. SHARED REQUEST FUNCTION
   Sends a request to the backend and handles
   HTTP errors and JSON responses.
========================================= */

async function apiRequest(endpoint, options = {}) {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        ...options,
        headers: {
            "Content-Type": "application/json",
            ...options.headers
        }
    });

    // A 204 response has no body, so do not try to parse JSON.
    if (response.status === 204) {
        return null;
    }

    // Read the response body as text first.
    const responseText = await response.text();

    // Convert a non-empty JSON response into a JavaScript object.
    let data = null;

    if (responseText) {
        try {
            data = JSON.parse(responseText);
        } catch {
            data = responseText;
        }
    }

    // Turn unsuccessful HTTP responses into useful errors.
    if (!response.ok) {
        const message =
            data && typeof data === "object"
                ? data.message || data.error || data.detail
                : data;

        throw new Error(
            message || `Request failed with status ${response.status}.`
        );
    }

    return data;
}

/* =========================================
   3. TASK API METHODS
   These methods match the endpoints in
   TaskController.java.
========================================= */

const TaskAPI = {

    // GET /api/tasks
    // Retrieve all tasks.
    getAll() {
        return apiRequest("/tasks");
    },

    // GET /api/tasks/{id}
    // Retrieve one task using its ID.
    getById(id) {
        return apiRequest(`/tasks/${id}`);
    },

    // POST /api/tasks
    // Create a new task.
    create(task) {
        return apiRequest("/tasks", {
            method: "POST",
            body: JSON.stringify(task)
        });
    },

    // PUT /api/tasks/{id}
    // Update an existing task.
    update(id, task) {
        return apiRequest(`/tasks/${id}`, {
            method: "PUT",
            body: JSON.stringify(task)
        });
    },

    // DELETE /api/tasks/{id}
    // Delete a task.
    delete(id) {
        return apiRequest(`/tasks/${id}`, {
            method: "DELETE"
        });
    }
};

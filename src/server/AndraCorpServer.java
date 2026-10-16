package server;

import applicationimpl.ChangeOrderImpl;
import applicationimpl.ExpenseBudgetImpl;
import applicationimpl.MaterialEquipmentImpl;
import applicationimpl.ProjectManagementImpl;
import applicationimpl.UserManagementImpl;
import applicationimpl.WorkforceManagementImpl;
import io.javalin.Javalin;
import model.ChangeOrder;
import storageimpl.DatabaseManager;
import java.util.HashMap;
import java.util.Map;

/*
 * Starts the AndraCorp backend server.
 * The React frontend communicates with
 * this server using HTTP requests.
 */
public class AndraCorpServer {

    public static void main(String[] args) {

//      Creates the SQLite database and tables
//      before starting the backend.
        DatabaseManager.initializeDatabase();

//      Creates the project management components.
        ProjectManagementImpl projectManager =
                new ProjectManagementImpl();

        ExpenseBudgetImpl expenseManager =
                new ExpenseBudgetImpl();
        
//      Creates the materials and equipment component.
        MaterialEquipmentImpl materialManager =
                new MaterialEquipmentImpl();

//      Creates the workforce management component.
        WorkforceManagementImpl workforceManager =
                new WorkforceManagementImpl();
        
//      Creates the user management component.
        UserManagementImpl userManager = new UserManagementImpl();
        
//      Handles project change orders.
        ChangeOrderImpl changeOrderManager = new ChangeOrderImpl();

//      Creates the HTTP server.
        Javalin app =
                Javalin.create(config -> {

//                  Allows the React frontend
//                  to communicate with this server.
                    config.bundledPlugins
                            .enableCors(cors -> {

                                cors.addRule(rule -> {

                                    rule.allowHost(
                                            "http://localhost:5173");
                                });
                            });
                });

        // Simple route used to test the backend.
        app.get("/api/test", ctx -> {

            ctx.result(
                    "AndraCorp backend is running!");
        });
        
        // Creates a new client account.
        app.post("/api/clients", ctx -> {
            Map<String, Object> request = ctx.bodyAsClass(Map.class);

            Object nameValue = request.get("name");
            Object emailValue = request.get("email");
            Object phoneValue = request.get("phoneNumber");

            if (!(nameValue instanceof String name) || name.isBlank()
                    || !(emailValue instanceof String email) || email.isBlank()
                    || (phoneValue != null && !(phoneValue instanceof String))) {
                ctx.status(400).json(Map.of(
                        "error", "Valid client name, email, and phone number are required."
                ));
                return;
            }

            if (!email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                ctx.status(400).json(Map.of(
                        "error", "Enter a valid email address."
                ));
                return;
            }

            String phoneNumber = phoneValue == null
                    ? null : ((String) phoneValue).trim();

            String clientId = userManager.createUser(
                    name.trim(), email.trim(), phoneNumber, "CLIENT"
            );

            if (clientId == null) {
                ctx.status(500).json(Map.of(
                        "error", "Could not create client."
                ));
                return;
            }

            Map<String, Object> response = new HashMap<>();
            response.put("clientId", clientId);
            response.put("name", name.trim());
            response.put("email", email.trim());
            response.put("phoneNumber", phoneNumber);
            response.put("role", "CLIENT");

            ctx.status(201).json(response);
        });
        
        // Client login using their name and phone number.
        // For local capstone demonstration only.
        app.post("/api/client/login", ctx -> {

            Map<String, Object> request;

            try {
                request = ctx.bodyAsClass(Map.class);
            } catch (Exception e) {
                ctx.status(400).json(Map.of(
                        "error", "Invalid request body."
                ));
                return;
            }

            String name = request.get("name") instanceof String
                    ? (String) request.get("name") : null;

            String phoneNumber = request.get("phoneNumber") instanceof String
                    ? (String) request.get("phoneNumber") : null;

            if (name == null || name.isBlank()
                    || phoneNumber == null || phoneNumber.isBlank()) {

                ctx.status(400).json(Map.of(
                        "error", "Name and phone number are required."
                ));
                return;
            }

            Map<String, String> client =
                    userManager.findClientByNameAndPhone(
                            name, phoneNumber
                    );

            if (client == null) {

                ctx.status(401).json(Map.of(
                        "error", "Client not found. Check your name and phone number."
                ));
                return;
            }

            ctx.status(200).json(Map.of(
                    "message", "Client found.",
                    "client", client
            ));
        });

        // Retrieves all registered clients.
        app.get("/api/clients", ctx -> {
            ctx.json(userManager.getAllClients());
        });

        // Updates an existing client's information.
        app.put("/api/clients/{id}", ctx -> {

            String clientId = ctx.pathParam("id");
            Map<String, Object> request = ctx.bodyAsClass(Map.class);

            Object nameValue = request.get("name");
            Object emailValue = request.get("email");
            Object phoneValue = request.get("phoneNumber");

            if (!(nameValue instanceof String name) || name.isBlank()
                    || !(emailValue instanceof String email) || email.isBlank()
                    || (phoneValue != null && !(phoneValue instanceof String))) {

                ctx.status(400).json(Map.of(
                        "error", "Valid client name and email are required."
                ));
                return;
            }

            if (!email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                ctx.status(400).json(Map.of(
                        "error", "Enter a valid email address."
                ));
                return;
            }

            // Makes sure the account belongs to a client.
            if (!userManager.userHasRole(clientId, "CLIENT")) {
                ctx.status(404).json(Map.of(
                        "error", "Client not found."
                ));
                return;
            }

            String phoneNumber = phoneValue == null
                    ? null : ((String) phoneValue).trim();

            boolean updated = userManager.updateUser(
                    clientId, name.trim(), email.trim(), phoneNumber
            );

            if (!updated) {
                ctx.status(500).json(Map.of(
                        "error", "Could not update client."
                ));
                return;
            }

            ctx.json(Map.of(
                    "clientId", clientId,
                    "message", "Client updated successfully."
            ));
        });
        
        // Removes a client only if they have no project history.
        app.delete("/api/clients/{id}", ctx -> {

            String clientId = ctx.pathParam("id");

            if (!userManager.userHasRole(clientId, "CLIENT")) {
                ctx.status(404).json(Map.of(
                        "error", "Client not found."
                ));
                return;
            }

            boolean hasHistory;

            try {
                hasHistory = userManager.clientHasHistory(clientId);
            } catch (IllegalStateException e) {
                ctx.status(500).json(Map.of(
                        "error", "Could not verify client assignments."
                ));
                return;
            }

            if (hasHistory) {
                ctx.status(409).json(Map.of(
                        "error",
                        "Client has project or change order history and cannot be deleted."
                ));
                return;
            }

            boolean deleted = userManager.deleteUser(clientId);

            if (!deleted) {
                ctx.status(500).json(Map.of(
                        "error", "Could not delete client."
                ));
                return;
            }

            ctx.json(Map.of(
                    "clientId", clientId,
                    "message", "Client deleted successfully."
            ));
        });

        // Assigns an existing client to a project.
        app.put("/api/projects/{id}/client", ctx -> {

            String projectId = ctx.pathParam("id");
            Map<String, Object> request = ctx.bodyAsClass(Map.class);

            Object clientValue = request.get("clientId");

            if (!(clientValue instanceof String clientId)
                    || clientId.isBlank()) {

                ctx.status(400).json(Map.of(
                        "error", "Client ID is required."
                ));
                return;
            }

            // Checks that the selected user is a registered client.
            if (!userManager.userHasRole(clientId, "CLIENT")) {
                ctx.status(404).json(Map.of(
                        "error", "Client not found."
                ));
                return;
            }

            // Checks that the project exists and is not archived.
            model.Project existingProject = projectManager.getProject(projectId);

            if (existingProject == null) {
                ctx.status(404).json(Map.of(
                        "error", "Project not found."
                ));
                return;
            }

            if ("ARCHIVED".equals(existingProject.getStatus())) {
                ctx.status(409).json(Map.of(
                        "error", "Cannot assign clients to archived projects."
                ));
                return;
            }

            boolean assigned = projectManager.addClientToProject(
                    projectId, clientId
            );

            if (!assigned) {
                ctx.status(500).json(Map.of(
                        "error", "Could not assign client."
                ));
                return;
            }

            ctx.json(Map.of(
                    "projectId", projectId,
                    "clientId", clientId,
                    "message", "Client assigned successfully."
            ));
        });
 
        // Retrieves projects assigned to a client.
        // Local development only until secure authentication is added.
        app.get("/api/client/projects", ctx -> {
            String clientId = ctx.queryParam("clientId");

            if (clientId == null || clientId.isBlank()) {
                ctx.status(400).json(Map.of(
                        "error", "Client ID is required."
                ));
                return;
            }

            ctx.json(projectManager.getProjectsByClient(clientId));
        });

        // Creates a new construction project.
        app.post("/api/projects", ctx -> {

            ProjectRequest request =
                    ctx.bodyAsClass(
                            ProjectRequest.class);


//          Makes sure a project name was provided.
            if (request.getProjectName() == null ||
                    request.getProjectName()
                            .isBlank()) {

                ctx.status(400);

                ctx.json(Map.of(
                        "error",
                        "Project name is required."));

                return;
            }


//          Creates the project using the existing
//          ProjectManagementImpl component.
            String projectId =
                    projectManager.createProject(
                            request.getContractorId(),
                            request.getProjectName());


//          Checks that the project was created.
            if (projectId == null) {

                ctx.status(500);

                ctx.json(Map.of(
                        "error",
                        "Could not create project."));

                return;
            }


//          Stores the project's budget.
            boolean budgetUpdated =
                    expenseManager
                            .updateProjectBudget(
                                    projectId,
                                    request.getBudget());


//          Creates the response returned to React.
            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "projectId",
                    projectId);

            response.put(
                    "projectName",
                    request.getProjectName());

            response.put(
                    "budget",
                    request.getBudget());

            response.put(
                    "status",
                    "ACTIVE");

            response.put(
                    "progress",
                    0);

            response.put(
                    "budgetUpdated",
                    budgetUpdated);


//          Sends the newly created project
//          back to the frontend.
            ctx.status(201);
            ctx.json(response);
        });
          
        
        // Adds a new expense to a construction project.
        app.post("/api/expenses", ctx -> {

            Map<String, Object> request =
                    ctx.bodyAsClass(Map.class);

            Object projectValue = request.get("projectId");
            Object descriptionValue = request.get("description");
            Object amountValue = request.get("amount");

            // Validates the required expense information.
            if (!(projectValue instanceof String projectId)
                    || projectId.isBlank()
                    || !(descriptionValue instanceof String description)
                    || description.isBlank()
                    || !(amountValue instanceof Number)) {

                ctx.status(400).json(Map.of(
                        "error", "Project, description, and amount are required."
                ));
                return;
            }

            double amount = ((Number) amountValue).doubleValue();

            // Makes sure the expense amount is valid.
            if (!Double.isFinite(amount) || amount <= 0) {

                ctx.status(400).json(Map.of(
                        "error", "Expense amount must be greater than zero."
                ));
                return;
            }

            // Checks that the project exists.
            model.Project existingProject =
                    projectManager.getProject(projectId);

            if (existingProject == null) {
                ctx.status(404).json(Map.of(
                        "error", "Project not found."
                ));
                return;
            }

            // Saves the expense using ExpenseBudgetImpl.
            String expenseId = expenseManager.addExpense(
                    projectId,
                    description.trim(),
                    amount
            );

            // Checks that the expense was saved successfully.
            if (expenseId == null) {

                ctx.status(500).json(Map.of(
                        "error", "Could not save expense."
                ));
                return;
            }

            // Returns the saved expense information to React.
            ctx.status(201).json(Map.of(
                    "expenseId", expenseId,
                    "projectId", projectId,
                    "description", description.trim(),
                    "amount", amount
            ));
        });

        
        // Adds a material to a construction project.
        app.post("/api/materials", ctx -> {

            Map<String, Object> request =
                    ctx.bodyAsClass(Map.class);

            Object projectValue = request.get("projectId");
            Object nameValue = request.get("materialName");
            Object quantityValue = request.get("quantity");

            if (!(projectValue instanceof String projectId)
                    || projectId.isBlank()
                    || !(nameValue instanceof String materialName)
                    || materialName.isBlank()
                    || !(quantityValue instanceof Number)) {

                ctx.status(400).json(Map.of(
                        "error", "Project, material name, and quantity are required."
                ));
                return;
            }

            double quantity = ((Number) quantityValue).doubleValue();

            if (!Double.isFinite(quantity) || quantity <= 0) {
                ctx.status(400).json(Map.of(
                        "error", "Quantity must be greater than zero."
                ));
                return;
            }

            String materialId = materialManager.addMaterial(
                    projectId, materialName.trim(), quantity
            );

            if (materialId == null) {
                ctx.status(500).json(Map.of(
                        "error", "Could not add material."
                ));
                return;
            }

            ctx.status(201).json(Map.of(
                    "materialId", materialId,
                    "projectId", projectId,
                    "materialName", materialName.trim(),
                    "quantity", quantity
            ));
        });

        // Updates the quantity of an existing material.
        app.put("/api/materials/{id}/quantity", ctx -> {

            String materialId = ctx.pathParam("id");
            Map<String, Object> request = ctx.bodyAsClass(Map.class);

            Object quantityValue = request.get("quantity");

            if (!(quantityValue instanceof Number)) {
                ctx.status(400).json(Map.of(
                        "error", "A valid quantity is required."
                ));
                return;
            }

            double quantity = ((Number) quantityValue).doubleValue();

            if (!Double.isFinite(quantity) || quantity < 0) {
                ctx.status(400).json(Map.of(
                        "error", "Quantity cannot be negative."
                ));
                return;
            }

            boolean updated = materialManager.updateMaterialQuantity(
                    materialId, quantity
            );

            if (!updated) {
                ctx.status(404).json(Map.of(
                        "error", "Material not found."
                ));
                return;
            }

            ctx.json(Map.of(
                    "materialId", materialId,
                    "quantity", quantity,
                    "message", "Material quantity updated successfully."
            ));
        });

        // Adds equipment to a construction project.
        app.post("/api/equipment", ctx -> {

            Map<String, Object> request = ctx.bodyAsClass(Map.class);

            Object projectValue = request.get("projectId");
            Object nameValue = request.get("equipmentName");

            if (!(projectValue instanceof String projectId)
                    || projectId.isBlank()
                    || !(nameValue instanceof String equipmentName)
                    || equipmentName.isBlank()) {

                ctx.status(400).json(Map.of(
                        "error", "Project and equipment name are required."
                ));
                return;
            }

            String equipmentId = materialManager.addEquipment(
                    projectId, equipmentName.trim()
            );

            if (equipmentId == null) {
                ctx.status(500).json(Map.of(
                        "error", "Could not add equipment."
                ));
                return;
            }

            ctx.status(201).json(Map.of(
                    "equipmentId", equipmentId,
                    "projectId", projectId,
                    "equipmentName", equipmentName.trim()
            ));
        });
        
        // Removes an expense from a project.
        app.delete("/api/expenses/{id}", ctx -> {
            String expenseId = ctx.pathParam("id");

            boolean removed = expenseManager.removeExpense(expenseId);

            if (!removed) {
                ctx.status(404).json(Map.of(
                        "error", "Expense not found."
                ));
                return;
            }

            ctx.json(Map.of(
                    "expenseId", expenseId,
                    "message", "Expense removed successfully."
            ));
        });
        
        // Removes a material from a project.
        app.delete("/api/materials/{id}", ctx -> {

            String materialId = ctx.pathParam("id");

            boolean removed = materialManager.removeMaterial(materialId);

            if (!removed) {
                ctx.status(404).json(Map.of(
                        "error", "Material not found."
                ));
                return;
            }

            ctx.json(Map.of(
                    "materialId", materialId,
                    "message", "Material removed successfully."
            ));
        });

        // Removes equipment from a project.
        app.delete("/api/equipment/{id}", ctx -> {

            String equipmentId = ctx.pathParam("id");

            boolean removed = materialManager.removeEquipment(equipmentId);

            if (!removed) {
                ctx.status(404).json(Map.of(
                        "error", "Equipment not found."
                ));
                return;
            }

            ctx.json(Map.of(
                    "equipmentId", equipmentId,
                    "message", "Equipment removed successfully."
            ));
        });
        
        // Retrieves materials and equipment for a project.
        app.get("/api/projects/{id}/inventory", ctx -> {

            String projectId = ctx.pathParam("id");

            if (projectId == null || projectId.isBlank()) {
                ctx.status(400).json(Map.of(
                        "error", "Project ID is required."
                ));
                return;
            }

            Map<String, Object> response = new HashMap<>();

            response.put("projectId", projectId);
            response.putAll(
                    materialManager.getProjectMaterialsAndEquipment(projectId)
            );

            ctx.json(response);
        });
     
        // Retrieves all expenses recorded for a specific project.
        app.get("/api/projects/{id}/expenses", ctx -> {

            String projectId = ctx.pathParam("id");

            if (projectId == null || projectId.isBlank()) {
                ctx.status(400).json(Map.of(
                        "error", "Project ID is required."
                ));
                return;
            }

            ctx.json(expenseManager.getProjectExpenseRecords(projectId));
        });

        // Retrieves the total amount spent on a project.
        app.get("/api/projects/{id}/expenses/total", ctx -> {

            String projectId = ctx.pathParam("id");

            double totalExpenses =
                    expenseManager.getProjectExpenses(projectId);

            ctx.json(Map.of(
                    "projectId", projectId,
                    "totalExpenses", totalExpenses
            ));
        });

        // Retrieves the remaining project budget.
        app.get("/api/projects/{id}/budget/remaining", ctx -> {

            String projectId = ctx.pathParam("id");

            double remainingBudget =
                    expenseManager.getRemainingBudget(projectId);

            ctx.json(Map.of(
                    "projectId", projectId,
                    "remainingBudget", remainingBudget
            ));
        });
        
        // Updates an existing project's information.
        app.put("/api/projects/{id}", ctx -> {
            String projectId = ctx.pathParam("id");
            Map<String, Object> request = ctx.bodyAsClass(Map.class);
            Object nameValue = request.get("projectName");
            Object progressValue = request.get("progress");
            Object dateValue = request.get("estimatedCompletionDate");
            Object budgetValue = request.get("budget");

            // Validates the project information.
            if (!(nameValue instanceof String projectName)
                    || projectName.isBlank()
                    || !(progressValue instanceof Number)
                    || !(budgetValue instanceof Number)
                    || (dateValue != null && !(dateValue instanceof String))) {
                ctx.status(400).json(Map.of(
                        "error", "Valid project name, progress, date, and budget are required."
                ));
                return;
            }

            double progress = ((Number) progressValue).doubleValue();
            double budget = ((Number) budgetValue).doubleValue();

            if (!Double.isFinite(progress) || progress < 0 || progress > 100
                    || !Double.isFinite(budget) || budget < 0) {
                ctx.status(400).json(Map.of(
                        "error", "Progress must be between 0 and 100, and budget cannot be negative."
                ));
                return;
            }

            // Retrieves the existing project without changing its assignments.
            model.Project existingProject = projectManager.getProject(projectId);

            if (existingProject == null) {
                ctx.status(404).json(Map.of(
                        "error", "Project not found."
                ));
                return;
            }

            if ("ARCHIVED".equals(existingProject.getStatus())) {
                ctx.status(409).json(Map.of(
                        "error", "Archived projects cannot be edited."
                ));
                return;
            }

            // Updates only the editable project fields.
            existingProject.setProjectName(projectName.trim());
            existingProject.setProgress(progress);
            existingProject.setEstimatedCompletionDate(
                    dateValue == null ? null : ((String) dateValue).trim()
            );
            existingProject.setBudget(budget);

            boolean updated = projectManager.updateProject(existingProject);

            if (!updated) {
                ctx.status(500).json(Map.of(
                        "error", "Could not update project."
                ));
                return;
            }

            ctx.json(Map.of(
                    "projectId", projectId,
                    "message", "Project updated successfully."
            ));
        });

        // Archives a project without deleting its information.
        app.delete("/api/projects/{id}", ctx -> {
            String projectId = ctx.pathParam("id");

            if (projectId == null || projectId.isBlank()) {
                ctx.status(400).json(Map.of(
                        "error", "Project ID is required."
                ));
                return;
            }

            boolean archived = projectManager.deleteProject(projectId);

            if (!archived) {
                ctx.status(404).json(Map.of(
                        "error", "Project not found or already archived."
                ));
                return;
            }

            ctx.json(Map.of(
                    "projectId", projectId,
                    "status", "ARCHIVED",
                    "message", "Project archived successfully."
            ));
        });

        // Returns all projects currently
        // stored in the SQLite database.
        app.get("/api/projects", ctx -> {

            ctx.json(
                    projectManager.getAllProjects());
        });

        // Updates the status of an existing project.
        app.put("/api/projects/{id}/status", ctx -> {
            String projectId = ctx.pathParam("id");
            Map<String, Object> request = ctx.bodyAsClass(Map.class);
            Object statusValue = request.get("status");
            if (!(statusValue instanceof String status) || status.isBlank()) {
                ctx.status(400).json(Map.of(
                        "error", "Project status is required."
                ));
                return;
            }
            String updatedStatus = status.trim().toUpperCase();
            if (!updatedStatus.equals("ACTIVE")
                    && !updatedStatus.equals("ON_HOLD")
                    && !updatedStatus.equals("COMPLETED")) {
                ctx.status(400).json(Map.of(
                        "error", "Status must be ACTIVE, ON_HOLD, or COMPLETED."
                ));
                return;
            }
            boolean updated = projectManager.updateProjectStatus(
                    projectId, updatedStatus
            );
            if (!updated) {
                ctx.status(404).json(Map.of(
                        "error", "Project not found or archived."
                ));
                return;
            }
            ctx.json(Map.of(
                    "projectId", projectId,
                    "status", updatedStatus,
                    "message", "Project status updated successfully."
            ));
        });

        // Retrieves detailed information about a specific project.
        app.get("/api/projects/{id}", ctx -> {
            String projectId = ctx.pathParam("id");

            if (projectId == null || projectId.isBlank()) {
                ctx.status(400).json(Map.of(
                        "error", "Project ID is required."
                ));
                return;
            }

            model.Project project = projectManager.getProject(projectId);

            if (project == null) {
                ctx.status(404).json(Map.of(
                        "error", "Project not found."
                ));
                return;
            }

            ctx.json(project);
        });

        // Returns all workers currently
        // stored in the SQLite database.
        app.get("/api/workers", ctx -> {

            ctx.json(
                    workforceManager.getAllWorkers());
        });
        
        // Deactivates a worker without deleting their work history.
        app.delete("/api/workers/{id}", ctx -> {

            String workerId = ctx.pathParam("id");

            if (workerId == null || workerId.isBlank()) {
                ctx.status(400).json(
                    Map.of("error", "Worker ID is required.")
                );
                return;
            }

            boolean deactivated = workforceManager.deactivateWorker(workerId);

            if (!deactivated) {
                ctx.status(404).json(
                    Map.of("error", "Worker not found or already deactivated.")
                );
                return;
            }

            ctx.status(200).json(
                Map.of(
                    "workerId", workerId,
                    "message", "Worker deactivated successfully."
                )
            );
        });
        
        // Retrieves all inactive workers.
        app.get("/api/workers/inactive", ctx -> {

            ctx.json(workforceManager.getInactiveWorkers());

        });

        // Reactivates a previously deactivated worker.
        app.put("/api/workers/{id}/reactivate", ctx -> {

            String workerId = ctx.pathParam("id");

            if (workerId == null || workerId.isBlank()) {

                ctx.status(400).json(Map.of(
                        "error", "Worker ID is required."
                ));

                return;
            }

            boolean reactivated =
                    workforceManager.reactivateWorker(workerId);

            if (!reactivated) {

                ctx.status(404).json(Map.of(
                        "error", "Worker not found or already active."
                ));

                return;
            }

            ctx.status(200).json(Map.of(
                    "workerId", workerId,
                    "message", "Worker reactivated successfully."
            ));

        });

        // Adds a new worker to a project.
        app.post("/api/workers", ctx -> {

            WorkerRequest request =
                    ctx.bodyAsClass(
                            WorkerRequest.class);


//          Makes sure the required worker
//          information was provided.
            if (request.getProjectId() == null ||
                    request.getProjectId().isBlank() ||
                    request.getWorkerName() == null ||
                    request.getWorkerName().isBlank() ||
                    request.getTrade() == null ||
                    request.getTrade().isBlank()) {

                ctx.status(400);

                ctx.json(Map.of(
                        "error",
                        "Project, worker name, and trade are required."));

                return;
            }


//          Adds the worker using the existing
//          WorkforceManagementImpl component.
            String workerId =
                    workforceManager.addWorker(
                            request.getProjectId(),
                            request.getWorkerName(),
                            request.getTrade());


//          Checks that the worker was created.
            if (workerId == null) {

                ctx.status(500);

                ctx.json(Map.of(
                        "error",
                        "Could not add worker."));

                return;
            }


//          Creates the worker information
//          returned to the React frontend.
            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "workerId",
                    workerId);

            response.put(
                    "projectId",
                    request.getProjectId());

            response.put(
                    "workerName",
                    request.getWorkerName());

            response.put(
                    "trade",
                    request.getTrade());

            response.put(
                    "area",
                    "");

            response.put(
                    "taskId",
                    "");


//          Sends the newly created worker
//          back to the frontend.
            ctx.status(201);
            ctx.json(response);
        });
        
        // Assigns or reassigns a worker to an area and task.
        app.put("/api/workers/{id}/assignment", ctx -> {

            String workerId = ctx.pathParam("id");

            Map<String, String> request =
                    ctx.bodyAsClass(Map.class);

            String projectId = request.get("projectId");
            String area = request.get("area");
            String task = request.get("task");

            if (projectId == null || projectId.isBlank() ||
                    area == null || area.isBlank() ||
                    task == null || task.isBlank()) {

                ctx.status(400);
                ctx.json(Map.of(
                        "error",
                        "Project, area, and task are required."));
                return;
            }

            boolean updated = workforceManager.assignWorker(
                    workerId,
                    projectId,
                    area,
                    task);

            if (!updated) {
                ctx.status(404);
                ctx.json(Map.of(
                        "error",
                        "Worker not found or assignment failed."));
                return;
            }

            ctx.json(Map.of(
                    "workerId", workerId,
                    "projectId", projectId,
                    "area", area,
                    "taskId", task
            ));
        });
        
        // Records the hours a worker spent on an assigned task.
        app.post("/api/workers/{id}/hours", ctx -> {

            String workerId = ctx.pathParam("id");

            Map<String, Object> request = ctx.bodyAsClass(Map.class);

            Object taskValue = request.get("taskId");
            Object hoursValue = request.get("hours");
            Object dateValue = request.get("workDate");

            if (!(taskValue instanceof String)
                    || ((String) taskValue).isBlank()
                    || !(hoursValue instanceof Number)
                    || !(dateValue instanceof String)
                    || ((String) dateValue).isBlank()) {

                ctx.status(400);
                ctx.json(Map.of(
                    "error", "Valid task ID, hours, and work date are required."
                ));
                return;
            }

            String taskId = ((String) taskValue).trim();
            double hours = ((Number) hoursValue).doubleValue();
            String workDate = ((String) dateValue).trim();

            if (!Double.isFinite(hours) || hours <= 0) {
                ctx.status(400);
                ctx.json(Map.of(
                    "error", "Hours must be greater than zero."
                ));
                return;
            }

            // Validates the work date format.
            try {
                java.time.LocalDate.parse(workDate);

            } catch (java.time.format.DateTimeParseException e) {

                ctx.status(400);
                ctx.json(Map.of(
                    "error", "Work date must be a valid date in YYYY-MM-DD format."
                ));
                return;
            }

            boolean saved = workforceManager.recordWorkHours(
                workerId,
                taskId,
                hours,
                workDate
            );

            if (!saved) {
                ctx.status(400);
                ctx.json(Map.of(
                    "error", "Could not record work hours. Check the worker, task, hours, and date."
                ));
                return;
            }

            ctx.status(201);
            ctx.json(Map.of(
                "workerId", workerId,
                "taskId", taskId,
                "hours", hours,
                "workDate", workDate,
                "message", "Work hours recorded successfully."
            ));

        });
        
        // Retrieves all recorded work hours for a worker.
        app.get("/api/workers/{id}/hours", ctx -> {

            String workerId = ctx.pathParam("id");

            if (workerId == null || workerId.isBlank()) {
                ctx.status(400).json(Map.of(
                        "error", "Worker ID is required."
                ));
                return;
            }

            ctx.json(workforceManager.getWorkerHours(workerId));
        });
    
        // Retrieves a change order by its ID.
        // Development-only route until authentication is implemented.
        app.get("/api/change-orders/{id}", ctx -> {

            String changeOrderId = ctx.pathParam("id");

            ChangeOrder changeOrder =
                    changeOrderManager.getChangeOrder(changeOrderId);

            if (changeOrder == null) {

                ctx.status(404).json(Map.of(
                        "error", "Change order not found."
                ));
                return;
            }

            ctx.json(changeOrder);
        });   
        
	    // Retrieves all change orders for a project.
	    // Development-only until project authorization is implemented.
	    app.get("/api/projects/{id}/change-orders", ctx -> {
	
	        String projectId = ctx.pathParam("id");
	
	        ctx.json(
	                changeOrderManager.getProjectChangeOrders(projectId)
	        );
	    });    

        // Starts the backend on port 7070.
        app.start(7070);
    }
}
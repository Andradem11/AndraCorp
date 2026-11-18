package server;

import applicationimpl.ChangeOrderImpl;
import applicationimpl.WorkforceProposalEstimator;
import applicationimpl.ExpenseBudgetImpl;
import applicationimpl.MaterialEquipmentImpl;
import applicationimpl.ProjectManagementImpl;
import applicationimpl.UserManagementImpl;
import applicationimpl.WorkforceManagementImpl;
import io.javalin.Javalin;
import model.ChangeOrder;
import storageimpl.DatabaseManager;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

        // Contractor-configured hourly billing rates for each trade.
        app.get("/api/trade-rates", ctx -> {
            List<Map<String, Object>> rates = new ArrayList<>();
            try (Connection connection = DatabaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT trade, hourly_rate FROM trade_rates ORDER BY trade");
                 ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rates.add(Map.of("trade", result.getString("trade"),
                            "hourlyRate", result.getDouble("hourly_rate")));
                }
                ctx.json(rates);
            } catch (SQLException e) {
                ctx.status(500).json(Map.of("error", "Could not load trade rates."));
            }
        });

        app.put("/api/trade-rates/{trade}", ctx -> {
            String trade = ctx.pathParam("trade");
            Map<?, ?> request = ctx.bodyAsClass(Map.class);
            Object rawRate = request.get("hourlyRate");
            double rate;
            try {
                rate = Double.parseDouble(String.valueOf(rawRate));
            } catch (NumberFormatException e) {
                ctx.status(400).json(Map.of("error", "A valid hourly rate is required."));
                return;
            }
            if (!Double.isFinite(rate) || rate < 0 || trade.isBlank()) {
                ctx.status(400).json(Map.of("error", "Hourly rate must be zero or greater."));
                return;
            }
            try (Connection connection = DatabaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement("""
                         INSERT INTO trade_rates(trade, hourly_rate) VALUES (?, ?)
                         ON CONFLICT(trade) DO UPDATE SET hourly_rate = excluded.hourly_rate
                         """)) {
                statement.setString(1, trade);
                statement.setDouble(2, rate);
                statement.executeUpdate();
                ctx.json(Map.of("trade", trade, "hourlyRate", rate));
            } catch (SQLException e) {
                ctx.status(500).json(Map.of("error", "Could not save trade rate."));
            }
        });

        // Updates one worker's hourly rate without changing the trade default.
        // Previously recorded labor costs keep their original rate snapshots.
        app.put("/api/workers/{id}/hourly-rate", ctx -> {
            String workerId = ctx.pathParam("id");
            Map<?, ?> request = ctx.bodyAsClass(Map.class);
            Object rawRate = request.get("hourlyRate");
            double hourlyRate;

            try {
                if (rawRate == null || String.valueOf(rawRate).isBlank()) {
                    throw new NumberFormatException();
                }
                hourlyRate = Double.parseDouble(String.valueOf(rawRate));
            } catch (NumberFormatException e) {
                ctx.status(400).json(Map.of("error", "Enter a valid hourly rate."));
                return;
            }

            if (!Double.isFinite(hourlyRate) || hourlyRate < 0) {
                ctx.status(400).json(Map.of("error", "Hourly rate must be zero or greater."));
                return;
            }

            try (Connection connection = DatabaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "UPDATE workers SET hourly_rate = ? WHERE worker_id = ? AND is_active = 1")) {
                statement.setDouble(1, hourlyRate);
                statement.setString(2, workerId);

                if (statement.executeUpdate() == 0) {
                    ctx.status(404).json(Map.of("error", "Active worker not found."));
                    return;
                }

                ctx.json(Map.of("workerId", workerId, "hourlyRate", hourlyRate));
            } catch (SQLException e) {
                ctx.status(500).json(Map.of("error", "Could not update worker hourly rate."));
            }
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
            try (Connection connection = DatabaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT hourly_rate FROM workers WHERE worker_id = ?")) {
                statement.setString(1, workerId);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        response.put("hourlyRate", result.getObject(1) == null
                                ? null : result.getDouble(1));
                    }
                }
            } catch (SQLException e) {
                System.out.println("Could not load worker rate: " + e.getMessage());
            }


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

        // Creates a client or contractor change request.
        app.post("/api/change-orders", ctx -> {
            Map<String, Object> request;
            try {
                request = ctx.bodyAsClass(Map.class);
            } catch (Exception e) {
                ctx.status(400).json(Map.of("error", "Invalid request body."));
                return;
            }

            Object projectId = request.get("projectId");
            Object requestedBy = request.get("requestedBy");
            Object description = request.get("description");

            if (!(projectId instanceof String) || ((String) projectId).isBlank()
                    || !(requestedBy instanceof String)
                    || !(description instanceof String) || ((String) description).isBlank()
                    || (!((String) requestedBy).equalsIgnoreCase("CLIENT")
                    && !((String) requestedBy).equalsIgnoreCase("CONTRACTOR"))) {
                ctx.status(400).json(Map.of("error", "Valid projectId, requestedBy, and description are required."));
                return;
            }

            // Old clients may omit changeType; their orders remain GENERAL.
            Object typeValue = request.getOrDefault("changeType", "GENERAL");
            Object actionValue = request.get("workforceAction");
            Object countValue = request.get("requestedWorkerCount");
            Object sourceValue = request.get("sourceArea");
            Object targetValue = request.get("targetArea");
            Object materialValue = request.get("materialPreference");
            if (!(typeValue instanceof String)
                    || (actionValue != null && !(actionValue instanceof String))
                    || (countValue != null && (!(countValue instanceof Number)
                        || ((Number) countValue).doubleValue() != ((Number) countValue).intValue()))
                    || (sourceValue != null && !(sourceValue instanceof String))
                    || (targetValue != null && !(targetValue instanceof String))
                    || (materialValue != null && !(materialValue instanceof String))) {
                ctx.status(400).json(Map.of("error", "Invalid structured change request."));
                return;
            }
            String id = changeOrderManager.createStructuredChangeOrder(
                    (String) projectId, (String) requestedBy, (String) description,
                    (String) typeValue, (String) actionValue,
                    countValue == null ? null : ((Number) countValue).intValue(),
                    (String) sourceValue, (String) targetValue, (String) materialValue);
            if (id == null) {
                ctx.status(400).json(Map.of("error", "Could not create change order. Check the project is active and has a client."));
                return;
            }
            ctx.status(201).json(changeOrderManager.getChangeOrder(id));
        });

        // Simulates labor, material, and equipment changes for a client request.
        app.post("/api/change-orders/{id}/simulate", ctx -> {
            Map<String, Object> request;
            try {
                request = ctx.bodyAsClass(Map.class);
            } catch (Exception e) {
                ctx.status(400).json(Map.of("error", "Invalid request body."));
                return;
            }

            Object labor = request.get("laborCostChange");
            Object materials = request.get("materialCostChange");
            Object equipment = request.get("equipmentCostChange");
            if (!(labor instanceof Number) || !(materials instanceof Number)
                    || !(equipment instanceof Number)
                    || !Double.isFinite(((Number) labor).doubleValue())
                    || !Double.isFinite(((Number) materials).doubleValue())
                    || !Double.isFinite(((Number) equipment).doubleValue())) {
                ctx.status(400).json(Map.of("error", "Valid numeric laborCostChange, materialCostChange, and equipmentCostChange are required."));
                return;
            }

            String id = ctx.pathParam("id");
            boolean saved = changeOrderManager.simulateClientChange(id,
                    ((Number) labor).doubleValue(),
                    ((Number) materials).doubleValue(),
                    ((Number) equipment).doubleValue());
            if (!saved) {
                ctx.status(409).json(Map.of("error", "Simulation failed. Order must be a pending client request."));
                return;
            }
            ctx.json(changeOrderManager.getChangeOrder(id));
        });

        // Read-only workforce estimate. Does not create expenses or update a proposal.
        app.post("/api/change-orders/{id}/workforce-estimate", ctx -> {
            Map<String, Object> request;
            try {
                request = ctx.bodyAsClass(Map.class);
            } catch (Exception e) {
                ctx.status(400).json(Map.of("error", "Invalid request body."));
                return;
            }
            Object contractorValue = request.get("contractorId");
            Object tradeValue = request.get("trade");
            Object countValue = request.get("workerCount");
            Object hoursValue = request.get("hoursPerWorker");
            if (!(contractorValue instanceof String contractorId) || contractorId.isBlank()
                    || !(tradeValue instanceof String trade) || trade.isBlank()
                    || !(countValue instanceof Number count) || count.doubleValue() != count.intValue()
                    || !(hoursValue instanceof Number hours)) {
                ctx.status(400).json(Map.of("error", "Contractor, trade, worker count, and estimated hours are required."));
                return;
            }
            String sql = """
                    SELECT tr.hourly_rate
                    FROM change_orders co
                    JOIN projects p ON p.project_id = co.project_id
                    JOIN trade_rates tr ON LOWER(tr.trade) = LOWER(?)
                    WHERE co.change_order_id = ? AND p.contractor_id = ?
                      AND UPPER(p.status) = 'ACTIVE'
                      AND co.status IN ('PENDING_REVIEW', 'COUNTERPROPOSAL_PENDING')
                      AND co.change_type = 'WORKFORCE'
                      AND co.workforce_action = 'ADD_WORKERS'
                    """;
            try (Connection connection = DatabaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, trade.trim());
                statement.setString(2, ctx.pathParam("id"));
                statement.setString(3, contractorId);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        ctx.status(404).json(Map.of("error", "Eligible workforce request or configured trade rate not found."));
                        return;
                    }
                    double rate = result.getDouble("hourly_rate");
                    double estimate = WorkforceProposalEstimator.calculate(count.intValue(), hours.doubleValue(), rate);
                    ctx.json(Map.of("trade", trade.trim(), "workerCount", count.intValue(),
                            "hoursPerWorker", hours.doubleValue(), "hourlyRate", rate,
                            "estimatedLaborCost", estimate,
                            "note", "Estimate only. No expenses or worker assignments have been changed."));
                }
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            } catch (SQLException e) {
                ctx.status(500).json(Map.of("error", "Unable to calculate workforce estimate."));
            }
        });

        // Read-only comparison for a client's worker reallocation request.
        // Moving existing workers is not treated as newly incurred labor expense.
        app.post("/api/change-orders/{id}/reallocation-compare", ctx -> {
            Map<String, Object> input;
            try { input = ctx.bodyAsClass(Map.class); }
            catch (Exception e) { ctx.status(400).json(Map.of("error", "Invalid request body.")); return; }
            Object contractor = input.get("contractorId");
            Object idsValue = input.get("workerIds");
            Object hoursValue = input.get("hoursPerWorker");
            Object tradeValue = input.get("hireTrade");
            if (!(contractor instanceof String contractorId) || contractorId.isBlank()
                    || !(idsValue instanceof List<?> workerIds) || workerIds.isEmpty()
                    || !(hoursValue instanceof Number hours) || !(tradeValue instanceof String hireTrade)
                    || hireTrade.isBlank() || !Double.isFinite(hours.doubleValue()) || hours.doubleValue() <= 0) {
                ctx.status(400).json(Map.of("error", "Contractor, selected workers, positive hours, and hiring trade are required.")); return;
            }
            if (workerIds.size() > 100 || workerIds.stream().anyMatch(id -> !(id instanceof String) || ((String) id).isBlank())
                    || workerIds.stream().distinct().count() != workerIds.size()) {
                ctx.status(400).json(Map.of("error", "Worker IDs must be unique and valid.")); return;
            }
            String eligibilitySql = """
                    SELECT co.project_id, co.source_area, co.target_area, co.requested_worker_count
                    FROM change_orders co JOIN projects p ON p.project_id = co.project_id
                    WHERE co.change_order_id = ? AND p.contractor_id = ? AND UPPER(p.status) = 'ACTIVE'
                    AND co.change_type = 'WORKFORCE' AND co.workforce_action = 'REALLOCATE'
                    AND co.status IN ('PENDING_REVIEW', 'COUNTERPROPOSAL_PENDING')
                    """;
            try (Connection connection = DatabaseManager.getConnection();
                 PreparedStatement eligible = connection.prepareStatement(eligibilitySql)) {
                eligible.setString(1, ctx.pathParam("id")); eligible.setString(2, contractorId);
                try (ResultSet order = eligible.executeQuery()) {
                    if (!order.next()) { ctx.status(404).json(Map.of("error", "Eligible reallocation request not found.")); return; }
                    String projectId = order.getString("project_id");
                    String source = order.getString("source_area");
                    String target = order.getString("target_area");
                    int requested = order.getInt("requested_worker_count");
                    if (source == null || target == null || source.isBlank() || target.isBlank()
                            || source.equalsIgnoreCase(target) || workerIds.size() != requested || requested < 1) {
                        ctx.status(400).json(Map.of("error", "Select exactly the requested number of workers from distinct work areas.")); return;
                    }
                    List<Map<String, Object>> selected = new ArrayList<>();
                    double existingHourlyCost = 0;
                    String workerSql = "SELECT worker_name, trade, hourly_rate FROM workers WHERE worker_id = ? AND project_id = ? AND area = ? AND is_active = 1";
                    try (PreparedStatement worker = connection.prepareStatement(workerSql)) {
                        for (Object id : workerIds) {
                            worker.setString(1, (String) id); worker.setString(2, projectId); worker.setString(3, source);
                            try (ResultSet result = worker.executeQuery()) {
                                if (!result.next()) { ctx.status(400).json(Map.of("error", "Selected worker is not active in the requested source area.")); return; }
                                double rate = result.getDouble("hourly_rate");
                                if (result.wasNull() || !Double.isFinite(rate) || rate < 0) {
                                    ctx.status(400).json(Map.of("error", "Selected worker has no valid hourly rate.")); return;
                                }
                                existingHourlyCost += rate;
                                selected.add(Map.of("workerId", id, "name", result.getString("worker_name"),
                                        "trade", result.getString("trade") == null ? "Unspecified" : result.getString("trade"), "hourlyRate", rate));
                            }
                        }
                    }
                    try (PreparedStatement rateQuery = connection.prepareStatement("SELECT hourly_rate FROM trade_rates WHERE LOWER(trade) = LOWER(?)")) {
                        rateQuery.setString(1, hireTrade.trim());
                        try (ResultSet rates = rateQuery.executeQuery()) {
                            if (!rates.next()) { ctx.status(400).json(Map.of("error", "No configured rate for the hiring trade.")); return; }
                            double hireRate = rates.getDouble(1);
                            double hireCost = WorkforceProposalEstimator.calculate(requested, hours.doubleValue(), hireRate);
                            Map<String, Object> comparison = new java.util.HashMap<>();
                            comparison.put("sourceArea", source);
                            comparison.put("targetArea", target);
                            comparison.put("selectedWorkers", selected);
                            comparison.put("estimatedHoursPerWorker", hours.doubleValue());
                            comparison.put("existingLaborAllocationValue", Math.round(existingHourlyCost * hours.doubleValue() * 100.0) / 100.0);
                            comparison.put("reallocationAdditionalLaborCost", 0);
                            comparison.put("hiringTrade", hireTrade.trim());
                            comparison.put("hiringHourlyRate", hireRate);
                            comparison.put("hiringAdditionalLaborCost", hireCost);
                            // Scenario forecast only: remaining effort and productivity are contractor assumptions.
                            // This does not modify task dates, worker assignments, expenses or approvals.
                            Object sourceEffort = input.get("sourceRemainingWorkerHours");
                            Object targetEffort = input.get("targetRemainingWorkerHours");
                            Object dailyCapacity = input.get("productiveHoursPerWorkerDay");
                            if (sourceEffort instanceof Number se && targetEffort instanceof Number te
                                    && dailyCapacity instanceof Number dc
                                    && Double.isFinite(se.doubleValue()) && se.doubleValue() >= 0
                                    && Double.isFinite(te.doubleValue()) && te.doubleValue() >= 0
                                    && Double.isFinite(dc.doubleValue()) && dc.doubleValue() > 0 && dc.doubleValue() <= 24) {
                                int sourceCount = 0;
                                int targetCount = 0;
                                try (PreparedStatement counts = connection.prepareStatement(
                                        "SELECT area, COUNT(*) AS n FROM workers WHERE project_id = ? AND is_active = 1 AND (area = ? OR area = ?) GROUP BY area")) {
                                    counts.setString(1, projectId);
                                    counts.setString(2, source);
                                    counts.setString(3, target);
                                    try (ResultSet rs = counts.executeQuery()) {
                                        while (rs.next()) {
                                            if (source.equals(rs.getString("area"))) sourceCount = rs.getInt("n");
                                            if (target.equals(rs.getString("area"))) targetCount = rs.getInt("n");
                                        }
                                    }
                                }
                                if (sourceCount >= requested && targetCount > 0) {
                                    double capacity = dc.doubleValue();
                                    double baselineSource = se.doubleValue() / (sourceCount * capacity);
                                    double baselineTarget = te.doubleValue() / (targetCount * capacity);
                                    Double movedSource = sourceCount > requested
                                            ? se.doubleValue() / ((sourceCount - requested) * capacity) : null;
                                    double movedTarget = te.doubleValue() / ((targetCount + requested) * capacity);
                                    double hiredTarget = movedTarget;
                                    Map<String, Object> forecast = new java.util.HashMap<>();
                                    forecast.put("sourceWorkersBefore", sourceCount);
                                    forecast.put("targetWorkersBefore", targetCount);
                                    forecast.put("baselineSourceDays", baselineSource);
                                    forecast.put("baselineTargetDays", baselineTarget);
                                    forecast.put("moveSourceDays", movedSource);
                                    forecast.put("moveTargetDays", movedTarget);
                                    forecast.put("hireSourceDays", baselineSource);
                                    forecast.put("hireTargetDays", hiredTarget);
                                    forecast.put("targetDaysSaved", baselineTarget - movedTarget);
                                    forecast.put("sourceDaysDelayed", movedSource == null ? null : movedSource - baselineSource);
                                    forecast.put("assumptions", "Remaining effort is in worker-hours; all counted workers are assumed equally productive and qualified. Calendar dependencies and availability are not modeled.");
                                    comparison.put("scheduleScenario", forecast);
                                    comparison.put("scheduleImpact", "Illustrative capacity-based scenario, not a committed completion date.");
                                } else {
                                    comparison.put("scheduleImpact", "Insufficient staffing for this scenario: source must have the selected workers and target must have at least one worker.");
                                }
                            } else {
                                comparison.put("scheduleImpact", "Enter nonnegative remaining worker-hours for both areas and productive hours per worker per day (greater than 0, up to 24) to compare schedule scenarios.");
                            }
                            comparison.put("note", "Comparison only. Existing worker pay is not a new expense. No workers, budgets, or approvals were changed.");
                            ctx.json(comparison);
                        }
                    }
                }
            } catch (SQLException e) { ctx.status(500).json(Map.of("error", "Unable to compare workforce options.")); }
              catch (IllegalArgumentException e) { ctx.status(400).json(Map.of("error", e.getMessage())); }
        });

        // Contractor confirms the proposed cost and schedule impact.
        app.post("/api/change-orders/{id}/proposal", ctx -> {
            Map<String, Object> request;
            try {
                request = ctx.bodyAsClass(Map.class);
            } catch (Exception e) {
                ctx.status(400).json(Map.of("error", "Invalid request body."));
                return;
            }

            Object contractorId = request.get("contractorId");
            Object solution = request.get("proposedSolution");
            Object cost = request.get("finalCostImpact");
            Object days = request.get("scheduleImpact");
            if (!(contractorId instanceof String) || ((String) contractorId).isBlank()
                    || !(solution instanceof String) || ((String) solution).isBlank()
                    || !(cost instanceof Number) || !Double.isFinite(((Number) cost).doubleValue())
                    || !(days instanceof Number) || !Double.isFinite(((Number) days).doubleValue())
                    || ((Number) days).doubleValue() != ((Number) days).intValue()) {
                ctx.status(400).json(Map.of("error", "Valid contractorId, proposedSolution, finalCostImpact, and integer scheduleImpact are required."));
                return;
            }

            String id = ctx.pathParam("id");
            boolean saved = changeOrderManager.submitContractorProposal(id,
                    (String) contractorId, (String) solution,
                    ((Number) cost).doubleValue(), ((Number) days).intValue());
            if (!saved) {
                ctx.status(409).json(Map.of("error", "Proposal failed. Check contractor assignment and order status."));
                return;
            }
            ctx.json(changeOrderManager.getChangeOrder(id));
        });

        // Client approves the contractor-confirmed proposal.
        app.post("/api/change-orders/{id}/approve", ctx -> {
            Map<String, Object> request;
            try {
                request = ctx.bodyAsClass(Map.class);
            } catch (Exception e) {
                ctx.status(400).json(Map.of("error", "Invalid request body."));
                return;
            }
            Object clientId = request.get("clientId");
            if (!(clientId instanceof String) || ((String) clientId).isBlank()) {
                ctx.status(400).json(Map.of("error", "Valid clientId is required."));
                return;
            }
            String id = ctx.pathParam("id");
            if (!changeOrderManager.approveAsClient(id, (String) clientId)) {
                ctx.status(409).json(Map.of("error", "Approval failed. Check client assignment and order status."));
                return;
            }
            ctx.json(changeOrderManager.getChangeOrder(id));
        });

        // Client declines a proposal and explains why.
        app.post("/api/change-orders/{id}/decline", ctx -> {
            Map<String, Object> request;
            try {
                request = ctx.bodyAsClass(Map.class);
            } catch (Exception e) {
                ctx.status(400).json(Map.of("error", "Invalid request body."));
                return;
            }
            Object clientId = request.get("clientId");
            Object reason = request.get("reason");
            if (!(clientId instanceof String) || ((String) clientId).isBlank()
                    || !(reason instanceof String) || ((String) reason).isBlank()) {
                ctx.status(400).json(Map.of("error", "Valid clientId and decline reason are required."));
                return;
            }
            String id = ctx.pathParam("id");
            if (!changeOrderManager.declineAsClient(id, (String) clientId, (String) reason)) {
                ctx.status(409).json(Map.of("error", "Decline failed. Check client assignment and order status."));
                return;
            }
            ctx.json(changeOrderManager.getChangeOrder(id));
        });

        // Client suggests a different material or another solution.
        // Prototype-only: clientId is not authenticated yet.
        app.post("/api/change-orders/{id}/counterproposal", ctx -> {
            Map<String, Object> request;
            try {
                request = ctx.bodyAsClass(Map.class);
            } catch (Exception e) {
                ctx.status(400).json(Map.of("error", "Invalid request body."));
                return;
            }
            Object clientId = request.get("clientId");
            Object suggestion = request.get("suggestion");
            if (!(clientId instanceof String) || ((String) clientId).isBlank()
                    || !(suggestion instanceof String) || ((String) suggestion).isBlank()) {
                ctx.status(400).json(Map.of("error", "Valid clientId and suggestion are required."));
                return;
            }
            String id = ctx.pathParam("id");
            if (!changeOrderManager.submitClientCounterproposal(
                    id, (String) clientId, (String) suggestion)) {
                ctx.status(409).json(Map.of("error", "Counterproposal failed. Check client assignment and order status."));
                return;
            }
            ctx.json(changeOrderManager.getChangeOrder(id));
        });

        // Returns the recorded actions for a change order.
        // Development-only until session authorization is implemented.
        app.get("/api/change-orders/{id}/history", ctx -> {
            String id = ctx.pathParam("id");
            if (changeOrderManager.getChangeOrder(id) == null) {
                ctx.status(404).json(Map.of("error", "Change order not found."));
                return;
            }
            ctx.json(changeOrderManager.getChangeOrderHistory(id));
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
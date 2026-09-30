package server;

import applicationimpl.ExpenseBudgetImpl;
import applicationimpl.ProjectManagementImpl;
import applicationimpl.WorkforceManagementImpl;
import io.javalin.Javalin;
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


//      Creates the workforce management component.
        WorkforceManagementImpl workforceManager =
                new WorkforceManagementImpl();


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


//      Simple route used to test the backend.
        app.get("/api/test", ctx -> {

            ctx.result(
                    "AndraCorp backend is running!");
        });


//      Creates a new construction project.
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


//      Returns all projects currently
//      stored in the SQLite database.
        app.get("/api/projects", ctx -> {

            ctx.json(
                    projectManager.getAllProjects());
        });


//      Returns all workers currently
//      stored in the SQLite database.
        app.get("/api/workers", ctx -> {

            ctx.json(
                    workforceManager.getAllWorkers());
        });


//      Adds a new worker to a project.
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


//      Starts the backend on port 7070.
        app.start(7070);
    }
}
import { useEffect, useState } from "react";

import "./App.css";


function App() {

  /*
   * Projects are loaded from the Java backend.
   * The Java backend retrieves the project
   * information from the SQLite database.
   */
  const [projects, setProjects] = useState([]);


  /*
   * Workers are loaded from the Java backend.
   * The backend retrieves the worker
   * information from the SQLite database.
   */
  const [workers, setWorkers] = useState([]);


  /*
   * Controls which page is displayed.
   * The dashboard is the default page.
   */
  const [activePage, setActivePage] =
    useState("Dashboard");


//  Controls whether the New Project form is visible.
  const [showProjectForm, setShowProjectForm] =
    useState(false);


//  Stores the information entered in the form.
  const [newProject, setNewProject] = useState({
    name: "",
    budget: "",
  });
  
  /*
   * Controls whether the Add Worker form is visible.
   */
  const [showWorkerForm, setShowWorkerForm] =
    useState(false);


  /*
   * Stores the worker information entered
   * in the Add Worker form.
   */
  const [newWorker, setNewWorker] = useState({
    workerName: "",
    trade: "",
    projectId: "",
  });


  /*
   * Loads all projects from the Java backend
   * when the contractor dashboard first opens.
   */
  useEffect(() => {

    fetch("http://localhost:7070/api/projects")

      .then((response) => {

        if (!response.ok) {
          throw new Error(
            "Could not load projects."
          );
        }

        return response.json();
      })

      .then((data) => {

        /*
         * Converts the backend project information
         * into the format used by the dashboard.
         */
        const loadedProjects = data.map(
          (project) => ({

            id: project.projectId,

            name: project.projectName,

            type: "Residential Project",

            status: project.status,

            progress: project.progress,

            budget: project.budget,
          })
        );

        setProjects(loadedProjects);
      })

      .catch((error) => {

        console.error(
          "Error loading projects:",
          error
        );
      });

  }, []);


  /*
   * Loads all workers from the Java backend
   * when the contractor dashboard first opens.
   */
  useEffect(() => {

    fetch("http://localhost:7070/api/workers")

      .then((response) => {

        if (!response.ok) {
          throw new Error(
            "Could not load workers."
          );
        }

        return response.json();
      })

      .then((data) => {

        /*
         * Stores the workers returned
         * from the SQLite database.
         */
        setWorkers(data);
      })

      .catch((error) => {

        console.error(
          "Error loading workers:",
          error
        );
      });

  }, []);


//  Updates the form when the user types.
  const handleProjectChange = (event) => {

    const { name, value } = event.target;

    setNewProject({
      ...newProject,
      [name]: value,
    });
  };


  /*
   * Sends a new project to the Java backend.
   * The backend saves the project in SQLite.
   */
  const handleCreateProject = async (event) => {

    event.preventDefault();

    if (newProject.name.trim() === "") {
      return;
    }


    const projectRequest = {

      /*
       * A temporary contractor ID is used
       * until user authentication is connected.
       */
      contractorId: "contractor1",

      projectName: newProject.name,

      budget:
        Number(newProject.budget) || 0,
    };


    try {

      const response = await fetch(
        "http://localhost:7070/api/projects",
        {
          method: "POST",

          headers: {
            "Content-Type": "application/json",
          },

          body: JSON.stringify(
            projectRequest
          ),
        }
      );


      if (!response.ok) {

        throw new Error(
          "Could not create project."
        );
      }


      const savedProject =
        await response.json();


      /*
       * Adds the project returned by the
       * backend to the dashboard.
       */
      const project = {

        id: savedProject.projectId,

        name: savedProject.projectName,

        type: "Residential Project",

        status: savedProject.status,

        progress: savedProject.progress,

        budget: savedProject.budget,
      };


      setProjects((currentProjects) => [
        ...currentProjects,
        project,
      ]);


//    Clears and closes the form.
      setNewProject({
        name: "",
        budget: "",
      });

      setShowProjectForm(false);

    } catch (error) {

      console.error(
        "Error creating project:",
        error
      );
    }
  };
  
// Updates the worker form when the user types.
  const handleWorkerChange = (event) => {

    const { name, value } = event.target;

    setNewWorker({
      ...newWorker,
      [name]: value,
    });
  };


  /*
   * Sends a new worker to the Java backend.
   * The backend saves the worker in SQLite
   * and assigns them to the selected project.
   */
  const handleCreateWorker = async (event) => {

    event.preventDefault();

    if (
      newWorker.workerName.trim() === "" ||
      newWorker.trade === "" ||
      newWorker.projectId === ""
    ) {
      return;
    }

    try {

      const response = await fetch(
        "http://localhost:7070/api/workers",
        {
          method: "POST",

          headers: {
            "Content-Type": "application/json",
          },

          body: JSON.stringify(newWorker),
        }
      );

      if (!response.ok) {
        throw new Error(
          "Could not create worker."
        );
      }

      const savedWorker =
        await response.json();

      /*
       * Adds the worker returned by the
       * backend to the Workforce page.
       */
      setWorkers((currentWorkers) => [
        ...currentWorkers,
        savedWorker,
      ]);

      // Clears and closes the form.
      setNewWorker({
        workerName: "",
        trade: "",
        projectId: "",
      });

      setShowWorkerForm(false);

    } catch (error) {

      console.error(
        "Error creating worker:",
        error
      );
    }
  };


  /*
   * Dashboard totals are calculated from
   * the current project information.
   */
  const totalBudget =
    projects.reduce(
      (total, project) =>
        total + project.budget,
      0
    );


  /*
   * Total workers is calculated using
   * the workers stored in SQLite.
   */
  const totalWorkers =
    workers.length;


  /*
   * Finds the project assigned to a worker.
   * The project ID is used to display the
   * project name on the Workforce page.
   */
  const getProjectName = (projectId) => {

    const project = projects.find(
      (project) =>
        project.id === projectId
    );

    return project
      ? project.name
      : "Unknown Project";
  };


  return (
    <div className="app">

      {/* Left navigation menu */}
      <aside className="sidebar">

        <div className="logo">
          <h2>ANDRACORP</h2>
          <p>Construction Management</p>
        </div>

        <nav>

          <button
            className={
              activePage === "Dashboard"
                ? "nav-button active"
                : "nav-button"
            }
            onClick={() =>
              setActivePage("Dashboard")
            }
          >
            Dashboard
          </button>

          <button className="nav-button">
            Projects
          </button>

          <button
            className={
              activePage === "Workforce"
                ? "nav-button active"
                : "nav-button"
            }
            onClick={() =>
              setActivePage("Workforce")
            }
          >
            Workforce
          </button>

          <button className="nav-button">
            Expenses & Budget
          </button>

          <button className="nav-button">
            Change Orders
          </button>

          <button className="nav-button">
            Materials & Equipment
          </button>

          <button className="nav-button">
            Daily Reports
          </button>

        </nav>

      </aside>


      {/* Main contractor content */}
      <main className="main-content">


        {/* Contractor Dashboard */}
        {activePage === "Dashboard" && (

          <>

            <header className="dashboard-header">

              <div>
                <h1>Contractor Dashboard</h1>

                <p>
                  Manage your construction projects
                  from one place.
                </p>
              </div>

              <button
                className="new-project-button"
                onClick={() =>
                  setShowProjectForm(true)
                }
              >
                + New Project
              </button>

            </header>


            {/* Dashboard summary */}
            <section className="summary-grid">

              <div className="summary-card">
                <p>Active Projects</p>
                <h2>{projects.length}</h2>
              </div>

              <div className="summary-card">
                <p>Total Workers</p>
                <h2>{totalWorkers}</h2>
              </div>

              <div className="summary-card">
                <p>Pending Changes</p>
                <h2>2</h2>
              </div>

              <div className="summary-card">
                <p>Total Budget</p>

                <h2>
                  ${totalBudget.toLocaleString()}
                </h2>
              </div>

            </section>


            {/* Current projects */}
            <section className="projects-section">

              <div className="section-header">

                <h2>Current Projects</h2>

                <button className="view-button">
                  View All
                </button>

              </div>


              {projects.map((project) => (

                <div
                  className="project-card"
                  key={project.id}
                >

                  <div>
                    <h3>{project.name}</h3>
                    <p>{project.type}</p>
                  </div>


                  <div className="project-details">

                    <div>
                      <span>Status</span>

                      <strong>
                        {project.status}
                      </strong>
                    </div>

                    <div>
                      <span>Progress</span>

                      <strong>
                        {project.progress}%
                      </strong>
                    </div>

                    <div>
                      <span>Workers</span>

                      <strong>
                        {
                          workers.filter(
                            (worker) =>
                              worker.projectId ===
                              project.id
                          ).length
                        }
                      </strong>
                    </div>

                    <div>
                      <span>Budget</span>

                      <strong>
                        $
                        {project.budget
                          .toLocaleString()}
                      </strong>
                    </div>

                  </div>


                  <div className="progress-bar">

                    <div
                      className="progress"
                      style={{
                        width:
                          `${project.progress}%`,
                      }}
                    ></div>

                  </div>

                </div>

              ))}

            </section>

          </>

        )}


        {/* Workforce page */}
        {activePage === "Workforce" && (

          <>

            <header className="dashboard-header">

              <div>
                <h1>Workforce</h1>

                <p>
                  Manage workers and project
                  assignments.
                </p>
              </div>

			  <button
			    className="new-project-button"
			    onClick={() =>
			      setShowWorkerForm(true)
			    }
			  >
			    + Add Worker
			  </button>

            </header>


            {/*
             * Displays all workers returned by
             * the Java backend and SQLite database.
             */}
            <section className="projects-section">

              <div className="section-header">

                <h2>Current Workers</h2>

                <span>
                  {totalWorkers} Workers
                </span>

              </div>


              {workers.length === 0 ? (

                <p>
                  No workers have been added yet.
                </p>

              ) : (

                workers.map((worker) => (

                  <div
                    className="project-card"
                    key={worker.workerId}
                  >

                    <div>
                      <h3>
                        {worker.workerName}
                      </h3>

                      <p>
                        {worker.trade}
                      </p>
                    </div>


                    <div className="project-details">

                      <div>
                        <span>Project</span>

                        <strong>
                          {getProjectName(
                            worker.projectId
                          )}
                        </strong>
                      </div>

                      <div>
                        <span>Trade</span>

                        <strong>
                          {worker.trade}
                        </strong>
                      </div>

                    </div>

                  </div>

                ))

              )}

            </section>

          </>

        )}

      </main>


      {/* New Project form */}
      {showProjectForm && (

        <div className="modal-background">

          <div className="project-modal">

            <div className="modal-header">

              <div>
                <h2>Create New Project</h2>

                <p>
                  Add a construction project
                  to AndraCorp.
                </p>
              </div>

              <button
                className="close-button"
                onClick={() =>
                  setShowProjectForm(false)
                }
              >
                ×
              </button>

            </div>


            <form
              onSubmit={handleCreateProject}
            >

              <label>
                Project Name
              </label>

              <input
                type="text"
                name="name"
                value={newProject.name}
                onChange={handleProjectChange}
                placeholder="Example: Kitchen Renovation"
                required
              />


              <label>
                Project Budget
              </label>

              <input
                type="number"
                name="budget"
                value={newProject.budget}
                onChange={handleProjectChange}
                placeholder="Example: 35000"
                min="0"
              />


              <div className="form-buttons">

                <button
                  type="button"
                  className="cancel-button"
                  onClick={() =>
                    setShowProjectForm(false)
                  }
                >
                  Cancel
                </button>

                <button
                  type="submit"
                  className="create-button"
                >
                  Create Project
                </button>

              </div>

            </form>

          </div>

        </div>

      )}
	  
	  {/* Add Worker form */}
	  {showWorkerForm && (

	    <div className="modal-background">

	      <div className="project-modal">

	        <div className="modal-header">

	          <div>
	            <h2>Add New Worker</h2>

	            <p>
	              Add a worker and assign them
	              to a construction project.
	            </p>
	          </div>

	          <button
	            className="close-button"
	            onClick={() =>
	              setShowWorkerForm(false)
	            }
	          >
	            ×
	          </button>

	        </div>


	        <form onSubmit={handleCreateWorker}>

	          <label>
	            Worker Name
	          </label>

	          <input
	            type="text"
	            name="workerName"
	            value={newWorker.workerName}
	            onChange={handleWorkerChange}
	            placeholder="Example: John Smith"
	            required
	          />


	          <label>
	            Trade
	          </label>

	          <select
	            name="trade"
	            value={newWorker.trade}
	            onChange={handleWorkerChange}
	            required
	          >
	            <option value="">
	              Select Trade
	            </option>

	            <option value="Carpentry">
	              Carpentry
	            </option>

	            <option value="Electrical">
	              Electrical
	            </option>

	            <option value="Plumbing">
	              Plumbing
	            </option>

	            <option value="Painting">
	              Painting
	            </option>

	            <option value="Flooring">
	              Flooring
	            </option>

	            <option value="Sheetrock">
	              Sheetrock
	            </option>

	            <option value="Other">
	              Other
	            </option>
	          </select>


	          <label>
	            Assign to Project
	          </label>

	          <select
	            name="projectId"
	            value={newWorker.projectId}
	            onChange={handleWorkerChange}
	            required
	          >
	            <option value="">
	              Select Project
	            </option>

	            {projects.map((project) => (

	              <option
	                key={project.id}
	                value={project.id}
	              >
	                {project.name}
	              </option>

	            ))}

	          </select>


	          <div className="form-buttons">

	            <button
	              type="button"
	              className="cancel-button"
	              onClick={() =>
	                setShowWorkerForm(false)
	              }
	            >
	              Cancel
	            </button>

	            <button
	              type="submit"
	              className="create-button"
	            >
	              Add Worker
	            </button>

	          </div>

	        </form>

	      </div>

	    </div>

	  )}

    </div>
  );
}


export default App;
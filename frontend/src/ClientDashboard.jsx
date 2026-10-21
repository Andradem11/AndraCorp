import { useEffect, useState } from "react";
import {
  FolderOpen,
  Wallet,
  FileText,
  ClipboardList,
  LogOut,
  Search,
  ArrowLeft,
  CalendarDays,
  HardHat,
  Menu,
  X,
  CircleDollarSign
} from "lucide-react";
import "./ClientDashboard.css";

const money = (value) => `$${Number(value || 0).toLocaleString()}`;
const progress = (value) => Math.max(0, Math.min(100, Number(value) || 0));
const dateLabel = (value) => {
  if (!value) return "Not set";
  const date = new Date(`${String(value).slice(0, 10)}T12:00:00`);
  return Number.isNaN(date.getTime())
    ? String(value)
    : date.toLocaleDateString("en-US", {
        month: "short",
        day: "numeric",
        year: "numeric"
      });
};

// Sidebar navigation
const navigation = [
  ["My Projects", FolderOpen],
  ["Budget & Expenses", Wallet],
  ["Change Orders", FileText],
  ["Daily Reports", ClipboardList]
];

export default function ClientDashboard({
  client,
  projects,
  projectsLoading,
  projectsError,
  selectedProject,
  projectWorkers,
  workersLoading,
  workersError,
  projectExpenses,
  expensesLoading,
  expensesError,
  onSelectProject,
  onBack,
  onSignOut
}) {
  const [section, setSection] = useState("My Projects");
  const [search, setSearch] = useState("");
  const [mobileOpen, setMobileOpen] = useState(false);
  const [projectExpensesById, setProjectExpensesById] = useState({});
  const [totalsError, setTotalsError] = useState(false);
  const [totalsLoading, setTotalsLoading] = useState(false);
  const [workersByProject, setWorkersByProject] = useState({});
  const [allWorkersLoading, setAllWorkersLoading] = useState(false);
  const [allWorkersError, setAllWorkersError] = useState(false);

  // Load expense records for each assigned project
  useEffect(() => {
    let active = true;
    if (!projects.length) {
      setProjectExpensesById({});
      setTotalsLoading(false);
      return;
    }
    setTotalsError(false);
    setTotalsLoading(true);
    Promise.all(
      projects.map(async (project) => {
        const response = await fetch(
          `http://localhost:7070/api/projects/${encodeURIComponent(project.projectId)}/expenses`
        );
        if (!response.ok) {
          throw new Error("Expense request failed");
        }
        const data = await response.json();
        if (!Array.isArray(data)) {
          throw new Error("Invalid expenses");
        }
        return [project.projectId, data];
      })
    )
      .then((entries) => {
        if (active) {
          setProjectExpensesById(Object.fromEntries(entries));
        }
      })
      .catch(() => {
        if (active) {
          setTotalsError(true);
        }
      })
      .finally(() => {
        if (active) {
          setTotalsLoading(false);
        }
      });
    return () => {
      active = false;
    };
  }, [projects]);

  // Load assigned workers for the Budget & Expenses section
  useEffect(() => {
    let active = true;
    if (projects.length === 0) {
      setWorkersByProject({});
      setAllWorkersLoading(false);
      setAllWorkersError(false);
      return;
    }
    const loadWorkers = async () => {
      setAllWorkersLoading(true);
      setAllWorkersError(false);
      setWorkersByProject({});
      try {
        const response = await fetch("http://localhost:7070/api/workers");
        if (!response.ok) {
          throw new Error("Could not load workers");
        }
        const workers = await response.json();
        if (!Array.isArray(workers)) {
          throw new Error("Invalid workers response");
        }
        if (active) {
          const groupedWorkers = {};
          projects.forEach((project) => {
            groupedWorkers[project.projectId] = workers.filter(
              (worker) => worker.projectId === project.projectId
            );
          });
          setWorkersByProject(groupedWorkers);
        }
      } catch (error) {
        console.error("Error loading project workforce:", error);
        if (active) {
          setAllWorkersError(true);
        }
      } finally {
        if (active) {
          setAllWorkersLoading(false);
        }
      }
    };
    loadWorkers();
    return () => {
      active = false;
    };
  }, [projects]);

  // Navigation and project calculations
  const openSection = (nextSection) => {
    setSection(nextSection);
    setMobileOpen(false);
    onBack();
  };
  const matchingProjects = projects.filter((project) =>
    String(project.projectName || "")
      .toLowerCase()
      .includes(search.toLowerCase())
  );
  const projectTotal = (projectId) => {
    return (projectExpensesById[projectId] || []).reduce(
      (sum, expense) => sum + Number(expense.amount || 0),
      0
    );
  };
  const selectedProjectTotal = projectExpenses.reduce(
    (sum, expense) => sum + Number(expense.amount || 0),
    0
  );
  const showDetail = section === "My Projects" && selectedProject;

  // Project cards
  const projectCards = (projectList) => {
    return projectList.map((project) => (
      <article className="cd-project" key={project.projectId}>
        <div className="cd-project-icon">
          <HardHat size={35} />
        </div>
        <div className="cd-project-body">
          <div className="cd-project-title">
            <h3>{project.projectName}</h3>
            <span className="cd-status">{project.status || "UNKNOWN"}</span>
          </div>
          <div className="cd-project-facts">
            <span>
              <CalendarDays size={16} />
              {dateLabel(project.estimatedCompletionDate)}
            </span>
            <span>
              <CircleDollarSign size={16} />
              {money(project.budget)}
            </span>
          </div>
          <div className="cd-progress-label">
            <span>Project Progress</span>
            <strong>{progress(project.progress)}%</strong>
          </div>
          <div className="cd-track">
            <div style={{ width: `${progress(project.progress)}%` }} />
          </div>
        </div>
        <button
          className="cd-outline"
          onClick={() => {
            setSection("My Projects");
            onSelectProject(project);
          }}
        >
          View Project Details
        </button>
      </article>
    ));
  };

  return (
    <div className="cd-shell">
      {/* Sidebar */}
      <aside className={`cd-sidebar ${mobileOpen ? "cd-sidebar-open" : ""}`}>
        <div className="cd-brand">
          <img
            src="/images/andracorp-watermark.png"
            alt="AndraCorp Logo"
            className="cd-brand-logo"
          />
          <strong>
            ANDRA<span>CORP</span>
          </strong>
          <small>Client Portal</small>
        </div>
        <nav aria-label="Client navigation">
          {navigation.map(([label, Icon]) => (
            <button
              key={label}
              className={section === label ? "active" : ""}
              onClick={() => openSection(label)}
            >
              <Icon size={21} />
              {label}
            </button>
          ))}
        </nav>
        <button className="cd-signout" onClick={onSignOut}>
          <LogOut size={20} />
          Sign Out
        </button>
      </aside>
      {mobileOpen && (
        <button
          className="cd-overlay"
          aria-label="Close menu"
          onClick={() => setMobileOpen(false)}
        />
      )}
      {/* Main dashboard */}
      <div className="cd-main">
        <header className="cd-topbar">
          <button
            className="cd-menu"
            onClick={() => setMobileOpen(!mobileOpen)}
            aria-label="Toggle menu"
          >
            {mobileOpen ? <X /> : <Menu />}
          </button>
          <div className="cd-search">
            <Search size={18} />
            <input
              aria-label="Search projects"
              placeholder="Search projects..."
              value={search}
              onChange={(event) => {
                setSearch(event.target.value);
                if (event.target.value) {
                  setSection("My Projects");
                  onBack();
                }
              }}
            />
          </div>
          <div className="cd-profile">
            <span className="cd-avatar">
              {String(client?.name || "C").charAt(0).toUpperCase()}
            </span>
            <div>
              <strong>{client?.name}</strong>
              <small>Client</small>
            </div>
          </div>
        </header>
        <main className="cd-content">
          {projectsLoading && <p>Loading your projects...</p>}
          {projectsError && (
            <p role="alert" className="cd-error">{projectsError}</p>
          )}
          {!projectsLoading && !projectsError && (
            <>
              {/* My Projects */}
              {section === "My Projects" && (
                <>
                  {showDetail ? (
                    <>
                      <button className="cd-back" onClick={onBack}>
                        <ArrowLeft size={18} />
                        Back to My Projects
                      </button>
                      <h1>{selectedProject.projectName}</h1>
                      <div className="cd-panel">
                        <h3>Project Overview</h3>
                        <p>Status: {selectedProject.status}</p>
                        <p>Budget: {money(selectedProject.budget)}</p>
                        <p>
                          Estimated Completion: {dateLabel(selectedProject.estimatedCompletionDate)}
                        </p>
                        <div className="cd-progress-label">
                          <span>Progress</span>
                          <strong>{progress(selectedProject.progress)}%</strong>
                        </div>
                        <div className="cd-track">
                          <div style={{ width: `${progress(selectedProject.progress)}%` }} />
                        </div>
                      </div>
                      <div className="cd-bottom">
                        <section className="cd-panel">
                          <h3>Workforce</h3>
                          {workersLoading ? (
                            <p>Loading workers...</p>
                          ) : workersError ? (
                            <p role="alert">{workersError}</p>
                          ) : projectWorkers.length > 0 ? (
                            projectWorkers.map((worker) => (
                              <div className="cd-record" key={worker.workerId}>
                                <strong>{worker.workerName}</strong>
                                <p>Trade: {worker.trade || "Not specified"}</p>
                                <p>Area: {worker.area || "Not assigned"}</p>
                                <p>Task: {worker.taskId || "Not assigned"}</p>
                              </div>
                            ))
                          ) : (
                            <p>No workers assigned.</p>
                          )}
                        </section>
                        <section className="cd-panel">
                          <h3>Expenses & Budget</h3>
                          {expensesLoading ? (
                            <p>Loading expenses...</p>
                          ) : expensesError ? (
                            <p role="alert">{expensesError}</p>
                          ) : (
                            <>
                              <p>Project Budget: {money(selectedProject.budget)}</p>
                              <p>Total Expenses: {money(selectedProjectTotal)}</p>
                              <p>
                                Remaining Budget: {money(Number(selectedProject.budget || 0) - selectedProjectTotal)}
                              </p>
                              <h4>Expense Records</h4>
                              {projectExpenses.length > 0 ? (
                                projectExpenses.map((expense) => (
                                  <div className="cd-record" key={expense.expenseId}>
                                    <strong>{expense.description}</strong>
                                    <p>{money(expense.amount)}</p>
                                  </div>
                                ))
                              ) : (
                                <p>No expenses recorded.</p>
                              )}
                            </>
                          )}
                        </section>
                      </div>
                    </>
                  ) : (
                    <>
                      <h1>My Projects</h1>
                      {matchingProjects.length > 0 ? (
                        projectCards(matchingProjects)
                      ) : (
                        <div className="cd-panel">No matching projects.</div>
                      )}
                    </>
                  )}
                </>
              )}
              {/* Budget & Expenses */}
              {section === "Budget & Expenses" && (
                <>
                  <h1>Budget & Expenses</h1>
                  {projects.length === 0 && (
                    <div className="cd-panel">No projects assigned yet.</div>
                  )}
                  {projects.map((project) => (
                    <section className="cd-panel" key={project.projectId}>
                      <h3>{project.projectName}</h3>
                      <p>
                        <strong>Project Budget:</strong> {money(project.budget)}
                      </p>
                      {totalsLoading ? (
                        <p>Loading expenses...</p>
                      ) : totalsError || !Object.hasOwn(projectExpensesById, project.projectId) ? (
                        <p role="alert" className="cd-error">
                          Expense records are currently unavailable.
                        </p>
                      ) : (
                        <>
                          <p>
                            <strong>Total Expenses:</strong> {money(projectTotal(project.projectId))}
                          </p>
                          <p>
                            <strong>Remaining Budget:</strong> {money(Number(project.budget || 0) - projectTotal(project.projectId))}
                          </p>
                          <h4>Expense Records</h4>
                          {projectExpensesById[project.projectId].length > 0 ? (
                            projectExpensesById[project.projectId].map((expense, index) => (
                              <div className="cd-record" key={expense.expenseId || index}>
                                <strong>{expense.description || "Expense"}</strong>
                                <p>{money(expense.amount)}</p>
                              </div>
                            ))
                          ) : (
                            <p>No expenses recorded.</p>
                          )}
                        </>
                      )}
                      <div className="cd-workforce-section">
                        <h4><HardHat size={20} /> Project Workforce</h4>
                        {allWorkersLoading ? (
                          <p>Loading workers...</p>
                        ) : allWorkersError ? (
                          <p role="alert" className="cd-error">Workforce information is currently unavailable.</p>
                        ) : (workersByProject[project.projectId] || []).length > 0 ? (
                          workersByProject[project.projectId].map((worker) => (
                            <div className="cd-worker-card" key={worker.workerId}>
                              <strong>{worker.workerName}</strong>
                              <p>Trade: {worker.trade || "Not specified"}</p>
                              <p>Assigned Area: {worker.area || "Not assigned"}</p>
                              <p>Assigned Task: {worker.taskId || "Not assigned"}</p>
                            </div>
                          ))
                        ) : (
                          <p>No workers assigned to this project.</p>
                        )}
                      </div>
                    </section>
                  ))}
                </>
              )}
              {/* Sections not implemented yet */}
              {["Change Orders", "Daily Reports"].includes(section) && (
                <>
                  <h1>{section}</h1>
                  <div className="cd-panel">
                    <h3>Coming soon</h3>
                  </div>
                </>
              )}
            </>
          )}
        </main>
      </div>
    </div>
  );
}

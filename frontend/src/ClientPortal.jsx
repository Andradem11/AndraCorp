import { useState } from "react";
import { ArrowLeft, ArrowRight, HardHat, Phone, User, UserRound } from "lucide-react";
import ClientDashboard from "./ClientDashboard";
import "./ClientPortal.css";

function ClientPortal({ onContractor }) {
  const [page, setPage] = useState("select");
  const [name, setName] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [client, setClient] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  // Projects assigned to the logged-in client.
  const [projects, setProjects] = useState([]);
  const [projectsLoading, setProjectsLoading] = useState(false);
  const [projectsError, setProjectsError] = useState("");

  // Workforce information for the selected project.
  const [projectWorkers, setProjectWorkers] = useState([]);
  const [workersLoading, setWorkersLoading] = useState(false);
  const [workersError, setWorkersError] = useState("");

  const [projectExpenses, setProjectExpenses] = useState([]);
  const [expensesLoading, setExpensesLoading] = useState(false);
  const [expensesError, setExpensesError] = useState("");

  const [selectedProject, setSelectedProject] = useState(null);

  const loadClientProjects = async (clientId) => {
    setProjectsLoading(true);
    setProjectsError("");
    try {
      const response = await fetch(
        `http://localhost:7070/api/client/projects?clientId=${encodeURIComponent(clientId)}`
      );
      if (!response.ok) {
        throw new Error("Could not load your projects.");
      }
      const data = await response.json();
      setProjects(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error("Error loading client projects:", error);
      setProjectsError("Could not load your projects. Please try again.");
    } finally {
      setProjectsLoading(false);
    }
  };

  const loadProjectWorkers = async (projectId) => {
    setWorkersLoading(true);
    setWorkersError("");
    setProjectWorkers([]);
    try {
      const response = await fetch(
        "http://localhost:7070/api/workers"
      );
      if (!response.ok) {
        throw new Error("Could not load project workers.");
      }
      const data = await response.json();
      // Only display workers assigned to this project.
      const assignedWorkers = data.filter(
        (worker) => worker.projectId === projectId
      );
      setProjectWorkers(assignedWorkers);
    } catch (error) {
      console.error("Error loading project workers:", error);
      setWorkersError("Could not load workforce information.");
    } finally {
      setWorkersLoading(false);
    }
  };

  const loadProjectExpenses = async (projectId) => {
    setExpensesLoading(true);
    setExpensesError("");
    setProjectExpenses([]);
    try {
      const response = await fetch(
        `http://localhost:7070/api/projects/${encodeURIComponent(projectId)}/expenses`
      );
      if (!response.ok) {
        throw new Error("Could not load project expenses.");
      }
      const data = await response.json();
      if (!Array.isArray(data)) {
        throw new Error("Unexpected expense response.");
      }
      setProjectExpenses(data);
    } catch (error) {
      console.error("Error loading project expenses:", error);
      setExpensesError("Could not load project expenses.");
    } finally {
      setExpensesLoading(false);
    }
  };

  const handleLogin = async (event) => {
    event.preventDefault();
    setError("");
    setLoading(true);
    try {
      const response = await fetch(
        "http://localhost:7070/api/client/login",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            name: name.trim(),
            phoneNumber: phoneNumber.trim(),
          }),
        }
      );
      const data = await response.json();
      if (!response.ok) {
        throw new Error(
          data.error || "Could not find your account."
        );
      }
      setClient(data.client);
      setPage("dashboard");
      await loadClientProjects(data.client.clientId);
    } catch (err) {
      setError(err.message || "Could not connect to the server.");
    } finally {
      setLoading(false);
    }
  };

  const signOut = () => {
    setClient(null);
    setName("");
    setPhoneNumber("");
    setProjects([]);
    setProjectsError("");
    setError("");
    setPage("select");
    setSelectedProject(null);
    setProjectWorkers([]);
    setWorkersError("");
    setProjectExpenses([]);
    setExpensesError("");
  };

  return (
    <div className={`client-portal ${page === "dashboard" ? "client-portal-dashboard" : "client-portal-auth"}`}>
      <div
        className={
          page === "dashboard"
            ? "client-login-card client-dashboard-card"
            : "client-login-card client-auth-card"
        }
      >
        {page !== "dashboard" && (
          <div className="client-brand client-auth-brand">
            <div className="client-gold-logo" aria-label="AndraCorp logo">
              <svg viewBox="0 0 110 104" role="img" aria-hidden="true">
                <path d="M55 5 105 93H84L55 42 26 93H5Z" fill="currentColor" />
                <path d="M55 58 78 97H33L44 78H58L55 73 43 94H68Z" fill="currentColor" />
              </svg>
            </div>
            <h1 className="client-auth-wordmark"><span>ANDRA</span><span>CORP</span></h1>
            <p>Construction Project Management</p>
          </div>
        )}
        {page === "select" && (
          <>
            <div className="client-auth-divider" aria-hidden="true" />
            <div className="client-auth-heading">
              <h2>Welcome to AndraCorp</h2>
            </div>
            <div className="client-role-options">
              <button type="button" className="client-role-contractor" onClick={onContractor}>
                <HardHat size={46} strokeWidth={1.8} />
                <strong>Contractor</strong>
                <span>Manage construction<br />projects</span>
                <span className="client-role-arrow" aria-hidden="true"><ArrowRight size={23} /></span>
              </button>
              <button type="button" className="client-role-client" onClick={() => setPage("login")}>
                <UserRound size={46} strokeWidth={1.8} />
                <strong>Client</strong>
                <span>View your project<br />information</span>
                <span className="client-role-arrow" aria-hidden="true"><ArrowRight size={23} /></span>
              </button>
            </div>
          </>
        )}
        {page === "login" && (
          <>
            <div className="client-auth-divider" aria-hidden="true" />
            <button
              type="button"
              className="client-back"
              onClick={() => {
                setError("");
                setPage("select");
              }}
            >
              <ArrowLeft size={20} />
              Back
            </button>
            <div className="client-auth-heading">
              <h2>Client Sign In</h2>
              <p>Access your project information.</p>
            </div>
            <form onSubmit={handleLogin}>
              <label className="client-field-label" htmlFor="client-name">Full Name</label>
              <div className="client-input-wrap">
                <User size={23} aria-hidden="true" />
                <input
                id="client-name"
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Full Name"
                autoComplete="name"
                required
              />
              </div>
              <label className="client-field-label" htmlFor="client-phone">Phone Number</label>
              <div className="client-input-wrap">
                <Phone size={23} aria-hidden="true" />
                <input
                id="client-phone"
                type="tel"
                value={phoneNumber}
                onChange={(e) => setPhoneNumber(e.target.value)}
                placeholder="Phone Number"
                autoComplete="tel"
                required
              />
              </div>
              {error && (
                <p className="client-login-error" role="alert">
                  {error}
                </p>
              )}
              <button
                className="client-submit"
                type="submit"
                disabled={loading}
              >
                <span>{loading ? "Checking..." : "Sign In"}</span>
                <ArrowRight size={24} aria-hidden="true" />
              </button>
            </form>
            <p className="client-auth-help">Need help? Contact your contractor.</p>
          </>
        )}
        {page === "dashboard" && client && (
          <ClientDashboard
            client={client}
            projects={projects}
            projectsLoading={projectsLoading}
            projectsError={projectsError}
            selectedProject={selectedProject}
            projectWorkers={projectWorkers}
            workersLoading={workersLoading}
            workersError={workersError}
            projectExpenses={projectExpenses}
            expensesLoading={expensesLoading}
            expensesError={expensesError}
            onSelectProject={(project) => {
              setSelectedProject(project);
              loadProjectWorkers(project.projectId);
              loadProjectExpenses(project.projectId);
            }}
            onBack={() => setSelectedProject(null)}
            onSignOut={signOut}
          />
        )}
      </div>
    </div>
  );
}

export default ClientPortal;

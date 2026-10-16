import { useEffect, useState } from "react";
import "./App.css";
import ClientPortal from "./ClientPortal";
import {
  LayoutDashboard,
  FolderKanban,
  Users,
  HardHat,
  CircleDollarSign,
  FileText,
  Package,
  CalendarDays,
  FolderOpen,
  ChartNoAxesColumnIncreasing,
  FileClock,
  TrendingUp,
  Search,
  Bell,
  ChevronDown,
  Mail, Phone, Pencil, Trash2, UserRound, BriefcaseBusiness, Link2, UserRoundPlus, Eye,
  Clock3, UserCheck, UserX, RotateCcw, ClipboardList,
} from "lucide-react";
function ContractorApp() {

  /*
   * Projects are loaded from the Java backend.
   * The Java backend retrieves the project
   * information from the SQLite database.
   */
  const [projects, setProjects] = useState([]);
  const [projectFilter, setProjectFilter] = useState("ALL");
  const [clientSearch, setClientSearch] = useState("");
  const [clientFilter, setClientFilter] = useState("ALL");
  const [expandedClientId, setExpandedClientId] = useState(null);
  const [dashboardExpenses, setDashboardExpenses] = useState(0);
  const [dashboardCategories, setDashboardCategories] = useState([]);
  const [dashboardBudgetLoading, setDashboardBudgetLoading] = useState(false);
  const [dashboardBudgetError, setDashboardBudgetError] = useState("");

  /*
   * Workers are loaded from the Java backend.
   * The backend retrieves the worker
   * information from the SQLite database.
   */
  const [workers, setWorkers] = useState([]);
  const [workDate, setWorkDate] = useState("");
  const [inactiveWorkers, setInactiveWorkers] = useState([]);
  const [workerActionBusy, setWorkerActionBusy] = useState(false);
  const [workerActionError, setWorkerActionError] = useState("");
  const [workerSearch, setWorkerSearch] = useState("");
  const [workerTradeFilter, setWorkerTradeFilter] = useState("ALL");
  const [workerProjectFilter, setWorkerProjectFilter] = useState("ALL");
  const [workerStatusFilter, setWorkerStatusFilter] = useState("ALL");

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

  // Assign or reassign a worker to a project area and task.
  const [showAssignmentForm, setShowAssignmentForm] = useState(false);
  const [selectedWorker, setSelectedWorker] = useState(null);
  const [assignment, setAssignment] = useState({ area: "", task: "" });

 // Record hours for a worker's assigned task.
 const [showHoursForm, setShowHoursForm] = useState(false);
 const [hoursWorker, setHoursWorker] = useState(null);
 const [hoursWorked, setHoursWorked] = useState("");
 const [savingHours, setSavingHours] = useState(false);

 // Work history is loaded from SQLite through the Java backend.
 const [workHoursByWorker, setWorkHoursByWorker] = useState({});
 const [hoursError, setHoursError] = useState("");
 const [historyWorkerId, setHistoryWorkerId] = useState(null);
 const loadWorkerHours = async (workerId) => {
   try {
     const response = await fetch(`http://localhost:7070/api/workers/${workerId}/hours`);
     if (!response.ok) throw new Error("Could not load work hours.");
     const entries = await response.json();
     setWorkHoursByWorker((current) => ({ ...current, [workerId]: entries }));
     setHoursError("");
   } catch (error) {
     console.error("Error loading work hours:", error);
     setHoursError("Could not load work hours. Check that the Java backend is running.");
   }
 };
 useEffect(() => {
   workers.forEach((worker) => {
     if (!(worker.workerId in workHoursByWorker)) {
       loadWorkerHours(worker.workerId);
     }
   });

 // Only fetch when the list of workers changes; successful posts refresh explicitly.
 // eslint-disable-next-line react-hooks/exhaustive-deps
 }, [workers]);

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
           clientId: project.clientId,
           estimatedCompletionDate: project.estimatedCompletionDate || "",
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

  // Loads inactive workers separately from the active workforce.
  const loadInactiveWorkers = async () => {
    const response = await fetch("http://localhost:7070/api/workers/inactive");
    if (!response.ok) throw new Error("Could not load inactive workers.");
    setInactiveWorkers(await response.json());
  };
  useEffect(() => {
    loadInactiveWorkers().catch((error) => {
      console.error("Error loading inactive workers:", error);
      setWorkerActionError("Could not load inactive workers. Check the backend.");
    });
  }, []);
  const refreshWorkforce = async () => {
    const [activeResponse, inactiveResponse] = await Promise.all([
      fetch("http://localhost:7070/api/workers"),
      fetch("http://localhost:7070/api/workers/inactive")
    ]);
    if (!activeResponse.ok || !inactiveResponse.ok) {
      throw new Error("Could not refresh workforce.");
    }
    const [active, inactive] = await Promise.all([
      activeResponse.json(), inactiveResponse.json()
    ]);
    setWorkers(active);
    setInactiveWorkers(inactive);
  };
  const handleReactivateWorker = async (worker) => {
    if (workerActionBusy) return;
    setWorkerActionBusy(true);
    setWorkerActionError("");
    try {
      const response = await fetch(
        `http://localhost:7070/api/workers/${worker.workerId}/reactivate`,
        { method: "PUT" }
      );
      if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.error || "Could not reactivate worker.");
      }
      await refreshWorkforce();
    } catch (error) {
      console.error("Error reactivating worker:", error);
      setWorkerActionError(error.message);
    } finally {
      setWorkerActionBusy(false);
    }
  };

 // Client records are loaded from the Java backend.
 const [clients, setClients] = useState([]);
 const [clientLoading, setClientLoading] = useState(false);
 const [clientSaving, setClientSaving] = useState(false);
 const [clientError, setClientError] = useState("");
 const [showClientForm, setShowClientForm] = useState(false);
 const [editingClientId, setEditingClientId] = useState(null);
 const [clientForm, setClientForm] = useState({ name: "", email: "", phoneNumber: "" });
 const [assigningProjectId, setAssigningProjectId] = useState(null);
 const [selectedClientId, setSelectedClientId] = useState("");
 const [assignmentSaving, setAssignmentSaving] = useState(false);
 const [assignmentError, setAssignmentError] = useState("");
 const loadClients = async () => {
   setClientLoading(true);
   try {
     const response = await fetch("http://localhost:7070/api/clients");
     if (!response.ok) throw new Error("Could not load clients.");
     setClients(await response.json());
     setClientError("");
   } catch (error) {
     console.error("Error loading clients:", error);
     setClientError("Could not load clients. Check that the Java backend is running.");
   } finally {
     setClientLoading(false);
   }
 };
 useEffect(() => { loadClients(); }, []);
 const openClientForm = (client = null) => {
   setEditingClientId(client?.clientId || null);
   setClientForm(client ? {
     name: client.name || "", email: client.email || "",
     phoneNumber: client.phoneNumber || ""
   } : { name: "", email: "", phoneNumber: "" });
   setClientError("");
   setShowClientForm(true);
 };
 const handleSaveClient = async (event) => {
   event.preventDefault();
   if (clientSaving) return;
   setClientSaving(true);
   setClientError("");
   try {
     const url = editingClientId
       ? `http://localhost:7070/api/clients/${editingClientId}`
       : "http://localhost:7070/api/clients";
     const response = await fetch(url, {
       method: editingClientId ? "PUT" : "POST",
       headers: { "Content-Type": "application/json" },
       body: JSON.stringify({
         name: clientForm.name.trim(), email: clientForm.email.trim(),
         phoneNumber: clientForm.phoneNumber.trim()
       }),
     });
     const data = await response.json();
     if (!response.ok) throw new Error(data.error || "Could not save client.");
     setShowClientForm(false);
     setEditingClientId(null);
     await loadClients();
   } catch (error) {
     setClientError(error.message);
   } finally {
     setClientSaving(false);
   }
 };
 const handleDeleteClient = async (client) => {
   if (!window.confirm(`Delete ${client.name}? Clients with project history cannot be deleted.`)) return;
   setClientError("");
   try {
     const response = await fetch(`http://localhost:7070/api/clients/${client.clientId}`, {
       method: "DELETE"
     });
     const data = await response.json();
     if (!response.ok) throw new Error(data.error || "Could not delete client.");
     await loadClients();
   } catch (error) {
     setClientError(error.message);
   }
 };
 const openClientAssignment = (project) => {
   setAssigningProjectId(project.id);
   setSelectedClientId(project.clientId || "");
   setAssignmentError("");
 };
 const handleAssignClient = async (event) => {
   event.preventDefault();
   if (!assigningProjectId || !selectedClientId || assignmentSaving) return;
   setAssignmentSaving(true);
   setAssignmentError("");
   try {
     const response = await fetch(`http://localhost:7070/api/projects/${assigningProjectId}/client`, {
       method: "PUT",
       headers: { "Content-Type": "application/json" },
       body: JSON.stringify({ clientId: selectedClientId }),
     });
     const data = await response.json();
     if (!response.ok) throw new Error(data.error || "Could not assign client.");
     setProjects((current) => current.map((project) =>
       project.id === assigningProjectId
         ? { ...project, clientId: selectedClientId } : project
     ));
     setAssigningProjectId(null);
   } catch (error) {
     setAssignmentError(error.message);
   } finally {
     setAssignmentSaving(false);
   }
 };
 const getClientName = (clientId) => {
   if (!clientId) return "Not assigned";
   return clients.find((client) => client.clientId === clientId)?.name || "Unknown client";
 };

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
       clientId: savedProject.clientId || null,
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
  const openAssignmentForm = (worker) => {
    setSelectedWorker(worker);
    setAssignment({
      area: worker.area || "",
      task: worker.taskId || "",
    });
    setShowAssignmentForm(true);
  };

  // Opens the Record Hours form for a worker with an assigned task.
  const openHoursForm = (worker) => {
    if (!worker.taskId) return;
    const today = new Date();
    const localDate = [
      today.getFullYear(),
      String(today.getMonth() + 1).padStart(2, "0"),
      String(today.getDate()).padStart(2, "0"),
    ].join("-");
    setHoursWorker(worker);
    setHoursWorked("");
    setWorkDate(localDate);
    setShowHoursForm(true);
  };
  const closeHoursForm = () => {
    setShowHoursForm(false);
    setHoursWorker(null);
    setHoursWorked("");
    setWorkDate("");
  };
 const handleRecordHours = async (event) => {
   event.preventDefault();
   if (!hoursWorker || savingHours) return;
   const hours = Number(hoursWorked);
   if (!Number.isFinite(hours) || hours <= 0) {
     alert("Enter a valid number of hours greater than zero.");
     return;
   }
   setSavingHours(true);
   try {
     const response = await fetch(
       `http://localhost:7070/api/workers/${hoursWorker.workerId}/hours`,
       {
         method: "POST",
         headers: { "Content-Type": "application/json" },
         body: JSON.stringify({ taskId: hoursWorker.taskId, hours,workDate }),
       }
     );
     if (!response.ok) {
       throw new Error("Could not record work hours.");
     }
     await loadWorkerHours(hoursWorker.workerId);
     closeHoursForm();
     alert("Work hours recorded successfully.");
   } catch (error) {
     console.error("Error recording work hours:", error);
     alert("Could not record work hours. Please try again.");
   } finally {
     setSavingHours(false);
   }
 };
 const handleSaveAssignment = async (event) => {
    event.preventDefault();
    if (!selectedWorker) return;
    try {
      const response = await fetch(
        `http://localhost:7070/api/workers/${selectedWorker.workerId}/assignment`,
        {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            projectId: selectedWorker.projectId,
            area: assignment.area.trim(),
            task: assignment.task.trim(),
          }),
        }
      );
      if (!response.ok) throw new Error("Could not save assignment.");
      const updatedWorker = await response.json();
      setWorkers((currentWorkers) =>
        currentWorkers.map((worker) =>
          worker.workerId === selectedWorker.workerId
            ? { ...worker, area: updatedWorker.area, taskId: updatedWorker.taskId }
            : worker
        )
      );
      setShowAssignmentForm(false);
      setSelectedWorker(null);
    } catch (error) {
      console.error("Error saving assignment:", error);
      alert("Could not save worker assignment.");
    }
  };

  /*
   * Sends a new worker to the Java backend.
   * The backend saves the worker in SQLite
   * and assigns them to the selected project.
   */
  // Deactivates a worker while keeping their stored work history.
  const handleDeleteWorker = async (worker) => {
    if (workerActionBusy) return;
    const confirmed = window.confirm(
      `Deactivate ${worker.workerName}? Their recorded work hours will be preserved.`
    );
    if (!confirmed) return;
    setWorkerActionBusy(true);
    setWorkerActionError("");
    try {
      const response = await fetch(
        `http://localhost:7070/api/workers/${worker.workerId}`,
        { method: "DELETE" }
      );
      if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.error || "Could not deactivate worker.");
      }
      await refreshWorkforce();
      setHistoryWorkerId((current) =>
        current === worker.workerId ? null : current
      );
    } catch (error) {
      console.error("Error deactivating worker:", error);
      setWorkerActionError(error.message);
    } finally {
      setWorkerActionBusy(false);
    }
  };
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
        total + (Number(project.budget) || 0),
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

  // Materials and equipment for the selected project.
  const [inventoryProjectId, setInventoryProjectId] = useState("");
  const [materials, setMaterials] = useState([]);
  const [equipment, setEquipment] = useState([]);
  const [inventoryLoading, setInventoryLoading] = useState(false);
  const [inventorySaving, setInventorySaving] = useState(false);
  const [inventoryError, setInventoryError] = useState("");
  const [materialName, setMaterialName] = useState("");
  const [materialQuantity, setMaterialQuantity] = useState("");
  const [equipmentName, setEquipmentName] = useState("");
  const [materialSearch, setMaterialSearch] = useState("");
  const [equipmentSearch, setEquipmentSearch] = useState("");
  useEffect(() => {
    if (!inventoryProjectId && projects.length > 0) {
      setInventoryProjectId(projects[0].id);
    }
  }, [projects, inventoryProjectId]);
  const loadInventory = async (projectId) => {
    if (!projectId) return;
    setInventoryLoading(true);
    setInventoryError("");
    try {
      const response = await fetch(`http://localhost:7070/api/projects/${projectId}/inventory`);
      if (!response.ok) throw new Error("Could not load inventory.");
      const data = await response.json();
      setMaterials(data.materials || []);
      setEquipment(data.equipment || []);
    } catch (error) {
      console.error("Error loading inventory:", error);
      setInventoryError("Could not load inventory. Check that the backend is running.");
    } finally {
      setInventoryLoading(false);
    }
  };
  useEffect(() => {
    if (activePage === "Materials & Equipment" && inventoryProjectId) {
      loadInventory(inventoryProjectId);
    }
  }, [activePage, inventoryProjectId]);
  const handleAddMaterial = async (event) => {
    event.preventDefault();
    const quantity = Number(materialQuantity);
    if (!inventoryProjectId || !materialName.trim() || !Number.isFinite(quantity) || quantity <= 0) {
      setInventoryError("Enter a material name and a quantity greater than zero.");
      return;
    }
    setInventorySaving(true);
    setInventoryError("");
    try {
      const response = await fetch("http://localhost:7070/api/materials", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ projectId: inventoryProjectId, materialName: materialName.trim(), quantity }),
      });
      if (!response.ok) throw new Error("Could not add material.");
      setMaterialName("");
      setMaterialQuantity("");
      await loadInventory(inventoryProjectId);
    } catch (error) {
      console.error(error);
      setInventoryError("Could not add material.");
    } finally {
      setInventorySaving(false);
    }
  };
  const handleUpdateMaterial = async (material) => {
    const value = window.prompt(`New quantity for ${material.materialName}:`, String(material.quantity));
    if (value === null) return;
    if (value.trim() === "" || !Number.isFinite(Number(value)) || Number(value) < 0) {
      alert("Enter a valid quantity of zero or greater.");
      return;
    }
    setInventorySaving(true);
    setInventoryError("");
    try {
      const response = await fetch(`http://localhost:7070/api/materials/${material.materialId}/quantity`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ quantity: Number(value) }),
      });
      if (!response.ok) throw new Error("Could not update quantity.");
      await loadInventory(inventoryProjectId);
    } catch (error) {
      console.error(error);
      setInventoryError("Could not update material quantity.");
    } finally {
      setInventorySaving(false);
    }
  };
  const handleAddEquipment = async (event) => {
    event.preventDefault();
    if (!inventoryProjectId || !equipmentName.trim()) return;
    setInventorySaving(true);
    setInventoryError("");
    try {
      const response = await fetch("http://localhost:7070/api/equipment", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ projectId: inventoryProjectId, equipmentName: equipmentName.trim() }),
      });
      if (!response.ok) throw new Error("Could not add equipment.");
      setEquipmentName("");
      await loadInventory(inventoryProjectId);
    } catch (error) {
      console.error(error);
      setInventoryError("Could not add equipment.");
    } finally {
      setInventorySaving(false);
    }
  };

  // Removes a material from SQLite and refreshes the project inventory.
  const handleRemoveMaterial = async (material) => {
    if (!window.confirm(`Permanently remove ${material.materialName} from this project?`)) return;
    setInventorySaving(true);
    setInventoryError("");
    try {
      const response = await fetch(`http://localhost:7070/api/materials/${material.materialId}`, {
        method: "DELETE",
      });
      if (!response.ok) throw new Error("Could not remove material.");
      await loadInventory(inventoryProjectId);
    } catch (error) {
      console.error(error);
      setInventoryError("Could not remove material. Please try again.");
    } finally {
      setInventorySaving(false);
    }
  };
  const handleRemoveEquipment = async (item) => {
    if (!window.confirm(`Permanently remove ${item.equipmentName} from this project?`)) return;
    setInventorySaving(true);
    setInventoryError("");
    try {
      const response = await fetch(`http://localhost:7070/api/equipment/${item.equipmentId}`, {
        method: "DELETE",
      });
      if (!response.ok) throw new Error("Could not remove equipment.");
      await loadInventory(inventoryProjectId);
    } catch (error) {
      console.error(error);
      setInventoryError("Could not remove equipment.");
    } finally {
      setInventorySaving(false);
    }
  };

  // Expenses and budget information for the selected project.
  const [expenseProjectId, setExpenseProjectId] = useState("");
  const [projectExpenses, setProjectExpenses] = useState([]);
  const [expenseTotal, setExpenseTotal] = useState(0);
  const [remainingBudget, setRemainingBudget] = useState(0);
  const [expenseDescription, setExpenseDescription] = useState("");
  const [expenseAmount, setExpenseAmount] = useState("");
  const [expenseLoading, setExpenseLoading] = useState(false);
  const [expenseSaving, setExpenseSaving] = useState(false);
  const [expenseError, setExpenseError] = useState("");
  const [expenseSearch, setExpenseSearch] = useState("");
  useEffect(() => {
    if (!expenseProjectId && projects.length > 0) {
      setExpenseProjectId(projects[0].id);
    }
  }, [projects, expenseProjectId]);
  const loadProjectExpenses = async (projectId) => {
    if (!projectId) return;
    setExpenseLoading(true);
    setExpenseError("");
    try {
      const base = `http://localhost:7070/api/projects/${projectId}`;
      const responses = await Promise.all([
        fetch(`${base}/expenses`),
        fetch(`${base}/expenses/total`),
        fetch(`${base}/budget/remaining`),
      ]);
      if (responses.some((response) => !response.ok)) {
        throw new Error("Could not load expense information.");
      }
      const [history, totals, remaining] = await Promise.all(
        responses.map((response) => response.json())
      );
      setProjectExpenses(history);
      setExpenseTotal(Number(totals.totalExpenses) || 0);
      setRemainingBudget(Number(remaining.remainingBudget) || 0);
    } catch (error) {
      console.error("Error loading expenses:", error);
      setExpenseError("Could not load expenses. Check that the Java backend is running.");
    } finally {
      setExpenseLoading(false);
    }
  };
  useEffect(() => {
    if (activePage === "Expenses & Budget" && expenseProjectId) {
      loadProjectExpenses(expenseProjectId);
    }

  // Reload when the selected project or page changes.
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activePage, expenseProjectId]);
  const handleAddExpense = async (event) => {
    event.preventDefault();
    if (!expenseProjectId || expenseSaving) return;
    const amount = Number(expenseAmount);
    if (!expenseDescription.trim() || !Number.isFinite(amount) || amount <= 0) {
      alert("Enter a description and an amount greater than zero.");
      return;
    }
    setExpenseSaving(true);
    setExpenseError("");
    try {
      const response = await fetch("http://localhost:7070/api/expenses", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          projectId: expenseProjectId,
          description: expenseDescription.trim(),
          amount,
        }),
      });
      if (!response.ok) throw new Error("Could not save expense.");
      setExpenseDescription("");
      setExpenseAmount("");
      await loadProjectExpenses(expenseProjectId);
      alert("Expense saved successfully.");
    } catch (error) {
      console.error("Error saving expense:", error);
      setExpenseError("Could not save expense. Please try again.");
    } finally {
      setExpenseSaving(false);
    }
  };

  // Removes an expense and reloads the project budget.
  const handleRemoveExpense = async (expense) => {
    if (expenseSaving) return;
    const confirmed = window.confirm(`Permanently remove ${expense.description} (${formatMoney(expense.amount)}) from this project?`);
    if (!confirmed) return;
    setExpenseSaving(true);
    setExpenseError("");
    try {
      const response = await fetch(`http://localhost:7070/api/expenses/${expense.expenseId}`, {
        method: "DELETE",
      });
      if (!response.ok) throw new Error("Could not remove expense.");
      await loadProjectExpenses(expenseProjectId);
    } catch (error) {
      console.error("Error removing expense:", error);
      setExpenseError("Could not remove expense. Please try again.");
    } finally {
      setExpenseSaving(false);
    }
  };

  // Edit project information without changing its client or status.
  const [editingProject, setEditingProject] = useState(null);
  const [projectEditForm, setProjectEditForm] = useState({ projectName: "", budget: "", progress: "", estimatedCompletionDate: "" });
  const [projectSaving, setProjectSaving] = useState(false);
  const [projectActionError, setProjectActionError] = useState("");
  const [statusProject, setStatusProject] = useState(null);
  const [newStatus, setNewStatus] = useState("ACTIVE");
  const refreshProjects = async () => {
    const response = await fetch("http://localhost:7070/api/projects");
    if (!response.ok) throw new Error("Could not reload projects.");
    const data = await response.json();
    setProjects(data.map((p) => ({
      id: p.projectId, name: p.projectName, type: "Residential Project",
      status: p.status, progress: Number(p.progress) || 0,
      budget: Number(p.budget) || 0, clientId: p.clientId,
      estimatedCompletionDate: p.estimatedCompletionDate || ""
    })));
  };
  const openEditProject = (project) => {
    setProjectActionError("");
    setEditingProject(project);
    setProjectEditForm({
      projectName: project.name, budget: String(project.budget),
      progress: String(project.progress),
      estimatedCompletionDate: project.estimatedCompletionDate || ""
    });
  };
  const handleEditProject = async (event) => {
    event.preventDefault();
    if (!editingProject || projectSaving) return;
    const budget = Number(projectEditForm.budget);
    const progress = Number(projectEditForm.progress);
    if (!projectEditForm.projectName.trim() || !Number.isFinite(budget) || budget < 0 ||
        !Number.isFinite(progress) || progress < 0 || progress > 100) {
      setProjectActionError("Enter a project name, a valid budget, and progress between 0 and 100.");
      return;
    }
    setProjectSaving(true);
    setProjectActionError("");
    try {
      const response = await fetch(`http://localhost:7070/api/projects/${editingProject.id}`, {
        method: "PUT", headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          projectName: projectEditForm.projectName.trim(), budget, progress,
          estimatedCompletionDate: projectEditForm.estimatedCompletionDate || null
        })
      });
      const result = await response.json();
      if (!response.ok) throw new Error(result.error || "Could not update project.");
      await refreshProjects();
      setEditingProject(null);
      if (expenseProjectId === editingProject.id && activePage === "Expenses & Budget") {
        await loadProjectExpenses(editingProject.id);
      }
    } catch (error) { setProjectActionError(error.message); }
    finally { setProjectSaving(false); }
  };
  const handleChangeProjectStatus = async (event) => {
    event.preventDefault();
    if (!statusProject || projectSaving) return;
    setProjectSaving(true);
    setProjectActionError("");
    try {
      const response = await fetch(`http://localhost:7070/api/projects/${statusProject.id}/status`, {
        method: "PUT", headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ status: newStatus })
      });
      const result = await response.json();
      if (!response.ok) throw new Error(result.error || "Could not update project status.");
      await refreshProjects();
      setStatusProject(null);
    } catch (error) { setProjectActionError(error.message); }
    finally { setProjectSaving(false); }
  };
  const handleArchiveProject = async (project) => {
    if (projectSaving || !window.confirm(`Archive ${project.name}? It will remain in your records, but cannot be edited or reactivated.`)) return;
    setProjectSaving(true);
    setProjectActionError("");
    try {
      const response = await fetch(`http://localhost:7070/api/projects/${project.id}`, { method: "DELETE" });
      const result = await response.json();
      if (!response.ok) throw new Error(result.error || "Could not archive project.");
      await refreshProjects();
    } catch (error) { setProjectActionError(error.message); }
    finally { setProjectSaving(false); }
  };
  // Load real expense totals for every project shown on the dashboard.
  useEffect(() => {
    if (activePage !== "Dashboard") return;
    let cancelled = false;
    const loadDashboardBudget = async () => {
      setDashboardBudgetLoading(true);
      setDashboardBudgetError("");
      try {
        const totals = await Promise.all(projects.map(async (project) => {
          const response = await fetch(`http://localhost:7070/api/projects/${project.id}/expenses/total`);
          if (!response.ok) throw new Error("Could not load expenses.");
          const data = await response.json();
          return Number(data.totalExpenses) || 0;
        }));
        if (!cancelled) setDashboardExpenses(totals.reduce((sum, value) => sum + value, 0));
        // Expense records currently expose description and amount; only use explicit
        // category values when provided. Unclassified records belong in Other.
        const expenseLists = await Promise.all(projects.map(async (project) => {
          const response = await fetch(`http://localhost:7070/api/projects/${project.id}/expenses`);
          if (!response.ok) throw new Error("Could not load expense details.");
          const records = await response.json();
          return Array.isArray(records) ? records : [];
        }));
        const categories = { Materials: 0, Labor: 0, Equipment: 0, Other: 0 };
        expenseLists.flat().forEach((expense) => {
          const category = String(expense.category || "").trim().toLowerCase();
          const name = category === "materials" || category === "material" ? "Materials"
            : category === "labor" || category === "labour" ? "Labor"
            : category === "equipment" ? "Equipment" : "Other";
          categories[name] += Number(expense.amount) || 0;
        });
        if (!cancelled) setDashboardCategories(Object.entries(categories).map(([name, amount]) => ({ name, amount })));
      } catch (error) {
        console.error("Error loading dashboard budget:", error);
        if (!cancelled) setDashboardBudgetError("Could not load expenses from the backend.");
      } finally {
        if (!cancelled) setDashboardBudgetLoading(false);
      }
    };
    loadDashboardBudget();
    return () => { cancelled = true; };
  }, [activePage, projects]);
  const upcomingDeadlines = projects
    .filter((project) => project.estimatedCompletionDate && project.status !== "ARCHIVED" && project.status !== "COMPLETED")
    .map((project) => ({
      ...project,
      deadline: new Date(`${project.estimatedCompletionDate.slice(0, 10)}T12:00:00`),
    }))
    .filter((project) => !Number.isNaN(project.deadline.getTime()))
    .sort((a, b) => a.deadline - b.deadline)
    .slice(0, 5);
  const budgetSpentPercent = totalBudget > 0 ? Math.min(100, Math.max(0, dashboardExpenses / totalBudget * 100)) : 0;
  const spendingColors = { Materials: "#f59e0b", Labor: "#0ea5e9", Equipment: "#a855f7", Other: "#94a3b8" };
  const categorizedSpending = dashboardCategories.reduce((sum, item) => sum + item.amount, 0);
  const spendingChartTotal = Math.max(dashboardExpenses, categorizedSpending);
  let spendingPosition = 0;
  const spendingStops = dashboardCategories.filter((item) => item.amount > 0).map((item) => {
    const start = spendingPosition;
    spendingPosition += spendingChartTotal > 0 ? item.amount / spendingChartTotal * 100 : 0;
    return `${spendingColors[item.name]} ${start}% ${spendingPosition}%`;
  });
  const spendingGradient = spendingStops.length ? `conic-gradient(${spendingStops.join(", ")})` : "conic-gradient(#263b53 0% 100%)";
  const formatMoney = (value) =>
    Number(value).toLocaleString("en-US", {
      style: "currency", currency: "USD",
    });
  return (
    <div className="app">

      {/* Left navigation menu */}
    <aside className="sidebar">
      <div className="logo">
        <img
          src="/images/andracorp-logo.png"
          alt="AndraCorp Logo"
          className="brand-logo"
        />
        <h2>ANDRACORP</h2>
        <p>Construction Management</p>
      </div>
      <nav>
        <button
          className={activePage === "Dashboard" ? "nav-button active" : "nav-button"}
          onClick={() => setActivePage("Dashboard")}
        >
          <LayoutDashboard size={20} />
          <span>Dashboard</span>
        </button>
        <button
          className={activePage === "Projects" ? "nav-button active" : "nav-button"}
          onClick={() => setActivePage("Projects")}
        >
          <FolderKanban size={20} />
          <span>Projects</span>
        </button>
        <button
          className={activePage === "Clients" ? "nav-button active" : "nav-button"}
          onClick={() => setActivePage("Clients")}
        >
          <Users size={20} />
          <span>Clients</span>
        </button>
        <button
          className={activePage === "Workforce" ? "nav-button active" : "nav-button"}
          onClick={() => setActivePage("Workforce")}
        >
          <HardHat size={20} />
          <span>Workforce</span>
        </button>
        <button
          className={activePage === "Expenses & Budget" ? "nav-button active" : "nav-button"}
          onClick={() => setActivePage("Expenses & Budget")}
        >
          <CircleDollarSign size={20} />
          <span>Expenses & Budget</span>
        </button>
        <button
          className="nav-button"
          type="button"
        >
          <FileText size={20} />
          <span>Change Orders</span>
        </button>
        <button
          className={activePage === "Materials & Equipment" ? "nav-button active" : "nav-button"}
          onClick={() => setActivePage("Materials & Equipment")}
        >
          <Package size={20} />
          <span>Materials & Equipment</span>
        </button>
        <button
          className="nav-button"
          type="button"
        >
          <CalendarDays size={20} />
          <span>Daily Reports</span>
        </button>
      </nav>
    </aside>

    {/* Main contractor content */}
    <main className="main-content">

    {/* Top navigation bar */}
    <header className="top-navbar">
      <div className="top-search">
        <Search size={20} />
        <input
          type="search"
          placeholder="Search projects, clients, workers, or change orders..."
          aria-label="Search AndraCorp"
          disabled
        />
      </div>
      <div className="top-navbar-right">

        {/* Notifications */}
      <div className="top-notifications">
        <Bell size={23} />
        <span className="notification-indicator" />
      </div>

        {/* Contractor profile */}
        <div className="top-profile">
          <div className="profile-avatar">
            MA
          </div>
          <div className="profile-info">
            <strong>Marco Andrade</strong>
            <span>Contractor</span>
          </div>
          <ChevronDown size={18} />
        </div>
      </div>
    </header>

      {/* Contractor Dashboard */}
      {activePage === "Dashboard" && (
        <>
          {/* Dashboard welcome header */}
          <header className="dashboard-header welcome-header">

            {/* AndraCorp gold logo watermark */}
            <div className="header-logo-watermark">
              <img
                src="/images/andracorp-watermark.png"
                alt=""
                aria-hidden="true"
              />
            </div>

            {/* Welcome message */}
            <div className="welcome-content">
              <h1>
                Welcome back, <span>Marco</span>
              </h1>
              <p>
                Manage your construction projects.
              </p>
            </div>

            {/* Create new project */}
            <button
              type="button"
              className="new-project-button"
              onClick={() => setShowProjectForm(true)}
            >
              + New Project
            </button>
          </header>

      {/* Dashboard summary */}
      <section className="summary-grid">
        <div className="summary-card projects-summary">
          <div className="summary-card-top">
            <FolderOpen size={27} />
            <ChartNoAxesColumnIncreasing size={20} />
          </div>
          <p>Active Projects</p>
          <h2>{projects.filter((p) => p.status === "ACTIVE").length}</h2>
        </div>
        <div className="summary-card workers-summary">
          <div className="summary-card-top">
            <Users size={27} />
            <ChartNoAxesColumnIncreasing size={20} />
          </div>
          <p>Total Workers</p>
          <h2>{totalWorkers}</h2>
        </div>
        <div className="summary-card changes-summary">
          <div className="summary-card-top">
            <FileClock size={27} />
            <ChartNoAxesColumnIncreasing size={20} />
          </div>
          <p>Pending Changes</p>
          <h2>—</h2>
        </div>
        <div className="summary-card budget-summary">
          <div className="summary-card-top">
            <CircleDollarSign size={27} />
            <ChartNoAxesColumnIncreasing size={20} />
          </div>
          <p>Total Budget</p>
          <h2>${totalBudget.toLocaleString()}</h2>
        </div>
      </section>

      {/* Current projects and recent activity */}
      <div className="dashboard-panels">
        <div className="dashboard-projects">
      <section className="projects-section">
        <div className="section-header">
          <h2>Current Projects</h2>
          <button
            type="button"
            className="view-button"
            onClick={() => setActivePage("Projects")}
          >
            View All →
          </button>
        </div>
        <div className="projects-table-wrapper">
          <table className="projects-table">
            <thead>
              <tr>
                <th>Project Name</th>
                <th>Client</th>
                <th>Progress</th>
                <th>Budget</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {projects.length === 0 ? (
                <tr>
                  <td colSpan={6} className="empty-projects">
                    No projects available.
                  </td>
                </tr>
              ) : (
                projects.map((project) => (
                  <tr key={project.id}>
                    <td>
                      <strong className="project-table-name">
                        {project.name}
                      </strong>
                      <span className="project-table-type">
                        {project.type}
                      </span>
                    </td>
                    <td>{getClientName(project.clientId)}</td>
                    <td>
                      <div className="table-progress">
                        <div className="progress-bar">
                          <div
                            className="progress"
                            style={{
                              background: Number(project.progress) >= 75 ? "#10b981" : Number(project.progress) >= 40 ? "#f59e0b" : Number(project.progress) >= 20 ? "#0ea5e9" : "#a855f7",
                              width: `${Math.max(
                                0,
                                Math.min(100, project.progress)
                              )}%`,
                            }}
                          />
                        </div>
                        <span>{project.progress}%</span>
                      </div>
                    </td>
                    <td>
                      ${project.budget.toLocaleString()}
                    </td>
                    <td>
                      <span
                        className={`project-status ${
                          project.status === "ACTIVE"
                            ? "status-active"
                            : "status-other"
                        }`}
                      >
                        {project.status}
                      </span>
                    </td>
                    <td>
                      <button
                        type="button"
                        className="table-action-button"
                        onClick={() => setActivePage("Projects")}
                        title={`Manage ${project.name}`}
                        aria-label={`Manage ${project.name}`}
                      >
                        <span aria-hidden="true">⋯</span>
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </section>
        </div>
        <aside className="recent-activity-panel">
          <div className="section-header"><h2>Recent Activity</h2></div>
          <div className="activity-empty">
            <FileClock size={32} />
            <p>No recent activity available.</p>
            <span>Project updates will appear here once activity tracking is connected.</span>
          </div>
        </aside>
      </div>
      {/* Budget overview and upcoming deadlines */}
      <div className="dashboard-charts">
        <section className="budget-overview-section">
          <div className="section-header"><h2>Budget Overview</h2></div>
          {dashboardBudgetLoading ? <p className="dashboard-muted">Loading expenses...</p> : dashboardBudgetError ? (
            <p role="alert" className="dashboard-error">{dashboardBudgetError}</p>
          ) : (
            <div className="budget-overview-content budget-category-layout">
              <div className="budget-chart" style={{ background: spendingGradient }}>
                <div className="budget-chart-center"><strong>{formatMoney(dashboardExpenses)}</strong><span>Total spent</span></div>
              </div>
              <div className="budget-overview-details">
                {dashboardCategories.filter((item) => item.amount > 0).length === 0 ? (
                  <p className="dashboard-muted">No expenses recorded yet.</p>
                ) : dashboardCategories.filter((item) => item.amount > 0).map((item) => (
                  <div key={item.name}>
                    <span className="budget-dot" style={{ background: spendingColors[item.name] }} />
                    <span>{item.name}</span>
                    <span className="budget-category-percent">{spendingChartTotal > 0 ? `${(item.amount / spendingChartTotal * 100).toFixed(1)}%` : "0%"}</span>
                    <strong>{formatMoney(item.amount)}</strong>
                  </div>
                ))}
                <div className="budget-total"><span>Remaining budget</span><strong>{formatMoney(Math.max(0, totalBudget - dashboardExpenses))}</strong></div>
                <div><span>Total budget</span><strong>{formatMoney(totalBudget)}</strong></div>
              </div>
            </div>
          )}
        </section>
        <section className="upcoming-deadlines-section">
          <div className="section-header"><h2>Upcoming Deadlines</h2><CalendarDays size={20} /></div>
          {upcomingDeadlines.length === 0 ? (
            <p className="dashboard-muted">No project completion dates scheduled.</p>
          ) : (
            <div className="deadline-list">
              {upcomingDeadlines.map((project) => {
                const daysLeft = Math.ceil((project.deadline.getTime() - Date.now()) / 86400000);
                return (
                  <div className="deadline-item" key={project.id}>
                    <CalendarDays className="deadline-calendar-icon" size={24} />
                    <div className="deadline-info"><strong>{project.name}</strong><span>{project.deadline.toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" })}</span></div>
                    <span className={daysLeft < 0 ? "deadline-overdue" : "deadline-days"}>
                      {daysLeft < 0 ? "Overdue" : daysLeft === 0 ? "Today" : `${daysLeft} days left`}
                    </span>
                  </div>
                );
              })}
            </div>
          )}
        </section>
      </div>
          </>
        )}

                {/* Projects page */}
        {activePage === "Projects" && (
          <>
            <header className="dashboard-header projects-page-header">
              <div>
                <p className="projects-eyebrow"><h1>PROJECT MANAGEMENT</h1></p>
              </div>
              <button type="button" className="new-project-button" onClick={() => setShowProjectForm(true)}>+ New Project</button>
            </header>
            <div className="projects-overview-grid">
              <div className="projects-overview-card"><span>All Projects</span><strong>{projects.length}</strong></div>
              <div className="projects-overview-card"><span>Active</span><strong>{projects.filter((p) => p.status === "ACTIVE").length}</strong></div>
              <div className="projects-overview-card"><span>Completed</span><strong>{projects.filter((p) => p.status === "COMPLETED").length}</strong></div>
              <div className="projects-overview-card"><span>Project Budgets</span><strong className="projects-budget-total">{formatMoney(totalBudget)}</strong></div>
            </div>
            <section className="projects-management-panel">
              <div className="projects-management-top">
                <div><h2>PROJECT PORTFOLIO</h2></div>
                <span className="projects-result-count">{projects.filter((p) => projectFilter === "ALL" || p.status === projectFilter).length} shown</span>
              </div>
              <div className="projects-filter-row" role="group" aria-label="Filter projects by status">
                {[["ALL", "All Projects"], ["ACTIVE", "Active"], ["ON_HOLD", "On Hold"], ["COMPLETED", "Completed"], ["ARCHIVED", "Archived"]].map(([value, label]) => (
                  <button key={value} type="button" className={`projects-filter ${projectFilter === value ? "selected" : ""}`} onClick={() => setProjectFilter(value)}>{label}</button>
                ))}
              </div>
              {projectActionError && <p role="alert" className="project-action-error">{projectActionError}</p>}
              <div className="projects-portfolio-list">
                {projects.filter((p) => projectFilter === "ALL" || p.status === projectFilter).length === 0 ? (
                  <div className="projects-empty-state"><FolderKanban size={34} /><h3>No projects found</h3><p>There are no projects in this category yet.</p></div>
                ) : projects.filter((p) => projectFilter === "ALL" || p.status === projectFilter).map((project) => (
                  <article className="projects-portfolio-card" key={project.id}>
                    <div className="projects-portfolio-heading">
                      <div className="projects-title-group">
                        <span className="projects-item-icon"><FolderKanban size={23} /></span>
                        <div><h3>{project.name}</h3><p>{project.type} <span aria-hidden="true">·</span> {getClientName(project.clientId)}</p></div>
                      </div>
                      <span className={`projects-status-pill projects-status-${String(project.status || "unknown").toLowerCase()}`}>{String(project.status || "Unknown").replaceAll("_", " ")}</span>
                    </div>
                    <div className="projects-metrics-grid">
                      <div><span>Project Budget</span><strong>{formatMoney(project.budget)}</strong></div>
                      <div><span>Assigned Workers</span><strong>{workers.filter((w) => w.projectId === project.id).length}</strong></div>
                      <div><span>Completion Date</span><strong>{project.estimatedCompletionDate ? new Date(`${project.estimatedCompletionDate.slice(0, 10)}T12:00:00`).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" }) : "Not set"}</strong></div>
                    </div>
                    <div className="projects-progress-heading"><span>Overall Progress</span><strong>{Number(project.progress) || 0}%</strong></div>
                    <div className="projects-progress-track"><div style={{ width: `${Math.max(0, Math.min(100, Number(project.progress) || 0))}%` }} /></div>
                    {project.status !== "ARCHIVED" ? (
                      <div className="projects-portfolio-actions">
                        <button type="button" onClick={() => openClientAssignment(project)}>{project.clientId ? "Change Client" : "Assign Client"}</button>
                        <button type="button" onClick={() => openEditProject(project)}>Edit Project</button>
                        <button type="button" onClick={() => { setProjectActionError(""); setStatusProject(project); setNewStatus(project.status); }}>Change Status</button>
                        <button type="button" className="projects-archive-action" disabled={projectSaving} onClick={() => handleArchiveProject(project)}>Archive</button>
                      </div>
                    ) : <p className="archived-project-note">Archived — read only</p>}
                  </article>
                ))}
              </div>
            </section>
          </>
        )}

{/* Clients page */}
       {activePage === "Clients" && (() => {
         const assignedClients = clients.filter((client) => projects.some((project) => project.clientId === client.clientId));
         const visibleClients = clients.filter((client) => {
           const assigned = projects.some((project) => project.clientId === client.clientId);
           const matchesFilter = clientFilter === "ALL" || (clientFilter === "ASSIGNED" ? assigned : !assigned);
           const query = clientSearch.trim().toLowerCase();
           return matchesFilter && [client.name, client.email, client.phoneNumber].some((value) => String(value || "").toLowerCase().includes(query));
         });
         return (
           <>
             <header className="dashboard-header">
               <div><h1>CLIENTS</h1></div>
               <button className="new-project-button" onClick={() => openClientForm()}>+ Add Client</button>
             </header>
             <div className="clients-overview-grid">
               <div className="clients-stat clients-stat-total"><Users size={27} /><div><span>Total Clients</span><strong>{clients.length}</strong></div></div>
               <div className="clients-stat clients-stat-assigned"><Link2 size={27} /><div><span>Assigned Clients</span><strong>{assignedClients.length}</strong></div></div>
               <div className="clients-stat clients-stat-unassigned"><UserRoundPlus size={27} /><div><span>Unassigned Clients</span><strong>{clients.length - assignedClients.length}</strong></div></div>
             </div>
             <section className="clients-directory">
               <div className="clients-directory-header"><h2>CLIENT DIRECTORY</h2><span>{visibleClients.length} {visibleClients.length === 1 ? "Client" : "Clients"}</span></div>
               <div className="clients-directory-tools">
                 <div className="clients-search"><Search size={18} /><input aria-label="Search clients" placeholder="Search clients by name, email, or phone..." value={clientSearch} onChange={(event) => setClientSearch(event.target.value)} /></div>
                 <select aria-label="Filter clients" value={clientFilter} onChange={(event) => setClientFilter(event.target.value)}>
                   <option value="ALL">All Clients</option><option value="ASSIGNED">Assigned</option><option value="UNASSIGNED">Unassigned</option>
                 </select>
               </div>
               {clientError && <p role="alert" className="clients-error">{clientError}</p>}
               {clientLoading && <p className="clients-placeholder">Loading clients...</p>}
               {!clientLoading && visibleClients.length === 0 && <p className="clients-placeholder">No clients match your search or filter.</p>}
               <div className="clients-scroll-list">
                 {!clientLoading && visibleClients.map((client) => {
                   const clientProjects = projects.filter((project) => project.clientId === client.clientId);
                   const expanded = expandedClientId === client.clientId;
                   const initials = (client.name || "?").trim().split(/\s+/).slice(0, 2).map((part) => part[0]).join("").toUpperCase();
                   return (
                     <article className="clients-directory-card" key={client.clientId}>
                       <div className="clients-card-main">
                         <div className="clients-avatar">{initials}</div>
                         <div className="clients-contact"><h3>{client.name}</h3><p><Mail size={14} />{client.email || "No email provided"}</p><p><Phone size={14} />{client.phoneNumber || "No phone provided"}</p></div>
                         <div className="clients-project-info"><span>Assigned Projects</span><strong>{clientProjects.length}</strong></div>
                         <div className="clients-card-actions">
                           <button type="button" className="clients-view-btn" onClick={() => setExpandedClientId(expanded ? null : client.clientId)} aria-expanded={expanded}><Eye size={15} />{expanded ? "Hide Projects" : "View Projects"}</button>
                           <button type="button" onClick={() => openClientForm(client)}><Pencil size={15} />Edit Client</button>
                           <button type="button" className="clients-delete-btn" onClick={() => handleDeleteClient(client)}><Trash2 size={15} />Delete</button>
                         </div>
                       </div>
                       {expanded && <div className="clients-project-expanded">{clientProjects.length ? clientProjects.map((project) => <div key={project.id}><FolderKanban size={16} /><strong>{project.name}</strong><span>{String(project.status || "Unknown").replaceAll("_", " ")}</span></div>) : <p>This client has no assigned projects yet.</p>}</div>}
                     </article>
                   );
                 })}
               </div>
             </section>
           </>
         );
       })()}

               {/* Workforce page */}
        {activePage === "Workforce" && (() => {
          const isAssigned = (worker) => Boolean(String(worker.area || "").trim() && String(worker.taskId || "").trim());
          const assignedCount = workers.filter(isAssigned).length;
          const totalHoursWorked = workers.reduce((total, worker) =>
            total + (workHoursByWorker[worker.workerId] || []).reduce((sum, entry) => sum + (Number(entry.hours) || 0), 0), 0);
          const hoursLoaded = workers.every((worker) => worker.workerId in workHoursByWorker);
          const trades = [...new Set(workers.map((worker) => worker.trade).filter(Boolean))].sort();
          const visibleWorkers = workers.filter((worker) => {
            const query = workerSearch.trim().toLowerCase();
            const matchesSearch = [worker.workerName, worker.trade, getProjectName(worker.projectId), worker.area, worker.taskId]
              .some((value) => String(value || "").toLowerCase().includes(query));
            return matchesSearch &&
              (workerTradeFilter === "ALL" || worker.trade === workerTradeFilter) &&
              (workerProjectFilter === "ALL" || worker.projectId === workerProjectFilter) &&
              (workerStatusFilter === "ALL" || (workerStatusFilter === "ASSIGNED" ? isAssigned(worker) : !isAssigned(worker)));
          });
          const initials = (name) => String(name || "?").trim().split(/\s+/).slice(0, 2).map((part) => part[0]).join("").toUpperCase();
          return (
            <>
              <header className="dashboard-header workforce-page-header">
                <div><h1>WORKFORCE</h1></div>
                <button type="button" className="new-project-button" onClick={() => setShowWorkerForm(true)}>+ Add Worker</button>
              </header>

              <div className="workforce-overview-grid">
                <div className="workforce-stat workforce-stat-total"><Users size={27} /><div><span>Total Workers</span><strong>{workers.length}</strong></div></div>
                <div className="workforce-stat workforce-stat-assigned"><UserCheck size={27} /><div><span>Assigned Workers</span><strong>{assignedCount}</strong></div></div>
                <div className="workforce-stat workforce-stat-unassigned"><UserX size={27} /><div><span>Unassigned Workers</span><strong>{workers.length - assignedCount}</strong></div></div>
                <div className="workforce-stat workforce-stat-hours"><Clock3 size={27} /><div><span>Total Hours Worked</span><strong>{hoursLoaded ? totalHoursWorked.toLocaleString(undefined, { maximumFractionDigits: 2 }) : "Loading..."}</strong></div></div>
              </div>

              <section className="workforce-directory">
                <div className="workforce-directory-header"><h2>WORKER DIRETORY</h2><span>{visibleWorkers.length} {visibleWorkers.length === 1 ? "Worker" : "Workers"}</span></div>
                <div className="workforce-directory-tools">
                  <div className="workforce-search"><Search size={18} /><input aria-label="Search workers" placeholder="Search workers by name, trade, or project..." value={workerSearch} onChange={(event) => setWorkerSearch(event.target.value)} /></div>
                  <select aria-label="Filter by trade" value={workerTradeFilter} onChange={(event) => setWorkerTradeFilter(event.target.value)}>
                    <option value="ALL">All Trades</option>{trades.map((trade) => <option key={trade} value={trade}>{trade}</option>)}
                  </select>
                  <select aria-label="Filter by project" value={workerProjectFilter} onChange={(event) => setWorkerProjectFilter(event.target.value)}>
                    <option value="ALL">All Projects</option>{projects.map((project) => <option key={project.id} value={project.id}>{project.name}</option>)}
                  </select>
                  <select aria-label="Filter by assignment" value={workerStatusFilter} onChange={(event) => setWorkerStatusFilter(event.target.value)}>
                    <option value="ALL">All Statuses</option><option value="ASSIGNED">Assigned</option><option value="UNASSIGNED">Unassigned</option>
                  </select>
                </div>
                {workerActionError && <p role="alert" className="workforce-error">{workerActionError}</p>}
                {hoursError && <p role="alert" className="workforce-error">{hoursError}</p>}
                <div className="workforce-scroll-list">
                  {visibleWorkers.length === 0 && <p className="workforce-empty">No workers match your search or filters.</p>}
                  {visibleWorkers.map((worker) => {
                    const assigned = isAssigned(worker);
                    const historyOpen = historyWorkerId === worker.workerId;
                    const entries = workHoursByWorker[worker.workerId];
                    const hours = entries ? entries.reduce((sum, entry) => sum + (Number(entry.hours) || 0), 0) : null;
                    return (
                      <article className="workforce-worker-card" key={worker.workerId}>
                        <div className="workforce-worker-main">
                          <div className="workforce-worker-identity">
                            <span className="workforce-avatar">{initials(worker.workerName)}</span>
                            <div><h3>{worker.workerName}</h3><p>{worker.trade || "No trade"}</p><span className={`workforce-status ${assigned ? "assigned" : "unassigned"}`}>{assigned ? "Assigned" : "Unassigned"}</span></div>
                          </div>
                          <div className="workforce-worker-detail"><span>Project</span><strong>{getProjectName(worker.projectId)}</strong></div>
                          <div className="workforce-worker-detail"><span>Area</span><strong>{worker.area || "Not assigned"}</strong></div>
                          <div className="workforce-worker-detail"><span>Task</span><strong>{worker.taskId || "Not assigned"}</strong></div>
                          <div className="workforce-worker-detail workforce-hours"><span>Total Hours</span><strong>{hours === null ? "Loading..." : hours.toLocaleString(undefined, { maximumFractionDigits: 2 })}</strong></div>
                          <div className="workforce-worker-actions">
                            <button type="button" className="workforce-history-button" aria-expanded={historyOpen} onClick={() => setHistoryWorkerId((current) => current === worker.workerId ? null : worker.workerId)}><Eye size={15} />{historyOpen ? "Hide History" : "View History"}</button>
                            <button type="button" className="workforce-assign-button" onClick={() => openAssignmentForm(worker)}><RotateCcw size={15} />{assigned ? "Reassign" : "Assign"}</button>
                            <button type="button" className="workforce-record-button" disabled={!worker.taskId} onClick={() => openHoursForm(worker)}><Clock3 size={15} />Record Hours</button>
                            <button type="button" className="workforce-deactivate-button" disabled={workerActionBusy} onClick={() => handleDeleteWorker(worker)}><Trash2 size={15} />Deactivate</button>
                          </div>
                        </div>
                        {historyOpen && <div className="workforce-history"><h4><ClipboardList size={17} /> Work History</h4>{!entries ? <p>Loading work history...</p> : entries.length === 0 ? <p>No work hours recorded yet.</p> : <ul>{entries.map((entry) => <li key={entry.id}>{entry.workDate || "Date not recorded"} — {entry.taskId || "No task"} — {entry.hours} hours</li>)}</ul>}</div>}
                      </article>
                    );
                  })}
                </div>
              </section>

              <section className="workforce-inactive-section">
                <div className="workforce-directory-header"><h2>Inactive Workers</h2><span>{inactiveWorkers.length} {inactiveWorkers.length === 1 ? "Worker" : "Workers"}</span></div>
                <div className="workforce-inactive-list">
                  {inactiveWorkers.length === 0 ? <p className="workforce-empty">No inactive workers.</p> : inactiveWorkers.map((worker) => (
                    <article className="workforce-inactive-card" key={worker.workerId}>
                      <div><h3>{worker.workerName}</h3><p>{worker.trade || "No trade"} · {getProjectName(worker.projectId)}</p><small>Area: {worker.area || "Not assigned"} · Task: {worker.taskId || "Not assigned"}</small></div>
                      <button type="button" disabled={workerActionBusy} onClick={() => handleReactivateWorker(worker)}><RotateCcw size={15} />{workerActionBusy ? "Updating..." : "Reactivate Worker"}</button>
                    </article>
                  ))}
                </div>
              </section>
            </>
          );
        })()}
        {activePage === "Expenses & Budget" && (() => {
          const selectedProject = projects.find((project) => project.id === expenseProjectId);
          const budget = Number(selectedProject?.budget) || 0;
          const spentPercent = budget > 0 ? (expenseTotal / budget) * 100 : 0;
          const visibleExpenses = projectExpenses.filter((expense) =>
            String(expense.description || "").toLowerCase().includes(expenseSearch.trim().toLowerCase())
          );
          return (
            <>
              <header className="dashboard-header expenses-page-header">
                <div>
                  <h1>EXPENSES &amp; BUDGET</h1>
                  
                </div>
              </header>
              <section className="expenses-management-panel">
                <div className="expenses-panel-heading">
                  <div><h2>PROJECT EXPENSES</h2></div>
                  <span>{projectExpenses.length} {projectExpenses.length === 1 ? "expense" : "expenses"} recorded</span>
                </div>
                <div className="expenses-project-picker">
                  <label htmlFor="expense-project">Select Project</label>
                  <select id="expense-project" value={expenseProjectId} onChange={(event) => { setExpenseProjectId(event.target.value); setExpenseSearch(""); }}>
                    {projects.length === 0 && <option value="">No projects available</option>}
                    {projects.map((project) => (
                      <option key={project.id} value={project.id}>
                        {project.name} — {getClientName(project.clientId)}
                      </option>
                    ))}
                  </select>
                  {selectedProject && <p className="expenses-project-client"><Users size={15} /> Client: <strong>{getClientName(selectedProject.clientId)}</strong></p>}
                </div>
                {expenseError && <p className="expenses-error" role="alert">{expenseError}</p>}
                {expenseProjectId && (
                  <>
                    <div className="expenses-overview-grid">
                      <div className="expenses-stat expenses-stat-budget"><CircleDollarSign size={24} /><span>PROJECT BUDGET</span><strong>{formatMoney(budget)}</strong></div>
                      <div className="expenses-stat expenses-stat-spent"><TrendingUp size={24} /><span>TOTAL EXPENSES</span><strong>{formatMoney(expenseTotal)}</strong></div>
                      <div className="expenses-stat expenses-stat-remaining"><ChartNoAxesColumnIncreasing size={24} /><span>REMAINING BUDGET</span><strong>{formatMoney(remainingBudget)}</strong></div>
                      <div className="expenses-stat expenses-stat-usage"><ChartNoAxesColumnIncreasing size={24} /><span>BUDGET USED</span><strong>{budget > 0 ? `${spentPercent.toFixed(1)}%` : "N/A"}</strong></div>
                    </div>
                    <div className="expenses-progress-panel">
                      <div className="expenses-progress-top"><h3>BUDGET PROGRESS</h3><strong>{budget > 0 ? `${spentPercent.toFixed(1)}% used` : "No budget set"}</strong></div>
                      <div className="expenses-progress-track"><div className={spentPercent > 100 ? "over-budget" : ""} style={{ width: `${Math.min(100, Math.max(0, spentPercent))}%` }} /></div>
                      <div className="expenses-progress-foot"><span>Spent: {formatMoney(expenseTotal)}</span><span>Budget: {formatMoney(budget)}</span></div>
                      {budget > 0 && spentPercent > 100 && <p className="expenses-over-budget">This project is over budget by {formatMoney(expenseTotal - budget)}.</p>}
                    </div>
                    <div className="expenses-bottom-grid">
                      <section className="expenses-add-panel">
                        <h3>ADD EXPENSE</h3>
                        <p>Record a new expense for {selectedProject?.name || "this project"}.</p>
                        <form onSubmit={handleAddExpense} className="expenses-add-form">
                          <label htmlFor="expense-description">Description</label>
                          <input id="expense-description" type="text" value={expenseDescription} onChange={(event) => setExpenseDescription(event.target.value)} placeholder="Example: Drywall Materials" required />
                          <label htmlFor="expense-amount">Amount ($)</label>
                          <input id="expense-amount" type="number" min="0.01" step="0.01" value={expenseAmount} onChange={(event) => setExpenseAmount(event.target.value)} placeholder="Example: 250" required />
                          <button type="submit" disabled={expenseSaving || expenseLoading}>{expenseSaving ? "Saving..." : "+ Add Expense"}</button>
                        </form>
                      </section>
                      <section className="expenses-history-panel">
                        <div className="expenses-history-heading"><h3>EXPENSE HISTORY</h3><span>{visibleExpenses.length} shown</span></div>
                        <div className="expenses-history-search"><Search size={17} /><input type="search" aria-label="Search expense history" placeholder="Search expenses by description..." value={expenseSearch} onChange={(event) => setExpenseSearch(event.target.value)} /></div>
                        <div className="expenses-history-list">
                          {expenseLoading ? <p className="expenses-empty">Loading expenses...</p> : visibleExpenses.length === 0 ? <p className="expenses-empty">{projectExpenses.length === 0 ? "No expenses recorded for this project." : "No expenses match your search."}</p> : visibleExpenses.map((expense) => (
                            <div className="expenses-history-item" key={expense.expenseId}>
                              <div className="expenses-history-description"><CircleDollarSign size={19} /><strong>{expense.description}</strong></div>
                              <strong className="expenses-history-amount">{formatMoney(expense.amount)}</strong>
                              <button type="button" disabled={expenseSaving} onClick={() => handleRemoveExpense(expense)}><Trash2 size={15} /> Remove</button>
                            </div>
                          ))}
                        </div>
                      </section>
                    </div>
                  </>
                )}
              </section>
            </>
          );
        })()}
        {activePage === "Materials & Equipment" && (() => {
          const selectedProject = projects.find((project) => project.id === inventoryProjectId);
          const visibleMaterials = materials.filter((material) =>
            String(material.materialName || "").toLowerCase().includes(materialSearch.trim().toLowerCase())
          );
          const visibleEquipment = equipment.filter((item) =>
            String(item.equipmentName || "").toLowerCase().includes(equipmentSearch.trim().toLowerCase())
          );
          const totalMaterialQuantity = materials.reduce((sum, material) => sum + (Number(material.quantity) || 0), 0);
          return (
            <>
              <header className="dashboard-header inventory-page-header">
                <div>
                  <h1>MATERIAL &amp; EQUIPMENT</h1>
                </div>
              </header>
              <section className="inventory-management-panel">
                <div className="inventory-panel-heading">
                  <div><h2>PROJECT INVENTORY</h2></div>
                  <span>{materials.length + equipment.length} inventory entries</span>
                </div>
                <div className="inventory-project-picker">
                  <label htmlFor="inventory-project">Select Project</label>
                  <select id="inventory-project" value={inventoryProjectId} onChange={(event) => {
                    setInventoryProjectId(event.target.value);
                    setMaterialSearch("");
                    setEquipmentSearch("");
                  }}>
                    {projects.length === 0 && <option value="">No projects available</option>}
                    {projects.map((project) => <option key={project.id} value={project.id}>{project.name} — {getClientName(project.clientId)}</option>)}
                  </select>
                  {selectedProject && <p className="inventory-project-client"><Users size={15} /> Client: <strong>{getClientName(selectedProject.clientId)}</strong></p>}
                </div>
                {inventoryError && <p role="alert" className="inventory-error">{inventoryError}</p>}
                {!inventoryProjectId ? <p className="inventory-empty">Create or select a project to manage its inventory.</p> : (
                  <>
                    <div className="inventory-overview-grid">
                      <div className="inventory-stat inventory-stat-materials"><Package size={25} /><span>MATERIAL TYPES</span><strong>{materials.length}</strong></div>
                      <div className="inventory-stat inventory-stat-quantity"><ChartNoAxesColumnIncreasing size={25} /><span>MATERIAL QUANTITY</span><strong>{totalMaterialQuantity.toLocaleString()}</strong></div>
                      <div className="inventory-stat inventory-stat-equipment"><HardHat size={25} /><span>EQUIPMENT ITEMS</span><strong>{equipment.length}</strong></div>
                      <div className="inventory-stat inventory-stat-total"><ClipboardList size={25} /><span>INVENTORY ENTRIES</span><strong>{materials.length + equipment.length}</strong></div>
                    </div>
                    <div className="inventory-content-grid">
                      <section className="inventory-section-panel">
                        <div className="inventory-section-heading"><div><h3>MATERIAL</h3></div><span>{materials.length} types</span></div>
                        <form onSubmit={handleAddMaterial} className="inventory-add-form">
                          <div className="inventory-form-fields">
                            <div><label htmlFor="material-name">Material Name</label><input id="material-name" type="text" value={materialName} onChange={(event) => setMaterialName(event.target.value)} placeholder="Example: Drywall Sheets" required /></div>
                            <div><label htmlFor="material-quantity">Quantity</label><input id="material-quantity" type="number" min="0.01" step="any" value={materialQuantity} onChange={(event) => setMaterialQuantity(event.target.value)} placeholder="Example: 20" required /></div>
                          </div>
                          <button type="submit" disabled={inventorySaving || inventoryLoading}>{inventorySaving ? "Saving..." : "+ Add Material"}</button>
                        </form>
                        <div className="inventory-list-heading"><h4>Material Inventory</h4><span>{visibleMaterials.length} shown</span></div>
                        <div className="inventory-search"><Search size={17} /><input type="search" aria-label="Search materials" placeholder="Search materials..." value={materialSearch} onChange={(event) => setMaterialSearch(event.target.value)} /></div>
                        <div className="inventory-scroll-list">
                          {inventoryLoading ? <p className="inventory-empty">Loading materials...</p> : visibleMaterials.length === 0 ? <p className="inventory-empty">{materials.length ? "No materials match your search." : "No materials recorded for this project."}</p> : visibleMaterials.map((material) => (
                            <article className="inventory-item" key={material.materialId}>
                              <div className="inventory-item-icon"><Package size={20} /></div>
                              <div className="inventory-item-info"><h4>{material.materialName}</h4><p>Quantity: <strong>{material.quantity}</strong></p></div>
                              <div className="inventory-item-actions">
                                <button type="button" disabled={inventorySaving} onClick={() => handleUpdateMaterial(material)}><Pencil size={15} /> Update Quantity</button>
                                <button type="button" className="inventory-remove" disabled={inventorySaving} onClick={() => handleRemoveMaterial(material)}><Trash2 size={15} /> Remove</button>
                              </div>
                            </article>
                          ))}
                        </div>
                      </section>
                      <section className="inventory-section-panel">
                        <div className="inventory-section-heading"><div><h3>EQUIPMENT</h3></div><span>{equipment.length} items</span></div>
                        <form onSubmit={handleAddEquipment} className="inventory-add-form">
                          <div className="inventory-form-fields inventory-equipment-fields"><div><label htmlFor="equipment-name">Equipment Name</label><input id="equipment-name" type="text" value={equipmentName} onChange={(event) => setEquipmentName(event.target.value)} placeholder="Example: Power Drill" required /></div></div>
                          <button type="submit" disabled={inventorySaving || inventoryLoading}>{inventorySaving ? "Saving..." : "+ Add Equipment"}</button>
                        </form>
                        <div className="inventory-list-heading"><h4>Equipment Inventory</h4><span>{visibleEquipment.length} shown</span></div>
                        <div className="inventory-search"><Search size={17} /><input type="search" aria-label="Search equipment" placeholder="Search equipment..." value={equipmentSearch} onChange={(event) => setEquipmentSearch(event.target.value)} /></div>
                        <div className="inventory-scroll-list">
                          {inventoryLoading ? <p className="inventory-empty">Loading equipment...</p> : visibleEquipment.length === 0 ? <p className="inventory-empty">{equipment.length ? "No equipment matches your search." : "No equipment recorded for this project."}</p> : visibleEquipment.map((item) => (
                            <article className="inventory-item" key={item.equipmentId}>
                              <div className="inventory-item-icon"><HardHat size={20} /></div>
                              <div className="inventory-item-info"><h4>{item.equipmentName}</h4><p>Assigned to {selectedProject?.name}</p></div>
                              <div className="inventory-item-actions"><button type="button" className="inventory-remove" disabled={inventorySaving} onClick={() => handleRemoveEquipment(item)}><Trash2 size={15} /> Remove</button></div>
                            </article>
                          ))}
                        </div>
                      </section>
                    </div>
                  </>
                )}
              </section>
            </>
          );
        })()}
      </main>

      {/* Edit Project form */}
    {editingProject && (
      <div className="modal-background">
        <div className="project-modal" role="dialog" aria-modal="true" aria-label="Edit Project">
          <div className="modal-header">
            <div><h2>Edit Project</h2><p>{editingProject.name}</p></div>
            <button className="close-button" type="button" onClick={() => setEditingProject(null)}>×</button>
          </div>
          <form onSubmit={handleEditProject}>
            <label htmlFor="edit-project-name">Project Name</label>
            <input id="edit-project-name" value={projectEditForm.projectName} onChange={(e) => setProjectEditForm((v) => ({...v, projectName: e.target.value}))} required />
            <label htmlFor="edit-project-budget">Budget ($)</label>
            <input id="edit-project-budget" type="number" min="0" step="0.01" value={projectEditForm.budget} onChange={(e) => setProjectEditForm((v) => ({...v, budget: e.target.value}))} required />
            <label htmlFor="edit-project-progress">Progress (%)</label>
            <input id="edit-project-progress" type="number" min="0" max="100" step="0.1" value={projectEditForm.progress} onChange={(e) => setProjectEditForm((v) => ({...v, progress: e.target.value}))} required />
            <label htmlFor="edit-project-date">Estimated Completion Date</label>
            <input id="edit-project-date" type="date" value={projectEditForm.estimatedCompletionDate} onChange={(e) => setProjectEditForm((v) => ({...v, estimatedCompletionDate: e.target.value}))} />
            {projectActionError && <p role="alert" className="project-action-error">{projectActionError}</p>}
            <div className="form-buttons">
              <button className="cancel-button" type="button" onClick={() => setEditingProject(null)}>Cancel</button>
              <button className="create-button" type="submit" disabled={projectSaving}>{projectSaving ? "Saving..." : "Save Changes"}</button>
            </div>
          </form>
        </div>
      </div>
    )}

    {/* Change Project Status form */}
    {statusProject && (
      <div className="modal-background">
        <div className="project-modal" role="dialog" aria-modal="true" aria-label="Change Project Status">
          <div className="modal-header">
            <div><h2>Change Status</h2><p>{statusProject.name}</p></div>
            <button className="close-button" type="button" onClick={() => setStatusProject(null)}>×</button>
          </div>
          <form onSubmit={handleChangeProjectStatus}>
            <label htmlFor="project-status">Project Status</label>
            <select id="project-status" value={newStatus} onChange={(e) => setNewStatus(e.target.value)} required>
              <option value="ACTIVE">Active</option>
              <option value="ON_HOLD">On Hold</option>
              <option value="COMPLETED">Completed</option>
            </select>
            {projectActionError && <p role="alert" className="project-action-error">{projectActionError}</p>}
            <div className="form-buttons">
              <button className="cancel-button" type="button" onClick={() => setStatusProject(null)}>Cancel</button>
              <button className="create-button" type="submit" disabled={projectSaving}>{projectSaving ? "Saving..." : "Save Status"}</button>
            </div>
          </form>
        </div>
      </div>
    )}

    {/* Add / Edit Client form */}
     {showClientForm && (
       <div className="modal-background">
         <div className="project-modal">
           <div className="modal-header">
             <div><h2>{editingClientId ? "Edit Client" : "Add New Client"}</h2><p>Client contact information</p></div>
             <button type="button" className="close-button" onClick={() => setShowClientForm(false)}>×</button>
           </div>
           <form onSubmit={handleSaveClient}>
             <label htmlFor="client-name">Client Name</label>
             <input id="client-name" value={clientForm.name} onChange={(event) => setClientForm((current) => ({ ...current, name: event.target.value }))} placeholder="Example: John Smith" required />
             <label htmlFor="client-email">Email</label>
             <input id="client-email" type="email" value={clientForm.email} onChange={(event) => setClientForm((current) => ({ ...current, email: event.target.value }))} placeholder="Example: john@example.com" required />
             <label htmlFor="client-phone">Phone Number (optional)</label>
             <input id="client-phone" type="tel" value={clientForm.phoneNumber} onChange={(event) => setClientForm((current) => ({ ...current, phoneNumber: event.target.value }))} placeholder="Example: 8455551234" />
             {clientError && <p role="alert" style={{ color: "#b42318" }}>{clientError}</p>}
             <div className="form-buttons">
               <button type="button" className="cancel-button" onClick={() => setShowClientForm(false)}>Cancel</button>
               <button type="submit" className="create-button" disabled={clientSaving}>{clientSaving ? "Saving..." : editingClientId ? "Save Changes" : "Add Client"}</button>
             </div>
           </form>
         </div>
       </div>
     )}

     {/* Assign client to project */}
     {assigningProjectId && (
       <div className="modal-background">
         <div className="project-modal">
           <div className="modal-header">
             <div><h2>Assign Client</h2><p>{getProjectName(assigningProjectId)}</p></div>
             <button type="button" className="close-button" onClick={() => setAssigningProjectId(null)}>×</button>
           </div>
           <form onSubmit={handleAssignClient}>
             <label htmlFor="assign-client">Select Client</label>
             <select id="assign-client" value={selectedClientId} onChange={(event) => setSelectedClientId(event.target.value)} required>
               <option value="">Select Client</option>
               {clients.map((client) => <option key={client.clientId} value={client.clientId}>{client.name} ({client.email})</option>)}
             </select>
             {clients.length === 0 && <p>Add a client from the Clients page first.</p>}
             {assignmentError && <p role="alert" style={{ color: "#b42318" }}>{assignmentError}</p>}
             <div className="form-buttons">
               <button type="button" className="cancel-button" onClick={() => setAssigningProjectId(null)}>Cancel</button>
               <button type="submit" className="create-button" disabled={assignmentSaving || !selectedClientId}>{assignmentSaving ? "Saving..." : "Save Assignment"}</button>
             </div>
           </form>
         </div>
       </div>
     )}

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

      {/* Assign / Reassign Worker form */}

      {/* Record Work Hours form */}
     {showHoursForm && hoursWorker && (
       <div className="modal-background">
         <div className="project-modal">
           <div className="modal-header">
             <div>
               <h2>Record Work Hours</h2>
               <p>{hoursWorker.workerName}</p>
             </div>
             <button type="button" className="close-button" onClick={closeHoursForm}>
               ×
             </button>
           </div>
           <form onSubmit={handleRecordHours}>
       <label>Work Date</label>
       <input
         type="date"
         value={workDate}
         onChange={(event) => setWorkDate(event.target.value)}
         required
       />
             <label>Assigned Task</label>
             <input type="text" value={hoursWorker.taskId || ""} readOnly />
             <label>Hours Worked</label>
             <input
               type="number"
               min="0.01"
               step="any"
               value={hoursWorked}
               onChange={(event) => setHoursWorked(event.target.value)}
               placeholder="Example: 8"
               required
             />
             <div className="form-buttons">
               <button type="button" className="cancel-button" onClick={closeHoursForm}>
                 Cancel
               </button>
               <button type="submit" className="create-button" disabled={savingHours}>
                 {savingHours ? "Saving..." : "Save Hours"}
               </button>
             </div>
           </form>
         </div>
       </div>
     )}
     {showAssignmentForm && selectedWorker && (
        <div className="modal-background">
          <div className="project-modal">
            <div className="modal-header">
              <div>
                <h2>Assign / Reassign Worker</h2>
                <p>{selectedWorker.workerName}</p>
              </div>
              <button
                type="button"
                className="close-button"
                onClick={() => {
                  setShowAssignmentForm(false);
                  setSelectedWorker(null);
                }}
              >
                ×
              </button>
            </div>
            <form onSubmit={handleSaveAssignment}>
              <label>Work Area</label>
              <input
                type="text"
                placeholder="Example: Kitchen"
                value={assignment.area}
                onChange={(event) =>
                  setAssignment((current) => ({ ...current, area: event.target.value }))
                }
                required
              />
              <label>Task</label>
              <input
                type="text"
                placeholder="Example: Plumbing"
                value={assignment.task}
                onChange={(event) =>
                  setAssignment((current) => ({ ...current, task: event.target.value }))
                }
                required
              />
              <div className="form-buttons">
                <button
                  type="button"
                  className="cancel-button"
                  onClick={() => {
                    setShowAssignmentForm(false);
                    setSelectedWorker(null);
                  }}
                >
                  Cancel
                </button>
                <button type="submit" className="create-button">
                  Save Assignment
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

function App() {
  const [view, setView] = useState("welcome");

  if (view === "contractor") {
    return <ContractorApp />;
  }

  return (
    <ClientPortal
      onContractor={() => setView("contractor")}
    />
  );
}

export default App;


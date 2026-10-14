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
  const [workDate, setWorkDate] = useState("");
  const [inactiveWorkers, setInactiveWorkers] = useState([]);
  const [workerActionBusy, setWorkerActionBusy] = useState(false);
  const [workerActionError, setWorkerActionError] = useState("");
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

  const formatMoney = (value) =>
    Number(value).toLocaleString("en-US", {
      style: "currency", currency: "USD",
    });
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
                  <button
          className={activePage === "Projects" ? "nav-button active" : "nav-button"}
          onClick={() => setActivePage("Projects")}
        >
          Projects
        </button>
        <button
          className={activePage === "Clients" ? "nav-button active" : "nav-button"}
          onClick={() => setActivePage("Clients")}
        >
          Clients
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
          <button
            className={activePage === "Expenses & Budget" ? "nav-button active" : "nav-button"}
            onClick={() => setActivePage("Expenses & Budget")}
          >
            Expenses & Budget
          </button>
          <button className="nav-button">
            Change Orders
          </button>
          <button
            className={activePage === "Materials & Equipment" ? "nav-button active" : "nav-button"}
            onClick={() => setActivePage("Materials & Equipment")}
          >
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
                <h2>{projects.filter((p) => p.status === "ACTIVE").length}</h2>
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
        {/* Projects page */}
       {activePage === "Projects" && (
         <>
           <header className="dashboard-header">
             <div><h1>Projects</h1><p>Manage project details, status, and client assignments.</p></div>
             <button className="new-project-button" onClick={() => setShowProjectForm(true)}>+ New Project</button>
           </header>
           <section className="projects-section">
             <div className="section-header"><h2>Project List</h2><span>{projects.length} Projects</span></div>
             {projectActionError && <p role="alert" className="project-action-error">{projectActionError}</p>}
             {projects.length === 0 ? <p>No projects have been added yet.</p> : projects.map((project) => (
               <div className="project-card" key={project.id}>
                 <h3>{project.name}</h3>
                 <div className="project-details">
                   <div><span>Status</span><strong>{project.status}</strong></div>
                   <div><span>Progress</span><strong>{project.progress}%</strong></div>
                   <div><span>Budget</span><strong>{formatMoney(project.budget)}</strong></div>
                   <div><span>Client</span><strong>{getClientName(project.clientId)}</strong></div>
                 </div>
                 {project.status !== "ARCHIVED" ? (
                   <div className="project-actions">
                     <button className="view-button" type="button" onClick={() => openClientAssignment(project)}>{project.clientId ? "Change Client" : "Assign Client"}</button>
                     <button className="view-button" type="button" onClick={() => openEditProject(project)}>Edit Project</button>
                     <button className="view-button" type="button" onClick={() => { setProjectActionError(""); setStatusProject(project); setNewStatus(project.status); }}>Change Status</button>
                     <button className="view-button danger-action" type="button" disabled={projectSaving} onClick={() => handleArchiveProject(project)}>Archive Project</button>
                   </div>
                 ) : <p className="archived-project-note">Archived — read only</p>}
               </div>
             ))}
           </section>
         </>
       )}
       {/* Clients page */}
       {activePage === "Clients" && (
         <>
           <header className="dashboard-header">
             <div><h1>Clients</h1><p>Manage client contact information and project relationships.</p></div>
             <button className="new-project-button" onClick={() => openClientForm()}>+ Add Client</button>
           </header>
           <section className="projects-section">
             <div className="section-header"><h2>Registered Clients</h2><span>{clients.length} Clients</span></div>
             {clientError && <p role="alert" style={{ color: "#b42318" }}>{clientError}</p>}
             {clientLoading && <p>Loading clients...</p>}
             {!clientLoading && clients.length === 0 && <p>No clients have been added yet.</p>}
             {clients.map((client) => (
               <div className="project-card" key={client.clientId}>
                 <h3>{client.name}</h3>
                 <div className="project-details">
                   <div><span>Email</span><strong>{client.email}</strong></div>
                   <div><span>Phone Number</span><strong>{client.phoneNumber || "Not provided"}</strong></div>
                   <div><span>Assigned Projects</span><strong>{projects.filter((project) => project.clientId === client.clientId).length}</strong></div>
                 </div>
                 <div style={{ display: "flex", gap: "12px", flexWrap: "wrap", marginTop: "12px" }}>
                   <button className="view-button" type="button" onClick={() => openClientForm(client)}>Edit Client</button>
                   <button className="view-button" type="button" style={{ color: "#b42318" }} onClick={() => handleDeleteClient(client)}>Delete Client</button>
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
               {workerActionError && <p role="alert" style={{ color: "#b42318" }}>{workerActionError}</p>}
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
                    <div>
                      <span>Area</span>
                      <strong>{worker.area || "Not assigned"}</strong>
                    </div>
                    <div>
                      <span>Task</span>
                      <strong>{worker.taskId || "Not assigned"}</strong>
                    </div>
                    </div>
                    <div style={{ marginTop: "12px", marginBottom: "12px" }}>
                      <strong>Total Hours: {workHoursByWorker[worker.workerId]
                        ? workHoursByWorker[worker.workerId].reduce((total, entry) => total + Number(entry.hours || 0), 0).toLocaleString(undefined, { maximumFractionDigits: 2 })
                        : "Loading..."}</strong>
                    </div>
                    <button
                      type="button"
                      className="view-button"
                      onClick={() => setHistoryWorkerId((current) => current === worker.workerId ? null : worker.workerId)}
                    >
                      {historyWorkerId === worker.workerId ? "Hide Work History" : "View Work History"}
                    </button>
                    {historyWorkerId === worker.workerId && (
                      <div style={{ marginTop: "12px", marginBottom: "12px" }}>
                        <h4>Work History</h4>
                        {(workHoursByWorker[worker.workerId] || []).length === 0 ? (
                          <p>No work hours recorded yet.</p>
                        ) : (
                          <ul>
                            {workHoursByWorker[worker.workerId].map((entry) => (
								<li key={entry.id}>
								  {entry.workDate || "Date not recorded"} — {entry.taskId} — {entry.hours} hours
								</li>
                            ))}
                          </ul>
                        )}
                      </div>
                    )}
                  <button
                    type="button"
                    className="new-project-button"
                    onClick={() => openAssignmentForm(worker)}
                  >
                    {worker.area ? "Reassign Worker" : "Assign Worker"}
                  </button>
                 <button
                   type="button"
                   className="new-project-button"
                   onClick={() => openHoursForm(worker)}
                   disabled={!worker.taskId}
                   style={{ marginLeft: "10px" }}
                 >
                   Record Hours
                 </button>
                <button
                  type="button"
                  className="view-button"
                  onClick={() => handleDeleteWorker(worker)}
                  disabled={workerActionBusy}
                  style={{ marginLeft: "10px", color: "#b42318" }}
                >
                  Deactivate Worker
                </button>
                  </div>
                ))
              )}
            </section>
             <section className="projects-section" style={{ marginTop: "24px" }}>
              <div className="section-header">
                 <h2>Inactive Workers</h2>
                 <span>{inactiveWorkers.length} Workers</span>
               </div>
               {inactiveWorkers.length === 0 ? (
                 <p>No inactive workers.</p>
               ) : (
                 inactiveWorkers.map((worker) => (
                   <div className="project-card" key={worker.workerId}>
                     <h3>{worker.workerName}</h3>
                     <p>{worker.trade}</p>
                     <div className="project-details">
                       <div><span>Project</span><strong>{getProjectName(worker.projectId)}</strong></div>
                       <div><span>Area</span><strong>{worker.area || "Not assigned"}</strong></div>
                       <div><span>Task</span><strong>{worker.taskId || "Not assigned"}</strong></div>
                       <div><span>Status</span><strong>Inactive</strong></div>
                     </div>
                     <button
                       type="button"
                       className="new-project-button"
                       disabled={workerActionBusy}
                       onClick={() => handleReactivateWorker(worker)}
                       style={{ marginTop: "12px" }}
                     >
                       {workerActionBusy ? "Updating..." : "Reactivate Worker"}
                     </button>
                   </div>
                 ))
               )}
            </section>
          </>
        )}
        {activePage === "Expenses & Budget" && (
          <>
            <header className="dashboard-header">
              <div>
                <h1>Expenses & Budget</h1>
                <p>Track project spending and remaining budgets.</p>
              </div>
            </header>
            <section className="projects-section">
              <div className="section-header">
                <h2>Project Expenses</h2>
              </div>
              <label htmlFor="expense-project">Select Project</label>
              <select
                id="expense-project"
                value={expenseProjectId}
                onChange={(event) => setExpenseProjectId(event.target.value)}
              >
                {projects.length === 0 && <option value="">No projects available</option>}
                {projects.map((project) => (
                  <option key={project.id} value={project.id}>{project.name}</option>
                ))}
              </select>
              {expenseProjectId && (
                <>
                  <div className="summary-grid" style={{ marginTop: "20px" }}>
                    <div className="summary-card">
                      <p>Project Budget</p>
                      <h2>{formatMoney(projects.find((p) => p.id === expenseProjectId)?.budget ?? 0)}</h2>
                    </div>
                    <div className="summary-card">
                      <p>Total Expenses</p>
                      <h2>{formatMoney(expenseTotal)}</h2>
                    </div>
                    <div className="summary-card">
                      <p>Remaining Budget</p>
                      <h2>{formatMoney(remainingBudget)}</h2>
                    </div>
                  </div>
                  {expenseError && <p role="alert" style={{ color: "#b42318" }}>{expenseError}</p>}
                  {expenseLoading && <p>Loading expense information...</p>}
                  <div className="project-card" style={{ marginTop: "20px" }}>
                    <h3>Add Expense</h3>
                    <form onSubmit={handleAddExpense}>
                      <label htmlFor="expense-description">Description</label>
                      <input
                        id="expense-description"
                        type="text"
                        value={expenseDescription}
                        onChange={(event) => setExpenseDescription(event.target.value)}
                        placeholder="Example: Drywall Materials"
                        required
                      />
                      <label htmlFor="expense-amount">Amount ($)</label>
                      <input
                        id="expense-amount"
                        type="number"
                        min="0.01"
                        step="0.01"
                        value={expenseAmount}
                        onChange={(event) => setExpenseAmount(event.target.value)}
                        placeholder="Example: 250"
                        required
                      />
                      <div className="form-buttons">
                        <button className="create-button" type="submit" disabled={expenseSaving}>
                          {expenseSaving ? "Saving..." : "Add Expense"}
                        </button>
                      </div>
                    </form>
                  </div>
                  <div style={{ marginTop: "24px" }}>
                    <h3>Expense History</h3>
                    {!expenseLoading && projectExpenses.length === 0 ? (
                      <p>No expenses recorded for this project.</p>
                    ) : (
                      projectExpenses.map((expense) => (
                        <div className="project-card" key={expense.expenseId}>
                          <div className="project-details" style={{ justifyContent: "space-between", alignItems: "center", gap: "12px", flexWrap: "wrap" }}>
                            <strong>{expense.description}</strong>
                            <strong>{formatMoney(expense.amount)}</strong>
                            <button type="button" className="view-button" style={{ color: "#b42318" }} disabled={expenseSaving} onClick={() => handleRemoveExpense(expense)}>Remove Expense</button>
                          </div>
                        </div>
                      ))
                    )}
                  </div>
                </>
              )}
            </section>
          </>
        )}
        {activePage === "Materials & Equipment" && (
          <>
            <header className="dashboard-header">
              <div>
                <h1>Materials & Equipment</h1>
                <p>Manage materials and equipment assigned to construction projects.</p>
              </div>
            </header>
            <section className="projects-section">
              <div className="section-header"><h2>Project Inventory</h2></div>
              <label htmlFor="inventory-project">Select Project</label>
              <select
                id="inventory-project"
                value={inventoryProjectId}
                onChange={(event) => setInventoryProjectId(event.target.value)}
              >
                {projects.length === 0 && <option value="">No projects available</option>}
                {projects.map((project) => (
                  <option key={project.id} value={project.id}>{project.name}</option>
                ))}
              </select>
              {inventoryError && <p role="alert" style={{ color: "#b42318" }}>{inventoryError}</p>}
              {inventoryLoading && <p>Loading inventory...</p>}
              {inventoryProjectId && (
                <>
                  <div className="summary-grid" style={{ marginTop: "20px" }}>
                    <div className="summary-card"><p>Material Types</p><h2>{materials.length}</h2></div>
                    <div className="summary-card"><p>Equipment Items</p><h2>{equipment.length}</h2></div>
                  </div>
                  <h2 style={{ marginTop: "24px" }}>Materials</h2>
                  <form onSubmit={handleAddMaterial} style={{ marginBottom: "20px" }}>
                    <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))", gap: "16px", marginBottom: "16px" }}>
                      <div style={{ display: "flex", flexDirection: "column", gap: "8px", minWidth: 0 }}>
                        <label htmlFor="material-name">Material Name</label>
                        <input id="material-name" type="text" value={materialName} onChange={(event) => setMaterialName(event.target.value)} placeholder="Example: Drywall Sheets" style={{ width: "100%", boxSizing: "border-box" }} required />
                      </div>
                      <div style={{ display: "flex", flexDirection: "column", gap: "8px", minWidth: 0 }}>
                        <label htmlFor="material-quantity">Quantity</label>
                        <input id="material-quantity" type="number" min="0.01" step="any" value={materialQuantity} onChange={(event) => setMaterialQuantity(event.target.value)} placeholder="Example: 20" style={{ width: "100%", boxSizing: "border-box" }} required />
                      </div>
                    </div>
                    <button className="create-button" type="submit" disabled={inventorySaving}>{inventorySaving ? "Saving..." : "Add Material"}</button>
                  </form>
                  {materials.length === 0 ? <p>No materials recorded for this project.</p> : materials.map((material) => (
                    <div className="project-card" key={material.materialId}>
                      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: "12px", flexWrap: "wrap" }}>
                        <div><h3>{material.materialName}</h3><p>Quantity: {material.quantity}</p></div>
                        <div style={{ display: "flex", alignItems: "center", gap: "12px", flexWrap: "wrap" }}>
                        <button type="button" className="view-button" disabled={inventorySaving} onClick={() => handleUpdateMaterial(material)}>Update Quantity</button>
                        <button type="button" className="view-button" style={{ color: "#b42318" }} disabled={inventorySaving} onClick={() => handleRemoveMaterial(material)}>Remove Material</button>
                      </div>
                      </div>
                    </div>
                  ))}
                  <h2 style={{ marginTop: "24px" }}>Equipment</h2>
                  <form onSubmit={handleAddEquipment} style={{ marginBottom: "20px" }}>
                    <label htmlFor="equipment-name">Equipment Name</label>
                    <input id="equipment-name" value={equipmentName} onChange={(event) => setEquipmentName(event.target.value)} placeholder="Example: Power Drill" required />
                    <div style={{ marginTop: "12px" }}><button className="create-button" type="submit" disabled={inventorySaving}>{inventorySaving ? "Saving..." : "Add Equipment"}</button></div>
                  </form>
                  {equipment.length === 0 ? <p>No equipment recorded for this project.</p> : equipment.map((item) => (
                    <div className="project-card" key={item.equipmentId}>
                      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: "12px", flexWrap: "wrap" }}>
                        <h3>{item.equipmentName}</h3>
                        <button type="button" className="view-button" disabled={inventorySaving} onClick={() => handleRemoveEquipment(item)}>Remove Equipment</button>
                      </div>
                    </div>
                  ))}
                </>
              )}
            </section>
          </>
        )}
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
export default App;

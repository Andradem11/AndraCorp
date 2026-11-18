import { useEffect, useState } from "react";
import "./ClientChangeOrders.css";

const API = "http://localhost:7070/api";
const money = (value) => Number(value || 0).toLocaleString("en-US", {
  style: "currency", currency: "USD"
});

export default function ClientChangeOrders({ clientId, projects }) {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [form, setForm] = useState({
    projectId: "", description: ""
  });
  const [requestType, setRequestType] = useState("MATERIAL");
  const [workers, setWorkers] = useState([]);
  const [workersError, setWorkersError] = useState("");
  const [workforce, setWorkforce] = useState({
    action: "ADD", fromArea: "", toArea: "", count: "", reason: ""
  });
  const [materialPreference, setMaterialPreference] = useState("");
  const [decliningId, setDecliningId] = useState("");
  const [reason, setReason] = useState("");
  const [counterId, setCounterId] = useState("");
  const [suggestion, setSuggestion] = useState("");
  const [historyId, setHistoryId] = useState("");
  const [history, setHistory] = useState([]);
  const [historyLoading, setHistoryLoading] = useState(false);

  useEffect(() => {
    let active = true;
    async function loadWorkers() {
      try {
        const response = await fetch(`${API}/workers`);
        if (!response.ok) throw new Error("Could not load worker assignments.");
        const data = await response.json();
        if (!Array.isArray(data)) throw new Error("Invalid worker response.");
        if (active) setWorkers(data);
      } catch (err) {
        if (active) setWorkersError(err.message);
      }
    }
    loadWorkers();
    return () => { active = false; };
  }, []);

  const projectWorkers = workers.filter((worker) => worker.projectId === form.projectId);
  const workAreas = [...new Set(projectWorkers.map((worker) =>
    String(worker.area || "").trim()
  ).filter(Boolean))];
  const canReallocate = workAreas.length >= 2;

  const activeProjects = projects.filter((project) => project.status === "ACTIVE");

  async function request(url, options) {
    const response = await fetch(url, options);
    const data = await response.json();
    if (!response.ok) throw new Error(data.error || "Request failed.");
    return data;
  }

  function post(data) {
    return {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data)
    };
  }

  async function loadOrders() {
    setLoading(true);
    setError("");
    try {
      const results = await Promise.all(projects.map((project) =>
        request(`${API}/projects/${encodeURIComponent(project.projectId)}/change-orders`)
      ));
      setOrders(results.flat().filter((order) =>
        projects.some((project) => project.projectId === order.projectId)
      ));
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (projects.length) loadOrders();
    else setOrders([]);
  }, [projects]);

  async function createOrder(event) {
    event.preventDefault();
    if (!clientId || !form.projectId) return;
    if (requestType === "MATERIAL" && !form.description.trim()) return;
    if (requestType === "WORKFORCE" && !workforce.reason.trim()) return;
    if (requestType === "WORKFORCE" && (!Number.isInteger(Number(workforce.count)) || Number(workforce.count) < 1)) {
      setError("Enter a valid number of workers.");
      return;
    }
    if (requestType === "WORKFORCE" && workforce.action === "MOVE" && canReallocate &&
        (!workforce.fromArea || !workforce.toArea || workforce.fromArea === workforce.toArea)) {
      setError("Choose two different work areas.");
      return;
    }
    const description = requestType === "MATERIAL"
      ? `${form.description.trim()}${materialPreference.trim() ? `\nPreferred material: ${materialPreference.trim()}` : ""}`
      : workforce.action === "MOVE" && canReallocate
        ? `Workforce request: Move ${workforce.count} worker(s) from ${workforce.fromArea} to ${workforce.toArea}.\nReason: ${workforce.reason.trim()}\nContractor to determine appropriate trades, cost, and schedule impact.`
        : `Workforce request: Add ${workforce.count} worker(s)${workforce.toArea ? ` to ${workforce.toArea}` : ""}.\nReason: ${workforce.reason.trim()}\nContractor to determine appropriate trades, cost, and schedule impact.`;
    setBusy(true);
    setError("");
    setMessage("");
    try {
      const requestData = {
        projectId: form.projectId,
        requestedBy: "CLIENT",
        description,
        changeType: requestType
      };

      if (requestType === "MATERIAL") {
        requestData.materialPreference = materialPreference.trim() || null;
      } else {
        const isReallocation = workforce.action === "MOVE" && canReallocate;
        requestData.workforceAction = isReallocation ? "REALLOCATE" : "ADD_WORKERS";
        requestData.requestedWorkerCount = Number(workforce.count);

        if (isReallocation) {
          requestData.sourceArea = workforce.fromArea;
          requestData.targetArea = workforce.toArea;
        } else if (workforce.toArea) {
          requestData.targetArea = workforce.toArea;
        }
      }

      await request(`${API}/change-orders`, post(requestData));
      // Cost estimates are not entered by clients. The backend will calculate
      // them once sufficient project pricing data is available.
      setMessage("Change request submitted. Waiting for contractor review.");
      setForm({ projectId: "", description: "" });
      setMaterialPreference("");
      setWorkforce({ action: "ADD", fromArea: "", toArea: "", count: "", reason: "" });
      await loadOrders();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function decide(orderId, action) {
    if (!clientId || busy) return;
    if (action === "decline" && !reason.trim()) {
      setError("Please explain why you declined the proposal.");
      return;
    }
    setBusy(true);
    setError("");
    setMessage("");
    try {
      await request(`${API}/change-orders/${encodeURIComponent(orderId)}/${action}`, post({
        clientId,
        ...(action === "decline" ? { reason: reason.trim() } : {})
      }));
      setDecliningId("");
      setReason("");
      setMessage(action === "approve" ? "Proposal approved." : "Proposal declined. Your reason was saved.");
      await loadOrders();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function submitCounterproposal(orderId) {
    if (!clientId || !suggestion.trim() || busy) return;
    setBusy(true);
    setError("");
    setMessage("");
    try {
      await request(`${API}/change-orders/${encodeURIComponent(orderId)}/counterproposal`, post({
        clientId,
        suggestion: suggestion.trim()
      }));
      setCounterId("");
      setSuggestion("");
      setMessage("Counterproposal sent to your contractor for review.");
      await loadOrders();
      if (historyId === orderId) await showHistory(orderId);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function showHistory(orderId) {
    if (historyId === orderId) {
      setHistoryId("");
      return;
    }
    setHistoryLoading(true);
    setError("");
    try {
      const entries = await request(`${API}/change-orders/${encodeURIComponent(orderId)}/history`);
      setHistory(entries);
      setHistoryId(orderId);
    } catch (err) {
      setError(err.message);
    } finally {
      setHistoryLoading(false);
    }
  }

  const projectName = (id) => projects.find((project) => project.projectId === id)?.projectName || id;
  const statusLabel = (status) => ({
    PENDING_REVIEW: "Waiting for Contractor",
    PENDING_APPROVAL: "Your Decision Needed",
    APPROVED: "Approved",
    DECLINED: "Declined",
    COUNTERPROPOSAL_PENDING: "Contractor Reviewing Your Suggestion"
  }[status] || status);

  return (
    <section className="cco-page">
      <header className="cco-header">
        <div>
          <h1>Change Orders</h1>
          <p>Request changes and review prices confirmed by your contractor.</p>
        </div>
        <button type="button" onClick={loadOrders} disabled={loading || busy}>
          {loading ? "Loading..." : "Refresh Orders"}
        </button>
      </header>

      {error && <p className="cco-error" role="alert">{error}</p>}
      {message && <p className="cco-success" role="status">{message}</p>}

      <div className="cco-panel">
        <h2>Request a Change</h2>
        <form className="cco-form" onSubmit={createOrder}>
          <label htmlFor="cco-project">Project</label>
          <select id="cco-project" required value={form.projectId}
            onChange={(event) => setForm({ ...form, projectId: event.target.value })}>
            <option value="">Select a project</option>
            {activeProjects.map((project) => (
              <option key={project.projectId} value={project.projectId}>{project.projectName}</option>
            ))}
          </select>
          <label htmlFor="cco-request-type">Change Order Type</label>
          <select id="cco-request-type" value={requestType}
            onChange={(event) => setRequestType(event.target.value)}>
            <option value="MATERIAL">Materials / Project Changes</option>
            <option value="WORKFORCE">Workforce / Scheduling Changes</option>
          </select>

          {requestType === "MATERIAL" ? (
            <>
              <label htmlFor="cco-description">Describe the change</label>
              <textarea id="cco-description" required rows={3} value={form.description}
                onChange={(event) => setForm({ ...form, description: event.target.value })}
				placeholder="" />
              <label htmlFor="cco-preference">Preferred material or product (optional)</label>
              <input id="cco-preference" value={materialPreference}
                onChange={(event) => setMaterialPreference(event.target.value)}
                placeholder="Product name or link; leave blank for contractor to choose" />
            </>
          ) : (
            <>
              {workersError && <p className="cco-error">{workersError}</p>}
              {form.projectId && <p className="cco-note">
                Assigned work areas: {workAreas.length ? workAreas.join(", ") : "Not available yet"}.
                {workAreas.length < 2 ? " You can request additional workers." : " You can request additional workers or move workers between areas."}
              </p>}
              <label htmlFor="cco-worker-action">Workforce request</label>
              <select id="cco-worker-action" value={canReallocate ? workforce.action : "ADD"}
                onChange={(event) => setWorkforce({ ...workforce, action: event.target.value })}>
                <option value="ADD">Request additional workers</option>
                {canReallocate && <option value="MOVE">Move existing workers between areas</option>}
              </select>
              {canReallocate && workforce.action === "MOVE" && (
                <>
                  <label htmlFor="cco-from-area">Move workers from</label>
                  <select id="cco-from-area" required value={workforce.fromArea}
                    onChange={(event) => setWorkforce({ ...workforce, fromArea: event.target.value })}>
                    <option value="">Select current area</option>
                    {workAreas.map((area) => <option key={area} value={area}>{area}</option>)}
                  </select>
                </>
              )}
              {workAreas.length > 0 && (
                <>
                  <label htmlFor="cco-to-area">{canReallocate && workforce.action === "MOVE" ? "Move workers to" : "Work area"}</label>
                  <select id="cco-to-area" required={canReallocate && workforce.action === "MOVE"}
                    value={workforce.toArea}
                    onChange={(event) => setWorkforce({ ...workforce, toArea: event.target.value })}>
                    <option value="">{canReallocate && workforce.action === "MOVE" ? "Select destination" : "Contractor decides / general project"}</option>
                    {workAreas.map((area) => <option key={area} value={area}>{area}</option>)}
                  </select>
                </>
              )}
              <label htmlFor="cco-worker-count">Number of workers requested</label>
              <input id="cco-worker-count" type="number" min="1" step="1" required
                value={workforce.count} onChange={(event) => setWorkforce({ ...workforce, count: event.target.value })}
                placeholder="" />
              <label htmlFor="cco-worker-reason">What would you like to improve?</label>
              <textarea id="cco-worker-reason" required rows={3} value={workforce.reason}
                onChange={(event) => setWorkforce({ ...workforce, reason: event.target.value })}
                placeholder="" />
            </>
          )}
          <button type="submit" disabled={busy || !activeProjects.length || !clientId}>
            {busy ? "Saving..." : "Submit Change Request"}
          </button>
          {!activeProjects.length && <p>No active projects are available for requests.</p>}
        </form>
      </div>

      <div className="cco-panel">
        <h2>My Change Orders</h2>
        {loading && <p>Loading change orders...</p>}
        {!loading && orders.length === 0 && <p>No change orders yet.</p>}
        {orders.map((order) => (
          <article className="cco-order" key={order.changeOrderId}>
            <div className="cco-order-heading">
              <div>
                <h3>{projectName(order.projectId)}</h3>
                <small>Requested by {order.requestedBy === "CLIENT" ? "Client" : "Contractor"}</small>
              </div>
              <span className="cco-status">{statusLabel(order.status)}</span>
            </div>
            <p>{order.description}</p>
            {order.requestedBy === "CLIENT" && order.simulatedCostImpact != null &&
              (order.laborCostChange !== 0 || order.materialCostChange !== 0 ||
               order.equipmentCostChange !== 0) && (
              <p><strong>System simulation:</strong> {money(order.simulatedCostImpact)}</p>
            )}
            {order.requestedBy === "CLIENT" && order.status === "PENDING_REVIEW" &&
              (order.laborCostChange === 0 && order.materialCostChange === 0 &&
               order.equipmentCostChange === 0) && (
                <p><strong>System estimate:</strong> Pending pricing data / contractor review</p>
              )}
            {order.proposedSolution && <p><strong>Contractor proposal:</strong> {order.proposedSolution}</p>}
            {order.status !== "PENDING_REVIEW" && order.proposedSolution && (
              <>
                <p><strong>Contractor-confirmed cost change:</strong> {money(order.finalCostImpact)}</p>
                <p><strong>Schedule impact:</strong> {order.estimatedScheduleImpact || 0} days</p>
              </>
            )}
            {order.status === "DECLINED" && order.declineReason && (
              <p><strong>Decline reason:</strong> {order.declineReason}</p>
            )}
            {order.status === "PENDING_APPROVAL" && (
              <div className="cco-actions">
                <button type="button" disabled={busy} onClick={() => decide(order.changeOrderId, "approve")}>Approve</button>
                <button type="button" className="cco-secondary" disabled={busy}
                  onClick={() => { setDecliningId(order.changeOrderId); setReason(""); }}>
                  Decline
                </button>
                <button type="button" className="cco-secondary" disabled={busy}
                  onClick={() => { setCounterId(order.changeOrderId); setSuggestion(""); setDecliningId(""); }}>
                  Suggest Another Option
                </button>
              </div>
            )}
            {counterId === order.changeOrderId && order.status === "PENDING_APPROVAL" && (
              <div className="cco-decline">
                <label htmlFor={`counter-${order.changeOrderId}`}>What would you like to change?</label>
                <textarea id={`counter-${order.changeOrderId}`} rows={3} value={suggestion}
                  placeholder="Example: Could we use these 4-inch lights instead?"
                  onChange={(event) => setSuggestion(event.target.value)} />
                <div className="cco-actions">
                  <button type="button" disabled={busy || !suggestion.trim()}
                    onClick={() => submitCounterproposal(order.changeOrderId)}>Send Counterproposal</button>
                  <button type="button" className="cco-secondary" disabled={busy}
                    onClick={() => setCounterId("")}>Cancel</button>
                </div>
              </div>
            )}
            <div className="cco-actions">
              <button type="button" className="cco-secondary" disabled={historyLoading}
                onClick={() => showHistory(order.changeOrderId)}>
                {historyId === order.changeOrderId ? "Hide History" : "View Negotiation History"}
              </button>
            </div>
            {historyId === order.changeOrderId && (
              <div className="cco-decline">
                <h4>Negotiation History</h4>
                {history.length === 0 && <p>No recorded actions yet.</p>}
                {history.map((entry) => (
                  <div key={entry.historyId}>
                    <strong>{entry.actorRole} — {entry.action.replaceAll("_", " ")}</strong>
                    <p>Version {entry.proposalVersion}: {entry.message || "No message"}</p>
                    {entry.costImpact != null && <p>Cost change: {money(entry.costImpact)}</p>}
                    {entry.scheduleImpact != null && <p>Schedule change: {entry.scheduleImpact} days</p>}
                    <small>{entry.createdAt}</small>
                  </div>
                ))}
              </div>
            )}
            {decliningId === order.changeOrderId && order.status === "PENDING_APPROVAL" && (
              <div className="cco-decline">
                <label htmlFor={`reason-${order.changeOrderId}`}>Why are you declining?</label>
                <textarea id={`reason-${order.changeOrderId}`} rows={3} value={reason}
                  onChange={(event) => setReason(event.target.value)} />
                <div className="cco-actions">
                  <button type="button" disabled={busy || !reason.trim()}
                    onClick={() => decide(order.changeOrderId, "decline")}>Send Decline</button>
                  <button type="button" className="cco-secondary" disabled={busy}
                    onClick={() => setDecliningId("")}>Cancel</button>
                </div>
              </div>
            )}
          </article>
        ))}
      </div>
    </section>
  );
}

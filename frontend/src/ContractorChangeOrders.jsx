import { useEffect, useState } from "react";
import "./ContractorChangeOrders.css";

const API = "http://localhost:7070/api";
const money = (amount) => Number(amount || 0).toLocaleString("en-US", {
  style: "currency", currency: "USD"
});

export default function ContractorChangeOrders({ projects, contractorId }) {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [busy, setBusy] = useState(false);
  const [historyId, setHistoryId] = useState("");
  const [history, setHistory] = useState([]);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [newOrder, setNewOrder] = useState({ projectId: "", description: "" });
  const [proposalId, setProposalId] = useState("");
  const [tradeRates, setTradeRates] = useState([]);
  const [workforceEstimate, setWorkforceEstimate] = useState({ trade: "", workerCount: "", hoursPerWorker: "" });
  const [estimateResult, setEstimateResult] = useState(null);
  const [availableWorkers, setAvailableWorkers] = useState([]);
  const [reallocation, setReallocation] = useState({ workerIds: [], hoursPerWorker: "", hireTrade: "",
    sourceRemainingWorkerHours: "", targetRemainingWorkerHours: "", productiveHoursPerWorkerDay: "8" });
  const [comparison, setComparison] = useState(null);
  const [proposal, setProposal] = useState({ proposedSolution: "", finalCostImpact: "", scheduleImpact: "0" });

  const eligibleProjects = projects.filter((project) => project.status === "ACTIVE" && project.clientId);

  async function request(url, options) {
    const response = await fetch(url, options);
    const data = await response.json();
    if (!response.ok) throw new Error(data.error || "Request failed.");
    return data;
  }

  const jsonPost = (data) => ({
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data)
  });

  async function loadOrders() {
    setLoading(true);
    setError("");
    try {
      const results = await Promise.all(projects.map((project) =>
        request(`${API}/projects/${encodeURIComponent(project.id)}/change-orders`)
      ));
      setOrders(results.flat());
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    request(`${API}/trade-rates`).then(setTradeRates).catch(() => setTradeRates([]));
  }, []);

  useEffect(() => {
    if (projects.length) loadOrders();
    else setOrders([]);
  }, [projects]);

  async function createOrder(event) {
    event.preventDefault();
    if (!newOrder.projectId || !newOrder.description.trim()) return;
    setBusy(true);
    setError("");
    setMessage("");
    try {
      const order = await request(`${API}/change-orders`, jsonPost({
        projectId: newOrder.projectId,
        requestedBy: "CONTRACTOR",
        description: newOrder.description.trim()
      }));
      setNewOrder({ projectId: "", description: "" });
      setProposalId(order.changeOrderId);
    setEstimateResult(null);
    setWorkforceEstimate({ trade: "", workerCount: String(order.requestedWorkerCount || ""), hoursPerWorker: "" });
      setProposal({ proposedSolution: "", finalCostImpact: "", scheduleImpact: "0" });
      setMessage("Order created. Enter the final price and submit the proposal below.");
      await loadOrders();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function calculateWorkforceEstimate() {
    setBusy(true);
    setError("");
    setEstimateResult(null);
    try {
      const result = await request(`${API}/change-orders/${encodeURIComponent(proposalId)}/workforce-estimate`, jsonPost({
        contractorId,
        trade: workforceEstimate.trade,
        workerCount: Number(workforceEstimate.workerCount),
        hoursPerWorker: Number(workforceEstimate.hoursPerWorker)
      }));
      setEstimateResult(result);
      setProposal((current) => ({ ...current, finalCostImpact: String(result.estimatedLaborCost) }));
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function compareReallocation() {
    setBusy(true);
    setError("");
    setComparison(null);
    try {
      const result = await request(`${API}/change-orders/${encodeURIComponent(proposalId)}/reallocation-compare`, jsonPost({
        contractorId, workerIds: reallocation.workerIds,
        hoursPerWorker: Number(reallocation.hoursPerWorker), hireTrade: reallocation.hireTrade,
        sourceRemainingWorkerHours: reallocation.sourceRemainingWorkerHours === "" ? null : Number(reallocation.sourceRemainingWorkerHours),
        targetRemainingWorkerHours: reallocation.targetRemainingWorkerHours === "" ? null : Number(reallocation.targetRemainingWorkerHours),
        productiveHoursPerWorkerDay: reallocation.productiveHoursPerWorkerDay === "" ? null : Number(reallocation.productiveHoursPerWorkerDay)
      }));
      setComparison(result);
    } catch (err) { setError(err.message); }
    finally { setBusy(false); }
  }

  useEffect(() => {
    request(`${API}/workers`).then((data) => setAvailableWorkers(Array.isArray(data) ? data : []))
      .catch(() => setAvailableWorkers([]));
  }, [proposalId]);

  async function submitProposal(event) {
    event.preventDefault();
    if (!proposalId || !proposal.proposedSolution.trim() || proposal.finalCostImpact === "") return;
    setBusy(true);
    setError("");
    setMessage("");
    try {
      await request(`${API}/change-orders/${encodeURIComponent(proposalId)}/proposal`, jsonPost({
        contractorId,
        proposedSolution: proposal.proposedSolution.trim(),
        finalCostImpact: Number(proposal.finalCostImpact),
        scheduleImpact: Number(proposal.scheduleImpact)
      }));
      setProposalId("");
      setMessage("Final proposal submitted. Waiting for the client's decision.");
      await loadOrders();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  const projectName = (id) => projects.find((p) => p.id === id)?.name || id;
  const pending = orders.filter((o) =>
    o.status === "PENDING_REVIEW" || o.status === "COUNTERPROPOSAL_PENDING"
  );

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

  function historyPanel(orderId) {
    return (
      <div>
        <button type="button" className="co-secondary" disabled={historyLoading}
          onClick={() => showHistory(orderId)}>
          {historyId === orderId ? "Hide History" : "View Negotiation History"}
        </button>
        {historyId === orderId && (
          <div className="co-panel">
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
      </div>
    );
  }

  function startProposal(order) {
    setProposalId(order.changeOrderId);
    setEstimateResult(null);
    setWorkforceEstimate({ trade: "", workerCount: String(order.requestedWorkerCount || ""), hoursPerWorker: "" });
    setProposal({
      proposedSolution: order.proposedSolution || "",
      finalCostImpact: order.simulatedCostImpact != null && order.requestedBy === "CLIENT"
        ? String(order.simulatedCostImpact) : "",
      scheduleImpact: String(order.estimatedScheduleImpact || 0)
    });
    setMessage("");
    setError("");
  }

  return (
    <section className="co-page">
      <header className="co-header">
        <div>
          <h1>CHANGE ORDERS</h1>
          <p>Review client requests, confirm pricing, and send proposals.</p>
        </div>
        <button type="button" onClick={loadOrders} disabled={loading || busy}>
          {loading ? "Loading..." : "Refresh Orders"}
        </button>
      </header>

      {error && <p className="co-error" role="alert">{error}</p>}
      {message && <p className="co-message" role="status">{message}</p>}

      <div className="co-grid">
        <section className="co-panel">
          <h2>Create Contractor Change Order</h2>
          <form onSubmit={createOrder} className="co-form">
            <label htmlFor="co-project">Project</label>
            <select id="co-project" value={newOrder.projectId} required
              onChange={(e) => setNewOrder({ ...newOrder, projectId: e.target.value })}>
              <option value="">Select project</option>
              {eligibleProjects.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
            </select>
            <label htmlFor="co-description">Description</label>
            <textarea id="co-description" rows={4} required value={newOrder.description}
              onChange={(e) => setNewOrder({ ...newOrder, description: e.target.value })}
              placeholder="Describe the proposed project change" />
            <button type="submit" disabled={busy || !eligibleProjects.length}>Create Order</button>
          </form>
          {!eligibleProjects.length && <p>Assign a client to an active project to create an order.</p>}
        </section>

        <section className="co-panel">
          <h2>Contractor Proposal</h2>
          {proposalId ? (
            <form className="co-form" onSubmit={submitProposal}>
              <p>Order: {proposalId}</p>
              {orders.find((o) => o.changeOrderId === proposalId)?.changeType === "WORKFORCE" &&
                orders.find((o) => o.changeOrderId === proposalId)?.workforceAction === "ADD_WORKERS" && (
                <div className="co-panel">
                  <h3>Additional Labor Estimate</h3>
                  <p>Select the trade and estimate hours per worker. This uses your saved trade rates.</p>
                  <label htmlFor="co-trade">Trade</label>
                  <select id="co-trade" value={workforceEstimate.trade}
                    onChange={(e) => { setWorkforceEstimate({ ...workforceEstimate, trade: e.target.value }); setEstimateResult(null); }}>
                    <option value="">Select trade</option>
                    {tradeRates.map((r) => <option key={r.trade} value={r.trade}>{r.trade} — {money(r.hourlyRate)}/hour</option>)}
                  </select>
                  <label htmlFor="co-worker-count">Number of workers</label>
                  <input id="co-worker-count" type="number" min="1" step="1" value={workforceEstimate.workerCount}
                    onChange={(e) => { setWorkforceEstimate({ ...workforceEstimate, workerCount: e.target.value }); setEstimateResult(null); setProposal((current) => ({ ...current, finalCostImpact: "" })); }} />
                  <label htmlFor="co-worker-hours">Estimated hours per worker</label>
                  <input id="co-worker-hours" type="number" min="0.25" step="0.25" value={workforceEstimate.hoursPerWorker}
                    onChange={(e) => { setWorkforceEstimate({ ...workforceEstimate, hoursPerWorker: e.target.value }); setEstimateResult(null); setProposal((current) => ({ ...current, finalCostImpact: "" })); }} />
                  <button type="button" disabled={busy || !workforceEstimate.trade ||
                    !(Number(workforceEstimate.workerCount) > 0) || !(Number(workforceEstimate.hoursPerWorker) > 0)}
                    onClick={calculateWorkforceEstimate}>Calculate Labor Estimate</button>
                  {estimateResult && <p><strong>Estimated additional labor: {money(estimateResult.estimatedLaborCost)}</strong>
                    <br />{estimateResult.workerCount} workers × {estimateResult.hoursPerWorker} hours × {money(estimateResult.hourlyRate)}/hour
                    <br />Estimate only. No actual expenses recorded.</p>}
                </div>
              )}
              {orders.find((o) => o.changeOrderId === proposalId)?.workforceAction === "REALLOCATE" && (
                <div className="co-panel">
                  <h3>Compare Worker Reallocation vs. Additional Hiring</h3>
                  <p>Choose qualified workers currently assigned to the source area. This is a read-only comparison.</p>
                  <p>From: {orders.find((o) => o.changeOrderId === proposalId)?.sourceArea} →
                    {" "}{orders.find((o) => o.changeOrderId === proposalId)?.targetArea}</p>
                  {availableWorkers.filter((w) => w.projectId === orders.find((o) => o.changeOrderId === proposalId)?.projectId &&
                    w.area === orders.find((o) => o.changeOrderId === proposalId)?.sourceArea).map((w) => (
                    <label key={w.workerId} style={{ display: "block" }}>
                      <input type="checkbox" checked={reallocation.workerIds.includes(w.workerId)}
                        onChange={(e) => { setComparison(null); setReallocation((current) => ({ ...current,
                          workerIds: e.target.checked ? [...current.workerIds, w.workerId] : current.workerIds.filter((id) => id !== w.workerId)
                        })); }} /> {w.workerName} — {w.trade} ({money(w.hourlyRate)}/hr)
                    </label>
                  ))}
                  <label>Estimated hours per worker</label>
                  <input type="number" min="0.25" step="0.25" value={reallocation.hoursPerWorker}
                    onChange={(e) => { setComparison(null); setReallocation({ ...reallocation, hoursPerWorker: e.target.value }); }} />
                  <label>Trade to hire instead</label>
                  <select value={reallocation.hireTrade} onChange={(e) => { setComparison(null); setReallocation({ ...reallocation, hireTrade: e.target.value }); }}>
                    <option value="">Select trade</option>
                    {tradeRates.map((r) => <option key={r.trade} value={r.trade}>{r.trade} — {money(r.hourlyRate)}/hr</option>)}
                  </select>
                  <p><strong>Optional schedule scenario assumptions</strong> (remaining effort, not days). All workers are assumed equally productive and qualified.</p>
                  <label>Source area remaining worker-hours
                    <input type="number" min="0" step="0.25" value={reallocation.sourceRemainingWorkerHours}
                      onChange={(e) => { setComparison(null); setReallocation({ ...reallocation, sourceRemainingWorkerHours: e.target.value }); }} />
                  </label>
                  <label>Target area remaining worker-hours
                    <input type="number" min="0" step="0.25" value={reallocation.targetRemainingWorkerHours}
                      onChange={(e) => { setComparison(null); setReallocation({ ...reallocation, targetRemainingWorkerHours: e.target.value }); }} />
                  </label>
                  <label>Productive hours per worker per day
                    <input type="number" min="0.25" max="24" step="0.25" value={reallocation.productiveHoursPerWorkerDay}
                      onChange={(e) => { setComparison(null); setReallocation({ ...reallocation, productiveHoursPerWorkerDay: e.target.value }); }} />
                  </label>
                  <button type="button" disabled={busy || !reallocation.hireTrade || !(Number(reallocation.hoursPerWorker) > 0) ||
                    reallocation.workerIds.length !== Number(orders.find((o) => o.changeOrderId === proposalId)?.requestedWorkerCount)}
                    onClick={compareReallocation}>Compare Options</button>
                  {comparison && <div>
                    <p><strong>Move existing workers:</strong> {money(comparison.reallocationAdditionalLaborCost)} additional labor cost.
                      Existing scheduled labor value: {money(comparison.existingLaborAllocationValue)} (not new spending).</p>
                    <p><strong>Hire additional workers:</strong> {money(comparison.hiringAdditionalLaborCost)} estimated additional labor.</p>
                    {comparison.scheduleScenario && (() => {
                      const s = comparison.scheduleScenario;
                      const days = (v) => v == null ? "Unavailable" : `${Number(v).toFixed(2)} days`;
                      return <div>
                        <p><strong>Baseline:</strong> {comparison.sourceArea}: {days(s.baselineSourceDays)}; {comparison.targetArea}: {days(s.baselineTargetDays)}</p>
                        <p><strong>Move workers:</strong> {comparison.sourceArea}: {days(s.moveSourceDays)}; {comparison.targetArea}: {days(s.moveTargetDays)}</p>
                        <p><strong>Hire workers:</strong> {comparison.sourceArea}: {days(s.hireSourceDays)}; {comparison.targetArea}: {days(s.hireTargetDays)}</p>
                        <p><strong>Estimated target improvement:</strong> {days(s.targetDaysSaved)}. <strong>Source delay if moved:</strong> {days(s.sourceDaysDelayed)}.</p>
                        <p><em>{s.assumptions}</em></p>
                      </div>;
                    })()}
                    <p>{comparison.scheduleImpact}</p>
                    <p>No actual workers or expenses changed. Describe your chosen option in the proposal below.</p>
                  </div>}
                </div>
              )}
              <label htmlFor="co-solution">Proposed solution</label>
              <textarea id="co-solution" rows={3} required value={proposal.proposedSolution}
                onChange={(e) => setProposal({ ...proposal, proposedSolution: e.target.value })} />
              <label htmlFor="co-price">Final cost change ($)</label>
              <input id="co-price" type="number" step="0.01" required value={proposal.finalCostImpact}
                onChange={(e) => setProposal({ ...proposal, finalCostImpact: e.target.value })} />
              <label htmlFor="co-days">Schedule change (days)</label>
              <input id="co-days" type="number" step="1" required value={proposal.scheduleImpact}
                onChange={(e) => setProposal({ ...proposal, scheduleImpact: e.target.value })} />
              <div className="co-actions">
                <button type="submit" disabled={busy}>Send to Client</button>
                <button type="button" className="co-secondary" onClick={() => setProposalId("")}>Cancel</button>
              </div>
            </form>
          ) : <p></p>}
        </section>
      </div>

      <section className="co-panel">
        <h2>Waiting for Contractor Review ({pending.length})</h2>
        {pending.length === 0 && <p>No requests waiting for pricing.</p>}
        {pending.map((order) => (
          <article className="co-order" key={order.changeOrderId}>
            <div className="co-order-heading">
              <strong>{projectName(order.projectId)}</strong>
              <span>{order.requestedBy === "CLIENT" ? "Client request" : "Contractor request"}</span>
            </div>
            <p>{order.description}</p>
            {order.requestedBy === "CLIENT" && (
              <p>Simulated cost: {order.simulatedCostImpact == null ? "Not calculated yet" : money(order.simulatedCostImpact)}</p>
            )}
            {order.status === "COUNTERPROPOSAL_PENDING" &&
              <p><strong>Client suggested another option. Review the history before repricing.</strong></p>}
            <button type="button" onClick={() => startProposal(order)}>
              {order.status === "COUNTERPROPOSAL_PENDING" ? "Submit Revised Proposal" : "Set Final Price"}
            </button>
            {historyPanel(order.changeOrderId)}
          </article>
        ))}
      </section>

      <section className="co-panel">
        <h2>All Change Orders ({orders.length})</h2>
        {orders.length === 0 && !loading && <p>No change orders yet.</p>}
        {orders.filter((o) => o.status !== "PENDING_REVIEW").map((order) => (
          <article className="co-order" key={order.changeOrderId}>
            <div className="co-order-heading">
              <strong>{projectName(order.projectId)}</strong>
              <span>{order.status.replaceAll("_", " ")}</span>
            </div>
            <p>{order.description}</p>
            {order.proposedSolution && <p>Proposal: {order.proposedSolution}</p>}
            <p>Final cost change: {money(order.finalCostImpact ?? order.estimatedCostImpact)}</p>
            <p>Schedule change: {order.estimatedScheduleImpact || 0} days</p>
            {order.declineReason && <p>Client's reason: {order.declineReason}</p>}
            {historyPanel(order.changeOrderId)}
          </article>
        ))}
      </section>
    </section>
  );
}


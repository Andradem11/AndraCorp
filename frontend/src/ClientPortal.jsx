
import { useState } from "react";
import { ArrowLeft, UserRound, HardHat } from "lucide-react";
import "./ClientPortal.css";

function ClientPortal({ onContractor }) {
  const [page, setPage] = useState("select");
  const [name, setName] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [client, setClient] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

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
    setError("");
    setPage("select");
  };

  return (
    <div className="client-portal">
      <div className="client-login-card">
        <div className="client-brand">
          <img
            src="/images/andracorp-logo.png"
            alt="AndraCorp"
          />
          <h1>ANDRACORP</h1>
          <p>Construction Project Management</p>
        </div>

        {page === "select" && (
          <>
            <h2>Welcome to AndraCorp</h2>
            <p>Select how you want to continue.</p>

            <div className="client-role-options">
              <button onClick={onContractor}>
                <HardHat size={28} />
                <strong>Contractor</strong>
                <span>Manage construction projects</span>
              </button>

              <button onClick={() => setPage("login")}>
                <UserRound size={28} />
                <strong>Client</strong>
                <span>View your project information</span>
              </button>
            </div>
          </>
        )}

        {page === "login" && (
          <>
            <button
              className="client-back"
              onClick={() => {
                setError("");
                setPage("select");
              }}
            >
              <ArrowLeft size={17} />
              Back
            </button>

            <h2>Client Sign In</h2>
            <p>Enter the information your contractor registered.</p>

            <form onSubmit={handleLogin}>
              <label htmlFor="client-name">Full Name</label>
              <input
                id="client-name"
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Enter your full name"
                required
              />

              <label htmlFor="client-phone">Phone Number</label>
              <input
                id="client-phone"
                type="tel"
                value={phoneNumber}
                onChange={(e) => setPhoneNumber(e.target.value)}
                placeholder="Enter your phone number"
                required
              />

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
                {loading ? "Checking..." : "Sign In"}
              </button>
            </form>
          </>
        )}

        {page === "dashboard" && client && (
          <>
            <h2>Welcome, {client.name}!</h2>
            <p>Your client account was found successfully.</p>

            <div className="client-welcome-message">
              Your project dashboard will be added next.
            </div>

            <button
              className="client-submit"
              onClick={signOut}
            >
              Sign Out
            </button>
          </>
        )}
      </div>
    </div>
  );
}

export default ClientPortal;

import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { forgotPassword } from "../../services/api.js";
import "./ForgotPasswordPage.css";

export default function ForgotPasswordPage() {
    const [email, setEmail] = useState("");
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError("");
        setMessage("");

        try {
            const res = await forgotPassword(email);
            setMessage("Verifică emailul. Dacă adresa există în sistem, a fost generat un link de resetare.");
            
            // Doar pentru testare pe mediul de dezvoltare:
            if (res && res.mock_token) {
                localStorage.setItem("mock_token", res.mock_token);
                // Redirecționăm automat pentru demonstrație
                setTimeout(() => navigate(`/reset-password?token=${res.mock_token}`), 2500);
            }
        } catch (err) {
            setError(err.message || "A apărut o eroare la solicitare.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="forgot-password-page">
            <div className="fp-container">
                <h2>Ai uitat parola?</h2>
                <p>Introdu adresa de email și îți vom trimite instrucțiuni pentru resetarea parolei.</p>
                {message && <div className="fp-alert fp-alert-success">{message}</div>}
                {error && <div className="fp-alert fp-alert-error">{error}</div>}
                <form onSubmit={handleSubmit} className="fp-form">
                    <div className="fp-field">
                        <label htmlFor="email">Email</label>
                        <input
                            type="email"
                            id="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                        />
                    </div>
                    <button type="submit" disabled={loading} className="fp-btn">
                        {loading ? "Se trimite..." : "Trimite solicitarea"}
                    </button>
                </form>
                <div className="fp-links">
                    <Link to="/login">Înapoi la autentificare</Link>
                </div>
            </div>
        </div>
    );
}
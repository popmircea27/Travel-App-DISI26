import { useState, useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { resetPassword } from "../../services/api.js";
import "./ResetPasswordPage.css";

export default function ResetPasswordPage() {
    const [searchParams] = useSearchParams();
    // Preluam token-ul fie din parametrii URL-ului fie din localStorage (daca ne aflam in stadiul mock pe mediul dev)
    const [token, setToken] = useState(searchParams.get("token") || localStorage.getItem("mock_token") || "");
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        if (searchParams.get("token")) {
            setToken(searchParams.get("token"));
        }
    }, [searchParams]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError("");
        setMessage("");

        if (newPassword !== confirmPassword) {
            setError("Parolele introduse nu se potrivesc.");
            setLoading(false);
            return;
        }

        try {
            await resetPassword(token, newPassword);
            setMessage("Parola a fost resetată cu succes. Vei fi redirecționat către login...");
            localStorage.removeItem("mock_token");
            
            setTimeout(() => {
                navigate("/login");
            }, 3000);
        } catch (err) {
            setError(err.message || "A apărut o eroare la resetarea parolei.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="reset-password-page">
            <div className="rp-container">
                <h2>Resetare Parolă</h2>
                <p>Introdu noua parolă pentru contul tău.</p>
                {message && <div className="rp-alert rp-alert-success">{message}</div>}
                {error && <div className="rp-alert rp-alert-error">{error}</div>}
                
                {!message && (
                    <form onSubmit={handleSubmit} className="rp-form">
                        <div className="rp-field" style={{ display: 'none' }}>
                            <label htmlFor="token">Token</label>
                            <input
                                type="text"
                                id="token"
                                value={token}
                                onChange={(e) => setToken(e.target.value)}
                                required
                            />
                        </div>
                        <div className="rp-field">
                            <label htmlFor="newPassword">Parolă nouă</label>
                            <input
                                type="password"
                                id="newPassword"
                                value={newPassword}
                                onChange={(e) => setNewPassword(e.target.value)}
                                required
                            />
                        </div>
                        <div className="rp-field">
                            <label htmlFor="confirmPassword">Confirmă parola nouă</label>
                            <input
                                type="password"
                                id="confirmPassword"
                                value={confirmPassword}
                                onChange={(e) => setConfirmPassword(e.target.value)}
                                required
                            />
                        </div>
                        <button type="submit" disabled={loading} className="rp-btn">
                            {loading ? "Se procesează..." : "Resetează parola"}
                        </button>
                    </form>
                )}
            </div>
        </div>
    );
}
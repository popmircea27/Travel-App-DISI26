// src/pages/loginPage/LoginPage.jsx
import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import "./LoginPage.css";
import { login } from "../../services/api.js";
import { useAuth } from "../../context/AuthContext.jsx";

function LoginPage() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [errors, setErrors] = useState({});
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();
    const { handleLoginSuccess } = useAuth();

    const validate = () => {
        const newErrors = {};
        if (!email) newErrors.email = "Email-ul este obligatoriu";
        if (!password) newErrors.password = "Parola este obligatorie";
        return newErrors;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const newErrors = validate();
        if (Object.keys(newErrors).length > 0) {
            setErrors(newErrors);
            return;
        }

        setLoading(true);
        setErrors({});
        try {
            const data = await login(email, password);
            handleLoginSuccess(data.token, { email });
            navigate("/");
        } catch (err) {
            setErrors({ general: err.message || "Email sau parolă greșite." });
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-container">
            <div className="auth-card">

                {/* Buton înapoi */}
                <button
                    className="auth-back-btn"
                    onClick={() => navigate("/")}
                    type="button"
                    aria-label="Înapoi la pagina principală"
                >
                    ← Înapoi
                </button>

                <h2>Bine ai revenit!</h2>
                <p className="auth-subtitle">Loghează-te în contul tău</p>

                {errors.general && <span className="error">{errors.general}</span>}

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>Email</label>
                        <input
                            type="email"
                            placeholder="email@exemplu.com"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                        />
                        {errors.email && <span className="error">{errors.email}</span>}
                    </div>

                    <div className="form-group">
                        <label>Parolă</label>
                        <input
                            type="password"
                            placeholder="Parola ta"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                        />
                        {errors.password && <span className="error">{errors.password}</span>}
                    </div>

                    <button type="submit" className="auth-btn" disabled={loading}>
                        {loading ? "Se încarcă..." : "Login"}
                    </button>
                </form>

                <p className="auth-switch">
                    Nu ai cont? <Link to="/register">Înregistrează-te</Link>
                </p>
            </div>
        </div>
    );
}

export default LoginPage;
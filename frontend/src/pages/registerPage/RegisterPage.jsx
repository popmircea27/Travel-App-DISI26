import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import "../loginPage/LoginPage.css";
import { register } from "../../services/api.js";

function RegisterPage() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [errors, setErrors] = useState({});
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const validate = () => {
        const newErrors = {};
        if (!email) newErrors.email = "Email-ul este obligatoriu";
        if (!password) newErrors.password = "Parola este obligatorie";
        else if (password.length < 6) newErrors.password = "Parola trebuie să aibă minim 6 caractere";
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
            await register(email, password);
            navigate("/login");
        } catch (err) {
            setErrors({ general: err.message || "Înregistrarea a eșuat. Încearcă din nou." });
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

                <h2>Creează cont</h2>
                <p className="auth-subtitle">Alătură-te comunității noastre</p>

                {errors.general && <span className="error">{errors.general}</span>}

                {/* autoComplete="off" previne precompletarea de browser */}
                <form onSubmit={handleSubmit} autoComplete="off">
                    <div className="form-group">
                        <label>Email</label>
                        <input
                            type="email"
                            placeholder="email@exemplu.com"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            autoComplete="off"
                        />
                        {errors.email && <span className="error">{errors.email}</span>}
                    </div>

                    <div className="form-group">
                        <label>Parolă</label>
                        <input
                            type="password"
                            placeholder="Minim 6 caractere"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            autoComplete="new-password"
                        />
                        {errors.password && <span className="error">{errors.password}</span>}
                    </div>

                    <button type="submit" className="auth-btn" disabled={loading}>
                        {loading ? "Se încarcă..." : "Înregistrează-te"}
                    </button>
                </form>

                <p className="auth-switch">
                    Ai deja cont? <Link to="/login">Loghează-te</Link>
                </p>
            </div>
        </div>
    );
}

export default RegisterPage;
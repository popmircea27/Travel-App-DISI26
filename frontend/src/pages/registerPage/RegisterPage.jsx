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
            // Apel real: POST /api/auth/register
            // Backend primește: { email, password }
            // Backend returnează: { id, email, role, createdAt }
            await register(email, password);

            // După înregistrare cu succes → redirect la login
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
                <h2>Creează cont</h2>
                <p className="auth-subtitle">Alătură-te comunității noastre</p>

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
                            placeholder="Minim 6 caractere"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
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
// src/components/AppLayout.jsx
// Navbar vizibil întotdeauna.
// Nelogat  → logo + Locații, Login, Register
// Logat    → logo + Dashboard, Locații, AI Itinerary, Contact, Profil, Logout

import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import "./AppLayout.css";

export default function AppLayout({ children }) {
    const { user, handleLogout } = useAuth();
    const navigate = useNavigate();

    const onLogout = () => {
        handleLogout();
        navigate("/");
    };

    return (
        <div className="app-layout">
            <nav className="app-navbar" aria-label="Navigare principală">

                {/* Brand – merge mereu la / */}
                <NavLink to="/" className="app-navbar__brand">
                    <span className="app-navbar__brand-icon">🌍</span>
                    <span>TravelApp</span>
                </NavLink>

                <div className="app-navbar__links">
                    {user ? (
                        /* ── LOGAT ── */
                        <>
                            <NavLink
                                to="/"
                                end
                                className={({ isActive }) => "app-navbar__link" + (isActive ? " active" : "")}
                            >
                                <span>🏠</span>
                                <span>Dashboard</span>
                            </NavLink>

                            <NavLink
                                to="/locations"
                                className={({ isActive }) => "app-navbar__link" + (isActive ? " active" : "")}
                            >
                                <span>📍</span>
                                <span>Locații</span>
                            </NavLink>

                            <NavLink
                                to="/ai-itinerary"
                                className={({ isActive }) => "app-navbar__link" + (isActive ? " active" : "")}
                            >
                                <span>🤖</span>
                                <span>AI Itinerary</span>
                            </NavLink>

                            <NavLink
                                to="/contact"
                                className={({ isActive }) => "app-navbar__link" + (isActive ? " active" : "")}
                            >
                                <span>✉️</span>
                                <span>Contact</span>
                            </NavLink>

                            <NavLink
                                to="/profile"
                                className={({ isActive }) => "app-navbar__link" + (isActive ? " active" : "")}
                            >
                                <span>👤</span>
                                <span>Profilul meu</span>
                            </NavLink>

                            <button
                                className="app-navbar__link app-navbar__logout"
                                onClick={onLogout}
                                type="button"
                                aria-label="Deconectare"
                            >
                                <span>🚪</span>
                                <span>Logout</span>
                            </button>
                        </>
                    ) : (
                        /* ── NELOGAT ── */
                        <>
                            <NavLink
                                to="/locations"
                                className={({ isActive }) => "app-navbar__link" + (isActive ? " active" : "")}
                            >
                                <span>📍</span>
                                <span>Locații</span>
                            </NavLink>

                            <NavLink
                                to="/login"
                                className={({ isActive }) => "app-navbar__link" + (isActive ? " active" : "")}
                            >
                                <span>🔐</span>
                                <span>Login</span>
                            </NavLink>

                            <NavLink
                                to="/register"
                                className={({ isActive }) =>
                                    "app-navbar__link app-navbar__register" + (isActive ? " active" : "")
                                }
                            >
                                <span>✨</span>
                                <span>Register</span>
                            </NavLink>
                        </>
                    )}
                </div>
            </nav>

            <main className="app-layout__content">
                {children}
            </main>
        </div>
    );
}
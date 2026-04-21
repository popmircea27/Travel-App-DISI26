// src/components/AppLayout.jsx
// Layout-ul paginilor autentificate: navbar sus + conținut jos.
// Navbarul are: brand, link Dashboard, link Profil, buton Logout.

import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import "./AppLayout.css";

export default function AppLayout({ children }) {
    const { handleLogout } = useAuth();
    const navigate = useNavigate();

    const onLogout = () => {
        handleLogout();
        navigate("/login");
    };

    return (
        <div className="app-layout">
            <nav className="app-navbar" aria-label="Navigare principală">
                {/* Brand */}
                <NavLink to="/dashboard" className="app-navbar__brand">
                    <span className="app-navbar__brand-icon">🌍</span>
                    <span>TravelApp</span>
                </NavLink>

                {/* Link-uri */}
                <div className="app-navbar__links">
                    <NavLink
                        to="/dashboard"
                        className={({ isActive }) =>
                            "app-navbar__link" + (isActive ? " active" : "")
                        }
                    >
                        <span>🏠</span>
                        <span>Dashboard</span>
                    </NavLink>

                    <NavLink
                        to="/profile"
                        className={({ isActive }) =>
                            "app-navbar__link" + (isActive ? " active" : "")
                        }
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
                </div>
            </nav>

            <main className="app-layout__content">
                {children}
            </main>
        </div>
    );
}
// src/components/AppLayout.jsx
import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import "./AppLayout.css";
import NotificationBell from './NotificationBell';

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
                <NavLink to="/" className="app-navbar__brand">
                    <span className="app-navbar__brand-icon">🌍</span>
                    <span>TravelApp</span>
                </NavLink>

                <div className="app-navbar__links">
                    {user ? (
                        <>
                            <NavLink to="/" end className={({ isActive }) => "app-navbar__link" + (isActive ? " active" : "")}>
                                <span>🏠</span>
                                <span>Dashboard</span>
                            </NavLink>

                            <NavLink to="/locations" className={({ isActive }) => "app-navbar__link" + (isActive ? " active" : "")}>
                                <span>📍</span>
                                <span>Locații</span>
                            </NavLink>

                            <NavLink to="/wishlist" className={({ isActive }) => "app-navbar__link" + (isActive ? " active" : "")}>
                                <span>♥</span>
                                <span>Wishlist</span>
                            </NavLink>

                            <NavLink to="/ai-itinerary" className={({ isActive }) => "app-navbar__link" + (isActive ? " active" : "")}>
                                <span>🤖</span>
                                <span>AI Itinerary</span>
                            </NavLink>

                            <NavLink to="/contact" className={({ isActive }) => "app-navbar__link" + (isActive ? " active" : "")}>
                                <span>✉️</span>
                                <span>Contact</span>
                            </NavLink>

                            {/* Notificări – componentă separată */}
                            <NotificationBell />

                            {user?.role === "ADMIN" && (
                                <NavLink to="/admin" className={({ isActive }) => "app-navbar__link" + (isActive ? " active" : "")}>
                                    <span>📊</span>
                                    <span>Admin</span>
                                </NavLink>
                            )}

                            <NavLink to="/profile" className={({ isActive }) => "app-navbar__link" + (isActive ? " active" : "")}>
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
                        <>
                            <NavLink to="/locations" className={({ isActive }) => "app-navbar__link" + (isActive ? " active" : "")}>
                                <span>📍</span>
                                <span>Locații</span>
                            </NavLink>

                            <NavLink to="/login" className={({ isActive }) => "app-navbar__link" + (isActive ? " active" : "")}>
                                <span>🔐</span>
                                <span>Login</span>
                            </NavLink>

                            <NavLink
                                to="/register"
                                className={({ isActive }) => "app-navbar__link app-navbar__register" + (isActive ? " active" : "")}
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
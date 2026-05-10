// src/components/AppLayout.jsx

import { NavLink, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext.jsx";
import { getNotifications } from "../services/api.js";
import "./AppLayout.css";

export default function AppLayout({ children }) {
    const { user, handleLogout } = useAuth();
    const navigate = useNavigate();
    const [unreadCount, setUnreadCount] = useState(0);

    // Polling notificări necitite la fiecare 60s (doar dacă e logat)
    useEffect(() => {
        if (!user) { setUnreadCount(0); return; }

        const fetchUnread = async () => {
            try {
                const data = await getNotifications();
                const count = Array.isArray(data) ? data.filter((n) => !n.read).length : 0;
                setUnreadCount(count);
            } catch {
                // silently ignore – nu blocăm UI-ul
            }
        };

        fetchUnread();
        const interval = setInterval(fetchUnread, 60000);
        return () => clearInterval(interval);
    }, [user]);

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

                            {/* Notificări cu badge */}
                            <NavLink to="/notifications" className={({ isActive }) => "app-navbar__link app-navbar__link--notif" + (isActive ? " active" : "")}>
                                <span className="app-navbar__notif-wrap">
                                    🔔
                                    {unreadCount > 0 && (
                                        <span className="app-navbar__notif-badge" aria-label={`${unreadCount} notificări necitite`}>
                                            {unreadCount > 9 ? "9+" : unreadCount}
                                        </span>
                                    )}
                                </span>
                                <span>Notificări</span>
                            </NavLink>

                            <NavLink to="/ai-itinerary" className={({ isActive }) => "app-navbar__link" + (isActive ? " active" : "")}>
                                <span>🤖</span>
                                <span>AI Itinerary</span>
                            </NavLink>

                            <NavLink to="/contact" className={({ isActive }) => "app-navbar__link" + (isActive ? " active" : "")}>
                                <span>✉️</span>
                                <span>Contact</span>
                            </NavLink>

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
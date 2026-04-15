// ============================================================
// src/context/AuthContext.jsx
// Context global pentru starea de autentificare.
// Folosit de orice componentă care are nevoie de datele userului.
// ============================================================

import { createContext, useContext, useState, useEffect } from "react";
import { getProfile, logout as apiLogout } from "../services/api";
import { MOCK_USERS } from "../services/mockData";

const AuthContext = createContext(null);

// ─── Provider ─────────────────────────────────────────────────
export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // La mount, verificăm dacă există user salvat în localStorage
        const savedUser = localStorage.getItem("user");
        if (savedUser) {
            try {
                setUser(JSON.parse(savedUser));
            } catch {
                localStorage.removeItem("user");
            }
        }
        setLoading(false);
    }, []);

    /**
     * Apelat după login cu succes.
     * Salvează token-ul și datele userului.
     */
    function handleLoginSuccess(token, userData) {
        localStorage.setItem("token", token);
        localStorage.setItem("user", JSON.stringify(userData));
        setUser(userData);
    }

    /**
     * Deconectare – curăță tot state-ul și localStorage.
     */
    function handleLogout() {
        apiLogout();
        setUser(null);
    }

    /**
     * Reîncarcă profilul de la server (ex: după update).
     * Folosit cu MOCK_USERS în dev, înlocuit cu getProfile() în prod.
     */
    async function refreshProfile(role = "tourist") {
        try {
            // TODO: Înlocuiește cu: const fresh = await getProfile();
            const fresh = MOCK_USERS[role]; // ← MOCK – șterge când backend-ul e gata
            setUser(fresh);
            localStorage.setItem("user", JSON.stringify(fresh));
        } catch (err) {
            console.error("Nu s-a putut reîncărca profilul:", err);
        }
    }

    return (
        <AuthContext.Provider
            value={{ user, loading, handleLoginSuccess, handleLogout, refreshProfile }}
        >
            {children}
        </AuthContext.Provider>
    );
}

// ─── Hook ─────────────────────────────────────────────────────
export function useAuth() {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error("useAuth trebuie folosit în interiorul AuthProvider");
    return ctx;
}
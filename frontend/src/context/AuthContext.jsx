// src/context/AuthContext.jsx
// Context global pentru starea de autentificare.
// Login returnează doar token → profilul complet se preia separat cu getProfile().

import { createContext, useContext, useState, useEffect } from "react";
import { logout as apiLogout } from "../services/api";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // La mount: refacem starea din localStorage (dacă userul era logat)
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
     * @param {string} token   - JWT primit de la backend
     * @param {object} userData - date minimale cunoscute la login (ex: { email })
     *                           Profilul complet se încarcă în ProfilePage via getProfile().
     */
    function handleLoginSuccess(token, userData) {
        localStorage.setItem("token", token);
        localStorage.setItem("user", JSON.stringify(userData));
        setUser(userData);
    }

    /**
     * Actualizează datele de user în context și localStorage.
     * Apelat din ProfilePage după ce getProfile() returnează profilul complet.
     */
    function setUserData(userData) {
        localStorage.setItem("user", JSON.stringify(userData));
        setUser(userData);
    }

    /**
     * Deconectare – curăță token-ul și starea.
     */
    function handleLogout() {
        apiLogout(); // șterge token și user din localStorage
        setUser(null);
    }

    return (
        <AuthContext.Provider
            value={{ user, loading, handleLoginSuccess, handleLogout, setUserData }}
        >
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error("useAuth trebuie folosit în interiorul AuthProvider");
    return ctx;
}
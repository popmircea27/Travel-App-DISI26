// src/components/PrivateRoute.jsx
// Dacă userul nu e logat, îl trimite la /login.
// Dacă AuthContext încă se inițializează (loading), nu face redirect prematur.

import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

export default function PrivateRoute({ children }) {
    const { user, loading } = useAuth();

    if (loading) {
        // Așteptăm să se hidrateze starea din localStorage
        return null;
    }

    if (!user) {
        return <Navigate to="/login" replace />;
    }

    return children;
}
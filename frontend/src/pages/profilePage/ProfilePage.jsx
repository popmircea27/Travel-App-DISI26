// src/pages/profilePage/ProfilePage.jsx
//
// Pagina de profil pentru utilizatorul autentificat (Tourist).
// Preia datele reale de la: GET /api/users/me  (necesită Bearer token)
// Răspuns backend: { id, email, role, created_at }

import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext.jsx";
import { getProfile } from "../../services/api.js";
import "./ProfilePage.css";

// ─── Componente mici ───────────────────────────────────────────

function Avatar({ email }) {
    // Inițialele: prima literă din email (înainte de @)
    const initial = email ? email[0].toUpperCase() : "?";
    return (
        <div className="pp-avatar" aria-hidden="true">
            {initial}
        </div>
    );
}

function InfoRow({ label, value }) {
    return (
        <div className="pp-info-row">
            <span className="pp-info-label">{label}</span>
            <span className="pp-info-value">{value}</span>
        </div>
    );
}

// ─── Loading skeleton ──────────────────────────────────────────

function ProfileSkeleton() {
    return (
        <div className="pp-skeleton" aria-label="Se încarcă...">
            <div className="pp-sk-avatar" />
            <div className="pp-sk-line pp-sk-wide" />
            <div className="pp-sk-line pp-sk-narrow" />
            <div className="pp-sk-card" />
        </div>
    );
}

// ─── Error state ───────────────────────────────────────────────

function ProfileError({ message, onRetry }) {
    return (
        <div className="pp-error" role="alert">
            <span className="pp-error-icon">⚠</span>
            <p>{message}</p>
            <button className="pp-retry-btn" onClick={onRetry}>
                Încearcă din nou
            </button>
        </div>
    );
}

// ─── Componenta principală ─────────────────────────────────────

export default function ProfilePage() {
    const { handleLogout } = useAuth();
    const navigate = useNavigate();

    const [profile, setProfile] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchProfile = async () => {
        setLoading(true);
        setError(null);
        try {
            // Apel real: GET /api/users/me cu Bearer token din localStorage
            // Răspuns: { id, email, role, created_at }
            const data = await getProfile();
            setProfile(data);
        } catch (err) {
            // Dacă token-ul e expirat/invalid, trimitem la login
            if (err.message.includes("401") || err.message.toLowerCase().includes("unauthorized")) {
                handleLogout();
                navigate("/login");
            } else {
                setError(err.message || "Nu s-a putut încărca profilul.");
            }
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchProfile();
    }, []);

    const handleLogoutClick = () => {
        handleLogout();
        navigate("/login");
    };

    // Formatare dată: "15 Martie 2024"
    const formatDate = (dateStr) => {
        if (!dateStr) return "—";
        try {
            return new Date(dateStr).toLocaleDateString("ro-RO", {
                year: "numeric",
                month: "long",
                day: "numeric",
            });
        } catch {
            return dateStr;
        }
    };

    // ── Render ─────────────────────────────────────────────────
    return (
        <div className="pp-page">
            <div className="pp-container">

                {loading && <ProfileSkeleton />}

                {error && (
                    <ProfileError message={error} onRetry={fetchProfile} />
                )}

                {!loading && !error && profile && (
                    <>
                        {/* ── Header ── */}
                        <div className="pp-header">
                            <Avatar email={profile.email} />
                            <div className="pp-header-info">
                                <h1 className="pp-name">{profile.email}</h1>
                                <span className="role-badge">
                                    {profile.role === "ADMIN" ? "⚙ Administrator" : "✦ Tourist"}
                                </span>
                            </div>
                        </div>

                        {/* ── Informații cont ── */}
                        <section className="pp-section">
                            <h2 className="pp-section-title">Informații cont</h2>
                            <div className="info-card">
                                <InfoRow label="Email" value={profile.email} />
                                <InfoRow label="Rol" value={profile.role} />
                                <InfoRow
                                    label="Membru din"
                                    value={formatDate(profile.created_at)}
                                />
                            </div>
                        </section>

                        {/* ── Statistici (placeholder – completat când backend-ul expune datele) ── */}
                        <section className="pp-section">
                            <h2 className="pp-section-title">Activitate</h2>
                            <div className="stats-grid">
                                <div className="stat-item">
                                    <span className="stat-value">—</span>
                                    <span className="stat-label">Călătorii</span>
                                </div>
                                <div className="stat-item">
                                    <span className="stat-value">—</span>
                                    <span className="stat-label">Review-uri</span>
                                </div>
                                <div className="stat-item">
                                    <span className="stat-value">—</span>
                                    <span className="stat-label">Favorite</span>
                                </div>
                            </div>
                        </section>

                        {/* ── Logout ── */}
                        <button
                            className="pp-logout-btn"
                            onClick={handleLogoutClick}
                            type="button"
                        >
                            Deconectare
                        </button>
                    </>
                )}
            </div>
        </div>
    );
}
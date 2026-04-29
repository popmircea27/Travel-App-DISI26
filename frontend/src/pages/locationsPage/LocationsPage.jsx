// src/pages/locationsPage/LocationsPage.jsx
// Afișează lista locațiilor turistice din backend.
// GET /api/locations → array de Location objects
// Câmpuri Location: id, name, description, latitude, longitude,
//                   country, city, imageUrl, createdAt, updatedAt

import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getLocations } from "../../services/api.js";
import { useAuth } from "../../context/AuthContext.jsx";
import "./LocationsPage.css";

// ─── Card individual ───────────────────────────────────────────
function LocationCard({ location }) {
    // Prescurtare descriere la max 120 caractere
    const shortDesc = location.description
        ? location.description.length > 120
            ? location.description.slice(0, 120).trimEnd() + "…"
            : location.description
        : "Nicio descriere disponibilă.";

    return (
        <article className="loc-card">
            {/* Imagine sau placeholder */}
            <div className="loc-card__img-wrap">
                {location.imageUrl ? (
                    <img
                        src={location.imageUrl}
                        alt={location.name}
                        className="loc-card__img"
                        loading="lazy"
                        onError={(e) => {
                            e.target.style.display = "none";
                            e.target.nextSibling.style.display = "flex";
                        }}
                    />
                ) : null}
                <div
                    className="loc-card__img-placeholder"
                    style={{ display: location.imageUrl ? "none" : "flex" }}
                    aria-hidden="true"
                >
                    📍
                </div>
            </div>

            {/* Conținut */}
            <div className="loc-card__body">
                <h2 className="loc-card__name">{location.name}</h2>

                {/* Localizare: city + country */}
                {(location.city || location.country) && (
                    <p className="loc-card__location">
                        <span className="loc-card__location-icon" aria-hidden="true">📌</span>
                        {[location.city, location.country].filter(Boolean).join(", ")}
                    </p>
                )}

                <p className="loc-card__desc">{shortDesc}</p>
            </div>
        </article>
    );
}

// ─── Skeleton loading ──────────────────────────────────────────
function LocationsSkeleton() {
    return (
        <div className="loc-grid" aria-label="Se încarcă locațiile...">
            {Array.from({ length: 6 }).map((_, i) => (
                <div key={i} className="loc-card loc-card--skeleton">
                    <div className="loc-sk loc-sk--img" />
                    <div className="loc-card__body">
                        <div className="loc-sk loc-sk--title" />
                        <div className="loc-sk loc-sk--sub" />
                        <div className="loc-sk loc-sk--line" />
                        <div className="loc-sk loc-sk--line loc-sk--short" />
                    </div>
                </div>
            ))}
        </div>
    );
}

// ─── Empty state ───────────────────────────────────────────────
function EmptyState() {
    return (
        <div className="loc-empty">
            <span className="loc-empty__icon" aria-hidden="true">🗺️</span>
            <h2>Nicio locație găsită</h2>
            <p>Nu există locații disponibile momentan. Revino mai târziu!</p>
        </div>
    );
}

// ─── Error state ───────────────────────────────────────────────
function ErrorState({ message, onRetry }) {
    return (
        <div className="loc-error" role="alert">
            <span className="loc-error__icon" aria-hidden="true">⚠</span>
            <h2>A apărut o eroare</h2>
            <p>{message}</p>
            <button className="loc-retry-btn" onClick={onRetry} type="button">
                Încearcă din nou
            </button>
        </div>
    );
}

// ─── Componenta principală ─────────────────────────────────────
export default function LocationsPage() {
    const { handleLogout } = useAuth();
    const navigate = useNavigate();

    const [locations, setLocations] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchLocations = async () => {
        setLoading(true);
        setError(null);
        try {
            // GET /api/locations – necesită Bearer token
            const data = await getLocations();
            setLocations(Array.isArray(data) ? data : []);
        } catch (err) {
            // Token expirat → redirect la login
            if (
                err.message.includes("401") ||
                err.message.toLowerCase().includes("unauthorized")
            ) {
                handleLogout();
                navigate("/login");
            } else {
                setError(err.message || "Nu s-au putut încărca locațiile.");
            }
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchLocations();
    }, []);

    return (
        <div className="loc-page">
            {/* ── Header ── */}
            <div className="loc-header">
                <h1 className="loc-title">Locații turistice</h1>
                <p className="loc-subtitle">
                    Explorează destinații din toată România
                </p>
            </div>

            {/* ── Conținut ── */}
            {loading && <LocationsSkeleton />}

            {!loading && error && (
                <ErrorState message={error} onRetry={fetchLocations} />
            )}

            {!loading && !error && locations.length === 0 && <EmptyState />}

            {!loading && !error && locations.length > 0 && (
                <>
                    <p className="loc-count">
                        {locations.length} {locations.length === 1 ? "locație" : "locații"} disponibile
                    </p>
                    <div className="loc-grid">
                        {locations.map((loc) => (
                            <LocationCard key={loc.id} location={loc} />
                        ))}
                    </div>
                </>
            )}
        </div>
    );
}
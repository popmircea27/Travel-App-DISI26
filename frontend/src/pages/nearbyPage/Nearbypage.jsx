// src/pages/nearbyPage/NearbyPage.jsx

import { useEffect, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext.jsx";
import "./NearbyPage.css";

// ─── Constants ──────────────────────────────────────────────────
const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api";
const DEFAULT_RADIUS_KM = 5;

// ─── Haversine ──────────────────────────────────────────────────
function haversineKm(lat1, lon1, lat2, lon2) {
    const R = 6371;
    const dLat = ((lat2 - lat1) * Math.PI) / 180;
    const dLon = ((lon2 - lon1) * Math.PI) / 180;
    const a =
        Math.sin(dLat / 2) ** 2 +
        Math.cos((lat1 * Math.PI) / 180) *
        Math.cos((lat2 * Math.PI) / 180) *
        Math.sin(dLon / 2) ** 2;
    return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

// ─── API helpers ────────────────────────────────────────────────

async function trySetMyLocation(latitude, longitude) {
    const token = localStorage.getItem("token");
    try {
        await fetch(`${BASE_URL}/proximity/me/location`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                ...(token ? { Authorization: `Bearer ${token}` } : {}),
            },
            body: JSON.stringify({ latitude, longitude }),
        });
        // ignorăm orice eroare – endpoint poate fi neimplementat încă
    } catch (e) {
        console.warn("trySetMyLocation:", e.message);
    }
}

async function fetchNearbyFriends(radiusKm) {
    const token = localStorage.getItem("token");
    const res = await fetch(
        `${BASE_URL}/proximity/friends/nearby?radiusKm=${radiusKm}`,
        { headers: token ? { Authorization: `Bearer ${token}` } : {} }
    );
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `HTTP ${res.status}`);
    }
    return res.json();
}

async function fetchAllUserLocations() {
    const token = localStorage.getItem("token");
    const res = await fetch(`${BASE_URL}/proximity/locations`, {
        headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `HTTP ${res.status}`);
    }
    return res.json();
}

async function fetchAttractions() {
    const token = localStorage.getItem("token");
    const res = await fetch(`${BASE_URL}/locations?size=1000`, {
        headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `HTTP ${res.status}`);
    }
    return res.json();
}

// ─── Formatters ─────────────────────────────────────────────────
function formatDistance(km) {
    if (km < 1) return `${Math.round(km * 1000)} m`;
    return `${km.toFixed(1)} km`;
}

function formatRelativeTime(isoString) {
    if (!isoString) return "";
    const diff = Date.now() - new Date(isoString).getTime();
    const mins = Math.floor(diff / 60000);
    if (mins < 1) return "acum";
    if (mins < 60) return `acum ${mins} min`;
    const hrs = Math.floor(mins / 60);
    if (hrs < 24) return `acum ${hrs} h`;
    return `acum ${Math.floor(hrs / 24)} zile`;
}

function getCategoryEmoji(category) {
    if (!category) return "📍";
    const c = category.toLowerCase();
    if (c.includes("muzeu") || c.includes("museum")) return "🏛️";
    if (c.includes("parc") || c.includes("park")) return "🌳";
    if (c.includes("restaurant") || c.includes("food")) return "🍽️";
    if (c.includes("hotel") || c.includes("cazare")) return "🏨";
    if (c.includes("biseric") || c.includes("church")) return "⛪";
    if (c.includes("plaj") || c.includes("beach")) return "🏖️";
    if (c.includes("munte") || c.includes("mountain")) return "⛰️";
    if (c.includes("castel") || c.includes("castle")) return "🏰";
    if (c.includes("mall") || c.includes("magazin")) return "🛍️";
    return "📍";
}

// ─── Sub-components ─────────────────────────────────────────────

function PermissionRequest({ onRequest, loading }) {
    return (
        <div className="nb-permission">
            <div className="nb-permission__icon" aria-hidden="true">
                <svg viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="32" cy="28" r="10" stroke="currentColor" strokeWidth="3" fill="none" />
                    <path d="M32 14C23.163 14 16 21.163 16 30c0 12 16 28 16 28s16-16 16-28c0-8.837-7.163-16-16-16z"
                          stroke="currentColor" strokeWidth="3" fill="none" strokeLinejoin="round" />
                    <circle cx="32" cy="28" r="4" fill="currentColor" />
                </svg>
            </div>
            <h2>Permite accesul la locație</h2>
            <p>
                Pentru a vedea prietenii și atracțiile din apropiere, aplicația
                are nevoie de locația ta curentă.
            </p>
            <button
                className="nb-btn nb-btn--primary"
                onClick={onRequest}
                disabled={loading}
                type="button"
            >
                {loading ? (
                    <><span className="nb-spinner" aria-hidden="true" /> Se obține locația…</>
                ) : (
                    <><span aria-hidden="true">📍</span> Permite locația</>
                )}
            </button>
        </div>
    );
}

function LocationDenied({ onRetry }) {
    return (
        <div className="nb-denied" role="alert">
            <span className="nb-denied__icon" aria-hidden="true">🔒</span>
            <h2>Acces refuzat</h2>
            <p>Accesul la locație a fost blocat. Permite accesul din setările browserului și încearcă din nou.</p>
            <button className="nb-btn nb-btn--secondary" onClick={onRetry} type="button">
                Încearcă din nou
            </button>
        </div>
    );
}

function LoadingState() {
    return (
        <div className="nb-loading" aria-label="Se încarcă…">
            <div className="nb-radar" aria-hidden="true">
                <div className="nb-radar__ring nb-radar__ring--1" />
                <div className="nb-radar__ring nb-radar__ring--2" />
                <div className="nb-radar__ring nb-radar__ring--3" />
                <div className="nb-radar__dot" />
                <div className="nb-radar__sweep" />
            </div>
            <p className="nb-loading__text">Se caută în zonă…</p>
        </div>
    );
}

function EmptyNearby({ radiusKm, mode }) {
    const icon  = mode === "friends" ? "👥" : mode === "all" ? "🌍" : "🏛️";
    const title =
        mode === "friends" ? "Niciun prieten în apropiere" :
            mode === "all"     ? "Niciun utilizator în apropiere" :
                "Nicio atracție în apropiere";
    const desc =
        mode === "friends" ? `Nu am găsit prieteni în raza de ${radiusKm} km.` :
            mode === "all"     ? `Nu am găsit utilizatori în raza de ${radiusKm} km.` :
                `Nu am găsit atracții turistice în raza de ${radiusKm} km.`;
    return (
        <div className="nb-empty">
            <span className="nb-empty__icon" aria-hidden="true">{icon}</span>
            <h2>{title}</h2>
            <p>{desc} Încearcă să mărești raza sau revino mai târziu.</p>
        </div>
    );
}

function FriendCard({ friend }) {
    const initials    = friend.email ? friend.email.slice(0, 2).toUpperCase() : "??";
    const displayName = friend.displayName || friend.email?.split("@")[0] || "Utilizator";
    return (
        <article className="nb-friend-card">
            <div className="nb-friend-card__avatar" aria-hidden="true">{initials}</div>
            <div className="nb-friend-card__body">
                <p className="nb-friend-card__name">{displayName}</p>
                <p className="nb-friend-card__email">{friend.email}</p>
                <p className="nb-friend-card__time">
                    <span aria-hidden="true">🕐</span>
                    {formatRelativeTime(friend.updatedAt)}
                </p>
            </div>
            <div className="nb-friend-card__distance">
                <span className="nb-friend-card__dist-val">{formatDistance(friend.distanceKm)}</span>
                <span className="nb-friend-card__dist-label">distanță</span>
            </div>
        </article>
    );
}

function AttractionCard({ attraction }) {
    const name     = attraction.name || "Atracție";
    const category = attraction.category || "";
    const emoji    = getCategoryEmoji(category);
    return (
        <article className="nb-friend-card nb-attraction-card">
            <div className="nb-friend-card__avatar nb-attraction-card__avatar" aria-hidden="true">
                {emoji}
            </div>
            <div className="nb-friend-card__body">
                <p className="nb-friend-card__name">{name}</p>
                {category && (
                    <p className="nb-friend-card__email nb-attraction-card__category">{category}</p>
                )}
                {attraction.description && (
                    <p className="nb-friend-card__time nb-attraction-card__desc">
                        {attraction.description.length > 80
                            ? attraction.description.slice(0, 80) + "…"
                            : attraction.description}
                    </p>
                )}
            </div>
            <div className="nb-friend-card__distance">
                <span className="nb-friend-card__dist-val">{formatDistance(attraction.distanceKm)}</span>
                <span className="nb-friend-card__dist-label">distanță</span>
            </div>
        </article>
    );
}

function RadiusSelector({ value, onChange }) {
    const options = [1, 2, 5, 10, 25];
    return (
        <div className="nb-radius-selector" role="group" aria-label="Selectează raza de căutare">
            <span className="nb-radius-label">Raza:</span>
            {options.map((km) => (
                <button
                    key={km}
                    className={`nb-radius-btn${value === km ? " nb-radius-btn--active" : ""}`}
                    onClick={() => onChange(km)}
                    type="button"
                    aria-pressed={value === km}
                >
                    {km} km
                </button>
            ))}
        </div>
    );
}

function ModeToggle({ value, onChange }) {
    return (
        <div className="nb-mode-toggle" role="group" aria-label="Selectează modul de afișare">
            {[
                { key: "friends",     icon: "👥", label: "Prieteni" },
                { key: "all",         icon: "🌍", label: "Utilizatori" },
                { key: "attractions", icon: "🏛️", label: "Atracții" },
            ].map(({ key, icon, label }) => (
                <button
                    key={key}
                    className={`nb-mode-btn${value === key ? " nb-mode-btn--active" : ""}`}
                    onClick={() => onChange(key)}
                    type="button"
                    aria-pressed={value === key}
                >
                    <span aria-hidden="true">{icon}</span> {label}
                </button>
            ))}
        </div>
    );
}

// ─── Main page ──────────────────────────────────────────────────
export default function NearbyPage() {
    const { handleLogout } = useAuth();
    const navigate         = useNavigate();

    const [geoState,      setGeoState]      = useState("idle");
    const [myCoords,      setMyCoords]      = useState(null);
    const [items,         setItems]         = useState([]);
    const [loading,       setLoading]       = useState(false);
    const [apiError,      setApiError]      = useState(null);
    const [radiusKm,      setRadiusKm]      = useState(DEFAULT_RADIUS_KM);
    const [lastRefreshed, setLastRefreshed] = useState(null);
    const [mode,          setMode]          = useState("friends");

    const requestLocation = useCallback(() => {
        if (!navigator.geolocation) {
            setGeoState("error");
            setApiError("Browserul tău nu suportă geolocația.");
            return;
        }
        setGeoState("requesting");
        navigator.geolocation.getCurrentPosition(
            (pos) => {
                setMyCoords({ latitude: pos.coords.latitude, longitude: pos.coords.longitude });
                setGeoState("granted");
            },
            (err) => {
                if (err.code === err.PERMISSION_DENIED) setGeoState("denied");
                else {
                    setGeoState("error");
                    setApiError(err.message || "Nu s-a putut obține locația.");
                }
            },
            { enableHighAccuracy: true, timeout: 10000 }
        );
    }, []);

    const loadNearby = useCallback(async () => {
        if (!myCoords) return;
        setLoading(true);
        setApiError(null);
        try {
            // Salvează locația silențios — nu blochează dacă endpoint-ul nu e gata
            await trySetMyLocation(myCoords.latitude, myCoords.longitude);

            if (mode === "friends") {
                const data = await fetchNearbyFriends(radiusKm);
                setItems(Array.isArray(data) ? data : []);

            } else if (mode === "all") {
                const all      = await fetchAllUserLocations();
                const filtered = (Array.isArray(all) ? all : [])
                    .map((u) => ({
                        ...u,
                        distanceKm: haversineKm(
                            myCoords.latitude, myCoords.longitude,
                            u.latitude, u.longitude
                        ),
                    }))
                    .filter((u) => u.distanceKm > 0.001 && u.distanceKm <= radiusKm);
                setItems(filtered);

            } else if (mode === "attractions") {
                const raw      = await fetchAttractions();
                const list     = Array.isArray(raw) ? raw : (raw.content || []);
                const filtered = list
                    .filter((loc) => loc.latitude != null && loc.longitude != null)
                    .map((loc) => ({
                        ...loc,
                        distanceKm: haversineKm(
                            myCoords.latitude, myCoords.longitude,
                            loc.latitude, loc.longitude
                        ),
                    }))
                    .filter((loc) => loc.distanceKm <= radiusKm);
                setItems(filtered);
            }

            setLastRefreshed(new Date());
        } catch (err) {
            if (err.message.includes("401") || err.message.toLowerCase().includes("unauthorized")) {
                handleLogout();
                navigate("/login");
            } else {
                setApiError(err.message || "Eroare la încărcarea datelor.");
            }
        } finally {
            setLoading(false);
        }
    }, [myCoords, radiusKm, mode, handleLogout, navigate]);

    useEffect(() => {
        if (geoState === "granted" && myCoords) loadNearby();
    }, [geoState, myCoords, radiusKm, mode]); // eslint-disable-line react-hooks/exhaustive-deps

    const countLabel = () => {
        const n = items.length;
        if (mode === "friends")     return `${n} ${n === 1 ? "prieten" : "prieteni"}`;
        if (mode === "all")         return `${n} ${n === 1 ? "utilizator" : "utilizatori"}`;
        return `${n} ${n === 1 ? "atracție" : "atracții"}`;
    };

    return (
        <div className="nb-page">

            {/* ── Header ── */}
            <div className="nb-header">
                <div className="nb-header__text">
                    <h1 className="nb-title">În apropiere</h1>
                    <p className="nb-subtitle">
                        {myCoords
                            ? `📍 ${myCoords.latitude.toFixed(4)}, ${myCoords.longitude.toFixed(4)}`
                            : "Descoperă ce este în jurul tău"}
                    </p>
                </div>
                {geoState === "granted" && (
                    <button
                        className="nb-btn nb-btn--icon"
                        onClick={loadNearby}
                        disabled={loading}
                        type="button"
                        aria-label="Reîncarcă"
                        title="Reîncarcă"
                    >
                        <svg className={loading ? "nb-spin" : ""} viewBox="0 0 24 24" fill="none"
                             stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <polyline points="23 4 23 10 17 10" />
                            <polyline points="1 20 1 14 7 14" />
                            <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15" />
                        </svg>
                    </button>
                )}
            </div>

            {/* ── Mode toggle ── */}
            {geoState === "granted" && (
                <ModeToggle value={mode} onChange={setMode} />
            )}

            {/* ── Radius selector ── */}
            {geoState === "granted" && !loading && (
                <RadiusSelector value={radiusKm} onChange={setRadiusKm} />
            )}

            {/* ── Results count ── */}
            {geoState === "granted" && !loading && !apiError && items.length > 0 && (
                <p className="nb-count">
                    {countLabel()} în raza de {radiusKm} km
                    {lastRefreshed && (
                        <span className="nb-count__time">
                            {" "}· actualizat {formatRelativeTime(lastRefreshed.toISOString())}
                        </span>
                    )}
                </p>
            )}

            {/* ── State machine ── */}
            {geoState === "idle"       && <PermissionRequest onRequest={requestLocation} loading={false} />}
            {geoState === "requesting" && <PermissionRequest onRequest={requestLocation} loading={true} />}
            {geoState === "denied"     && <LocationDenied onRetry={requestLocation} />}

            {(geoState === "error" || (geoState === "granted" && apiError)) && (
                <div className="nb-error" role="alert">
                    <span aria-hidden="true">⚠</span>
                    <div>
                        <strong>A apărut o eroare</strong>
                        <p>{apiError}</p>
                    </div>
                    <button className="nb-btn nb-btn--secondary" onClick={loadNearby} type="button">
                        Încearcă din nou
                    </button>
                </div>
            )}

            {geoState === "granted" && loading && <LoadingState />}

            {geoState === "granted" && !loading && !apiError && items.length === 0 && (
                <EmptyNearby radiusKm={radiusKm} mode={mode} />
            )}

            {geoState === "granted" && !loading && !apiError && items.length > 0 && (
                <div className="nb-list">
                    {items
                        .slice()
                        .sort((a, b) => a.distanceKm - b.distanceKm)
                        .map((item) =>
                            mode === "attractions" ? (
                                <AttractionCard key={item.id} attraction={item} />
                            ) : (
                                <FriendCard key={item.userId} friend={item} />
                            )
                        )}
                </div>
            )}

        </div>
    );
}
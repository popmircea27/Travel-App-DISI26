// src/pages/adminDashboardPage/AdminDashboardPage.jsx
import { useEffect, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext.jsx";
import { broadcastNotification } from "../../services/api.js";
import "./AdminDashboardPage.css";

// ─── Constants ───────────────────────────────────────────────────
const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api";

// ─── API helpers ─────────────────────────────────────────────────
function authHeaders() {
    const token = localStorage.getItem("token");
    return token ? { Authorization: `Bearer ${token}` } : {};
}

async function apiFetch(endpoint) {
    const res = await fetch(`${BASE_URL}${endpoint}`, { headers: authHeaders() });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `HTTP ${res.status}`);
    }
    if (res.status === 204) return null;
    return res.json();
}

// ─── Formatters ──────────────────────────────────────────────────
function formatDate(iso) {
    if (!iso) return "—";
    return new Date(iso).toLocaleDateString("ro-RO", {
        day: "2-digit", month: "short", year: "numeric",
    });
}

// ─── Mini chart: bar ─────────────────────────────────────────────
function BarChart({ data, colorVar = "--adm-accent" }) {
    if (!data || data.length === 0) return <p className="adm-no-data">Fără date</p>;
    const max = Math.max(...data.map((d) => d.value), 1);
    return (
        <div className="adm-bar-chart">
            {data.map((d) => (
                <div key={d.label} className="adm-bar-row">
                    <span className="adm-bar-label" title={d.label}>{d.label}</span>
                    <div className="adm-bar-track">
                        <div
                            className="adm-bar-fill"
                            style={{
                                width: `${(d.value / max) * 100}%`,
                                background: `var(${colorVar})`,
                            }}
                        />
                    </div>
                    <span className="adm-bar-val">{d.value}</span>
                </div>
            ))}
        </div>
    );
}

// ─── Donut chart ─────────────────────────────────────────────────
function DonutChart({ slices, size = 120 }) {
    if (!slices || slices.length === 0) return <p className="adm-no-data">Fără date</p>;
    const total = slices.reduce((s, d) => s + d.value, 0) || 1;
    const COLORS = ["#43a047", "#1565c0", "#f57c00", "#c62828", "#6a1b9a", "#00838f"];
    // folosim o variabilă locală, nu stare
    let cumAngle = -90;

    const arcs = slices.map((d, i) => {
        const pct = d.value / total;
        const startAngle = cumAngle;
        cumAngle += pct * 360;
        const endAngle = cumAngle;
        const r = size / 2 - 14;
        const cx = size / 2;
        const cy = size / 2;
        const toRad = (a) => (a * Math.PI) / 180;
        const x1 = cx + r * Math.cos(toRad(startAngle));
        const y1 = cy + r * Math.sin(toRad(startAngle));
        const x2 = cx + r * Math.cos(toRad(endAngle));
        const y2 = cy + r * Math.sin(toRad(endAngle));
        const largeArc = pct > 0.5 ? 1 : 0;
        return { d: `M ${cx} ${cy} L ${x1} ${y1} A ${r} ${r} 0 ${largeArc} 1 ${x2} ${y2} Z`, color: COLORS[i % COLORS.length], label: d.label, value: d.value, pct };
    });

    return (
        <div className="adm-donut-wrap">
            <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`}>
                {arcs.map((arc, i) => (
                    <path key={i} d={arc.d} fill={arc.color} opacity={0.9} />
                ))}
                <circle cx={size / 2} cy={size / 2} r={size / 2 - 30} fill="var(--bg)" />
                <text x={size / 2} y={size / 2 + 4} textAnchor="middle" fontSize="13" fontWeight="700" fill="var(--text-h)">
                    {total}
                </text>
            </svg>
            <div className="adm-donut-legend">
                {arcs.map((arc, i) => (
                    <div key={i} className="adm-legend-item">
                        <span className="adm-legend-dot" style={{ background: arc.color }} />
                        <span className="adm-legend-label">{arc.label}</span>
                        <span className="adm-legend-pct">{Math.round(arc.pct * 100)}%</span>
                    </div>
                ))}
            </div>
        </div>
    );
}

// ─── Stat card ───────────────────────────────────────────────────
function StatCard({ icon, label, value, sub, accent }) {
    return (
        <div className="adm-stat-card" style={{ "--card-accent": accent }}>
            <div className="adm-stat-icon">{icon}</div>
            <div className="adm-stat-body">
                <p className="adm-stat-label">{label}</p>
                <p className="adm-stat-value">{value ?? "—"}</p>
                {sub && <p className="adm-stat-sub">{sub}</p>}
            </div>
        </div>
    );
}

// ─── Skeleton ────────────────────────────────────────────────────
function Skeleton({ h = 20, w = "100%", radius = 6 }) {
    return (
        <div
            className="adm-skeleton"
            style={{ height: h, width: w, borderRadius: radius }}
        />
    );
}

// ─── Recent locations table ───────────────────────────────────────
function RecentLocations({ locations }) {
    const recent = [...locations]
        .sort((a, b) => new Date(b.created_at || b.createdAt) - new Date(a.created_at || a.createdAt))
        .slice(0, 8);

    return (
        <div className="adm-table-wrap">
            <table className="adm-table">
                <thead>
                <tr>
                    <th>Nume</th>
                    <th>Categorie</th>
                    <th>Locație</th>
                    <th>Adăugat</th>
                </tr>
                </thead>
                <tbody>
                {recent.map((loc) => (
                    <tr key={loc.id}>
                        <td className="adm-td-name">{loc.name}</td>
                        <td>
                            <span className="adm-badge">{loc.category || "—"}</span>
                        </td>
                        <td className="adm-td-muted">
                            {loc.location_name || loc.locationName || loc.city || "—"}
                        </td>
                        <td className="adm-td-muted">
                            {formatDate(loc.created_at || loc.createdAt)}
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}

// ─── Recent users table ───────────────────────────────────────────
function RecentUsers({ users }) {
    const recent = [...users]
        .sort((a, b) => new Date(b.createdAt || b.created_at) - new Date(a.createdAt || a.created_at))
        .slice(0, 8);

    return (
        <div className="adm-table-wrap">
            <table className="adm-table">
                <thead>
                <tr>
                    <th>Email</th>
                    <th>Rol</th>
                    <th>Înregistrat</th>
                </tr>
                </thead>
                <tbody>
                {recent.map((u) => (
                    <tr key={u.id}>
                        <td className="adm-td-name">{u.email}</td>
                        <td>
                                <span className={`adm-badge adm-badge--${(u.role || "tourist").toLowerCase()}`}>
                                    {u.role || "TOURIST"}
                                </span>
                        </td>
                        <td className="adm-td-muted">
                            {formatDate(u.createdAt || u.created_at)}
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}

// ─── Componenta principală ─────────────────────────────────────
export default function AdminDashboardPage() {
    const { handleLogout } = useAuth();
    const navigate = useNavigate();

    const [users, setUsers] = useState([]);
    const [locations, setLocations] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [activeTab, setActiveTab] = useState("overview");

    // State pentru broadcast
    const [broadcastTitle, setBroadcastTitle] = useState('');
    const [broadcastMessage, setBroadcastMessage] = useState('');
    const [broadcastLoading, setBroadcastLoading] = useState(false);
    const [broadcastSuccess, setBroadcastSuccess] = useState(false);
    const [broadcastError, setBroadcastError] = useState('');

    const handleBroadcast = async (e) => {
        e.preventDefault();
        setBroadcastLoading(true);
        setBroadcastSuccess(false);
        setBroadcastError('');
        try {
            await broadcastNotification(broadcastTitle, broadcastMessage);
            setBroadcastSuccess(true);
            setBroadcastTitle('');
            setBroadcastMessage('');
            setTimeout(() => setBroadcastSuccess(false), 3000);
        } catch (err) {
            setBroadcastError(err.message || 'Eroare la trimitere');
        } finally {
            setBroadcastLoading(false);
        }
    };

    const fetchData = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const [usersData, locationsData] = await Promise.all([
                apiFetch("/users"),
                apiFetch("/locations?size=1000"),
            ]);
            setUsers(Array.isArray(usersData) ? usersData : []);
            const locList = Array.isArray(locationsData)
                ? locationsData
                : (locationsData?.content || []);
            setLocations(locList);
        } catch (err) {
            if (err.message.includes("401") || err.message.toLowerCase().includes("unauthorized")) {
                handleLogout();
                navigate("/login");
            } else if (err.message.includes("403")) {
                setError("Nu ai permisiuni de administrator pentru această pagină.");
            } else {
                setError(err.message || "Eroare la încărcarea datelor.");
            }
        } finally {
            setLoading(false);
        }
    }, [handleLogout, navigate]);

    useEffect(() => { fetchData(); }, [fetchData]);

    // Derived stats
    const totalUsers = users.length;
    const totalTourists = users.filter((u) => (u.role || "").toUpperCase() === "TOURIST").length;
    const totalAdmins = users.filter((u) => (u.role || "").toUpperCase() === "ADMIN").length;
    const totalLocations = locations.length;

    const categoryMap = {};
    locations.forEach((l) => {
        const cat = l.category || "Altele";
        categoryMap[cat] = (categoryMap[cat] || 0) + 1;
    });
    const categoryData = Object.entries(categoryMap)
        .map(([label, value]) => ({ label, value }))
        .sort((a, b) => b.value - a.value);

    const roleData = [
        { label: "Turiști", value: totalTourists },
        { label: "Admini", value: totalAdmins },
    ].filter((d) => d.value > 0);

    const cityMap = {};
    locations.forEach((l) => {
        const city = l.city || (l.location_name || l.locationName || "").split(",")[0].trim() || "Necunoscut";
        cityMap[city] = (cityMap[city] || 0) + 1;
    });
    const cityData = Object.entries(cityMap)
        .map(([label, value]) => ({ label, value }))
        .sort((a, b) => b.value - a.value)
        .slice(0, 6);

    return (
        <div className="adm-page">
            <div className="adm-header">
                <div>
                    <h1 className="adm-title">Dashboard Admin</h1>
                    <p className="adm-subtitle">Statistici și analize aplicație</p>
                </div>
                <button
                    className="adm-refresh-btn"
                    onClick={fetchData}
                    disabled={loading}
                    type="button"
                    title="Reîncarcă datele"
                >
                    <svg className={loading ? "adm-spin" : ""} viewBox="0 0 24 24" fill="none"
                         stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <polyline points="23 4 23 10 17 10" />
                        <polyline points="1 20 1 14 7 14" />
                        <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15" />
                    </svg>
                    Reîncarcă
                </button>
            </div>

            {error && (
                <div className="adm-error" role="alert">
                    <span>⚠</span>
                    <div>
                        <strong>Eroare</strong>
                        <p>{error}</p>
                    </div>
                    <button onClick={fetchData} type="button">Încearcă din nou</button>
                </div>
            )}

            {!error && (
                <div className="adm-tabs">
                    <button
                        className={`adm-tab${activeTab === "overview" ? " adm-tab--active" : ""}`}
                        onClick={() => setActiveTab("overview")}
                    >
                        📊 Prezentare generală
                    </button>
                    <button
                        className={`adm-tab${activeTab === "users" ? " adm-tab--active" : ""}`}
                        onClick={() => setActiveTab("users")}
                    >
                        👥 Utilizatori
                    </button>
                    <button
                        className={`adm-tab${activeTab === "locations" ? " adm-tab--active" : ""}`}
                        onClick={() => setActiveTab("locations")}
                    >
                        📍 Locații
                    </button>
                    <button
                        className={`adm-tab${activeTab === "notifications" ? " adm-tab--active" : ""}`}
                        onClick={() => setActiveTab("notifications")}
                    >
                        📢 Notificări
                    </button>
                </div>
            )}

            {/* OVERVIEW TAB */}
            {!error && activeTab === "overview" && (
                <>
                    <div className="adm-stats-grid">
                        {loading ? (
                            <>
                                <div className="adm-stat-card"><Skeleton h={80} /></div>
                                <div className="adm-stat-card"><Skeleton h={80} /></div>
                                <div className="adm-stat-card"><Skeleton h={80} /></div>
                                <div className="adm-stat-card"><Skeleton h={80} /></div>
                            </>
                        ) : (
                            <>
                                <StatCard icon="👥" label="Total utilizatori" value={totalUsers}
                                          sub={`${totalTourists} turiști · ${totalAdmins} admini`}
                                          accent="#43a047" />
                                <StatCard icon="📍" label="Total locații" value={totalLocations}
                                          sub={`${categoryData.length} categorii`}
                                          accent="#1565c0" />
                                <StatCard icon="🗺️" label="Orașe acoperite" value={Object.keys(cityMap).length}
                                          sub="locații unice"
                                          accent="#f57c00" />
                                <StatCard icon="🏆" label="Top categorie"
                                          value={categoryData[0]?.label || "—"}
                                          sub={categoryData[0] ? `${categoryData[0].value} locații` : ""}
                                          accent="#c62828" />
                            </>
                        )}
                    </div>
                    <div className="adm-charts-grid">
                        <div className="adm-card">
                            <h2 className="adm-card-title">Distribuție utilizatori</h2>
                            {loading ? <Skeleton h={120} /> : <DonutChart slices={roleData} size={140} />}
                        </div>
                        <div className="adm-card">
                            <h2 className="adm-card-title">Locații pe categorie</h2>
                            {loading ? <Skeleton h={160} /> : <BarChart data={categoryData} colorVar="--adm-accent" />}
                        </div>
                        <div className="adm-card">
                            <h2 className="adm-card-title">Top orașe</h2>
                            {loading ? <Skeleton h={160} /> : <BarChart data={cityData} colorVar="--adm-blue" />}
                        </div>
                    </div>
                </>
            )}

            {/* USERS TAB */}
            {!error && activeTab === "users" && (
                <div className="adm-card adm-card--full">
                    <div className="adm-card-header">
                        <h2 className="adm-card-title">Toți utilizatorii</h2>
                        <span className="adm-count-badge">{totalUsers}</span>
                    </div>
                    {loading ? <><Skeleton h={40} /><br /><Skeleton h={40} /><br /><Skeleton h={40} /></> : <RecentUsers users={users} />}
                </div>
            )}

            {/* LOCATIONS TAB */}
            {!error && activeTab === "locations" && (
                <div className="adm-card adm-card--full">
                    <div className="adm-card-header">
                        <h2 className="adm-card-title">Toate locațiile</h2>
                        <span className="adm-count-badge">{totalLocations}</span>
                    </div>
                    {loading ? <><Skeleton h={40} /><br /><Skeleton h={40} /><br /><Skeleton h={40} /></> : <RecentLocations locations={locations} />}
                </div>
            )}

            {/* NOTIFICATIONS TAB - Admin broadcast */}
            {!error && activeTab === "notifications" && (
                <div className="adm-card adm-card--full">
                    <h2 className="adm-card-title">Trimite notificare globală</h2>
                    <form
                        className="adm-notify-form"
                        onSubmit={handleBroadcast}
                        style={{ display: 'flex', flexDirection: 'column', gap: '16px', marginTop: '16px' }}
                    >
                        <div className="adm-form-group">
                            <label htmlFor="notify-title">Titlu *</label>
                            <input
                                id="notify-title"
                                type="text"
                                value={broadcastTitle}
                                onChange={(e) => setBroadcastTitle(e.target.value)}
                                required
                                className="adm-form-input"
                                style={{ width: '100%', padding: '8px', borderRadius: '6px', border: '1px solid var(--border)' }}
                            />
                        </div>
                        <div className="adm-form-group">
                            <label htmlFor="notify-message">Mesaj *</label>
                            <textarea
                                id="notify-message"
                                value={broadcastMessage}
                                onChange={(e) => setBroadcastMessage(e.target.value)}
                                required
                                rows={4}
                                className="adm-form-textarea"
                                style={{ width: '100%', padding: '8px', borderRadius: '6px', border: '1px solid var(--border)' }}
                            />
                        </div>
                        <button
                            type="submit"
                            className="adm-btn adm-btn--primary"
                            disabled={broadcastLoading}
                            style={{ alignSelf: 'flex-start' }}
                        >
                            {broadcastLoading ? 'Se trimite...' : '📢 Trimite notificare tuturor utilizatorilor'}
                        </button>
                        {broadcastSuccess && <p className="adm-success" style={{ color: '#2e7d32', margin: '8px 0 0' }}>✅ Notificare trimisă cu succes!</p>}
                        {broadcastError && <p className="adm-error-text" style={{ color: '#c62828', margin: '8px 0 0' }}>{broadcastError}</p>}
                    </form>
                </div>
            )}
        </div>
    );
}
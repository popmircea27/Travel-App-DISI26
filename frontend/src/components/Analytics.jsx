import { useEffect, useState } from "react";
import {
    getAnalyticsOverview,
    getVisitFrequency,
} from "../services/api.js";
import "./Analytics.css";

// ─── Analytics overview card ───────────────────────────────────
function AnalyticsCard({ icon, label, value, sub, color }) {
    return (
        <div className="analytics-card" style={{ "--card-color": color }}>
            <div className="analytics-icon">{icon}</div>
            <div className="analytics-info">
                <p className="analytics-label">{label}</p>
                <p className="analytics-value">{value ?? "—"}</p>
                {sub && <p className="analytics-sub">{sub}</p>}
            </div>
        </div>
    );
}

// ─── Visit frequency chart ────────────────────────────────────
function VisitFrequencyChart({ data, title }) {
    if (!data || data.length === 0) {
        return <p className="analytics-empty">Fără date</p>;
    }

    const max = Math.max(...data.map((d) => d.total_visits || 0), 1);

    return (
        <div className="analytics-frequency">
            <h3>{title}</h3>
            <div className="frequency-bars">
                {data.map((item, idx) => (
                    <div key={idx} className="frequency-bar">
                        <div className="frequency-track">
                            <div
                                className="frequency-fill"
                                style={{
                                    height: `${(item.total_visits / max) * 100}%`,
                                }}
                            />
                        </div>
                        <span className="frequency-label">{item.time_period}</span>
                        <span className="frequency-value">{item.total_visits}</span>
                    </div>
                ))}
            </div>
        </div>
    );
}

// ─── Top locations table ──────────────────────────────────────
function TopLocationsTable({ locations }) {
    if (!locations || locations.length === 0) {
        return <p className="analytics-empty">Fără date</p>;
    }

    return (
        <div className="top-locations">
            <h3>Top locații vizitate</h3>
            <div className="top-locations-list">
                {locations.map((loc, idx) => (
                    <div key={idx} className="top-location-item">
                        <div className="top-location-rank">#{idx + 1}</div>
                        <div className="top-location-info">
                            <p className="top-location-name">{loc.location_name || "—"}</p>
                            <p className="top-location-category">{loc.category || "—"}</p>
                        </div>
                        <div className="top-location-stats">
                            <span className="stat-badge">
                                👁 {loc.total_visits} vizite
                            </span>
                            <span className="stat-badge">
                                ⭐ {(loc.average_rating || 0).toFixed(1)}
                            </span>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}

// ─── Categories breakdown ─────────────────────────────────────
function CategoriesBreakdown({ categories }) {
    if (!categories || categories.length === 0) {
        return <p className="analytics-empty">Fără date</p>;
    }

    const totalVisits = categories.reduce((sum, c) => sum + c.total_visits, 0) || 1;

    return (
        <div className="categories-breakdown">
            <h3>Categorii populare</h3>
            <div className="categories-list">
                {categories.map((cat, idx) => {
                    const percentage = ((cat.total_visits / totalVisits) * 100).toFixed(1);
                    return (
                        <div key={idx} className="category-item">
                            <div className="category-info">
                                <p className="category-name">{cat.category}</p>
                                <p className="category-stats">
                                    {cat.total_locations} locații · {cat.total_visits} vizite
                                </p>
                            </div>
                            <div className="category-bar">
                                <div
                                    className="category-bar-fill"
                                    style={{ width: `${percentage}%` }}
                                />
                            </div>
                            <span className="category-percentage">{percentage}%</span>
                        </div>
                    );
                })}
            </div>
        </div>
    );
}

// ─── Main Analytics component ──────────────────────────────────
export default function Analytics() {
    const [overview, setOverview] = useState(null);
    const [visitFrequency, setVisitFrequency] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    // Filter state
    const [frequencyType, setFrequencyType] = useState("monthly");
    const [frequencyParam, setFrequencyParam] = useState("");

    const handleLoadAnalytics = async () => {
        setLoading(true);
        setError("");
        try {
            const overviewData = await getAnalyticsOverview();
            setOverview(overviewData);
            
            // Load visit frequency data based on selected type
            const frequencyData = await getVisitFrequency(frequencyType, buildQueryParams());
            setVisitFrequency(frequencyData);
        } catch (err) {
            setError(err.message || "Eroare la încărcarea analiticii");
        } finally {
            setLoading(false);
        }
    };

    const buildQueryParams = () => {
        const params = {};
        if (frequencyType === "daily" && frequencyParam) {
            params.month = frequencyParam;
        } else if (frequencyType === "hourly" && frequencyParam) {
            params.date = frequencyParam;
        } else if (frequencyType === "monthly" && frequencyParam) {
            params.year = frequencyParam;
        }
        return params;
    };

    const handleFrequencyChange = (type) => {
        setFrequencyType(type);
        setFrequencyParam("");
    };

    useEffect(() => {
        handleLoadAnalytics();
    }, []);

    // Refetch frequency data when frequency type or param changes
    useEffect(() => {
        if (!overview) return; // Don't fetch until we have overview data
        
        const fetchFrequency = async () => {
            setLoading(true);
            try {
                const frequencyData = await getVisitFrequency(frequencyType, buildQueryParams());
                setVisitFrequency(frequencyData);
            } catch (err) {
                setError(err.message || "Eroare la încărcarea frecvenței");
            } finally {
                setLoading(false);
            }
        };

        fetchFrequency();
    }, [frequencyType, frequencyParam]);

    const topLocations = overview?.most_visited_locations || [];
    const categories = overview?.popular_categories || [];

    return (
        <div className="analytics-container">
            <div className="analytics-header">
                <h2>Analitics și statistici</h2>
                <button
                    className="btn-refresh"
                    onClick={handleLoadAnalytics}
                    disabled={loading}
                >
                    {loading ? "⏳ Se încarcă..." : "↻ Reîncarcă"}
                </button>
            </div>

            {error && (
                <div className="analytics-error">
                    <strong>Eroare:</strong> {error}
                </div>
            )}

            {/* Overview stats */}
            {loading && !overview ? (
                <div className="analytics-skeleton">Încarcă...</div>
            ) : overview ? (
                <>
                    <div className="analytics-stats">
                        <AnalyticsCard
                            icon="👁"
                            label="Total vizite"
                            value={overview.total_visits || 0}
                            color="#4285f4"
                        />
                        <AnalyticsCard
                            icon="📍"
                            label="Total locații"
                            value={overview.total_locations || 0}
                            color="#34a853"
                        />
                        <AnalyticsCard
                            icon="📂"
                            label="Total categorii"
                            value={overview.total_categories || 0}
                            color="#fbbc04"
                        />
                    </div>

                    {/* Frequency selector */}
                    <div className="frequency-selector">
                        <div>
                            <label>Frecvență vizite:</label>
                            <div className="frequency-buttons">
                                <button
                                    className={`freq-btn ${frequencyType === "hourly" ? "active" : ""}`}
                                    onClick={() => handleFrequencyChange("hourly")}
                                    disabled={loading}
                                >
                                    Pe oră
                                </button>
                                <button
                                    className={`freq-btn ${frequencyType === "daily" ? "active" : ""}`}
                                    onClick={() => handleFrequencyChange("daily")}
                                    disabled={loading}
                                >
                                    Pe zi
                                </button>
                                <button
                                    className={`freq-btn ${frequencyType === "monthly" ? "active" : ""}`}
                                    onClick={() => handleFrequencyChange("monthly")}
                                    disabled={loading}
                                >
                                    Pe lună
                                </button>
                            </div>
                        </div>
                        <div>
                            <label>
                                {frequencyType === "hourly" && "Data (YYYY-MM-DD):"}
                                {frequencyType === "daily" && "Luna (YYYY-MM):"}
                                {frequencyType === "monthly" && "An (YYYY):"}
                            </label>
                            <input
                                type="text"
                                value={frequencyParam}
                                onChange={(e) => setFrequencyParam(e.target.value)}
                                placeholder={
                                    frequencyType === "hourly"
                                        ? "2025-05-12"
                                        : frequencyType === "daily"
                                        ? "2025-05"
                                        : "2025"
                                }
                            />
                        </div>
                    </div>

                    {/* Charts grid */}
                    <div className="analytics-grid">
                        <div className="analytics-card-full">
                            {visitFrequency ? (
                                <VisitFrequencyChart
                                    data={visitFrequency}
                                    title={`Frecvență vizite (${frequencyType})`}
                                />
                            ) : (
                                <p className="analytics-empty">Fără date</p>
                            )}
                        </div>
                    </div>

                    {/* Top locations and categories */}
                    <div className="analytics-grid analytics-grid--two">
                        <div className="analytics-card-full">
                            {topLocations && topLocations.length > 0 ? (
                                <TopLocationsTable locations={topLocations} />
                            ) : (
                                <p className="analytics-empty">Fără date</p>
                            )}
                        </div>
                        <div className="analytics-card-full">
                            {categories && categories.length > 0 ? (
                                <CategoriesBreakdown
                                    categories={categories}
                                />
                            ) : (
                                <p className="analytics-empty">Fără date</p>
                            )}
                        </div>
                    </div>
                </>
            ) : null}
        </div>
    );
}

// src/pages/locationsPage/LocationsPage.jsx
//
// Filtrare client-side pe datele încărcate de la GET /api/locations.
// Când backend-ul adaugă suport pentru query params, înlocuiești
// doar apelul din fetchLocations() – UI-ul rămâne neschimbat.

import { useEffect, useState, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { getLocations } from "../../services/api.js";
import { useAuth } from "../../context/AuthContext.jsx";
import "./LocationsPage.css";

// ─── LocationCard ──────────────────────────────────────────────
function LocationCard({ location, onOpen }) {
    const shortDesc = location.description
        ? location.description.length > 120
            ? location.description.slice(0, 120).trimEnd() + "…"
            : location.description
        : "Nicio descriere disponibilă.";

    return (
        <article
            className="loc-card"
            onClick={() => onOpen(location.id)}
            onKeyDown={(e) => {
                if (e.key === "Enter" || e.key === " ") {
                    e.preventDefault();
                    onOpen(location.id);
                }
            }}
            tabIndex={0}
            role="button"
            aria-label={`Vezi detalii pentru ${location.name}`}
        >
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

            <div className="loc-card__body">
                <h2 className="loc-card__name">{location.name}</h2>
                {(location.city || location.country) && (
                    <p className="loc-card__location">
                        <span aria-hidden="true">📌</span>
                        {[location.city, location.country].filter(Boolean).join(", ")}
                    </p>
                )}
                <p className="loc-card__desc">{shortDesc}</p>
            </div>
        </article>
    );
}

// ─── Skeleton ──────────────────────────────────────────────────
function LocationsSkeleton() {
    return (
        <div className="loc-grid" aria-label="Se încarcă...">
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

// ─── Empty / Error ─────────────────────────────────────────────
function EmptyState({ hasFilters, onReset }) {
    return (
        <div className="loc-empty">
            <span className="loc-empty__icon" aria-hidden="true">
                {hasFilters ? "🔍" : "🗺️"}
            </span>
            <h2>{hasFilters ? "Niciun rezultat" : "Nicio locație găsită"}</h2>
            <p>
                {hasFilters
                    ? "Nicio locație nu corespunde filtrelor aplicate."
                    : "Nu există locații disponibile momentan. Revino mai târziu!"}
            </p>
            {hasFilters && (
                <button className="loc-reset-btn" onClick={onReset} type="button">
                    Resetează filtrele
                </button>
            )}
        </div>
    );
}

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

// ─── Bara de filtre ────────────────────────────────────────────
function FilterBar({ search, onSearch, country, onCountry, city, onCity,
                       sortBy, onSort, countries, cities, onReset, activeCount }) {
    return (
        <div className="loc-filterbar">
            {/* Search */}
            <div className="loc-filter-group loc-filter-group--search">
                <span className="loc-filter-icon" aria-hidden="true">🔍</span>
                <input
                    className="loc-filter-input"
                    type="search"
                    placeholder="Caută după nume sau descriere…"
                    value={search}
                    onChange={(e) => onSearch(e.target.value)}
                    aria-label="Caută locații"
                />
                {search && (
                    <button
                        className="loc-filter-clear"
                        onClick={() => onSearch("")}
                        type="button"
                        aria-label="Șterge căutarea"
                    >
                        ✕
                    </button>
                )}
            </div>

            {/* Country select */}
            <select
                className="loc-filter-select"
                value={country}
                onChange={(e) => { onCountry(e.target.value); onCity(""); }}
                aria-label="Filtrează după țară"
            >
                <option value="">Toate țările</option>
                {countries.map((c) => (
                    <option key={c} value={c}>{c}</option>
                ))}
            </select>

            {/* City select */}
            <select
                className="loc-filter-select"
                value={city}
                onChange={(e) => onCity(e.target.value)}
                disabled={cities.length === 0}
                aria-label="Filtrează după oraș"
            >
                <option value="">Toate orașele</option>
                {cities.map((c) => (
                    <option key={c} value={c}>{c}</option>
                ))}
            </select>

            {/* Sort */}
            <select
                className="loc-filter-select"
                value={sortBy}
                onChange={(e) => onSort(e.target.value)}
                aria-label="Sortează"
            >
                <option value="default">Sortare: implicită</option>
                <option value="name_asc">Nume A → Z</option>
                <option value="name_desc">Nume Z → A</option>
                <option value="newest">Cele mai noi</option>
            </select>

            {/* Reset */}
            {activeCount > 0 && (
                <button
                    className="loc-filter-reset"
                    onClick={onReset}
                    type="button"
                >
                    Resetează ({activeCount})
                </button>
            )}
        </div>
    );
}

// ─── Pagina principală ─────────────────────────────────────────
export default function LocationsPage() {
    const { handleLogout } = useAuth();
    const navigate = useNavigate();

    // ── Date brute de la API
    const [locations, setLocations] = useState([]);
    const [loading, setLoading]     = useState(true);
    const [error, setError]         = useState(null);

    // ── State filtre
    const [search,  setSearch]  = useState("");
    const [country, setCountry] = useState("");
    const [city,    setCity]    = useState("");
    const [sortBy,  setSortBy]  = useState("default");

    // ── Fetch
    const fetchLocations = async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await getLocations({ search, country, city, sortBy });
            setLocations(Array.isArray(data) ? data : []);
        } catch (err) {
            if (err.message.includes("401") || err.message.toLowerCase().includes("unauthorized")) {
                handleLogout();
                navigate("/login");
            } else {
                setError(err.message || "Nu s-au putut încărca locațiile.");
            }
        } finally {
            setLoading(false);
        }
    };
    /*const fetchLocations = async () => {
        setLoading(true);
        setError(null);

        try {
            // 🔥 DATE FAKE (mock)
            const data = [
                {
                    id: 1,
                    name: "Castelul Bran",
                    city: "Brașov",
                    country: "România",
                    description: "Unul dintre cele mai cunoscute castele din România.",
                    imageUrl: ""
                },
                {
                    id: 2,
                    name: "Salina Turda",
                    city: "Turda",
                    country: "România",
                    description: "O salină spectaculoasă transformată în atracție turistică.",
                    imageUrl: ""
                }
            ];

            setLocations(data);
        } catch (err) {
            setError("Eroare la încărcare");
        } finally {
            setLoading(false);
        }
    };*/

    useEffect(() => { fetchLocations(); }, []);

    // ── Liste unice pentru selecturi (derivate din date)
    const countries = useMemo(() => {
        const set = new Set(locations.map((l) => l.country).filter(Boolean));
        return [...set].sort();
    }, [locations]);

    // Orașele se filtrează după țara selectată
    const cities = useMemo(() => {
        const source = country
            ? locations.filter((l) => l.country === country)
            : locations;
        const set = new Set(source.map((l) => l.city).filter(Boolean));
        return [...set].sort();
    }, [locations, country]);

    // ── Filtrare + sortare client-side
    const filtered = useMemo(() => {
        let result = [...locations];

        // search: caută în name + description
        if (search.trim()) {
            const q = search.trim().toLowerCase();
            result = result.filter(
                (l) =>
                    l.name?.toLowerCase().includes(q) ||
                    l.description?.toLowerCase().includes(q)
            );
        }

        // country
        if (country) result = result.filter((l) => l.country === country);

        // city
        if (city) result = result.filter((l) => l.city === city);

        // sort
        if (sortBy === "name_asc")
            result.sort((a, b) => a.name?.localeCompare(b.name));
        else if (sortBy === "name_desc")
            result.sort((a, b) => b.name?.localeCompare(a.name));
        else if (sortBy === "newest")
            result.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

        return result;
    }, [locations, search, country, city, sortBy]);

    // ── Reset toate filtrele
    const resetFilters = () => {
        setSearch("");
        setCountry("");
        setCity("");
        setSortBy("default");
    };

    // Câte filtre active (fără sort)
    const activeFilterCount = [search, country, city].filter(Boolean).length;
    const hasFilters = activeFilterCount > 0;
    const openLocationDetails = (id) => navigate(`/locations/${id}`);

    return (
        <div className="loc-page">
            {/* ── Header ── */}
            <div className="loc-header">
                <h1 className="loc-title">Locații turistice</h1>
                <p className="loc-subtitle">Explorează destinații din toată România</p>
            </div>

            {/* ── Filtre – vizibile doar când datele sunt încărcate ── */}
            {!loading && !error && (
                <FilterBar
                    search={search}     onSearch={setSearch}
                    country={country}   onCountry={setCountry}
                    city={city}         onCity={setCity}
                    sortBy={sortBy}     onSort={setSortBy}
                    countries={countries}
                    cities={cities}
                    onReset={resetFilters}
                    activeCount={activeFilterCount}
                />
            )}

            {/* ── Counter rezultate ── */}
            {!loading && !error && locations.length > 0 && (
                <p className="loc-count">
                    {hasFilters
                        ? `${filtered.length} din ${locations.length} locații`
                        : `${locations.length} ${locations.length === 1 ? "locație" : "locații"} disponibile`}
                </p>
            )}

            {/* ── Stări ── */}
            {loading && <LocationsSkeleton />}

            {!loading && error && (
                <ErrorState message={error} onRetry={fetchLocations} />
            )}

            {!loading && !error && filtered.length === 0 && (
                <EmptyState hasFilters={hasFilters} onReset={resetFilters} />
            )}

            {!loading && !error && filtered.length > 0 && (
                <div className="loc-grid">
                    {filtered.map((loc) => (
                        <LocationCard key={loc.id} location={loc} onOpen={openLocationDetails} />
                    ))}
                </div>
            )}
        </div>
    );
}
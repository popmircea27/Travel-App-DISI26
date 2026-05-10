// src/pages/wishlistPage/WishlistPage.jsx
//
// API endpoints folosite:
//   GET    /api/wishlist              → lista itemelor
//   DELETE /api/wishlist/{locationId} → scoate din wishlist
//
// WishlistResponseDto câmpuri:
//   location_id, locationName, description, category, price, location_name, created_at

import { useEffect, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { getWishlist, removeFromWishlist } from "../../services/api.js";
import { useAuth } from "../../context/AuthContext.jsx";
import "./WishlistPage.css";

// ─── Card ─────────────────────────────────────────────────────
function WishlistCard({ item, onRemove, removing }) {
    const shortDesc = item.description
        ? item.description.length > 100
            ? item.description.slice(0, 100).trimEnd() + "…"
            : item.description
        : "Nicio descriere disponibilă.";

    const addedDate = item.created_at
        ? new Date(item.created_at).toLocaleDateString("ro-RO", {
            day: "numeric", month: "long", year: "numeric",
        })
        : null;

    return (
        <article className="wl-card">
            <div className="wl-card__body">
                <div className="wl-card__top">
                    <div className="wl-card__info">
                        <h2 className="wl-card__name">{item.locationName || "Locație"}</h2>

                        <div className="wl-card__meta">
                            {item.location_name && (
                                <span className="wl-card__location">
                                    <span aria-hidden="true">📌</span> {item.location_name}
                                </span>
                            )}
                            {item.category && (
                                <span className="wl-card__badge">{item.category}</span>
                            )}
                            {item.price != null && (
                                <span className="wl-card__price">
                                    {item.price === 0 ? "Gratuit" : `${item.price} RON`}
                                </span>
                            )}
                        </div>

                        <p className="wl-card__desc">{shortDesc}</p>

                        {addedDate && (
                            <p className="wl-card__date">Adăugat pe {addedDate}</p>
                        )}
                    </div>

                    <button
                        className="wl-card__remove"
                        onClick={() => onRemove(item.location_id)}
                        disabled={removing === item.location_id}
                        type="button"
                        aria-label={`Scoate ${item.locationName} din wishlist`}
                        title="Scoate din wishlist"
                    >
                        {removing === item.location_id ? "…" : "♥"}
                    </button>
                </div>

                <Link
                    to={`/locations/${item.location_id}`}
                    className="wl-card__link"
                >
                    Vezi detalii →
                </Link>
            </div>
        </article>
    );
}

// ─── Skeleton ─────────────────────────────────────────────────
function WishlistSkeleton() {
    return (
        <div className="wl-list">
            {Array.from({ length: 3 }).map((_, i) => (
                <div key={i} className="wl-card wl-card--skeleton">
                    <div className="wl-sk wl-sk--title" />
                    <div className="wl-sk wl-sk--sub" />
                    <div className="wl-sk wl-sk--line" />
                </div>
            ))}
        </div>
    );
}

// ─── Empty state ───────────────────────────────────────────────
function EmptyState() {
    return (
        <div className="wl-empty">
            <span className="wl-empty__icon" aria-hidden="true">♡</span>
            <h2>Wishlist-ul tău e gol</h2>
            <p>Adaugă locații la wishlist din pagina de detalii a fiecărei locații.</p>
            <Link to="/locations" className="wl-empty__btn">
                Explorează locații
            </Link>
        </div>
    );
}

// ─── Error state ───────────────────────────────────────────────
function ErrorState({ message, onRetry }) {
    return (
        <div className="wl-error" role="alert">
            <span aria-hidden="true">⚠</span>
            <p>{message}</p>
            <button className="wl-retry-btn" onClick={onRetry} type="button">
                Încearcă din nou
            </button>
        </div>
    );
}

// ─── Componenta principală ─────────────────────────────────────
export default function WishlistPage() {
    const { handleLogout } = useAuth();
    const navigate = useNavigate();

    const [items, setItems]     = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError]     = useState(null);
    const [removing, setRemoving] = useState(null); // locationId în curs de ștergere

    const fetchWishlist = async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await getWishlist();
            setItems(Array.isArray(data) ? data : []);
        } catch (err) {
            if (err.message.includes("401") || err.message.toLowerCase().includes("unauthorized")) {
                handleLogout();
                navigate("/login");
                return;
            }
            setError(err.message || "Nu s-a putut încărca wishlist-ul.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { fetchWishlist(); }, []);

    const handleRemove = async (locationId) => {
        setRemoving(locationId);
        try {
            await removeFromWishlist(locationId);
            // Scoatem din state local – fără re-fetch
            setItems((prev) => prev.filter((i) => i.location_id !== locationId));
        } catch (err) {
            if (err.message.includes("401") || err.message.toLowerCase().includes("unauthorized")) {
                handleLogout();
                navigate("/login");
                return;
            }
            setError(err.message || "Nu s-a putut șterge locația.");
        } finally {
            setRemoving(null);
        }
    };

    return (
        <div className="wl-page">
            <div className="wl-header">
                <h1 className="wl-title">Wishlist</h1>
                <p className="wl-subtitle">Locațiile tale salvate</p>
            </div>

            {loading && <WishlistSkeleton />}

            {!loading && error && (
                <ErrorState message={error} onRetry={fetchWishlist} />
            )}

            {!loading && !error && items.length === 0 && <EmptyState />}

            {!loading && !error && items.length > 0 && (
                <>
                    <p className="wl-count">
                        {items.length} {items.length === 1 ? "locație salvată" : "locații salvate"}
                    </p>
                    <div className="wl-list">
                        {items.map((item) => (
                            <WishlistCard
                                key={item.location_id}
                                item={item}
                                onRemove={handleRemove}
                                removing={removing}
                            />
                        ))}
                    </div>
                </>
            )}
        </div>
    );
}
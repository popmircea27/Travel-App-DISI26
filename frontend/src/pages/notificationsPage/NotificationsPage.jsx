// src/pages/notificationsPage/NotificationsPage.jsx
//
// API endpoints:
//   GET  /api/notifications          → List<NotificationDto>
//   PUT  /api/notifications/{id}/read → marchează ca citită
//
// NotificationDto câmpuri: id, title, message, isRead (read), createdAt

import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getNotifications, markNotificationAsRead } from "../../services/api.js";
import { useAuth } from "../../context/AuthContext.jsx";
import "./NotificationsPage.css";

// ─── Item notificare ───────────────────────────────────────────
function NotificationItem({ notif, onMarkRead }) {
    const date = notif.createdAt
        ? new Date(notif.createdAt).toLocaleDateString("ro-RO", {
            day: "numeric", month: "long", year: "numeric",
            hour: "2-digit", minute: "2-digit",
        })
        : null;

    return (
        <article className={`notif-item ${notif.read ? "notif-item--read" : "notif-item--unread"}`}>
            <div className="notif-item__indicator" aria-hidden="true" />

            <div className="notif-item__content">
                <div className="notif-item__header">
                    <h2 className="notif-item__title">{notif.title}</h2>
                    {!notif.read && (
                        <span className="notif-item__new" aria-label="Necitită">NOU</span>
                    )}
                </div>

                <p className="notif-item__message">{notif.message}</p>

                <div className="notif-item__footer">
                    {date && <span className="notif-item__date">{date}</span>}

                    {!notif.read && (
                        <button
                            className="notif-item__read-btn"
                            onClick={() => onMarkRead(notif.id)}
                            type="button"
                        >
                            Marchează ca citită
                        </button>
                    )}
                </div>
            </div>
        </article>
    );
}

// ─── Skeleton ─────────────────────────────────────────────────
function NotifSkeleton() {
    return (
        <div className="notif-list">
            {Array.from({ length: 4 }).map((_, i) => (
                <div key={i} className="notif-item notif-item--skeleton">
                    <div className="notif-sk notif-sk--title" />
                    <div className="notif-sk notif-sk--line" />
                    <div className="notif-sk notif-sk--line notif-sk--short" />
                </div>
            ))}
        </div>
    );
}

// ─── Empty state ───────────────────────────────────────────────
function EmptyState() {
    return (
        <div className="notif-empty">
            <span className="notif-empty__icon" aria-hidden="true">🔔</span>
            <h2>Nicio notificare</h2>
            <p>Nu ai notificări momentan. Revino mai târziu!</p>
        </div>
    );
}

// ─── Error state ───────────────────────────────────────────────
function ErrorState({ message, onRetry }) {
    return (
        <div className="notif-error" role="alert">
            <span aria-hidden="true">⚠</span>
            <p>{message}</p>
            <button className="notif-retry-btn" onClick={onRetry} type="button">
                Încearcă din nou
            </button>
        </div>
    );
}

// ─── Componenta principală ─────────────────────────────────────
export default function NotificationsPage() {
    const { handleLogout } = useAuth();
    const navigate = useNavigate();

    const [notifications, setNotifications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError]     = useState(null);
    const [filter, setFilter]   = useState("all"); // "all" | "unread" | "read"

    const fetchNotifications = async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await getNotifications();
            setNotifications(Array.isArray(data) ? data : []);
        } catch (err) {
            if (err.message.includes("401") || err.message.toLowerCase().includes("unauthorized")) {
                handleLogout();
                navigate("/login");
                return;
            }
            setError(err.message || "Nu s-au putut încărca notificările.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { fetchNotifications(); }, []);

    const handleMarkRead = async (id) => {
        try {
            await markNotificationAsRead(id);
            // Update local state fără re-fetch
            setNotifications((prev) =>
                prev.map((n) => n.id === id ? { ...n, read: true } : n)
            );
        } catch (err) {
            if (err.message.includes("401") || err.message.toLowerCase().includes("unauthorized")) {
                handleLogout();
                navigate("/login");
            }
        }
    };

    const handleMarkAllRead = async () => {
        const unread = notifications.filter((n) => !n.read);
        await Promise.allSettled(unread.map((n) => markNotificationAsRead(n.id)));
        setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
    };

    // Filtrare locală
    const filtered = notifications.filter((n) => {
        if (filter === "unread") return !n.read;
        if (filter === "read")   return n.read;
        return true;
    });

    const unreadCount = notifications.filter((n) => !n.read).length;

    return (
        <div className="notif-page">
            {/* ── Header ── */}
            <div className="notif-header">
                <div>
                    <h1 className="notif-title">
                        Notificări
                        {unreadCount > 0 && (
                            <span className="notif-badge" aria-label={`${unreadCount} necitite`}>
                                {unreadCount}
                            </span>
                        )}
                    </h1>
                    <p className="notif-subtitle">
                        {notifications.length === 0
                            ? "Nicio notificare"
                            : `${notifications.length} notificări, ${unreadCount} necitite`}
                    </p>
                </div>

                {unreadCount > 0 && !loading && (
                    <button
                        className="notif-mark-all-btn"
                        onClick={handleMarkAllRead}
                        type="button"
                    >
                        Marchează toate ca citite
                    </button>
                )}
            </div>

            {/* ── Filtre tabs ── */}
            {!loading && !error && notifications.length > 0 && (
                <div className="notif-filters" role="tablist" aria-label="Filtrează notificările">
                    {[
                        { key: "all",    label: "Toate" },
                        { key: "unread", label: "Necitite" },
                        { key: "read",   label: "Citite" },
                    ].map(({ key, label }) => (
                        <button
                            key={key}
                            role="tab"
                            aria-selected={filter === key}
                            className={`notif-filter-btn ${filter === key ? "notif-filter-btn--active" : ""}`}
                            onClick={() => setFilter(key)}
                            type="button"
                        >
                            {label}
                            {key === "unread" && unreadCount > 0 && (
                                <span className="notif-filter-count">{unreadCount}</span>
                            )}
                        </button>
                    ))}
                </div>
            )}

            {/* ── Conținut ── */}
            {loading && <NotifSkeleton />}

            {!loading && error && (
                <ErrorState message={error} onRetry={fetchNotifications} />
            )}

            {!loading && !error && notifications.length === 0 && <EmptyState />}

            {!loading && !error && notifications.length > 0 && filtered.length === 0 && (
                <div className="notif-empty">
                    <span className="notif-empty__icon" aria-hidden="true">
                        {filter === "unread" ? "✅" : "📭"}
                    </span>
                    <h2>
                        {filter === "unread" ? "Toate notificările sunt citite!" : "Nicio notificare citită"}
                    </h2>
                </div>
            )}

            {!loading && !error && filtered.length > 0 && (
                <div className="notif-list">
                    {filtered.map((n) => (
                        <NotificationItem key={n.id} notif={n} onMarkRead={handleMarkRead} />
                    ))}
                </div>
            )}
        </div>
    );
}
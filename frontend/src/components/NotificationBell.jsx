import { useState, useEffect, useRef, useCallback } from 'react';
import { createPortal } from 'react-dom';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getNotifications, markNotificationAsRead } from '../services/api';
import './NotificationBell.css';

export default function NotificationBell() {
    const { user, handleLogout } = useAuth();
    const navigate = useNavigate();

    const [notifications, setNotifications] = useState([]);
    const [isOpen, setIsOpen] = useState(false);
    const [loading, setLoading] = useState(false);
    const [dropdownPos, setDropdownPos] = useState({ top: 0, right: 0 });

    const buttonRef = useRef(null);
    const lastToggle = useRef(0);
    const ignoreClose = useRef(false);

    // --- Fetch (toate notificările, inclusiv citite) ---
    const fetchNotifications = useCallback(async () => {
        if (!user) return;
        setLoading(true);
        try {
            const data = await getNotifications();
            setNotifications(Array.isArray(data) ? data : []);
        } catch (err) {
            if (err.message?.includes('401')) {
                handleLogout();
                navigate('/login');
            } else {
                console.error('Eroare la încărcare:', err);
            }
        } finally {
            setLoading(false);
        }
    }, [user, handleLogout, navigate]);

    useEffect(() => { fetchNotifications(); }, [fetchNotifications]);

    useEffect(() => {
        if (!user) return;
        const id = setInterval(fetchNotifications, 30000);
        return () => clearInterval(id);
    }, [user, fetchNotifications]);

    useEffect(() => {
        if (isOpen) fetchNotifications();
    }, [isOpen]);

    // --- Poziționare dropdown ---
    const updatePos = () => {
        if (!buttonRef.current) return;
        const rect = buttonRef.current.getBoundingClientRect();
        setDropdownPos({
            top: rect.bottom + window.scrollY + 8,
            right: window.innerWidth - rect.right,
        });
    };

    const toggleDropdown = (e) => {
        e.stopPropagation();
        const now = Date.now();
        if (now - lastToggle.current < 200) return;
        lastToggle.current = now;
        if (!isOpen) updatePos();
        setIsOpen(prev => !prev);
    };

    // --- Închide la click afară, dar nu când se marchează ---
    useEffect(() => {
        if (!isOpen) return;
        const handleOutside = (e) => {
            if (ignoreClose.current) return;
            if (buttonRef.current && buttonRef.current.contains(e.target)) return;
            if (e.target.closest && e.target.closest('.notification-bell__dropdown')) return;
            setIsOpen(false);
        };
        const timer = setTimeout(() => {
            document.addEventListener('mousedown', handleOutside);
        }, 80);
        return () => {
            clearTimeout(timer);
            document.removeEventListener('mousedown', handleOutside);
        };
    }, [isOpen]);

    useEffect(() => {
        if (!isOpen) return;
        const update = () => updatePos();
        window.addEventListener('scroll', update, true);
        window.addEventListener('resize', update);
        return () => {
            window.removeEventListener('scroll', update, true);
            window.removeEventListener('resize', update);
        };
    }, [isOpen]);

    // --- Marcare individuală ---
    const handleMarkAsRead = async (id, e) => {
        e.stopPropagation();
        ignoreClose.current = true;
        try {
            await markNotificationAsRead(id);
            // Reîncarcă notificările după marcare
            await fetchNotifications();
        } catch (err) {
            console.error('Eroare la marcarea notificării:', err);
        } finally {
            setTimeout(() => { ignoreClose.current = false; }, 200);
        }
    };

    // --- Marcare toate necititele ---
    const handleMarkAll = async (e) => {
        e.stopPropagation();
        ignoreClose.current = true;
        const unreadIds = notifications.filter(n => !n.read).map(n => n.id);
        if (unreadIds.length === 0) return;
        try {
            await Promise.all(unreadIds.map(id => markNotificationAsRead(id)));
            await fetchNotifications();
        } catch (err) {
            console.error('Eroare la marcarea tuturor:', err);
        } finally {
            setTimeout(() => { ignoreClose.current = false; }, 200);
        }
    };

    const formatDate = (iso) => {
        if (!iso) return '';
        return new Date(iso).toLocaleDateString('ro-RO', {
            day: '2-digit', month: 'short',
            hour: '2-digit', minute: '2-digit',
        });
    };

    // Numărul notificărilor NECITITE (pentru badge)
    const unreadCount = notifications.filter(n => !n.read).length;

    if (!user) return null;

    // --- Dropdown via portal ---
    const dropdown = isOpen ? createPortal(
        <div
            className="notification-bell__dropdown"
            role="dialog"
            aria-label="Notificări"
            style={{
                position: 'absolute',
                top: dropdownPos.top,
                right: dropdownPos.right,
            }}
            onClick={(e) => e.stopPropagation()}
        >
            <div className="notification-bell__header">
                <span>Notificări necitite</span>
                <div className="notification-bell__header-actions">
                    {loading && <span className="spinner-small" aria-hidden="true" />}
                    {unreadCount > 0 && !loading && (
                        <button
                            className="notification-mark-all"
                            onClick={handleMarkAll}
                            type="button"
                        >
                            Toate citite
                        </button>
                    )}
                </div>
            </div>

            <div className="notification-bell__list">
                {loading && unreadCount === 0 && (
                    <div className="notification-loading">Se încarcă...</div>
                )}
                {!loading && unreadCount === 0 && (
                    <div className="notification-empty">
                        <span aria-hidden="true">🔔</span>
                        <p>Nu ai notificări necitite</p>
                    </div>
                )}
                {/* Afișăm doar notificările necitite */}
                {notifications
                    .filter(n => !n.read)
                    .map(notif => (
                        <div
                            key={notif.id}
                            className="notification-item unread"
                        >
                            <div className="notification-item__indicator" aria-hidden="true" />
                            <div className="notification-item__content">
                                <div className="notification-title">{notif.title}</div>
                                <div className="notification-message">{notif.message}</div>
                                <div className="notification-date">{formatDate(notif.createdAt)}</div>
                            </div>
                            <button
                                className="notification-mark-read"
                                onClick={(e) => handleMarkAsRead(notif.id, e)}
                                type="button"
                                title="Marchează ca citită"
                            >
                                ✓
                            </button>
                        </div>
                    ))}
            </div>
        </div>,
        document.body
    ) : null;

    return (
        <>
            <button
                ref={buttonRef}
                className="notification-bell__button"
                onClick={toggleDropdown}
                aria-label={`Notificări${unreadCount > 0 ? ` (${unreadCount} necitite)` : ''}`}
                aria-expanded={isOpen}
                type="button"
            >
                <span className="bell-icon" aria-hidden="true">🔔</span>
                {unreadCount > 0 && (
                    <span className="notification-bell__badge">
                        {unreadCount > 9 ? '9+' : unreadCount}
                    </span>
                )}
            </button>
            {dropdown}
        </>
    );
}
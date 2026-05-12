import { useState } from "react";
import {
    getLocations,
    createLocation,
    updateLocation,
    deleteLocation,
} from "../services/api.js";
import "./LocationManagement.css";

// ─── Location form modal ───────────────────────────────────────
function LocationFormModal({ isOpen, location, onClose, onSuccess }) {
    const [formData, setFormData] = useState(
        location || {
            name: "",
            locationName: "",
            description: "",
            category: "",
            city: "",
            country: "",
            audio_url: "",
            price: "",
            offers: "",
            latitude: "",
            longitude: "",
        }
    );
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError("");
        
        const payload = { ...formData };
        payload.latitude = payload.latitude !== "" && payload.latitude !== null ? parseFloat(payload.latitude) : null;
        payload.longitude = payload.longitude !== "" && payload.longitude !== null ? parseFloat(payload.longitude) : null;
        payload.price = payload.price !== "" && payload.price !== null ? parseFloat(payload.price) : 0;

        try {
            if (location?.id) {
                await updateLocation(location.id, payload);
            } else {
                await createLocation(payload);
            }
            onSuccess();
            onClose();
        } catch (err) {
            if (err.message && err.message.includes("400")) {
                setError("Te rugăm să completezi toate câmpurile obligatorii corect");
            } else {
                setError(err.message || "Eroare la salvare");
            }
        } finally {
            setLoading(false);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <h2>{location?.id ? "Editare locație" : "Locație nouă"}</h2>
                    <button className="modal-close" onClick={onClose}>✕</button>
                </div>

                <form onSubmit={handleSubmit} className="location-form">
                    {error && <div className="form-error">{error}</div>}

                    <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="name">Nume *</label>
                            <input
                                id="name"
                                name="name"
                                type="text"
                                value={formData.name}
                                onChange={handleChange}
                                required
                                placeholder="Ex: Castelul Bran"
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="category">Categorie *</label>
                            <select
                                id="category"
                                name="category"
                                value={formData.category}
                                onChange={handleChange}
                                required
                            >
                                <option value="">Selectează...</option>
                                <option value="Castle">Castel</option>
                                <option value="Museum">Muzeu</option>
                                <option value="Park">Parc</option>
                                <option value="Nature">Natură</option>
                                <option value="Religious">Religios</option>
                                <option value="Architecture">Arhitectură</option>
                                <option value="Citadel">Citadelă</option>
                                <option value="Other">Altele</option>
                            </select>
                        </div>
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="city">Oraș *</label>
                            <input
                                id="city"
                                name="city"
                                type="text"
                                value={formData.city || ""}
                                onChange={handleChange}
                                required
                                placeholder="Ex: Brașov"
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="locationName">Locație (detaliu) *</label>
                            <input
                                id="locationName"
                                name="locationName"
                                type="text"
                                value={formData.locationName || ""}
                                onChange={handleChange}
                                required
                                placeholder="Ex: Brașov, Județ"
                            />
                        </div>
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="country">Țară</label>
                            <input
                                id="country"
                                name="country"
                                type="text"
                                value={formData.country || ""}
                                onChange={handleChange}
                                placeholder="Ex: România"
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="price">Preț</label>
                            <input
                                id="price"
                                name="price"
                                type="number"
                                step="0.01"
                                value={formData.price || ""}
                                onChange={handleChange}
                                placeholder="Ex: 50"
                            />
                        </div>
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="latitude">Latitudine</label>
                            <input
                                id="latitude"
                                name="latitude"
                                type="number"
                                step="any"
                                value={formData.latitude}
                                onChange={handleChange}
                                placeholder="45.508889"
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="longitude">Longitudine</label>
                            <input
                                id="longitude"
                                name="longitude"
                                type="number"
                                step="any"
                                value={formData.longitude}
                                onChange={handleChange}
                                placeholder="25.568889"
                            />
                        </div>
                    </div>

                    <div className="form-group">
                        <label htmlFor="audio_url">URL Audio</label>
                        <input
                            id="audio_url"
                            name="audio_url"
                            type="url"
                            value={formData.audio_url || ""}
                            onChange={handleChange}
                            placeholder="https://..."
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="description">Descriere</label>
                        <textarea
                            id="description"
                            name="description"
                            rows={4}
                            value={formData.description}
                            onChange={handleChange}
                            placeholder="Descriere detaliată..."
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="offers">Oferte</label>
                        <textarea
                            id="offers"
                            name="offers"
                            rows={3}
                            value={formData.offers || ""}
                            onChange={handleChange}
                            placeholder="Oferte speciale, pachete..."
                        />
                    </div>

                    <div className="form-actions">
                        <button
                            type="button"
                            className="btn btn-secondary"
                            onClick={onClose}
                            disabled={loading}
                        >
                            Anulează
                        </button>
                        <button
                            type="submit"
                            className="btn btn-primary"
                            disabled={loading}
                        >
                            {loading ? "Se salvează..." : "Salvează"}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}

// ─── Confirm delete modal ──────────────────────────────────────
function DeleteConfirmModal({ isOpen, location, onConfirm, onCancel, loading }) {
    if (!isOpen) return null;

    return (
        <div className="modal-overlay" onClick={onCancel}>
            <div className="modal-content modal-content--small" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <h2>Confirmare ștergere</h2>
                </div>
                <p>Ești sigur că vrei să ștergi <strong>{location?.name}</strong>?</p>
                <p style={{ fontSize: "0.9rem", color: "var(--text-muted)", marginTop: "8px" }}>
                    Această acțiune nu poate fi anulată.
                </p>
                <div className="form-actions" style={{ justifyContent: "flex-end", gap: "8px", marginTop: "16px" }}>
                    <button
                        className="btn btn-secondary"
                        onClick={onCancel}
                        disabled={loading}
                    >
                        Anulează
                    </button>
                    <button
                        className="btn btn-danger"
                        onClick={onConfirm}
                        disabled={loading}
                    >
                        {loading ? "Se șterge..." : "Șterge"}
                    </button>
                </div>
            </div>
        </div>
    );
}

// ─── Main component ───────────────────────────────────────────
export default function LocationManagement({ onRefresh }) {
    const [locations, setLocations] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    // Modals
    const [showFormModal, setShowFormModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [selectedLocation, setSelectedLocation] = useState(null);

    // Search/filter
    const [searchQuery, setSearchQuery] = useState("");

    const handleLoadLocations = async () => {
        setLoading(true);
        setError("");
        try {
            const data = await getLocations();
            const locList = Array.isArray(data) ? data : (data?.content || []);
            setLocations(locList);
        } catch (err) {
            setError(err.message || "Eroare la încărcarea locațiilor");
        } finally {
            setLoading(false);
        }
    };

    const handleOpenForm = (location = null) => {
        setSelectedLocation(location);
        setShowFormModal(true);
    };

    const handleCloseForm = () => {
        setShowFormModal(false);
        setSelectedLocation(null);
    };

    const handleOpenDelete = (location) => {
        setSelectedLocation(location);
        setShowDeleteModal(true);
    };

    const handleCloseDelete = () => {
        setShowDeleteModal(false);
        setSelectedLocation(null);
    };

    const handleConfirmDelete = async () => {
        if (!selectedLocation?.id) return;
        setLoading(true);
        try {
            await deleteLocation(selectedLocation.id);
            handleCloseDelete();
            handleLoadLocations();
        } catch (err) {
            setError(err.message || "Eroare la ștergerea locației");
            setLoading(false);
        }
    };

    const handleFormSuccess = () => {
        handleLoadLocations();
    };

    const filteredLocations = locations.filter((loc) =>
        (loc.name || "").toLowerCase().includes(searchQuery.toLowerCase()) ||
        (loc.category || "").toLowerCase().includes(searchQuery.toLowerCase()) ||
        (loc.city || "").toLowerCase().includes(searchQuery.toLowerCase())
    );

    return (
        <div className="location-management">
            <div className="lm-header">
                <div>
                    <h2>Gestionare locații</h2>
                    <p style={{ fontSize: "0.9rem", color: "var(--text-muted)" }}>
                        {filteredLocations.length} locații
                    </p>
                </div>
                <div className="lm-actions">
                    <button
                        className="btn btn-secondary"
                        onClick={handleLoadLocations}
                        disabled={loading}
                    >
                        ↻ Reîncarcă
                    </button>
                    <button
                        className="btn btn-primary"
                        onClick={() => handleOpenForm()}
                        disabled={loading}
                    >
                        ➕ Locație nouă
                    </button>
                </div>
            </div>

            {error && (
                <div className="error-message">
                    <strong>Eroare:</strong> {error}
                </div>
            )}

            <div className="lm-search">
                <input
                    type="text"
                    placeholder="Caută după nume, categorie sau oraș..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                />
            </div>

            {loading && locations.length === 0 ? (
                <div className="loading-skeleton">
                    <p>Se încarcă...</p>
                </div>
            ) : filteredLocations.length === 0 ? (
                <div className="empty-state">
                    <p>Nicio locație găsită</p>
                </div>
            ) : (
                <div className="lm-table-wrap">
                    <table className="lm-table">
                        <thead>
                            <tr>
                                <th>Nume</th>
                                <th>Categorie</th>
                                <th>Oraș</th>
                                <th>Locație</th>
                                <th style={{ textAlign: "center" }}>Acțiuni</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredLocations.map((loc) => (
                                <tr key={loc.id}>
                                    <td className="lm-td-name">{loc.name}</td>
                                    <td>
                                        <span className="badge badge-category">{loc.category || "—"}</span>
                                    </td>
                                    <td>{loc.city || "—"}</td>
                                    <td className="lm-td-muted">{loc.locationName || "—"}</td>
                                    <td className="lm-actions-cell">
                                        <button
                                            className="btn-icon btn-icon-edit"
                                            onClick={() => handleOpenForm(loc)}
                                            title="Editează"
                                        >
                                            ✏️
                                        </button>
                                        <button
                                            className="btn-icon btn-icon-delete"
                                            onClick={() => handleOpenDelete(loc)}
                                            title="Șterge"
                                        >
                                            🗑️
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}

            {/* Modals */}
            <LocationFormModal
                isOpen={showFormModal}
                location={selectedLocation}
                onClose={handleCloseForm}
                onSuccess={handleFormSuccess}
            />
            <DeleteConfirmModal
                isOpen={showDeleteModal}
                location={selectedLocation}
                onConfirm={handleConfirmDelete}
                onCancel={handleCloseDelete}
                loading={loading}
            />
        </div>
    );
}

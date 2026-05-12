// ============================================================
// src/services/api.js
// Centralized API Service Layer
// Toate apelurile HTTP trec prin acest fișier.
// Când backend-ul e gata, schimbi doar BASE_URL și endpoint-urile.
// ============================================================

const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api";
const AI_URL = import.meta.env.VITE_AI_URL || "http://localhost:8000/api";
// ─── Helper intern ────────────────────────────────────────────
/**
 * Wrapper peste fetch cu:
 *  - JSON headers automat
 *  - Bearer token din localStorage (dacă există)
 *  - Aruncă eroare cu mesaj lizibil când status-ul nu e 2xx
 */
async function request(endpoint, options = {}) {
    const token = localStorage.getItem("token");

    const headers = {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.headers,
    };

    const config = {
        ...options,
        headers,
    };

    const response = await fetch(`${BASE_URL}${endpoint}`, config);
    // Dacă răspunsul nu e OK, aruncăm eroarea cu mesajul de la server
    if (!response.ok) {
        let errorMessage = `HTTP error! status: ${response.status}`;
        try {
            const errorData = await response.json();
            errorMessage = errorData.message || errorMessage;
        } catch {
            // dacă body-ul nu e JSON, păstrăm mesajul default
        }
        throw new Error(errorMessage);
    }

    // 204 No Content – nu are body
    if (response.status === 204) return null;

    return response.json();
}

// ─── Helper pentru AI requests ────────────────────────────────
/**
 * Wrapper pentru request-uri către AI endpoint
 * Similar cu request() dar folosește AI_URL în loc de BASE_URL
 */
async function aiRequest(endpoint, options = {}) {
    const headers = {
        "Content-Type": "application/json",
        ...options.headers,
    };

    const config = {
        ...options,
        headers,
    };

    const response = await fetch(`${AI_URL}/${endpoint}`, config);
    
    if (!response.ok) {
        let errorMessage = `AI request failed! status: ${response.status}`;
        try {
            const errorData = await response.json();
            errorMessage = errorData.message || errorMessage;
        } catch {
            // dacă body-ul nu e JSON, păstrăm mesajul default
        }
        throw new Error(errorMessage);
    }

    // 204 No Content – nu are body
    if (response.status === 204) return null;

    return response.json();
}

// ─── AUTH ─────────────────────────────────────────────────────

/**
 * Autentifică un utilizator.
 * @param {string} email
 * @param {string} password
 * @returns {Promise<{token: string, user: object}>}
 */
export async function login(email, password) {
    return request("/auth/login", {
        method: "POST",
        body: JSON.stringify({ email, password }),
    });
}

/**
 * Înregistrează un utilizator nou.
 * @param {string} email
 * @param {string} password
 * @param {string} role  - "TOURIST" | "ADMIN"
 * @returns {Promise<object>}
 */
export async function register(email, password, role = "TOURIST") {
    return request("/auth/register", {
        method: "POST",
        body: JSON.stringify({ email, password, role }),
    });
}

/**
 * Deconectează utilizatorul (șterge token-ul local).
 * Dacă backend-ul tău are un endpoint de logout, adaugă request-ul aici.
 */
export function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
}

/**
 * Solicită trimiterea email-ului de resetare parolă
 * @param {string} email
 */
export async function forgotPassword(email) {
    return request("/auth/forgot-password", {
        method: "POST",
        body: JSON.stringify({ email }),
    });
}

/**
 * Setează o parolă nouă pe baza token-ului de resetare primit
 */
export async function resetPassword(token, newPassword) {
    return request("/auth/reset-password", {
        method: "POST",
        body: JSON.stringify({ token, newPassword }),
    });
}

// ─── USER / PROFILE ───────────────────────────────────────────

/**
 * Returnează datele profilului utilizatorului autentificat.
 * @returns {Promise<object>}
 */
export async function getProfile() {
    return request("/users/me");
}

/**
 * Actualizează profilul utilizatorului autentificat.
 * @param {object} data  - câmpurile de actualizat
 * @returns {Promise<object>}
 */
export async function updateProfile(data) {
    return request("/users/me", {
        method: "PUT",
        body: JSON.stringify(data),
    });
}

/**
 * [ADMIN ONLY] Returnează lista tuturor utilizatorilor.
 * @returns {Promise<object[]>}
 */
export async function getAllUsers() {
    return request("/users");
}

/**
 * [ADMIN ONLY] Returnează detaliile unui utilizator după ID.
 * @param {string|number} id
 * @returns {Promise<object>}
 */
export async function getUserById(id) {
    return request(`/users/${id}`);
}

// ─── LOCATIONS (exemplu pentru restul echipei) ────────────────

/**
 * Returnează toate locațiile turistice.
 * @returns {Promise<object[]>}
 */
export async function getLocations() {
    return request("/locations");
}

/**
 * Returnează o locație după ID.
 * @param {string|number} id
 * @returns {Promise<object>}
 */
export async function getLocationById(id) {
    return request(`/locations/${id}`);
}

/**
 * [ADMIN ONLY] Creează o locație nouă.
 * @param {object} locationData
 * @returns {Promise<object>}
 */
export async function createLocation(locationData) {
    return request("/locations", {
        method: "POST",
        body: JSON.stringify(locationData),
    });
}

/**
 * [ADMIN ONLY] Actualizează o locație.
 * @param {string|number} id
 * @param {object} locationData
 * @returns {Promise<object>}
 */
export async function updateLocation(id, locationData) {
    return request(`/locations/${id}`, {
        method: "PUT",
        body: JSON.stringify(locationData),
    });
}

/**
 * [ADMIN ONLY] Șterge o locație.
 * @param {string|number} id
 * @returns {Promise<null>}
 */
export async function deleteLocation(id) {
    return request(`/locations/${id}`, { method: "DELETE" });
}

// ─── REVIEWS (extins cu editare/ștergere) ────────────────────
export async function getReviews(locationId) {
    return request(`/locations/${locationId}/reviews`);
}

export async function addReview(locationId, reviewData) {
    return request(`/locations/${locationId}/reviews`, {
        method: "POST",
        body: JSON.stringify(reviewData),
    });
}

export async function updateReview(reviewId, reviewData) {
    return request(`/reviews/${reviewId}`, {
        method: "PUT",
        body: JSON.stringify(reviewData),
    });
}

export async function deleteReview(reviewId) {
    return request(`/reviews/${reviewId}`, { method: "DELETE" });
}
// ─── CONTACT ──────────────────────────────────────────────────

/**
 * Trimite un mesaj de contact către admin.
 * POST /api/contact
 * Body: { subject, message }
 * Răspuns așteptat: { id, subject, message, userEmail, createdAt } sau 204
 *
 * Colega de backend va implementa endpoint-ul pe baza acestui contract.
 */
export async function sendContactMessage(subject, message) {
    return request("/contact", {
        method: "POST",
        body: JSON.stringify({ subject, message }),
    });

}
// ─── UPLOAD IMAGE (pentru admini) ────────────────────────────
export async function uploadLocationImage(locationId, file) {
    const token = localStorage.getItem("token");
    const formData = new FormData();
    formData.append("image", file);

    const response = await fetch(`${BASE_URL}/locations/${locationId}/image`, {
        method: "POST",
        headers: {
            Authorization: `Bearer ${token}`,
        },
        body: formData,
    });

    if (!response.ok) {
        let errorMessage = `Upload failed: ${response.status}`;
        try {
            const data = await response.json();
            errorMessage = data.message || errorMessage;
        } catch {}
        throw new Error(errorMessage);
    }
    return response.json();
}                          // ← acolada de închidere a uploadLocationImage

export async function uploadLocationAudio(locationId, file) {
    const token = localStorage.getItem("token");
    const formData = new FormData();
    formData.append("audio", file);

    const response = await fetch(`${BASE_URL}/locations/${locationId}/audio`, {
        method: "POST",
        headers: {
            Authorization: `Bearer ${token}`,
        },
        body: formData,
    });

    if (!response.ok) {
        let errorMessage = `Upload failed: ${response.status}`;
        try {
            const data = await response.json();
            errorMessage = data.message || errorMessage;
        } catch {}
        throw new Error(errorMessage);
    }
    return response.json();
}

// ─── WISHLIST ──────────────────────────────────────────────────

/**
 * GET /api/wishlist
 * Returnează lista locațiilor din wishlist-ul userului autentificat.
 */
export async function getWishlist() {
    return request("/wishlist");
}

/**
 * POST /api/wishlist
 * Adaugă o locație în wishlist.
 * Body: { location_id: UUID }
 */
export async function addToWishlist(locationId) {
    return request("/wishlist", {
        method: "POST",
        body: JSON.stringify({ location_id: locationId }),
    });
}

/**
 * DELETE /api/wishlist/{locationId}
 * Scoate o locație din wishlist.
 */
export async function removeFromWishlist(locationId) {
    return request(`/wishlist/${locationId}`, { method: "DELETE" });
}

/**
 * GET /api/wishlist/check/{locationId}
 * Verifică dacă o locație e în wishlist.
 * Răspuns: { inWishlist: boolean }
 */
export async function checkWishlist(locationId) {
    return request(`/wishlist/check/${locationId}`);
}

// ─── NOTIFICATIONS ────────────────────────────────────────────

/**
 * Get all notifications for the current user (authenticated).
 * GET /api/notifications
 * @returns {Promise<Array<{id: string, title: string, message: string, read: boolean, createdAt: string}>>}
 */
export async function getNotifications() {
    return request('/notifications');
}

/**
 * Mark a notification as read.
 * PUT /api/notifications/{id}/read
 * Răspuns: 200 OK fără body (conform specificației)
 */
export async function markNotificationAsRead(id) {
    const token = localStorage.getItem("token");
    const response = await fetch(
        `${import.meta.env.VITE_API_URL || "http://localhost:8080/api"}/notifications/${id}/read`,
        {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                ...(token ? { Authorization: `Bearer ${token}` } : {}),
            },
        }
    );

    if (!response.ok) {
        let errorMessage = `HTTP error! status: ${response.status}`;
        try {
            const errorData = await response.json();
            errorMessage = errorData.message || errorData.error || errorMessage;
        } catch { }
        throw new Error(errorMessage);
    }

    // 200 fără body SAU 204 — ambele sunt OK, nu parsăm JSON
    return null;
}


/**
 * Broadcast a notification to all users (ADMIN only).
 * POST /api/notifications/broadcast
 * @param {string} title
 * @param {string} message
 * @returns {Promise<{message: string}>}
 */
export async function broadcastNotification(title, message) {
    return request('/notifications/broadcast', {
        method: 'POST',
        body: JSON.stringify({ title, message }),
    });
}


// ─── AI ITINERARY PLANNER ─────────────────────────────────────

// ─── AI ITINERARY PLANNER ─────────────────────────────────────

/**
 * Generates a travel itinerary or sends a follow-up message.
 *
 * FIRST REQUEST (no sessionId provided):
 *   Sends: { city, location, days, preferences, message }
 *   Returns: { sessionId, answer }
 *
 * FOLLOW-UP REQUEST (with sessionId):
 *   Sends: { sessionId, message }
 *   Returns: { sessionId, answer }
 *
 * @param {object} data
 * @param {string} data.message - User's request or follow-up message
 * @param {string} [data.sessionId] - Session ID (only for follow-ups)
 * @param {string} [data.city] - City (only for first request)
 * @param {string} [data.location] - Location (only for first request)
 * @param {number} [data.days] - Number of days (only for first request)
 * @param {string[]} [data.preferences] - Preferences (only for first request)
 * @returns {Promise<{sessionId: string, answer: string}>}
 */
export async function generateItinerary(data) {
    // Build request body based on whether this is first request or follow-up
    let body;

    if (data.sessionId) {
        // Follow-up request: only send sessionId and message
        body = {
            sessionId: data.sessionId,
            message: data.message,
        };
    } else {
        // First request: send all form data
        body = {
            city: data.city,
            location: data.location,
            days: data.days,
            preferences: data.preferences || [],
            message: data.message,
        };
    }

    return aiRequest(`ai/itinerary`, {
        method: "POST",
        body: JSON.stringify(body),
    });
}

// ─── VISIT TRACKING ───────────────────────────────────────────

/**
 * Records a new visit to a location in the analytics_visits table.
 * Should be called when a user clicks on a location.
 * 
 * @param {string|UUID} objectiveId - The location/objective ID
 * @returns {Promise<object>}
 */
export async function recordLocationVisit(objectiveId) {
    return request("/newVisit", {
        method: "POST",
        body: JSON.stringify({ objectiveId }),
    });
}

// ─── ANALYTICS ────────────────────────────────────────────────

/**
 * Get analytics overview with:
 * - Most visited locations
 * - Popular categories
 * - Visit frequency by time
 * - Total statistics
 * 
 * Requires ADMIN role.
 * GET /api/analytics/overview
 * 
 * @returns {Promise<object>}
 */
export async function getAnalyticsOverview() {
    return request("/analytics/overview");
}

/**
 * Get visit frequency statistics filtered by time period.
 * 
 * Requires ADMIN role.
 * GET /api/analytics/visit-frequency/{frequency}
 * 
 * @param {string} frequency - "hourly", "daily", or "monthly"
 * @param {object} params - Query parameters:
 *   - For "hourly": { date: "YYYY-MM-DD" }
 *   - For "daily": { month: "YYYY-MM" }
 *   - For "monthly": { year: "YYYY" }
 * @returns {Promise<Array>}
 */
export async function getVisitFrequency(frequency, params = {}) {
    const queryString = new URLSearchParams(params).toString();
    const endpoint = `/analytics/visit-frequency/${frequency}${queryString ? '?' + queryString : ''}`;
    return request(endpoint);
}

/**
 * Get analytics for a specific location.
 * Requires ADMIN role.
 * 
 * @param {string|UUID} locationId - The location ID
 * @returns {Promise<object>}
 */
export async function getLocationAnalytics(locationId) {
    return request(`/analytics/locations/${locationId}`);
}

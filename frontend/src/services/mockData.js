// ============================================================
// src/services/mockData.js
// Date mock pentru development – folosite când backend-ul nu e gata.
// Șterge acest fișier sau nu-l mai importa când backend-ul e funcțional.
// ============================================================

export const MOCK_USERS = {
    tourist: {
        id: 1,
        email: "maria.popescu@example.com",
        firstName: "Maria",
        lastName: "Popescu",
        role: "TOURIST",
        createdAt: "2024-03-15T10:30:00Z",
        avatarInitials: "MP",
        tripsCount: 7,
        reviewsCount: 12,
        savedLocations: 5,
    },
    admin: {
        id: 2,
        email: "admin@tourapp.ro",
        firstName: "Alexandru",
        lastName: "Ionescu",
        role: "ADMIN",
        createdAt: "2023-01-10T08:00:00Z",
        avatarInitials: "AI",
        managedLocations: 34,
        totalUsers: 128,
        pendingReviews: 6,
    },
};

export const MOCK_RECENT_ACTIVITY = [
    { id: 1, type: "review", text: "Ai lăsat un review pentru Castelul Peleș", date: "Acum 2 zile" },
    { id: 2, type: "save", text: "Ai salvat Delta Dunării la favorite", date: "Acum 5 zile" },
    { id: 3, type: "trip", text: "Ai marcat vizita la Sinaia ca finalizată", date: "Acum 1 săptămână" },
];
// src/App.jsx
//
// Structura rutelor:
//
//  /            → HomePage (unificată: guest view SAU dashboard view)
//  /login       → LoginPage       (publică, fără navbar propriu)
//  /register    → RegisterPage    (publică, fără navbar propriu)
//  /locations   → LocationsPage   (publică – vizibilă și nelogat)
//  /locations/:id → LocationDetailsPage (publică)
//  /profile     → ProfilePage     (privată – necesită login)
//  /contact     → ContactPage     (privată – necesită login)
//  /ai-itinerary → AIItineraryPage (privată – necesită login)

import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";

// Layout cu navbar adaptiv
import AppLayout    from "./components/AppLayout.jsx";
import PrivateRoute from "./components/PrivateRoute.jsx";

// Pagini
import HomePage           from "./pages/homePage/HomePage.jsx";
import LoginPage          from "./pages/loginPage/LoginPage.jsx";
import RegisterPage       from "./pages/registerPage/RegisterPage.jsx";
import LocationsPage      from "./pages/locationsPage/LocationsPage.jsx";
import LocationDetailsPage from "./pages/locationDetailsPage/LocationDetailsPage.jsx";
import ProfilePage        from "./pages/profilePage/ProfilePage.jsx";
import ContactPage        from "./pages/contactPage/ContactPage.jsx";
import AIItineraryPage    from "./pages/aiItineraryPage/AIItineraryPage.jsx";
import NearbyPage from "./pages/nearbyPage/NearbyPage.jsx";
function App() {
    return (
        <AuthProvider>
            <BrowserRouter>
                <Routes>

                    {/* ── Rute cu navbar (AppLayout) ── */}

                    {/* Pagina principală – publică, conținut diferit după auth */}
                    <Route path="/" element={
                        <AppLayout><HomePage /></AppLayout>
                    } />

                    {/* Locații – publică (poate fi văzută și nelogat) */}
                    <Route path="/locations" element={
                        <AppLayout><LocationsPage /></AppLayout>
                    } />
                    <Route path="/locations/:id" element={
                        <AppLayout><LocationDetailsPage /></AppLayout>
                    } />

                    {/* Rute private – necesită login */}
                    <Route path="/profile" element={
                        <PrivateRoute>
                            <AppLayout><ProfilePage /></AppLayout>
                        </PrivateRoute>
                    } />

                    <Route path="/contact" element={
                        <PrivateRoute>
                            <AppLayout><ContactPage /></AppLayout>
                        </PrivateRoute>
                    } />
                    <Route path="/ai-itinerary" element={
                        <PrivateRoute>
                            <AppLayout><AIItineraryPage /></AppLayout>
                        </PrivateRoute>
                    } />
                    <Route path="/nearby" element={
                        <PrivateRoute>
                            <AppLayout><NearbyPage /></AppLayout>
                        </PrivateRoute>
                    } />

                    {/* ── Rute fără navbar (pagini standalone) ── */}
                    <Route path="/login"    element={<LoginPage />} />
                    <Route path="/register" element={<RegisterPage />} />

                    {/* /dashboard redirecționează la / ca să nu se rupă linkuri vechi */}
                    <Route path="/dashboard" element={<Navigate to="/" replace />} />

                </Routes>
            </BrowserRouter>
        </AuthProvider>
    );
}

export default App;
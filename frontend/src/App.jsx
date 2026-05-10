// src/App.jsx

import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";

import AppLayout    from "./components/AppLayout.jsx";
import PrivateRoute from "./components/PrivateRoute.jsx";

import HomePage            from "./pages/homePage/HomePage.jsx";
import LoginPage           from "./pages/loginPage/LoginPage.jsx";
import RegisterPage        from "./pages/registerPage/RegisterPage.jsx";
import LocationsPage       from "./pages/locationsPage/LocationsPage.jsx";
import LocationDetailsPage from "./pages/locationDetailsPage/LocationDetailsPage.jsx";
import ProfilePage         from "./pages/profilePage/ProfilePage.jsx";
import ContactPage         from "./pages/contactPage/ContactPage.jsx";
import AIItineraryPage     from "./pages/aiItineraryPage/AIItineraryPage.jsx";
import NearbyPage          from "./pages/nearbyPage/NearbyPage.jsx";
import AdminDashboardPage  from "./pages/adminDashboardPage/AdminDashboardPage.jsx";
import WishlistPage        from "./pages/wishlistPage/WishlistPage.jsx";
import NotificationsPage   from "./pages/notificationsPage/NotificationsPage.jsx";

function App() {
    return (
        <AuthProvider>
            <BrowserRouter>
                <Routes>

                    {/* ── Publice cu navbar ── */}
                    <Route path="/" element={<AppLayout><HomePage /></AppLayout>} />
                    <Route path="/locations" element={<AppLayout><LocationsPage /></AppLayout>} />
                    <Route path="/locations/:id" element={<AppLayout><LocationDetailsPage /></AppLayout>} />

                    {/* ── Private ── */}
                    <Route path="/profile" element={
                        <PrivateRoute><AppLayout><ProfilePage /></AppLayout></PrivateRoute>
                    } />
                    <Route path="/contact" element={
                        <PrivateRoute><AppLayout><ContactPage /></AppLayout></PrivateRoute>
                    } />
                    <Route path="/ai-itinerary" element={
                        <PrivateRoute><AppLayout><AIItineraryPage /></AppLayout></PrivateRoute>
                    } />
                    <Route path="/nearby" element={
                        <PrivateRoute><AppLayout><NearbyPage /></AppLayout></PrivateRoute>
                    } />
                    <Route path="/admin" element={
                        <PrivateRoute><AppLayout><AdminDashboardPage /></AppLayout></PrivateRoute>
                    } />
                    <Route path="/wishlist" element={
                        <PrivateRoute><AppLayout><WishlistPage /></AppLayout></PrivateRoute>
                    } />
                    <Route path="/notifications" element={
                        <PrivateRoute><AppLayout><NotificationsPage /></AppLayout></PrivateRoute>
                    } />

                    {/* ── Standalone (fără navbar) ── */}
                    <Route path="/login"     element={<LoginPage />} />
                    <Route path="/register"  element={<RegisterPage />} />
                    <Route path="/dashboard" element={<Navigate to="/" replace />} />

                </Routes>
            </BrowserRouter>
        </AuthProvider>
    );
}

export default App;
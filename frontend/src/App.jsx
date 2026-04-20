// src/App.jsx
// Structura rutelor:
//
//  PUBLIC  (fără navbar):
//    /              → HomePage
//    /login         → LoginPage
//    /register      → RegisterPage
//
//  PRIVATE (cu navbar AppLayout, protejate de PrivateRoute):
//    /dashboard     → DashboardPage
//    /profile       → ProfilePage

import { BrowserRouter, Routes, Route } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";

// Pagini publice
import HomePage     from "./pages/homePage/HomePage.jsx";
import LoginPage    from "./pages/loginPage/LoginPage.jsx";
import RegisterPage from "./pages/registerPage/RegisterPage.jsx";

// Pagini private
import DashboardPage from "./pages/dashboardPage/DashboardPage.jsx";
import ProfilePage   from "./pages/profilePage/ProfilePage.jsx";

// Componente de layout / protecție
import PrivateRoute from "./components/PrivateRoute.jsx";
import AppLayout    from "./components/AppLayout.jsx";

function App() {
    return (
        <AuthProvider>
            <BrowserRouter>
                <Routes>

                    {/* ── Rute PUBLICE – fără navbar ── */}
                    <Route path="/"         element={<HomePage />} />
                    <Route path="/login"    element={<LoginPage />} />
                    <Route path="/register" element={<RegisterPage />} />

                    {/* ── Rute PRIVATE – cu navbar, necesită autentificare ── */}
                    <Route
                        path="/dashboard"
                        element={
                            <PrivateRoute>
                                <AppLayout>
                                    <DashboardPage />
                                </AppLayout>
                            </PrivateRoute>
                        }
                    />
                    <Route
                        path="/profile"
                        element={
                            <PrivateRoute>
                                <AppLayout>
                                    <ProfilePage />
                                </AppLayout>
                            </PrivateRoute>
                        }
                    />

                </Routes>
            </BrowserRouter>
        </AuthProvider>
    );
}

export default App;
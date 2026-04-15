// ============================================================
// src/App.jsx
// Routing principal. Adaugă React Router dacă nu e deja instalat:
//   npm install react-router-dom
// ============================================================

import { BrowserRouter, Routes, Route, NavLink } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import ProfilePage from "./pages/profilePage/ProfilePage.jsx";
import "./App.css";

// Pagini placeholder – înlocuiți cu paginile reale
const Home = () => <div className="placeholder-page">Home</div>;
const Login = () => <div className="placeholder-page">Login</div>;

function App() {
  return (
      <AuthProvider>
        <BrowserRouter>
          <nav className="app-nav">
            <NavLink to="/" end className={({ isActive }) => isActive ? "active" : ""}>Home</NavLink>
            <NavLink to="/profile" className={({ isActive }) => isActive ? "active" : ""}>Profil</NavLink>
            <NavLink to="/login" className={({ isActive }) => isActive ? "active" : ""}>Login</NavLink>
          </nav>

          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<Login />} />
            {/* Ruta de profil – în producție va fi protejată cu PrivateRoute */}
            <Route path="/profile" element={<ProfilePage />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
  );
}

export default App;
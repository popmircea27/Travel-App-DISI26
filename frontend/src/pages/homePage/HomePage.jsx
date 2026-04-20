// src/pages/homePage/HomePage.jsx
import { Link } from "react-router-dom";
import { useAuth } from "../../context/AuthContext.jsx";
import "./HomePage.css";

export default function HomePage() {
    const { user } = useAuth();

    return (
        <div className="home-page">

            {/* ── Hero ── */}
            <section className="home-hero">
                <div className="home-hero-icon" aria-hidden="true">🌍</div>
                <h1 className="home-title">Explorează România</h1>
                <p className="home-subtitle">
                    Descoperă locuri unice, lasă review-uri și planifică-ți
                    următoarea aventură.
                </p>
                <div className="home-cta-group">
                    {user ? (
                        <Link to="/profile" className="home-btn home-btn--primary">
                            Profilul meu
                        </Link>
                    ) : (
                        <>
                            <Link to="/login" className="home-btn home-btn--primary">
                                Intră în cont
                            </Link>
                            <Link to="/register" className="home-btn home-btn--secondary">
                                Creează cont
                            </Link>
                        </>
                    )}
                </div>
            </section>

            {/* ── Feature cards ── */}
            <section className="home-features" aria-label="Ce poți face">
                <div className="home-feature-card">
                    <span className="home-feature-icon" aria-hidden="true">📍</span>
                    <h2>Locații turistice</h2>
                    <p>Browsează sute de destinații verificate din toată țara.</p>
                </div>
                <div className="home-feature-card">
                    <span className="home-feature-icon" aria-hidden="true">★</span>
                    <h2>Review-uri reale</h2>
                    <p>Citește și scrie recenzii din experiențe autentice.</p>
                </div>
                <div className="home-feature-card">
                    <span className="home-feature-icon" aria-hidden="true">♥</span>
                    <h2>Salvează favorite</h2>
                    <p>Creează-ți lista personală de locuri de vizitat.</p>
                </div>
            </section>

            {/* ── Banner dacă nu e logat ── */}
            {!user && (
                <section className="home-banner">
                    <p>
                        Ai deja un cont?{" "}
                        <Link to="/login" className="home-banner-link">
                            Loghează-te
                        </Link>{" "}
                        pentru a-ți vedea profilul și activitatea.
                    </p>
                </section>
            )}
        </div>
    );
}
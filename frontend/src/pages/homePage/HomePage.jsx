// src/pages/homePage/HomePage.jsx
// Pagina principală unificată (/ ).
// Nelogat  → mesaj de bun venit generic + carduri de prezentare + CTA login/register
// Logat    → mesaj de bun venit cu numele userului + carduri de acces rapid

import { Link } from "react-router-dom";
import { useAuth } from "../../context/AuthContext.jsx";
import "./HomePage.css";

// ── Carduri pentru userul LOGAT ────────────────────────────────
function LoggedInCards() {
    return (
        <section className="home-cards" aria-label="Acces rapid">
            <Link to="/locations" className="home-card">
                <span className="home-card__icon" aria-hidden="true">📍</span>
                <h2>Locații</h2>
                <p>Descoperă destinații turistice din România.</p>
            </Link>

            <Link to="/profile" className="home-card">
                <span className="home-card__icon" aria-hidden="true">👤</span>
                <h2>Profilul meu</h2>
                <p>Vezi detaliile contului tău: email, rol și data înregistrării.</p>
            </Link>

            <Link to="/contact" className="home-card">
                <span className="home-card__icon" aria-hidden="true">✉️</span>
                <h2>Contact</h2>
                <p>Ai o întrebare? Scrie-ne și îți răspundem cât mai curând.</p>
            </Link>
        </section>
    );
}

// ── Carduri pentru userul NELOGAT ──────────────────────────────
function GuestCards() {
    return (
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
    );
}

// ── Componenta principală ──────────────────────────────────────
export default function HomePage() {
    const { user } = useAuth();
    const displayName = user?.email?.split("@")[0] || "";

    return (
        <div className="home-page">

            {/* ── Hero ── */}
            <section className="home-hero">
                <div className="home-hero__icon" aria-hidden="true">
                    {user ? "👋" : "🌍"}
                </div>

                <h1 className="home-hero__title">
                    {user ? (
                        <>
                            Bine ai venit,{" "}
                            <span className="home-hero__name">{displayName}</span>!
                        </>
                    ) : (
                        "Explorează România"
                    )}
                </h1>

                <p className="home-hero__subtitle">
                    {user
                        ? "Ești autentificat. Explorează aplicația folosind meniul de sus sau cardurile de mai jos."
                        : "Descoperă locuri unice, lasă review-uri și planifică-ți următoarea aventură."}
                </p>


            </section>

            {/* ── Carduri diferențiate ── */}
            {user ? <LoggedInCards /> : <GuestCards />}


        </div>
    );
}
// src/pages/dashboardPage/DashboardPage.jsx

import { useAuth } from "../../context/AuthContext.jsx";
import { Link } from "react-router-dom";
import "./DashboardPage.css";

export default function DashboardPage() {
    const { user } = useAuth();
    const displayName = user?.email?.split("@")[0] || "explorator";

    return (
        <div className="dash-page">

            <section className="dash-hero">
                <div className="dash-hero__icon" aria-hidden="true">👋</div>
                <h1 className="dash-hero__title">
                    Bine ai venit, <span className="dash-hero__name">{displayName}</span>!
                </h1>
                <p className="dash-hero__subtitle">
                    Ești acum autentificat. Explorează aplicația folosind meniul de sus.
                </p>
            </section>

            <section className="dash-cards" aria-label="Acces rapid">
                <Link to="/profile" className="dash-card">
                    <span className="dash-card__icon" aria-hidden="true">👤</span>
                    <h2>Profilul meu</h2>
                    <p>Vezi detaliile contului tău: email, rol și data înregistrării.</p>
                </Link>

                {/* Locații – acum funcțional */}
                <Link to="/locations" className="dash-card">
                    <span className="dash-card__icon" aria-hidden="true">📍</span>
                    <h2>Locații</h2>
                    <p>Descoperă destinații turistice din România.</p>
                </Link>

                <div className="dash-card dash-card--soon">
                    <span className="dash-card__icon" aria-hidden="true">★</span>
                    <h2>Review-uri</h2>
                    <p>Lasă și citește recenzii despre locuri vizitate.</p>
                    <span className="dash-card__badge">În curând</span>
                </div>
            </section>
        </div>
    );
}
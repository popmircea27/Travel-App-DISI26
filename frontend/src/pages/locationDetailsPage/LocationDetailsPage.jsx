// src/pages/locationDetailsPage/LocationDetailsPage.jsx

import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { getLocationById, getReviews } from "../../services/api.js";
import { useAuth } from "../../context/AuthContext.jsx";
import "./LocationDetailsPage.css";

function LocationDetailsSkeleton() {
    return (
        <div className="ld-skeleton" aria-label="Se încarcă...">
            <div className="ld-sk ld-sk--hero" />
            <div className="ld-sk ld-sk--title" />
            <div className="ld-sk ld-sk--line" />
            <div className="ld-sk ld-sk--line ld-sk--short" />
        </div>
    );
}

function ErrorState({ message, onRetry }) {
    return (
        <div className="ld-error" role="alert">
            <span className="ld-error__icon" aria-hidden="true">⚠</span>
            <h2>A apărut o eroare</h2>
            <p>{message}</p>
            <button className="ld-btn" onClick={onRetry} type="button">
                Încearcă din nou
            </button>
        </div>
    );
}

function ReviewItem({ review }) {
    const stars = "★".repeat(Math.max(1, Math.min(5, review.rating || 0)));
    return (
        <article className="ld-review">
            <div className="ld-review__header">
                <strong>{review.userEmail || "Turist"}</strong>
                <span className="ld-review__rating" aria-label={`Rating ${review.rating || 0} din 5`}>
                    {stars}
                </span>
            </div>
            <p>{review.comment || "Fără comentariu."}</p>
        </article>
    );
}

export default function LocationDetailsPage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const { handleLogout } = useAuth();

    const [location, setLocation] = useState(null);
    const [reviews, setReviews] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchLocationDetails = async () => {
        setLoading(true);
        setError(null);

        try {
            const [locationData, reviewsData] = await Promise.all([
                getLocationById(id),
                getReviews(id),
            ]);

            setLocation(locationData || null);
            setReviews(Array.isArray(reviewsData) ? reviewsData : []);
        } catch (err) {
            if (err.message.includes("401") || err.message.toLowerCase().includes("unauthorized")) {
                handleLogout();
                navigate("/login");
                return;
            }
            setError(err.message || "Nu s-au putut încărca detaliile locației.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchLocationDetails();
    }, [id]);

    if (loading) {
        return (
            <div className="ld-page">
                <LocationDetailsSkeleton />
            </div>
        );
    }

    if (error) {
        return (
            <div className="ld-page">
                <ErrorState message={error} onRetry={fetchLocationDetails} />
            </div>
        );
    }

    if (!location) {
        return (
            <div className="ld-page">
                <ErrorState message="Locația nu a fost găsită." onRetry={() => navigate("/locations")} />
            </div>
        );
    }

    return (
        <div className="ld-page">
            <button className="ld-back" type="button" onClick={() => navigate("/locations")}>
                ← Înapoi la locații
            </button>

            <div className="ld-media">
                {location.imageUrl ? (
                    <img src={location.imageUrl} alt={location.name} className="ld-media__img" />
                ) : (
                    <div className="ld-media__placeholder" aria-hidden="true">📍</div>
                )}
            </div>

            <section className="ld-card">
                <h1>{location.name}</h1>
                <p className="ld-place">{[location.city, location.country].filter(Boolean).join(", ") || "Locație nedefinită"}</p>
                <p>{location.description || "Nu există descriere disponibilă pentru această locație."}</p>

                <div className="ld-coords">
                    <span>Lat: {location.latitude ?? "—"}</span>
                    <span>Lng: {location.longitude ?? "—"}</span>
                </div>
            </section>

            <section className="ld-card">
                <h2>Review-uri</h2>
                {reviews.length === 0 ? (
                    <p>Nu există review-uri momentan pentru această locație.</p>
                ) : (
                    <div className="ld-reviews">
                        {reviews.map((review) => (
                            <ReviewItem key={review.id} review={review} />
                        ))}
                    </div>
                )}
            </section>
        </div>
    );
}

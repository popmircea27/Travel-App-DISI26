// src/pages/locationDetailsPage/LocationDetailsPage.jsx

import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { addReview, getLocationById, getReviews } from "../../services/api.js";
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
    const REVIEWS_PER_PAGE = 4;
    const { id } = useParams();
    const navigate = useNavigate();
    const { handleLogout } = useAuth();

    const [location, setLocation] = useState(null);
    const [reviews, setReviews] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [rating, setRating] = useState(5);
    const [comment, setComment] = useState("");
    const [formError, setFormError] = useState(null);
    const [submitLoading, setSubmitLoading] = useState(false);
    const [reviewsPage, setReviewsPage] = useState(1);

    const totalReviewPages = Math.max(1, Math.ceil(reviews.length / REVIEWS_PER_PAGE));
    const paginatedReviews = reviews.slice(
        (reviewsPage - 1) * REVIEWS_PER_PAGE,
        reviewsPage * REVIEWS_PER_PAGE,
    );

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
            setReviewsPage(1);
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
    /*const fetchLocationDetails = async () => {
        setLoading(true);
        setError(null);
        try {
            const locationData = {
                id: id,
                name: id == 1 ? "Castelul Bran" : "Salina Turda",
                city: id == 1 ? "Brașov" : "Turda",
                country: "România",
                description: "Descriere detaliată pentru această locație.",
                latitude: 45.515,
                longitude: 25.367,
                imageUrl: ""
            };

            // GENEREAZĂ 10 REVIEW-URI FAKE
            const fakeReviews = [];
            for (let i = 1; i <= 10; i++) {
                fakeReviews.push({
                    id: i,
                    userEmail: `user${i}@example.com`,
                    rating: (i % 5) + 1,
                    comment: `Acesta este review-ul numărul ${i}. Lorem ipsum dolor sit amet.`
                });
            }

            setLocation(locationData);
            setReviews(fakeReviews);
            setReviewsPage(1); // asigură-te că ești pe prima pagină
        } catch (err) {
            setError("Eroare la încărcare");
        } finally {
            setLoading(false);
        }
    };*/
    useEffect(() => {
        fetchLocationDetails();
    }, [id]);

    const validateReview = () => {
        const numericRating = Number(rating);
        const trimmedComment = comment.trim();

        if (!Number.isInteger(numericRating) || numericRating < 1 || numericRating > 5) {
            return "Rating-ul trebuie să fie între 1 și 5.";
        }

        if (trimmedComment.length < 3) {
            return "Comentariul este obligatoriu și trebuie să aibă minim 3 caractere.";
        }

        return null;
    };

    const handleReviewSubmit = async (e) => {
        e.preventDefault();
        setFormError(null);

        const validationError = validateReview();
        if (validationError) {
            setFormError(validationError);
            return;
        }

        setSubmitLoading(true);
        try {
            const createdReview = await addReview(id, {
                rating: Number(rating),
                comment: comment.trim(),
            });
            setReviews((prev) => [createdReview, ...prev]);
            setReviewsPage(1);
            setRating(5);
            setComment("");
        } catch (err) {
            if (err.message.includes("401") || err.message.toLowerCase().includes("unauthorized")) {
                handleLogout();
                navigate("/login");
                return;
            }
            setFormError(err.message || "Nu s-a putut trimite review-ul.");
        } finally {
            setSubmitLoading(false);
        }
    };

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
            {(location.audioUrl || location.audio_url) && (
                <section className="ld-audio">
                    <h2>🎧 Audio ghid</h2>
                    <p className="ld-audio__desc">Ascultă ghidul audio pentru această locație.</p>
                    <audio className="ld-audio__player" controls>
                        <source src={location.audioUrl || location.audio_url} type="audio/mpeg" />
                        Browser-ul tău nu suportă redarea audio.
                    </audio>
                </section>
            )}

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

                <form className="ld-review-form" onSubmit={handleReviewSubmit} noValidate>
                    <div className="ld-review-form__row">
                        <label htmlFor="review-rating">Rating</label>
                        <select
                            id="review-rating"
                            value={rating}
                            onChange={(e) => setRating(Number(e.target.value))}
                            disabled={submitLoading}
                        >
                            <option value={5}>5 - Excelent</option>
                            <option value={4}>4 - Foarte bun</option>
                            <option value={3}>3 - Bun</option>
                            <option value={2}>2 - Slab</option>
                            <option value={1}>1 - Foarte slab</option>
                        </select>
                    </div>

                    <div className="ld-review-form__row">
                        <label htmlFor="review-comment">Comentariu</label>
                        <textarea
                            id="review-comment"
                            value={comment}
                            onChange={(e) => setComment(e.target.value)}
                            placeholder="Scrie experiența ta..."
                            rows={4}
                            disabled={submitLoading}
                        />
                    </div>

                    {formError && (
                        <p className="ld-review-form__error" role="alert">
                            {formError}
                        </p>
                    )}

                    <button className="ld-btn ld-btn--primary" type="submit" disabled={submitLoading}>
                        {submitLoading ? "Se trimite..." : "Trimite review"}
                    </button>
                </form>

                {reviews.length === 0 ? (
                    <p>Nu există review-uri momentan pentru această locație.</p>
                ) : (
                    <>
                        <div className="ld-reviews">
                            {paginatedReviews.map((review) => (
                                <ReviewItem key={review.id} review={review} />
                            ))}
                        </div>

                        <div className="ld-reviews-pagination" aria-label="Paginare review-uri">
                            <button
                                className="ld-btn"
                                type="button"
                                onClick={() => setReviewsPage((prev) => Math.max(1, prev - 1))}
                                disabled={reviewsPage === 1}
                            >
                                ← Anterior
                            </button>

                            <span className="ld-reviews-pagination__info">
                                Pagina {reviewsPage} din {totalReviewPages}
                            </span>

                            <button
                                className="ld-btn"
                                type="button"
                                onClick={() => setReviewsPage((prev) => Math.min(totalReviewPages, prev + 1))}
                                disabled={reviewsPage === totalReviewPages}
                            >
                                Următor →
                            </button>
                        </div>
                    </>
                )}
            </section>
        </div>
    );
}

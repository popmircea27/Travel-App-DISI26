// src/pages/contactPage/ContactPage.jsx
//
// Formular de contact Tourist → Admin.
// API: POST /api/contact  cu Bearer token
// Body trimis:  { subject, message }
// Răspuns OK:   { id, subject, message, userEmail, createdAt }  sau 204

import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext.jsx";
import { sendContactMessage } from "../../services/api.js";
import "./ContactPage.css";

// ─── Stări posibile ale formularului ──────────────────────────
// "idle" | "loading" | "success" | "error"

// ─── Subiecte predefinite (ușurează munca adminului) ──────────
const SUBJECTS = [
    { value: "", label: "Alege un subiect…" },
    { value: "Întrebare generală", label: "Întrebare generală" },
    { value: "Problemă cu o locație", label: "Problemă cu o locație" },
    { value: "Raportează un review", label: "Raportează un review" },
    { value: "Problemă tehnică", label: "Problemă tehnică" },
    { value: "Altele", label: "Altele" },
];

const MAX_MESSAGE = 1000;

// ─── Ecran de succes ───────────────────────────────────────────
function SuccessScreen({ onReset }) {
    return (
        <div className="contact-success" role="status">
            <div className="contact-success__icon" aria-hidden="true">✅</div>
            <h2 className="contact-success__title">Mesaj trimis!</h2>
            <p className="contact-success__text">
                Mesajul tău a fost transmis adminului. Vei primi un răspuns în
                cel mai scurt timp posibil.
            </p>
            <button
                className="contact-btn contact-btn--secondary"
                onClick={onReset}
                type="button"
            >
                Trimite alt mesaj
            </button>
        </div>
    );
}

// ─── Componenta principală ─────────────────────────────────────
export default function ContactPage() {
    const { user, handleLogout } = useAuth();
    const navigate = useNavigate();

    const [subject, setSubject]   = useState("");
    const [message, setMessage]   = useState("");
    const [errors, setErrors]     = useState({});
    const [status, setStatus]     = useState("idle"); // idle | loading | success | error
    const [apiError, setApiError] = useState("");

    // ── Validare ────────────────────────────────────────────────
    const validate = () => {
        const e = {};
        if (!subject) e.subject = "Te rugăm să alegi un subiect.";
        if (!message.trim()) e.message = "Mesajul nu poate fi gol.";
        else if (message.trim().length < 10) e.message = "Mesajul trebuie să aibă cel puțin 10 caractere.";
        return e;
    };

    // ── Submit ───────────────────────────────────────────────────
    const handleSubmit = async (e) => {
        e.preventDefault();
        const errs = validate();
        if (Object.keys(errs).length > 0) {
            setErrors(errs);
            return;
        }

        setErrors({});
        setStatus("loading");
        setApiError("");

        try {
            // POST /api/contact – token adăugat automat de request() din api.js
            await sendContactMessage(subject, message.trim());
            setStatus("success");
        } catch (err) {
            // Token expirat → logout
            if (
                err.message.includes("401") ||
                err.message.toLowerCase().includes("unauthorized")
            ) {
                handleLogout();
                navigate("/login");
                return;
            }
            setApiError(err.message || "A apărut o eroare. Încearcă din nou.");
            setStatus("error");
        }
    };

    // ── Reset formular după succes ───────────────────────────────
    const handleReset = () => {
        setSubject("");
        setMessage("");
        setErrors({});
        setApiError("");
        setStatus("idle");
    };

    const charsLeft = MAX_MESSAGE - message.length;
    const isLoading = status === "loading";

    // ── Ecran de succes ─────────────────────────────────────────
    if (status === "success") {
        return (
            <div className="contact-page">
                <SuccessScreen onReset={handleReset} />
            </div>
        );
    }

    // ── Formular ────────────────────────────────────────────────
    return (
        <div className="contact-page">
            <div className="contact-container">

                {/* Header */}
                <div className="contact-header">
                    <span className="contact-header__icon" aria-hidden="true">✉️</span>
                    <div>
                        <h1 className="contact-header__title">Contactează adminul</h1>
                        <p className="contact-header__sub">
                            Ai o întrebare sau o problemă? Scrie-ne și îți răspundem cât mai curând.
                        </p>
                    </div>
                </div>

                {/* Info user logat */}
                {user?.email && (
                    <div className="contact-user-info">
                        <span aria-hidden="true">👤</span>
                        Vei trimite ca: <strong>{user.email}</strong>
                    </div>
                )}

                {/* Eroare API */}
                {status === "error" && (
                    <div className="contact-alert contact-alert--error" role="alert">
                        <span aria-hidden="true">⚠</span> {apiError}
                    </div>
                )}

                {/* Formular */}
                <form className="contact-form" onSubmit={handleSubmit} noValidate>

                    {/* Subiect */}
                    <div className="contact-field">
                        <label className="contact-label" htmlFor="contact-subject">
                            Subiect <span className="contact-required" aria-hidden="true">*</span>
                        </label>
                        <select
                            id="contact-subject"
                            className={`contact-select ${errors.subject ? "contact-select--error" : ""}`}
                            value={subject}
                            onChange={(e) => {
                                setSubject(e.target.value);
                                if (errors.subject) setErrors((p) => ({ ...p, subject: "" }));
                            }}
                            disabled={isLoading}
                            aria-describedby={errors.subject ? "subject-err" : undefined}
                            aria-invalid={!!errors.subject}
                        >
                            {SUBJECTS.map((s) => (
                                <option key={s.value} value={s.value} disabled={s.value === ""}>
                                    {s.label}
                                </option>
                            ))}
                        </select>
                        {errors.subject && (
                            <span id="subject-err" className="contact-error" role="alert">
                                {errors.subject}
                            </span>
                        )}
                    </div>

                    {/* Mesaj */}
                    <div className="contact-field">
                        <label className="contact-label" htmlFor="contact-message">
                            Mesaj <span className="contact-required" aria-hidden="true">*</span>
                        </label>
                        <textarea
                            id="contact-message"
                            className={`contact-textarea ${errors.message ? "contact-textarea--error" : ""}`}
                            placeholder="Descrie problema sau întrebarea ta cât mai detaliat…"
                            value={message}
                            onChange={(e) => {
                                if (e.target.value.length <= MAX_MESSAGE) {
                                    setMessage(e.target.value);
                                    if (errors.message) setErrors((p) => ({ ...p, message: "" }));
                                }
                            }}
                            rows={6}
                            disabled={isLoading}
                            aria-describedby={errors.message ? "message-err" : "message-counter"}
                            aria-invalid={!!errors.message}
                        />
                        <div className="contact-textarea-footer">
                            {errors.message ? (
                                <span id="message-err" className="contact-error" role="alert">
                                    {errors.message}
                                </span>
                            ) : (
                                <span />
                            )}
                            <span
                                id="message-counter"
                                className={`contact-counter ${charsLeft < 50 ? "contact-counter--warn" : ""}`}
                                aria-live="polite"
                            >
                                {charsLeft} caractere rămase
                            </span>
                        </div>
                    </div>

                    {/* Submit */}
                    <button
                        type="submit"
                        className="contact-btn contact-btn--primary"
                        disabled={isLoading}
                    >
                        {isLoading ? (
                            <>
                                <span className="contact-spinner" aria-hidden="true" />
                                Se trimite…
                            </>
                        ) : (
                            "Trimite mesajul"
                        )}
                    </button>
                </form>
            </div>
        </div>
    );
}
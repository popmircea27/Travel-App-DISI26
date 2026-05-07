import { useState, useEffect, useRef } from "react";
import { generateItinerary } from "../../services/api.js";
import "./AIItineraryPage.css";

export default function AIItineraryPage() {
    // ─── Form State (for initial itinerary generation) ───
    const [formData, setFormData] = useState({
        city: "",
        location: "",
        days: 3,
        preferences: [],
    });

    // ─── Chat/Conversation State ───
    const [messages, setMessages] = useState([]);
    const [sessionId, setSessionId] = useState(null);
    const [userInput, setUserInput] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);
    const messagesEndRef = useRef(null);

    // ─── UI State ───
    const hasStartedConversation = messages.length > 0;

    // Auto-scroll to bottom when messages change
    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    }, [messages]);

    // Handle form input changes
    const handleFormChange = (e) => {
        const { name, value, type, checked } = e.target;

        if (type === "checkbox") {
            setFormData((prev) => ({
                ...prev,
                preferences: checked
                    ? [...prev.preferences, value]
                    : prev.preferences.filter((p) => p !== value),
            }));
        } else {
            setFormData((prev) => ({
                ...prev,
                [name]: name === "days" ? parseInt(value) || 0 : value,
            }));
        }

        setError(null);
    };

    // Handle initial itinerary generation
    const handleGenerateItinerary = async (e) => {
        e.preventDefault();

        if (!formData.city.trim() || !formData.location.trim() || formData.days < 1) {
            setError("Please fill in all fields correctly");
            return;
        }

        const userMessage = `Plan a ${formData.days}-day itinerary for ${formData.city}, ${formData.location}${formData.preferences.length > 0 ? ` with preferences: ${formData.preferences.join(", ")}` : ""}`;

        // Add user message to chat immediately
        setMessages([
            {
                id: Date.now(),
                role: "user",
                content: userMessage,
            },
        ]);

        setIsLoading(true);
        setError(null);

        try {
            // Send first request without sessionId
            const response = await generateItinerary({
                city: formData.city,
                location: formData.location,
                days: formData.days,
                preferences: formData.preferences,
                message: userMessage,
            });

            // Save sessionId from response
            setSessionId(response.sessionId);

            // Add AI response to chat
            setMessages((prev) => [
                ...prev,
                {
                    id: Date.now() + 1,
                    role: "assistant",
                    content: response.answer,
                },
            ]);
        } catch (err) {
            setError(err.message || "Failed to generate itinerary");
            // Keep the user message, show error
            setMessages((prev) => [
                ...prev,
                {
                    id: Date.now() + 1,
                    role: "error",
                    content: err.message || "Failed to generate itinerary",
                },
            ]);
        } finally {
            setIsLoading(false);
        }
    };

    // Handle follow-up messages
    const handleSendMessage = async (e) => {
        e.preventDefault();

        if (!userInput.trim() || !sessionId) return;

        const messageContent = userInput.trim();

        // Add user message to chat immediately
        setMessages((prev) => [
            ...prev,
            {
                id: Date.now(),
                role: "user",
                content: messageContent,
            },
        ]);

        setUserInput("");
        setIsLoading(true);
        setError(null);

        try {
            // Send follow-up request with sessionId
            const response = await generateItinerary({
                sessionId: sessionId,
                message: messageContent,
            });

            // Add AI response to chat
            setMessages((prev) => [
                ...prev,
                {
                    id: Date.now() + 1,
                    role: "assistant",
                    content: response.answer,
                },
            ]);
        } catch (err) {
            setError(err.message || "Failed to send message");
            setMessages((prev) => [
                ...prev,
                {
                    id: Date.now() + 1,
                    role: "error",
                    content: err.message || "Failed to send message",
                },
            ]);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="ai-itinerary-page">
            <div className="ai-itinerary-container">
                {/* Header */}
                <div className="ai-itinerary-header">
                    <h1 className="ai-itinerary-title">
                        <span className="ai-itinerary-icon">🤖</span>
                        AI Itinerary Planner
                    </h1>
                    <p className="ai-itinerary-subtitle">
                        {hasStartedConversation
                            ? "Continue refining your itinerary or ask follow-up questions"
                            : "Create a personalized travel itinerary with AI"}
                    </p>
                </div>

                {/* Form Section */}
                {!hasStartedConversation ? (
                    <form onSubmit={handleGenerateItinerary} className="ai-itinerary-form">
                        <div className="ai-form-group">
                            <label htmlFor="city" className="ai-form-label">
                                City
                            </label>
                            <input
                                id="city"
                                type="text"
                                name="city"
                                className="ai-form-input"
                                placeholder="e.g., Cluj-Napoca"
                                value={formData.city}
                                onChange={handleFormChange}
                                disabled={isLoading}
                            />
                        </div>

                        <div className="ai-form-group">
                            <label htmlFor="location" className="ai-form-label">
                                Region/Location
                            </label>
                            <input
                                id="location"
                                type="text"
                                name="location"
                                className="ai-form-input"
                                placeholder="e.g., Transylvania"
                                value={formData.location}
                                onChange={handleFormChange}
                                disabled={isLoading}
                            />
                        </div>

                        <div className="ai-form-group">
                            <label htmlFor="days" className="ai-form-label">
                                Number of Days
                            </label>
                            <input
                                id="days"
                                type="number"
                                name="days"
                                className="ai-form-input"
                                min="1"
                                max="30"
                                value={formData.days}
                                onChange={handleFormChange}
                                disabled={isLoading}
                            />
                        </div>

                        <div className="ai-form-group">
                            <label className="ai-form-label">Preferences (optional)</label>
                            <div className="ai-form-checkboxes">
                                {["Adventure", "Culture", "Food", "Nature", "Budget", "Luxury"].map(
                                    (pref) => (
                                        <label key={pref} className="ai-form-checkbox-label">
                                            <input
                                                type="checkbox"
                                                name="preferences"
                                                value={pref}
                                                checked={formData.preferences.includes(pref)}
                                                onChange={handleFormChange}
                                                disabled={isLoading}
                                            />
                                            <span>{pref}</span>
                                        </label>
                                    )
                                )}
                            </div>
                        </div>

                        {error && <div className="ai-form-error">{error}</div>}

                        <button
                            type="submit"
                            className="ai-form-submit"
                            disabled={isLoading || !formData.city.trim() || !formData.location.trim()}
                        >
                            {isLoading ? "Generating..." : "Generate Itinerary"}
                        </button>
                    </form>
                ) : (
                    <>
                        {/* Chat Area */}
                        <div className="ai-chat-area">
                            <div className="ai-chat-messages">
                                {messages.map((message) => (
                                    <div
                                        key={message.id}
                                        className={`ai-chat-message ai-chat-message--${message.role}`}
                                    >
                                        <div className="ai-chat-message-avatar">
                                            {message.role === "user" && <span>👤</span>}
                                            {message.role === "assistant" && <span>🤖</span>}
                                            {message.role === "error" && <span>⚠️</span>}
                                        </div>
                                        <div className="ai-chat-message-bubble">
                                            {message.content}
                                        </div>
                                    </div>
                                ))}

                                {/* Loading indicator */}
                                {isLoading && (
                                    <div className="ai-chat-message ai-chat-message--assistant">
                                        <div className="ai-chat-message-avatar">
                                            <span>🤖</span>
                                        </div>
                                        <div className="ai-chat-message-bubble ai-chat-loading">
                                            <span className="ai-loading-dot"></span>
                                            <span className="ai-loading-dot"></span>
                                            <span className="ai-loading-dot"></span>
                                        </div>
                                    </div>
                                )}

                                <div ref={messagesEndRef} />
                            </div>
                        </div>

                        {/* Input Form for Follow-up Messages */}
                        <form onSubmit={handleSendMessage} className="ai-chat-input-form">
                            {error && <div className="ai-chat-error-bar">{error}</div>}
                            <div className="ai-chat-input-wrapper">
                                <input
                                    type="text"
                                    className="ai-chat-input"
                                    placeholder="Ask a follow-up question or request changes..."
                                    value={userInput}
                                    onChange={(e) => {
                                        setUserInput(e.target.value);
                                        setError(null);
                                    }}
                                    disabled={isLoading}
                                    aria-label="Follow-up message input"
                                />
                                <button
                                    type="submit"
                                    className="ai-chat-send-btn"
                                    disabled={isLoading || !userInput.trim()}
                                    aria-label="Send message"
                                >
                                    {isLoading ? "..." : "Send"}
                                </button>
                            </div>
                        </form>
                    </>
                )}
            </div>
        </div>
    );
}

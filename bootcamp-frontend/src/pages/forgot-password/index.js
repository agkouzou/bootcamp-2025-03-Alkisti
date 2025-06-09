import Head from "next/head";
import { useState } from "react";
import axios from "axios";

export default function ForgotPasswordPage() {
    const [email, setEmail] = useState("");
    const [message, setMessage] = useState(null);
    const [loading, setLoading] = useState(false);

    function handleReturnToLoginClick() {
        setTimeout(() => {
            window.location.href = "/login";
        }, 200);
    }

    async function handleSubmit(e) {
        e.preventDefault();
        setLoading(true);
        setMessage(null);
        try {
            await axios.post("http://localhost:8080/users/password-reset-request", { email });
            setMessage(
                <>
                    If the email exists, a reset link has been sent. You may now{" "}
                    <span
                        onClick={handleReturnToLoginClick}
                        style={{
                            color: "blue",
                            textDecoration: "underline",
                            cursor: "pointer",
                        }}
                    >
                        return to login
                    </span>
                    .
                </>
            );
            setEmail(""); // <-- Clear email input once successful
        } catch (error) {
            setMessage("Error sending reset link, please try again.");
        }
        setLoading(false);
    }

    return (
        <>
            <Head>
                <title>Forgot Password - Chat Application</title>
                <meta name="description" content="Reset your password securely" />
                <meta name="viewport" content="width=device-width, initial-scale=1" />
                <link rel="icon" href="/favicon.ico" />
            </Head>

            <div className="page-container">
                <header>
                    <div className="header-content">
                        <div className="header-brand">
                            <img src="./bootcamp-2025.03-logo.jpg" alt="Logo" className="header-logo" />
                            <div className="header-title">Chat Application</div>
                        </div>
                    </div>
                </header>

                <div className="content">
                    <div className="form-section single-form">
                        <h1>Forgot Password</h1>
                        <form onSubmit={handleSubmit}>
                            <div className="form-group">
                                <label htmlFor="forgot-email">Email</label>
                                <input
                                    id="forgot-email"
                                    type="email"
                                    placeholder="you@example.com"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    required
                                    disabled={loading}
                                />
                            </div>
                            <div className="form-actions">
                                <button type="submit" className="btn btn-primary" disabled={loading}>
                                    {loading ? "Sending..." : "Send Reset Link"}
                                </button>
                                <a
                                    href="/login"
                                    className="btn btn-secondary"
                                    style={{ marginLeft: "10px", textDecoration: "none" }}
                                >
                                    Cancel
                                </a>
                            </div>
                            {message && <p>{message}</p>}
                        </form>
                    </div>
                </div>

                <footer>© 2025 Chat App, Inc.</footer>
            </div>
        </>
    );
}

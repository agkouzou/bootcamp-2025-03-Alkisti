import Head from "next/head";
import { useRouter } from "next/router";
import { useEffect, useState } from "react";
import axios from "axios";

export default function ResetPasswordPage() {
    const router = useRouter();

    const [token, setToken] = useState(null);
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [message, setMessage] = useState("");
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (router.isReady) {
            const t = router.query.token;
            if (t) {
                setToken(t);
            } else {
                setMessage("Invalid or missing token.");
            }
        }
    }, [router.isReady, router.query.token]);

    async function handleSubmit(e) {
        e.preventDefault();
        setMessage("");

        if (password !== confirmPassword) {
            setMessage("Passwords do not match.");
            return;
        }

        setLoading(true);

        try {
            await axios.post("http://localhost:8080/users/password-reset", {
                token,
                newPassword: password,
            });

            setPassword("");
            setConfirmPassword("");
            setMessage("Password reset successful! Redirecting to login...");

            setTimeout(() => {
                router.push("/login");
            }, 3000);
        } catch (error) {
            setMessage("Failed to reset password. The token may be invalid or expired.");
        } finally {
            setLoading(false);
        }
    }

    return (
        <>
            <Head>
                <title>Reset Password - Chat Application</title>
                <meta name="description" content="Set a new password for your account" />
                <meta name="viewport" content="width=device-width, initial-scale=1" />
                <link rel="icon" href="/favicon.ico" />
            </Head>

            <div className="page-container">
                <header>
                    <div className="header-content">
                        <div className="header-brand">
                            <img
                                src="./bootcamp-2025.03-logo.jpg"
                                alt="Logo"
                                className="header-logo"
                            />
                            <div className="header-title">Chat Application</div>
                        </div>
                    </div>
                </header>

                <div className="content">
                    <div className="account-settings">
                        <h1>Reset Password</h1>
                        <form onSubmit={handleSubmit}>
                            <div className="form-group">
                                <label htmlFor="reset-password">New Password</label>
                                <input
                                    id="reset-password"
                                    type="password"
                                    placeholder="••••••••"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    required
                                    minLength={8}
                                    disabled={loading}
                                />
                            </div>
                            <div className="form-group">
                                <label htmlFor="reset-confirm">Confirm Password</label>
                                <input
                                    id="reset-confirm"
                                    type="password"
                                    placeholder="••••••••"
                                    value={confirmPassword}
                                    onChange={(e) => setConfirmPassword(e.target.value)}
                                    required
                                    minLength={8}
                                    disabled={loading}
                                />
                            </div>
                            <div className="form-actions">
                                <button
                                    type="submit"
                                    className="btn btn-primary"
                                    disabled={loading}
                                >
                                    {loading ? "Resetting..." : "Reset Password"}
                                </button>
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
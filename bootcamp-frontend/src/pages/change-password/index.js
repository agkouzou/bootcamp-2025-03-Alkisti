import Head from "next/head";
import { useEffect, useState } from "react";
import axios from "axios";
import Link from "next/link";

function parseJwt(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(
            atob(base64)
                .split('')
                .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
                .join('')
        );
        return JSON.parse(jsonPayload);
    } catch (e) {
        console.error("Failed to parse JWT", e);
        return null;
    }
}

export default function ChangePasswordPage() {
    const [currentPassword, setCurrentPassword] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [successMessage, setSuccessMessage] = useState("");
    const [errorMessage, setErrorMessage] = useState("");
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        const token = localStorage.getItem("authToken");

        if (!token) {
            console.warn("No token found, redirecting to login...");
            window.location.href = "/login";
            return;
        }

        const decoded = parseJwt(token);
        console.log("Decoded JWT:", decoded);

        const userId = parseInt(decoded?.sub);

        if (!userId || isNaN(userId)) {
            console.error("Invalid user ID from token, redirecting...");
            window.location.href = "/login";
            return;
        }

        // Fetch user data (if needed for anything else)
        const fetchData = async () => {
            try {
                const response = await axios.get(`http://localhost:8080/users/${userId}`, {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                });
                const data = response.data;
                // No need to pre-populate fields anymore
            } catch (e) {
                if (e.response?.status === 401) {
                    console.warn("Unauthorized. Redirecting to login.");
                    localStorage.removeItem("authToken");
                    window.location.href = "/login";
                } else {
                    console.error("Failed to fetch user info", e);
                }
            }
        };

        fetchData();
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();

        // Reset messages
        setSuccessMessage("");
        setErrorMessage("");

        setLoading(true);

        await new Promise((resolve) => setTimeout(resolve, 1000));

        // Validate password fields
        if (!currentPassword || !newPassword || !confirmPassword) {
            setErrorMessage("All fields are required!");
            setLoading(false);
            return;
        }

        if (newPassword !== confirmPassword) {
            setErrorMessage("New password and confirm password do not match!");
            setLoading(false);
            return;
        }

        const token = localStorage.getItem("authToken");

        const decoded = parseJwt(token);
        const userId = parseInt(decoded?.sub);

        if (!userId || isNaN(userId)) {
            setErrorMessage("User ID not found in token.");
            setLoading(false);
            return;
        }

        const userData = {
            oldPassword: currentPassword,
            newPassword
        };

        try {
            await axios.patch(`http://localhost:8080/users/${userId}/change-password`, userData, {
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json"
                }
            });
            setSuccessMessage("Password changed successfully!"); // Set success message

            // Reset fields after success
            setCurrentPassword("");
            setNewPassword("");
            setConfirmPassword("");

            setTimeout(() => {
                window.location.reload();
            }, 2000);

        } catch (e) {
            if (e.response) {
                setErrorMessage(e.response.data.message || 'An error occurred');
            } else {
                setErrorMessage("Failed to change password");
            }
        } finally {
            setLoading(false);
        }
    };

    const handleAccount = () => {
        window.location.href = "/account-settings";
    };

    const handlePassword = () => {
        window.location.href = "/change-password";
    };

    const handleLogout = () => {
        localStorage.removeItem("authToken");
        window.location.href = "/login";
    };

    return (
        <>
            <Head>
                <title>Change Password</title>
                <meta name="description" content="Update your password" />
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
                        <div className="profile-dropdown">
                            <input type="checkbox" id="profile-toggle" />
                            <label htmlFor="profile-toggle" className="profile-icon">JD</label>
                            <div className="dropdown-menu">
                                <a href="/account-settings" onClick={(e) => { e.preventDefault(); handleAccount(); }}>
                                    Account Settings
                                </a>
                                <a href="/change-password" onClick={(e) => { e.preventDefault(); handlePassword(); }}>
                                    Change Password
                                </a>
                                <a href="/login" onClick={(e) => { e.preventDefault(); handleLogout(); }}>
                                    Logout
                                </a>
                            </div>
                            <label htmlFor="profile-toggle" className="overlay"></label>
                        </div>
                    </div>
                </header>
                <div className="content">
                    <div className="account-settings">
                        <h1>Change Password</h1>
                        <Link href="/chat">
                            <button
                                type="button"
                                className="submit-btn"
                                onClick={(e) => {
                                    e.preventDefault();
                                    window.location.href = '/chat';
                                }}
                            >
                                <span>←</span> Back to Chat
                            </button>
                        </Link>
                        <form onSubmit={handleSubmit}>
                            <div className="form-group">
                                <label htmlFor="currentPassword">Current Password</label>
                                <input
                                    type="password"
                                    value={currentPassword}
                                    onChange={(e) => setCurrentPassword(e.target.value)}
                                    required
                                    className="input-field"
                                />
                            </div>
                            <div className="form-group">
                                <label htmlFor="newPassword">New Password</label>
                                <input
                                    type="password"
                                    value={newPassword}
                                    onChange={(e) => setNewPassword(e.target.value)}
                                    required
                                    className="input-field"
                                />
                            </div>
                            <div className="form-group">
                                <label htmlFor="confirmPassword">Confirm New Password</label>
                                <input
                                    type="password"
                                    value={confirmPassword}
                                    onChange={(e) => setConfirmPassword(e.target.value)}
                                    required
                                    className="input-field"
                                />
                            </div>
                            <button type="submit" className="submit-btn" disabled={loading}>
                                {loading ? "Processing..." : "Change Password"}
                            </button>
                            {(successMessage || errorMessage) && !loading && (
                                <div style={{ marginTop: "10px", fontSize: "14px" }}>
                                    {successMessage || errorMessage}
                                </div>
                            )}
                        </form>
                    </div>
                </div>
                <footer>© 2025 Chat App, Inc.</footer>
            </div>
        </>
    );
}

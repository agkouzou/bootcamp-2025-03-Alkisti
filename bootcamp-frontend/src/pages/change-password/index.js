import Head from "next/head";
import { useEffect, useState } from "react";
import axios from "axios";

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

function getInitials(name) {
    if (!name || typeof name !== "string") return "";
    const cleanedName = name.trim();
    if (cleanedName === "" || cleanedName.toLowerCase() === "undefined") return "";
    const parts = cleanedName.split(/\s+/);
    if (parts.length === 1) return parts[0][0].toUpperCase();
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
}

export default function ChangePasswordPage() {
    const [currentPassword, setCurrentPassword] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");

    const [successMessage, setSuccessMessage] = useState("");
    const [errorMessage, setErrorMessage] = useState("");
    const [loading, setLoading] = useState(false);

    const [userName, setUserName] = useState("");
    const [userId, setUserId] = useState(null);
    const [token, setToken] = useState("");

    // On mount: validate token, fetch user data
    useEffect(() => {
        const storedToken = localStorage.getItem("authToken");

        if (!storedToken) {
            console.warn("No token found, redirecting...");
            window.location.href = "/login";
            return;
        }

        const decoded = parseJwt(storedToken);
        const uid = parseInt(decoded?.sub);

        if (!uid || isNaN(uid)) {
            console.error("Invalid user ID from token.");
            window.location.href = "/login";
            return;
        }

        setUserId(uid);
        setToken(storedToken);

        // Fetch user data
        axios.get(`http://localhost:8080/users/${uid}`, {
            headers: { Authorization: `Bearer ${storedToken}` },
        })
            .then((res) => {
                const name = res.data?.name;
                setUserName(name && name !== "undefined" ? name : "");
            })
            .catch((err) => {
                if (err.response?.status === 401) {
                    console.warn("Unauthorized. Redirecting...");
                    localStorage.removeItem("authToken");
                    window.location.href = "/login";
                } else {
                    console.error("Error fetching user:", err);
                }
            });
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSuccessMessage("");
        setErrorMessage("");
        setLoading(true);

        // Validation
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

        try {
            await axios.patch(`http://localhost:8080/users/${userId}/change-password`, {
                oldPassword: currentPassword,
                newPassword,
            }, {
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                }
            });

            setSuccessMessage("Password changed successfully!");
            setCurrentPassword("");
            setNewPassword("");
            setConfirmPassword("");

            setTimeout(() => window.location.reload(), 2000);

        } catch (e) {
            if (e.response) {
                setErrorMessage(e.response.data.message || "An error occurred");
            } else {
                setErrorMessage("Failed to change password");
            }
        } finally {
            setLoading(false);
        }
    };

    const initials = getInitials(userName);

    return (
        <>
            <Head>
                <title>Change Password</title>
                <meta name="description" content="Change your account password" />
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
                            <label htmlFor="profile-toggle" className="profile-icon">
                                {initials || "??"}
                            </label>
                            <div className="dropdown-menu">
                                <a href="/account-settings">Account Settings</a>
                                <a href="/change-password">Change Password</a>
                                <a href="#" onClick={() => {
                                    localStorage.removeItem("authToken");
                                    window.location.href = "/login";
                                }}>Logout</a>
                            </div>
                            <label htmlFor="profile-toggle" className="overlay"></label>
                        </div>
                    </div>
                </header>

                <div className="content">
                    <div className="account-settings">
                        <h1>Change Password</h1>
                        <form onSubmit={handleSubmit}>
                            <div className="form-group">
                                <label htmlFor="current-password">Current Password</label>
                                <input
                                    type="password"
                                    id="current-password"
                                    placeholder="••••••••"
                                    value={currentPassword}
                                    onChange={(e) => setCurrentPassword(e.target.value)}
                                    required
                                />
                            </div>
                            <div className="form-group">
                                <label htmlFor="new-password">New Password</label>
                                <input
                                    type="password"
                                    id="new-password"
                                    placeholder="••••••••"
                                    value={newPassword}
                                    onChange={(e) => setNewPassword(e.target.value)}
                                    required
                                />
                            </div>
                            <div className="form-group">
                                <label htmlFor="confirm-password">Confirm New Password</label>
                                <input
                                    type="password"
                                    id="confirm-password"
                                    placeholder="••••••••"
                                    value={confirmPassword}
                                    onChange={(e) => setConfirmPassword(e.target.value)}
                                    required
                                />
                            </div>

                            <div className="form-actions">
                                <button type="submit" className="submit-btn" disabled={loading}>
                                    {loading ? "Processing..." : "Change Password"}
                                </button>
                                <a
                                    href="/chat"
                                    className="btn btn-secondary"
                                    style={{ marginLeft: "10px", textDecoration: "none" }}
                                >
                                    Back to Chat
                                </a>
                            </div>

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
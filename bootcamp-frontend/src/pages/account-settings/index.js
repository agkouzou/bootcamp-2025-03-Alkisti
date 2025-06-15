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

export default function AccountPage() {
    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [intro, setIntro] = useState("");
    const [nickname, setNickname] = useState("");
    const [job, setJob] = useState("");
    const [notes, setNotes] = useState("");
    const [traits, setTraits] = useState([]);

    const [message, setMessage] = useState("");
    const [loading, setLoading] = useState(false);
    const [userId, setUserId] = useState(null);
    const [token, setToken] = useState(null);
    const [userName, setUserName] = useState("");

    const traitOptions = [
        "Chatty", "Witty", "Straight shooting", "Encouraging",
        "Gen Z", "Skeptical", "Traditional", "Forward thinking", "Poetic"
    ];

    useEffect(() => {
        const storedToken = localStorage.getItem("authToken");

        if (!storedToken) {
            console.warn("No token found, redirecting to login...");
            window.location.href = "/login";
            return;
        }

        setToken(storedToken);
        const decoded = parseJwt(storedToken);
        const uid = parseInt(decoded?.sub);

        if (!uid || isNaN(uid)) {
            console.error("Invalid user ID from token, redirecting...");
            window.location.href = "/login";
            return;
        }

        setUserId(uid);

        const fetchData = async () => {
            try {
                const userRes = await axios.get(`http://localhost:8080/users/${uid}`, {
                    headers: { Authorization: `Bearer ${storedToken}` }
                });
                const profileRes = await axios.get(`http://localhost:8080/users/${uid}/profile-settings`, {
                    headers: { Authorization: `Bearer ${storedToken}` }
                });

                const userData = userRes.data;
                const profileData = profileRes.data || {};

                setName(userData.name || "");
                setEmail(userData.email || "");
                setIntro(profileData.introduction || "");
                setNickname(profileData.nickname || "");
                setJob(profileData.job || "");
                setNotes(profileData.notes || "");
                setTraits(profileData.traits || []);
            } catch (e) {
                if (e.response?.status === 401) {
                    localStorage.removeItem("authToken");
                    window.location.href = "/login";
                } else {
                    console.error("Failed to fetch user info", e);
                }
            }
        };

        fetchData();
    }, []);

    useEffect(() => {
        if (!token || !userId) return;

        axios.get(`http://localhost:8080/users/${userId}`, {
            headers: { Authorization: `Bearer ${token}` },
        })
            .then(res => {
                const nameFromApi = res.data.name;
                setUserName(nameFromApi && nameFromApi !== "undefined" ? nameFromApi : "");
            })
            .catch(err => {
                console.error("Failed to fetch user info:", err);
                setUserName("");
            });
    }, [token, userId]);

    const toggleTrait = (trait) => {
        setTraits(prev =>
            prev.includes(trait) ? prev.filter(t => t !== trait) : [...prev, trait]
        );
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setMessage("");
        setLoading(true);

        const token = localStorage.getItem("authToken");
        const decoded = parseJwt(token);
        const userId = parseInt(decoded?.sub);

        if (!userId || isNaN(userId)) {
            setMessage("User ID not found in token.");
            setLoading(false);
            return;
        }

        const userData = { name, email };
        const profileData = { introduction: intro, nickname, job, notes, traits };

        try {
            await axios.put(`http://localhost:8080/users/${userId}`, userData, {
                headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" }
            });

            await axios.put(`http://localhost:8080/users/${userId}/profile-settings`, profileData, {
                headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" }
            });

            setMessage("Profile updated successfully!");
            setTimeout(() => window.location.reload(), 2000);
        } catch (e) {
            if (e.response?.status === 401) {
                alert("Session expired. Please log in again.");
                localStorage.removeItem("authToken");
                window.location.href = "/login";
            } else {
                setMessage("Update failed.");
                console.error("Update error", e);
            }
        } finally {
            setLoading(false);
        }
    };

    const handleAccount = () => window.location.href = "/account-settings";

    const handlePassword = () => window.location.href = "/change-password";

    const handleLogout = () => {
        localStorage.removeItem('authToken');
        window.location.href = "/login";
    };

    const initials = getInitials(userName);

    return (
        <>
            <Head>
                <title>Account Settings</title>
                <meta name="description" content="Manage your profile" />
                <meta name="viewport" content="width=device-width, initial-scale=1" />
                <link rel="icon" href="/favicon.ico" />
            </Head>

            <div className="page-container">
                <header>
                    <div className="header-content">
                        <div className="header-brand">
                            <img src="./bootcamp-2025.03-logo.jpg" alt="Logo" className="header-logo" />
                            <div className="header-title">Chat App</div>
                        </div>
                        <div className="profile-dropdown">
                            <input type="checkbox" id="profile-toggle" />
                            <label htmlFor="profile-toggle" className="profile-icon">
                                {initials || "??"}
                            </label>
                            <div className="dropdown-menu">
                                <a href="/account-settings" onClick={(e) => { e.preventDefault(); handleAccount(); }}>Account Settings</a>
                                <a href="/account-settings" onClick={(e) => { e.preventDefault(); handlePassword(); }}>Change Password</a>
                                <a href="/login" onClick={(e) => { e.preventDefault(); handleLogout(); }}>Logout</a>
                            </div>
                            <label htmlFor="profile-toggle" className="overlay"></label>
                        </div>
                    </div>
                </header>

                <div className="content">
                    <div className="account-settings">
                        <h1>Account Settings</h1>
                        <form onSubmit={handleSubmit}>
                            <div className="form-group">
                                <label htmlFor="name">Name</label>
                                <input type="text" id="name" value={name} onChange={(e) => setName(e.target.value)} />
                            </div>
                            <div className="form-group">
                                <label htmlFor="email">Email</label>
                                <input type="email" id="email" value={email} onChange={(e) => setEmail(e.target.value)} />
                            </div>
                            <div className="form-group">
                                <label htmlFor="intro">Customize Chat App</label>
                                <small>Introduce yourself to get better, more personalized responses</small>
                                <textarea id="intro" rows="3" value={intro} onChange={(e) => setIntro(e.target.value)} />
                            </div>
                            <div className="form-group">
                                <label htmlFor="nickname">What should Chat App call you?</label>
                                <input type="text" id="nickname" value={nickname} onChange={(e) => setNickname(e.target.value)} />
                            </div>
                            <div className="form-group">
                                <label htmlFor="job">What do you do?</label>
                                <input type="text" id="job" value={job} onChange={(e) => setJob(e.target.value)} />
                            </div>
                            <div className="form-group">
                                <label>What traits should Chat App have?</label>
                                <div className="traits">
                                    {traitOptions.map((trait) => (
                                        <label key={trait}>
                                            <input type="checkbox" checked={traits.includes(trait)} onChange={() => toggleTrait(trait)} />
                                            {trait}
                                        </label>
                                    ))}
                                </div>
                            </div>
                            <div className="form-group">
                                <label htmlFor="notes">Anything else Chat App should know about you?</label>
                                <textarea id="notes" rows="3" value={notes} onChange={(e) => setNotes(e.target.value)} />
                            </div>
                            <div className="form-actions">
                                <button type="submit" className="submit-btn" disabled={loading}>
                                    {loading ? "Processing..." : "Save Changes"}
                                </button>
                                <a href="/chat" className="btn btn-secondary" style={{ marginLeft: "10px", textDecoration: "none" }}>
                                    Back to Chat
                                </a>
                            </div>
                            {message && (
                                <div style={{ marginTop: "10px", fontSize: "14px" }}>
                                    {message}
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
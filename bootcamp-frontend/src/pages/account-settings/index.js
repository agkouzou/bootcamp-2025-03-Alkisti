import Head from "next/head";
import { useEffect, useState } from "react";
import axios from "axios";
import Link from 'next/link';


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

export default function AccountPage() {
    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [intro, setIntro] = useState("");
    const [nickname, setNickname] = useState("");
    const [job, setJob] = useState("");
    const [notes, setNotes] = useState("");
    const [traits, setTraits] = useState([]);
    const traitOptions = [
        "Chatty", "Witty", "Straight shooting", "Encouraging",
        "Gen Z", "Skeptical", "Traditional", "Forward thinking", "Poetic"
    ];
    const [message, setMessage] = useState("");
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

        const fetchData = async () => {
            try {
                const userResponse = await axios.get(`http://localhost:8080/users/${userId}`, {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                });
                const userData = userResponse.data;

                setName(userData.name || "");
                setEmail(userData.email || "");

                const profileResponse = await axios.get(`http://localhost:8080/users/${userId}/profile-settings`, {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                });
                const profileData = profileResponse.data || {};

                setIntro(profileData.introduction || "");
                setNickname(profileData.nickname || "");
                setJob(profileData.job || "");
                setNotes(profileData.notes || "");
                setTraits(profileData.traits || []);
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

    const toggleTrait = (trait) => {
        setTraits((prev) =>
            prev.includes(trait)
                ? prev.filter((t) => t !== trait)
                : [...prev, trait]
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
        const profileData = {
            introduction: intro,
            nickname,
            job,
            notes,
            traits
        };

        try {
            await axios.put(`http://localhost:8080/users/${userId}`, userData, {
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json"
                }
            });

            await axios.put(`http://localhost:8080/users/${userId}/profile-settings`, profileData, {
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json"
                }
            });

            // Fetch fresh user info
            const refreshedUserResponse = await axios.get(`http://localhost:8080/users/${userId}`, {
                headers: { Authorization: `Bearer ${token}` }
            });
            const refreshedUser = refreshedUserResponse.data;
            setName(refreshedUser.name || "");
            setEmail(refreshedUser.email || "");

            // Fetch fresh profile settings
            const refreshedProfileResponse = await axios.get(`http://localhost:8080/users/${userId}/profile-settings`, {
                headers: { Authorization: `Bearer ${token}` }
            });
            const refreshedProfile = refreshedProfileResponse.data || {};
            setIntro(refreshedProfile.intro || "");
            setNickname(refreshedProfile.nickname || "");
            setJob(refreshedProfile.job || "");
            setNotes(refreshedProfile.notes || "");
            setTraits(refreshedProfile.traits || []);

            await new Promise((resolve) => setTimeout(resolve, 1000));

            setMessage("Profile updated successfully!");

            setTimeout(() => {
                window.location.reload();
            }, 2000);

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

    const handleAccount = () => {
        window.location.href = "/account-settings";
    };

    const handlePassword = () => {
        window.location.href = "/change-password";
    };

    const handleLogout = () => {
        // Clear auth data here, e.g.:
        localStorage.removeItem('authToken');
        window.location.href = "/login";
    };

    const getUserInfo = async () => {
        const authToken = localStorage.getItem('authToken');

        try {
            const response = await axios.get('/user/profile', {
                headers: {
                    Authorization: `Bearer ${authToken}`,
                },
            });
        } catch (error) {
            console.error("Error fetching user info", error);
        }
    };

    const updateProfile = async (updatedUserInfo) => {
        const authToken = localStorage.getItem('authToken');

        try {
            const response = await axios.put('/user/profile', updatedUserInfo, {
                headers: {
                    Authorization: `Bearer ${authToken}`,
                },
            });
        } catch (error) {
            console.error("Error updating profile", error);
        }
    };

    return (
        <>
            <Head>
                <title>Account Settings</title>
                <meta name="description" content="Manage your profile"/>
                <meta name="viewport" content="width=device-width, initial-scale=1"/>
                <link rel="icon" href="/favicon.ico"/>
            </Head>

            <div className="page-container">
                <header>
                    <div className="header-content">
                        <div className="header-brand">
                            <img src="./bootcamp-2025.03-logo.jpg" alt="Logo" className="header-logo"/>
                            <div className="header-title">Chat Application</div>
                        </div>
                        <div className="profile-dropdown">
                            <input type="checkbox" id="profile-toggle"/>
                            <label htmlFor="profile-toggle" className="profile-icon">JD</label>
                            <div className="dropdown-menu">
                                <a href="/account-settings" onClick={(e) => {
                                    e.preventDefault();
                                    handleAccount();
                                }}>
                                    Account Settings
                                </a>
                                <a href="/account-settings" onClick={(e) => {
                                    e.preventDefault();
                                    handlePassword();
                                }}>
                                    Change Password
                                </a>
                                <a href="/login" onClick={(e) => {
                                    e.preventDefault();
                                    handleLogout();
                                }}>
                                    Logout
                                </a>
                            </div>
                            <label htmlFor="profile-toggle" className="overlay"></label>
                        </div>
                    </div>
                </header>
                <div className="content">
                    <div className="account-settings">
                        <h1>Account Settings</h1>
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
                                <label htmlFor="name">Name</label>
                                <input type="text" id="name" value={name} onChange={(e) => setName(e.target.value)}/>
                            </div>
                            <div className="form-group">
                                <label htmlFor="email">Email</label>
                                <input type="email" id="email" value={email}
                                       onChange={(e) => setEmail(e.target.value)}/>
                            </div>
                            <div className="form-group">
                                <label htmlFor="intro">Customize ChatGPT</label>
                                <small>Introduce yourself to get better, more personalized responses</small>
                                <textarea id="intro" rows="3" value={intro} onChange={(e) => setIntro(e.target.value)}/>
                            </div>
                            <div className="form-group">
                                <label htmlFor="nickname">What should ChatGPT call you?</label>
                                <input type="text" id="nickname" value={nickname}
                                       onChange={(e) => setNickname(e.target.value)}/>
                            </div>
                            <div className="form-group">
                                <label htmlFor="job">What do you do?</label>
                                <input type="text" id="job" value={job} onChange={(e) => setJob(e.target.value)}/>
                            </div>
                            <div className="form-group">
                                <label>What traits should ChatGPT have?</label>
                                <div className="traits">
                                    {traitOptions.map((trait) => (
                                        <label key={trait}>
                                            <input type="checkbox" checked={traits.includes(trait)}
                                                   onChange={() => toggleTrait(trait)}/>
                                            {trait}
                                        </label>
                                    ))}
                                </div>
                            </div>
                            <div className="form-group">
                                <label htmlFor="notes">Anything else ChatGPT should know about you?</label>
                                <textarea id="notes" rows="3" value={notes} onChange={(e) => setNotes(e.target.value)}/>
                            </div>
                            <button type="submit" className="submit-btn" disabled={loading}>
                                {loading ? "Processing..." : "Save Changes"}
                            </button>
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

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

    useEffect(() => {
        const token = localStorage.getItem("token");

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
                const response = await axios.get(`http://localhost:8080/users/${userId}`, {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                });
                const data = response.data;

                if (data) {
                    setName(data.name || "");
                    setEmail(data.email || "");
                    setIntro(data.intro || "");
                    setNickname(data.nickname || "");
                    setJob(data.job || "");
                    setNotes(data.notes || "");
                    setTraits(data.traits || []);
                } else {
                    console.warn("No user data received from API");
                }
            } catch (e) {
                if (e.response?.status === 401) {
                    console.warn("Unauthorized. Redirecting to login.");
                    localStorage.removeItem("token");
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
        const token = localStorage.getItem("token");

        const decoded = parseJwt(token);
        const userId = parseInt(decoded?.sub);

        if (!userId || isNaN(userId)) {
            alert("User ID not found in token.");
            return;
        }

        const userData = {
            name, email, intro, nickname, job, notes, traits
        };

        try {
            await axios.put(`http://localhost:8080/users/${userId}`, userData, {
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json"
                }
            });
            alert("Profile updated!");
        } catch (e) {
            if (e.response?.status === 401) {
                alert("Session expired. Please log in again.");
                localStorage.removeItem("token");
                window.location.href = "/login";
            } else {
                alert("Update failed");
                console.error("Update error", e);
            }
        }
    };

    return (
        <>
            <Head>
                <title>User Profile & Settings</title>
                <meta name="description" content="Manage your profile" />
                <meta name="viewport" content="width=device-width, initial-scale=1" />
                {/*<link rel="icon" href="/favicon.ico" />*/}
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
                                <a href="#">Profile</a>
                                <a href="#">Settings</a>
                                <a href="#" onClick={() => {
                                    localStorage.removeItem("token");
                                    window.location.href = "/login";
                                }}>Logout</a>
                                {/*<a href="#">Logout</a>*/}
                            </div>
                            <label htmlFor="profile-toggle" className="overlay"></label>
                        </div>
                    </div>
                </header>
                <div className="content">
                    <div className="account-settings">
                        <h1>User Profile & Settings</h1>
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
                            <label htmlFor="intro">Customize ChatGPT</label>
                            <small>Introduce yourself to get better, more personalized responses</small>
                            <textarea id="intro" rows="3" value={intro} onChange={(e) => setIntro(e.target.value)} />
                        </div>
                        <div className="form-group">
                            <label htmlFor="nickname">What should ChatGPT call you?</label>
                            <input type="text" id="nickname" value={nickname} onChange={(e) => setNickname(e.target.value)} />
                        </div>
                        <div className="form-group">
                            <label htmlFor="job">What do you do?</label>
                            <input type="text" id="job" value={job} onChange={(e) => setJob(e.target.value)} />
                        </div>
                        <div className="form-group">
                            <label>What traits should ChatGPT have?</label>
                            <div className="traits">
                                {traitOptions.map((trait) => (
                                    <label key={trait}>
                                        <input type="checkbox" checked={traits.includes(trait)} onChange={() => toggleTrait(trait)} />
                                        {trait}
                                    </label>
                                ))}
                                {/*<label><input type="checkbox"/> Chatty</label>*/}
                                {/*<label><input type="checkbox"/> Witty</label>*/}
                                {/*<label><input type="checkbox"/> Straight shooting</label>*/}
                                {/*<label><input type="checkbox"/> Encouraging</label>*/}
                                {/*<label><input type="checkbox"/> Gen Z</label>*/}
                                {/*<label><input type="checkbox"/> Skeptical</label>*/}
                                {/*<label><input type="checkbox"/> Traditional</label>*/}
                                {/*<label><input type="checkbox"/> Forward thinking</label>*/}
                                {/*<label><input type="checkbox"/> Poetic</label>*/}
                            </div>
                        </div>
                        <div className="form-group">
                            <label htmlFor="notes">Anything else ChatGPT should know about you?</label>
                            <textarea id="notes" rows="3" value={notes} onChange={(e) => setNotes(e.target.value)} />
                        </div>
                        <button type="submit" className="submit-btn">Save Changes</button>
                    </form>
                    </div>
                </div>
                <footer>© 2025 Chat App, Inc.</footer>
            </div>
        </>
    );
}

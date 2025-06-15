import Head from "next/head";
import { useEffect, useState } from "react";
import axios from "axios";
import { useRouter } from "next/router";
import { format } from "date-fns";

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

function sortMessagesByUpdated(messages) {
    return [...messages].sort((a, b) => {
        const dateA = a.updatedAt ? new Date(a.updatedAt) : new Date(a.createdAt);
        const dateB = b.updatedAt ? new Date(b.updatedAt) : new Date(b.createdAt);
        return dateA - dateB;
    });
}

export default function ChatPage() {
    const router = useRouter();

    const [isMounted, setIsMounted] = useState(false);
    const [token, setToken] = useState(null);
    const [userId, setUserId] = useState(null);

    const [userName, setUserName] = useState("");

    const [threads, setThreads] = useState([]);
    const [selectedThreadId, setSelectedThreadId] = useState(null);

    const [messages, setMessages] = useState([]);
    const [newMessage, setNewMessage] = useState("");

    const [selectedModel, setSelectedModel] = useState("llama3-70b-8192");

    const [editingMessageId, setEditingMessageId] = useState(null);
    const [editedContent, setEditedContent] = useState("");

    const api = axios.create({
        baseURL: "http://localhost:8080",
        headers: { Authorization: `Bearer ${token}` },
    });

    // On mount, get token and userId
    useEffect(() => {
        const t = localStorage.getItem("authToken");
        setToken(t);
        const decoded = parseJwt(t);
        setUserId(parseInt(decoded?.sub));
        setIsMounted(true);
    }, []);

    // Redirect if no valid token or userId
    useEffect(() => {
        if (isMounted && (!token || !userId || isNaN(userId))) {
            window.location.href = "/login";
        }
    }, [isMounted, token, userId]);

    // Fetch user info when token and userId available
    useEffect(() => {
        if (!token || !userId) return;

        api.get(`/users/${userId}`)
            .then(res => {
                const userNameFromApi = res.data.name;
                setUserName(userNameFromApi && userNameFromApi !== "undefined" ? userNameFromApi : "");
            })
            .catch(err => {
                console.error("Failed to fetch user info:", err);
                setUserName("");
            });
    }, [token, userId]);

    // Fetch threads when token available
    useEffect(() => {
        if (!token) return;

        api.get("/threads")
            .then(res => {
                const fetchedThreads = res.data;
                fetchedThreads.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
                setThreads(fetchedThreads);

                const threadIdFromUrl = router.query.threadId;
                const parsedThreadId = parseInt(threadIdFromUrl, 10);

                if (parsedThreadId && fetchedThreads.some(t => t.id === parsedThreadId)) {
                    setSelectedThreadId(parsedThreadId);
                } else if (fetchedThreads.length > 0) {
                    const latestThread = fetchedThreads.reduce((latest, thread) =>
                            new Date(thread.createdAt) > new Date(latest.createdAt) ? thread : latest
                        , fetchedThreads[0]);

                    setSelectedThreadId(latestThread.id);
                    router.replace({ pathname: '/chat', query: { threadId: latestThread.id } });
                } else {
                    setSelectedThreadId(null);
                }
            })
            .catch(err => {
                console.error("Error loading threads:", err);
            });
    }, [token, router]);

    // Fetch messages when selectedThreadId or token changes
    useEffect(() => {
        if (!selectedThreadId || !token) {
            setMessages([]);
            return;
        }

        api.get(`/threads/${selectedThreadId}`)
            .then(res => {
                // Sort messages by updatedAt or createdAt before setting state
                setMessages(sortMessagesByUpdated(res.data.messages));
            })
            .catch(err => {
                console.error("Failed to load messages:", err);
                setMessages([]);
            });
    }, [selectedThreadId, token]);

    const handleSendMessage = async () => {
        if (!newMessage || newMessage.trim() === "") {
            console.warn("New message is empty, skipping send");
            return;
        }

        try {
            let threadId = selectedThreadId;

            if (!threadId) {
                const threadResponse = await api.post("/threads", {
                    title: "",
                    completionModel: selectedModel,
                });
                threadId = threadResponse.data.id;
                setThreads(prev => [...prev, threadResponse.data]);
                setSelectedThreadId(threadId);
                router.push(`/chat?threadId=${threadId}`);
            }

            console.log("newMessage:", JSON.stringify(newMessage));
            console.log("selectedModel:", selectedModel);

            const messagePayload = {
                content: newMessage,
                completionModel: selectedModel,
            };

            console.log("Sending message payload:", JSON.stringify(messagePayload), "to thread:", threadId);

            const response = await api.post(`/threads/${threadId}/messages`, messagePayload);
            const updatedThread = response.data;

            setMessages(sortMessagesByUpdated(updatedThread.messages));
            setNewMessage("");

            setThreads(prevThreads =>
                prevThreads.map(t => (t.id === updatedThread.id ? { ...t, title: updatedThread.title } : t))
            );
        } catch (error) {
            console.error("Error sending message:", error);
        }
    };

    const handleUpdateMessage = async (messageId, updatedContent) => {
        if (!updatedContent || updatedContent.trim() === "") return;

        try {
            setMessages(prev =>
                prev.map(msg => (msg.id === messageId ? { ...msg, content: updatedContent } : msg))
            );

            await axios.put(
                `http://localhost:8080/messages/${messageId}`,
                { content: updatedContent, isCompletion: false },
                { headers: { Authorization: `Bearer ${token}` } }
            );

            const response = await axios.get(
                `http://localhost:8080/threads/${selectedThreadId}/messages`,
                { headers: { Authorization: `Bearer ${token}` } }
            );

            if (Array.isArray(response.data)) {
                setMessages(sortMessagesByUpdated(response.data));
                setEditingMessageId(null);
                setEditedContent("");
            } else {
                console.error("Expected an array of messages but got:", response.data);
            }
        } catch (error) {
            console.error("Failed to update message or fetch messages:", error);
        }
    };

    const createNewThread = async () => {
        try {
            const response = await api.post("/threads", { completionModel: selectedModel });

            const threadWithFallbackTitle = {
                ...response.data,
                title: response.data.title || "New Chat",
            };

            setThreads(prev => [...prev, threadWithFallbackTitle]);
            setSelectedThreadId(response.data.id);
            router.push(`/chat?threadId=${response.data.id}`);
        } catch (error) {
            console.error("Error creating new thread:", error);
        }
    };

    const handleDeleteThread = async (threadIdToDelete) => {
        try {
            await api.delete(`/threads/${threadIdToDelete}`);
            const res = await api.get("/threads");
            setThreads(res.data);

            if (selectedThreadId === threadIdToDelete) {
                if (res.data.length > 0) {
                    const newId = res.data[0].id;
                    setSelectedThreadId(newId);
                    router.replace(`/chat?threadId=${newId}`);
                } else {
                    setSelectedThreadId(null);
                    router.replace(`/chat`);
                }
            }
        } catch (err) {
            console.error("Failed to delete thread:", err);
        }
    };

    const handleDeleteMessageWithResponse = async (userMessageId) => {
        try {
            const userMessageIndex = messages.findIndex(msg => msg.id === userMessageId);
            if (userMessageIndex === -1) return;

            const assistantMessage = messages[userMessageIndex + 1];
            await api.delete(`/messages/${userMessageId}`);

            if (assistantMessage?.isCompletion) {
                try {
                    await api.delete(`/messages/${assistantMessage.id}`);
                } catch (err) {
                    if (err?.response?.status !== 404) throw err;
                }
            }

            const updatedMessages = messages.filter(
                msg => msg.id !== userMessageId && msg.id !== assistantMessage?.id
            );
            setMessages(sortMessagesByUpdated(updatedMessages));
        } catch (error) {
            console.error("Failed to delete messages:", error);
        }
    };

    const handleAccount = () => {
        window.location.href = "/account-settings";
    };

    const handlePassword = () => {
        window.location.href = "/change-password";
    };

    const handleLogout = () => {
        localStorage.removeItem('authToken');
        window.location.href = "/login";
    };

    if (!isMounted) return null;

    const initials = getInitials(userName);

    return (
        <>
            <Head>
                <title>Chat Application</title>
                <meta name="description" content="Chat app using Groq API" />
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

                        <div className="profile-dropdown">
                            <input type="checkbox" id="profile-toggle" />
                            <label htmlFor="profile-toggle" className="profile-icon">
                                {initials || "??"}
                            </label>

                            <div className="dropdown-menu">
                                <a
                                    href="/account-settings"
                                    onClick={(e) => {
                                        e.preventDefault();
                                        handleAccount();
                                    }}
                                >
                                    Account Settings
                                </a>

                                <a
                                    href="/account-settings"
                                    onClick={(e) => {
                                        e.preventDefault();
                                        handlePassword();
                                    }}
                                >
                                    Change Password
                                </a>

                                <a
                                    href="/login"
                                    onClick={(e) => {
                                        e.preventDefault();
                                        handleLogout();
                                    }}
                                >
                                    Logout
                                </a>
                            </div>

                            <label htmlFor="profile-toggle" className="overlay"></label>
                        </div>
                    </div>
                </header>

                <div className="center-container">
                    <aside className="threads-list">
                        <h2>Threads</h2>
                        <button
                            className="new-chat-btn"
                            onClick={createNewThread}
                            aria-label="Start a new chat conversation"
                        >
                            <span className="plus-icon">＋</span> New Chat
                        </button>

                        {threads.length === 0 ? (
                            <div className="empty-threads-message">
                                No threads yet.{" "}
                                <button
                                    className="new-chat-btn-inline"
                                    onClick={createNewThread}
                                >
                                    Start a new conversation!
                                </button>
                            </div>
                        ) : (
                            <div className="threads">
                                {[...threads]
                                    .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
                                    .map((thread) => {
                                    const isSelected = selectedThreadId === thread.id;

                                        return (
                                            <div
                                                key={thread.id}
                                                className={`thread-item ${isSelected ? "active" : ""}`}
                                                onClick={() => {
                                                    router.push(`/chat?threadId=${thread.id}`);
                                                    setSelectedThreadId(thread.id);
                                                }}
                                            >
                                                <div className="thread-title">
                                                    {thread.title.replace(/^["“]/, '')}
                                                </div>

                                                <div className="thread-meta">
                                                    <div className="timestamp">
                                                        {thread.createdAt
                                                            ? format(new Date(thread.createdAt), "PPpp")
                                                            : "No timestamp"}
                                                    </div>

                                                    <div className="thread-actions">
                                                        <button
                                                            onClick={(e) => {
                                                                e.stopPropagation();
                                                                handleDeleteThread(thread.id);
                                                            }}
                                                        >
                                                            🗑️
                                                        </button>
                                                    </div>
                                                </div>
                                            </div>
                                        );
                                    })}
                            </div>
                        )}
                    </aside>

                    <main className="main-container">
                        <div className="chat-window">
                            <div className="chat-model-select">
                                <select
                                    id="model-select"
                                    value={selectedModel}
                                    onChange={(e) => setSelectedModel(e.target.value)}
                                >
                                    <option value="llama3-70b-8192">LLaMA 3 70B (Default)</option>
                                    <option value="mixtral-8x7b-32768">Mixtral 8x7B</option>
                                    <option value="deepseek-llm-67b">DeepSeek 67B</option>
                                </select>
                            </div>

                            <div className="messages">
                                {messages.map((msg, index) => {
                                    const isUser = !msg.isCompletion;
                                    const isEditing = editingMessageId === msg.id;
                                    const timestamp =
                                        new Date(msg.updatedAt) > new Date(msg.createdAt)
                                            ? msg.updatedAt
                                            : msg.createdAt;
                                    const isEdited =
                                        msg.updatedAt &&
                                        new Date(msg.updatedAt) > new Date(msg.createdAt);

                                    const handleCopy = () => {
                                        navigator.clipboard
                                            .writeText(msg.content)
                                            .then(() => alert("Copied to clipboard!"))
                                            .catch((err) =>
                                                console.error("Copy failed: ", err)
                                            );
                                    };

                                    return (
                                        <div
                                            key={msg.id}
                                            className={`message ${msg.isCompletion ? "bot" : "user"}`}
                                        >
                                            <div className="message-content">
                                                {isUser && isEditing ? (
                                                    <div className="edit-container">
                                                        <input
                                                            type="text"
                                                            value={editedContent ?? ""}
                                                            onChange={(e) => setEditedContent(e.target.value)}
                                                            onKeyDown={(e) =>
                                                                e.key === "Enter" &&
                                                                handleUpdateMessage(msg.id, editedContent)
                                                            }
                                                        />
                                                        <button
                                                            onClick={() =>
                                                                handleUpdateMessage(msg.id, editedContent)
                                                            }
                                                        >
                                                            Save
                                                        </button>
                                                        <button onClick={() => setEditingMessageId(null)}>
                                                            Cancel
                                                        </button>
                                                    </div>
                                                ) : (
                                                    <>
                                                        <div>{msg.content}</div>

                                                        <div className="message-meta">
                                                            <div className="timestamp">
                                                                {timestamp && !isNaN(new Date(timestamp)) && (
                                                                    <>
                                                                        {format(new Date(timestamp), "PPpp")}{" "}
                                                                        {isEdited && "(Edited)"}
                                                                    </>
                                                                )}
                                                            </div>

                                                            <div className="message-actions">
                                                                <button onClick={handleCopy}>📋</button>
                                                                {isUser && (
                                                                    <>
                                                                        <button
                                                                            onClick={() => {
                                                                                setEditingMessageId(msg.id);
                                                                                setEditedContent(msg.content);
                                                                            }}
                                                                        >
                                                                            ✎
                                                                        </button>
                                                                        <button
                                                                            onClick={() =>
                                                                                handleDeleteMessageWithResponse(msg.id)
                                                                            }
                                                                            style={{ color: "red" }}
                                                                        >
                                                                            🗑️
                                                                        </button>
                                                                    </>
                                                                )}
                                                            </div>
                                                        </div>
                                                    </>
                                                )}
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>

                            <div className="input-container">
                                <input
                                    type="text"
                                    placeholder="Type a message…"
                                    value={newMessage}
                                    onChange={(e) => setNewMessage(e.target.value)}
                                    onKeyDown={(e) => e.key === "Enter" && handleSendMessage()}
                                />
                                <button onClick={handleSendMessage}>➤</button>
                            </div>
                        </div>
                    </main>
                </div>

                <footer>© 2025 Chat App, Inc.</footer>
            </div>
        </>
    );
}
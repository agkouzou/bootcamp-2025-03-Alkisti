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

export default function ChatPage() {
    // All hooks declared first, unconditionally
    const [isMounted, setIsMounted] = useState(false);
    const [token, setToken] = useState(null);
    const [userId, setUserId] = useState(null);

    const [threads, setThreads] = useState([]);
    const [selectedThreadId, setSelectedThreadId] = useState(null);
    const [messages, setMessages] = useState([]);
    const [newMessage, setNewMessage] = useState("");
    const [selectedModel, setSelectedModel] = useState("llama3-70b-8192");

    const [editingMessageId, setEditingMessageId] = useState(null);
    const [editedContent, setEditedContent] = useState("");

    // Get token & userId on client after mount
    useEffect(() => {
        const t = localStorage.getItem("token");
        setToken(t);
        const decoded = parseJwt(t);
        setUserId(parseInt(decoded?.sub));
        setIsMounted(true);
    }, []);

    // Redirect if no valid token or userId (client side)
    useEffect(() => {
        if (isMounted && (!token || !userId || isNaN(userId))) {
            window.location.href = "/login";
        }
    }, [isMounted, token, userId]);

    // Create axios instance after token is set
    const api = axios.create({
        baseURL: "http://localhost:8080",
        headers: { Authorization: `Bearer ${token}` },
    });

    // Load threads on token available
    useEffect(() => {
        if (!token) return;

        api.get("/threads")
            .then((res) => {
                setThreads(res.data);
                if (res.data.length > 0) {
                    setSelectedThreadId(res.data[0].id);
                }
            })
            .catch((err) => console.error("Error loading threads:", err));
    }, [token]);

    // Load messages when selectedThreadId changes
    useEffect(() => {
        if (selectedThreadId) {
            api.get(`/threads/${selectedThreadId}`)
                .then(res => {
                    setMessages(res.data.messages);
                })
            // api.get(`/threads/${selectedThreadId}/messages`)
            //     .then(res => {
            //         setMessages(res.data);
            //     })
                .catch(err => {
                    console.error("Failed to load messages:", err);
                });
        } else {
            setMessages([]);
        }
    }, [selectedThreadId]);

    // Send message handler
    const handleSendMessage = async () => {
        if (newMessage.trim() === "") return;

        try {
            let threadId = selectedThreadId;

            // Auto-create thread if none selected
            if (!threadId) {
                // const threadResponse = await api.post("/threads", {
                //     title: "New Chat Thread",
                //     completionModel: selectedModel,
                // });
                const threadPayload = {
                    title: "",
                    // initialMessageContent: newMessage,
                    completionModel: selectedModel,
                };

                // // Only include model if selected
                // if (selectedModel) {
                //     threadPayload.completionModel = selectedModel;
                // }

                const threadResponse = await api.post("/threads", threadPayload);

                threadId = threadResponse.data.id;
                setThreads((prev) => [...prev, threadResponse.data]);
                setSelectedThreadId(threadId);
            }

            // Send message
            const messagePayload = {
                content: newMessage,
                completionModel: selectedModel,
            };

            // if (selectedModel) {
            //     messagePayload.completionModel = selectedModel;
            // }

            // POST message and receive updated thread
            const response = await api.post(`/threads/${threadId}/messages`, messagePayload);
            const updatedThread = response.data;

            // Replace messages with full updated list
            setMessages(updatedThread.messages);
            setNewMessage("");

            // Update the thread title in the sidebar, if it changed
            setThreads((prevThreads) =>
                prevThreads.map((t) =>
                    t.id === updatedThread.id ? { ...t, title: updatedThread.title } : t
                )
            );

            // setMessages((prev) => [...prev, response.data]);
            // setNewMessage("");
        } catch (error) {
            console.error("Error sending message:", error);
        }
    };

    // Avoid rendering before mount + token loaded
    if (!isMounted) {
        return null;
    }

    const handleUpdateMessage = async (id) => {
        try {
            const response = await api.put(`/messages/${id}`, {
                content: editedContent,
                completionModel: selectedModel,
                regenerate: true,
            });

            // Replace old message + assistant response
            const updatedMessages = messages
                .filter((msg) => msg.id !== id) // remove old user message
                .filter((msg, i, arr) => !(arr[i - 1]?.id === id && msg.isCompletion)); // remove old assistant reply if right after

            setMessages([...updatedMessages, { id, content: editedContent, isCompletion: false, completionModel: selectedModel }, response.data]);
            setEditingMessageId(null);
            setEditedContent("");
        } catch (err) {
            console.error("Error updating message:", err);
        }
    };

    const createNewThread = async () => {
        try {
            const response = await api.post("/threads", {
                completionModel: selectedModel, // Only send what's needed
            });

            // Update UI with title fallback if needed
            const threadWithFallbackTitle = {
                ...response.data,
                title: response.data.title || "New Chat", // Only for display, not DB
            };

            setThreads((prevThreads) => [...prevThreads, threadWithFallbackTitle]);
            setSelectedThreadId(response.data.id);
        } catch (error) {
            console.error("Error creating new thread:", error);
        }
    };

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
                            <img src="./bootcamp-2025.03-logo.jpg" alt="Logo" className="header-logo" />
                            <div className="header-title">Chat Application</div>
                        </div>
                        <div className="profile-dropdown">
                            <input type="checkbox" id="profile-toggle" />
                            <label htmlFor="profile-toggle" className="profile-icon">JD</label>
                            <div className="dropdown-menu">
                                <a href="#">Profile</a>
                                <a href="#">Settings</a>
                                <a href="#">Logout</a>
                            </div>
                            <label htmlFor="profile-toggle" className="overlay"></label>
                        </div>
                    </div>
                </header>
                <div className="center-container">
                    <aside className="threads-list">
                        <h2>Threads</h2>
                        <button onClick={createNewThread}>New Chat</button> {/* Button to create a new thread */}
                        <div className="threads">
                            {threads.map((thread) => (
                                <div
                                    key={thread.id}
                                    className={`thread-item ${selectedThreadId === thread.id ? "active" : ""}`}
                                    onClick={() => setSelectedThreadId(thread.id)}
                                >
                                    {thread.title}
                                </div>
                            ))}
                        </div>
                    </aside>
                    <main className="main-container">
                        <div className="chat-window">
                            {/* Model selector */}
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

                                    return (
                                        <div key={msg.id} className={`message ${msg.isCompletion ? "bot" : "user"}`}>
                                            {isUser && isEditing ? (
                                                <div className="edit-container">
                                                    <input
                                                        type="text"
                                                        value={editedContent}
                                                        onChange={(e) => setEditedContent(e.target.value)}
                                                        onKeyDown={(e) =>
                                                            e.key === "Enter" && handleUpdateMessage(msg.id)
                                                        }
                                                    />
                                                    <button onClick={() => handleUpdateMessage(msg.id)}>Save</button>
                                                    <button onClick={() => setEditingMessageId(null)}>Cancel</button>
                                                </div>
                                            ) : (
                                                <>
                                                    {msg.content}
                                                    {isUser && (
                                                        <button
                                                            className="edit-button"
                                                            onClick={() => {
                                                                setEditingMessageId(msg.id);
                                                                setEditedContent(msg.content);
                                                            }}
                                                        >
                                                            ✎
                                                        </button>
                                                    )}
                                                </>
                                            )}
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

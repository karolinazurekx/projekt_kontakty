// src/App.js
import React, { useState, useEffect } from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Contacts from "./pages/Contacts";

function App() {
    const [isLogged, setIsLogged] = useState(!!localStorage.getItem("token"));
    const [username, setUsername] = useState(localStorage.getItem("username") || "");
    const [theme, setTheme] = useState(localStorage.getItem("theme") || "light");

    useEffect(() => {
        document.documentElement.setAttribute("data-bs-theme", theme);
        localStorage.setItem("theme", theme);
    }, [theme]);

    const toggleTheme = () => {
        setTheme((prev) => (prev === "light" ? "dark" : "light"));
    };

    const handleLogin = (loggedUsername) => {
        setIsLogged(true);
        setUsername(loggedUsername);
    };

    const handleLogout = () => {
        localStorage.removeItem("token");
        localStorage.removeItem("username");
        setIsLogged(false);
        setUsername("");
    };

    return (
        <BrowserRouter>
            {isLogged && (
                <nav className="navbar navbar-dark bg-dark px-3 d-flex justify-content-between">
                    <span className="navbar-brand">Kontakty</span>

                    <div className="d-flex align-items-center gap-3 text-white">

                        <span>
                            Zalogowany jako: <strong>{username}</strong>
                        </span>
                        <button className="btn btn-outline-light" onClick={handleLogout}>
                            Wyloguj
                        </button>
                        <button className="btn btn-outline-light" onClick={toggleTheme}>
                            {theme === "light" ? "🌙" : "☀️"}
                        </button>
                    </div>
                </nav>
            )}

            <Routes>
                {!isLogged ? (
                    <>
                        <Route
                            path="/"
                            element={<Login onLogin={handleLogin} />}
                        />
                        <Route path="/register" element={<Register />} />
                    </>
                ) : (
                    <Route path="/" element={<Contacts />} />
                )}
            </Routes>
        </BrowserRouter>
    );
}

export default App;

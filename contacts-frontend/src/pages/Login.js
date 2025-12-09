// src/pages/Login.js
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";

function Login({ onLogin }) {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");

        try {
            const res = await api.post("/auth/login", {
                username,
                password
            });

            // token
            localStorage.setItem("token", res.data.token);

            // login
            localStorage.setItem("username", username);

            // powiadom App.js
            onLogin(username);

        } catch (err) {
                setError("Złe dane");
        }
    };

    return (
        <div className="container mt-5" style={{ maxWidth: "400px" }}>
            <h2>Logowanie</h2>

            {error && <div className="alert alert-danger">{error}</div>}

            <form onSubmit={handleSubmit}>
                <div className="mb-3">
                    <label>Nazwa użytkownika</label>
                    <input className="form-control"
                           value={username}
                           onChange={(e) => setUsername(e.target.value)} />
                </div>

                <div className="mb-3">
                    <label>Hasło</label>
                    <input type="password"
                           className="form-control"
                           value={password}
                           onChange={(e) => setPassword(e.target.value)} />
                </div>

                <button className="btn btn-primary w-100" type="submit">Zaloguj się</button>
            </form>

            <p className="mt-3">
                Nie masz konta?{" "}
                <span className="text-primary" role="button"
                      onClick={() => navigate("/register")}>
                    Zarejestruj się
                </span>
            </p>
        </div>
    );
}
export default Login;

import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";

function Register() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [info, setInfo] = useState("");
    const navigate = useNavigate();

    const handleRegister = async (e) => {
        e.preventDefault();
        setInfo("");

        try {
            await api.post("/auth/register", { username, password });
            setInfo("Konto utworzone! Możesz się zalogować.");
        } catch (err) {
            setInfo("Błąd: użytkownik istnieje.");
        }
    };

    return (
        <div className="container mt-5" style={{ maxWidth: "400px" }}>
            <h2>Rejestracja</h2>

            {info && <div className="alert alert-info">{info}</div>}

            <form onSubmit={handleRegister}>
                <div className="mb-3">
                    <label>Nazwa użytkownika</label>
                    <input className="form-control" value={username}
                           onChange={(e) => setUsername(e.target.value)} />
                </div>

                <div className="mb-3">
                    <label>Hasło</label>
                    <input type="password" className="form-control" value={password}
                           onChange={(e) => setPassword(e.target.value)} />
                </div>

                <button className="btn btn-success w-100">Utwórz konto</button>
            </form>

            <p className="mt-3">
                Masz konto?{" "}
                <span className="text-primary" role="button" onClick={() => navigate("/")}>
                    Zaloguj się
                </span>
            </p>
        </div>
    );
}

export default Register;

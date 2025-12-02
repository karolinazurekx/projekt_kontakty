import { useEffect, useState } from "react";
import api from "../api/axios";
import "bootstrap/dist/css/bootstrap.min.css";
import * as bootstrap from "bootstrap";
import "./Contacts.css";

function Contacts() {

    const [contacts, setContacts] = useState([]);
    const [filtered, setFiltered] = useState([]);
    const [search, setSearch] = useState("");
    const [role, setRole] = useState("");

    const [form, setForm] = useState({
        id: null,
        firstName: "",
        lastName: "",
        email: "",
        phone: ""
    });

    const [importText, setImportText] = useState("");
    const [importFileName, setImportFileName] = useState("");
    const [importType, setImportType] = useState("");
    const [infoMessage, setInfoMessage] = useState(""); // for export/import info feedback

    const resetForm = () =>
        setForm({ id: null, firstName: "", lastName: "", email: "", phone: "" });

    useEffect(() => {
        loadUserRole();
        loadContacts();
    }, []);

    const loadUserRole = async () => {
        try {
            const res = await api.get("/auth/me");
            setRole(res.data.role);
        } catch (err) {
            console.error("Cannot load user role:", err);
        }
    };

    const loadContacts = async () => {
        try {
            const res = await api.get("/api/contacts");
            const sorted = [...res.data].sort((a, b) => (a.lastName || "").localeCompare(b.lastName || ""));
            setContacts(sorted);
            setFiltered(sorted);
        } catch (err) {
            console.error("Cannot load contacts:", err);
        }
    };

    const handleSearch = (text) => {
        setSearch(text);
        setFiltered(
            contacts.filter((c) =>
                ((c.firstName || "") + " " + (c.lastName || "") + " " + (c.email || ""))
                    .toLowerCase()
                    .includes(text.toLowerCase())
            )
        );
    };

    const openModal = (id) => {
        new bootstrap.Modal(document.getElementById(id)).show();
    };

    const openAddModal = () => {
        if (role === "ROLE_ADMIN") return;
        resetForm();
        openModal("contactModal");
    };

    const openEditModal = (c) => {
        setForm(c);
        openModal("contactModal");
    };

    const openImportModal = () => {
        setImportText("");
        setImportFileName("");
        setImportType("");
        setInfoMessage("");
        openModal("importModal");
    };

    const saveContact = async () => {
        try {
            if (form.id) {
                await api.put(`/api/contacts/${form.id}`, form);
            } else {
                if (role === "ROLE_ADMIN") return;
                await api.post("/api/contacts", form);
            }
            loadContacts();
        } catch (err) {
            console.error("Save contact error:", err);
            alert("Błąd zapisu kontaktu");
        }
    };

    const deleteContact = async (id) => {
        if (window.confirm("Napewno chcesz usunąć kontakt?")) {
            try {
                await api.delete(`/api/contacts/${id}`);
                loadContacts();
            } catch (err) {
                console.error("Delete error:", err);
                alert("Błąd usuwania kontaktu");
            }
        }
    };

    const initials = (c) =>
        `${c.firstName?.[0] || ""}${c.lastName?.[0] || ""}`.toUpperCase();

    const randomColor = (text) => {
        let hash = 0;
        for (let i = 0; i < text.length; i++) {
            hash = text.charCodeAt(i) + ((hash << 5) - hash);
        }
        const hue = Math.abs(hash) % 360;
        return `hsl(${hue}, 65%, 45%)`;
    };

    const hasContacts = contacts && contacts.length > 0;

    // ============================
    //        EXPORT JSON
    // ============================
    const exportJson = async () => {
        if (!hasContacts) {
            setInfoMessage("Brak kontaktów do eksportu.");
            return;
        }
        try {
            const res = await api.get("/api/contacts/export/json", { responseType: "text" });

            let data;
            try {
                data = JSON.parse(res.data);
            } catch (e) {
                data = Array.isArray(res.data) ? res.data : [];
            }

            const cleaned = data.map(({ ownerUsername, id, ...rest }) => rest);

            const blob = new Blob([JSON.stringify(cleaned, null, 2)], { type: "application/json" });
            const a = document.createElement("a");
            a.href = URL.createObjectURL(blob);
            a.download = "contacts.json";
            a.click();

            setInfoMessage("Eksport JSON zakończony pomyślnie.");
        } catch (err) {
            console.error("Export JSON error:", err);
            setInfoMessage("Błąd eksportu JSON");
            alert("Błąd eksportu JSON");
        }
    };

    // ============================
    //        EXPORT XML
    // ============================
    const exportXml = async () => {
        if (!hasContacts) {
            setInfoMessage("Brak kontaktów do eksportu.");
            return;
        }
        try {
            const res = await api.get("/api/contacts/export/xml", { responseType: "text" });
            let xml = res.data;

            xml = xml.replace(/<ownerUsername>.*?<\/ownerUsername>\s*/g, "");
            xml = xml.replace(/<id>.*?<\/id>\s*/g, "");

            const blob = new Blob([xml], { type: "application/xml" });
            const a = document.createElement("a");
            a.href = URL.createObjectURL(blob);
            a.download = "contacts.xml";
            a.click();

            setInfoMessage("Eksport XML zakończony pomyślnie.");
        } catch (err) {
            console.error("Export XML error:", err);
            setInfoMessage("Błąd eksportu XML");
            alert("Błąd eksportu XML");
        }
    };

    // ============================
    //        IMPORT HELPERS
    // ============================

    const handleFileChange = (e) => {
        const file = e.target.files && e.target.files[0];
        if (!file) return;

        const reader = new FileReader();
        reader.onload = () => {
            setImportText(reader.result);
            setImportFileName(file.name);

            const lower = file.name.toLowerCase();
            if (lower.endsWith(".xml") || file.type.includes("xml")) {
                setImportType("xml");
            } else {
                setImportType("json");
            }
        };
        reader.onerror = () => alert("Nie udało się wczytać pliku");
        reader.readAsText(file);
    };

    const importJson = async () => {
        try {
            if (!importText || importText.trim() === "") {
                setInfoMessage("Brak treści do zaimportowania");
                alert("Brak treści do zaimportowania");
                return;
            }

            let parsed;
            try {
                parsed = JSON.parse(importText);
            } catch (e) {
                setInfoMessage("Nieprawidłowy JSON");
                alert("Nieprawidłowy JSON");
                return;
            }

            if (!Array.isArray(parsed)) {
                setInfoMessage("Oczekiwany format: tablica obiektów kontaktów");
                alert("Oczekiwany format: tablica obiektów kontaktów");
                return;
            }

            if (!window.confirm("Import JSON zastąpi wszystkie Twoje obecne kontakty. Kontynuować?")) return;

            const cleaned = parsed.map(({ ownerUsername, id, ...rest }) => rest);

            await api.post("/api/contacts/import/json", cleaned, {
                headers: { "Content-Type": "application/json" }
            });

            loadContacts();
            setInfoMessage("Zaimportowano JSON");
            alert("Zaimportowano JSON");
        } catch (err) {
            console.error("Import JSON error:", err);
            setInfoMessage("Błąd importu JSON");
            alert("Błąd importu JSON: " + (err.response?.data || err.message));
        }
    };

    const importXml = async () => {
        try {
            if (!window.confirm("Import XML zastąpi wszystkie Twoje obecne kontakty. Kontynuować?")) return;

            await api.post("/api/contacts/import/xml", importText, {
                headers: { "Content-Type": "application/xml" }
            });
            loadContacts();
            setInfoMessage("Zaimportowano XML");
            alert("Zaimportowano XML");
        } catch (err) {
            console.error("Import XML error:", err);
            setInfoMessage("Błąd importu XML");
            alert("Błąd importu XML: " + (err.response?.data || err.message));
        }
    };

    const importDetectedFile = async () => {
        if (!importType) {
            alert("Nie rozpoznano typu pliku.");
            return;
        }
        if (importType === "json") await importJson();
        else await importXml();
    };

    // ============================
    //    RENDER
    // ============================
    return (
        <div className="container py-4">
            <div className="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
                <h2 className="m-0">Twoje Kontakty</h2>

                <div className="actions">
                    {role !== "ROLE_ADMIN" && (
                        <button className="btn btn-outline-secondary" onClick={openImportModal}>
                            Zaimportuj
                        </button>
                    )}
                    <button
                        className="btn btn-outline-success"
                        onClick={exportJson}
                        disabled={!hasContacts}
                        title={!hasContacts ? "Brak kontaktów do eksportu" : ""}
                    >
                        Eksportuj JSON
                    </button>
                    <button
                        className="btn btn-outline-primary"
                        onClick={exportXml}
                        disabled={!hasContacts}
                        title={!hasContacts ? "Brak kontaktów do eksportu" : ""}
                    >
                        Eksportuj XML
                    </button>
                </div>
            </div>

            {/* info message */}
            {infoMessage && (
                <div className="mb-3">
                    <div className="alert alert-info py-1 px-2 small mb-0">
                        {infoMessage}
                    </div>
                </div>
            )}

            {/* SEARCH + ADD */}
            <div className="search-add-row mb-4">
                <input
                    className="form-control"
                    placeholder="Szukaj kontaktów..."
                    value={search}
                    onChange={(e) => handleSearch(e.target.value)}
                />

                {role !== "ROLE_ADMIN" && (
                    <button className="btn btn-primary add-btn" onClick={openAddModal}>
                        ➕ Dodaj kontakt
                    </button>
                )}
            </div>

            {/* when no contacts show friendly placeholder */}
            {!hasContacts ? (
                <div className="alert alert-secondary" role="status">
                    Nie masz jeszcze żadnych kontaktów — brak danych do eksportu.
                </div>
            ) : (
                <div className="row g-3">
                    {filtered.map((c) => (
                        <div className="col-12 col-sm-6 col-lg-4" key={c.id}>
                            <div className="card shadow-sm p-3 h-100 d-flex contact-card flex-row align-items-center">

                                {/* Avatar */}
                                <div
                                    className="avatar rounded-circle text-white d-flex justify-content-center align-items-center flex-shrink-0"
                                    style={{
                                        backgroundColor: randomColor(c.email || c.firstName || ""),
                                    }}
                                >
                                    <span style={{ fontSize: 22, fontWeight: "bold" }}>{initials(c)}</span>
                                </div>

                                <div className="flex-grow-1 ms-3">
                                    <h5 className="mb-1">{c.firstName} {c.lastName}</h5>
                                    <div className="text-muted small">{c.email}</div>

                                    {/* phone with icon */}
                                    <div className="small phone-row" title={c.phone || ""}>
                                        <svg className="phone-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                                            <path d="M6.6 10.79a15.05 15.05 0 006.61 6.61l2.2-2.2a1 1 0 01.96-.27 11.36 11.36 0 003.55.57 1 1 0 011 1V20a1 1 0 01-1 1A17 17 0 013 4a1 1 0 011-1h2.5a1 1 0 011 1 11.36 11.36 0 00.57 3.55 1 1 0 01-.27.96l-2.2 2.28z"></path>
                                        </svg>
                                        <span className="ms-2">{c.phone}</span>
                                    </div>
                                </div>

                                <div className="d-flex flex-column gap-2 ms-3">
                                    <button
                                        className="btn btn-sm btn-outline-secondary"
                                        onClick={() => openEditModal(c)}
                                    >
                                        Edytuj
                                    </button>
                                    <button
                                        className="btn btn-sm btn-outline-danger"
                                        onClick={() => deleteContact(c.id)}
                                    >
                                        Usuń
                                    </button>
                                </div>

                            </div>
                        </div>
                    ))}
                </div>
            )}

            {/* CONTACT MODAL */}
            <div className="modal fade" id="contactModal" tabIndex="-1">
                <div className="modal-dialog modal-dialog-centered">
                    <div className="modal-content">

                        <div className="modal-header">
                            <h5 className="modal-title">{form.id ? "Edytuj kontakt" : "Dodaj kontakt"}</h5>
                            <button className="btn-close" data-bs-dismiss="modal"></button>
                        </div>

                        <div className="modal-body">
                            <div className="mb-3">
                                <label>Imie</label>
                                <input
                                    className="form-control"
                                    value={form.firstName}
                                    onChange={(e) => setForm({ ...form, firstName: e.target.value })}
                                />
                            </div>

                            <div className="mb-3">
                                <label>Nazwisko</label>
                                <input
                                    className="form-control"
                                    value={form.lastName}
                                    onChange={(e) => setForm({ ...form, lastName: e.target.value })}
                                />
                            </div>

                            <div className="mb-3">
                                <label>Email</label>
                                <input
                                    className="form-control"
                                    value={form.email}
                                    onChange={(e) => setForm({ ...form, email: e.target.value })}
                                />
                            </div>

                            <div className="mb-3">
                                <label>Telefon</label>
                                <input
                                    className="form-control"
                                    value={form.phone}
                                    onChange={(e) => setForm({ ...form, phone: e.target.value })}
                                />
                            </div>
                        </div>

                        <div className="modal-footer">
                            <button className="btn btn-secondary" data-bs-dismiss="modal">
                                Anuluj
                            </button>
                            <button className="btn btn-primary" data-bs-dismiss="modal" onClick={saveContact}>
                                Zapisz
                            </button>
                        </div>

                    </div>
                </div>
            </div>

            {/* IMPORT MODAL */}
            <div className="modal fade" id="importModal" tabIndex="-1">
                <div className="modal-dialog modal-dialog-centered modal-lg">
                    <div className="modal-content">

                        <div className="modal-header">
                            <h5 className="modal-title">Importuj kontakty</h5>
                            <button className="btn-close" data-bs-dismiss="modal"></button>
                        </div>

                        <div className="modal-body">
                            <div className="mb-3">
                                <label>Plik JSON/XML</label>
                                <input
                                    type="file"
                                    accept=".json,.xml,application/json,application/xml,text/xml"
                                    className="form-control"
                                    onChange={handleFileChange}
                                />
                                {importFileName && (
                                    <div className="form-text">
                                        Wybrano: {importFileName} (type: {importType || "unknown"})
                                    </div>
                                )}
                            </div>

                            <div className="mb-3">
                                <label>Treść JSON / XML</label>
                                <textarea
                                    className="form-control"
                                    rows="10"
                                    value={importText}
                                    onChange={(e) => setImportText(e.target.value)}
                                ></textarea>
                            </div>
                        </div>

                        <div className="modal-footer d-flex flex-wrap gap-2">
                            <button className="btn btn-secondary flex-fill" data-bs-dismiss="modal">
                                Zamknij
                            </button>
                            <button className="btn btn-success flex-fill" data-bs-dismiss="modal" onClick={importJson}>
                                Import JSON
                            </button>
                            <button className="btn btn-primary flex-fill" data-bs-dismiss="modal" onClick={importXml}>
                                Import XML
                            </button>
                            <button className="btn btn-info flex-fill" data-bs-dismiss="modal" onClick={importDetectedFile}>
                                Import detected file
                            </button>
                        </div>

                    </div>
                </div>
            </div>
        </div>
    );
}

export default Contacts;
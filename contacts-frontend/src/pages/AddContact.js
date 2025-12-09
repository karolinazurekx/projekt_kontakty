import { useState } from "react";
import api from "../api/axios";

function AddContact({ onAdded }) {
    const [firstName, setFirstName] = useState("");
    const [lastName, setLastName] = useState("");
    const [email, setEmail] = useState("");
    const [phone, setPhone] = useState("");

    const addContact = async (e) => {
        e.preventDefault();
        try {
            await api.post("/api/contacts", {
                firstName,
                lastName,
                email,
                phone
            });

            setFirstName("");
            setLastName("");
            setEmail("");
            setPhone("");

            onAdded(); // refresh
        } catch (err) {
            console.error(err);
        }
    };

    return (
        <form onSubmit={addContact} style={{ marginBottom: "1rem" }}>
            <input
                placeholder="First name"
                value={firstName}
                onChange={(e) => setFirstName(e.target.value)}
            />
            <input
                placeholder="Last name"
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
            />
            <input
                placeholder="Email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
            />
            <input
                placeholder="Phone"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
            />
            <button type="submit">Add</button>
        </form>
    );
}

export default AddContact;

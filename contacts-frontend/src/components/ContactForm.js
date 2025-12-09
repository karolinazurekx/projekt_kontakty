import React, { useState } from "react";

export default function ContactForm({ onAdd }) {
    const [form, setForm] = useState({
        firstName: "",
        lastName: "",
        email: "",
        phone: ""
    });

    const handleChange = (e) => {
        setForm({...form, [e.target.name]: e.target.value});
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        onAdd(form);
        setForm({ firstName: "", lastName: "", email: "", phone: "" });
    };

    return (
        <form onSubmit={handleSubmit}>
            <input name="firstName" placeholder="First Name" value={form.firstName} onChange={handleChange} required />
            <input name="lastName" placeholder="Last Name" value={form.lastName} onChange={handleChange} required />
            <input name="email" placeholder="Email" value={form.email} onChange={handleChange} />
            <input name="phone" placeholder="Phone" value={form.phone} onChange={handleChange} />

            <button type="submit">Add Contact</button>
        </form>
    );
}

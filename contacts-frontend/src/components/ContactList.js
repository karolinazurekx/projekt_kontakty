import React from "react";

export default function ContactList({ contacts, onDelete }) {
    return (
        <ul>
            {contacts.map((c) => (
                <li key={c.id}>
                    {c.firstName} {c.lastName} – {c.email} – {c.phone}
                    <button onClick={() => onDelete(c.id)}>Delete</button>
                </li>
            ))}
        </ul>
    );
}

# 📝 Note Taking App (Java Swing)

This is a **desktop-based Note Taking App** implemented in **Java Swing** as part of my **Pinnacle Labs Internship (Task 2)**.  
The app allows you to create, save, pin, delete, and search notes – all stored locally with a colorful and simple interface.

---

## 🚀 Features

- **Create Notes**
  - Start a new note via button or shortcut (**Ctrl+N**).
- **Save Notes**
  - Save notes easily with a button or shortcut (**Ctrl+S**).
  - Both title and content are persisted.
- **Auto-Color Notes**
  - Every new note gets a different pastel background colour (default white if none open).
- **Pin/Unpin Notes**
  - Keep important notes always at the top.
- **Delete Notes**
  - Remove unwanted notes permanently.
- **Search Notes**
  - Search notes by title or content instantly.
- **Local Storage**
  - Notes are saved locally to ensure persistence even after closing the app.

---

## 🛠️ Tech Stack

- **Java SE**
- **Java Swing (GUI)**
- **Serialisation for persistence**
- **OOP Principles**

---

## 📂 Project Structure

- `NoteApp.java` → Main application with GUI.  
- `Note` → Model class representing each note.  
- `persist()` → Handles saving/loading notes to local storage.  

---

## ⚡ How to Run

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/note-taking-app.git
   cd note-taking-app
2. Compile the program:
   ```bash
   javac NoteApp.java
3. Run the program:
   ```bash
   java NoteApp

---

## 🎮 Shortcuts

Ctrl+N → Create a new note
Ctrl+S → Save current note

---

## 🙌 Acknowledgement

This project was built as part of my internship at Pinnacle Labs to practice GUI design, data persistence, and user experience in Java.

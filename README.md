📡 Private Ping Android

Private Ping is a real-time Android chat application built with Java, Firebase Realtime Database, and Android Studio. It allows users to securely create or join chat rooms using a Room ID and a Secret Code for protected communication.  
The app features a WhatsApp-like UI supporting text and media messaging (images, PDFs).

---

✨ Features
- 💬 Real-Time Messaging — Instantly exchange text messages using Firebase Realtime Database.
- 🖼️ Media Sharing — Upload and view images and PDFs inline with thumbnails or icons.
- 🔐 Room Management — Create or join rooms via Room ID and Secret Code with secure Firebase validation.
- 🎨 Responsive UI — WhatsApp-style chat bubbles, left-aligned for received messages and right-aligned for sent messages.
- 🧹 Clear Chat — Option to clear chat history within a room.

---

🛠️ Technologies Used
- 💻 Java — Core app development
- 🔥 Firebase Realtime Database — Data storage and real-time sync
- 📦 Firebase Storage — Media file uploads
- 🏗️ Android Studio — IDE for building and testing
- 🎨 XML — UI layouts
- 📚 Libraries
  - Firebase SDK
  - RecyclerView
  - AndroidX Support Libraries

---

📂 Project Structure
```
PrivatePing/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/privateping/
│   │   │   │   ├── MainActivity.java
│   │   │   │   ├── JoinRoomActivity.java
│   │   │   │   ├── ChatActivity.java
│   │   │   │   ├── ChatMessage.java
│   │   │   │   ├── ChatAdapter.java
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   │   ├── activity_main.xml
│   │   │   │   │   ├── activity_join_room.xml
│   │   │   │   │   ├── activity_chat.xml
│   │   │   │   │   ├── chat_message_item.xml
│   │   │   ├── AndroidManifest.xml
│   ├── build.gradle
├── README.md
```

---

🚀 Setup Instructions

1. Clone the Repository
   ```bash
   git clone https://github.com/G-ShivaKrishna/PrivatePing-Android.git
   ```
2. Open in Android Studio
   Open Android Studio and select "Open an existing project".  
   Choose the PrivatePing folder.
3. Configure Firebase
   Go to Firebase Console  
   Create a new project  
   Register your Android app and download the google-services.json file  
   Place google-services.json inside the app directory
4. Set Firebase Realtime Database Rules
   ```json
   {
     "rules": {
       "rooms": {
         "$roomId": {
           ".read": "data.child('secret').val() === 'your-secret-code'",
           ".write": "data.child('secret').val() === 'your-secret-code'"
         }
       }
     }
   }
   ```
5. Build and Run
   Sync Project with Gradle Files  
   Run the app on an emulator or physical device (API 21 and above)

---

🧩 Challenges Overcome
- 🚫 Firebase Permission Denied Errors — Solved by implementing secret code validation in database rules
- 🎭 Custom Chat UI — Developed WhatsApp-like message alignment
- 🛠️ Stable Build — Fixed constructor mismatches and layout inflation issues

---

🔮 Future Improvements
- 🔒 Integrate Firebase Authentication for enhanced security
- 📶 Add offline messaging with local caching
- 📄 Improve file handling with PDF preview dialogs

---

📸 Screenshots  
Upload screenshots to the screenshots folder and add their links

<img src="https://github.com/user-attachments/assets/4eea5080-d643-4f0a-88ba-f1746ccf6a9b" width="250" />  
<img src="https://github.com/user-attachments/assets/f9a65508-e99e-42c2-8901-2689eded2b66" width="250" />  
<img src="https://github.com/user-attachments/assets/eead2423-b03c-4acb-92d2-39317175db01" width="250" />


---

📬 Contact

- 📧 Email: reddyshivakrishna04@gmail.com
- 🔗 LinkedIn: https://www.linkedin.com/in/gottimukkulashivakrishna/

---

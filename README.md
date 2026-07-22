# Postmark

A travel journal Android app that allows users to capture memories, attach photos, and visualize their journey on an interactive map. Made by and for people who want to archive memories in relation to physical space!

## ✨ Features

- **Personal Journaling**: Create, edit, and delete text-based travel entries on the go.
- **Photo Attachments**: Upload and host travel photos securely using Supabase Storage.
- **Interactive Map**: View all your journal entries as markers on a Google Map.
- **Cloud Sync**: All entries are synced in real-time across devices via Firebase Firestore.
- **Secure Authentication**: Private user accounts powered by Firebase Auth.

## 🛠 Tech Stack

- **UI**: Jetpack Compose (Kotlin)
- **Database**: Firebase Firestore
- **Auth**: Firebase Authentication
- **Storage**: Supabase Storage
- **Maps**: Google Maps SDK for Android
- **Async**: Kotlin Coroutines & Flow
- **Image Loading**: Coil

## 📁 Project Structure

```
app/src/main/java/com/ait/postmark/
├── MainActivity.kt              # entry point
├── auth/
│   └── AuthRepository.kt        # Firebase Auth wrapper
├── data/
│   ├── Entry.kt                 # entry data class (Firestore-serializable)
│   └── EntryRepository.kt       # Firestore CRUD, Supabase image storage
├── navigation/
│   └── PostmarkNavGraph.kt      # routes
└── ui/
    ├── components/              # shared UI (overflow menu, date format)
    ├── theme/                   # Color, Theme
    ├── login/                   # login + register screen
    ├── list/                    # list view (also exports EntriesViewModel)
    ├── map/                     # Google Map view
    └── entry/                   # new entry composer + entry detail
```

## 🚀 Setup Instructions

1. **Firebase Configuration**:
   - Create a project in the [Firebase Console](https://console.firebase.google.com/).
   - Register your Android app with the package name `com.ait.postmark`.
   - Enable **Authentication** and turn on the **Email/Password** provider.
   - Enable **Cloud Firestore** and create a database.
   - Download `google-services.json` and place it in the `app/` directory.

2. **API Keys & Secrets**:
   This project uses the Secrets Gradle Plugin. Create/Update your `local.properties` file in the root directory:
   ```properties
   # Google Maps SDK
   MAPS_API_KEY=your_google_maps_key
   
   # Supabase Credentials
   SUPABASE_URL=your_supabase_project_url
   SUPABASE_KEY=your_supabase_anon_key
   ```

3. **Google Maps SDK**:
   - Enable the **Maps SDK for Android** in the [Google Cloud Console](https://console.cloud.google.com/).
   - Ensure your API key is restricted to your Android app's package name and SHA-1 for security.

4. **Supabase Storage**:
   - Create a project at [Supabase](https://supabase.com/).
   - Navigate to **Storage** and create a new public bucket named `photos`.
   - Update bucket policies to allow public read access.

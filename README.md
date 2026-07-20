# Postmark

A travel journal Android app built with Jetpack Compose, Firebase, and Google Maps.

## Project structure

```
app/src/main/java/com/ait/postmark/
├── MainActivity.kt              # entry point
├── auth/
│   └── AuthRepository.kt        # Firebase Auth wrapper
├── data/
│   ├── Entry.kt                 # entry data class (Firestore-serializable)
│   └── EntryRepository.kt       # Firestore CRUD
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

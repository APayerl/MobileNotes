# MobileNotes GUI Application

## Översikt

Ett desktop-GUI för MobileNotes-appen byggt med Jetpack Compose Multiplatform. Appen kommunicerar med backend via REST API och stödjer alla tre huvudfunktionerna:

1. **Skapa rader** (NoteLine)
2. **Kopiera listor** (värdekopiering)  
3. **Referera listor** (levande referenser)

## Funktioner

### Login-skärm
- Enkel inloggning genom att ange User ID
- För testning: använd `user1` som redan har testdata

### Huvudskärm (två-kolumn layout)

#### Vänster kolumn: Lista med anteckningar
- Visar alla anteckningar för den inloggade användaren
- Sorterade efter senaste ändring (nyaste först)
- Klicka på en anteckning för att öppna den
- Vald anteckning markeras med en ljus bakgrund (shade)
- Listan fortsätter vara synlig när en anteckning är vald

#### Höger kolumn: Detaljvy
- Visas när en anteckning är vald
- Visar titel, innehåll och metadata
- Tre funktionsknappar:
  - **Kopiera** - Skapar en värdekopiering av anteckningen
  - **Skapa referens** - Skapar en levande länk till en annan anteckning
  - **Expandera referenser** - Hämtar anteckningen med alla referenser expanderade

## Arkitektur

### Komponenter

```
App.kt                      # Huvudapp med state management
├── LoginScreen.kt          # Login-vy
└── NotesListScreen.kt      # Huvudvy med lista + detalj
    ├── NotesList           # Vänster kolumn
    ├── NoteListItem        # Rad i listan
    └── NoteDetailView      # Höger kolumn
        ├── CopyNoteDialog
        └── CreateReferenceDialog
```

### Data Layer

```
NotesViewModel.kt           # State management och business logic
├── NotesUiState           # Login | NotesList
└── API calls via NotesApiClient

NotesApiClient.kt          # REST API kommunikation
└── Ktor Client
```

### Models
Använder modeller från `shared`-modulen (ingen duplicering):
- `Note`
- `NoteLine`
- `NoteReference`
- `NoteElement`

## Användning

### Starta appen

1. **Starta backend-servern först:**
   ```bash
   cd C:\Users\payerl\git\MobileNotes
   gradlew.bat :server:run
   ```

2. **Starta desktop-appen:**
   ```bash
   gradlew.bat :composeApp:run
   ```

### Workflow

#### 1. Logga in
- Ange User ID (t.ex. `user1`)
- Klicka "Logga in"

#### 2. Visa anteckningar
- Listan till vänster visar alla dina anteckningar
- Klicka på en för att öppna den till höger

#### 3. Kopiera en anteckning
- Öppna en anteckning
- Klicka på kopierings-ikonen (📋)
- Ange ny titel
- Klicka "Kopiera"
- Den nya anteckningen skapas och väljs automatiskt

#### 4. Skapa referens
- Öppna en anteckning (t.ex. "Packlista skidsemester")
- Klicka på länk-ikonen (🔗)
- Välj vilken anteckning som ska refereras (t.ex. "Generell packlista")
- Klicka "Skapa referens"
- Anteckningarna laddas om automatiskt

#### 5. Expandera referenser
- Öppna en anteckning som har referenser
- Klicka på expandera-ikonen (↗️)
- Innehållet uppdateras med det faktiska innehållet från refererade anteckningar

## Dependencies

### Ktor Client
- `ktor-client-core` - Core client functionality
- `ktor-client-cio` - CIO engine
- `ktor-client-content-negotiation` - JSON support
- `ktor-serialization-kotlinx-json` - Kotlinx serialization

### Compose
- `compose.material3` - Material Design 3
- `compose.foundation` - Foundation components
- `lifecycle-viewmodel-compose` - ViewModel integration

### Shared
- `projects.shared` - Delade modeller (Note, NoteLine, etc.)

## API Endpoints som används

```kotlin
GET    /users/{userId}/notes              // Hämta alla anteckningar
GET    /users/{userId}/notes/{noteId}     // Hämta en anteckning
POST   /users/{userId}/notes              // Skapa ny anteckning
POST   /users/{userId}/notes/{noteId}/copy          // Kopiera
POST   /users/{userId}/notes/{noteId}/reference     // Skapa referens
GET    /users/{userId}/notes/{noteId}/expanded      // Expandera referenser
```

## State Management

### ViewModel State
```kotlin
uiState: StateFlow<NotesUiState>           // Login | NotesList
currentUserId: StateFlow<String?>          // Inloggad användare
notes: StateFlow<List<NoteDto>>            // Alla anteckningar
selectedNote: StateFlow<NoteDto?>          // Vald anteckning
isLoading: StateFlow<Boolean>              // Loading state
error: StateFlow<String?>                  // Felmeddelanden
```

### UI State Flow
```
Login Screen
    ↓ (användare anger ID)
Notes List Screen
    ├── Lista visas (vänster)
    ├── Välj anteckning → Detaljvy (höger)
    ├── Kopiera → Ny anteckning skapas
    ├── Skapa referens → Dialog → Referenser uppdateras
    └── Expandera → Innehåll uppdateras
```

## Styling

### Material Design 3
- **Primary Container** - För markerad anteckning
- **Surface Variant** - För innehållsområden
- **Cards** - För login-formulär
- **Elevation** - Djup och skuggor

### Layout
- **Two-pane master-detail** - Lista + Detalj
- **Responsive** - Detaljvyn döljs när inget är valt
- **Dividers** - Visuell separation mellan kolumner

## Felsökning

### "Connection refused"
- Kontrollera att backend-servern körs på port 8080
- Starta med: `gradlew.bat :server:run`

### "Inga anteckningar ännu"
- Logga in med `user1` som har testdata
- Eller skapa nya anteckningar via Swagger UI först

### Gradle sync-fel
- Kör: `gradlew.bat --refresh-dependencies`
- Kontrollera att alla dependencies finns i libs.versions.toml

## Framtida förbättringar

- [ ] Skapa ny anteckning direkt i GUI
- [ ] Redigera befintliga anteckningar
- [ ] Radera anteckningar
- [ ] Sök i anteckningar
- [ ] Filtrering och sortering
- [ ] Dark mode
- [ ] Offline-stöd med lokal cache

## Teknisk stack

- **Kotlin** - Programmeringsspråk
- **Jetpack Compose Multiplatform** - UI framework
- **Ktor Client** - HTTP client
- **Kotlinx Serialization** - JSON parsing
- **StateFlow** - State management
- **Material Design 3** - UI komponenter

---

**Appen är nu redo att använda!** 🚀

Starta backend, starta desktop-app, logga in med `user1` och utforska!


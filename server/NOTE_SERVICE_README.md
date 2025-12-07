# Note Service

En Kotlin-baserad service för hantering av anteckningar i MobileNotes-applikationen.

## Översikt

NoteService tillhandahåller CRUD-operationer (Create, Read, Update, Delete) för anteckningar kopplade till specifika användare. Servicen använder Exposed som ORM (Object-Relational Mapping) och H2 som databas.

## Arkitektur

### Komponenter

1. **DatabaseConfig** (`database/DatabaseConfig.kt`)
   - Initierar databaskopplingen
   - Skapar nödvändiga tabeller
   - Lägger till testdata för utveckling

2. **Tables** (`database/Tables.kt`)
   - Definierar databasscheman för `users` och `notes` tabeller
   - UsersTable: Lagrar användarinformation
   - NotesTable: Lagrar anteckningar kopplade till användare

3. **NoteDto** (`database/dto/NoteDto.kt`)
   - Data Transfer Object för anteckningar
   - Serialiserbar för JSON-konvertering

4. **NoteService** (`service/NoteService.kt`)
   - Huvudservicen för anteckningshantering
   - Tillhandahåller metoder för databasoperationer

## API Endpoints

### Hämta alla anteckningar för en användare
```
GET /notes/{userId}
```
**Svar**: Lista med NoteDto-objekt sorterade efter senaste ändring

### Hämta en specifik anteckning
```
GET /notes/{userId}/{noteId}
```
**Svar**: Ett NoteDto-objekt eller 404 om anteckningen inte hittas

## NoteService Metoder

### `getNotesByUserId(userId: String): List<NoteDto>`
Hämtar alla anteckningar för en specifik användare, sorterade efter senaste ändring (nyaste först).

**Parametrar:**
- `userId`: Användarens unika ID

**Returnerar:**
- Lista med NoteDto-objekt

**Exempel:**
```kotlin
val noteService = NoteService()
val notes = noteService.getNotesByUserId("user1")
```

### `getNoteById(noteId: String, userId: String): NoteDto?`
Hämtar en specifik anteckning baserat på antecknings-ID och användar-ID.

**Parametrar:**
- `noteId`: Anteckningens unika ID
- `userId`: Användarens unika ID (för säkerhetsvalidering)

**Returnerar:**
- NoteDto-objekt eller null om anteckningen inte hittas

**Exempel:**
```kotlin
val note = noteService.getNoteById("note1", "user1")
```

### `createNote(userId: String, title: String, content: String): NoteDto`
Skapar en ny anteckning för användaren.

**Parametrar:**
- `userId`: Användarens unika ID
- `title`: Anteckningens titel
- `content`: Anteckningens innehåll (JSON-sträng)

**Returnerar:**
- Den nyskapade NoteDto

**Exempel:**
```kotlin
val newNote = noteService.createNote(
    userId = "user1",
    title = "Min nya anteckning",
    content = """{"lines": [{"text": "Detta är innehållet"}]}"""
)
```

### `updateNote(noteId: String, userId: String, title: String, content: String): Boolean`
Uppdaterar en befintlig anteckning.

**Parametrar:**
- `noteId`: Anteckningens unika ID
- `userId`: Användarens unika ID
- `title`: Ny titel
- `content`: Nytt innehåll (JSON-sträng)

**Returnerar:**
- `true` om uppdateringen lyckades, `false` annars

**Exempel:**
```kotlin
val success = noteService.updateNote(
    noteId = "note1",
    userId = "user1",
    title = "Uppdaterad titel",
    content = """{"lines": [{"text": "Uppdaterat innehåll"}]}"""
)
```

### `deleteNote(noteId: String, userId: String): Boolean`
Raderar en anteckning.

**Parametrar:**
- `noteId`: Anteckningens unika ID
- `userId`: Användarens unika ID (för säkerhetsvalidering)

**Returnerar:**
- `true` om raderingen lyckades, `false` annars

**Exempel:**
```kotlin
val deleted = noteService.deleteNote("note1", "user1")
```

## Databas Schema

### UsersTable
```sql
CREATE TABLE users (
    id VARCHAR(50) PRIMARY KEY,
    username VARCHAR(100),
    email VARCHAR(100)
);
```

### NotesTable
```sql
CREATE TABLE notes (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50),
    title VARCHAR(255),
    content TEXT,
    last_modified BIGINT
);
```

## Testdata

Vid initialisering skapas testdata automatiskt:
- En testanvändare: `user1`
- Tre testanteckningar för denna användare

Du kan testa API:et genom att anropa:
```
GET http://localhost:8080/notes/user1
```

## Säkerhet

NoteService kontrollerar alltid att `userId` matchar för alla operationer, vilket säkerställer att användare endast kan komma åt sina egna anteckningar.

## Dependencies

- **Ktor**: Web framework
- **Exposed**: ORM för databashantering
- **H2**: In-memory SQL-databas
- **Kotlinx Serialization**: JSON-serialisering

## Framtida Förbättringar

- [ ] Lägg till POST/PUT/DELETE endpoints
- [ ] Implementera autentisering och auktorisering
- [ ] Migrera från in-memory till persistent databas
- [ ] Lägg till paginering för stora datamängder
- [ ] Implementera fulltext-sökning
- [ ] Lägg till validering av innehåll


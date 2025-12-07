# 🚀 Snabbstart - Swagger UI i MobileNotes

## Steg-för-steg guide

### 1️⃣ Synka Gradle (OBLIGATORISKT)
Innan du kan använda Swagger måste du synka Gradle för att ladda ner de nya dependencies:

**I IntelliJ IDEA:**
- Klicka på notifikationen "Gradle files have changed" → "Sync Now"
- ELLER: Högerklicka på projektet → "Reload Gradle Project"
- ELLER: Gradle tool window → Klicka på refresh-ikonen (🔄)

**Från terminalen:**
```cmd
cd C:\Users\payerl\git\MobileNotes
gradlew.bat --refresh-dependencies
```

### 2️⃣ Starta servern
```cmd
cd C:\Users\payerl\git\MobileNotes
gradlew.bat :server:run
```

Vänta tills du ser:
```
[main] INFO ktor.application - Responding at http://0.0.0.0:8080
```

### 3️⃣ Öppna Swagger UI
Öppna din webbläsare och gå till:
```
http://localhost:8080/swagger
```

Du kommer att se en interaktiv API-dokumentation med alla dina endpoints!

### 4️⃣ Testa ditt första API-anrop
1. **Klicka** på `GET /notes/{userId}` för att expandera
2. **Klicka** på knappen "Try it out" (högst upp till höger)
3. **Fyll i** userId: `user1`
4. **Klicka** på den blå "Execute"-knappen
5. **Scrolla ner** till "Server response"
6. **Se** listan med 3 testanteckningar i JSON-format!

## 🎯 Snabbtestning

### Test 1: Hämta alla anteckningar
```
Endpoint: GET /notes/{userId}
Parameter: userId = "user1"
Förväntat: 3 anteckningar returneras
```

### Test 2: Hämta en specifik anteckning
```
Endpoint: GET /notes/{userId}/{noteId}
Parameter: userId = "user1", noteId = "note1"
Förväntat: En anteckning med titeln "Min första anteckning"
```

### Test 3: Skapa en ny anteckning
```
Endpoint: POST /notes/{userId}
Parameter: userId = "user1"
Body: 
{
  "title": "Test från Swagger",
  "content": "{\"lines\": [{\"text\": \"Detta fungerar!\"}]}"
}
Förväntat: Status 201 Created och ett nytt NoteDto-objekt
```

## 📍 Viktiga URLs

| URL | Beskrivning |
|-----|-------------|
| http://localhost:8080/swagger | Swagger UI (interaktiv dokumentation) |
| http://localhost:8080/openapi | Raw OpenAPI specification (YAML) |
| http://localhost:8080/ | Hälsokontroll |
| http://localhost:8080/notes/user1 | Hämta anteckningar (direkt GET) |

## 💡 Tips

### Swagger UI-funktioner:
- **Try it out** - Testa API-anrop direkt
- **Example Value** - Fyll i automatiskt med exempel
- **Schemas** - Se alla datamodeller längst ner
- **Curl** - Kopiera curl-kommando efter Execute

### Testdata som finns:
- **User ID:** `user1`
- **Note IDs:** `note1`, `note2`, `note3`

### Vanliga fel:
❌ "Failed to fetch" → Servern körs inte, starta den igen
❌ "404 Not Found" → Gradle inte synkat, synka och starta om
❌ Inga endpoints syns → Fel i YAML-filen, kolla serverlogs

## 📚 Mer information

Detaljerad dokumentation finns i:
- `server/SWAGGER_GUIDE.md` - Komplett guide för Swagger UI
- `SWAGGER_IMPLEMENTATION.md` - Teknisk implementation
- `server/NOTE_SERVICE_README.md` - API-dokumentation

## ✅ Checklista

- [ ] Gradle synkat
- [ ] Servern startad
- [ ] Swagger UI öppnat (http://localhost:8080/swagger)
- [ ] Testat GET /notes/user1
- [ ] Sett 3 testanteckningar

Lycka till! 🎉


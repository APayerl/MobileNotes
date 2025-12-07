# Testing Guide - Note Service API

## Förutsättningar
1. Gradle-projektet måste vara synkat
2. Servern måste vara startad på port 8080

## Starta servern

```cmd
cd C:\Users\payerl\git\MobileNotes
gradlew.bat :server:run
```

## Test Endpoints med curl (Windows CMD)

### 1. Hämta alla anteckningar för user1
```cmd
curl http://localhost:8080/notes/user1
```

**Förväntat svar:**
```json
[
  {
    "id": "note1",
    "userId": "user1",
    "title": "Min första anteckning",
    "content": "{\"lines\": [{\"text\": \"Detta är min första anteckning\"}]}",
    "lastModified": 1730707200000
  },
  {
    "id": "note2",
    "userId": "user1",
    "title": "Shoppinglista",
    "content": "{\"lines\": [{\"text\": \"Mjölk\"}, {\"text\": \"Bröd\"}, {\"text\": \"Smör\"}]}",
    "lastModified": 1730620800000
  },
  {
    "id": "note3",
    "userId": "user1",
    "title": "Att-göra",
    "content": "{\"lines\": [{\"text\": \"Slutföra projektet\"}, {\"text\": \"Ringa läkaren\"}]}",
    "lastModified": 1730534400000
  }
]
```

### 2. Hämta en specifik anteckning
```cmd
curl http://localhost:8080/notes/user1/note1
```

**Förväntat svar:**
```json
{
  "id": "note1",
  "userId": "user1",
  "title": "Min första anteckning",
  "content": "{\"lines\": [{\"text\": \"Detta är min första anteckning\"}]}",
  "lastModified": 1730707200000
}
```

### 3. Försök hämta en anteckning som inte finns
```cmd
curl http://localhost:8080/notes/user1/nonexistent
```

**Förväntat svar:**
```
Note not found
```

### 4. Test med POST (om du använder NoteRoutes)
```cmd
curl -X POST http://localhost:8080/notes/user1 ^
  -H "Content-Type: application/json" ^
  -d "{\"title\":\"Ny anteckning\",\"content\":\"{\\\"lines\\\":[{\\\"text\\\":\\\"Test innehåll\\\"}]}\"}"
```

### 5. Test med PUT (om du använder NoteRoutes)
```cmd
curl -X PUT http://localhost:8080/notes/user1/note1 ^
  -H "Content-Type: application/json" ^
  -d "{\"title\":\"Uppdaterad titel\",\"content\":\"{\\\"lines\\\":[{\\\"text\\\":\\\"Uppdaterat innehåll\\\"}]}\"}"
```

### 6. Test med DELETE (om du använder NoteRoutes)
```cmd
curl -X DELETE http://localhost:8080/notes/user1/note3
```

## Test med PowerShell

### Hämta alla anteckningar
```powershell
Invoke-RestMethod -Uri http://localhost:8080/notes/user1 -Method Get
```

### Hämta en specifik anteckning
```powershell
Invoke-RestMethod -Uri http://localhost:8080/notes/user1/note1 -Method Get
```

### Skapa ny anteckning (POST)
```powershell
$body = @{
    title = "Ny anteckning från PowerShell"
    content = '{"lines": [{"text": "Detta är innehållet"}]}'
} | ConvertTo-Json

Invoke-RestMethod -Uri http://localhost:8080/notes/user1 `
    -Method Post `
    -Body $body `
    -ContentType "application/json"
```

### Uppdatera anteckning (PUT)
```powershell
$body = @{
    title = "Uppdaterad från PowerShell"
    content = '{"lines": [{"text": "Nytt innehåll"}]}'
} | ConvertTo-Json

Invoke-RestMethod -Uri http://localhost:8080/notes/user1/note1 `
    -Method Put `
    -Body $body `
    -ContentType "application/json"
```

### Radera anteckning (DELETE)
```powershell
Invoke-RestMethod -Uri http://localhost:8080/notes/user1/note2 -Method Delete
```

## Test med webbläsare

Öppna webbläsaren och navigera till:
- http://localhost:8080/notes/user1
- http://localhost:8080/notes/user1/note1

GET-requests fungerar direkt i webbläsaren.

## Test med Postman eller IntelliJ HTTP Client

### IntelliJ HTTP Client (.http file)

Skapa en fil `test-requests.http`:

```http
### Hämta alla anteckningar för user1
GET http://localhost:8080/notes/user1

### Hämta en specifik anteckning
GET http://localhost:8080/notes/user1/note1

### Skapa ny anteckning
POST http://localhost:8080/notes/user1
Content-Type: application/json

{
  "title": "Ny anteckning från HTTP Client",
  "content": "{\"lines\": [{\"text\": \"Test innehåll\"}]}"
}

### Uppdatera anteckning
PUT http://localhost:8080/notes/user1/note1
Content-Type: application/json

{
  "title": "Uppdaterad titel",
  "content": "{\"lines\": [{\"text\": \"Uppdaterat innehåll\"}]}"
}

### Radera anteckning
DELETE http://localhost:8080/notes/user1/note3
```

## Validera säkerhet

### Test: Försök hämta anteckning för annan användare
```cmd
curl http://localhost:8080/notes/user2/note1
```

Detta ska returnera "Note not found" eftersom note1 tillhör user1, inte user2.

## Felsökning

### Problem: "Connection refused"
- Kontrollera att servern körs
- Verifiera att port 8080 är ledig

### Problem: "404 Not Found"
- Kontrollera URL-spelling
- Verifiera att endpoints är korrekt konfigurerade

### Problem: Tomma resultat
- Kontrollera att testdata har laddats korrekt
- Se i serverloggen för eventuella fel

### Problem: "Unresolved reference" i IDE
- Synka Gradle-projektet: File → Reload All from Disk
- ELLER: Gradle tool window → Refresh

## Loggar

Servern loggar alla requests. Kontrollera konsolen för:
```
[main] INFO ktor.application - Responding at http://0.0.0.0:8080
```

## Nästa steg

När du verifierat att GET fungerar:
1. Implementera POST/PUT/DELETE endpoints (använd NoteRoutes.kt som mall)
2. Lägg till autentisering
3. Validera input
4. Implementera error handling


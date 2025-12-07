# Swagger UI Guide - MobileNotes API

## Översikt

Swagger UI har lagts till i MobileNotes-projektet för att ge en interaktiv API-dokumentation där du kan:
- Se alla tillgängliga endpoints
- Läsa detaljerad dokumentation för varje endpoint
- Testa API-anrop direkt från webbläsaren
- Se request/response-exempel
- Validera API-schemas

## Åtkomst till Swagger UI

### Efter att servern har startats:

**Swagger UI (Interaktiv dokumentation):**
```
http://localhost:8080/swagger
```

**OpenAPI Specification (Raw YAML):**
```
http://localhost:8080/openapi
```

## Snabbstart

### 1. Starta servern
```cmd
cd C:\Users\payerl\git\MobileNotes
gradlew.bat :server:run
```

### 2. Öppna Swagger UI
Öppna din webbläsare och gå till:
```
http://localhost:8080/swagger
```

### 3. Utforska API:et
- Klicka på en endpoint för att se detaljer
- Klicka på "Try it out" för att testa anropet
- Fyll i parametrar (t.ex. userId: "user1")
- Klicka på "Execute"
- Se response direkt i webbläsaren

## Tillgängliga Endpoints i Swagger

### 🏷️ Notes (Anteckningar)

#### GET /notes/{userId}
**Hämta alla anteckningar för en användare**
- **Parameter:** userId (t.ex. "user1")
- **Response:** Array av NoteDto-objekt
- **Test:** Använd "user1" för att se testdata

#### GET /notes/{userId}/{noteId}
**Hämta en specifik anteckning**
- **Parametrar:** 
  - userId (t.ex. "user1")
  - noteId (t.ex. "note1")
- **Response:** Ett NoteDto-objekt
- **Test:** userId="user1", noteId="note1"

#### POST /notes/{userId}
**Skapa en ny anteckning**
- **Parameter:** userId (t.ex. "user1")
- **Request Body:**
  ```json
  {
    "title": "Min nya anteckning",
    "content": "{\"lines\": [{\"text\": \"Test innehåll\"}]}"
  }
  ```
- **Response:** Det skapade NoteDto-objektet med nytt ID

#### PUT /notes/{userId}/{noteId}
**Uppdatera en befintlig anteckning**
- **Parametrar:** userId, noteId
- **Request Body:**
  ```json
  {
    "title": "Uppdaterad titel",
    "content": "{\"lines\": [{\"text\": \"Uppdaterat innehåll\"}]}"
  }
  ```
- **Response:** Success-meddelande

#### DELETE /notes/{userId}/{noteId}
**Radera en anteckning**
- **Parametrar:** userId, noteId
- **Response:** Success-meddelande

### 🏥 Health

#### GET /
**Hälsokontroll**
- Enkel endpoint för att verifiera att servern körs

#### GET /posts
**Placeholder för framtida funktionalitet**

## Använda Swagger UI

### Testa en GET-request

1. **Expandera** endpoint genom att klicka på den
2. **Klicka** på "Try it out"-knappen (högst upp till höger)
3. **Fyll i** parametrar:
   - userId: `user1`
4. **Klicka** på "Execute"
5. **Se resultatet** under "Server response"

### Testa en POST-request

1. **Expandera** POST /notes/{userId}
2. **Klicka** på "Try it out"
3. **Fyll i** userId: `user1`
4. **Redigera** request body i JSON-editorn:
   ```json
   {
     "title": "Test från Swagger",
     "content": "{\"lines\": [{\"text\": \"Detta skapades via Swagger UI\"}]}"
   }
   ```
5. **Klicka** på "Execute"
6. **Verifiera** att statusen är 201 Created
7. **Kopiera** det returnerade ID:t från response

### Testa en PUT-request

1. **Expandera** PUT /notes/{userId}/{noteId}
2. **Klicka** på "Try it out"
3. **Fyll i** parametrar:
   - userId: `user1`
   - noteId: (använd ett ID från tidigare GET-request)
4. **Redigera** request body
5. **Klicka** på "Execute"

### Testa en DELETE-request

1. **Expandera** DELETE /notes/{userId}/{noteId}
2. **Klicka** på "Try it out"
3. **Fyll i** parametrar
4. **Klicka** på "Execute"
5. **Verifiera** att statusen är 200 OK

## Swagger UI-funktioner

### Schemas
Klicka på "Schemas" längst ner för att se:
- **NoteDto** - Struktur för anteckningsobjekt
- **CreateNoteRequest** - Request-format för att skapa anteckningar
- **UpdateNoteRequest** - Request-format för att uppdatera anteckningar

### Response-exempel
Varje endpoint visar exempel på:
- Lyckad response (200, 201, etc.)
- Felmeddelanden (400, 404, etc.)

### Curl-kommando
Efter att ha klickat "Execute" kan du se motsvarande curl-kommando under "Curl".

## Testdata

Testdata som laddas automatiskt vid serverstart:

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

## Tips och tricks

### 1. Schema-validering
Swagger UI validerar automatiskt din input mot schemat. Om du anger felaktiga värden visas valideringsfel.

### 2. Exempel
Klicka på "Example Value" i request body för att automatiskt fylla i ett giltigt exempel.

### 3. Export OpenAPI Spec
Du kan ladda ner OpenAPI-specifikationen från:
```
http://localhost:8080/openapi
```

### 4. Använd i Postman
Du kan importera OpenAPI-specifikationen till Postman:
1. Öppna Postman
2. File → Import
3. Klistra in: `http://localhost:8080/openapi`

### 5. Generera klientkod
Använd OpenAPI Generator för att generera API-klienter:
```bash
# Ladda ner spec
curl http://localhost:8080/openapi > mobilenotes-api.yaml

# Generera Kotlin-klient
openapi-generator generate -i mobilenotes-api.yaml -g kotlin -o generated-client/
```

## Anpassa Swagger UI

### Ändra path
I `Application.kt`:
```kotlin
swaggerUI(path = "api-docs", swaggerFile = "openapi/documentation.yaml")
```
Då blir URL:en: `http://localhost:8080/api-docs`

### Uppdatera dokumentation
Redigera filen:
```
server/src/main/resources/openapi/documentation.yaml
```

Efter ändringar, starta om servern för att se uppdateringarna.

## Felsökning

### Swagger UI visas inte
- Kontrollera att servern körs på port 8080
- Verifiera att dependencies har laddats ner (synka Gradle)
- Kontrollera serverloggar för felmeddelanden

### "Failed to fetch"
- Kontrollera att endpoint finns i `documentation.yaml`
- Verifiera att endpoint är implementerat i `Application.kt` eller `NoteRoutes.kt`

### Schema-fel
- Kontrollera att request body matchar schemat i `documentation.yaml`
- Validera YAML-syntaxen (använd en online YAML validator)

## Säkerhet i produktion

⚠️ **Viktigt:** I produktion bör du:
1. Inaktivera Swagger UI eller skydda den med autentisering
2. Endast exponera OpenAPI-spec via säkra kanaler
3. Ta bort testdata och exempel med känslig information

Exempel på att inaktivera i produktion:
```kotlin
val isDevelopment = System.getProperty("io.ktor.development")?.toBoolean() ?: false
if (isDevelopment) {
    swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
}
```

## Resurser

- **Swagger UI Dokumentation:** https://swagger.io/tools/swagger-ui/
- **OpenAPI Specification:** https://swagger.io/specification/
- **Ktor OpenAPI Plugin:** https://ktor.io/docs/openapi.html

## Nästa steg

1. ✅ Utforska alla endpoints i Swagger UI
2. ✅ Testa att skapa, uppdatera och radera anteckningar
3. ✅ Exportera OpenAPI-spec och importera till Postman
4. ✅ Anpassa dokumentationen efter dina behov
5. ✅ Lägg till autentisering och säkerhet

Swagger UI är nu redo att användas! 🎉


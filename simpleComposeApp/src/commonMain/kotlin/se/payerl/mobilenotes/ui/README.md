# UI Package - Arkitektur och Best Practices

## 📁 Katalogstruktur

```
ui/
├── components/           # Återanvändbara UI-komponenter
│   ├── NoteDetailView.kt    # Detaljvy för anteckningar
│   └── TooltipComponents.kt # Återanvändbara tooltip-wrappers
├── dialogs/             # Dialog-komponenter
│   └── NoteDialogs.kt       # Alla dialoger (Create, Copy, Import, etc.)
├── models/              # Data models och konstanter
│   └── NoteModels.kt        # JsonKeys, CheckboxItem, ContentItem, ViewMode
├── parser/              # Business logic för parsing
│   └── NoteContentParser.kt # Interface och implementation
├── screens/             # Skärmar/views
│   └── NotesListScreen.kt   # Huvudskärm med lista och detaljvy
└── utils/               # Utility-funktioner
    └── DateUtils.kt         # Datumformatering
```

## 🎯 Design Principles

### 1. Separation of Concerns (SoC)
- **UI-komponenter** (`components/`, `screens/`) - Endast presentation och användarinteraktion
- **Business logic** (`parser/`) - Parsing och datahantering
- **Models** (`models/`) - Datastrukturer och konstanter
- **Dialoger** (`dialogs/`) - Separerade för återanvändning

### 2. Single Responsibility Principle (SRP)
Varje fil har ett enda, väldefinierat ansvar:
- `NoteModels.kt` - Endast data models
- `NoteContentParser.kt` - Endast parsing-logik
- `NoteDialogs.kt` - Endast dialog-komponenter
- `TooltipComponents.kt` - Endast återanvändbara tooltip-wrappers

### 3. DRY (Don't Repeat Yourself)
Alla repetitiva mönster har extraherats till återanvändbara komponenter:
```kotlin
// Innan: 15+ rader TooltipBox-kod upprepades överallt
// Efter: En enda rad
TooltipIconButton(
    tooltip = "Uppdatera",
    icon = Icons.Default.Refresh,
    contentDescription = "Uppdatera",
    onClick = onRefresh
)
```

### 4. Fail-Fast med Result<T>
Parser-logiken använder `Result<T>` för explicit felhantering:
```kotlin
fun parseToCheckboxItems(jsonContent: String): Result<List<CheckboxItem>>
```

### 5. Konstanter istället för Magic Strings
```kotlin
object JsonKeys {
    const val LINES = "lines"
    const val TEXT = "text"
    const val CHECKED = "checked"
    // ...
}
```

## 📊 Förbättringar från Refaktorisering

### Före refaktorisering:
- ❌ **1 fil, 1298 rader** - omöjlig att navigera
- ❌ Blandad UI-kod, parsing och models
- ❌ Magic strings överallt
- ❌ 15+ duplicerade TooltipBox-implementationer
- ❌ Tyst felhantering med `println()`
- ❌ Omöjligt att enhetstesta parsing-logik

### Efter refaktorisering:
- ✅ **8 filer, välorganiserade**
- ✅ `NotesListScreen.kt` reducerad till **249 rader** (80% minskning)
- ✅ Tydlig separation: UI, logic, models, dialogs
- ✅ Återanvändbara komponenter
- ✅ Explicit felhantering med `Result<T>`
- ✅ Testbar parsing-logik via interface
- ✅ Alla JSON-nycklar som konstanter

## 🧪 Testbarhet

### Parser kan nu testas isolerat:
```kotlin
class NoteContentParserTest {
    private val parser = JsonNoteContentParser()
    
    @Test
    fun `parseToCheckboxItems handles empty content`() {
        val result = parser.parseToCheckboxItems("""{"lines": []}""")
        assertTrue(result.isSuccess)
        assertEquals(emptyList(), result.getOrNull())
    }
}
```

### Mock-vänlig interface:
```kotlin
interface NoteContentParser {
    fun parseToCheckboxItems(jsonContent: String): Result<List<CheckboxItem>>
    fun parseToContentItems(jsonContent: String): Result<List<ContentItem>>
    fun convertToFreeText(jsonContent: String, notes: List<NoteDto>): Result<String>
    fun serializeCheckboxItems(items: List<CheckboxItem>): String
}
```

## 📝 Användning

### Att använda parser:
```kotlin
val parser = JsonNoteContentParser()
val result = parser.parseToCheckboxItems(note.content)

result.onSuccess { items ->
    // Hantera items
}.onFailure { exception ->
    // Hantera fel
}
```

### Att använda återanvändbara komponenter:
```kotlin
TooltipIconButton(
    tooltip = "Kopiera",
    icon = Icons.Default.FileCopy,
    contentDescription = "Kopiera",
    onClick = onCopyClick
)
```

## 🔄 Framtida Förbättringar

1. **ViewModel-arkitektur**: Flytta state-management från Composables till ViewModels
2. **Dependency Injection**: Använd Koin eller Hilt för att injicera parser
3. **Logging**: Implementera proper logging istället för println()
4. **Error Boundaries**: Skapa error boundary-komponenter för UI-fel
5. **Unit Tests**: Lägg till enhetstester för parser och business logic

## 📚 Best Practices

### När du skapar ny kod:
1. ✅ Placera UI-komponenter i `components/`
2. ✅ Placera business logic i separata klasser (inte i Composables)
3. ✅ Använd konstanter från `JsonKeys` istället för strings
4. ✅ Returnera `Result<T>` för operationer som kan misslyckas
5. ✅ Skapa återanvändbara komponenter för repetitiv UI-kod
6. ✅ Håll filer under 300 rader
7. ✅ En fil = Ett ansvar

### Kodgranskning - Checklista:
- [ ] Följer filen SRP (Single Responsibility)?
- [ ] Är all business logic separerad från UI?
- [ ] Används konstanter istället för magic strings?
- [ ] Finns det repetitiv kod som kan extraheras?
- [ ] Är felhantering explicit och tydlig?
- [ ] Är koden testbar?
- [ ] Är filen under 300 rader?

## 🎓 Lärdomar

Denna refaktorisering visar hur viktigt det är att:
- Dela upp stora filer i hanterbara komponenter
- Separera concerns (UI, logic, data)
- Tänka på testbarhet från början
- Använda konstanter och type-safety
- Skapa återanvändbara komponenter

**"Good code is easy to read, easy to test, and easy to change."**


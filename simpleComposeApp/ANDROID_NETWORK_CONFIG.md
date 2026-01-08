# Android Nätverkskonfiguration

## Problem
Android-emulatorn har sitt eget nätverk och kan inte direkt nå värddatorns `localhost`. Detta innebär att appen inte kan ansluta till servern som körs på din utvecklingsdator.

## Android-behörigheter (VIKTIGT!)
AndroidManifest.xml måste innehålla följande behörigheter för att appen ska kunna använda internet:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

Dessa behörigheter är redan konfigurerade i `composeApp/src/androidMain/AndroidManifest.xml`.

## Lösning
Applikationen använder nu plattformsspecifika API-URL:er genom Kotlin Multiplatform's `expect`/`actual` mekanism:

### Android-emulator
- **URL**: `http://10.0.2.2:8080`
- **Förklaring**: `10.0.2.2` är Android-emulatorns speciella IP-adress för att nå värddatorns `localhost`
- **Filer**: `PlatformConfig.android.kt`

### Desktop (JVM)
- **URL**: `http://localhost:8080`
- **Förklaring**: Desktop-appen körs på samma maskin som servern
- **Filer**: `PlatformConfig.jvm.kt`

### Web
- **URL**: `http://localhost:8080`
- **Förklaring**: Webbläsaren körs på samma maskin som servern
- **Filer**: `PlatformConfig.web.kt`

## Serverns konfiguration
Servern är konfigurerad att lyssna på `0.0.0.0:8080`, vilket betyder att den accepterar anslutningar från:
- localhost (127.0.0.1)
- Android-emulator (10.0.2.2 från emulatorns perspektiv)
- Andra enheter på samma nätverk (via datorns IP-adress)

Se `server/src/main/kotlin/se/payerl/mobilenotes/Application.kt`:
```kotlin
embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
```

## För fysiska Android-enheter

Om du vill testa på en fysisk Android-enhet (istället för emulatorn):

1. **Hitta din dators IP-adress**:
   ```cmd
   ipconfig
   ```
   Leta efter IPv4-adressen (t.ex. `192.168.1.100`)

2. **Uppdatera Android-konfigurationen**:
   Redigera `composeApp/src/androidMain/kotlin/se/payerl/mobilenotes/api/PlatformConfig.android.kt`:
   ```kotlin
   actual fun getApiBaseUrl(): String {
       // Använd din dators IP-adress
       return "http://192.168.1.100:8080"
   }
   ```

3. **Se till att båda enheterna är på samma WiFi-nätverk**

## Felsökning

### Android-emulator kan inte ansluta
1. Kontrollera att servern körs: `gradlew.bat :server:run`
2. Kontrollera att servern lyssnar på `0.0.0.0:8080` (inte bara `localhost:8080`)
3. Testa anslutningen från emulatorn med `curl`:
   ```bash
   # I Android Studio Terminal eller ADB shell
   adb shell
   curl http://10.0.2.2:8080
   ```

### Fysisk enhet kan inte ansluta
1. Kontrollera att båda enheterna är på samma WiFi
2. Kontrollera brandväggen på din dator - den måste tillåta inkommande anslutningar på port 8080
3. Testa från telefonen:
   - Öppna webbläsaren på telefonen
   - Navigera till `http://[DIN_DATOR_IP]:8080`
   - Du borde se serversvaret

### Nätverksfel i appen
Felmeddelanden som "Unable to resolve host" eller "Connection refused" betyder vanligtvis att:
- Servern inte körs
- Fel IP-adress konfigurerad
- Brandvägg blockerar anslutningen
- Android-enheten inte har internetåtkomst

## Alternativ konfiguration med miljövariabler (avancerat)

För mer flexibilitet kan du skapa olika build-varianter:

```kotlin
// build.gradle.kts
android {
    buildTypes {
        debug {
            buildConfigField("String", "API_URL", "\"http://10.0.2.2:8080\"")
        }
        release {
            buildConfigField("String", "API_URL", "\"https://production-server.com\"")
        }
    }
}
```

Detta är särskilt användbart när du har olika servrar för utveckling och produktion.


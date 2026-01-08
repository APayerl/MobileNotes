package se.payerl.mobilenotes.api

/**
 * Android-specifik implementation av API base URL.
 * Använder 10.0.2.2 för att nå värddatorns localhost från Android-emulatorn.
 * 
 * OBS: För fysiska enheter måste du använda din dators faktiska IP-adress.
 */
actual fun getApiBaseUrl(): String {
    // 10.0.2.2 är Android-emulatorns speciella IP för värddatorns localhost
    return "http://10.0.2.2:8080"
}


Sample app to set the device locale in an Android device (for testing localization).

To programatically set the device local, one of the following is required:
- root
- su granted
- Run `adb shell pm grant com.example.locale android.permission.CHANGE_CONFIGURATION`

Features:
- Material design (who says a util app has to be ugly?)
- No ads
- Search by language code, country code, and country name
- Add a custom locale
- Home screen widget to quickly set a locale

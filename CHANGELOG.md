card.io Cordova Plugin Release Notes
====================================

2.0.2
-----
* Android: Add ability to blur all digits in the scanned card image, minus any number of digits to remain unblurred, enabled via `CardIOActivity.EXTRA_UNBLUR_DIGITS`.   (Thank you Michael Schmoock!)
* Android: Fix issue where Maestro cards were not correctly recognized [#154](https://github.com/card-io/card.io-Android-SDK/issues/154).
* Android: Fix issue on Android 23 and above where `CardIOActivity#canReadCardWithCamera()` would return the incorrect value if permissions had not been granted [#136](https://github.com/card-io/card.io-Android-SDK/issues/136).  Now defaults to `true` in such cases.
* Android: Add missing locales to javadocs [card.io-Android-source#75](https://github.com/card-io/card.io-Android-source/issues/75).
* Android: Upgrade gradle to 2.13.
* Android: Upgrade Android Gradle plugin to 2.1.0.

2.0.1
------
* Fixes Plugin.xml

2.0.0
------
* Added more configurations to cordova app
* Added support for Android
* Updated CardIO iOS version to 5.3.2

1.0.0
------
* simplify integration instructions
* add CardIO library 4.0.0
* fix deprecated warnings
* re-arrange structure for plugman support
* minor cleanup

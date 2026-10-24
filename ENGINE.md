# Rendering engine investigation

The goal is useful modern-page support on Android 4.0+ without bundling Chromium. The current system-WebView prototype establishes the native interface only.

Modern GeckoView requires Android 8 / API 26. Older Firefox/Gecko versions that supported Android 4.0 are discontinued and do not provide a maintained modern engine. Ultralight does not offer a supported Android SDK. Servo embedding remains development work, not an Android 4.0 drop-in library.

A new Gecko or Servo port to API 14 requires a dedicated native-engine project: toolchain and libc compatibility, rendering/GPU fallback, font and media integration, Java bindings, memory work and an ongoing security-update process. Adding Java dependencies or new root certificates cannot accomplish that port.

Possible next steps are a separately supported API26 Gecko edition, a dedicated legacy-engine port, or an opt-in remote renderer with its own server and privacy constraints. None is silently substituted for the requested local modern engine in this repository.

## References

- https://bugzilla.mozilla.org/show_bug.cgi?id=1982312
- https://firefox-source-docs.mozilla.org/mobile/android/geckoview/contributor/geckoview-quick-start.html
- https://support.mozilla.org/en-US/kb/firefox-android-68-and-earlier-end-support
- https://ultralig.ht/
- https://doc.servo.org/

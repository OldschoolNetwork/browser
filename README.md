# ON Browser

An Android 4.0+ Holo browser interface prototype: address/search, back and forward navigation, reload, up to six tabs, bookmarks, bounded history, downloads and page-feature settings. The supplied ON Browser icon retains its transparency.

**Renderer status:** this build uses the system WebView. It does not include a modern independent browser engine. Android 4.0–4.3 uses the platform WebKit; newer systems may use a Chromium-derived WebView. Modern page support depends on the installed platform renderer.

JavaScript, images, storage, cookies and desktop user-agent options control page behaviour. They do not add unsupported JavaScript syntax or CSS features. Invalid HTTPS certificates are rejected; no trust-all mode is provided.

## Build

JDK 8, SDK platform 28, build-tools 28.0.3, Gradle wrapper 5.4.1. Minimum API 14. Configure your SDK locally, then run `gradlew.bat assembleDebug`.

See [ENGINE.md](ENGINE.md) for the compatibility investigation and remaining engine work. This is a preview, not a completed modern-engine release.

## Links

[Website](https://oldschoolnetwork.online) · [Documentation](https://oldschoolnetwork.online/downloads/docs/) · [Bluesky](https://bsky.app/profile/oldnet.bsky.social) · [YouTube](https://www.youtube.com/@oldschoolnetwork) · [X](https://x.com/oldschool_net) · [Telegram](https://t.me/OldschoolNetwork)

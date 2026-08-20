# SmartDialer Privacy

SmartDialer is offline-first. Core dialing, local rules, timers, private entries, blocking settings, themes, contacts lookup, and call history views are designed to work without internet access.

## Data accessed
- Contacts: used to display names, photos, numbers, favorites, and T9 lookup.
- Call logs: used to show recents and analytics when permission is granted.
- Phone state/Telecom: used for supported incoming, outgoing, ongoing-call, and screening behavior.

## Data stored locally
SmartDialer-specific rules, notes, private-contact metadata, timer preferences, reminders, and analytics summaries should be stored locally. The app must not duplicate the entire Android contacts or call-log database.

## Private data protection
Private contacts and PIN-protected features must distinguish SmartDialer privacy from system-level Android privacy. Android, the carrier, the default Telecom stack, or system call history may still expose call information depending on platform behavior. Raw PINs must never be stored; Android Keystore-backed cryptographic storage should be used as the privacy implementation is completed.

## Network
No contact, call-log, or private-contact data leaves the device unless the user explicitly enables a future sync or external spam database feature.

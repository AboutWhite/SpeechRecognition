﻿Das Android-Projekt ist unter liblsl-Android/AndroidStudio zu finden. Das zu startende Modul heißt ReceiveStringMarkers - das Modul SendStringMarkers sollte automatisch nicht geladen werden und kann ignoriert werden; im Dateiordner wird immer noch automatisch eine .iml-Datei erstellt, der Ordner ist aber ansonsten leer.

Die Namen passen nicht - warum?
LSL in unser Projekt einzubinden hat nicht funkioniert, das Projekt in LSL einzusetzen hat nicht funktioniert, also musste ich den Java-Code von Hand in LSL einbetten, um externe Abhängigkeiten nicht zu löschen/falsch zu verlinken. Und diese Abhängigkeiten sind anscheinend u.a. namensbezogen, wodurch ich die Namen von Ordnern/Dateien nicht anfassen will. Ansonsten kann es sein, dass uns sonst irgendwann alles über dem Kopf zusammenbricht und wir keine Ahnung haben, was genau da jetzt falsch referenziert, weil Namen nicht korrekt sind.

Achtung: Außer in liblsl-Android/AndroidStudio/ReceiveStringMarkers bitte nichts verändern! Alle außerhalb dieses Ordners vorhandenen Dateien sind notwendig und dürfen nicht gelöscht werden!

Sollte das Projekt nicht zusammengebaut werden können, bitte hier (https://developer.android.com/ndk/downloads/older_releases) die 14b (März 2017) Version herunterladen und als NDK einfügen.
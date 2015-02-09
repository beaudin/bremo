@echo off

echo = IFKM-BREMO =
echo Bremo.jar erstellen
echo ------------------------------
jar cvfm Applet/Bremo.jar MANIFEST.MF -C bin/ .

echo Bremo.jar signieren
echo ------------------------------
rem * Das signierte Applet wird wieder im Ordner "Applet" abgelegt
rem * In Anführungszeichen den Alias des Zertifikatschlüssels angeben
rem * mit dem Befehl "-keystore" den Pfad zum Zertifikatschlüssel angeben
rem * Der Schlüssel darf NICHT im Workspace liegen!
jarsigner -verbose signedjar Applet/Bremo.jar Applet/Bremo.jar "das software-sicherheitsmodul:karlsruhe institute of technology id von grp: softwareentwicklung am ifkm" -keystore D:/Daten/BREMO/Zertifikat/BREMO.p12 -storetype pkcs12
rem echo Weitere *.jar signieren
rem echo ------------------------------
rem jarsigner -verbose signedjar Applet/jcommon-1.0.18.jar bibliothek/jcommon-1.0.18/jcommon-1.0.18.jar "das software-sicherheitsmodul:karlsruhe institute of technology id von grp: softwareentwicklung am ifkm" -keystore Zertifikat/BREMO.p12 -storetype pkcs12
rem jarsigner -verbose signedjar Applet/jfreechart-1.0.15.jar bibliothek/jfreechart-1.0.15/jfreechart-1.0.15.jar "das software-sicherheitsmodul:karlsruhe institute of technology id von grp: softwareentwicklung am ifkm" -keystore Zertifikat/BREMO.p12 -storetype pkcs12

pause

echo.
echo Zertifikat testen
echo ----------------------------
rem --> anpassen!
jarsigner -verify -verbose -certs Bremo.jar

echo Zur Kontrolle: Ein paar Zeilen weiter oben sollte nun 'jar verified' stehen!
echo Das ist die Bestaetigung dafür, dass die Jar-Datei erfolgreich signiert wurde.

rem Konsole für Ausgaben noch geöffnet lassen
echo.
pause
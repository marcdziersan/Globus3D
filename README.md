# Globus3D – JavaFX 3D Earth–Moon–ISS Simulation

Globus3D ist ein kleines, in reinem Java / JavaFX geschriebenes Lernprojekt.  
Die Anwendung visualisiert ein vereinfachtes Erde–Mond–System in 3D und ergänzt es um eine Internationale Raumstation (ISS) in niedriger Erdumlaufbahn.

Beim Start fährt die Kamera automatisch heraus (Auto-Zoom), anschließend kann die Szene frei mit Maus und Mausrad erkundet werden.

---

## Features

- 3D-Darstellung der Erde als Textur-Kugel (Sphere)  
- Realistische Achsneigung der Erde (ca. 23,5°) mit passender Rotation
- Mond mit:
  - geneigter Umlaufbahn (ca. 5°)
  - gebundener Rotation (immer dieselbe Seite zur Erde)
- ISS als kleines 3D-Objekt in niedriger, geneigter Umlaufbahn (ca. 51,6°)
- Auto-Zoom beim Start: Kamera fährt von „nah“ auf den maximalen Auszoom-Punkt
- Interaktive Steuerung:
  - Linke Maustaste gedrückt + ziehen: Szene drehen
  - Mausrad: rein-/rauszoomen (nach Auto-Zoom)

---

## Was man an diesem Projekt lernen kann

Dieses Repository eignet sich gut, um folgende Themen praktisch nachzuvollziehen:

- Grundlagen von **JavaFX** ohne zusätzliche Frameworks
- Aufbau einer **3D-Szene** mit `Sphere`, `Box`, `Group` und `PerspectiveCamera`
- Arbeiten mit **Texturen** (`Image`, `PhongMaterial`) und lokalen Bilddateien
- Einsatz von **Transformationsmatrizen**:
  - `Rotate` für Achsneigung und Objektrotation
  - Zusammenspiel von mehreren `Rotate`-Instanzen (z. B. Tilt + Spin)
- Implementierung von **kontinuierlichen Animationen** mit `AnimationTimer`
  - Berechnung von Winkeln aus Zeitdifferenzen (`deltaSec`)
  - Simulieren von Umlaufbahnen (Orbit) über trigonometrische Funktionen
- Einfache **Benutzerinteraktion**:
  - Maus-Events (`MOUSE_DRAGGED`) für 3D-Rotation
  - Scroll-Events (`SCROLL`) für Zoom
- Struktur eines **minimalen JavaFX-Projekts**, das ohne IDE laufen kann (nur `javac` / `java` + Batch-Skripte)

---

## Projektstruktur

Die Struktur entspricht dem Screenshot und ist so auch für GitHub gedacht:

```text
GLOBUS3D/
├─ lib/
│  ├─ javafx-base.jar
│  ├─ javafx-controls.jar
│  ├─ javafx-fxml.jar
│  ├─ javafx-graphics.jar
│  ├─ javafx-media.jar
│  ├─ javafx-swing.jar
│  ├─ javafx-swt.jar
│  ├─ javafx-web.jar
│  └─ javafx.properties
│
├─ out/
│  ├─ Globe3DApp.class
│  ├─ Globe3DApp$1.class
│  ├─ Globe3DApp$2.class
│  ├─ Globe3DApp$3.class
│  ├─ Globe3DApp$4.class
│  └─ Globe3DApp$5.class
│
├─ src/
│  └─ Globe3DApp.java
│
├─ build.bat
├─ run.bat
├─ earth.jpg
├─ moon.jpg
└─ iss.png
````

**Hinweise zur Struktur:**

* `src/Globe3DApp.java`
  Enthält die komplette JavaFX-Applikation (Erde, Mond, ISS, Camera-Steuerung).

* `out/`
  Zielordner für die kompilierten `.class`-Dateien.
  Die Batch-Skripte (`build.bat`, `run.bat`) gehen davon aus, dass hierhin kompiliert und von hier gestartet wird.

* `lib/`
  Enthält die JavaFX-Runtime-JARs.
  Diese werden per `--module-path` und `--add-modules` an `javac`/`java` übergeben.

* `earth.jpg`, `moon.jpg`, `iss.png`
  Texturen / Bilder, die zur Laufzeit relativ zum aktuellen Arbeitsverzeichnis geladen werden.
  Beim Start muss das Arbeitsverzeichnis also das Projekt-Root (GLOBUS3D) sein.

---

## Voraussetzungen

* Java JDK 17 oder neuer (getestet mit aktuellen OpenJDK-/Liberica-Versionen)
* JavaFX SDK (die benötigten JARs liegen im Ordner `lib/`)
* Betriebssystem: Windows (Batch-Skripte).
  Unter Linux/macOS funktionieren die Kommandos analog, nur ohne `.bat`.

---

## Build

Die exakte Implementierung von `build.bat` kann je nach JavaFX-Version leicht variieren.
Ein mögliches Beispiel (für Windows) könnte so aussehen:

```bat
@echo off
mkdir out 2>nul

javac ^
  --module-path ".\lib" ^
  --add-modules javafx.controls,javafx.graphics,javafx.fxml,javafx.web,javafx.media,javafx.swing ^
  -d out ^
  src\Globe3DApp.java
```

Alternativ kannst du direkt auf der Kommandozeile kompilieren:

```bash
javac --module-path ./lib ^
      --add-modules javafx.controls,javafx.graphics,javafx.fxml,javafx.web,javafx.media,javafx.swing ^
      -d out ^
      src/Globe3DApp.java
```

Wichtig:

* `-d out` schreibt alle `.class`-Dateien in den Ordner `out/`.
* `--module-path ./lib` zeigt auf den Ordner mit den JavaFX-JARs.
* Die Liste in `--add-modules` kann bei Bedarf reduziert werden (für dieses Projekt werden primär `javafx.graphics` und `javafx.controls` benötigt).

---

## Run

Auch hier hängt die konkrete `run.bat` von deiner lokalen Umgebung ab.
Typischer Inhalt für Windows:

```bat
@echo off
java ^
  --module-path ".\lib" ^
  --add-modules javafx.controls,javafx.graphics,javafx.fxml,javafx.web,javafx.media,javafx.swing ^
  -cp ".\out" ^
  Globe3DApp
```

Direkt auf der Kommandozeile (PowerShell / CMD):

```bash
java --module-path ./lib ^
     --add-modules javafx.controls,javafx.graphics,javafx.fxml,javafx.web,javafx.media,javafx.swing ^
     -cp ./out ^
     Globe3DApp
```

Stelle sicher, dass du den Befehl aus dem Projekt-Root (`GLOBUS3D`) heraus ausführst, damit die Texturen `earth.jpg`, `moon.jpg` und `iss.png` gefunden werden.

---

## Konfigurierbare Parameter (in `Globe3DApp.java`)

Im Quellcode sind viele Werte als `static final`-Konstanten definiert, u. a.:

* `GLOBE_RADIUS` – Größe der Erde (Sphere-Radius)
* `MOON_RADIUS`, `MOON_DISTANCE` – Größe und Abstand des Mondes
* `EARTH_ROTATION_PERIOD_SEC` – Simulationsdauer einer Erdumdrehung
* `MOON_ORBIT_PERIOD_SEC`, `MOON_SELF_ROTATION_PERIOD_SEC` – Umlauf- und Rotationsdauer des Mondes
* `ISS_ORBIT_RADIUS`, `ISS_ORBIT_PERIOD_SEC`, `ISS_ORBIT_INCLINATION_DEG` – ISS-Bahnhöhe, Umlaufzeit, Bahnneigung
* `ZOOM_NEAR`, `ZOOM_FAR`, `ZOOM_SPEED` – Zoombereich und -geschwindigkeit
* `AUTO_ZOOM_DURATION_SEC`, `AUTO_ZOOM_START_Z` – Verhalten des automatischen Start-Zooms

Damit lässt sich das physikalische Verhalten (schneller/langsamer, näher/weiter, andere Bahnen) sehr einfach anpassen.

---

## Texturen & Quellen

Die Dateien `earth.jpg`, `moon.jpg` und `iss.png` sind **nicht** Bestandteil des Java-Codes und müssen separat bereitgestellt werden.
Bitte achte darauf, nur Texturen zu verwenden, für die du die entsprechenden Nutzungsrechte besitzt (z. B. frei verfügbare NASA-/ESA-Daten oder eigene Assets).

---

## Lizenz

Dieses Projekt steht unter der **MIT License**.

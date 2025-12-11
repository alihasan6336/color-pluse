# 🎨 Color Pluse — OpenGL Color Switch Clone

Color Pluse is a modern, fast OpenGL clone of the popular **Color Switch** game, built using **JOGL (Java OpenGL)** and **Java 17**.  
The goal is simple: **jump through rotating rings — but only through sections that match your ball's color.**  
Each ring cleared increases your score. One wrong color… and it's game over!

---

## 🕹️ Gameplay

- Press **SPACE** to jump.
- Avoid touching any ring segment that does **NOT** match your ball’s current color.
- Pass through correctly → **Score +1**
- Collect color changers to get a new random color.
- If you fall or hit the wrong color → **GAME OVER**

The world moves upward dynamically as you progress, generating infinite rings with random rotations.

---

## ⭐ Features

- 🎆 Smooth OpenGL rendering using **JOGL**
- 🔄 Randomly generated rings & animations
- 🌈 Color changer pickups
- ⭐ Dynamic background stars
- 🎯 Point system + real-time collision detection
- ⌨ Keyboard controls
- ⚡ Stable 60 FPS animation loop
- 🧩 Clean object-oriented architecture

---

## 📂 Project Structure
src/main/java/org/example/
│
├── Main.java # App entry point (window + GLCanvas)
├── Game.java # Game loop + update + render
├── StartPage.java # Start UI screen (Play button)
│
├── PlayerBall.java # Player ball physics + colors
├── Ring.java # Rotating rings + collision logic
├── ColorChanger.java # Color pickup logic
├── BackgroundStars.java # Animated starfield
└── GameStateListener.java # Callbacks for game state changes


---

## 🚀 How to Run (Maven)

### Requirements
- **Java 17**
- **Apache Maven 3.9+**
- GPU compatible with OpenGL 2.0+  
  (NVIDIA / AMD / Intel)

### Run the game:

```sh
mvn clean compile exec:java -Dexec.mainClass="org.example.Main"
IntelliJ is used:

Import → Maven project

Run Main.java

🎮 Controls
Key	Action
SPACE	Jump
ESC	Exit / Return to Start Screen
🧠 Technical Notes

Physics uses simple gravity simulation.

Rings rotate independently using randomized speeds.

Collision detection is angle-based for precision.

Camera shifts upward automatically to create infinite level generation.

Game resets dynamically without recreating the window.
```

📜 License

MIT License.
Feel free to modify, extend, or contribute.


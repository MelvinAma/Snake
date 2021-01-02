import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.commons.lang3.ArrayUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {
    static final int SCREEN_WIDTH = 800;
    static final int SCREEN_HEIGHT = 800;
    static final int UNIT_SIZE = 50;
    static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / UNIT_SIZE;
    static final int DELAY = 100;
    final int[] x = new int[GAME_UNITS];
    final int[] y = new int[GAME_UNITS];
    int[] allCoordinates_X = new int[(GAME_UNITS / SCREEN_WIDTH) * (GAME_UNITS / SCREEN_WIDTH)];
    int[] allCoordinates_Y = new int[(GAME_UNITS / SCREEN_HEIGHT) * (GAME_UNITS / SCREEN_HEIGHT)];
    int bodyParts = 5;
    int applesEaten;
    int appleX;
    int appleY;
    char direction = 'R';
    boolean running = false;
    Timer timer;
    Random random;
    final JFXPanel fxPanel = new JFXPanel();
    public static final String AUDIO_PATH = "C:\\Users\\Melvin Amandusson\\Downloads\\appleBite.wav";
    Media media = new Media(new File(AUDIO_PATH).toURI().toString());
    MediaPlayer mediaPlayer = new MediaPlayer(media);

    GamePanel() {
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground((Color.black));
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        // Neccessary initialization for audio output
        final JFXPanel fxPanel = new JFXPanel();
        startGame();
    }

    public void startGame() {
        newApple();
        running = true;
        timer = new Timer(DELAY, this);
        timer.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        if (running) {
            // Grid:
            for (int i = 0; i < SCREEN_HEIGHT / UNIT_SIZE; i++) {
                g.drawLine(i * UNIT_SIZE, 0, i * UNIT_SIZE, SCREEN_HEIGHT);
                g.drawLine(0, i * UNIT_SIZE, SCREEN_WIDTH, i * UNIT_SIZE);
            }
            g.setColor(Color.red);
            g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

            g.setColor(Color.green);
            for (int i = 0; i < bodyParts; i++) {
                if (i == 0) {
                    g.setColor(Color.green);
                } else {
                    g.setColor(new Color(0, 128, 0));
                }
                g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
            }
            g.setColor(Color.red);
            g.setFont(new Font("Times New Roman", Font.BOLD, 20));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString(
                    "Score: " + applesEaten,
                    (SCREEN_WIDTH - metrics.stringWidth("Score: " + applesEaten)) / 2,
                    g.getFont().getSize());
        } else {
            gameOver(g);
        }
    }

    public void newApple() {

        // Makes sure the apple is not placed inside the snake
        int[] xy = findFreeCoordinates(); // xy[0] = x & xy[1] = y
        appleX = xy[0];
        appleY = xy[1];
    }

    // Removes all squares occupied by the snake from an Array containing all coordinates
    private int[] findFreeCoordinates() {
        // allCoordinates_X[i] and allCoordinates_Y[i] are coordinates for the same square
        // Loops through each row:
        for (int i = 0; i < GAME_UNITS / SCREEN_WIDTH; i++) {
            // One full row
            for (int j = 0; j < GAME_UNITS / SCREEN_WIDTH; j++) {
                allCoordinates_X[j + (GAME_UNITS / SCREEN_WIDTH) * i] = j * UNIT_SIZE;
                allCoordinates_Y[j + (GAME_UNITS / SCREEN_WIDTH) * i] = i * UNIT_SIZE;
            }
        }
        // Loops through all squares
        for (int i = 0; i < allCoordinates_X.length; i++) {
            // Loops through the bodyparts
            for (int j = 0; j < bodyParts + 1; j++) {
                // If square is occupied by the snake
                if (allCoordinates_X[i] == x[j] && allCoordinates_Y[i] == y[j]) {
                    // Removes the coordinates currently occupied by the snake
                    allCoordinates_X[i] = -1;
                    allCoordinates_Y[i] = -1;
                    break;
                }
            }
        }
        // Generates a random index, and rerolls if the value at said index has been set to -1
        int randomInt;
        do {
            randomInt = random.nextInt(allCoordinates_X.length);
        } while (allCoordinates_X[randomInt] < 0 || allCoordinates_Y[randomInt] < 0);

        return new int[]{allCoordinates_X[randomInt], allCoordinates_Y[randomInt]};
    }

    public void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }
        switch (direction) {
            case 'U':
                y[0] = y[0] - UNIT_SIZE;
                break;
            case 'D':
                y[0] = y[0] + UNIT_SIZE;
                break;
            case 'L':
                x[0] = x[0] - UNIT_SIZE;
                break;
            case 'R':
                x[0] = x[0] + UNIT_SIZE;
                break;
        }
    }

    public void checkApple() throws FileNotFoundException, MalformedURLException {
        if ((x[0] == appleX) && (y[0] == appleY)) {
            bodyParts++;
            applesEaten++;
            newApple();
            playAudio();
        }
    }

    private void playAudio() {
        Media media = new Media(new File(AUDIO_PATH).toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();
    }

    public void checkCollisions() {
        // Checks if head collides with body
        for (int i = bodyParts; i > 0; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                running = false;
                break;
            }
        }
        // Checks if head touches left border
        if (x[0] < 0) {
            running = false;
        }
        // Checks if head touches right border
        if (x[0] >= SCREEN_WIDTH) {
            running = false;
        }
        // Checks if head touches top border
        if (y[0] < 0) {
            running = false;
        }
        // Checks if head touches bottom border
        if (y[0] >= SCREEN_HEIGHT) {
            running = false;
        }
        if (!running) {
            timer.stop();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            try {
                checkApple();
            } catch (FileNotFoundException | MalformedURLException fileNotFoundException) {
                System.out.println("ERRORERRO");
            }
            checkCollisions();
        }
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP:
                    if (direction != 'D') {
                        direction = 'U';
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if (direction != 'U') {
                        direction = 'D';
                    }
                    break;
                case KeyEvent.VK_LEFT:
                    if (direction != 'R') {
                        direction = 'L';
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if (direction != 'L') {
                        direction = 'R';
                    }
                    break;
                case KeyEvent.VK_ENTER: // Restarts the game
                    for (int i = 0; i < bodyParts; i++) {
                        x[i] = 0;
                        y[i] = 0;
                    }
                    applesEaten = 0;
                    bodyParts = 6;
                    direction = 'R';
                    timer.stop();
                    startGame();
                    //new GameFrame().setVisible(true);
                    break;
            }
        }
    }

    public void gameOver(Graphics g) {
        // Score
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 40));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString(
                "Score: " + applesEaten,
                (SCREEN_WIDTH - metrics1.stringWidth("Score: " + applesEaten)) / 2,
                SCREEN_HEIGHT / 2 + 50);
        // Game over text
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString(
                "Game Over",
                (SCREEN_WIDTH - metrics2.stringWidth("Game Over")) / 2,
                SCREEN_HEIGHT / 2);
    }
}
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

import java.io.BufferedReader;  // Import for reading files
import java.io.FileReader;  // Import for reading iles
import java.io.FileWriter;  // Import for writing files
import java.io.IOException; // Import for handling IOExceptions
import javax.swing.Timer; // Make sure to import the Timer

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 360;
    int boardHeight = 640;

    //Additional variables for high score and game state
    int highScore = 0;
    boolean gameStarted = false;

    //Images
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    //Bird
    int birdX = boardWidth/8;
    int birdY = boardHeight/2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    //Pipes
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;  //scaled by 1/6
    int pipeHeight = 512;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }


    //upwards -y, downwards +y, left -x, right +x
    //game logic
    Bird bird;
    int velocityX = -4; //moves pipes to the left speed (simulates bird moving right)
    int velocityY = 0;
    int gravity = 1;

    ArrayList<Pipe> pipes; 
    Random random = new Random();

    Timer gameLoop;
    Timer placePipesTimer;
    boolean gameOver = false;
    double score = 0;
    int lastMilestone =  0; // Tracks the last mileston reached
    boolean showMessage = false; // Controls whether the message should be displayed
    Timer messageTimer; // Timer to control the duration of the message
    String milestoneMessage = ""; // The message to dispalay

    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        // setBackground(Color.blue);
        setFocusable(true);
        addKeyListener(this);

        // load images
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        //bird
        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        //Load the high score when the game starts
        loadHighscore();

        //place pipes timer
        placePipesTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });
        placePipesTimer.start();

        //1000 is 1 second, 1500 is 1.5 seconds

        //game timer
        gameLoop = new Timer(1000/60, this); //1000/60 = 16.6
        gameLoop.start();

    }

    public void placePipes() {
        //(0-1) * pipeHeight/2 -> (0-256)
        //128
        //0 - 128 - (0-256) --> pipeHeight/4 -> 3/4 pipeHeight

        int randomPipeY = (int) (pipeY - pipeHeight/4 - Math.random()*(pipeHeight/2));
        int openingSpace = boardHeight/4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void saveHighScore() {
        try (FileWriter writer = new FileWriter("highscore.txt")) {
            writer.write(String.valueOf(highScore));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadHighscore() {
        try (BufferedReader reader = new BufferedReader(new FileReader("highscore.txt"))) {
            highScore = Integer.parseInt(reader.readLine());
        } catch (IOException e ) {
            highScore = 0; // Default to 0 if file does not exist or cannot be read
        }
     }

    public void draw(Graphics g) {
    
        //background
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);
        // 0, 0, is the title bar using x, y, starting positon top left corner

        //bird
        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

        //pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        // Score display during game play
        g.setColor(Color.white);
        g.setFont(new Font("Bosmatic", Font.BOLD, 50));
        //g.drawString("Score: " + String.valueOf((int) score), 10, 35);

        // High score display
        g.setFont(new Font("Bosmatic", Font.BOLD, 20));
        g.drawString("High Score: " + highScore, 195, 20);

        if (!gameStarted) {
            // g.setFont(new Font("Bosmatic", Font.BOLD, 15));
            // g.drawString("High Score: " + highScore, 240, 20);

            // "Press 'Space' to Start" prompt in the center
            g.setFont(new Font("Bosmatic", Font.BOLD, 20));
            g.drawString("Press 'Space' to Start", 70, 200);
        } 

        // Display "Good Job" message for reaching a milestone
        if (showMessage) {
            g.setFont(new Font("Bosmatic", Font.BOLD, 15));
            g.drawString(milestoneMessage, 60, 170); 
        }

        // Display "Good Job" message if the player reached 100 score
        //if (showGoodJobMessage) {
            //g.setFont(new Font("Bosmatic", Font.BOLD, 18));
            //g.drawString("Good job, you have reached 10! :D", 35, 100);
        //}

        if (gameOver) {
            g.setFont(new Font("Bosmatic", Font.BOLD, 40));
            g.setColor(Color.red);
            g.drawString("Game Over", 70, 270);
        }

        if (gameOver) {
            g.setFont(new Font("Bosmatic", Font.BOLD, 20));
            g.drawString("Your score: " + String.valueOf((int) score), 110, 300);
        } else {
            g.setFont(new Font("Bosmatic", Font.BOLD, 35));
            g.drawString(String.valueOf((int) score), 10,35);
        }
    }

    public void move() {
        //bird
        if (gameStarted && !gameOver) {
            // Apply gravity and move the bird only if the game has started
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);

        // Move pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                pipe.passed = true;
                score += 0.5; // 0.5 because there are 2 pipes so 0.5*2 = 1, 1 for each set of pipes
            }

            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        if (bird.y > boardHeight) {
            gameOver = true;
        }

        // Check if the score is a new multiple of 10
        if ((int) score >= lastMilestone + 10) {
            lastMilestone += 10; // Update the last milestone
            milestoneMessage = "Good Job! You have reached " + lastMilestone + "! :D"; 

            // Show the message
            showMessage = true;

            // Start a timer to hide the message after 3 seconds
            if (messageTimer != null) {
                messageTimer.stop(); // Stop any existing timer
            }

            messageTimer = new Timer(3000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showMessage = false; // Hide the message after 3 seconds
                    messageTimer.stop(); // Stop the timer
                }
            });
            messageTimer.setRepeats(true); // repeats
            messageTimer.start();
        }
    }
}
                

    public boolean collision(Bird a, Pipe b) {
        return  a.x < b.x + b.width &&  // a's top left corner doesn't reach b's top right corner
                a.x + a.width > b.x &&  // a's top right corner passes b's top left corner
                a.y < b.y + b.height && // a's top left corner doesn't reach b's bottom left corner
                a.y + a.height > b.y;   // a's bottom left corner passes b's top left corner
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            placePipesTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -9;

            if (!gameStarted) {
                gameStarted = true;
                velocityY = -9;
            } else if (gameOver) {
                // Restart the game after a Game Over
                // Check if the current score is higher than the high score
                if (score > highScore) {
                    highScore = (int) score;
                    saveHighScore(); // Save the new high score
                }

                //restart the game by resetting the coniditons
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                score = 0;
                gameOver = false;
                gameStarted = false; // Ensure the game hasn't started yet
                lastMilestone = 0; // Reset the last milestone to 0
                gameLoop.start();
                placePipesTimer.start();
            } else {
                // Normal space bar press during game
                velocityY = -9;
            }
        }
    }


    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
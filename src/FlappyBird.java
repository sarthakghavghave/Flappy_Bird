import java.awt.*;            //(abstract window toolkit)   for GUIs
import java.awt.event.*;      // event-handling
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener{
    int boardHeight = 640;
    int boardWidth = 360;

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

    //pipe
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

    class BackGround {
        int x = boardWidth;
        int y = 0;
        Image img;
        BackGround(Image img){
            this.img = img;
        }
    }

    int firstBGX = 0;

    //game logic
    Bird bird;
    int velocityX = -4;  //moves pipes to left
    int velocityY = 0;
    int gravity = 1;

    ArrayList<Pipe> pipes;
    ArrayList<BackGround> bgs;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipesTimer;
    Timer placeBGTimer;

    boolean gameOver = false;
    double score = 0.0;

    FlappyBird() {
        setPreferredSize(new Dimension(360, 640));

        setFocusable(true);
        addKeyListener(this);

        //load images
        backgroundImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("/resources/flappybirdbg.png"))).getImage();
        birdImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("/resources/flappybird.png"))).getImage();
        topPipeImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("/resources/toppipe.png"))).getImage();
        bottomPipeImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("/resources/bottompipe.png"))).getImage();

        //bird
        bird = new Bird(birdImg);
        pipes = new ArrayList<>();
        bgs = new ArrayList<>();

        //place pipes timer
        placePipesTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });
        placePipesTimer.start();

        //Background timer
        placeBGTimer = new Timer( 5000, new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                BackGround bg = new BackGround(backgroundImg);
                bgs.add(bg);
            }
        });
        placeBGTimer.setInitialDelay(0); // starts with 0th millisecond and then delays
        placeBGTimer.start();

        //Game timer
        gameLoop = new Timer(1000/60, this);
        gameLoop.start();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (gameOver && e.getButton() == MouseEvent.BUTTON1) {
                    gameOver = false;
                    bird.y = birdY;
                    pipes.clear();
                    score = 0;
                    velocityY = -9;
                    gameLoop.start();
                    placePipesTimer.start();
                    placeBGTimer.start();
                }
            }
        });
    }

    public void placePipes(){
        int randomPipeY = (int) (pipeY - pipeHeight/4 - Math.random()*(pipeHeight/2));
        int openingSpace = boardHeight/4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y  + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }
    public void draw(Graphics g) {
        //background
        g.drawImage(backgroundImg, firstBGX, 0, this.boardWidth, this.boardHeight, null);

        for(int i = 0; i < bgs.size(); i++){
            BackGround bg = bgs.get(i);
            g.drawImage(bg.img, bg.x,bg.y, this.boardWidth, this.boardHeight, null);
        }

        //bird
        g.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);

        //pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        g.setColor(Color.black);
        g.setFont(new Font("Bradley Hand ITC", Font.BOLD, 40));
        if (gameOver) {
            g.setFont(new Font("Bradley Hand ITC", Font.BOLD, 40));
            FontMetrics fm = g.getFontMetrics();

            String gameOverText = "Game Over!";
            String scoreText = "Score: " + (int) score;
            String restartText = "[Click to restart]";

            int centerX = boardWidth / 2;

            // Center "Game Over!"
            int gameOverX = centerX - fm.stringWidth(gameOverText) / 2;
            int gameOverY = boardHeight / 2 - 40;
            g.drawString(gameOverText, gameOverX, gameOverY);

            // Center "Score"
            int scoreX = centerX - fm.stringWidth(scoreText) / 2;
            int scoreY = gameOverY + 40;
            g.drawString(scoreText, scoreX, scoreY);

            // Center "[Click to restart]"
            int restartX = centerX - fm.stringWidth(restartText) / 2;
            int restartY = scoreY + 40;
            g.drawString(restartText, restartX, restartY);
        }

        else g.drawString("Score: " + String.valueOf((int) score), 10, 35);
    }

    public void move() {
        //bird
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);

        firstBGX += -2;

        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;
            //scoring
            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                pipe.passed = true;
                score += 0.5;
            }

            if (collision(bird, pipe)) gameOver = true;
        }

        for (int i = 0; i<bgs.size(); i++){
            BackGround bg = bgs.get(i);
            bg.x += -2;
        }

        if(bird.y > boardHeight) gameOver = true;

    }

    public boolean collision(Bird a, Pipe b){
        return a.x < b.x + b.width &&   //a's top left corner doesn't reach b's top right corner
                a.x + a.width > b.x &&   //a's top right corner passes b's top left corner
                a.y < b.y + b.height &&  //a's top left corner doesn't reach b's bottom left corner
                a.y + a.height > b.y;    //a's bottom left corner passes b's top left corner
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver){
            placePipesTimer.stop();
            gameLoop.stop();
            placeBGTimer.stop();
        }
    }


    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_UP)
            velocityY = -9;
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyReleased(KeyEvent e) {}
}

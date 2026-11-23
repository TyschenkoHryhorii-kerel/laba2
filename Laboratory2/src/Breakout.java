/*
- File: Breakout.java
- -------------------
- Name:
- Section Leader:
 *
- This file will eventually implement the game of Breakout.
 */

import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class Breakout extends GraphicsProgram {

    /** Width and height of application window in pixels */
    public static final int APPLICATION_WIDTH = 600;
    public static final int APPLICATION_HEIGHT = 600;

    /** Dimensions of game board (usually the same) */
    private static final int WIDTH = APPLICATION_WIDTH;
    private static final int HEIGHT = APPLICATION_HEIGHT;

    /** Dimensions of the paddle */
    private static final int PADDLE_WIDTH = 60;
    private static final int PADDLE_HEIGHT = 10;

    /** Offset of the paddle up from the bottom */
    private static final int PADDLE_Y_OFFSET = 30;

    /** Number of bricks per row */
    public static final int NBRICKS_PER_ROW = 10;

    /** Number of rows of bricks */
    public static final int NBRICK_ROWS = 5;

    /** Separation between bricks */
    public static final int BRICK_SEP = 4;

    /** Width of a brick */
    public static final int BRICK_WIDTH = (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

    /** Height of a brick */
    public static final int BRICK_HEIGHT = 8;

    /** Radius of the ball in pixels */
    private static final int BALL_RADIUS = 5;

    /** Offset of the top brick row from the top */
    public static final int BRICK_Y_OFFSET = 100;

    private static final double KEREL_PADDLE_GAP = 50;
    // Константи станів гри
    private static final int STATE_MENU = 1;
    private static final int STATE_PLAYING = 2;
    private static final int STATE_GAME_OVER = 3;

    private int gameState = STATE_MENU;
    private int currentLevel = 1;

    // Кнопки головного меню
    private GRect btnLevel1;
    private GRect btnLevel2;
    private GRect btnLevel3;
    private GRect btnExit;

    // Кнопки меню програшу
    private GRect btnRetry;
    private GRect btnLevelMenu;
    private GRect btnGameOverExit;

    private long lastClickTime = 0;
    private boolean mouseListenersAdded = false;
    private boolean rotationDone = false;

    /* Method: run() */
    /** Runs the Breakout program. */
    public GOval ball;
    public GRect paddle;
    public GRect leftWall;
    public GRect rightWall;
    public GRect upperWall;
    public GImage bomb;
    public Kerel kerel;
    private GImage heart1;
    private GImage heart2;
    private GImage heart3;
    public boolean gameInProcess = true;
    public int countOfLives;
    public int needPointsToWin;
    public int currentPoints = 0;
    public double xSpeedBall = 2;
    public double ySpeedBall = -2;
    public double kerelFactor = 10;
    public GRect kerelPaddle;
    private GLabel score = new GLabel(""+currentPoints+"/"+needPointsToWin);
    private static final double KEREL_PADDLE_SPEED = 1.5;
    private boolean rotated = false;
    public static void main(String[] args) {new Breakout().start(args);}
    //run
    public void run() {
        setSize(APPLICATION_WIDTH, APPLICATION_HEIGHT);
        initMouseListeners();
        showMainMenu();
        while (true) {
            if (gameState == STATE_PLAYING) {
                if (currentLevel == 1) {
                    level1();
                } else if (currentLevel == 2) {
                    level2();
                } else if (currentLevel == 3) {
                    level3();
                }
                if (gameState == STATE_PLAYING) {
                    showMainMenu();
                }
            }
            pause(50);
        }
    }
    //ініціалізація слухачів подій (тільки один раз)
    private void initMouseListeners() {
        if (!mouseListenersAdded) {
            addMouseListeners();
            mouseListenersAdded = true;
        }
    }
    //апдейтить рух всіх обʼєктів
    private void gameUpdate(){
        gameInProcess = true;
        while (gameInProcess){
            updateHearts();
            scoreBoard();
            ballMoving();
            updateKerelPaddle();
            updateBomb();
            gameTriggers();
            pause(kerelFactor);
        }
    }
    //перевірка чи не трапилось чогось, що має впливати на гру(виліт мʼячика за межі ігрового поля і tд)
    private void gameTriggers(){
        lifesCheck();
        winCheck();
        if (gameInProcess && !rotationDone && currentPoints >= needPointsToWin / 2) {
            rotationDone = true;
            rotateGame();
        }
    }
    //брейкає гру, якщо більше не залишилося життів
    private void lifesCheck() {
        double xBall = ball.getX();

        boolean lost = rotated
                ? xBall + BALL_RADIUS * 2 < 0
                : ball.getY() > APPLICATION_HEIGHT;

        if (lost) {
            countOfLives--;

            if (countOfLives <= 0) {
                gameInProcess = false;
                showGameOverMenu();
                return;
            }

            pause(100);

            if (!rotated) {
                ball.setLocation(
                        APPLICATION_WIDTH / 2 - BALL_RADIUS,
                        APPLICATION_HEIGHT - PADDLE_Y_OFFSET - BALL_RADIUS * 2 - 20
                );
                xSpeedBall = 2;
                ySpeedBall = -2;
            } else {
                ball.setLocation(
                        APPLICATION_WIDTH / 2 - BALL_RADIUS,
                        APPLICATION_HEIGHT / 2 - BALL_RADIUS
                );
                xSpeedBall = 2;
                ySpeedBall = 1;
            }
        }
    }
    //брейкає гру, якщо гравець переміг
    private void winCheck(){
        if(needPointsToWin <= currentPoints){
            gameInProcess = false;

            //тут встав віконце переможне
        }
    }
    //створює ракетку, якою гравець відбиває мʼяч
    private void paddleCreate(int paddleX, int paddleY, int offset){
        paddle = new GRect((APPLICATION_WIDTH/2)-30, APPLICATION_HEIGHT-offset, paddleX, paddleY);
        add(paddle);
    }
    //рухає ракетку в залежності від положення мишки гравця
    public void mouseMoved(MouseEvent e) {
        if (!gameInProcess || paddle == null) {
            return;
        }

        if (!rotated) {
            double x = Math.max(0, Math.min(
                    WIDTH - paddle.getWidth(),
                    e.getX() - paddle.getWidth() / 2
            ));

            paddle.setLocation(x, paddle.getY());
        } else {
            double y = Math.max(0, Math.min(
                    HEIGHT - paddle.getHeight(),
                    e.getY() - paddle.getHeight() / 2
            ));

            paddle.setLocation(paddle.getX(), y);
        }
    }
    //обробка кліків по кнопках меню
    public void mouseClicked(MouseEvent e) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < 250) {
            return;
        }
        lastClickTime = currentTime;

        int x = e.getX();
        int y = e.getY();
        if (gameState == STATE_MENU) {
            if (isClicked(btnLevel1, x, y)) {
                currentLevel = 1;
                gameState = STATE_PLAYING;
            } else if (isClicked(btnLevel2, x, y)) {
                currentLevel = 2;
                gameState = STATE_PLAYING;
            } else if (isClicked(btnLevel3, x, y)) {
                currentLevel = 3;
                gameState = STATE_PLAYING;
            } else if (isClicked(btnExit, x, y)) {
                System.exit(0);
            }
        } else if (gameState == STATE_GAME_OVER) {
            if (isClicked(btnRetry, x, y)) {
                gameState = STATE_PLAYING;
            } else if (isClicked(btnLevelMenu, x, y)) {
                showMainMenu();
            } else if (isClicked(btnGameOverExit, x, y)) {
                System.exit(0);
            }
        }
    }
    //створює стінки
    private void createWalls(){
        leftWall = new GRect(-10, 0, 10, APPLICATION_HEIGHT);
        rightWall = new GRect(APPLICATION_WIDTH+5, 0, 10, APPLICATION_HEIGHT);
        upperWall = new GRect(0, -30, APPLICATION_WIDTH, 35);
        leftWall.setColor(Color.black);
        leftWall.setFilled(true);
        rightWall.setColor(Color.black);
        rightWall.setFilled(true);
        upperWall.setColor(Color.black);
        upperWall.setFilled(true);
        add(upperWall);
        add(leftWall);
        add(rightWall);
    }
    //створює мʼяч
    private void ball(){
        ball = new GOval((APPLICATION_WIDTH/2)-BALL_RADIUS, APPLICATION_HEIGHT-PADDLE_Y_OFFSET-BALL_RADIUS*2-20, BALL_RADIUS*2, BALL_RADIUS*2);
        add(ball);
    }
    //логіка відбивання мʼяча, а також знищення бріків
    private void ballMoving() {
        double ballX = ball.getX();
        double ballY = ball.getY();
        double ballSize = BALL_RADIUS * 2;

        double nextX = ballX + xSpeedBall;
        double nextY = ballY + ySpeedBall;

        if (rotated && kerelPaddle != null && xSpeedBall > 0) {
            if (nextX + ballSize >= kerelPaddle.getX()
                    && nextX <= kerelPaddle.getX() + kerelPaddle.getWidth()
                    && nextY + ballSize >= kerelPaddle.getY()
                    && nextY <= kerelPaddle.getY() + kerelPaddle.getHeight()) {

                double paddleCenter = kerelPaddle.getY() + kerelPaddle.getHeight() / 2;
                double ballCenter = nextY + BALL_RADIUS;

                ySpeedBall = (ballCenter - paddleCenter) * 0.15;
                xSpeedBall = -Math.abs(xSpeedBall);

                nextX = kerelPaddle.getX() - ballSize;
            }
        }

        if (paddle != null && rotated && xSpeedBall < 0) {
            if (nextX <= paddle.getX() + paddle.getWidth()
                    && nextX + ballSize >= paddle.getX()
                    && nextY + ballSize >= paddle.getY()
                    && nextY <= paddle.getY() + paddle.getHeight()) {

                double paddleCenter = paddle.getY() + paddle.getHeight() / 2;
                double ballCenter = nextY + BALL_RADIUS;

                ySpeedBall = (ballCenter - paddleCenter) * 0.15;
                xSpeedBall = Math.abs(xSpeedBall);

                nextX = paddle.getX() + paddle.getWidth();
            }
        }

        GObject collider = getCollidingObject();

        if (collider != null && collider != paddle && collider != kerelPaddle) {
            if (collider instanceof Brick) {
                double ballCenterX = ballX + BALL_RADIUS;
                double ballCenterY = ballY + BALL_RADIUS;
                double brickCenterX = collider.getX() + collider.getWidth() / 2;
                double brickCenterY = collider.getY() + collider.getHeight() / 2;

                if (Math.abs(ballCenterX - brickCenterX) > Math.abs(ballCenterY - brickCenterY)) {
                    xSpeedBall *= -1;
                } else {
                    ySpeedBall *= -1;
                }

                remove(collider);
                currentPoints += 100;
            }
        }

        nextX = ballX + xSpeedBall;
        nextY = ballY + ySpeedBall;

        if (!rotated) {
            if (nextX <= 0) {
                nextX = 0;
                xSpeedBall = Math.abs(xSpeedBall);
            }

            if (nextX + ballSize >= WIDTH) {
                nextX = WIDTH - ballSize;
                xSpeedBall = -Math.abs(xSpeedBall);
            }

            if (nextY <= 0) {
                nextY = 0;
                ySpeedBall = Math.abs(ySpeedBall);
            }
        } else {
            if (nextY <= 0) {
                nextY = 0;
                ySpeedBall = Math.abs(ySpeedBall);
            }

            if (nextY + ballSize >= HEIGHT) {
                nextY = HEIGHT - ballSize;
                ySpeedBall = -Math.abs(ySpeedBall);
            }
        }

        ball.setLocation(nextX, nextY);
    }
    //повертає обʼєкт з яким зіткнувся мʼяч
    private GObject getCollidingObject() {
        double ballRadius = BALL_RADIUS;
        double ballCenterX = ball.getX() + ballRadius;
        double ballCenterY = ball.getY() + ballRadius;
        for (int angle = 0; angle < 360; angle += 45) {
            double rad = Math.toRadians(angle);
            double checkX = ballCenterX + ballRadius * Math.cos(rad);
            double checkY = ballCenterY + ballRadius * Math.sin(rad);
            GObject obj = getElementAt(checkX, checkY);
            if (obj != null && obj != ball && obj != kerel) {
                return obj;
            }
        }
        return null;
    }
    //виставляє стандартний рядок бріків
    private void lineOfBricks(int startY){
        int startX = 0;
        for(int i = 0; i<Breakout.NBRICKS_PER_ROW; i++){
            Brick brick = new Brick(startX, startY);
            add(brick);
            startX+=Breakout.BRICK_WIDTH + Breakout.BRICK_SEP;
        }
    }
    //допоміжний метод для створення простої кнопки
    private GRect createButton(String text, double x, double y, double width, double height) {
        GRect box = new GRect(x, y, width, height);
        box.setFilled(true);
        box.setFillColor(Color.LIGHT_GRAY);
        box.setColor(Color.BLACK);
        add(box);

        GLabel label = new GLabel(text);
        label.setFont("SansSerif-BOLD-14");
        double textX = x + (width - label.getWidth()) / 2;
        double textY = y + (height + label.getAscent()) / 2 - 2;
        add(label, textX, textY);

        return box;
    }

    //перевірка кліку по кнопці
    private boolean isClicked(GRect button, int x, int y) {
        if (button == null) {
            return false;
        }
        return x >= button.getX() && x <= button.getX() + button.getWidth() &&
                y >= button.getY() && y <= button.getY() + button.getHeight();
    }

    //віконце головного меню
    private void showMainMenu() {
        gameState = STATE_MENU;
        removeAll();

        GLabel title = new GLabel("BREAKOUT");
        title.setFont("SansSerif-BOLD-28");
        double titleX = (APPLICATION_WIDTH - title.getWidth()) / 2;
        add(title, titleX, 120);

        double btnWidth = 200;
        double btnHeight = 40;
        double btnX = (APPLICATION_WIDTH - btnWidth) / 2;

        btnLevel1 = createButton("Рівень 1", btnX, 180, btnWidth, btnHeight);
        btnLevel2 = createButton("Рівень 2", btnX, 240, btnWidth, btnHeight);
        btnLevel3 = createButton("Рівень 3", btnX, 300, btnWidth, btnHeight);
        btnExit = createButton("Вихід", btnX, 360, btnWidth, btnHeight);
    }

    //віконце при програші
    private void showGameOverMenu() {
        gameState = STATE_GAME_OVER;
        removeAll();

        GLabel title = new GLabel("ВИ ПРОГРАЛИ");
        title.setFont("SansSerif-BOLD-28");
        title.setColor(Color.RED);
        double titleX = (APPLICATION_WIDTH - title.getWidth()) / 2;
        add(title, titleX, 140);

        double btnWidth = 220;
        double btnHeight = 42;
        double btnX = (APPLICATION_WIDTH - btnWidth) / 2;

        btnRetry = createButton("Спробувати ще раз", btnX, 200, btnWidth, btnHeight);
        btnLevelMenu = createButton("Меню рівнів", btnX, 260, btnWidth, btnHeight);
        btnGameOverExit = createButton("Вихід з гри", btnX, 320, btnWidth, btnHeight);
    }
    private void createAndDropBomb() {
        if (bomb == null) {
            bomb = new GImage("beeper.jpeg");
            bomb.setSize(30, 30);
            bomb.setLocation(kerel.getX(), kerel.getY());
            add(bomb);
        }
    }
    private void scoreBoard(){
        remove(score);
        score.setLabel(currentPoints+"/"+needPointsToWin);
        score.setLocation(APPLICATION_WIDTH-80, 30);
        score.setFont("Roboto-15");
        add(score);
    }
    private void updateBomb() {
        if (bomb != null) {
            bomb.move(0, 5);
            GObject collidingObject = getElementAt(bomb.getX() + bomb.getWidth() / 2, bomb.getY() + bomb.getHeight());
            if (collidingObject != null && collidingObject != bomb) {
                remove(bomb);
                bomb = null;
            } else if (bomb.getY() > APPLICATION_HEIGHT) {
                remove(bomb);
                bomb = null;
            }
        }
    }
    private void createHearts() {
        heart1 = new GImage("heart.png");
        heart1.setSize(25, 25);
        heart1.setLocation(10, 10);
        add(heart1);

        heart2 = new GImage("heart.png");
        heart2.setSize(25, 25);
        heart2.setLocation(40, 10);
        add(heart2);

        heart3 = new GImage("heart.png");
        heart3.setSize(25, 25);
        heart3.setLocation(70, 10);
        add(heart3);
    }

    private void updateHearts(){
        if (heart3 != null) {
            heart3.setVisible(countOfLives >= 3);
        }
        if (heart2 != null) {
            heart2.setVisible(countOfLives >= 2);
        }
        if (heart1 != null) {
            heart1.setVisible(countOfLives >= 1);
        }
    }
    private void attackHearts() {
        kerel.moveToX(85);
        kerel.turnLeft();
        kerel.prepareToAttack();

        double centerX = (heart2.getX() + heart3.getX() + heart3.getWidth()) / 2;
        double centerY = heart2.getY() + heart2.getHeight() / 2;

        GImage hand = new GImage("kerelHand.jpeg");
        hand.setSize(4, 4);
        hand.setLocation(centerX - 2, centerY - 2);
        add(hand);

        for (int size = 4; size <= 50; size += 6) {
            hand.setSize(size, size);
            hand.setLocation(centerX - size / 2.0, centerY - size / 2.0);
            pause(20);
        }

        for (int i = 0; i < 4; i++) {
            hand.move(-3, 1);
            pause(30);
        }

        for (int i = 0; i < 3; i++) {
            hand.move(8, -1);
            pause(15);
        }

        double handCenterX = hand.getX() + hand.getWidth() / 2;
        double handCenterY = hand.getY() + hand.getHeight() / 2;

        for (int i = 0; i < 24; i++) {
            heart2.move(6, -1);
            heart3.move(6, -1);
            if (i < 8) {
                int size = 50 - i * 6;
                hand.setSize(size, size);
                hand.setLocation(handCenterX - size / 2.0, handCenterY - size / 2.0);
            }
            pause(20);
        }

        remove(hand);
        remove(heart2);
        remove(heart3);
        countOfLives -= 2;
    }
    private void createKerelPaddle() {
        kerel.resetPose();
        double center = Math.max(PADDLE_WIDTH / 2.0, Math.min(WIDTH - PADDLE_WIDTH / 2.0, kerel.centerX()));
        kerel.moveBy(center - kerel.centerX());
        double paddleX = kerel.centerX() - PADDLE_WIDTH / 2.0;
        double paddleY = kerel.bottomY() + KEREL_PADDLE_GAP;
        kerelPaddle = new GRect(paddleX, paddleY, PADDLE_WIDTH, PADDLE_HEIGHT);
        kerelPaddle.setFilled(true);
        add(kerelPaddle);
    }
    private void updateKerelPaddle() {
        if (kerelPaddle == null || kerel == null) {
            return;
        }

        if (!rotated) {
            double target = ball.getX() + BALL_RADIUS;

            target = Math.max(
                    kerel.getWidth() / 2,
                    Math.min(WIDTH - kerel.getWidth() / 2, target)
            );

            double dx = target - kerel.centerX();

            dx = Math.max(
                    -kerel.getX(),
                    Math.min(WIDTH - kerel.getX() - kerel.getWidth(), dx)
            );

            kerel.move(dx, 0);
            kerelPaddle.move(dx, 0);

        } else {
            double targetY = ball.getY() + BALL_RADIUS
                    - kerelPaddle.getHeight() / 2;

            targetY = Math.max(
                    0,
                    Math.min(HEIGHT - kerelPaddle.getHeight(), targetY)
            );

            double targetX = WIDTH - kerelPaddle.getWidth() - 10;

            double dx = targetX - kerelPaddle.getX();
            double dy = targetY - kerelPaddle.getY();

            kerel.move(dx, dy);
            kerelPaddle.move(dx, dy);
        }
    }
    private void kerelReturnBall() {
        if (kerelPaddle == null) {
            return;
        }
        double paddleBottom = kerelPaddle.getY() + PADDLE_HEIGHT;
        if (ball.getY() <= paddleBottom) {
            ySpeedBall = Math.abs(ySpeedBall);
            ball.setLocation(ball.getX(), paddleBottom);
        }
    }
    private void rotateGame() {
        final double cx = WIDTH / 2.0;
        final double cy = HEIGHT / 2.0;

        int count = getElementCount();

        for (int i = 0; i < count; i++) {
            GObject obj = getElement(i);

            double oldX = obj.getX();
            double oldY = obj.getY();
            double oldW = obj.getWidth();
            double oldH = obj.getHeight();

            double centerX = oldX + oldW / 2;
            double centerY = oldY + oldH / 2;

            double newX = cx - (centerY - cy);
            double newY = cy + (centerX - cx);

            if (obj instanceof GScalable && oldW > 0 && oldH > 0) {
                ((GScalable) obj).scale(oldH / oldW, oldW / oldH);
            }

            obj.setLocation(
                    newX - obj.getWidth() / 2,
                    newY - obj.getHeight() / 2
            );
        }

        double oldXSpeed = xSpeedBall;
        xSpeedBall = -ySpeedBall;
        ySpeedBall = oldXSpeed;

        rotated = !rotated;
        kerel.move(-250, 20);
        kerelPaddle.move(-50, 0);
        pause(300);
    }
    //створює перший рівень
    private void level1(){
        rotated = false;
        score = new GLabel(""+currentPoints+"/"+needPointsToWin);
        add(score);
        countOfLives = 3;
        needPointsToWin = 5000;
        currentPoints = 0;
        rotationDone = false;
        xSpeedBall = 2;
        ySpeedBall = -2;
        removeAll();
        paddleCreate(PADDLE_WIDTH, PADDLE_HEIGHT, PADDLE_Y_OFFSET);
        createWalls();
        ball();
        createHearts();
        int startY = Breakout.BRICK_Y_OFFSET;
        for(int i = 0; i<NBRICK_ROWS; i++){
            lineOfBricks(startY);
            startY+=(BRICK_HEIGHT+BRICK_SEP);
        }
        initMouseListeners();
        gameUpdate();
    }
    //створює другий рівень
    private void level2(){
    }
    //створює третій рівень
    private void level3(){
        rotated = false;
        kerelFactor = 6;
        countOfLives = 3;
        needPointsToWin = 5000;
        currentPoints = 0;
        rotationDone = false;
        xSpeedBall = 2;
        ySpeedBall = -2;
        removeAll();
        paddleCreate(PADDLE_WIDTH, PADDLE_HEIGHT, PADDLE_Y_OFFSET);
        createWalls();
        ball();
        createHearts();
        Sounds.playKerelMusic();
        kerel = new Kerel();
        add(kerel);
        for(int i = 0; i<10; i++) {
            kerel.randomMoving();
        }
        attackHearts();
        createKerelPaddle();
        createAndDropBomb();
        rotateGame();
        initMouseListeners();
        gameUpdate();
    }
}
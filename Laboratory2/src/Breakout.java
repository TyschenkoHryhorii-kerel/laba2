
/*
 * File: Breakout.java
 * -------------------
 * Name:
 * Section Leader:
 *
 * Реалізація гри Breakout (3 рівні, бос Керел на 3-му рівні).
 */

import acm.graphics.*;
import acm.program.*;
import acm.util.*;

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

    // Параметри відбиття від ракеток
    private static final double PADDLE_BOUNCE_FACTOR = 0.15;
    private static final double MIN_BALL_X_SPEED = 0.5;
    private static final double MAX_BALL_X_SPEED = 4;

    private static final int POINTS_PER_BRICK = 100;
    private static final int SURVIVE_SECONDS = 100;

    // Константи станів гри
    private static final int STATE_MENU = 1;
    private static final int STATE_PLAYING = 2;
    private static final int STATE_GAME_OVER = 3;
    private static final int STATE_WIN = 4;

    // volatile, бо стан змінюється і з потоку гри, і з потоку подій миші
    private volatile int gameState = STATE_MENU;
    private int currentLevel = 1;

    // Кнопки головного меню
    private GRect btnLevel1;
    private GRect btnLevel2;
    private GRect btnLevel3;
    private GRect btnExit;

    // Кнопки меню програшу (використовуються і в меню перемоги)
    private GRect btnRetry;
    private GRect btnLevelMenu;
    private GRect btnGameOverExit;

    private long lastClickTime = 0;
    private boolean mouseListenersAdded = false;

    public GOval ball;
    public GRect paddle;
    public GRect leftWall;
    public GRect rightWall;
    public GRect upperWall;

    private GImage verticalBomb1;
    private GImage horizontalBomb1;
    private GImage verticalBomb2;
    private GImage horizontalBomb2;
    private GImage verticalBomb3;
    private GImage horizontalBomb3;
    private GImage verticalBomb4;
    private GImage horizontalBomb4;
    private GImage verticalBomb5;
    private GImage horizontalBomb5;
    private GImage verticalBomb6;
    private GImage horizontalBomb6;
    private GImage verticalBomb7;
    private GImage horizontalBomb7;
    private GImage verticalBomb8;
    private GImage horizontalBomb8;
    private GImage verticalBomb9;
    private GImage horizontalBomb9;

    private int lastBombAttackSecond = 0;

    public Kerel kerel;
    private GImage heart1;
    private GImage heart2;
    private GImage heart3;
    public volatile boolean gameInProcess = false;
    public int countOfLives;
    public int needPointsToWin;
    public int currentPoints = 0;
    public double xSpeedBall = 2;
    public double ySpeedBall = -2;
    public double kerelFactor = 10; // пауза між кадрами (мс), чим менше - тим швидша гра
    public GRect kerelPaddle;
    private GLabel score = new GLabel("");
    private volatile boolean rotated = false;
    private boolean rotationDone = false;
    private boolean rotationEnabled = false; // поворот екрана - тільки на рівні з Керелом
    private boolean timedLevel = false;      // рівень, де бали - це секунди виживання
    private boolean playerWon = false;
    private long lastTickTime = 0;

    public static void main(String[] args) {
        new Breakout().start(args);
    }

    /* Method: run() */
    /** Runs the Breakout program. */
    public void run() {
        setSize(APPLICATION_WIDTH, APPLICATION_HEIGHT);
        initMouseListeners();
        showMainMenu();

        while (true) {
            if (gameState == STATE_PLAYING) {
                switch (currentLevel) {
                    case 1:
                        level1();
                        break;
                    case 2:
                        level2();
                        break;
                    case 3:
                        level3();
                        break;
                    default:
                        break;
                }

                // якщо рівень з якоїсь причини завершився без меню - повертаємось у головне
                if (gameState == STATE_PLAYING) {
                    showMainMenu();
                }
            }
            pause(50);
        }
    }

    // ініціалізація слухачів подій (тільки один раз)
    private void initMouseListeners() {
        if (!mouseListenersAdded) {
            addMouseListeners();
            mouseListenersAdded = true;
        }
    }

    // апдейтить рух всіх обʼєктів, поки триває гра; після виходу з циклу показує меню
    private void gameUpdate() {
        gameInProcess = true;
        playerWon = false;
        lastTickTime = System.currentTimeMillis();

        while (gameInProcess) {
            updateHearts();
            scoreBoard();
            updateKerelPaddle();
            ballMoving();
            updateBomb();

            if (timedLevel) {
                updateSurvivalTimer();
                createBombAttack();

                if (currentPoints == 30) {
                    kerelFactor = 4;
                    GLabel nextPhase = new GLabel("Lets Move Faster");
                    nextPhase.setFont("Arial-30");
                    nextPhase.setLocation(APPLICATION_WIDTH / 2 - 100, APPLICATION_HEIGHT / 2);
                    add(nextPhase);
                    pause(500);
                    remove(nextPhase);
                }
                else if (currentPoints == 60) {
                    kerelFactor = 3;
                    GLabel nextPhase = new GLabel("Lets Move Faster");
                    nextPhase.setFont("Arial-30");
                    nextPhase.setLocation(APPLICATION_WIDTH / 2 - 100, APPLICATION_HEIGHT / 2);
                    add(nextPhase);
                    pause(500);
                    remove(nextPhase);
                }
                else if (currentPoints == 90) {
                    kerelFactor = 2;
                    GLabel nextPhase = new GLabel("Lets Move Faster");
                    nextPhase.setFont("Arial-30");
                    nextPhase.setLocation(APPLICATION_WIDTH / 2 - 100, APPLICATION_HEIGHT / 2);
                    add(nextPhase);
                    pause(500);
                    remove(nextPhase);
                }
            }
            gameTriggers();
            pause(kerelFactor);
        }
        if (playerWon) {
            showWinMenu();
        } else {
            showGameOverMenu();
        }
    }

    // на рівні з Керелом 1 очко = 1 секунда виживання
    private void updateSurvivalTimer() {
        long now = System.currentTimeMillis();
        while (now - lastTickTime >= 1000) {
            currentPoints++;
            lastTickTime += 1000;
        }
    }

    // створює вертикальну бомбу
    private GImage createVerticalBomb() {
        GImage newBomb = new GImage("beeper.jpeg");
        newBomb.setSize(30, 30);
        newBomb.setLocation(kerel.getX() + kerel.getWidth() / 2 - 15, kerel.getY() + kerel.getHeight());
        add(newBomb);
        return newBomb;
    }

    // створює горизонтальну бомбу
    private GImage createHorizontalBomb() {
        GImage newBomb = new GImage("beeper.jpeg");
        newBomb.setSize(30, 30);
        newBomb.setLocation(WIDTH - 30, paddle.getY() + paddle.getHeight() / 2 - 15
        );
        add(newBomb);
        return newBomb;
    }

    // створює дві бомби кожні 5 секунд до 45 секунди включно
    private void createBombAttack() {
        while (currentPoints >= lastBombAttackSecond + 5) {
            lastBombAttackSecond += 5;
            if (lastBombAttackSecond == 5) {
                verticalBomb1 = createVerticalBomb();
                horizontalBomb1 = createVerticalBomb();
            }
            else if (lastBombAttackSecond == 10) {
                verticalBomb2 = createVerticalBomb();
                horizontalBomb2 = createVerticalBomb();
            }
            else if (lastBombAttackSecond == 15) {
                verticalBomb3 = createVerticalBomb();
                horizontalBomb3 = createVerticalBomb();
            }
            else if (lastBombAttackSecond == 20) {
                verticalBomb4 = createVerticalBomb();
                horizontalBomb4 = createVerticalBomb();
            }
            else if (lastBombAttackSecond == 25) {
                verticalBomb5 = createVerticalBomb();
                horizontalBomb5 = createVerticalBomb();
            }
            else if (lastBombAttackSecond == 30) {
                verticalBomb6 = createVerticalBomb();
                horizontalBomb6 = createVerticalBomb();
            }
            else if (lastBombAttackSecond == 35) {
                verticalBomb7 = createVerticalBomb();
                horizontalBomb7 = createVerticalBomb();
            }
            else if (lastBombAttackSecond == 40) {
                verticalBomb8 = createVerticalBomb();
                horizontalBomb8 = createVerticalBomb();
            }
            else if (lastBombAttackSecond == 55) {
                verticalBomb9 = createVerticalBomb();
                horizontalBomb9 = createVerticalBomb();
            }
            else if (lastBombAttackSecond == 60) {
                verticalBomb2 = createVerticalBomb();
                horizontalBomb2 = createVerticalBomb();
            }
            else if (lastBombAttackSecond == 65) {
                verticalBomb3 = createVerticalBomb();
                horizontalBomb3 = createVerticalBomb();
            }
            else if (lastBombAttackSecond == 70) {
                verticalBomb4 = createVerticalBomb();
                horizontalBomb4 = createVerticalBomb();
            }
            else if (lastBombAttackSecond == 75) {
                verticalBomb5 = createVerticalBomb();
                horizontalBomb5 = createVerticalBomb();
            }
            else if (lastBombAttackSecond == 80) {
                verticalBomb6 = createVerticalBomb();
                horizontalBomb6 = createVerticalBomb();
            }
            else if (lastBombAttackSecond == 85) {
                verticalBomb7 = createVerticalBomb();
                horizontalBomb7 = createVerticalBomb();
            }
            else if (lastBombAttackSecond == 90) {
                verticalBomb8 = createVerticalBomb();
                horizontalBomb8 = createVerticalBomb();
            }
            else if (lastBombAttackSecond == 95) {
                verticalBomb9 = createVerticalBomb();
                horizontalBomb9 = createVerticalBomb();
            }
        }
    }

    // оновлює одну бомбу
    private GImage updateOneBomb(GImage currentBomb, double dx, double dy) {
        if (currentBomb == null) {
            return null;
        }

        if (rotated) {
            double oldDx = dx;
            dx = -dy;
            dy = oldDx;
        }

        currentBomb.move(dx, dy);

        if (paddle != null && overlaps(currentBomb, paddle)) {
            remove(currentBomb);
            gameInProcess = false;
            playerWon = false;
            return null;
        }

        if (currentBomb.getX() + currentBomb.getWidth() < 0
                || currentBomb.getX() > APPLICATION_WIDTH
                || currentBomb.getY() + currentBomb.getHeight() < 0
                || currentBomb.getY() > APPLICATION_HEIGHT) {
            remove(currentBomb);
            return null;
        }

        return currentBomb;
    }

    // оновлює рух усіх бомб
    private void updateBomb() {
        verticalBomb1 = updateOneBomb(verticalBomb1, 0, 5);
        horizontalBomb1 = updateOneBomb(horizontalBomb1, -5, 0);

        verticalBomb2 = updateOneBomb(verticalBomb2, 0, 2);
        horizontalBomb2 = updateOneBomb(horizontalBomb2, -2, 0);

        verticalBomb3 = updateOneBomb(verticalBomb3, 0, 2);
        horizontalBomb3 = updateOneBomb(horizontalBomb3, -2, 0);

        verticalBomb4 = updateOneBomb(verticalBomb4, 0, 5);
        horizontalBomb4 = updateOneBomb(horizontalBomb4, -5, 0);

        verticalBomb5 = updateOneBomb(verticalBomb5, 0, 5);
        horizontalBomb5 = updateOneBomb(horizontalBomb5, -5, 0);

        verticalBomb6 = updateOneBomb(verticalBomb6, 0, 5);
        horizontalBomb6 = updateOneBomb(horizontalBomb6, -5, 0);

        verticalBomb7 = updateOneBomb(verticalBomb7, 0, 5);
        horizontalBomb7 = updateOneBomb(horizontalBomb7, -5, 0);

        verticalBomb8 = updateOneBomb(verticalBomb8, 0, 5);
        horizontalBomb8 = updateOneBomb(horizontalBomb8, -5, 0);

        verticalBomb9 = updateOneBomb(verticalBomb9, 0, 5);
        horizontalBomb9 = updateOneBomb(horizontalBomb9, -5, 0);
    }

    // перевірка чи не трапилось чогось, що має впливати на гру
    private void gameTriggers() {
        lifesCheck();

        if (!gameInProcess) {
            return;
        }

        winCheck();

        if (!gameInProcess) {
            return;
        }

        if (rotationEnabled && !rotationDone && currentPoints >= needPointsToWin / 2) {
            rotationDone = true;
            rotateGame();
        }
    }

    // віднімає життя, якщо мʼяч вилетів; зупиняє гру, якщо життів не залишилось
    private void lifesCheck() {
        double xBall = ball.getX();

        boolean lost = rotated
                ? xBall + BALL_RADIUS * 2 < 0
                : ball.getY() > APPLICATION_HEIGHT;

        if (lost) {
            countOfLives--;

            if (countOfLives <= 0) {
                playerWon = false;
                gameInProcess = false;
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

    // зупиняє гру, якщо гравець переміг
    private void winCheck() {
        if (currentPoints >= needPointsToWin) {
            playerWon = true;
            gameInProcess = false;
        }
    }

    // рухає ракетку в залежності від положення мишки гравця
    public void mouseMoved(MouseEvent e) {
        if (!gameInProcess || paddle == null) {
            return;
        }

        if (!rotated) {
            double x = Math.max(0, Math.min(WIDTH - paddle.getWidth(), e.getX() - paddle.getWidth() / 2));
            paddle.setLocation(x, paddle.getY());
        } else {
            double y = Math.max(0, Math.min(HEIGHT - paddle.getHeight(), e.getY() - paddle.getHeight() / 2));
            paddle.setLocation(paddle.getX(), y);
        }
    }

    // обробка натискань по кнопках меню
    public void mousePressed(MouseEvent e) {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastClickTime < 250) {
            return;
        }

        lastClickTime = currentTime;
        int x = e.getX();
        int y = e.getY();
        int state = gameState;

        if (state == STATE_MENU) {
            if (isClicked(btnLevel1, x, y)) {
                currentLevel = 1;
                gameState = STATE_PLAYING;
            }
            else if (isClicked(btnLevel2, x, y)) {
                currentLevel = 2;
                gameState = STATE_PLAYING;
            }
            else if (isClicked(btnLevel3, x, y)) {
                currentLevel = 3;
                gameState = STATE_PLAYING;
            }
            else if (isClicked(btnExit, x, y)) {
                System.exit(0);
            }
        }
        else if (state == STATE_GAME_OVER) {
            if (isClicked(btnRetry, x, y)) {
                gameState = STATE_PLAYING;
            }
            else if (isClicked(btnLevelMenu, x, y)) {
                showMainMenu();
            }
            else if (isClicked(btnGameOverExit, x, y)) {
                System.exit(0);
            }
        } else if (state == STATE_WIN) {
            if (isClicked(btnLevelMenu, x, y)) {
                showMainMenu();
            }
            else if (isClicked(btnGameOverExit, x, y)) {
                System.exit(0);
            }
        }
    }

    // створює ракетку, якою гравець відбиває мʼяч
    private void paddleCreate(int paddleX, int paddleY, int offset) {
        paddle = new GRect((APPLICATION_WIDTH / 2) - 30, APPLICATION_HEIGHT - offset, paddleX, paddleY);
        paddle.setFilled(true);
        add(paddle);
    }

    // створює стінки
    private void createWalls() {
        leftWall = new GRect(-10, 0, 10, APPLICATION_HEIGHT);
        rightWall = new GRect(APPLICATION_WIDTH + 5, 0, 10, APPLICATION_HEIGHT);
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

    // створює мʼяч
    private void ball() {
        ball = new GOval(
                (APPLICATION_WIDTH / 2) - BALL_RADIUS,
                APPLICATION_HEIGHT - PADDLE_Y_OFFSET - BALL_RADIUS * 2 - 20,
                BALL_RADIUS * 2,
                BALL_RADIUS * 2
        );
        ball.setFilled(true);
        add(ball);
    }

    // виставляє стандартний рядок бріків (по центру екрана)
    private void lineOfBricks(int startY) {
        int rowWidth = NBRICKS_PER_ROW * BRICK_WIDTH + (NBRICKS_PER_ROW - 1) * BRICK_SEP;
        int startX = (WIDTH - rowWidth) / 2;

        for (int i = 0; i < NBRICKS_PER_ROW; i++) {
            Brick brick = new Brick(startX, startY);
            add(brick);
            startX += BRICK_WIDTH + BRICK_SEP;
        }
    }

    // виставляє задану кількість рядків цеглин і рахує, скільки балів треба для перемоги
    private void buildBricks(int rows) {
        int startY = BRICK_Y_OFFSET;

        for (int i = 0; i < rows; i++) {
            lineOfBricks(startY);
            startY += (BRICK_HEIGHT + BRICK_SEP);
        }

        needPointsToWin = rows * NBRICKS_PER_ROW * POINTS_PER_BRICK;
    }

    private void createScore() {
        score = new GLabel(currentPoints + "/" + needPointsToWin);
        score.setFont("Roboto-15");
        add(score, APPLICATION_WIDTH - 80, 30);
    }

    private void scoreBoard() {
        score.setLabel(currentPoints + "/" + needPointsToWin);
        score.setLocation(APPLICATION_WIDTH - 80, 30);
    }

    private void createHearts() {
        heart1 = new GImage("heart.png");
        heart1.setSize(50, 50);
        heart1.setLocation(10, 10);
        add(heart1);

        heart2 = new GImage("heart.png");
        heart2.setSize(50, 50);
        heart2.setLocation(60, 10);
        add(heart2);

        heart3 = new GImage("heart.png");
        heart3.setSize(50, 50);
        heart3.setLocation(120, 10);
        add(heart3);
    }

    private void updateHearts() {
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

    // допоміжне: чи перетинається квадрат (x, y, size) з обʼєктом
    private boolean overlapsRect(double x, double y, double size, GObject o) {
        return x < o.getX() + o.getWidth()
                && x + size > o.getX()
                && y < o.getY() + o.getHeight()
                && y + size > o.getY();
    }

    // визначає чи торкнувся мʼяч якоїсь цеглини
    private boolean overlaps(GObject a, GObject b) {
        return a.getX() < b.getX() + b.getWidth()
                && a.getX() + a.getWidth() > b.getX()
                && a.getY() < b.getY() + b.getHeight()
                && a.getY() + a.getHeight() > b.getY();
    }

    // повертає першу цеглину, з якою перетнеться мʼяч у новій позиції
    private GObject findHitBrick(double x, double y, double size) {
        double startX = ball.getX();
        double startY = ball.getY();
        double distanceX = x - startX;
        double distanceY = y - startY;

        int steps = (int) Math.ceil(Math.max(Math.abs(distanceX), Math.abs(distanceY)));
        steps = Math.max(1, steps);

        for (int i = 1; i <= steps; i++) {
            double progress = (double) i / steps;
            double checkX = startX + distanceX * progress;
            double checkY = startY + distanceY * progress;

            for (int j = 0; j < getElementCount(); j++) {
                GObject object = getElement(j);

                if (object instanceof Brick && overlapsRect(checkX, checkY, size, object)) {
                    return object;
                }
            }
        }

        return null;
    }

    // відбиває мʼяч від цеглини по тій осі, де проникнення менше
    private void bounceOffBrick(GObject brick, double nextX, double nextY, double size) {
        double oldX = ball.getX();
        double oldY = ball.getY();

        if (oldY + size <= brick.getY() && nextY + size >= brick.getY()) {
            ySpeedBall = -Math.abs(ySpeedBall);
        } else if (oldY >= brick.getY() + brick.getHeight() && nextY <= brick.getY() + brick.getHeight()) {
            ySpeedBall = Math.abs(ySpeedBall);
        } else if (oldX + size <= brick.getX() && nextX + size >= brick.getX()) {
            xSpeedBall = -Math.abs(xSpeedBall);
        } else if (oldX >= brick.getX() + brick.getWidth() && nextX <= brick.getX() + brick.getWidth()) {
            xSpeedBall = Math.abs(xSpeedBall);
        } else {
            double overlapX = Math.min(nextX + size, brick.getX() + brick.getWidth()) - Math.max(nextX, brick.getX());
            double overlapY = Math.min(nextY + size, brick.getY() + brick.getHeight()) - Math.max(nextY, brick.getY());

            if (overlapX < overlapY) {
                xSpeedBall = oldX + size / 2 < brick.getX() + brick.getWidth() / 2
                        ? -Math.abs(xSpeedBall)
                        : Math.abs(xSpeedBall);
            } else {
                ySpeedBall = oldY + size / 2 < brick.getY() + brick.getHeight() / 2
                        ? -Math.abs(ySpeedBall)
                        : Math.abs(ySpeedBall);
            }
        }
    }

    // горизонтальна швидкість після відбиття від ракетки (залежить від місця влучання)
    private double bounceAngle(double ballCenter, double paddleCenter) {
        double v = (ballCenter - paddleCenter) * PADDLE_BOUNCE_FACTOR;
        v = Math.max(-MAX_BALL_X_SPEED, Math.min(MAX_BALL_X_SPEED, v));

        if (Math.abs(v) < MIN_BALL_X_SPEED) {
            double dir = v != 0 ? Math.signum(v) : (xSpeedBall < 0 ? -1 : 1);
            v = dir * MIN_BALL_X_SPEED; // щоб мʼяч не літав суто вертикально
        }
        return v;
    }

    // логіка руху мʼяча: цеглини, ракетки, стіни
    private void ballMoving() {
        double ballX = ball.getX();
        double ballY = ball.getY();
        double ballSize = BALL_RADIUS * 2;
        double nextX = ballX + xSpeedBall;
        double nextY = ballY + ySpeedBall;

        GObject brick = findHitBrick(nextX, nextY, ballSize);

        if (brick != null) {
            bounceOffBrick(brick, nextX, nextY, ballSize);
            remove(brick);
            currentPoints += POINTS_PER_BRICK;
        }

        nextX = ballX + xSpeedBall;
        nextY = ballY + ySpeedBall;

        // ракетки
        if (!rotated) {
            // ракетка гравця (знизу)
            if (paddle != null && ySpeedBall > 0
                    && nextY + ballSize >= paddle.getY()
                    && ballY + ballSize <= paddle.getY() + paddle.getHeight()
                    && nextX + ballSize > paddle.getX()
                    && nextX < paddle.getX() + paddle.getWidth()) {

                xSpeedBall = bounceAngle(nextX + BALL_RADIUS, paddle.getX() + paddle.getWidth() / 2);
                ySpeedBall = -Math.abs(ySpeedBall);
                nextY = paddle.getY() - ballSize;
            }

            // ракетка Керела (зверху)
            if (kerelPaddle != null && ySpeedBall < 0
                    && nextY <= kerelPaddle.getY() + kerelPaddle.getHeight()
                    && ballY >= kerelPaddle.getY() + kerelPaddle.getHeight()
                    && nextX + ballSize > kerelPaddle.getX()
                    && nextX < kerelPaddle.getX() + kerelPaddle.getWidth()) {

                double error = RandomGenerator.getInstance().nextDouble(-7.0, 7.0);
                xSpeedBall = bounceAngle(
                        nextX + BALL_RADIUS + error,
                        kerelPaddle.getX() + kerelPaddle.getWidth() / 2
                );
                ySpeedBall = Math.abs(ySpeedBall);
                nextY = kerelPaddle.getY() + kerelPaddle.getHeight();
            }
        }
        else {
            // ракетка Керела (праворуч): відбивання тільки при контакті з нею
            if (kerelPaddle != null && xSpeedBall > 0
                    && nextX + ballSize >= kerelPaddle.getX()
                    && ballX + ballSize <= kerelPaddle.getX() + kerelPaddle.getWidth()
                    && nextY + ballSize > kerelPaddle.getY()
                    && nextY < kerelPaddle.getY() + kerelPaddle.getHeight()) {

                double paddleCenter = kerelPaddle.getY() + kerelPaddle.getHeight() / 2;
                double randomError = RandomGenerator.getInstance().nextDouble(-7.0, 7.0);
                double impactOffset = nextY + BALL_RADIUS - paddleCenter + randomError;

                ySpeedBall = impactOffset * PADDLE_BOUNCE_FACTOR;
                ySpeedBall = Math.max(-MAX_BALL_X_SPEED, Math.min(MAX_BALL_X_SPEED, ySpeedBall));

                if (Math.abs(ySpeedBall) < MIN_BALL_X_SPEED) {
                    double direction = RandomGenerator.getInstance().nextBoolean() ? 1 : -1;
                    ySpeedBall = direction * MIN_BALL_X_SPEED;
                }

                xSpeedBall = -Math.max(MIN_BALL_X_SPEED, Math.abs(xSpeedBall));
                nextX = kerelPaddle.getX() - ballSize;
            }

            // ракетка гравця (ліворуч)
            if (paddle != null && xSpeedBall < 0
                    && nextX <= paddle.getX() + paddle.getWidth()
                    && ballX >= paddle.getX() + paddle.getWidth()
                    && nextY + ballSize > paddle.getY()
                    && nextY < paddle.getY() + paddle.getHeight()) {

                double paddleCenter = paddle.getY() + paddle.getHeight() / 2;
                double impactOffset = nextY + BALL_RADIUS - paddleCenter;

                ySpeedBall = impactOffset * PADDLE_BOUNCE_FACTOR;
                ySpeedBall = Math.max(-MAX_BALL_X_SPEED, Math.min(MAX_BALL_X_SPEED, ySpeedBall));

                if (Math.abs(ySpeedBall) < MIN_BALL_X_SPEED) {
                    if (ySpeedBall < 0) {
                        ySpeedBall = -MIN_BALL_X_SPEED;
                    } else {
                        ySpeedBall = MIN_BALL_X_SPEED;
                    }
                }

                xSpeedBall = Math.max(MIN_BALL_X_SPEED, Math.abs(xSpeedBall));
                nextX = paddle.getX() + paddle.getWidth();
            }
        }

        // стіни
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

    // допоміжний метод для створення простої кнопки
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
    // перевірка кліку по кнопці
    private boolean isClicked(GRect button, int x, int y) {
        if (button == null) {
            return false;
        }

        return x >= button.getX() && x <= button.getX() + button.getWidth()
                && y >= button.getY() && y <= button.getY() + button.getHeight();
    }
    // віконце головного меню
    private void showMainMenu() {
        gameInProcess = false;
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

    // віконце при програші
    private void showGameOverMenu() {
        Sounds.stopAll();
        gameInProcess = false;
        removeAll();
        gameState = STATE_GAME_OVER;

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

    // віконце при перемозі
    private void showWinMenu() {
        gameInProcess = false;
        removeAll();
        gameState = STATE_WIN;

        GLabel title = new GLabel("ВИ ПЕРЕМОГЛИ!");
        title.setFont("SansSerif-BOLD-28");
        title.setColor(new Color(0, 140, 0));

        double titleX = (APPLICATION_WIDTH - title.getWidth()) / 2;
        add(title, titleX, 140);

        double btnWidth = 220;
        double btnHeight = 42;
        double btnX = (APPLICATION_WIDTH - btnWidth) / 2;

        btnLevelMenu = createButton("Меню рівнів", btnX, 200, btnWidth, btnHeight);
        btnGameOverExit = createButton("Вихід з гри", btnX, 260, btnWidth, btnHeight);
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

        double center = Math.max(
                PADDLE_WIDTH / 2.0,
                Math.min(WIDTH - PADDLE_WIDTH / 2.0, kerel.centerX())
        );

        kerel.moveBy(center - kerel.centerX());

        double paddleX = kerel.centerX() - PADDLE_WIDTH / 2.0;
        double paddleY = kerel.bottomY() + KEREL_PADDLE_GAP;

        kerelPaddle = new GRect(paddleX, paddleY, PADDLE_WIDTH, PADDLE_HEIGHT);
        kerelPaddle.setFilled(true);
        add(kerelPaddle);
    }

    private void updateKerelPaddle() {
        if (kerelPaddle == null || kerel == null || ball == null) {
            return;
        }

        double followSpeed = 1.0 / Math.max(1, kerelFactor);

        if (!rotated) {
            double targetPaddleX = ball.getX() + BALL_RADIUS - kerelPaddle.getWidth() / 2;
            targetPaddleX = Math.max(0, Math.min(WIDTH - kerelPaddle.getWidth(), targetPaddleX));

            double targetKerelX = targetPaddleX + kerelPaddle.getWidth() / 2 - kerel.getWidth() / 2;
            targetKerelX = Math.max(0, Math.min(WIDTH - kerel.getWidth(), targetKerelX));

            double newPaddleX = kerelPaddle.getX() + (targetPaddleX - kerelPaddle.getX()) * followSpeed;
            double newKerelX = kerel.getX() + (targetKerelX - kerel.getX()) * followSpeed;

            if (ySpeedBall < 0 && ball.getY() - kerelPaddle.getY() - kerelPaddle.getHeight() < Math.max(12, Math.abs(ySpeedBall) * 3)) {
                newPaddleX = targetPaddleX;
                newKerelX = targetKerelX;
            }

            kerelPaddle.setLocation(newPaddleX, kerelPaddle.getY());
            kerel.setLocation(newKerelX-10, kerel.getY());

        } else {
            double targetPaddleY = ball.getY() + BALL_RADIUS - kerelPaddle.getHeight() / 2;
            targetPaddleY = Math.max(0, Math.min(HEIGHT - kerelPaddle.getHeight(), targetPaddleY));

            double targetPaddleX = WIDTH - kerelPaddle.getWidth();

            double targetKerelX = targetPaddleX - kerel.getWidth() - 15;
            targetKerelX = Math.max(0, Math.min(WIDTH - kerel.getWidth(), targetKerelX));

            double targetKerelY = targetPaddleY + kerelPaddle.getHeight() / 2 - kerel.getHeight() / 2;
            targetKerelY = Math.max(0, Math.min(HEIGHT - kerel.getHeight(), targetKerelY));

            double newPaddleY = kerelPaddle.getY() + (targetPaddleY - kerelPaddle.getY()) * followSpeed;
            double newKerelY = kerel.getY() + (targetKerelY - kerel.getY()) * followSpeed;

            if (xSpeedBall > 0 && kerelPaddle.getX() - ball.getX() - BALL_RADIUS * 2 < Math.max(12, Math.abs(xSpeedBall) * 3)) {
                newPaddleY = targetPaddleY;
                newKerelY = targetKerelY;
            }

            kerelPaddle.setLocation(targetPaddleX-120, newPaddleY);
            kerel.setLocation(targetKerelX-100, newKerelY);
        }
    }

    // повертає всю сцену на 90 градусів
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
        updateKerelPaddle();
        pause(300);
    }

    private void textFromStartKerel() {
        GLabel attention = new GLabel("протримайтеся " + SURVIVE_SECONDS + " секунд");
        attention.setLocation(APPLICATION_WIDTH / 2 - 200, APPLICATION_HEIGHT / 2);
        attention.setColor(Color.red);
        attention.setFont("Arial-30");
        add(attention);
        pause(3700);
        remove(attention);
    }

    // ініціалізація рівнів
    private void initLevel(int lives, double frameDelay) {
        removeAll();

        kerel = null;
        kerelPaddle = null;

        verticalBomb1 = null;
        horizontalBomb1 = null;
        verticalBomb2 = null;
        horizontalBomb2 = null;
        verticalBomb3 = null;
        horizontalBomb3 = null;
        verticalBomb4 = null;
        horizontalBomb4 = null;
        verticalBomb5 = null;
        horizontalBomb5 = null;
        verticalBomb6 = null;
        horizontalBomb6 = null;
        verticalBomb7 = null;
        horizontalBomb7 = null;
        verticalBomb8 = null;
        horizontalBomb8 = null;
        verticalBomb9 = null;
        horizontalBomb9 = null;

        lastBombAttackSecond = 0;

        heart1 = null;
        heart2 = null;
        heart3 = null;

        rotated = false;
        rotationDone = false;
        rotationEnabled = false;
        timedLevel = false;
        playerWon = false;

        countOfLives = lives;
        currentPoints = 0;
        needPointsToWin = 0;

        kerelFactor = frameDelay;
        xSpeedBall = 2;
        ySpeedBall = -2;

        paddleCreate(PADDLE_WIDTH, PADDLE_HEIGHT, PADDLE_Y_OFFSET);
        createWalls();
        ball();
        createHearts();
        createScore();

        gameInProcess = true; // ракетка вже слухається мишку
    }

    // створює перший рівень
    private void level1() {
        initLevel(3, 10);
        buildBricks(NBRICK_ROWS);
        gameUpdate();
    }

    // створює другий рівень: більше рядків і швидша гра
    private void level2() {
        initLevel(3, 8);
        buildBricks(NBRICK_ROWS + 3);
        gameUpdate();
    }

    // створює третій рівень: бос Керел, виживання і поворот екрана
    private void level3() {
        initLevel(3, 6);

        Sounds.playKerelMusic();

        kerel = new Kerel();
        add(kerel);

        needPointsToWin = SURVIVE_SECONDS;
        timedLevel = true;
        rotationEnabled = true;

        textFromStartKerel();

        for (int i = 0; i < 10; i++) {
            kerel.randomMoving();
        }
        attackHearts();
        createKerelPaddle();
        gameUpdate();
    }
}

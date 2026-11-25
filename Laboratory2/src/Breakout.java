/*
 * File: Breakout.java
 * -------------------
 * Реалізація гри Breakout (3 рівні, бонуси на 2-му рівні, бос Керел на 3-му рівні).
 */

import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import java.awt.*;
import java.awt.event.*;

/** Основний клас гри Breakout: меню, правила, 3 рівні, бонуси 2-го рівня та звуки. */
public class Breakout extends GraphicsProgram {

    /** Розміри вікна та ігрового поля. */
    public static final int APPLICATION_WIDTH = 600;
    public static final int APPLICATION_HEIGHT = 600;
    private static final int WIDTH = APPLICATION_WIDTH;
    private static final int HEIGHT = APPLICATION_HEIGHT;

    /** Розміри ракетки та відступ знизу. */
    private static final int PADDLE_WIDTH = 60;
    private static final int PADDLE_HEIGHT = 10;
    private static final int PADDLE_Y_OFFSET = 30;

    /** Параметри сітки цеглин. */
    public static final int NBRICKS_PER_ROW = 10;
    public static final int NBRICK_ROWS = 5;
    public static final int BRICK_SEP = 4;
    public static final int BRICK_WIDTH = (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;
    public static final int BRICK_HEIGHT = 8;
    public static final int BRICK_Y_OFFSET = 100;

    private static final int BALL_RADIUS = 5;
    private static final double KEREL_PADDLE_GAP = 50;
    private static final double PADDLE_BOUNCE_FACTOR = 0.15;
    private static final double MIN_BALL_X_SPEED = 0.5;
    private static final double MAX_BALL_X_SPEED = 4;
    private static final int POINTS_PER_BRICK = 100;
    private static final int SURVIVE_SECONDS = 100;

    // Стани гри
    private static final int STATE_MENU = 1;
    private static final int STATE_PLAYING = 2;
    private static final int STATE_GAME_OVER = 3;
    private static final int STATE_WIN = 4;
    private static final int STATE_RULES = 5;

    // Параметри бонусів 2-го рівня
    private static final double BONUS_DROP_CHANCE = 0.28;
    private static final double BONUS_FALL_SPEED = 2.6;
    private static final int MAX_LIVES_LEVEL2 = 4;
    private static final int EXPANDED_PADDLE_WIDTH = 96;
    private static final int SHRUNK_PADDLE_WIDTH = 36;
    private static final long PADDLE_EFFECT_MS = 7500;
    private static final long SPEED_EFFECT_MS = 6500;
    private static final double LEVEL2_SLOW_DELAY = 12;
    private static final double LEVEL2_FAST_DELAY = 5;
    private static final long TOAST_MS = 1800;
    private static final long HEART_DROP_INTERVAL_MS = 10000;
    private static final double HEART_FALL_SPEED = 2.5;

    // Кольори меню
    private static final Color BTN_BLUE = new Color(36, 123, 160);
    private static final Color BTN_PURPLE = new Color(112, 72, 196);
    private static final Color BTN_RED = new Color(192, 57, 43);
    private static final Color BTN_GREEN = new Color(39, 174, 96);
    private static final Color BTN_GRAY = new Color(74, 85, 104);
    private static final Color BTN_BORDER = new Color(210, 230, 255);
    private static final Color TITLE_CYAN = new Color(99, 230, 255);

    private volatile int gameState = STATE_MENU;
    private int currentLevel = 1;

    private GRect btnLevel1, btnLevel2, btnLevel3, btnRules, btnExit, btnBack;
    private GRect btnRetry, btnLevelMenu, btnGameOverExit;

    private long lastClickTime = 0;
    private boolean mouseListenersAdded = false;

    public GOval ball;
    public GRect paddle, leftWall, rightWall, upperWall;

    private GImage verticalBomb1, horizontalBomb1, verticalBomb2, horizontalBomb2;
    private GImage verticalBomb3, horizontalBomb3, verticalBomb4, horizontalBomb4;
    private GImage verticalBomb5, horizontalBomb5, verticalBomb6, horizontalBomb6;
    private GImage verticalBomb7, horizontalBomb7, verticalBomb8, horizontalBomb8;
    private GImage verticalBomb9, horizontalBomb9;
    private int lastBombAttackSecond = 0;

    // Бонуси 2-го рівня та таймери ефектів
    private boolean bonusesEnabled = false;
    private Bonus bonus1, bonus2, bonus3, bonus4;
    private long paddleEffectEndTime = 0;
    private long speedEffectEndTime = 0;
    private double baseFrameDelay = 10;
    private String paddleEffectName = "";
    private String speedEffectName = "";
    private GLabel activeEffectLabel, bonusToast;
    private long bonusToastEndTime = 0;
    private GImage fallingHeart;
    private long lastHeartDropTime = 0;

    public Kerel kerel;
    private GImage heart1, heart2, heart3, heart4;
    public volatile boolean gameInProcess = false;
    public int countOfLives, needPointsToWin;
    public int currentPoints = 0;
    public double xSpeedBall = 2;
    public double ySpeedBall = -2;
    public double kerelFactor = 10;
    public GRect kerelPaddle;
    private GLabel score = new GLabel("");
    private volatile boolean rotated = false;
    private boolean rotationDone = false;
    private boolean rotationEnabled = false;
    private boolean timedLevel = false;
    private boolean playerWon = false;
    private long lastTickTime = 0;

    /** Точка входу: запускає вікно гри Breakout. */
    public static void main(String[] args) {
        new Breakout().start(args);
    }

    /** Головний цикл програми: показує меню та запускає обраний рівень. */
    public void run() {
        setSize(APPLICATION_WIDTH, APPLICATION_HEIGHT);
        pause(50);
        int extraW = Math.max(0, APPLICATION_WIDTH - getWidth());
        int extraH = Math.max(0, APPLICATION_HEIGHT - getHeight());
        if (extraW > 0 || extraH > 0) {
            setSize(APPLICATION_WIDTH + extraW, APPLICATION_HEIGHT + extraH);
            pause(50);
        }
        initMouseListeners();
        showMainMenu();

        while (true) {
            if (gameState == STATE_PLAYING) {
                switch (currentLevel) {
                    case 1: level1(); break;
                    case 2: level2(); break;
                    case 3: level3(); break;
                    default: break;
                }
                if (gameState == STATE_PLAYING) {
                    showMainMenu();
                }
            }
            pause(50);
        }
    }

    /** Підключає слухачів миші один раз. */
    private void initMouseListeners() {
        if (!mouseListenersAdded) {
            addMouseListeners();
            mouseListenersAdded = true;
        }
    }

    /** Основний ігровий цикл: оновлює об'єкти, бонуси та перевіряє умови завершення. */
    private void gameUpdate() {
        gameInProcess = true;
        playerWon = false;
        lastTickTime = System.currentTimeMillis();
        lastHeartDropTime = lastTickTime;

        while (gameInProcess) {
            updateHearts();
            scoreBoard();
            updateKerelPaddle();
            ballMoving();
            updateBomb();
            updateFallingHeart();

            if (bonusesEnabled) {
                updateBonuses();
                updateBonusEffects();
            }

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

    /** Оновлює таймер виживання на 3-му рівні (1 очко = 1 секунда). */
    private void updateSurvivalTimer() {
        long now = System.currentTimeMillis();
        while (now - lastTickTime >= 1000) {
            currentPoints++;
            lastTickTime += 1000;
        }
    }

    /** Створює вертикальну бомбу від боса Керела. */
    private GImage createVerticalBomb() {
        GImage newBomb = new GImage("beeper.jpeg");
        newBomb.setSize(30, 30);
        newBomb.setLocation(kerel.getX() + kerel.getWidth() / 2 - 15, kerel.getY() + kerel.getHeight());
        add(newBomb);
        return newBomb;
    }

    /** Створює горизонтальну бомбу на рівні ракетки. */
    private GImage createHorizontalBomb() {
        GImage newBomb = new GImage("beeper.jpeg");
        newBomb.setSize(30, 30);
        newBomb.setLocation(WIDTH - 30, paddle.getY() + paddle.getHeight() / 2 - 15);
        add(newBomb);
        return newBomb;
    }

    /** Створює хвилі бомб кожні 5 секунд на 3-му рівні. */
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

    /** Рухає одну бомбу та перевіряє зіткнення з ракеткою або вихід за межі. */
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

    /** Оновлює рух усіх активних бомб на 3-му рівні. */
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

    /** Перевіряє втрату життя, перемогу та момент повороту екрана. */
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

    /** Віднімає життя при вильоті м'яча або завершує гру, якщо життів немає. */
    private void lifesCheck() {
        double xBall = ball.getX();
        boolean lost = rotated ? xBall + BALL_RADIUS * 2 < 0 : ball.getY() > APPLICATION_HEIGHT;

        if (lost) {
            countOfLives--;
            if (countOfLives <= 0) {
                playerWon = false;
                gameInProcess = false;
                return;
            }
            Sounds.playLoseLife();
            pause(100);

            if (!rotated) {
                ball.setLocation(APPLICATION_WIDTH / 2 - BALL_RADIUS, APPLICATION_HEIGHT - PADDLE_Y_OFFSET - BALL_RADIUS * 2 - 20);
                xSpeedBall = 2;
                ySpeedBall = -2;
            } else {
                ball.setLocation(APPLICATION_WIDTH / 2 - BALL_RADIUS, APPLICATION_HEIGHT / 2 - BALL_RADIUS);
                xSpeedBall = 2;
                ySpeedBall = 1;
            }
        }
    }

    /** Завершує гру перемогою, якщо набрано потрібну кількість очок. */
    private void winCheck() {
        if (currentPoints >= needPointsToWin) {
            playerWon = true;
            gameInProcess = false;
        }
    }

    /** Рухає ракетку гравця за курсором миші. */
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

    /** Обробляє кліки по кнопках у всіх меню гри. */
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
            } else if (isClicked(btnLevel2, x, y)) {
                currentLevel = 2;
                gameState = STATE_PLAYING;
            } else if (isClicked(btnLevel3, x, y)) {
                currentLevel = 3;
                gameState = STATE_PLAYING;
            } else if (isClicked(btnRules, x, y)) {
                showRulesMenu();
            } else if (isClicked(btnExit, x, y)) {
                System.exit(0);
            }
        } else if (state == STATE_RULES) {
            if (isClicked(btnBack, x, y)) {
                showMainMenu();
            }
        } else if (state == STATE_GAME_OVER) {
            if (isClicked(btnRetry, x, y)) {
                gameState = STATE_PLAYING;
            } else if (isClicked(btnLevelMenu, x, y)) {
                showMainMenu();
            } else if (isClicked(btnGameOverExit, x, y)) {
                System.exit(0);
            }
        } else if (state == STATE_WIN) {
            if (isClicked(btnLevelMenu, x, y)) {
                showMainMenu();
            } else if (isClicked(btnGameOverExit, x, y)) {
                System.exit(0);
            }
        }
    }

    /** Створює ракетку гравця. */
    private void paddleCreate(int paddleX, int paddleY, int offset) {
        paddle = new GRect((APPLICATION_WIDTH / 2) - 30, APPLICATION_HEIGHT - offset, paddleX, paddleY);
        paddle.setFilled(true);
        add(paddle);
    }

    /** Створює обмежувальні стінки поля. */
    private void createWalls() {
        leftWall = new GRect(-10, 0, 10, APPLICATION_HEIGHT);
        rightWall = new GRect(APPLICATION_WIDTH, 0, 10, APPLICATION_HEIGHT);
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

    /** Створює м'яч у стартовій позиції над ракеткою. */
    private void ball() {
        ball = new GOval((APPLICATION_WIDTH / 2) - BALL_RADIUS,
                APPLICATION_HEIGHT - PADDLE_Y_OFFSET - BALL_RADIUS * 2 - 20,
                BALL_RADIUS * 2, BALL_RADIUS * 2);
        ball.setFilled(true);
        add(ball);
    }

    /** Виставляє один рядок цеглин по центру екрана. */
    private void lineOfBricks(int startY) {
        int rowWidth = NBRICKS_PER_ROW * BRICK_WIDTH + (NBRICKS_PER_ROW - 1) * BRICK_SEP;
        int startX = (WIDTH - rowWidth) / 2;
        for (int i = 0; i < NBRICKS_PER_ROW; i++) {
            add(new Brick(startX, startY));
            startX += BRICK_WIDTH + BRICK_SEP;
        }
    }

    /** Будує задану кількість рядків цеглин та обчислює очки для перемоги. */
    private void buildBricks(int rows) {
        int startY = BRICK_Y_OFFSET;
        for (int i = 0; i < rows; i++) {
            lineOfBricks(startY);
            startY += (BRICK_HEIGHT + BRICK_SEP);
        }
        needPointsToWin = rows * NBRICKS_PER_ROW * POINTS_PER_BRICK;
    }

    /** Створює напис рахунку у верхній панелі. */
    private void createScore() {
        score = new GLabel(currentPoints + "/" + needPointsToWin);
        score.setFont("Roboto-15");
        add(score, APPLICATION_WIDTH - 80, 30);
    }

    /** Оновлює текст та позицію рахунку. */
    private void scoreBoard() {
        score.setLabel(currentPoints + "/" + needPointsToWin);
        score.setLocation(timedLevel ? APPLICATION_WIDTH - 80 : APPLICATION_WIDTH - score.getWidth() - 16,
                timedLevel ? 30 : 36);
    }

    /** Створює три стартові сердечка життів. */
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

    /** Додає слот 4-го (бонусного) сердечка на 2-му рівні. */
    private void createExtraHeartSlot() {
        heart4 = new GImage("heart.png");
        heart4.setSize(50, 50);
        heart4.setLocation(175, 10);
        heart4.setVisible(false);
        add(heart4);
    }

    /** Оновлює видимість сердечок за кількістю життів. */
    private void updateHearts() {
        if (heart4 != null) { heart4.setVisible(countOfLives >= 4); }
        if (heart3 != null) { heart3.setVisible(countOfLives >= 3); }
        if (heart2 != null) { heart2.setVisible(countOfLives >= 2); }
        if (heart1 != null) { heart1.setVisible(countOfLives >= 1); }
    }

    /** Перевіряє перетин квадрата (x, y, size) з об'єктом. */
    private boolean overlapsRect(double x, double y, double size, GObject o) {
        return x < o.getX() + o.getWidth() && x + size > o.getX()
                && y < o.getY() + o.getHeight() && y + size > o.getY();
    }

    /** Перевіряє перетин двох графічних об'єктів. */
    private boolean overlaps(GObject a, GObject b) {
        return a.getX() < b.getX() + b.getWidth() && a.getX() + a.getWidth() > b.getX()
                && a.getY() < b.getY() + b.getHeight() && a.getY() + a.getHeight() > b.getY();
    }

    /** Повертає першу цеглину на шляху м'яча або null. */
    private GObject findHitBrick(double x, double y, double size) {
        double startX = ball.getX();
        double startY = ball.getY();
        double distanceX = x - startX;
        double distanceY = y - startY;
        int steps = Math.max(1, (int) Math.ceil(Math.max(Math.abs(distanceX), Math.abs(distanceY))));

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

    /** Відбиває м'яч від цеглини залежно від сторони зіткнення. */
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
                xSpeedBall = oldX + size / 2 < brick.getX() + brick.getWidth() / 2 ? -Math.abs(xSpeedBall) : Math.abs(xSpeedBall);
            } else {
                ySpeedBall = oldY + size / 2 < brick.getY() + brick.getHeight() / 2 ? -Math.abs(ySpeedBall) : Math.abs(ySpeedBall);
            }
        }
    }

    /** Обчислює нову горизонтальну швидкість після відбиття від ракетки. */
    private double bounceAngle(double ballCenter, double paddleCenter) {
        double v = Math.max(-MAX_BALL_X_SPEED, Math.min(MAX_BALL_X_SPEED, (ballCenter - paddleCenter) * PADDLE_BOUNCE_FACTOR));
        if (Math.abs(v) < MIN_BALL_X_SPEED) {
            double dir = v != 0 ? Math.signum(v) : (xSpeedBall < 0 ? -1 : 1);
            v = dir * MIN_BALL_X_SPEED;
        }
        return v;
    }

    /** Рухає м'яч і обробляє зіткнення з цеглинами, ракетками та стінами. */
    private void ballMoving() {
        double ballX = ball.getX();
        double ballY = ball.getY();
        double ballSize = BALL_RADIUS * 2;
        double nextX = ballX + xSpeedBall;
        double nextY = ballY + ySpeedBall;

        GObject brick = findHitBrick(nextX, nextY, ballSize);
        if (brick != null) {
            bounceOffBrick(brick, nextX, nextY, ballSize);
            Sounds.playHit();
            if (bonusesEnabled) {
                maybeSpawnBonus(brick);
            }
            remove(brick);
            currentPoints += POINTS_PER_BRICK;
        }

        nextX = ballX + xSpeedBall;
        nextY = ballY + ySpeedBall;

        if (!rotated) {
            if (paddle != null && ySpeedBall > 0 && nextY + ballSize >= paddle.getY()
                    && ballY + ballSize <= paddle.getY() + paddle.getHeight()
                    && nextX + ballSize > paddle.getX() && nextX < paddle.getX() + paddle.getWidth()) {
                xSpeedBall = bounceAngle(nextX + BALL_RADIUS, paddle.getX() + paddle.getWidth() / 2);
                ySpeedBall = -Math.abs(ySpeedBall);
                nextY = paddle.getY() - ballSize;
                Sounds.playHit();
            }
            if (kerelPaddle != null && ySpeedBall < 0 && nextY <= kerelPaddle.getY() + kerelPaddle.getHeight()
                    && ballY >= kerelPaddle.getY() + kerelPaddle.getHeight()
                    && nextX + ballSize > kerelPaddle.getX() && nextX < kerelPaddle.getX() + kerelPaddle.getWidth()) {
                double error = RandomGenerator.getInstance().nextDouble(-7.0, 7.0);
                xSpeedBall = bounceAngle(nextX + BALL_RADIUS + error, kerelPaddle.getX() + kerelPaddle.getWidth() / 2);
                ySpeedBall = Math.abs(ySpeedBall);
                nextY = kerelPaddle.getY() + kerelPaddle.getHeight();
                Sounds.playHit();
            }
        } else {
            if (kerelPaddle != null && xSpeedBall > 0 && nextX + ballSize >= kerelPaddle.getX()
                    && ballX + ballSize <= kerelPaddle.getX() + kerelPaddle.getWidth()
                    && nextY + ballSize > kerelPaddle.getY() && nextY < kerelPaddle.getY() + kerelPaddle.getHeight()) {
                double paddleCenter = kerelPaddle.getY() + kerelPaddle.getHeight() / 2;
                double impactOffset = nextY + BALL_RADIUS - paddleCenter + RandomGenerator.getInstance().nextDouble(-7.0, 7.0);
                ySpeedBall = Math.max(-MAX_BALL_X_SPEED, Math.min(MAX_BALL_X_SPEED, impactOffset * PADDLE_BOUNCE_FACTOR));
                if (Math.abs(ySpeedBall) < MIN_BALL_X_SPEED) {
                    ySpeedBall = (RandomGenerator.getInstance().nextBoolean() ? 1 : -1) * MIN_BALL_X_SPEED;
                }
                xSpeedBall = -Math.max(MIN_BALL_X_SPEED, Math.abs(xSpeedBall));
                nextX = kerelPaddle.getX() - ballSize;
                Sounds.playHit();
            }
            if (paddle != null && xSpeedBall < 0 && nextX <= paddle.getX() + paddle.getWidth()
                    && ballX >= paddle.getX() + paddle.getWidth()
                    && nextY + ballSize > paddle.getY() && nextY < paddle.getY() + paddle.getHeight()) {
                double impactOffset = nextY + BALL_RADIUS - (paddle.getY() + paddle.getHeight() / 2);
                ySpeedBall = Math.max(-MAX_BALL_X_SPEED, Math.min(MAX_BALL_X_SPEED, impactOffset * PADDLE_BOUNCE_FACTOR));
                if (Math.abs(ySpeedBall) < MIN_BALL_X_SPEED) {
                    ySpeedBall = ySpeedBall < 0 ? -MIN_BALL_X_SPEED : MIN_BALL_X_SPEED;
                }
                xSpeedBall = Math.max(MIN_BALL_X_SPEED, Math.abs(xSpeedBall));
                nextX = paddle.getX() + paddle.getWidth();
                Sounds.playHit();
            }
        }

        if (!rotated) {
            if (nextX <= 0) { nextX = 0; xSpeedBall = Math.abs(xSpeedBall); Sounds.playHit(); }
            if (nextX + ballSize >= WIDTH) { nextX = WIDTH - ballSize; xSpeedBall = -Math.abs(xSpeedBall); Sounds.playHit(); }
            if (nextY <= 0) { nextY = 0; ySpeedBall = Math.abs(ySpeedBall); Sounds.playHit(); }
        } else {
            if (nextY <= 0) { nextY = 0; ySpeedBall = Math.abs(ySpeedBall); Sounds.playHit(); }
            if (nextY + ballSize >= HEIGHT) { nextY = HEIGHT - ballSize; ySpeedBall = -Math.abs(ySpeedBall); Sounds.playHit(); }
        }

        ball.setLocation(nextX, nextY);
    }

    /** З певною ймовірністю створює падаючий бонус на місці розбитої цеглини. */
    private void maybeSpawnBonus(GObject brick) {
        RandomGenerator rgen = RandomGenerator.getInstance();
        if (rgen.nextDouble(0.0, 1.0) > BONUS_DROP_CHANCE
                || (bonus1 != null && bonus2 != null && bonus3 != null && bonus4 != null)) {
            return;
        }
        BonusType[] types = BonusType.values();
        BonusType chosen = types[rgen.nextInt(0, types.length - 1)];
        double x = Math.max(4, Math.min(WIDTH - Bonus.BONUS_WIDTH - 4,
                brick.getX() + (brick.getWidth() - Bonus.BONUS_WIDTH) / 2.0));
        Bonus b = new Bonus(chosen, x, brick.getY());
        add(b);

        if (bonus1 == null) { bonus1 = b; }
        else if (bonus2 == null) { bonus2 = b; }
        else if (bonus3 == null) { bonus3 = b; }
        else { bonus4 = b; }
    }

    /** Рухає один падаючий бонус і перевіряє його підбирання ракеткою. */
    private Bonus updateOneBonus(Bonus b) {
        if (b == null) {
            return null;
        }
        b.fall(BONUS_FALL_SPEED);
        if (paddle != null && overlaps(b, paddle)) {
            BonusType type = b.getType();
            remove(b);
            applyBonus(type);
            return null;
        }
        if (b.getY() > APPLICATION_HEIGHT) {
            remove(b);
            return null;
        }
        return b;
    }

    /** Оновлює всі активні падаючі бонуси 2-го рівня. */
    private void updateBonuses() {
        bonus1 = updateOneBonus(bonus1);
        bonus2 = updateOneBonus(bonus2);
        bonus3 = updateOneBonus(bonus3);
        bonus4 = updateOneBonus(bonus4);
    }

    /** Застосовує ефект підібраного бонусу та відтворює відповідний звук. */
    private void applyBonus(BonusType type) {
        long now = System.currentTimeMillis();
        switch (type) {
            case EXTRA_LIFE:
                Sounds.playBonusGood();
                if (countOfLives < MAX_LIVES_LEVEL2) {
                    countOfLives++;
                    showBonusToast("+1 ЖИТТЯ!", type.getFillColor());
                } else {
                    showBonusToast("МАКС. ЖИТТІВ!", type.getFillColor());
                }
                break;
            case EXPAND_PADDLE:
                Sounds.playBonusGood();
                resizePaddle(EXPANDED_PADDLE_WIDTH);
                paddleEffectEndTime = now + PADDLE_EFFECT_MS;
                paddleEffectName = "WIDE";
                showBonusToast("ШИРША РАКЕТКА!", type.getFillColor());
                break;
            case SLOW_BALL:
                Sounds.playBonusGood();
                kerelFactor = LEVEL2_SLOW_DELAY;
                speedEffectEndTime = now + SPEED_EFFECT_MS;
                speedEffectName = "SLOW";
                showBonusToast("СПОВІЛЬНЕННЯ!", type.getFillColor());
                break;
            case SHRINK_PADDLE:
                Sounds.playBonusBad();
                resizePaddle(SHRUNK_PADDLE_WIDTH);
                paddleEffectEndTime = now + PADDLE_EFFECT_MS;
                paddleEffectName = "SHORT";
                showBonusToast("ВУЖЧА РАКЕТКА!", type.getFillColor());
                break;
            case FAST_BALL:
                Sounds.playBonusBad();
                kerelFactor = LEVEL2_FAST_DELAY;
                speedEffectEndTime = now + SPEED_EFFECT_MS;
                speedEffectName = "FAST";
                showBonusToast("ПРИСКОРЕННЯ!", type.getFillColor());
                break;
            default:
                break;
        }
    }

    /** Змінює ширину ракетки зі збереженням її центру в межах вікна. */
    private void resizePaddle(double newWidth) {
        if (paddle == null) {
            return;
        }
        double cx = paddle.getX() + paddle.getWidth() / 2.0;
        paddle.setSize(newWidth, PADDLE_HEIGHT);
        paddle.setLocation(Math.max(0, Math.min(WIDTH - newWidth, cx - newWidth / 2.0)), paddle.getY());
    }

    /** Показує коротке спливаюче повідомлення про підібраний бонус. */
    private void showBonusToast(String message, Color color) {
        if (bonusToast != null) {
            remove(bonusToast);
        }
        bonusToast = new GLabel(message);
        bonusToast.setFont("SansSerif-BOLD-18");
        bonusToast.setColor(color);
        add(bonusToast, (APPLICATION_WIDTH - bonusToast.getWidth()) / 2.0, APPLICATION_HEIGHT - 85);
        bonusToastEndTime = System.currentTimeMillis() + TOAST_MS;
    }

    /** Скидає завершені тимчасові ефекти бонусів та оновлює індикатор у HUD. */
    private void updateBonusEffects() {
        long now = System.currentTimeMillis();
        if (paddleEffectEndTime > 0 && now >= paddleEffectEndTime) {
            resizePaddle(PADDLE_WIDTH);
            paddleEffectEndTime = 0;
            paddleEffectName = "";
        }
        if (speedEffectEndTime > 0 && now >= speedEffectEndTime) {
            kerelFactor = baseFrameDelay;
            speedEffectEndTime = 0;
            speedEffectName = "";
        }
        if (bonusToast != null && now >= bonusToastEndTime) {
            remove(bonusToast);
            bonusToast = null;
        }
        if (activeEffectLabel != null) {
            String text = "";
            if (paddleEffectEndTime > 0) {
                text = paddleEffectName + " " + ((paddleEffectEndTime - now + 999) / 1000) + "s";
            }
            if (speedEffectEndTime > 0) {
                if (!text.isEmpty()) { text = text + " | "; }
                text = text + speedEffectName + " " + ((speedEffectEndTime - now + 999) / 1000) + "s";
            }
            activeEffectLabel.setLabel(text);
            activeEffectLabel.setLocation((APPLICATION_WIDTH - activeEffectLabel.getWidth()) / 2.0, 36);
        }
    }

    /** Оновлює падаюче додаткове серце, яке з'являється кожні 10 секунд. */
    private void updateFallingHeart() {
        long now = System.currentTimeMillis();
        if (fallingHeart == null && now - lastHeartDropTime >= HEART_DROP_INTERVAL_MS) {
            lastHeartDropTime = now;
            RandomGenerator rgen = RandomGenerator.getInstance();
            fallingHeart = new GImage("heart.png");
            fallingHeart.setSize(32, 32);
            if (!rotated) {
                fallingHeart.setLocation(rgen.nextInt(30, WIDTH - 62), 70);
            } else {
                fallingHeart.setLocation(WIDTH - 40, rgen.nextInt(40, HEIGHT - 72));
            }
            add(fallingHeart);
        }

        if (fallingHeart == null) {
            return;
        }
        fallingHeart.move(rotated ? -HEART_FALL_SPEED : 0, rotated ? 0 : HEART_FALL_SPEED);

        if (paddle != null && overlaps(fallingHeart, paddle)) {
            remove(fallingHeart);
            fallingHeart = null;
            Sounds.playBonusGood();
            int maxLives = (currentLevel == 2) ? MAX_LIVES_LEVEL2 : 3;
            if (countOfLives < maxLives) {
                countOfLives++;
                if (heart2 != null && heart2.getParent() == null) {
                    heart2.setLocation(60, 10);
                    add(heart2);
                }
                if (heart3 != null && heart3.getParent() == null) {
                    heart3.setLocation(120, 10);
                    add(heart3);
                }
            }
            return;
        }

        if (fallingHeart.getY() > APPLICATION_HEIGHT || fallingHeart.getX() + fallingHeart.getWidth() < 0) {
            remove(fallingHeart);
            fallingHeart = null;
        }
    }

    /** Додає вертикальний градієнт із горизонтальних смуг GRect у контейнер. */
    private void addGradientBands(GCompound container, Color top, Color bottom, int steps) {
        double w = Math.max(APPLICATION_WIDTH + 40, getWidth() + 40);
        double h = (double) (APPLICATION_HEIGHT + 40) / steps;
        for (int i = 0; i < steps; i++) {
            double k = (double) i / (steps - 1);
            Color c = new Color(
                    (int) (top.getRed() + (bottom.getRed() - top.getRed()) * k),
                    (int) (top.getGreen() + (bottom.getGreen() - top.getGreen()) * k),
                    (int) (top.getBlue() + (bottom.getBlue() - top.getBlue()) * k));
            GRect band = new GRect(-20, -20 + i * h, w, h + 1);
            band.setFilled(true);
            band.setFillColor(c);
            band.setColor(c);
            container.add(band);
        }
    }

    /** Створює темний аркадний фон для меню та інформаційних екранів. */
    private void createMenuBackground() {
        setBackground(new Color(18, 20, 48));
        GCompound bg = new GCompound();
        addGradientBands(bg, new Color(11, 18, 38), new Color(28, 22, 62), 20);
        int[] sx = {42, 115, 190, 275, 360, 445, 520, 565, 78, 155, 310, 485, 540, 230, 395};
        int[] sy = {38, 85, 45, 70, 32, 92, 55, 130, 210, 315, 495, 420, 540, 535, 510};
        for (int i = 0; i < sx.length; i++) {
            GOval star = new GOval(sx[i], sy[i], (i % 3 == 0) ? 4 : 2, (i % 3 == 0) ? 4 : 2);
            star.setFilled(true);
            Color c = (i % 2 == 0) ? new Color(180, 230, 255) : new Color(255, 220, 150);
            star.setFillColor(c);
            star.setColor(c);
            bg.add(star);
        }
        add(bg);
        bg.sendToBack();
    }

    /** Створює окремий фон для 1-го, 2-го або 3-го рівня. */
    private void createLevelBackground(int level) {
        GCompound bg = new GCompound();
        if (level == 1) {
            setBackground(new Color(20, 40, 63));
            addGradientBands(bg, new Color(13, 27, 42), new Color(27, 54, 84), 20);
            int[] dx = {40, 140, 260, 380, 500, 90, 210, 340, 460, 555};
            int[] dy = {150, 260, 210, 310, 185, 420, 470, 395, 490, 360};
            for (int i = 0; i < dx.length; i++) {
                GOval dot = new GOval(dx[i], dy[i], 3, 3);
                dot.setFilled(true);
                Color c = new Color(150, 215, 255, 110);
                dot.setFillColor(c);
                dot.setColor(c);
                bg.add(dot);
            }
        } else if (level == 2) {
            setBackground(new Color(38, 18, 65));
            addGradientBands(bg, new Color(24, 14, 46), new Color(52, 22, 84), 20);
            GOval orb1 = new GOval(-40, 260, 220, 220);
            orb1.setFilled(true);
            orb1.setFillColor(new Color(120, 55, 190, 38));
            orb1.setColor(new Color(120, 55, 190, 38));
            bg.add(orb1);
            GOval orb2 = new GOval(420, 320, 240, 240);
            orb2.setFilled(true);
            orb2.setFillColor(new Color(52, 152, 219, 32));
            orb2.setColor(new Color(52, 152, 219, 32));
            bg.add(orb2);
        } else {
            setBackground(new Color(50, 13, 23));
            addGradientBands(bg, new Color(36, 10, 18), new Color(68, 16, 28), 20);
            GOval ring1 = new GOval(120, 120, 360, 360);
            ring1.setColor(new Color(220, 60, 80, 55));
            bg.add(ring1);
            GOval ring2 = new GOval(200, 200, 200, 200);
            ring2.setColor(new Color(255, 100, 110, 45));
            bg.add(ring2);
        }

        if (level != 3) {
            double hudWidth = Math.max(APPLICATION_WIDTH + 40, getWidth() + 40);
            GRect hud = new GRect(-20, -20, hudWidth, 88);
            hud.setFilled(true);
            hud.setFillColor(new Color(8, 12, 24, 170));
            hud.setColor(new Color(8, 12, 24, 170));
            bg.add(hud);

            GLine line = new GLine(-20, 68, hudWidth, 68);
            line.setColor(level == 1 ? new Color(72, 190, 235, 140) : new Color(185, 120, 255, 150));
            bg.add(line);
        }

        add(bg);
        bg.sendToBack();
    }

    /** Задає контрастні кольори ракетки, м'яча та рахунку під фон рівня. */
    private void styleLevelObjects(int level) {
        if (paddle != null) {
            if (level == 1) { paddle.setFillColor(new Color(64, 224, 255)); }
            else if (level == 2) { paddle.setFillColor(new Color(255, 196, 61)); }
            else { paddle.setFillColor(new Color(255, 95, 95)); }
            paddle.setColor(Color.WHITE);
        }
        if (ball != null) {
            ball.setFillColor(Color.WHITE);
            ball.setColor(level == 1 ? new Color(140, 240, 255) : new Color(255, 230, 130));
        }
        if (score != null) {
            score.setFont("SansSerif-BOLD-16");
            score.setColor(Color.WHITE);
        }
    }

    /** Створює стилізовану кнопку зі скругленими кутами та тінню. */
    private GRect createButton(String text, double x, double y, double width, double height, Color fill) {
        GRoundRect shadow = new GRoundRect(x + 3, y + 4, width, height, 14, 14);
        shadow.setFilled(true);
        shadow.setFillColor(new Color(0, 0, 0, 110));
        shadow.setColor(new Color(0, 0, 0, 110));
        add(shadow);

        GRoundRect box = new GRoundRect(x, y, width, height, 14, 14);
        box.setFilled(true);
        box.setFillColor(fill);
        box.setColor(BTN_BORDER);
        add(box);

        GLabel label = new GLabel(text);
        label.setFont("SansSerif-BOLD-15");
        label.setColor(Color.WHITE);
        add(label, x + (width - label.getWidth()) / 2.0, y + (height + label.getAscent()) / 2.0 - 2);
        return box;
    }

    /** Перевіряє, чи потрапив клік у межі кнопки. */
    private boolean isClicked(GRect button, int x, int y) {
        return button != null && x >= button.getX() && x <= button.getX() + button.getWidth()
                && y >= button.getY() && y <= button.getY() + button.getHeight();
    }

    /** Додає заголовок із тінню по центру екрана. */
    private void addStyledTitle(String text, double y, Color color) {
        GLabel shadow = new GLabel(text);
        shadow.setFont("SansSerif-BOLD-38");
        shadow.setColor(new Color(18, 42, 76));
        double x = (APPLICATION_WIDTH - shadow.getWidth()) / 2.0;
        add(shadow, x + 3, y + 3);

        GLabel title = new GLabel(text);
        title.setFont("SansSerif-BOLD-38");
        title.setColor(color);
        add(title, x, y);
    }

    /** Показує головне меню гри. */
    private void showMainMenu() {
        Sounds.stopAll();
        Sounds.playBgMusic();
        gameInProcess = false;
        gameState = STATE_MENU;
        removeAll();
        createMenuBackground();

        addStyledTitle("BREAKOUT", 108, TITLE_CYAN);
        GLabel sub = new GLabel("АРКАДА • 3 РІВНІ • БОС КЕРЕЛ");
        sub.setFont("SansSerif-BOLD-12");
        sub.setColor(new Color(185, 210, 240));
        add(sub, (APPLICATION_WIDTH - sub.getWidth()) / 2.0, 136);

        double w = 230, h = 44, x = (APPLICATION_WIDTH - w) / 2.0;
        btnLevel1 = createButton("Рівень 1 — Класика", x, 175, w, h, BTN_BLUE);
        btnLevel2 = createButton("Рівень 2 — Бонуси", x, 233, w, h, BTN_PURPLE);
        btnLevel3 = createButton("Рівень 3 — Бос Керел", x, 291, w, h, BTN_RED);
        btnRules = createButton("Правила гри", x, 349, w, h, BTN_GREEN);
        btnExit = createButton("Вихід", x, 407, w, h, BTN_GRAY);
    }

    /** Показує екран правил гри, рівнів та бонусів 2-го рівня. */
    private void showRulesMenu() {
        gameInProcess = false;
        gameState = STATE_RULES;
        removeAll();
        createMenuBackground();
        addStyledTitle("ПРАВИЛА ГРИ", 78, TITLE_CYAN);

        GRoundRect card = new GRoundRect(42, 102, APPLICATION_WIDTH - 84, 370, 18, 18);
        card.setFilled(true);
        card.setFillColor(new Color(14, 22, 46, 215));
        card.setColor(new Color(99, 230, 255, 140));
        add(card);

        String[] lines = {
                "Керування: рухайте мишею, щоб відбивати м'яч ракеткою.",
                "Рівень 1: класичний Breakout (5 рядків цеглин).",
                "Рівень 2: 8 рядків цеглин, швидший темп і бонуси.",
                "Рівень 3: бос Керел — протримайтесь 100 секунд!",
                "Корисні бонуси (2-й рівень — ловіть ракеткою):",
                "  +1♥ — додаткове життя (максимум 4).",
                "  WIDE — ширша ракетка   |   SLOW — сповільнення.",
                "Ускладнюючі бонуси (краще уникати!):",
                "  SHORT — вужча ракетка  |   FAST — прискорення."
        };
        double y = 142;
        for (int i = 0; i < lines.length; i++) {
            GLabel lbl = new GLabel(lines[i]);
            boolean header = (i == 0 || i == 4 || i == 7);
            lbl.setFont(header ? "SansSerif-BOLD-14" : "SansSerif-13");
            if (i == 4) { lbl.setColor(new Color(82, 230, 145)); }
            else if (i == 7) { lbl.setColor(new Color(255, 120, 110)); }
            else { lbl.setColor(new Color(225, 235, 250)); }
            add(lbl, 64, y);
            y += header ? 34 : 28;
        }
        btnBack = createButton("Назад до меню", (APPLICATION_WIDTH - 210) / 2.0, 494, 210, 44, BTN_BLUE);
    }

    /** Додає картку з номером рівня та рахунком на екрани завершення гри. */
    private void addStatsCard(double y) {
        GRoundRect panel = new GRoundRect(145, y, 310, 74, 16, 16);
        panel.setFilled(true);
        panel.setFillColor(new Color(14, 22, 46, 210));
        panel.setColor(new Color(140, 200, 255, 130));
        add(panel);

        GLabel lvl = new GLabel("Рівень: " + currentLevel);
        lvl.setFont("SansSerif-BOLD-16");
        lvl.setColor(new Color(185, 225, 255));
        add(lvl, (APPLICATION_WIDTH - lvl.getWidth()) / 2.0, y + 30);

        String txt = timedLevel ? "Витримано: " + currentPoints + " / " + needPointsToWin + " сек"
                : "Очки: " + currentPoints + " / " + needPointsToWin;
        GLabel pts = new GLabel(txt);
        pts.setFont("SansSerif-BOLD-16");
        pts.setColor(Color.WHITE);
        add(pts, (APPLICATION_WIDTH - pts.getWidth()) / 2.0, y + 58);
    }

    /** Показує екран поразки та відтворює звук програшу. */
    private void showGameOverMenu() {
        Sounds.stopAll();
        Sounds.playLose();
        gameInProcess = false;
        removeAll();
        gameState = STATE_GAME_OVER;
        createMenuBackground();

        addStyledTitle("ВИ ПРОГРАЛИ", 122, new Color(255, 95, 95));
        addStatsCard(152);
        double w = 230, h = 44, x = (APPLICATION_WIDTH - w) / 2.0;
        btnRetry = createButton("Спробувати ще раз", x, 255, w, h, BTN_GREEN);
        btnLevelMenu = createButton("Меню рівнів", x, 315, w, h, BTN_BLUE);
        btnGameOverExit = createButton("Вихід з гри", x, 375, w, h, BTN_GRAY);
    }

    /** Показує екран перемоги та відтворює звук перемоги. */
    private void showWinMenu() {
        Sounds.stopAll();
        Sounds.playWinSound();
        gameInProcess = false;
        removeAll();
        gameState = STATE_WIN;
        createMenuBackground();

        addStyledTitle("ВИ ПЕРЕМОГЛИ!", 122, new Color(82, 235, 140));
        addStatsCard(152);
        double w = 230, h = 44, x = (APPLICATION_WIDTH - w) / 2.0;
        btnLevelMenu = createButton("Меню рівнів", x, 265, w, h, BTN_BLUE);
        btnGameOverExit = createButton("Вихід з гри", x, 325, w, h, BTN_GRAY);
    }

    /** Анімація атаки боса Керела по сердечках на 3-му рівні. */
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

    /** Створює ракетку боса Керела. */
    private void createKerelPaddle() {
        kerel.resetPose();
        double center = Math.max(PADDLE_WIDTH / 2.0, Math.min(WIDTH - PADDLE_WIDTH / 2.0, kerel.centerX()));
        kerel.moveBy(center - kerel.centerX());
        kerelPaddle = new GRect(kerel.centerX() - PADDLE_WIDTH / 2.0, kerel.bottomY() + KEREL_PADDLE_GAP, PADDLE_WIDTH, PADDLE_HEIGHT);
        kerelPaddle.setFilled(true);
        add(kerelPaddle);
    }

    /** Оновлює рух боса Керела та його ракетки за м'ячем. */
    private void updateKerelPaddle() {
        if (kerelPaddle == null || kerel == null || ball == null) {
            return;
        }
        double followSpeed = 1.0 / Math.max(1, kerelFactor);

        if (!rotated) {
            double targetPaddleX = Math.max(0, Math.min(WIDTH - kerelPaddle.getWidth(), ball.getX() + BALL_RADIUS - kerelPaddle.getWidth() / 2));
            double targetKerelX = Math.max(0, Math.min(WIDTH - kerel.getWidth(), targetPaddleX + kerelPaddle.getWidth() / 2 - kerel.getWidth() / 2));
            double newPaddleX = kerelPaddle.getX() + (targetPaddleX - kerelPaddle.getX()) * followSpeed;
            double newKerelX = kerel.getX() + (targetKerelX - kerel.getX()) * followSpeed;
            if (ySpeedBall < 0 && ball.getY() - kerelPaddle.getY() - kerelPaddle.getHeight() < Math.max(12, Math.abs(ySpeedBall) * 3)) {
                newPaddleX = targetPaddleX;
                newKerelX = targetKerelX;
            }
            kerelPaddle.setLocation(newPaddleX, kerelPaddle.getY());
            kerel.setLocation(newKerelX - 10, kerel.getY());
        } else {
            double targetPaddleY = Math.max(0, Math.min(HEIGHT - kerelPaddle.getHeight(), ball.getY() + BALL_RADIUS - kerelPaddle.getHeight() / 2));
            double targetPaddleX = WIDTH - kerelPaddle.getWidth();
            double targetKerelX = Math.max(0, Math.min(WIDTH - kerel.getWidth(), targetPaddleX - kerel.getWidth() - 15));
            double targetKerelY = Math.max(0, Math.min(HEIGHT - kerel.getHeight(), targetPaddleY + kerelPaddle.getHeight() / 2 - kerel.getHeight() / 2));
            double newPaddleY = kerelPaddle.getY() + (targetPaddleY - kerelPaddle.getY()) * followSpeed;
            double newKerelY = kerel.getY() + (targetKerelY - kerel.getY()) * followSpeed;
            if (xSpeedBall > 0 && kerelPaddle.getX() - ball.getX() - BALL_RADIUS * 2 < Math.max(12, Math.abs(xSpeedBall) * 3)) {
                newPaddleY = targetPaddleY;
                newKerelY = targetKerelY;
            }
            kerelPaddle.setLocation(targetPaddleX - 120, newPaddleY);
            kerel.setLocation(targetKerelX - 100, newKerelY);
        }
    }

    /** Повертає всі елементи сцени на 90 градусів (3-й рівень). */
    private void rotateGame() {
        final double cx = WIDTH / 2.0;
        final double cy = HEIGHT / 2.0;
        int count = getElementCount();

        for (int i = 0; i < count; i++) {
            GObject obj = getElement(i);
            double oldW = obj.getWidth();
            double oldH = obj.getHeight();
            double centerX = obj.getX() + oldW / 2;
            double centerY = obj.getY() + oldH / 2;
            double newX = cx - (centerY - cy);
            double newY = cy + (centerX - cx);
            if (obj instanceof GScalable && oldW > 0 && oldH > 0) {
                ((GScalable) obj).scale(oldH / oldW, oldW / oldH);
            }
            obj.setLocation(newX - obj.getWidth() / 2, newY - obj.getHeight() / 2);
        }

        double oldXSpeed = xSpeedBall;
        xSpeedBall = -ySpeedBall;
        ySpeedBall = oldXSpeed;
        rotated = !rotated;
        updateKerelPaddle();
        pause(300);
    }

    /** Показує стартове попередження перед 3-м рівнем. */
    private void textFromStartKerel() {
        GLabel attention = new GLabel("протримайтеся " + SURVIVE_SECONDS + " секунд");
        attention.setLocation(APPLICATION_WIDTH / 2 - 200, APPLICATION_HEIGHT / 2);
        attention.setColor(Color.red);
        attention.setFont("Arial-30");
        add(attention);
        pause(3700);
        remove(attention);
    }

    /** Скидає стан і створює базові об'єкти перед стартом рівня. */
    private void initLevel(int lives, double frameDelay) {
        removeAll();
        kerel = null;
        kerelPaddle = null;
        verticalBomb1 = null; horizontalBomb1 = null; verticalBomb2 = null; horizontalBomb2 = null;
        verticalBomb3 = null; horizontalBomb3 = null; verticalBomb4 = null; horizontalBomb4 = null;
        verticalBomb5 = null; horizontalBomb5 = null; verticalBomb6 = null; horizontalBomb6 = null;
        verticalBomb7 = null; horizontalBomb7 = null; verticalBomb8 = null; horizontalBomb8 = null;
        verticalBomb9 = null; horizontalBomb9 = null;
        lastBombAttackSecond = 0;

        bonusesEnabled = false;
        bonus1 = null; bonus2 = null; bonus3 = null; bonus4 = null;
        paddleEffectEndTime = 0;
        speedEffectEndTime = 0;
        paddleEffectName = "";
        speedEffectName = "";
        activeEffectLabel = null;
        bonusToast = null;
        bonusToastEndTime = 0;
        fallingHeart = null;
        lastHeartDropTime = 0;
        baseFrameDelay = frameDelay;

        heart1 = null; heart2 = null; heart3 = null; heart4 = null;
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
        gameInProcess = true;
    }

    /** Запускає 1-й рівень (класичний Breakout). */
    private void level1() {
        initLevel(3, 10);
        createLevelBackground(1);
        styleLevelObjects(1);
        buildBricks(NBRICK_ROWS);
        Sounds.stopAll();
        Sounds.playLevel1Music();
        gameUpdate();
    }

    /** Запускає 2-й рівень (більше цеглин і падаючі бонуси). */
    private void level2() {
        initLevel(3, 8);
        createLevelBackground(2);
        styleLevelObjects(2);
        createExtraHeartSlot();

        bonusesEnabled = true;
        activeEffectLabel = new GLabel("");
        activeEffectLabel.setFont("SansSerif-BOLD-13");
        activeEffectLabel.setColor(new Color(255, 225, 115));
        add(activeEffectLabel, APPLICATION_WIDTH / 2.0, 36);

        buildBricks(NBRICK_ROWS + 3);
        Sounds.stopAll();
        Sounds.playLevel2Music();
        gameUpdate();
    }

    /** Запускає 3-й рівень (бос Керел, виживання та поворот екрана). */
    private void level3() {
        initLevel(3, 6);
        createLevelBackground(3);
        styleLevelObjects(3);
        Sounds.stopAll();
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
        kerelPaddle.setFillColor(new Color(255, 190, 90));
        kerelPaddle.setColor(Color.WHITE);
        gameUpdate();
    }
}

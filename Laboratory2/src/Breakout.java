/*
 * File: Breakout.java
 * -------------------
 * Name:
 * Section Leader:
 *
 * This file will eventually implement the game of Breakout.
 */

import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class Breakout extends GraphicsProgram {

    /** Width and height of application window in pixels */
    public static final int APPLICATION_WIDTH = 400;
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
    private static final int BALL_RADIUS = 10;

    /** Offset of the top brick row from the top */
    public static final int BRICK_Y_OFFSET = 100;

    /** Number of turns */
    private static final int NTURNS = 3;

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

    /* Method: run() */
    /** Runs the Breakout program. */
    public GOval ball;
    public GRect paddle;
    public GRect leftWall;
    public GRect rightWall;
    public GRect upperWall;
    public boolean gameInProcess = true;
    public int countOfLives;
    public int needPointsToWin;
    public int currentPoints = 0;
    public double xSpeedBall = 1;
    public double ySpeedBall = -1;
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
            ballMoving();
            gameTriggers();
            pause(10);
        }
    }
    //перевірка чи не трапилось чогось, що має впливати на гру(виліт мʼячика за межі ігрового поля і тд)
    private void gameTriggers(){
        lifesCheck();
        winCheck();
    }
    //брейкає гру, якщо більше не залишилося життів
    private void lifesCheck(){
        double xBall = ball.getX();
        double yBall = ball.getY();
        if(xBall < -10 || xBall > APPLICATION_WIDTH+10 || yBall > APPLICATION_HEIGHT || yBall<-5){
            countOfLives--;
            if(countOfLives==0){
                gameInProcess = false;
                //тут встав програшне віконце
                showGameOverMenu();
                return;
            }
            pause(100);
            ball.setLocation((APPLICATION_WIDTH/2)-BALL_RADIUS*2, APPLICATION_HEIGHT-PADDLE_Y_OFFSET-BALL_RADIUS-20);
            ySpeedBall = -2;
            xSpeedBall = 2;
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
        int x = e.getX();
        if(x>0 && x<APPLICATION_WIDTH-PADDLE_WIDTH) {
            paddle.setLocation(x, APPLICATION_HEIGHT-PADDLE_Y_OFFSET);
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
        upperWall = new GRect(0, -20, APPLICATION_WIDTH, 20);
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
        ball = new GOval((APPLICATION_WIDTH/2)-BALL_RADIUS, APPLICATION_HEIGHT-PADDLE_Y_OFFSET-BALL_RADIUS-20, BALL_RADIUS, BALL_RADIUS);
        add(ball);
    }
    //логіка відбивання мʼяча, а також знищення бріків
    private void ballMoving() {
        GObject collider = getCollidingObject();
        if (collider == paddle) {
            double paddleCenterX = paddle.getX() + PADDLE_WIDTH/2;
            double ballCenterX = ball.getX() + BALL_RADIUS/2;
            double hitOffset = ballCenterX - paddleCenterX;
            xSpeedBall = hitOffset * 0.15;
            ySpeedBall *= -1;
        }
        else if (collider == upperWall) {
            ySpeedBall *= -1;
        }
        else if (collider == leftWall) {
            xSpeedBall *= -1;
        }
        else if (collider == rightWall) {
            xSpeedBall *= -1;
        }
        else if (collider != null) {
            GObject hitBrick = collider;
            if (collider.getParent() != null && collider.getParent() instanceof Brick) {
                hitBrick = (GObject) collider.getParent();
            }

            if (hitBrick instanceof Brick) {
                double ballCenterX = ball.getX() + BALL_RADIUS;
                double ballCenterY = ball.getY() + BALL_RADIUS;
                double brickX = hitBrick.getX();
                double brickY = hitBrick.getY();
                double brickWidth = hitBrick.getWidth();
                double brickHeight = hitBrick.getHeight();
                double brickCenterX = brickX + brickWidth / 2;
                double brickCenterY = brickY + brickHeight / 2;
                double deltaX = Math.abs(ballCenterX - brickCenterX);
                double deltaY = Math.abs(ballCenterY - brickCenterY);
                if (deltaX * (brickHeight / brickWidth) > deltaY) {
                    xSpeedBall *= -1;
                } else {
                    ySpeedBall *= -1;
                }
                remove(hitBrick);
                currentPoints+=100;
            }
        }
        ball.move(xSpeedBall, ySpeedBall);
        ball.move(xSpeedBall, ySpeedBall);
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
            if (obj != null && obj != ball) {
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

    //створює перший рівень
    private void level1(){
        countOfLives = 3;
        needPointsToWin = 5000;
        currentPoints = 0;
        xSpeedBall = 1;
        ySpeedBall = -1;
        removeAll();
        paddleCreate(PADDLE_WIDTH, PADDLE_HEIGHT, PADDLE_Y_OFFSET);
        createWalls();
        ball();
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
        countOfLives = 3;
        needPointsToWin = 5000;
        currentPoints = 0;
        xSpeedBall = 1;
        ySpeedBall = -1;
        removeAll();
        paddleCreate(PADDLE_WIDTH, PADDLE_HEIGHT, PADDLE_Y_OFFSET);
        createWalls();
        ball();
        //
        Kerel kerel = new Kerel();
        add(kerel);
        kerel.randomMoving();
        initMouseListeners();
        gameUpdate();
    }
}


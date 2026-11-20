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
    public static final int NBRICK_ROWS = 10;

    /** Separation between bricks */
    public static final int BRICK_SEP = 4;

    /** Width of a brick */
    public static final int BRICK_WIDTH =
            (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

    /** Height of a brick */
    public static final int BRICK_HEIGHT = 8;

    /** Radius of the ball in pixels */
    private static final int BALL_RADIUS = 10;

    /** Offset of the top brick row from the top */
    public static final int BRICK_Y_OFFSET = 70;

    /** Number of turns */
    private static final int NTURNS = 3;

    /* Method: run() */
    /** Runs the Breakout program. */
    public GOval ball;
    public GRect paddle;
    public GRect leftWall;
    public GRect rightWall;
    public GRect upperWall;
    public double xSpeedBall = 1;
    public double ySpeedBall = -1;
    public static void main(String[] args) {new Breakout().start(args);}
    public void run() {
        setSize(APPLICATION_WIDTH, APPLICATION_HEIGHT);
        paddleCreate(PADDLE_WIDTH, PADDLE_HEIGHT, PADDLE_Y_OFFSET);
        createWalls();
        ball();
        level1();
        addMouseListeners();
        gameUpdate();

    }
    private void gameUpdate(){
        while (true){
            ballMoving();
            pause(10);
        }
    }
    private void paddleCreate(int paddleX, int paddleY, int offset){
        paddle = new GRect((APPLICATION_WIDTH/2)-30, APPLICATION_HEIGHT-offset, paddleX, paddleY);
        add(paddle);
    }
    public void mouseMoved(MouseEvent e) {
        int x = e.getX();
        if(x>10 && x<APPLICATION_WIDTH-PADDLE_WIDTH-10) {
            paddle.setLocation(x, APPLICATION_HEIGHT-PADDLE_Y_OFFSET);
        }
    }
    private void createWalls(){
        leftWall = new GRect(-10, 0, 10, APPLICATION_HEIGHT);
        rightWall = new GRect(APPLICATION_WIDTH, 0, 10, APPLICATION_HEIGHT);
        upperWall = new GRect(0, 0, APPLICATION_WIDTH, 15);
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
    private void ball(){
        ball = new GOval((APPLICATION_WIDTH/2)-BALL_RADIUS*2, APPLICATION_HEIGHT-PADDLE_Y_OFFSET-BALL_RADIUS-20, BALL_RADIUS, BALL_RADIUS);
        add(ball);
    }
    private void ballMoving() {
        GObject collider = getCollidingObject();
        if (collider == paddle) {
            double paddleCenterX = paddle.getX() + PADDLE_WIDTH / 2;
            double ballCenterX = ball.getX() + BALL_RADIUS;
            double hitOffset = ballCenterX - paddleCenterX;
            xSpeedBall = hitOffset * 0.15;
            ySpeedBall *= -1;

        } else if (collider == upperWall) {
            ySpeedBall *= -1;
        } else if (collider == leftWall) {
            xSpeedBall *= -1;
        } else if (collider == rightWall) {
            xSpeedBall *= -1;
        } else if (collider != null) {
            GObject hitBrick = collider;

            // Якщо collider — це внутрішня лінія, піднімаємося до самої цеглинки (GCompound)
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
                    xSpeedBall *= -1; // Рикошет по горизонталі
                } else {
                    ySpeedBall *= -1; // Рикошет по вертикалі
                }
                remove(hitBrick);
            }
        }

        ball.move(xSpeedBall, ySpeedBall);
    }
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
    private void lineOfBricks(int startY){
        int startX = 0;
        for(int i = 0; i<Breakout.NBRICKS_PER_ROW; i++){
            Brick brick = new Brick(startX, startY);
            add(brick);
            startX+=Breakout.BRICK_WIDTH + Breakout.BRICK_SEP;
        }
    }
    private void level1(){
        int startY = Breakout.BRICK_Y_OFFSET;
        for(int i = 0; i<NBRICK_ROWS; i++){
            lineOfBricks(startY);
            startY+=(BRICK_HEIGHT+BRICK_SEP);
        }
    }
}


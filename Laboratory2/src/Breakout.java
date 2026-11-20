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
    private static final int NBRICKS_PER_ROW = 10;

    /** Number of rows of bricks */
    private static final int NBRICK_ROWS = 10;

    /** Separation between bricks */
    private static final int BRICK_SEP = 4;

    /** Width of a brick */
    private static final int BRICK_WIDTH =
            (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

    /** Height of a brick */
    private static final int BRICK_HEIGHT = 8;

    /** Radius of the ball in pixels */
    private static final int BALL_RADIUS = 10;

    /** Offset of the top brick row from the top */
    private static final int BRICK_Y_OFFSET = 70;

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
    public void run() {
        setSize(APPLICATION_WIDTH, APPLICATION_HEIGHT);
        paddleCreate(PADDLE_WIDTH, PADDLE_HEIGHT, PADDLE_Y_OFFSET);
        createWalls();
        ball();
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
        leftWall = new GRect(0, 0, 10, APPLICATION_HEIGHT);
        rightWall = new GRect(APPLICATION_WIDTH-10, 0, 10, APPLICATION_HEIGHT);
        upperWall = new GRect(0, 0, APPLICATION_WIDTH, 10);
        leftWall.setColor(Color.black);
        leftWall.setFilled(true);
        rightWall.setColor(Color.black);
        rightWall.setFilled(true);
        upperWall.setColor(Color.black);
        upperWall.setFilled(true);
        add(leftWall);
        add(rightWall);
        add(upperWall);
    }
    private void ball(){
        ball = new GOval((APPLICATION_WIDTH/2)-BALL_RADIUS*2, APPLICATION_HEIGHT-PADDLE_Y_OFFSET-BALL_RADIUS-20, BALL_RADIUS, BALL_RADIUS);
        add(ball);
    }
    private void ballMoving(){
        GObject collider = getCollidingObject();
        if (collider==paddle) {
            double paddleCenterX = paddle.getX() + PADDLE_WIDTH / 2;
            double ballCenterX = ball.getX() + BALL_RADIUS;
            double hitOffset = ballCenterX - paddleCenterX;
            xSpeedBall = hitOffset*0.15;
            ySpeedBall*=-1;
        }
        else if (collider==upperWall){
            ySpeedBall*=-1;
        }
        else if (collider==leftWall){
            xSpeedBall*=-1;
        }
        else if (collider==rightWall){
            xSpeedBall*=-1;
        }
        ball.move(xSpeedBall, ySpeedBall);

    }
    private GObject getCollidingObject() {
        double ballRadius = BALL_RADIUS; // радіус м'яча
        double ballCenterX = ball.getX() + ballRadius;
        double ballcenterY = ball.getY() + ballRadius;
        for (int angle = 0; angle < 360; angle += 45) {
            double rad = Math.toRadians(angle);
            double checkX = ballCenterX + ballRadius * Math.cos(rad);
            double checkY = ballcenterY + ballRadius * Math.sin(rad);
            GObject obj = getElementAt(checkX, checkY);
            if (obj != null) {
                return obj;
            }
        }
        return null;
    }

}

import acm.graphics.GCompound;
import acm.graphics.GImage;
import acm.graphics.GObject;
import acm.util.RandomGenerator;


public class Kerel extends GCompound {
    GImage kerel;
    GImage bomb;
    boolean right = true;
    public Kerel() {
        kerel = new GImage("kerelOrdinarR.png");
        kerel.setSize(40, 40);
        add(kerel);
        kerel.setLocation(Breakout.APPLICATION_WIDTH/2, 40);
    }
    public void turnRight(){
        kerel.setImage("kerelOrdinarR.png");
        kerel.setSize(40, 40);
        right = true;
    }
    public void turnLeft(){
        kerel.setImage("kerelOrdinarL.png");
        kerel.setSize(40, 40);
        right = false;
    }
    public void randomMoving(){
        double moveTo = rgen.nextInt(0, Breakout.APPLICATION_WIDTH-60);
        double moveFrom = kerel.getX();
        if(moveFrom<moveTo){
            turnRight();
        }
        else{
            turnLeft();
        }
        double distance = moveTo-moveFrom;
        for(int i = 0; i<3; i++){
            kerel.move(distance/3, 0);
            pause(60);
        }
    }
    public void prepareToAttack() {
        if(right) {
            kerel.setImage("kerelPoveringR.png");
        }
        else{
            kerel.setImage("kerelPoveringL.png");
        }
        int shakes = 6;
        double offset = 4;
        for (int i = 0; i < shakes; i++) {
            kerel.move(offset, 0);
            pause(50);
            kerel.move(-offset * 2, 0);
            pause(50);
            kerel.move(offset, 0);
            pause(50);
        }
    }
    public void moveToX(double targetX) {
        double from = kerel.getX();
        if (from < targetX) {
            turnRight();
        } else {
            turnLeft();
        }
        double distance = targetX - from;
        for (int i = 0; i < 5; i++) {
            kerel.move(distance / 5, 0);
            pause(40);
        }
    }
    public double centerX() {
        return kerel.getX() + kerel.getWidth() / 2;
    }

    public void moveBy(double dx) {
        if (dx > 0.5 && !right) {
            turnRight();
        } else if (dx < -0.5 && right) {
            turnLeft();
        }
        kerel.move(dx, 0);
    }

    public void resetPose() {
        if (right) {
            turnRight();
        } else {
            turnLeft();
        }
    }public double bottomY() {
        return kerel.getY() + kerel.getHeight();
    }
    private RandomGenerator rgen = RandomGenerator.getInstance();
}
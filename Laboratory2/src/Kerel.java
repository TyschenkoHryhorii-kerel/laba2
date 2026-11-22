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
    private RandomGenerator rgen = RandomGenerator.getInstance();
}
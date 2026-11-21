import acm.graphics.GCompound;
import acm.graphics.GImage;
import acm.util.RandomGenerator;


public class Kerel extends GCompound {
    GImage kerel;
    boolean right = true;
    public Kerel() {
        kerel = new GImage("kerelOrdinarR.png");
        kerel.setSize(60, 60);
        add(kerel);
        kerel.setLocation(Breakout.APPLICATION_WIDTH/2, 40);
    }
    public void turnRight(){
        kerel.setImage("kerelOrdinarR.png");
        kerel.setSize(60, 60);
        right = true;
    }
    public void turnLeft(){
        kerel.setImage("kerelOrdinarL.png");
        kerel.setSize(60, 60);
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
        for(int i = 0; i<10; i++){
            kerel.move(distance/10, 0);
            pause(60);
        }
    }
    private RandomGenerator rgen = RandomGenerator.getInstance();
}
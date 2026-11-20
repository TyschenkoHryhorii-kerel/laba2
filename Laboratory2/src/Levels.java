import acm.graphics.GCompound;

public class Levels extends GCompound {
    public Levels (int number){
        switch (number){
            case 1:{
                lineOfBricks();

            break;
            }

            case 2:{
                break;
            }

            case 3:{
                break;
            }
        }
    }
    private void lineOfBricks(){
        int startX = 20;
        for(int i = 0; i<Breakout.NBRICKS_PER_ROW; i++){
            Brick brick = new Brick(startX, Breakout.APPLICATION_HEIGHT-Breakout.BRICK_Y_OFFSET);
            add(brick);
            startX+=Breakout.BRICK_WIDTH + Breakout.BRICK_SEP;
        }
    }
}

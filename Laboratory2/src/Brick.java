import acm.graphics.GCompound;
import acm.graphics.GLine;
import acm.graphics.GRect;
import acm.program.GraphicsProgram;
import acm.util.RandomGenerator;

import java.awt.*;

public class Brick extends GCompound {
    private int width = Breakout.BRICK_WIDTH;
    private int height = Breakout.BRICK_HEIGHT;
    public GLine lineLeft;
    public GLine lineRight;
    public GLine lineUpper;
    public GLine lineDown;
        public Brick(int x, int y) {
            this.lineLeft = new GLine(x, y, x, y + height);
            this.lineRight = new GLine(x + width, y, x + width, y + height);
            this.lineUpper = new GLine(x, y, x + width, y);
            this.lineDown = new GLine(x, y + height, x + width, y + height);
            GRect inside = new GRect(x, y, width, height);
            inside.setFillColor(rgen.nextColor());
            add(this.lineLeft);
            add(this.lineRight);
            add(this.lineUpper);
            add(this.lineDown);
        }
    private RandomGenerator rgen = RandomGenerator.getInstance();
}


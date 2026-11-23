
import acm.graphics.GCompound;
import acm.graphics.GLine;
import acm.graphics.GRect;
import acm.util.RandomGenerator;

import java.awt.*;

public class Brick extends GCompound {
    private int width = Breakout.BRICK_WIDTH;
    private int height = Breakout.BRICK_HEIGHT;
    public GLine lineLeft;
    public GLine lineRight;
    public GLine lineUpper;
    public GLine lineDown;
    private RandomGenerator rgen = RandomGenerator.getInstance();

    public Brick(int x, int y) {
        this.lineLeft = new GLine(0, 0, 0, height);
        this.lineRight = new GLine(width, 0, width, height);
        this.lineUpper = new GLine(0, 0, width, 0);
        this.lineDown = new GLine(0, height, width, height);

        GRect inside = new GRect(0, 0, width, height);
        inside.setFillColor(rgen.nextColor());
        inside.setFilled(true);

        add(inside);
        add(this.lineLeft);
        add(this.lineRight);
        add(this.lineUpper);
        add(this.lineDown);

        setLocation(x, y);
    }
}

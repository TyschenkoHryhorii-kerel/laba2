import acm.graphics.GCompound;
import acm.graphics.GLine;
import acm.graphics.GRect;
import acm.util.RandomGenerator;
import java.awt.*;

/** Клас цеглини з гармонійною палітрою кольорів і легким бліком. */
public class Brick extends GCompound {
    private static final Color[] PALETTE = {
            new Color(231, 76, 60), new Color(243, 156, 18), new Color(46, 204, 113),
            new Color(52, 152, 219), new Color(155, 89, 182), new Color(26, 188, 156)
    };

    private int width = Breakout.BRICK_WIDTH;
    private int height = Breakout.BRICK_HEIGHT;
    public GLine lineLeft;
    public GLine lineRight;
    public GLine lineUpper;
    public GLine lineDown;
    private RandomGenerator rgen = RandomGenerator.getInstance();

    /** Створює цеглину в координатах (x, y) із випадковим кольором палітри. */
    public Brick(int x, int y) {
        Color fill = PALETTE[rgen.nextInt(0, PALETTE.length - 1)];
        Color border = fill.darker();

        this.lineLeft = new GLine(0, 0, 0, height);
        this.lineRight = new GLine(width, 0, width, height);
        this.lineUpper = new GLine(0, 0, width, 0);
        this.lineDown = new GLine(0, height, width, height);
        this.lineLeft.setColor(border);
        this.lineRight.setColor(border);
        this.lineUpper.setColor(border);
        this.lineDown.setColor(border);

        GRect inside = new GRect(0, 0, width, height);
        inside.setFillColor(fill);
        inside.setColor(border);
        inside.setFilled(true);

        GRect shine = new GRect(1, 1, width - 2, 2);
        shine.setFilled(true);
        shine.setFillColor(new Color(255, 255, 255, 85));
        shine.setColor(new Color(255, 255, 255, 85));

        add(inside);
        add(shine);
        add(this.lineLeft);
        add(this.lineRight);
        add(this.lineUpper);
        add(this.lineDown);
        setLocation(x, y);
    }
}

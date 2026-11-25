import acm.graphics.GCompound;
import acm.graphics.GLabel;
import acm.graphics.GRoundRect;
import java.awt.Color;

/** Падаючий бонус на 2-му рівні у вигляді кольорової капсули з підписом. */
public class Bonus extends GCompound {
    public static final double BONUS_WIDTH = 48;
    public static final double BONUS_HEIGHT = 20;

    private final BonusType type;

    /** Створює капсулу бонусу заданого типу у координатах (x, y). */
    public Bonus(BonusType type, double x, double y) {
        this.type = type;

        GRoundRect body = new GRoundRect(0, 0, BONUS_WIDTH, BONUS_HEIGHT, 12, 12);
        body.setFilled(true);
        body.setFillColor(type.getFillColor());
        body.setColor(type.getBorderColor());
        add(body);

        GLabel label = new GLabel(type.getLabel());
        label.setFont("SansSerif-BOLD-11");
        label.setColor(Color.WHITE);
        add(label, (BONUS_WIDTH - label.getWidth()) / 2.0, (BONUS_HEIGHT + label.getAscent()) / 2.0 - 1.5);

        setLocation(x, y);
    }

    /** Повертає тип бонусу. */
    public BonusType getType() { return type; }

    /** Перевіряє, чи бонус корисний. */
    public boolean isPositive() { return type.isPositive(); }

    /** Зміщує бонус вертикально вниз на dy пікселів. */
    public void fall(double dy) { move(0, dy); }
}

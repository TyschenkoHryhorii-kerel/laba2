import acm.graphics.GCompound;
import acm.graphics.GLabel;
import acm.graphics.GRoundRect;
import java.awt.Color;

/** Падаючий бонус на 2-му рівні у вигляді кольорової капсули з підписом. */
public class Bonus extends GCompound {
    public static final double BONUS_WIDTH = 48;
    public static final double BONUS_HEIGHT = 20;

    private BonusType type;

    /** Створює капсулу бонусу заданого типу у координатах (x, y). */
    public Bonus(BonusType type, double x, double y) {
        this.type = type;

        GRoundRect body = new GRoundRect(0, 0, BONUS_WIDTH, BONUS_HEIGHT, 12, 12);
        body.setFilled(true);
        body.setFillColor(getColorForType(type));
        body.setColor(getBorderForType(type));
        add(body);

        GLabel label = new GLabel(getLabelForType(type));
        label.setFont("SansSerif-BOLD-11");
        label.setColor(Color.WHITE);
        add(label, (BONUS_WIDTH - label.getWidth()) / 2.0, (BONUS_HEIGHT + label.getAscent()) / 2.0 - 1.5);

        setLocation(x, y);
    }

    /** Повертає тип бонусу. */
    public BonusType getType() {
        return type;
    }

    /** Перевіряє, чи бонус корисний для гравця. */
    public boolean isPositive() {
        return type == BonusType.EXTRA_LIFE
                || type == BonusType.EXPAND_PADDLE
                || type == BonusType.SLOW_BALL;
    }

    /** Зміщує бонус вертикально вниз на dy пікселів. */
    public void fall(double dy) {
        move(0, dy);
    }

    /** Повертає підпис для заданого типу бонусу. */
    public static String getLabelForType(BonusType type) {
        String text = "";
        switch (type) {
            case EXTRA_LIFE:
                text = "+1♥";
                break;
            case EXPAND_PADDLE:
                text = "WIDE";
                break;
            case SLOW_BALL:
                text = "SLOW";
                break;
            case SHRINK_PADDLE:
                text = "SHORT";
                break;
            case FAST_BALL:
                text = "FAST";
                break;
            default:
                break;
        }
        return text;
    }

    /** Повертає колір заливки для заданого типу бонусу. */
    public static Color getColorForType(BonusType type) {
        Color color = new Color(52, 152, 219);
        switch (type) {
            case EXTRA_LIFE:
                color = new Color(46, 204, 113);
                break;
            case EXPAND_PADDLE:
                color = new Color(52, 152, 219);
                break;
            case SLOW_BALL:
                color = new Color(241, 196, 15);
                break;
            case SHRINK_PADDLE:
                color = new Color(231, 76, 60);
                break;
            case FAST_BALL:
                color = new Color(155, 89, 182);
                break;
            default:
                break;
        }
        return color;
    }

    /** Повертає колір рамки для заданого типу бонусу. */
    public static Color getBorderForType(BonusType type) {
        Color color = Color.WHITE;
        switch (type) {
            case EXTRA_LIFE:
                color = new Color(180, 255, 205);
                break;
            case EXPAND_PADDLE:
                color = new Color(174, 224, 255);
                break;
            case SLOW_BALL:
                color = new Color(255, 240, 165);
                break;
            case SHRINK_PADDLE:
                color = new Color(255, 185, 178);
                break;
            case FAST_BALL:
                color = new Color(225, 185, 245);
                break;
            default:
                break;
        }
        return color;
    }
}

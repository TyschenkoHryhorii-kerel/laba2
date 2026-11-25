import java.awt.Color;

/** Типи падаючих бонусів на 2-му рівні гри (корисні та ускладнюючі). */
public enum BonusType {
    EXTRA_LIFE("+1♥", new Color(46, 204, 113), new Color(180, 255, 205), true),
    EXPAND_PADDLE("WIDE", new Color(52, 152, 219), new Color(174, 224, 255), true),
    SLOW_BALL("SLOW", new Color(241, 196, 15), new Color(255, 240, 165), true),
    SHRINK_PADDLE("SHORT", new Color(231, 76, 60), new Color(255, 185, 178), false),
    FAST_BALL("FAST", new Color(155, 89, 182), new Color(225, 185, 245), false);

    private final String label;
    private final Color fillColor;
    private final Color borderColor;
    private final boolean positive;

    /** Створює тип бонусу з підписом, кольорами та ознакою корисності. */
    BonusType(String label, Color fillColor, Color borderColor, boolean positive) {
        this.label = label;
        this.fillColor = fillColor;
        this.borderColor = borderColor;
        this.positive = positive;
    }

    /** Повертає підпис бонусу. */
    public String getLabel() { return label; }

    /** Повертає колір заливки капсули. */
    public Color getFillColor() { return fillColor; }

    /** Повертає колір рамки капсули. */
    public Color getBorderColor() { return borderColor; }

    /** Повертає true, якщо бонус корисний. */
    public boolean isPositive() { return positive; }
}

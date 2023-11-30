package windows;

import java.awt.*;

@SuppressWarnings("unused")
public class UnitNew {
    public static final int unit = getUnit();
    public static Font normalFont(int size) {
        return new Font("微软雅黑", Font.BOLD, size);
    }
    public static Dimension normalSize(double width, double height) {
        return new Dimension((int) (width * unit), (int) (height * unit));
    }
    public static Point normalPoint(double x, double y) {
        return new Point((int) (x * unit), (int) (y * unit));
    }
    private static int getUnit() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int height = screen.height; // 1600
        int width = screen.width; // 2560
        return height >> 5;
    }
}

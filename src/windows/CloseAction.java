package windows;

import java.awt.*;
import java.util.function.Consumer;

/**
 * 预定义的窗口关闭行为
 * 利用枚举类来实现方便管理和添加不同的关闭方法
 */
public enum CloseAction {
    DISPOSE(Window::dispose),    //调用窗口的dispose方法，关闭窗口
    EXIT(window -> System.exit(0));   //调用窗口的退出方法,退出整个程序

    //action的属性值为以上值
    private final Consumer<AbstractWindow> action;
    //构造函数
    CloseAction(Consumer<AbstractWindow> action){
        this.action = action;
    }
    //实际是调用action
    public void doAction(AbstractWindow window){
        action.accept(window);
    }
}

package windows;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * AbstractWindow 是当前项目中，所有窗口的顶层抽象类，继承自JFrame类。
 * - 窗口内只需要编写组件部分，仅包括UI相关内容。
 */
public abstract class AbstractWindow extends JFrame {
    //窗口的默认关闭方法
    private final CloseAction closeAction=CloseAction.DISPOSE;
    //组件哈希表 用于获取组件
    private final HashMap<String, Component> componentMap=new HashMap<>();
    private final Dimension screenSize;

    protected AbstractWindow(String title, Dimension defaultSize, boolean resizeable,AbstractWindow parentWindow){
        this.setTitle(title);
        //获取屏幕大小
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int) Math.min(screenSize.getWidth(), defaultSize.getWidth()),
                (int) Math.min(screenSize.getHeight(), defaultSize.getHeight()));
        //移动到屏幕中心
        if(parentWindow ==null) {
           setCenter(defaultSize);
        }else {
            this.setLocation(parentWindow.getLocation());
        }

        this.setResizable(resizeable);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(onClose()){
                    AbstractWindow.this.closeWindow();
                }
            }
        });
        this.setVisible(true);
    }
    private Point calculateCenter(double outerWidth, double outerHeight, double innerWidth, double innerHeight){
        int x=(int) (outerWidth-innerWidth)/2;
        int y=(int) (outerHeight-innerHeight)/2;
        return new Point(x,y);
    }
    protected void setCenter(Dimension defaultSize){
        Point screenCenter = calculateCenter(screenSize.getWidth(), screenSize.getHeight(),
                defaultSize.getWidth(), defaultSize.getHeight());
        this.setLocation(screenCenter);
    }
    //此处用来添加窗口在关闭时做的事情
    protected abstract boolean onClose();
    //初始化窗口，添加组件在这里
    protected abstract void initWindow();
    protected void closeWindow(){
        this.closeAction.doAction(this);
    }
    @SuppressWarnings("unchecked")
    public  <T extends Component> T getComponent(String componentName){
        return (T) componentMap.get(componentName);
    }
    /**
     *以下为封装好的组件添加函数
     */
    protected <T extends Component> void addComponent(Container target, String name, T component, Consumer<T> consumer){
        if(consumer!=null)
            consumer.accept(component);
        this.componentMap.put(name,component);
        target.add(component);
    }
    protected <T extends Component> void addComponent(Container target, String name, T component,Object constrains, Consumer<T> consumer){
        if(consumer!=null)
            consumer.accept(component);
        this.componentMap.put(name,component);
        target.add(component,constrains);
    }
    @SuppressWarnings("all")
    protected <T extends Component> void addComponentScrollable(Container target, String name, T component, Object constraints, Consumer<T> consumer){
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setLayout(new ScrollPaneLayout.UIResource());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setViewport(new JViewport());
        scrollPane.setVerticalScrollBar(scrollPane.createVerticalScrollBar());
        scrollPane.setHorizontalScrollBar(scrollPane.createHorizontalScrollBar());
        if(consumer != null)
            consumer.accept(component);
        scrollPane.setSize(component.getSize());
        scrollPane.setPreferredSize(component.getPreferredSize());
        scrollPane.setLocation(component.getLocation());
        this.componentMap.put(name, component);
        scrollPane.setViewportView(component);
        target.add(scrollPane, constraints);
    }
}
package windows;

import web.Player;

import javax.swing.*;

import java.awt.*;
import java.io.IOException;

import static windows.UnitNew.*;
public class WelcomeWindow extends AbstractWindow{
    static ImageIcon icon=new ImageIcon("image/backGround.png");
    JLabel backGround=new JLabel(icon);
    ImageIcon title=new ImageIcon("image/五子棋.png");
    JLabel titleLabel =new JLabel(title);
    public WelcomeWindow(AbstractWindow parentWindow) {
        super("五子棋",new Dimension(icon.getIconWidth(),icon.getIconHeight()),false,parentWindow);
        JPanel panel =(JPanel) getContentPane();
        panel.setOpaque(false);
        panel.add(backGround);
        getLayeredPane().add(backGround, Integer.valueOf(Integer.MIN_VALUE));//再将JLabel设置为最底层，然后再在JLabel上添加组件
        backGround.setBounds(0, 0, icon.getIconWidth(), icon.getIconHeight());//将label的大小设置为图片的大小
        panel.setLayout(null);//使用绝对布局
        initWindow();
    }

    @Override
    protected boolean onClose() {
        CloseAction.EXIT.doAction(this);
        return false;
    }

    @Override
    protected void initWindow() {
        addComponent(this,"标题",titleLabel,label -> {
            label.setSize(title.getIconWidth(),title.getIconHeight());
            label.setLocation(normalPoint(3,2));
        });
        addComponent(this,"人人对弈",new JButton("人人对弈"),button -> {
            button.setFont(normalFont(20));
            button.setSize(normalSize(5,2.5));
            button.setLocation(normalPoint(4,7));
            button.addActionListener(e ->goToPvP());
        });
        addComponent(this,"人机对弈",new JButton("人机对弈"),button -> {
            button.setFont(normalFont(20));
            button.setSize(normalSize(5,2.5));
            button.setLocation(normalPoint(4,10));
            button.addActionListener(e ->goToPvE());
        });
    }
    public void goToPvP(){
        try {
            String host=JOptionPane.showInputDialog(this,"请输入主机的服务器的地址","连接服务器",JOptionPane.PLAIN_MESSAGE);
            if (host==null)
                return;
            new Player(this, host);
            closeWindow();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,"该服务器不存在或未开启","连接失败",JOptionPane.ERROR_MESSAGE);
        }

    }
    public void goToPvE(){
        new PveWindow(this);
        closeWindow();
    }
}

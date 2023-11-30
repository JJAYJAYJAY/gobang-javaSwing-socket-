package windows;

import eneity.Message;
import web.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import static windows.UnitNew.*;

public class RoomWindow extends AbstractWindow {
    //对应的player
    private final Player player;
    //房间列表
    public Set<String> roomSet;
    public RoomWindow(Player player,AbstractWindow parentWindow) {
        super("搜索房间", normalSize(30, 22), false,parentWindow);
        this.player = player;
        initWindow();
    }
    @Override
    protected boolean onClose() {
        player.close();
        CloseAction.EXIT.doAction(this);
        return false;
    }
    @Override
    @SuppressWarnings("all")
    protected void initWindow() {
        setLayout(null);
        addComponent(this, "房间列表面板", new JPanel(), panel -> {
            panel.setLayout(new BorderLayout());
            panel.setLocation(normalPoint(0,0));
            panel.setSize(normalSize(30,15));
            addComponent(panel, "标签", new JLabel("房间列表:"),BorderLayout.NORTH, label -> {
                label.setFont(normalFont(13));
                label.setPreferredSize(normalSize(30,3));
                label.setFont(normalFont(20));
            });
            addComponentScrollable(panel,"房间列表",new JList(),BorderLayout.CENTER,list->{
                list.setFont(normalFont(20));
            });
        });

        addComponent(this, "按钮面板", new JPanel(new FlowLayout()),panel -> {
            panel.setLocation(normalPoint(0,16));
            panel.setSize(normalSize(30,6));
            addComponent(panel,"进入",new JButton("进入"),button ->{
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if(((JList) getComponent("房间列表")).isSelectionEmpty())
                            return;
                        String name=((JList) getComponent("房间列表")).getSelectedValue().toString();
                        player.send(new Message("enter",name));
                        goToPvpWindow(player);
                        player.color=2;
                    }
                });
                button.setPreferredSize(normalSize(5,3));
                button.setFont(normalFont(30));
            });

            addComponent(panel,"创建房间",new JButton("创建房间"),button ->{
                button.addActionListener(new ActionListener(){
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String name = JOptionPane.showInputDialog(RoomWindow.this, "请输入房间名","创建房间",JOptionPane.PLAIN_MESSAGE);
                        if(name==null)
                            return;
                        while (name.equals("")){
                            JOptionPane.showMessageDialog(RoomWindow.this, "房间名不能为空！","警告",JOptionPane.WARNING_MESSAGE);
                            name=JOptionPane.showInputDialog(RoomWindow.this, "请输入房间名","创建用户",JOptionPane.PLAIN_MESSAGE);
                        }
                        player.send(new Message("addRoom",name));
                        player.color=1;
                    }
                });
                button.setPreferredSize(normalSize(5,3));
                button.setFont(normalFont(30));
            });

            addComponent(panel,"刷新",new JButton("刷新"),button -> {
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        player.send(new Message("getRoomList", null));
                    }
                });
                button.setPreferredSize(normalSize(5,3));
                button.setFont(normalFont(30));
            });

            addComponent(panel,"返回",new JButton("返回"),button -> {
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        player.close();
                        new WelcomeWindow(RoomWindow.this);
                        closeWindow();
                    }
                });
                button.setPreferredSize(normalSize(5,3));
                button.setFont(normalFont(30));
            });

        });
    }
    public void goToPvpWindow(Player player){
        player.setPvpWindow(new PvpWindow(player,this));
        closeWindow();
    }
}

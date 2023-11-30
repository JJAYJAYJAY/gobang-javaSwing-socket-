package windows;

import eneity.CheckerBoard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static windows.UnitNew.*;

public class PveWindow extends AbstractWindow{

    ButtonGroup group=new ButtonGroup();
    public PveWindow(AbstractWindow parentWindow) {
        super("五子棋-人机对弈", normalSize(30,22), false,parentWindow);
        initWindow();
    }

    @Override
    protected boolean onClose() {
        CloseAction.EXIT.doAction(this);
        return false;
    }
    @Override
    protected void initWindow() {
        addComponent(this,"分割板",new JSplitPane(JSplitPane.HORIZONTAL_SPLIT),jSplitPane -> {
            jSplitPane.setDividerLocation(21*unit);
            jSplitPane.setEnabled(false);
            addComponent(jSplitPane,"棋盘",new CheckerBoard(this),JSplitPane.LEFT,checkerBoard -> checkerBoard.setLocation(normalPoint(0,0)));
            addComponent(jSplitPane,"信息面板",new JPanel(),JSplitPane.RIGHT,panel -> {

                GridLayout gridLayout=new GridLayout(7,1);
                gridLayout.setVgap(unit);
                panel.setLayout(gridLayout);

                addComponent(panel,"信息显示",new JLabel("等待游戏开始"),label -> {
                    label.setFont(normalFont(30));
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                });

                addComponent(panel,"开始游戏",new JButton("开始游戏"),button -> {
                    button.setFont(normalFont(30));
                    button.addActionListener(e -> {
                        ((CheckerBoard) getComponent("棋盘")).restart();
                        ((CheckerBoard) getComponent("棋盘")).setPve();
                        button.setText("重新开始");
                        if(((JRadioButton) getComponent("人机先手")).isSelected()){
                            ((CheckerBoard) getComponent("棋盘")).blackFirst();
                            ((CheckerBoard) getComponent("棋盘")).ai.aiPlayChess();
                        }
                        ((JLabel)getComponent("信息显示")).setText("<html><p align=\"center\">游戏开始，请玩家落子<html>");
                        getComponent("悔棋").setEnabled(true);
                        getComponent("放弃").setEnabled(true);
                        button.setEnabled(false);
                    });
                });

                addComponent(panel,"多选小面板",new JPanel(new GridLayout(1,2)),radioPanel->{
                    addComponent(radioPanel,"玩家先手",new JRadioButton("玩家先手"),button -> {
                        group.add(button);
                        button.setSelected(true);
                        button.setFont(normalFont(15));
                    });
                    addComponent(radioPanel,"人机先手",new JRadioButton("人机先手"),button -> {
                        group.add(button);
                        button.setFont(normalFont(15));
                    });
                });

                addComponent(panel,"悔棋",new JButton("悔棋"),button -> {
                    button.setFont(normalFont(30));
                    button.addActionListener(e -> ((CheckerBoard)getComponent("棋盘")).playChessService.repent( ((CheckerBoard) getComponent("棋盘")).count%2+1));
                    button.setEnabled(false);
                });

                addComponent(panel,"放弃",new JButton("放弃"),button -> {
                    button.setFont(normalFont(30));
                    button.addActionListener(e -> {
                        ((CheckerBoard) getComponent("棋盘")).playChessService.giveUp();
                        getComponent("开始游戏").setEnabled(true);
                    });
                    button.setEnabled(false);
                });
                addComponent(panel,"返回",new JButton("返回"),button -> {
                    button.setLocation(normalPoint(21,0));
                    button.setSize(normalSize(7,3));
                    button.setFont(normalFont(30));
                    button.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            goBack();
                        }
                    });
                });
            });
        });
    }
    public void goBack(){
        new WelcomeWindow(this);
        closeWindow();
    }
}
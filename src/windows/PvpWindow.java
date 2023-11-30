package windows;

import eneity.CheckerBoard;
import eneity.Message;
import web.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static windows.UnitNew.*;

public class PvpWindow extends AbstractWindow{
    //窗口对应的客户端
    private final Player player;
    //对应的棋盘
    public CheckerBoard checkerBoard;
    public PvpWindow(Player player,AbstractWindow parentWindow) {
        super("五子棋-人人对弈", normalSize(30,22), false,parentWindow);
        this.player=player;
        initWindow();
    }
    @Override
    protected boolean onClose() {
        player.close();
        CloseAction.EXIT.doAction(this);
        return false;
    }

    @Override
    protected void initWindow() {
        addComponent(this,"分割板",new JSplitPane(JSplitPane.HORIZONTAL_SPLIT), jSplitPane -> {
            jSplitPane.setDividerLocation(21*unit);
            jSplitPane.setEnabled(false);
            addComponent(jSplitPane,"棋盘",new CheckerBoard(this,player),JSplitPane.LEFT,checkerBoard -> {
                checkerBoard.setLocation(normalPoint(0,0));
                this.checkerBoard=checkerBoard;
            });

            addComponent(jSplitPane,"信息面板",new JPanel(),JSplitPane.RIGHT,panel -> {
                GridLayout gridLayout=new GridLayout(7,1);
                gridLayout.setVgap(unit);
                panel.setLayout(gridLayout);

                addComponent(panel,"信息显示",new JLabel("对方未准备"),label -> {
                    label.setFont(normalFont(30));
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                });

                addComponent(panel,"开始游戏",new JToggleButton("未准备"),button -> {
                    button.setFont(normalFont(30));
                    button.addActionListener(e -> {
                        if(button.isSelected()){
                            button.setText("已准备");
                            player.send(new Message("thisReady"));
                        }else {
                            button.setText("未准备");
                            player.send(new Message("thisNotReady"));
                        }
                    });
                });

                addComponent(panel,"悔棋",new JButton("悔棋"),button -> {
                    button.setFont(normalFont(30));
                    button.addActionListener(e -> {
                        if(!checkerBoard.getHistory().empty()) {
                            player.send(new Message("wantToRepent",player.color));
                            ((JLabel) getComponent("信息显示")).setText("等待对方同意悔棋");
                            checkerBoard.removeListeners();
                            getComponent("悔棋").setEnabled(false);
                            getComponent("放弃").setEnabled(false);
                        }
                    });
                    button.setEnabled(false);
                });

                addComponent(panel,"放弃",new JButton("放弃"),button -> {
                    button.setFont(normalFont(30));
                    button.addActionListener(e -> {
                        ((CheckerBoard) getComponent("棋盘")).playChessService.giveUp();
                        player.send(new Message("giveUp",1));
                        getComponent("开始游戏").setEnabled(true);
                        ((JLabel) getComponent("信息显示")).setText("对方获胜");
                        ((JToggleButton)getComponent("开始游戏")).setText("再来一局");
                        ((JToggleButton)getComponent("开始游戏")).setSelected(false);
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
                            player.send(new Message("competitorExit"));
                            goBack();
                        }
                    });
                });
            });
        });
    }

    public void goBack(){
        player.setRoomWindow(new RoomWindow(player,this));
        closeWindow();
    }
}

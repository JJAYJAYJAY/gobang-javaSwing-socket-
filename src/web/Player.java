package web;


import eneity.Message;
import windows.PvpWindow;
import windows.RoomWindow;
import windows.WelcomeWindow;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class Player{
    private final Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private RoomWindow roomWindow;
    private PvpWindow pvpWindow;
    public WelcomeWindow welcomeWindow;
    public int color;
    public Player(WelcomeWindow welcomeWindow ,String host) throws IOException {
        try {
            this.socket = new Socket(host,8000);
            roomWindow=new RoomWindow(this,welcomeWindow);
            this.welcomeWindow=welcomeWindow;
            this.new ReadThread().start();
            String name = JOptionPane.showInputDialog(roomWindow, "请输入用户名", "创建用户", JOptionPane.PLAIN_MESSAGE);
            if (name == null) {
                this.close();
                this.welcomeWindow=new WelcomeWindow(welcomeWindow);
                roomWindow.dispose();
            }else {
                while (name.equals("")) {
                    JOptionPane.showMessageDialog(roomWindow, "用户名不能为空！", "警告", JOptionPane.WARNING_MESSAGE);
                    name = JOptionPane.showInputDialog(roomWindow, "请输入用户名", "创建用户", JOptionPane.PLAIN_MESSAGE);
                }
                this.send(new Message("setName", name));
            }
        }catch (IOException e){
            throw e;
        }
    }
    public void send(Object object){
        try {
            if(oos==null)
                this.oos=new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(object);
            oos.flush();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,"服务器已关闭");
            new WelcomeWindow(null);
            roomWindow.dispose();
            welcomeWindow.dispose();
            pvpWindow.dispose();
        }
    }
    public Message read() throws InterruptedException{
        try {
            if(ois==null) {
                this.ois = new ObjectInputStream(socket.getInputStream());
            }
            return (Message) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new InterruptedException();
        }
    }
    public void close(){
        try {
            send(new Message("close"));
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void setRoomWindow(RoomWindow roomWindow) {
        this.roomWindow = roomWindow;
    }
    public void setPvpWindow(PvpWindow pvpWindow) {
        this.pvpWindow = pvpWindow;
    }
    public class ReadThread extends Thread{
        Message message;
        HashMap<String,Command> commandMap=new HashMap<>();
        {
            commandMap.put("hadName",this::hadNamePerform);
            commandMap.put("hadRoom",this::hadRoomPerform);
            commandMap.put("addRoomSuccessfully",this::addRoomSuccessfully);
            commandMap.put("roomList",this::roomListPerform);
            commandMap.put("hadPeople",this::hadPeoplePerform);
            commandMap.put("hasPeople",this::hasPeoplePerform);
            commandMap.put("competitorExit",this::competitorExitPerform);
            commandMap.put("targetReady",this::targetReadyPerform);
            commandMap.put("targetNotReady",this::targetNotReadyPerform);
            commandMap.put("start",this::startPerform);
            commandMap.put("board",this::boardPerform);
            commandMap.put("win",this::winPerform);
            commandMap.put("wantToRepent",this::wantToRepentPerform);
            commandMap.put("agreeRepent",this::agreeRepentPerform);
            commandMap.put("rejectRepent",this::rejectRepentPerform);
            commandMap.put("giveUp",this::giveUpPerform);
        }
        @Override
        public void run() {
            while (true){
                try {
                    message=Player.this.read();
                    System.out.println(message.type);
                    Command command=commandMap.get(message.type);
                    if(command!=null)
                        command.command();
                }catch (IOException|InterruptedException e){
                    System.out.println("房间读取线程结束");
                    break;
                }
            }
        }
        private void hadNamePerform(){
            JOptionPane.showMessageDialog(roomWindow,"用户名已存在重新取名","警告",JOptionPane.WARNING_MESSAGE);
            String name = JOptionPane.showInputDialog(roomWindow, "请输入用户名", "创建用户", JOptionPane.PLAIN_MESSAGE);
            if (name == null) {
                close();
                new WelcomeWindow(welcomeWindow);
                roomWindow.dispose();
            }else {
                while (name.equals("")) {
                    JOptionPane.showMessageDialog(roomWindow, "用户名不能为空！", "警告", JOptionPane.WARNING_MESSAGE);
                    name = JOptionPane.showInputDialog(roomWindow, "请输入用户名", "创建用户", JOptionPane.PLAIN_MESSAGE);
                }
                send(new Message("setName", name));
            }
        }
        private void hadRoomPerform(){
            JOptionPane.showMessageDialog(roomWindow,"房间名已存在重新取名","警告",JOptionPane.WARNING_MESSAGE);
            String name = JOptionPane.showInputDialog(roomWindow, "请输入房间名","创建房间",JOptionPane.PLAIN_MESSAGE);
            if(name==null)
                return;
            while (name.equals("")){
                JOptionPane.showMessageDialog(roomWindow, "房间名不能为空！","警告",JOptionPane.WARNING_MESSAGE);
                name=JOptionPane.showInputDialog(roomWindow, "请输入房间名","创建用户",JOptionPane.PLAIN_MESSAGE);
            }
            send(new Message("addRoom",name));
        }
        private void addRoomSuccessfully(){
            pvpWindow=new PvpWindow(Player.this,roomWindow);
            roomWindow.dispose();
            ((JLabel)pvpWindow.getComponent("信息显示")).setText("等待玩家进入...");
            (pvpWindow.getComponent("开始游戏")).setEnabled(false);
        }
        @SuppressWarnings("all")
        private void roomListPerform(){
            roomWindow.roomSet=(HashSet<String>) message.value;
            JList list=roomWindow.getComponent("房间列表");
            list.removeAll();
            ListModel jListModel =  new DefaultComboBoxModel(roomWindow.roomSet.toArray());
            list.setModel(jListModel);
            System.out.println(roomWindow.roomSet);
            roomWindow.repaint();
        }
        private void hadPeoplePerform(){
            JOptionPane.showMessageDialog(roomWindow,"房间已满","房间已满",JOptionPane.PLAIN_MESSAGE);
        }
        private void hasPeoplePerform(){
            Player.this.send(message);
            new Thread(() -> JOptionPane.showMessageDialog(pvpWindow,message.value+"已进入","有玩家进入",JOptionPane.PLAIN_MESSAGE)).start();
            ((JLabel)pvpWindow.getComponent("信息显示")).setText("对方未准备");
            pvpWindow.getComponent("开始游戏").setEnabled(true);
        }
        private void competitorExitPerform(){
            JOptionPane.showMessageDialog(pvpWindow,"对方已掉线，请返回房间列表","提示",JOptionPane.PLAIN_MESSAGE);
            pvpWindow.getComponent("悔棋").setEnabled(false);
            pvpWindow.getComponent("放弃").setEnabled(false);
            pvpWindow.getComponent("开始游戏").setEnabled(false);
            send(new Message("competitorExit",1));
        }
        private void targetReadyPerform(){
            Player.this.send(message);
            ((JLabel)pvpWindow.getComponent("信息显示")).setText("对方已准备");
        }
        private void targetNotReadyPerform(){
            Player.this.send(message);
            ((JLabel)pvpWindow.getComponent("信息显示")).setText("对方未准备");
        }
        private void startPerform(){
            pvpWindow.checkerBoard.restart();
            int currentColor=(pvpWindow.checkerBoard.count%2)+1;
            if(color==currentColor){
                pvpWindow.checkerBoard.addListeners();
                ((JLabel)pvpWindow.getComponent("信息显示")).setText("请您落子");
            }
            pvpWindow.getComponent("悔棋").setEnabled(true);
            pvpWindow.getComponent("放弃").setEnabled(true);
            pvpWindow.getComponent("开始游戏").setEnabled(false);
        }
        @SuppressWarnings("all")
        private void boardPerform(){
            pvpWindow.checkerBoard.setGrid((int[][]) message.values[0]);
            pvpWindow.checkerBoard.setHistory((Stack<int[]>) message.values[1]);
            pvpWindow.checkerBoard.setCount((int) message.values[2]);
            pvpWindow.checkerBoard.repaint();
            int currentColor=(pvpWindow.checkerBoard.count%2)+1;
            if(color==currentColor){
                ((JLabel)pvpWindow.getComponent("信息显示")).setText("请您落子");
                pvpWindow.checkerBoard.addListeners();
            }
        }
        private void winPerform(){
            JOptionPane.showMessageDialog(pvpWindow,"对方获胜","游戏结束",JOptionPane.PLAIN_MESSAGE);
            ((JLabel)pvpWindow.getComponent("信息显示")).setText("对方获胜");
            ((JToggleButton)pvpWindow.getComponent("开始游戏")).setText("再来一局");
            ((JToggleButton) pvpWindow.getComponent("开始游戏")).setSelected(false);
            pvpWindow.getComponent("开始游戏").setEnabled(true);
            pvpWindow.getComponent("悔棋").setEnabled(false);
            pvpWindow.getComponent("放弃").setEnabled(false);
            pvpWindow.checkerBoard.removeListeners();
            send(new Message("win",0));
        }
        private void wantToRepentPerform(){
            int option=JOptionPane.showOptionDialog(pvpWindow,"对方请求悔棋，是否同意？","请求悔棋",
                    JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,null,
                    new String[]{"同意","拒绝"},"同意");
            if(option==0){
                send(new Message("agreeRepent",message.value));
                ((JLabel)pvpWindow.getComponent("信息显示")).setText("请等待对方落子");
                pvpWindow.checkerBoard.removeListeners();
            }else {
                send(new Message("rejectRepent"));
            }
        }
        private void agreeRepentPerform(){
            JOptionPane.showMessageDialog(pvpWindow,"对方同意悔棋","提示",JOptionPane.PLAIN_MESSAGE);
            pvpWindow.checkerBoard.playChessService.repent((int)message.value);
            pvpWindow.checkerBoard.sendCheckerMessage();
            pvpWindow.getComponent("悔棋").setEnabled(true);
            pvpWindow.getComponent("放弃").setEnabled(true);
            pvpWindow.checkerBoard.addListeners();
        }
        private void rejectRepentPerform(){
            JOptionPane.showMessageDialog(pvpWindow,"对方不同意悔棋","提示",JOptionPane.PLAIN_MESSAGE);
            pvpWindow.getComponent("悔棋").setEnabled(true);
            pvpWindow.getComponent("放弃").setEnabled(true);
            if(color==((pvpWindow.checkerBoard.count%2)+1))
                pvpWindow.checkerBoard.addListeners();
        }
        private void giveUpPerform(){
            JOptionPane.showMessageDialog(pvpWindow,"对方认输，您已获胜！","游戏结束",JOptionPane.PLAIN_MESSAGE);
            ((JLabel) pvpWindow.getComponent("信息显示")).setText("恭喜您已获胜");
            ((JToggleButton) pvpWindow.getComponent("开始游戏")).setText("再来一局");
            ((JToggleButton) pvpWindow.getComponent("开始游戏")).setSelected(false);
            pvpWindow.getComponent("悔棋").setEnabled(false);
            pvpWindow.getComponent("放弃").setEnabled(false);
            pvpWindow.getComponent("开始游戏").setEnabled(true);
            pvpWindow.checkerBoard.removeListeners();
            send(new Message("giveUp",0));
        }
    }
}
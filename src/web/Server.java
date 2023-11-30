package web;

import eneity.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;

public class Server {
    //房间map
    public static HashMap<String,Socket> roomMap=new HashMap<>();
    //socket map
    public static HashMap<String,Socket> socketMap =new HashMap<>();
    //socket和线程的map
    public static HashMap<Socket,UserThread> socketThreadMap=new HashMap<>();
    //用户线程
    static class UserThread extends Thread{
        private final Socket socket;
        private String name;
        private Socket targetSocket;
        public  ObjectOutputStream oos;
        private Message message;
        private boolean thisFlag=false,targetFlag=false,start=false;
        private final HashMap<String,Command> commandMap=new HashMap<>();
        {
            commandMap.put("setName",this::setNamePerform);
            commandMap.put("addRoom",this::addRoomPerform);
            commandMap.put("enter",this::enterPerform);
            commandMap.put("hasPeople",this::hasPeoplePerform);
            commandMap.put("board",this::boardPerform);
            commandMap.put("getRoomList",this::getRoomListPerform);
            commandMap.put("thisReady",this::thisReadyPerform);
            commandMap.put("thisNotReady",this::thisNotReadyPerform);
            commandMap.put("targetReady",this::targetReadyPerform);
            commandMap.put("targetNotReady",this::targetNotReadyPerform);
            commandMap.put("win",this::winPerform);
            commandMap.put("competitorExit",this::competitorExitPerform);
            commandMap.put("wantToRepent",this::wantToRepentPerform);
            commandMap.put("agreeRepent",this::agreeRepentPerform);
            commandMap.put("rejectRepent",this::rejectRepentPerform);
            commandMap.put("giveUp",this::giveUpPerform);
        }
        public UserThread(Socket socket) {
            this.socket=socket;
        }

        @Override
        public void run() {
            try {
                //创建输入输出流
                System.out.println("客户端:" + socket.getInetAddress().getHostAddress() + "已经连接");
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                oos = new ObjectOutputStream(socket.getOutputStream());
                //启动后台读取循环
                while (true) {
                    if(thisFlag&&targetFlag&&!start){
                        oos.writeObject(new Message("start"));
                        start=true;
                    }
                    message =(Message) ois.readObject();
                    System.out.println(message.type);
                    if(message.type.equals("close")){
                        socket.close();
                        break;
                    }else {
                        Command command=commandMap.get(message.type);
                        if(command!=null)
                            command.command();
                    }
                }
                //断连后的操作
                socketMap.remove(name,socket);
                removeRoom(socket);
                if(targetSocket!=null){
                System.out.println("提示对手");
                socketThreadMap.get(targetSocket).oos.writeObject(new Message("competitorExit", name));
                }

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("断开链接");
                removeRoom(socket);
                if(targetSocket!=null){
                    try {
                        System.out.println("提示对手");
                        socketThreadMap.get(targetSocket).oos.writeObject(new Message("competitorExit"));
                    } catch (IOException ignore) {}
                }
            }
        }
        private void setNamePerform() throws IOException{
            System.out.println("设置姓名 "+message.value);
            if(socketMap.containsKey((String) message.value)){
                oos.writeObject(new Message("hadName"));
                return;
            }
            socketMap.put((String) message.value, socket);
            name=(String) message.value;
        }
        private void addRoomPerform() throws IOException{
            System.out.println("添加房间:" + message.value);
            String roomName = (String) message.value;
            if(roomMap.containsKey(roomName)){
                oos.writeObject(new Message("hadRoom"));
                return;
            }
            addRoom(message.value, socket);
            oos.writeObject(new Message("addRoomSuccessfully"));
            System.out.println("添加成功");
        }
        private void enterPerform() throws IOException{
            System.out.println(message.value);
            if (targetSocket == null) {
                targetSocket = roomMap.get((String) message.value);
                socketThreadMap.get(targetSocket).oos.writeObject(new Message("hasPeople", name));
                System.out.println("进入成功");
            } else {
                oos.writeObject(new Message("hadPeople"));
            }
        }
        private void hasPeoplePerform(){
            targetSocket = socketMap.get((String) message.value);
            System.out.println("添加成功");
        }
        private void boardPerform() throws IOException{
            socketThreadMap.get(targetSocket).oos.writeObject(message);
            oos.writeObject(message);
            System.out.println("发送棋盘信息");
        }
        private void getRoomListPerform() throws IOException{
            oos.writeObject(new Message("roomList",new HashSet<>(roomMap.keySet())));
            System.out.println(roomMap.keySet());
            System.out.println("发送房间列表");
        }
        private void thisReadyPerform() throws IOException{
            thisFlag=true;
            socketThreadMap.get(targetSocket).oos.writeObject(new Message("targetReady"));
        }
        private void thisNotReadyPerform() throws IOException{
            thisFlag=false;
            socketThreadMap.get(targetSocket).oos.writeObject(new Message("targetNotReady"));
        }
        private void targetReadyPerform(){
            targetFlag=true;
        }
        private void targetNotReadyPerform(){
            targetFlag=false;
        }
        private void winPerform() throws IOException{
            if((int)message.value==1)
                socketThreadMap.get(targetSocket).oos.writeObject(message);
            start=false;
            thisFlag=false;
            targetFlag=false;
        }
        private void competitorExitPerform() throws IOException{
            if(targetSocket!=null&&message.value==null)
                socketThreadMap.get(targetSocket).oos.writeObject(message);
            targetSocket=null;
            removeRoom(socket);
        }
        private void wantToRepentPerform() throws IOException{
            socketThreadMap.get(targetSocket).oos.writeObject(message);
        }
        private void agreeRepentPerform() throws IOException{
            socketThreadMap.get(targetSocket).oos.writeObject(message);
        }
        private void rejectRepentPerform() throws IOException{
            socketThreadMap.get(targetSocket).oos.writeObject(message);
        }
        private void giveUpPerform() throws IOException{
            if((int)message.value==1)
                socketThreadMap.get(targetSocket).oos.writeObject(message);
            start=false;
            thisFlag=false;
            targetFlag=false;
        }
    }
    //移除房间
    private static void removeRoom(Socket socket){
        for(String str:roomMap.keySet()){
            if(roomMap.get(str).equals(socket)){
                System.out.println("remove");
                roomMap.remove(str,socket);
                break;
            }
        }
    }
    //添加房间
    private static void addRoom(Object name,Socket socket){
        roomMap.put((String) name,socket);
    }
    //服务器启动程序
    @SuppressWarnings("all")
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket=new ServerSocket(8000);
            System.out.println("服务器启动等待连接");
            while (true){
                Socket socket=serverSocket.accept();
                UserThread user = new UserThread(socket);
                socketThreadMap.put(socket,user);
                user.start();
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}

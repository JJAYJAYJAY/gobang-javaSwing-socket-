package eneity;

import service.PlayChessService;
import web.Player;
import windows.AbstractWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Stack;

import static windows.UnitNew.normalSize;
import static windows.UnitNew.unit;

public class CheckerBoard extends JPanel {
    //记录当然棋盘情况
    private int[][] grid=new int[19][19];
    //记录操作历史
    private Stack<int[]> history=new Stack<>();
    //棋盘原点坐标
    public final int x0=unit,y0=unit;
    //计数器计算轮到哪方了
    public int count=0;
    public PlayChessService playChessService;
    //用于储存鼠标目前坐标
    int[] index;
    //以下用于缓冲棋盘图像
    private Image iBuffer;
    private Graphics gBuffer;
    //人机
    public AI ai;
    //相对的窗口
    public AbstractWindow window;
    //玩家
    private Player player;
    /**
     * 两个构造函数一个用于pve一个用于pvp
     * @param window 对应的窗口
     */
    public CheckerBoard(AbstractWindow window) {
        this.window=window;
        playChessService=new PlayChessService(this);
        this.setLayout(null);
        this.setBackground(new Color(234,215,176));
        this.setSize(normalSize(20,20));
    }
    public CheckerBoard(AbstractWindow window,Player player) {
        this.window=window;
        playChessService=new PlayChessService(this);
        this.setLayout(null);
        this.setBackground(new Color(234,215,176));
        this.setSize(normalSize(20,20));
        this.player=player;
    }
    /**
     * 发送棋盘信息
     */
    public void sendCheckerMessage(){
        player.send(new Message("board",new Object[]{grid,history,count}));
    }
    /**
     * 添加该棋盘的监听器，此方法只用于pvp时
     */
    public void addListeners(){
        if(getMouseListeners().length>0||getMouseMotionListeners().length>0){
            return;
        }
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    playChessService.placeChess(e);
                    playChessService.paintChess(CheckerBoard.this.getGraphics());
                    sendCheckerMessage();
                    if(playChessService.isWin()) {
                        player.send(new Message("win",1));
                        JOptionPane.showMessageDialog(window,"您已获胜","游戏结束",JOptionPane.PLAIN_MESSAGE);
                        ((JLabel)(window.getComponent("信息显示"))).setText("恭喜您已获胜");
                        ((JToggleButton) window.getComponent("开始游戏")).setText("再来一局");
                        ((JToggleButton) window.getComponent("开始游戏")).setSelected(false);
                        window.getComponent("开始游戏").setEnabled(true);
                        return;
                    }
                    removeListeners();
                    ((JLabel)window.getComponent("信息显示")).setText("等待对方落子");
                } catch (MeaninglessPointException | IndexOutOfBoundsException ignore) {}
            }
        });
        this.addMouseMotionListener(new MouseAdapter() {
            int[] lastIndex;
            @Override
            public void mouseMoved(MouseEvent e) {
                //此处判断是为了减少刷新次数
                if(Arrays.equals(lastIndex, playChessService.getMouseLocation(e)))
                    return;
                CheckerBoard.this.index=playChessService.getMouseLocation(e);
                lastIndex= playChessService.getMouseLocation(e);
                repaint();
            }


        });
    }
    public void removeListeners(){
        if(getMouseListeners().length>0&&getMouseMotionListeners().length>0) {
            removeMouseListener(getMouseListeners()[0]);
            removeMouseMotionListener(getMouseMotionListeners()[0]);
        }
    }
    /**
     * pve的监听器配置
     */
    public void setPve(){
        this.ai=new AI(this);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    playChessService.placeChess(e);
                    playChessService.paintChess(CheckerBoard.this.getGraphics());
                    if (playChessService.isWin()) {
                        JOptionPane.showMessageDialog(window,"白方获胜！","游戏结束",JOptionPane.PLAIN_MESSAGE);
                        return;
                    }
                    ((JLabel)(window.getComponent("信息显示"))).setText("等待电脑落子");
                    ai.aiPlayChess();
                    if (playChessService.isWin()){
                        JOptionPane.showMessageDialog(window,"黑方获胜！","游戏结束",JOptionPane.PLAIN_MESSAGE);
                      return;
                    }
                    ((JLabel)(window.getComponent("信息显示"))).setText("请玩家落子");
                } catch (MeaninglessPointException | IndexOutOfBoundsException ignore) {}
            }
        });
        this.addMouseMotionListener(new MouseAdapter() {
            int[] lastIndex;
            @Override
            public void mouseMoved(MouseEvent e) {
                //此处判断是为了减少刷新次数
                if(Arrays.equals(lastIndex, playChessService.getMouseLocation(e)))
                    return;
                CheckerBoard.this.index=playChessService.getMouseLocation(e);
                lastIndex= playChessService.getMouseLocation(e);
                repaint();
            }
        });
    }
    /**
     * 重写绘制方法将棋盘和棋子绘制出来
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        playChessService.paintBoard(g);
        playChessService.paintChess(g);
        try {
            playChessService.showIndex(g,index);
            index=null;
        } catch (Exception ignored) {}
    }
    /**双缓冲法，绘制好图片在展示 可以相比起其他方法延迟少
     */
    @Override
    public void update(Graphics g) {
        if(iBuffer==null)
        {
            iBuffer=createImage(this.getSize().width,this.getSize().height);
            gBuffer=iBuffer.getGraphics();
        }
        gBuffer.setColor(getBackground());
        gBuffer.fillRect(0,0,this.getSize().width,this.getSize().height);
        paint(gBuffer);
        g.drawImage(iBuffer,0,0,this);
    }
/*线程限制固定刷新率 可行 但导致预下点刷新速度有限
    public class repaintThread extends Thread{
        @Override
        public void run() {
            while (true) {
               repaint();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


刷新法不断绘制棋盘覆盖 不行会越画越多越画越卡
    public void flush(Graphics g) {
        g.setColor(new Color(234,215,176));
        g.fillRect(0,0,getWidth(),getHeight());
        g.setColor(Color.black);
        //绘制棋盘
        for (int i=0;i<19;i++){
            g.drawLine(x0,y0+i*x0,x0+18*unit,y0+i*unit);
            g.drawLine(x0+i*unit,y0,x0+i*unit,y0+18*unit);
        }
        playChessService.paintChess(g);
        try {
            showIndex(g,index);
        } catch (Exception e){
        }
    }

*/
    public Stack<int[]> getHistory() {
        return history;
    }
    /**获取当前棋盘状态
     */
    public int[][] getGrid() {
        return grid;
    }
    public void setGrid(int[][] grid) {
        this.grid = grid;
    }
    public void setCount(int count) {
        this.count = count;
    }
    public void setHistory(Stack<int[]> history) {
        this.history = history;
    }
    public void setBlack(int[] index){
        grid[index[0]][index[1]]=2;
    }
    public void setWhite(int[] index){
        grid[index[0]][index[1]]=1;
    }
    public void setNull(int[] index){
        grid[index[0]][index[1]]=0;
    }
    /**更改为黑方先手
     */
    public void blackFirst() {
        this.count = 1;
    }
    /**
     * 重新开始
     */
    public void restart(){
        count=0;
        grid=new int[19][19];
        history.clear();
        repaint();
    }
}

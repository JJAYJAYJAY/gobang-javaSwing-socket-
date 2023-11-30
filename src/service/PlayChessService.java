package service;

import eneity.CheckerBoard;
import eneity.MeaninglessPointException;
import windows.AbstractWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Stack;

import static windows.UnitNew.unit;
public class PlayChessService {
    private final CheckerBoard checkerBoard;
    private final AbstractWindow window;
    public PlayChessService(CheckerBoard checkerBoard) {
        this.checkerBoard = checkerBoard;
        this.window=checkerBoard.window;
    }
    //计算鼠标落子点
    public int[] getMouseLocation(MouseEvent e){
        int x=e.getX();
        int y=e.getY();
        int x1,y1,x0=checkerBoard.x0,y0=checkerBoard.y0;
        if((x-x0)%unit>unit/2){
            x1=(x-x0)/unit+1;
        }
        else
            x1=(x-x0)/unit;
        if((y-y0)%unit>unit/2){
            y1=(y-y0)/unit+1;
        }
        else
            y1=(y-y0)/unit;
        return new int[]{x1,y1};
    }
    //绘制棋盘
    public void paintBoard(Graphics g){
        int x0=checkerBoard.x0,y0=checkerBoard.y0;
        for (int i=0;i<19;i++){
            g.drawLine(x0, y0+i*unit,x0+18*unit,y0+i*unit);
            g.drawLine(x0+i*unit,y0,x0+i*unit,y0+18*unit);
        }
    }
    /**绘制棋子
     */
    @SuppressWarnings("unchecked")
    public void paintChess(Graphics g) {
        Stack<int[]> history = (Stack<int[]>) checkerBoard.getHistory().clone();
        int[][] grid = checkerBoard.getGrid();
        int x0 = checkerBoard.x0, y0 = checkerBoard.y0;

        while (!history.empty()) {
            int[] index = history.pop();
            if (grid[index[0]][index[1]] == 1) {
                g.setColor(Color.white);
                g.fillOval(x0 + index[0] * unit - (int) (0.4 * unit), y0 + index[1] * unit - (int) (0.4 * unit), (int) (unit * 0.8), (int) (unit * 0.8));
            }
            if (grid[index[0]][index[1]] == 2) {
                g.setColor(Color.black);
                g.fillOval(x0 + index[0] * unit - (int) (0.4 * unit), y0 + index[1] * unit - (int) (0.4 * unit), (int) (unit * 0.8), (int) (unit * 0.8));
            }
        }
/*原代码 新代码提升了运行速度
//        for(int x=0;x<checkerBoard.getGrid().length;x++){
//            for (int y=0;y<checkerBoard.getGrid()[0].length;y++){
//                if(checkerBoard.getGrid()[x][y]==1) {
//                    g.setColor(Color.white);
//                    g.fillOval(checkerBoard.getX0() + x * unit - (int) (0.4 * unit), checkerBoard.getY0() + y * unit - (int) (0.4 * unit), (int) (unit * 0.8), (int) (unit * 0.8));
//                }
//                if(checkerBoard.getGrid()[x][y]==2){
//                    g.setColor(Color.black);
//                    g.fillOval(checkerBoard.getX0() + x * unit - (int) (0.4 * unit), checkerBoard.getY0() + y * unit - (int) (0.4 * unit), (int) (unit * 0.8), (int) (unit * 0.8));
//                }
//            }
//        }
*/
    }
    /**显示目前的鼠标位置
     * */
    public void showIndex(Graphics g, int[] index) throws IndexOutOfBoundsException{
        if(checkerBoard.getGrid()[index[0]][index[1]]!=0)
            return;
        g.setColor(new Color(3, 248, 76, 128));

        g.fillOval(checkerBoard.x0 + index[0] * unit - (int) (0.4 * unit), checkerBoard.y0 + index[1] * unit - (int) (0.4 * unit), (int) (unit * 0.8), (int) (unit * 0.8));
    }
    @SuppressWarnings("unused")
    public void placeChess(MouseEvent e) throws MeaninglessPointException,IndexOutOfBoundsException {
        int[] index=getMouseLocation(e);
        int x=index[0],y=index[1];
        int[][] grid=checkerBoard.getGrid();
        int a=grid[x][y];
        if (grid[x][y]!=0)
            throw new MeaninglessPointException();

        if (checkerBoard.count % 2 == 0 && grid[x][y]==0)
            checkerBoard.setWhite(index);
        else if(grid[x][y]==0)
            checkerBoard.setBlack(index);
        checkerBoard.getHistory().push(index);
        checkerBoard.count++;
    }
    @SuppressWarnings("unused")
    public void placeChess(int[] index){
        int x=index[0],y=index[1];
        int[][] grid=checkerBoard.getGrid();
        try {
            int a=grid[x][y];
        }catch (IndexOutOfBoundsException c){
            return;
        }
        if (grid[x][y]!=0)
            return;
        if (checkerBoard.count % 2 == 0 && grid[x][y]==0)
            checkerBoard.setWhite(index);
        else if(grid[x][y]==0)
            checkerBoard.setBlack(index);
        checkerBoard.getHistory().push(index);
        checkerBoard.count++;
    }
    @SuppressWarnings("unchecked")
    public boolean isWin(){
        Stack<int[]> history=(Stack<int[]>) checkerBoard.getHistory().clone();
        int[] index=history.pop();
        int x=index[0],y=index[1];
        int color=checkerBoard.getGrid()[x][y];

        int[][] directs=new int[][]{{1,0},{0,1},{1,1},{-1,1}};
        for (int[] direct:directs){
            if(isLineWin(direct)>=5){
                switch (color){
                    case 1-> {
                        ((JLabel)(window.getComponent("信息显示"))).setText("白方获胜");
                        window.getComponent("悔棋").setEnabled(false);
                        window.getComponent("放弃").setEnabled(false);
                        window.getComponent("开始游戏").setEnabled(true);
                    }
                    case 2->{

                        ((JLabel)(window.getComponent("信息显示"))).setText("黑方获胜");
                        window.getComponent("悔棋").setEnabled(false);
                        window.getComponent("放弃").setEnabled(false);
                        window.getComponent("开始游戏").setEnabled(true);
                    }
                }
                checkerBoard.removeListeners();
                return true;
            }
        }
       return false;
    }
    @SuppressWarnings("unchecked")
    public int isLineWin(int[] direct) {
        int directX=direct[0],directY=direct[1];
        Stack<int[]> history=(Stack<int[]>) checkerBoard.getHistory().clone();
        int[][] grid=checkerBoard.getGrid();
        int[] index=history.pop();
        int x=index[0],y=index[1];
        int color=grid[x][y];
        int count=1;
        try {
            for (int i = 1; i < 5; i++) {
                if (color == grid[x - i*directX][y - i*directY]) {
                    count++;
                } else break;
            }
        }catch (IndexOutOfBoundsException ignore){}
        try {
            for (int i = 1; i < 5; i++) {
                if (color == grid[x + i*directX][y + i*directY]) {
                    count++;
                } else break;
            }
        }catch (IndexOutOfBoundsException ignore){}
        return count;
    }
    /**
     * 悔棋功能
     */
    public void repent(int color){
        Stack<int[]> history=checkerBoard.getHistory();
        if(history.empty())
            return;
        int current_color=checkerBoard.count%2+1;
        switch (color){
            case 1-> ((JLabel)(window.getComponent("信息显示"))).setText("白方悔棋");
            case 2-> ((JLabel)(window.getComponent("信息显示"))).setText("黑方悔棋");
        }
        int[] index=history.pop();
        checkerBoard.setNull(index);
        if(current_color==color) {
            index = history.pop();
            checkerBoard.setNull(index);
        }
        checkerBoard.count=color-1;
        checkerBoard.repaint();
    }
    /**
     * 放弃功能
     */
    public void giveUp(){
        int color=checkerBoard.count%2+1;
        switch (color){
            case 1-> {
                JOptionPane.showMessageDialog(window,"白方认输","游戏结束",JOptionPane.PLAIN_MESSAGE);
                ((JLabel)(window.getComponent("信息显示"))).setText("白方认输，黑方获胜");
            }
            case 2->{
                JOptionPane.showMessageDialog(window,"黑方认输","游戏结束",JOptionPane.PLAIN_MESSAGE);
                ((JLabel)(window.getComponent("信息显示"))).setText("黑方认输，白方获胜");
            }
        }
        checkerBoard.removeListeners();
        window.getComponent("悔棋").setEnabled(false);
        window.getComponent("放弃").setEnabled(false);
    }
}
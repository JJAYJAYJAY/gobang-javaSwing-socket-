package eneity;

import java.util.*;

public class AI {
    private final CheckerBoard checkerBoard;
    private final HashMap<String,Integer> checkMap=new HashMap<>();
    {
        //五连
        checkMap.put("XXXXX",10000);
        //活四
        checkMap.put("ZXXXXZ",4000);
        //冲四
        checkMap.put("ZXXXXY",1010);
        checkMap.put("YXXXXZ",1010);
        checkMap.put("XZXXXZ",1000);
        checkMap.put("ZXXXZX",1000);
        checkMap.put("XXZXX",1000);
        //活三
        checkMap.put("ZXXXZ",610);
        checkMap.put("ZXZXXZ",600);
        checkMap.put("ZXXZXZ",600);
        //眠三
        checkMap.put("ZZXXXY",310);
        checkMap.put("YXXXZZ",310);
        checkMap.put("ZXZXXY",300);
        checkMap.put("YXXZXZ",300);
        checkMap.put("ZXXZXY",300);
        checkMap.put("YXZXXZ",300);
        checkMap.put("YZXXXZY",300);
        checkMap.put("XZZXX",300);
        checkMap.put("XXZZX",300);
        checkMap.put("XZXZX",300);
        //活二
        checkMap.put("ZZXXZZ",305);
        checkMap.put("ZXZXZ",260);
        checkMap.put("XZZX",260);
        //眠二
        checkMap.put("ZZZXXY",30);
        checkMap.put("YXXZZZ",30);
        checkMap.put("ZZXZXY",30);
        checkMap.put("YXZXZZ",30);
        checkMap.put("ZXZZXY",30);
        checkMap.put("YXZZXZ",30);
        checkMap.put("XZZZX",30);
    }
    public AI(CheckerBoard checkerBoard) {
        this.checkerBoard=checkerBoard;
    }
    public  void  aiPlayChess(){
        if(checkerBoard.getHistory().empty()){
            checkerBoard.playChessService.placeChess(new int[]{9,9});
            checkerBoard.playChessService.paintChess(checkerBoard.getGraphics());
        }
        else {
            checkerBoard.playChessService.placeChess(bestPoint(checkerBoard.getHistory(), checkerBoard.getGrid()));
            checkerBoard.playChessService.paintChess(checkerBoard.getGraphics());
        }
    }
    //一层博弈
    private int[] bestPoint(Stack<int[]> history,int[][] grid){
        int maxScore=0;
        int[] bestPoint=null;
        List<int[]> checkRange=getCheckRange(history,grid);
        String[] lines=new String[4];
        for (int[] point:checkRange) {
            int color=checkerBoard.count%2+1;
            int aiScore=0;
            lines[0] = getLine(color, grid, point, 1, 0);
            lines[1] = getLine(color, grid, point, 0, 1);
            lines[2] = getLine(color, grid, point, 1, 1);
            lines[3] = getLine(color, grid, point, -1, 1);
            for (String line : lines) {
                aiScore+=scoreLine(line);
            }

            color=(checkerBoard.count+1)%2+1;
            int playerScore=0;
            lines[0] = getLine(color, grid, point, 1, 0);
            lines[1] = getLine(color, grid, point, 0, 1);
            lines[2] = getLine(color, grid, point, 1, 1);
            lines[3] = getLine(color, grid, point, -1, 1);
            for (String line : lines) {
                playerScore-=scoreLine(line);
            }
            int score=aiScore-(int)(playerScore*0.6);
            if(score>maxScore){
                bestPoint=point;
                maxScore=score;
            }
            else if(score==maxScore){
                if((int) (Math.random()*2)==0){
                    bestPoint=point;
                }
            }
        }
        return bestPoint;
    }
    /**
     * 获得检查范围
     */
    @SuppressWarnings("all")
    private List<int[]> getCheckRange(Stack<int[]> history,int[][] grid){
        List<int[]> range =new ArrayList<>();
        List<String> hasSum=new ArrayList<>();
        for(int[] point:history){
            for(int i=-2;i<=2;i++){
                for(int j=-2;j<=2;j++){
                    try {
                        if(grid[point[0]+i][point[1]+j]!=0||hasSum.contains(Arrays.toString(new int[]{point[0] + i, point[1]+j}))) {}
                        else {
                            range.add(new int[]{point[0] + i, point[1]+j});
                            hasSum.add(Arrays.toString(new int[]{point[0] + i, point[1]+j}));
                        }
                    } catch (IndexOutOfBoundsException ignore){}
                }
            }
        }
        return range;
    }
    private String getLine(int color,int[][] grid, int[] point,int x,int y){
        StringBuilder line=new StringBuilder("X");//记录棋形 X代表本方Y代表对方Z代表空
            try {
                for (int i = 1; i < 6; i++) {
                    if (grid[point[0] - i*x][point[1]-i*y] == color) {
                        line.insert(0, "X");
                    } else if (grid[point[0] - i*x][point[1]-i*y]==0 ) {
                        line.insert(0, "Z");
                    } else {
                        line.insert(0, "Y");
                    }
                }
            }catch (IndexOutOfBoundsException ignore){}
            try {
                for (int i = 1; i < 6; i++) {
                    if (grid[point[0] + i*x][point[1]+i*y] == color) {
                        line.append("X");
                    } else if (grid[point[0] +i*x][point[1]+i*y]==0) {
                        line.append("Z");
                    } else {
                        line.append("Y");
                    }
                }
            }catch (IndexOutOfBoundsException ignore){}
        return line.toString();
    }
    private int scoreLine(String line){
        int score=0;
        for(String check :checkMap.keySet()){
            if(line.contains(check)){
                score=checkMap.get(check);
                break;
            }
        }
        return score;
    }
}

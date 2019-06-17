package com.example.lanchessgame.dataClass;

public class ChessBroad {
    private int[] broad;
    private int row;
    private int col;
    public ChessBroad(int row, int col){
        setSize(row,col);
    }
    public void setSize(int row,int col){
        this.row = row;
        this.col = col;
        broad = new int[row*col];
        for(int i = 0;i<broad.length;i++){
            broad[i] = 0;
        }
    }
    public void putChess(int x,int y,int Type){
        int position = (x+(y-1)*col)-1;
        if(position>=0&&position<broad.length)
            broad[position] = Type;

    }
    public static int judge(int[] chessBoard,int width,int height,int x,int y){
        int position = (x+(y-1)*width)-1;
        int verCount = vertical(chessBoard,width,height,position);
        int horCount = horizontal(chessBoard,width,height,position);
        int leftCount = leftDiagonal(chessBoard,width,height,position);
        int rightCount = rightDiagonal(chessBoard,width,height,position);
        System.out.println("Vertical:   "+verCount);
        System.out.println("Horizontal: "+horCount);
        System.out.println("leftDiagonal:   "+leftCount);
        System.out.println("rightDiagonal:   "+rightCount);
        return Math.max(Math.max(verCount,horCount), Math.max(leftCount,rightCount));
    }
    private static int vertical(int[] chessBoard,int width,int height,int position){
        int sum = -1;
        int chessType = chessBoard[position],next = position;
        int size = width*height;
        while(next<size&&chessType==chessBoard[next]){
            sum++;
            next+=width;
        }
        next = position;
        while(next>=0&&chessType==chessBoard[next]){
            sum++;
            next-=width;
        }
        return sum;
    }
    private static int horizontal(int[] chessBoard,int width,int height,int position){
        int sum = -1;
        int chessType = chessBoard[position];
        int next = position;
        while(next%width>=position%width&&chessType==chessBoard[next]){
            sum++;
            next++;
        }
        next = position;
        while((next%width)<=position%width&&next>=0&&chessType==chessBoard[next]){
            sum++;
            next--;
        }
        return sum;
    }
    private static int leftDiagonal(int[] chessBoard,int width,int height,int position){
        int sum = -1;
        int chessType = chessBoard[position],next = position;
        int size = width*height;
        while(next<size&&(next%width)>=position%width&&chessType==chessBoard[next]){
            sum++;
            next+=width+1;
        }
        next = position;
        while(next>=0&&(next%width)<=position%width&&chessType==chessBoard[next]){
            sum++;
            next-=width+1;
        }
        return sum;
    }
    private static int rightDiagonal(int[] chessBoard,int width,int height,int position){
        int sum = -1;
        int chessType = chessBoard[position],next = position;
        int size = width*height;
        while(next<size&&(next%width)<=position%width&&chessType==chessBoard[next]){
            sum++;
            next+=width-1;
        }
        next = position;
        while(next>=0&&(next%width)>=position%width&&chessType==chessBoard[next]){
            sum++;
            next-=width-1;
        }
        return sum;
    }
    public int getChess(int x,int y){
        int position = (x+(y-1)*col)-1;
        return broad[position];
    }
    public int[] getBroad() {
        return broad;
    }
}

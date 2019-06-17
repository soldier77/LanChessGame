package com.example.lanchessgame.myView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.lanchessgame.dataClass.ChessBroad;


public class BroadView extends View {
    ChessBroad broad;
    private boolean touchable = true;
    float hd = 0,vd = 0;
    private int chessRadius = 25;
    private int mWidth,mHeight;
    private int row = 0,col = 0;
    private Paint mPaint,cPaint;
    private Chess nowChess;
    public BroadView(Context context, AttributeSet attr){
        super(context,attr);
        nowChess = new Chess();
        cPaint = new Paint();
        cPaint.setColor(Color.BLACK);
        cPaint.setStyle(Paint.Style.FILL);
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(2f);
    }
    public void setBroad(int row,int col){
        this.row = row;
        this.col = col;
        broad = new ChessBroad(row,col);
    }
    public void setChessPaint(int color,int radius){
        chessRadius = radius;
        nowChess.type = color;
    }
    public void setTouchable(boolean b){
        touchable = b;
    }
    public void setChessPaint(int color){
        nowChess.type = color;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = getMeasure(widthMeasureSpec);
        mHeight = getMeasure(heightMeasureSpec);
        setMeasuredDimension(mWidth,mHeight);
    }
    private int getMeasure(int measureSpec){
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if(specMode == MeasureSpec.EXACTLY){
            result = specSize;
        }else{
            if(specMode == MeasureSpec.AT_MOST){
                result = Math.min(300,specSize);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        hd = (float) (mWidth*1.0/(col+1));
        vd = (float) (mHeight*1.0/(row+1));
        if(row > 0&&col > 0){
            //画横线
            for(int i = 1;i<(row+1);i++){
                if(i==1||i==row){
                    mPaint.setStrokeWidth(5);
                }else mPaint.setStrokeWidth(2);
                canvas.drawLine(hd,vd*i,mWidth-hd,vd*i,mPaint);
            }
            //画竖线
            for(int i = 1;i<(col+1);i++){
                if(i==1||i==col){
                    mPaint.setStrokeWidth(5);
                }else mPaint.setStrokeWidth(2);
                canvas.drawLine(hd*i,vd,hd*i,mHeight-vd,mPaint);
            }
            drawChess(canvas);
        }
        super.onDraw(canvas);
    }

    private void drawChess(Canvas canvas){
        int size = col*row;
        int[] mbroad = broad.getBroad();
        for(int i=0;i<size;i++){
            if (mbroad[i] != 0){
                int x = (i+1)%col;
                int y = (i+1)/col+1;
                cPaint.setColor(mbroad[i]);
                canvas.drawCircle(x*hd,y*vd,chessRadius,cPaint);
            }
        }
        drawNowChess(canvas);
    }
    private void drawNowChess(Canvas canvas){
        if(!touchable) return;
        if(nowChess.x != -1 && nowChess.y != -1){
            if(broad.getChess(nowChess.x,nowChess.y) != 0){
                nowChess.x = -1;
                nowChess.y = -1;
                return;
            }
            cPaint.setColor(nowChess.type);
            canvas.drawCircle(nowChess.x*hd,nowChess.y*vd,chessRadius,cPaint);
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!touchable) return super.onTouchEvent(event);
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                setChess(event.getX(),event.getY());
                invalidate();
                break;
        }
        return super.onTouchEvent(event);
    }
    private void setChess(float x,float y){
        nowChess.x = getBroadX(x);
        nowChess.y = getBroadY(y);
    }
    public int putChess(){
        if(nowChess.x != -1 && nowChess.y != -1){
            broad.putChess(nowChess.x,nowChess.y,nowChess.type);
            return ChessBroad.judge(broad.getBroad(),col,row,nowChess.x,nowChess.y);
        }
        return -1;
    }
    public boolean hasPut(){
        return (nowChess.x != -1 && nowChess.y != -1);
    }
    public int putChess(int x,int y,int color){
        broad.putChess(x,y,color);
        return ChessBroad.judge(broad.getBroad(),col,row,x,y);
    }
    private int getBroadX(float x){
        x = x-(hd/2);
        if(x<0||x>(mWidth-hd)) return -1;
        return (int)(x/hd)+1;
    }
    private int getBroadY(float y){
        y = y-(vd/2);
        if(y<0||y>(mHeight-vd)) return -1;
        return (int)(y/vd)+1;
    }

    public Chess getNowChess() {
        return nowChess;
    }

    public void initNowChess(){
        nowChess.x = -1;
        nowChess.y = -1;
        nowChess.type = Color.GRAY;
    }
    public static class Chess{
        public int x = -1;
        public int y = -1;
        public int type = Color.GRAY;
    }
}

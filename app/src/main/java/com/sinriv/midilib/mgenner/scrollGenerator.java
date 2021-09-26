package com.sinriv.midilib.mgenner;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.util.Log;

class imgBuffer{
    public final Bitmap bitmap;
    public final Canvas canvas;
    public final Paint  paint;
    public final RectF  rect;
    imgBuffer(){
        bitmap = Bitmap.createBitmap(1024,30,Bitmap.Config.RGB_565);
        canvas = new Canvas(bitmap);
        paint = new Paint();
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(3);
        rect = new RectF();
    }
}

//绘制滚动条的预览图
abstract public class scrollGenerator extends mgenner_native{
    imgBuffer buffer,buffer_last;
    public scrollGenerator(){
        buffer = new imgBuffer();
        buffer_last = new imgBuffer();
    }
    private int hlen;
    private int nmax;
    private int nmin;
    protected float noteTimeMax;
    @Override
    protected void scrollBuilder_onGetNoteArea(float noteTimeMax,float noteToneMin,float noteToneMax){
        this.noteTimeMax = noteTimeMax;
        buffer.canvas.drawColor(0xFF000020, PorterDuff.Mode.CLEAR);
        hlen = (int)(noteToneMax-noteToneMin);//纵向的距离
        nmax = (int)(noteToneMax);
        nmin = (int)(noteToneMin);
        if(hlen<30){
            int s =(int)((noteToneMax+noteToneMin)/2);
            hlen = 30;
            nmax = s+15;
            nmin = s-15;
        }
        buffer.paint.setColor(0xFF777777);
    }
    @Override
    protected void scrollBuilder_onGetAllNotePos(float b,float d,float t){
        int x,y,w;
        if(t > nmin && t < nmax){
            y = 30 - (int)(((t - nmin)*30)/hlen);
            w = (int)((d*1024)/noteTimeMax);
            if(w<=0) {
                w = 1;
            }
            x = (int)((b*1024)/noteTimeMax);
            buffer.rect.set(x,y,x+w,y+1);
            buffer.canvas.drawRect(buffer.rect, buffer.paint);
        }
    }

    @Override
    protected void scrollBuilder_onSwap() {
        imgBuffer tmp = buffer;
        buffer = buffer_last;
        buffer_last = tmp;
    }

    public Bitmap updateScroll(){
        buildScroll();
        return buffer_last.bitmap;
    }
}

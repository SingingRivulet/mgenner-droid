package com.sinriv.midilib.mgenner;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import java.util.Date;
import java.util.HashMap;
import java.util.Random;

abstract public class uiView extends scrollGenerator{
    public int windowWidth;
    public int windowHeight;
    public Canvas viewCanvas;
    private final Paint  paint;
    private final RectF  rect;
    private final Rect   rectSrc;
    private final RectF  bd;
    private Bitmap bitmap;
    private float scale_x;
    private final Random rand = new Random(0);
    private int FPS_count = 0;
    private long FPS_time = 0;
    private int FPS = 0;

    boolean selectingByBox = false;
    int selectBoxX,selectBoxXend,selectBoxY,selectBoxYend;

    static int[] pianoKey    = { 1 , 0  , 1 , 0  , 1 , 1 , 0  , 1 , 0  , 1 , 0  , 1};
    static int[] pianoColorR = { 20,10  , 20, 10 , 20, 10, 10 , 20, 10 , 20, 10 , 10};
    static int[] pianoColorG = { 20,10  , 20, 10 , 20, 20, 10 , 20, 10 , 20, 10 , 20};
    static int[] pianoColorB = { 30,20  , 30, 20 , 30, 30, 20 , 30, 20 , 30, 20 , 30};
    static String [] tones = {"C","C#","D","D#","E","F","F#","G","G#","A","A#","B"};
    //自定义的颜色类型
    private static class colorInt{
        byte r,g,b;
        colorInt(byte r,byte g,byte b){
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }
    private final HashMap<String, colorInt> colors = new HashMap<>();

    //接收端的参数
    private String infoFilter;

    public uiView(){
        paint = new Paint();
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(3);
        rect = new RectF();
        bd   = new RectF();
        rectSrc = new Rect(0,0,1024,30);
    }
    public void updateNotes(){
        setScreen(windowWidth,windowHeight);
        scale_x = getNoteLength();
        bitmap = updateScroll();
        infoFilter = getInfoFilter();
        render();
        long time = System.currentTimeMillis()/1000;
        ++FPS_count;
        if(time!=FPS_time){
            FPS_time = time;
            FPS = FPS_count;
            FPS_count = 0;
        }
        paint.setColor(0xFFFFFFFF);
        paint.setTextSize(20);
        viewCanvas.drawText("FPS="+FPS,windowWidth-20*6,50,paint);
    }
    @Override
    protected void drawNote_begin(){
        rect.set(0,0,windowWidth,windowHeight);
        paint.setARGB(255,0 , 0 , 30);
        viewCanvas.drawRect(rect, paint);
    }
    @Override
    protected void drawNote(int fx,int fy,int tx,int ty, int volume,String info,boolean selected,boolean onlydisplay){
        int fontSize = ty-fy;
        rect.set(fx,fy,tx,ty);
        bd.set(fx-2,fy-2,tx+2,ty+2);
        if(onlydisplay) {
            paint.setARGB(255,volume, volume, 30);
            viewCanvas.drawRect(bd, paint);
        }else if(selected){
            paint.setARGB(255,255, 128, 192);
            viewCanvas.drawRect(bd, paint);
        }else if(fontSize>15){
            paint.setARGB(255,0, 0, 0);
            viewCanvas.drawRect(bd, paint);
        }

        if(!info.isEmpty()){
            if(info.charAt(0)!='@'){
                byte r,g,b;
                colorInt it = colors.get(info);
                if(it==null){
                    r= (byte) rand.nextInt(64);
                    g= (byte) rand.nextInt(64);
                    b= (byte) rand.nextInt(64);
                    colors.put(info,new colorInt(r,g,b));
                }else {
                    r = it.r;
                    g = it.g;
                    b = it.b;
                }
                paint.setARGB(255,r+volume, g+volume, b+volume);
            }else {
                paint.setARGB(255,128,128,192);
            }
        }else {
            paint.setARGB(255,64+volume, 64+volume, 64+volume);
        }
        viewCanvas.drawRect(rect, paint);

        if(
                fontSize>=60 ||
                        (
                                (fontSize>=40) &&
                                        !info.isEmpty() &&
                                        infoFilter.isEmpty() &&
                                        (selected || info.charAt(0)=='@')
                        )
        ){
            paint.setTextSize(40);
            paint.setColor(0xFFFFFFFF);
            viewCanvas.drawText(info, fx,fy+40 , paint);
        }
        if(fontSize>=60){
            paint.setTextSize(20);
            paint.setColor(0xFFFFFFFF);
            viewCanvas.drawText("音量："+volume, fx,ty , paint);
        }
    }
    @Override
    protected void drawNote_end(){
        //TODO
        if(selectingByBox){
            int bx,ex,by,ey;
            if(selectBoxX<selectBoxXend){
                bx = selectBoxX;
                ex = selectBoxXend;
            }else{
                ex = selectBoxX;
                bx = selectBoxXend;
            }
            if(selectBoxY<selectBoxYend){
                by = selectBoxY;
                ey = selectBoxYend;
            }else{
                ey = selectBoxY;
                by = selectBoxYend;
            }
            if(!(bx==ex || by==ey)) {
                paint.setColor(0xFFFFFFFF);
                rect.set(bx, by, bx + 1, ey);
                viewCanvas.drawRect(rect, paint);
                rect.set(ex, by, ex + 1, ey);
                viewCanvas.drawRect(rect, paint);
                rect.set(bx, by, ex, by + 1);
                viewCanvas.drawRect(rect, paint);
                rect.set(bx, ey, ex, ey + 1);
                viewCanvas.drawRect(rect, paint);
            }
        }
    }
    @Override
    protected void drawTableRaw(int from,int to,int t){
        int k = t%12;
        rect.set(0,from,windowWidth,to);
        paint.setARGB(255,pianoColorR[k], pianoColorG[k], pianoColorB[k]);
        viewCanvas.drawRect(rect, paint);
        if(t>=0 && t<128){

            rect.set(0,from,60,to);
            if(pianoKey[k]==1) {
                paint.setARGB(255,190, 190, 170);
            }else {
                paint.setARGB(255,0, 0, 0);
            }
            viewCanvas.drawRect(rect, paint);

            if(to-from>=40) {
                paint.setTextSize(40);
                paint.setColor(0xFFFFFFFF);
                String str = tones[k];
                if(k==0){
                    str += " "+(t/12);
                }
                viewCanvas.drawText(str, 0, from + 40, paint);
            }
        }
    }
    @Override
    protected void drawTimeCol(float p){
        rect.set(p,0,p+2,windowHeight);
        paint.setARGB(255,5, 5, 20);
        viewCanvas.drawRect(rect, paint);
    }
    int sectionNum_pos = 250;
    @Override
    protected void drawSectionCol(float p,int n){
        rect.set((int)p,0,(int)(p+3),windowHeight);
        paint.setARGB(255,5, 5, 5);
        viewCanvas.drawRect(rect, paint);

        int size = (int)(scale_x*100);
        if(size<5){
            size = 5;
        }else if(size>40){
            size = 40;
        }
        paint.setTextSize(size);
        paint.setARGB(255,64, 128, 128);
        viewCanvas.drawText(Integer.toString(n), p,windowHeight-sectionNum_pos+40 , paint);
    }
    int tempo_pos = 200;
    int tempo_height = 100;
    @Override
    protected void drawTempo(float p,double t){
        rect.set(p,0,p+1,windowHeight-30);
        paint.setARGB(255,128,64,128);
        viewCanvas.drawRect(rect, paint);
        paint.setARGB(255,128, 64, 128);
        paint.setTextSize(40);
        if(p<=0) {
            viewCanvas.drawText("BPM=" + (int) Math.round(t), p, windowHeight - tempo_pos + 40, paint);
        }else{
            viewCanvas.drawText(Integer.toString((int)Math.round(t)), p, windowHeight - tempo_pos + 40, paint);
        }
    }
    @Override
    protected void drawTempoPadd(){
        rect.set(0,windowHeight-tempo_pos,windowWidth,windowHeight-tempo_pos+tempo_height);
        paint.setARGB(255,0 , 0 , 30);
        viewCanvas.drawRect(rect, paint);
    }
    int scroll_pos = 100;
    int scroll_height = 100;
    @Override
    protected void drawScroll(){
        rect.set(0,windowHeight-scroll_pos,windowWidth,windowHeight-scroll_pos+scroll_height);
        rectSrc.set(0,0,1024,30);
        viewCanvas.drawBitmap(bitmap,rectSrc,rect,paint);
        if(noteTimeMax>0) {
            float x = getLookAtX();
            int rx= (int) ((x*windowWidth)/noteTimeMax);
            rect.set(rx,windowHeight-scroll_pos-5,rx+1,windowHeight-scroll_pos+scroll_height);
            viewCanvas.drawRect(rect, paint);
        }
    }
}

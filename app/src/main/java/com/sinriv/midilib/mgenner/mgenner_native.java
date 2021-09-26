package com.sinriv.midilib.mgenner;

public abstract class mgenner_native {

    abstract protected void onUseInfo(String info);
    abstract protected void onLoadName(String info);
    abstract protected void drawNote_begin();
    abstract protected void drawNote(int fx,int fy,int tx,int ty, int volume,String info,boolean selected,boolean onlydisplay);
    abstract protected void drawNote_end();
    abstract protected void drawTableRaw(int from,int to,int t);
    abstract protected void drawTimeCol(float p);
    abstract protected void drawSectionCol(float p,int n);
    abstract protected void drawTempo(float p,double t);
    abstract protected void drawTempoPadd();
    abstract protected void drawScroll();

    abstract protected void scrollBuilder_onGetNoteArea(float b,float d,float t);
    abstract protected void scrollBuilder_onGetAllNotePos(float b,float d,float t);
    abstract protected void scrollBuilder_onSwap();

    abstract protected void onSetChannelIns(int c,int ins);
    abstract protected void onNoteOn(int channel,int tone,int vol);
    abstract protected void onNoteOff(int channel,int tone);

    abstract protected void onSetDefaultInfo(String info);
    abstract protected void onSetSection(int s);

    private native long init_native();
    private native void destroy_native(long ptr);
    private native void setScreen_native(long ptr,int w,int h);
    private native void render_native(long ptr);
    private native void playStep_native(long ptr);
    private native void playStart_native(long ptr);
    private native void playStop_native(long ptr);
    private native float getLookAtX_native(long ptr);
    private native float getLookAtY_native(long ptr);
    private native void setLookAtX_native(long ptr,float x);
    private native void setLookAtY_native(long ptr,float y);
    private native boolean updateTimeMax_native(long ptr);
    private native void loadMidi_native(long ptr,String path);
    private native void exportMidi_native(long ptr,String path);
    private native void buildScroll_native(long ptr);
    private native String getInfoFilter_native(long ptr);
    private native boolean getPlayingStatus_native(long ptr);
    private native float getNoteHeight_native(long ptr);
    private native float getNoteLength_native(long ptr);
    private native void setNoteLength_native(long ptr,float len);
    private native void setNoteHeight_native(long ptr,float hei);
    private native void clickToDisplay_native(long ptr,int x,int y);
    private native void clickToLookAt_native(long ptr,int x,int y);
    private native void clickToRemoveTempo_native(long ptr,int x,int y);
    private native void clickToSetTempo_native(long ptr,int x,int y,double tp);
    private native void clickToDisplay_close_native(long ptr);
    private native void addDisplaied_native(long ptr);
    private native int clickToSelect_native(long ptr,int x,int y);
    private native int getTPQ_native(long ptr);
    private native void setTPQ_native(long ptr,int tpq);
    private native void undo_native(long ptr);
    private native void removeSelected_native(long ptr);
    private native boolean hideMode_native(long ptr);
    private native void clearSelected_native(long ptr);
    private native void setTableWidth_native(long ptr,float w);
    private native void setSection_native(long ptr,int s);
    private native void setInfo_native(long ptr,String f);
    private native int selectByArea_native(long ptr,int selectBoxX,int selectBoxXend,int selectBoxY,int selectBoxYend);

    long self=0;
    public void init(){
        if(self==0) {
            self = init_native();
        }
    }
    public void destroy(){
        if(self!=0) {
            destroy_native(self);
        }
    }
    public void setScreen(int w,int h){
        setScreen_native(self,w,h);
    }
    public void render(){
        render_native(self);
    }
    public void playStep(){
        playStep_native(self);
    }
    public float getLookAtX(){
        return getLookAtX_native(self);
    }
    public float getLookAtY(){
        return getLookAtY_native(self);
    }
    public void setLookAtX(float x){
        setLookAtX_native(self,x);
    }
    public void setLookAtY(float y){
        setLookAtY_native(self,y);
    }
    public boolean updateTimeMax(){
        return updateTimeMax_native(self);
    }
    public void loadMidi(String path){
        loadMidi_native(self,path);
    }
    public void buildScroll(){
        buildScroll_native(self);
    }
    public String getInfoFilter(){
        return getInfoFilter_native(self);
    }
    public void playStart(){
        playStart_native(self);
    }
    public void playStop(){
        playStop_native(self);
    }
    public boolean getPlayingStatus(){
        return getPlayingStatus_native(self);
    }
    public float getNoteHeight(){
        return getNoteHeight_native(self);
    }
    public float getNoteLength(){
        return getNoteLength_native(self);
    }
    public void clickToDisplay(int x,int y){
        clickToDisplay_native(self,x,y);
    }
    public void clickToDisplay_close(){
        clickToDisplay_close_native(self);
    }
    public void addDisplaied(){
        addDisplaied_native(self);
    }
    public int clickToSelect(int x,int y){
        return clickToSelect_native(self,x,y);
    }
    public void undo(){
        undo_native(self);
    }
    public void removeSelected(){
        removeSelected_native(self);
    }
    public boolean hideMode(){
        return hideMode_native(self);
    }
    public void clearSelected(){
        clearSelected_native(self);
    }
    public int selectByArea(int selectBoxX,int selectBoxXend,int selectBoxY,int selectBoxYend){
        return selectByArea_native(self,selectBoxX, selectBoxXend, selectBoxY, selectBoxYend);
    }
    public void setTableWidth(float w){
        setTableWidth_native(self,w);
    }
    public void clickToLookAt(int x,int y){
        clickToLookAt_native(self,x,y);
    }
    public void clickToRemoveTempo(int x,int y){
        clickToRemoveTempo_native(self,x,y);
    }
    public void clickToSetTempo(int x,int y,double tp){
        clickToSetTempo_native(self,x,y,tp);
    }
    public void setSection(int s){
        setSection_native(self,s);
    }
    public void setInfo(String f){
        setInfo_native(self,f);
    }
    public void exportMidi(String path){
        exportMidi_native(self,path);
    }
    public void setNoteLength(float len){
        setNoteLength_native(self,len);
    }
    public void setNoteHeight(float hei){
        setNoteHeight_native(self,hei);
    }
    public int getTPQ(){
        return getTPQ_native(self);
    }
    public void setTPQ(int tpq){
        setTPQ_native(self,tpq);
    }
}

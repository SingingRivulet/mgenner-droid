package com.sinriv.midilib.midiPlayerService;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class player {
    private long ptr = 0;
    Context context;
    private native long init_native(final String sf, int sampleRate, float gain);
    private native void release_native(long ptr);
    private native void noteOn_native(long ptr,int channel,int key,int vel);
    private native void noteOff_native(long ptr,int channel,int key);
    private native void setProgram_native(long ptr,int channel,int program);
    private native void noteOffAll_native(long ptr,int channel);
    private native void render_float_native(long ptr,float [] arr);
    private native void render_short_native(long ptr,short [] arr);
    public void init(final Context ctx, final String soundFont, float gain) throws Exception {
        context = ctx;
        String dir  = context.getExternalCacheDir().toString();
        String path = dir+"/"+soundFont;
        if(copyAssetsSingleFile(dir,soundFont)) {
            ptr = init_native(path, 44100, gain);
        }
    }
    public void release(){
        if(ptr!=0){
            release_native(ptr);
            ptr = 0;
        }
    }
    public void noteOn(int channel,int key,int vel){
        if(ptr!=0){
            noteOn_native(ptr,channel,key,vel);
        }
    }
    public void noteOff(int channel,int key){
        if(ptr!=0){
            noteOff_native(ptr,channel,key);
        }
    }
    public void noteOffAll(int channel){
        if(ptr!=0){
            noteOffAll_native(ptr,channel);
        }
    }
    public void setProgram(int channel,int program){
        if(ptr!=0){
            setProgram_native(ptr,channel,program);
        }
    }
    public void render_float(float [] arr){
        if(ptr!=0){
            render_float_native(ptr,arr);
        }
    }
    public void render_short(short [] arr){
        if(ptr!=0){
            render_short_native(ptr,arr);
        }
    }
    private boolean copyAssetsSingleFile(String outPath, String fileName) {
        File file = new File(outPath);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e("midiPlayerService", "copyAssetsSingleFile: cannot create directory.");
                return false;
            }
        }
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            File outFile = new File(file, fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(outFile);
            if(outFile.length()>0){
                Log.d("midiPlayerService", "file extracted");
                return true;
            }
            // Transfer bytes from inputStream to fileOutputStream
            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = inputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            inputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}

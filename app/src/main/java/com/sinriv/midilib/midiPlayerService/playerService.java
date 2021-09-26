package com.sinriv.midilib.midiPlayerService;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.sinriv.midilib.midiPlayerAIDL;

import java.util.concurrent.atomic.AtomicBoolean;

public class playerService extends Service {
    static {
        System.loadLibrary("native-lib");
    }

    public player synth;
    short [] playBuffer;

    public playerService() {
        synth = new player();
        playBuffer = new short[128];
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            synth.init(getApplicationContext(),"sndfnt.sf2",0.0f);//gain单位为分贝
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        synth.release();
    }

    public class Finder extends midiPlayerAIDL.Stub{

        @Override
        public void noteOn(int channel, int key, int vel){
            synth.noteOn(channel, key, vel);
        }

        @Override
        public void noteOff(int channel, int key){
            synth.noteOff(channel, key);
        }

        @Override
        public void noteOffAll(int channel){
            synth.noteOffAll(channel);
        }

        @Override
        public void setProgram(int channel, int program){
            synth.setProgram(channel, program);
        }

        @Override
        public void start(){
            startPlay();
        }

        @Override
        public void stop(){
            stopPlay();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Finder();
    }

    //音频播放组件
    AudioTrack audioOut;
    Thread playThread;
    AtomicBoolean running = new AtomicBoolean(true);
    public void startPlay(){
        if(audioOut==null) {
            int audioOut_size = AudioTrack.getMinBufferSize(44100,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            audioOut = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    audioOut_size,
                    AudioTrack.MODE_STREAM);
            audioOut.play();
            running.set(true);
            playThread = new Thread(this::playThread_func);
            playThread.start();
        }
    }

    private void loop(){
        synth.render_short(playBuffer);
        audioOut.write(playBuffer,0,playBuffer.length);
    }
    private void playThread_func(){
        while (running.get()){
            loop();
            //Log.d("playThread_func","loop");
        }
    }

    public void stopPlay(){
        if(audioOut!=null){
            running.set(false);
            if(playThread!=null){
                try {
                    playThread.join();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            audioOut.stop();
            audioOut.release();
            audioOut = null;
        }
    }
}

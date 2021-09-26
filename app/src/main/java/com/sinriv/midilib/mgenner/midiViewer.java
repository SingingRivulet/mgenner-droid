package com.sinriv.midilib.mgenner;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Canvas;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.sinriv.mgenner.R;
import com.sinriv.midilib.midiPlayerAIDL;

public class midiViewer extends View {
    uiView view;
    midiPlayerAIDL remoteService;
    ServiceConnection conn;
    Context context;
    public boolean editMode = false;
    public boolean selectByBox = false;
    public midiViewer(Context context) {
        super(context);
        this.context = context;
        setClickable(true);
    }
    public void init(){
        view = new uiView() {
            @Override
            protected void onUseInfo(String info) {
            }

            @Override
            protected void onLoadName(String info) {
            }

            @Override
            protected void onSetChannelIns(int c, int ins) {
                if(remoteService!=null) {
                    try {
                        remoteService.setProgram(c, ins);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            protected void onNoteOn(int channel, int tone, int vol) {
                if(remoteService!=null) {
                    try {
                        remoteService.noteOn(channel, tone, vol);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            protected void onNoteOff(int channel, int tone) {
                if(remoteService!=null) {
                    try {
                        remoteService.noteOff(channel, tone);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            protected void onSetDefaultInfo(String info) {
                setDefaultInfo(info);
            }

            @Override
            protected void onSetSection(int s) {
                setSectionView(s);
            }
        };
        view.init();
        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d("midiPlayerService", "service connected");
                try {
                    remoteService = midiPlayerAIDL.Stub.asInterface(service);
                    remoteService.start();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d("midiPlayerService", "service disconnected");
            }
        };
        //连接
        Intent service = new Intent();
        service.setPackage("com.sinriv.mgenner");
        service.setAction("com.sinriv.midiPlayerService.playerService");
        context.bindService(service, conn, Context.BIND_AUTO_CREATE);
        //触摸事件
        setOnTouchListener(new OnTouchListener() {
            float point0_x_last=0;
            float point0_y_last=0;
            float point0_x_start=0;
            float point0_y_start=0;
            int last_len_x=0;
            int last_len_y=0;
            float last_len_x_view=0;
            float last_len_y_view=0;
            boolean point0_using   = false;
            boolean selectSingle   = false;
            boolean scrollMode     = false;
            boolean setScaleMode   = false;
            final int minScaleSize = 200;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean allowEdit = true;
                float x = event.getX();
                float y = event.getY();
                int action = event.getAction();
                int count = event.getPointerCount();

                if(count==2) {
                    setScaleMode = true;
                    switch (action & MotionEvent.ACTION_MASK){
                        case MotionEvent.ACTION_MOVE:

                            double now_len_x = Math.abs(event.getX(1)-event.getX(0));
                            double now_len_y = Math.abs(event.getY(1)-event.getY(0));

                            if(now_len_y>minScaleSize) {
                                if (last_len_y <= 0) {
                                    last_len_y = (int)now_len_y;
                                    last_len_y_view = view.getNoteHeight();
                                }else {
                                    if (last_len_y > minScaleSize && now_len_y > minScaleSize) {
                                        double delta_y = now_len_y / last_len_y;
                                        delta_y = (delta_y-1.0)*0.5+1.0;
                                        double scale_y = last_len_y_view * delta_y;
                                        if (scale_y <= 5) {
                                            scale_y = 5;
                                        }
                                        if (scale_y >= 100) {
                                            scale_y = 100;
                                        }
                                        view.setNoteHeight((float) scale_y);
                                    }
                                }
                            }

                            if(now_len_x>minScaleSize) {
                                if (last_len_x <= 0) {
                                    last_len_x = (int)now_len_x;
                                    last_len_x_view = view.getNoteLength();
                                }else {
                                    if (last_len_x > minScaleSize && now_len_x > minScaleSize) {
                                        double delta_x = now_len_x / last_len_x;
                                        delta_x = (delta_x-1.0)*0.5+1.0;
                                        double scale_x = last_len_x_view * delta_x;
                                        if (scale_x <= 0.1) {
                                            scale_x = 0.1;
                                        }
                                        if (scale_x >= 10) {
                                            scale_x = 10;
                                        }
                                        view.setNoteLength((float) scale_x);
                                    }
                                }
                            }

                            break;
                    }
                }else if(count==1) {
                    //处理滚动条和速度条
                    if (action == MotionEvent.ACTION_UP) {
                        point0_using   = false;
                        selectSingle   = false;
                        scrollMode     = false;
                        setScaleMode   = false;
                        last_len_x=0;
                        last_len_y=0;
                    } else if (y > view.windowHeight - view.tempo_pos) {
                        if (y > view.windowHeight - view.scroll_pos) {
                            //滚动
                            switch (action) {
                                case MotionEvent.ACTION_DOWN:
                                    view.clickToLookAt((int) x, (int) y);
                                    scrollMode = true;
                                    break;
                                case MotionEvent.ACTION_MOVE:
                                    view.clickToLookAt((int) x, (int) y);
                                    break;
                            }

                        } else {
                            //设置速度
                            if (editMode && action == MotionEvent.ACTION_DOWN) {
                                tempoSetting((int) x, (int) y);
                            }
                            allowEdit = false;
                        }
                    } else {
                        if (scrollMode) {
                            allowEdit = false;
                            if (action == MotionEvent.ACTION_MOVE) {
                                view.clickToLookAt((int) x, (int) y);
                            }
                        }
                    }

                    if (!setScaleMode && !scrollMode && allowEdit) {
                        if (editMode) {
                            if (selectByBox) {
                                switch (action) {
                                    case MotionEvent.ACTION_DOWN:
                                        point0_using = true;
                                        view.selectBoxX = (int) x;
                                        view.selectBoxY = (int) y;
                                        view.selectBoxXend = (int) x;
                                        view.selectBoxYend = (int) y;
                                        view.selectingByBox = true;
                                        break;
                                    case MotionEvent.ACTION_UP:
                                        point0_using = false;
                                        view.selectBoxXend = (int) x;
                                        view.selectBoxYend = (int) y;
                                        view.selectingByBox = false;
                                        view.selectByArea(view.selectBoxX, (int) x, view.selectBoxY, (int) y);
                                        break;
                                    case MotionEvent.ACTION_MOVE:
                                        view.selectBoxXend = (int) x;
                                        view.selectBoxYend = (int) y;
                                        break;
                                }
                            } else {
                                switch (action) {
                                    case MotionEvent.ACTION_DOWN:
                                        point0_using = true;
                                        point0_x_last = x;
                                        point0_y_last = y;
                                        if (view.clickToSelect((int) x, (int) y) <= 0) {
                                            selectSingle = false;
                                            view.clickToDisplay((int) x, (int) y);
                                        } else {
                                            selectSingle = true;
                                        }
                                        break;
                                    case MotionEvent.ACTION_UP:
                                        point0_using = false;
                                        if (!selectSingle) {
                                            view.addDisplaied();
                                            view.clickToDisplay_close();
                                        }
                                        selectSingle = false;
                                        break;
                                    case MotionEvent.ACTION_MOVE:
                                        point0_x_last = x;
                                        point0_y_last = y;
                                        if (!selectSingle) {
                                            view.clickToDisplay((int) x, (int) y);
                                        }
                                        break;
                                }
                            }
                        } else {
                            switch (action) {
                                case MotionEvent.ACTION_DOWN:
                                    point0_using = true;
                                    point0_x_last = x;
                                    point0_y_last = y;
                                    point0_x_start = x;
                                    point0_y_start = y;
                                    break;
                                case MotionEvent.ACTION_UP:
                                    point0_using = false;
                                    break;
                                case MotionEvent.ACTION_MOVE:
                                    if(point0_using) {
                                        float dx = x - point0_x_last;
                                        float dy = y - point0_y_last;
                                        if(view.getPlayingStatus() && (Math.abs(x-point0_x_start)- Math.abs(y-point0_y_start))<100){
                                            dx = 0;
                                        }
                                        onMoveScreen(dx, dy);
                                    }
                                    point0_x_last = x;
                                    point0_y_last = y;
                                    break;
                            }
                        }
                    }
                }
                v.performClick();
                return false;
            }
        });
    }

    public void destroy(){
        if(view!=null){
            view.destroy();
            view = null;
        }
        if(conn!=null){
            try {
                for(int i=0;i<16;++i) {
                    remoteService.noteOffAll(i);
                }
                remoteService.stop();
                context.unbindService(conn);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        view.viewCanvas   = canvas;
        view.windowHeight = getHeight();
        view.windowWidth  = getWidth();
        view.updateNotes();
        view.playStep();
        invalidate();
    }
    
    protected void onMoveScreen(float dx,float dy){
        float lx = view.getLookAtX();
        float ly = view.getLookAtY();
        float sx = view.getNoteLength();
        float sy = view.getNoteHeight();
        lx -= dx/sx;
        ly += dy/sy;
        view.setLookAtX(lx);
        view.setLookAtY(ly);
    }
    protected void setDefaultInfo(String info){}
    protected void setSectionView(int s){}
    String [] tempoSetting_items = {"添加","删除"};
    protected void tempoSetting(int x,int y){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.tempo_setting_title);
        AlertDialog [] d = {null};
        builder.setItems(tempoSetting_items, (dialog, which) -> {
            switch (which){
                case 0:
                    addTempo(x,y);
                    d[0].cancel();
                    break;
                case 1:
                    view.clickToRemoveTempo(x,y);
                    d[0].cancel();
                    break;
            }
        });
        d[0] = builder.create();
        d[0].show();
    }
    protected void addTempo(int x,int y){
        View v = View.inflate(context, R.layout.midieditor_tempo_add, null);
        final TextView tpview = v.findViewById(R.id.midieditor_tempo_value);
        SeekBar s = v.findViewById(R.id.midieditor_tempo_seekbar);
        final int[] tp = {120};
        s.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        tpview.setText(Integer.toString(progress));
                        tp[0] = progress;
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                }
        );
        s.setProgress(120);
        android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(context);
        b.setTitle("添加速度控制");
        b.setView(v);
        b.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                view.clickToSetTempo(x,y,(double)tp[0]);
            }
        });
        b.create().show();
    }

    public void playStep(){
        view.playStep();
    }
    public void playStart(){
        view.playStart();
    }
    public void playStop(){
        view.playStop();
    }
    public boolean getPlayingStatus(){
        return view.getPlayingStatus();
    }
    public void loadMidi(String path){
        view.loadMidi(path);
    }
}

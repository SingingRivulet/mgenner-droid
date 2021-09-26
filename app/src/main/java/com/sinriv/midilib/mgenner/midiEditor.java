package com.sinriv.midilib.mgenner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sinriv.mgenner.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class midiEditor extends AppCompatActivity {

    static {
        System.loadLibrary("native-lib");
    }
    LinearLayout view_box;
    LinearLayout editTool_box;
    TextView loading_box;
    TextView using_info;
    String using_info_text = "";
    TextView section_view;
    TextView midi_table_width;
    midiViewer viewer;
    ImageButton play_button;
    ImageButton edit_button;
    ImageButton edit_select;
    ImageButton edit_clear;
    ImageButton edit_undo;
    ImageButton edit_focus;
    ImageButton edit_delete;
    ImageButton edit_setting;
    boolean editMode = false;
    boolean selectByBox = false;

    String opendFile = "";
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_midi_editor);
        setTitle(R.string.midiEditor_view);

        using_info = findViewById(R.id.using_info);
        section_view = findViewById(R.id.now_section);
        midi_table_width = findViewById(R.id.midi_table_width);

        viewer = new midiViewer(this){
            @Override
            protected void setDefaultInfo(String info){
                using_info_text = info;
                using_info.setText(info);
            }
            @Override
            protected void setSectionView(int s){
                section_view.setText(Integer.toString(s));
            }
        };
        viewer.init();

        editTool_box = findViewById(R.id.midieditor_editBar);
        editTool_box.setVisibility(View.INVISIBLE);
        view_box = findViewById(R.id.view_box);
        loading_box = findViewById(R.id.loading);

        view_box.removeView(loading_box);
        loading_box = null;
        view_box.addView(viewer);

        //播放键
        play_button = findViewById(R.id.midieditor_play);
        play_button.setOnClickListener(v -> {
            if(viewer.getPlayingStatus()) {
                viewer.playStop();
                play_button.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_baseline_play_arrow_48));
            }else {
                viewer.playStart();
                play_button.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_baseline_pause_48));
            }
        });

        //编辑键
        edit_button = findViewById(R.id.midieditor_edit);
        edit_button.setOnClickListener(v -> {
            if(editMode){
                setTitle(R.string.midiEditor_view);
                edit_button.setBackgroundColor(getResources().getColor(R.color.midieditor_button));
                editMode = false;
                editTool_box.setVisibility(View.INVISIBLE);
            }else {
                setTitle(R.string.midiEditor_edit);
                edit_button.setBackgroundColor(getResources().getColor(R.color.midieditor_button_selected));
                editMode = true;
                editTool_box.setVisibility(View.VISIBLE);
            }
            viewer.editMode = editMode;
        });

        edit_select = findViewById(R.id.midieditor_select);
        edit_select.setOnClickListener(v -> {
            if(selectByBox){
                selectByBox = false;
                viewer.view.selectingByBox = false;
                edit_select.setBackgroundColor(getResources().getColor(R.color.midieditor_button));
            }else{
                selectByBox = true;
                edit_select.setBackgroundColor(getResources().getColor(R.color.midieditor_button_selected));
            }
            viewer.selectByBox = selectByBox;
        });
        edit_clear = findViewById(R.id.midieditor_clear);
        edit_clear.setOnClickListener(v -> viewer.view.clearSelected());
        edit_undo = findViewById(R.id.midieditor_undo);
        edit_undo.setOnClickListener(v -> viewer.view.undo());
        edit_focus = findViewById(R.id.midieditor_focus);
        edit_focus.setOnClickListener(v -> {
            if(viewer.view.hideMode()){
                edit_focus.setBackgroundColor(getResources().getColor(R.color.midieditor_button_selected));
            }else{
                edit_focus.setBackgroundColor(getResources().getColor(R.color.midieditor_button));
            }
        });
        edit_delete = findViewById(R.id.midieditor_delete);
        edit_delete.setOnClickListener(v -> {viewer.view.removeSelected();});
        edit_setting = findViewById(R.id.midieditor_setting);
        edit_setting.setOnClickListener(v -> settingMenu());
        findViewById(R.id.midi_table_width_box).setOnClickListener(v -> setTableWidth_dialog());
        findViewById(R.id.set_section).setOnClickListener(v -> setSection_dialog());
        findViewById(R.id.using_info_box).setOnClickListener(v -> instrument.show(this,viewer.view,using_info_text));
        findViewById(R.id.midieditor_file).setOnClickListener(v -> openFileMenu());

        updateTableWidth();

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        if(name!=null && !name.isEmpty()){
            (new Thread(() -> download(name))).start();
        }else {
            String action = intent.getAction();
            if (Intent.ACTION_VIEW.equals(action)) {
                Uri uri = intent.getData();
                String str = uri.getPath();
                try {
                    openMidiFile(str);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void download(String name){
        String path = downloadFile(name);
        if(path!=null && !path.isEmpty()){
            handler.post(() ->{//回到主线程
                openMidiFile(path);
                opendFile = "";//线上midi禁止编辑
            });
        }
    }
    @Nullable
    private String downloadFile(String name){
        String url = "https://midi.sinriv.com/upload/"+name+".mid";
        String saveName = getExternalCacheDir()+"/"+name+".mid";
        Log.d("mgenner","download:"+url);
        try {
            URL myURL = new URL(url);
            URLConnection conn = myURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            int fileSize = conn.getContentLength();//根据响应获取文件大小
            if (fileSize <= 0){
                return null;
            }
            if (is == null){
                return null;
            }
            //把数据存入路径+文件名
            FileOutputStream fos = new FileOutputStream(saveName);
            int writeNum = 0;
            byte[] buf = new byte[4096];
            while (true){
                //循环读取
                int numread = is.read(buf);
                if (numread == -1){
                    break;
                }
                fos.write(buf, 0, numread);
                writeNum += numread;
            }
        }catch (Exception e){
            return null;
        }
        return saveName;
    }
    private void openMidiFile(String str){
        Log.d("mgenner","openfile:"+str);
        if(fileExist(str)) {
            opendFile = str;
            viewer.loadMidi(str);
            System.gc();
            Log.d("mgenner","load file success:"+str);
        }else {
            Log.e("mgenner","file no found:"+str);
        }
    }

    private boolean fileExist(String path){
        File f=new File(path);
        return f.exists();
    }

    private void saveFile(){
        if(opendFile!=null && !opendFile.isEmpty()) {
            Log.d("mgenner","save file:"+opendFile);
            viewer.view.exportMidi(opendFile);
        }
    }

    static float [] tableWidthDef_val = {1.0f/8.0f , 1.0f/4.0f , 1.0f/2.0f , 1.0f  , 2.0f  , 3.0f  , 4.0f};
    static String[] tableWidthDef_str = {"1/32"    , "1/16"    , "1/8"     , "1/4" , "1/2" , "3/4" , "1"};
    int tableWidthDef_index = 3;
    protected void setTableWidth(int index){
        if(index>=0 && index<tableWidthDef_str.length){
            tableWidthDef_index = index;
            viewer.view.setTableWidth(tableWidthDef_val[index]);
            updateTableWidth();
        }
    }
    protected void updateTableWidth(){
        midi_table_width.setText(tableWidthDef_str[tableWidthDef_index]);
    }
    protected void setTableWidth_dialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.tempo_setting_table_width);
        builder.setItems(tableWidthDef_str, (dialog, which) -> setTableWidth(which));
        builder.show();
    }

    static String[] setSection_dialog_str = {"1/4","2/4","3/4","4/4","5/4","6/4","7/4"};
    protected void setSection_dialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.tempo_setting_section);
        builder.setItems(setSection_dialog_str, (dialog, which) -> viewer.view.setSection(which+1));
        builder.show();
    }

    @Override
    protected void onDestroy() {
        viewer.destroy();
        super.onDestroy();
    }

    private void settingMenu(){
        final View v = View.inflate(this, R.layout.midieditor_setting, null);
        final EditText TPQ = v.findViewById(R.id.midieditor_tpq);
        TPQ.setText(Integer.toString(viewer.view.getTPQ()));
        v.findViewById(R.id.midieditor_tpq_submt).setOnClickListener(v1 -> {
            int newTPQ = Integer.parseInt(TPQ.getText().toString());
            if(newTPQ>0){
                viewer.view.setTPQ(newTPQ);
            }
        });
        android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(this);
        b.setTitle("设置");
        b.setView(v);
        b.create().show();
    }

    String [] menu_no_open = {"打开","另存为"};
    String [] menu_with_open = {"打开","另存为","保存"};
    private void openFileMenu(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("文件");
        String [] menu;
        if(opendFile==null || opendFile.isEmpty()){
            menu = menu_no_open;
        }else {
            menu = menu_with_open;
        }
        builder.setItems(menu, (dialog, which) -> {
            switch (which){
                case 0:
                    pickFile();
                    break;
                case 1:
                    pickDir();
                    break;
                case 2:
                    saveFile();
                    break;
            }
        });
        builder.show();
    }
    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/midi");//无类型限制
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 1);
    }
    private void pickDir(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, 2);
    }
    private void onSelectDir(String path){
        Log.d("mgenner","save path:"+path);
        String defaultValue = "default";
        if(opendFile!=null && !opendFile.isEmpty()) {
            String f = getFileName(opendFile);
            if(f!=null && !f.isEmpty()){
                defaultValue = f;
            }
        }
        final View v = View.inflate(this, R.layout.midieditor_set_file_name, null);
        ((EditText) v.findViewById(R.id.prompt_input_field)).setText(defaultValue);
        android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(this);
        b.setTitle("设置文件名");
        b.setView(v);
        b.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            String value = ((EditText) v.findViewById(R.id.prompt_input_field)).getText().toString();
            opendFile = path+"/"+value+".mid";
            saveFile();
        });
        b.create().show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            switch (requestCode) {
                case 1:
                    if (resultCode == Activity.RESULT_OK) {
                        Uri uri = data.getData();
                        if ("file".equalsIgnoreCase(uri.getScheme())) {//打开
                            String path = uri.getPath();
                            openMidiFile(path);
                        } else {
                            String path = getPath(this, uri);
                            openMidiFile(path);
                        }
                    }
                    break;
                case 2:
                    if (resultCode == Activity.RESULT_OK) {
                        Uri uri = data.getData();
                        Uri docUri = DocumentsContract.buildDocumentUriUsingTree(uri,
                                DocumentsContract.getTreeDocumentId(uri));
                        String path = getPath(this, docUri);
                        onSelectDir(path);
                    }
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private String getPath(final Context context, final Uri uri) {
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if(split.length>=2) {
                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                }else {
                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory().toString();
                    }
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }
    private String getDataColumn(@NonNull Context context, Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private boolean isExternalStorageDocument(@NonNull Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private boolean isDownloadsDocument(@NonNull Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private boolean isMediaDocument(@NonNull Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private String getFileName(String pathandname){
        int start=pathandname.lastIndexOf("/");
        int end=pathandname.lastIndexOf(".");
        if(start!=-1 && end!=-1){
            return pathandname.substring(start+1,end);
        }else{
            return null;
        }
    }
}
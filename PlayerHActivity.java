package com.example.nelo.musicplayerh;

import android.media.MediaPlayer;
import android.os.Build;
import android.os.Environment;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PlayerHActivity extends AppCompatActivity implements View.OnClickListener, ListView.OnItemSelectedListener {

    ListView listView;
    private File[] files;
    private ArrayList<String> arrayList;
    //String sdPath = Environment.getExternalStoragePublicDirectory().getPath();
    String sdPath = "/sdcard/Music";
    Button playButton;
    Button addButton;
    Button stopButton;
    int selectedPosition = -1;
    MediaPlayer mediaPlayer = new MediaPlayer();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_player_h);



        setViews();
        setListeners();

        getExternalStorageDirectories();

        arrayList = new ArrayList<String>();
        searchFile();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,R.layout.row,arrayList);

        listView.setAdapter(arrayAdapter);
    }

    protected void searchFile(){
        //getExternalStorageDirectories();
        //files = new File(sdPath).listFiles(); // 内部ストレージ内のデータ検索 ※不具合発生中
        String path = this.getExternalFilesDirs(null)[0].getPath();
        files =  new File(path).listFiles();  // アプリケーション内のファイル検索ができる
        //files = new File(getMount_sd()).listFiles();  外部ストレージアクセス用




        if (files != null) {

            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile() && files[i].getName().endsWith(".mp3")) {
                    arrayList.add(files[i].getName());
                }
            }
        }
    }

    protected String[] getExternalStorageDirectories() {

        List<String> results = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //Method 1 for KitKat & above
            File[] externalDirs = getExternalFilesDirs(null);

            for (File file : externalDirs) {
                String path = file.getPath().split("/Android")[0];

                boolean addPath = false;

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    addPath = Environment.isExternalStorageRemovable(file);
                }
                else{
                    addPath = Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(file));
                }

                if(addPath){
                    results.add(path);
                }
            }
        }

        if(results.isEmpty()) { //Method 2 for all versions
            // better variation of: http://stackoverflow.com/a/40123073/5002496
            String output = "";
            try {
                final Process process = new ProcessBuilder().command("mount | grep /dev/block/vold")
                        .redirectErrorStream(true).start();
                process.waitFor();
                final InputStream is = process.getInputStream();
                final byte[] buffer = new byte[1024];
                while (is.read(buffer) != -1) {
                    output = output + new String(buffer);
                }
                is.close();
            } catch (final Exception e) {
                e.printStackTrace();
            }
            if(!output.trim().isEmpty()) {
                String devicePoints[] = output.split("\n");
                for(String voldPoint: devicePoints) {
                    results.add(voldPoint.split(" ")[2]);
                }
            }
        }

        //Below few lines is to remove paths which may not be external memory card, like OTG (feel free to comment them out)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < results.size(); i++) {
                if (!results.get(i).toLowerCase().matches(".*[0-9a-f]{4}[-][0-9a-f]{4}")) {
                    //Log.d(LOG_TAG, results.get(i) + " might not be extSDcard"); ログ出力のためのコードなので一旦無効化
                    results.remove(i--);
                }
            }
        } else {
            for (int i = 0; i < results.size(); i++) {
                if (!results.get(i).toLowerCase().contains("ext") && !results.get(i).toLowerCase().contains("sdcard")) {
                    //Log.d(LOG_TAG, results.get(i) + " might not be extSDcard");
                    results.remove(i--);
                }
            }
        }

        String[] storageDirectories = new String[results.size()];
        for(int i=0; i<results.size(); ++i) storageDirectories[i] = results.get(i);

        return storageDirectories;
    }






    protected void setViews(){
        listView = (ListView)findViewById(R.id.listView);
        playButton = (Button)findViewById(R.id.play_btn);
        addButton = (Button)findViewById(R.id.add_btn);
        stopButton = (Button)findViewById(R.id.stop_btn);
    }

    protected void setListeners(){
        playButton.setOnClickListener(this);
        addButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        listView.setOnItemSelectedListener(this);
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.play_btn:
                play_music();
                break;
            case R.id.add_btn:
                add_list();
                break;
            case R.id.stop_btn:
                stop_music();
                break;
        }
    }



    protected void play_music(){
        //create(Context context, Uri uri)　()内を再生するプレイヤーを作成
        //getDuration()　ファイルの長さ
        //setOnCompletionListener( MediaPlayer.OnCompletionListener l)　ファイルのENDに現れたらコールされるメソッド
        //setLooping(boolean looping)　trueでループ、falseでしない
        //mediaPlayer.setDataSource(playlist);//playlistを作成するはボタン2
        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //mediaPlayer.start();


    }

    protected void add_list(){

        ListView listView = (ListView)this.findViewById(R.id.listView);
        this.selectedPosition = listView.getSelectedItemPosition();
        String strRow = (String)listView.getItemAtPosition(this.selectedPosition);//上で選択したものを追加、下でリスト変換

        System.out.println(strRow);
        Toast.makeText(this, strRow, Toast.LENGTH_LONG);

    }

    protected void stop_music(){
        mediaPlayer.stop();
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        this.selectedPosition = position;
        return;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

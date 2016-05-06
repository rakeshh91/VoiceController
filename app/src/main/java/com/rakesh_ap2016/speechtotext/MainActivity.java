package com.rakesh_ap2016.speechtotext;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener2 {
    private TextView txtSpeechInput;
    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private TextView textView;
    private String text;
    private String songName;
    private RelativeLayout textLayout;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 800;
    private ArrayList<Song> songList;
    private MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Voice Controller");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        songList = new ArrayList<Song>();
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        textView = (TextView)findViewById(R.id.textView);
        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
        textLayout = (RelativeLayout)findViewById(R.id.textLayout);

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                text = "";
                promptSpeechInput();
            }
        });

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        250);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        else {

        }
    }

    public void getSongList() {
        //retrieve song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int fullPath = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisFilePath = musicCursor.getString(fullPath);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist,thisFilePath));
            }
            while (musicCursor.moveToNext());
        }
    }
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    protected  void onResume(){
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                "en-IN");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    text = result.get(0).toLowerCase();
                    String[] values={};
                    if(text.contains(" ")) {
                        values = text.split(" ");
                        if (values[1].length() > 0) {
                            songName = values[1].toLowerCase();
                        }
                    }
                    textLayout.setVisibility(View.VISIBLE);
                    txtSpeechInput.setText(text);
                    if((result.get(0).toLowerCase()).equals("play")){
                        Intent intentPlay = new Intent("com.android.music.musicservicecommand");
                        intentPlay.putExtra("command", "play");
                        sendBroadcast(intentPlay);
                    }
                    if((result.get(0).toLowerCase()).equals("next")){
                        Intent intentNext = new Intent("com.android.music.musicservicecommand");
                        intentNext.putExtra("command", "next");
                        sendBroadcast(intentNext);
                    }
                    if((result.get(0).toLowerCase()).equals("previous")){
                        Intent intentPrevious = new Intent("com.android.music.musicservicecommand");
                        intentPrevious.putExtra("command", "previous");
                        sendBroadcast(intentPrevious);
                    }
                    if((result.get(0).toLowerCase()).equals("pause")){
                        if(mediaPlayer.isPlaying()){
                            mediaPlayer.pause();
                        }else {
                            Intent intentPause = new Intent("com.android.music.musicservicecommand");
                            intentPause.putExtra("command", "pause");
                            sendBroadcast(intentPause);
                        }
                    }
                    if(text.contains(" ")) {
                        if (values[0].toLowerCase().equals("play") && values[1].length() > 0) {
                            playSong();
                        }
                    }
                }
                break;
            }

        }
    }

    protected void playSong(){
        getSongList();
        boolean flag = false;
        int position=0;
        for(int i=0;i<songList.size();i++){
            if(songList.get(i).getTitle().toLowerCase().contains(songName)){
                flag = true;
                position=i;
                break;
            }
        }
        if(flag==true){
            String filePath = songList.get(position).getFilePath();
            position = 0;
            mediaPlayer = new MediaPlayer();
            File file = new File(filePath);
            try {
                mediaPlayer.setDataSource(String.valueOf(Uri.fromFile(file)));
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.start();
        }else{
            txtSpeechInput.setTextSize(16);
            txtSpeechInput.setText("Audio file with name '"+ songName+"' is not found");
            position = 0;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 250: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.


                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFlushCompleted(Sensor sensor) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;
        if(mySensor.getType()==Sensor.TYPE_ACCELEROMETER){
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float speed  = Math.abs(x+y+z-last_x-last_y-last_z)/diffTime*10000;
                if(speed>SHAKE_THRESHOLD){
                    textLayout.setVisibility(View.VISIBLE);
                    txtSpeechInput.setText("Shake Detected - Play Next Song");
                    Intent intentNext = new Intent("com.android.music.musicservicecommand");
                    intentNext.putExtra("command", "next");
                    sendBroadcast(intentNext);
                }
                last_x = x;
                last_y= y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

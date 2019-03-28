package com.tools.prog;

import java.io.IOException;

import java.util.ArrayList;
import java.util.TreeMap;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnPreparedListener, OnCompletionListener {
    ListView _dynamic;
    final String LOG_TAG = "myLogs";
    TreeMap<String,String> song;
    ArrayList<String> paths;
    ArrayList<String> names;
    String DATA_SD = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            + "/music.mp3";


    MediaPlayer mediaPlayer;
    AudioManager am;
    CheckBox chbLoop;
    ArrayList<String> list;
    boolean bluethooth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       //////////////////////////////////////
        if(savedInstanceState!=null)
        savedInstanceState.getBoolean("bluetooth");
       list = new ArrayList<String>();
       _dynamic=(ListView)findViewById(R.id.dynamic);
        paths=new ArrayList<>();
        names=new ArrayList<>();
       song=new TreeMap<>();
        //////////////////////////////////////
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        chbLoop = (CheckBox) findViewById(R.id.chbLoop);
        chbLoop.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (mediaPlayer != null)
                    mediaPlayer.setLooping(isChecked);
            }
        });
        _dynamic.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.d(LOG_TAG, _dynamic.getAdapter().getItem(position)+"");
                Log.d(LOG_TAG, song.get(_dynamic.getAdapter().getItem(position))+"");
                DATA_SD=song.get(_dynamic.getAdapter().getItem(position));

            }
        });
        _dynamic.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Log.d(LOG_TAG, "itemSelect: position = " + position + ", id = "
                        + id);
                DATA_SD=song.get(_dynamic.getAdapter().getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(LOG_TAG, "itemSelect: nothing");
            }

        });
    }
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(
            final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context,
                            Manifest.permission.READ_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }
    public void showDialog(final String msg, final Context context,
                           final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[] { permission },
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                } else {
                    Toast.makeText(MainActivity.this, "GET_ACCOUNTS Denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }


    public void onClickStart(View view) {
        releaseMP();

        try {
            switch (view.getId()) {
                case R.id.search:
                    if (checkPermissionREAD_EXTERNAL_STORAGE(this)) {
                        // do your stuff..

                    Log.d(LOG_TAG, "start Search");
                    String[] STAR = { "*" };
                    Uri allaudiosong = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    String audioselection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
                     Cursor cursor;
                     cursor = managedQuery(allaudiosong, STAR, audioselection, null, null);
                     if (cursor != null) {
                         if (cursor.moveToFirst()) {
                             do {
                                 String song_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                                 Log.d(LOG_TAG,"Audio Song Name= " + song_name);
                                 int song_id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                                 Log.d(LOG_TAG, "Audio Song ID= " + song_id);
                                 String fullpath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                                 Log.d(LOG_TAG, "Audio Song FullPath= " + fullpath);
                                 DATA_SD=fullpath;
                                 String album_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                                 Log.d(LOG_TAG, "Audio Album Name= " + album_name);
                                 int album_id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                                 Log.d(LOG_TAG, "Audio Album Id= " + album_id);
                                 String artist_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                                 Log.d(LOG_TAG, "Audio Artist Name= " + artist_name);
                                 int artist_id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
                                 Log.d(LOG_TAG, "Audio Artist ID= " + artist_id);
                                 song.put(song_name.split(".mp3")[0],fullpath);
                                     names.add(song_name.split(".mp3")[0]);

                                 ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                                         android.R.layout.simple_list_item_1, names);

                                 // присваиваем адаптер списку
                                 _dynamic.setAdapter(adapter);
                                    }
                                      while (cursor.moveToNext());
                                    }
                                 }
                                }
                             Log.d("searched","searched");

                             break;
                case R.id.btnStop:
                    startActivity(new Intent(this, BluetoothConectActivity.class));
                    break;

                             case R.id.btnStartSD:

                                         // disable speakerphone button
                                         am.setSpeakerphoneOn(false);

                                         mediaPlayer = new MediaPlayer();
                                         mediaPlayer.setDataSource(DATA_SD);
                                         mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                         mediaPlayer.prepare();
                                         mediaPlayer.start();

                                 Log.d(LOG_TAG, "start SD");

                                 break;



                         }


        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mediaPlayer == null)
            return;

        mediaPlayer.setLooping(chbLoop.isChecked());
        mediaPlayer.setOnCompletionListener(this);
    }

    private void releaseMP() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onClick(View view) {
        if (mediaPlayer == null)
            return;
        switch (view.getId()) {
            case R.id.btnPause:
                if (mediaPlayer.isPlaying())
                    mediaPlayer.pause();
                break;
            case R.id.btnResume:
                if (!mediaPlayer.isPlaying())
                    mediaPlayer.start();
                break;

            case R.id.btnBackward:
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 3000);
                break;
            case R.id.btnForward:
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 3000);
                break;
            case R.id.btnInfo:
                Log.d(LOG_TAG, "Playing " + mediaPlayer.isPlaying());
                Log.d(LOG_TAG, "Time " + mediaPlayer.getCurrentPosition() + " / "
                        + mediaPlayer.getDuration());
                Log.d(LOG_TAG, "Looping " + mediaPlayer.isLooping());
                Log.d(LOG_TAG,
                        "Volume " + am.getStreamVolume(AudioManager.STREAM_MUSIC));
                break;

        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(LOG_TAG, "onPrepared");
        mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(LOG_TAG, "onCompletion");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMP();
    }

}

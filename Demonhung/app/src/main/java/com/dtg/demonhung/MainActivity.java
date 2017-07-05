package com.dtg.demonhung;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AudioManager.OnAudioFocusChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName() + "_dtg: ";

    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private TextView tvNhietDo;
    private TextView tvDoAm;
    private TextView tvCO;
    private TextView tvSmoke;
    private TextView tvLPG;
    private TextView tvStatus;
    private Button btnTatBaoDong;
    private Data data;

    private boolean baoDong = false;
    private boolean doiTrangThai = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        createThreadMedia();
        data = new Data();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setUpFireBase();
        releaseMediaPlayer();
        tvStatus.setText(getString(R.string.status_binh_thuong));
        tvStatus.setTextColor(Color.BLUE);
        setButtonCanhBaoVisible(false);
    }

    private void initViews() {
        tvCO = (TextView) findViewById(R.id.tv_co);
        tvDoAm = (TextView) findViewById(R.id.tv_do_am);
        tvLPG = (TextView) findViewById(R.id.tv_lpg);
        tvNhietDo = (TextView) findViewById(R.id.tv_nhiet_do);
        tvSmoke = (TextView) findViewById(R.id.tv_smoke);
        tvStatus = (TextView) findViewById(R.id.tv_status);
        btnTatBaoDong = (Button) findViewById(R.id.btn_tat_bao_dong);
        btnTatBaoDong.setOnClickListener(this);
    }

    private void setUpFireBase() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().getRoot();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator iterator = dataSnapshot.getChildren().iterator();

                while (iterator.hasNext()) {
                    data.setCo(Float.parseFloat(((DataSnapshot) iterator.next()).getValue().toString()));
                    data.setDoAm(Float.parseFloat(((DataSnapshot) iterator.next()).getValue().toString()));
                    data.setLpg(Float.parseFloat(((DataSnapshot) iterator.next()).getValue().toString()));
                    data.setNhietDo(Float.parseFloat(((DataSnapshot) iterator.next()).getValue().toString()));
                    data.setSmoke(Float.parseFloat(((DataSnapshot) iterator.next()).getValue().toString()));
                }

                tvCO.setText(String.valueOf(data.getCo()) + " ppm");
                tvSmoke.setText(String.valueOf(data.getSmoke()) + " ppm");
                tvLPG.setText(String.valueOf(data.getLpg()) + " ppm");
                tvNhietDo.setText(String.valueOf(data.getNhietDo()) + " độ C");
                tvDoAm.setText(String.valueOf(data.getDoAm()) + " %");

                if (data.getCo() > 5000f || data.getLpg() > 5000f || data.getSmoke() > 5000f) {
                    if(!doiTrangThai) {
                        tvStatus.setText(getString(R.string.status_chay));
                        tvStatus.setTextColor(Color.RED);
                        doiTrangThai=true;
                        createAlarmNotification();
                        setButtonCanhBaoVisible(true);
                        baoDong = true;
                    }
                } else {
                    if(doiTrangThai){
                        baoDong = false;
                        tvStatus.setText(getString(R.string.status_binh_thuong));
                        tvStatus.setTextColor(Color.BLUE);
                        doiTrangThai = false;
                        releaseMediaPlayer();
                        setButtonCanhBaoVisible(false);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Data snapshot error", "" + databaseError);
            }
        });
    }


    @Override
    protected void onStop() {
        super.onStop();
        releaseMediaPlayer();
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            audioManager.abandonAudioFocus(this);
        }
    }

    @Override
    public void onClick(View v) {
        mediaPlayer.pause();
        audioManager.abandonAudioFocus(this);
        setButtonCanhBaoVisible(false);
    }

    private void setButtonCanhBaoVisible(boolean visible) {
        if (visible) {
            btnTatBaoDong.setClickable(true);
            btnTatBaoDong.setVisibility(View.VISIBLE);
        } else {
            btnTatBaoDong.setClickable(false);
            btnTatBaoDong.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            mediaPlayer.release();
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            mediaPlayer.start();
        }
    }

    private void playMusic() {
        if (mediaPlayer == null) {
            int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mediaPlayer = MediaPlayer.create(this, R.raw.chay_nha);
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            }
        }
    }

    private void createThreadMedia() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (baoDong) {
                        playMusic();
                    }
                }
            }
        });
        thread.start();
    }

    private void createAlarmNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.chay_icon);
        builder.setContentTitle("Hệ thống nhúng");
        builder.setContentInfo("Cháy rồi nhé!");

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }
}

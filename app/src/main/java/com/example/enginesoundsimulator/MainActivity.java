package com.example.enginesoundsimulator;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private LocationManager locationManager;
    private TextView speedText;
    private TextView rpmText;

    private MediaPlayer mediaPlayer;

    private float currentSpeed = 0f;

    private final int minRPM = 800;
    private final int maxRPM = 6000;

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private AudioManager audioManager;
    private AudioFocusRequest focusRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        speedText = findViewById(R.id.speedText);
        rpmText = findViewById(R.id.rpmText);

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

            focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(focusChange -> {
                    // Optional: handle audio focus changes here
                })
                .build();

            audioManager.requestAudioFocus(focusRequest);
        } else {
            audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
        }

        mediaPlayer = MediaPlayer.create(this, R.raw.engine_loop);
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(0.5f, 0.5f);  // Mix volume so other audio can play too
        mediaPlayer.start();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0f, this);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        currentSpeed = location.getSpeed();
        float speedKmh = currentSpeed * 3.6f;
        speedText.setText(String.format("Speed: %.1f km/h", speedKmh));
        int rpm = Math.min(maxRPM, Math.max(minRPM,
                minRPM + (int) ((speedKmh / 200f) * (maxRPM - minRPM))));
        rpmText.setText("Simulated RPM: " + rpm);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            float playbackSpeed = 0.5f + ((float) (rpm - minRPM) / (maxRPM - minRPM)) * 1.5f;
            playbackSpeed = Math.max(0.5f, Math.min(playbackSpeed, 2.0f));
            PlaybackParams params = new PlaybackParams();
            params.setSpeed(playbackSpeed);
            mediaPlayer.setPlaybackParams(params);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            1000, 0f, this);
                }
            } else {
                speedText.setText("Location permission denied.");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        locationManager.removeUpdates(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(focusRequest);
        } else {
            audioManager.abandonAudioFocus(null);
        }
    }

    // Unused LocationListener methods
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }
    @Override
    public void onProviderEnabled(@NonNull String provider) { }
    @Override
    public void onProviderDisabled(@NonNull String provider) { }
}

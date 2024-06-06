package kr.ac.cu.moai.dcumusicplayer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.Objects;

public class PlayerActivity extends AppCompatActivity {
    MediaPlayer mediaPlayer;
    String mp3file;
    SeekBar seekBar;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        mp3file = intent.getStringExtra("mp3");

        ImageView ivCover = findViewById(R.id.ivCover);
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvDuration = findViewById(R.id.tvDuration);
        TextView tvArtist = findViewById(R.id.tvArtist);
        Button playButton = findViewById(R.id.play_button);
        Button pauseButton = findViewById(R.id.pause_button);
        seekBar = findViewById(R.id.seekBar);

        try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            retriever.setDataSource(mp3file);
            byte[] b = retriever.getEmbeddedPicture();
            Bitmap cover = BitmapFactory.decodeByteArray(b, 0, b.length);
            ivCover.setImageBitmap(cover);

            String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            tvTitle.setText(title);

            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            tvDuration.setText(duration);

            String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            tvArtist.setText(artist);
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Error loading metadata", Toast.LENGTH_SHORT).show();
            throw new RuntimeException(e);
        }

        playButton.setOnClickListener(view -> {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(mp3file);
                    mediaPlayer.prepare();
                    seekBar.setMax(mediaPlayer.getDuration());
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Error preparing media player", Toast.LENGTH_SHORT).show();
                }
            }
            mediaPlayer.start();
            updateSeekBar();
        });

        pauseButton.setOnClickListener(view -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    mediaPlayer.pause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                }
            }
        });
    }

    private void updateSeekBar() {
        if (mediaPlayer != null) {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            handler.postDelayed(this::updateSeekBar, 1000);
        }
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}

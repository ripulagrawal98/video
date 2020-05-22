package com.nit.video;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class LocalPlayer extends AppCompatActivity {

    private VideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_player);

        mVideoView = findViewById(R.id.videoView1);

        String uri_format = getIntent().getStringExtra("URI_VIDEO");
        Uri uri = Uri.parse(uri_format);


        MediaController controller = new MediaController(this);
        controller.setAnchorView(mVideoView);
        mVideoView.setMediaController(controller);
        mVideoView.setVideoURI(uri);
        mVideoView.start();




    }




}

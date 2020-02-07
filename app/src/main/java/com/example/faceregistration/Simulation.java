package com.example.faceregistration;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.dinuscxj.progressbar.CircleProgressBar;

public class Simulation extends AppCompatActivity {
    private CircleProgressBar mCustomProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simulation);

        Intent intent = getIntent();
        final Privacy privacy = (Privacy)intent.getSerializableExtra("class");
        TextView textView4 = (TextView) findViewById(R.id.textView4);
        textView4.setText(privacy.getName()+privacy.getBirthdate()+privacy.getEmail()+privacy.getGender());


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ImageView image=(ImageView)findViewById(R.id.checkimg);
        Animation anima= AnimationUtils.loadAnimation(this,R.anim.fadeinimage);
        image.startAnimation(anima);

        TextView textView2=(TextView)findViewById(R.id.turn);
        Animation anim3s=AnimationUtils.loadAnimation(this, R.anim.fadeintext);
        textView2.startAnimation(anim3s);

        Button okbutton = (Button)findViewById(R.id.okbutton);
        Animation animaok=AnimationUtils.loadAnimation(this,R.anim.fadeinimage);
        okbutton.startAnimation(animaok);


        mCustomProgressBar = (CircleProgressBar)findViewById(R.id.progressBar);
        VideoView videoView = (VideoView) findViewById(R.id.videoView1);
        //MediaController mediaController = new MediaController(this);
        // mediaController.setAnchorView(videoView);
        Uri video = Uri.parse("android.resource://" + getPackageName() + "/raw/face");

        //videoView.setMediaController(mediaController);
        videoView.setVideoURI(video);
        videoView.requestFocus();
        videoView.start();

        mCustomProgressBar.setProgressFormatter(null);

        Button button = (Button)findViewById(R.id.okbutton);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Privacy privacy2 = new Privacy(privacy.getName(), privacy.getBirthdate(), privacy.getEmail(), privacy.getGender());
                Intent intentSend2 = new Intent(getApplicationContext(), FaceCamera2.class);
                intentSend2.putExtra("class", privacy2);
                startActivity(intentSend2);
//                startActivity(new Intent(getApplicationContext(), FaceCamera2.class));

            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        simulateProgress();
    }

    private void simulateProgress() {
        ValueAnimator animator = ValueAnimator.ofInt(0, -100);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int progress = (int) animation.getAnimatedValue();
                mCustomProgressBar.setProgress(progress);
            }
        });
        animator.setDuration(10000);
        animator.start();
    }

}

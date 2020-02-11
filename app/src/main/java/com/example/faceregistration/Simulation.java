package com.example.faceregistration;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.hardware.camera2.params.Face;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dinuscxj.progressbar.CircleProgressBar;

public class Simulation extends AppCompatActivity {
    private CircleProgressBar customProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simulation);

        // 데이터 수신
        Intent intent = getIntent();
        final Privacy privacy = (Privacy)intent.getSerializableExtra("class");

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 시간에 따른 안내멘트 및 확인 버튼
        // 체크 이미지
        ImageView image=findViewById(R.id.checkimg);
        Animation image_ani= AnimationUtils.loadAnimation(this,R.anim.fadeinimage);
        image.startAnimation(image_ani);

        // 얼굴을 돌리세요
        TextView turn =findViewById(R.id.turn);
        Animation anim3s=AnimationUtils.loadAnimation(this, R.anim.fadeintext);
        turn.startAnimation(anim3s);

        // 완료되었습니다.
        TextView complete = findViewById(R.id.complete);
        Animation complete_ani = AnimationUtils.loadAnimation(this, R.anim.fadeinimage);
        complete.startAnimation(complete_ani);

        // 다음화면으로 넘어가는 확인버튼
        Button sim_ok = findViewById(R.id.sim_ok);
        Animation ok_ani =AnimationUtils.loadAnimation(this,R.anim.fadeinimage);
        sim_ok.startAnimation(ok_ani);

        // 시뮬레이션 동영상 재생
        VideoView videoView = findViewById(R.id.faceVideo);
        Uri video = Uri.parse("android.resource://" + getPackageName() + "/raw/face");
        videoView.setVideoURI(video);
        videoView.requestFocus();
        videoView.start();

        // 프로그레스 바
        customProgressBar = findViewById(R.id.progressBar);
        customProgressBar.setProgressFormatter(null);

        // 다음화면으로 데이터 전송
        sim_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Privacy privacy1 = new Privacy(privacy.getName(), privacy.getBirthdate(), privacy.getEmail(), privacy.getGender(), privacy.getAgreement());
                Intent intentSend1 = new Intent(getApplicationContext(), FaceCamera.class);
                intentSend1.putExtra("class", privacy1);
                startActivity(intentSend1);
                finish();
            }
        });

    }
    private void simulateProgress(){
        ValueAnimator animator = ValueAnimator.ofInt(0, -100);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int progress = (int) animation.getAnimatedValue();
                customProgressBar.setProgress(progress);
            }
        });
        animator.setDuration(10000);
        animator.start();
    }

    @Override
    protected void onResume(){
        super.onResume();
        simulateProgress();
    }
}

package com.example.faceregistration;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.dinuscxj.progressbar.CircleProgressBar;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class FaceCamera2 extends AppCompatActivity {

    //권한
    private static final int REQUEST_USED_PERMISSION = 200;
    private static final String[] needPermissions={Manifest.permission.CAMERA};

    // 데이터베이스 연결
    // 에뮬레이터 10.0.2.2
    private static String IP_ADDRESS = "10.0.2.2";
    private static String TAG = "phptest";

    // 프로그래스바
    private CircleProgressBar mCustomProgressBar;

    // 녹화
    private Size previewSize;
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder previewBuilder;
    private CameraCaptureSession previewSession;
    private TextureView textureView;
    private MediaRecorder mediaRecorder;
    String recordFilePath;

    // 권한 확인
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean permissionToRecordAccepted = true;

        if (requestCode == REQUEST_USED_PERMISSION) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    permissionToRecordAccepted = false;
                    break;
                }
            }
        }
        if(!permissionToRecordAccepted){
            finish();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face_camera2);

        // 개인정보 데이터 수신
        Intent intent = getIntent();
        final Privacy privacy = (Privacy)intent.getSerializableExtra("class");

        // 카운트다운
        final TextView text_timer = findViewById(R.id.text_timer);

        new CountDownTimer(4000, 1000) {
            public void onTick(long millisUntilFinished) {
                text_timer.setText(String.format(Locale.getDefault(), "%d", millisUntilFinished / 1000L));
            }
            public void onFinish() {
                text_timer.setVisibility(View.GONE);
                simulateProgress();

                if(isRecording()){
                    //stopRecording(true);
                    startActivity(new Intent(getApplicationContext(), FinishActivity.class));
                } else{
                    startRecording();
                }
            }
        }.start();

        for(String permission : needPermissions){
            if(ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, needPermissions, REQUEST_USED_PERMISSION);
                break;
            }
        }

        // 카메라
        textureView = findViewById(R.id.cameraTextureView);

        //프로그래스바
        mCustomProgressBar = findViewById(R.id.progressBar2);
        mCustomProgressBar.setProgressFormatter(null);

        // 시간에 따른 안내멘트 및 확인 버튼
        ImageView image2 = findViewById(R.id.checkimg2);
        Animation image_ani2= AnimationUtils.loadAnimation(this,R.anim.fadeinbutton);
        image2.startAnimation(image_ani2);

        TextView turn2 =findViewById(R.id.turn2);
        Animation anim3s2 =AnimationUtils.loadAnimation(this, R.anim.fadeintext2);
        turn2.startAnimation(anim3s2);

        TextView complete2 = findViewById(R.id.complete2);
        Animation complete_ani2 = AnimationUtils.loadAnimation(this, R.anim.fadeinbutton);
        complete2.startAnimation(complete_ani2);

        Button camera_ok = findViewById(R.id.camera_ok);
        Animation animation1 = AnimationUtils.loadAnimation(FaceCamera2.this, R.anim.fadeinbutton);
        camera_ok.startAnimation(animation1);

        camera_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 전달받은 데이터
                String name = privacy.getName();
                String birth = privacy.getBirthdate();
                String email = privacy.getEmail();
                String gender = privacy.getGender();
                String agreement = privacy.getAgreement();

                //데이터 전송
                InsertData task = new InsertData();
                task.execute("http://"+IP_ADDRESS+"/insert.php", name, birth, email, gender, agreement, recordFilePath);
                startActivity(new Intent(getApplicationContext(), FinishActivity.class));
                finish();
            }
        });
    }

    private void simulateProgress() {
        ValueAnimator animator = ValueAnimator.ofInt(0,-100);
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

    @Override
    protected void onResume() {
        super.onResume();
        startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRecording();
    }

    private void startPreview(){
        if(textureView.isAvailable()){
            openCamera();
        } else{
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    private void stopPreview(){
        if(previewSession != null){
            previewSession.close();
            previewSession = null;
        }
        if(cameraDevice != null){
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    private void openCamera(){
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);

        try {
            String backCameraId = null;

            for(final String cameraId : manager.getCameraIdList()){
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                int cameraOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if(cameraOrientation == CameraCharacteristics.LENS_FACING_FRONT){
                    backCameraId = cameraId;
                    break;
                }
            }

            if (backCameraId == null) {
                return;
            }

            CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(backCameraId);

            StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            previewSize = streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0];

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.openCamera(backCameraId, deviceStateCallback, null);
        } catch (CameraAccessException e){
            e.printStackTrace();
        }
    }

    private void showPreview() {
        SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
        Surface surface = new Surface(surfaceTexture);

        try {
            previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Arrays.asList(surface), captureStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        try {
            previewSession.setRepeatingRequest(previewBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private boolean isRecording() {
        return mediaRecorder != null;
    }

    private void startRecording() {
        if (mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();
        }

        recordFilePath = getOutputMediaFile().getAbsolutePath(); // 저장할 파일 경로

        mediaRecorder.setVideoEncodingBitRate(5000000); //전송률
//        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); //음성 소스 설정
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE); //비디오 소스 설정
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); //비디오 포맷 설정
        mediaRecorder.setVideoFrameRate(24); //프레임 설정
        CamcorderProfile camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        if(camcorderProfile.videoFrameWidth > previewSize.getWidth() || camcorderProfile.videoFrameHeight > previewSize.getHeight()){
            camcorderProfile.videoFrameWidth = previewSize.getWidth();
            camcorderProfile.videoFrameHeight = previewSize.getHeight();
        }
        mediaRecorder.setVideoSize(camcorderProfile.videoFrameWidth, camcorderProfile.videoFrameHeight);
//        mediaRecorder.setVideoSize(previewSize.getWidth(), previewSize.getHeight()); //비디오 사이즈 설정
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264); //비디오 인코더
//        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC); //오디오 인코더
        mediaRecorder.setOutputFile(recordFilePath); // 저장할 파일 전체 경로
        mediaRecorder.setOrientationHint(270); // 비디오 방향 설정
        mediaRecorder.setMaxDuration(100000); // 녹화시간 10초

        try {
            mediaRecorder.prepare(); // 미디어 레코더 녹화 준비
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        List<Surface> surfaces = new ArrayList<>(); // Surface 리스트

        SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
        Surface previewSurface = new Surface(surfaceTexture);
        surfaces.add(previewSurface); // 텍스처뷰의 surface 를 리스트에 추가

        Surface mediaRecorderSurface = mediaRecorder.getSurface();
        surfaces.add(mediaRecorderSurface); // 미디어 레코더의 surface 를 리스트에 추가

        try {
            // 프리뷰 빌더를 레코드 모드로 설정
            previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);

            previewBuilder.addTarget(previewSurface);
            previewBuilder.addTarget(mediaRecorderSurface); // 프리뷰 빌더에 텍스처뷰와 미디어레코드의 surface 추가

            cameraDevice.createCaptureSession(surfaces, captureStateCallback, null); // 캡처 세션 생성

            mediaRecorder.start(); // 미디어 레코더 녹화 시작
//            mediaRecorder.setOrientationHint(90);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

     // 데이터 전송
    class InsertData extends AsyncTask<String, Void, String> {
//        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

//            progressDialog = ProgressDialog.show(FaceCamera2.this, "Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
//            progressDialog.dismiss();
            Log.d(TAG, "POst response");
        }

        @Override
        protected String doInBackground(String... params) {
            String name = params[1];
            String birth = params[2];
            String email = params[3];
            String gender = params[4];
            String video = params[5];
            String agreement = params[6];
            String serverURL = params[0];
            String postParameters = "name="+name+"&birth="+birth+"&email="+email+"&gender="+gender+"&video="+video+"&agreement="+agreement;

            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "POST response code - "+responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode==HttpURLConnection.HTTP_OK){
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line=bufferedReader.readLine())!=null){
                    Log.d(TAG, line);
                    stringBuilder.append(line);
                }

                bufferedReader.close();
                return stringBuilder.toString();

            } catch (Exception e){
                Log.d(TAG, "InsertData: Error", e);
                return "Error: " + e.getMessage();
            }
        }
    }

    private void stopRecording() {

        stopPreview();

        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }

    }

    private File getOutputMediaFile(){ // 녹화 파일을 리턴하는 메서드
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HHmmss", Locale.KOREA);
        Date date = new Date();
        String time = format.format(date);
        String recordPath = getExternalCacheDir().getAbsolutePath();
        return new File(recordPath + File.separator + time + ".mp4");
    }

    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    private CameraDevice.StateCallback deviceStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice camera) {
            cameraDevice = camera;
            showPreview();
        }

        @Override
        public void onClosed(@NonNull CameraDevice camera) {
            super.onClosed(camera);
            stopRecording();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {

        }

        @Override
        public void onError(CameraDevice camera, int error) {

        }
    };

    private CameraCaptureSession.StateCallback captureStateCallback = new CameraCaptureSession.StateCallback() {

        @Override
        public void onConfigured(CameraCaptureSession session) {
            previewSession = session;
            updatePreview();
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {

        }
    };
}

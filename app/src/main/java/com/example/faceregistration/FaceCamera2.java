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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class FaceCamera2 extends AppCompatActivity {
    private static final int REQUEST_USED_PERMISSION = 200;
    private static String IP_ADDRESS = "10.0.2.2";
    private static String TAG = "phptest";


    // 프로그래스바
    private CircleProgressBar mCustomProgressBar;
    private TextView textView;
    private TextView textEnd;

    //권한
    private static final String[] needPermissions={
            Manifest.permission.CAMERA
    };

    // 녹화
    private Size previewSize;
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder previewBuilder;
    private CameraCaptureSession previewSession;
    private Button button;
    private TextureView textureView;
    private MediaRecorder mediaRecorder;
    String recordFilePath;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean permissionToRecordAccepted = true;

        switch (requestCode){
            case REQUEST_USED_PERMISSION:
                for(int result : grantResults){
                    if(result != PackageManager.PERMISSION_GRANTED){
                        permissionToRecordAccepted = false;
                        break;
                    }
                }
                break;
        }
        if(permissionToRecordAccepted == false){
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
        TextView textView3 = (TextView)findViewById(R.id.textView3);
        textView3.setText(privacy.getName()+privacy.getBirthdate()+privacy.getEmail()+privacy.getGender());

        final TextView text_timer = (TextView)findViewById(R.id.text_timer);

        CountDownTimer countDownTimer = new CountDownTimer(4000, 1000) {
            public void onTick(long millisUntilFinished) {
                text_timer.setText(String.format(Locale.getDefault(), "%d 초", millisUntilFinished / 1000L));
            }
            public void onFinish() {
                text_timer.setText("녹화 시작");
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

        //녹화
        textureView = (TextureView) findViewById(R.id.cameraTextureView);
        button = (Button) findViewById(R.id.button);

        //프로그래스바
        mCustomProgressBar = findViewById(R.id.progressBar);
        mCustomProgressBar.setProgressFormatter(null);
        textView = findViewById(R.id.textView);
        textEnd=findViewById(R.id.end_time);


        Animation animation = AnimationUtils.loadAnimation(FaceCamera2.this, R.anim.fadeintext2);
        textView.startAnimation(animation);

        Animation animation1 = AnimationUtils.loadAnimation(FaceCamera2.this, R.anim.fadeinbutton);
        button.startAnimation(animation1);
        textEnd.startAnimation(animation1);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = privacy.getName();
                Log.d(TAG, privacy.getName());
                String birth = privacy.getBirthdate();
                String email = privacy.getEmail();
                String gender = privacy.getGender();
                Log.d(TAG, name+birth+email+gender+recordFilePath);

                InsertData task = new InsertData();
                task.execute("http://"+IP_ADDRESS+"/insert.php", name, birth, email, gender, recordFilePath);
                startActivity(new Intent(FaceCamera2.this, FinishActivity.class));
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
        //capture.setText("녹화 중지");

        if (mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();
        }

        recordFilePath = getOutputMediaFile().getAbsolutePath(); // 저장할 파일 경로
//        String recordFilePath = "/sdcard/record.mp4";
        Log.d("dsfa", recordFilePath);

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
        mediaRecorder.setOrientationHint(90); // 비디오 방향 설정
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

    class InsertData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(FaceCamera2.this, "Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            progressDialog.dismiss();
//            result.setText(s);
            Log.d(TAG, "POst response");
        }

        @Override
        protected String doInBackground(String... params) {
            String name = (String)params[1];
            String birth = (String)params[2];
            String email = (String)params[3];
            String gender = (String)params[4];
            String video = (String)params[5];
            String serverURL = (String)params[0];
            String postParameters = "name="+name+"&birth="+birth+"&email="+email+"&gender="+gender+"&video="+video;

            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
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

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder stringBuilder = new StringBuilder();
                String line=null;

                while ((line=bufferedReader.readLine())!=null){
                    Log.d(TAG, line);
                    stringBuilder.append(line);
                }

                bufferedReader.close();
                return stringBuilder.toString();

            } catch (Exception e){
                Log.d(TAG, "InsertData: Error", e);
                return new String("Error: "+e.getMessage());
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
        String recordPath = getExternalCacheDir().getAbsolutePath();
        File mediaFile = new File(recordPath + File.separator + "recorddddd.mp4");
//        Log.d("dsfa", String.valueOf(mediaFile));
        return mediaFile;
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

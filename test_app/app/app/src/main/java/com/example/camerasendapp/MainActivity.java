package com.example.camerasendapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.w3c.dom.Text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.speech.tts.TextToSpeech.ERROR;


public class MainActivity extends AppCompatActivity {




        private Button btn_record;
        private SurfaceView surfaceView;
        private Camera camera;
        private MediaRecorder mediaRecorder;
        private SurfaceHolder surfaceHolder;
        private boolean recording = false;

        private String TAG = "MainActivity.java";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            TedPermission.with(this) //권한을 얻기 위한 코드이다.
                    .setPermissionListener(permission)
                    .setRationaleMessage("녹화를 위하여 권한을 허용해주세요.")
                    .setDeniedMessage("권한이 거부되었습니다. 설정 > 권한에서 허용할 수 있습니다.")
                    .setPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
                    .check();

            btn_record = (Button)findViewById(R.id.btn_record);
            btn_record.setOnClickListener(v -> {
                if (recording) { //녹화 중일 때 버튼을 누르면 녹화가 종료하도록 한다.
                    mediaRecorder.stop();
                    mediaRecorder.release();
                    camera.lock();
                    recording = false;
                    btn_record.setText("녹화 시작");
                } else { //녹화 중이 아닐 때 버튼을 누르면 녹화가 시작하게 한다.
                    runOnUiThread(new Runnable() { //녹화를 하는 것은 백그라운드로 하는 것이 좋다.
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "녹화가 시작되었습니다.", Toast.LENGTH_SHORT).show();
                            try {
                                mediaRecorder = new MediaRecorder();
                                camera.unlock();
                                mediaRecorder.setCamera(camera);
                                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                                mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                                mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
                                mediaRecorder.setOrientationHint(90);
                                mediaRecorder.setOutputFile("/storage/emulated/0/Download/test.mp4");
                                mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
                                mediaRecorder.prepare();
                                mediaRecorder.start();
                                recording = true;
                                btn_record.setText("녹화 종료");
                            } catch (IOException e) {
                                Log.e(TAG, "Error in 79" + e.getMessage());
                                e.printStackTrace();
                                mediaRecorder.release();
                            }
                        }

                    });
                }
            });
        }

        PermissionListener permission = new PermissionListener() {
            @Override
            public void onPermissionGranted() { //권한을 허용받았을 때 camera와 surfaceView에 대한 설정을 해준다.
                camera = Camera.open();
                camera.setDisplayOrientation(90);
                surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
                surfaceHolder = surfaceView.getHolder();
                surfaceHolder.addCallback((SurfaceHolder.Callback) MainActivity.this);
                surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                Toast.makeText(MainActivity.this, "권한 허가", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) { //권한이 거부됐을 때 이벤트를 설정할 수 있다.
                Toast.makeText(MainActivity.this, "권한 거부", Toast.LENGTH_SHORT).show();
            }
        };

//        @Override
//        public void surfaceCreated(@NonNull SurfaceHolder holder) {
//        }

        private void refreshCamera(Camera camera) {
            if (surfaceHolder.getSurface() == null) {
                return;
            }
            try {
                camera.stopPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
            setCamera(camera);
        }

        private void setCamera(Camera cam) {
            camera = cam;
        }

//        @Override
//        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
//            refreshCamera(camera);
//        }

//        @Override
//        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
//
//        }







    //이미지 플라시크로 전송
//    private void sendImage() {
//
//        //비트맵 이미지를 byte로 변환 -> base64형태로 변환
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//        byte[] imageBytes = baos.toByteArray();
//        imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);
//        TextView textView1 = (TextView) findViewById(R.id.textView);
//
//        //base64형태로 변환된 이미지 데이터를 플라스크 서버로 전송
//        String flask_url = "http://223.194.252.240:8080/sendFrame";
//        StringRequest request = new StringRequest(Request.Method.DEPRECATED_GET_OR_POST, flask_url,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        progress.dismiss();
//                        textView1.setText("");
//                        System.out.println("여길 찾아" + response);
//                        Toast.makeText(MainActivity.this, "잠시 기다려 주세요", Toast.LENGTH_LONG).show();
////                        if(response.equals("true")){
////                            Toast.makeText(MainActivity.this, "Uploaded Successful", Toast.LENGTH_LONG).show();
////                        }
////                        else{
////                            Toast.makeText(MainActivity.this, "Some error occurred!", Toast.LENGTH_LONG).show();
////                        }
//                        new Handler().postDelayed(new Runnable() {  // 5초뒤에 AlertDialog 실행
//                            @Override
//                            public void run() {
//                                textView1.setText(response);
//                                tts.speak(textView1.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
//                            }
//                        },5000);// 5초 설정
//
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        progress.dismiss();
//                        Toast.makeText(MainActivity.this, "Some error occurred -> "+error, Toast.LENGTH_LONG).show();
//                    }
//                }){
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                Map<String, String> params = new HashMap<>();
//                params.put("image", imageString);
//
//                return params;
//            }
//        };
//        System.out.println("먼저인가요");
//        queue.add(request);
//        System.out.println("여기서 문제가 생기나요");
//    }

}
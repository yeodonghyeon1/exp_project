package com.example.camerasendapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Color;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
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
//import com.ogaclejapan.smarttablayout.utils.ViewPagerItemAdapter;
//import com.ogaclejapan.smarttablayout.utils.ViewPagerItems;
//import com.ogaclejapan.smarttablayout.utils.v4.Bundler;
//import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
//import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.*;
import okhttp3.Callback;
import okhttp3.RequestBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;

import okio.BufferedSink;
import okio.Okio;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.Path;

import static android.speech.tts.TextToSpeech.ERROR;
public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    private ImageView img;
    private Button btn_capture, btn_gallery, btn_send , btn_test, btn_save, btn_load;
    private ProgressDialog progress;
    private TextView textView1;
    private ViewPager viewPager;
    private RequestQueue queue;
    private String currentPhotoPath;
    private Bitmap bitmap;
    private TextToSpeech tts;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int GET_GALLERY_IMAGE = 2;
    public static final String API_URL = "https://api.github.com";  // 보낼 주소

    private Uri video_URI;

    private String save_data = "0";
    private Camera camera;
    private boolean recording = false;
    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView;
    static final String TAG = "카메라";
    private String imageString;
    static final int REQUEST_VIDEO_CAPTURE = 1;
    private MediaRecorder mediaRecorder;
    private String vedio_result = "";
    final Handler handler = new Handler(){
        public void handleMessage(Message msg){
            textView1.setText(vedio_result);
            tts.speak(textView1.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_main);

        init();
//
//        ViewPagerItemAdapter adapter = new ViewPagerItemAdapter(ViewPagerItems.with(this)
//                .add(R.string.title, R.layout.test_main)
//                .add("title", R.layout.test_main)
//                .create());
//
//        viewPager.setAdapter(adapter);

//...
//        @Override
//        public void onPageSelected(int position){
//
//            //.instantiateItem() from until .destroyItem() is called it will be able to get the View of page.
//            View page = adapter.getPage(position);
//
//        }

        btn_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera_open_intent();
            }
        });
        btn_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gallery_open_intent();
            }
        });

//      저장 버튼
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save_data = "1";
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress = new ProgressDialog(MainActivity.this);
                progress.setMessage("Uploading...");
                progress.show();

                sendImage();
            }
        });
        btn_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                dispatchTakeVideoIntent();


                VideoRecord();

            }
        });


        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != ERROR){
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
    }

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

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        refreshCamera(camera);
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }


    public void getVideoFromGallery(){
        video_URI = Uri.parse("/storage/emulated/0/Download/test.mp4");
        Intent intent = new Intent(Intent.ACTION_VIEW, video_URI);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        Log.d(TAG, "test -1번");

    }

    public void sendVideoContentToServer() {
        Log.d(TAG, "test 0번");
        class sendDataToHttp extends AsyncTask<Void, Void, String> {
            String serverUrl = "http:///123.248.247.15:8080/sendvideo";
//            String serverUrl = "http://192.168.0.104:8080/sendvideo";

            OkHttpClient client;
            Context context;
            public sendDataToHttp(Context context) {
                this.context = context;
                Log.d(TAG, "test 2번");


                client = new OkHttpClient();

            }

            @Override
            protected String doInBackground(Void... voids) {
                getVideoFromGallery();

                Log.d(TAG, "test 4번");
                ContentResolver contentResolver = context.getContentResolver();
                Log.d(TAG, "test 5번");
                String path = contentResolver.getType(Uri.parse(String.valueOf(video_URI)));
                System.out.println(path);
                Log.d(TAG, "test 6번");
//            File file = new File(path);
                AssetFileDescriptor fd;
                try {

                    System.out.println(video_URI);
                    System.out.println(Uri.fromFile(new File(String.valueOf(video_URI))));
                    fd = contentResolver.openAssetFileDescriptor(Uri.fromFile(new File(String.valueOf(video_URI))), "r");
                    File copyFile = new File(String.valueOf(video_URI));

                    if (fd == null) {
                        throw new FileNotFoundException("could not open file descriptor");
                    }

                    okhttp3.RequestBody videoFile = new okhttp3.RequestBody() {
                        @Override
                        public long contentLength() {
                            return fd.getDeclaredLength();
                        }

                        @Override
                        public MediaType contentType() {
                            return MediaType.parse("video/mp4");

                        }

                        @Override
                        public void writeTo(BufferedSink sink) throws IOException {
                            try (InputStream is = fd.createInputStream()) {
                                sink.writeAll(Okio.buffer(Okio.source(is)));
                            }
                        }
                    };
                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            .connectTimeout(100, TimeUnit.SECONDS)
                            .readTimeout(100, TimeUnit.SECONDS)
                            .writeTimeout(100, TimeUnit.SECONDS)
                            .build();

                    okhttp3.RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("files", copyFile.getName(),videoFile)
                            .addFormDataPart("problem", "2")
                            .build();
                    Log.d(TAG, "test 7번");

                    okhttp3.Request request = new okhttp3.Request.Builder()
                            .url(serverUrl)
                            .post(requestBody)
                            .build();

                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onResponse(@NotNull okhttp3.Call call, @NotNull okhttp3.Response response) throws IOException {
                            vedio_result = response.body().string();
                            Log.d("결과",""+ vedio_result);
                            fd.close();
                            if(vedio_result.equals(("None"))){
                                vedio_result = "제품 인식 실패";
                            }
                            else if(vedio_result.contains("None")){
                                vedio_result = vedio_result.replaceAll("None", "바코드 인식 실패");
                            }
                            Message msg = handler.obtainMessage();
                            handler.sendMessage(msg);

                        }

                        @Override
                        public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                            try {
                                fd.close();
                            } catch (IOException ex) {
                                e.addSuppressed(ex);
                            }
                            vedio_result = "인식 실패";
                            Log.d("인식 실패", "failed", e);


                        }
                    });
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Log.d(TAG, "test error번");
                }


                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.d(TAG, "test 3번");
            }


            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
            }
        }
        sendDataToHttp sendData = new sendDataToHttp(this);
        Log.d(TAG, "test 1번");

        sendData.execute();
        Log.d(TAG, "11"+vedio_result);


    }




    //이미지 플라시크로 전송
    private void sendImage() {

        //비트맵 이미지를 byte로 변환 -> base64형태로 변환
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        //base64형태로 변환된 이미지 데이터를 플라스크 서버로 전송
        String flask_url = "http:///123.248.247.15:8080/sendFrame";
//        String flask_url = "http://192.168.0.104:8080/sendFrame";

        StringRequest request = new StringRequest(Request.Method.DEPRECATED_GET_OR_POST, flask_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response == "1" && response == "0"){

                        }
                        progress.dismiss();
                        Toast.makeText(MainActivity.this, "잠시 기다려 주세요", Toast.LENGTH_LONG).show();
                        textView1.setText(response);
                        tts.speak(textView1.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
//                        if(response.equals("true")){
//                            Toast.makeText(MainActivity.this, "Uploaded Successful", Toast.LENGTH_LONG).show();
//                        }
//                        else{
//                            Toast.makeText(MainActivity.this, "Some error occurred!", Toast.LENGTH_LONG).show();
//                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progress.dismiss();
                        Toast.makeText(MainActivity.this, "Some error occurred -> "+error, Toast.LENGTH_LONG).show();
                    }
                })

        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("image", imageString);
                params.put("save",  save_data);

                return params;
            }
        };

//        new Handler().postDelayed(new Runnable() {  // 5초뒤에 AlertDialog 실행
//            @Override
//            public void run() {
//            }
//        },5000);// 5초 설정
        request.setShouldCache(false); //이전 결과 있어도 새로 요청하여 응답을 보여준다.
        queue.add(request);
        System.out.println(request);
    }

//    //Contributor 정확히 뭔지 모르겠음
//    public static class Contributor {
//        public final String login;
//        public final int contributions;
//
//        public Contributor(String login, int contributions) {
//            this.login = login;
//            this.contributions = contributions;
//        }
//    }
//
//    //Retrofit을 활용한 데이터 통신
//    public interface GitHubService {
//        @GET("/users/{repo}/contributions") // 겟 방식
//        Call<List<Contributor>> contributions(@Path("user") String repo);
//    }
//
//    public void send_vedio() {
//        Retrofit retrofit =
//                new Retrofit.Builder()
//                        .baseUrl(API_URL)
//                        .addConverterFactory(GsonConverterFactory.create())
//                        .build();
//
//        // Create an instance of our GitHub API interface.
//        GitHub github = retrofit.create(GitHub.class);
//
//        // Create a call instance for looking up Retrofit contributors.
//        Call<List<Contributor>> call = github.contributors("square", "retrofit");
//
//        // Fetch and print a list of the contributors to the library.
//        List<Contributor> contributors = call.execute().body();
//        for (Contributor contributor : contributors) {
//            System.out.println(contributor.login + " (" + contributor.contributions + ")");
//        }
//    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Uri picturePhotoURI = Uri.fromFile(new File(currentPhotoPath));

            getBitmap(picturePhotoURI);
            img.setImageBitmap(bitmap);

            //갤러리에 사진 저장
            saveFile(currentPhotoPath);

        } else if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK) {
            Uri galleryURI = data.getData();
            //img.setImageURI(galleryURI);

            getBitmap(galleryURI);
            img.setImageBitmap(bitmap);
        } else if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
//          videoView.setVideoURI(videoUri);
            getBitmap(videoUri);
            img.setImageBitmap(bitmap);
        }
    }



    //Uri에서 bisap
    private void getBitmap(Uri picturePhotoURI) {
        try {
            //서버로 이미지를 전송하기 위한 비트맵 변환하기
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), picturePhotoURI);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //xml에 정의한 view 초기화
    private void init() {
        img = findViewById(R.id.img);
        btn_capture = findViewById(R.id.btn_capture);

        btn_capture.setBackgroundColor(Color.rgb(1,2,2));
        btn_gallery = findViewById(R.id.btn_gallery);
        btn_gallery.setBackgroundColor(Color.rgb(1,2,2));
        btn_send = findViewById(R.id.btn_send);
        btn_send.setBackgroundColor(Color.rgb(1,2,2));
        btn_test = findViewById(R.id.btn_test);
        btn_test.setBackgroundColor(Color.rgb(1,2,2));
        btn_save = findViewById(R.id.btn_save);
        btn_save.setBackgroundColor(Color.rgb(1,2,2));
        btn_load = findViewById(R.id.btn_load);
        btn_load.setBackgroundColor(Color.rgb(1,2,2));

        queue = Volley.newRequestQueue(MainActivity.this);
        textView1 = (TextView) findViewById(R.id.textView);
        textView1.setTextColor(Color.rgb(1,2,2));
        requestPermission();
    }

    //카메라, 쓰기, 읽기 권한 체크/요청
    private void requestPermission() {
        //민감한 권한 사용자에게 허용요청
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) { // 저장소에 데이터를 쓰는 권한을 부여받지 않았다면~

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    //동영상 확인
    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    private void VideoRecord(){

        if (recording) { //녹화 중일 때 버튼을 누르면 녹화가 종료하도록 한다.
            mediaRecorder.stop();
            mediaRecorder.release();
            camera.lock();
            recording = false;
            btn_test.setText("녹화 시작");
            Log.d(TAG, "1번");
            Toast.makeText(MainActivity.this, "녹화가 종료되었습니다.", Toast.LENGTH_SHORT).show();
            Toast.makeText(MainActivity.this, "잠시 기다려 주세요", Toast.LENGTH_LONG).show();

            sendVideoContentToServer();

        }
        else { //녹화 중이 아닐 때 버튼을 누르면 녹화가 시작하게 한다.\

// set Camera parameters

            camera = Camera.open();
            Camera.Parameters params = camera.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            camera.setParameters(params);
            camera.setDisplayOrientation(90);
            surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
            surfaceHolder = surfaceView.getHolder();
            surfaceHolder.addCallback(MainActivity.this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            runOnUiThread(new Runnable() { //녹화를 하는 것은 백그라운드로 하는 것이 좋다.
                  @Override
                  public void run() {
                      Toast.makeText(MainActivity.this, "녹화가 시작되었습니다.", Toast.LENGTH_SHORT).show();
                      try {
                          Log.d(TAG, "2번");


                          mediaRecorder = new MediaRecorder();
                          camera.unlock();
                          mediaRecorder.setCamera(camera);
                          Log.d(TAG, "3번");

//                          mediaRecorder.setVideoFrameRate(16);

                          mediaRecorder.setVideoEncodingBitRate(3000000);
//                          mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);// MPEG_4_SP
//                          mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                          mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                          mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                          mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));



                          mediaRecorder.setOrientationHint(90);
                          mediaRecorder.setOutputFile("/storage/emulated/0/Download/test.mp4");
                          mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
                          mediaRecorder.prepare();
                          mediaRecorder.start();
                          recording = true;
                          Log.d(TAG, "4번");
                          btn_test.setText("녹화 종료");
                      } catch (IOException e) {
                          Log.e(TAG, "Error in 79" + e.getMessage());
                          e.printStackTrace();
                          mediaRecorder.release();
                      }
                  }
            });
        }
    }


    //갤러리 띄우기
    private void gallery_open_intent() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GET_GALLERY_IMAGE);
    }


    //갤러리 사진 저장 기능
    private void saveFile(String currentPhotoPath) {

        Bitmap bitmap = BitmapFactory.decodeFile( currentPhotoPath );

        ContentValues values = new ContentValues( );

        //실제 앨범에 저장될 이미지이름
        values.put( MediaStore.Images.Media.DISPLAY_NAME, new SimpleDateFormat( "yyyyMMdd_HHmmss", Locale.US ).format( new Date( ) ) + ".jpg" );
        values.put( MediaStore.Images.Media.MIME_TYPE, "image/*" );

        //저장될 경로 -> /내장 메모리/DCIM/ 에 'AndroidQ' 폴더로 지정
        values.put( MediaStore.Images.Media.RELATIVE_PATH, "DCIM/AndroidQ" );

        Uri u = MediaStore.Images.Media.getContentUri( MediaStore.VOLUME_EXTERNAL );
        Uri uri = getContentResolver( ).insert( u, values ); //이미지 Uri를 MediaStore.Images에 저장

        try {
            /*
             ParcelFileDescriptor: 공유 파일 요청 객체
             ContentResolver: 어플리케이션끼리 특정한 데이터를 주고 받을 수 있게 해주는 기술(공용 데이터베이스)
                            ex) 주소록이나 음악 앨범이나 플레이리스트 같은 것에도 접근하는 것이 가능

            getContentResolver(): ContentResolver객체 반환
            */

            ParcelFileDescriptor parcelFileDescriptor = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                parcelFileDescriptor = getContentResolver( ).openFileDescriptor( uri, "w", null ); //미디어 파일 열기
            }
            if ( parcelFileDescriptor == null ) return;

            //바이트기반스트림을 이용하여 JPEG파일을 바이트단위로 쪼갠 후 저장
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream( );

            //비트맵 형태 이미지 크기 압축
            bitmap.compress( Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream );
            byte[] b = byteArrayOutputStream.toByteArray( );
            InputStream inputStream = new ByteArrayInputStream( b );

            ByteArrayOutputStream buffer = new ByteArrayOutputStream( );
            int bufferSize = 1024;
            byte[] buffers = new byte[ bufferSize ];

            int len = 0;
            while ( ( len = inputStream.read( buffers ) ) != -1 ) {
                buffer.write( buffers, 0, len );
            }

            byte[] bs = buffer.toByteArray( );
            FileOutputStream fileOutputStream = new FileOutputStream( parcelFileDescriptor.getFileDescriptor( ) );
            fileOutputStream.write( bs );
            fileOutputStream.close( );
            inputStream.close( );
            parcelFileDescriptor.close( );

            getContentResolver( ).update( uri, values, null, null ); //MediaStore.Images 테이블에 이미지 행 추가 후 업데이트

        } catch ( Exception e ) {
            e.printStackTrace( );
        }

        values.clear( );
        values.put( MediaStore.Images.Media.IS_PENDING, 0 ); //실행하는 기기에서 앱이 IS_PENDING 값을 1로 설정하면 독점 액세스 권한 획득
        getContentResolver( ).update( uri, values, null, null );

    }

    //카메라 호출
    private void camera_open_intent() {
        Log.d("Camera", "카메라실행!");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            File photoFile = null;

            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.d(TAG, "에러발생!!");
            }

            if (photoFile != null) {
                Uri providerURI = FileProvider.getUriForFile(this, "com.example.camera.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }

        }
    }

    //카메라 촬영 시 임시로 사진을 저장하고 사진위치에 대한 Uri 정보를 가져오는 메소드
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        Log.d(TAG, "사진저장>> "+storageDir.toString());

        currentPhotoPath = image.getAbsolutePath();

        return image;
    }
}
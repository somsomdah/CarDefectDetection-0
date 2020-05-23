package com.example.carcarcarcar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AfterPastHistory extends AppCompatActivity {

    private static final String TAG = "MAIN";
    private RequestQueue queue;
    private File mImageFolder;

    private String rentid, userid, carid;

    String[] imagelist;
    private ArrayList<Uri> arrayList;
    OkHttpClient okHttpClient;


    private int state = 1;  // 0 : before  1 : after

    private Button compareBtn;
    private Button sendBtn;

    private boolean yolo_done = false;

    private Intent serviceIntent_compare;

    ApiService service;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_past_history);



        rentid=Config.rent_id;

        System.out.println("RENT ID : " + rentid);
        userid = Config.user_id;
        carid = Config.car_id;

        compareBtn = findViewById(R.id.compareBtn);
        sendBtn = findViewById(R.id.sendBtn);

        compareBtn.setEnabled(false);

        serviceIntent_compare = new Intent(this, YOLOService.class);


        File imageFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        mImageFolder = new File(imageFile, "YOCO");



        ImageView imageview_frontal1 = findViewById(R.id.ff_imageview_compare);
        ImageView imageview_frontal2 = findViewById(R.id.ft_imageview_compare);
        ImageView imageView_profile1 = findViewById(R.id.bf_imageview_compare);
        ImageView imageView_profile2 = findViewById(R.id.bt_imageview_compare);
        ImageView imageView_profile3 = findViewById(R.id.lf_imageview_compare);
        ImageView imageView_profile4 = findViewById(R.id.lb_imageview_compare);
        ImageView imageView_back1 = findViewById(R.id.rf_imageview_compare);
        ImageView imageView_back2 = findViewById(R.id.rb_imageview_compare);

        imagelist = new String[8];
        //이미지 넣기

        try { //uri 경로의 이미지 파일을 로드

            arrayList = new ArrayList<>();


            String newPath = String.valueOf(Paths.get(mImageFolder.getAbsolutePath())) + "/";



            Uri uri1 = Uri.parse("file:///" + newPath + rentid +"_" + "ft_a.jpg");
            System.out.println(uri1);
            imageview_frontal1.setImageURI(uri1);
            arrayList.add(uri1);


            Uri uri2 = Uri.parse("file:///" + newPath + rentid +"_" + "ff_a.jpg");
            imageview_frontal2.setImageURI(uri2);
            arrayList.add(uri2);

            Uri uri3 = Uri.parse("file:///" + newPath + rentid +"_" + "rf_a.jpg");
            imageView_profile1.setImageURI(uri3);
            arrayList.add(uri3);

            Uri uri4 = Uri.parse("file:///" + newPath + rentid +"_" + "rb_a.jpg");
            imageView_profile2.setImageURI(uri4);
            arrayList.add(uri4);

            Uri uri5 = Uri.parse("file:///"+ newPath + rentid +"_" + "bt_a.jpg");
            imageView_profile3.setImageURI(uri5);
            arrayList.add(uri5);

            Uri uri6 = Uri.parse("file:///" + newPath + rentid +"_" + "bf_a.jpg");
            imageView_profile4.setImageURI(uri6);
            arrayList.add(uri6);

            Uri uri7 = Uri.parse("file:///" + newPath + rentid +"_" + "lb_a.jpg");
            imageView_back1.setImageURI(uri7);
            arrayList.add(uri7);

            Uri uri8 = Uri.parse("file:///" + newPath + rentid +"_" + "lf_a.jpg");
            imageView_back2.setImageURI(uri8);
            arrayList.add(uri8);

        }catch (Exception e){
            e.printStackTrace();
        }

        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.MINUTES)
                .readTimeout(30, TimeUnit.MINUTES)
                .writeTimeout(30, TimeUnit.MINUTES)
                .build();





    }

    public void onSendButtonClicked(View v){
        Intent serviceIntent_yolo = new Intent(this, YOLOService.class);
        serviceIntent_yolo.putExtra("YOLO_done", yolo_done); // send false
        startService(serviceIntent_yolo);   // 사용자에게 "YOLO 분석중"을 알림

        uploadImagesToServer(); // send photos


    }

    public void onCompareButtonClicked(View v){
        stopService(new Intent(this, YOLOService.class));
        Intent intent2 = new Intent(this, CompareActivity.class);
        intent2.putExtra("state", state);
        startActivity(intent2);
    }

    private void uploadImagesToServer(){
        //Internet connection check
        if(((ConnectivityManager) Objects.requireNonNull(AfterPastHistory.this.getSystemService
                (Context.CONNECTIVITY_SERVICE))).getActiveNetworkInfo() != null){



            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(ApiService.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();

            List<MultipartBody.Part> parts = new ArrayList<>();

            service = retrofit.create(ApiService.class);

            if(arrayList != null){
                // create part for file
                for(int i=0; i < arrayList.size(); i++){
                    System.out.println(arrayList.get(i));
                    parts.add(prepareFilePart("image" + i, arrayList.get(i)));
                }
            }




            // execute the request 사진전송
            Call<ResponseBody> call = service.uploadMultiple(parts);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                    if(response.isSuccessful()){
                        Log.v("Upload", "success");
                        Toast.makeText(AfterPastHistory.this,
                                "Images successfully uploaded!", Toast.LENGTH_SHORT).show();

                        // create a map of data to pass along
                        RequestBody rent_id = createPartFromString(rentid);
                        RequestBody state = createPartFromString("a");
                        RequestBody yolo_request = createPartFromString("true");

                        //  YOLO 요청 전송
                        Call<ResponseBody> yolo_call = service.requestYOLO(rent_id, state, yolo_request);
                        Log.v("YOLO Request", "Request YOLO");

                        yolo_call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if(response.isSuccessful()){
                                    Log.v("YOLO server", "YOLO Complete");
                                    // background service 전송
                                    serviceIntent_compare.putExtra("YOLO_done", yolo_done); // send true
                                    startService(serviceIntent_compare);    // 사용자에게 YOLO가 완료됐음을 알림

                                    sendBtn.setEnabled(false);
                                    compareBtn.setEnabled(true);

                                }
                                else {
                                    Snackbar.make(findViewById(android.R.id.content),
                                            "Something went wrong with YOLO.", Snackbar.LENGTH_LONG).show();
                                }

                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Log.v("YOLO Server", "YOLO failed");
                            }
                        });

                    }
                    else {
                        Snackbar.make(findViewById(android.R.id.content),
                                "Something went wrong with Sending.", Snackbar.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e(TAG, "Image upload failed!", t);
                    Snackbar.make(findViewById(android.R.id.content),
                            "Image upload failed!", Snackbar.LENGTH_LONG).show();

                }
            });

        }
    }

    @NonNull
    private RequestBody createPartFromString(String string) {
        return RequestBody.create(MultipartBody.FORM, string);
    }

    @NonNull
    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        // use the FileUtils to get the actual file by uri
        File file = FileUtils.getFile(this, fileUri);


        // create RequestBody instance from file

        RequestBody requestFile = RequestBody.create(MediaType.parse(FileUtils.MIME_TYPE_IMAGE), file);

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }




}
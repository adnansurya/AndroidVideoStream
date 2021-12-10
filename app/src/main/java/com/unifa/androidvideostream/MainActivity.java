package com.unifa.androidvideostream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    ProgressDialog pDialog;
    VideoView videoview;
//    String vid_url ="https://www.youtube.com/watch?v=QnOcXQL2wDA&t=18s";
    String vid_url ="192.168.1.5";

    ImageView imView;
    Button tesBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imView = findViewById(R.id.imageView);
        tesBtn = findViewById(R.id.button);
        WebView webView = findViewById(R.id.webView1);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);

        // Tiga baris di bawah ini agar laman yang dimuat dapat
        // melakukan zoom.
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.setDrawingCacheEnabled(true);
        // Baris di bawah untuk menambahkan scrollbar di dalam WebView-nya
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
//        webView.setWebViewClient(new WebViewClient(){
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                super.onPageFinished(view, url);
//
//                Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
//                imView.setImageBitmap(bitmap);
//            }
//        });
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(vid_url);

        tesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = viewToImage(MainActivity.this, webView);
                imView.setImageBitmap(bitmap);
                DateFormat df = new SimpleDateFormat("d-MM-yyyy_HH:mm:ss");
                String date = df.format(Calendar.getInstance().getTime());

                StorageReference storageRef = FirebaseStorage.getInstance().getReference();

// Create a reference to "mountains.jpg"
                StorageReference imagesRef = storageRef.child("images/" + date + ".jpg");


//                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                UploadTask uploadTask = imagesRef.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                        // ...
                    }
                });

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.action,menu);
        return true;
    }

    public static Bitmap viewToImage(Context context,
                                     WebView viewToBeConverted) {
        int extraSpace = 200; //because getContentHeight doesn't always return the full screen height.
        int height = viewToBeConverted.getContentHeight() + extraSpace;

        Bitmap viewBitmap = Bitmap.createBitmap(
                viewToBeConverted.getWidth(), height,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(viewBitmap);
        viewToBeConverted.draw(canvas);//w ww . ja  va 2 s. c  o m

        //If the view is scrolled, cut off the top part that is off the screen.
        try {
            int scrollY = viewToBeConverted.getScrollY();

            if (scrollY > 0) {
                viewBitmap = Bitmap.createBitmap(viewBitmap, 0, scrollY,
                        viewToBeConverted.getWidth(), height - scrollY);
            }
        } catch (Exception ex) {
            Log.e("Bitmap","Could not remove top part of the webview image. ex: "
                    + ex);
        }

        return viewBitmap;
    }
}
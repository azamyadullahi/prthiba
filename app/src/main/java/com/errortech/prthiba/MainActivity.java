package com.errortech.prthiba;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

import static android.provider.MediaStore.ACTION_IMAGE_CAPTURE;
import static android.provider.MediaStore.ACTION_VIDEO_CAPTURE;
import static android.provider.MediaStore.EXTRA_OUTPUT;

public class MainActivity extends AppCompatActivity {


    //global Declaration
    WebView webView;
    // file uploading declarion

    static boolean ASWP_FUPLOAD = true;
    private final static int file_perm = 2;
    private static String file_type = "*/*";// file types to be allowed for upload
    private boolean multiple_files = true;
    private static final String TAG =  MainActivity.class.getSimpleName();
    private String cam_file_data = null;        // for storing camera file information
    private ValueCallback<Uri>  file_data;       // data/header received after file selection
    private ValueCallback<Uri[]> file_path;
    private final static int FCR = 1;
    private static final String[] PERM_CAMERA =
            {Manifest.permission.CAMERA};

    public static final int REQUEST_SELECT_FILE = 100;
    //for camera
    static final int REQUEST_VIDEO_CAPTURE = 101;
    // private final static int videoView = 1;

    //for testing the video record
    private PermissionRequest mPermissionRequest;

    private static final int REQUEST_CAMERA_PERMISSION = 200;

    private String asw_cam_message;


    //ob backpressed
    boolean doubleBackToExitPressedOnce = false;


    private final static int file_req_code = 1;



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);


        if (Build.VERSION.SDK_INT >= 21) {
            Uri[] results = null;

            //Check if response is positive
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == FCR) {

                    if (null == file_path) {
                        return;
                    }
                    ClipData clipData;
                    String stringData;
                    try {
                        clipData = intent.getClipData();
                        stringData = intent.getDataString();
                    }catch (Exception e){
                        clipData = null;
                        stringData = null;
                    }
                    if (clipData == null && stringData == null && asw_cam_message != null) {
                        results = new Uri[]{Uri.parse(asw_cam_message)};
                    } else {
                        if (null != intent.getClipData()) { // checking if multiple files selected or not
                            final int numSelectedFiles = intent.getClipData().getItemCount();
                            results = new Uri[numSelectedFiles];
                            for (int i = 0; i < intent.getClipData().getItemCount(); i++) {
                                results[i] = intent.getClipData().getItemAt(i).getUri();
                            }
                        } else {
                            results = new Uri[]{Uri.parse(intent.getDataString())};
                        }
                    }

                    if (intent == null) {
                        //Capture Photo if no image available
                        if (file_type != null) {
                            results = new Uri[]{Uri.parse(file_type)};
                        }
                    } else {
                        String dataString = intent.getDataString();
                        if (dataString != null) {
                            results = new Uri[]{Uri.parse(dataString)};
                        }
                    }
                }
            }
            file_path.onReceiveValue(results);
            file_path = null;
        } else {

            if (requestCode == FCR) {
                if (null == file_data) return;
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                file_data.onReceiveValue(result);
                file_data = null;
            }
        }
    }


    @SuppressLint({"SetJavaScriptEnabled", "WrongViewCast"})

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = (WebView) findViewById(R.id.webView);
        webView = (WebView) findViewById(R.id.webView);


        //Runtime External storage permission for saving download files
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED) {
                Log.d("permission", "permission denied to WRITE_EXTERNAL_STORAGE - requesting it");
                String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions, 1);
            }

        }
        //definations
        webView.loadUrl("http://owlbridge.in/mobile/");



        WebSettings webSettings = webView.getSettings();
        // it can be use for the loging with 3rd party
        webSettings.setDomStorageEnabled(true);   // localStorage
        webView.clearCache(true);
        webSettings.setAllowFileAccess(true);
        if (Build.VERSION.SDK_INT >= 21) {
            webSettings.setMixedContentMode(0);
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else if (Build.VERSION.SDK_INT >= 19) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        }
        webView.clearHistory();
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.setWebViewClient(new WebViewClient() {
            private boolean alreadyLoaded = false;


            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (alreadyLoaded)
                    return;
                alreadyLoaded = true;
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                webView.loadUrl("file:///android_asset/error.html");
            }

        });
        webView.setWebChromeClient(new WebChromeClient() {


            //for video recording
            // Grant permissions for camera
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onPermissionRequest(final PermissionRequest request) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    int hasCameraPermission = checkSelfPermission(Manifest.permission.CAMERA);
                    Log.d(TAG, "has camera permission: " + hasCameraPermission);
                    int hasRecordPermission = checkSelfPermission(Manifest.permission.RECORD_AUDIO);
                    Log.d(TAG, "has record permission: " + hasRecordPermission);
                    int hasAudioPermission = checkSelfPermission(Manifest.permission.MODIFY_AUDIO_SETTINGS);
                    Log.d(TAG, "has audio permission: " + hasAudioPermission);
                    List<String> permissions = new ArrayList<>();
                    if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
                        permissions.add(Manifest.permission.CAMERA);
                    }
                    if (hasRecordPermission != PackageManager.PERMISSION_GRANTED) {
                        permissions.add(Manifest.permission.RECORD_AUDIO);
                    }
                    if (hasAudioPermission != PackageManager.PERMISSION_GRANTED) {
                        permissions.add(Manifest.permission.MODIFY_AUDIO_SETTINGS);
                    }

                    if (!permissions.isEmpty()) {
                        requestPermissions(permissions.toArray(new String[permissions.size()]),111);

                    }
                    Log.i(TAG, "onPermissionRequest");
                }

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        request.grant(request.getResources());
                    }
                });
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mPermissionRequest = request;
                }


            }



            @Override
            public void onPermissionRequestCanceled(PermissionRequest request) {
                super.onPermissionRequestCanceled(request);
                Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }


            //For Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {

                file_data = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                MainActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), FCR);
            }

            // For Android 3.0+, above method not supported in some android 3+ versions, in such case we use this
            public void openFileChooser(ValueCallback uploadMsg, String acceptType) {

                file_data = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                MainActivity.this.startActivityForResult(
                        Intent.createChooser(i, "File Browser"),
                        FCR);
            }

            //For Android 4.1+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {

                file_data = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                MainActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), MainActivity.FCR);
            }

            //For Android 5.0+
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    FileChooserParams fileChooserParams) {

                if (file_path != null) {
                    file_path.onReceiveValue(null);
                }

                file_path = filePathCallback;
                Intent takePictureIntent = new Intent(ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(MainActivity.this.getPackageManager()) != null) {

                    File photoFile = null;


                    try {
                        photoFile = createImageFile();
                        takePictureIntent.putExtra("PhotoPath", file_type);
                    } catch (IOException ex) {
                        Log.e(TAG, "Image file creation failed", ex);
                    }
                    if (photoFile != null) {
                        file_type = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    } else {
                        takePictureIntent = null;
                    }
                }


                Intent takeVideoIntent = new Intent(ACTION_VIDEO_CAPTURE);
                if (takeVideoIntent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
                    File videoFile = null;
                    try {
                        videoFile = create_video();
                    } catch (IOException ex) {
                        Log.e(TAG, "Video file creation failed", ex);
                    }
                    if (videoFile != null) {
                        asw_cam_message = "file:" + videoFile.getAbsolutePath();
                        takeVideoIntent.putExtra(EXTRA_OUTPUT, Uri.fromFile(videoFile));
                    } else {
                        takeVideoIntent = null;
                    }
                }


                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("*/*");
                Intent[] intentArray;

                if (takePictureIntent != null) {
                    intentArray = new Intent[]{takePictureIntent};
                } else if (takeVideoIntent != null) {
                    intentArray = new Intent[]{takeVideoIntent};
                } else {
                    intentArray = new Intent[0];
                }

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser,Video chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                startActivityForResult(chooserIntent, FCR);

                return true;
            }
        });

    }

    private boolean hasCameraPermission() {
        return EasyPermissions.hasPermissions(MainActivity.this, PERM_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    // Create an image file
    private File createImageFile() throws IOException {

        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "img_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    // Create an image file
    private File create_video() throws IOException {
        @SuppressLint("SimpleDateFormat")
        String file_name = new SimpleDateFormat("yyyy_mm_ss").format(new Date());
        String new_name = "file_" + file_name + "_";
        File sd_directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(new_name, ".3gp", sd_directory);
    }

    public boolean file_permission() {
        if (Build.VERSION.SDK_INT >= 23 && (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
            return false;
        } else {
            return true;
        }
    }
    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return false;
        }
        return true;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA_PERMISSION);
    }

   // @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

   // @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }


    class Controller {
        final static int FILE_SELECTED = 4;

        Activity getActivity() {
            return MainActivity.this;
        }
    }
    private File create_image() throws IOException {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "img_"+timeStamp+"_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName,".jpg",storageDir);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    private class CustomWebViewClient extends WebViewClient {
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (!DetectConnection.checkInternetConnection(MainActivity.this)) {
                Toast.makeText(getApplicationContext(), "No Internet!", Toast.LENGTH_SHORT).show();
            } else {
                view.loadUrl(url);
            }
            return true;
        }
    }
}
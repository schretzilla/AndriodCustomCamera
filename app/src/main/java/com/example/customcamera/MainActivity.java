package com.example.customcamera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class MainActivity extends AppCompatActivity {

    //Region private members


    /**
     * The Layout frame that displays the camera's view
     */
    FrameLayout frameLayout;

    /**
     * The Camera Preview
     */
    ShowCamera cameraPreview;

    /**
     * Log tag name
     */
    private static final String TAG = "Main Activity";

    private VideoRecorder videoRecorder;

    //TODO: Can we get this out?
    Camera m_camera;
    //endregion

    //region Activity Lifecycle Methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        frameLayout = findViewById(R.id.frameLayout);
        requestPermissions();
        //open and start camera
        m_camera = getCameraInstance();

        //show camera view
        cameraPreview = new ShowCamera(this, m_camera);
        frameLayout.addView(cameraPreview);
        videoRecorder = new VideoRecorder(cameraPreview);

        //Add an empty preview to a layout otherwise media will throw errors when video is recorded
        //TODO: Switchout for fake preview in prodcution
        //FrameLayout videoLayout =  findViewById(R.id.videoLayout);
        //EmptyPreview fakeVideoSurface = new EmptyPreview(this);
        // videoLayout.addView(fakeVideoSurface);



        //TODO: check that storage is mounted if we use external storage
//        String state = Environment.getExternalStorageState();
//        if(!state.equals(Environment.MEDIA_MOUNTED))
//        {
        //    log some error
//            return null;
//        }

        // Create the parent data directory if it does not exist
        if (! FileUtility.GetEncounterHomeDir().exists()){
            if (! FileUtility.GetEncounterHomeDir().mkdirs()){
                Log.d(TAG, "failed to create directory: " + FileUtility.GetEncounterHomeDir().getName());
            }
        }

    }

    /**
     * What to do when the application is paused
     * <p>
     *     Need to make sure that the video is exited when the application is paused
     * </p>
     */
    @Override
    protected void onPause() {
        super.onPause();
        videoRecorder.closeRecording();
        //videoRecorder.releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        //releaseCamera();              // release the camera immediately on pause event
    }


    //endregion

    //region Events

    /**
     * Event for handling camera photo button pushes
     * @param v The current view that calls this method
     */
//    public void captureImage(View v)
//    {
//        takePhoto();
//    }

    /**
     * Event for handling capturing video button pushes
     * @param v The current view
     */
    public void captureVideo(View v)
    {
        videoRecorder.toggleRecording();

        if(videoRecorder.isRecording())
        {
            setCaptureButtonText("Stop");
        }
        else
        {
            setCaptureButtonText("Record Video");
        }
    }

    //endregion

    //region private methods

    /**
     * Request permissions to required hardware
     */
    private void requestPermissions()
    {
        HashMap<String, Integer> reqMap = new HashMap<>();
        reqMap.put(Manifest.permission.CAMERA, 1);
        reqMap.put(Manifest.permission.RECORD_AUDIO, 2);
        reqMap.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, 3);

        for (String requirement : reqMap.keySet())
        {
            if (ContextCompat.checkSelfPermission(this,
                    requirement) != PackageManager.PERMISSION_GRANTED) {

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{requirement},
                        reqMap.get(requirement));


            }

        }

    }

    /**
     * Callback when permissions have been accepted or denied
     * @param requestCode The request code for the permissions that was requested
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        boolean requestGranted = true;
        String requestName = "";
        switch (requestCode) {
            case 1:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    requestGranted = false;
                    requestName = "Camera";
                }

            }
            case 2:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    requestGranted = false;
                    requestName = "Record Audio";
                }
            }
            case 3:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    requestGranted = false;
                    requestName = "Write to External Storage";
                }


            }
        }

        if(!requestGranted)
        {
            Toast.makeText(MainActivity.this, "Permission: " + requestName + " denied!", Toast.LENGTH_SHORT).show();

        }

    }


    //endregion

    //region Gui Specific Methods

    /**
     * Change the record Video button's text
     * @param text The text to change it to
     */
    private void setCaptureButtonText(String text)
    {
       Button recordButton = (Button) findViewById(R.id.recordVideo);
       recordButton.setText(text);
    }

    //endregion

    //region photo specific events

//    /**
//     * Trigger the global camera obj to take a photo
//     */
//    private void takePhoto()
//    {
//        if(m_camera != null)
//        {
//            m_camera.takePicture(null, null, mPictureCallback);
//        }
//    }

//    /**
//     * Camera Callbacks for when a photo is taken
//     */
//    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
//        @Override
//        public void onPictureTaken(byte[] data, Camera camera)
//        {
//            File newParentDir = FileUtility.createEncounterFolder(FileUtility.GetEncounterHomeDir());
//
//            // TODO: Handle all data but for now just write the bird photo
//            FileUtility.writeFile(data, newParentDir, "BirdPhoto.jpg");
//
//            //TODO: Remove when view is removed
//            camera.startPreview();
//
//        }
//    };

    //endregion


    //region Camera Methods

    //TODO: Remove from here or video recorder
    private static Camera getCameraInstance() {
        Camera c = null;
        try{
            c = Camera.open();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Log.d(TAG, "getCameraInstance: " + ex.getMessage());
        }
        return c;
    }


    //endregion
}

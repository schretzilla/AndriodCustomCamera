package com.example.customcamera;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.File;
import java.io.IOException;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class MainActivity extends AppCompatActivity {

    /**
     * Region private members
     */
    //Camera camera;

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

    private VideoRecorder videoRecorder = new VideoRecorder();

    //TODO: Can we get this out?
    Camera m_camera;
    //endregion

    //region Activity Lifecycle Methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        frameLayout = findViewById(R.id.frameLayout);

        //open and start camera
        m_camera = getCameraInstance();

        //show camera view
        cameraPreview = new ShowCamera(this, m_camera);

        frameLayout.addView(cameraPreview);

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
    public void captureImage(View v)
    {
        takePhoto();
    }

    /**
     * Event for handling capturing video button pushes
     * @param v The current view
     */
    public void captureVideo(View v)
    {
        videoRecorder.toggleRecording(cameraPreview);

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

    /**
     * Trigger the global camera obj to take a photo
     */
    private void takePhoto()
    {
//        if(camera != null)
//        {
//            camera.takePicture(null, null, mPictureCallback);
//        }
    }

    /**
     * Camera Callbacks for when a photo is taken
     */
    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera)
        {
            File newParentDir = FileUtility.createEncounterFolder(FileUtility.GetEncounterHomeDir());

            // TODO: Handle all data but for now just write the bird photo
            FileUtility.writeFile(data, newParentDir, "BirdPhoto.jpg");

            //TODO: Remove when view is removed
            camera.startPreview();

        }
    };

    //endregion


    //region Camera Methods

    //Opens the camera

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

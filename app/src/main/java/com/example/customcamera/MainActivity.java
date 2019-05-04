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
    Camera camera;

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

    /**
     * bool for if video is recording
     */
    private boolean isRecording = false;

    /**
     * Home directory for all Encounter Data
     */
    private static File encounterHomeDir = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS), "EncounterData");

    /**
     * Image Media type
     */
    private static final int MEDIA_TYPE_IMAGE = 1;

    /**
     * Video media type
     */
    private static final int MEDIA_TYPE_VIDEO = 2;

    /**
     * Used to handle recording audio and video as well as storing the preferences to use with the
     * audio and video
     */
    MediaRecorder mediaRecorder;

    //endregion

    //region Activity Lifecycle Methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        frameLayout = findViewById(R.id.frameLayout);

        //open and start camera
        camera = getCameraInstance();

        //show camera view
        cameraPreview = new ShowCamera(this,camera);
        frameLayout.addView(cameraPreview);

        //TODO: check that storage is mounted if we use external storage
//        String state = Environment.getExternalStorageState();
//        if(!state.equals(Environment.MEDIA_MOUNTED))
//        {
        //    log some error
//            return null;
//        }

        // Create the parent data directory if it does not exist
        if (! encounterHomeDir.exists()){
            if (! encounterHomeDir.mkdirs()){
                Log.d(TAG, "failed to create directory: " + encounterHomeDir.getName());
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
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
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
        toggleRecording();
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
        if(camera != null)
        {
            camera.takePicture(null, null, mPictureCallback);
        }
    }

    /**
     * Camera Callbacks for when a photo is taken
     */
    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera)
        {
            File newParentDir = FileUtility.createEncounterFolder(encounterHomeDir);

            // TODO: Handle all data but for now just write the bird photo
            FileUtility.writeFile(data, newParentDir, "BirdPhoto.jpg");

            //TODO: Remove when view is removed
            camera.startPreview();

        }
    };

    //endregion

    //region video camera methods

    /**
     * Handles locking the camera, configuring and preparing the media recorder instance
     * @param outputFolder The parent folder that the video file will live in
     * @return bool representing the state of success of preparing the video recorder
     */
    private boolean prepareVideoRecorder(File outputFolder){

        camera = getCameraInstance();
        mediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        camera.unlock();
        mediaRecorder.setCamera(camera);

        // Step 2: Set sources
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        String outputFile = getOutputMediaFile(outputFolder, MEDIA_TYPE_VIDEO).toString();
        mediaRecorder.setOutputFile(outputFile);

        // Step 5: Set the preview output
        mediaRecorder.setPreviewDisplay(cameraPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    /**
     * Starts/Stops recording of video from the phones main camera
     */
    private void toggleRecording()
    {
        if (isRecording) {
            // stop recording and release camera
            mediaRecorder.stop();  // stop the recording
            releaseMediaRecorder(); // release the MediaRecorder object
            camera.lock();         // take camera access back from MediaRecorder

            // inform the user that recording has stopped
            setCaptureButtonText("Record Video");
            isRecording = false;
        } else {
            // initialize video camera
            File encounterFolder = FileUtility.createEncounterFolder(encounterHomeDir);
            boolean videoStartedUpSuccessfully = prepareVideoRecorder(encounterFolder);
            if (videoStartedUpSuccessfully) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                mediaRecorder.start();

                // inform the user that recording has started
                setCaptureButtonText("Stop");
                isRecording = true;
            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                // inform user
                Log.d(TAG, "failed to start Media Recorder");
            }
        }
    }

    private void releaseMediaRecorder(){
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            camera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (camera != null){
            camera.release();        // release the camera for other applications

            //camera = null; //TODO Do we need to set camera to null?
        }
    }

    /** Create a file Uri for saving an image or video */
    //TODO: Figure out if we need this URI
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(encounterHomeDir, type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(File parentFolder, int type)
    {
        // Create a media file name
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(parentFolder + File.separator +
                    "FeedingPhoto" + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(parentFolder + File.separator +
                    "FeedingVideo" + ".mp4");
        } else {
            Log.d(TAG, "getOutputMediaFile: Invalid Media Type provided.");
            return null;
        }

        return mediaFile;
    }

    //endregion

    //region Utility Methods

    //Opens the camera
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

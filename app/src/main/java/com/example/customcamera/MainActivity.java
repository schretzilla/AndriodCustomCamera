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
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class MainActivity extends AppCompatActivity {

    //region private members
    Camera camera;

    //The Layout frame that displays the camera's view
    FrameLayout frameLayout;

    //The Camera Preview
    ShowCamera cameraPreview;

    //Log tag name
    private static final String TAG = "Main Activity";

    //bool for if video is recording
    private boolean isRecording = false;

    //Home directory for all Encounter Data
    private static File encounterHomeDir = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS), "EncounterData");

    //Image Media type
    private static final int MEDIA_TYPE_IMAGE = 1;

    //Video media type
    private static final int MEDIA_TYPE_VIDEO = 2;

    /**
     * Used to handle recording audio and video as well as storing the preferences to use with the
     * audio and video
     */
    MediaRecorder mediaRecorder;

    //endregion

    //region View Methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);

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

        // Create the birdwatch video directory if it does not exist
        if (! encounterHomeDir.exists()){
            if (! encounterHomeDir.mkdirs()){
                Log.d(TAG, "failed to create directory: " + encounterHomeDir.getName());
            }
        }

    }

    //Camera Callbacks
    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera)
        {
            File newParentDir = createEncounterFolder(encounterHomeDir);

            // TODO: Handle all data but for now just write the bird photo
            writeFile(data, newParentDir, "BirdPhoto.jpg");

            //TODO: Remove when view is removed
            camera.startPreview();

        }
    };

    //endregion

    //region Events

    /**
     * Event for handling camera photo button pushes
     * @param v The current view that calls this method
     */
    public void captureImage(View v)
    {
        if(camera != null)
        {
            camera.takePicture(null, null, mPictureCallback);
        }
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
            File encounterFolder = createEncounterFolder(encounterHomeDir);
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

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
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

    //Methods for Saving video from Camera


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

    //

    /**
     * Write the supplied data to the directory to the file name
     * <p>
     *     Note: Not used for writing videos. MediaRecorder handles that.
     * </p>
     * @param data The byte array of data to write
     * @param directory The parent directory to write this new data file in
     * @param fileName The name to use when saving the data to a file.
     */
    private void writeFile(byte[] data, File directory, String fileName)
    {

        if(directory == null || fileName == null)
        {
            return;
        }
        else
        {
            try
            {
                //write data
                File dataFile = new File(directory, fileName);
                FileOutputStream fos = new FileOutputStream(dataFile);
                fos.write(data);
                fos.close();

            } catch(IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Creates a new unique directory in the provided home directory using timestamps
     * @param homeDir The parent directory to create this encounter in
     * @return The newly created uniquely named directory using the timestamp as the unique identifier
     */
    private File createEncounterFolder(File homeDir)
    {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date());

        File encounterFolder = new File(homeDir.getPath() + File.separator + timeStamp);
        encounterFolder.mkdir();

        return encounterFolder;
    }

    //endregion
}

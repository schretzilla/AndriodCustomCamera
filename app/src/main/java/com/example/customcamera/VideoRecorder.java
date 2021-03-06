package com.example.customcamera;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.util.Log;
import android.view.SurfaceView;

import java.io.File;
import java.io.IOException;

public class VideoRecorder
{
    //region private members

    /**
     * bool for if video is recording
     */
    private boolean isRecording = false;

    /**
     * Used to handle recording audio and video as well as storing the preferences to use with the
     * audio and video
     */
    MediaRecorder mediaRecorder;

    /**
     * Tag for debugging log
     */
    private static final String TAG = "VideoRecorder";

    /**
     * Camera instance
     */
    Camera m_camera;

    /**
     * preview is required for any video recording. can be 1x1px surface as workaround.
     */
    SurfaceView m_preview;
    //endregion

    //region getters/setters

    /**
     * Get Recording State
     * @return the current recording state
     */
    public boolean isRecording()
    {
        return this.isRecording;
    }
    //endregion

    //region public methods

    public VideoRecorder(SurfaceView preview)
    {
        m_preview = preview;
    }

    //endregion

    //region public methods

    /**
     * Starts/Stops recording of video from the phones main camera
     */
    public void toggleRecording()
    {
        if (isRecording) {
            stopRecording();
        } else {
            startRecording();
        }
    }

    /**
     * Starts recording if recording is not already happening
     */
    public void startRecording()
    {
        if(isRecording)
        {
           Log.d(TAG, "Already recording video, can't start recording.");
        }
        else
        {
            // initialize video camera
            File encounterFolder = FileUtility.createEncounterFolder(FileUtility.GetEncounterHomeDir());
            boolean videoStartedUpSuccessfully = prepareVideoRecorder(encounterFolder);
            if (videoStartedUpSuccessfully) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                mediaRecorder.start();

                // inform the user that recording has started
                isRecording = true;
            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                // inform user
                Log.d(TAG, "failed to start Media Recorder");
            }
        }
    }

    /**
     * Stops recording and cleans up and resources used for it
     */
    public void stopRecording()
    {
        if(isRecording) {
            // stop recording and release camera
            mediaRecorder.stop();  // stop the recording
            releaseMediaRecorder(); // release the MediaRecorder object
            m_camera.lock();         // take camera access back from MediaRecorder

            isRecording = false;
        }
    }

    /**
     * Stops and frees all recording objects
     * Required on pause of an application otherwise bad things happen
     */
    public void closeRecording()
    {
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }
    //endregion

    //region private methods
    /**
     * Handles locking the camera, configuring and preparing the media recorder instance
     * @param outputFolder The parent folder that the video file will live in
     * @return bool representing the state of success of preparing the video recorder
     */
    private boolean prepareVideoRecorder(File outputFolder){

        m_camera = getCameraInstance();

        mediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        m_camera.unlock();
        mediaRecorder.setCamera(m_camera);

        // Step 2: Set sources
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        String outputFile = FileUtility.getOutputMediaFile(outputFolder, FileUtility.MEDIA_TYPE_VIDEO).toString();
        mediaRecorder.setOutputFile(outputFile);

        // Step 5: Set the preview output
        mediaRecorder.setPreviewDisplay(m_preview.getHolder().getSurface());

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

    private void releaseMediaRecorder(){
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            m_camera.lock();           // lock camera for later use
        }
    }

    /** Create a file Uri for saving an image or video */
    //TODO: Figure out if we need this URI
    private static Uri getOutputMediaFileUri(File parentDirectory, int type){
        return Uri.fromFile(FileUtility.getOutputMediaFile(parentDirectory, type));
    }

    private void releaseCamera() {
        if (m_camera != null) {
            m_camera.release();        // release the camera for other applications
            //m_camera.stopPreview();

            m_camera = null;
        }
    }

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

package com.example.customcamera;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.util.Log;

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

    /**
     * Tag for debugging log
     */
    private static final String TAG = "VideoRecorder";

    /**
     * Camera instance
     */
    Camera m_camera;
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

    /**
     * Handles locking the camera, configuring and preparing the media recorder instance
     * @param outputFolder The parent folder that the video file will live in
     * @return bool representing the state of success of preparing the video recorder
     */
    private boolean prepareVideoRecorder(File outputFolder, ShowCamera cameraPreview){

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
        String outputFile = getOutputMediaFile(outputFolder, MEDIA_TYPE_VIDEO).toString();
        mediaRecorder.setOutputFile(outputFile);

        //TODO: Remove view
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
    public void toggleRecording(ShowCamera cameraPreview)
    {
        if (isRecording) {
            // stop recording and release camera
            mediaRecorder.stop();  // stop the recording
            releaseMediaRecorder(); // release the MediaRecorder object
            m_camera.lock();         // take camera access back from MediaRecorder

            // inform the user that recording has stopped
            isRecording = false;
        } else {
            // initialize video camera
            File encounterFolder = FileUtility.createEncounterFolder(FileUtility.GetEncounterHomeDir());
            boolean videoStartedUpSuccessfully = prepareVideoRecorder(encounterFolder, cameraPreview);
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

    private void releaseMediaRecorder(){
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            m_camera.lock();           // lock camera for later use
        }
    }

    public void closeRecording()
    {
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    /** Create a file Uri for saving an image or video */
    //TODO: Figure out if we need this URI
    private static Uri getOutputMediaFileUri(File parentDirectory, int type){
        return Uri.fromFile(getOutputMediaFile(parentDirectory, type));
    }

    //todo: move to File utility
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

    private void releaseCamera() {
        if (m_camera != null) {
            m_camera.release();        // release the camera for other applications

            //camera = null; //TODO Do we need to set camera to null?
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

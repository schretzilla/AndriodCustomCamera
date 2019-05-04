package com.example.customcamera;

import android.hardware.Camera;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Camera camera;
    FrameLayout frameLayout;
    ShowCamera showCamera;

    //Home directory to store all files in
    File birdWatchHomeDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            , "BirdWatch");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);

        camera = getCameraInstance();

        //show camera view
        showCamera = new ShowCamera(this,camera);
        frameLayout.addView(showCamera);

        //create birdwatch home directory if it doesn't exist
        if(!birdWatchHomeDir.exists())
        {
            birdWatchHomeDir.mkdir();
        }

    }

    //Camera Callbacks
    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera)
        {
            File newParentDir = getUniqeOutputDir();

            // TODO: Handle all data but for now just write the bird photo
            writeFile(data, newParentDir, "BirdPhoto.jpg");

            //TODO: Remove when view is removed
            camera.startPreview();

        }
    };

    // Creates a new unique directory using timestamps and UUID's to store bird data in
    private File getUniqeOutputDir()
    {
        String state = Environment.getExternalStorageState();
        if(!state.equals(Environment.MEDIA_MOUNTED))
        {
            return null;
        }
        else
        {
            //create File with timestamp
            String dateString = DateFormat.getDateTimeInstance().format(new Date());
            File newBirdDirectory = new File(birdWatchHomeDir, dateString + "-" + UUID.randomUUID().toString());

            newBirdDirectory.mkdir();
            return newBirdDirectory;
        }
    }

    //Write the supplied data to the directory to the file name
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

    //Opens the camera
    public static Camera getCameraInstance() {
        Camera c = null;
        try{
            c = Camera.open();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return c;
    }

    // Event for camera photo captured
    public void captureImage(View v)
    {
        if(camera != null)
        {
            camera.takePicture(null, null, mPictureCallback);
        }
    }


}

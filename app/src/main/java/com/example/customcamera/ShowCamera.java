package com.example.customcamera;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class ShowCamera extends SurfaceView implements SurfaceHolder.Callback
{
    Camera camera;

    //holds the view
    SurfaceHolder holder;

    public ShowCamera(Context context, Camera camera)
    {
        super(context);
        this.camera = camera;
        holder = getHolder();
        holder.addCallback(this); //calls surface created
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //set camera params
        Camera.Parameters params = camera.getParameters();

        //change orientation of the camera..
        if(this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
        {
            params.set("orientation", "portrait");
            camera.setDisplayOrientation(90);
            params.setRotation(90);
        }
        else
        {
            params.set("orientation","landscape");
            camera.setDisplayOrientation(0);
            params.setRotation(0);
        }

        //set params
        camera.setParameters(params);
        //tell it where to set the display
        try{
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {


    }
}

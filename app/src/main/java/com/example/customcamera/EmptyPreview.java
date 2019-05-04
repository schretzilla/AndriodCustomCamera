package com.example.customcamera;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Empty Preview for video recording.
 * Video mediarecording with video requires a surface. So we give it a fake one that
 * doesn't require the camera
 */
public class EmptyPreview extends SurfaceView implements SurfaceHolder.Callback
{

    //holds the view
    SurfaceHolder holder;

    public EmptyPreview(Context context) {
        super(context);
        //try without camera first - doesnt work without camera
        holder = getHolder();
        holder.addCallback(this); //calls surface created
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}

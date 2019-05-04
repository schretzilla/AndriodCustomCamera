package com.example.customcamera;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Static File Utility methods
 */
public class FileUtility
{
    //region public constants
    /**
     * Image Media type
     */
    public static final int MEDIA_TYPE_IMAGE = 1;

    /**
     * Video media type
     */
    public static final int MEDIA_TYPE_VIDEO = 2;

    /**
     * Home directory for all Encounter Data
     */
    private static File encounterHomeDir = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS), "EncounterData");

    //endregion

    /**
     * Tag used for debugging output
     */
    private static final String TAG = "FileUtility";


    public static File GetEncounterHomeDir()
    {
        return encounterHomeDir;
    }

    /**
     * Write the supplied data to the directory to the file name
     * <p>
     *     Note: Not used for writing videos. MediaRecorder handles that.
     * </p>
     * @param data The byte array of data to write
     * @param directory The parent directory to write this new data file in
     * @param fileName The name to use when saving the data to a file.
     */
    public static void writeFile(byte[] data, File directory, String fileName)
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
    public static File createEncounterFolder(File homeDir)
    {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date());

        File encounterFolder = new File(homeDir.getPath() + File.separator + timeStamp);
        encounterFolder.mkdir();

        return encounterFolder;
    }

    /** Create a File for saving an image or video */
    public static File getOutputMediaFile(File parentFolder, int type)
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
}

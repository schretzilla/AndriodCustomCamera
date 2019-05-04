package com.example.customcamera;

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
}

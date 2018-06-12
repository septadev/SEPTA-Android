package org.septa.android.app.database;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ExpandDBZip extends AsyncTask<Object, Object, Void> {

    // tag for logging purposes
    private static final String TAG = ExpandDBZip.class.getSimpleName();

    private Context context;
    private ExpandDBZipListener mListener;
    private File zipFile;
    private int version;

    public ExpandDBZip(Context context, ExpandDBZipListener listener, File zipFile, int versionDownloaded) {
        this.context = context;
        this.mListener = listener;
        this.zipFile = zipFile;
        this.version = versionDownloaded;
    }

    @Override
    protected Void doInBackground(Object... voids) {
        final File rootDir = new File(new File(context.getApplicationInfo().dataDir), "databases");

        // expand new database from zip in external storage into app storage
        try {
            ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                File destFile = new File(rootDir, entry.getName());
                if (!entry.isDirectory()) {
                    // if the entry is a file, extracts it
                    extractFile(zipIn, destFile);
                } else {
                    // if the entry is a directory, make the directory
                    destFile.mkdir();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();

                Log.d(TAG, "New DB Expanded Internal Location: " + destFile.getAbsolutePath() + "\nFile Size: " + destFile.length());
            }
            zipIn.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.toString());
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }

        // delete downloaded zip from external storage after expanding inside app
        zipFile.delete();

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        // pass back installed version number
        mListener.afterDBUnzipped(version);
    }

    private void extractFile(ZipInputStream zipIn, File file) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        byte[] bytesIn = new byte[256];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

    public interface ExpandDBZipListener {
        void afterDBUnzipped(int versionInstalled);
    }

}

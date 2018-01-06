package name.glonki.upsidedown;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Glonki on 06.01.2018.
 */

public class PictureStorage {

    public static final String UPSIDE_DOWN_ALBUM = "UpsideDown";

    public static final IOException STORAGE_NOT_WRITABLE = new IOException("Storage not writable");
    public static final IOException CANT_CREATE_DIRECTORY = new IOException("Cannot create directory");
    public static final IOException NO_PERMISSION_TO_WRITE = new IOException("No permission to write");

    public static final Map<IOException, Integer> STORAGE_ERRORS = new HashMap<>();
    static {
        STORAGE_ERRORS.put(STORAGE_NOT_WRITABLE, R.string.storage_not_writable);
        STORAGE_ERRORS.put(CANT_CREATE_DIRECTORY, R.string.cannot_create_dir);
        STORAGE_ERRORS.put(NO_PERMISSION_TO_WRITE, R.string.file_no_permission);
    }

    public static boolean hasWritePermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestWritePermission(Activity activity, int requestCode){
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
    }

    public static boolean externalStorageIsWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static File getUpsideDownAlbumDir() throws IOException {
        return getAlbumStorageDir(UPSIDE_DOWN_ALBUM);
    }

    public static File getAlbumStorageDir(String albumName) throws IOException {
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.exists() && !file.mkdirs()) {
            throw CANT_CREATE_DIRECTORY;
        }
        return file;
    }

    public static void storeBitmap(Context context, String url, Bitmap bitmap) throws IOException {
        if(!hasWritePermission(context)) {
            throw NO_PERMISSION_TO_WRITE;
        }
        if(!externalStorageIsWritable()) {
            throw STORAGE_NOT_WRITABLE;
        }
        File albumDir = getUpsideDownAlbumDir();
        File fileName = new File(albumDir.getAbsolutePath()
                .concat(File.separator).concat(getFileName(url)).concat(".png"));
        storeBitmap(fileName, bitmap);
    }

    private static String getFileName(String url) {
        String result;
        int lastSlash = url.lastIndexOf('/');
        if(lastSlash >= 0) {
            int lastDot = url.lastIndexOf('.');
            if(lastDot > lastSlash) {
                result = removeInvalidFilenameCharacters(url.substring(lastSlash, lastDot));
            } else {
                result = removeInvalidFilenameCharacters(url.substring(lastDot));
            }
            if(result.trim().length() == 0) {
                result = getTimestampFilename();
            }
        } else {
            result = getTimestampFilename();
        }
        return result;
    };

    private static String getTimestampFilename() {
        return Long.toString(System.currentTimeMillis());
    }

    private static String removeInvalidFilenameCharacters(String string) {
        return string.replaceAll("[\\/:*?\"<>|.]", "");
    }

    public static void storeBitmap(File filename, Bitmap bitmap) throws IOException {
        FileOutputStream out = null;
        IOException exception = null;
        try {
            out = new FileOutputStream(filename);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (IOException e) {
            exception = e;
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(exception != null) {
            throw exception;
        }
    }

}

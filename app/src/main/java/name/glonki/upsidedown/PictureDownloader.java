package name.glonki.upsidedown;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by Glonki on 06.01.2018.
 */

public class PictureDownloader extends IntentService {

    private static final String PICTURE_DOWNLOADER = "Picture_downloader";

    public PictureDownloader() {
        super(PICTURE_DOWNLOADER);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Intent resultIntent = new Intent(Constants.PICTURE_BROADCAST_ACTION);

        try {
            String url = intent.getStringExtra(Constants.PICTURE_URL);
            resultIntent.putExtra(Constants.PICTURE_URL, url);
            resultIntent.putExtra(Constants.PICTURE_DATA, downloadPicture(url));
        } catch (Throwable e) {
            e.printStackTrace();
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
    }

    private byte[] downloadPicture(String urlString) throws IOException {
        URL url = new URL(urlString);
        InputStream in = url.openStream();
        Bitmap bitmap = BitmapFactory.decodeStream(in);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

}

package name.glonki.upsidedown;

import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private final static int PERMISSION_REQUEST_CODE = 42;
    private final static IntentFilter statusIntentFilter = new IntentFilter(Constants.PICTURE_BROADCAST_ACTION);

    private PictureReceiver pictureReceiver;
    private EditText urlText;
    private String pictureUrl;
    private ImageView picture;
    private Button downloadButton;
    private View progressBar;
    private boolean resumed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pictureReceiver = new PictureReceiver();

        urlText = findViewById(R.id.url_text);
        downloadButton = findViewById(R.id.download_button);
        picture = findViewById(R.id.picture);
        progressBar = findViewById(R.id.progress_bar);

        PictureUrlWatcher urlWatcher = new PictureUrlWatcher() {
            @Override
            public void onUrlChanged(String url) {
                pictureUrl = url;
            }
        };

        urlText.addTextChangedListener(urlWatcher);

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadPicture(pictureUrl);
            }
        });

        if(!PictureStorage.hasWritePermission(this)) {
            showPermissionRequestDialog();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        resumed = true;
        String clipBoardText = getTextFromClipboard(this);
        if(clipBoardText != null && clipBoardText.trim().length() > 0 && urlIsValid(clipBoardText)) {
            urlText.setText(clipBoardText);
            urlText.setSelection(clipBoardText.length());
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(pictureReceiver, statusIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        resumed = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(pictureReceiver);
        setDownloading(false);
    }

    private static String getTextFromClipboard(Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        String text = null;
        try {
            text = clipboard.getPrimaryClip().getItemAt(0).getText().toString();
        } catch (Exception e) {
        }
        return text;
    }

    private void showPermissionRequestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.permissions_needed_dialog_text)
                .setTitle(R.string.permissions_needed_dialog_title);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                PictureStorage.requestWritePermission(MainActivity.this, PERMISSION_REQUEST_CODE);
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static Bitmap turnUpsideDown(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(180);
        return Bitmap.createBitmap(bitmap , 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode == PERMISSION_REQUEST_CODE) {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                displayToast(R.string.saving_pictures_is_not_possible);
            }
        }
    }

    private void downloadPicture(String urlString) {
        if(urlIsValid(urlString)) {
            setDownloading(true);
            Intent intent = new Intent(this, PictureDownloader.class);
            intent.putExtra(Constants.PICTURE_URL, urlString);
            startService(intent);
        } else {
            displayToast(R.string.invalid_url);
        }
    }

    private static boolean urlIsValid(String url) {
        boolean result;
        try {
            new URL(url);
            result = true;
        } catch (MalformedURLException e) {
            result = false;
        }
        return result;
    }

    private void displayToast(@StringRes int id) {
        Toast.makeText(this, id, Toast.LENGTH_LONG).show();
    }

    private void displayUnknownErrorToast(Exception e) {
        String unknownError = getString(R.string.unknown_error);
        String text = e.getMessage() == null? unknownError : unknownError.concat(": ").concat(e.getMessage());
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    private class PictureReceiver extends BroadcastReceiver {

        private PictureReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if(resumed) {
                if(intent.hasExtra(Constants.PICTURE_DATA)) {
                    try {
                        byte[] byteArray = intent.getByteArrayExtra(Constants.PICTURE_DATA);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                        picture.setImageBitmap(turnUpsideDown(bitmap));
                        String url = intent.getStringExtra(Constants.PICTURE_URL);
                        PictureStorage.storeBitmap(MainActivity.this, url, bitmap);
                        displayToast(R.string.picture_saved);
                    } catch (Exception e) {
                        if(PictureStorage.STORAGE_ERRORS.containsKey(e)) {
                            displayToast(PictureStorage.STORAGE_ERRORS.get(e));
                        } else {
                            displayUnknownErrorToast(e);
                        }
                    }
                } else {
                    displayToast(R.string.invalid_image);
                }
                setDownloading(false);
            }
        }

    }

    private void setDownloading(boolean downloading) {
        if(downloadButton != null && progressBar != null && picture != null) {
            downloadButton.setEnabled(!downloading);
            progressBar.setVisibility(downloading? View.VISIBLE : View.GONE);
            picture.setVisibility(downloading? View.GONE : View.VISIBLE);
        }
    }

}

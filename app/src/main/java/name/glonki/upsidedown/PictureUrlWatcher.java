package name.glonki.upsidedown;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Created by Glonki on 06.01.2018.
 */

public abstract class PictureUrlWatcher implements TextWatcher {

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        onUrlChanged(editable.toString());
    }

    public abstract void onUrlChanged(String url);

}

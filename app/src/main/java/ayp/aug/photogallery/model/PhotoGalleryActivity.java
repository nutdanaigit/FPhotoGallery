package ayp.aug.photogallery.model;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import ayp.aug.photogallery.PhotoGalleryFragment;

public class PhotoGalleryActivity extends SingleFragmentActivity {

    @Override
    protected Fragment onCreateFragment() {
        return PhotoGalleryFragment.newInstance();
    }

    public static Intent newIntent(Context context) {
        return  new Intent(context,PhotoGalleryActivity.class);
    }
}

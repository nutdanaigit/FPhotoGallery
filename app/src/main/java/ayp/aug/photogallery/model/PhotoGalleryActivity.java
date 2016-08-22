package ayp.aug.photogallery.model;

import android.support.v4.app.Fragment;

import ayp.aug.photogallery.PhotoGalleryFragment;

public class PhotoGalleryActivity extends SingleFragmentActivity {

    @Override
    protected Fragment onCreateFragment() {
        return PhotoGalleryFragment.newInstance();
    }
}

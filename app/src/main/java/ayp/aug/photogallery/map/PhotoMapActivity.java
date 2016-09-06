package ayp.aug.photogallery.map;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.support.v4.app.Fragment;

import ayp.aug.photogallery.model.SingleFragmentActivity;

/**
 * Created by Nutdanai on 9/5/2016.
 */
public class PhotoMapActivity extends SingleFragmentActivity {
    private static final String KEY_LOCATION = "KEY_LOCATION";
    private static final String KEY_GALLERYITEMLOC = "KEY_GALLERY_ITEM";
    private static final String KEY_BITMAP = "KEY_BITMAP";

    public static Intent newIntent(Context context , Location location, Location galleryItemLoc, String url){
        Intent i = new  Intent(context,PhotoMapActivity.class);
        i.putExtra(KEY_LOCATION,location);
        i.putExtra(KEY_GALLERYITEMLOC,galleryItemLoc);
        i.putExtra(KEY_BITMAP,url);
        return i;
    }
    @Override
    protected Fragment onCreateFragment() {
        if(getIntent() != null){
            Location location = getIntent().getParcelableExtra(KEY_LOCATION);
            Location galleryLoc = getIntent().getParcelableExtra(KEY_GALLERYITEMLOC);
            String url = getIntent().getStringExtra(KEY_BITMAP);
            return PhotoMapFragment.newInstance(location,galleryLoc,url);
        }
        return PhotoMapFragment.newInstance();
    }
}

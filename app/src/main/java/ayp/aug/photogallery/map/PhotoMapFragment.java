package ayp.aug.photogallery.map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;

import ayp.aug.photogallery.FlickrFetcher;
import ayp.aug.photogallery.R;

/**
 * Created by Nutdanai on 9/5/2016.
 */
public class PhotoMapFragment extends SupportMapFragment {
    private static final String TAG = "PhotoMapFragment";
    private static final String KEY_LOCATION = "KEY_LOCATION";
    private static final String KEY_GALLERYITEMLOC = "KEY_GALLERY_ITEM";
    private static final String KEY_BITMAP = "KEY_BITMAP";
    private GoogleMap mGoogleMap;
    private Location mLocation;
    private Location mLocationGallery;
    private String mUrl;
    private Bitmap mBitmap;
    private MarkerFetcherTask mFetcherTask;


    public static PhotoMapFragment newInstance(Location location, Location galleryItemLoc, String url) {
        Bundle args = new Bundle();
        args.putParcelable(KEY_LOCATION, location);
        args.putParcelable(KEY_GALLERYITEMLOC, galleryItemLoc);
        args.putString(KEY_BITMAP, url);
        PhotoMapFragment fragment = new PhotoMapFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mLocation = getArguments().getParcelable(KEY_LOCATION);
            mLocationGallery = getArguments().getParcelable(KEY_GALLERYITEMLOC);
            mUrl = getArguments().getString(KEY_BITMAP);
        }
        if (mUrl != null) {
            Log.d(TAG, "Get url : " + mUrl);
        }
        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
                if(mUrl==null){
                    updateMapUI();
                }else{
                    mFetcherTask =new MarkerFetcherTask();
                    mFetcherTask.execute(mUrl);
                }
            }
        });
    }

    private void updateMapUI() {
        mGoogleMap.clear();

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        if (mLocation != null) {
            Log.d(TAG, "Found location for my location ");
            plotMarker(mLocation, builder);
        }
        if (mLocationGallery != null) {
            Log.d(TAG, "Found location for gallery item ");
            if(mBitmap ==null) {
                plotMarker(mLocationGallery, builder);
            }else{
                plotMarker(mLocationGallery,builder,mBitmap);
            }
        }
        int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), margin);
        mGoogleMap.animateCamera(cameraUpdate);

    }


    private void plotMarker(final Location location, final LatLngBounds.Builder builder) {
        // ใช้ itemPoint หาจุด ใช้ MarkerOption Mark จุด
        Log.d(TAG, "Plot location = " + location);
        LatLng itemPoint = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions itemMarkerOption = new MarkerOptions()
                .position(itemPoint);
        mGoogleMap.addMarker(itemMarkerOption);
        builder.include(itemPoint);
    }

    private void plotMarker(final Location location ,final LatLngBounds.Builder builder,final  Bitmap bitmap){
        Log.d(TAG, "Plot location = " + location);
        LatLng itemPoint = new LatLng(location.getLatitude(), location.getLongitude());

        //use bitmap as marker
        BitmapDescriptor descriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
        MarkerOptions itemMarkerOption = new MarkerOptions()
                .position(itemPoint)
                .icon(descriptor);
        mGoogleMap.addMarker(itemMarkerOption);
        builder.include(itemPoint);
    }

    private class MarkerFetcherTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            //Fetch photo
            String url = params[0];
            FlickrFetcher flickrFetcher = new FlickrFetcher();
            try {
                byte[] imageBytes = flickrFetcher.getUrlBytes(url);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                return bitmap;
            }catch (IOException ioe){
                Log.e(TAG,"Error in IO "+ ioe );
                return null;
            }


        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mBitmap = bitmap;
            updateMapUI();
            mFetcherTask = null;

        }
    }
}

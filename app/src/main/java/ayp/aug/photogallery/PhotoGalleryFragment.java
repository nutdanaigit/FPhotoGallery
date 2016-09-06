package ayp.aug.photogallery;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.LruCache;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ayp.aug.photogallery.map.PhotoMapActivity;
import ayp.aug.photogallery.model.GalleryItem;
import ayp.aug.photogallery.setting.SettingActivity;

/**
 * Created by Nutdanai on 8/16/2016.
 */
public class PhotoGalleryFragment extends VisibleFragment implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "PhotoGalleryFragment";
    private String mSearchKey;
    private static final int REQUEST_PERMISSION_LOCATION = 394;

    public static PhotoGalleryFragment newInstance() {

        Bundle args = new Bundle();

        PhotoGalleryFragment fragment = new PhotoGalleryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    //    private final LruCache mCache = new LruCache(60);
    private RecyclerView mRecyclerView;
    private PhotoGalleryAdapter mAdapter;
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloaderThread;
    private FetcherTask mFetcherTask;
    private Boolean mUseGPS;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;

    private LruCache<String, Bitmap> mMemoryCache;
    final int maxMemory = (int) (Runtime.getRuntime().maxMemory()) / 1024;
    final int cacheSize = maxMemory / 8;

    private GoogleApiClient.ConnectionCallbacks mConnectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {

        @Override
        @SuppressWarnings("all")
        public void onConnected(@Nullable Bundle bundle) {
            Log.d(TAG, "Google API connect");
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            Log.i(TAG,"Last Location : "+ mLocation);

            if (mUseGPS) {
                findLocation();
                loadPhotos();
            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.i(TAG, "Google API suspended");
        }
    };

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "Got location : " + location.getLatitude() + "," + location.getLongitude());
            mLocation = location;

            if(mUseGPS){
                loadPhotos();
            }

            Toast.makeText(getActivity(), location.getLatitude() + "," + location.getLongitude(), Toast.LENGTH_LONG).show();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        mUseGPS = PhotoGalleryPreference.getUseGPS(getActivity());
        mSearchKey = PhotoGalleryPreference.getStoredSearchKey(getActivity());

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };


        //Move from onCreateView
//        mFlickrFetcher = new FlickrFetcher();q
//        mFetcherTask = new FetcherTask();


//        new FetcherTask().execute(); //run another thread


        Handler responseUIHandler = new Handler();
//        Looper.getMainLooper();

        ThumbnailDownloader.ThumbnailDownloaderListener<PhotoHolder> listener
                = new ThumbnailDownloader.ThumbnailDownloaderListener<PhotoHolder>() {
            @Override
            public void onThumbnailDownloaded(PhotoHolder target, Bitmap thumbnail, String url) {
                if (null == mMemoryCache.get(url)) {
                    mMemoryCache.put(url, thumbnail);
                }
                Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                target.bindDrawable(drawable);
            }
        };


        mThumbnailDownloaderThread = new ThumbnailDownloader<>(responseUIHandler);
        mThumbnailDownloaderThread.setThumbnailDownloaderListner(listener);
        mThumbnailDownloaderThread.start();
        mThumbnailDownloaderThread.getLooper();

        //builder มันจะสร้าง GoogleApi
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(mConnectionCallbacks)
                .addOnConnectionFailedListener(this)
                .build();

        Log.i(TAG, "Start background thread");
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    private void findLocation() {
        Log.d("Google API connect", " " + hasPermission());
        if (hasPermission()) {
            requestLocation();
        }
    }

    private boolean hasPermission() {
        int permissionStatus = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        requestPermissions(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, REQUEST_PERMISSION_LOCATION);

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocation();
            }
        }
    }

    @SuppressWarnings("all")
    private void requestLocation() {

        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setNumUpdates(50); // จำนวนที่update ได้ 50ครั้ง
        request.setInterval(1000); // ระห่างระหว่างครั้งที่Update

        Log.d(TAG, "Request for location");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, mLocationListener);

        LocationAvailability availability = LocationServices.FusedLocationApi.getLocationAvailability(mGoogleApiClient);
        Log.d(TAG, "is Available = " + availability.isLocationAvailable());

    }

    private void unFindLocation() {
        if (mGoogleApiClient.isConnected()) {
            //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloaderThread.quit();
        Log.i(TAG, "Stop background thread");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloaderThread.clearQueue();
    }

    @Override
    public void onPause() {
        super.onPause();
        PhotoGalleryPreference.setStoredSearchKey(getActivity(), mSearchKey);
        unFindLocation();
    }

    @Override
    public void onResume() {
        super.onResume();
        String searchKey = PhotoGalleryPreference.getStoredSearchKey(getActivity());
        if (searchKey != null) {
            mSearchKey = searchKey;
        }

        mUseGPS = PhotoGalleryPreference.getUseGPS(getActivity());

        if(!mUseGPS){
            loadPhotos();
        }
        Log.d(TAG, "On Resume complete ,mSearchKey = " + mSearchKey + ", mUseGPS = " + mUseGPS);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.photo_gallery_recycler_view);
        Resources r = getResources();
        int gridSize = r.getInteger(R.integer.grid_size);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), gridSize));
        mSearchKey = PhotoGalleryPreference.getStoredSearchKey(getActivity());
        Log.d(TAG, "On create completed - Loaded search Key = " + mSearchKey);
//        mRecyclerView.setAdapter(new PhotoGalleryAdapter(itemsList));
        return v;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Connection Failed: " + connectionResult.getErrorMessage());
    }


    class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener, MenuItem.OnMenuItemClickListener, View.OnCreateContextMenuListener {
        ImageView mPhoto;
        Drawable drawableTest;
        GalleryItem mGalleryItem;
        private static final String REQUEST_STRING = "Request_String_Dialog";


        public PhotoHolder(View itemView) {
            super(itemView);
            mPhoto = (ImageView) itemView.findViewById(R.id.image_photo);
            mPhoto.setOnClickListener(this);

            itemView.setOnCreateContextMenuListener(this);

        }

        public void bindDrawable(@NonNull Drawable drawable) {
            this.drawableTest = drawable;
            mPhoto.setImageDrawable(drawable);

        }

        public void bindGalleryItem(GalleryItem galleryItem) {
            mGalleryItem = galleryItem;
        }


        @Override
        public void onClick(View view) {
//            FragmentManager fm = getActivity().getSupportFragmentManager();
//            PhotoGalleryDialog photoGalleryDialog = new PhotoGalleryDialog(drawableTest);
//            photoGalleryDialog.show(fm,REQUEST_STRING);

            Snackbar.make(mRecyclerView, "Clicked  on Photo", Snackbar.LENGTH_SHORT).show();
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final ImageView imageView = new ImageView(getActivity());
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
//            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//            imageView.setMaxHeight(2000);
//            imageView.setMaxWidth(2000);
            builder.setView(imageView);
            builder.setPositiveButton("Close", null);

            // Execute Async Task
            new AsyncTask<String, Void, Bitmap>() {

                @Override
                protected Bitmap doInBackground(String... urls) {
                    FlickrFetcher flickrFetcher = new FlickrFetcher();
                    Bitmap bm = null;
                    try {
                        byte[] bytes = flickrFetcher.getUrlBytes(urls[0]);
                        bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    } catch (IOException ioe) {
                        Log.e(TAG, "error in reading Bitmap", ioe);
                        return null;
                    }
                    return bm;
                }

                @Override
                protected void onProgressUpdate(Void... values) {
                    super.onProgressUpdate(values);
                }

                @Override
                protected void onPostExecute(Bitmap img) {
                    builder.create().show();
                    imageView.setImageDrawable(new BitmapDrawable(getResources(), img));
                }
            }.execute(mGalleryItem.getBigSizeUrl());
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
//            Toast.makeText(getActivity(),"Open menu for . . ." + mGalleryItem,Toast.LENGTH_LONG).show();
            switch (item.getItemId()) {
                case 1:
                    Intent i = new Intent(Intent.ACTION_VIEW, mGalleryItem.getPhotoUri());
                    startActivity(i); // call external browser by implicit intent
                    return true;
                case 2:
                    Intent intentPhotoPage = PhotoPageActivity.newIntent(getActivity(), mGalleryItem.getPhotoUri());
                    startActivity(intentPhotoPage); //Call Internal Activity by explicit Intent
                    return true;
                case 3:
                    Location itemLoc = null;
                    if(mGalleryItem.isGeoCorrect()) {
                        itemLoc = new Location("");
                        itemLoc.setLatitude(Double.valueOf(mGalleryItem.getLat()));
                        itemLoc.setLongitude(Double.valueOf(mGalleryItem.getLon()));
                    }

                    Intent i3 = PhotoMapActivity.newIntent(getActivity(),mLocation,itemLoc,mGalleryItem.getUrl());
                    startActivity(i3);
                    return true;
                default:
            }
            return false;
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(mGalleryItem.getPhotoUri().toString());

            MenuItem menuItem = menu.add(0, 1, 0, R.string.open_with_external_browser);
            menuItem.setOnMenuItemClickListener(this);
            MenuItem menuItem2 = menu.add(0, 2, 0, R.string.open_in_app_browser);
            menuItem2.setOnMenuItemClickListener(this);
            MenuItem menuItem3 = menu.add(0,3,0,R.string.open_in_map);
            menuItem3.setOnMenuItemClickListener(this);
        }
    }

    class PhotoGalleryAdapter extends RecyclerView.Adapter<PhotoHolder> {
        List<GalleryItem> mGalleryItemsList;

        public PhotoGalleryAdapter(List<GalleryItem> mGalleryItemsList) {
            this.mGalleryItemsList = mGalleryItemsList;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_photo, parent, false);

            return new PhotoHolder(v);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            Drawable smileyDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.pokemon, null);
            GalleryItem mGalleryItem = mGalleryItemsList.get(position);
            Log.d(TAG, "bind position # " + position + " , url : " + mGalleryItem.getUrl());
            holder.bindGalleryItem(mGalleryItem);
            holder.bindDrawable(smileyDrawable);

            if (mMemoryCache.get(mGalleryItem.getUrl()) != null) {
                Bitmap bitmap = mMemoryCache.get(mGalleryItem.getUrl());
                holder.bindDrawable(new BitmapDrawable(getResources(), bitmap));
            } else {
                //
                mThumbnailDownloaderThread.queueThumbnailDownloader(holder, mGalleryItem.getUrl());
            }

//            if(mMemoryCache.get(mGalleryItem.getBigSizeUrl())!= null){
//                Bitmap bitmap = mMemoryCache.get(mGalleryItem.getBigSizeUrl());
//                holder.bindDrawable(new BitmapDrawable(getResources(),bitmap));
//            }else{
//                //
//                mThumbnailDownloaderThread.queueThumbnailDownloader(holder ,mGalleryItem.getBigSizeUrl());
//            }

        }

        @Override
        public int getItemCount() {
            return mGalleryItemsList.size();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_item, menu);
        MenuItem menuItem = menu.findItem(R.id.mnu_search);
//        MenuItem menuItem1 = menu.findItem(R.id.mnu_search);
        final SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "Query text submitted: " + query);
                mSearchKey = query;
                loadPhotos();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "Query text submitted: " + newText);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.setQuery(mSearchKey, false);
            }
        });
        MenuItem toggleItem = menu.findItem(R.id.mnu_toggle_polling);
//        if (PollService.isServiceAlarmOn(getActivity())) {
        if (PollService.isServiceAlarmOn(getActivity())) {
            toggleItem.setTitle(R.string.stop_polling);
        } else {
            toggleItem.setTitle(R.string.start_polling);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_reload_photo:
                loadPhotos();
                return true;
            case R.id.mnu_toggle_polling:
                Log.d(TAG, "Start Intent service");
                //        Intent i = PollService.newIntent(getActivity());
                //        getActivity().startService(i);
                boolean shouldStart = !PollService.isServiceAlarmOn(getActivity());
                Log.d(TAG, ((shouldStart) ? "Start" : "Stop") + " Intent service");
                PollService.setServiceAlarm(getActivity(), shouldStart);
                getActivity().invalidateOptionsMenu(); //refresh menu
                return true;
            case R.id.mnu_clear_search:
                mSearchKey = null;
                loadPhotos();
                return true;
            case R.id.mnu_manual_check:
                Intent pollIntent = PollService.newIntent(getActivity());
                getActivity().startService(pollIntent);
                return true;
            case R.id.mnu_setting:
                Intent pSetting = SettingActivity.newIntent(getActivity());
                getActivity().startActivity(pSetting);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }


    private void loadPhotos() {
        if (mFetcherTask == null ) {
            mFetcherTask = new FetcherTask();
            if (mSearchKey != null) {
                mFetcherTask.execute(mSearchKey);
            } else {
                mFetcherTask.execute();
            }
        }else{
            Log.d(TAG,"Fetch task is running now");
        }
    }


    class FetcherTask extends AsyncTask<String, Void, List<GalleryItem>> {
//        boolean running = false;

        @Override
        protected List<GalleryItem> doInBackground(String... params) {
//            synchronized (this) {
//                running = true;
//            }

                Log.d(TAG, "Start fetcher task");
                 List<GalleryItem> itemsList = new ArrayList<>();
                FlickrFetcher flickrFetcher = new FlickrFetcher();
                if (params.length > 0) {
                    if (mUseGPS && mLocation != null) {
                        flickrFetcher.searchPhotos(itemsList, params[0],
                                String.valueOf(mLocation.getLatitude()),
                                String.valueOf(mLocation.getLongitude()));
                    } else {
                        flickrFetcher.searchPhotos(itemsList, params[0]);
                    }
                } else {
                    flickrFetcher.getRecentPhotos(itemsList);
                    // (The end)!!Don't forget fetchItems "s"!!

                }

                Log.d(TAG, "Fetcher fetcher finished");
                return itemsList;

        }


//        boolean isRunning() {
//            return running;
//        }
//        @Override
//        protected void onProgressUpdate(Void... values) {
//            super.onProgressUpdate(values);
//            String formatString = getResources().getString(R.string.photo_progress_loaded);
//            Snackbar.make(mRecyclerView,formatString,Snackbar.LENGTH_SHORT).show();
//        }

        //รับ Response Message จาก doInBackground ที่เป็นตัวสร้างThread และทำงานส่งกลับมา
        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {

            mRecyclerView.setAdapter(new PhotoGalleryAdapter(galleryItems));

            String formatString = getResources().getString(R.string.photo_progress_loaded);
            Snackbar.make(mRecyclerView, formatString, Snackbar.LENGTH_SHORT).show();
            mFetcherTask = null;
        }
    }

}

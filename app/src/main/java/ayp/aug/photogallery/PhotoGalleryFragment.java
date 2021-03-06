package ayp.aug.photogallery;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ayp.aug.photogallery.model.GalleryItem;

/**
 * Created by Nutdanai on 8/16/2016.
 */
public class PhotoGalleryFragment extends Fragment{
     private static final String TAG = "PhotoGalleryFragment";
     private String mSearchKey;

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


    private LruCache<String,Bitmap> mMemoryCache;
    final int maxMemory = (int) (Runtime.getRuntime().maxMemory())/1024;
    final int cacheSize = maxMemory / 8 ;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);



        mMemoryCache = new LruCache<String,Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() /1024;
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
            public void onThumbnailDownloaded(PhotoHolder target, Bitmap thumbnail,String url) {
                if(null == mMemoryCache.get(url)){
                    mMemoryCache.put(url,thumbnail);
                }
                Drawable drawable = new BitmapDrawable(getResources(),thumbnail);
                target.bindDrawable(drawable);
            }
        };



        mThumbnailDownloaderThread = new ThumbnailDownloader<>(responseUIHandler);
        mThumbnailDownloaderThread.setThumbnailDownloaderListner(listener);
        mThumbnailDownloaderThread.start();
        mThumbnailDownloaderThread.getLooper();
        Log.i(TAG,"Start background thread");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloaderThread.quit();
        Log.i(TAG,"Stop background thread");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloaderThread.clearQueue();
    }

    @Override
    public void onPause() {
        super.onPause();
        PhotoGalleryPreference.setStoredSearchKey(getActivity(),mSearchKey);
    }

    @Override
    public void onResume() {
        super.onResume();
        String searchKey = PhotoGalleryPreference.getStoredSearchKey(getActivity());
        if(searchKey != null){
            mSearchKey = searchKey;
        }
        Log.d(TAG,"On Resume complete ");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery,container,false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.photo_gallery_recycler_view);
        Resources  r= getResources();
        int gridSize = r.getInteger(R.integer.grid_size);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),gridSize));
        mSearchKey =PhotoGalleryPreference.getStoredSearchKey(getActivity());
                loadPhotos();
        Log.d(TAG,"On create completed - Loaded search Key = " +mSearchKey);
//        mRecyclerView.setAdapter(new PhotoGalleryAdapter(itemsList));
        return v;
    }

    class PhotoHolder extends  RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView mPhoto;
        Drawable drawableTest;
        private static final String REQUEST_STRING = "Request_String_Dialog";
        private String mBigUrl;

        public PhotoHolder(View itemView) {
            super(itemView);
            mPhoto = (ImageView) itemView.findViewById(R.id.image_photo);
            mPhoto.setOnClickListener(this);

        }

        public void bindDrawable(@NonNull Drawable drawable){
            this.drawableTest = drawable;
            mPhoto.setImageDrawable(drawable);

        }
        public  void setBigUrl(String bigUrl){
            mBigUrl =bigUrl;
        }


        @Override
        public void onClick(View view) {
//            FragmentManager fm = getActivity().getSupportFragmentManager();
//            PhotoGalleryDialog photoGalleryDialog = new PhotoGalleryDialog(drawableTest);
//            photoGalleryDialog.show(fm,REQUEST_STRING);

            Snackbar.make(mRecyclerView,"Clicked  on Photo",Snackbar.LENGTH_SHORT).show();
            final AlertDialog.Builder builder  = new AlertDialog.Builder(getActivity());
            final ImageView imageView = new ImageView(getActivity());
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            builder.setView(imageView);
            builder.setPositiveButton("Close",null);

            // Execute Async Task
            new AsyncTask<String, Void, Bitmap>() {

                @Override
                protected Bitmap doInBackground(String... urls) {
                    FlickrFetcher flickrFetcher = new FlickrFetcher();
                    Bitmap bm = null;
                    try{
                        byte[] bytes = flickrFetcher.getUrlBytes(urls[0]);
                        bm = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    }catch (IOException ioe){
                        Log.e(TAG,"error in reading Bitmap",ioe);
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
                    imageView.setImageDrawable(new BitmapDrawable(getResources(),img));
                }
            }.execute(mBigUrl);
        }


    }
    class PhotoGalleryAdapter extends RecyclerView.Adapter<PhotoHolder>{
        List<GalleryItem> mGalleryItemsList;

        public PhotoGalleryAdapter(List<GalleryItem> mGalleryItemsList) {
            this.mGalleryItemsList = mGalleryItemsList;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_photo,parent,false);

            return new PhotoHolder(v);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            Drawable smileyDrawable = ResourcesCompat.getDrawable(getResources(),R.drawable.pokemon,null);
            GalleryItem mGalleryItem = mGalleryItemsList.get(position);
            Log.d(TAG,"bind position # " + position + " , url : " + mGalleryItem.getUrl());
            holder.setBigUrl(mGalleryItem.getBigSizeUrl());
            holder.bindDrawable(smileyDrawable);

            if(mMemoryCache.get(mGalleryItem.getUrl())!= null){
                Bitmap bitmap = mMemoryCache.get(mGalleryItem.getUrl());
                holder.bindDrawable(new BitmapDrawable(getResources(),bitmap));
            }else{
                //
                mThumbnailDownloaderThread.queueThumbnailDownloader(holder ,mGalleryItem.getUrl());
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
        if (PollService.isServiceAlarmOn(getActivity())) {
            toggleItem.setTitle(R.string.stop_polling);
        } else {
            toggleItem.setTitle(R.string.start_polling);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_reload_photo:
                loadPhotos();
                return true;
            case R.id.mnu_toggle_polling:
                Log.d(TAG,"Start Intent service");
      //        Intent i = PollService.newIntent(getActivity());
                //        getActivity().startService(i);
                boolean shouldStart = !PollService.isServiceAlarmOn(getActivity());
                Log.d(TAG, ((shouldStart)?"Start":"Stop") + " Intent service" );
                PollService.setServiceAlarm(getActivity(),shouldStart);
                getActivity().invalidateOptionsMenu(); //refresh menu
                return true;
            case R.id.mnu_clear_search:
                mSearchKey= null;
                loadPhotos();
                return true;
            default:return super.onOptionsItemSelected(item);

        }
    }


    private void loadPhotos(){
        if(mFetcherTask == null || !mFetcherTask.isRunning()){

            mFetcherTask = new FetcherTask();
            if(mSearchKey !=null) {
                mFetcherTask.execute(mSearchKey);
            }else{
                mFetcherTask.execute();

            }
        }
    }



    class FetcherTask extends AsyncTask<String,Void,List<GalleryItem>>{


        boolean running = false;
        @Override
        protected List<GalleryItem> doInBackground(String ... params) {
            synchronized (this){
                running =true;
            }

            try {
                Log.d(TAG,"Start fetcher task");
                List<GalleryItem> itemsList = new ArrayList<>();
                FlickrFetcher flickrFetcher = new FlickrFetcher();
                if(params.length>0) {
                    flickrFetcher.searchPhotos(itemsList,params[0]);
                }else{
                    flickrFetcher.getRecentPhotos(itemsList);
                    // (The end)!!Don't forget fetchItems "s"!!

                }

                Log.d(TAG,"Fetcher fetcher finished");
                return itemsList;
            }finally {
                synchronized (this) {
                    running = false;
                }
            }
        }

        boolean isRunning(){
            return running;
        }
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

            String formatString=getResources().getString(R.string.photo_progress_loaded);
            Snackbar.make(mRecyclerView,formatString,Snackbar.LENGTH_SHORT).show();
        }
    }

}

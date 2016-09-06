package ayp.aug.photogallery.model;

import android.net.Uri;

/**
 * Created by Nutdanai on 8/16/2016.
 */
public class GalleryItem extends Object {
    private  String mId;
    private  String mTitle;
    private  String mUrl;
    private String bigSizeUrl;
    private String mOwner;
    private String mLat;
    private String mLon;

    public String getUrl() {
        return mUrl;
    }
    public String getTitle() {
        return mTitle;
    }
    public String getId() {
        return mId;
    }
    public String getName(){
        return getTitle();
    }
    public String getLat() {return mLat;}
    public String getLon() {return mLon;}



    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }
    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }
    public void setId(String mId) {
        this.mId = mId;
    }
    public void setName(String name){
        setTitle(name);
    }
    public void setLat(String lat) {this.mLat = lat;}
    public void setLon(String lon) {this.mLon = lon;}

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof GalleryItem){
            //is GalleryItem too!!
            GalleryItem that = (GalleryItem) obj;
           return that.mId!=null && mId!=null && that.mId.equals(mId);
        }
        return false;
    }

    public void setBigSizeUrl(String bigSizeUrl) {
        this.bigSizeUrl = bigSizeUrl;
    }

    public String getBigSizeUrl() {
        return bigSizeUrl;
    }

    public void setOwner(String mOwner) {
        this.mOwner = mOwner;
    }
//    public static void printHello(){}


    public String getOwner() {
        return mOwner;
    }
    private static final String PHOTO_URL_PREFIX = "https://www.flickr.com/photos/" ;
    public Uri getPhotoUri(){
              return   Uri.parse(PHOTO_URL_PREFIX) //Return builder
                      .buildUpon()
                      .appendPath(mOwner)
                      .appendPath(mId)
                      .build();// Return Uri
    }

    public boolean isGeoCorrect(){
        return !"0".equals(mLat) && !"0".equals(mLon);
    }
}

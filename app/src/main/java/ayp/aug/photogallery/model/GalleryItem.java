package ayp.aug.photogallery.model;

/**
 * Created by Nutdanai on 8/16/2016.
 */
public class GalleryItem extends Object {
    private  String mId;
    private  String mTitle;
    private  String mUrl;
    private String bigSizeUrl;

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
//    public static void printHello(){}

}

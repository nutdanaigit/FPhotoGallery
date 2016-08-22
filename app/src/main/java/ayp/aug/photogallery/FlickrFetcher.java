package ayp.aug.photogallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import ayp.aug.photogallery.model.GalleryItem;

/**
 * Created by Nutdanai on 8/16/2016.
 */
public class FlickrFetcher {
    private static final String TAG = "FlickrFetcher";

    /**
     * Connect กับ internet เอาไฟล์ที่ดึงมาจาก JSON มาเขียนเป็นByte
     * @param urlSpec สร้าง object url <b>String</b>
     * @return url byte
     * @throws IOException
     */
    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            //if connection is not OK throw new IOException
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ":with " + urlSpec);
            }
            int byteRead = 0;
            byte[] buffer = new byte[2048];
            while ((byteRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, byteRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    /**
     * รับค่า <b>String</b> มาแล้ว return ค่า <b>String</b> กลับไป
     * ใน return จะมีการเรียกใช้ class String โดยข้างใน Class จะมีการเรียกใช้ Method getUrlBytes พร้อมส่งค่า urlSpec
     *
     * @param urlSpec รับค่า urlSpec <b>String</b>
     * @return <b>String</b> จากการเรียกใช้ class <b>String</b>
     * @throws IOException
     */
    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    //
    private static final String FLICK_URL = "https://api.flickr.com/services/rest/";
    private static final String API_KEY = "0eae29c79033fd52e932785bed5353a6";

    private static final String METHOD_GET_RECENT = "flickr.photos.getRecent";
    private static final String METHOD_SEARCH = "flickr.photos.search";
//    public String fetchItem() throws IOException {
//        String jsonString = null;
//        String url = Uri.parse(FLICK_URL).buildUpon()
//                .appendQueryParameter("method", METHOD_GET_RECENT)
//                .appendQueryParameter("api_key", API_KEY)
//                .appendQueryParameter("format", "json")
//                .appendQueryParameter("nojsoncallback", "1")
//                .appendQueryParameter("extras", "url_s")
//                .build().toString();
//
//        jsonString = getUrlString(url);
//        Log.i(TAG, "Recive JSON " + jsonString);
//
//        return jsonString;
//    }


    /**
     * Create QueryParameter Structure And get baseUrl <b>JSON</b>  form Url
     * get Longer url Stream
     * @param method
     * @param param
     * @return url
     * @throws IOException
     */
    private String buildUri(String method,String ... param) throws IOException {
        Uri baseUrl = Uri.parse(FLICK_URL);
        Uri.Builder builder = baseUrl.buildUpon();
        builder.appendQueryParameter("method", method);
        builder.appendQueryParameter("api_key", API_KEY);
        builder.appendQueryParameter("format", "json");
        builder.appendQueryParameter("nojsoncallback", "1");
        builder.appendQueryParameter("extras", "url_s");
        //equals without case (insensitive).
        if(METHOD_SEARCH.equalsIgnoreCase(method)){
            builder.appendQueryParameter("text",param[0]);
        }

        Uri completeUrl = builder.build();
        String url = completeUrl.toString();

        Log.i(TAG,"Run URL: "+ url);

        return url;
    }

    /**
     * Receive data to <b>JSON STRING</b>
     * @param url
     * @return <b>JSON String</b>
     * @throws IOException
     */
    private String queryItem(String url) throws  IOException{
        Log.i(TAG,"Run URL: "+ url);
        String jsonString = getUrlString(url);
        Log.i(TAG, "Receive JSON " + jsonString);

        return jsonString;
    }


    /**
     * Search photo then put into <b>items</b>
     * @param items array target
     * @param key to search
     */
    public void searchPhotos(List<GalleryItem> items, String key) {
        try {
            String url = buildUri(METHOD_SEARCH,key);
            String jsonStr = queryItem(url);
            if (jsonStr != null) {
                parseJSON(items, jsonStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to fetchItems ", e);
        }
    }

    /**
     * Get Data from url
     * @param items
     */
    public void getRecentPhotos(List<GalleryItem> items) {
        try {
            String url = buildUri(METHOD_GET_RECENT);
            String jsonStr = queryItem(url);
            if (jsonStr != null) {
                parseJSON(items, jsonStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to fetchItems ", e);
        }
    }

    /**
     * Create <b>JSON String</b> to object <b>String</b> list
     * Then get photo list
     *
     * @param newGalleryItemList
     * @param jsonBodyStr
     * @throws IOException
     * @throws JSONException
     */
    private void parseJSON(List<GalleryItem> newGalleryItemList, String jsonBodyStr) throws IOException, JSONException {
        JSONObject jsonBody = new JSONObject(jsonBodyStr);
        JSONObject photosJson = jsonBody.getJSONObject("photos");
        JSONArray photoListJson = photosJson.getJSONArray("photo");

//        JSONArray photoListJson = new JSONObject(jsonBodyStr).getJSONObject("photos").getJSONArray("photo");

        for (int i = 0; i < photoListJson.length(); i++) {
            JSONObject jsonPhotoItem = photoListJson.getJSONObject(i);
            GalleryItem item = new GalleryItem();
            item.setId(jsonPhotoItem.getString("id"));
            item.setTitle(jsonPhotoItem.getString("title"));
            if (!jsonPhotoItem.has("url_s")) {
                continue;
            }

            item.setUrl(jsonPhotoItem.getString("url_s"));
            newGalleryItemList.add(item);
        }
    }

}

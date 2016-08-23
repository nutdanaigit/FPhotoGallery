package ayp.aug.photogallery;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ayp.aug.photogallery.model.GalleryItem;
import ayp.aug.photogallery.model.PhotoGalleryActivity;

/**
 * Created by Nutdanai on 8/22/2016.
 */
public class PollService extends IntentService {
    private static final String TAG = "PollService";
    private static final int POLL_INTERVAL = 1000*60;

    public static Intent newIntent(Context context){
        return new  Intent(context,PollService.class);
    }
    public PollService() {
        super(TAG);
    }

    //Static Method
    public static void setServiceAlarm(Context context, boolean isOn){
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context,0,i,0);
        AlarmManager am  = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);//เอาAlarmManager ออกมาขากระบบ
        if(isOn){
            //AlarmManager.RTC --- > System.currentTimeMillis();
            am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,       // param 1 : Mode
                    SystemClock.elapsedRealtime(),                      // param 2 : Start time elapsed เป็นตัวSystemClock ของตัวAndroid
                    POLL_INTERVAL,                                      // param 3 : Interval ระยะห่าง ระยะเวลา
                    pi);                                                // param 4 : Pending action(intent)
        }else{
            am.cancel(pi);  // cancel interval call
            pi.cancel();    // cancel Pending intent call
        }

    }
    public static boolean isServiceAlarmOn(Context ctx){
        Intent i = PollService.newIntent(ctx);
        PendingIntent pi = PendingIntent.getService(ctx,0,i,PendingIntent.FLAG_NO_CREATE);
        return pi!=null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG,"Receive a call from intent: " + intent);
        if(!isNetworkAvilableAndConnected()){
            return;
        }
        Log.i(TAG,"Active network!!");

        String query = PhotoGalleryPreference.getStoredSearchKey(this);
        String storedLastId = PhotoGalleryPreference.getStoredLastId(this);
        List<GalleryItem> galleryItemsList = new ArrayList<>();
        FlickrFetcher flickrFetcher = new FlickrFetcher();
        if(query == null){
            flickrFetcher.getRecentPhotos(galleryItemsList);
        }else{
            flickrFetcher.searchPhotos(galleryItemsList,query);
        }

        if(galleryItemsList.size() == 0 ){
            return;
        }

        Log.i(TAG,"Found search or recent items");

        String newestId = galleryItemsList.get(0).getId();//fetch first Item
        if(newestId.equals(storedLastId)){
            Log.i(TAG,"No new item");
        }else{
            Log.i(TAG,"New item found");

            Resources res = getResources();
            Intent i = PhotoGalleryActivity.newIntent(this);
            PendingIntent pi = PendingIntent.getActivity(this,0,i,0);

            //Build to build notification object
            NotificationCompat.Builder notification = new NotificationCompat.Builder(this);
            notification.setTicker(res.getString(R.string.new_picture_arriving));
            notification.setSmallIcon(android.R.drawable.ic_menu_report_image);
            notification.setContentTitle(res.getString(R.string.new_picture_title));
            notification.setContentText(res.getString(R.string.new_picture_content));
            notification.setContentIntent(pi);
            notification.setAutoCancel(true);

            Notification notification1= notification.build(); //<<Build notification from
            NotificationManagerCompat nm = NotificationManagerCompat.from(this);
            nm.notify(0,notification1);

        }

        PhotoGalleryPreference.setStoredLastId(this,newestId);
    }


    private  boolean isNetworkAvilableAndConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isActiveNetwork = cm.getActiveNetworkInfo() != null;
        boolean isActiveNetworkConnected = isActiveNetwork && cm . getActiveNetworkInfo().isConnected();
        return isActiveNetworkConnected;
    }
}

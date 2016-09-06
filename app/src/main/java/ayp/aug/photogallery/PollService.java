package ayp.aug.photogallery;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
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
    private static final int POLL_INTERVAL = 1000*1;  //1 sec
    //public broadcast name for this action
    public static final String ACTION_SHOW_NOTIFICATION = "ayp.aug.photogallery.ACTION_SHOW_NOTIFICATION";
    public static final String PERMISSION_SHOW_NOTF = "ayp.aug.photogallery.RECEIVE_SHOW_NOTIFICATION";
    public static final String NOTIFICATION = "ayp.aug.photogallery.RECEIVE_SHOW_NOTIFICATION";
    public static final String REQUEST_CODE="request_code";



    public static Intent newIntent(Context context){
        return new  Intent(context,PollService.class);
    }
    public PollService() {
        super(TAG);
    }

    //Static Method ใช้ตัว Alarm ตั้งเตื่อน โดยใช้ AlarmManger ในารจัดการ
    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0); //PendingIntent ตั้งการนัดหมาย Schedule ของตัวService
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);//เอาAlarmManager ออกมาขากระบบ
        //มี 2 Mode เปิดกับปิด
        if (isOn) {
//            if(Build.VERSION.SDK_INT< Build.VERSION_CODES.LOLLIPOP) {
            //AlarmManager.RTC --->> System.currentTimeMillis();
            //ELAPSED_REALTIME คือเวลาตั้งแต่เรา boot เครื่อง  |  WAKEUP  ทำงานทั้งตอนปิดจอและเปิดจออยู่ แต่ถ้าไม่มีก็จะทำงานแค่ตอนเปิดจอ เท่านั้น

            am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,// param 1 : Mode
                    SystemClock.elapsedRealtime(),                      // param 2 : Start time elapsed เป็นตัวSystemClock ของตัวAndroid
                    POLL_INTERVAL,                                      // param 3 : Interval ระยะห่าง ระยะเวลา
                    pi);                                                // param 4 : Pending action(intent)

            //RTC_WAKEUP เครื่องเราปิดอยู่มันจะ wake ชึ้นมา
            //Exact คือเริ่มทำตอนที่เราสั่ง  am.setExact(AlarmManager.RTC);
            Log.d(TAG, "Run by Alarm Manager");
        } else {
            am.cancel(pi);  // cancel interval call
            pi.cancel();    // cancel Pending intent call

        }
        /**
         *  Run by Scheduler
         */
//            }else{
//                PollJobService.start(context);
//                Log.d(TAG,"Run by Scheduler");
//            }
////        }else{
////            if(Build.VERSION.SDK_INT< Build.VERSION_CODES.LOLLIPOP) {
////                am.cancel(pi);  // cancel interval call
////                pi.cancel();    // cancel Pending intent call
////            }else{
////                PollJobService.stop(context);
////            }
////        }
        PhotoGalleryPreference.setStoredIsAlarmOn(context,isOn);
    }



    public static boolean isServiceAlarmOn(Context ctx){
//        if(Build.VERSION.SDK_INT< Build.VERSION_CODES.LOLLIPOP) {
            Intent i = PollService.newIntent(ctx);
            PendingIntent pi = PendingIntent.getService(ctx, 0, i, PendingIntent.FLAG_NO_CREATE);
            return pi != null;
//        }else{
//           return PollJobService.isRun(ctx);
//        }
    }

    //คือการStart Service ทำแค่1เดียว
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG,"Receive a call from intent: " + intent);

        if(!isNetworkAvailableAndConnected()){
            return;
        }
        Log.i(TAG,"Active network!!");

        String query = PhotoGalleryPreference.getStoredSearchKey(this);
        String storedLastId = PhotoGalleryPreference.getStoredLastId(this);
//                String query = PhotoGalleryPreference.mySharedPref(this).getString(PhotoGalleryPreference.PREF_SEARCH_KEY,null);
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

        String newestId = galleryItemsList.get(0).getId();//fetch first Item เวลามีรูปใหม่มันจะใส่ไว้ใน on Top เราถึงCheck แค่ตำแหน่งแรกคือ 0
        if(newestId.equals(storedLastId)){
            Log.i(TAG,"No new item");
        }else{
            Log.i(TAG,"New item found");

            Resources res = getResources();
            Intent i = PhotoGalleryActivity.newIntent(this);
            PendingIntent pi = PendingIntent.getActivity(this,0,i,0); //PendingIntent ของ getActivity

            //Build to build notification object
            NotificationCompat.Builder notification = new NotificationCompat.Builder(this);
            notification.setTicker(res.getString(R.string.new_picture_arriving));
            notification.setSmallIcon(android.R.drawable.ic_menu_report_image);
            notification.setContentTitle(res.getString(R.string.new_picture_title));
            notification.setContentText(res.getString(R.string.new_picture_content));
            notification.setContentIntent(pi); //ตรงนี้พอกดที่ Notification มันจะไปเรียก Activity
            notification.setAutoCancel(true); //ถ้ามีNotification เวลากดเข้าไปมันจะหายจาก Notification

            Notification notification1= notification.build(); // << Build notification from builder
//            NotificationManagerCompat nm = NotificationManagerCompat.from(this);
//            nm.notify(0,notification1);
            //newestId.hashCode() เป็น hashCode() ของตัวString การทำให้Android มีNotification มากกว่า 1 (เหมือนLine)
            //Long.valueOf(newestId).intValue() เหมือนกัน
//            new Screen().on(this);

            /**
             * Start Broadcast
             */
            // ทำการBroadcast ออกไป
//            sendBroadcast(new Intent((ACTION_SHOW_NOTIFICATION)),PERMISSION_SHOW_NOTF); //STEP1

            sendBackgroundNotification(0,notification1); //STEP 2
        }

        PhotoGalleryPreference.setStoredLastId(this,newestId);
    }

    private void sendBackgroundNotification(int requestCode , Notification notification){
        Intent intent = new Intent(ACTION_SHOW_NOTIFICATION);
        intent.putExtra(REQUEST_CODE,requestCode);
        intent.putExtra(NOTIFICATION,notification);

        sendOrderedBroadcast(intent,PERMISSION_SHOW_NOTF,null,null, Activity.RESULT_OK,null,null);
    }

    private  boolean isNetworkAvailableAndConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isActiveNetwork = cm.getActiveNetworkInfo() != null;
        boolean isActiveNetworkConnected = isActiveNetwork && cm . getActiveNetworkInfo().isConnected();
        return isActiveNetworkConnected;
    }
}

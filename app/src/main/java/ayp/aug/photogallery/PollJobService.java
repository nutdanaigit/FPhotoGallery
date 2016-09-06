package ayp.aug.photogallery;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ayp.aug.photogallery.model.GalleryItem;
import ayp.aug.photogallery.model.PhotoGalleryActivity;

/**
 * Created by Nutdanai on 8/23/2016.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class PollJobService extends JobService {
    private static final String TAG = "PollJobService";
//    private static final int
    private PollTask mPollTask;

    @Override
    public boolean onStartJob(JobParameters params) {
        mPollTask = new PollTask();
        mPollTask.execute(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (mPollTask != null) {
            mPollTask.cancel(true);
        }
        return true;
    }

    public static final int JOB_ID = 2186;

    public static boolean isRun(Context ctx) {
        JobScheduler sch = (JobScheduler) ctx.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        List<JobInfo> jobInfoList = sch.getAllPendingJobs();
        for (JobInfo jobInfo : jobInfoList) {
            if (jobInfo.getId() == JOB_ID) {
                return true;
            }
        }
        return false;
    }
    public static void stop(Context context){
        JobScheduler sch = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        sch.cancel(JOB_ID);
    }

    public static void start(Context context) {

        JobScheduler sch = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, new ComponentName(context, PollJobService.class));
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        builder.setPeriodic(1000 * 60);
//       builder.setPersisted(true);
        JobInfo jobInfo = builder.build();
        sch.schedule(jobInfo);
    }

    public class PollTask extends AsyncTask<JobParameters, Void, Void> {

        @Override
        protected Void doInBackground(JobParameters... params) {
            //Do whatever job
            //
            Log.d(TAG, "Job Poll running");


            String query = PhotoGalleryPreference.getStoredSearchKey(PollJobService.this);
            String storedLastId = PhotoGalleryPreference.getStoredLastId(PollJobService.this);
//                String query = PhotoGalleryPreference.mySharedPref(this).getString(PhotoGalleryPreference.PREF_SEARCH_KEY,null);
            List<GalleryItem> galleryItemsList = new ArrayList<>();
            FlickrFetcher flickrFetcher = new FlickrFetcher();
            if (query == null) {
                flickrFetcher.getRecentPhotos(galleryItemsList);
            } else {
                flickrFetcher.searchPhotos(galleryItemsList, query);
            }

            if (galleryItemsList.size() == 0) {
                return null;
            }

            Log.i(TAG, "Found search or recent items");

            String newestId = galleryItemsList.get(0).getId();//fetch first Item เวลามีรูปใหม่มันจะใส่ไว้ใน on Top เราถึงCheck แค่ตำแหน่งแรกคือ 0
            if (newestId.equals(storedLastId)) {
                Log.i(TAG, "No new item");
            } else {
                Log.i(TAG, "New item found");

                Resources res = getResources();
                Intent i = PhotoGalleryActivity.newIntent(PollJobService.this);
                PendingIntent pi = PendingIntent.getActivity(PollJobService.this, 0, i, 0); //PendingIntent ของ getActivity

                //Build to build notification object
                NotificationCompat.Builder notification = new NotificationCompat.Builder(PollJobService.this);
                notification.setTicker(res.getString(R.string.new_picture_arriving));
                notification.setSmallIcon(android.R.drawable.ic_menu_report_image);
                notification.setContentTitle(res.getString(R.string.new_picture_title));
                notification.setContentText(res.getString(R.string.new_picture_content));
                notification.setContentIntent(pi); //ตรงนี้พอกดที่ Notification มันจะไปเรียก Activity
                notification.setAutoCancel(true); //ถ้ามีNotification เวลากดเข้าไปมันจะหายจาก Notification

                Notification notification1 = notification.build(); // << Build notification from builder
                NotificationManagerCompat nm = NotificationManagerCompat.from(PollJobService.this);
                nm.notify(0, notification1);
                new Screen().on(PollJobService.this);
            }

                PhotoGalleryPreference.setStoredLastId(PollJobService.this, newestId);
                //Finish
                jobFinished(params[0], false);
                return null;
            }
    }
}

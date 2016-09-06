package ayp.aug.photogallery;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class VisibleFragment extends Fragment {

     private static final String TAG = "VisibleFragment";

    public VisibleFragment() {
        // Required empty public constructor
    }
    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Toast.makeText(getActivity(), "Got a broadcast: "+intent.getAction(), Toast.LENGTH_SHORT).show(); //Step1
            Log.d(TAG,"In application receiver");
            setResultCode(Activity.RESULT_CANCELED); //ถ้าเราอยู่ในแอพตัวนี้จะไปทำ
        }
    };
    @Override
    public void onStart() {
        super.onStart();
        // Create IntentFilter / as same AndroidManifest
        IntentFilter intentFilter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        // มีตัวรับมารับ - ใครก็ตามแต่ Broadcast มา ตัวนี้จะเป็นตัวรับ
        getActivity().registerReceiver(mOnShowNotification,intentFilter,PollService.PERMISSION_SHOW_NOTF,null);
    }

    @Override
    public void onStop() {
        super.onStop();
        //แต่เมื่อเราปิดแอพออกไปมันจะไม่รับ Broadcast
        getActivity().unregisterReceiver(mOnShowNotification);
    }
}

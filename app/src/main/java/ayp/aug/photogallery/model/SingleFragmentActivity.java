package ayp.aug.photogallery.model;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import ayp.aug.photogallery.R;

/**
 * Created by Nutdanai on 8/16/2016.
 */
public abstract class SingleFragmentActivity extends AppCompatActivity {
    private static final String TAG = "SingleFragmentActivity";

        @LayoutRes
        protected int getLayoutResId()
        {
            return R.layout.activity_photo_gallery;
        }
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(getLayoutResId());

            Log.d(TAG,"On create activity");

            FragmentManager fm = getSupportFragmentManager();
            Fragment f =fm.findFragmentById(R.id.fragment_container);

            if( f == null){
                f = onCreateFragment(); //CreateFragment Get Method form onCreateFragment
                //
                fm.beginTransaction()
                        .add(R.id.fragment_container,f)
                        .commit();
                Log.d(TAG," Fragment is created");
            }else{
                Log.d(TAG," Fragment have already been created");
            }
        }
        protected abstract Fragment onCreateFragment();
    }


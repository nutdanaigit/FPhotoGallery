package ayp.aug.photogallery;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

/**
 * Created by Nutdanai on 8/29/2016.
 */
public class PhotoPageFragment extends VisibleFragment {

    private static final String KEY_URI ="KEY_URI" ;
    private Uri mUri;
    private WebView webView;
    private ProgressBar mProgressBar;


    public static PhotoPageFragment newInstance(Uri uri) {
        Bundle args = new Bundle();
        args.putParcelable(KEY_URI,uri);
        //Parcelable เป็น Object อีกชนิดหนึ่ง คล้ายๆSerializable เป็นการทำงานแบบStream
        //Uri implement Parcelable ไว้ จึงถึงใช้ตัวนี้กับ Uri
        PhotoPageFragment fragment = new PhotoPageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            mUri = getArguments().getParcelable(KEY_URI);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_page,container,false);

        mProgressBar = (ProgressBar) v.findViewById(R.id.fragment_photo_web_progress);
        mProgressBar.setMax(100);

        webView = (WebView) v.findViewById(R.id.fragment_photo_web_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                }else{
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(newProgress);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
                appCompatActivity.getSupportActionBar().setSubtitle(title);
            }
        });
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });
        webView.loadUrl(mUri.toString());
        return v;
    }

}

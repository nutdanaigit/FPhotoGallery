package ayp.aug.photogallery;

import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Nutdanai on 8/22/2016.
 */
public class PhotoGalleryDialog extends DialogFragment {
    private ImageView mImageView;
    private Drawable mImage;
    private static final String REQUEST_KEY = "TestGallery";

    public PhotoGalleryDialog(Drawable mImage) {
        this.mImage = mImage;
    }

//        public static PhotoGalleryDialog newInstance() {
//
//        Bundle args = new Bundle();
//        args.putSerializable(REQUEST_KEY, gallery);
//
//        PhotoGalleryDialog fragment = new PhotoGalleryDialog();
//        fragment.setArguments(args);
//        return fragment;
//    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_photo_gallery,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        mImageView = (ImageView) v.findViewById(R.id.image_dialog);
        mImageView.setImageDrawable(mImage);
        builder.setView(v);
        builder.setTitle("Image View");

        builder.setPositiveButton("OK",null);
        return builder.create();

    }
}

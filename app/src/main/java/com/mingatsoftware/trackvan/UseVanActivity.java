package com.mingatsoftware.trackvan;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mingatsoftware.trackvan.models.Van;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UseVanActivity extends AppCompatActivity {

    private static final String TAG = "ReturnVanFragment";

    String mVanId;
    TextView mModel_tv;
    TextView mYear_tv;
    TextView mName_tv;
    TextView mLicense_tv;
    ImageView mVan_iv;
    private static final String VAN_IMAGE_URL = "http://worldartsme.com/images/van-clipart-1.jpg";

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mUid;
    private DatabaseReference mFirebaseDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_use_van);

        mModel_tv = (TextView)findViewById (R.id.activity_use_van_model_tv);
        mYear_tv = (TextView)findViewById (R.id.activity_use_van_year_tv);
        mName_tv = (TextView)findViewById (R.id.activity_use_van_name_tv);
        mLicense_tv = (TextView)findViewById (R.id.activity_use_van_plate_tv);

        mVan_iv = (ImageView ) findViewById (R.id.van_imageView);
        mVan_iv.setTag (VAN_IMAGE_URL);
        new DownloadImageTask().execute(mVan_iv);

        // code transition - enter
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Slide slide = new Slide (Gravity.BOTTOM);
            slide.addTarget (R.id.fragment_placeholder);
            slide.setInterpolator (AnimationUtils.loadInterpolator (this,
                    android.R.interpolator.linear_out_slow_in));
            //slide.setDuration (slideDuration);
            getWindow ().setEnterTransition (slide);
        }

        // Instantiate database
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null) {
            mUid = mFirebaseUser.getUid ();
            //Log.d("MYDEBUG", "Login username:" +  mFirebaseUser.getDisplayName () + " uid: " + mUid);
        }

        Intent intent = getIntent ();
        mVanId = intent.getStringExtra (MainActivity.EXTRA_VAN_ID);
        //Log.d("MYDEBUG", "Extra VAN:" +  mVanId);

        Query query = mFirebaseDatabaseReference.child ("vans").child (mVanId);
        query.addListenerForSingleValueEvent (new ValueEventListener () {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists ()){
                    mModel_tv.setText(dataSnapshot.child ("model").getValue ().toString ());
                    mName_tv.setText(dataSnapshot.child ("name").getValue ().toString ());
                    mYear_tv.setText(dataSnapshot.child ("year").getValue ().toString ());
                    mLicense_tv.setText(dataSnapshot.getKey ());

                    if (dataSnapshot.child ("inUse").exists()) {
                        //Toast.makeText(getApplicationContext (), "VanId ["+ mVanId + "] is in-use!", Toast.LENGTH_LONG).show();
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.fragment_placeholder, ReturnVanFragment.newInstance (mUid, mVanId));
                        ft.commit();
                    } else {
                        // Begin the transaction
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        // Replace the contents of the container with the new fragment
                        ft.replace(R.id.fragment_placeholder, UseVanFragment.newInstance (mUid, mVanId));
                        // Complete the changes added above
                        ft.commit();
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, getString (R.string.firebase_data_fail_load), databaseError.toException());
            }
        });

    }


    private class DownloadImageTask extends AsyncTask<ImageView, Void, Bitmap> {

        ImageView imageView = null;

        @Override
        protected Bitmap doInBackground(ImageView... imageViews) {
            this.imageView = imageViews[0];
            return download_Image((String)imageView.getTag());
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }

        private Bitmap download_Image(String url) {

            Bitmap bmp =null;
            try{
                URL ulrn = new URL(url);
                HttpURLConnection con = (HttpURLConnection)ulrn.openConnection();
                InputStream is = con.getInputStream();
                bmp = BitmapFactory.decodeStream(is);
                if (null != bmp)
                    return bmp;

            }catch(Exception e){}
            return bmp;
        }
    }

}

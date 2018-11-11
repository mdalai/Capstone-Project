package com.mingatsoftware.trackvan;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;

public class SubscribeVanActivity extends AppCompatActivity {

    private static final String TAG = "ReturnVanFragment";

    EditText mVanId_et;
    String mVanId;
    TextView mModel_tv;
    TextView mYear_tv;
    TextView mName_tv;
    TextView mLicense_tv;
    Button mSubmit_btn;

    // Firebase instance variables
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    String mUid;
    DatabaseReference mFirebaseDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_subscribe_van);

        mVanId_et = (EditText)findViewById (R.id.sub_van_search_et);
        //mVanId_et.setText ("XYY501");
        mModel_tv = (TextView)findViewById (R.id.sub_van_model_tv);
        mYear_tv = (TextView)findViewById (R.id.sub_van_year_tv);
        mName_tv = (TextView)findViewById (R.id.sub_van_name_tv);
        mLicense_tv = (TextView)findViewById (R.id.sub_van_license_tv);
        mSubmit_btn = (Button ) findViewById (R.id.sub_van_submit_btn);
        mSubmit_btn.setEnabled (false);

        // Instantiate database
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null) {
            mUid = mFirebaseUser.getUid ();
            //Log.d("MYDEBUG", "Login username:" +  mFirebaseUser.getDisplayName () + " uid: " + mUid);
        }

    }

    public void onClickFindVan(View view){
        //get the input vanId
        EditText vanId_et = (EditText)findViewById (R.id.sub_van_search_et);
        mVanId = vanId_et.getText ().toString ();

        // query database
        Query query = mFirebaseDatabaseReference.child("vans").child (mVanId);
        query.addListenerForSingleValueEvent(new ValueEventListener () {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mModel_tv.setText (dataSnapshot.child ("model").getValue ().toString ());
                    mYear_tv.setText (dataSnapshot.child ("year").getValue ().toString ());
                    mName_tv.setText (dataSnapshot.child ("name").getValue ().toString ());
                    mLicense_tv.setText (dataSnapshot.getKey ());
                    mSubmit_btn.setEnabled (true);
                    //for (DataSnapshot vans : dataSnapshot.getChildren()) { Log.d("MYDEBUG", "Key:" +  vans.getKey () + " | Value: " + vans.getValue ());}
                } else {
                    mSubmit_btn.setEnabled (false);
                    Toast.makeText(getApplicationContext (), getString (R.string.firebase_data_fail_toast_text)+ mVanId , Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, getString (R.string.firebase_data_load_fail_log_text), databaseError.toException());
            }
        });
    }

    public void onClickSubmitVan(View view){
        DatabaseReference usersRef = mFirebaseDatabaseReference.child("users");
        usersRef.child (mUid).child ("vans").child (mVanId).setValue (ServerValue.TIMESTAMP);
        DatabaseReference vanRef = mFirebaseDatabaseReference.child("vans").child (mVanId);
        vanRef.child ("users").child (mUid).setValue (true);
        //Toast.makeText (getApplicationContext (), "VanId: " + mVanId, Toast.LENGTH_LONG).show ();
        finish ();
    }
}

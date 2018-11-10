package com.mingatsoftware.trackvan;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.mingatsoftware.trackvan.models.Van;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ReturnVanFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ReturnVanFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReturnVanFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ReturnVanFragment";

    EditText mReturnMileage_et;
    Button mSubmit_btn;
    private DatabaseReference mDatabase;
    Van.InUse inUse;

    private String mUser;
    private String mStartMileage;
    private String mStartTime;
    private String mDestination;
    private String mDepartment;
    private String mWillReturn;

    private static final String ARG_PARAM1_UID = "param1";
    private static final String ARG_PARAM2_VANID = "param2";

    // TODO: Rename and change types of parameters
    private String mUid;
    private String mVanId;


    public ReturnVanFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReturnVanFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReturnVanFragment newInstance(String param1, String param2) {
        ReturnVanFragment fragment = new ReturnVanFragment ();
        Bundle args = new Bundle ();
        args.putString (ARG_PARAM1_UID, param1);
        args.putString (ARG_PARAM2_VANID, param2);
        fragment.setArguments (args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        if (getArguments () != null) {
            mUid = getArguments ().getString (ARG_PARAM1_UID);
            mVanId = getArguments ().getString (ARG_PARAM2_VANID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate (R.layout.fragment_return_van, container, false);

        mReturnMileage_et = (EditText)view.findViewById (R.id.return_van_mileage_et);
        mReturnMileage_et.setText ("10010");
        mSubmit_btn = (Button ) view.findViewById (R.id.return_van_submit_btn);
        mSubmit_btn.setOnClickListener (this);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Instantiate database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        Query query = mDatabase.child ("vans").child (mVanId);
        query.addListenerForSingleValueEvent (new ValueEventListener () {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists ()){
                    inUse = dataSnapshot.child ("inUse").getValue (Van.InUse.class);
                    //Log.d ("MY-DEBUG", "USER ID: "+ inUse.getDepartment ());
                } else {
                    Log.d (TAG, getString (R.string.firebase_data_fail_insue));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId ()){
            case R.id.return_van_submit_btn:

                //String usageId = ServerValue.TIMESTAMP.toString ();
                inUse.setReturnMileage (Integer.parseInt (mReturnMileage_et.getText ().toString ()));
                String usageId = mDatabase.child ("usage").child (mVanId).push ().getKey ();
                mDatabase.child ("usage").child (mVanId).child (usageId).setValue (inUse);
                mDatabase.child ("usage").child (mVanId).child (usageId).child ("returnTime").setValue (ServerValue.TIMESTAMP);

                mDatabase.child ("vans").child (mVanId).child ("inUse").removeValue ();

                getActivity().onBackPressed(); // close
                break;
        }

    }
}

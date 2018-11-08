package com.mingatsoftware.trackvan;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.mingatsoftware.trackvan.models.Van;

import java.sql.Timestamp;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UseVanFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UseVanFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UseVanFragment extends Fragment implements View.OnClickListener {


    EditText mStart_et;
    EditText mDestination_et;
    EditText mDepartment_et;
    EditText mWillReturn_et;
    Button mSubmit_btn;

    Van.InUse inUse;
    private DatabaseReference mDatabase;

    private static final String ARG_PARAM1_UID = "param1";
    private static final String ARG_PARAM2_VANID = "param2";

    private String mUid;
    private String mVanId;

    public UseVanFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UseVanFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UseVanFragment newInstance(String param1, String param2) {
        UseVanFragment fragment = new UseVanFragment ();
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
        View view =  inflater.inflate (R.layout.fragment_use_van, container, false);

        mStart_et = (EditText)view.findViewById (R.id.use_van_start_mileage_et);
        mStart_et.setText ("10000");
        mDestination_et = (EditText)view.findViewById (R.id.use_van_destination_et);
        mDestination_et.setText ("West Bend Walmart");
        mDepartment_et = (EditText)view.findViewById (R.id.use_van_department_et);
        mDepartment_et.setText ("Dorm");
        mWillReturn_et = (EditText)view.findViewById (R.id.use_van_will_return_et);
        mWillReturn_et.setText ("2");
        mSubmit_btn = (Button ) view.findViewById (R.id.use_van_submit_btn);
        mSubmit_btn.setOnClickListener (this);
        //mSubmit_btn.setEnabled (false);

        return view;

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Instantiate database
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId ()){
            case R.id.use_van_submit_btn:
                inUse = new Van.InUse (mUid,
                        Integer.parseInt (String.valueOf (mStart_et.getText ())),
                        //ServerValue.TIMESTAMP,
                        String.valueOf (mDestination_et.getText ()),
                        String.valueOf (mDepartment_et.getText ()),
                        Double.parseDouble (String.valueOf (mWillReturn_et.getText ())),
                        0
                        //null
                );

                mDatabase.child ("vans").child (mVanId).child ("inUse").setValue (inUse);
                closeFragment();
                break;
        }
    }

    public void closeFragment(){
        getActivity().onBackPressed();
    }

}

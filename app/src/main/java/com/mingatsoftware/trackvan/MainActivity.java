package com.mingatsoftware.trackvan;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.github.brnunes.swipeablerecyclerview.SwipeableRecyclerViewTouchListener;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mingatsoftware.trackvan.models.Van;

import java.util.ArrayList;
//import com.google.android.gms.auth.api.Auth;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    public static class VanViewHolder extends RecyclerView.ViewHolder {
        TextView vanName_tv;
        TextView vanModel_tv;
        TextView vanYear_tv;
        TextView vanPlate_tv;

        TextView vanLabel1_tv,vanLabel2_tv,vanLabel3_tv;
        TextView vanVal1_tv,vanVal2_tv,vanVal3_tv;

        public VanViewHolder(View view) {
            super(view);
            vanName_tv =  (TextView) itemView.findViewById(R.id.user_van_name_tv);
            vanModel_tv = (TextView) itemView.findViewById(R.id.user_van_model_tv);
            vanYear_tv = (TextView) itemView.findViewById(R.id.user_van_year_tv);
            vanPlate_tv = (TextView) itemView.findViewById(R.id.user_van_plate_tv);

            vanLabel1_tv = (TextView ) itemView.findViewById (R.id.user_van_label1_tv);
            vanLabel2_tv = (TextView ) itemView.findViewById (R.id.user_van_label2_tv);
            vanLabel3_tv = (TextView ) itemView.findViewById (R.id.user_van_label3_tv);
            vanVal1_tv = (TextView ) itemView.findViewById (R.id.user_van_val1_tv);
            vanVal2_tv = (TextView ) itemView.findViewById (R.id.user_van_val2_tv);
            vanVal3_tv = (TextView ) itemView.findViewById (R.id.user_van_val3_tv);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mClickListener.onItemClick(view, getAdapterPosition ());
                }
            });
            /*
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mClickListener.onItemLongClick(v, getAdapterPosition());
                    return true;
                }
            }); */
        }

        private VanViewHolder.ClickListener mClickListener;
        //Interface to send callbacks...
        public interface ClickListener{
            public void onItemClick(View view, int position);
            //public void onItemLongClick(View view, int position);
        }

        public void setOnClickListener(VanViewHolder.ClickListener clickListener){
            mClickListener = clickListener;
        }

    }


    private static final String TAG = "MainActivity";
    public static final String EXTRA_VAN_ID = "VAN_ID";
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";
    public static final String ANONYMOUS = "anonymous";

    private String mUsername;
    private String mPhotoUrl;

    ArrayList<String> mUserVanIds = new ArrayList<> ();


    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;
    private RecyclerView mVanRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private FirebaseRecyclerAdapter<Van, VanViewHolder> mFirebaseAdapter;

    private DatabaseReference mVansRef;
    private DatabaseReference mUserRef;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);
        Toolbar toolbar = ( Toolbar ) findViewById (R.id.toolbar);
        setSupportActionBar (toolbar);

        FloatingActionButton fab = ( FloatingActionButton ) findViewById (R.id.fab);
        fab.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                //Snackbar.make (view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction ("Action", null).show ();
                startActivity (new Intent (MainActivity.this, SubscribeVanActivity.class));
            }
        });

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent (this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();
        //Log.d(TAG, "Login username:" + mUsername + " uid: " + mFirebaseUser.getUid ());


        // populate RecyclerView with Firebase-UI-database -----------------------------------------------------------
        mVanRecyclerView = (RecyclerView ) findViewById (R.id.vanRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager (this);
        mLinearLayoutManager.setStackFromEnd (true);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mUserRef = mFirebaseDatabaseReference.child ("users").child (mFirebaseUser.getUid ());

        // custom how model class is parsed.
        SnapshotParser<Van> parser = new SnapshotParser<Van>() {
            @Override
            public Van parseSnapshot(DataSnapshot dataSnapshot) {
                Van van = dataSnapshot.getValue(Van.class);
                if (van != null) {
                    van.setVanId(dataSnapshot.getKey());
                }
                return van;
            }
        };

        // keyQuery - the Firebase location containing the list of keys to be found in dataRef
        // dataRef - the Firebase location to watch for data changes. Each key found at keyRef's location represents a list item.
        DatabaseReference dataRef = mFirebaseDatabaseReference.child ("vans");
        Query keyQuery = mFirebaseDatabaseReference.child ("users").child (mFirebaseUser.getUid ()).child ("vans");
        FirebaseRecyclerOptions<Van> options = new FirebaseRecyclerOptions.Builder<Van>()
                .setIndexedQuery(keyQuery, dataRef, parser)
                .build();

        // create adapter
        mFirebaseAdapter = new FirebaseRecyclerAdapter<Van, VanViewHolder> (options) {
            @Override
            protected void onBindViewHolder(@NonNull VanViewHolder holder, int position, @NonNull Van model) {
                //Log.d ("MYDEBUG", "Model: " + String.valueOf (model.getInUse ().getUser ()));
                //&& (!model.getInUse ().getUser ().equals (mFirebaseUser.getUid ()))
                if (model.getInUse () != null &&  !model.getInUse ().getUser ().equals (mFirebaseUser.getUid ()) ) {
                    holder.itemView.setBackgroundColor (getResources ().getColor (R.color.colorVanDisabled));
                    holder.itemView.setClickable (false);

                    holder.vanLabel1_tv.setText ("Using Dept");
                    holder.vanLabel2_tv.setText ("Started");
                    holder.vanLabel3_tv.setText ("Will Return in");
                    holder.vanVal1_tv.setText (model.getInUse ().getDepartment ());
                    holder.vanVal2_tv.setText (String.valueOf (model.getInUse ().getStartTimeLong ()));
                    holder.vanVal3_tv.setText (String.valueOf (model.getInUse ().getWillReturn ()));

                } else {
                    holder.vanLabel1_tv.setVisibility (View.INVISIBLE);
                    holder.vanLabel2_tv.setVisibility (View.INVISIBLE);
                    holder.vanLabel3_tv.setVisibility (View.INVISIBLE);
                    holder.vanVal1_tv.setVisibility (View.INVISIBLE);
                    holder.vanVal2_tv.setVisibility (View.INVISIBLE);
                    holder.vanVal3_tv.setVisibility (View.INVISIBLE);
                }
                holder.vanName_tv.setText (model.getName ());
                holder.vanModel_tv.setText (model.getModel ());
                holder.vanYear_tv.setText (model.getYear ());
                holder.vanPlate_tv.setText (model.getVanId ());



            }

            @NonNull
            @Override
            public VanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from (parent.getContext ()).inflate (R.layout.content_main_item, parent, false);
                //return new VanViewHolder(view);

                VanViewHolder vanViewHolder = new VanViewHolder (view);
                vanViewHolder.setOnClickListener (new VanViewHolder.ClickListener () {
                    @Override
                    public void onItemClick(View view, int position) {
                        String clickedVanId = mFirebaseAdapter.getRef (position).getKey ();
                        Intent intent = new Intent (getApplicationContext (), UseVanActivity.class);
                        intent.putExtra (EXTRA_VAN_ID, clickedVanId);
                        startActivity (intent);
                        //Toast.makeText(getApplicationContext (), "Clicked at " + clickedVanId, Toast.LENGTH_SHORT).show();
                    }
                });
                return vanViewHolder;
            }
        };


        // attach the adapter to the RecyclerView
        mVanRecyclerView.setLayoutManager (mLinearLayoutManager);
        mVanRecyclerView.setAdapter (mFirebaseAdapter);

        // Swipe to dismiss
        SwipeableRecyclerViewTouchListener swipeTouchListener =
                new SwipeableRecyclerViewTouchListener(mVanRecyclerView,
                        new SwipeableRecyclerViewTouchListener.SwipeListener() {
                            @Override
                            public boolean canSwipeLeft(int position) {
                                return true;
                            }

                            @Override
                            public boolean canSwipeRight(int position) {
                                return true;
                            }

                            @Override
                            public void onDismissedBySwipeLeft(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    mFirebaseAdapter.getRef (position).child ("users").child (mFirebaseUser.getUid ()).removeValue ();
                                    mUserRef.child ("vans").child (mFirebaseAdapter.getRef (position).getKey ()).removeValue ();
                                    mFirebaseAdapter.notifyItemRemoved(position);
                                }
                                mFirebaseAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    //Log.d ("MYDEBUG", mFirebaseAdapter.getRef (position).getKey ());
                                    mFirebaseAdapter.notifyItemRemoved(position);
                                }
                                mFirebaseAdapter.notifyDataSetChanged();
                            }
                        });

        mVanRecyclerView.addOnItemTouchListener(swipeTouchListener);


    }


    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAdapter.startListening();
    }
    @Override
    protected void onStop() {
        super.onStop();
        mFirebaseAdapter.stopListening();
    }

    /*
    @Override
    public void onPause() {
        mFirebaseAdapter.stopListening();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAdapter.startListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater ().inflate (R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId ();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected (item); */

        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mUsername = ANONYMOUS;
                startActivity(new Intent(this, SignInActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not be available.
        Log.d(TAG, getString (R.string.connection_fail_log) + String.valueOf (connectionResult));
        Toast.makeText(this, getString (R.string.connection_fail_toast), Toast.LENGTH_SHORT).show();
    }
}

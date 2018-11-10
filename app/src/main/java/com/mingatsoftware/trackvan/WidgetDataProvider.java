package com.mingatsoftware.trackvan;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class WidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mUid;
    private DatabaseReference mFirebaseDatabaseReference;
    private int mCount;

    Context context;
    Intent intent;

    List<String> vanIds = new ArrayList<> ();

    public WidgetDataProvider(Context context, Intent intent) {
        this.context = context;
        this.intent = intent;
    }

    private void dataInitialize() {
        vanIds.clear ();

        // Instantiate database
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null) {
            mUid = mFirebaseUser.getUid ();
        }

        Query query = mFirebaseDatabaseReference.child("users").child (mUid).child ("vans");
        query.addValueEventListener (new ValueEventListener () {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists ()){
                    //mCount = (int) dataSnapshot.getChildrenCount ();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        vanIds.add (snapshot.getKey ());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onCreate() {
        dataInitialize ();
    }

    @Override
    public void onDataSetChanged() {
        dataInitialize ();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return vanIds.size ();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        RemoteViews views = new RemoteViews (context.getPackageName (), R.layout.track_van_app_widget_list_item);
        views.setTextViewText (R.id.appwidget_van_id_tv, vanIds.get (i));

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}

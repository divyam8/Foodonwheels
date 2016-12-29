package edu.vit.pubnubmaptracker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.views.MapView;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;

import org.json.JSONException;
import org.json.JSONObject;

public class MBoxFollowLocationActivity extends ActionBarActivity {

    // =========================================================================
    // Properties
    // =========================================================================

    private static final String TAG = "Tracker - GMaps Follow";
    private static final String PUBNUB_TAG = "PUBNUB";
    private boolean isFirstMessage = true;
    private boolean mRequestingLocationUpdates = false;
    private MenuItem mFollowButton;

    // MapBox
    private MapView mMapView;
    private PathOverlay mLine;
    private LatLng mLatLng;
    private Marker mMarker;

    // PubNub
    private Pubnub mPubnub;
    private String channelName;

    // =========================================================================
    // Activity Life Cycle
    // =========================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mbox_view);

        // Get Channel Name
        Intent intent = getIntent();
        channelName = intent.getExtras().getString("channel");
        Log.d(TAG, "Passed Channel Name: " + channelName);

        // Get MapView
        mMapView = (MapView) findViewById(R.id.mapview);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.follow, menu);
        mFollowButton = menu.findItem(R.id.follow_locations);
        return true;
    }

    // =========================================================================
    // Button CallBacks
    // =========================================================================

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.follow_locations:
                Log.d(TAG, "'Follow Friend's Location' Button Pressed");
                mRequestingLocationUpdates = !mRequestingLocationUpdates;
                if (mRequestingLocationUpdates) {
                    startFollowingLocation();
                    mFollowButton.setTitle("Stop Viewing Your Friend's Location");
                }
                if (!mRequestingLocationUpdates) {
                    stopFollowingLocation();
                    mFollowButton.setTitle("Start Viewing Your Friend's Location");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startFollowingLocation() {
        initializePolyline();
        startPubNub();
    }

    private void stopFollowingLocation() {
        mPubnub.unsubscribe(channelName);
        isFirstMessage = true;
    }

    // =========================================================================
    // Map Editing Methods
    // =========================================================================

    private void initializePolyline() {
        mMapView.removeOverlay(mLine);
        mMapView.clear();
        mLine = new PathOverlay(Color.BLUE, 5);
    }

    private void updatePolyline() {
        mMapView.removeOverlay(mLine);
        mLine.addPoint(mLatLng);
        mMapView.getOverlays().add(mLine);
    }

    private void updateCamera() {
        mMapView.setCameraDistance(10);
        mMapView.setCenter(mLatLng);
    }

    private void updateMarker() {
        if (!isFirstMessage) {
            mMapView.removeMarker(mMarker);
        }
        isFirstMessage = false;
        mMarker = new Marker(mMapView, "", "", mLatLng);
        mMapView.addMarker(mMarker);
    }

    // =========================================================================
    // PubNub Start & Callbacks
    // =========================================================================

    private void startPubNub() {
        mPubnub = new Pubnub("demo", "demo");

        // Subscribe to channel
        try {
            mPubnub.subscribe(channelName, subscribeCallback);
        } catch (PubnubException e) {
            Log.e(TAG, e.toString());
        }
    }

    Callback subscribeCallback = new Callback() {

        @Override
        public void successCallback(String channel, Object message) {
            Log.d(PUBNUB_TAG, "Message Received: " + message.toString());
            JSONObject jsonMessage = (JSONObject) message;
            try {
                double mLat = jsonMessage.getDouble("lat");
                double mLng = jsonMessage.getDouble("lng");
                mLatLng = new LatLng(mLat, mLng);
            } catch (JSONException e) {
                Log.e(TAG, e.toString());
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updatePolyline();
                    updateCamera();
                    updateMarker();
                }
            });
        }
    };
}
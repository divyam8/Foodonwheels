package edu.vit.pubnubmaptracker;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.views.MapView;
import com.pubnub.api.Pubnub;

public class MBoxShareLocationActivity extends ActionBarActivity implements
        ConnectionCallbacks, LocationListener {

    // =========================================================================
    // Properties
    // =========================================================================

    private static final String TAG = "Tracker - MBox Share";
    private boolean mRequestingLocationUpdates = false;
    private boolean isFirstMessage = true;
    private MenuItem mShareButton;

    // Google API - Locations
    private GoogleApiClient mGoogleApiClient;

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

        // Get MapView
		mMapView = (MapView) findViewById(R.id.mapview);

        // Get Channel Name
        Intent intent = getIntent();
        channelName = intent.getExtras().getString("channel");
        Log.d(TAG, "Passed Channel Name: " + channelName);

        // Start Google Client
        this.buildGoogleApiClient();

        // Start PubNub
        mPubnub = PubNubManager.startPubnub();
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this).addApi(LocationServices.API)
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share, menu);
        mShareButton = menu.findItem(R.id.share_locations);
        return true;
    }

    // =========================================================================
    // Google Location API CallBacks
    // =========================================================================

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "Connected to Google API for Location Management");
        if (mRequestingLocationUpdates) {
            LocationRequest mLocationRequest = createLocationRequest();
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
            initializePolyline();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "Connection to Google API suspended");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location Detected");
        mLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        // Broadcast information on PubNub Channel
        PubNubManager.broadcastLocation(mPubnub, channelName, location.getLatitude(),
                location.getLongitude(), location.getAltitude());

        // Update Map
        updateCamera();
        updatePolyline();
        updateMarker();
    }

    private LocationRequest createLocationRequest() {
        Log.d(TAG, "Building request");
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    // =========================================================================
    // Button CallBacks
    // =========================================================================

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.share_locations:
                Log.d(TAG, "'Share Your Location' Button Pressed");
                mRequestingLocationUpdates = !mRequestingLocationUpdates;
                if (mRequestingLocationUpdates) {
                    startSharingLocation();
                    mShareButton.setTitle("Stop Sharing Your Location");
                }
                if (!mRequestingLocationUpdates) {
                    stopSharingLocation();
                    mShareButton.setTitle("Start Sharing Your Location");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void startSharingLocation() {
        Log.d(TAG, "Starting Location Updates");
        mGoogleApiClient.connect();
    }

    public void stopSharingLocation() {
        Log.d(TAG, "Stop Location Updates & Disconect to Google API");
        isFirstMessage = true;
        stopLocationUpdates();
        mGoogleApiClient.disconnect();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
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
}
package edu.vit.pubnubmaptracker;

import android.util.Log;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.json.JSONException;
import org.json.JSONObject;

public class PubNubManager {

    public final static String TAG = "PUBNUB";

    public static Pubnub startPubnub() {
        Log.d(TAG, "Initializing PubNub");
        return new Pubnub("pub-c-8dbf29ef-0f77-4840-8de9-e6ab9232ecab", "sub-c-fff5d0b8-ae71-11e6-8319-02ee2ddab7fe");
    }

    public static void subscribe(Pubnub mPubnub, String channelName, Callback subscribeCallback) {
        // Subscribe to channel
        try {
            mPubnub.subscribe(channelName, subscribeCallback);
            Log.d(TAG, "Subscribed to Channel");
        } catch (PubnubException e) {
            Log.e(TAG, e.toString());
        }
    }

    public static void broadcastLocation(Pubnub pubnub, String channelName, double latitude,
                                         double longitude, double altitude) {
        JSONObject message = new JSONObject();
        try {
            message.put("lat", latitude);
            message.put("lng", longitude);
            message.put("alt", altitude);
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
        Log.d(TAG, "Sending JSON Message: " + message.toString());
        pubnub.publish(channelName, message, publishCallback);
    }

    public static Callback publishCallback = new Callback() {

        @Override
        public void successCallback(String channel, Object response) {
            Log.d("PUBNUB", "Sent Message: " + response.toString());
        }

        @Override
        public void errorCallback(String channel, PubnubError error) {
            Log.d("PUBNUB", error.toString());
        }
    };
}

package edu.vit.pubnubmaptracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

    private boolean useMapBox;
    private EditText channelEditText;
    private Switch mSwitch;
    private String channelName;
    private static final String TAG = "Tracker - MainActivity";

    // ==============================================================================
    // Activity Life Cycle
    // ==============================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSwitch = (Switch) findViewById(R.id.switchMaps);

        // Attach a listener to check for changes in state
        mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    useMapBox = true;
                } else {
                    useMapBox = false;
                }

            }
        });

        channelEditText = (EditText) findViewById(R.id.channelEditText);
        channelEditText.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_ENTER:
                            channelName = channelEditText.getText().toString();
                            String message = "Chosen channel: " + channelName;
                            Toast.makeText(MainActivity.this, message,
                                    Toast.LENGTH_SHORT).show();
                            Log.d(TAG, message);
                            return true;
                        default:
                            break;
                    }
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // ==============================================================================
    // Button Actions
    // ==============================================================================

    public void shareLocation(View view) {
        if (useMapBox) {
            Log.d(TAG, "Share Location With MapBox Chosen on channel: "
                    + channelName);
            callActivity(MBoxShareLocationActivity.class);
        } else {
            Log.d(TAG, "Share Location With Google Maps Chosen on channel: "
                    + channelName);
            callActivity(GMapsShareLocationActivity.class);
        }
        return;
    }

    public void followLocation(View view) {
        if (useMapBox) {
            Log.d(TAG, "Follow Location With MapBox Chosen on channel: "
                    + channelName);
            callActivity(MBoxFollowLocationActivity.class);
        } else {
            Log.d(TAG, "Follow Location With Google Maps Chosen on channel: "
                    + channelName);
            callActivity(GMapsFollowLocationActivity.class);
        }
        return;
    }

    private void callActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        intent.putExtra("channel", channelName);
        startActivity(intent);
    }

}




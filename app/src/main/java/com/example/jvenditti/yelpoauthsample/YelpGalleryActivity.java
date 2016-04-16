package com.example.jvenditti.yelpoauthsample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class YelpGalleryActivity extends SingleFragmentActivity {

    private static String LOG_TAG = YelpGalleryActivity.class.getSimpleName();

    private static final String IS_NETWORK_REACHABLE = "is_network_reachable";
    private static final String IS_NETWORK_LISTENER_INIT = "is_network_listener_init";

    private Context mContext;
    private Boolean mIsNetworkReachable;
    private Boolean mIsNetworkListenerInit;
    private Toast mToast;

    @Override
    protected Fragment createFragment() {
        Log.d(LOG_TAG, "createFragment");
        return YelpGalleryFragment.newInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mContext = this;
        if (savedInstanceState != null) {
            mIsNetworkReachable = savedInstanceState.getBoolean(IS_NETWORK_REACHABLE);
            mIsNetworkListenerInit = savedInstanceState.getBoolean(IS_NETWORK_LISTENER_INIT);
        } else {
            mIsNetworkReachable = false;
            mIsNetworkListenerInit = false;
        }
        registerReceivers();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(LOG_TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_NETWORK_REACHABLE, mIsNetworkReachable);
        outState.putBoolean(IS_NETWORK_LISTENER_INIT, mIsNetworkListenerInit);
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        unregisterReceivers();
        super.onDestroy();
    }

    private void showNetworkConnectionMessage(Boolean isConnected) {
        Log.d(LOG_TAG, "showNetworkConnectionMessage");
        if (mToast != null) {
            mToast.cancel();
        }
        if (isConnected) {
            mToast = Toast.makeText(mContext, R.string.message_network_connectivity_true, Toast.LENGTH_LONG);
        } else {
            mToast = Toast.makeText(mContext, R.string.message_network_connectivity_false, Toast.LENGTH_LONG);
        }
        mToast.show();
    }

    private void showErrorMessage() {
        Log.d(LOG_TAG, "showErrorMessage");
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(mContext, R.string.error_could_not_load_yelp_data, Toast.LENGTH_LONG);
        mToast.show();
    }

    private void checkForInitialContentLoad() {
        Log.d(LOG_TAG, "checkForInitialContentLoad");
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (((YelpGalleryFragment) fragment).getYelpItemAdapterCount() == 0) {
            ((YelpGalleryFragment) fragment).requestYelpItems();
        }
    }

    private void registerReceivers() {

        Log.d(LOG_TAG, "registerReceivers");

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(connectivityChangeReceiver, intentFilter);

        intentFilter = new IntentFilter();
        intentFilter.addAction(YelpFetcher.ACTION_COULD_NOT_LOAD_YELP_DATA);
        LocalBroadcastManager.getInstance(this).registerReceiver(couldNotLoadYelpDataReceiver, intentFilter);
    }

    private void unregisterReceivers() {
        Log.d(LOG_TAG, "unregisterReceivers");
        try {
            unregisterReceiver(connectivityChangeReceiver);
        } catch (IllegalArgumentException e) {
            // Ignore exception
        }
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(couldNotLoadYelpDataReceiver);
        } catch (IllegalArgumentException e) {
            // Ignore exception
        }
    }

    private BroadcastReceiver connectivityChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "onReceive: connectivityChangeReceiver");
            if (!mIsNetworkListenerInit) {
                Log.d(LOG_TAG, "********************** network listener init");
                mIsNetworkListenerInit = true;
                mIsNetworkReachable = Util.isNetworkReachable(mContext);
            } else if (mIsNetworkReachable != Util.isNetworkReachable(mContext)) {
                mIsNetworkReachable = Util.isNetworkReachable(mContext);
                if (mIsNetworkReachable) {
                    Log.d(LOG_TAG, "********************** network connected");
                    showNetworkConnectionMessage(true);
                    checkForInitialContentLoad();
                } else {
                    Log.d(LOG_TAG, "********************** network not connected");
                    showNetworkConnectionMessage(false);
                }
            }
        }
    };

    private BroadcastReceiver couldNotLoadYelpDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int errorCode = intent.getIntExtra(YelpFetcher.ERROR_CODE_KEY, YelpFetcher.ERROR_CODE_UNKNOWN_HOST);
            Log.d(LOG_TAG, "onReceive: couldNotLoadYelpDataReceiver: " + errorCode);
            showErrorMessage();
        }
    };
}
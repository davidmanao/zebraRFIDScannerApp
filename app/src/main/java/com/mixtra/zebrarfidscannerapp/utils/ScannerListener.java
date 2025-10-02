package com.mixtra.zebrarfidscannerapp.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public interface ScannerListener {

    static final String TAG = "PalletManagement";

    static Bundle createPluginBundle(String pluginName, Bundle params) {
        Bundle bundle = new Bundle();
        bundle.putString("PLUGIN_NAME", pluginName);
        bundle.putBundle("PARAM_LIST", params);
        return bundle;
    }
    
    static void configureDataWedge(Context context) {
        Log.d(TAG, "Configuring DataWedge for barcode scanning...");

        // First, create the profile
        Bundle profileConfig = new Bundle();
        profileConfig.putString("PROFILE_NAME", "ZebraRFIDScanner");
        profileConfig.putString("PROFILE_ENABLED", "true");
        profileConfig.putString("CONFIG_MODE", "CREATE_IF_NOT_EXIST");

        Intent intent = new Intent();
        intent.setAction("com.symbol.datawedge.api.ACTION");
        intent.putExtra("com.symbol.datawedge.api.SET_CONFIG", profileConfig);
        context.sendBroadcast(intent);

        // Configure the profile with barcode input
        Bundle barcodeConfig = new Bundle();
        barcodeConfig.putString("PROFILE_NAME", "ZebraRFIDScanner");
        barcodeConfig.putString("PROFILE_ENABLED", "true");

        Bundle barcodeParams = new Bundle();
        barcodeParams.putString("scanner_selection", "auto");
        barcodeParams.putString("scanner_input_enabled", "true");

        barcodeConfig.putBundle("PLUGIN_CONFIG", createPluginBundle("BARCODE", barcodeParams));

        intent = new Intent();
        intent.setAction("com.symbol.datawedge.api.ACTION");
        intent.putExtra("com.symbol.datawedge.api.SET_CONFIG", barcodeConfig);
        context.sendBroadcast(intent);

        // Configure output to send intent
        Bundle outputConfig = new Bundle();
        outputConfig.putString("PROFILE_NAME", "ZebraRFIDScanner");
        outputConfig.putString("PROFILE_ENABLED", "true");

        Bundle intentParams = new Bundle();
        intentParams.putString("intent_output_enabled", "true");
        intentParams.putString("intent_action", "com.zebra.SCAN_RESULT");
        intentParams.putString("intent_category", "android.intent.category.DEFAULT");
        intentParams.putString("intent_delivery", "2"); // Broadcast intent

        outputConfig.putBundle("PLUGIN_CONFIG", createPluginBundle("INTENT", intentParams));

        intent = new Intent();
        intent.setAction("com.symbol.datawedge.api.ACTION");
        intent.putExtra("com.symbol.datawedge.api.SET_CONFIG", outputConfig);
        context.sendBroadcast(intent);

        // Also configure keystroke output as backup
        Bundle keystrokeConfig = new Bundle();
        keystrokeConfig.putString("PROFILE_NAME", "ZebraRFIDScanner");
        keystrokeConfig.putString("PROFILE_ENABLED", "true");

        Bundle keystrokeParams = new Bundle();
        keystrokeParams.putString("keystroke_output_enabled", "true");
        keystrokeParams.putString("keystroke_action_char", "9"); // Tab character
        keystrokeParams.putString("keystroke_delay_control_chars", "50");

        keystrokeConfig.putBundle("PLUGIN_CONFIG", createPluginBundle("KEYSTROKE", keystrokeParams));

        intent = new Intent();
        intent.setAction("com.symbol.datawedge.api.ACTION");
        intent.putExtra("com.symbol.datawedge.api.SET_CONFIG", keystrokeConfig);
        context.sendBroadcast(intent);

        // Associate this app with the profile
        Bundle appConfig = new Bundle();
        appConfig.putString("PROFILE_NAME", "ZebraRFIDScanner");
        appConfig.putString("PROFILE_ENABLED", "true");

        Bundle appList = new Bundle();
        appList.putString("PACKAGE_NAME", context.getPackageName());
        appList.putStringArray("ACTIVITY_LIST", new String[]{"*"});

        appConfig.putParcelableArray("APP_LIST", new Bundle[]{appList});

        intent = new Intent();
        intent.setAction("com.symbol.datawedge.api.ACTION");
        intent.putExtra("com.symbol.datawedge.api.SET_CONFIG", appConfig);
        context.sendBroadcast(intent);

        Log.d(TAG, "DataWedge configuration completed");
    }


}

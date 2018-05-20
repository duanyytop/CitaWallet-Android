package org.nervos.neuron.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharePrefUtil {

    private static final String FILE_NAME = "shared_name";
    private static final String WALLET_NAME = "wallet_name";
    private static SharedPreferences sharedPreferences;

    public static void init(Context context) {
        sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    public static void putString(String key, String value){
        sharedPreferences.edit().putString(key, value).apply();
    }

    public static String getString(String key) {
        return sharedPreferences.getString(key, "");
    }

    public static void putWalletName(String name){
        putString(WALLET_NAME, name);
    }

    public static String getWalletName() {
        return getString(WALLET_NAME);
    }


}
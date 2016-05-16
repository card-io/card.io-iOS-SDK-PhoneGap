//  Copyright (c) 2016 PayPal. All rights reserved.

package io.card.cordova.sdk;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.net.Uri;
import android.os.Bundle;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

public class CardIOCordovaPlugin extends CordovaPlugin {

    private JSONArray executeArgs = null;
    private CallbackContext callbackContext;
    private Activity activity = null;
    private static final int REQUEST_CARD_SCAN = 10;
    private static final int PERMISSION_DENIED_ERROR = 20;

    private static final String[] permissions = { Manifest.permission.VIBRATE, Manifest.permission.CAMERA };

    @Override
    public boolean execute(String action, JSONArray args,
                           CallbackContext callbackContext) throws JSONException {
        this.executeArgs = args;
        this.callbackContext = callbackContext;
        this.activity = this.cordova.getActivity();
        boolean retValue = true;
        if (action.equals("scan")) {
            this.scan(args);
        } else if (action.equals("canScan")) {
            requestMissingPermissions(REQUEST_CARD_SCAN, this.permissions);
        } else if (action.equals("version")) {
            this.callbackContext.success(CardIOActivity.sdkVersion());
        } else {
            retValue = false;
        }

        return retValue;
    }

    /**
     * Executes Cordova request after ensuring required permissions have been granted.
     * 
     * @param requestCode Identifier of the request to execute.
     */
    private void executeWithPermissions(int requestCode) {
        final CallbackContext callbackContext = this.callbackContext;
        final JSONArray executeArgs = this.executeArgs;
        
        switch(requestCode) {
            case REQUEST_CARD_SCAN:
                this.canScan(executeArgs);
                break;
        }
    }

    /**
     * Ensures that a given set of permissions is granted before executing the request.
     * 
     * @param requestCode Identifier of the request to execute.
     * @param permissions Permission names to request.
     */
    private void requestMissingPermissions(int requestCode, String[] permissions) {
        ArrayList<String> missingPermissions = new ArrayList(permissions.length);
        //Builds the list of missing permissions
        for(String permission:permissions) {
            if (!PermissionHelper.hasPermission(this, permission)) {
                missingPermissions.add(permission);
            }
        }
        
        // Requests permissions if needed, otherwise execute request
        if (missingPermissions.isEmpty()) {
            executeWithPermissions(requestCode);
        } else {
            PermissionHelper.requestPermissions(this, requestCode, missingPermissions.toArray(new String[0]));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void prepareToRender(JSONArray args) throws JSONException {
        this.callbackContext.success();
    }

    private void scan(JSONArray args) throws JSONException {
        Intent scanIntent = new Intent(this.activity, CardIOActivity.class);
        JSONObject configurations = args.getJSONObject(0);
        // customize these values to suit your needs.
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, this.getConfiguration(configurations, "requireExpiry", false)); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, this.getConfiguration(configurations, "requireCVV", false)); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, this.getConfiguration(configurations, "requirePostalCode", false)); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, this.getConfiguration(configurations, "supressManual", false)); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_RESTRICT_POSTAL_CODE_TO_NUMERIC_ONLY, this.getConfiguration(configurations, "restrictPostalCodeToNumericOnly", false)); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_KEEP_APPLICATION_THEME, this.getConfiguration(configurations, "keepApplicationTheme", false)); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CARDHOLDER_NAME, this.getConfiguration(configurations, "requireCardholderName", false)); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_USE_CARDIO_LOGO, this.getConfiguration(configurations, "useCardIOLogo", false)); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_SCAN_INSTRUCTIONS, this.getConfiguration(configurations, "scanInstructions", false)); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_NO_CAMERA, this.getConfiguration(configurations, "noCamera", false)); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_SCAN_EXPIRY, this.getConfiguration(configurations, "scanExpiry", false)); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_LANGUAGE_OR_LOCALE, this.getConfiguration(configurations, "languageOrLocale", false)); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_GUIDE_COLOR, this.getConfiguration(configurations, "guideColor", false)); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_CONFIRMATION, this.getConfiguration(configurations, "suppressConfirmation", false)); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_HIDE_CARDIO_LOGO, this.getConfiguration(configurations, "hideCardIOLogo", false)); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_SCAN, this.getConfiguration(configurations, "suppressScan", false)); // default: false
        this.cordova.startActivityForResult(this, scanIntent, REQUEST_CARD_SCAN);
    }

    private void canScan(JSONArray args) {
        if (CardIOActivity.canReadCardWithCamera()) {
            // This is where we return if scanning is enabled.
            this.callbackContext.success("Card Scanning is enabled");
        } else {
            this.callbackContext.error("Card Scanning is not enabled");
        }
    }

    // onActivityResult
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (REQUEST_CARD_SCAN == requestCode) {
            if (resultCode == CardIOActivity.RESULT_CARD_INFO) {
                CreditCard scanResult = null;
                if (intent.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                    scanResult = intent
                            .getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);
                    this.callbackContext.success(this.toJSONObject(scanResult));
                } else {
                    this.callbackContext
                            .error("card was scanned but no result");
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                this.callbackContext.error("card scan cancelled");
            } else {
                this.callbackContext.error(resultCode);
            }
        }
    }

	// onRequestPermissionResult
    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
        for(int r:grantResults) {
            if(r == PackageManager.PERMISSION_DENIED) {
                this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR));
                return;
            }
        }
        
        //Execute request with permissions granted
        executeWithPermissions(requestCode);
    }

    // onRestoreStateForActivityResult
    public void onRestoreStateForActivityResult(Bundle state, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
    }

    private JSONObject toJSONObject(CreditCard card) {
        JSONObject scanCard = new JSONObject();
        try {
            scanCard.put("cardType", card.getCardType());
            scanCard.put("redactedCardNumber", card.getRedactedCardNumber());
            scanCard.put("cardNumber", card.cardNumber);
            scanCard.put("expiryMonth", card.expiryMonth);
            scanCard.put("expiryYear", card.expiryYear);
            scanCard.put("cvv", card.cvv);
            scanCard.put("postalCode", card.postalCode);
        } catch (JSONException e) {
            scanCard = null;
        }

        return scanCard;
    }

    private <T> T getConfiguration(JSONObject configurations, String name, T defaultValue) {
        if (configurations.has(name)) {
            try {
                return (T)configurations.get(name);
            } catch (JSONException ex) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }
}

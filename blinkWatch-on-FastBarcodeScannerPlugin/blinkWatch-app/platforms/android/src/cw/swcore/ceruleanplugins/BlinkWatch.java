/*
   Copyright 2017 Maxim Zhukov

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
/*
  This is a shameless cannibalisation of the most excellent cordova-plugin-qrscanner package by BitPay Inc,
 originally distributed under MIT license.
 See README.md for details.
*/
package cw.swcore.ceruleanplugins;

import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaInterfaceImpl;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebViewImpl;
import org.apache.cordova.PluginResult;
import org.apache.cordova.LOG;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.provider.Settings;
import android.view.TextureView;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Toast;
import android.view.WindowManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.Override;

import dk.schaumburgit.fastbarcodescanner.FastBarcodeScanner;
import dk.schaumburgit.stillsequencecamera.camera2.StillSequenceCamera2;

public class BlinkWatch extends CordovaPlugin implements FastBarcodeScanner.BarcodeDetectedListener, FastBarcodeScanner.MultipleBarcodesDetectedListener
{
 public static final String TAG = "CW.blinkWatch";

 private static final String ACTION_SHOW_TOAST = "showToast";
 private static final String ACTION_START_SCANNING = "startScanning";
 private static final String ACTION_STOP_SCANNING = "stopScanning";

 private TextureView myTextureView;
 private Context myContext;
 private ViewParent parentView;
 private Activity myActivity;
 private CordovaActivity myCordovaActivity;
 private CordovaInterfaceImpl myCordova;
 private CordovaWebViewImpl myCordovaWebView;
 private SurfaceTexture mySurfaceTexture;

 public BlinkWatch() {
 }

/*************************************************************************************************************************/
/* Sets the context of the Command. This can then be used to do things like get file paths associated with the Activity. */
/*                                                                                                                       */
/* @param cordova The context of the main Activity.                                                                      */
/* @param webView The CordovaWebView Cordova is running in.                                                              */
/*************************************************************************************************************************/
 public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        LOG.d(TAG, "[[initialize]] initialize called...");
    }

/********************************************************************/
/* Overridden execute method                                        */
/*                                                                  */
/* @param action the string representation of the action to execute */
/* @param args                                                      */
/* @param callbackContext the cordova {@link CallbackContext}       */
/* @return true if the action exists, false otherwise               */
/* @throws JSONException if the args parsing fails                  */
/********************************************************************/
 @Override public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
  Log.d(TAG, "Action: " + action);
  JSONObject arg_object = args.optJSONObject(0);
  if (ACTION_SHOW_TOAST.equals(action)) {
   String data = arg_object.getString("data");
   showToast(data);
   return true;
  }
  else if (ACTION_START_SCANNING.equals(action)) {
   int resolution = 1024*768;
   if (arg_object != null) {resolution = arg_object.optInt("resolution", 1024*768);}
   this.myActivity=cordova.getActivity();
   this.myContext = webView.getContext();
   this.myTextureView = new TextureView(myContext);
   this.myTextureView.setContentDescription("A supposedly future View to yield Surface for the Preview camera stream...");
   this.myActivity.setContentView(this.myTextureView);
   this.myTextureView.getSurfaceTextureListener().onSurfaceTextureAvailable(this.mySurfaceTexture,0,0);
   startScanning(resolution, callbackContext);
   return true;
  }
  else if (ACTION_STOP_SCANNING.equals(action)) {
   stopScanning(callbackContext);
   return true;
  }
  return false;
 }

 private void showToast(final String text) {final int duration = Toast.LENGTH_SHORT;cordova.getActivity().runOnUiThread(new Runnable() {public void run() {Toast toast=Toast.makeText(cordova.getActivity().getApplicationContext(),text,duration);toast.show();}});}

 private FastBarcodeScanner mScanner = null;

// callback that will be used to send back data to the cordova app
 private CallbackContext mScanCallback;
 private HandlerThread mScanCallbackThread;
 private Handler mScanCallbackHandler;
 private int mRequestedResolution;
 private void retryStartScanning(final CallbackContext callbackContext) {
  startScanning(mRequestedResolution, callbackContext);
 }

 private void startScanning(int resolution, final CallbackContext callbackContext) {
  LOG.d(TAG, "[[startScanning]] Starting scanning...");
  mRequestedResolution = resolution;

  if (mScanner == null) {
   boolean hasCameraPermission = requestCameraPermission(CALL_START_WHEN_DONE, callbackContext);
   if (!hasCameraPermission) {LOG.d(TAG, "[[startScanning]] Postponing scanning...");return;}
   mScanner = new FastBarcodeScanner(cordova.getActivity(), resolution);
  }

  mScanCallback = callbackContext;

  cordova.getActivity().runOnUiThread(new Runnable() {public void run() {cordova.getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);}});

  cordova.getThreadPool().execute(new Runnable() {
   public void run() {
    LOG.d(TAG, "[[startScanning]] Registering scan Callback...");
    // Keep the callback for later use:
    mScanCallback = callbackContext;
    // Use a dedicated thread for handling all the incoming images
    mScanCallbackThread = new HandlerThread("FastBarcodeScanner plugin callback");
    mScanCallbackThread.start();
    mScanCallbackHandler = new Handler(mScanCallbackThread.getLooper());
    // Start listening for callbacks
    try {
     mScanner.StartScan(false, BlinkWatch.this, mScanCallbackHandler);
     // Create an OK result:
     JSONObject returnObj = new JSONObject();
     addProperty(returnObj, "startScanning", "true");
     PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, returnObj);
     pluginResult.setKeepCallback(true); // make sure the callback is kept open
     // Return:
     //callbackContext.sendPluginResult(pluginResult);
    } catch (Exception exc) {
     LOG.e(TAG, "[[startScanning]] StartScan failed...", exc);
     // Create an ERROR result:
     JSONObject returnObj = new JSONObject();
     addProperty(returnObj, "startScanning", exc.getMessage());
     PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, returnObj);
     // make sure the callback is closed:
     pluginResult.setKeepCallback(false);
     mScanCallback = null;
     // Clean-up and close callback thread and handler:
     if (mScanCallbackHandler != null) {
      mScanCallbackHandler.removeCallbacksAndMessages(null);
      mScanCallbackHandler = null;
     }
     if (mScanCallbackThread != null) {
      try {
       mScanCallbackThread.quitSafely();
       mScanCallbackThread.join();
      } catch (Exception e) {
       e.printStackTrace();
      }
      mScanCallbackThread = null;
     }
     // Return the error:
     callbackContext.sendPluginResult(pluginResult);
    }
   }
  });
 }

 private void stopScanning(final CallbackContext callbackContext) {
  LOG.v(TAG, "[[stopScanning]] Stopping scanning...");
  cordova.getActivity().runOnUiThread(new Runnable() {public void run() {cordova.getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);}});
  mScanCallback = null;
  if (mScanner == null) {return;}
  try {
   mScanner.StopScan();
  } catch (Exception e) {
   e.printStackTrace();
   PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
   callbackContext.sendPluginResult(pluginResult);
  }
  if (mScanCallback != null) {
   PluginResult result = new PluginResult(PluginResult.Status.ERROR, "stopScanning called");
   result.setKeepCallback(false);
   mScanCallback.sendPluginResult(result);
   mScanCallback = null;
  }
  LOG.i(TAG, "[[stopScanning]] Killing scan callback");
  try {
   if (mScanCallbackHandler != null) {
    mScanCallbackHandler.removeCallbacksAndMessages(null);
    mScanCallbackHandler = null;
   }
   if (mScanCallbackThread != null) {
    mScanCallbackThread.quitSafely();
    mScanCallbackThread.join();
    mScanCallbackThread = null;
   }
  } catch (Exception e) {
   e.printStackTrace();
   PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
   callbackContext.sendPluginResult(pluginResult);
  }
  callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, (String) null));
 }

 @Override public void onSingleBarcodeAvailable(FastBarcodeScanner.BarcodeInfo barcodeInfo, byte[] image, int format, int width, int height) {
  LOG.d(TAG, "[[onSingleBarcodeAvailable]] Start barcode");
  String barcode = null;
  if (barcodeInfo != null) {barcode = barcodeInfo.barcode;}
  LOG.d(TAG, "[[onSingleBarcodeAvailable]] Barcode: " + barcode);

  if (mScanCallback != null) {
   LOG.d(TAG, "[[onSingleBarcodeAvailable]] Callback");
   JSONObject returnObj = new JSONObject();
   addProperty(returnObj, "barcode", barcode);
   addProperty(returnObj, "format", format);
   PluginResult result = new PluginResult(PluginResult.Status.OK, returnObj);
   result.setKeepCallback(true);
   mScanCallback.sendPluginResult(result);
  }
 }

 @Override public void onMultipleBarcodeAvailable(FastBarcodeScanner.BarcodeInfo[] barcodes, byte[] image, int format, int width, int height) {
  String barcode = null;
  if (barcodes != null && barcodes.length > 0) {barcode = barcodes[0].barcode;}

  if( mScanCallback != null ) {
   JSONObject returnObj = new JSONObject();
   addProperty(returnObj, "barcodes", barcodes);
   addProperty(returnObj, "format", format);
   PluginResult result = new PluginResult(PluginResult.Status.OK, returnObj);
   result.setKeepCallback(true);
   mScanCallback.sendPluginResult(result);
  }
 }

 @Override public void onError(Exception error) {if( mScanCallback != null ) {PluginResult result = new PluginResult(PluginResult.Status.ERROR, error.getMessage()); result.setKeepCallback(true); mScanCallback.sendPluginResult(result);}}
 @Override public void onPause(boolean multitasking) {LOG.d(TAG, "[[onPause]] Pause"); if (mScanner == null) {return;} mScanner.Pause();}
 @Override public void onResume(boolean multitasking) {LOG.d(TAG, "[[onResume]] Resume"); if (mScanner == null) {return;} mScanner.Resume();}
 @Override public void onDestroy() {LOG.d(TAG, "[[onDestroy]] Destroy");}
 private void addProperty(JSONObject obj, String key, Object value) {try {obj.put(key, value);} catch (JSONException e) {}}

 private static final int CALL_START_WHEN_DONE = 2;

 private CallbackContext mPermissionCallbackContext;
 private boolean requestCameraPermission(int whatNext, CallbackContext callbackContext) {
  LOG.d(TAG, "[[requestCameraPermission]] Check camera permission...");
  if (mPermissionCallbackContext != null) {
   LOG.e(TAG, "[[requestCameraPermission]] RACE CONDITION: two overlapping permission requests...");
   callbackContext.error("Application error requesting permissions - see the log for details");
   PluginResult r = new PluginResult(PluginResult.Status.ERROR);
   callbackContext.sendPluginResult(r);
   return false;
  }
  if (cordova.hasPermission(Manifest.permission.CAMERA)) {
   LOG.d(TAG, "[[requestCameraPermission]] ...already have camera permission - life is good");
   return true;
  }
  LOG.d(TAG, "[[requestCameraPermission]] ...dont have camera permission - will have to ask the user");
  mPermissionCallbackContext = callbackContext;
  cordova.requestPermission(this, whatNext, Manifest.permission.CAMERA);
  return false;
 }

 @Override public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
  if (mPermissionCallbackContext == null) {LOG.e(TAG, "[[onRequestPermissionResult]] No context - got a permission result we didnt ask for...??? "); return;}
  for(int r:grantResults) {if(r == PackageManager.PERMISSION_DENIED) {LOG.d(TAG, "[[onRequestPermissionResult]] User refused us access to  the camera - there is nothing we can do");return;}}
  int whatNext = requestCode;
  CallbackContext ctx = mPermissionCallbackContext;
  mPermissionCallbackContext = null; // if there's a race-condition, let's make life hard for it...
  switch (whatNext) {
   case CALL_START_WHEN_DONE:
    retryStartScanning(ctx);
    break;
   default:
    LOG.e(TAG, "[[onRequestPermissionResult]] Unexpected requestCode - got a permission result we didnt ask for...???");
    ctx.error("Application error requesting permissions - see the log for details");
    PluginResult r = new PluginResult(PluginResult.Status.ERROR);
    ctx.sendPluginResult(r);
   break;
  }
 }
}

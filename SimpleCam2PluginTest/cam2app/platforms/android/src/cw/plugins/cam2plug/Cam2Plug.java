/****************************************************************************/
/* Copyright 2017 Maxim Zhukov                                              */
/*                                                                          */
/* Licensed under the Apache License, Version 2.0 (the "License");          */
/* you may not use this file except in compliance with the License.         */
/* You may obtain a copy of the License at                                  */
/*                                                                          */
/*     http://www.apache.org/licenses/LICENSE-2.0                           */
/*                                                                          */
/* Unless required by applicable law or agreed to in writing, software      */
/* distributed under the License is distributed on an "AS IS" BASIS,        */
/* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. */
/* See the License for the specific language governing permissions and      */
/* limitations under the License.                                           */
/****************************************************************************/
package cw.plugins.cam2plug;

import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;

import android.app.FragmentManager;
import android.app.FragmentTransaction;

import android.widget.FrameLayout;
import android.widget.Toast;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;

import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import android.util.DisplayMetrics;
import android.util.Size;
import android.util.SparseIntArray;
import android.util.TypedValue;

import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.IllegalStateException;
import java.lang.InterruptedException;
import java.lang.NullPointerException;
import java.lang.RuntimeException;
import java.lang.Thread;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Objects;

public class Cam2Plug extends CordovaPlugin {

 private String gTG = "Cam2Plug:";
/*Variables dealing with hardware permissions and plugin initialisation:*/
 private static final String  CAMERA = Manifest.permission.CAMERA;
 private static final int   REQ_CODE = 0;
 private boolean hasCameraPermission = false;
 private boolean  isFullyInitialised = false;



/*======Exec method together with a wrapper to deal with potential problems with plugins delayed initialisation:======================================================*/
 @Override public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {return checkInitStateWrapper(action, args, callbackContext);}

 private boolean checkInitStateWrapper(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
  String lTG = "[checkInitStateWrapper] ", msg="";boolean actionIsKnown = false;
  /*-------------Actions, that do not depend on full initialisation of plugin:-------------*/
  if      (action.equals("isFullyInitialised")) {if(isFullyInitialised){callbackContext.success("true");}else{callbackContext.error("false");} return true;}
  else if (action.equals("coolMethod"))         {String message = args.getString(0);coolMethod(message, callbackContext);                      return true;}
  /*-------------And actions, that do:-----------------------------------------------------*/
  if      (action.equals("startVideo")) { if (isFullyInitialised) {startVideo(callbackContext);return true;} else {actionIsKnown=true;} }
  else if (action.equals("stopVideo"))  { if (isFullyInitialised) {stopVideo(callbackContext); return true;} else {actionIsKnown=true;} }

  if (actionIsKnown) {
                      msg="Unfortunately it is impossible to carry out this action ("+action+") until the app is given access to CAMERA. Please consider going to Settings and granting CAMERA access permission to this app.";
                      LOG.d(gTG,lTG+msg);sTst(msg);callbackContext.error(msg);return true;
                     }
  else {msg="Requested action '"+action+"' is not known to this plugin!";LOG.d(gTG,lTG+msg);return false;}
 }
/*====================================================================================================================================================================*/



/*======Initialisation, split in two parts with later one being deferred until proper hardware permissions are obtained:==============================================*/
 @Override public void pluginInitialize() {
  String lTG = "[pluginInitialize] ";
  LOG.setLogLevel(LOG.VERBOSE);
  LOG.i(gTG, lTG+"Logging is on. LOGLEVEL has been set to INFO.");
  LOG.d(gTG, lTG+"Checking for CAMERA permission and requesting it if necessary... Currently it is set to "+String.valueOf(cordova.hasPermission(CAMERA))+".");
  if (!cordova.hasPermission(CAMERA)) {cordova.requestPermission(this, REQ_CODE, CAMERA);}
  else
  {
   LOG.d(gTG, lTG+"We have the CAMERA permission! Proceeding with the rest of initialisation...");
   hasCameraPermission = true;
   deferredPluginInitialisation();
  }
 }

 @Override public void onRequestPermissionResult(int requestCode, String[] permissions,int[] grantResults) throws JSONException {
  String lTG = "[onRequestPermissionResult] ";
  hasCameraPermission = cordova.hasPermission(CAMERA);
  LOG.d(gTG, lTG+"We've got a response to our request! Checking the result. It is now set to %s.",String.valueOf(hasCameraPermission));
  if (!hasCameraPermission)
  {
   LOG.i(gTG, lTG+"This is bad - we were not given CAMERA permission. There is no way we can work now.");
  }
  else
  {
   LOG.i(gTG, lTG+"We've been granted CAMERA permission! On to the deferred part of initialisation!");
   deferredPluginInitialisation();
  }
 }

 private void deferredPluginInitialisation() {
  String lTG = "[deferredPluginInitialisation] ";
//  getProperCamera();
  isFullyInitialised = true;
  LOG.d(gTG, lTG+"Plugin initialisation is now complete.");
//Also send some sort of message/event to JS so that it would be able to not only poll plugins' initialisation state, but also to setup an EventListener and react to its' firing...*/
 }
/*====================================================================================================================================================================*/



/*===============================Assorted utilities===================================================================================================================*/
 private void sTst(final String s){
  cordova.getActivity().runOnUiThread(new Runnable(){public void run(){Toast toast=Toast.makeText(cordova.getActivity().getApplicationContext(),s,Toast.LENGTH_LONG);toast.show();}});
 }

 @Override public void onPause(boolean multitasking) {/*App loses focus*/
  String lTG = "[onPause] ";
  LOG.v(gTG, lTG+"Pausing operations (app is loosing focus, but is still visible to the user). [multitasking is "+String.valueOf(multitasking)+".]....");
 }

 @Override public void onResume(boolean multitasking) {/*App regains focus*/
  String lTG = "[onResume] ";
  LOG.v(gTG, lTG+"Resuming operations (acquiring focus). [multitasking is "+String.valueOf(multitasking)+".]... ");
 }

 @Override public void onStart() { /*App becomes visible to the user*/
  String lTG = "[onStart] ";
  LOG.v(gTG, lTG+"Starting operations (becoming visible to user). Checking if it is necessary to re-run pluginInitialize() and re-runnung it if it is... Most probably it is...");
  if (!isFullyInitialised) {pluginInitialize();}/*For now we only check CAMERA permission. Later we should check general hardware availability, etc, as well.*/
 }

 @Override public void onStop() {/*App stops being visible to the user*/
  String lTG = "[onStop] ";
  LOG.v(gTG, lTG+"Stopping operations (becoming invisible to the user). Shutting down some stuff and releasing some resources (e.g. camera). Will have to re-initialise on Start...");
  /*Partial shutdown here.*/
 }

 @Override public void onReset() {/*WebView just did a top-level navigation or refreshed.*/
  String lTG = "[onReset] ";
  LOG.v(gTG, lTG+"Plugins are supposed to stop any long-running processes and clean up internal state....");
  /*Partial shutdown here.*/
 }

 @Override public void onDestroy() {/*The one final call before Activity gets destroyed by the system. Clean-up and free all resources.*/
  String lTG = "[onDestroy] ";
  LOG.v(gTG, lTG+"Activity (app) is to be destroyed by the OS. Do final clean-up, release every resource that's being held etc...");
  /*Final shutdown and clean-up here.*/
 }

 @Override public Object onMessage(String id, Object data) {/*Called when a message is sent to this plugin.*/
  String lTG = "[onMessage] ";
  LOG.v(gTG, lTG+"A message is received by the plugin. msgId:"+id+", data:["+Objects.toString(data)+"]. For now return null, so as not to stop its propagation...");
  /* args:    [id (message id), data (message data)],             */
  /* returns: Either object, to stop message propagation, or null.*/
  return null;
 }

/*====================================================================================================================================================================*/



/*=======================================Actions (i.e. methods that ar callable from JS) are defined below here=======================================================*/
 private void coolMethod(String message, CallbackContext callbackContext) {
  if (message != null && message.length() > 0) {callbackContext.success(message);                                }
  else                                         {callbackContext.error("Expected one non-empty string argument.");}
 }

 private void startVideo(CallbackContext callbackContext) {
  String lTG = "[startVideo] ";
  boolean result = false;
  String reason = "No code in startVideo yet.";
  String message = "startVideo worked!";
  LOG.d(gTG, lTG+"Method called. Firing up the video...");
//............
  if (result) {callbackContext.success(message);} else {callbackContext.error(reason);}
 }

 private void stopVideo(CallbackContext callbackContext) {
  String lTG = "[stopVideo] ";
  boolean result = false;
  String reason = "No code in stopVideo yet.";
  String message = "stopVideo worked!";
  LOG.d(gTG, lTG+"Method called. Stopping up the video and tidying up...");
//............
  if (result) {callbackContext.success(message);} else {callbackContext.error(reason);}
 }

}
/*====================================================================================================================================================================*/

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

//import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CallbackContext;
//import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
//import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.Manifest;
//import android.content.pm.PackageManager;
//import android.content.res.Configuration;

//import android.app.FragmentManager;
//import android.app.FragmentTransaction;

//import android.widget.FrameLayout;
import android.widget.Toast;

//import android.os.Handler;
//import android.os.HandlerThread;
//import android.os.Looper;
import android.os.SystemClock;

import android.hardware.camera2.CameraAccessException;
//import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
//import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
//import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;

import android.graphics.ImageFormat;
//import android.graphics.SurfaceTexture;

//import android.view.Surface;
//import android.view.SurfaceView;
//import android.view.SurfaceHolder;
//import android.view.TextureView;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.ViewParent;

//import android.util.DisplayMetrics;
import android.util.Size;
//import android.util.SparseIntArray;
//import android.util.TypedValue;

import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.IllegalStateException;
//import java.lang.InterruptedException;
import java.lang.NullPointerException;
import java.lang.RuntimeException;
import java.lang.Thread;
import java.lang.StackTraceElement;

import java.util.Arrays;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Timer;
//import java.util.TimerTask;
//import java.util.Objects;

public class Cam2Plug extends CordovaPlugin {

 private String gTG = "Cam2Plug";
/*Variables dealing with hardware permissions and plugin initialisation:*/
 private static final String  CAMERA = Manifest.permission.CAMERA;
 private static final int   REQ_CODE = 0;
 private boolean hasCameraPermission = false;
 private boolean  isFullyInitialised = false;
////// private JSONObject cameraState      = new JSONObject();
/*Context for sending events to js:*/
 private CallbackContext commsCallbackContext = null;

/*======Exec method together with a wrapper to deal with potential problems with plugins delayed initialisation:======================================================*/
 @Override public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {return checkInitStateWrapper(action, args, callbackContext);}

 private boolean checkInitStateWrapper(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
  String lTG = "[checkInitStateWrapper] ", msg=""; boolean actionIsKnown = false;
  /*-------------Actions, that do not depend on full initialisation of plugin:-------------*/
  if      (action.equals("isFullyInitialised"))   {if(isFullyInitialised){callbackContext.success("true");}else{callbackContext.error("false");} return true;}
  else if (action.equals("coolMethod"))           {String message = args.getString(0);coolMethod(message, callbackContext);                      return true;}
  else if (action.equals("j2jsLinkCreate"))       {this.commsCallbackContext = callbackContext;                                                  return true;}
  /*-------------And actions, that do:-----------------------------------------------------*/
  if      (action.equals("startVideo")) { if (isFullyInitialised) {startVideo(callbackContext);return true;} else {actionIsKnown=true;} }
  else if (action.equals("stopVideo"))  { if (isFullyInitialised) {stopVideo(callbackContext); return true;} else {actionIsKnown=true;} }

  if (actionIsKnown) {
                      msg = "Unfortunately it is impossible to carry out this action (" + action + ") until the app is given access to CAMERA. Please consider going to Settings and granting CAMERA access permission to this app.";
                      LOG.d(gTG,lTG+msg);sTst(msg);callbackContext.error(msg);return true;
                     }
  else {msg = "Requested action '" + action + "' is not known to this plugin!"; LOG.d(gTG,lTG+msg); return false;}
 }
/*====================================================================================================================================================================*/



/*======Initialisation, split in two parts with later one being deferred until proper hardware permissions are obtained:==============================================*/
 @Override public void pluginInitialize() {
  String lTG = "[pluginInitialize] ";
  LOG.setLogLevel(LOG.VERBOSE);LOG.i(gTG, lTG+"Logging is on. LOGLEVEL has been set to INFO.");
  hasCameraPermission = cordova.hasPermission(CAMERA);
  LOG.d(gTG, lTG+"Checking for CAMERA permission and requesting it if necessary... Currently it is set to "+String.valueOf(hasCameraPermission)+".");
  notifyJs_bool("camAccess",hasCameraPermission);
  if (!hasCameraPermission) {
   LOG.d(gTG, lTG+"We have no CAMERA permission! Proceeding to request it in order to be able to complete the initialisation...");
   isFullyInitialised  = false;
   notifyJs_bool("initState",isFullyInitialised);
   cordova.requestPermission(this, REQ_CODE, CAMERA);
  }
  else {
   LOG.d(gTG, lTG+"We have the CAMERA permission! Proceeding with the rest of initialisation...");
   deferredPluginInitialisation();
  }
 }

 @Override public void onRequestPermissionResult(int requestCode, String[] permissions,int[] grantResults) throws JSONException {
  String lTG = "[onRequestPermissionResult] ";
  hasCameraPermission = cordova.hasPermission(CAMERA);
  LOG.d(gTG, lTG+"We've got a response to our request! Checking the result. It is now set to %s.",String.valueOf(hasCameraPermission));
  notifyJs_bool("camAccess",hasCameraPermission);
  if (!hasCameraPermission)
  {
   LOG.i(gTG, lTG+"This is bad - we were not given CAMERA permission. There is no way we can work now.");
   isFullyInitialised = false;
   notifyJs_bool("initState",isFullyInitialised);
  }
  else
  {
   LOG.i(gTG, lTG+"We've been granted CAMERA permission! On to the deferred part of initialisation!");
   deferredPluginInitialisation();
  }
 }

 private void deferredPluginInitialisation() {
  String lTG = "[deferredPluginInitialisation] ";
  setupCam();
  isFullyInitialised = true;
  LOG.d(gTG, lTG+"Plugin initialisation is now complete.");
  notifyJs_bool("initState",isFullyInitialised);
 }
/*====================================================================================================================================================================*/



/*===============================Assorted utilities===================================================================================================================*/
 private void sendToJs(JSONObject msg) {
  PluginResult dataResult = new PluginResult(PluginResult.Status.OK, msg);
  dataResult.setKeepCallback(true);
  commsCallbackContext.sendPluginResult(dataResult);
 }

/*ToDo: A queueing system (see README.md). Implement as a separate class with a really simple public interface.*/
/*---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
/* private void notifyJs_bool(       String propName, boolean propValue,    JSONObject... flagsObjectsArray );                                                                           */
/* private void notifyJs_String(     String propName, String propValue,     JSONObject... flagsObjectsArray );                                                                           */
/* private void notifyJs_JSONObject( String propName, JSONObject propValue, JSONObject... flagsObjectsArray );                                                                           */
/*                                                                                                                                                                                       */
/* flagsObjectsArray - an optional parameter, a nullable array of JSONObjects.                                                                                                           */
/* flagsObjectsArray[0] is a caller-id flag,  it is a JSONObject and looks like this: {"provideCallerId":boolean} (if ommited, defaults to true).                                        */
/* flagsObjectsArray[1] is a timestamp (ns),  it is a JSONObject and looks like this: {"timeStamp":long}          (if ommited or zero, defaults to the current .elapsedRealtimeNanos()). */
/* flagsObjectsArray[2] is a combinable flag, it is a JSONObject and looks like this: {"isCombinable":boolean}    (if ommited, defaults to false).                                       */
/*---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
/*       notifyJs_bool("initState", isFullyInitialised     );                                                                                                                            */
/*       notifyJs_bool("camAccess", hasCameraPermission    );                                                                                                                            */
/* notifyJs_JSONObject("camState",  cameraState            );                                                                                                                            */
/* notifyJs_JSONObject("errors",    JSONObject camerasIdsEtc);                                                                                                                           */
/*---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
/*~~~notifyJs_xxx(...) functions:~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
 private void notifyJs_bool(String propName,boolean propValue, JSONObject... flagsObjectsArray) {
   String lTG = "[notifyJs_bool_"+propName+"] ", caller;
   StackTraceElement[] stackTraceElements;
   boolean doCallerId   = true;  if (flagsObjectsArray.length >= 1) {doCallerId   = flagsObjectsArray[0].optBoolean("provideCallerId",true);} else { doCallerId   = true;  }                                                                     /*if ommited, <provideCallerId> flag defaults to true*/
   long    timeStamp    = 0;     if (flagsObjectsArray.length >= 2) {timeStamp    = flagsObjectsArray[1].optLong("timeStamp",0);            } else { timeStamp    = 0;     } if (timeStamp==0) {timeStamp = SystemClock.elapsedRealtimeNanos();} /*if ommited or zero, timeStamp flag defaults to the current value of elapsedRealtimeNanos()*/
   boolean isCombinable = false; if (flagsObjectsArray.length >= 3) {isCombinable = flagsObjectsArray[2].optBoolean("isCombinable",false);  } else { isCombinable = false; }                                                                     /*if ommited, <isCombinable> flag defaults to false*/
   JSONObject tmpObj = new JSONObject();
   if (doCallerId) {
     stackTraceElements = Thread.currentThread().getStackTrace();}
     caller = "Caller class: " + stackTraceElements[3].getClassName() + " from file: " +  stackTraceElements[3].getFileName() + ", line number: " + String.valueOf(stackTraceElements[3].getLineNumber()) + " (method: " + stackTraceElements[3].getMethodName() + "), and looks like: '" + stackTraceElements[3].toString() + "'!";
     LOG.w(gTG, lTG+"Caller: "+caller);
   }
   if (commsCallbackContext == null) {LOG.d(gTG, lTG+"Need to notify js about "+propName+", but can't because commsCallbackContext is null (i.e. js hasn't yet called us to establish comms link....");}
   else if (commsCallbackContext.isFinished()) {LOG.d(gTG, lTG+"Need to notify js about "+propName+", but can't because commsCallbackContext.isFinished() returns true (i.e. it has already been used at least once and PluginResult.KeepCallback wasn't set to true at the moment, so commsCallbackContext burned after one use)....");}
   else {
     LOG.d(gTG, lTG+"Notifying js about "+propName+"....");
      try { tmpObj.put(propName,String.valueOf(propValue)); if (doCallerId){tmpObj.put("caller",JSONObject.quote(caller));} tmpObj.put("isCombinable",isCombinable); tmpObj.put("timeStamp",timeStamp); } catch (JSONException e) { throw new RuntimeException(e); }
      sendToJs(tmpObj); 
     LOG.d(gTG, lTG+"Notification sent....");
   }
 }

 private void notifyJs_String(String propName,String propValue,JSONObject... flagsObjectsArray) {
   String lTG = "[notifyJs_String_"+propName+"] ", caller;
   StackTraceElement[] stackTraceElements;
   boolean doCallerId   = true;  if (flagsObjectsArray.length >= 1) {doCallerId   = flagsObjectsArray[0].optBoolean("provideCallerId",true);} else { doCallerId   = true;  }                                                                     /*if ommited, <provideCallerId> flag defaults to true*/
   long    timeStamp    = 0;     if (flagsObjectsArray.length >= 2) {timeStamp    = flagsObjectsArray[1].optLong("timeStamp",0);            } else { timeStamp    = 0;     } if (timeStamp==0) {timeStamp = SystemClock.elapsedRealtimeNanos();} /*if ommited or zero, timeStamp flag defaults to the current value of elapsedRealtimeNanos()*/
   boolean isCombinable = false; if (flagsObjectsArray.length >= 3) {isCombinable = flagsObjectsArray[2].optBoolean("isCombinable",false);  } else { isCombinable = false; }                                                                     /*if ommited, <isCombinable> flag defaults to false*/
   JSONObject tmpObj = new JSONObject();
   if (doCallerId) {
     stackTraceElements = Thread.currentThread().getStackTrace();
     caller = "Caller class: " + stackTraceElements[3].getClassName() + " from file: " +  stackTraceElements[3].getFileName() + ", line number: " + String.valueOf(stackTraceElements[3].getLineNumber()) + " (method: " + stackTraceElements[3].getMethodName() + "), and looks like: '" + stackTraceElements[3].toString() + "'!";
     LOG.w(gTG, lTG+"Caller: "+caller);
   }
   if (commsCallbackContext == null) {LOG.d(gTG, lTG+"Need to notify js about "+propName+", but can't because commsCallbackContext is null (i.e. js hasn't yet called us to establish comms link....");}
   else if (commsCallbackContext.isFinished()) {LOG.d(gTG, lTG+"Need to notify js about "+propName+", but can't because commsCallbackContext.isFinished() returns true (i.e. it has already been used at least once and PluginResult.KeepCallback wasn't set to true at the moment, so commsCallbackContext burned after one use)....");}
   else {
     LOG.d(gTG, lTG+"Notifying js about "+propName+"....");
      try { tmpObj.put(propName, JSONObject.quote(propValue)); if (doCallerId){tmpObj.put("caller",JSONObject.quote(caller));} tmpObj.put("isCombinable",isCombinable); tmpObj.put("timeStamp",timeStamp); } catch (JSONException e) { throw new RuntimeException(e); }
      sendToJs(tmpObj); 
     LOG.d(gTG, lTG+"Notification sent....");
   }
 }

 private void notifyJs_JSONObject(String propName, JSONObject propValue, JSONObject... flagsObjectsArray) {
   String lTG = "[notifyJs_JSONObject_"+propName+"] ", caller;
   StackTraceElement[] stackTraceElements;
   boolean doCallerId   = true;  if (flagsObjectsArray.length >= 1) {doCallerId   = flagsObjectsArray[0].optBoolean("provideCallerId",true);} else { doCallerId   = true;  }                                                                     /*if ommited, <provideCallerId> flag defaults to true*/
   long    timeStamp    = 0;     if (flagsObjectsArray.length >= 2) {timeStamp    = flagsObjectsArray[1].optLong("timeStamp",0);            } else { timeStamp    = 0;     } if (timeStamp==0) {timeStamp = SystemClock.elapsedRealtimeNanos();} /*if ommited or zero, timeStamp flag defaults to the current value of elapsedRealtimeNanos()*/
   boolean isCombinable = false; if (flagsObjectsArray.length >= 3) {isCombinable = flagsObjectsArray[2].optBoolean("isCombinable",false);  } else { isCombinable = false; }                                                                     /*if ommited, <isCombinable> flag defaults to false*/
   JSONObject tmpObj = new JSONObject();
   if (doCallerId) {
     stackTraceElements = Thread.currentThread().getStackTrace();
     caller = "Caller class: " + stackTraceElements[3].getClassName() + " from file: " +  stackTraceElements[3].getFileName() + ", line number: " + String.valueOf(stackTraceElements[3].getLineNumber()) + " (method: " + stackTraceElements[3].getMethodName() + "), and looks like: '" + stackTraceElements[3].toString() + "'!";
     LOG.w(gTG, lTG+"Caller: "+caller);
   }
   if (commsCallbackContext == null) {LOG.d(gTG, lTG+"Need to notify js about "+propName+", but can't because commsCallbackContext is null (i.e. js hasn't yet called us to establish comms link....");}
   else if (commsCallbackContext.isFinished()) {LOG.d(gTG, lTG+"Need to notify js about "+propName+", but can't because commsCallbackContext.isFinished() returns true (i.e. it has already been used at least once and PluginResult.KeepCallback wasn't set to true at the moment, so commsCallbackContext burned after one use)....");}
   else {
     LOG.d(gTG, lTG+"Notifying js about "+propName+"....");
      try { tmpObj.put(propName,propValue); if (doCallerId){tmpObj.put("caller",JSONObject.quote(caller));} tmpObj.put("isCombinable",isCombinable); tmpObj.put("timeStamp",timeStamp); } catch (JSONException e) { throw new RuntimeException(e); }
      sendToJs(tmpObj); 
     LOG.d(gTG, lTG+"Notification sent....");
   }
 }
/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

 private void sTst(final String s){
  cordova.getActivity().runOnUiThread(new Runnable(){public void run(){Toast toast=Toast.makeText(cordova.getActivity().getApplicationContext(),s,Toast.LENGTH_LONG);toast.show();}});
 }

 @Override public void onPause(boolean multitasking) {/*App loses focus*/
  String lTG = "[onPause] ";
  super.onPause(multitasking);
  LOG.v(gTG, lTG+"Pausing operations (app is loosing focus, but is still visible to the user). [multitasking is "+String.valueOf(multitasking)+".]....");
 }

 @Override public void onResume(boolean multitasking) {/*App regains focus*/
  super.onResume(multitasking);
  String lTG = "[onResume] ";
  LOG.v(gTG, lTG+"Resuming operations (acquiring focus). [multitasking is "+String.valueOf(multitasking)+".]... ");
  notifyJs_bool("camAccess",hasCameraPermission);
 }

 @Override public void onStart() { /*App becomes visible to the user*/
  String lTG = "[onStart] ";
  super.onStart();
  LOG.v(gTG, lTG+"Starting operations (becoming visible to user). Checking if it is necessary to re-run pluginInitialize() and re-runnung it if it is... Most probably it is...");
  notifyJs_bool("camAccess",hasCameraPermission);
  notifyJs_bool("initState",isFullyInitialised);
  if (!isFullyInitialised) {pluginInitialize();}/*For now we only check CAMERA permission. Later we should check general hardware availability, etc, as well.*/
 }

 @Override public void onStop() {/*App stops being visible to the user*/
  String lTG = "[onStop] ";
  super.onStop();
  LOG.v(gTG, lTG+"Stopping operations (becoming invisible to the user). Shutting down some stuff and releasing some resources (e.g. camera). Will have to re-initialise on Start...");
  isFullyInitialised = false;
  notifyJs_bool("camAccess",hasCameraPermission);
  notifyJs_bool("initState",isFullyInitialised);
  /*Partial shutdown here.*/
 }

 @Override public void onReset() {/*WebView just did a top-level navigation or refreshed.*/
  String lTG = "[onReset] ";
  super.onReset();
  LOG.v(gTG, lTG+"Plugins are supposed to stop any long-running processes and clean up internal state....");
  /*Partial shutdown here.*/
 }

 @Override public void onDestroy() {/*The one final call before Activity gets destroyed by the system. Clean-up and free all resources.*/
  String lTG = "[onDestroy] ";
  super.onDestroy();
  LOG.v(gTG, lTG+"Activity (app) is to be destroyed by the OS. Do final clean-up, release every resource that's being held etc...");
  /*Final shutdown and clean-up here.*/
 }

/*====================================================================================================================================================================*/



/*=======================================Cam-related stuff============================================================================================================*/
  private android.app.Activity                      theActivity; /* ACTIVITY IS THE CONTEXT!!!!! */
  private CameraManager                           cameraManager;
  private CameraCharacteristics           cameraCharacteristics;
  private CameraCharacteristics      frontCameraCharacteristics;
  private CameraCharacteristics       backCameraCharacteristics;
  private StreamConfigurationMap frontCamStreamConfigurationMap;
  private StreamConfigurationMap  backCamStreamConfigurationMap;
  private JSONObject                frontCam = new JSONObject();
  private JSONObject                 backCam = new JSONObject();
  private JSONObject             formatNames = new JSONObject();
  private JSONObject controlEffectsModeNames = new JSONObject();
////// private CameraDevice.StateCallback myCameraDeviceStateCallback;
////// private CameraCaptureSession.StateCallback myCameraCaptureSessionStateCallback;
////// private SurfaceTexture mySurfaceTexture;
////// private SurfaceTexture.OnFrameAvailableListener mySurfaceTextureOnFrameAvailableListener;
////// private Surface mySurface;
////// private SurfaceView mySurfaceView;
////// private SurfaceHolder mySurfaceHolder;

 private void initNamesReference() {
   formatNames.put(             "imageFormats",       JSONObject.quote("{ " + "\"" + String.valueOf(ImageFormat.DEPTH16) + "\":\"DEPTH16\", " + "\"" + String.valueOf(ImageFormat.DEPTH_POINT_CLOUD + "\":\"DEPTH_POINT_CLOUD\", " + "\"" + String.valueOf(ImageFormat.FLEX_RGBA_8888) + "\":\"FLEX_RGBA_8888\", " + "\"" + String.valueOf(ImageFormat.FLEX_RGB_888 + "\":\"FLEX_RGB_888\", " + "\"" + String.valueOf(ImageFormat.JPEG) + "\":\"JPEG\", " + "\"" + String.valueOf(ImageFormat.NV16 + "\":\"NV16\", " + "\"" + String.valueOf(ImageFormat.NV21) + "\":\"NV21\", " + "\"" + String.valueOf(ImageFormat.PRIVATE + "\":\"PRIVATE\", " + "\"" + String.valueOf(ImageFormat.RAW10) + "\":\"RAW10\", " + "\"" + String.valueOf(ImageFormat.RAW12 + "\":\"RAW12\", " + "\"" + String.valueOf(ImageFormat.RAW_PRIVATE) + "\":\"RAW_PRIVATE\", " + "\"" + String.valueOf(ImageFormat.RAW_SENSOR + "\":\"RAW_SENSOR\", " + "\"" + String.valueOf(ImageFormat.RGB_565) + "\":\"RGB_565\", " + "\"" + String.valueOf(ImageFormat.UNKNOWN + "\":\"UNKNOWN\", " + "\"" + String.valueOf(ImageFormat.YUV_420_888) + "\":\"YUV_420_888\", " + "\"" + String.valueOf(ImageFormat.YUV_422_888 + "\":\"YUV_422_888\", " + "\"" + String.valueOf(ImageFormat.YUV_444_888) + "\":\"YUV_444_888\", " + "\"" + String.valueOf(ImageFormat.YUY2 + "\":\"YUY2\", " + "\"" + String.valueOf(ImageFormat.YV12) + "\":\"YV12\"" + " }"));
   formatNames.put(             "pixelFormats",       JSONObject.quote("{ " + "\"" + String.valueOf(PixelFormat.A_8) + "\":\"A_8\", " + "\"" + String.valueOf(PixelFormat.JPEG) + "\":\"JPEG\", " + "\"" + String.valueOf(PixelFormat.LA_88) + "\":\"LA_88\", " + "\"" + String.valueOf(PixelFormat.L_8) + "\":\"L_8\", " + "\"" + String.valueOf(PixelFormat.OPAQUE) + "\":\"OPAQUE\", " + "\"" + String.valueOf(PixelFormat.RGBA_1010102) + "\":\"RGBA_1010102\", " + "\"" + String.valueOf(PixelFormat.RGBA_4444) + "\":\"RGBA_4444\", " + "\"" + String.valueOf(PixelFormat.RGBA_5551) + "\":\"RGBA_5551\", " + "\"" + String.valueOf(PixelFormat.RGBA_8888) + "\":\"RGBA_8888\", " + "\"" + String.valueOf(PixelFormat.RGBA_F16) + "\":\"RGBA_F16\", " + "\"" + String.valueOf(PixelFormat.RGBX_8888) + "\":\"RGBX_8888\", " + "\"" + String.valueOf(PixelFormat.RGB_332) + "\":\"RGB_332\", " + "\"" + String.valueOf(PixelFormat.RGB_565) + "\":\"RGB_565\", " + "\"" + String.valueOf(PixelFormat.RGB_888) + "\":\"RGB_888\", " + "\"" + String.valueOf(PixelFormat.TRANSLUCENT) + "\":\"TRANSLUCENT\", " + "\"" + String.valueOf(PixelFormat.TRANSPARENT) + "\":\"TRANSPARENT\", " + "\"" + String.valueOf(PixelFormat.UNKNOWN) + "\":\"UNKNOWN\", " + "\"" + String.valueOf(PixelFormat.YCbCr_420_SP) + "\":\"YCbCr_420_SP\", " + "\"" + String.valueOf(PixelFormat.YCbCr_422_I) + "\":\"YCbCr_422_I\", " + "\"" + String.valueOf(PixelFormat.YCbCr_422_SP) + "\":\"YCbCr_422_SP\"" + " }"));
   controlEffectsModeNames.put( "controlEffectModes", JSONObject.quote("{ " + "\"" + String.valueOf(CameraMetaData.CONTROL_EFFECT_MODE_OFF) + "\":\"CONTROL_EFFECT_MODE_OFF\", " + "\"" + String.valueOf(CameraMetaData.CONTROL_EFFECT_MODE_MONO) + "\":\"CONTROL_EFFECT_MODE_MONO\", " + "\"" + String.valueOf(CameraMetaData.CONTROL_EFFECT_MODE_NEGATIVE) + "\":\"CONTROL_EFFECT_MODE_NEGATIVE\", " + "\"" + String.valueOf(CameraMetaData.CONTROL_EFFECT_MODE_SOLARIZE) + "\":\"CONTROL_EFFECT_MODE_SOLARIZE\", " + "\"" + String.valueOf(CameraMetaData.CONTROL_EFFECT_MODE_SEPIA) + "\":\"CONTROL_EFFECT_MODE_SEPIA\", " + "\"" + String.valueOf(CameraMetaData.CONTROL_EFFECT_MODE_POSTERIZE) + "\":\"CONTROL_EFFECT_MODE_POSTERIZE\", " + "\"" + String.valueOf(CameraMetaData.CONTROL_EFFECT_MODE_WHITEBOARD) + "\":\"CONTROL_EFFECT_MODE_WHITEBOARD\", " + "\"" + String.valueOf(CameraMetaData.CONTROL_EFFECT_MODE_BLACKBOARD) + "\":\"CONTROL_EFFECT_MODE_BLACKBOARD\", " + "\"" + String.valueOf(CameraMetaData.CONTROL_EFFECT_MODE_AQUA) + "\":\"CONTROL_EFFECT_MODE_AQUA\"" + " }"));
 }

 private void setupOneCam(String camId, JSONObject camDataContainer, StreamConfigurationMap streamConfigurationMap, CameraCharacteristics cameraCharacteristics) {
   String     lTG = "[setupOneCam("+camId+")] ", formatName;
   int[]      tmpAr, tmpArr;
   Size[]     tmpSizesAr;
   Size       minSize;
   Long       minFrameDur;
   JSONObject controlAEModes, controlModes, tmpFPSAttempt = new JSONObject(), formatsSizes, sizes, effectsAvailable;
   try {
     try {
       camDataContainer.put("camId",camId);
       /***Get several specific capabilities:**************************************************************************************************/
       tmpAr = cameraCharacteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES); Arrays.sort(tmpAr);
       if ( Arrays.binarySearch(tmpAr,CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_BURST_CAPTURE)                != -1 ) {camDataContainer.put("REQUEST_AVAILABLE_CAPABILITIES_BURST_CAPTURE",true);} else {camDataContainer.put("REQUEST_AVAILABLE_CAPABILITIES_BURST_CAPTURE",false);}
       if ( Arrays.binarySearch(tmpAr,CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO) != -1 ) {camDataContainer.put("REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO",true);} else {camDataContainer.put("REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO",false);}
       /***************************************************************************************************************************************/

       /***Get hardware level:*****************************************************************************************************************/
       switch (cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL))
       {
        case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY:  camDataContainer.put("INFO_SUPPORTED_HARDWARE_LEVEL","LEGACY");  break;
        case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED: camDataContainer.put("INFO_SUPPORTED_HARDWARE_LEVEL","LIMITED"); break;
        case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL:    camDataContainer.put("INFO_SUPPORTED_HARDWARE_LEVEL","FULL");    break;
        case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3:       camDataContainer.put("INFO_SUPPORTED_HARDWARE_LEVEL","3");       break;
        default: break;
       }
       /***************************************************************************************************************************************/

       /***Get formats, their associated sizes and available effects:**************************************************************************/
       streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);//The active streamConfigurationMap object for one camera, stored in an array (of just two elements)
       camDataContainer.put("streamConfigurationMap",JSONObject.quote(streamConfigurationMap.toString()));

       formatsSizes = new JSONObject(); /* create empty container to store formats/sizes info */
       for (int format : streamConfigurationMap.getOutputFormats()) {
         sizes = new JSONObject(); int i = 0; /* create empty container to store sizes info for a given format */
         for (Size s : streamConfigurationMap.getOutputSizes(format)) {
           LOG.d(gTG,"Format/Size:\t" + format + " / " + s.toString());
           sizes.put(String.valueOf(i),s.toString()); /* fill newly created sizes JSONObject with (essentially) an array of String representation of sizes */
           i++;
         }
         if      (formatNames.getJSONObject("imageFormats").optString(String.valueOf(format)).length>0) {formatName = formatNames.getJSONObject("imageFormats").optString(String.valueOf(format));}
         else if (formatNames.getJSONObject("pixelFormats").optString(String.valueOf(format)).length>0) {formatName = formatNames.getJSONObject("pixelFormats").optString(String.valueOf(format));}
         else                                                                                           {formatName = "Unknown format("+String.valueOf(format)+")"; LOG.e(gTG, lTG+"getOutputFormats() returned an unknown format (" + format + ")!");}
         formatsSizes.put(String.valueOf(format),JSONObject.quote("{\"name\":\""+formatName+"\", \"sizes\":"+JSONObject.quote(sizes)+"}")); /* construct and add another format-sizes JSONObject to formatsSizes JSONObject */
       }
       camDataContainer.put("formatsAndSizes", JSONObject.quote(formatsSizes)); /* store the formats and sizes info in the main camera data container JSONObject */

       int[] camEffects0 = cameraCharacteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS);
       effectsAvailable = new JSONObject();
       for (int effect : camEffects0) {
         effectsAvailable.put(String.valueOf(effect),controlEffectsModeNames.optString(String.valueOf(effect),"Unknown effect ("+String.valueOf(effect)+")!"));
         LOG.d(gTG, "Effect available: " + effect);
       }
       camDataContainer.put("effectsAvailable", JSONObject.quote(effectsAvailable));
       /***************************************************************************************************************************************/

       /***Attempt to assess fastest FPS available here (must also do it via HighSpeedFPS methods etc):****************************************/
       tmpAr = streamConfigurationMap.getOutputFormats(); Arrays.sort(tmpAr);
       if(Arrays.binarySearch(tmpAr, ImageFormat.YUV_420_888) != -1) {/*If this format is supported...:*/
         tmpSizesAr = streamConfigurationMap.getOutputSizes(ImageFormat.YUV_420_888); tmpArr = new int[tmpSizesAr.length];
         for (int i=0;i<tmpSizesAr.length;i++) {tmpArr[i]=tmpSizesAr[i].getWidth()*tmpSizesAr[i].getHeight();} Arrays.sort(tmpArr);
         minSize = new Size(tmpSizesAr[0].getWidth(),tmpSizesAr[0].getHeight());
         for (int i=0;i<tmpSizesAr.length;i++) {if (tmpArr[0]==tmpSizesAr[i].getWidth()*tmpSizesAr[i].getHeight()) {minSize = new Size(tmpSizesAr[i].getWidth(),tmpSizesAr[i].getHeight());break;}}
         minFrameDur = streamConfigurationMap.getOutputMinFrameDuration(ImageFormat.YUV_420_888, minSize);//0 if unsupported or Long(ns) Minimal frame duration (for the given format/Size and assumng all mode AF AWB etc are set to OFF)
         if (minFrameDur != 0) {
           tmpFPSAttempt.put("minFrameDuration_ns",     minFrameDur);
           tmpFPSAttempt.put("ImageFormat",             "ImageFormat.YUV_420_888");
           tmpFPSAttempt.put("Size",                    JSONObject.quote("{\"width\":" + minSize.getWidth() + ", \"height\":" + String.valueOf(minSize.getHeight()) + "}"));
           camDataContainer.put("highestFPSAssessment", JSONObject.quote(tmpFPSAttempt.toString()));
         }
       }
       else {/*...and if ImageFormat.YUV_420_888 is not unsupported:*/
         LOG.w(gTG, lTG+" Format ImageFormat.YUV_420_888 (" + ImageFormat.YUV_420_888 + ") is not supported. Assumption is wrong. Skipping highestFPSAssessment.");
       }
       /***************************************************************************************************************************************/

       /***Get CONTROL_AE_AVAILABLE_MODES:*****************************************************************************************************/
       tmpAr = new int[cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES).length]; controlAEModes = new JSONObject();
       for(int i=0;i<tmpAr.length;i++)
       {
        switch(tmpAr[i])
        {
         case CameraCharacteristics.CONTROL_AE_MODE_OFF:                  controlAEModes.put(String.valueOf(tmpAr[i]),"CONTROL_AE_MODE_OFF");                  break;
         case CameraCharacteristics.CONTROL_AE_MODE_ON:                   controlAEModes.put(String.valueOf(tmpAr[i]),"CONTROL_AE_MODE_ON");                   break;
         case CameraCharacteristics.CONTROL_AE_MODE_ON_AUTO_FLASH:        controlAEModes.put(String.valueOf(tmpAr[i]),"CONTROL_AE_MODE_ON_AUTO_FLASH");        break;
         case CameraCharacteristics.CONTROL_AE_MODE_ON_ALWAYS_FLASH:      controlAEModes.put(String.valueOf(tmpAr[i]),"CONTROL_AE_MODE_ON_ALWAYS_FLASH");      break;
         case CameraCharacteristics.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE: controlAEModes.put(String.valueOf(tmpAr[i]),"CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE"); break;
         default: break;
        }
       }
       camDataContainer.put("CONTROL_AE_AVAILABLE_MODES", JSONObject.quote(controlAEModes.toString()));
       /***************************************************************************************************************************************/

       /***Get CONTROL_AVAILABLE_MODES:********************************************************************************************************/
       tmpAr = new int[cameraCharacteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_MODES).length]; controlModes = new JSONObject();
       for(int i=0;i<tmpAr.length;i++)
       {
        switch(tmpAr[i])
        {
         case CameraCharacteristics.CONTROL_MODE_OFF:            controlModes.put(String.valueOf(tmpAr[i]),"CONTROL_MODE_OFF");            break;
         case CameraCharacteristics.CONTROL_MODE_AUTO:           controlModes.put(String.valueOf(tmpAr[i]),"CONTROL_MODE_AUTO");           break;
         case CameraCharacteristics.CONTROL_MODE_USE_SCENE_MODE: controlModes.put(String.valueOf(tmpAr[i]),"CONTROL_MODE_USE_SCENE_MODE"); break;
         case CameraCharacteristics.CONTROL_MODE_OFF_KEEP_STATE: controlModes.put(String.valueOf(tmpAr[i]),"CONTROL_MODE_OFF_KEEP_STATE"); break;
         default: break;
        }
       }
       camDataContainer.put("CONTROL_AVAILABLE_MODES", JSONObject.quote(controlModes.toString()));
       /***************************************************************************************************************************************/

     }
     catch(CameraAccessException    e) {throw new Exception(e);       }
     catch(JSONException            e) {throw new Exception(e);       }
     catch(IllegalArgumentException e) {throw new RuntimeException(e);}
     catch(NullPointerException     e) {throw new RuntimeException(e);}
     catch(SecurityException        e) {throw new RuntimeException(e);}
     catch(IllegalStateException    e) {throw new RuntimeException(e);}
   }
   catch (RuntimeException e) { notifyJs_String("errors","Caught RuntimeException at setupOneCam(\""+camId+"\")!<br><br>The reason is given as:<br>"+e.toString()+"<br><br>While it itself seem to be caused by:<br>"+String.valueOf(e.getCause())+"<br><br><hr>"); }
   catch (Exception        e) { notifyJs_String("errors","Caught Exception at setupOneCam(\""+camId+"\")!<br><br>The reason is given as:<br>"+e.toString()+"<br><br>While it itself seem to be caused by:<br>"+String.valueOf(e.getCause())+"<br><br><hr>"); }
 } /*End of setupOneCam(...)*/


 private void setupCam() {
   String lTG = "[setupCam()] ";
   String[] camIdList;

   initNamesReference();

   try {
     theActivity   = cordova.getActivity();
     notifyJs_String("errors","Got theActivity from cordova.<br>It shows, that:<br>This apps package name is: "+theActivity.getPackageName()+"<br><hr>className: "+theActivity.getApplicationInfo().className+"<br>minSdkVersion: "+theActivity.getApplicationInfo().minSdkVersion+"<br>targetSdkVersion: "+theActivity.getApplicationInfo().targetSdkVersion+"<br><hr>");
     cameraManager = (CameraManager) theActivity.getSystemService(Context.CAMERA_SERVICE);
     notifyJs_String("errors","Got CameraManager from cordova.<br>Activity: "+theActivity+"<br>Context.CAMERA_SERVICE: (String)\""+Context.CAMERA_SERVICE+"\"<br><hr>");
     /*We are only interested in cameras with id of "0" or "1". Lets find out which one is back-facing and which one is forward-facing, see what each can do and store interesting for us stuff in two  JSONObjects (frontCam and backCam):*/
     try{
       camIdList = cameraManager.getCameraIdList();
       notifyJs_String("errors","Got camera ids list from CameraManager.<br>It has "+camIdList.length+" elements, with the two first being: \""+camIdList[0]+"\" and \""+camIdList[1]+"\"<br><hr>");
       frontCameraCharacteristics = cameraManager.getCameraCharacteristics(camIdList[0]);
       if (frontCameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
         /* Do camera 0 as front, 1 as back: */
         backCameraCharacteristics = cameraManager.getCameraCharacteristics(camIdList[1]);
         setupOneCam(camIdList[0], frontCam, frontCamStreamConfigurationMap, frontCameraCharacteristics);
         setupOneCam(camIdList[1],  backCam,  backCamStreamConfigurationMap,  backCameraCharacteristics);
       } else {
         /* Do camera 0 as back, 1 as front: */
         backCameraCharacteristics  = frontCameraCharacteristics;
         frontCameraCharacteristics = cameraManager.getCameraCharacteristics(camIdList[1]);
         setupOneCam(camIdList[0],  backCam,  backCamStreamConfigurationMap,  backCameraCharacteristics);
         setupOneCam(camIdList[1], frontCam, frontCamStreamConfigurationMap, frontCameraCharacteristics);
       }

       notifyJs_String("errors","Found out about two cams here.<br>Front-facing one has id: \'"+frontCam.optString("camId","unknown")+"\', and<br>Back-facing one has id: \'"+backCam.optString("camId","unknown")+"\'.<br>N.B: They MUST both appear above!<br>If not - then there is trouble.<br><hr>");
       notifyJs_JSONObject("errors",frontCam);
       notifyJs_JSONObject("errors",backCam );
     }
     catch(CameraAccessException    e) {throw new Exception(e);       }
     catch(JSONException            e) {throw new Exception(e);       }
     catch(IllegalArgumentException e) {throw new RuntimeException(e);}
   }
   catch (RuntimeException e) { notifyJs_String("errors","Caught RuntimeException at setupCam()!<br><br>The reason is given as:<br>"+e.toString()+"<br><br>While it itself seem to be caused by:<br>"+String.valueOf(e.getCause())+"<br><br><hr>"); }
   catch (Exception        e) { notifyJs_String("errors","Caught Exception at setupCam()!<br><br>The reason is given as:<br>"+e.toString()+"<br><br>While it itself seem to be caused by:<br>"+String.valueOf(e.getCause())+"<br><br><hr>"); }
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

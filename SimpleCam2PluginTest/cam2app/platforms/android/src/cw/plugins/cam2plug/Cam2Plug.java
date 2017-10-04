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
import android.view.SurfaceView;
import android.view.SurfaceHolder;
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
 private JSONObject cameraState      = new JSONObject();
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
/*-------------------------------------------------------------------------------------------------------------*/
/* private void notifyJs_bool(String propName,boolean propValue);           */
/* private void notifyJs_String(String propName,String propValue);          */
/* private void notifyJs_JSONObject(String propName, JSONObject propValue); */
/*--------------------------------------------------------------------------*/
/*       notifyJs_bool("initState", isFullyInitialised     );               */
/*       notifyJs_bool("camAccess", hasCameraPermission    );               */
/* notifyJs_JSONObject("camState",  cameraState            );               */
/* notifyJs_JSONObject("errors",    JSONObject cameraIdsEtc);               */
/*--------------------------------------------------------------------------*/

 private void notifyJs_bool(String propName,boolean propValue) {
   String lTG = "[notifyJs_bool_"+propName+"] ";
   if (commsCallbackContext == null) {LOG.d(gTG, lTG+"Need to notify js about "+propName+", but can't because commsCallbackContext is null (i.e. js hasn't yet called us to establish comms link....");}
   else if (commsCallbackContext.isFinished()) {LOG.d(gTG, lTG+"Need to notify js about "+propName+", but can't because commsCallbackContext.isFinished() returns true (i.e. it has already been used at least once and PluginResult.KeepCallback wasn't set to true at the moment, so commsCallbackContext burned after one use)....");}
   else {
     LOG.d(gTG, lTG+"Notifying js about "+propName+"....");
      try { sendToJs(new JSONObject("{\""+propName+"\":" + String.valueOf(propValue) + "}")); } catch (JSONException e) { throw new RuntimeException(e); }
     LOG.d(gTG, lTG+"Notification sent....");
   }
 }

 private void notifyJs_bool(String propName,String propValue) {
   String lTG = "[notifyJs_String_"+propName+"] ";
   if (commsCallbackContext == null) {LOG.d(gTG, lTG+"Need to notify js about "+propName+", but can't because commsCallbackContext is null (i.e. js hasn't yet called us to establish comms link....");}
   else if (commsCallbackContext.isFinished()) {LOG.d(gTG, lTG+"Need to notify js about "+propName+", but can't because commsCallbackContext.isFinished() returns true (i.e. it has already been used at least once and PluginResult.KeepCallback wasn't set to true at the moment, so commsCallbackContext burned after one use)....");}
   else {
     LOG.d(gTG, lTG+"Notifying js about "+propName+"....");
      try { sendToJs(new JSONObject("{\""+propName+"\":\"" + JSONObject.quote(propValue) + "\"}")); } catch (JSONException e) { throw new RuntimeException(e); }
     LOG.d(gTG, lTG+"Notification sent....");
   }
 }

 private void notifyJs_JSONObject(String propName, JSONObject propValue) {
   String lTG = "[notifyJs_JSONObject_"+propName+"] ";
   JSONObject tmpObj = new JSONObject();
   if (commsCallbackContext == null) {LOG.d(gTG, lTG+"Need to notify js about "+propName+", but can't because commsCallbackContext is null (i.e. js hasn't yet called us to establish comms link....");}
   else if (commsCallbackContext.isFinished()) {LOG.d(gTG, lTG+"Need to notify js about "+propName+", but can't because commsCallbackContext.isFinished() returns true (i.e. it has already been used at least once and PluginResult.KeepCallback wasn't set to true at the moment, so commsCallbackContext burned after one use)....");}
   else {
     LOG.d(gTG, lTG+"Notifying js about "+propName+"....");
      try { tmpObj.put(propName,propValue); } catch (JSONException e) { throw new RuntimeException(e); }
      sendToJs(tmpObj); 
     LOG.d(gTG, lTG+"Notification sent....");
   }
 }

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
 private Activity theActivity; /* ACTIVITY IS THE CONTEXT!!!!! */
 private CameraManager cameraManager;
 private CameraCharacteristics cameraCharacteristics;
 private JSONObject camerasIdsEtc = new JSONObject();
 private StreamConfigurationMap streamConfigurationMaps[];
 private String cameraId="";
 private CameraDevice.StateCallback myCameraDeviceStateCallback;
 private CameraCaptureSession.StateCallback myCameraCaptureSessionStateCallback;
 private SurfaceTexture mySurfaceTexture;
 private SurfaceTexture.OnFrameAvailableListener mySurfaceTextureOnFrameAvailableListener;
 private Surface mySurface;
 private SurfaceView mySurfaceView;
 private SurfaceHolder mySurfaceHolder;

 private int setupCam() {
   int[] tmpAr, tmpArr;
   Size[] tmpSizesAr;
   Size minSize;
   Long minFrameDur;
   JSONObject controlAEModes,controlModes, tmpCamObj, tmpFPSAttempt;
   try {
     theActivity   = cordova.getActivity();
        notifyJs_String("errors","Got theActivity from cordova.<br>It shows, that:<br>This apps package name is: "+theActivity.getPackageName()+"<br><hr>className: "+theActivity.getApplicationInfo().className+"<br>minSdkVersion: "+theActivity.getApplicationInfo().minSdkVersion+"<br>targetSdkVersion: "+theActivity.getApplicationInfo().targetSdkVersion+"<br><hr>");
     cameraManager = theActivity.getSystemService(theActivity.CAMERA_SERVICE);
        notifyJs_String("errors","Getting CameraManager from cordova.<br>theActivity.CAMERA_SERVICE: "+theActivity.CAMERA_SERVICE+"<br>Context.CAMERA_SERVICE: "+Context.CAMERA_SERVICE+"<br><hr>");
/*We are only interested in camera with id of "0" or "1". Lets find out which one is back-facing and which one is forward-facing, see what each can do and store interesting for us stuff in one  JSONObject (camerasIdsEtc):*/
     try{
       cameraCharacteristics = cameraManager.getCameraCharacteristics("0");
       if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
         cameraIdsEtc.put("frontCameraId","0");

         tmpCamObj = new JSONObject();/*Fill it with stuff for one cam we are ding now:*/

         tmpAr = cameraCharacteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);Arrays.sort(tmpAr);
         if (Arrays.binarySearch(tmpAr, REQUEST_AVAILABLE_CAPABILITIES_BURST_CAPTURE)!=-1) {tmpCamObj.put("REQUEST_AVAILABLE_CAPABILITIES_BURST_CAPTURE",true);} else {tmpCamObj.put("REQUEST_AVAILABLE_CAPABILITIES_BURST_CAPTURE",false);}
         if (Arrays.binarySearch(tmpAr, REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO)!=-1) {tmpCamObj.put("REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO",true);} else {tmpCamObj.put("REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO",false);}

         switch (cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL))
         {
          case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY:
            tmpCamObj.put("INFO_SUPPORTED_HARDWARE_LEVEL","LEGACY");
          break;
          case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED:
            tmpCamObj.put("INFO_SUPPORTED_HARDWARE_LEVEL","LIMITED");
          break;
          case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL:
            tmpCamObj.put("INFO_SUPPORTED_HARDWARE_LEVEL","FULL");
          break;
          case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3:
            tmpCamObj.put("INFO_SUPPORTED_HARDWARE_LEVEL","3");
          break;
          default:
          break;
         }

         streamConfigurationMaps[Integer.parseUnsignedInt(cameraIdsEtc.getInt("frontCameraId"))] = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);//The active streamConfigurationMap object for one camera, stored in an array (of just two elements)
         /*Attempt to assess fastest FPS available here (must also do it via HighSpeedFPS methods etc - below*/
         tmpAr = streamConfigurationMaps[Integer.parseUnsignedInt(cameraIdsEtc.getInt("frontCameraId"))].getOutputFormats();
         Arrays.sort(tmpAr);
         if(Arrays.binarySearch(tmpAr, ImageFormat.YUV_420_888) != -1) {//If this format is supported
           tmpSizesAr = streamConfigurationMaps[Integer.parseUnsignedInt(cameraIdsEtc.getInt("frontCameraId"))].getOutputSizes(ImageFormat.YUV_420_888);
           tmpArr = new int[tmpSizesAr.length];
           for (int i=0;i<tmpSizesAr.length;i++) {tmpArr[i]=tmpSizesAr[i].getWidth()*tmpSizesAr[i].getHeight();}
           Arrays.sort(tmpArr);
           for (int i=0;i<tmpSizesAr.length;i++) {if (tmpArr[0]==tmpSizesAr[i].getWidth()*tmpSizesAr[i].getHeight()) {minSize = new Size(tmpSizesAr[i].getWidth(),tmpSizesAr[i].getHeight());break;}}
           minFrameDur = streamConfigurationMaps[Integer.parseUnsignedInt(cameraIdsEtc.getInt("frontCameraId"))].getOutputMinFrameDuration(ImageFormat.YUV_420_888, minSize);//0 if unsupported or Long(ns) Minimal frame duration (for the given format/Size and assumng all mode AF AWB etc are set to OFF)
           if (minFrameDur != 0) {
             tmpFPSAttempt.put("minFrameDuration_ns",minFrameDur);
             tmpFPSAttempt.put("ImageFormat","ImageFormat.YUV_420_888");
             tmpFPSAttempt.put("Size", "{\"width\":" + minSize.getWidth() + ", \"height\":" + String.valueOf(minSize.getHeight()) + "}");
             tmpCamObj.put("highestFPSAssessment",tmpFPSAttempt.toString());
           }
         }
         
           /*Size[] getHighSpeedVideoSizesFor (Range<Integer> fpsRange)*/
           /*Range[]<Integer> getHighSpeedVideoFpsRanges ()*/

       tmpCamObj.put("streamConfigurationMap",JSONObject.quote(streamConfigurationMaps[Integer.parseUnsignedInt(cameraIdsEtc.getInt("frontCameraId"))].toString()));
     
       if (cameraCharacteristics.get(CameraCharacteristics.SENSOR_FRAME_DURATION)!=null) {tmpCamObj.put("SENSOR_FRAME_DURATION", cameraCharacteristics.get(CameraCharacteristics.SENSOR_FRAME_DURATION));} else {tmpCamObj.put("SENSOR_FRAME_DURATION", "null");}

       if (cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_MAX_FRAME_DURATION)!=null) {tmpCamObj.put("SENSOR_INFO_MAX_FRAME_DURATION", cameraCharacteristics.get(CameraCharacteristics.SENSOR_FRAME_DURATION));} else {tmpCamObj.put("SENSOR_INFO_MAX_FRAME_DURATION", "null");}

       tmpAr = new int[cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES).length];
       controlAEModes=JSONObject();
       for(int i=0;i<tmpAr.length;i++)
       {
        switch(tmpAr[i])
        {
         case CameraCharacteristics.CONTROL_AE_MODE_OFF:
          controlAEModes.put("OFF",tmpAr[i]);
         break;
         case CameraCharacteristics.CONTROL_AE_MODE_ON:
          controlAEModes.put("ON",tmpAr[i]);
         break;
         case CameraCharacteristics.CONTROL_AE_MODE_ON_AUTO_FLASH:
          controlAEModes.put("ON_AUTO_FLASH",tmpAr[i]);
         break;
         case CameraCharacteristics.CONTROL_AE_MODE_ON_ALWAYS_FLASH:
          controlAEModes.put("ON_ALWAYS_FLASH",tmpAr[i]);
         break;
         case CameraCharacteristics.CONTROL_AE_MODE_ON_AUTOFLASH_REDEYE:
          controlAEModes.put("ON_AUTOFLASH_REDEYE",tmpAr[i]);
         break;
         default:
         break;
        }
       }
       tmpCamObj.put("CONTROL_AE", JSONObject.quote("{\"CONTROL_AE_AVAILABLE_MODES\":"+controlAEModes.toString()+",\"CONTROL_AE_MODE\":"+String.valueOf(cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_MODE))+"}"));

       tmpAr = new int[cameraCharacteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_MODES).length];
       controlModes=JSONObject();
       for(int i=0;i<tmpAr.length;i++)
       {
        switch(tmpAr[i])
        {
         case CameraCharacteristics.CONTROL_MODE_OFF:
          controlModes.put("OFF",tmpAr[i]);
         break;
         case CameraCharacteristics.CONTROL_MODE_AUTO:
          controlModes.put("AUTO",tmpAr[i]);
         break;
         case CameraCharacteristics.CONTROL_MODE_USE_SCENE_MODE:
          controlModes.put("USE_SCENE_MODE",tmpAr[i]);
         break;
         case CameraCharacteristics.CONTROL_MODE_OFF_KEEP_STATE:
          controlModes.put("OFF_KEEP_STATE",tmpAr[i]);
         break;
         default:
         break;
        }
       }
       tmpCamObj.put("CONTROL", JSONObject.quote("{\"CONTROL_AVAILABLE_MODES\":"+controlModes.toString()+",\"CONTROL_MODE\":"+String.valueOf(cameraCharacteristics.get(CameraCharacteristics.CONTROL_MODE))+"}"));

        cameraIdsEtc.put(cameraIdsEtc.get("frontCameraId"),tmpCamObj.toString());
       }
       cameraCharacteristics = cameraManager.getCameraCharacteristics("1");
       if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK)  {
         cameraIdsEtc.put("backCameraId","1");

         tmpCamObj = new JSONObject();/*Fill it with stuff for one cam we are ding now:*/

         tmpAr = cameraCharacteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);Arrays.sort(tmpAr);
         if (Arrays.binarySearch(tmpAr, REQUEST_AVAILABLE_CAPABILITIES_BURST_CAPTURE)!=-1) {tmpCamObj.put("REQUEST_AVAILABLE_CAPABILITIES_BURST_CAPTURE",true);} else {tmpCamObj.put("REQUEST_AVAILABLE_CAPABILITIES_BURST_CAPTURE",false);}
         if (Arrays.binarySearch(tmpAr, REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO)!=-1) {tmpCamObj.put("REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO",true);} else {tmpCamObj.put("REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO",false);}

         switch (cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL))
         {
          case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY:
            tmpCamObj.put("INFO_SUPPORTED_HARDWARE_LEVEL","LEGACY");
          break;
          case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED:
            tmpCamObj.put("INFO_SUPPORTED_HARDWARE_LEVEL","LIMITED");
          break;
          case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL:
            tmpCamObj.put("INFO_SUPPORTED_HARDWARE_LEVEL","FULL");
          break;
          case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3:
            tmpCamObj.put("INFO_SUPPORTED_HARDWARE_LEVEL","3");
          break;
          default:
          break;
         }

         streamConfigurationMaps[Integer.parseUnsignedInt(cameraIdsEtc.getInt("backCameraId"))] = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);//The active streamConfigurationMap object for one camera, stored in an array (of just two elements)
         /*Attempt to assess fastest FPS available here (must also do it via HighSpeedFPS methods etc - below*/
         tmpAr = streamConfigurationMaps[Integer.parseUnsignedInt(cameraIdsEtc.getInt("backCameraId"))].getOutputFormats();
         Arrays.sort(tmpAr);
         if(Arrays.binarySearch(tmpAr, ImageFormat.YUV_420_888) != -1) {//If this format is supported
           tmpSizesAr = streamConfigurationMaps[Integer.parseUnsignedInt(cameraIdsEtc.getInt("backCameraId"))].getOutputSizes(ImageFormat.YUV_420_888);
           tmpArr = new int[tmpSizesAr.length];
           for (int i=0;i<tmpSizesAr.length;i++) {tmpArr[i]=tmpSizesAr[i].getWidth()*tmpSizesAr[i].getHeight();}
           Arrays.sort(tmpArr);
           for (int i=0;i<tmpSizesAr.length;i++) {if (tmpArr[0]==tmpSizesAr[i].getWidth()*tmpSizesAr[i].getHeight()) {minSize = new Size(tmpSizesAr[i].getWidth(),tmpSizesAr[i].getHeight());break;}}
           minFrameDur = streamConfigurationMaps[Integer.parseUnsignedInt(cameraIdsEtc.getInt("backCameraId"))].getOutputMinFrameDuration(ImageFormat.YUV_420_888, minSize);//0 if unsupported or Long(ns) Minimal frame duration (for the given format/Size and assumng all mode AF AWB etc are set to OFF)
           if (minFrameDur != 0) {
             tmpFPSAttempt.put("minFrameDuration_ns",minFrameDur);
             tmpFPSAttempt.put("ImageFormat","ImageFormat.YUV_420_888");
             tmpFPSAttempt.put("Size", "{\"width\":" + minSize.getWidth() + ", \"height\":" + String.valueOf(minSize.getHeight()) + "}");
             tmpCamObj.put("highestFPSAssessment",tmpFPSAttempt.toString());
           }
         }

           /*Size[] getHighSpeedVideoSizesFor (Range<Integer> fpsRange)*/
           /*Range[]<Integer> getHighSpeedVideoFpsRanges ()*/

       tmpCamObj.put("streamConfigurationMap",JSONObject.quote(streamConfigurationMaps[Integer.parseUnsignedInt(cameraIdsEtc.getInt("backCameraId"))].toString()));

       if (cameraCharacteristics.get(CameraCharacteristics.SENSOR_FRAME_DURATION)!=null) {tmpCamObj.put("SENSOR_FRAME_DURATION", cameraCharacteristics.get(CameraCharacteristics.SENSOR_FRAME_DURATION));} else {tmpCamObj.put("SENSOR_FRAME_DURATION", "null");}

       if (cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_MAX_FRAME_DURATION)!=null) {tmpCamObj.put("SENSOR_INFO_MAX_FRAME_DURATION", cameraCharacteristics.get(CameraCharacteristics.SENSOR_FRAME_DURATION));} else {tmpCamObj.put("SENSOR_INFO_MAX_FRAME_DURATION", "null");}

       tmpAr = new int[cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES).length];
       controlAEModes=JSONObject();
       for(int i=0;i<tmpAr.length;i++)
       {
        switch(tmpAr[i])
        {
         case CameraCharacteristics.CONTROL_AE_MODE_OFF:
          controlAEModes.put("OFF",tmpAr[i]);
         break;
         case CameraCharacteristics.CONTROL_AE_MODE_ON:
          controlAEModes.put("ON",tmpAr[i]);
         break;
         case CameraCharacteristics.CONTROL_AE_MODE_ON_AUTO_FLASH:
          controlAEModes.put("ON_AUTO_FLASH",tmpAr[i]);
         break;
         case CameraCharacteristics.CONTROL_AE_MODE_ON_ALWAYS_FLASH:
          controlAEModes.put("ON_ALWAYS_FLASH",tmpAr[i]);
         break;
         case CameraCharacteristics.CONTROL_AE_MODE_ON_AUTOFLASH_REDEYE:
          controlAEModes.put("ON_AUTOFLASH_REDEYE",tmpAr[i]);
         break;
         default:
         break;
        }
       }
       tmpCamObj.put("CONTROL_AE", JSONObject.quote("{\"CONTROL_AE_AVAILABLE_MODES\":"+controlAEModes.toString()+",\"CONTROL_AE_MODE\":"+String.valueOf(cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_MODE))+"}"));

       tmpAr = new int[cameraCharacteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_MODES).length];
       controlModes=JSONObject();
       for(int i=0;i<tmpAr.length;i++)
       {
        switch(tmpAr[i])
        {
         case CameraCharacteristics.CONTROL_MODE_OFF:
          controlModes.put("OFF",tmpAr[i]);
         break;
         case CameraCharacteristics.CONTROL_MODE_AUTO:
          controlModes.put("AUTO",tmpAr[i]);
         break;
         case CameraCharacteristics.CONTROL_MODE_USE_SCENE_MODE:
          controlModes.put("USE_SCENE_MODE",tmpAr[i]);
         break;
         case CameraCharacteristics.CONTROL_MODE_OFF_KEEP_STATE:
          controlModes.put("OFF_KEEP_STATE",tmpAr[i]);
         break;
         default:
         break;
        }
       }
       tmpCamObj.put("CONTROL", JSONObject.quote("{\"CONTROL_AVAILABLE_MODES\":"+controlModes.toString()+",\"CONTROL_MODE\":"+String.valueOf(cameraCharacteristics.get(CameraCharacteristics.CONTROL_MODE))+"}"));

        cameraIdsEtc.put(cameraIdsEtc.get("backCameraId"),tmpCamObj.toString());
       }
       notifyJs_String("errors","Found out about two cams here.<br>Front-facing one has id: \'"+String.valueOf(optString("frontCameraId","null"))+"\', and<br>Back-facing one has id: \'"+String.valueOf(optString("backCameraId","null"))+"\'.<br>N.B: They MUST both appear above!<br>If not - then there is trouble.<br><hr>");
       notifyJs_JSONObject("errors",cameraIdsEtc);
/*Statistics etc can (and should) be easily cut out from here later on.*/
       cameraId = cameraIdsEtc.get("frontCameraId");/*Pick one camera to make it easier for now*/
/*

       public class MyCameraDeviceStateCallback extends CameraDevice.StateCallback {
        private JSONObject error_codes;

         @Override public MyCameraDeviceStateCallback() {
           super();
           notifyJs_String("errors","MyCameraDeviceStateCallback says: constructor called!");
           error_codes= new JSONObject();
           error_codes.put(JSONObject.numberToString(ERROR_CAMERA_DEVICE),      "ERROR_CAMERA_DEVICE");
           error_codes.put(JSONObject.numberToString(ERROR_CAMERA_DISABLED),    "ERROR_CAMERA_DISABLED");
           error_codes.put(JSONObject.numberToString(ERROR_CAMERA_IN_USE),      "ERROR_CAMERA_IN_USE");
           error_codes.put(JSONObject.numberToString(ERROR_CAMERA_SERVICE),     "ERROR_CAMERA_SERVICE");
           error_codes.put(JSONObject.numberToString(ERROR_MAX_CAMERAS_IN_USE), "ERROR_MAX_CAMERAS_IN_USE");
         }

         @Override void onClosed (CameraDevice camera) {
           notifyJs_String("errors","MyCameraDeviceStateCallback says: onClosed called!");
         }

         @Override void onOpen (CameraDevice camera) {
           notifyJs_String("errors","MyCameraDeviceStateCallback says: onOpen called!");
         }

         @Override void onDisconnected (CameraDevice camera) {
           notifyJs_String("errors","MyCameraDeviceStateCallback says: onDisconnected called!");
         }

         @Override void onError (CameraDevice camera, int error) {
           String txt_error = error_codes.get(JSONObject.numberToString(error));
           notifyJs_String("errors","MyCameraDeviceStateCallback says: "+txt_error);
         }

         }
       }
       myCameraDeviceStateCallback = new MyCameraDeviceStateCallback();


       cameraManager.openCamera(cameraId,myCameraDeviceStateCallback,null);//Use current thread's looper for now


       public class MyCameraCaptureSessionStateCallback extends CameraCaptureSession.StateCallback {
         @Override public MyCameraCaptureSessionStateCallback() {
           super();
           notifyJs_String("errors","MyCameraCaptureSessionStateCallback says: constructor called!");
         }

         @Override void onActive (CameraCaptureSession session) {
           notifyJs_String("errors","MyCameraCaptureSessionStateCallback says: onActive called!");
         }

         @Override void onCaptureQueueEmpty (CameraCaptureSession session) {
           notifyJs_String("errors","MyCameraCaptureSessionStateCallback says: onCaptureQueueEmpty called!");
         }

         @Override void onClosed (CameraCaptureSession session) {
           notifyJs_String("errors","MyCameraCaptureSessionStateCallback says: onClosed called!");
         }

         @Override void onConfigureFailed (CameraCaptureSession session) {
           notifyJs_String("errors","MyCameraCaptureSessionStateCallback says: onConfigureFailed called!");
         }

         @Override void onConfigured (CameraCaptureSession session) {
           notifyJs_String("errors","MyCameraCaptureSessionStateCallback says: onConfigured called!");
         }

         @Override void onReady (CameraCaptureSession session) {
           notifyJs_String("errors","MyCameraCaptureSessionStateCallback says: onReady called!");
         }

         @Override void onSurfacePrepared (CameraCaptureSession session, Surface surface) {
           notifyJs_String("errors","MyCameraCaptureSessionStateCallback says: onSurfacePrepared called!");
         }
       }
       myCameraCaptureSessionStateCallback = new MyCameraCaptureSessionStateCallback();




       public class MySurfaceTextureOnFrameAvailableListener implements SurfaceTexture.OnFrameAvailableListener {

         public MySurfaceTextureOnFrameAvailableListener() {
           notifyJs_String("errors","MySurfaceTextureOnFrameAvailableListener says: constructor called!");
         }

         public void onFrameAvailable(SurfaceTexture surfaceTexture) {
           notifyJs_String("errors","MySurfaceTextureOnFrameAvailableListener says: onFrameAvailable called!");
         }
       }
       mySurfaceTextureOnFrameAvailableListener = new MySurfaceTextureOnFrameAvailableListener();
*/


//      mySurfaceTexture = 
//      mySurface        = new Surface(mySurfaceTexture);

//      mySurfaceView   = SurfaceView(theActivity);
//      mySurfaceHolder = mySurfaceView.getHolder();
//      mySurface       = mySurfaceHolder.getSurface();

//createCaptureSession (List<Surface> outputs, myCameraCaptureSessionStateCallback, Handler handler)


     }
     catch(CameraAccessException e)    {throw new Exception(e);}
     catch(JSONException e)            {throw new Exception(e);}
     catch(IllegalArgumentException e) {throw new RuntimeException(e);}
     catch(NullPointerException e)     {throw new RuntimeException(e);}
     catch(SecurityException e)        {throw new RuntimeException(e);}
     catch(IllegalStateException e)    {throw new RuntimeException(e);}
   }
   catch (Exception e)        { notifyJs_String("errors","Caught Exception at setupCam()!<br><br>The reason is given as:<br>"+e.toString()+"<br><br>While it itself seem to be caused by:<br>"+String.valueOf(e.getCause())+"<br><br><hr>"); }
   catch (RuntimeException e) { notifyJs_String("errors","Caught RuntimeException at setupCam()!<br><br>The reason is given as:<br>"+e.toString()+"<br><br>While it itself seem to be caused by:<br>"+String.valueOf(e.getCause())+"<br><br><hr>"); }
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

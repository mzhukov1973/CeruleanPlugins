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
package cw.swcore.ceruleanplugins;

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
//import android.util.Log;
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




public class CeruleanPlugins extends CordovaPlugin {
    public static final String CAMERA                  = Manifest.permission.CAMERA;
    public static final int    SEARCH_REQ_CODE         = 0;
    public static final int    PERMISSION_DENIED_ERROR = 20;
    private CameraManager                      cameraManager;
    private String[]                           camList;
    private CameraCharacteristics              currCamParam;
    private int                                currCamId = 0;
    private boolean                            isFlashOn;
    private FlashCallback                      flashCallback = new FlashCallback();
    private StreamConfigurationMap             streamConfigurationMap;
    private CameraDevice                       cameraDevice;
    private CameraDevice.StateCallback         stateCallback = new CameraDevice.StateCallback(){@Override public void onOpened(CameraDevice camera){cameraDevice=camera;startCameraPreview();} @Override public void onDisconnected(CameraDevice camera){cameraDevice.close();} @Override public void onError(CameraDevice camera,int error){cameraDevice.close();cameraDevice = null;}};
    private Size                               previewSize;
    private TextureView                        textureView;
    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() 
{
 @Override public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height)
{
 if(dbgDebug){LOG.d("CeruleanWhisper---onSurfaceTextureAvailable",String.format("It has fired! surface:%s, width:%d height:%d",surface.toString(),width,height));}
 openCamera();
} 
 @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height){} 
 @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface){return false;} 
 @Override public void onSurfaceTextureUpdated(SurfaceTexture surface){}};
    private SurfaceTexture                     texture;
    private CaptureRequest                     captureRequest;
    private CaptureRequest.Builder             captureRequestBuilder;
    private Surface                            surface;
    private CameraCaptureSession               cameraCaptureSession;
    private Handler                            backgroundCamHandler;
    private HandlerThread                      backgroundCamThread;

/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~debug flags. These are ANDed for each LOG.i~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
    private boolean dbgDebug     = true; /* global flag allowing or disallowing logging */
/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~debug flags, the are ORed for each LOG.i statement~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
    private boolean dbgPlugin    = true; /* section: Plugin, Echo, Flash, Watch */
    private boolean dbgEcho      = false;
    private boolean dbgFlash     = false;
    private boolean dbgWatch     = true;
/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
    private boolean dbgPluginInitialize        = true; /* per-function flags. N.B.: Exceptions & errors are governed only by simplest of flags!  */
    private boolean dbgPluginInitializeOutside = true;
    private boolean dbgPluginInitializeInside  = true;

    private boolean dbgOpenCamera        = true;
    private boolean dbgOpenCameraOutside = true;
    private boolean dbgOpenCameraInside  = true;

    private boolean dbgStartCameraPreview        = true;
    private boolean dbgStartCameraPreviewOutside = true;
    private boolean dbgStartCameraPreviewInside  = true;


    private boolean dbgBlinkFlashFunc        = false;
    private boolean dbgBlinkFlashFuncOutside = false;
    private boolean dbgBlinkFlashFuncInside  = false;
    private boolean dbgBlinkFlashClass        = false;
    private boolean dbgBlinkFlashClassOutside = false;
    private boolean dbgBlinkFlashClassInside  = false;

    private boolean dbgMisc                   = true;


/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException 
    {
     if      (action.equals("myMethod"))   {if(dbgDebug&&dbgPlugin){LOG.i("CeruleanWhisper---execute","Calling myMethod.");}String message = args.getString(0); this.myMethod(message, callbackContext); return true;}
     else if (action.equals("blinkFlash")) {if(dbgDebug&&dbgPlugin){LOG.i("CeruleanWhisper---execute","Calling blinkFlash.");}this.blinkFlash(args, callbackContext); return true;}
     else if (action.equals("blinkWatch")) {if(dbgDebug&&dbgPlugin){LOG.i("CeruleanWhisper---execute","Calling blinkWatch.");}this.blinkWatch(args, callbackContext); return true;}
     return false;
    }

    @Override
    public void pluginInitialize() {
        LOG.setLogLevel(LOG.INFO);
        if(dbgDebug&&dbgPlugin&&dbgPluginInitialize){LOG.i("CeruleanWhisper---pluginInitialize","Logging is on. LOGLEVEL has just been set to INFO. Starting plugin initialization.");}
        if(dbgDebug&&dbgPlugin&&dbgPluginInitialize&&dbgPluginInitializeOutside){LOG.i("CeruleanWhisper---pluginInitialize","Checking for CAMERA permission... Currently it is to %s",String.valueOf(cordova.hasPermission(CAMERA)));}
        if (!cordova.hasPermission(CAMERA)) {cordova.requestPermission(this, SEARCH_REQ_CODE, CAMERA);}
        if(dbgDebug&&dbgPlugin&&dbgPluginInitialize&&dbgPluginInitializeInside){LOG.i("CeruleanWhisper---pluginInitialize","Checking for CAMERA permission AFTER the request... Currently it is to %s",String.valueOf(cordova.hasPermission(CAMERA)));}
        getProperCamera();                                             /*blinkFlash, blinkWatch*/
        setupFlash();                                                  /*blinkFlash, blinkWatch*/
        startBackgroundCamThread();                                    /*            blinkWatch*/
        textureView = new TextureView(webView.getContext());           /*            blinkWatch*/
        textureView.setSurfaceTextureListener(surfaceTextureListener); /*            blinkWatch*/
        if(dbgDebug&&dbgPlugin&&dbgPluginInitialize&&dbgPluginInitializeOutside){LOG.i("CeruleanWhisper---pluginInitialize","Plugin initialisation complete.");}
    }

@Override public void onRequestPermissionResult(int requestCode, String[] permissions,int[] grantResults) throws JSONException {
        if(dbgDebug&&dbgPlugin){LOG.i("CeruleanWhisper---pluginInitialize","And checking for CAMERA permission for the THIRD time, as a reaction to RequestPermissionResult event... Currently it is to %s",String.valueOf(cordova.hasPermission(CAMERA)));}
}
  /*  @Override public void onRequestPermissionResult(int requestCode, String[] permissions,int[] grantResults) throws JSONException {for(int r:grantResults) {if (r == PackageManager.PERMISSION_DENIED){this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR));return;}}switch(requestCode){case SEARCH_REQ_CODE:search(executeArgs);break;case SAVE_REQ_CODE:save(executeArgs);break; case REMOVE_REQ_CODE:remove(executeArgs);break;default:}} */

    /*======Cordovas' android lifecycle hooks:======*/
    @Override
        public void onDestroy()
        {
         super.onDestroy();
         if((cameraManager!=null)&&(flashCallback!=null)) 
         {
          cameraManager.unregisterTorchCallback(flashCallback);
         }
         stopBackgroundCamThread();
        }

    @Override public void onReset   ()                      {super.onReset();              if((cameraManager!=null)&&(flashCallback!=null)) {cameraManager.unregisterTorchCallback(flashCallback);}}
    @Override public void onPause   (boolean multitasking)  {super.onPause(multitasking);  if((cameraManager!=null)&&(flashCallback!=null)) {cameraManager.unregisterTorchCallback(flashCallback);}}
    @Override public void onResume  (boolean multitasking)  {super.onResume(multitasking); if((cameraManager!=null)&&(flashCallback!=null)) {try{cameraManager.registerTorchCallback(flashCallback,null);} catch(IllegalArgumentException e) {if(dbgDebug&&dbgPlugin){LOG.i("CeruleanWhisper---onResume",String.format("Caught %s at cameraManager.registerTorchCallback(flashCallback,null)! (%s) Not fatal, re-throwing it as an RuntimeException and moving on.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}throw new RuntimeException(e);}}}
/*|===End of plugin related stuff:=================================================================================================|*/
/*|================================================================================================================================|*/


/*|================================================================================================================================|*/
/*|===Echo related stuff:==========================================================================================================|*/
    /*Echo action (the method, that gets called from Cordova):*/
    private void myMethod(String message, CallbackContext callbackContext) 
    {
     if (message == null || message.length() == 0) {callbackContext.error("Expected one non-empty string argument.");return;}
     callbackContext.success(message);
    }
/*|===End of echo related stuff:===================================================================================================|*/
/*|================================================================================================================================|*/



/*|==================================================================================================================================================================================|*/
/*dfg===Cam related stuff:=============================================================================================================================================================|*/
    class FlashCallback extends CameraManager.TorchCallback
    {
     @Override public void onTorchModeUnavailable(String cameraId) {super.onTorchModeUnavailable(cameraId );if(dbgDebug&&dbgFlash&&dbgMisc){LOG.i("CeruleanWhisper---FlashCallback.onTorchModeUnavailable",String.format("FlashCallBack.onTorchModeUnavailable has been called. CameraID: %s",cameraId));}}
     @Override public void onTorchModeChanged(String cameraId, boolean enabled) {super.onTorchModeChanged(cameraId, enabled); if(dbgDebug&&dbgFlash&&dbgMisc){LOG.i("CeruleanWhisper---FlashCallback.onTorchModeChanged",String.format("FlashCallBack.onTorchModeChanged has been called. CameraID: %s, Enabled: %s",cameraId,String.valueOf(enabled)));}}
    }
    private void   getProperCamera()         {if(dbgDebug&&(dbgFlash||dbgWatch)&&dbgMisc){LOG.i("CeruleanWhisper---getProperCamera","getProperCamera has been called.");}if (cameraManager == null) {try {cameraManager = webView.getContext().getSystemService(CameraManager.class); camList=cameraManager.getCameraIdList();for (currCamId=0;currCamId<camList.length;currCamId++) {currCamParam=cameraManager.getCameraCharacteristics(camList[currCamId]);if (currCamParam.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {break;}}} catch(CameraAccessException e) {if(dbgDebug&&(dbgFlash||dbgWatch)&&dbgMisc){LOG.i("CeruleanWhisper---getProperCamera",String.format("Caught %s somewhere in getProperCamera()! (%s) Not fatal, re-throwing it as an RuntimeException and moving on.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}throw new RuntimeException(e);} catch(IllegalArgumentException e) {if(dbgDebug&&(dbgFlash||dbgWatch)&&dbgMisc){LOG.i("CeruleanWhisper---getProperCamera",String.format("Caught %s somewhere in getProperCamera()! (%s) Not fatal, re-throwing it as an RuntimeException and moving on.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}throw new RuntimeException(e);}}}
    private void   setupFlash()              {if(dbgDebug&&dbgFlash&&dbgMisc){LOG.i("CeruleanWhisper---setupFlash","setupFlash has been called.");}try{cameraManager.registerTorchCallback(flashCallback,null);} catch(IllegalArgumentException e){if(dbgDebug&&dbgFlash&&dbgMisc){LOG.i("CeruleanWhisper---setupFlash",String.format("Caught %s somewhere in setupFlash()! (%s) Not fatal, re-throwing it as an RuntimeException and moving on.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}throw new RuntimeException(e);}}
    private String composeCurrCamStatusMsg() {if(dbgDebug&&(dbgFlash||dbgWatch)&&dbgMisc){LOG.i("CeruleanWhisper---composeCurrCamStatusMsg","composeCurrCamStatusMsg has been called.");}String facing,isFlashAvailable;switch (currCamParam.get(CameraCharacteristics.LENS_FACING)){case CameraCharacteristics.LENS_FACING_BACK:facing="back";break;case CameraCharacteristics.LENS_FACING_FRONT:facing="front"; break;case CameraCharacteristics.LENS_FACING_EXTERNAL:facing="external";break;default:facing="none";}isFlashAvailable = String.valueOf(currCamParam.get(CameraCharacteristics.FLASH_INFO_AVAILABLE));return String.format("Camera with id:%d of %d [0 - %d], is selected. It is facing %s, flash exists: %s.",camList[currCamId],camList.length,camList.length-1,facing,isFlashAvailable);}
    private void blinkFlash(JSONArray args, CallbackContext callbackContext)
    {
     String argS; JSONArray arg; int argLen; Thread blinkThread;
     if(dbgDebug&&dbgFlash&&dbgBlinkFlashFunc&&dbgBlinkFlashFuncOutside){LOG.i("CeruleanWhisper---blinkFlash","blinkFlash has been called.");}
     class BlinkFlash implements Runnable
     {
       private CameraManager   cameraManager;   private String currCamIdStr;                           private int       argLen;
       private CallbackContext callbackContext; private String successMsg = "End of the flash run..."; private JSONArray arg;
       public BlinkFlash(CameraManager par_camera, String par_currCamIdStr, JSONArray par_args, CallbackContext par_callbackContext) throws Exception /*0 = constructor*/
       {
        this.cameraManager=par_camera; this.currCamIdStr=par_currCamIdStr; this.callbackContext=par_callbackContext;
        try{this.arg=new JSONArray(par_args.getString(0));} 
        catch(JSONException e) {
                      if(dbgDebug&&dbgFlash&&dbgBlinkFlashClass){LOG.i("CeruleanWhisper---BlinkFlash.constructor",String.format("Caught %s at JSONArray(par_args.getString(0))! (%s) Exiting the function...",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}
                               }
        this.argLen = 2*((int)this.arg.length()/2);
        if (this.argLen<2) {
                            if(dbgDebug&&dbgFlash&&dbgBlinkFlashClass){LOG.i("CeruleanWhisper---BlinkFlash.constructor",String.format("Error! arg array has less than 2 elements (%s).",String.valueOf(this.arg)));}
                            return;
                           }
       }
       /*1 = dealWithExc*/
       private void dealWithExc(Exception e)                          {String excTxt = String.format("Exception! msg:'%s' (cause:'%s')",e.getMessage(),((e.getCause()==null)?"none":e.getCause().getMessage()));
                                                                       if(dbgDebug&&dbgFlash&&dbgBlinkFlashClass){LOG.i("CeruleanWhisper---BlinkFlash.dealWithExc",String.format("Caught %s somewhere at BlinkFlash.run()! (%s).",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}
                                                                       this.callbackContext.error(excTxt);
                                                                      }
       /*2 = flachOff*/
       private void flashOff   (String currCamIdStr) throws Exception {try {this.cameraManager.setTorchMode(currCamIdStr,false);} 
                                                                       catch(CameraAccessException e) {
                                                                          if(dbgDebug&&dbgFlash&&dbgBlinkFlashClass){LOG.i("CeruleanWhisper---BlinkFlash.flashOff",String.format("Caught %s at this.cameraManager.setTorchMode(currCamIdStr,false)! (%s) Not fatal, logging it and moving on.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}
                                                                                                      } 
                                                                       catch(IllegalArgumentException e) {
                                                                          if(dbgDebug&&dbgFlash&&dbgBlinkFlashClass){LOG.i("CeruleanWhisper---BlinkFlash.flashOff",String.format("Caught %s at this.cameraManager.setTorchMode(currCamIdStr,false)! (%s) Not fatal, logging it and moving on.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}
                                                                                                         }
                                                                      }
       /*3 = flashOn*/
       private void flashOn    (String currCamIdStr) throws Exception {try {this.cameraManager.setTorchMode(currCamIdStr,true);} 
                                                                       catch(CameraAccessException e) {
                                                                              if(dbgDebug&&dbgFlash&&dbgBlinkFlashClass){LOG.i("CeruleanWhisper---BlinkFlash.flashOn",String.format("Caught %s at this.cameraManager.setTorchMode(currCamIdStr,true)! (%s) Not fatal, logging it and moving on.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}
                                                                              throw new Exception("flashOn method failed",e);
                                                                                                      } 
                                                                       catch(IllegalArgumentException e) {
                                                                              if(dbgDebug&&dbgFlash&&dbgBlinkFlashClass){LOG.i("CeruleanWhisper---BlinkFlash.flashOn",String.format("Caught %s at this.cameraManager.setTorchMode(currCamIdStr,true)! (%s) Not fatal, re-throwing it as an Exception and moving on.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}
                                                                                                         }
                                                                      }
       /*4 = pause*/
       private void pause      (int i)               throws Exception {int ms=0; try{ms=this.arg.getInt(i);} 
                                                                                 catch(JSONException e) {
                                                                                    if(dbgDebug&&dbgFlash&&dbgBlinkFlashClass){LOG.i("CeruleanWhisper---BlinkFlash.pause",String.format("Caught %s at ms=this.arg.getInt(i)! (%s) Not fatal, logging it and moving on.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}
                                                                                                        } 
                                                                                 try {Thread.sleep(ms);} 
                                                                                 catch(IllegalArgumentException e) {
                                                                                    if(dbgDebug&&dbgFlash&&dbgBlinkFlashClass){LOG.i("CeruleanWhisper---BlinkFlash.pause",String.format("Caught %s at Thread.sleep(ms)! (%s) Not fatal, logging it and moving on.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}
                                                                                                                   } 
                                                                                 catch(InterruptedException e) {
                                                                                    if(dbgDebug&&dbgFlash&&dbgBlinkFlashClass){LOG.i("CeruleanWhisper---BlinkFlash.pause",String.format("Caught %s at Thread.sleep(ms)! (%s) Not fatal, logging it moving on.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}
                                                                                                               }
                                                                      }
       /*5 = run*/
       public  void run        ()                                     {if(dbgDebug&&dbgFlash&&dbgBlinkFlashClass&&dbgBlinkFlashClassInside){LOG.i("CeruleanWhisper---BlinkFlash.run","BlinkFlash.run has been launched in a separate thread.");}
                                                                       try {this.flashOff(this.currCamIdStr);
                                                                            for(int i=0;i<this.argLen;i+=2) {this.flashOn(this.currCamIdStr);this.pause(i);this.flashOff(this.currCamIdStr);this.pause(i+1);} 
                                                                            this.flashOff(this.currCamIdStr);
                                                                            if(dbgDebug&&dbgFlash&&dbgBlinkFlashClass&&dbgBlinkFlashClassOutside){LOG.i("CeruleanWhisper---BlinkFlash.run","BlinkFlash.run is done, thread is finishing.");} 
                                                                            this.callbackContext.success(this.successMsg);
                                                                           }
                                                                       catch (Exception e) {this.dealWithExc(e);return;} 
                                                                      }
     }
     try {blinkThread = new Thread( new BlinkFlash(cameraManager,camList[currCamId],args,callbackContext) );
          blinkThread.start();
         } 
     catch(IllegalThreadStateException e) {
                if(dbgDebug&&dbgFlash&&dbgBlinkFlashFunc){LOG.i("CeruleanWhisper---blinkFlash",String.format("Caught %s at blinkThread.start()! (%s) Not fatal, logging it and moving on.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}
                                          }
     catch(Exception e) {
                if(dbgDebug&&dbgFlash&&dbgBlinkFlashFunc){LOG.i("CeruleanWhisper---blinkFlash",String.format("Caught %s at blinkThread.start()! (%s) Not fatal, exiting blinkFlash.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}
                String excTxt = String.format("Exception in blinkFlash()! msg:'%s' (cause:'%s')",e.getMessage(),((e.getCause()==null)?"none":e.getCause().getMessage()));
                callbackContext.error(excTxt);
                return;
                        }
     if(dbgDebug&&dbgFlash&&dbgBlinkFlashFunc&&dbgBlinkFlashFuncOutside){LOG.i("CeruleanWhisper---blinkFlash","blinkFlash is done, exiting it.");}
    }

/*======Now cam-capture per se:======*/
    private void startBackgroundCamThread () {
                    if(dbgDebug&&dbgWatch&&dbgMisc){LOG.i("CeruleanWhisper---startBackgroundCamThread","startBackgroundCamThread has been called.");}
                    backgroundCamThread=new HandlerThread("Background Camera Thread");
                    backgroundCamThread.start();
                    backgroundCamHandler=new Handler(backgroundCamThread.getLooper());
                    LOG.i("CeruleanWhisper---startBackgroundCamThread","startBackgroundCamThread is done, exiting it.");
                                             }
    private void stopBackgroundCamThread  () {
                    if(dbgDebug&&dbgWatch&&dbgMisc){LOG.i("CeruleanWhisper---stopBackgroundCamThread","stopBackgroundCamThread has been called.");}
                    backgroundCamThread.quitSafely();
                    try {backgroundCamThread.join();
                         backgroundCamThread=null;
                         backgroundCamHandler=null;
                         if(dbgDebug&&dbgWatch&&dbgMisc){LOG.i("CeruleanWhisper---stopBackgroundCamThread","stopBackgroundCamThread is done, exiting it.");}
                        }
                    catch (InterruptedException e) {
                                if(dbgDebug&&dbgWatch&&dbgMisc){LOG.i("CeruleanWhisper---stopBackgroundCamThread",String.format("Caught %s at backgroundCamThread.join()! (%s) Not fatal, re-throwing it as a RuntimeException and moving on.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}
                                throw new RuntimeException(e);
                                                   }
                                             }

    private void openCamera()
    {
     if(dbgDebug&&dbgWatch&&dbgOpenCamera&&dbgOpenCameraOutside){LOG.i("CeruleanWhisper---openCamera","openCamera has been called.");}
     try
     {
      if(dbgDebug&&dbgWatch&&dbgOpenCamera&&dbgOpenCameraInside){LOG.i("CeruleanWhisper---openCamera","Attempting currCamParam.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)...");}
      streamConfigurationMap = currCamParam.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
      if(dbgDebug&&dbgWatch&&dbgOpenCamera&&dbgOpenCameraInside){LOG.i("CeruleanWhisper---openCamera","Done with currCamParam.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).");}
      if(dbgDebug&&dbgWatch&&dbgOpenCamera&&dbgOpenCameraInside){LOG.i("CeruleanWhisper---openCamera","Attempting streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0]...");}
      previewSize = streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0];
      if(dbgDebug&&dbgWatch&&dbgOpenCamera&&dbgOpenCameraInside){LOG.i("CeruleanWhisper---openCamera","Done with streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0].");}
      if(dbgDebug&&dbgWatch&&dbgOpenCamera&&dbgOpenCameraInside){LOG.i("CeruleanWhisper---openCamera","Checking permission for the CAMERA...");}
      if (!cordova.hasPermission(CAMERA)) {return;} /*Just a final check here. Permissions are usually checked and requested in initialize method if the plugin.*/
      if(dbgDebug&&dbgWatch&&dbgOpenCamera&&dbgOpenCameraInside){LOG.i("CeruleanWhisper---openCamera","Done checking CAMERA permission.");}
      if(dbgDebug&&dbgWatch&&dbgOpenCamera&&dbgOpenCameraInside){LOG.i("CeruleanWhisper---openCamera","Attempting cameraManager.openCamera(camList[currCamId], stateCallback, null)...");}
      cameraManager.openCamera(camList[currCamId], stateCallback, null);
      if(dbgDebug&&dbgWatch&&dbgOpenCamera&&dbgOpenCameraInside){LOG.i("CeruleanWhisper---openCamera","Done with cameraManager.openCamera(camList[currCamId], stateCallback, null).");}
     } catch (CameraAccessException e) {if(dbgDebug&&dbgWatch&&dbgOpenCamera){LOG.i("CeruleanWhisper---openCamera",String.format("Caught %s somewhere in openCamera()! (%s) Not fatal, re-throwing it as an RuntimeException and moving on.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}throw new RuntimeException(e);}
     if(dbgDebug&&dbgWatch&&dbgOpenCamera&&dbgOpenCameraOutside){LOG.i("CeruleanWhisper---openCamera","openCamera is done, exiting it.");}
    }

    protected void updatePreview()
    {
     if(dbgDebug&&dbgWatch){LOG.i("CeruleanWhisper---updatePreview","updatePreview has been called.");}
     if(null == cameraDevice) {if(dbgDebug&&dbgWatch){LOG.i("CeruleanWhisper---updatePreview","Error: cameraDevice is null! Exiting the function..");}return;}
     captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
     try {cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundCamHandler);} catch (CameraAccessException e) {if(dbgDebug&&dbgWatch){LOG.i("CeruleanWhisper---updatePreview",String.format("Caught %s at cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundCamHandler)! (%s) Not fatal, re-throwing it as a RuntimeException and moving on.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));} throw new RuntimeException(e);}
     if(dbgDebug&&dbgWatch){LOG.i("CeruleanWhisper---updatePreview","updatePreview is done, exiting it.");}
    }

    private void startCameraPreview()
    {
     if(dbgDebug&&dbgWatch&&dbgStartCameraPreview&&dbgStartCameraPreviewOutside){LOG.i("CeruleanWhisper---startCameraPreview","startCameraPreview has been called");}
     try {
//          setContentView(textureView);
if(dbgDebug&&dbgWatch&&dbgStartCameraPreview&&dbgStartCameraPreviewInside)
{
 LOG.w("CeruleanWhisper---startCameraPreview",String.format("View info(textureView): WindowId:%s, hasWindowFocus:%s, isActivated:%s, isAttachedToWindow:%s, isEnabled:%s, isShown %s, is itself:%s",
                                                                    String.valueOf(textureView.getWindowId()),
                                                                    String.valueOf(textureView.hasWindowFocus()),
                                                                    String.valueOf(textureView.isActivated()),
                                                                    String.valueOf(textureView.isAttachedToWindow()),
                                                                    String.valueOf(textureView.isEnabled()),
                                                                    String.valueOf(textureView.isShown()),
                                                                    textureView.toString()
                                                           ));
}
if(dbgDebug&&dbgWatch&&dbgStartCameraPreview&&dbgStartCameraPreviewInside){LOG.i("CeruleanWhisper---startCameraPreview","Attempting texture = textureView.getSurfaceTexture()...");}
      texture = textureView.getSurfaceTexture();
if(dbgDebug&&dbgWatch&&dbgStartCameraPreview&&dbgStartCameraPreviewInside){LOG.i("CeruleanWhisper---startCameraPreview",String.format("Done with texture = textureView.getSurfaceTexture(), texture: %s",String.valueOf(texture)));}
if(dbgDebug&&dbgWatch&&dbgStartCameraPreview&&dbgStartCameraPreviewInside){LOG.i("CeruleanWhisper---startCameraPreview",String.format("Attempting texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight()), previewSize.getWidth():%s, previewSize.getHeight():%s",String.valueOf(previewSize.getWidth()),String.valueOf(previewSize.getWidth())));}
      texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
if(dbgDebug&&dbgWatch&&dbgStartCameraPreview&&dbgStartCameraPreviewInside){LOG.i("CeruleanWhisper---startCameraPreview",String.format("Done with texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight())"));}
if(dbgDebug&&dbgWatch&&dbgStartCameraPreview&&dbgStartCameraPreviewInside){LOG.i("CeruleanWhisper---startCameraPreview",String.format("Attempting captureRequestBuilder  = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW), CameraDevice.TEMPLATE_PREVIEW:%s",String.valueOf(CameraDevice.TEMPLATE_PREVIEW)));}
      captureRequestBuilder  = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
if(dbgDebug&&dbgWatch&&dbgStartCameraPreview&&dbgStartCameraPreviewInside){LOG.i("CeruleanWhisper---startCameraPreview",String.format("Attempting done with captureRequestBuilder  = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW), captureRequestBuilder:%s",String.valueOf(captureRequestBuilder)));}
      surface                = new Surface(texture);
      captureRequestBuilder.addTarget(surface);
      cameraDevice.createCaptureSession(Arrays.asList(surface),
                                        new CameraCaptureSession.StateCallback() 
                                            {
                                             @Override public void onConfigured(CameraCaptureSession ccs) {if (cameraDevice == null || !textureView.isAvailable() || previewSize == null) {return;} cameraCaptureSession = ccs; if(dbgDebug&&dbgWatch&&dbgStartCameraPreview&&dbgStartCameraPreviewInside){LOG.i("CeruleanWhisper---startCameraPreview","onConfigured has fired - executing updatePreview.");} updatePreview();}
                                             @Override public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {if(dbgDebug&&dbgWatch&&dbgStartCameraPreview&&dbgStartCameraPreviewInside){LOG.i("CeruleanWhisper---startCameraPreview","onConfigureFailed has fired. Doing nothing.");}}
                                            },
                                        null);
     } catch (CameraAccessException e) {
                         if(dbgDebug&&dbgWatch&&dbgStartCameraPreview){LOG.i("CeruleanWhisper---startCameraPreview",String.format("Caught %s somewhere inside startCameraPreview! (%s) Not fatal, loggin it and moving on.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}
                                       }
     if(dbgDebug&&dbgWatch&&dbgStartCameraPreview&&dbgStartCameraPreviewOutside){LOG.i("CeruleanWhisper---startCameraPreview","startCameraPreview is done, exiting it.");}
    }


    /*Cam-related action (the method, that gets called from Cordova):*/
    /*Ideally uses camera2, captures smallest possible size to Allocate surface where it gets processed by RenderScript to get, say, average brightness, which gets stored together with timestamp to be analysed for controll patterns later*/
    /*Works mostly in background*/
    private void blinkWatch(JSONArray args, CallbackContext callbackContext) 
    {
     JSONObject arg;
     boolean pic = false, stats = false;
     String res;
     if(dbgDebug&&dbgWatch){LOG.i("CeruleanWhisper---blinkWatch","blinkWatch has beem called.");}
     try {arg  = new JSONObject(args.getString(0));} catch(JSONException e) {if(dbgDebug&&dbgWatch){LOG.i("CeruleanWhisper---blinkWatch",String.format("Caught %s at JSONObject(args.getString(0))! (%s) Not fatal, re-throwing it as a RuntimeException and moving on.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}throw new RuntimeException(e.getMessage());}
     try {pic = arg.getBoolean("pic");}              catch(JSONException e) {if(dbgDebug&&dbgWatch){LOG.i("CeruleanWhisper---blinkWatch",String.format("Caught %s at arg.getBoolean(\"pic\")! (%s) Not fatal, re-throwing it as a RuntimeException and moving on.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}throw new RuntimeException(e.getMessage());}
     try {stats = arg.getBoolean("stats");}          catch(JSONException e) {if(dbgDebug&&dbgWatch){LOG.i("CeruleanWhisper---blinkWatch",String.format("Caught %s at arg.getBoolean(\"stats\")! (%s) Not fatal, re-throwing it as a RuntimeException and moving on.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}throw new RuntimeException(e.getMessage());}

     //openCamera();

     res = String.format("End of the watch process...{pic:%s,stats:%s}",String.valueOf(pic),String.valueOf(stats));
     callbackContext.success(res);
     if(dbgDebug&&dbgWatch){LOG.i("CeruleanWhisper---blinkWatch","blinkWatch is done, exitting it.");}
    }

//|===End of cam related stuff:======================================================================================================================================================|
//|==================================================================================================================================================================================|



}

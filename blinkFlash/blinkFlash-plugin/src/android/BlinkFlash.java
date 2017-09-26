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
package cw.swcore.ceruleanplugins.blinkflash;

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




public class BlinkFlash extends CordovaPlugin {
    public static final String CAMERA                  = Manifest.permission.CAMERA;
    public static final int    SEARCH_REQ_CODE         = 0;
    public static final int    PERMISSION_DENIED_ERROR = 20;
    private CameraManager                      cameraManager;
    private String[]                           camList;
    private CameraCharacteristics              currCamParam;
    private int                                currCamId = 0;
    private boolean                            isFlashOn;
    private FlashCallback                      flashCallback = new FlashCallback();
    private CameraDevice                       cameraDevice;
    private CameraDevice.StateCallback         stateCallback = new CameraDevice.StateCallback(){@Override public void onOpened(CameraDevice camera){cameraDevice=camera;startCameraPreview();} @Override public void onDisconnected(CameraDevice camera){cameraDevice.close();} @Override public void onError(CameraDevice camera,int error){cameraDevice.close();cameraDevice = null;}};
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


    private boolean dbgBlinkFunc        = false;
    private boolean dbgBlinkFuncOutside = false;
    private boolean dbgBlinkFuncInside  = false;
    private boolean dbgBlinkClass        = false;
    private boolean dbgBlinkClassOutside = false;
    private boolean dbgBlinkClassInside  = false;

    private boolean dbgMisc                   = true;


/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException 
    {
     if (action.equals("blink")) {if(dbgDebug&&dbgPlugin){LOG.i("blinkFlash---execute","Calling blink.");}this.blink(args, callbackContext); return true;}
     return false;
    }

    @Override
    public void pluginInitialize() {
        LOG.setLogLevel(LOG.INFO);
        if(dbgDebug&&dbgPlugin&&dbgPluginInitialize){LOG.i("blinkFlash---pluginInitialize","Logging is on. LOGLEVEL has just been set to INFO. Starting plugin initialization.");}
        if(dbgDebug&&dbgPlugin&&dbgPluginInitialize&&dbgPluginInitializeOutside){LOG.i("blinkFlash---pluginInitialize","Checking for CAMERA permission... Currently it is to %s",String.valueOf(cordova.hasPermission(CAMERA)));}
        if (!cordova.hasPermission(CAMERA)) {cordova.requestPermission(this, SEARCH_REQ_CODE, CAMERA);}
        if(dbgDebug&&dbgPlugin&&dbgPluginInitialize&&dbgPluginInitializeInside){LOG.i("blinkFlash---pluginInitialize","Checking for CAMERA permission AFTER the request... Currently it is to %s",String.valueOf(cordova.hasPermission(CAMERA)));}
        getProperCamera();
        setupFlash();
        if(dbgDebug&&dbgPlugin&&dbgPluginInitialize&&dbgPluginInitializeOutside){LOG.i("blinkFlash---pluginInitialize","Plugin initialisation complete.");}
    }

    @Override public void onRequestPermissionResult(int requestCode, String[] permissions,int[] grantResults) throws JSONException {if(dbgDebug&&dbgPlugin){LOG.i("blinkFlash---pluginInitialize","And checking for CAMERA permission for the THIRD time, as a reaction to RequestPermissionResult event... Currently it is to %s",String.valueOf(cordova.hasPermission(CAMERA)));}}

    /*======Cordovas' android lifecycle hooks:======*/
    @Override public void onDestroy ()                      {super.onDestroy();            if((cameraManager!=null)&&(flashCallback!=null)) {cameraManager.unregisterTorchCallback(flashCallback);}}
    @Override public void onReset   ()                      {super.onReset();              if((cameraManager!=null)&&(flashCallback!=null)) {cameraManager.unregisterTorchCallback(flashCallback);}}
    @Override public void onPause   (boolean multitasking)  {super.onPause(multitasking);  if((cameraManager!=null)&&(flashCallback!=null)) {cameraManager.unregisterTorchCallback(flashCallback);}}
    @Override public void onResume  (boolean multitasking)  {super.onResume(multitasking); if((cameraManager!=null)&&(flashCallback!=null)) {try{cameraManager.registerTorchCallback(flashCallback,null);} catch(IllegalArgumentException e) {if(dbgDebug&&dbgPlugin){LOG.i("blinkFlash---onResume",String.format("Caught %s at cameraManager.registerTorchCallback(flashCallback,null)! (%s) Not fatal, re-throwing it as an RuntimeException and moving on.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}throw new RuntimeException(e);}}}
/*|===End of plugin related stuff:=================================================================================================|*/
/*|================================================================================================================================|*/

/*|==================================================================================================================================================================================|*/
    class FlashCallback extends CameraManager.TorchCallback
    {
     @Override public void onTorchModeUnavailable(String cameraId) {super.onTorchModeUnavailable(cameraId );if(dbgDebug&&dbgFlash&&dbgMisc){LOG.i("blinkFlash---FlashCallback.onTorchModeUnavailable",String.format("FlashCallBack.onTorchModeUnavailable has been called. CameraID: %s",cameraId));}}
     @Override public void onTorchModeChanged(String cameraId, boolean enabled) {super.onTorchModeChanged(cameraId, enabled); if(dbgDebug&&dbgFlash&&dbgMisc){LOG.i("blinkFlash---FlashCallback.onTorchModeChanged",String.format("FlashCallBack.onTorchModeChanged has been called. CameraID: %s, Enabled: %s",cameraId,String.valueOf(enabled)));}}
    }
    private void   getProperCamera()         {if(dbgDebug&&(dbgFlash||dbgWatch)&&dbgMisc){LOG.i("blinkFlash---getProperCamera","getProperCamera has been called.");}if (cameraManager == null) {try {cameraManager = webView.getContext().getSystemService(CameraManager.class); camList=cameraManager.getCameraIdList();for (currCamId=0;currCamId<camList.length;currCamId++) {currCamParam=cameraManager.getCameraCharacteristics(camList[currCamId]);if (currCamParam.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {break;}}} catch(CameraAccessException e) {if(dbgDebug&&(dbgFlash||dbgWatch)&&dbgMisc){LOG.i("blinkFlash---getProperCamera",String.format("Caught %s somewhere in getProperCamera()! (%s) Not fatal, re-throwing it as an RuntimeException and moving on.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}throw new RuntimeException(e);} catch(IllegalArgumentException e) {if(dbgDebug&&(dbgFlash||dbgWatch)&&dbgMisc){LOG.i("blinkFlash---getProperCamera",String.format("Caught %s somewhere in getProperCamera()! (%s) Not fatal, re-throwing it as an RuntimeException and moving on.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}throw new RuntimeException(e);}}}
    private void   setupFlash()              {if(dbgDebug&&dbgFlash&&dbgMisc){LOG.i("blinkFlash---setupFlash","setupFlash has been called.");}try{cameraManager.registerTorchCallback(flashCallback,null);} catch(IllegalArgumentException e){if(dbgDebug&&dbgFlash&&dbgMisc){LOG.i("blinkFlash---setupFlash",String.format("Caught %s somewhere in setupFlash()! (%s) Not fatal, re-throwing it as an RuntimeException and moving on.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}throw new RuntimeException(e);}}
    private String composeCurrCamStatusMsg() {if(dbgDebug&&(dbgFlash||dbgWatch)&&dbgMisc){LOG.i("blinkFlash---composeCurrCamStatusMsg","composeCurrCamStatusMsg has been called.");}String facing,isFlashAvailable;switch (currCamParam.get(CameraCharacteristics.LENS_FACING)){case CameraCharacteristics.LENS_FACING_BACK:facing="back";break;case CameraCharacteristics.LENS_FACING_FRONT:facing="front"; break;case CameraCharacteristics.LENS_FACING_EXTERNAL:facing="external";break;default:facing="none";}isFlashAvailable = String.valueOf(currCamParam.get(CameraCharacteristics.FLASH_INFO_AVAILABLE));return String.format("Camera with id:%d of %d [0 - %d], is selected. It is facing %s, flash exists: %s.",camList[currCamId],camList.length,camList.length-1,facing,isFlashAvailable);}
    private void blink(JSONArray args, CallbackContext callbackContext)
    {
     String argS; JSONArray arg; int argLen; Thread blinkThread;
     if(dbgDebug&&dbgFlash&&dbgBlinkFunc&&dbgBlinkFuncOutside){LOG.i("blinkFlash---blink","blink has been called.");}
     class Blink implements Runnable
     {
       private CameraManager   cameraManager;   private String currCamIdStr;                           private int       argLen;
       private CallbackContext callbackContext; private String successMsg = "End of the flash run..."; private JSONArray arg;
       public Blink(CameraManager par_camera, String par_currCamIdStr, JSONArray par_args, CallbackContext par_callbackContext) throws Exception /*0 = constructor*/
       {
        this.cameraManager=par_camera; this.currCamIdStr=par_currCamIdStr; this.callbackContext=par_callbackContext;
        try{this.arg=new JSONArray(par_args.getString(0));} 
        catch(JSONException e) {
                      if(dbgDebug&&dbgFlash&&dbgBlinkClass){LOG.i("blinkFlash---Blink.constructor",String.format("Caught %s at JSONArray(par_args.getString(0))! (%s) Exiting the function...",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}
                               }
        this.argLen = 2*((int)this.arg.length()/2);
        if (this.argLen<2) {
                            if(dbgDebug&&dbgFlash&&dbgBlinkClass){LOG.i("blinkFlash---Blink.constructor",String.format("Error! arg array has less than 2 elements (%s).",String.valueOf(this.arg)));}
                            return;
                           }
       }
       /*1 = dealWithExc*/
       private void dealWithExc(Exception e)                          {String excTxt = String.format("Exception! msg:'%s' (cause:'%s')",e.getMessage(),((e.getCause()==null)?"none":e.getCause().getMessage()));
                                                                       if(dbgDebug&&dbgFlash&&dbgBlinkClass){LOG.i("blinkFlash---Blink.dealWithExc",String.format("Caught %s somewhere at Blink.run()! (%s).",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}
                                                                       this.callbackContext.error(excTxt);
                                                                      }
       /*2 = flashOff*/
       private void flashOff   (String currCamIdStr) throws Exception {try {this.cameraManager.setTorchMode(currCamIdStr,false);} 
                                                                       catch(CameraAccessException e) {
                                                                          if(dbgDebug&&dbgFlash&&dbgBlinkClass){LOG.i("blinkFlash---Blink.flashOff",String.format("Caught %s at this.cameraManager.setTorchMode(currCamIdStr,false)! (%s) Not fatal, logging it and moving on.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}
                                                                                                      } 
                                                                       catch(IllegalArgumentException e) {
                                                                          if(dbgDebug&&dbgFlash&&dbgBlinkClass){LOG.i("blinkFlash---Blink.flashOff",String.format("Caught %s at this.cameraManager.setTorchMode(currCamIdStr,false)! (%s) Not fatal, logging it and moving on.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}
                                                                                                         }
                                                                      }
       /*3 = flashOn*/
       private void flashOn    (String currCamIdStr) throws Exception {try {this.cameraManager.setTorchMode(currCamIdStr,true);} 
                                                                       catch(CameraAccessException e) {
                                                                              if(dbgDebug&&dbgFlash&&dbgBlinkClass){LOG.i("blinkFlash---Blink.flashOn",String.format("Caught %s at this.cameraManager.setTorchMode(currCamIdStr,true)! (%s) Not fatal, logging it and moving on.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}
                                                                              throw new Exception("flashOn method failed",e);
                                                                                                      } 
                                                                       catch(IllegalArgumentException e) {
                                                                              if(dbgDebug&&dbgFlash&&dbgBlinkClass){LOG.i("blinkFlash---Blink.flashOn",String.format("Caught %s at this.cameraManager.setTorchMode(currCamIdStr,true)! (%s) Not fatal, re-throwing it as an Exception and moving on.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}
                                                                                                         }
                                                                      }
       /*4 = pause*/
       private void pause      (int i)               throws Exception {int ms=0; try{ms=this.arg.getInt(i);} 
                                                                                 catch(JSONException e) {
                                                                                    if(dbgDebug&&dbgFlash&&dbgBlinkClass){LOG.i("blinkFlash---Blink.pause",String.format("Caught %s at ms=this.arg.getInt(i)! (%s) Not fatal, logging it and moving on.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}
                                                                                                        } 
                                                                                 try {Thread.sleep(ms);} 
                                                                                 catch(IllegalArgumentException e) {
                                                                                    if(dbgDebug&&dbgFlash&&dbgBlinkClass){LOG.i("blinkFlash---Blink.pause",String.format("Caught %s at Thread.sleep(ms)! (%s) Not fatal, logging it and moving on.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}
                                                                                                                   } 
                                                                                 catch(InterruptedException e) {
                                                                                    if(dbgDebug&&dbgFlash&&dbgBlinkClass){LOG.i("blinkFlash---Blink.pause",String.format("Caught %s at Thread.sleep(ms)! (%s) Not fatal, logging it moving on.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}
                                                                                                               }
                                                                      }
       /*5 = run*/
       public  void run        ()                                     {if(dbgDebug&&dbgFlash&&dbgBlinkClass&&dbgBlinkClassInside){LOG.i("blinkFlash---Blink.run","Blink.run has been launched in a separate thread.");}
                                                                       try {this.flashOff(this.currCamIdStr);
                                                                            for(int i=0;i<this.argLen;i+=2) {this.flashOn(this.currCamIdStr);this.pause(i);this.flashOff(this.currCamIdStr);this.pause(i+1);} 
                                                                            this.flashOff(this.currCamIdStr);
                                                                            if(dbgDebug&&dbgFlash&&dbgBlinkClass&&dbgBlinkClassOutside){LOG.i("blinkFlash---Blink.run","Blink.run is done, thread is finishing.");} 
                                                                            this.callbackContext.success(this.successMsg);
                                                                           }
                                                                       catch (Exception e) {this.dealWithExc(e);return;} 
                                                                      }
     }
     try {blinkThread = new Thread( new Blink(cameraManager,camList[currCamId],args,callbackContext) );
          blinkThread.start();
         } 
     catch(IllegalThreadStateException e) {
                if(dbgDebug&&dbgFlash&&dbgBlinkFunc){LOG.i("blinkFlash---blink",String.format("Caught %s at blinkThread.start()! (%s) Not fatal, logging it and moving on.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}
                                          }
     catch(Exception e) {
                if(dbgDebug&&dbgFlash&&dbgBlinkFunc){LOG.i("blinkFlash---blink",String.format("Caught %s at blinkThread.start()! (%s) Not fatal, exiting blink.",e.toString().substring(0,e.toString().indexOf(':')),e.getMessage()));}
                String excTxt = String.format("Exception in blink()! msg:'%s' (cause:'%s')",e.getMessage(),((e.getCause()==null)?"none":e.getCause().getMessage()));
                callbackContext.error(excTxt);
                return;
                        }
     if(dbgDebug&&dbgFlash&&dbgBlinkFunc&&dbgBlinkFuncOutside){LOG.i("blinkFlash---blink","blink is done, exiting it.");}
    }


//|===End of cam related stuff:======================================================================================================================================================|
//|==================================================================================================================================================================================|



}

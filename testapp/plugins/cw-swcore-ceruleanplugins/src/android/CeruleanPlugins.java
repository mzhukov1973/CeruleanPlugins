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
    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {@Override public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height){openCamera();} @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height){} @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface){return false;} @Override public void onSurfaceTextureUpdated(SurfaceTexture surface){}};
    private SurfaceTexture                     texture;
    private CaptureRequest                     captureRequest;
    private CaptureRequest.Builder             captureRequestBuilder;
    private Surface                            surface;
    private CameraCaptureSession               cameraCaptureSession;
    private Handler                            backgroundCamHandler;
    private HandlerThread                      backgroundCamThread;



    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException 
    {
     if      (action.equals("myMethod"))   {String message = args.getString(0); this.myMethod(message, callbackContext); return true;}
     else if (action.equals("blinkFlash")) {this.blinkFlash(args, callbackContext); return true;}
     else if (action.equals("blinkWatch")) {this.blinkWatch(args, callbackContext); return true;}
     return false;
    }

    @Override
    public void pluginInitialize() {
        if (!cordova.hasPermission(CAMERA)) {cordova.requestPermission(this, SEARCH_REQ_CODE, CAMERA);}
        getProperCamera();                                             /*blinkFlash, blinkWatch*/
        setupFlash();                                                  /*blinkFlash, blinkWatch*/
        startBackgroundCamThread();                                    /*            blinkWatch*/
        textureView = new TextureView(webView.getContext());           /*            blinkWatch*/
        textureView.setSurfaceTextureListener(surfaceTextureListener); /*            blinkWatch*/
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
    @Override public void onResume  (boolean multitasking)  {super.onResume(multitasking); if((cameraManager!=null)&&(flashCallback!=null)) {try{cameraManager.registerTorchCallback(flashCallback,null);} catch(IllegalArgumentException e) {throw new RuntimeException(e);}}}
/*|===End of plugin related stuff:=================================================================================================|*/
/*|================================================================================================================================|*/


/*|================================================================================================================================|*/
/*|===Echo related stuff:==========================================================================================================|*/
    /*Echo action (the method, that gets called from Cordova):*/
    private void myMethod(String message, CallbackContext callbackContext) 
    {
     if (message == null || message.length() == 0) {callbackContext.error("Expected one non-empty string argument.");return;}
     Toast.makeText(webView.getContext(), message, Toast.LENGTH_LONG).show();
     callbackContext.success(message);
    }
/*|===End of echo related stuff:===================================================================================================|*/
/*|================================================================================================================================|*/



/*|==================================================================================================================================================================================|*/
/*|===Cam related stuff:=============================================================================================================================================================|*/
    class FlashCallback extends CameraManager.TorchCallback
    {
     @Override public void onTorchModeUnavailable(String cameraId) {super.onTorchModeUnavailable(cameraId); /*Log.e("onTorchModeUnavailable", "CameraID:" + cameraId);*/}
     @Override public void onTorchModeChanged(String cameraId, boolean enabled) {super.onTorchModeChanged(cameraId, enabled); /*Log.e("onTorchModeChanged", "CameraID:"+cameraId+" TorchMode : "+enabled);*/}
    }
    private void   getProperCamera()         {if (cameraManager == null) {try {cameraManager = webView.getContext().getSystemService(CameraManager.class); camList=cameraManager.getCameraIdList();for (currCamId=0;currCamId<camList.length;currCamId++) {currCamParam=cameraManager.getCameraCharacteristics(camList[currCamId]);if (currCamParam.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {break;}}} catch(CameraAccessException e)    {throw new RuntimeException(e);} catch(IllegalArgumentException e) {throw new RuntimeException(e);}}}
    private void   setupFlash()              {try{cameraManager.registerTorchCallback(flashCallback,null);} catch(IllegalArgumentException e){throw new RuntimeException(e);}}
    private String composeCurrCamStatusMsg() {String facing,isFlashAvailable;switch (currCamParam.get(CameraCharacteristics.LENS_FACING)){case CameraCharacteristics.LENS_FACING_BACK:facing="back";break;case CameraCharacteristics.LENS_FACING_FRONT:facing="front"; break;case CameraCharacteristics.LENS_FACING_EXTERNAL:facing="external";break;default:facing="none";}isFlashAvailable = String.valueOf(currCamParam.get(CameraCharacteristics.FLASH_INFO_AVAILABLE));return String.format("Camera with id:%d of %d [0 - %d], is selected. It is facing %s, flash exists: %s.",camList[currCamId],camList.length,camList.length-1,facing,isFlashAvailable);}

    private void blinkFlash(JSONArray args, CallbackContext callbackContext) 
    {
     String argS; JSONArray arg; int argLen; Thread blinkThread;
     class BlinkFlash implements Runnable
     {
       private CameraManager   cameraManager;   private String currCamIdStr;                           private int       argLen;
       private CallbackContext callbackContext; private String successMsg = "End of the flash run..."; private JSONArray arg;
       public BlinkFlash(CameraManager par_camera, String par_currCamIdStr, JSONArray par_args, CallbackContext par_callbackContext) throws Exception
       {
        this.cameraManager=par_camera; this.currCamIdStr=par_currCamIdStr; this.callbackContext=par_callbackContext;
        try{this.arg=new JSONArray(par_args.getString(0));} catch(JSONException e) {throw new Exception("BlinkFlash instantiation failed",e);}
        this.argLen = 2*((int)this.arg.length()/2);
        if (this.argLen<2) {throw new Exception(String.format("arg array has less than 2 elements. (%s)",String.valueOf(this.arg)));}
       }
       private void dealWithExc(Exception e)                          {String excTxt = String.format("Exception! msg:'%s' (cause:'%s')",e.getMessage(),((e.getCause()==null)?"none":e.getCause().getMessage()));this.callbackContext.error(excTxt);}
       private void flashOff   (String currCamIdStr) throws Exception {try {this.cameraManager.setTorchMode(currCamIdStr,false);} catch(CameraAccessException e) {throw new Exception("flashOff method failed",e);} catch(IllegalArgumentException e) {throw new Exception("flashOff method failed",e);}}
       private void flashOn    (String currCamIdStr) throws Exception {try {this.cameraManager.setTorchMode(currCamIdStr,true);} catch(CameraAccessException e) {throw new Exception("flashOn method failed",e);} catch(IllegalArgumentException e) {throw new Exception("flashOn method failed",e);}}
       private void pause      (int i)               throws Exception {int ms=0; try{ms=this.arg.getInt(i);} catch(JSONException e) {throw new Exception("pause method failed",e);} try {Thread.sleep(ms);} catch(IllegalArgumentException e) {throw new Exception("Thread.sleep failed",e);} catch(InterruptedException e) {throw new Exception("Thread.sleep failed",e);}}
       public  void run        ()                                     {try {this.flashOff(this.currCamIdStr);for(int i=0;i<this.argLen;i+=2) {this.flashOn(this.currCamIdStr);this.pause(i);this.flashOff(this.currCamIdStr);this.pause(i+1);}this.flashOff(this.currCamIdStr);this.callbackContext.success(this.successMsg);} catch (Exception e) {this.dealWithExc(e);return;} }
     }
     try {blinkThread = new Thread( new BlinkFlash(cameraManager,camList[currCamId],args,callbackContext) );try {blinkThread.start();} catch(IllegalThreadStateException e) {throw new Exception("blinkThread.start() failed",e);}} catch(Exception e) {String excTxt = String.format("Exception in blinkFlash()! msg:'%s' (cause:'%s')",e.getMessage(),((e.getCause()==null)?"none":e.getCause().getMessage()));callbackContext.error(excTxt);Toast.makeText(webView.getContext(), excTxt, Toast.LENGTH_LONG).show();return;}
    }

/*======Now cam-capture per se:======*/
    private void startBackgroundCamThread () {backgroundCamThread=new HandlerThread("Background Camera Thread");backgroundCamThread.start();backgroundCamHandler=new Handler(backgroundCamThread.getLooper());}
    private void stopBackgroundCamThread  () {backgroundCamThread.quitSafely();try {backgroundCamThread.join();backgroundCamThread=null;backgroundCamHandler=null;} catch (InterruptedException e) {throw new RuntimeException(e);}}

    private void openCamera()
    {
     try
     {
      streamConfigurationMap = currCamParam.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
      previewSize = streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0];
      if (!cordova.hasPermission(CAMERA)) {return;} /*Just a final check here. Permissions are usually checked and requested in initialize method if the plugin.*/
      cameraManager.openCamera(camList[currCamId], stateCallback, null);
     } catch (CameraAccessException e) {throw new RuntimeException(e);}
    }

    protected void updatePreview()
    {
     if(null == cameraDevice) { /*Log.e(TAG, "updatePreview error, return");*/ }
     captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
     try {cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundCamHandler);} catch (CameraAccessException e) {throw new RuntimeException(e);}
    }

    private void startCameraPreview()
    {
     try {
      texture = textureView.getSurfaceTexture();
      texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
      captureRequestBuilder  = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
      surface                = new Surface(texture);
      captureRequestBuilder.addTarget(surface);
      cameraDevice.createCaptureSession(Arrays.asList(surface),
                                        new CameraCaptureSession.StateCallback() 
                                            {
                                             @Override public void onConfigured(CameraCaptureSession ccs) {if (cameraDevice == null || !textureView.isAvailable() || previewSize == null) {return;} cameraCaptureSession = ccs; Toast.makeText(webView.getContext(), "onConfigured()", Toast.LENGTH_SHORT).show(); updatePreview();}
                                             @Override public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {Toast.makeText(webView.getContext(), "Configuration change", Toast.LENGTH_SHORT).show();}
                                            },
                                        null);
     } catch (CameraAccessException e) {throw new RuntimeException(e);}
    }


    /*Cam-related action (the method, that gets called from Cordova):*/
    /*Ideally uses camera2, captures smallest possible size to Allocate surface where it gets processed by RenderScript to get, say, average brightness, which gets stored together with timestamp to be analysed for controll patterns later*/
    /*Works mostly in background*/
    private void blinkWatch(JSONArray args, CallbackContext callbackContext) 
    {
     JSONObject arg;
     boolean pic = false, stats = false;
     String res;
     Toast.makeText(webView.getContext(), "blinkWatch called...", Toast.LENGTH_LONG).show();
     try {arg  = new JSONObject(args.getString(0));} catch(JSONException e) {throw new RuntimeException(e.getMessage());}
     try {pic = arg.getBoolean("pic");}              catch(JSONException e) {throw new RuntimeException(e.getMessage());}
     try {stats = arg.getBoolean("stats");}          catch(JSONException e) {throw new RuntimeException(e.getMessage());}

     openCamera();

     res = String.format("End of the watch process...{pic:%s,stats:%s}",String.valueOf(pic),String.valueOf(stats));
     Toast.makeText(webView.getContext(), res, Toast.LENGTH_LONG).show();
     callbackContext.success(res);
    }

//|===End of cam related stuff:======================================================================================================================================================|
//|==================================================================================================================================================================================|



}

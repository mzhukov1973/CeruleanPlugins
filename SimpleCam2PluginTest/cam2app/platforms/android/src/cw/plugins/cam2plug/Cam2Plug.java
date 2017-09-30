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

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.LOG;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.hardware.camera2.CameraManager;



public class Cam2Plug extends CordovaPlugin {

 private String TAG = "Cam2Plug:";

 @Override public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
  if (action.equals("coolMethod"))      {String message = args.getString(0);this.coolMethod(message, callbackContext);return true;}
  else if (action.equals("startVideo")) {this.startVideo(callbackContext);return true;}
  else if (action.equals("stopVideo"))  {this.stopVideo(callbackContext); return true;}
  return false;
 }

 @Override public void pluginInitialize() {
  LOG.setLogLevel(LOG.INFO);
  LOG.i(TAG, "[pluginInitialize] Logging is on. LOGLEVEL has been set to INFO.");
 }
/*================================================================================================================================*/


/*=======================================Actions (i.e. methods that ar callable from JS) are defined below here===================*/
 private void coolMethod(String message, CallbackContext callbackContext) {
  if (message != null && message.length() > 0) {callbackContext.success(message);                                }
  else                                         {callbackContext.error("Expected one non-empty string argument.");}
 }

 private void startVideo(CallbackContext callbackContext) {
  boolean result = false;
  String reason = "No code in startVideo yet.";
  String message = "startVideo worked!";
  LOG.i(TAG, "[startVideo] Method called. Firing up the video...");
//............
  if (result) {callbackContext.success(message);} else {callbackContext.error(reason);}
 }

 private void stopVideo(CallbackContext callbackContext) {
  boolean result = false;
  String reason = "No code in stopVideo yet.";
  String message = "stopVideo worked!";
  LOG.i(TAG, "[stopVideo] Method called. Stopping up the video and tidying up...");
//............
  if (result) {callbackContext.success(message);} else {callbackContext.error(reason);}
 }

}

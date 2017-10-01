# CeruleanPlugins Cordova-Android Camera2 test plugin/app
##### Version: 0.0.2-alpha1

### ToDo:
##### 0.0.0
- [x] ~~Two clean apps - an app and a plugin that is, that build nicely together with no warnings and all according to best practices, as far as they can be fathomed in this case.~~
##### 0.0.1
- [x] ~~Apps' initialisation should be two-staged - the second one deferred until CAMERA access permission is obtained.~~
- [x] ~~Until the deferred part of the initialisation is complete app should function normally, except for the specific functions that require CAMERA access.~~
- [x] ~~When attempting to make use of these CAMERA-related functions before the app is fully initialised, it should fail gracefully, infroming the user about the necessity of granting it CAMERA permissions in order for these functions to become available.~~
- [x] ~~Do not just poll for the change in permissions - react to it as it happens, running the deferred part of the initialisation at once.~~
- [x] ~~*(phase 1: when there's no actual HW resources invloved yet)* Implement proper reaction to Pause,Resume,Start,Stop,Message and Destroy events - especially concerning relinquishing and reacquisitioning hardware resources.~~
- [x] ~~Expose to JS a method to query the initialisation state of the plugin.~~
- [ ] Arrange for the JS part of the app to be able to just listen on an event (as is with the DeviceReady one) to be notified of changes plugins' initialisation state.
- [x] ~~Ignore anything related to old CAMERA api, got exclusively for CAMERA2.~~
- [ ] Redo the semantics of `onCreate`, `onRestart`, `onStart`, `onResume`, `onPause`, `onStop` and `onDestroy` event handlers, taking into account the material from Android developers' guide article on `Activity` class.
###### 0.0.2
- [ ] *(phase 2: when there are **some** resources to acquire/relinquish)* Implement proper reaction to Pause,Resume,Start,Stop,Message and Destroy events - especially concerning relinquishing and reacquisitioning hardware resources.
- [ ] Gradually add the actual CeruleanWhisper functionality to the plugin, testing it in the process.
- [ ] Both image-acquisition and image-analysis threads should be background threads, not hampering the UI in any way.
- [ ] Image stream should be set to lowest resolution possible to ease hardware load and increase FPS.
- [ ] JS should be continuously notified of the state of the observation task, as per the specification, until the observation mode is switched off (messages?).

### Miscellaneous

#### Creating Java=>JS communication channel:

   For Android (N.B.! works only from CordovaActivity):

       this.appView.loadUrl("javascript:yourmethodname());"); // Where yourmethodname() is the js function you want to call in webView.

   So, to be called from CordovaPlugin it has to look like this:

       this.cordova.getActivity().appView.loadUrl("javascript:yourmethodname());");
 or
       cordova.getActivity().appView.loadUrl("javascript:yourmethodname());");

   Interesting... Why go through CordovaActivity and not go directly through webView, since we already have it locally in CordovaPlugin?..
 E.g. something along these lines:

       webView.loadUrlIntoView("javascript:yourmethodname());",true);
 or
       this.webView.loadUrlIntoView("javascript:yourmethodname());",true);
-------
   Another method (works anywhere in your java code though requires a tad more work to set up and use):

 1. Create a private CallbackContext in your CordovaPlugin.
 2. Store there the CallbackContext, supplied to you from JS via the exec() method.
 3. Anywhere else in Java code you may use it to send a PluginResult back to JS.
 N.B.! the callback will become invalid after it gets triggered unless you set the KeepCallback of the PluginResult you are sending to true.

 (1) private CallbackContext callbackContext;
      ...
     public boolean execute(final String action, JSONArray args, CallbackContext callbackContext) throws JSONException
 (2)  { ... this.callbackContext = callbackContext; ... }
      ...
 (3) PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, "WHAT");
     pluginResult.setKeepCallback(true);
     callbackContext.sendPluginResult(pluginResult);
-------
   Seemingly definitive way (from a comment in CordovaWebView.java):

   Instead of executing snippets of JS, you should use the exec bridge to create a Java->JS communication channel.

   To do this:
 1. Within plugin.xml (to have your JS run before deviceready):
      <js-module><runs/></js-module>
 2. Within your .js (call exec on start-up):
      require('cordova/channel').onCordovaReady.subscribe(function() {
require('cordova/exec')(win, null, 'Plugin', 'method', []);
function win(message) {
  ... process message from java here ...
}
      });
 3. Within your .java:
      PluginResult dataResult = new PluginResult(PluginResult.Status.OK, CODE);
      dataResult.setKeepCallback(true);
      savedCallbackContext.sendPluginResult(dataResult);

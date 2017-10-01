# CeruleanPlugins Cordova-Android Camera2 test plugin/app
##### Version: 0.0.2-alpha1

## ToDo:
##### 0.0.0
- [x] ~~Two clean apps - an app and a plugin that is, that build nicely together with no warnings and all according to best practices, as far as they can be fathomed in this case.~~
##### 0.0.1
- [x] ~~Apps' initialisation should be two-staged - the second one deferred until CAMERA access permission is obtained.~~
- [x] ~~Until the deferred part of the initialisation is complete, app should function normally, except for the specific functions that require CAMERA access.~~
- [x] ~~When attempting to make use of these CAMERA-related functions before the app is fully initialised, it should fail gracefully, informing the user about the necessity of granting it CAMERA permissions in order for these functions to become available.~~
- [x] ~~Do not just poll for the change in permissions - react to it as it happens, running the deferred part of the initialisation at once.~~
- [x] ~~*(phase<sup>1</sup>: when there're no actual hardware resources invloved yet)* Implement proper reaction to Pause,Resume,Start,Stop,Message and Destroy events - especially concerning relinquishing and reacquisitioning hardware resources.~~
- [x] ~~Expose to **js** a method to query the initialisation state of the plugin.~~
- [ ] Arrange for the **js** part of the app to be able to just listen on an event (as is with the `deviceready` one) to be notified of changes to plugins' initialisation state.
- [x] ~~Ignore anything related to old CAMERA api, got exclusively for CAMERA2.~~
- [ ] Redo the semantics of `onRestart`, `onStart`, `onResume`, `onPause`, `onStop` and `onDestroy` event handlers, taking into account the material from Android developers' guide [article](https://developer.android.com/reference/android/app/Activity.html#ActivityLifecycle) on `Activity` class.
###### 0.0.2
- [ ] *(phase<sup>2</sup>: when there **are** some resources to acquire/relinquish)* Implement proper reaction to `Restart`, `Start`, `Resume`, `Pause`, `Stop` and `Destroy` events - especially concerning relinquishing and reacquisitioning hardware resources.
- [ ] Gradually add the actual **CeruleanWhisper** functionality to the plugin, testing it in the process.
- [ ] Both image-acquisition and image-analysis threads should be background threads, not hampering the UI in any way.
- [ ] Image stream should be set to lowest resolution possible to ease hardware load and increase FPS.
- [ ] **js** should be continuously notified of the state of the observation task, as per the specification, until the observation mode is switched off (~~messages?~~ arrange it through normal callbacks/syntetic **js** events).


## Miscellaneous

### Creating Java&#x2794;js communication channel:
#### The simplest, shortest method with practicaly no set-up required:
 <sup>**N.B.!**</sup>*Works only from `CordovaActivity` and seems to be generally frowned upon for some reason.*
>```javascript
>   this.appView.loadUrl("javascript:yourmethodname());");
>   //Where yourmethodname() is the js function you want to call in webView.
>```
So, to be called from `CordovaPlugin` it has to look like this:
>```javascript
>   this.cordova.getActivity().appView.loadUrl("javascript:yourmethodname());");
>```
Interesting... Why go through `CordovaActivity` and not directly through `CordovaWebView`, since we already have it locally in `CordovaPlugin`?..
   
   E.g. something along these lines:
>```javascript
>   webView.loadUrlIntoView("javascript:yourmethodname());",true);
>```
#### Another method (works anywhere in your Java code though requires a tad more work to set up and use):
1. Create a private `CallbackContext` in your `CordovaPlugin.`
>   ```javascript
>      private CallbackContext callbackContext;
>   ```
2. Store there the `CallbackContext`, supplied to you from **js** via the `exec()` method.
>   ```javascript
>     ...
>     public boolean execute(final String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
>       ... 
>       this.callbackContext = callbackContext; 
>       ...
>     }
>     ...
>   ```
3. Anywhere else in **Java** code you may use it to send a `PluginResult` back to **js**.

   <sup>**N.B.!**</sup>The callback will become invalid after it gets triggered unless you set the `KeepCallback` of the `PluginResult` you are sending to `true`.
>   ```javascript
>      PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, "WHAT");
>      pluginResult.setKeepCallback(true);
>      callbackContext.sendPluginResult(pluginResult);
>   ```
#### Seemingly the definitive way (from a comment in `CordovaWebView.java`):
Instead of executing snippets of **js**, you should use the exec bridge to create a **Java**&#x2794;**js** communication channel.

##### To do this:
###### 1. Within `plugin.xml` (to have your js run before `deviceready`):
>   ```xml
>      <js-module>
>        <runs/>
>      </js-module>
>   ```
###### 2. Within your **`.js`** (call `exec` on start-up):
>   ```javascript
>      require('cordova/channel').onCordovaReady.subscribe(function() {
>        require('cordova/exec')(win, null, 'Plugin', 'method', []);
>        function win(message) {
>          ... process message from Java here ...
>        }
>      });
>   ```
###### 3. Within your **`.java`**:
>   ```javascript
>      PluginResult dataResult = new PluginResult(PluginResult.Status.OK, CODE);
>      dataResult.setKeepCallback(true);
>      savedCallbackContext.sendPluginResult(dataResult);
>   ```


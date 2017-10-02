# CeruleanPlugins Cordova-Android Camera2 test plugin/app
##### Version: 0.0.2

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
- [x] ~~Arrange for the **js** part of the app to be able to just listen on an event (as is with the `deviceready` one) to be notified of changes to plugins' initialisation state.~~
- [x] ~~Ignore anything related to old CAMERA api, got exclusively for CAMERA2.~~
- [x] ~~Redo the semantics of `onRestart()`, `onStart()`, `onResume()`, `onPause()`, `onStop()` and `onDestroy()` event handlers, taking into account the material from Android developers' guide [article](https://developer.android.com/reference/android/app/Activity.html#ActivityLifecycle) on `Activity` class.~~
##### 0.0.2
- [ ] *(phase<sup>2</sup>: when there **are** some resources to acquire/relinquish)* Implement proper reaction to `Restart`, `Start`, `Resume`, `Pause`, `Stop` and `Destroy` events - especially concerning relinquishing and reacquisitioning hardware resources.
- [ ] Gradually add the actual **CeruleanWhisper** functionality to the plugin, testing it in the process.
- [ ] Both image-acquisition and image-analysis threads should be background threads, not hampering the UI in any way.
- [ ] Image stream should be set to lowest resolution possible to ease hardware load and increase FPS.
- [ ] **js** should be continuously notified of the state of the observation task, as per the specification, until the observation mode is switched off (~~messages?~~ arrange it through normal callbacks/syntetic **js** events).
###### 0.0.3

## Miscellaneous

### Creating Java&#x2794;js communication channel:
#### ~~The simplest, shortest method with practicaly no set-up required:~~
<sup>~~**N.B.!**~~</sup>~~*Works only from `CordovaActivity` and seems to be generally frowned upon for some reason.*~~

>~~` 
>this.appView.loadUrl("javascript:yourmethodname());");
>/*Where yourmethodname() is the js function you want to call in webView.*/
>`~~

~~So, to be called from `CordovaPlugin` it has to look like this:~~

>~~`
>this.cordova.getActivity().appView.loadUrl("javascript:yourmethodname());");
>`~~

~~Interesting... Why go through `CordovaActivity` and not directly through `CordovaWebView`, since we already have it locally in `CordovaPlugin`?..~~

~~E.g. something along these lines:~~

>~~`
>   webView.loadUrlIntoView("javascript:yourmethodname());",true);
>`~~
#### Another method (works anywhere in your Java code though requires a tad more work to set up and use):
1. Create a private `CallbackContext` in your `CordovaPlugin.`
>   ```java
>      private CallbackContext callbackContext;
>   ```
2. Store there the `CallbackContext`, supplied to you from **js** via the `exec()` method.
>   ```java
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
>   ```java
>      PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, "WHAT");
>      pluginResult.setKeepCallback(true);
>      callbackContext.sendPluginResult(pluginResult);
>   ```
#### ~~Seemingly the definitive way (from a comment in `CordovaWebView.java`):~~
~~Instead of executing snippets of **js**, you should use the exec bridge to create a **Java**&#x2794;**js** communication channel.~~

##### ~~To do this:~~
###### ~~1. Within `plugin.xml` (to have your js run before `deviceready`):~~
>   ~~`
>      <js-module>
>        <runs/>
>      </js-module>
>   `~~
###### ~~2. Within your **`.js`** (call `exec()` on start-up):~~
>   ~~`
>      require('cordova/channel').onCordovaReady.subscribe(function() {
>        require('cordova/exec')(win, null, 'Plugin', 'method', []);
>        function win(message) {
>          ... process message from Java here ...
>        }
>      });
>   `~~
###### ~~3. Within your **`.java`**:~~
>   ~~`
>      PluginResult dataResult = new PluginResult(PluginResult.Status.OK, CODE);
>      dataResult.setKeepCallback(true);
>      savedCallbackContext.sendPluginResult(dataResult);
>   `~~

### Java&#x2794;js data exchange format
What gets sent from **Java** side of things is an [org.json.JSONObject](https://developer.android.com/reference/org/json/JSONObject.html). It gets analysed on **js** side and those of its set properties, that are recoginsed, get acted upon. No configuration of its contents should bring about catastrophic events - it's foolproofed contents-wise from the **js** side: anything that is not understood or doesn't strictly conform to the format (e.g. string as a value for a boolean property:

>`{ ... "boolean_prop":`~~`"true"`~~`, ... }`

instead of:

>```java
>{ ... "boolean_prop":true, ... }
>```

with an actual boolean value) gets silently ignored, save for diagnostics, dropped to `console.log()` and/or **Java**&#x2794;**js** comms status box if it is present.

Here is the most complete form of the the JSON data object possible as it looks on the **Java** side, listing all the properties, recognized by the **js** side of things. 

Should be kept up-to-date.
>```java
>{
>  "initState": boolean,
>  "camAccess": boolean,
>  "camStatusTechnical": String,
>  "camStatusSemantical": String
>}
>```

>##### `camStatusTechnical` property can take following values:
>- "Off"
>- "Observing"
>- "SettingUp"

>##### `camStatusSemantical` property can take the following values:
>- "Nothing"
>- "SeeingSource(s)"
>- "TrackingSource"
>- "AttemptingToIdSource"
>- "RecoginsedSource"

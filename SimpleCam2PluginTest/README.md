# CeruleanPlugins Cordova-Android Camera2 test plugin/app
##### Version: 0.0.2

## ToDo:
##### 0.0.0
**[**&#x2714;**]** ~~Two clean apps - an app and a plugin that is, that build nicely together with no warnings and all according to best practices, as far as they can be fathomed in this case.~~
##### 0.0.1
**[**&#x2714;**]** ~~Apps' initialisation should be two-staged - the second one deferred until CAMERA access permission is obtained.~~

**[**&#x2714;**]** ~~Until the deferred part of the initialisation is complete, app should function normally, except for the specific functions that require CAMERA access.~~

**[**&#x2714;**]** ~~When attempting to make use of these CAMERA-related functions before the app is fully initialised, it should fail gracefully, informing the user about the necessity of granting it CAMERA permissions in order for these functions to become available.~~

**[**&#x2714;**]** ~~Do not just poll for the change in permissions - react to it as it happens, running the deferred part of the initialisation at once.~~

**[**&#x2714;**]** ~~*(phase<sup>1</sup>: when there're no actual hardware resources involved yet)* Implement proper reaction to Pause,Resume,Start,Stop,Message and Destroy events - especially concerning relinquishing and re-acquisitioning hardware resources.~~

**[**&#x2714;**]** ~~Expose to **js** a method to query the initialisation state of the plugin.~~

**[**&#x2714;**]** ~~Arrange for the **js** part of the app to be able to just listen on an event (as is with the `deviceready` one) to be notified of changes to plugins' initialisation state.~~

**[**&#x2714;**]** ~~Ignore anything related to old CAMERA API, got exclusively for CAMERA2.~~

**[**&#x2714;**]** ~~Redo the semantics of `onRestart()`, `onStart()`, `onResume()`, `onPause()`, `onStop()` and `onDestroy()` event handlers, taking into account the material from Android developers' guide [article](https://developer.android.com/reference/android/app/Activity.html#ActivityLifecycle) on `Activity` class.~~
##### 0.0.2
**[**&#xA0;&#xA0;**]** *(phase<sup>2</sup>: when there **are** some resources to acquire/relinquish)* Implement proper reaction to `Restart`, `Start`, `Resume`, `Pause`, `Stop` and `Destroy` events - especially concerning relinquishing and re-acquisitioning hardware resources.

**[**&#xA0;&#xA0;**]** Gradually add the actual **CeruleanWhisper** functionality to the plugin, testing it in the process.

**[**&#xA0;&#xA0;**]** Both image-acquisition and image-analysis threads should be background threads, not hampering the UI in any way.

**[**&#xA0;&#xA0;**]** Image stream should be set to lowest resolution possible to ease hardware load and increase FPS.

**[**&#xA0;&#xA0;**]** **js** should be continuously notified of the state of the observation task, as per the specification, until the observation mode is switched off (~~messages?~~ arrange it through normal callbacks/synthetic **js** events).

<ruby>**[**&#x00B1;**]**<rt>PRIORITY</rt></ruby> **Message Queue mechanism.** In case of comms channel unavailability when attempting to send a message from **Java** to **js** messages should become queued and later auto-sent, when channel re-appears. Probably should combine them in one big message, with some messages overwriting each other and some not (this should be governed by a flag with each message. Those which are not to be superimposed on one another, deleting history of messages generated during comms channel unavailability, should be sent consecutively, once channel gets re-established.

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;<ruby>**[**&#x2714;**]**<rt><del>PRIORITY</del></rt></ruby> ~~Add mandatory timestamp field to message format.~~

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;<ruby>**[**&#xA0;&#xA0;**]**<rt>PRIORITY</rt></ruby> Create a task that runs in a separate background thread, serving the message queue.

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;<ruby>**[**&#xA0;&#xA0;**]**<rt>PRIORITY</rt></ruby> Create and formalise queue handling protocol, class(?... perhaps just a task?..).

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;<ruby>**[**&#x2714;**]**<rt><del>PRIORITY</del></rt></ruby> ~~Formalise message format.~~

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;<ruby>**[**&#x00B1;**]**<rt>PRIORITY</rt></ruby> In message format provide for the ability to combine enqueued messages into one big message, to save on overhead. To this end, among other things:

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;<ruby>**[**&#x2714;**]**<rt><del>PRIORITY</del></rt></ruby> ~~Timestamp should be added on a `notifyJs_xxx()` functions level, so that message superimposer would know the exact message precedence.~~

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;<ruby>**[**&#x2714;**]**<rt><del>PRIORITY</del></rt></ruby> ~~Flag, governing messages' "*combinability*" should be set at the same level.~~

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;<ruby>**[**&#xA0;&#xA0;**]**<rt>PRIORITY</rt></ruby> Superimposer algorithm should combine the combinable, skip the uncombinable and sort the resulting new message queue according to every messages' effective timestamp, so that time uniformity is not lost.

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;<ruby>**[**&#x2714;**]**<rt><del>PRIORITY</del></rt></ruby> ~~Make caller id property of a message an option, given (e.g.) as an argument to `notifyJs_xxx()` function family.~~

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;<ruby>**[**&#x2714;**]**<rt><del>PRIORITY</del></rt></ruby> ~~Combine all options (so far:`boolean provideCallerId`, `long timeStamp` and `boolean isCombinable`) into one array of `JSONObject`s, provide default values for all options and make it (the extra argument) - optional for all `notifyJs_xxx()` functions.~~

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;<ruby>**[**&#xA0;&#xA0;**]**<rt>PRIORITY</rt></ruby> Combine `notifyJs_xxx()` function family into one function, that accepts different argument types (a-la multiple constructors). 


**[**&#x00B1;**]** Get to the Camera:

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[**&#x2714;**]** ~~Identify all available cameras, choose the two we need, i.e. front and back cameras.~~

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[**&#x00B1;**]** Find out all camera-related device capabilities that are relevant to us. In broad terms, these are:

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[**&#x2714;**]** ~~Minimal resolution available when using the format, that is most easy overhead-wise.~~

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[&#xA0;&#xA0;]** Maximum expected sustained FPS at that resolution.

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[&#xA0;&#xA0;]** Some simple form of an actual test to see if we are anywhere near the calculated FPS in practice.

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[**&#x00B1;**]** Ability to switch off unneeded complications, most importantly:

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[**&#x2714;**]** ~~Auto-focus~~

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[**&#x2714;**]** ~~Automatic exposure~~

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[&#xA0;&#xA0;]** Presence of high-speed burst video capture capability. And more specifically:

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[&#xA0;&#xA0;]** Formats and frame dimensions, supported in high-speed capture mode.

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[&#xA0;&#xA0;]** The smallest available resolution for this mode, coupled with the appropriate image format.

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[&#xA0;&#xA0;]** The best FPS we can reliably expect under these circumstances.

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[&#xA0;&#xA0;]** Some simple form of an actual test to see if we are anywhere near the calculated FPS in practice.

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[&#xA0;&#xA0;]** Maximum high-speed capture burst duration we can attain on this device in practical terms.

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[**&#x2714;**]** ~~Do so, jumping through every hoop official Android docs prescribe to jump through - adhere to proper protocol as much as possible.~~

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[&#xA0;&#xA0;]** Store all the this information in a conveniently compact and structured way - container should be easy for storage and retrieval and contents must yield to perusal with as little overhead as possible. (Even a well thought-out JSONObject might do the trick.)

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[**&#x00B1;**]** Arrange reporting this info to **js** side both on demand and on change. 

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[**&#x2714;**]** ~~At first just dumping it all is enough.~~

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[&#xA0;&#xA0;]** Next step would be to implement an ability to poll just a subset of this data.

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[&#xA0;&#xA0;]** With the next one being implementation of the ability to subscribe to all or part of it, to rely on **Java** side of things to push changes to **js** side, once they occur.

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[&#xA0;&#xA0;]** The following phase would be to create a simulacrum of the container with data on **js** side (JSONObject looks even better at this point as a candidate) and make them self-synchronising, so that camera state and capabilities are always known on both sides of the bridge.

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[**&#x2714;**]** ~~Arrange the code so, that it is easy to select which camera the plugin is working with - both for the programmer and for the app/device itself (minimal code changes, minimal re-calculations overhead, etc).~~

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[&#xA0;&#xA0;]** Implement (switchable on and off) video stream output to a visible surface.

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[&#xA0;&#xA0;]** Implement video output to an Allocation surface, remaining completely in the background.

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[&#xA0;&#xA0;]** At a later stage, re-implement all video processing that is required by our protocol in Renderscript, mainly to take advantage of serious parallelism, offered by Renderscript and many-cored CPUs found on modern devices.

**[&#xA0;&#xA0;]** Implement the basic semantic blocks of the **Pub** app:

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[&#xA0;&#xA0;]** Always-on (when switched on) Observer, that lives 100% in the background.

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[&#xA0;&#xA0;]** Eye-centred UI with minimal controls and detailed display of what's going on with Observer thread.

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[&#xA0;&#xA0;]** Traditional notification system (to get users' attention when a suitable message or SMS arrives).

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[&#xA0;&#xA0;]** Minimal unprotected local storage system for untransmitted message queues and general state of the app (ideally it should be sudden reboot proof too).

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[&#xA0;&#xA0;]** Final version of the QR component.

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[&#xA0;&#xA0;]** Basic message-processing mechanics (transmission/interception of messages, chunks, handing chunks over (and receiving them from) the **Priv** app, etc).

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[&#xA0;&#xA0;]** Minimal flash-based command logic.

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[&#xA0;&#xA0;]** At a later date explore the option of **Priv**&#x279E;**Pub** data transmission via high-speed burst captures/analysis.

&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;**[&#xA0;&#xA0;]** Minimal **Priv** app authentication, to at least attempt to get in the way of foreign **Priv** app trying to spoil things. 

**[&#xA0;&#xA0;]** Create and implement minimal mandatory message format - timestamps, etc.
###### 0.0.3

# Miscellaneous

### Creating Java&#x279C;js communication channel:
#### The method that is actually implemented now (later explore the advantages of moving all comms to a unified, dedicated bridge):
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

### Java&#x279C;js data exchange format
What gets sent from **Java** side of things is an [org.json.JSONObject](https://developer.android.com/reference/org/json/JSONObject.html). It gets analysed on **js** side and those of its set properties, that are recognised, get acted upon. No configuration of its contents should bring about catastrophic events - it's fool-proofed contents-wise from the **js** side: anything that is not understood or doesn't strictly conform to the format (e.g. string as a value for a boolean property:

>`{ ... "boolean_prop":`~~`"true"`~~`, ... }`

instead of:

>```java
>{ ... "boolean_prop":true, ... }
>```

with an actual boolean value) gets silently ignored, save for diagnostics, dropped to `console.log()` and/or **Java**&#x279C;**js** comms status box if it is present.

Here is the most complete form of the the JSON data object possible as it looks on the **Java** side, listing all the properties, recognised by the **js** side of things. 

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

### Java&#x279C;js data exchange functions:
##### (As implemented now - the proto comms-API it is to become later, aggregated into one or two Java/js objects, providing data flow control and message queues management)

###### Prototypes:

```java
private void       notifyJs_bool(String propName, boolean    propValue);
private void     notifyJs_String(String propName, String     propValue);
private void notifyJs_JSONObject(String propName, JSONObject propValue);
```

###### The way they are actually used (with actual parameters - the list is supposed to be exhaustive):

```java
      notifyJs_bool("initState", isFullyInitialised );
      notifyJs_bool("camAccess", hasCameraPermission);
    notifyJs_String("errors",    <various strings>  );
notifyJs_JSONObject("camState",  cameraState        );
notifyJs_JSONObject("errors",    cameraIdsEtc       );
```


[comment]: # (N.B.! For this comment format to work a blank empty line before AND after it is a must! Also, one mustn't use newlines and parentheses. <I decided to use these triangular HTML/XML tag parentheses instead.>)

[comment]: # (Some Unicode symbols: a bold <not used> left-to-right arrow:&#x2794; another <used> bold left-to-right arrow:&#x279C; a non-bold <used> left-to-right arrow:&#x279E; another non-bold <not used> left-to-right arrow:&#x279D; a very nice non-bold right arrow:&#x2192; a slightly 3D empty checkbox<GOOD>:&#x274F; beautiful check-marks, bold<GOOD, MAY BE USED BETWEEN SQUARE BRACKETS>:&#x2714; and not bold<WORSE>:&#x2713; beautiful checkbox crosses, bold<GOOD, MAY BE USED BETWEEN SQUARE BRACKETS>:&#x2718; and not bold<WORSE>:&#x2717; a warning sign:&#x26A0; a framed key:&#x26BF; a high voltage sign:&#x26A1; a Russian-style number sign:&#x2116; an information sign:&#x2139; a skull and crossbones:&#x2620; a radioactive sign:&#x2622; a bio-hazard sign:&#x2623; a hammer and sickle:&#x262D; a trademark sign:&#x2122; a 'Reserved' symbol:&#x00AE; a copyright symbol:&#x00A9; a footnote bookmark <dagger - cross-like>:&#x2020; 
a footnote bookmark <double dagger - cross-like>:&#x2021; a small footnote-mark style black star:&#x22C6; an 'exists' symbol:&#x2203; a 'does not exist' symbol:&#x2204; a 'for any' symbol:&#x22C1; a 'for all' symbol:&#x2200; a capital lambda:&#x039B; a large plus symbol:&#x2795; a large minus symbol:&#x2796; a 'minus-plus' symbol:&#x2213; a 'plus-minus' symbol:&#x00B1; an 'of the same order of magnitude' sign:&#x223D;)

[comment]: # (<ruby> A <rt>&lt;bold&gt;</rt></ruby> <== a cool one!)

[comment]: # (<details><summary>Sort of a heading</summary><p>Hidden stuff.</p><p>More hidden stuff.</p></details> <== some trivial interactivity, probably shall never need it though.)

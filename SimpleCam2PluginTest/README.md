# CeruleanPlugins Cordova-Android Camera2 test plugin/app
##### Version: 0.0.2-alpha1

### ToDo:
##### 0.0.0
- [x] ~~Two clean apps - an app and a plugin that is, that build nicely together with no warnings and all according to best practices, as far as they can be fathomed in this case.~~
##### 0.0.1
- [x] ~~Apps' initialisation should be two-staged - the second one deferred until CAMERA access permission is obtained.~~
- [x] ~~Until the deferred part of the initialisation is complete app should function normally, except for the specific functions that require CAMERA access.~~
- [x] ~~When attempting to make use of these CAMERA-related functions before the app is fully initialised, it should fail gracefully, infroming the user about the necessity of granting it CAMERA permissions in order for these functions to become available.~~
- [ ] Do not just poll for the change in permissions - react to it as it happens, running the deferred part of the initialisation at once.
- [x] ~~*(phase 1: when there's no actual HW resources invloved yet)* Implement proper reaction to Pause,Resume,Start,Stop,Message and Destroy events - especially concerning relinquishing and reacquisitioning hardware resources.~~
- [x] ~~Expose to JS a method to query the initialisation state of the plugin.~~
- [ ] Arrange for the JS part of the app to be able to just listen on an event (as is with the DeviceReady one) to be notified of changes plugins' initialisation state.
- [x] ~~Ignore anything related to old CAMERA api, got exclusively for CAMERA2.~~
###### 0.0.2
- [ ] *(phase 2: when there are **some** resources to acquire/relinquish)* Implement proper reaction to Pause,Resume,Start,Stop,Message and Destroy events - especially concerning relinquishing and reacquisitioning hardware resources.
- [ ] Gradually add the actual CeruleanWhisper functionality to the plugin, testing it in the process.
- [ ] Both image-acquisition and image-analysis threads should be background threads, not hampering the UI in any way.
- [ ] Image stream should be set to lowest resolution possible to ease hardware load and increase FPS.
- [ ] JS should be continuously notified of the state of the observation task, as per the specification, until the observation mode is switched off (messages?).

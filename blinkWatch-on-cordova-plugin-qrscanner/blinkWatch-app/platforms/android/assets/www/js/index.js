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
var app = {
    // Application Constructor
    initialize: function() {
        document.addEventListener('deviceready', this.onDeviceReady.bind(this), false);
    },

    onClickOpToggle:  function() {
        if (document.body.style.opacity==0) {document.body.style.opacity=1;} else {document.body.style.opacity=0;}
        document.getElementById('OpToggle').style.opacity=1;
    },

    onClickBlinkWatchPrepare: function() {
        document.getElementById('blinkWatchPrepareLabel').style.color = '#000000';
        document.getElementById('blinkWatchPrepareLabel').innerHTML = 'Launching plugin function blinkWatch(prepare).....';
        document.getElementById('deviceready').querySelector('.received').innerHTML = 'Preparing.....';
        document.getElementById('deviceready').querySelector('.received').style.color = '#000000';
        function displayContents(err, text){if(err){document.getElementById('blinkWatchPrepareLabel').style.color = '#FF0000';document.getElementById('blinkWatchPrepareLabel').innerHTML = ' ERROR! ('+err.name+', '+err.code+', '+err._message+')..';} else {document.getElementById('blinkWatchPrepareLabel').style.color = '#009900';document.getElementById('blinkWatchPrepareLabel').innerHTML = ' NO ERROR! ('+JSON.stringify(text)+')..';}}
        cordova.plugins.CeruleanPlugins.BlinkWatch.prepare(displayContents);
    },

    onClickBlinkWatchDestroy: function() {
        document.getElementById('blinkWatchDestroyLabel').style.color = '#000000';
        document.getElementById('blinkWatchDestroyLabel').innerHTML = 'Launching plugin function blinkWatch(destroy).....';
        document.getElementById('deviceready').querySelector('.received').innerHTML = 'Destroying.....';
        document.getElementById('deviceready').querySelector('.received').style.color = '#000000';
        function displayContents(err, text){if(err){document.getElementById('blinkWatchDestroyLabel').style.color = '#FF0000';document.getElementById('blinkWatchDestroyLabel').innerHTML = 'ERROR! ('+err.name+', '+err.code+', '+err._message+')..';} else {document.getElementById('blinkWatchDestroyLabel').style.color = '#009900';document.getElementById('blinkWatchDestroyLabel').innerHTML = ' NO ERROR! ('+JSON.stringify(text)+')..';}}
        cordova.plugins.CeruleanPlugins.BlinkWatch.destroy(displayContents);
    },


    onClickBlinkWatchUseCamera: function() {
        document.getElementById('blinkWatchUseCameraLabel').style.color = '#000000';
        document.getElementById('blinkWatchUseCameraLabel').innerHTML = 'Launching plugin function blinkWatch(useCamera).....';
        document.getElementById('deviceready').querySelector('.received').innerHTML = 'Using camera.....';
        document.getElementById('deviceready').querySelector('.received').style.color = '#000000';
        function displayContents(err, text){if(err){document.getElementById('blinkWatchUseCameraLabel').style.color = '#FF0000';document.getElementById('blinkWatchUseCameraLabel').innerHTML = 'ERROR! ('+err.name+', '+err.code+', '+err._message+')..';} else {document.getElementById('blinkWatchUseCameraLabel').style.color = '#009900';document.getElementById('blinkWatchUseCameraLabel').innerHTML = ' NO ERROR! ('+JSON.stringify(text)+')..';}}
        cordova.plugins.CeruleanPlugins.BlinkWatch.useCamera(displayContents);
    },

    onClickBlinkWatchUseFrontCamera: function() {
        document.getElementById('blinkWatchUseFrontCameraLabel').style.color = '#000000';
        document.getElementById('blinkWatchUseFrontCameraLabel').innerHTML = 'Launching plugin function blinkWatch(useFrontCamera).....';
        document.getElementById('deviceready').querySelector('.received').innerHTML = 'Using front camera.....';
        document.getElementById('deviceready').querySelector('.received').style.color = '#000000';
        function displayContents(err, text){if(err){document.getElementById('blinkWatchUseFrontCameraLabel').style.color = '#FF0000';document.getElementById('blinkWatchUseFrontCameraLabel').innerHTML = 'ERROR! ('+err.name+', '+err.code+', '+err._message+')..';} else {document.getElementById('blinkWatchUseFrontCameraLabel').style.color = '#009900';document.getElementById('blinkWatchUseFrontCameraLabel').innerHTML = ' NO ERROR! ('+JSON.stringify(text)+')..';}}
        cordova.plugins.CeruleanPlugins.BlinkWatch.useFrontCamera(displayContents);
    },

    onClickBlinkWatchUseBackCamera: function() {
        document.getElementById('blinkWatchUseBackCameraLabel').style.color = '#000000';
        document.getElementById('blinkWatchUseBackCameraLabel').innerHTML = 'Launching plugin function blinkWatch(useBackCamera).....';
        document.getElementById('deviceready').querySelector('.received').innerHTML = 'Using back camera.....';
        document.getElementById('deviceready').querySelector('.received').style.color = '#000000';
        function displayContents(err, text){if(err){document.getElementById('blinkWatchUseBackCameraLabel').style.color = '#FF0000';document.getElementById('blinkWatchUseBackCameraLabel').innerHTML = 'ERROR! ('+err.name+', '+err.code+', '+err._message+')..';} else {document.getElementById('blinkWatchUseBackCameraLabel').style.color = '#009900';document.getElementById('blinkWatchUseBackCameraLabel').innerHTML = ' NO ERROR! ('+JSON.stringify(text)+')..';}}
        cordova.plugins.CeruleanPlugins.BlinkWatch.useBackCamera(displayContents);
    },

    onClickBlinkWatchScan: function() {
        document.getElementById('blinkWatchScanLabel').style.color = '#000000';
        document.getElementById('blinkWatchScanLabel').innerHTML = 'Launching plugin function blinkWatch(scan).....';
        document.getElementById('deviceready').querySelector('.received').innerHTML = 'Scanning.....';
        document.getElementById('deviceready').querySelector('.received').style.color = '#000000';
        function displayContents(err, text){if(err){document.getElementById('blinkWatchScanLabel').style.color = '#FF0000';document.getElementById('blinkWatchScanLabel').innerHTML = 'ERROR! ('+err.name+', '+err.code+', '+err._message+')..';} else {document.getElementById('blinkWatchScanLabel').style.color = '#009900';document.getElementById('blinkWatchScanLabel').innerHTML = ' NO ERROR! ('+JSON.stringify(text)+')..';}}
        cordova.plugins.CeruleanPlugins.BlinkWatch.scan(displayContents);
    },

    onClickBlinkWatchCancelScan: function() {
        document.getElementById('blinkWatchCancelScanLabel').style.color = '#000000';
        document.getElementById('blinkWatchCancelScanLabel').innerHTML = 'Launching plugin function blinkWatch(cancelscan).....';
        document.getElementById('deviceready').querySelector('.received').innerHTML = 'Canceled scan.....';
        document.getElementById('deviceready').querySelector('.received').style.color = '#000000';
        function displayContents(err, text){if(err){document.getElementById('blinkWatchCancelScanLabel').style.color = '#FF0000';document.getElementById('blinkWatchCancelScanLabel').innerHTML = 'ERROR! ('+err.name+', '+err.code+', '+err._message+')..';} else {document.getElementById('blinkWatchCancelScanLabel').style.color = '#009900';document.getElementById('blinkWatchCancelScanLabel').innerHTML = ' NO ERROR! ('+JSON.stringify(text)+')..';}}
        cordova.plugins.CeruleanPlugins.BlinkWatch.cancelScan(displayContents);
    },

    onClickBlinkWatchPausePreview: function() {
        document.getElementById('blinkWatchPausePreviewLabel').style.color = '#000000';
        document.getElementById('blinkWatchPausePreviewLabel').innerHTML = 'Launching plugin function blinkWatch(pausePreview).....';
        document.getElementById('deviceready').querySelector('.received').innerHTML = 'Pausing preview.....';
        document.getElementById('deviceready').querySelector('.received').style.color = '#000000';
        function displayContents(err, text){if(err){document.getElementById('blinkWatchPausePreviewLabel').style.color = '#FF0000';document.getElementById('blinkWatchPausePreviewLabel').innerHTML = 'ERROR! ('+err.name+', '+err.code+', '+err._message+')..';} else {document.getElementById('blinkWatchPausePreviewLabel').style.color = '#009900';document.getElementById('blinkWatchPausePreviewLabel').innerHTML = ' NO ERROR! ('+JSON.stringify(text)+')..';}}
        cordova.plugins.CeruleanPlugins.BlinkWatch.pausePreview(displayContents);
    },

    onClickBlinkWatchResumePreview: function() {
        document.getElementById('blinkWatchResumePreviewLabel').style.color = '#000000';
        document.getElementById('blinkWatchResumePreviewLabel').innerHTML = 'Launching plugin function blinkWatch(resumePreview).....';
        document.getElementById('deviceready').querySelector('.received').innerHTML = 'Resuming preview.....';
        document.getElementById('deviceready').querySelector('.received').style.color = '#000000';
        function displayContents(err, text){if(err){document.getElementById('blinkWatchResumePreviewLabel').style.color = '#FF0000';document.getElementById('blinkWatchResumePreviewLabel').innerHTML = 'ERROR! ('+err.name+', '+err.code+', '+err._message+')..';} else {document.getElementById('blinkWatchResumePreviewLabel').style.color = '#009900';document.getElementById('blinkWatchResumePreviewLabel').innerHTML = ' NO ERROR! ('+JSON.stringify(text)+')..';}}
        cordova.plugins.CeruleanPlugins.BlinkWatch.resumePreview(displayContents);
    },

    onClickBlinkWatchOpenSettings: function() {
        document.getElementById('blinkWatchOpenSettingsLabel').style.color = '#000000';
        document.getElementById('blinkWatchOpenSettingsLabel').innerHTML = 'Launching plugin function blinkWatch(openSettings).....';
        document.getElementById('deviceready').querySelector('.received').innerHTML = 'Opening settings.....';
        document.getElementById('deviceready').querySelector('.received').style.color = '#000000';
        function displayContents(err, text){if(err){document.getElementById('blinkWatchOpenSettingsLabel').style.color = '#FF0000';document.getElementById('blinkWatchOpenSettingsLabel').innerHTML = 'ERROR! ('+err.name+', '+err.code+', '+err._message+')..';} else {document.getElementById('blinkWatchOpenSettingsLabel').style.color = '#009900';document.getElementById('blinkWatchOpenSettingsLabel').innerHTML = ' NO ERROR! ('+JSON.stringify(text)+')..';}}
        cordova.plugins.CeruleanPlugins.BlinkWatch.openSettings(displayContents);
    },

    onClickBlinkWatchGetStatus: function() {
        document.getElementById('blinkWatchGetStatusLabel').style.color = '#000000';
        document.getElementById('blinkWatchGetStatusLabel').innerHTML = 'Launching plugin function blinkWatch(getStatus).....';
        document.getElementById('deviceready').querySelector('.received').innerHTML = 'Getting status.....';
        document.getElementById('deviceready').querySelector('.received').style.color = '#000000';
        function displayContents(err, text){if(err){document.getElementById('blinkWatchGetStatusLabel').style.color = '#FF0000';document.getElementById('blinkWatchGetStatusLabel').innerHTML = 'ERROR! ('+err.name+', '+err.code+', '+err._message+')..';} else {document.getElementById('blinkWatchGetStatusLabel').style.color = '#009900';document.getElementById('blinkWatchGetStatusLabel').innerHTML = ' NO ERROR! ('+JSON.stringify(text)+')..';}}
        cordova.plugins.CeruleanPlugins.BlinkWatch.getStatus(displayContents);
    },

    onClickBlinkWatchShow: function() {
        document.getElementById('blinkWatchShowLabel').style.color = '#000000';
        document.getElementById('blinkWatchShowLabel').innerHTML = 'Launching plugin function blinkWatch(show).....';
        document.getElementById('deviceready').querySelector('.received').innerHTML = 'Showing.....';
        document.getElementById('deviceready').querySelector('.received').style.color = '#000000';
        function displayContents(err, text){if(err){document.getElementById('blinkWatchShowLabel').style.color = '#FF0000';document.getElementById('blinkWatchShowLabel').innerHTML = 'ERROR! ('+err.name+', '+err.code+', '+err._message+')..';} else {document.getElementById('blinkWatchShowLabel').style.color = '#009900';document.getElementById('blinkWatchShowLabel').innerHTML = ' NO ERROR! ('+JSON.stringify(text)+')..';}}
        cordova.plugins.CeruleanPlugins.BlinkWatch.show(displayContents);
    },

    onClickBlinkWatchHide: function() {
        document.getElementById('blinkWatchHideLabel').style.color = '#000000';
        document.getElementById('blinkWatchHideLabel').innerHTML = 'Launching plugin function blinkWatch(hide).....';
        document.getElementById('deviceready').querySelector('.received').innerHTML = 'Hiding.....';
        document.getElementById('deviceready').querySelector('.received').style.color = '#000000';
        function displayContents(err, text){if(err){document.getElementById('blinkWatchHideLabel').style.color = '#FF0000';document.getElementById('blinkWatchHideLabel').innerHTML = 'ERROR! ('+err.name+', '+err.code+', '+err._message+')..';} else {document.getElementById('blinkWatchHideLabel').style.color = '#009900';document.getElementById('blinkWatchHideLabel').innerHTML = ' NO ERROR! ('+JSON.stringify(text)+')..';}}
        cordova.plugins.CeruleanPlugins.BlinkWatch.hide(displayContents);
    },

    onClickBlinkWatchEnableLight: function() {
        document.getElementById('blinkWatchEnableLightLabel').style.color = '#000000';
        document.getElementById('blinkWatchEnableLightLabel').innerHTML = 'Launching plugin function blinkWatch(enableLight).....';
        document.getElementById('deviceready').querySelector('.received').innerHTML = 'Enabling light.....';
        document.getElementById('deviceready').querySelector('.received').style.color = '#000000';
        function displayContents(err, text){if(err){document.getElementById('blinkWatchEnableLightLabel').style.color = '#FF0000';document.getElementById('blinkWatchEnableLightLabel').innerHTML = 'ERROR! ('+err.name+', '+err.code+', '+err._message+')..';} else {document.getElementById('blinkWatchEnableLightLabel').style.color = '#009900';document.getElementById('blinkWatchEnableLightLabel').innerHTML = ' NO ERROR! ('+JSON.stringify(text)+')..';}}
        cordova.plugins.CeruleanPlugins.BlinkWatch.enableLight(displayContents);
    },

    onClickBlinkWatchDisableLight: function() {
        document.getElementById('blinkWatchDisableLightLabel').style.color = '#000000';
        document.getElementById('blinkWatchDisableLightLabel').innerHTML = 'Launching plugin function blinkWatch(disableLight).....';
        document.getElementById('deviceready').querySelector('.received').innerHTML = 'Disabling light.....';
        document.getElementById('deviceready').querySelector('.received').style.color = '#000000';
        function displayContents(err, text){if(err){document.getElementById('blinkWatchDisableLightLabel').style.color = '#FF0000';document.getElementById('blinkWatchDisableLightLabel').innerHTML = 'ERROR! ('+err.name+', '+err.code+', '+err._message+')..';} else {document.getElementById('blinkWatchDisableLightLabel').style.color = '#009900';document.getElementById('blinkWatchDisableLightLabel').innerHTML = ' NO ERROR! ('+JSON.stringify(text)+')..';}}
        cordova.plugins.CeruleanPlugins.BlinkWatch.disableLight(displayContents);
    },

    // deviceready Event Handler
    //
    // Bind any cordova events here. Common events are:
    // 'pause', 'resume', etc.
    onDeviceReady: function() {
        document.getElementById('OpToggle').addEventListener('click', this.onClickOpToggle.bind(this), false);
        document.getElementById('blinkWatchButtonPrepare').addEventListener('click', this.onClickBlinkWatchPrepare.bind(this), false);
        document.getElementById('blinkWatchPrepareLabel').innerHTML = 'Click event handler calling <span class=\'strng\'>\'prepare\'</span> action at service <span class=\'strng\'>\'CeruleanPlugins.blinkWatch\'</span> is bound to blinkWatchButtonPrepare...';
        document.getElementById('blinkWatchButtonDestroy').addEventListener('click', this.onClickBlinkWatchDestroy.bind(this), false);
        document.getElementById('blinkWatchDestroyLabel').innerHTML = 'Click event handler calling <span class=\'strng\'>\'destroy\'</span> action at service <span class=\'strng\'>\'CeruleanPlugins.blinkWatch\'</span> is bound to blinkWatchButtonDestroy...';
        document.getElementById('blinkWatchButtonUseCamera').addEventListener('click', this.onClickBlinkWatchUseCamera.bind(this), false);
        document.getElementById('blinkWatchUseCameraLabel').innerHTML = 'Click event handler calling <span class=\'strng\'>\'useCamera\'</span> action at service <span class=\'strng\'>\'CeruleanPlugins.blinkWatch\'</span> is bound to blinkWatchButtonUseCamera...';
        document.getElementById('blinkWatchButtonUseFrontCamera').addEventListener('click', this.onClickBlinkWatchUseFrontCamera.bind(this), false);
        document.getElementById('blinkWatchUseFrontCameraLabel').innerHTML = 'Click event handler calling <span class=\'strng\'>\'useFrontCamera\'</span> action at service <span class=\'strng\'>\'CeruleanPlugins.blinkWatch\'</span> is bound to blinkWatchButtonUseFrontCamera...';
        document.getElementById('blinkWatchButtonUseBackCamera').addEventListener('click', this.onClickBlinkWatchUseBackCamera.bind(this), false);
        document.getElementById('blinkWatchUseBackCameraLabel').innerHTML = 'Click event handler calling <span class=\'strng\'>\'useBackCamera\'</span> action at service <span class=\'strng\'>\'CeruleanPlugins.blinkWatch\'</span> is bound to blinkWatchButtonUseBackCamera...';
        document.getElementById('blinkWatchButtonScan').addEventListener('click', this.onClickBlinkWatchScan.bind(this), false);
        document.getElementById('blinkWatchScanLabel').innerHTML = 'Click event handler calling <span class=\'strng\'>\'scan\'</span> action at service <span class=\'strng\'>\'CeruleanPlugins.blinkWatch\'</span> is bound to blinkWatchButtonScan...';
        document.getElementById('blinkWatchButtonCancelScan').addEventListener('click', this.onClickBlinkWatchCancelScan.bind(this), false);
        document.getElementById('blinkWatchCancelScanLabel').innerHTML = 'Click event handler calling <span class=\'strng\'>\'cancelScan\'</span> action at service <span class=\'strng\'>\'CeruleanPlugins.blinkWatch\'</span> is bound to blinkWatchButtonCancelScan...';
        document.getElementById('blinkWatchButtonPausePreview').addEventListener('click', this.onClickBlinkWatchPausePreview.bind(this), false);
        document.getElementById('blinkWatchPausePreviewLabel').innerHTML = 'Click event handler calling <span class=\'strng\'>\'pausePreview\'</span> action at service <span class=\'strng\'>\'CeruleanPlugins.blinkWatch\'</span> is bound to blinkWatchButtonPausePreview...';
        document.getElementById('blinkWatchButtonResumePreview').addEventListener('click', this.onClickBlinkWatchResumePreview.bind(this), false);
        document.getElementById('blinkWatchResumePreviewLabel').innerHTML = 'Click event handler calling <span class=\'strng\'>\'resumePreview\'</span> action at service <span class=\'strng\'>\'CeruleanPlugins.blinkWatch\'</span> is bound to blinkWatchButtonResumePreview...';
        document.getElementById('blinkWatchButtonOpenSettings').addEventListener('click', this.onClickBlinkWatchOpenSettings.bind(this), false);
        document.getElementById('blinkWatchOpenSettingsLabel').innerHTML = 'Click event handler calling <span class=\'strng\'>\'openSettings\'</span> action at service <span class=\'strng\'>\'CeruleanPlugins.blinkWatch\'</span> is bound to blinkWatchButtonOpenSettings...';
        document.getElementById('blinkWatchButtonGetStatus').addEventListener('click', this.onClickBlinkWatchGetStatus.bind(this), false);
        document.getElementById('blinkWatchGetStatusLabel').innerHTML = 'Click event handler calling <span class=\'strng\'>\'getStatus\'</span> action at service <span class=\'strng\'>\'CeruleanPlugins.blinkWatch\'</span> is bound to blinkWatchButtonGetStatus...';
        document.getElementById('blinkWatchButtonShow').addEventListener('click', this.onClickBlinkWatchShow.bind(this), false);
        document.getElementById('blinkWatchShowLabel').innerHTML = 'Click event handler calling <span class=\'strng\'>\'show\'</span> action at service <span class=\'strng\'>\'CeruleanPlugins.blinkWatch\'</span> is bound to blinkWatchButtonShow...';
        document.getElementById('blinkWatchButtonHide').addEventListener('click', this.onClickBlinkWatchHide.bind(this), false);
        document.getElementById('blinkWatchHideLabel').innerHTML = 'Click event handler calling <span class=\'strng\'>\'hide\'</span> action at service <span class=\'strng\'>\'CeruleanPlugins.blinkWatch\'</span> is bound to blinkWatchButtonHide...';
        document.getElementById('blinkWatchButtonEnableLight').addEventListener('click', this.onClickBlinkWatchEnableLight.bind(this), false);
        document.getElementById('blinkWatchEnableLightLabel').innerHTML = 'Click event handler calling <span class=\'strng\'>\'enableLight\'</span> action at service <span class=\'strng\'>\'CeruleanPlugins.blinkWatch\'</span> is bound to blinkWatchButtonEnableLight...';
        document.getElementById('blinkWatchButtonDisableLight').addEventListener('click', this.onClickBlinkWatchDisableLight.bind(this), false);
        document.getElementById('blinkWatchDisableLightLabel').innerHTML = 'Click event handler calling <span class=\'strng\'>\'disableLight\'</span> action at service <span class=\'strng\'>\'CeruleanPlugins.blinkWatch\'</span> is bound to blinkWatchButtonDisableLight...';

        this.receivedEvent('deviceready');
    },

    // Update DOM on a Received Event
    receivedEvent: function(id) {
        var parentElement = document.getElementById(id);
        var listeningElement = parentElement.querySelector('.listening');
        var receivedElement = parentElement.querySelector('.received');

        listeningElement.setAttribute('style', 'display:none;');
        receivedElement.setAttribute('style', 'display:block;');

        console.log('Received Event: ' + id);
    }
};

app.initialize();

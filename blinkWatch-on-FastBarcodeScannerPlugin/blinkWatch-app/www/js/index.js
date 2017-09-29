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

    onClickOpToggle:  function() {
        if (document.body.style.opacity==0) {document.body.style.opacity=1;} else {document.body.style.opacity=0;}
        document.getElementById('OpToggle').style.opacity=1;
    },

    onClickOpToggle2: function () {var body = document.body; if (body.style) {body.style.backgroundColor = 'rgba(0,0,0,0.01)'; body.style.backgroundImage = ''; setTimeout(function() { body.style.backgroundColor = 'transparent'; }, 1); if (body.parentNode && body.parentNode.style) { body.parentNode.style.backgroundColor = 'transparent'; body.parentNode.style.backgroundImage = ''; }}},


    successCb: function(success_txt) {console.log('cw-sqcore-ceruleanplugins-blinkwatch: SUCCESS! (message:\''+JSON.stringify(success_txt)+'\')');},
    errorCb:   function(error_txt)   {console.log('cw-sqcore-ceruleanplugins-blinkwatch: ERROR! (name:'+error_txt.name+', code:'+error_txt.code+', message:\''+error_txt._message+'\')');},

    onClickBlinkWatchStartScanning: function() {
        document.getElementById('blinkWatchStartScanningLabel').style.color = '#000000';
        document.getElementById('blinkWatchStartScanningLabel').innerHTML = 'Launching plugin function blinkWatch(startScanning).....';
        document.getElementById('deviceready').querySelector('.received').innerHTML = 'Starting Scan.....';
        document.getElementById('deviceready').querySelector('.received').style.color = '#000000';
/*        cordova.plugins.CeruleanPlugins.BlinkWatch.startScanning(displayPluginCallResults);*/
        cordova.plugins.CeruleanPlugins.BlinkWatch.startScanning(this.successCb,this.errorCb);
    },

    onClickBlinkWatchStopScanning: function() {
        document.getElementById('blinkWatchStopScanningLabel').style.color = '#000000';
        document.getElementById('blinkWatchStopScanningLabel').innerHTML = 'Launching plugin function blinkWatch(stopScanning).....';
        document.getElementById('deviceready').querySelector('.received').innerHTML = 'Stopping Scan.....';
        document.getElementById('deviceready').querySelector('.received').style.color = '#000000';
/*        function displayContents(err, text){if(err){document.getElementById('blinkWatchStopScanningLabel').style.color = '#FF0000';document.getElementById('blinkWatchStopScanningLabel').innerHTML = 'ERROR! ('+err.name+', '+err.code+', '+err._message+')..';} else {document.getElementById('blinkWatchStopScanningLabel').style.color = '#009900';document.getElementById('blinkWatchStopScanningLabel').innerHTML = ' NO ERROR! ('+JSON.stringify(text)+')..';}}*/
/*        cordova.plugins.CeruleanPlugins.BlinkWatch.stopScanning(displayPluginCallResults);*/
        cordova.plugins.CeruleanPlugins.BlinkWatch.stopScanning(this.successCb,this.errorCb);
    },

    // deviceready Event Handler
    //
    // Bind any cordova events here. Common events are:
    // 'pause', 'resume', etc.
    onDeviceReady: function() {
        document.getElementById('OpToggle').addEventListener('click', this.onClickOpToggle.bind(this), false);
        document.getElementById('OpToggle2').addEventListener('click', this.onClickOpToggle2.bind(this), false);
        document.getElementById('blinkWatchButtonStartScanning').addEventListener('click', this.onClickBlinkWatchStartScanning.bind(this), false);
        document.getElementById('blinkWatchStartScanningLabel').innerHTML = 'Click event handler calling <span class=\'strng\'>\'startScanning\'</span> action at service <span class=\'strng\'>\'CeruleanPlugins.blinkWatch\'</span> is bound to blinkWatchButtonStartScanning...';
        document.getElementById('blinkWatchButtonStopScanning').addEventListener('click', this.onClickBlinkWatchStopScanning.bind(this), false);
        document.getElementById('blinkWatchStopScanningLabel').innerHTML = 'Click event handler calling <span class=\'strng\'>\'stopScanning\'</span> action at service <span class=\'strng\'>\'CeruleanPlugins.blinkWatch\'</span> is bound to blinkWatchButtonStopScanning...';

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

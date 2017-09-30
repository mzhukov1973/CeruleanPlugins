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
    // Application Constructor:
    initialize: function() {
        document.addEventListener('deviceready', this.onDeviceReady.bind(this), false);
    },

    onClickOpToggle:  function() {
     var body = document.body; if (body.style) {body.style.backgroundColor = 'rgba(0,0,0,0.01)'; body.style.backgroundImage = ''; setTimeout(function() { body.style.backgroundColor = 'transparent'; }, 1); if (body.parentNode && body.parentNode.style) { body.parentNode.style.backgroundColor = 'transparent'; body.parentNode.style.backgroundImage = ''; }}
    },

    successCallback: function(success_txt) {
     var strTxt = 'cordova.plugins.Cam2Plug: SUCCESS! (message:\''+JSON.stringify(success_txt)+'\')';
     console.log(strTxt);
     document.getElementById('videoLabel').style.color = '#009900';
     document.getElementById('videoLabel').innerHTML = StrTxt;
     document.getElementById('deviceready').querySelector('.received').style.color = '#009900';
     document.getElementById('deviceready').querySelector('.received').innerHTML = 'Done!';
    },

    errorCallback: function(error_txt) {
     var strTxt = 'cordova.plugins.Cam2Plug: ERROR! (message:\''+JSON.stringify(error_txt)+'\')';
     console.log(strTxt);
     document.getElementById('videoLabel').style.color = '#AA0000';
     document.getElementById('videoLabel').innerHTML = StrTxt;
     document.getElementById('deviceready').querySelector('.received').style.color = '#FF0000';
     document.getElementById('deviceready').querySelector('.received').innerHTML = 'Error!';
    },

    errorCallback:   function(error_txt)   {console.log('cordova.plugins.Cam2Plug: ERROR! (message:\''+JSON.stringify(error_txt)+'\')');},

    onStartVideoClick: function() {
        document.getElementById('videoLabel').style.color = '#000000';
        document.getElementById('videoLabel').innerHTML = 'Starting video preview.....';
        document.getElementById('deviceready').querySelector('.received').style.color = '#000000';
        document.getElementById('deviceready').querySelector('.received').innerHTML = 'Starting video.....';
        cordova.plugins.Cam2Plug.startVideo(this.successCallback,this.errorCallback);
    },

    onStopVideoClick: function() {
        document.getElementById('videoLabel').style.color = '#000000';
        document.getElementById('videoLabel').innerHTML = 'Stopping video preview.....';
        document.getElementById('deviceready').querySelector('.received').style.color = '#000000';
        document.getElementById('deviceready').querySelector('.received').innerHTML = 'Stopping video.....';
        cordova.plugins.Cam2Plug.startVideo(this.successCallback,this.errorCallback);
    },
    // deviceready Event Handler.
    // Bind any cordova events here. Common events are: 'pause', 'resume', etc.
    onDeviceReady: function() {
        document.getElementById('startVideo').addEventListener('click', this.onStartVideoClick.bind(this), false);
        document.getElementById('stopVideo').addEventListener('click', this.onStopVideoClick.bind(this), false);
        document.getElementById('videoLabel').innerHTML = 'Click event handlers calling <span class=\'strng\'>\'startVideo\'</span> and <span class=\'strng\'>\'startVideo\'</span> actions at service <span class=\'strng\'>\'Cam2Plug\'</span> are now bound to these two buttons...';
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

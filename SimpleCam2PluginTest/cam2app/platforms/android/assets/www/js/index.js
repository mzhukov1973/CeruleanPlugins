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
        app.strInitState = 'Unknown';
        app.strcamAccess = 'Unknown';
    },

    displayCamState: function(camStateObject,htmlContainer) {
      var camStateMarkup = '<span style="font-weight:700;color:black;">P</span><span style="font-weight:400;color:green;">la</span><span style="font-weight:400;color:red;">ce</span><span style="font-weight:400;color:black;">ho</span><span style="font-weight:400;color:blue;">ld</span><span style="font-weight:700;color:rgba(255,0,0,1);">er</span>';
      htmlContainer.innerHTML = camStateMarkup;
    },

    j2jsCallback: function(jsonObj) {
      /*Dump received stringified JSON object as string to console:*/
      console.log('Java=>js link: data received (should be a JSON object as a string): '+JSON.stringify(jsonObj)+'.');

      /*Then proceed iterating over and reacting to its properties:*/
      if (Object.prototype.hasOwnProperty.call(jsonObj, 'initState')) {
        if (jsonObj['initState']) {app.strInitState = 'Fully';document.getElementById('initState1').classList.add('green');document.getElementById('initState1').classList.remove('red');document.getElementById('initState1').classList.remove('black');}
        else                      {app.strInitState = 'Not Fully';document.getElementById('initState1').classList.add('red');document.getElementById('initState1').classList.remove('green');document.getElementById('initState1').classList.remove('black');}
        console.log('Java=>js link: Cam2Plug plugin is currently: \'' + app.strInitState + ' initialised\'!');
        document.getElementById('initState1').innerHTML = app.strInitState + ' initialised';
      }

      else if (Object.prototype.hasOwnProperty.call(jsonObj, 'camAccess')) {
        if (jsonObj['camAccess']) {app.strCamAccess = 'Granted';document.getElementById('camAccess1').classList.add('green');document.getElementById('camAccess1').classList.remove('red');document.getElementById('camAccess1').classList.remove('black');}
        else                      {app.strCamAccess = 'Denied';document.getElementById('camAccess1').classList.add('red');document.getElementById('camAccess1').classList.remove('green');document.getElementById('camAccess1').classList.remove('black');}
        console.log('Java=>js link: Camera access for the plugin is currently: \'' + app.strCamAccess + '\'.');
        document.getElementById('camAccess1').innerHTML = app.strCamAccess;
      }

      else if (Object.prototype.hasOwnProperty.call(jsonObj, 'camState')) {
        console.log('Java=>js link: camState is currently:');console.log(jsonObj['camState']);
        displayCamState(jsonObj['camState'],document.getElementById('megaStatus'));
      }

      else if (Object.prototype.hasOwnProperty.call(jsonObj, 'errors')) {
        console.log('Java=>js link: errors object currently looks like this:');console.log(jsonObj['errors']);
      }
    },

    successCallback: function(success_txt) {
     var strTxt = 'cordova.plugins.Cam2Plug: SUCCESS! (message:\''+JSON.stringify(success_txt)+'\')';
     console.log(strTxt);
     document.getElementById('videoLabel').style.color = '#009900';
     document.getElementById('videoLabel').innerHTML = strTxt;
     document.getElementById('deviceready').querySelector('.received').style.color = '#009900';
     document.getElementById('deviceready').querySelector('.received').innerHTML = 'Done!';
    },

    errorCallback: function(error_txt) {
     var strTxt = 'cordova.plugins.Cam2Plug: ERROR! (message:\''+JSON.stringify(error_txt)+'\')';
     console.log(strTxt);
     document.getElementById('videoLabel').style.color = '#AA0000';
     document.getElementById('videoLabel').innerHTML = strTxt;
     document.getElementById('deviceready').querySelector('.received').style.color = '#FF0000';
     document.getElementById('deviceready').querySelector('.received').innerHTML = 'Error!';
    },

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
        cordova.plugins.Cam2Plug.stopVideo(this.successCallback,this.errorCallback);
    },

    onIsFullyInitialisedClick: function() {
        document.getElementById('videoLabel').style.color = '#000000';
        document.getElementById('videoLabel').innerHTML = 'Finding out if plugin is fully initialised.....';
        document.getElementById('deviceready').querySelector('.received').style.color = '#000000';
        document.getElementById('deviceready').querySelector('.received').innerHTML = 'Checking init status.....';
        cordova.plugins.Cam2Plug.isFullyInitialised(this.successCallback,this.errorCallback);
    },

    onCoolMethodClick: function() {
        document.getElementById('videoLabel').style.color = '#000000';
        document.getElementById('videoLabel').innerHTML = 'Firing up the echo.....';
        document.getElementById('deviceready').querySelector('.received').style.color = '#000000';
        document.getElementById('deviceready').querySelector('.received').innerHTML = 'Checking if echo works.....';
        cordova.plugins.Cam2Plug.coolMethod('Heeeeeeeeeey!.....',this.successCallback,this.errorCallback);
    },
    // deviceready Event Handler.
    // Bind any cordova events here. Common events are: 'pause', 'resume', etc.
    onDeviceReady: function() {
        document.getElementById('startVideo').addEventListener('click', this.onStartVideoClick.bind(this), false);
        document.getElementById('stopVideo').addEventListener('click', this.onStopVideoClick.bind(this), false);
        document.getElementById('isFullyInitialised').addEventListener('click', this.onIsFullyInitialisedClick.bind(this), false);
        document.getElementById('coolMethod').addEventListener('click', this.onCoolMethodClick.bind(this), false);
        document.getElementById('videoLabel').innerHTML = 'Click event handlers calling <span class=\'strng\'>\'startVideo\'</span>, <span class=\'strng\'>\'stopVideo\'</span>, <span class=\'strng\'>\'isFullyInittialised\'</span> and <span class=\'strng\'>\'coolMethod\'</span> actions at the <span class=\'strng\'>\'Cam2Plug\'</span> service are now bound to these four buttons...';
        cordova.plugins.Cam2Plug.j2jsLinkCreate(this.j2jsCallback,this.j2jsCallback); /*Establish the Java=>js link.*/
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

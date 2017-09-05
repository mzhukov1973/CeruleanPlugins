/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var app = {
    // Application Constructor
    initialize: function() {
        document.addEventListener('deviceready', this.onDeviceReady.bind(this), false);
    },
    onClickBlinkFlash: function() {/*2x10*/
        const blink = [25,1000,25,5000,10,50,10,50,10,50,10,50,10,50,10,50,10,50,10,50,10,50,10,50];/*an array with on/off times in ms (starts with 'on')*/
        document.getElementById('blinkFlashLabel').style.color = '#000000';
        document.getElementById('blinkFlashLabel').innerHTML = 'Launching plugin function blinkFlash.....';
        document.getElementById('deviceready').querySelector('.received').innerHTML = 'Flasging.....';
        document.getElementById('deviceready').querySelector('.received').style.color = '#000000';
        cordova.plugins.CeruleanPlugins.blinkFlash(
         blink, 
         function(msg) {
                        document.getElementById('deviceready').querySelector('.received').innerHTML = msg;
                        document.getElementById('blinkFlashLabel').style.color = '#00AA00';
                        document.getElementById('blinkFlashLabel').innerHTML = 'Success. blinkFlash actions\' message is:\''+msg+'\'';
                       },
         function(err) {
                        document.getElementById('deviceready').querySelector('.received').innerHTML = err;
                        document.getElementById('deviceready').querySelector('.received').style.color='#FF0000';
                        document.getElementById('blinkFlashLabel').style.color = '#FF0000';
                        document.getElementById('blinkFlashLabel').innerHTML = 'Error. blinkFlash actions\' message is:\''+err+'\'';
                       }
        );
    },
    onClickBlinkWatch: function(event) {
        document.getElementById('blinkWatchLabel').style.color = '#000000';
        document.getElementById('blinkWatchLabel').innerHTML = 'Launching plugin function blinkWatch.....';
        event.stopImmediatePropagation();
        document.getElementById('deviceready').querySelector('.received').innerHTML = 'Watching.....';
        document.getElementById('deviceready').querySelector('.received').style.color = '#000000';
        cordova.plugins.CeruleanPlugins.blinkWatch(
         {pic:true,stats:false}, 
         function(msg) {
                        document.getElementById('deviceready').querySelector('.received').innerHTML = msg;
                        document.getElementById('blinkWatchLabel').style.color = '#00AA00';
                        document.getElementById('blinkWatchLabel').innerHTML = 'Success. blinkWatch actions\' message is:\''+msg+'\'';
                       },
         function(err) {
                        document.getElementById('deviceready').querySelector('.received').innerHTML = err;
                        document.getElementById('deviceready').querySelector('.received').style.color = '#FF0000;';
                        document.getElementById('blinkWatchLabel').style.color = '#FF0000';
                        document.getElementById('blinkWatchLabel').innerHTML = 'Error. blinkWatch actions\' message is:\''+err+'\'';
                       }
        );
    },
    // deviceready Event Handler
    //
    // Bind any cordova events here. Common events are:
    // 'pause', 'resume', etc.
    onDeviceReady: function() {
        document.getElementById('blinkFlashButton').addEventListener('click', this.onClickBlinkFlash.bind(this), false);
        document.getElementById('blinkFlashLabel').innerHTML = 'Click event handler calling <span class=\'strng\'>\'blinkFlash\'</span> action at service <span class=\'strng\'>\'CeruleanPlugins\'</span> is bound to blinkFlashButton...';
        document.getElementById('blinkWatchButton').addEventListener('click', this.onClickBlinkWatch.bind(this), false);
        document.getElementById('blinkWatchLabel').innerHTML = 'Click event handler calling <span class=\'strng\'>\'blinkWatch\'</span> action at service <span class=\'strng\'>\'CeruleanPlugins\'</span> isbound to blinkWatchButton...';
        this.receivedEvent('deviceready');
/*
        cordova.plugins.CeruleanPlugins.myMethod(
         'Plugin Ready!', 
         function(msg) {document.getElementById('deviceready').querySelector('.received').innerHTML = msg;},
         function(err) {document.getElementById('deviceready').innerHTML = '<p class="event received">' + err + '</p>';}
        );
*/
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
cordova.define("cw-swcore-ceruleanplugins.CeruleanPlugins", function(require, exports, module) {
/*
   Copyright 2017 Maxim Zhukov

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
var exec = require('cordova/exec');

exports.myMethod   = function(arg0, success, error) {exec(success, error, "CeruleanPlugins", "myMethod",   [arg0]);};
exports.blinkFlash = function(arg0, success, error) {exec(success, error, "CeruleanPlugins", "blinkFlash", [arg0]);};
exports.blinkWatch = function(arg0, success, error) {exec(success, error, "CeruleanPlugins", "blinkWatch", [arg0]);};





/*----------------------------------------------------*/
/*Pure JS stuff:--------------------------------------*/
exports.myMethodJS = function(arg0, success, error) {
  // Do something
  success(arg0+'<==Success callback called, from myMethodJS.');
};
/*End of pure JS stuff:-------------------------------*/
/*----------------------------------------------------*/

});

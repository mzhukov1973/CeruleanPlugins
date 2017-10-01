cordova.define("cw.plugins.cam2plug.Cam2Plug", function(require, exports, module) {
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
var exec = require('cordova/exec');

exports.coolMethod         = function(arg0, success, error) {exec(success, error, "Cam2Plug", "coolMethod", [arg0]);};
exports.startVideo         = function(success, error) {exec(success, error, "Cam2Plug", "startVideo");};
exports.stopVideo          = function(success, error) {exec(success, error, "Cam2Plug",  "stopVideo");};
exports.isFullyInitialised = function(success, error) {exec(success, error, "Cam2Plug",  "isFullyInitialised");}; /*To poll for result*/
exports.isFullyInitialized = isFullyInitialised; /*There are all sorts of people*/                                /*To poll for result*/

});

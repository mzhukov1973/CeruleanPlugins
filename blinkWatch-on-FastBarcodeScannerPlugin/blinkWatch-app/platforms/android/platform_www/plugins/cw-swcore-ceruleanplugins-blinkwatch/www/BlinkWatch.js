cordova.define("cw-swcore-ceruleanplugins-blinkwatch.BlinkWatch", function(require, exports, module) {
function successCallback(callback) {
 if (!callback) {return null;}
 return callback(null, "Succsess!");
}

function errorCallback(callback) {
  if (!callback) {return null;}
  return function(error) {
    var errorCode = parseInt(error); 
    var BlinkWatchError = {};
    switch (errorCode) {
      case 0:  BlinkWatchError = {name: 'UNEXPECTED_ERROR',          code: 0, _message: 'BlinkWatch experienced an unexpected error.'}; break;
      default: BlinkWatchError = {name: 'UNEXPECTED_ERROR',          code: 0, _message: 'BlinkWatch returned an invalid error code.'};  break;
    }
    callback(BlinkWatchError, null);
  };
}

/* To use these callbacks do it like this (this is entirely optional, you may supply your own success and error handling functions, just uncomment the other version of the export): */
/*===================================================================================================================================================================================*/
/*                                                                                                                                                                                   */
/*                                                                                                                                                                                   */
/* function displayPluginCallResults(error_txt, success_txt) {                                                                                                                       */
/*  if (error_txt != null) {console.log('cw-sqcore-ceruleanplugins-blinkwatch: ERROR! (name:'+error_txt.name+', code:'+error_txt.code+', message:\''+error_txt._message+'\')');}     */
/*  else                   {console.log('cw-sqcore-ceruleanplugins-blinkwatch: SUCCESS! (message:\''+JSON.stringify(success_txt)+'\')');}                                            */
/* }                                                                                                                                                                                 */
/* cordova.plugins.CeruleanPlugins.BlinkWatch.activityname(displayPluginCallResults,[extraArgs]);                                                                                    */
/*                                                                                                                                                                                   */
/*                                                                                                                                                                                   */
/* OR if using user-supplied success and error handling functions:                                                                                                                   */
/*                                                                                                                                                                                   */
/*                                                                                                                                                                                   */
/* function successCallback(success) {                                                                                                                                               */
/*  ....                                                                                                                                                                             */
/* }                                                                                                                                                                                 */
/* function errorCallback(error) {                                                                                                                                                   */
/*  ....                                                                                                                                                                             */
/* }                                                                                                                                                                                 */
/* cordova.plugins.CeruleanPlugins.BlinkWatch.activityname(successCallback,errorCallback,[extraArgs]);                                                                               */
/*                                                                                                                                                                                   */
/*                                                                                                                                                                                   */
/*===================================================================================================================================================================================*/
/* Where <activityname> is the name of the method of BlinkWatch java class that extends CorodvaPlugin class and [extraArgs] - optional arguments to be passed to the plugin.         */


var exec = require('cordova/exec');
/*exports.activity1      = function(callback,arg)                       { exec(successCallback(callback),errorCallback(callback),'BlinkWatch','activity1',[arg]); }; */
/*exports.activity2      = function(callback)                           { exec(successCallback(callback),errorCallback(callback),'BlinkWatch','activity2',[]   ); }; */
/* OR to use user-supplied success and error handling functions:                                                                                                     */
/*exports.activity1      = function(successCallback,errorCallback,arg)  { exec(successCallback,errorCallback,'BlinkWatch','activity1',[arg]); };                     */
/*exports.activity2      = function(successCallback,errorCallback)      { exec(successCallback,errorCallback,'BlinkWatch','activity2',[]   ); };                     */

exports.showToast = function(toastText) { exec(null,null,'BlinkWatch','showToast' [{'data': toastText}]); };
/*exports.startScanning = function(callback) { exec(successCallback(callback),errorCallback(callback),'BlinkWatch','startScanning',[]); };*/
exports.startScanning = function(successCallback,errorCallback)  { exec(successCallback,errorCallback,'BlinkWatch','startScanning',[]); };
/*exports.stopScanning  = function(callback) { exec(successCallback(callback),errorCallback(callback),'BlinkWatch','stopScanning',[]); };*/
exports.stopScanning  = function(successCallback, errorCallback) { exec(successCallback,errorCallback,'BlinkWatch','stopScanning',[]); };
});

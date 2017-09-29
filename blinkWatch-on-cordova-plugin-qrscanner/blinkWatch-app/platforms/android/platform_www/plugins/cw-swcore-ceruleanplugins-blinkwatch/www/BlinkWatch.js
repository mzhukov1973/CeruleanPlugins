cordova.define("cw-swcore-ceruleanplugins-blinkwatch.BlinkWatch", function(require, exports, module) {
function stringToBool(string) {switch (string) { case '1': return true; case '0': return false; default: throw new Error('BlinkWatch plugin returned an invalid boolean number-string: ' + string); }}

/* Converts the returned ['string':'string'] dictionary to a status object. */
function convertStatus(statusDictionary) {
  return {
    authorized: stringToBool(statusDictionary.authorized),
    denied: stringToBool(statusDictionary.denied),
    restricted: stringToBool(statusDictionary.restricted),
    prepared: stringToBool(statusDictionary.prepared),
    scanning: stringToBool(statusDictionary.scanning),
    previewing: stringToBool(statusDictionary.previewing),
    showing: stringToBool(statusDictionary.showing),
    lightEnabled: stringToBool(statusDictionary.lightEnabled),
    canOpenSettings: stringToBool(statusDictionary.canOpenSettings),
    canEnableLight: stringToBool(statusDictionary.canEnableLight),
    canChangeCamera: stringToBool(statusDictionary.canChangeCamera),
    currentCamera: parseInt(statusDictionary.currentCamera)
  };
}

/* Simple utility method to ensure the background is transparent. Used by the plugin to force re-rendering immediately after the native webview background is made transparent.*/
function clearBackground() {var body = document.body; if (body.style) {body.style.backgroundColor = 'rgba(0,0,0,0.01)'; body.style.backgroundImage = ''; setTimeout(function() { body.style.backgroundColor = 'transparent'; }, 1); if (body.parentNode && body.parentNode.style) { body.parentNode.style.backgroundColor = 'transparent'; body.parentNode.style.backgroundImage = ''; }}}

function errorCallback(callback) {
  if (!callback) {return null;}
  return function(error) {
    var errorCode = parseInt(error); var BlinkWatchError = {};
    switch (errorCode) {
      case 0:  BlinkWatchError = {name: 'UNEXPECTED_ERROR',          code: 0, _message: 'BlinkWatch experienced an unexpected error.'}; break;
      case 1:  BlinkWatchError = {name: 'CAMERA_ACCESS_DENIED',      code: 1, _message: 'The user denied camera access.'};              break;
      case 2:  BlinkWatchError = {name: 'CAMERA_ACCESS_RESTRICTED',  code: 2, _message: 'Camera access is restricted.'};                break;
      case 3:  BlinkWatchError = {name: 'BACK_CAMERA_UNAVAILABLE',   code: 3, _message: 'The back camera is unavailable.'};             break;
      case 4:  BlinkWatchError = {name: 'FRONT_CAMERA_UNAVAILABLE',  code: 4, _message: 'The front camera is unavailable.'};            break;
      case 5:  BlinkWatchError = {name: 'CAMERA_UNAVAILABLE',        code: 5, _message: 'The camera is unavailable.'};                  break;
      case 6:  BlinkWatchError = {name: 'SCAN_CANCELED',             code: 6, _message: 'Scan was canceled.'};                          break;
      case 7:  BlinkWatchError = {name: 'LIGHT_UNAVAILABLE',         code: 7, _message: 'The device light is unavailable.'};            break;
      case 8:  BlinkWatchError = {name: 'OPEN_SETTINGS_UNAVAILABLE', code: 8, _message: 'The device is unable to open settings.'};      break; /* Open settings is only available on iOS 8.0+. */
      default: BlinkWatchError = {name: 'UNEXPECTED_ERROR',          code: 0, _message: 'BlinkWatch returned an invalid error code.'};  break;
}
    callback(BlinkWatchError);
  };
}

function successCallback(callback)     { if (!callback) {return null;} return function(statusDict) {                                 callback(null, convertStatus(statusDict)); }; }
function doneCallback(callback, clear) { if (!callback) {return null;} return function(statusDict) { if (clear) {clearBackground();} callback(null, convertStatus(statusDict)); }; }

var exec = require('cordova/exec');
/*exports.blinkWatch = function(arg0, success, error) {exec(success, error, "BlinkWatch", "watch", [arg0]);};*/

exports.prepare        = function(callback)        {exec(successCallback(callback), errorCallback(callback), 'BlinkWatch', 'prepare', []);        };
exports.destroy        = function(callback)        {exec(doneCallback(callback, true), null, 'BlinkWatch', 'destroy', []);                        };
exports.scan           = function(callback)        {if (!callback) {throw new Error('No callback provided to scan method.');} var success=function(result) {callback(null, result);}; exec(success, errorCallback(callback), 'BlinkWatch', 'scan', []);};
exports.cancelScan     = function(callback)        {exec(doneCallback(callback), null, 'BlinkWatch', 'cancelScan', []);                           };
exports.show           = function(callback)        {exec(doneCallback(callback, true), null, 'BlinkWatch', 'show', []);                           };
exports.hide           = function(callback)        {exec(doneCallback(callback, true), null, 'BlinkWatch', 'hide', []);                           };
exports.pausePreview   = function(callback)        {exec(doneCallback(callback), null, 'BlinkWatch', 'pausePreview', []);                         };
exports.resumePreview  = function(callback)        {exec(doneCallback(callback), null, 'BlinkWatch', 'resumePreview', []);                        };
exports.useCamera      = function(index, callback) {exec(successCallback(callback), errorCallback(callback), 'BlinkWatch', 'useCamera', [index]); };
exports.useFrontCamera = function(callback)        {var frontCamera=1; if (callback) {this.useCamera(frontCamera, callback);} else {exec(null, null, 'BlinkWatch', 'useCamera', [frontCamera]);}      };
exports.useBackCamera  = function(callback)        {var backCamera=0;  if (callback) {this.useCamera(backCamera, callback);}  else {exec(null, null, 'BlinkWatch', 'useCamera', [backCamera]); }      };
exports.openSettings   = function(callback)        {if (callback) {exec(successCallback(callback), errorCallback(callback), 'BlinkWatch', 'openSettings', []);} else {exec(null, null, 'BlinkWatch', 'openSettings', []);}};
exports.getStatus      = function(callback)        {if (!callback) {throw new Error('No callback provided to getStatus method.');} exec(doneCallback(callback), null, 'BlinkWatch', 'getStatus', []); };
exports.enableLight    = function(callback)        {exec(successCallback(callback), errorCallback(callback), 'BlinkWatch', 'enableLight', []);     };
exports.disableLight   = function(callback)        {exec(successCallback(callback), errorCallback(callback), 'BlinkWatch', 'disableLight', []);     };

});

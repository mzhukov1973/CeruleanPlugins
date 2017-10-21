#!/bin/bash

#Plugin remove-add:
cordova plugin remove cw.plugins.cam2plug --searchpath ../cam2plug --verbose --save
#cordova plugin add cw.plugins.cam2plug@0.0.2-alpha1 --searchpath ../cam2plug --noregistry --verbose --save

#cordova build android --debug  --device -- --keystore=$mz_keyStore --storePassword=$mz_keyStorePassword --alias=$mz_keyAlias --password=$mz_keyAliasPassword
#cordova run   android --debug  --device -- --keystore=$mz_keyStore --storePassword=$mz_keyStorePassword --alias=$mz_keyAlias --password=$mz_keyAliasPassword
#cordova run   android --debug  --noprepare --nobuild --device -d

rm -f ./platforms/android/*signing.properties

killall -HUP 'java'

~/GitHub/sounds/alarm.sh

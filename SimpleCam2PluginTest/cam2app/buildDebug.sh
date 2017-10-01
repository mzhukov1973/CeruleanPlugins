#!/bin/bash

#cordova plugin remove cw.plugins.cam2plug --verbose --save

#cordova plugin add cw.plugins.cam2plug@0.0.2-alpha1 --searchpath ../cam2plug --noregistry --verbose --save
#plugman install --platform android --project . --plugin ../cam2plug --debug
#plugman install --platform android --project . --plugin cw.plugins.cam2plug --searchpath ../cam2plug --debug 
##cordova plugin add ../cam2plug --noregistry --verbose --save

#cordova build android --debug  --device -- --keystore=$mz_keyStore --storePassword=$mz_keyStorePassword --alias=$mz_keyAlias --password=$mz_keyAliasPassword
#cordova run   android --debug           -- --keystore=$mz_keyStore --storePassword=$mz_keyStorePassword --alias=$mz_keyAlias --password=$mz_keyAliasPassword
cordova run   android --debug  --device -- --keystore=$mz_keyStore --storePassword=$mz_keyStorePassword --alias=$mz_keyAlias --password=$mz_keyAliasPassword
#cordova run   android --debug  --noprepare --nobuild --device -d
rm -f ./platforms/android/*signing.properties

#!/bin/bash

cordova plugin remove cw-swcore-ceruleanplugins-blinkflash --save
cordova plugin add ../blinkFlash-plugin --save

#cordova build android --debug  --device -- --keystore=$mz_keyStore --storePassword=$mz_keyStorePassword --alias=$mz_keyAlias --password=$mz_keyAliasPassword
#cordova run   android --debug           -- --keystore=$mz_keyStore --storePassword=$mz_keyStorePassword --alias=$mz_keyAlias --password=$mz_keyAliasPassword
#cordova run   android --debug  --device -- --keystore=$mz_keyStore --storePassword=$mz_keyStorePassword --alias=$mz_keyAlias --password=$mz_keyAliasPassword
cordova run   android --debug  --noprepare --nobuild --device -d
rm -f ./platforms/android/*signing.properties

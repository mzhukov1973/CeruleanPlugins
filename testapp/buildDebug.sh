#!/bin/bash

#cordova plugin remove cw-swcore-ceruleanplugins --save
#cordova plugin add ../CeruleanPlugins --save

#cordova plugin remove cw-swcore-ceruleanplugins-blinkwatch --save
#cordova plugin add ../blinkWatch --save


#cordova build android --debug  --device -- --keystore=$mz_keyStore --storePassword=$mz_keyStorePassword --alias=$mz_keyAlias --password=$mz_keyAliasPassword
#cordova run   android --debug           -- --keystore=$mz_keyStore --storePassword=$mz_keyStorePassword --alias=$mz_keyAlias --password=$mz_keyAliasPassword
#cordova run   android --debug  --device -- --keystore=$mz_keyStore --storePassword=$mz_keyStorePassword --alias=$mz_keyAlias --password=$mz_keyAliasPassword
cordova run   android --debug  --noprepare --nobuild --device -d
rm -f ./platforms/android/*signing.properties

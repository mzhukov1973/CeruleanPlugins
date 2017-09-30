#!/bin/bash

cordova plugin remove cw.plugins.cam2plug --save
cordova plugin add ../cam2plug --save

cordova build android --debug  --device -- --keystore=$mz_keyStore --storePassword=$mz_keyStorePassword --alias=$mz_keyAlias --password=$mz_keyAliasPassword
#cordova run   android --debug           -- --keystore=$mz_keyStore --storePassword=$mz_keyStorePassword --alias=$mz_keyAlias --password=$mz_keyAliasPassword
#cordova run   android --debug  --device -- --keystore=$mz_keyStore --storePassword=$mz_keyStorePassword --alias=$mz_keyAlias --password=$mz_keyAliasPassword
#cordova run   android --debug  --noprepare --nobuild --device -d

#!/bin/bash

cordova plugin remove cw-swcore-ceruleanplugins --save
cordova plugin add ../CeruleanPlugins --save

#cordova build android --debug  --device -- --keystore= --storePassword= --alias= --password=
#cordova run   android --debug           -- --keystore= --storePassword= --alias= --password=
cordova run   android --debug  --device -- --keystore= --storePassword= --alias= --password=

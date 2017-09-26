<!--
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
-->
# Cerulean Plugins

**Cerulean Plugins** is a plugins suite for Apache Cordova (at the moment it's Android only), developed to be used in **CeruleanWhisper** protected comms software.

Programming languages thus include Java, Androids' RenderScript, C/C++, shell integration, HTML5, CSS3+, JS(ES6,ES7+).

## blinkFlash

###### Version 0.1.1

:warning: Report issues on the [Cerulean Plugins issue tracker](https://github.com/mzhukov1973/CeruleanPlugins/issues)

## Requires
- cordova-android >6.2.0
- Java JDK 1.7 or greater
- Android SDK [http://developer.android.com](http://developer.android.com)

## ToDo:
- [x] ~~Create a Cordova Android dedicated testing app for the blinkFlash plugin.~~
- [x] ~~Move blinkFlash code to a separate Cordova project.~~
- [x] ~~Move blinkFlash plugin and it corresponding testing app to a separate folder.~~
- [ ] Cleanup exceptions handling in plugins' java code - currently it's a mess.
- [ ] Add some primitive morse code capability to testing app.
- [ ] Set up some UI elements to controll overall blink-speed.
- [ ] Add simple pattern editing controls to testing apps' UI:
  - [ ] General blinks sequence.
  - [ ] Morse code (including ASCII text input).

#!/bin/bash

adb logcat -b all -v long -v color --pid=$1 *:V

# UI Inspector system (WIP)

#### Overview

This library is for developers, designers and testers to inspect the UI of an Android app. It requires a library be added to the app and a viewer app running on an Android tablet or on a computer.

It works by sending a JSON file containing data about parameters, etc to the viewer app which reconstructs the UI.

* Android app library
  * [Readme and source code](https://github.com/raybritton/ui-inspector-android-library)
  * Download: See readme
* Viewer app for Android Tablet
  * [Readme and source code](https://github.com/raybritton/ui-inspector-android-viewer)
  * [Download](https://play.google.com/store/apps/details?id=com.raybritton.uiinspectorserver) (join via https://play.google.com/apps/testing/com.raybritton.uiinspectorserver first) 
* Viewer app for Desktop
  * [Readme and source code](https://github.com/raybritton/ui-inspector-jvm-viewer)
  * Download: 
* Viewer app for Desktop (CLI)
  * [Readme and source code](https://github.com/raybritton/ui-inspector-jvm-cli-viewer)
  * Download: https://github.com/raybritton/ui-inspector-jvm-cli-viewer/releases

#### Communication

* [SYN/ACK Standard](https://github.com/raybritton/ui-inspector-android-library/blob/master/SYN_ACK.md)
* [UI JSON Standard](https://github.com/raybritton/ui-inspector-android-library/blob/master/JSON_STANDARD.md)

## Android Viewer

### Usage

(Once both viewer and library are on the same LAN)
1. Open viewer
2. Start inspection in library
3. Select device from dialog on viewer

### Download

Currently this viewer is in beta, so you have to join the beta program [here](https://play.google.com/apps/testing/com.raybritton.uiinspectorserver) first, then download the app from the [play store](https://play.google.com/store/apps/details?id=com.raybritton.uiinspectorserver)

### License

```
Copyright 2017 Ray Britton

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

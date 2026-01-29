# debugkit

[![Android CI status](https://github.com/lokahe/debugkit/actions/workflows/maven.yml/badge.svg?branch=main)](https://github.com/lokahe/debugkit/actions/workflows/maven.yml)
![](https://img.shields.io/badge/Android-21%20--%2036-green.svg)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.lokahe/io.github.lokahe.debugkit)](https://central.sonatype.com/namespace/io.github.lokahe)\
![Android](https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-%237F52FF.svg?logo=kotlin&logoColor=white)

a debug took kit for android app development.

usage
-----
```build.gradle```\
dependencies {
```
    debugImplementation 'io.github.lokahe:debugkit:1.0.2'
```
local usage/debug
-----
local build (release)
```
./gradlew :library:assembleRelease
```
local build (debug)
```
./gradlew :library:assembleDebug
```
local publish
```
 ./gradlew publishToMavenCentral
```
local publish (debug)
```
./gradlew publishToMavenLocal
```

tools
-----
ViewHierarchyUtils
- log view hierarchy
```
2026-01-21 12:53:06.979  4760-4760  ViewHierarchyUtils      com.android.example         D  [2]					x:0,y:48,w:1600,h:2512			visible		enable	unclickable	lp.w/h:MATCH_PARENT/MATCH_PARENT	resId:(-1)													com.android.internal.policy.DecorView@124879460
2026-01-21 12:53:06.980  4760-4760  ViewHierarchyUtils      com.android.example         D  0[2]				x:0,y:48,w:1600,h:2512			visible		enable	unclickable	lp.w/h:MATCH_PARENT/MATCH_PARENT	resId:(-1)													android.widget.LinearLayout@241642701
2026-01-21 12:53:06.980  4760-4760  ViewHierarchyUtils      com.android.example         D  00[0]				x:0,y:48,w:0,h:0				gone		enable	unclickable	lp.w/h:MATCH_PARENT/WRAP_CONTENT	resId:action_mode_bar_stub(16908777)						android.view.ViewStub@86161794
2026-01-21 12:53:06.981  4760-4760  ViewHierarchyUtils      com.android.example         D  01[2]				x:0,y:48,w:1600,h:2512			visible		enable	unclickable	lp.w/h:MATCH_PARENT/MATCH_PARENT	resId:content(16908290)										android.widget.FrameLayout@14338195
2026-01-21 12:53:06.981  4760-4760  ViewHierarchyUtils      com.android.example         D  010[2]				x:0,y:48,w:1600,h:2512			visible		enable	unclickable	lp.w/h:MATCH_PARENT/MATCH_PARENT	resId:parentPanel(16909418)									android.widget.LinearLayout@183918800
...
```

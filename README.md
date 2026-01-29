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
    debugImplementation 'io.github.lokahe:debugkit:1.0.3'
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
### ReflectUtils
base on [AndroidHiddenApiBypass](https://github.com/LSPosed/AndroidHiddenApiBypass/tree/main)
- set private / static final field
```agsl
... ReflectUtils ... D  InputMethodService field: mStylusHwSessionsTimeout long private 10000 
... ReflectUtils ... D  InputMethodService field: mStylusHwSessionsTimeout long private 9999 
... ReflectUtils ... D  InputMethodService field: DEBUG boolean static final false 
... ReflectUtils ... D  InputMethodService field: DEBUG boolean static final true 
```
- invoke private method
```
... HiddenApiBypass ... D  invoke updateEditorToolTypeInternal with [2]
... MyInputMethodService ... D  onUpdateEditorToolType 2
```
- log members
```agsl
... ReflectUtils ... jp.co.omronsoft.iwnnime.ml           D  InputMethodService field: mActionClickListener                      OnClickListener                    final                android.inputmethodservice.InputMethodService$$ExternalSyntheticLambda4@872ffb6                                                                                                                                                                                                                                                                                                                  
... ReflectUtils ... jp.co.omronsoft.iwnnime.ml           D  InputMethodService field: mBackCallbackRegistered                   boolean                            private              true 
    ...
... ReflectUtils ... D  InputMethodService constructor: android.inputmethodservice.InputMethodService         InputMethodService                public                                                                                                                                                                                                                                                                                         
... ReflectUtils ... D  InputMethodService method: dispatchOnCurrentInputMethodSubtypeChanged            void                              private             class android.view.inputmethod.InputMethodSubtype                                                                                  
... ReflectUtils ... D  InputMethodService method: dispatchOnShowInputRequested                          boolean                           private             int, boolean   
    ...
```

### ViewHierarchyUtils
- log view hierarchy
```
... ViewHierarchyUtils ... D  [2]					x:0,y:48,w:1600,h:2512			visible		enable	unclickable	lp.w/h:MATCH_PARENT/MATCH_PARENT	resId:(-1)													com.android.internal.policy.DecorView@124879460
... ViewHierarchyUtils ... D  0[2]				x:0,y:48,w:1600,h:2512			visible		enable	unclickable	lp.w/h:MATCH_PARENT/MATCH_PARENT	resId:(-1)													android.widget.LinearLayout@241642701
... ViewHierarchyUtils ... D  00[0]				x:0,y:48,w:0,h:0				gone		enable	unclickable	lp.w/h:MATCH_PARENT/WRAP_CONTENT	resId:action_mode_bar_stub(16908777)						android.view.ViewStub@86161794
... ViewHierarchyUtils ... D  01[2]				x:0,y:48,w:1600,h:2512			visible		enable	unclickable	lp.w/h:MATCH_PARENT/MATCH_PARENT	resId:content(16908290)										android.widget.FrameLayout@14338195
... ViewHierarchyUtils ... D  010[2]				x:0,y:48,w:1600,h:2512			visible		enable	unclickable	lp.w/h:MATCH_PARENT/MATCH_PARENT	resId:parentPanel(16909418)									android.widget.LinearLayout@183918800
...
```

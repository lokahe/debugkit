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
... ReflectUtils ... D  InputMethodService field: mActionClickListener                      OnClickListener                    final                android.inputmethodservice.InputMethodService$$ExternalSyntheticLambda4@872ffb6                                                                                                                                                                                                                                                                                                                  
... ReflectUtils ... D  InputMethodService field: mBackCallbackRegistered                   boolean                            private              true 
    ...
... ReflectUtils ... D  InputMethodService constructor: android.inputmethodservice.InputMethodService         InputMethodService                public                                                                                                                                                                                                                                                                                         
... ReflectUtils ... D  InputMethodService method: dispatchOnCurrentInputMethodSubtypeChanged            void                              private             class android.view.inputmethod.InputMethodSubtype                                                                                  
... ReflectUtils ... D  InputMethodService method: dispatchOnShowInputRequested                          boolean                           private             int, boolean   
    ...
```

### DebugKit ```Debugkit.getInstance(this).setDecorView(getDecorView())```
・show a floating button on screen to toggle on/off of the view of the DecorView's hierarchy for showing information to debug
・move the views for view the hierarchy directly
<img src="./screenshot01.png" width="160" /><img src="./screenshot02.png" width="160" /><img src="./screenshot03.png" width="160" />

### ViewHierarchyUtils
- log view hierarchy ```ViewHierarchyUtils.logAllSubViews(getDecorView())```
```
... ViewHierarchyUtils ... D             [2]  x:0,y:152,w:1080,h:2272  visible   enable unclickable MATCH_PARENT/MATCH_PARENT (-1)                                   com.android.internal.policy.DecorView@55831969 
... ViewHierarchyUtils ... D 0           [2]  x:0,y:152,w:1080,h:2272  visible   enable unclickable MATCH_PARENT/MATCH_PARENT (-1)                                   android.widget.LinearLayout@145320902          
... ViewHierarchyUtils ... D 00          [0]  x:0,y:152,w:0,h:0        gone      enable unclickable MATCH_PARENT/WRAP_CONTENT action_mode_bar_stub                   android.view.ViewStub@1927047                  
... ViewHierarchyUtils ... D 01          [2]  x:0,y:152,w:1080,h:2272  visible   enable unclickable MATCH_PARENT/MATCH_PARENT content                                android.widget.FrameLayout@192571060           
... ViewHierarchyUtils ... D 010         [2]  x:0,y:152,w:1080,h:2272  visible   enable unclickable MATCH_PARENT/MATCH_PARENT parentPanel                            android.widget.LinearLayout@27312861           
    ...
```

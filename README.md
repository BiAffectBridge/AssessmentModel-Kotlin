# Assessment Model

This repo contains Sage Bionetwork's cross-platform architecture for serializing 
and displaying assessments run on mobile devices and used in scientific research. 
This repo was originally designed to use Kotlin multiplatform for both the Android 
and iOS implementations, but has since been restructured to include only the Android
implementation.

## iOS

The parallel architecture for iOS is located here:
[AssessmentModel-Swift](http://github/BiAffectBridge/AssessmentModel-Swift)

## Android

The application can be built and executed on a device or emulator using Android Studio 3.2 or higher.
One can also compile the application and run tests from the command line:

```
   > ./gradlew :androidApp:build
```


# R8 rules for the androidTest APK (wired up with testProguardFiles in build.gradle).
#
# mockito-core pulls in Byte Buddy and its agent, which reference JVM-only APIs
# (java.lang.instrument, java.lang.Module, JNA, SpotBugs annotations).
# None of them exist on Android and none are called there, so silence the warnings.
-dontwarn com.sun.jna.**
-dontwarn edu.umd.cs.findbugs.annotations.SuppressFBWarnings
-dontwarn java.lang.Module
-dontwarn java.lang.instrument.**
-dontwarn org.mockito.internal.creation.bytebuddy.inject.MockMethodDispatcher

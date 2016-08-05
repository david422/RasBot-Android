LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := tutorial-1
LOCAL_SRC_FILES := tutorial-1.c
LOCAL_SHARED_LIBRARIES := gstreamer_android
LOCAL_LDLIBS := -llog -landroid
include $(BUILD_SHARED_LIBRARY)


GSTREAMER_SDK_ROOT := /Users/dawidpodolak/Desktop/Android/libraries/gstreamer-1.0-android-arm-1.9.1
GSTREAMER_SDK_ROOT_ANDROID := /Users/dawidpodolak/Library/Android/sdk

GSTREAMER_NDK_BUILD_PATH  := $(GSTREAMER_SDK_ROOT)/share/gst-android/ndk-build

include $(GSTREAMER_NDK_BUILD_PATH)/plugins.mk
GSTREAMER_PLUGINS         := $(GSTREAMER_PLUGINS_CORE) $(GSTREAMER_PLUGINS_PLAYBACK) $(GSTREAMER_PLUGINS_CODECS) $(GSTREAMER_PLUGINS_NET) $(GSTREAMER_PLUGINS_SYS) $(GSTREAMER_PLUGINS_CODECS_RESTRICTED)
G_IO_MODULES              := gnutls
GSTREAMER_EXTRA_DEPS      := gstreamer-video-1.0

GSTREAMER_ROOT            := /Users/dawidpodolak/Desktop/Android/libraries/gstreamer-1.0-android-arm-1.9.1
include $(GSTREAMER_NDK_BUILD_PATH)/gstreamer-1.0.mk

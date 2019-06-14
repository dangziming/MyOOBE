LOCAL_PATH := $(my-dir)
include $(CLEAR_VARS)

LOCAL_PACKAGE_NAME := NeostraOOBE

LOCAL_MODULE_TAGS := optional

LOCAL_CERTIFICATE := platform

LOCAL_STATIC_JAVA_LIBRARIES := neostraCustomization  hidden libcore_hidden oobe android-support-v4

#setupwizardlib_dir := setupwizardlib

#res_dir := res $(setupwizardlib_dir)/res

LOCAL_SDK_VERSION := current

LOCAL_SRC_FILES := $(call all-java-files-under,src)

LOCAL_SRC_FILES += $(call all-java-files-under, setupwizardlib/src)

LOCAL_RESOURCE_DIR := ${LOCAL_PATH}/setupwizardlib/res ${LOCAL_PATH}/res
LOCAL_AAPT_FLAGS := --auto-add-overlay

LOCAL_AAPT_FLAGS += --extra-packages com.android.setupwizardlib


#LOCAL_OVERRIDES_PACKAGES := Provision

LOCAL_PRIVILEGED_MODULE := true

include $(BUILD_PACKAGE)

###########################################################

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
           neostraCustomization:libs/neostraCustomization.jar \
		   hidden:libs/hidden.jar \
		   libcore_hidden:libs/libcore_hidden.jar \
		   oobe:libs/oobe.jar

include $(BUILD_MULTI_PREBUILT)

include $(call all-makefiles-under,$(LOCAL_PATH))

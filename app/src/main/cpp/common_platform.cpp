/*==============================================================================
FMOD Example Framework
Copyright (c), Firelight Technologies Pty, Ltd 2013-2019.
==============================================================================*/
#include "common.h"
#include <string.h>
#include <jni.h>
#include <unistd.h>
#include <string>
#include <vector>
#include "3d.cpp"
#include "channel_groups.cpp"
#include "convolution_reverb.cpp"
#include "dsp_custom.cpp"
#include "dsp_effect_per_speaker.cpp"
#include "dsp_inspector.cpp"
#include "effects.cpp"
#include "gapless_playback.cpp"
#include "generate_tone.cpp"
#include "granular_synth.cpp"
#include "load_from_memory.cpp"
#include "multiple_speaker.cpp"
#include "multiple_system.cpp"
#include "net_stream.cpp"
#include "play_sound.cpp"
#include "play_stream.cpp"
#include "record.cpp"
#include "record_enumeration.cpp"
#include "user_created_sound.cpp"
#include "musicplayer.cpp"


JNIEnv *gJNIEnv;
jobject gMainActivityObject;
int gDownButtons;
int gLastDownButtons;
int gPressedButtons;
bool gSuspendState;
bool gQuitState;
std::string gUIString;
std::vector<char *> gPathList;

JNIEnv *getJNIEnv(){
    return gJNIEnv;
};
jobject getCallBack(){
    return gMainActivityObject;
};
int getStringHash(char * s){
    jstring text = gJNIEnv->NewStringUTF(s);
    if(gJNIEnv->ExceptionCheck()){
        gJNIEnv->ExceptionDescribe();
        gJNIEnv->ExceptionClear();
        return -1;
    }
    jclass mainActivityClass = gJNIEnv->GetObjectClass(gMainActivityObject);
    jmethodID methodID = gJNIEnv->GetMethodID(mainActivityClass, "getStringHash",
                                                          "(Ljava/lang/String;)I");

    int hash =gJNIEnv->CallIntMethod(gMainActivityObject, methodID, text);
//    LOGD("getStringHash %d %s",hash,s)
    gJNIEnv->DeleteLocalRef(text);
    gJNIEnv->DeleteLocalRef(mainActivityClass);
    return hash;
};
void Common_Init(void **extraDriverData) {
    LOGD("Common_Init");
    gDownButtons = 0;
    gLastDownButtons = 0;
    gPressedButtons = 0;
    gSuspendState = false;
    gQuitState = false;
}

void Common_Close() {
    for (std::vector<char *>::iterator item = gPathList.begin(); item != gPathList.end(); ++item) {
        free(*item);
    }

    gPathList.clear();

}

void jnitoJavaData(int key, char *data) {
    jstring text = gJNIEnv->NewStringUTF(data);
    if(gJNIEnv->ExceptionCheck()){
        gJNIEnv->ExceptionDescribe();
        gJNIEnv->ExceptionClear();
        return;
    }
    jclass mainActivityClass = gJNIEnv->GetObjectClass(gMainActivityObject);
    jmethodID updateScreenMethodID = gJNIEnv->GetMethodID(mainActivityClass, "getJniData",
                                                          "(ILjava/lang/String;)V");

    gJNIEnv->CallVoidMethod(gMainActivityObject, updateScreenMethodID, key, text);

    gJNIEnv->DeleteLocalRef(text);
    gJNIEnv->DeleteLocalRef(mainActivityClass);
}

void Common_Update() {
//    LOGE("Common_Update  %s",gUIString.c_str());
    jstring text = gJNIEnv->NewStringUTF(gUIString.c_str());
    if(gJNIEnv->ExceptionCheck()) {
        gJNIEnv->ExceptionDescribe();
        gJNIEnv->ExceptionClear();
        return;
    }
    jclass mainActivityClass = gJNIEnv->GetObjectClass(gMainActivityObject);
    jmethodID updateScreenMethodID = gJNIEnv->GetMethodID(mainActivityClass, "updateScreen",
                                                          "(Ljava/lang/String;)V");

    gJNIEnv->CallVoidMethod(gMainActivityObject, updateScreenMethodID, text);

    gJNIEnv->DeleteLocalRef(text);
    gJNIEnv->DeleteLocalRef(mainActivityClass);

    gUIString.clear();
    gPressedButtons = (gLastDownButtons ^ gDownButtons) & gDownButtons;
    gLastDownButtons = gDownButtons;
    if (gQuitState) {
        gPressedButtons |= (1 << BTN_QUIT);
    }
}

void Common_Sleep(unsigned int ms) {
    usleep(ms * 1000);
}

void Common_Exit(int returnCode) {
    exit(returnCode);
}

void Common_DrawText(const char *text) {
    char s[256];
    snprintf(s, sizeof(s), "%s\n", text);

    gUIString.append(s);
}

void Common_LoadFileMemory(const char *name, void **buff, int *length) {
    FILE *file = fopen(name, "rb");

    fseek(file, 0, SEEK_END);
    long len = ftell(file);
    fseek(file, 0, SEEK_SET);

    void *mem = malloc(len);
    fread(mem, 1, len, file);

    fclose(file);

    *buff = mem;
    *length = len;
}

void Common_UnloadFileMemory(void *buff) {
    free(buff);
}

bool Common_BtnPress(Common_Button btn) {
//    if (btn == 9) {
//        LOGD("Common_BtnPress  %d  ,  %d   ,  %d  ", gPressedButtons, btn,
//             (gPressedButtons & (1 << btn)));
//    }
    return ((gPressedButtons & (1 << btn)) != 0);
}

bool Common_BtnDown(Common_Button btn) {
    return ((gDownButtons & (1 << btn)) != 0);
}

const char *Common_BtnStr(Common_Button btn) {
    switch (btn) {
        case BTN_ACTION1:
            return "A";
        case BTN_ACTION2:
            return "B";
        case BTN_ACTION3:
            return "C";
        case BTN_ACTION4:
            return "D";
        case BTN_UP:
            return "Up";
        case BTN_DOWN:
            return "Down";
        case BTN_LEFT:
            return "Left";
        case BTN_RIGHT:
            return "Right";
        case BTN_MORE:
            return "E";
        case BTN_QUIT:
            return "Back";
        default:
            return "Unknown";
    }
}

const char *Common_MediaPath(const char *fileName) {
    char *filePath = (char *) calloc(256, sizeof(char));

    strcat(filePath, "file:///android_asset/");
    strcat(filePath, fileName);
    gPathList.push_back(filePath);

    return filePath;
}

const char *Common_WritePath(const char *fileName) {
    return Common_MediaPath(fileName);
}

bool Common_SuspendState() {
    return gSuspendState;
}



extern "C"
{
jstring
Java_com_example_cloud_fmoddemo_FmodActivity_getButtonLabel(JNIEnv *env, jobject thiz, jint index) {
    return env->NewStringUTF(Common_BtnStr((Common_Button) index));
}

void
Java_com_example_cloud_fmoddemo_FmodActivity_buttonDown(JNIEnv *env, jobject thiz, jint index) {
    gDownButtons |= (1 << index);
}

void Java_com_example_cloud_fmoddemo_FmodActivity_buttonUp(JNIEnv *env, jobject thiz, jint index) {
    gDownButtons &= ~(1 << index);
}

void Java_com_example_cloud_fmoddemo_FmodActivity_setStateCreate(JNIEnv *env, jobject thiz) {

}

void Java_com_example_cloud_fmoddemo_FmodActivity_setStateStart(JNIEnv *env, jobject thiz) {
    gSuspendState = false;
}

void Java_com_example_cloud_fmoddemo_FmodActivity_setStateStop(JNIEnv *env, jobject thiz) {
    gSuspendState = true;
}

void Java_com_example_cloud_fmoddemo_FmodActivity_setStateDestroy(JNIEnv *env, jobject thiz) {
    gQuitState = true;
}
JNIEXPORT void JNICALL
Java_com_example_cloud_fmoddemo_FmodActivity_mainExample(JNIEnv *env, jobject thiz,
                                                         jstring example_name_) {
    const char *example_name = env->GetStringUTFChars(example_name_, 0);
    gJNIEnv = env;
    gMainActivityObject = thiz;
    std::string name(example_name);
    LOGD("example_name = %s", name.c_str());
    if (name.compare("three_d") == 0) {
        FMOD_Main_3d();
    } else if (name.compare("channel_groups") == 0) {
        FMOD_Main_channel_groups();
    } else if (name.compare("convolution_reverb") == 0) {
        FMOD_Main_convolution_reverb();
    } else if (name.compare("dsp_custom") == 0) {
        FMOD_Main_dsp_custom();
    } else if (name.compare("dsp_effect_per_speaker") == 0) {
        FMOD_Main_dsp_effect_per_speaker();
    } else if (name.compare("dsp_inspector") == 0) {
        FMOD_Main_dsp_inspector();
    } else if (name.compare("effects") == 0) {
        FMOD_Main_effects();
    } else if (name.compare("gapless_playback") == 0) {
        FMOD_Main_gapless_playback();
    } else if (name.compare("generate_tone") == 0) {
        FMOD_Main_generate_tone();
    } else if (name.compare("granular_synth") == 0) {
        FMOD_Main_granular_synth();
    } else if (name.compare("load_from_memory") == 0) {
        FMOD_Main_load_from_memory();
    } else if (name.compare("multiple_speaker") == 0) {
        FMOD_Main_multiple_speaker();
    } else if (name.compare("multiple_system") == 0) {
        FMOD_Main_multiple_system();
    } else if (name.compare("net_stream") == 0) {
        FMOD_Main_net_stream();
    } else if (name.compare("play_sound") == 0) {
        FMOD_Main_play_sound();
    } else if (name.compare("play_stream") == 0) {
        FMOD_Main_play_stream();
    } else if (name.compare("record") == 0) {
        FMOD_Main_record();
    } else if (name.compare("record_enumeration") == 0) {
        FMOD_Main_record_enumeration();
    } else if (name.compare("user_created_sound") == 0) {
        FMOD_Main_user_created_sound();
    } else {
        FMOD_Main();
    }

    env->ReleaseStringUTFChars(example_name_, example_name);
}
} /* extern "C" */

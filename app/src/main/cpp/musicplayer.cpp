//
// Created by cloud on 2019/4/10.
//

#include <string>
#include "inc/fmod.hpp"
#include "common.h"

#define MODE_NORMAL 0
#define MODE_GAOGUAI1 1
#define MODE_LUOLI 2
#define MODE_DASHU 3
#define MODE_JINGSONG 4
#define MODE_KONGLING 5
#define MODE_GAOGUAI2 6

#define EVENT_FINISH 0
#define EVENT_USER_SELECT 1

bool playMode = true;//false 单曲，true循环
bool isPlay = false;
char *musicPath = 0;
char *nextPath = 0;
bool playNext = false;
int effect = 0;//普通，搞怪，萝莉，惊悚，大叔，空灵
bool use_effect_change = false;
int use_effect = 0;
float defaultfre;
float frequency = 1.3;//语速
float frequency2 = 0.8;
float luoli_param = 2.5;
float jingsong_parma = 0.5;
float dashu_parma = 0.8;
float kongling_parma1 = 300;
float kongling_parma2 = 20;
FMOD::DSP *dspluoli = 0;
FMOD::DSP *dspjingsong = 0;
FMOD::DSP *dspdashu = 0;
FMOD::DSP *dspkongling = 0;

void getparam(int type, char *result) {
    switch (type) {
        case MODE_NORMAL:
            break;
        case MODE_GAOGUAI1:
            sprintf(result, "%.2f", frequency);
            break;
        case MODE_LUOLI:
            sprintf(result, "%.2f", luoli_param);
            break;
        case MODE_DASHU:
            sprintf(result, "%.2f", dashu_parma);
            break;
        case MODE_JINGSONG:
            sprintf(result, "%.2f", jingsong_parma);
            break;
        case MODE_KONGLING:
            sprintf(result, "%.2f:%.2f", kongling_parma1, kongling_parma2);
            break;
        case MODE_GAOGUAI2:
            sprintf(result, "%.2f", frequency2);
            break;
    }
}

extern "C" {

JNIEXPORT void JNICALL
Java_com_example_cloud_fmoddemo_MusicPlayerActivity_setPlayMode(JNIEnv *env, jobject instance,
                                                                jboolean playMode_) {
    playMode = playMode_;
    LOGD("setPlayMode %d",playMode);
}

JNIEXPORT jboolean JNICALL
Java_com_example_cloud_fmoddemo_MusicPlayerActivity_getPlayMode(JNIEnv *env, jobject instance) {

    return playMode;

}

JNIEXPORT jint JNICALL
Java_com_example_cloud_fmoddemo_MusicPlayerActivity_getUseEffectIndex(JNIEnv *env, jobject instance) {
    return effect;
}

JNIEXPORT jint JNICALL
Java_com_example_cloud_fmoddemo_MusicPlayerActivity_playSong(JNIEnv *gJNIEnv, jobject instance,
                                                             jstring musicPath_) {
    const char *musicPath1 = gJNIEnv->GetStringUTFChars(musicPath_, 0);
    char *path = (char *) calloc(250, sizeof(char));
    strcpy(path, musicPath1);

    nextPath = path;
    playNext = true;
    jclass mainActivityClass = gJNIEnv->GetObjectClass(instance);
    jmethodID methodID = gJNIEnv->GetMethodID(mainActivityClass, "getStringHash",
                                              "(Ljava/lang/String;)I");
    int hash = gJNIEnv->CallIntMethod(instance, methodID, musicPath_);
    LOGD("playSong %d %s", hash, musicPath1)

    gJNIEnv->DeleteLocalRef(mainActivityClass);
    gJNIEnv->ReleaseStringUTFChars(musicPath_, musicPath1);
    return hash;
}
JNIEXPORT jstring JNICALL
Java_com_example_cloud_fmoddemo_MusicPlayerActivity_getEffect(JNIEnv *env, jobject instance, jint type) {
    if(type != MODE_NORMAL) {
        char result[40];
        LOGE("getEffect %d", type);
        getparam(type, result);
        LOGE("getEffect %d %s", type, result);
        return env->NewStringUTF(result);
    }else {
        return env->NewStringUTF("");
    }
}
JNIEXPORT void JNICALL
Java_com_example_cloud_fmoddemo_FmodActivity_useEffect(JNIEnv *env, jobject instance, jint type,
                                                       jstring param_) {
    const char *param = env->GetStringUTFChars(param_, 0);
    LOGD("useEffect  %d   %s",type,param)
    char c[50];
    strcpy(c, param);
    char *p;
    p = strtok(c, ":");//key:value
    float par[2];
    int i = 0;
    while (p) {
        sscanf(p, "%f", &par[i++]); // 将字符串转换成浮点数 fp = 15.455000
        LOGD("setEffect %f\n", par[i - 1]);
        p = strtok(NULL, ":");
    }
    switch (type) {
        case MODE_GAOGUAI1:
            frequency = par[0];
            break;
        case MODE_LUOLI:
            luoli_param = par[0];
            break;
        case MODE_DASHU:
            dashu_parma = par[0];
            break;
        case MODE_JINGSONG:
            jingsong_parma = par[0];
            break;
        case MODE_KONGLING:
            kongling_parma1 = par[0];
            kongling_parma2 = par[1];
            break;
        case MODE_GAOGUAI2:
            frequency2 = par[0];
            break;
    }
    use_effect = type;
    use_effect_change = true;
    env->ReleaseStringUTFChars(param_, param);
}

}

void close() {
    effect = 0;//普通，搞怪，萝莉，惊悚，大叔，空灵
    playMode = true;//false 单曲，true循环
    isPlay = false;
    frequency = 1.3;//语速
    frequency2 = 0.8;//语速
    dspluoli = 0;
    dspjingsong = 0;
    dspdashu = 0;
    dspkongling = 0;
}

int FMOD_Main() {
    FMOD::System *system = 0;
    FMOD::Sound *sound = 0;
    FMOD::Channel *channel = 0;
    FMOD::ChannelGroup *mastergroup = 0;
    FMOD_RESULT result;
    unsigned int version;
    void *extradriverdata = 0;
    Common_Init(&extradriverdata);

    /*
        Create a System object and initialize
    */
    result = FMOD::System_Create(&system);
    ERRCHECK(result);

    result = system->getVersion(&version);
    ERRCHECK(result);

    if (version < FMOD_VERSION) {
        Common_Fatal("FMOD lib version %08x doesn't match header version %08x", version,
                     FMOD_VERSION);
    }

    result = system->init(32, FMOD_INIT_NORMAL, extradriverdata);
    ERRCHECK(result);

    result = system->getMasterChannelGroup(&mastergroup);
    ERRCHECK(result);
    /*
        Create some effects to play with
    */
    result = system->createDSPByType(FMOD_DSP_TYPE_PITCHSHIFT, &dspluoli);
    ERRCHECK(result);
    //设置音调的参数
    dspluoli->setParameterFloat(FMOD_DSP_PITCHSHIFT_PITCH, luoli_param);
    result = system->createDSPByType(FMOD_DSP_TYPE_TREMOLO, &dspjingsong);
    ERRCHECK(result);
    dspjingsong->setParameterFloat(FMOD_DSP_TREMOLO_SKEW, jingsong_parma);
    result = system->createDSPByType(FMOD_DSP_TYPE_PITCHSHIFT, &dspdashu);
    dspdashu->setParameterFloat(FMOD_DSP_PITCHSHIFT_PITCH, dashu_parma);
    ERRCHECK(result);
    result = system->createDSPByType(FMOD_DSP_TYPE_ECHO, &dspkongling);
    dspkongling->setParameterFloat(FMOD_DSP_ECHO_DELAY, kongling_parma1);
    dspkongling->setParameterFloat(FMOD_DSP_ECHO_FEEDBACK, kongling_parma2);
    ERRCHECK(result);

    /*
        Add them to the master channel group.  Each time an effect is added (to position 0) it pushes the others down the list.
    */
    result = mastergroup->addDSP(0, dspluoli);
    ERRCHECK(result);
    result = mastergroup->addDSP(0, dspjingsong);
    ERRCHECK(result);
    result = mastergroup->addDSP(0, dspdashu);
    ERRCHECK(result);
    result = mastergroup->addDSP(0, dspkongling);
    ERRCHECK(result);

    //false 开启音效  true 暂停音效
    result = dspluoli->setBypass(true);
    ERRCHECK(result);
    result = dspjingsong->setBypass(true);
    ERRCHECK(result);
    result = dspdashu->setBypass(true);
    ERRCHECK(result);
    result = dspkongling->setBypass(true);
    ERRCHECK(result);
    do {
        LOGD("UPDATE")
        if (playNext && nextPath) {
            if (sound) {
                result = sound->release();
                ERRCHECK(result);
                sound = 0;
            }
            if (musicPath) {
                JNIEnv *env = getJNIEnv();
                jobject callBack = getCallBack();
                jclass objcls = env->GetObjectClass(callBack);
                jmethodID mid = env->GetMethodID(objcls, "onPlayFinish", "(II)V");
                env->CallVoidMethod(callBack, mid, getStringHash(musicPath),EVENT_USER_SELECT);
                delete (musicPath);
            }
            musicPath = nextPath;
            result = system->createSound(musicPath, FMOD_DEFAULT, 0, &sound);
            ERRCHECK(result);
            result = system->playSound(sound, 0, false, &channel);
            ERRCHECK(result);
            channel->getFrequency(&defaultfre);
            JNIEnv *env = getJNIEnv();
            jobject callBack = getCallBack();
            jclass objcls = env->GetObjectClass(callBack);
            jmethodID mid = env->GetMethodID(objcls, "onPlayStart", "(I)V");
            env->CallVoidMethod(callBack, mid, getStringHash(musicPath));
            if (effect == MODE_GAOGUAI1) {
                float temp;
                temp = defaultfre * frequency;
                channel->setFrequency(temp);
            } else if (effect == MODE_GAOGUAI2) {
                float temp1;
                temp1 = defaultfre * frequency2;
                channel->setFrequency(temp1);
            }
            nextPath = 0;
            playNext = false;
        }
        Common_Update();
        if (Common_BtnPress(BTN_MORE)&&channel)//E
        {
            //播放/暂停
            LOGD("BTN_MORE  A");
            bool paused;

            result = channel->getPaused(&paused);
            ERRCHECK(result);

            paused = !paused;

            result = channel->setPaused(paused);
            ERRCHECK(result);
            JNIEnv *env = getJNIEnv();
            jobject callBack = getCallBack();
            jclass objcls = env->GetObjectClass(callBack);
            jmethodID mid = env->GetMethodID(objcls, paused?"onPlayPause":"onPlayStart", "(I)V");
            env->CallVoidMethod(callBack, mid, getStringHash(musicPath));
        }
        if (Common_BtnPress(BTN_LEFT) || Common_BtnPress(BTN_RIGHT) || use_effect_change) {
            switch (effect) {
                case MODE_GAOGUAI1:
                case MODE_GAOGUAI2:
                    channel->setFrequency(defaultfre);
                    LOGD("setFrequency l");
                    break;
                case MODE_LUOLI:
                    dspluoli->setBypass(true);
                    break;
                case MODE_JINGSONG:
                    dspjingsong->setBypass(true);
                    break;
                case MODE_DASHU:
                    dspdashu->setBypass(true);
                    break;
                case MODE_KONGLING:
                    dspkongling->setBypass(true);
                    break;
            }
            LOGD(" pre effect %d", effect);
            if (Common_BtnPress(BTN_LEFT)) {//上一个音效
                effect = ((effect == 0) ? 6 : ((effect - 1) % 7));
            } else if (Common_BtnPress(BTN_RIGHT)){//下一个音效
                effect = ((effect + 1) % 7);
            } else {
                effect = use_effect;
                use_effect_change = false;
            }
            switch (effect) {
                case MODE_GAOGUAI1:
                    float temp;
                    temp = defaultfre * frequency;
                    channel->setFrequency(temp);
                    break;
                case MODE_LUOLI:
                    dspluoli->setParameterFloat(FMOD_DSP_PITCHSHIFT_PITCH, luoli_param);
                    dspluoli->setBypass(false);
                    break;
                case MODE_JINGSONG:
                    dspjingsong->setParameterFloat(FMOD_DSP_TREMOLO_SKEW, jingsong_parma);
                    dspjingsong->setBypass(false);
                    break;
                case MODE_DASHU:
                    dspdashu->setParameterFloat(FMOD_DSP_PITCHSHIFT_PITCH, dashu_parma);
                    dspdashu->setBypass(false);
                    break;
                case MODE_KONGLING:
                    dspkongling->setParameterFloat(FMOD_DSP_ECHO_DELAY, kongling_parma1);
                    dspkongling->setParameterFloat(FMOD_DSP_ECHO_FEEDBACK, kongling_parma2);
                    dspkongling->setBypass(false);
                    break;
                case MODE_GAOGUAI2:
                    float temp1;
                    temp1 = defaultfre * frequency2;
                    channel->setFrequency(temp1);
                    break;
            }
            LOGD(" current effect %d", effect);
        }

        if (Common_BtnPress(BTN_ACTION4))//D
        {
            //播放模式切换
            playMode = !playMode;
            LOGD("playMode = %d", playMode);
        }

        result = system->update();
        ERRCHECK(result);

        {
            bool paused = 0;
            if (channel) {
                result = channel->getPaused(&paused);
                if (!paused) {
                    bool isPlaying;
                    channel->isPlaying(&isPlaying);
                    if (!isPlaying) {
                        if (!playMode) {
                            result = system->playSound(sound, 0, false, &channel);
                            ERRCHECK(result);
                            channel->getFrequency(&defaultfre);
                            JNIEnv *env = getJNIEnv();
                            jobject callBack = getCallBack();
                            jclass objcls = env->GetObjectClass(callBack);
                            jmethodID mid = env->GetMethodID(objcls, "onPlaystart", "(I)V");
                            env->CallVoidMethod(callBack, mid, getStringHash(musicPath));
                            if (effect == MODE_GAOGUAI1) {
                                float temp;
                                temp = defaultfre * frequency;
                                channel->setFrequency(temp);
                                LOGD("setFrequency d");
                            } else if (effect == MODE_GAOGUAI2) {
                                float temp;
                                temp = defaultfre * frequency2;
                                channel->setFrequency(temp);
                                LOGD("setFrequency c");
                            }
                        } else {
                            //播放完成
                            LOGE("playFinish");
                            if (sound) {
                                result = sound->release();
                                sound = 0;
                            }
                            ERRCHECK(result);
                            JNIEnv *env = getJNIEnv();
                            jobject callBack = getCallBack();
                            jclass objcls = env->GetObjectClass(callBack);
                            jmethodID mid = env->GetMethodID(objcls, "onPlayFinish", "(II)V");
                            env->CallVoidMethod(callBack, mid, getStringHash(musicPath),EVENT_FINISH);

                        }
                        if (effect == MODE_GAOGUAI1) {
                            float temp;
                            temp = defaultfre * frequency;
                            channel->setFrequency(temp);
                            LOGD("setFrequency b");
                        } else if (effect == MODE_GAOGUAI2) {
                            float temp;
                            temp = defaultfre * frequency2;
                            channel->setFrequency(temp);
                            LOGD("setFrequency a");
                        }
                    } else {
                        unsigned int ms = 0;
                        unsigned int lenms = 0;
                        result = channel->getPosition(&ms, FMOD_TIMEUNIT_MS);
                        if ((result != FMOD_OK) && (result != FMOD_ERR_INVALID_HANDLE) &&
                            (result != FMOD_ERR_CHANNEL_STOLEN)) {
                            ERRCHECK(result);
                        }
                        if (sound) {
                            result = sound->getLength(&lenms, FMOD_TIMEUNIT_MS);
                            if ((result != FMOD_OK) && (result != FMOD_ERR_INVALID_HANDLE) &&
                                (result != FMOD_ERR_CHANNEL_STOLEN)) {
                                ERRCHECK(result);
                            }
                        }
                        char data[50];
                        sprintf(data, "%d:%d:%d", getStringHash(musicPath), ms, lenms);
                        LOGD("%s", data)
                        jnitoJavaData(100, data);
                    }
                }
                if ((result != FMOD_OK) && (result != FMOD_ERR_INVALID_HANDLE) &&
                    (result != FMOD_ERR_CHANNEL_STOLEN)) {
                    ERRCHECK(result);
                }
            }

            Common_Draw("");
            Common_Draw("按 %s 添加歌曲(先选好歌)", "⊕");
            Common_Draw("按 %s 删除歌曲（先长按那首歌）", "Θ");
            Common_Draw("按 %s 查看/设置音效", "设置参数");
            Common_Draw("按 %s 退出", "back");
            Common_Draw("播放模式 %s", (playMode ? "列表循环" : "单曲循环"));
            Common_Draw("%s : 正常[%c] 搞怪快[%c] 萝莉[%c] 大叔[%c] 惊悚[%c] 空灵[%c] 搞怪慢[%c]",
                        paused ? "Paused " : "Playing",
                        effect == MODE_NORMAL ? 'x' : ' ',
                        effect == MODE_GAOGUAI1 ? 'x' : ' ',
                        effect == MODE_LUOLI ? 'x' : ' ',
                        effect == MODE_DASHU ? 'x' : ' ',
                        effect == MODE_JINGSONG ? 'x' : ' ',
                        effect == MODE_KONGLING ? 'x' : ' ',
                        effect == MODE_GAOGUAI2 ? 'x' : ' ');
        }

        Common_Sleep(50);
    } while (!Common_BtnPress(BTN_QUIT));

    /*
        Shut down
    */
    result = mastergroup->removeDSP(dspluoli);
    ERRCHECK(result);
    result = mastergroup->removeDSP(dspjingsong);
    ERRCHECK(result);
    result = mastergroup->removeDSP(dspdashu);
    ERRCHECK(result);
    result = mastergroup->removeDSP(dspkongling);
    ERRCHECK(result);

    result = dspluoli->release();
    ERRCHECK(result);
    result = dspjingsong->release();
    ERRCHECK(result);
    result = dspdashu->release();
    ERRCHECK(result);
    result = dspkongling->release();
    ERRCHECK(result);
    if (sound) {
        result = sound->release();
        sound = 0;
    }
    ERRCHECK(result);
    result = system->close();
    ERRCHECK(result);
    result = system->release();
    ERRCHECK(result);

    Common_Close();
    close();
    return 0;
}

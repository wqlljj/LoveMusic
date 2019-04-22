/*==============================================================================
Play Sound Example
Copyright (c), Firelight Technologies Pty, Ltd 2004-2019.

This example shows how to simply load and play multiple sounds, the simplest 
usage of FMOD. By default FMOD will decode the entire file into memory when it
loads. If the sounds are big and possibly take up a lot of RAM it would be
better to use the FMOD_CREATESTREAM flag, this will stream the file in realtime
as it plays.
==============================================================================*/
#include "inc/fmod.hpp"
#include "common.h"
#include <string>


int FMOD_Main_play_sound()
{
    FMOD::System     *system;
    FMOD::Sound      *sound1, *sound2, *sound3;
    FMOD::Channel    *channel = 0;
    FMOD_RESULT       result;
    unsigned int      version;
    void             *extradriverdata = 0;
    
    Common_Init(&extradriverdata);

    /*
        Create a System object and initialize
    */
    result = FMOD::System_Create(&system);
    LOGD("System_Create %d",result);
    ERRCHECK(result);

    result = system->getVersion(&version);
    LOGD("getVersion %d  Version %d",result,version);
    ERRCHECK(result);

    if (version < FMOD_VERSION)
    {
        Common_Draw("FMOD lib version %08x doesn't match header version %08x", version, FMOD_VERSION);
    }

    result = system->init(32, FMOD_INIT_NORMAL, extradriverdata);
    LOGD("init %d",result);
    ERRCHECK(result);

    result = system->createSound(Common_MediaPath("drumloop.wav"), FMOD_DEFAULT, 0, &sound1);
    LOGD("sound1 drumloop createSound %d",result);
    ERRCHECK(result);

    result = sound1->setMode(FMOD_LOOP_OFF);    /* drumloop.wav has embedded loop points which automatically makes looping turn on, */
    LOGD("setMode %d",result);
    ERRCHECK(result);                           /* so turn it off here.  We could have also just put FMOD_LOOP_OFF in the above CreateSound call. */

    result = system->createSound(Common_MediaPath("jaguar.wav"), FMOD_DEFAULT, 0, &sound2);
    LOGD("sound2 jaguar createSound %d",result);
    ERRCHECK(result);

    result = system->createSound(Common_MediaPath("swish.wav"), FMOD_DEFAULT, 0, &sound3);
    LOGD("sound3 swish createSound %d",result);
    ERRCHECK(result);

    /*
        Main loop
    */
    do
    {
        Common_Update();
//        result = system->playSound(sound1, 0, false, &channel);
//        LOGD("playSound sound1 %d",result);
        if (Common_BtnPress(BTN_ACTION1))
        {
            result = system->playSound(sound1, 0, false, &channel);
            LOGD("playSound sound1 %d",result);
            ERRCHECK(result);
        }

        if (Common_BtnPress(BTN_ACTION2))
        {
            result = system->playSound(sound2, 0, false, &channel);
            LOGD("playSound sound2 %d",result);
            ERRCHECK(result);
        }

        if (Common_BtnPress(BTN_ACTION3))
        {
            result = system->playSound(sound3, 0, false, &channel);
            LOGD("playSound sound3 %d",result);
            ERRCHECK(result);
        }

        result = system->update();
        LOGD("update %d",result);
        ERRCHECK(result);

        {
            unsigned int ms = 0;
            unsigned int lenms = 0;
            bool         playing = 0;
            bool         paused = 0;
            int          channelsplaying = 0;
            LOGD("channel %d,",channel);
            if (channel)
            {
                FMOD::Sound *currentsound = 0;

                result = channel->isPlaying(&playing);
                LOGD("isPlaying %d result %d",playing,result);
                if ((result != FMOD_OK) && (result != FMOD_ERR_INVALID_HANDLE) && (result != FMOD_ERR_CHANNEL_STOLEN))
                {
                    ERRCHECK(result);
                }

                result = channel->getPaused(&paused);
                LOGD("getPaused %d result %d",paused,result);
                if ((result != FMOD_OK) && (result != FMOD_ERR_INVALID_HANDLE) && (result != FMOD_ERR_CHANNEL_STOLEN))
                {
                    ERRCHECK(result);
                }

                result = channel->getPosition(&ms, FMOD_TIMEUNIT_MS);
                LOGD("getPosition %d result %d",ms,result);
                if ((result != FMOD_OK) && (result != FMOD_ERR_INVALID_HANDLE) && (result != FMOD_ERR_CHANNEL_STOLEN))
                {
                    ERRCHECK(result);
                }
               
                channel->getCurrentSound(&currentsound);
                LOGD("getCurrentSound %d result %d",currentsound,result);
                if (currentsound)
                {
                    result = currentsound->getLength(&lenms, FMOD_TIMEUNIT_MS);
                    LOGD("getLength %d result %d",lenms,result);
                    if ((result != FMOD_OK) && (result != FMOD_ERR_INVALID_HANDLE) && (result != FMOD_ERR_CHANNEL_STOLEN))
                    {
                        ERRCHECK(result);
                    }
                }
            }

            system->getChannelsPlaying(&channelsplaying, NULL);
            LOGD("getChannelsPlaying %d",channelsplaying);

            Common_Draw("Play Sound Example.");
            Common_Draw("Copyright (c) Firelight Technologies 2004-2019.");
            Common_Draw("==================================================");
            Common_Draw("");
            Common_Draw("Press %s to play a mono sound (drumloop)", Common_BtnStr(BTN_ACTION1));
            Common_Draw("Press %s to play a mono sound (jaguar)", Common_BtnStr(BTN_ACTION2));
            Common_Draw("Press %s to play a stereo sound (swish)", Common_BtnStr(BTN_ACTION3));
            Common_Draw("Press %s to quit", Common_BtnStr(BTN_QUIT));
            Common_Draw("");
            Common_Draw("Time %02d:%02d:%02d/%02d:%02d:%02d : %s", ms / 1000 / 60, ms / 1000 % 60, ms / 10 % 100, lenms / 1000 / 60, lenms / 1000 % 60, lenms / 10 % 100, paused ? "Paused " : playing ? "Playing" : "Stopped");
            Common_Draw("Channels Playing %d", channelsplaying);
        }

        Common_Sleep(50);
    } while (!Common_BtnPress(BTN_QUIT));

    /*
        Shut down
    */
    result = sound1->release();
    ERRCHECK(result);
    result = sound2->release();
    ERRCHECK(result);
    result = sound3->release();
    ERRCHECK(result);
    result = system->close();
    ERRCHECK(result);
    result = system->release();
    ERRCHECK(result);

    Common_Close();

    return 0;
}

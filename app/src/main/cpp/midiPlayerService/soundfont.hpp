#ifndef MIDILIB_SOUNDFONT_HPP
#define MIDILIB_SOUNDFONT_HPP
#define TSF_IMPLEMENTATION
#include "tsf.h"
#include <mutex>
#include <android/log.h>

namespace midiPlayerService{
    struct soundfont{
        tsf * soundfont;
        std::mutex locker;
    };
    template<typename T>
    void soundfont_init(T & self){
        self.soundfont = nullptr;
    }
    template<typename T> requires soundfont_init<T>
    void soundfont_load(T & self,const char * sf, int sampleRate, float gain){
        __android_log_print(ANDROID_LOG_INFO,"midiPlayerService","soundfont:%s",sf);
        soundfont_init(self);
        __android_log_print(ANDROID_LOG_INFO,"midiPlayerService","create player");
        self.soundfont = tsf_load_filename(sf);
        if(!self.soundfont){
            __android_log_print(ANDROID_LOG_FATAL,"midiPlayerService","fail to load sound font");
            return;
        }
        __android_log_print(ANDROID_LOG_INFO,"midiPlayerService","set output sampleRate=%d gain=%f",sampleRate,gain);
        tsf_set_output(self.soundfont, TSF_MONO, sampleRate, gain);
        tsf_channel_set_bank_preset(self.soundfont, 9, 128, 0);
        __android_log_print(ANDROID_LOG_INFO,"midiPlayerService","configure sound font finished");
    }
    template<typename T> requires soundfont_init<T>
    void soundfont_destroy(T & self){
        if(self.soundfont) {
            tsf_close(self.soundfont);
        }
    }
}

#endif //MIDILIB_SOUNDFONT_HPP

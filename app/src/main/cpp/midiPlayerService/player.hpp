#ifndef MIDILIB_PLAYER_HPP
#define MIDILIB_PLAYER_HPP
#include "soundfont.hpp"

namespace midiPlayerService{
    template<typename T> requires soundfont_init<T>
    void player_noteon(T & self,int channel,int key,int vel){
        self.locker.lock();
        if(self.soundfont) {
            tsf_channel_note_on(self.soundfont, channel, key, vel/128.0);
        }else{
            __android_log_print(ANDROID_LOG_FATAL,"midiPlayerService","soundfont=null");
        }
        self.locker.unlock();
    }
    template<typename T> requires soundfont_init<T>
    void player_noteoff(T & self,int channel,int key){
        self.locker.lock();
        if(self.soundfont) {
            tsf_channel_note_off(self.soundfont, channel, key);
        }else{
            __android_log_print(ANDROID_LOG_FATAL,"midiPlayerService","soundfont=null");
        }
        self.locker.unlock();
    }
    template<typename T> requires soundfont_init<T>
    void player_setProgram(T & self,int channel,int program){
        self.locker.lock();
        if(self.soundfont) {
            tsf_channel_set_presetnumber(self.soundfont, channel, program);
            //__android_log_print(ANDROID_LOG_DEBUG,"midiPlayerService","set preset channel=%d program=%d",channel,program);
        }else{
            __android_log_print(ANDROID_LOG_FATAL,"midiPlayerService","soundfont=null");
        }
        self.locker.unlock();
    }
    template<typename T> requires soundfont_init<T>
    void player_noteOffAll(T & self,int channel){
        self.locker.lock();
        if(self.soundfont) {
            tsf_channel_note_off_all(self.soundfont, channel);
        }else{
            __android_log_print(ANDROID_LOG_FATAL,"midiPlayerService","soundfont=null");
        }
        self.locker.unlock();
    }
    template<typename T> requires soundfont_init<T>
    void player_render(T & self,short* buffer, int samples, int flag_mixing=0){
        self.locker.lock();
        if(self.soundfont) {
            tsf_render_short(self.soundfont, buffer, samples, flag_mixing);
        }
        self.locker.unlock();
    }
    template<typename T> requires soundfont_init<T>
    void player_render(T & self,float* buffer, int samples, int flag_mixing=0){
        self.locker.lock();
        if(self.soundfont) {
            tsf_render_float(self.soundfont, buffer, samples, flag_mixing);
        }
        self.locker.unlock();
    }
}

#endif //MIDILIB_PLAYER_HPP

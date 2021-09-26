#include "player.hpp"
#include <jni.h>

extern "C"
JNIEXPORT jlong JNICALL
Java_com_sinriv_midilib_midiPlayerService_player_init_1native(JNIEnv *env, jobject thiz, jstring sf,
                                                              jint sample_rate, jfloat gain) {
    // implement init_native()
    const char *psf = env->GetStringUTFChars(sf, nullptr);
    if (!psf) return 0;
    auto ret = new midiPlayerService::soundfont;
    midiPlayerService::soundfont & self = *ret;
    midiPlayerService::soundfont_load(self , psf , sample_rate, gain);
    env->ReleaseStringUTFChars(sf, psf);

    return (jlong)ret;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_midiPlayerService_player_release_1native(JNIEnv *env, jobject thiz,
                                                                 jlong ptr) {
    // implement release_native()
    midiPlayerService::soundfont & self = *((midiPlayerService::soundfont*)ptr);
    midiPlayerService::soundfont_destroy(self);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_midiPlayerService_player_noteOn_1native(JNIEnv *env, jobject thiz,
                                                                jlong ptr, jint channel, jint key,
                                                                jint vel) {
    // implement noteOn_native()
    midiPlayerService::soundfont & self = *((midiPlayerService::soundfont*)ptr);
    midiPlayerService::player_noteon(self,channel,key,vel);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_midiPlayerService_player_noteOff_1native(JNIEnv *env, jobject thiz,
                                                                 jlong ptr, jint channel,
                                                                 jint key) {
    // implement noteOff_native()
    midiPlayerService::soundfont & self = *((midiPlayerService::soundfont*)ptr);
    midiPlayerService::player_noteoff(self,channel,key);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_midiPlayerService_player_setProgram_1native(JNIEnv *env, jobject thiz,
                                                                    jlong ptr, jint channel,
                                                                    jint program) {
    // implement setProgram_native()
    midiPlayerService::soundfont & self = *((midiPlayerService::soundfont*)ptr);
    midiPlayerService::player_setProgram(self,channel,program);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_midiPlayerService_player_noteOffAll_1native(JNIEnv * env, jobject thiz, jlong ptr , jint channel) {
    // implement noteOffAll_native()
    midiPlayerService::soundfont & self = *((midiPlayerService::soundfont*)ptr);
    midiPlayerService::player_noteOffAll(self,channel);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_midiPlayerService_player_render_1float_1native(JNIEnv *env, jobject thiz,
                                                                       jlong ptr, jfloatArray arr) {
    //  implement render_float_native()
    midiPlayerService::soundfont & self = *((midiPlayerService::soundfont*)ptr);
    auto len = env->GetArrayLength(arr);
    jfloat * floatArray = env->GetFloatArrayElements(arr, NULL);
    if (floatArray) {
        midiPlayerService::player_render(self,floatArray,len);
        env->SetFloatArrayRegion(arr,0,len,floatArray);
        env->ReleaseFloatArrayElements(arr,floatArray,0);
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_midiPlayerService_player_render_1short_1native(JNIEnv *env, jobject thiz,
                                                                       jlong ptr, jshortArray arr) {
    //  implement render_short_native()
    midiPlayerService::soundfont & self = *((midiPlayerService::soundfont*)ptr);
    auto len = env->GetArrayLength(arr);
    jshort * shortArray = env->GetShortArrayElements(arr, NULL);
    if (shortArray) {
        midiPlayerService::player_render(self,shortArray,len);
        env->SetShortArrayRegion(arr,0,len,shortArray);
        env->ReleaseShortArrayElements(arr,shortArray,0);
    }
}

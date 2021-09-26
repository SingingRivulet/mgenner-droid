#include <jni.h>
#include <android/log.h>
#include "synth.h"
class mgenner: public mgnr::synth{
    public:

        mgenner(){
            setSection(4);
        }

        JNIEnv * env = nullptr;
        jobject self = nullptr;
        std::string lastDefaultInfo;
        int lastSection = -1;

        jmethodID onSetDefaultInfo_func = nullptr;
        void onSetDefaultInfo(const std::string & info){
            auto str = env->NewStringUTF(info.c_str());
            env->CallVoidMethod(self,onSetDefaultInfo_func,str);
            env->DeleteLocalRef(str);
        }

        jmethodID onUseInfo_func = nullptr;
        void onUseInfo(const std::string & info)override{
            auto str = env->NewStringUTF(info.c_str());
            env->CallVoidMethod(self,onUseInfo_func,str);
            env->DeleteLocalRef(str);
        }

        jmethodID onSetSection_func = nullptr;
        void onSetSection(int sec){
            env->CallVoidMethod(self,onSetSection_func,sec);
        }

        double noteWidth = 1.0;
        void rebuildNoteLen()override{
            defaultDelay = noteWidth*TPQ;
            maticBlock   = noteWidth*TPQ;
            setSection();
        }

        jmethodID onLoadName_func = nullptr;
        void onLoadName(const std::string & name)override{
            auto str = env->NewStringUTF(name.c_str());
            env->CallVoidMethod(self,onLoadName_func,str);
            env->DeleteLocalRef(str);
        }

        jmethodID drawNote_begin_func = nullptr;
        void drawNote_begin()override{
            env->CallVoidMethod(self,drawNote_begin_func);
        }

        jmethodID drawNote_func = nullptr;
        void drawNote(int fx,int fy,int tx,int ty, int volume,const std::string & info,bool selected,bool onlydisplay) override{
            jstring str = env->NewStringUTF(info.c_str());
            env->CallVoidMethod(self,drawNote_func,fx,fy,tx,ty,volume,str,selected,onlydisplay);
            env->DeleteLocalRef(str);
        }

        jmethodID drawNote_end_func = nullptr;
        void drawNote_end()override{
            env->CallVoidMethod(self,drawNote_end_func);
        }

        jmethodID drawTableRaw_func = nullptr;
        void drawTableRaw(int from,int to,int t)override{
            env->CallVoidMethod(self,drawTableRaw_func,from,to,t);
        }

        jmethodID drawTimeCol_func = nullptr;
        void drawTimeCol(float p)override{
            env->CallVoidMethod(self,drawTimeCol_func,p);
        }

        jmethodID drawSectionCol_func = nullptr;
        void drawSectionCol(float p,int n)override{
            env->CallVoidMethod(self,drawSectionCol_func,p,n);
        }

        jmethodID drawTempo_func = nullptr;
        void drawTempo(float p,double t)override{
            env->CallVoidMethod(self,drawTempo_func,p,t);
        }

        jmethodID drawTempoPadd_func = nullptr;
        void drawTempoPadd()override{
            env->CallVoidMethod(self,drawTempoPadd_func);
        }

        jmethodID drawScroll_func = nullptr;
        void drawScroll()override{
            env->CallVoidMethod(self,drawScroll_func);
        }

        jmethodID onSetChannelIns_func = nullptr;
        void onSetChannelIns(int c,int ins)override{
            env->CallVoidMethod(self,onSetChannelIns_func,c,ins);
        }
        jmethodID onNoteOn_func = nullptr;
        void callJavaNoteOn(const char * info,int channel,int tone,int vol)override{
            env->CallVoidMethod(self,onNoteOn_func,channel,tone,vol);
        }
        jmethodID onNoteOff_func = nullptr;
        void callJavaNoteOff(const char * info,int channel,int tone)override{
            env->CallVoidMethod(self,onNoteOff_func,channel,tone);
        }

        void envInit(JNIEnv *ienv , jobject iself){
            this->env  = ienv;
            this->self = iself;
            jclass cla = env->GetObjectClass(self);
            onSetSection_func = env->GetMethodID(cla,"onSetSection","(I)V");
            onSetDefaultInfo_func = env->GetMethodID(cla,"onSetDefaultInfo","(Ljava/lang/String;)V");
            onUseInfo_func = env->GetMethodID(cla,"onUseInfo","(Ljava/lang/String;)V");
            onLoadName_func = env->GetMethodID(cla, "onLoadName", "(Ljava/lang/String;)V");
            drawNote_begin_func = env->GetMethodID(cla,"drawNote_begin","()V");
            drawNote_func = env->GetMethodID(cla,"drawNote","(IIIIILjava/lang/String;ZZ)V");
            drawNote_end_func = env->GetMethodID(cla,"drawNote_end","()V");
            drawTableRaw_func = env->GetMethodID(cla,"drawTableRaw","(III)V");
            drawTimeCol_func = env->GetMethodID(cla,"drawTimeCol","(F)V");
            drawSectionCol_func = env->GetMethodID(cla,"drawSectionCol","(FI)V");
            drawTempo_func = env->GetMethodID(cla,"drawTempo","(FD)V");
            drawTempoPadd_func = env->GetMethodID(cla,"drawTempoPadd","()V");
            drawScroll_func = env->GetMethodID(cla,"drawScroll","()V");
            onSetChannelIns_func = env->GetMethodID(cla,"onSetChannelIns","(II)V");
            onNoteOn_func = env->GetMethodID(cla,"onNoteOn","(III)V");
            onNoteOff_func = env->GetMethodID(cla,"onNoteOff","(II)V");
        }

        void draw(JNIEnv *ienv , jobject iself){
            envInit(ienv,iself);
            this->render();
            if(lastDefaultInfo!=defaultInfo){
                lastDefaultInfo = defaultInfo;
                onSetDefaultInfo(defaultInfo);
            }
            if(section!=lastSection){
                lastSection = section;
                onSetSection(section);
            }
        }
        void playStep(JNIEnv *ienv , jobject iself){
            this->envInit(ienv,iself);
            this->mgnr::player::playStep();
        }
        jmethodID scrollBuilder_onGetNoteArea_func = nullptr;
        void scrollBuilder_onGetNoteArea()override{
            env->CallVoidMethod(self,scrollBuilder_onGetNoteArea_func,noteTimeMax,noteToneMin,noteToneMax);
        }
        jmethodID scrollBuilder_onGetAllNotePos_func = nullptr;
        void scrollBuilder_onGetAllNotePos(mgnr::note * n)override{
            env->CallVoidMethod(self,scrollBuilder_onGetAllNotePos_func,n->begin,n->delay,n->tone);
        }
        jmethodID scrollBuilder_onSwap_func = nullptr;
        void scrollBuilder_onSwap()override{
            env->CallVoidMethod(self,scrollBuilder_onSwap_func);
        }
        void buildScroll(JNIEnv *ienv , jobject iself){
            this->env  = ienv;
            this->self = iself;
            jclass cla = env->GetObjectClass(self);
            scrollBuilder_onGetNoteArea_func = env->GetMethodID(cla,"scrollBuilder_onGetNoteArea","(FFF)V");
            scrollBuilder_onGetAllNotePos_func = env->GetMethodID(cla,"scrollBuilder_onGetAllNotePos","(FFF)V");
            scrollBuilder_onSwap_func = env->GetMethodID(cla,"scrollBuilder_onSwap","()V");
            scrollBuilder_process();
        }
        void hideMode(){
            if(infoFilter.empty()){
                infoFilter=defaultInfo;
            }else{
                infoFilter.clear();
            }
        }
};
extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_setScreen_1native(JNIEnv *env, jobject thiz,
                                                                  jlong ptr, jint w, jint h) {
    // implement setScreen_native()
    auto self = (mgenner*)ptr;
    self->windowWidth  = w;
    self->windowHeight = h;
}
extern "C"
JNIEXPORT jfloat JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_getLookAtX_1native(JNIEnv *env, jobject thiz,
                                                                   jlong ptr) {
    // implement getLookAtX_native()
    auto self = (mgenner*)ptr;
    return self->lookAtX;
}
extern "C"
JNIEXPORT jfloat JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_getLookAtY_1native(JNIEnv *env, jobject thiz,
                                                                   jlong ptr) {
    // implement getLookAtY_native()
    auto self = (mgenner*)ptr;
    return self->lookAtY;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_setLookAtX_1native(JNIEnv *env, jobject thiz,
                                                                   jlong ptr,jfloat x) {
    // implement setLookAtX_native()
    auto self = (mgenner*)ptr;
    self->lookAtX = x;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_setLookAtY_1native(JNIEnv *env, jobject thiz,
                                                                   jlong ptr,jfloat y) {
    // implement setLookAtY_native()
    auto self = (mgenner*)ptr;
    self->lookAtY = y;
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_updateTimeMax_1native(JNIEnv *env, jobject thiz,
                                                                      jlong ptr) {
    // implement updateTimeMax_native()
    auto self = (mgenner*)ptr;
    return self->updateTimeMax();
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_init_1native(JNIEnv *env, jobject thiz) {
    // implement init_native()
    auto res = new mgenner();
    return (long)res;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_destroy_1native(JNIEnv *env, jobject thiz,
                                                                jlong ptr) {
    // implement destroy_native()
    auto self = (mgenner*)ptr;
    delete self;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_render_1native(JNIEnv *env, jobject thiz,
                                                               jlong ptr) {
    // implement render_native()
    auto self = (mgenner*)ptr;
    self->draw(env,thiz);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_playStep_1native(JNIEnv *env, jobject thiz,
                                                                 jlong ptr) {
    // implement playStep_native()
    auto self = (mgenner*)ptr;
    self->playStep(env,thiz);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_loadMidi_1native(JNIEnv *env, jobject thiz,
                                                                 jlong ptr, jstring path) {
    // implement loadMidi_native()
    auto self = (mgenner*)ptr;
    const char * str = env->GetStringUTFChars(path, NULL);
    if(str != NULL) {
        self->envInit(env,thiz);
        self->loadMidi(str);
        env->ReleaseStringUTFChars(path, str);
    }
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_getInfoFilter_1native(JNIEnv *env, jobject thiz,
                                                                      jlong ptr) {
    // implement getInfoFilter_native()
    auto self = (mgenner*)ptr;
    return env->NewStringUTF(self->infoFilter.c_str());
}
extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_playStart_1native(JNIEnv *env, jobject thiz,
                                                                  jlong ptr) {
    // implement playStart_native()
    auto self = (mgenner*)ptr;
    self->envInit(env,thiz);
    self->playStart();
}
extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_playStop_1native(JNIEnv *env, jobject thiz,
                                                                 jlong ptr) {
    // implement playStop_native()
    auto self = (mgenner*)ptr;
    self->envInit(env,thiz);
    self->playStop();
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_getPlayingStatus_1native(JNIEnv *env, jobject thiz,
                                                                         jlong ptr) {
    // implement getPlayingStatus_native()
    auto self = (mgenner*)ptr;
    return self->playingStatus;
}
extern "C"
JNIEXPORT jfloat JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_getNoteHeight_1native(JNIEnv *env, jobject thiz,
                                                                      jlong ptr) {
    // implement getNoteHeight_native()
    auto self = (mgenner*)ptr;
    return self->noteHeight;
}
extern "C"
JNIEXPORT jfloat JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_getNoteLength_1native(JNIEnv *env, jobject thiz,
                                                                      jlong ptr) {
    // implement getNoteLength_native()
    auto self = (mgenner*)ptr;
    return self->noteLength;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_clickToDisplay_1native(JNIEnv *env, jobject thiz,
                                                                       jlong ptr, jint x, jint y) {
    // implement clickToDisplay_native()
    auto self = (mgenner*)ptr;
    self->clickToDisplay(x,y);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_clickToDisplay_1close_1native(JNIEnv *env,
                                                                              jobject thiz,
                                                                              jlong ptr) {
    // implement clickToDisplay_close_native()
    auto self = (mgenner*)ptr;
    self->clickToDisplay_close();
}
extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_addDisplaied_1native(JNIEnv *env, jobject thiz,
                                                                     jlong ptr) {
    // implement addDisplaied_native()
    auto self = (mgenner*)ptr;
    self->envInit(env,thiz);
    self->addDisplaied();
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_clickToSelect_1native(JNIEnv *env, jobject thiz,
                                                                      jlong ptr, jint x, jint y) {
    // implement clickToSelect_native()
    auto self = (mgenner*)ptr;
    return self->clickToSelect(x,y);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_undo_1native(JNIEnv *env, jobject thiz, jlong ptr) {
    // implement undo_native()
    auto self = (mgenner*)ptr;
    self->envInit(env,thiz);
    self->undo();
}
extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_removeSelected_1native(JNIEnv *env, jobject thiz,
                                                                       jlong ptr) {
    // implement removeSelected_native()
    auto self = (mgenner*)ptr;
    self->envInit(env,thiz);
    self->removeSelected();
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_hideMode_1native(JNIEnv *env, jobject thiz,
                                                                 jlong ptr) {
    // implement hideMode_native()
    auto self = (mgenner*)ptr;
    self->hideMode();
    return !self->infoFilter.empty();
}
extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_clearSelected_1native(JNIEnv *env, jobject thiz,
                                                                      jlong ptr) {
    // implement clearSelected_native()
    auto self = (mgenner*)ptr;
    self->clearSelected();
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_selectByArea_1native(JNIEnv *env, jobject thiz,
                                                                     jlong ptr, jint select_box_x,
                                                                     jint select_box_xend,
                                                                     jint select_box_y,
                                                                     jint select_box_yend) {
    // implement selectByArea_native()
    auto self = (mgenner*)ptr;
    return self->selectByArea(select_box_x,select_box_xend,select_box_y,select_box_yend);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_setTableWidth_1native(JNIEnv *env, jobject thiz,
                                                                      jlong ptr, jfloat w) {
    // implement setTableWidth_native()
    auto self = (mgenner*)ptr;
    self->noteWidth = w;
    self->defaultDelay = w*((float)self->TPQ);
    self->maticBlock   = w*((float)self->TPQ);
    self->setSection();
}
extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_clickToLookAt_1native(JNIEnv *env, jobject thiz,
                                                                      jlong ptr, jint x, jint y) {
    // implement clickToLookAt_native()
    auto self = (mgenner*)ptr;
    self->clickToLookAt(x,y);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_clickToRemoveTempo_1native(JNIEnv *env,
                                                                           jobject thiz, jlong ptr,
                                                                           jint x, jint y) {
    // implement clickToRemoveTempo_native()
    auto self = (mgenner*)ptr;
    self->clickToRemoveTempo(x,y);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_clickToSetTempo_1native(JNIEnv *env, jobject thiz,
                                                                        jlong ptr, jint x, jint y,
                                                                        jdouble tp) {
    // implement clickToSetTempo_native()
    auto self = (mgenner*)ptr;
    self->clickToSetTempo(x,y,tp);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_setSection_1native(JNIEnv *env, jobject thiz,
                                                                   jlong ptr, jint s) {
    // implement setSection_native()
    auto self = (mgenner*)ptr;
    self->setSection(s);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_setInfo_1native(JNIEnv *env, jobject thiz,
                                                                jlong ptr, jstring f) {
    // implement setInfo_native()
    auto self = (mgenner*)ptr;
    const char * str = env->GetStringUTFChars(f, NULL);
    if(str != NULL) {
        self->defaultInfo = str;
        env->ReleaseStringUTFChars(f, str);
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_exportMidi_1native(JNIEnv *env, jobject thiz,
                                                                   jlong ptr, jstring path) {
    // implement exportMidi_native()
    auto self = (mgenner*)ptr;
    const char * str = env->GetStringUTFChars(path, NULL);
    if(str != NULL) {
        self->envInit(env,thiz);
        self->exportMidi(str);
        env->ReleaseStringUTFChars(path, str);
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_setNoteLength_1native(JNIEnv *env, jobject thiz,
                                                                      jlong ptr, jfloat len) {
    // implement setNoteLength_native()
    auto self = (mgenner*)ptr;
    self->noteLength = len;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_setNoteHeight_1native(JNIEnv *env, jobject thiz,
                                                                      jlong ptr, jfloat hei) {
    // implement setNoteHeight_native()
    auto self = (mgenner*)ptr;
    self->noteHeight = hei;
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_getTPQ_1native(JNIEnv *env, jobject thiz,
                                                               jlong ptr) {
    // implement getTPQ_native()
    auto self = (mgenner*)ptr;
    return self->TPQ;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_setTPQ_1native(JNIEnv *env, jobject thiz, jlong ptr,
                                                               jint tpq) {
    // implement setTPQ_native()
    auto self = (mgenner*)ptr;
    self->TPQ = tpq;
    self->rebuildNoteLen();
}
extern "C"
JNIEXPORT void JNICALL
Java_com_sinriv_midilib_mgenner_mgenner_1native_buildScroll_1native(JNIEnv *env, jobject thiz,
                                                                    jlong ptr) {
    // implement buildScroll_native()
    auto self = (mgenner*)ptr;
    self->buildScroll(env,thiz);
}
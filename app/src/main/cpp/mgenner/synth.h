#ifndef MGNR_SYNTH
#define MGNR_SYNTH
#include "player.h"
#include "lcs.h"
#include <set>
#include <list>
#include <vector>
#include <sstream>
namespace mgnr{
    struct voiceWord{
        int length;//时长
        int tone;//音高（-1为休止符）
        int volume;
        std::string word;//词汇
        voiceWord(int l,int t,int v,const std::string & w){
            length  = l;
            tone    = t;
            word    = w;
            volume  = v;
        }
        voiceWord(const voiceWord & i){
            length  = i.length;
            tone    = i.tone;
            word    = i.word;
            volume  = i.volume;
        }
    };
    struct voiceTrack{
        std::list<voiceWord> words;
        int length;
        void pushPause(int len);
        void pushNote(int len,const std::string & info,int tone,int v);
        voiceTrack(){
            length=0;
            words.clear();
        }
    };
    class synth:public player{
        public:
            void onNoteOn(note * n,int c);
            void onNoteOff(note * n,int c);
            void onUseInfo(const std::string & info);
            
            void synthOutput();//链接外部引擎合成
            
            void toHashSeries(std::vector<std::pair<int,int> > & out);
            void diff(const std::string & out,std::function<void (int)> const & callback_d,std::function<void (int,int,int,int)> const & callback_s);
            void diff(const std::vector<std::pair<int,int> > & out,std::function<void (int)> const & callback_d,std::function<void (int,int,int,int)> const & callback_s);
            void toHashSeries(std::string & out);
            void toThemesTrain(std::string & out,int delta);//生成主旋律预测训练数据（供outTunner使用）
            void toThemesPredict(std::string & out,int delta);//生成主旋律预测数据（供outTunner使用）
            
            std::vector<voiceTrack*> tracks;
            virtual void callJavaNoteOn(const char * info,int channel,int tone,int vol)=0;
            virtual void callJavaNoteOff(const char * info,int channel,int tone)=0;
            virtual void onLoadName(const std::string & name)=0;

            ~synth();
            
        private:
            
            void splitTracks();//将midimap分解成音轨
            void clearTracks();
            
            void synthOutput_init(int);
            void synthOutput_addWord(int channel,const std::string & word,int tone,int volume,float sec);
            void synthOutput_addPause(int channel,float sec);
            void synthOutput_addIntoChannel(int channel,const voiceTrack & );
            void synthOutput_start();
    };
}
#endif

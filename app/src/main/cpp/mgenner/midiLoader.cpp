#include "editTable.h"
#include "MidiFile.h"
#include "Options.h"
#include <iostream>
#include <map>
#include <string>
namespace mgnr{

using namespace std;
using namespace smf;

static const char * instrumentName[] = {
    "Piano",
    "BrightPiano",
    "ElectricPiano",
    "HonkyTonkPiano",
    "RhodesPiano",
    "ChorusedPiano",
    "Harpsichord",
    "Clavinet",
    "Celesta",
    "Glockenspiel",
    "MusicBoX",
    "Vibraphone",
    "Marimba",
    "Xylophone",
    "TubularBells",
    "Dulcimer",
    "HammondOrgan",
    "PercussiveOrgan",
    "RockOrgan",
    "ChurchOrgan",
    "ReedOrgan",
    "Accordion",
    "Harmonica",
    "TangoAccordian",
    "Guitar-nylon",
    "Guitar-steel",
    "Guitar-jazz",
    "Guitar-clean",
    "Guitar-muted",
    "OverdrivenGuitar",
    "DistortionGuitar",
    "GuitarHarmonics",
    "AcousticBass",
    "ElectricBass-finger",
    "ElectricBass-pick",
    "FretlessBass",
    "SlapBass1",
    "SlapBass2",
    "SynthBass1",
    "SynthBass2",
    "Violin",
    "Viola",
    "Cello",
    "Contrabass",
    "TremoloStrings",
    "PizzicatoStrings",
    "OrchestralHarp",
    "Timpani",
    "StringEnsemble1",
    "StringEnsemble2",
    "SynthStrings1",
    "SynthStrings2",
    "ChoirAahs",
    "VoiceOohs",
    "SynthVoice",
    "OrchestraHit",
    "Trumpet",
    "Trombone",
    "Tuba",
    "MutedTrumpet",
    "FrenchHorn",
    "BrassSection",
    "SynthBrass1",
    "SynthBrass2",
    "SopranoSaX",
    "AltoSaX",
    "TenorSaX",
    "BaritoneSaX",
    "Oboe",
    "EnglishHorn",
    "Bassoon",
    "Clarinet",
    "Piccolo",
    "Flute",
    "Record",
    "PanFlute",
    "BottleBlow",
    "Skakuhachi",
    "Whistle",
    "Ocarina",
    "Lead1-square",
    "Lead2-sawtooth",
    "Lead3-calliope",
    "Lead4-chiff",
    "Lead5-charang",
    "Lead6-voice",
    "Lead7-fifths",
    "Lead8-bass",
    "Pad1-newage",
    "Pad2-warm",
    "Pad3-polysynth",
    "Pad4-choir",
    "Pad5-bowed",
    "Pad6-metallic",
    "Pad7-halo",
    "Pad8-sweep",
    "FX1-rain",
    "FX2-soundtrack",
    "FX3-crystal",
    "FX4-atmosphere",
    "FX5-brightness",
    "FX6-goblins",
    "FX7-echoes",
    "FX8-sci-fi",
    "Sitar",
    "Banjo",
    "Shamisen",
    "Koto",
    "Kalimba",
    "Bagpipe",
    "Fiddle",
    "Shanai",
    "Tinkle Bell",
    "Agogo",
    "SteelDrums",
    "Woodblock",
    "TaikoDrum",
    "MelodicTom",
    "SynthDrum",
    "ReverseCymbal",
    "GuitarFretNoise",
    "BreathNoise",
    "Seashore",
    "BirdTweet",
    "TelephoneRing",
    "Helicopter",
    "Applause",
    "Gunshot"
};

void editTable::instrument2Id_init(){
    for(int i=0;i<128;++i){
        instrument2Id[instrumentName[i]] = i;
    }
}
void editTable::loadInstrument(int id){
    if(id<0||id>=128)
        return;
    if(instrumentLoaded[id])
        return;
    instrumentLoaded[id] = true;

    ::__android_log_print(ANDROID_LOG_INFO, "mgenner","require instrument:%s",instrumentName[id]);
}
int editTable::getInstrumentId(const std::string & name){
    char n[128];
    snprintf(n,128,"%s",name.c_str());
    for(int i=0;i<128;++i){
        if(n[i]=='\0'){
            break;
        }else if(n[i]=='.'){
            n[i] = '\0';
            break;
        }
    }
    auto it = instrument2Id.find(n);
    if(it==instrument2Id.end()){
        return 0;
    }else{
        return it->second;
    }
}
void editTable::loadMidi(const std::string & str){
    MidiFile midifile;
    midifile.read(str);
    midifile.doTimeAnalysis();
    midifile.linkNotePairs();

    TPQ=midifile.getTicksPerQuarterNote();
    rebuildNoteLen();
    ::__android_log_print(ANDROID_LOG_INFO, "mgenner","TPQ:%d",TPQ);
    int tracks = midifile.getTrackCount();

    if (tracks > 1){
        ::__android_log_print(ANDROID_LOG_INFO, "mgenner","TRACKS:%d",tracks);
    }

    std::set<int> iset;

    for (int track=0; track<tracks; track++) {

        char infoBuf[128];

        int instrumentId = 0;

        for (int event=0; event<midifile[track].size(); event++) {

            if (midifile[track][event].isNoteOn() && midifile[track][event].size()>=3){
                int position = midifile[track][event].tick;
                int delay = midifile[track][event].getTickDuration();
                int delayS = midifile[track][event].getDurationInSeconds();
                int tone = (int)midifile[track][event][1];
                int v = (int)midifile[track][event][2];
                snprintf(infoBuf,sizeof(infoBuf),"%s.%d",instrumentName[instrumentId],track);
                addNote(position, tone, delay, v,infoBuf);
                iset.insert(instrumentId);
            }else if(midifile[track][event].isTimbre()){
                instrumentId = midifile[track][event].getP1();
                if(instrumentId<0)
                    instrumentId = 0;
                else if(instrumentId>128)
                    instrumentId = 128;
            }
        }
    }

    auto numTracks = midifile.getNumTracks();
    ::__android_log_print(ANDROID_LOG_INFO, "mgenner","load controls numTracks:%d",numTracks);
    for(int trackIndex=0;trackIndex<numTracks;++trackIndex) {
        for (int i = 0; i < midifile.getNumEvents(trackIndex); i++) {
            if (midifile.getEvent(trackIndex, i).isTempo()) {//???????????????
                double tp = midifile.getEvent(trackIndex, i).getTempoBPM();
                addTempo(midifile.getEvent(trackIndex, i).tick, tp);
            }
        }
    }

    ::__android_log_print(ANDROID_LOG_INFO, "mgenner","load instruments");
    for (auto it : iset){
        loadInstrument(it);
    }

    ::__android_log_print(ANDROID_LOG_INFO, "mgenner","load midi success");
}

void editTable::exportMidi(const std::string & filename){
    map<string,int> tracks;
    int trackNum=1;//0???????????????info?????????
    int track;
    MidiFile midifile;

    midifile.setTPQ(TPQ);//0??????
    midifile.addTrack();//0??????

    struct noteMap_t{
        int tone,volume,time;
        bool isNoteOn;
    };

    std::map<int,std::pair<int,std::vector<noteMap_t*> > > noteMap;

    for(auto it:notes){
        if(it->info.empty()){
            track=0;

            //midifile.addNoteOn(track, it->begin , 0, it->tone , it->volume > 100 ? 100 : it->volume);
            //midifile.addNoteOff(track, it->begin + it->delay , 0, it->tone);

        }else{
            if(it->info.at(0)!='@'){//???@???????????????
                auto tit=tracks.find(it->info);

                if(tit==tracks.end()){//????????????
                    midifile.addTrack();
                    tracks[it->info]=trackNum;
                    track=trackNum;
                    ++trackNum;

                    auto p1 = new noteMap_t;
                    p1->tone   = it->tone;
                    p1->volume = it->volume > 100 ? 100 : it->volume;
                    p1->time   = it->begin;
                    p1->isNoteOn = true;

                    auto p2 = new noteMap_t;
                    p2->tone   = it->tone;
                    p2->volume = 0;
                    p2->time   = it->begin + it->delay;
                    p2->isNoteOn = false;

                    auto & lst = noteMap[track];
                    lst.first  = getInstrumentId(it->info);
                    lst.second.push_back(p1);
                    lst.second.push_back(p2);

                    //midifile.addNoteOn(track, it->begin , 0, it->tone , it->volume > 100 ? 100 : it->volume);
                    //midifile.addNoteOff(track, it->begin + it->delay , 0, it->tone);

                }else{
                    track=tit->second;


                    auto p1 = new noteMap_t;
                    p1->tone   = it->tone;
                    p1->volume = it->volume > 100 ? 100 : it->volume;
                    p1->time   = it->begin;
                    p1->isNoteOn = true;

                    auto p2 = new noteMap_t;
                    p2->tone   = it->tone;
                    p2->volume = 0;
                    p2->time   = it->begin + it->delay;
                    p2->isNoteOn = false;

                    auto & lst = noteMap[track];
                    lst.second.push_back(p1);
                    lst.second.push_back(p2);

                    //midifile.addNoteOn(track, it->begin , 0, it->tone , it->volume > 100 ? 100 : it->volume);
                    //midifile.addNoteOff(track, it->begin + it->delay , 0, it->tone);

                }

            }
        }
    }
    for(auto it:timeMap){//??????time map
        midifile.addTempo(0,it.first,it.second);
    }
    for(auto itlst:noteMap){
        int tk = itlst.first;
        int ch = tk;
        if(ch>15)
            ch = 15;

        sort(itlst.second.second.begin(),itlst.second.second.end(),[](noteMap_t * a,noteMap_t * b){
            return a->time < b->time;
        });

        midifile.addTimbre(tk,0,ch,itlst.second.first);

        for(auto it:itlst.second.second){//????????????
            if(it->isNoteOn){
                midifile.addNoteOn(tk, it->time , ch, it->tone , it->volume);
            }else{
                midifile.addNoteOff(tk, it->time , ch, it->tone);
            }
            delete it;
        }
    }
    midifile.write(filename);
}

}

// midiPlayerAIDL.aidl
package com.sinriv.midilib;

interface midiPlayerAIDL {
    void noteOn(int channel,int key,int vel);
    void noteOff(int channel,int key);
    void noteOffAll(int channel);
    void setProgram(int channel,int program);
    void start();
    void stop();
}
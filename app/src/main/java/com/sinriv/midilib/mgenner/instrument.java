package com.sinriv.midilib.mgenner;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import com.sinriv.mgenner.R;

public class instrument {
    static String [] instrumentName = {
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
    static public void show(Context context , mgenner_native m, String name){
        View v = View.inflate(context, R.layout.midieditor_instruments, null);
        final TextView select_instrument_text = v.findViewById(R.id.select_instrument_text);
        select_instrument_text.setText(name);
        v.findViewById(R.id.select_instrument).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.select_instrument);
                builder.setItems(instrumentName, (dialog, which) -> {
                    select_instrument_text.setText(instrumentName[which]);
                });
                builder.show();
            }
        });
        android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(context);
        b.setTitle("设置乐器");
        b.setView(v);
        b.setPositiveButton("确认", (dialog, which) -> m.setInfo(select_instrument_text.getText().toString()));
        b.create().show();
    }
}

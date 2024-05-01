package fr.radi3nt.openal.engine.source.sources.unit;

import fr.radi3nt.openal.al.AlSoundSource;
import fr.radi3nt.openal.engine.clip.SoundClip;
import fr.radi3nt.openal.engine.source.attenuation.AudioAttenuation;
import fr.radi3nt.openal.engine.source.handle.SoundHandle;
import fr.radi3nt.openal.engine.source.playback.AudioPlayback;
import fr.radi3nt.openal.engine.source.sources.AlSoundSourceHolder;
import fr.radi3nt.openal.high.gain.ParentPercentModifier;
import fr.radi3nt.openal.high.gain.PercentModifier;

import java.util.ArrayList;

public class UnitSoundSource implements AlSoundSourceHolder {

    private final AudioPlayback playbackModule;

    private final ParentPercentModifier globalSourceGain = new ParentPercentModifier(new ArrayList<>());
    private final ParentPercentModifier globalSourcePitch = new ParentPercentModifier(new ArrayList<>());

    private final ParentPercentModifier currentSoundGain = new ParentPercentModifier(new ArrayList<>());
    private final ParentPercentModifier currentSoundPitch = new ParentPercentModifier(new ArrayList<>());

    private final HandledSoundSource handledSource;

    public UnitSoundSource(AudioAttenuation audioAttenuation, AudioPlayback playbackModule) {
        this(audioAttenuation, playbackModule, new HandledSoundSource());
    }

    public UnitSoundSource(AudioAttenuation audioAttenuation, AudioPlayback playbackModule, HandledSoundSource source) {
        this.playbackModule = playbackModule;
        this.handledSource = source;

        globalSourceGain.getModifiers().add(audioAttenuation.getAttenuationModifier());
        globalSourceGain.getModifiers().add(playbackModule.getGainMultiplier());

        globalSourcePitch.getModifiers().add(audioAttenuation.getDopplerModifier());
        globalSourcePitch.getModifiers().add(playbackModule.getPitchMultiplier());

        globalSourceGain.getModifiers().add(currentSoundGain);
        globalSourcePitch.getModifiers().add(currentSoundPitch);
    }

    public SoundHandle play(SoundClip clip, PercentModifier gain, PercentModifier pitch) {
        playbackModule.set(clip, handledSource.getSource());

        currentSoundGain.getModifiers().clear();
        currentSoundGain.getModifiers().add(gain);

        currentSoundPitch.getModifiers().clear();
        currentSoundPitch.getModifiers().add(pitch);

        playbackModule.play();

        return handledSource.setHandle(playbackModule, gain, pitch);
    }

    public void update() {
        playbackModule.update();
        handledSource.update(globalSourceGain.percentModifier(), globalSourcePitch.percentModifier(), !playbackModule.isPlaying());
    }

    @Override
    public AlSoundSource getSource() {
        return handledSource.getSource();
    }

    public HandledSoundSource getHandledSource() {
        return handledSource;
    }

    public boolean isCompleted() {
        return handledSource.isDone();
    }
}
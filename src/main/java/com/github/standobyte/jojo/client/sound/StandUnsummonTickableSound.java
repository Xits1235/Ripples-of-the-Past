package com.github.standobyte.jojo.client.sound;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.network.packets.fromserver.TrStandSoundPacket.StandSoundType;

import net.minecraft.client.audio.EntityTickableSound;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

public class StandUnsummonTickableSound extends EntityTickableSound {
    private StandEntity stand;
    
    public StandUnsummonTickableSound(SoundEvent sound, SoundCategory category, 
            float volume, float pitch, LivingEntity standUser, StandEntity stand) {
        super(sound, category, volume, pitch, standUser);
        this.stand = stand;
    }

    @Override
    public void tick() {
        if (stand != null && !stand.isAlive()) {
            stand = null;
        }
        if (stand != null && stand.checkSoundStop(StandSoundType.UNSUMMON)) {
            stop();
        }
        else {
            super.tick();
        }
    }
}

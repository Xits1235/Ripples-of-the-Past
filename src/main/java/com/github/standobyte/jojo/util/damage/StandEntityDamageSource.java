package com.github.standobyte.jojo.util.damage;

import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class StandEntityDamageSource extends EntityDamageSource implements IStandDamageSource {
    protected final IStandPower stand;
//    @Nullable
//    protected final Entity standUser;
    private float knockbackFactor = 1;
    protected boolean showStandUserName;

    public StandEntityDamageSource(String msgId, Entity damagingEntity, IStandPower stand) {
        super(msgId, damagingEntity);
        this.stand = stand;
    }
    
    StandEntityDamageSource(DamageSource damageSource, IStandPower stand) {
        super(damageSource.getMsgId(), damageSource.getDirectEntity());
        this.stand = stand;
    }
    
    public IStandPower getStandPower() {
        return stand;
    }

    @Override
    public boolean scalesWithDifficulty() {
        if (super.scalesWithDifficulty()) {
            if (stand == null) {
               return true;
            } else {
               LivingEntity standUser = stand.getUser();
               return standUser != null && standUser instanceof LivingEntity 
                       && !(standUser instanceof PlayerEntity);
            }
        }
        return false;
    }

    public StandEntityDamageSource setKnockbackReduction(float factor) {
        this.knockbackFactor = MathHelper.clamp(factor, 0, 1);
        return this;
    }

    public float getKnockbackFactor() {
        return knockbackFactor;
    }

    public StandEntityDamageSource setShowStandUserName() {
        this.showStandUserName = true;
        return this;
    }

    @Override
    public ITextComponent getLocalizedDeathMessage(LivingEntity dead) {
        if (showStandUserName && stand != null) {
            LivingEntity standUser = stand.getUser();
            if (standUser != null) {
                return new TranslationTextComponent("death.attack." + msgId + ".stand_user", dead.getDisplayName(), standUser.getDisplayName(), entity.getDisplayName());
            }
        }
        return new TranslationTextComponent("death.attack." + msgId, dead.getDisplayName(), entity.getDisplayName());
    }

    @Override
    public String toString() {
       return "StandEntityDamageSource (" + entity + ")";
    }
}

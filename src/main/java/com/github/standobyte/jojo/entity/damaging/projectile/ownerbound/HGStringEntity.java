package com.github.standobyte.jojo.entity.damaging.projectile.ownerbound;

import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.ModEntityTypes;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class HGStringEntity extends OwnerBoundProjectileEntity {
    private float yRotOffset;
    private float xRotOffset;
    private boolean isBinding;
    private boolean dealtDamage;

    public HGStringEntity(World world, LivingEntity entity, float angleXZ, float angleYZ, boolean isBinding) {
        super(ModEntityTypes.HG_STRING.get(), entity, world);
        this.yRotOffset = angleXZ;
        this.xRotOffset = angleYZ;
        this.isBinding = isBinding;
    }
    
    public HGStringEntity(EntityType<? extends HGStringEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean standDamage() {
        return true;
    }
    
    public boolean isBinding() {
        return isBinding;
    }
    
    @Override
    public float getBaseDamage() {
        LivingEntity owner = getOwner();
        float dmg = owner != null ? (float) owner.getAttributeValue(Attributes.ATTACK_DAMAGE) : 
            (float) ModEntityTypes.HIEROPHANT_GREEN.get().getStats().getDamage();
        if (isBinding) {
            dmg *= 0.4F;
        }
        return dmg;
    }
    
    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        return !dealtDamage ? super.hurtTarget(target, owner) : false;
    }
    
    @Override
    protected boolean shouldHurtThroughInvulTicks() {
        return true;
    }
    
    @Override
    protected void afterEntityHit(EntityRayTraceResult entityRayTraceResult, boolean entityHurt) {
        if (entityHurt) {
            dealtDamage = true;
            if (isBinding) {
                Entity target = entityRayTraceResult.getEntity();
                if (target instanceof LivingEntity) {
                    LivingEntity livingTarget = (LivingEntity) target;
                    livingTarget.addEffect(new EffectInstance(ModEffects.STUN.get(), ticksLifespan() - tickCount));
                    attachToEntity(livingTarget);
                }
            }
            else {
                setIsRetracting(true);
            }
        }
    }
    
    @Override
    protected float getMaxHardnessBreakable() {
        return 0.0F;
    }

    @Override
    protected int ticksLifespan() {
        return isBinding ? ModActions.HIEROPHANT_GREEN_STRING_BIND.get().getCooldownValue() : ModActions.HIEROPHANT_GREEN_STRING_ATTACK.get().getCooldownValue();
    }
    
    @Override
    protected float movementSpeed() {
        return isBinding ? 1.28F : 3.2F;
    }
    
    @Override
    protected boolean isBodyPart() {
        return true;
    }

    @Override
    protected Vector3d originOffset(float yRot, float xRot, double distance) {
        return super.originOffset(yRot + yRotOffset, xRot + xRotOffset, distance);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        super.writeSpawnData(buffer);
        buffer.writeFloat(yRotOffset);
        buffer.writeFloat(xRotOffset);
        buffer.writeBoolean(isBinding);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        super.readSpawnData(additionalData);
        this.yRotOffset = additionalData.readFloat();
        this.xRotOffset = additionalData.readFloat();
        this.isBinding = additionalData.readBoolean();
    }
}

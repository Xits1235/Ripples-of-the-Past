package com.github.standobyte.jojo.entity.damaging.projectile.ownerbound;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.power.IPower;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class HGGrapplingStringEntity extends OwnerBoundProjectileEntity {
    private IPower<?> userStandPower;
    private boolean bindEntities;
    private StandEntity stand;

    public HGGrapplingStringEntity(World world, StandEntity entity, IPower<?> userStand) {
        super(ModEntityTypes.HG_GRAPPLING_STRING.get(), entity, world);
        this.userStandPower = userStand;
    }
    
    public HGGrapplingStringEntity(EntityType<? extends HGGrapplingStringEntity> entityType, World world) {
        super(entityType, world);
    }
    
    @Override
    public void tick() {
        super.tick();
        if (!isAlive()) {
            return;
        }
        if (!level.isClientSide()) {
            if (userStandPower == null || userStandPower.getHeldAction() != (bindEntities ? ModActions.HIEROPHANT_GREEN_GRAPPLE_ENTITY.get() : ModActions.HIEROPHANT_GREEN_GRAPPLE.get())) {
                remove();
                return;
            }
            LivingEntity bound = getEntityAttachedTo();
            if (bound != null) {
                LivingEntity owner = getOwner();
                if (!bound.isAlive()) {
                    remove();
                }
                else {
                    Vector3d vecToOwner = owner.position().subtract(bound.position());
                    if (vecToOwner.lengthSqr() > 4) {
                        bound.move(MoverType.PLAYER, vecToOwner.normalize().scale(2D));
                        bound.fallDistance = 0;
                    }
                }
            }
        }
    }
    
    public void setBindEntities(boolean bindEntities) {
        this.bindEntities = bindEntities;
    }
    
    @Override
    protected boolean moveToBlockAttached() {   
        if (super.moveToBlockAttached()) {
            LivingEntity owner = getOwner();
            Vector3d vecFromOwner = position().subtract(owner.position());
            if (vecFromOwner.lengthSqr() > 4) {
                Vector3d grappleVec = vecFromOwner.normalize().scale(2D);
                owner.move(MoverType.SELF, grappleVec);
                if (stand == null && owner instanceof StandEntity) {
                    stand = (StandEntity) owner;
                }
                if (stand != null && stand.isFollowingUser()) {
                    LivingEntity user = stand.getUser();
                    if (user != null) {
                        user.move(MoverType.SELF, grappleVec);
                        user.fallDistance = 0;
                    }
                }
            }
            return true;
        }
        return false;
    }
    
    @Override
    protected boolean isBodyPart() {
        return true;
    }

    private static final Vector3d OFFSET = new Vector3d(-0.3, -0.2, 0.55);
    @Override
    protected Vector3d getOwnerRelativeOffset() {
        return OFFSET;
    }

    @Override
    protected int ticksLifespan() {
        return getEntityAttachedTo() == null && !getBlockPosAttachedTo().isPresent() ? 40 : Integer.MAX_VALUE;
    }
    
    @Override
    protected float movementSpeed() {
        return 2.0F;
    }
    
    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        if (getEntityAttachedTo() == null && bindEntities) {
            if (target instanceof LivingEntity) {
                attachToEntity((LivingEntity) target);
                return true;
            }
        }
        return false;
    }
    
    @Override
    protected void checkRetract() {}
    
    @Override
    protected void afterBlockHit(BlockRayTraceResult blockRayTraceResult, boolean brokenBlock) {
        if (!brokenBlock && !bindEntities) {
            attachToBlockPos(blockRayTraceResult.getBlockPos());
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    @Override
    public float getBaseDamage() {
        return 0;
    }

    @Override
    protected float getMaxHardnessBreakable() {
        return 0;
    }

    @Override
    public boolean standDamage() {
        return true;
    }
}

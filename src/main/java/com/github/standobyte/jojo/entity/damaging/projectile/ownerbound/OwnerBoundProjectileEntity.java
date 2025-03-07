package com.github.standobyte.jojo.entity.damaging.projectile.ownerbound;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.damaging.projectile.ModdedProjectileEntity;
import com.github.standobyte.jojo.util.JojoModUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public abstract class OwnerBoundProjectileEntity extends ModdedProjectileEntity {
    protected static final DataParameter<Boolean> IS_BOUND_TO_OWNER = EntityDataManager.defineId(OwnerBoundProjectileEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Optional<BlockPos>> BLOCK_ATTACHED_TO = EntityDataManager.defineId(OwnerBoundProjectileEntity.class, DataSerializers.OPTIONAL_BLOCK_POS);
    private static final DataParameter<Integer> ENTITY_ATTACHED_TO = EntityDataManager.defineId(OwnerBoundProjectileEntity.class, DataSerializers.INT);
    private static final DataParameter<Boolean> IS_RETRACTING = EntityDataManager.defineId(OwnerBoundProjectileEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float> DISTANCE = EntityDataManager.defineId(OwnerBoundProjectileEntity.class, DataSerializers.FLOAT);
    private LivingEntity attachedEntity;
    private UUID attachedEntityUUID;

    public OwnerBoundProjectileEntity(EntityType<? extends OwnerBoundProjectileEntity> entityType, @Nonnull LivingEntity owner, World world) {
        super(entityType, owner, world);
    }

    public OwnerBoundProjectileEntity(EntityType<? extends OwnerBoundProjectileEntity> entityType, World world) {
        super(entityType, world);
    }
    
    @Override
    public void tick() {
        if (isBoundToOwner()) {
            LivingEntity owner = getOwner();
            if (owner == null) {
                remove();
                return;
            }
        }
        if (!level.isClientSide() && attachedEntityUUID != null && attachedEntity == null) {
            Entity entity = ((ServerWorld) level).getEntity(attachedEntityUUID);
            if (entity instanceof LivingEntity) {
                attachToEntity((LivingEntity) entity);
                attachedEntityUUID = null;
            }
        }
        super.tick();
    }
    
    @Override
    protected void moveProjectile() {
        if (!moveToEntityAttached() && !moveToBlockAttached() && !moveBoundToOwner()) {
            super.moveProjectile();
        }
    }
    
    protected boolean moveBoundToOwner() {
        if (isBoundToOwner()) {
            Entity owner = getOwner();
            setRot(owner.yRot, owner.xRot);
    
            Vector3d originPoint = ownerPosition(1.0F, false);
            Vector3d nextOriginOffset = getNextOriginOffset();
            if (nextOriginOffset == null) {
                remove();
                return true;
            }
    
            double x = getX();
            double y = getY();
            double z = getZ();
            double nextX = originPoint.x + nextOriginOffset.x;
            double nextY = originPoint.y + nextOriginOffset.y;
            double nextZ = originPoint.z + nextOriginOffset.z;
            setDeltaMovement(new Vector3d(nextX - getX(), nextY - getY(), nextZ - getZ()));
    
            xo = x;
            yo = y;
            zo = z;
            xOld = x;
            yOld = y;
            zOld = z;
            setPos(nextX, nextY, nextZ);
            return true;
        }
        return false;
    }
    
    protected boolean moveToEntityAttached() {
        LivingEntity bound = getEntityAttachedTo();
        if (bound != null) {
            moveTo(bound.getX(), bound.getY(0.5D), bound.getZ(), bound.yRot, bound.xRot);
            return true;
        }
        return false;
    }
    
    protected boolean moveToBlockAttached() {
        Optional<BlockPos> blockPosOptional = getBlockPosAttachedTo();
        if (blockPosOptional.isPresent()) {
            BlockPos blockPos = blockPosOptional.get();
            moveTo(
                    blockPos.getX() + 0.5D, 
                    blockPos.getY() + 0.5D, 
                    blockPos.getZ() + 0.5D);
            return true;
        }
        return false;
    }
    
    protected final Vector3d getOriginPoint() {
        return getOriginPoint(1.0F);
    }
    
    public Vector3d getOriginPoint(float partialTick) {
        return ownerPosition(partialTick, isBodyPart());
    }
    
    protected final Vector3d ownerPosition(float partialTick, boolean useBodyRotation) {
        LivingEntity owner = getOwner();
        if (owner != null) {
            return getPos(owner, partialTick, 
                    useBodyRotation ? MathHelper.lerp(partialTick, owner.yBodyRotO, owner.yBodyRot) : MathHelper.lerp(partialTick, owner.yRotO, owner.yRot), 
                            MathHelper.lerp(partialTick, owner.xRotO, owner.xRot));
        }
        return partialTick == 1.0F ? position() : getPosition(partialTick);
    }
    
    protected boolean isBodyPart() {
        return false;
    }
    
    @Nullable
    protected Vector3d getNextOriginOffset() {
        LivingEntity owner = getOwner();
        float distance = updateDistance();
        if (!isRetracting()) {
            checkRetract();
        }
        if (isRetracting() && distance <= 0) {
            return null;
        }
        setDistance(distance);
        return originOffset(owner.yRot, owner.xRot, distance);
    }
    
    protected float updateDistance() {
        if (!isRetracting()) {
            return getDistance() + movementSpeed();
        }
        else {
            return getDistance() - retractSpeed();
        }
    }
    
    protected abstract float movementSpeed();
    
    protected float retractSpeed() {
        return movementSpeed();
    }
    
    protected void checkRetract() {
        if (tickCount >= (double) ticksLifespan() * retractSpeed() / (movementSpeed() + retractSpeed())) {
            setIsRetracting(true);
        }
    }
    
    protected Vector3d originOffset(float yRot, float xRot, double distance) {
        return Vector3d.directionFromRotation(xRot, yRot).scale(distance);
    }
    
    @Override
    public AxisAlignedBB getBoundingBoxForCulling() {
        return getBoundingBox().expandTowards(getOriginPoint().subtract(position()));
    }

    @Override
    protected RayTraceResult rayTrace() {
        Vector3d startPos = getOriginPoint();
        Vector3d endPos = position().add(getDeltaMovement());
        Vector3d rtVec = startPos.subtract(endPos);
        AxisAlignedBB aabb = getBoundingBox().expandTowards(rtVec).inflate(1.0D);
        double minDistance = rtVec.length();
        return JojoModUtil.rayTrace(startPos, endPos, aabb, minDistance, level, this, this::canHitEntity, getBbWidth() / 2);
    }
    
    @Override
    protected boolean hurtTarget(Entity target, DamageSource dmgSource, float dmgAmount) {
        return shouldHurtThroughInvulTicks() ? super.hurtTarget(target, dmgSource, dmgAmount) : target.hurt(dmgSource, dmgAmount);
    }
    
    protected boolean shouldHurtThroughInvulTicks() {
        return false;
    }
    
    @Override
    protected void breakProjectile(TargetType targetType) {}

    @Override
    protected void afterBlockHit(BlockRayTraceResult blockRayTraceResult, boolean blockDestroyed) {
        if (!blockDestroyed) {
            setIsRetracting(true);
        }
    }
    
    protected void setBoundToOwner(boolean value) {
        entityData.set(IS_BOUND_TO_OWNER, value);
    }
    
    public boolean isBoundToOwner() {
        return entityData.get(IS_BOUND_TO_OWNER);
    }
    
    public void attachToEntity(LivingEntity boundTarget) {
        this.attachedEntity = boundTarget;
        entityData.set(ENTITY_ATTACHED_TO, boundTarget.getId());
    }
    
    @Nullable
    protected LivingEntity getEntityAttachedTo() {
        if (attachedEntity == null) {
            int id = entityData.get(ENTITY_ATTACHED_TO);
            if (id == -1) {
                return null;
            }
            Entity entity = level.getEntity(id);
            if (entity instanceof LivingEntity) {
                attachedEntity = (LivingEntity) entity;
            }
        }
        return attachedEntity;
    }
    
    public void attachToBlockPos(BlockPos blockPos) {
        entityData.set(BLOCK_ATTACHED_TO, Optional.of(blockPos));
    }
    
    public Optional<BlockPos> getBlockPosAttachedTo() {
        return entityData.get(BLOCK_ATTACHED_TO);
    }
    
    protected void setDistance(float distance) {
        entityData.set(DISTANCE, distance);
    }
    
    protected float getDistance() {
        return entityData.get(DISTANCE);
    }
    
    protected void setIsRetracting(boolean isRetracting) {
        entityData.set(IS_RETRACTING, isRetracting);
    }
    
    protected boolean isRetracting() {
        return entityData.get(IS_RETRACTING);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(IS_BOUND_TO_OWNER, true);
        entityData.define(ENTITY_ATTACHED_TO, -1);
        entityData.define(BLOCK_ATTACHED_TO, Optional.empty());
        entityData.define(DISTANCE, 0F);
        entityData.define(IS_RETRACTING, false);
    }
    
    @Override
    public boolean isInvisible() {
        boolean ownerInvisible = false;
        if (ownerInvisibility()) {
            LivingEntity owner = getOwner();
            if (owner != null) {
                ownerInvisible = owner.isInvisible();
            }
        }
        return ownerInvisible || super.isInvisible();
    }

    @Override
    public boolean isInvisibleTo(PlayerEntity player) {
        boolean ownerInvisible = false;
        if (ownerInvisibility()) {
            LivingEntity owner = getOwner();
            if (owner != null) {
                ownerInvisible = owner.isInvisibleTo(player);
            }
        }
        return ownerInvisible || super.isInvisibleTo(player);
    }
    
    public boolean ownerInvisibility() {
        return isBodyPart();
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return isBoundToOwner() && getOwner() == ClientUtil.getClientPlayer() ? true : super.shouldRenderAtSqrDistance(distance);
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("BoundToOwner", isBoundToOwner());
        Optional<BlockPos> blockAttachedTo = getBlockPosAttachedTo();
        if (blockAttachedTo.isPresent()) {
            BlockPos pos = blockAttachedTo.get();
            nbt.putIntArray("AttachedBlock", new int[] { pos.getX(), pos.getY(), pos.getZ() } );
        }
        else if (attachedEntity != null) {
            nbt.putUUID("AttachedEntity", attachedEntity.getUUID());
        }
        nbt.putFloat("Distance", getDistance());
        nbt.putBoolean("IsRetracting", isRetracting());
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        setBoundToOwner(nbt.getBoolean("BoundToOwner"));
        int[] posArray = nbt.getIntArray("AttachedBlock");
        if (posArray.length == 3) {
            attachToBlockPos(new BlockPos(posArray[0], posArray[1], posArray[2]));
        }
        else if (nbt.hasUUID("BoundTarget")) {
            this.attachedEntityUUID = nbt.getUUID("AttachedEntity");
        }
        setDistance(nbt.getFloat("Distance"));
        setIsRetracting(nbt.getBoolean("IsRetracting"));
     }
}

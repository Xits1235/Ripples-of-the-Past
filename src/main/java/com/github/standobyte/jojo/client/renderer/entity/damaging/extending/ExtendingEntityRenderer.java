package com.github.standobyte.jojo.client.renderer.entity.damaging.extending;

import com.github.standobyte.jojo.client.model.entity.ownerbound.repeating.RepeatingModel;
import com.github.standobyte.jojo.client.renderer.entity.SimpleEntityRenderer;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.OwnerBoundProjectileEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.util.MathUtil;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public abstract class ExtendingEntityRenderer<T extends OwnerBoundProjectileEntity, M extends RepeatingModel<T>> extends SimpleEntityRenderer<T, M> {

    public ExtendingEntityRenderer(EntityRendererManager renderManager, M model, ResourceLocation texPath) {
        super(renderManager, model, texPath);
    }
    
    protected float getAlpha(T entity, float partialTick) {
        LivingEntity owner = entity.getOwner();
        return owner instanceof StandEntity ? ((StandEntity) entity.getOwner()).getAlpha(partialTick) : 1.0F;
    }

    @Override
    protected void rotateModel(M model, T entity, float partialTick, float yRotation, float xRotation, MatrixStack matrixStack) {
        Vector3d originPos = entity.getOriginPoint(partialTick);
        Vector3d entityPos = new Vector3d(
                MathHelper.lerp((double) partialTick, entity.xo, entity.getX()), 
                MathHelper.lerp((double) partialTick, entity.yo, entity.getY()), 
                MathHelper.lerp((double) partialTick, entity.zo, entity.getZ()));
        Vector3d extentVec = entityPos.subtract(originPos);
        yRotation = MathUtil.yRotDegFromVec(extentVec);
        xRotation = MathUtil.xRotDegFromVec(extentVec);
        model.setLength((float) extentVec.length());
        model.setupAnim(entity, 0, 0, entity.tickCount + partialTick, yRotation, xRotation);
    }
    
    @Override
    protected void doRender(T entity, M model, float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        LivingEntity owner = entity.getOwner();
        if (owner != null) {
            packedLight = entityRenderDispatcher.getPackedLightCoords(entity.getOwner(), partialTick);
        }
        super.doRender(entity, model, partialTick, matrixStack, buffer, packedLight);
    }
}

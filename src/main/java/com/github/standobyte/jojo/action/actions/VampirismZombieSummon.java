package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.mob.HungryZombieEntity;
import com.github.standobyte.jojo.power.IPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

public class VampirismZombieSummon extends Action {

    public VampirismZombieSummon(AbstractBuilder<?> builder) {
        super(builder);
    }
    
    @Override
    public ActionConditionResult checkConditions(LivingEntity user, LivingEntity performer, IPower<?> power, ActionTarget target) {
        World world = user.level;
        if (world.getDifficulty() == Difficulty.PEACEFUL) {
            return conditionMessage("peaceful");
        }
        int zombiesMaxInArea = world.getDifficulty().getId() * 10;
        if (world.getEntitiesOfClass(HungryZombieEntity.class, new AxisAlignedBB(
                user.getX(), 0, user.getZ(), 
                user.getX(), 256, user.getZ())
                .inflate(16, 0, 16))
                .size() > zombiesMaxInArea) {
            return conditionMessage("zombies_limit");
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    public void perform(World world, LivingEntity user, IPower<?> power, ActionTarget target) {
        if (!world.isClientSide()) {
            int zombiesToSummon = world.getDifficulty().getId();
            for (int i = 0; i < zombiesToSummon; i++) {
                HungryZombieEntity zombie = new HungryZombieEntity(world);
                zombie.copyPosition(user);
                zombie.setOwner(user);
                world.addFreshEntity(zombie);
            }
        }
    }
}

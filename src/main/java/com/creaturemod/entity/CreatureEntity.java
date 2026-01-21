package com.creaturemod.entity;

import com.mojang.serialization.Dynamic;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class CreatureEntity extends PathfinderMob {
    public CreatureEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired(); //don't delete the mob during chunk updates!
        this.getNavigation().setCanFloat(true);
        System.out.println("==================NAV TYPE: " + this.getNavigation().getClass() + "=========");
    }


    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 4.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }


    @Override
    protected Brain.Provider<Mob> brainProvider() {
        return CreatureBrain.provider();
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return CreatureBrain.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    @Override
    public Brain<?> getBrain() {
        return super.getBrain();
    }

    @Override
    protected void customServerAiStep() {
        //this.getNavigation().tick();
        //System.out.println("TICK!");
        CreatureBrain.tick((ServerLevel) this.level(), this);
        this.getNavigation().tick();
        super.customServerAiStep();

    }
}

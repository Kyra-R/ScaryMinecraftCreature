package com.creaturemod.entity.behaviors;

import com.creaturemod.entity.necessities.NewMemoryModuleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LungeForPreyBehavior extends Behavior<Mob>{

        private double followRangeSq = 64;
        private Player target;

        public LungeForPreyBehavior() {
            super(Map.of(
                    MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT
            ), 20, 100);
        }

        @Override
        protected boolean checkExtraStartConditions(ServerLevel world, Mob mob) {
            Optional<NearestVisibleLivingEntities> opt = mob.getBrain()
                    .getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
            Optional<Boolean> lungeReady = mob.getBrain().getMemory(NewMemoryModuleTypes.LUNGE_READY);
            if (opt.isPresent() && lungeReady.isPresent() && lungeReady.get()) {
                Iterable<LivingEntity> nearest = opt.get().findAll(e -> e instanceof Player);
                List<LivingEntity> list = new LinkedList<>();
                for (var e : nearest) {
                    list.add(e);
                }
                for (LivingEntity e : list) {
                    if (e instanceof Player player && !player.isSpectator()) {
                        double dx = player.getX() - mob.getX();
                        double dz = player.getZ() - mob.getZ();
                        double dy = player.getY() - mob.getY();
                        double distSq = dx * dx + dy * dy + dz * dz;
                        if (distSq <= this.followRangeSq) {
                            target = player;
                            return true;
                        }
                    }
                }
            }

            return false;
        }

    private boolean IsCreatureVisible(Mob mob){
        if(target == null){
            return false;
        }

        Vec3 playerView = target.getLookAngle().normalize();
        Vec3 toMob = mob.position().subtract(target.position()).normalize();

        double dot = playerView.dot(toMob);
        boolean isVisible = dot > Math.cos(Math.toRadians(45));

        return isVisible;

    }



        @Override
        protected boolean canStillUse(ServerLevel level, Mob mob, long gameTime) {
           // System.out.println(target + " target");
            // Поведение продолжается, пока цель существует и видна
            boolean canContinueToLunge = !IsCreatureVisible(mob) || mob.distanceToSqr(target) < 4; //if visible and too far, stop
            return target != null
                    //&& target.isAlive()
                    && mob.distanceToSqr(target) <= followRangeSq && canContinueToLunge;
                    //&& !IsCreatureVisible(mob);
        }


        @Override
        protected void start(ServerLevel level, Mob mob, long gameTime) {
            if (target == null) return;
            System.out.println("___START LUNGE");

            mob.getBrain().setMemory(
                    MemoryModuleType.LOOK_TARGET,
                    new EntityTracker(target, true)
            );

            mob.getBrain().setMemory(
                    MemoryModuleType.WALK_TARGET,
                    new WalkTarget(new EntityTracker(target, true), 1.0f, 1)
            );

            var nav = mob.getNavigation();
            var path = nav.createPath(target, 0);
            if (path != null) {
                mob.getBrain().setMemory(MemoryModuleType.PATH, path);
                nav.moveTo(path, 1.0f);
                System.out.println("Path exists!");
            } else {
                // если path==null — попытка прямого moveTo
                System.out.println("Path DOES NOT exist!");
                nav.moveTo(target, 1.0f);
            }
        }

        @Override
        protected void tick(ServerLevel level, Mob mob, long gameTime) {
            if (target == null) return;

            mob.getBrain().setMemory(
                    MemoryModuleType.LOOK_TARGET,
                    new EntityTracker(target, true)
            );

            /*mob.getBrain().setMemory(
                    MemoryModuleType.WALK_TARGET,
                    new WalkTarget(new EntityTracker(target, true), 1.2f, 1)
            );*/

            Vec3 pos = mob.position();
            //System.out.println("WALK WORKING | pos=" + pos + " | moving=" + mob.getNavigation().isInProgress()
            //      + " to target " + mob.getBrain().getMemory(MemoryModuleType.WALK_TARGET).orElse(null));
        }



        @Override
        protected void stop(ServerLevel world, Mob mob, long gameTime) {
            mob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            mob.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
            mob.getBrain().eraseMemory(NewMemoryModuleTypes.LUNGE_READY);
            mob.getBrain().setMemory(NewMemoryModuleTypes.LUNGE_ENDED, true);
            //mob.getBrain().setActiveActivityIfPossible(Activity.IDLE);
            System.out.println("STOP=====LUNGE");
        }


}

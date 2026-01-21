package com.creaturemod.entity;

import com.creaturemod.entity.behaviors.BackOffBehavior;
import com.creaturemod.entity.behaviors.CirclePreyBehavior;
import com.creaturemod.entity.behaviors.LungeForPreyBehavior;
import com.creaturemod.entity.necessities.NewMemoryModuleTypes;
import com.google.common.collect.ImmutableList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class CreatureBrain {

    // ——— Инициализация типов памяти и сенсоров ———

    /**
     * Настройка типов памяти и сенсоров, которые нужны существу
     */
    public static Brain.Provider<Mob> provider() {
        return Brain.provider(
                List.of(
                        MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                        MemoryModuleType.LOOK_TARGET,
                        MemoryModuleType.WALK_TARGET,
                        MemoryModuleType.PATH,
                        NewMemoryModuleTypes.LUNGE_READY,
                        NewMemoryModuleTypes.LUNGE_ENDED
                ),
                List.of(
                        SensorType.NEAREST_LIVING_ENTITIES
                )
        );
    }

    /**
     * Конфигурация мозга
     */
    public static Brain<Mob> makeBrain(Brain<Mob> brain) {
        // Базовые активности
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink()
        ));

        brain.addActivity(Activity.FIGHT, 1, ImmutableList.of(
                // new FindPlayerBehavior() // наш кастомный Behavior
                new LungeForPreyBehavior(),
                new BackOffBehavior()
        ));

        brain.addActivity(Activity.IDLE, 2, ImmutableList.of(
                // new FindPlayerBehavior() // наш кастомный Behavior
                new CirclePreyBehavior()
        ));

        brain.setMemory(NewMemoryModuleTypes.LUNGE_READY, false);

        brain.setCoreActivities(Set.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    /**
     * Обновление логики мозга каждый тик
     */
    public static void tick(ServerLevel level, Mob entity) {
        Brain<Mob> brain = (Brain<Mob>) entity.getBrain();
        brain.tick(level, entity);
        //System.out.println("Tick CORE, moving=" + entity.getNavigation().isInProgress());
    }


    /*public static class FindPlayerBehavior extends Behavior<Mob> {

        private double followRangeSq = 64;
        private Player target;

        public FindPlayerBehavior() {
            super(Map.of(
                    MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT
            ));
        }

        @Override
        protected boolean checkExtraStartConditions(ServerLevel world, Mob mob) {
            Optional<NearestVisibleLivingEntities> opt = mob.getBrain()
                    .getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
            if (opt.isPresent()) {
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



        @Override
        protected boolean canStillUse(ServerLevel level, Mob mob, long gameTime) {
            // Поведение продолжается, пока цель существует и видна
            return target != null
                    //&& target.isAlive()
                    && mob.distanceToSqr(target) <= followRangeSq;
        }


        @Override
        protected void start(ServerLevel level, Mob mob, long gameTime) {
            if (target == null) return;

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
                nav.moveTo(path, 0.3f);
                System.out.println("Path exists!");
            } else {
                // если path==null — попытка прямого moveTo
                System.out.println("Path DOES NOT exist!");
                nav.moveTo(target, 0.3f);
            }
        }

        @Override
        protected void tick(ServerLevel level, Mob mob, long gameTime) {
            if (target == null) return;

            mob.getBrain().setMemory(
                    MemoryModuleType.LOOK_TARGET,
                    new EntityTracker(target, true)
            );


            Vec3 pos = mob.position();
        }



        @Override
        protected void stop(ServerLevel world, Mob mob, long gameTime) {
            mob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            mob.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
            target = null;
            System.out.println("STOP=====");
        }


        }*/
}


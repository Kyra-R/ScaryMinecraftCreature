package com.creaturemod.entity.behaviors;

import com.creaturemod.entity.necessities.NewMemoryModuleTypes;
import net.minecraft.core.BlockPos;
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

import java.util.*;

import static java.lang.Math.abs;

public class CirclePreyBehavior extends Behavior<Mob> {

    private double followRangeSq = 256;
    private Player target;

    private BlockPos movementTargetPos;

    private BlockPos prevMovementTargetPos;

    private CirclingDirection CIRCLING_DIRECTION = CirclingDirection.CLOCKWISE;

    private int countTicks = 0;

    private Random rand = new Random();

    private double radiusOfCircle = 8;

    private byte octogonAngle = 0; //bytemask of sorts? 00000001 -> 00000010 -> ...

    public CirclePreyBehavior() {
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
                        //System.out.println("checkExtraStartConditions -> " + true);
                        return true;
                    }
                }
            }
        }
        //System.out.println("checkExtraStartConditions -> " + false);
        return false;
    }



    @Override
    protected boolean canStillUse(ServerLevel level, Mob mob, long gameTime) {
        // Поведение продолжается, пока цель существует и видна
        if(target == null || octogonAngle == 0 || mob.distanceToSqr(target) > followRangeSq
            || mob.getBrain().getMemory(NewMemoryModuleTypes.LUNGE_READY).orElse(false) ||
        prevMovementTargetPos == movementTargetPos) //это чтоб не вис
        {
            System.out.println("BEHAVIOR STOP1: target " + target +
                    ", ANGLE " + octogonAngle + ", DISTANCE LESS " + (mob.distanceToSqr(target) > followRangeSq));
            System.out.println("BEHAVIOR STOP2: lunge ready " + mob.getBrain().getMemory(NewMemoryModuleTypes.LUNGE_READY));
        }


        if(!IsCreatureVisible(mob)){
            System.out.println("Condition met — switching to Lunge!");
            mob.getBrain().setMemory(
                    NewMemoryModuleTypes.LUNGE_READY,
                    true
            );
            mob.getBrain().setActiveActivityIfPossible(Activity.FIGHT);
            return false;
        }



        return target != null && octogonAngle != 0
                //&& target.isAlive()
                && mob.distanceToSqr(target) <= followRangeSq
                && !mob.getBrain().getMemory(NewMemoryModuleTypes.LUNGE_READY).orElse(false);
    }

    private double FindOctogonAngle(){
        double step = Math.PI / 4.0;
        int index = Integer.numberOfTrailingZeros(octogonAngle & 0xFF); // преобразуем бит в индекс [0..7]
        return (step * index);
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
    protected void start(ServerLevel level, Mob mob, long gameTime) {
        if (target == null) return;
        System.out.println("___START CIRCLE");

        octogonAngle = 1;
        countTicks = 0;

        mob.getBrain().setMemory(
                MemoryModuleType.LOOK_TARGET,
                new EntityTracker(target, true)
        );

        mob.getBrain().setMemory(
                MemoryModuleType.WALK_TARGET,
                new WalkTarget(new EntityTracker(target, true), 1.0f, 1) //to coordinates instead of entity
        );

        double dx = Math.sin(FindOctogonAngle()) * radiusOfCircle;
        double dz = -Math.cos(FindOctogonAngle()) * radiusOfCircle; // минус, чтобы 0 (первая вершина) указывала на север

        movementTargetPos = BlockPos.containing(target.position().add(dx, 0, dz));


        //BlockPos targetPos = BlockPos.containing(target.position().add(2.0, 0, 2.0)); //w-e u-d n-s
        var nav = mob.getNavigation();
        var path = nav.createPath(movementTargetPos, 0);

        if (path != null) {
            mob.getBrain().setMemory(MemoryModuleType.PATH, path);
            nav.moveTo(path, 1.0f);
        } else {
            nav.moveTo(movementTargetPos.getX(), movementTargetPos.getY(), movementTargetPos.getZ(), 1.0f);
        }
    }

    @Override
    protected void tick(ServerLevel level, Mob mob, long gameTime) {
        if (target == null) return;
        //System.out.println("TICK!");
        if(countTicks % 120 == 0){ //check if coord to move to updates
            //random direction clockwise counterclockwise

            prevMovementTargetPos = movementTargetPos;

            int randNum = rand.nextInt(5);
            if(randNum == 0){
                CIRCLING_DIRECTION = CirclingDirection.RANDOM;
            } else if(randNum % 2 == 0){
                CIRCLING_DIRECTION = CirclingDirection.COUNTERCLOCKWISE;
            } else {
                CIRCLING_DIRECTION = CirclingDirection.CLOCKWISE;
            }
            System.out.println(randNum + " CIRCLING DIRECTION UPDATED: " + CIRCLING_DIRECTION.toString());
        }


        mob.getBrain().setMemory(
                MemoryModuleType.LOOK_TARGET,
                new EntityTracker(target, true)
        );


            /*mob.getBrain().setMemory(
                    MemoryModuleType.WALK_TARGET,
                    new WalkTarget(new EntityTracker(target, true), 1.2f, 1)
            );*/


        //System.out.println("WALK WORKING | pos=" + pos + " | moving=" + mob.getNavigation().isInProgress()
        //      + " to target " + mob.getBrain().getMemory(MemoryModuleType.WALK_TARGET).orElse(null));

        if(mob.distanceToSqr(movementTargetPos.getBottomCenter()) <= 1.21) {
            if(CIRCLING_DIRECTION == CirclingDirection.CLOCKWISE){
                octogonAngle <<= 1;
            } else if(CIRCLING_DIRECTION == CirclingDirection.COUNTERCLOCKWISE){
                octogonAngle >>= 1;
            } else if(CIRCLING_DIRECTION == CirclingDirection.RANDOM){
                int randNum = rand.nextInt(2);
                if(randNum == 0){
                    octogonAngle <<= 1;
                } else {
                    octogonAngle >>= 1;
                }
            }

            System.out.println("OCTOGON ANGLE = " + octogonAngle + ", MOVEMENT TARGET X coord = " + movementTargetPos.getX());
            double dx = Math.sin(FindOctogonAngle()) * radiusOfCircle;
            double dz = -Math.cos(FindOctogonAngle()) * radiusOfCircle; // минус, чтобы 0 (первая вершина) указывала на север
            //System.out.println("NEW COORDINATES ADDITION: " + dx + " " + dz);

            prevMovementTargetPos = movementTargetPos;
            movementTargetPos = BlockPos.containing(target.position().add(dx, 0, dz));

            //BlockPos targetPos = BlockPos.containing(target.position().add(2.0, 0, 2.0)); //w-e u-d n-s
            var nav = mob.getNavigation();
            var path = nav.createPath(movementTargetPos, 0);

            if (path != null) {
                mob.getBrain().setMemory(MemoryModuleType.PATH, path);
                nav.moveTo(path, 1.0f);
            } else {
                nav.moveTo(movementTargetPos.getX(), movementTargetPos.getY(), movementTargetPos.getZ(), 1.0f);
            }
        } else {
            //System.out.println("OCTOGON ANGLE SAME " + octogonAngle);
        }
        countTicks++;
    }


    @Override
    protected boolean timedOut(long gameTime) {
        return false; // никогда не завершается по таймеру
    }



    @Override
    protected void stop(ServerLevel world, Mob mob, long gameTime) {
        if(!mob.getBrain().getMemory(NewMemoryModuleTypes.LUNGE_READY).orElse(true)){
            mob.getBrain().eraseMemory(NewMemoryModuleTypes.LUNGE_READY);
        }

        mob.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        mob.getBrain().eraseMemory(MemoryModuleType.PATH);
        mob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
;
        target = null;
        movementTargetPos = null;
        octogonAngle = 0;
        System.out.println("STOP=====CIRCLE");
    }


}

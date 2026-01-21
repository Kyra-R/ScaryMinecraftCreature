package com.creaturemod.entity.necessities;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.Optional;

import static com.creaturemod.UntitledDemonMod.MOD_ID;

public class NewMemoryModuleTypes {
        public static final MemoryModuleType<Boolean> LUNGE_READY = register("lunge_ready");
        public static final MemoryModuleType<Boolean> LUNGE_ENDED = register("lunge_ended");

        private static <T> MemoryModuleType<T> register(String name) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MOD_ID , name);

            MemoryModuleType type = new MemoryModuleType<>(Optional.empty());

            return Registry.register(BuiltInRegistries.MEMORY_MODULE_TYPE, id, type);
        }

        public static void init() {
            // просто чтобы вызвать при инициализации мода
        }
    }

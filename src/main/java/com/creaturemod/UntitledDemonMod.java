package com.creaturemod;

import com.creaturemod.entity.CreatureEntity;
import com.creaturemod.entity.necessities.NewMemoryModuleTypes;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import net.minecraft.world.entity.MobCategory;
import org.intellij.lang.annotations.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UntitledDemonMod implements ModInitializer {
	public static final String MOD_ID = "untitled-demon-mod";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final EntityType<CreatureEntity> CUBE =
			Registry.register(
					BuiltInRegistries.ENTITY_TYPE,
					ResourceLocation.fromNamespaceAndPath(MOD_ID , "cube"),
					EntityType.Builder
							.of(CreatureEntity::new, MobCategory.CREATURE)
							.sized(0.75f, 0.75f)
							.build("cube")
			);

	@Override
	public void onInitialize() {
		System.out.println("=========INIT==========");
		NewMemoryModuleTypes.init();
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		FabricDefaultAttributeRegistry.register(CUBE, CreatureEntity.createAttributes());
		LOGGER.info("Hello Fabric world!");
	}
}
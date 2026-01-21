package com.creaturemod;

import com.creaturemod.model.CreatureEntityModel;
import com.creaturemod.renderer.CreatureEntityRenderer;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

import static com.creaturemod.UntitledDemonMod.CUBE;

public class UntitledDemonModClient implements ClientModInitializer {

	public static final String MOD_ID = "untitled-demon-mod";

	public static final ModelLayerLocation CREATURE_LAYER =
			new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MOD_ID, "cube"), "main");

	@Override
	public void onInitializeClient() {
		// 1) Регистрируем слой модели (LayerDefinition), чтобы context.bakeLayer(...) работал
		EntityModelLayerRegistry.registerModelLayer(CREATURE_LAYER, CreatureEntityModel::createBodyLayer);

		// 2) Регистрируем рендерер сущности
		EntityRendererRegistry.register(
				CUBE,
				CreatureEntityRenderer::new
		);
	}
	}

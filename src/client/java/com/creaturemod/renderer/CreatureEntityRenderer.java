package com.creaturemod.renderer;

import com.creaturemod.UntitledDemonModClient;
import com.creaturemod.entity.CreatureEntity;
import com.creaturemod.model.CreatureEntityModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CreatureEntityRenderer extends MobRenderer<CreatureEntity, CreatureEntityModel> {

    public static final String MOD_ID = "untitled-demon-mod";

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/entity/cube.png");

    public CreatureEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new CreatureEntityModel(context.bakeLayer(UntitledDemonModClient.CREATURE_LAYER)), 0.5f); //?
    }

    @Override
    public ResourceLocation getTextureLocation(CreatureEntity entity) {
        return TEXTURE;
    }
}

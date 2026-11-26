package cam72cam.immersiverailroading.render.item;

import cam72cam.immersiverailroading.items.ItemRailAugment;
import cam72cam.immersiverailroading.registry.AugmentDefinition;
import cam72cam.immersiverailroading.registry.DefinitionManager;
import cam72cam.mod.item.ItemStack;
import cam72cam.mod.math.Vec3d;
import cam72cam.mod.render.Color;
import cam72cam.mod.render.ItemRender;
import cam72cam.mod.render.StandardModel;
import cam72cam.mod.render.common.ModelRenderer;
import cam72cam.mod.world.World;
import util.Matrix4;

public class RailAugmentItemModel implements ItemRender.IItemModel {
	@Override
	public StandardModel getModel(World world, ItemStack stack) {
		ItemRailAugment.Data data = new ItemRailAugment.Data(stack);
		Color color = data.augment.color();
		StandardModel fallback = new StandardModel().addColorBlock(color, new Matrix4().translate(0, 0.4, 0).scale(1, 0.2f, 1));
		if (data.modelId == null || data.modelId.isEmpty()) {
			return fallback;
		}
		return new StandardModel().addCustom((state, pt) -> {
			AugmentDefinition definition = data.modelId == null ? null : DefinitionManager.getAugmentDefinition(data.modelId);
			if (definition == null || definition.augment != data.augment) {
				fallback.render(state);
			} else {
				try (ModelRenderer.Binding bound = ModelRenderer.getRendererFor(definition.model).bind(state)) {
					bound.enqueueTransparent();
				}
			}
		});
	}
}

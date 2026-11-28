package cam72cam.immersiverailroading.registry;

import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.immersiverailroading.library.Augment;
import cam72cam.immersiverailroading.util.DataBlock;
import cam72cam.mod.model.common.ModelLoader;
import cam72cam.mod.model.common.mesh.Model;
import cam72cam.mod.resource.Identifier;

/** A resource-pack appearance for a rail augment. */
public class AugmentDefinition {
    public final String id;
    public final String name;
    public final Augment augment;
    public final Model model;
    public final boolean alignToTrack;

    AugmentDefinition(String id, DataBlock data) throws Exception {
        this.id = id;
        this.name = data.getValue("name").asString(id);
        String type = data.getValue("augment").asString();
        String modelPath = data.getValue("model").asString();
        if (type == null || modelPath == null || modelPath.isEmpty()) {
            throw new IllegalArgumentException("Augment definition requires augment and model");
        }
        this.augment = Augment.valueOf(type);
        this.alignToTrack = data.getValue("align_to_track").asBoolean(false);
        Identifier resource = modelPath.contains(":") ? new Identifier(modelPath) : new Identifier(ImmersiveRailroading.MODID, modelPath);
        this.model = ModelLoader.load(resource);
    }
}

# Rail augment models

Resource packs can add appearances for the existing functional rail augments.
The appearance determines the model only; the augment type still determines
the behavior. Existing augments without an appearance continue to use the
original colored block.

Add an index at `assets/immersiverailroading/augment/augment.json`:

```json
{
  "types": ["example/detector"]
}
```

Each name in `types` points to a definition under the same `augment` directory.
For example, `assets/immersiverailroading/augment/example/detector.json`:

```json
{
  "name": "Example detector",
  "augment": "DETECTOR",
  "model": "immersiverailroading:models/augment/example/detector.obj"
}
```

`name` is the displayed name, `augment` is one of `SPEED_RETARDER`,
`WATER_TROUGH`, `LOCO_CONTROL`, `ITEM_LOADER`, `ITEM_UNLOADER`,
`FLUID_LOADER`, `FLUID_UNLOADER`, `DETECTOR`, `COUPLER`, or `ACTUATOR`,
and `model` is a resource identifier understood by the model loader. The
model may refer to its materials and textures in the usual OBJ/MTL format.
The water trough is currently unavailable for normal placement.

Both index and definitions may use CAML instead of JSON. Indexes from resource
packs are combined. A definition's stable ID is
`immersiverailroading:augment/example/detector.json`, including when the
definition is a CAML file. Use distinct names for distinct variants. As with
other resources, a higher-priority pack may replace a definition or model.
When both definition formats exist for the same name, JSON is preferred;
use the same extension when overriding a definition from another pack.

Model coordinates are local to the block: the block occupies 0 to 1 on each
axis, with Y=0 at its base. The model is rendered without automatic rotation
or scaling, and the item uses the same coordinates. Design the model for this
space, including any geometry that should be visible over the rail bed.
The model does not automatically conform to the rail bed's height or slope,
or turn with the track; take this into account when designing the appearance.

The selected ID is stored with the item and placed augment. If its pack is
missing, the ID is retained and the original colored block is displayed; the
appearance can return when the pack is restored. Resource packs must be
available on clients to display their models.

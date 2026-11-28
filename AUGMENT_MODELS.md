# Rail augment models

Resource packs can add appearances for rail augments, including decorative ones.
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
  "model": "immersiverailroading:models/augment/example/detector.obj",
  "align_to_track": true
}
```

`name` is the displayed name, `augment` is one of `SPEED_RETARDER`,
`WATER_TROUGH`, `LOCO_CONTROL`, `ITEM_LOADER`, `ITEM_UNLOADER`,
`FLUID_LOADER`, `FLUID_UNLOADER`, `DETECTOR`, `COUPLER`, `ACTUATOR`, or
`DECORATIVE`,
and `model` is a resource identifier understood by the model loader. The
model may refer to its materials and textures in the usual OBJ/MTL format.
The water trough is currently unavailable for normal placement. `DECORATIVE`
has no redstone, loading, or other operational behavior and does not open a
configuration screen. It can be used for signs, markings, and other visual
track equipment. Like other augments, it occupies one rail block and can be
removed with the large wrench.

For example, a purely visual track marker can use:

```json
{
  "name": "Track marker",
  "augment": "DECORATIVE",
  "model": "immersiverailroading:models/augment/example/marker.obj",
  "align_to_track": true
}
```

Both index and definitions may use CAML instead of JSON. Indexes from resource
packs are combined. The creative and casting menus show built-in augments first,
then custom models in the order they appear in the indexes. List a pack's models
together in one index to keep them together in these menus. JSON indexes are
processed before CAML indexes.

A definition's stable ID is
`immersiverailroading:augment/example/detector.json`, including when the
definition is a CAML file. Use distinct names for distinct variants. As with
other resources, a higher-priority pack may replace a definition or model.
When both definition formats exist for the same name, JSON is preferred;
use the same extension when overriding a definition from another pack.

Model coordinates are local to the block: the block occupies 0 to 1 on each
axis, with Y=0 at its base. `align_to_track` opts into horizontal alignment:
make the model's longitudinal direction the X axis, centered on X=0.5 and
Z=0.5. Each block follows the nearest track path, including curves and switch
branches. On iterable paths, the model is aligned to the nearest one-block-wide
lane relative to the track axis, preserving its lateral block position. Crossings
and tables retain block-centered positioning.
On a shared crossing or switch section the choice is deterministic
but may be ambiguous. Without this option, existing models remain unrotated.
Items are unrotated. Models are not scaled or fitted to bed height, slope or
cant.

The selected ID is stored with the item and placed augment. If its pack is
missing, the ID is retained and the original colored block is displayed; the
appearance can return when the pack is restored. Resource packs must be
available on clients to display their models.

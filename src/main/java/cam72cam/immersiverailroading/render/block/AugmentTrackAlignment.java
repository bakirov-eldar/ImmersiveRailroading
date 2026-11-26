package cam72cam.immersiverailroading.render.block;

import cam72cam.immersiverailroading.library.TrackItems;
import cam72cam.immersiverailroading.tile.TileRail;
import cam72cam.immersiverailroading.tile.TileRailBase;
import cam72cam.immersiverailroading.track.BuilderBase;
import cam72cam.immersiverailroading.track.BuilderSwitch;
import cam72cam.immersiverailroading.track.IIterableTrack;
import cam72cam.immersiverailroading.track.VecYPR;
import cam72cam.immersiverailroading.util.RailInfo;
import cam72cam.mod.math.Vec3d;

import java.util.List;

/** Horizontal alignment of a block-local, X-axis model to the nearest track. */
final class AugmentTrackAlignment {
    private double bestDistance = Double.POSITIVE_INFINITY;
    private float yaw;
    private final Vec3d target;
    private final Vec3d parentPosition;

    private AugmentTrackAlignment(TileRailBase tile, TileRail parent) {
        target = new Vec3d(tile.getPos()).add(0.5, tile.getRailHeight(), 0.5);
        parentPosition = new Vec3d(parent.getPos());
    }

    static float rotation(TileRailBase tile, TileRail parent) {
        RailInfo info = parent.info;
        if (info.settings.type == TrackItems.CROSSING) {
            // The center is shared by both routes; choose X deterministically.
            double x = tile.getPos().x - parent.getPos().x;
            double z = tile.getPos().z - parent.getPos().z;
            return Math.abs(x) >= Math.abs(z) ? 0 : -90;
        }
        if (info.settings.type == TrackItems.TURNTABLE) {
            return (float) (-info.tablePos - info.placementInfo.facing().getAngle() - 90);
        }
        if (info.settings.type.isTable()) {
            return info.placementInfo.facing().getAngle() - 270;
        }

        AugmentTrackAlignment result = new AugmentTrackAlignment(tile, parent);
        result.yaw = info.placementInfo.yaw;
        result.find(info.getBuilder(tile.getWorld()));
        // Track yaw is measured from +Z towards +X; the OBJ's length is +X.
        return result.yaw - 90;
    }

    private void find(BuilderBase builder) {
        if (builder instanceof BuilderSwitch) {
            for (BuilderBase branch : ((BuilderSwitch) builder).getAlignmentBuilders()) find(branch);
            return;
        }
        if (!(builder instanceof IIterableTrack)) return;
        IIterableTrack track = (IIterableTrack) builder;
        List<BuilderBase> children = track.getSubBuilders();
        if (children != null && !children.isEmpty()) {
            for (BuilderBase child : children) find(child);
            return;
        }
        Vec3d origin = parentPosition.add(builder.pos).add(builder.info.placementInfo.placementPosition)
                .add(0, builder.info.getTrackHeight() * builder.info.settings.gauge.scale(), 0);
        for (VecYPR point : track.getPath(0.25 * builder.info.settings.gauge.scale())) {
            double distance = origin.add(point).distanceToSquared(target);
            if (distance < bestDistance) {
                bestDistance = distance;
                yaw = point.getYaw();
            }
        }
    }
}

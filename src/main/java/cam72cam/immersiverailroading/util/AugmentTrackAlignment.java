package cam72cam.immersiverailroading.util;

import cam72cam.immersiverailroading.library.TrackItems;
import cam72cam.immersiverailroading.tile.TileRail;
import cam72cam.immersiverailroading.tile.TileRailBase;
import cam72cam.immersiverailroading.track.BuilderBase;
import cam72cam.immersiverailroading.track.BuilderSwitch;
import cam72cam.immersiverailroading.track.IIterableTrack;
import cam72cam.immersiverailroading.track.VecYPR;
import cam72cam.mod.math.Vec3d;
import cam72cam.mod.math.Vec3i;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class AugmentTrackAlignment {
    public static final class Alignment {
        public final float rotation;
        public final double offsetX;
        public final double offsetZ;

        private Alignment(float rotation, double offsetX, double offsetZ) {
            this.rotation = rotation;
            this.offsetX = offsetX;
            this.offsetZ = offsetZ;
        }
    }

    private static final class CacheEntry {
        final WeakReference<TileRail> parent;
        final WeakReference<RailInfo> info;
        final Vec3i tilePos;
        final Vec3i parentPos;
        final Alignment alignment;

        CacheEntry(TileRailBase tile, TileRail parent, RailInfo info, Alignment alignment) {
            this.parent = new WeakReference<>(parent);
            this.info = new WeakReference<>(info);
            this.tilePos = tile.getPos();
            this.parentPos = parent.getPos();
            this.alignment = alignment;
        }
    }

    private static final Map<TileRailBase, CacheEntry> CACHE = new WeakHashMap<>();

    private AugmentTrackAlignment() {}

    public static synchronized Alignment get(TileRailBase tile, TileRail parent) {
        RailInfo info = parent.info;
        if (info == null) return new Alignment(0, 0, 0);
        CacheEntry cached = CACHE.get(tile);
        if (cached != null && cached.parent.get() == parent && cached.info.get() == info
                && cached.tilePos.equals(tile.getPos()) && cached.parentPos.equals(parent.getPos())) {
            return cached.alignment;
        }
        Alignment alignment = snapToBlockLane(calculate(tile, parent, info));
        CACHE.put(tile, new CacheEntry(tile, parent, info, alignment));
        return alignment;
    }

    public static Alignment snapToBlockLane(Alignment alignment) {
        double radians = Math.toRadians(alignment.rotation);
        double normalX = Math.sin(radians);
        double normalZ = Math.cos(radians);
        double distance = -alignment.offsetX * normalX - alignment.offsetZ * normalZ;
        // Use the same tie-break at block boundaries in either track direction.
        double direction = normalX > 1e-9 || (Math.abs(normalX) <= 1e-9 && normalZ >= 0) ? 1 : -1;
        double lane = direction * Math.floor(direction * distance + 0.5 + 1e-9);
        double shift = lane - distance;
        return new Alignment(alignment.rotation,
                shift * normalX, shift * normalZ);
    }

    private static Alignment calculate(TileRailBase tile, TileRail parent, RailInfo info) {
        if (info.settings.type == TrackItems.CROSSING) {
            double x = tile.getPos().x - parent.getPos().x;
            double z = tile.getPos().z - parent.getPos().z;
            return new Alignment(Math.abs(x) >= Math.abs(z) ? 0 : -90, 0, 0);
        }
        if (info.settings.type == TrackItems.TURNTABLE) {
            return new Alignment((float) (-info.tablePos - info.placementInfo.facing().getAngle() - 90), 0, 0);
        }
        if (info.settings.type.isTable()) {
            return new Alignment(info.placementInfo.facing().getAngle() - 270, 0, 0);
        }
        Vec3d target = new Vec3d(tile.getPos()).add(0.5, tile.getRailHeight(), 0.5);
        Alignment nearest = find(info.getBuilder(tile.getWorld()), new Vec3d(parent.getPos()), target);
        return nearest != null ? nearest : new Alignment(info.placementInfo.yaw - 90, 0, 0);
    }

    private static Alignment find(BuilderBase builder, Vec3d parentPosition, Vec3d target) {
        if (builder == null) return null;
        if (builder instanceof BuilderSwitch) {
            return findNearest(((BuilderSwitch) builder).getAlignmentBuilders(), parentPosition, target);
        }
        if (!(builder instanceof IIterableTrack)) return null;
        IIterableTrack track = (IIterableTrack) builder;
        List<BuilderBase> children = track.getSubBuilders();
        if (children != null && !children.isEmpty()) return findNearest(children, parentPosition, target);
        Vec3d origin = parentPosition.add(builder.pos).add(builder.info.placementInfo.placementPosition);
        return project(track.getPath(0.25 * builder.info.settings.gauge.scale()), origin, target);
    }

    private static Alignment findNearest(List<BuilderBase> builders, Vec3d parentPosition, Vec3d target) {
        Alignment best = null;
        for (BuilderBase builder : builders) {
            Alignment candidate = find(builder, parentPosition, target);
            if (candidate != null && (best == null || distanceSquared(candidate) < distanceSquared(best))) {
                best = candidate;
            }
        }
        return best;
    }

    private static double distanceSquared(Alignment alignment) {
        return alignment.offsetX * alignment.offsetX + alignment.offsetZ * alignment.offsetZ;
    }

    public static Alignment project(List<VecYPR> path, Vec3d origin, Vec3d target) {
        if (path == null || path.isEmpty()) return null;
        Alignment best = null;
        int count = Math.max(1, path.size() - 1);
        for (int i = 0; i < count; i++) {
            VecYPR a = path.get(i);
            VecYPR b = path.get(Math.min(i + 1, path.size() - 1));
            double ax = origin.x + a.x;
            double az = origin.z + a.z;
            double dx = b.x - a.x;
            double dz = b.z - a.z;
            double lengthSquared = dx * dx + dz * dz;
            if (!Double.isFinite(ax) || !Double.isFinite(az) || !Double.isFinite(lengthSquared)
                    || !Float.isFinite(a.getYaw()) || !Float.isFinite(b.getYaw())) continue;
            double t = lengthSquared > 1e-12
                    ? Math.max(0, Math.min(1, ((target.x - ax) * dx + (target.z - az) * dz) / lengthSquared)) : 0;
            double offsetX = ax + t * dx - target.x;
            double offsetZ = az + t * dz - target.z;
            if (!Double.isFinite(offsetX) || !Double.isFinite(offsetZ)) continue;
            float yaw = (float) (a.getYaw() + Math.IEEEremainder(b.getYaw() - a.getYaw(), 360) * t);
            Alignment candidate = new Alignment(yaw - 90, offsetX, offsetZ);
            if (best == null || distanceSquared(candidate) < distanceSquared(best)) best = candidate;
        }
        return best;
    }
}

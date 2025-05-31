package dev.loneranger.addon.modules;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Categories;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.Entity;

public class ItemFrameDupe extends Module {
    public ItemFrameDupe() {
        super(Categories.Misc, "Item Frame Dupe", "Detects item frame under crosshair when enabled.");
    }

    @Override
    public void onActivate() {
        if (mc.player == null || mc.world == null) {
            toggle();
            return;
        }

        double reach = 4.5;
        Vec3d cameraPos = mc.player.getCameraPosVec(1.0F);
        Vec3d rotation = mc.player.getRotationVec(1.0F);
        Vec3d end = cameraPos.add(rotation.multiply(reach));

        Entity closest = null;
        double closestDist = reach * reach;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof ItemFrameEntity)) continue;
            Box box = entity.getBoundingBox().expand(0.3); // small tolerance
            Vec3d hit = box.raycast(cameraPos, end).orElse(null);
            if (hit != null) {
                double dist = cameraPos.squaredDistanceTo(hit);
                if (dist < closestDist) {
                    closest = entity;
                    closestDist = dist;
                }
            }
        }

        if (closest != null) {
            info("Looking at an Item Frame.");
        } else {
            info("Not looking at an Item Frame.");
        }

        toggle();
    }
}

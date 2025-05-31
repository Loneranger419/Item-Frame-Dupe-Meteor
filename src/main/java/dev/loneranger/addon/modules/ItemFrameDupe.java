package dev.loneranger.addon.modules;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Categories;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.hit.HitResult;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;

public class ItemFrameDupe extends Module {
    public ItemFrameDupe() {
        super(Categories.Misc, "Item Frame Dupe", "Detects or places item frame.");
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
            Box box = entity.getBoundingBox().expand(0.3);
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
            info("Not looking at an Item Frame. Attempting to place one...");

            int slot = findItemInHotbar(Items.ITEM_FRAME);
            if (slot == -1) {
                info("No item frame in hotbar.");
            } else {
                int prevSlot = mc.player.getInventory().getSelectedSlot();
                mc.player.getInventory().setSelectedSlot(slot);

                // Use raycast to find block face to place on
                HitResult hitResult = mc.crosshairTarget;
                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult bhr = (BlockHitResult) hitResult;
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
                } else {
                    info("No block in view to place item frame on.");
                }

                mc.player.getInventory().setSelectedSlot(prevSlot);
            }
        }
    }

    private int findItemInHotbar(Item item) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) return i;
        }
        return -1;
    }
}

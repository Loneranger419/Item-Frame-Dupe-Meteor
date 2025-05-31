package dev.loneranger.addon.modules;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.Entity;

public class ItemFrameDupe extends Module {
    private int tickCounter = 0;
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> delayTicks = sgGeneral.add(new IntSetting.Builder()
        .name("delay-ticks")
        .description("Ticks to wait between dupe cycles.")
        .defaultValue(4)
        .min(1)
        .sliderMax(20)
        .build()
    );

    private final Setting<Double> reachDistance = sgGeneral.add(new DoubleSetting.Builder()
        .name("reach-distance")
        .description("Reach distance to detect item frames.")
        .defaultValue(4.5)
        .min(1.0)
        .sliderMax(6.0)
        .build()
    );

    public ItemFrameDupe() {
        super(Categories.Misc, "Item Frame Dupe", "Loops shulker in/out of item frames.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;
        if (++tickCounter < delayTicks.get()) return;
        tickCounter = 0;

        double reach = reachDistance.get();
        Vec3d cameraPos = mc.player.getCameraPosVec(1.0F);
        Vec3d rotation = mc.player.getRotationVec(1.0F);
        Vec3d end = cameraPos.add(rotation.multiply(reach));

        ItemFrameEntity targetFrame = null;
        double closestDist = reach * reach;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof ItemFrameEntity)) continue;
            Box box = entity.getBoundingBox().expand(0.3);
            Vec3d hit = box.raycast(cameraPos, end).orElse(null);
            if (hit != null) {
                double dist = cameraPos.squaredDistanceTo(hit);
                if (dist < closestDist) {
                    targetFrame = (ItemFrameEntity) entity;
                    closestDist = dist;
                }
            }
        }

        if (targetFrame != null) {
            int shulkerSlot = findShulkerInHotbar();
            if (shulkerSlot != -1) {
                int prevSlot = mc.player.getInventory().getSelectedSlot();
                mc.player.getInventory().setSelectedSlot(shulkerSlot);

                mc.interactionManager.interactEntity(mc.player, targetFrame, Hand.MAIN_HAND);

                if (targetFrame.getHeldItemStack().getItem().toString().contains("shulker_box")) {
                    mc.interactionManager.attackEntity(mc.player, targetFrame);
                    mc.player.swingHand(Hand.MAIN_HAND);
                }

                mc.player.getInventory().setSelectedSlot(prevSlot);
            }
        } else {
            int itemFrameSlot = findItemInHotbar(Items.ITEM_FRAME);
            if (itemFrameSlot != -1) {
                int prevSlot = mc.player.getInventory().getSelectedSlot();
                mc.player.getInventory().setSelectedSlot(itemFrameSlot);

                HitResult hitResult = mc.crosshairTarget;
                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult bhr = (BlockHitResult) hitResult;
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
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

    private int findShulkerInHotbar() {
        for (int i = 0; i < 9; i++) {
            Item item = mc.player.getInventory().getStack(i).getItem();
            if (item.toString().contains("shulker_box")) return i;
        }
        return -1;
    }
}

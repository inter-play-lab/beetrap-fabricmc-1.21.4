package beetrap.btfmc;

import beetrap.btfmc.networking.PlayerPollinateC2SPayload;
import beetrap.btfmc.networking.PlayerTargetNewEntityC2SPayload;
import beetrap.btfmc.networking.PlayerTimeTravelRequestC2SPayload;
import beetrap.btfmc.networking.PlayerTimeTravelRequestC2SPayload.Operations;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResult.Pass;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.world.World;

public class BeetrapGameClient {
    private final MinecraftClient client;
    private Entity targetedEntity;
    private Entity glowingEntity;

    public BeetrapGameClient() {
        this.client = MinecraftClient.getInstance();
    }

    private boolean updateTargetEntity() {
        HitResult hit = client.crosshairTarget;

        if(hit == null) {
            return false;
        }

        switch(hit.getType()) {
            case Type.MISS, Type.BLOCK -> {
                if(this.targetedEntity == null) {
                    return false;
                }

                this.targetedEntity = null;
            }

            case Type.ENTITY -> {
                EntityHitResult entityHit = (EntityHitResult)hit;
                Entity entity = entityHit.getEntity();

                if(this.targetedEntity == entity) {
                    return false;
                }

                this.targetedEntity = entity;
                ClientGlowingEntityManager.addGlowingEntity(this.targetedEntity);
            }
        }

        return true;
    }

    private void updateGlowingEntity() {
        if(this.targetedEntity == this.glowingEntity) {
            return;
        }

        if(this.glowingEntity != null) {
            ClientGlowingEntityManager.removeGlowingEntity(this.glowingEntity);
        }

        this.glowingEntity = this.targetedEntity;

        if(this.targetedEntity != null) {
            ClientGlowingEntityManager.addGlowingEntity(this.glowingEntity);
        }
    }

    public void onStartWorldTick(ClientWorld clientWorld) {
        if(this.updateTargetEntity()) {
            this.updateGlowingEntity();

            PlayerTargetNewEntityC2SPayload ptnec2sp;

            if(this.targetedEntity != null) {
                ptnec2sp = new PlayerTargetNewEntityC2SPayload(true, this.targetedEntity.getId());
            } else {
                ptnec2sp = new PlayerTargetNewEntityC2SPayload(false, 0);
            }

            ClientPlayNetworking.send(ptnec2sp);
        }
    }

    public ActionResult onPlayerUseItem(PlayerEntity player, World world, Hand hand) {
        ActionResult ar = new Pass();

        switch(player.getInventory().selectedSlot) {
            case 0 -> {
                ClientPlayNetworking.send(new PlayerTimeTravelRequestC2SPayload(-1, Operations.ADD));
            }

            case 4 -> {
                PlayerPollinateC2SPayload ppc2sp;

                if(this.targetedEntity != null) {
                    ppc2sp = new PlayerPollinateC2SPayload(true, this.targetedEntity.getId());
                } else {
                    ppc2sp = new PlayerPollinateC2SPayload(false, 0);
                }

                ClientPlayNetworking.send(ppc2sp);
            }

            case 8 -> {
                ClientPlayNetworking.send(new PlayerTimeTravelRequestC2SPayload(1, Operations.ADD));
            }
        }

        return ar;
    }

    public void onEntityPositionUpdate(int entityId, double x, double y, double z) {
        Entity e = this.client.world.getEntityById(entityId);

        if(e == null) {
            return;
        }

        e.setPosition(x, y, z);
    }
}

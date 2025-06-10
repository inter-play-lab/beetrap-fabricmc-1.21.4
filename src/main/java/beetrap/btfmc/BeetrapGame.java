package beetrap.btfmc;

import beetrap.btfmc.flower.Flower;
import beetrap.btfmc.flower.FlowerManager;
import beetrap.btfmc.flower.FlowerPool;
import beetrap.btfmc.flower.FlowerValueScoreboardDisplayerService;
import beetrap.btfmc.networking.NetworkingService;
import beetrap.btfmc.networking.PlayerTimeTravelRequestC2SPayload.Operations;
import beetrap.btfmc.state.BeetrapState;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3i;
import net.minecraft.util.math.Vec3d;

public class BeetrapGame {
    public static final int AMOUNT_OF_BUDS_RANKED = 3;
    public static final int AMOUNT_OF_BUDS_TO_PLACE = 40;
    public static final double SMALL_POLLINATION_CIRCLE_RADIUS = 3;
    public static final double LARGE_POLLINATION_CIRCLE_RADIUS = 3.5;
    public static final int AMOUNT_OF_FLOWERS_TO_WITHER_DEFAULT_MODE = 3;
    public static final int AMOUNT_OF_FLOWERS_TO_WITHER_DIVERSIFYING_MODE = 1;
    public static final BlockPos CHANGE_RANKING_METHOD_LEVER_POSITION = new BlockPos(0, 1, -22);
    private static final long TICK_INTERVAL_NANO = 50_000_000L;
    private final MinecraftServer server;
    private final ServerWorld world;
    private final FlowerManager flowerManager;
    private final BeeNestController beeNestController;
    private final PlayerInteractionService interaction;
    private final NetworkingService net;
    private BeetrapState state;
    private final GardenInformationBossBar gardenInformationBossBar;
    private final PollinationController pollinationController;
    private long lastTickTime;
    private final Vector3i bottomLeft;
    private final Vector3i topRight;
    private double pollinationCircleRadius;
    private boolean usingDiversifyingRankingMethod;
    private int amountOfFlowersToWither;

    public BeetrapGame(MinecraftServer server, Vector3i bottomLeft, Vector3i topRight) {
        this.server = server;
        this.bottomLeft = bottomLeft;
        this.topRight = topRight;
        this.world = this.server.getOverworld();
        this.net = new NetworkingService(this.world);
        FlowerPool flowerPool = new FlowerPool(200);
        this.flowerManager = new FlowerManager(this, 200, this.world, bottomLeft, topRight);
        this.beeNestController = new BeeNestController(this, this.world, this.net, this.calculateNestBase(bottomLeft, topRight));
        this.state = new BeetrapState(flowerPool);
        this.state.populateFlowers(20);
        this.interaction = new PlayerInteractionService(this.world, this.flowerManager, new FlowerValueScoreboardDisplayerService(server));
        this.gardenInformationBossBar = new GardenInformationBossBar(this, this.server);
        this.pollinationController = new PollinationController(this, this.world, this.flowerManager, this.beeNestController, this.interaction, this.gardenInformationBossBar);
        this.flowerManager.placeFlowerEntities(this.state);
        this.beeNestController.spawnNest();
        this.pollinationCircleRadius = SMALL_POLLINATION_CIRCLE_RADIUS;
        this.amountOfFlowersToWither = AMOUNT_OF_FLOWERS_TO_WITHER_DEFAULT_MODE;
    }

    private void sendMessageToAllPlayers(String message) {
        this.world.getPlayers().forEach(player -> player.sendMessage(Text.of(message)));
    }

    public void onWorldTick() {
        if(System.nanoTime() - this.lastTickTime < TICK_INTERVAL_NANO) return;
        this.lastTickTime = System.nanoTime();

        this.pollinationController.tick();

        this.clearItems();

        if(this.interaction.rankingMethodLeverChanged()) {
            boolean b = this.interaction.isChangeRankingMethodLeverPowered();

            this.usingDiversifyingRankingMethod = b;

            if(b) {
                this.sendMessageToAllPlayers("Diversifying ranking method enabled!");
                this.amountOfFlowersToWither = AMOUNT_OF_FLOWERS_TO_WITHER_DIVERSIFYING_MODE;
            } else {
                this.sendMessageToAllPlayers("Diversifying ranking method disabled!");
                this.amountOfFlowersToWither = AMOUNT_OF_FLOWERS_TO_WITHER_DEFAULT_MODE;
            }
        }
    }

    private void clearItems() {
        List<? extends ItemEntity> entities = this.world.getEntitiesByType(TypeFilter.instanceOf(ItemEntity.class),
                itemEntity -> true);

        for(ItemEntity ie : entities) {
            ie.kill(this.world);
        }

        if(!entities.isEmpty()) {
            for(ServerPlayerEntity spe : this.world.getPlayers()) {
                this.interaction.giveInteractablesToPlayer(spe);
            }
        }
    }

    public void onPlayerTargetNewEntity(ServerPlayerEntity player, boolean exists, int id) {
        this.interaction.handleTarget(this.state, player, exists, id);
    }

    public void onPlayerPollinate(ServerPlayerEntity player, boolean exists, int id) {
        if(!exists) {
            return;
        }

        Flower f = this.flowerManager.getFlowerByEntityId(id);

        if(f == null) {
            return;
        }

        Entity e = this.flowerManager.getFlowerEntity(f);

        if(e == null) {
            return;
        }

        if(!this.state.isLeaf()) {
            for(ServerPlayerEntity entity : this.world.getPlayers()) {
                entity.sendMessage(Text.of("You are not in the newest garden!"));
            }

            return;
        }

        this.pollinationController.onPollinationStart(f, e.getPos());
    }

    public void onPlayerRequestTimeTravel(ServerPlayerEntity player, int n, int operation) {
        if(operation == Operations.ADD) {
            if(n == 1) {
                if(this.state.hasChild()) {
                    this.state = this.state.getChild();
                    this.flowerManager.destroyAll();
                    this.flowerManager.placeFlowerEntities(this.state);
                    this.beeNestController.setBeeNestPosition(this.state.getBeeNestMinecraftPosition());
                    this.gardenInformationBossBar.updateBossBar();
                }
            }

            if(n == -1) {
                if(!this.state.isRoot()) {
                    this.state = this.state.getParent();
                    this.flowerManager.destroyAll();
                    this.flowerManager.placeFlowerEntities(this.state);
                    this.beeNestController.setBeeNestPosition(this.state.getBeeNestMinecraftPosition());
                    this.gardenInformationBossBar.updateBossBar();
                }
            }
        }
    }

    public void dispose() {
        this.flowerManager.destroyAll();
        this.beeNestController.dispose();
        this.gardenInformationBossBar.dispose();
    }

    private Vec3d calculateNestBase(Vector3i tl, Vector3i br) {
        double x = (tl.x + br.x) / 2.0 + 0.5;
        double z = (tl.z + br.z) / 2.0 + 0.5;
        double y = Math.min(tl.y, br.y) + 3;
        return new Vec3d(x, y, z);
    }

    public BeetrapState getState() {
        return this.state;
    }

    public double getBaseY() {
        return Math.min(this.bottomLeft.y, this.topRight.y);
    }

    public Vec3d getCenterXZBaseY() {
        double x = (this.bottomLeft.x + this.topRight.x) / 2.0 + 0.5;
        double z = (this.bottomLeft.z + this.topRight.z) / 2.0 + 0.5;
        return new Vec3d(x, this.getBaseY(), z);
    }

    public void regenerateState() {
        this.state = this.state.createChild(this.beeNestController.getBeeNestPosition());
    }

    public void onPollinationCircleRadiusChangeRequested() {
        if(this.pollinationController.isActive()) {
            return;
        }

        if(this.pollinationCircleRadius == SMALL_POLLINATION_CIRCLE_RADIUS) {
            this.pollinationCircleRadius = LARGE_POLLINATION_CIRCLE_RADIUS;
            this.world.getPlayers().forEach(playerEntity -> {
                playerEntity.sendMessage(Text.of("Pollination circle radius enlarged!"));
            });
        } else {
            this.pollinationCircleRadius = SMALL_POLLINATION_CIRCLE_RADIUS;
            this.world.getPlayers().forEach(playerEntity -> {
                playerEntity.sendMessage(Text.of("Pollination circle radius shrank!"));
            });
        }
    }

    public double getPollinationCircleRadius() {
        return this.pollinationCircleRadius;
    }

    public boolean isUsingDiversifyingRankingMethod() {
        return this.usingDiversifyingRankingMethod;
    }

    public int getAmountOfFlowersToWither() {
        return this.amountOfFlowersToWither;
    }
}

package com.lumentech.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;

// GeckoLib 4.8.x (MC 1.20.1) keeps everything under core.*; 4.9.x (MC 1.21.1) hoisted it out.
//? if <1.21 {
/*import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
*///?} else {
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
//?}
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * GeckoLib-backed block entity for the Signal Receiver.
 *
 * <p>The dish deploys the moment the block is placed: {@link #deploy()} is called by
 * {@link FabricSignalReceiver} on the server's block-place event, sets the {@code open}
 * flag and syncs it to tracking clients. The animation controller then plays the
 * {@code open} animation once and holds its last frame.</p>
 *
 * <p>Because the flag is driven by the <em>server</em>, players who are already nearby
 * see the dish open, and anyone who loads the world later finds it already open &mdash;
 * the animation is not re-played on every client load.</p>
 *
 * <p>The {@code rotation} animation (dish tracking a target) is intentionally left out
 * for now, per the current scope.</p>
 */
public class SignalReceiverBlockEntity extends BlockEntity implements GeoBlockEntity {
    /** The deploy animation; holds its final frame (matches loop=hold_on_last_frame). */
    public static final RawAnimation OPEN = RawAnimation.begin().thenPlayAndHold("open");

    private static final String TAG_OPENED = "opened";

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /** Whether the dish has deployed. Synced from server to clients. */
    private boolean opened = false;

    public SignalReceiverBlockEntity(BlockPos pos, BlockState state) {
        super(FabricSignalReceiver.SIGNAL_RECEIVER.get(), pos, state);
    }

    /** Server-side: mark the dish as deployed and push the flag to clients. */
    public void deploy() {
        if (this.opened) {
            return;
        }
        this.opened = true;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
        }
    }

    /**
     * Server-side one-shot: deploys the dish when the block entity first exists in the
     * world. {@link #onLoad()} fires both on placement and when a chunk is loaded, and
     * the {@code opened} flag is persisted, so the animation plays exactly once &mdash;
     * right after the block is placed.
     */
    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level != null && !this.level.isClientSide()) {
            this.deploy();
        }
    }

    /** Client-side mirror of {@link #deploy()}, driven by the data packet. */
    public void setOpenedFromServer(boolean opened) {
        this.opened = opened;
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        super.onDataPacket(connection, packet);
        if (this.level != null && this.level.isClientSide() && packet.getTag() != null) {
            this.setOpenedFromServer(packet.getTag().getBoolean(TAG_OPENED));
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "open", 0, state -> {
            if (this.opened) {
                state.setAnimation(OPEN);
            }
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // --- Block entity sync (so clients learn the dish is open) ---

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        // saveWithoutMetadata() takes no argument before 1.21; 1.21+ needs the registry access.
        //? if <1.21 {
        /*return this.saveWithoutMetadata();
        *///?} else {
        return this.saveWithoutMetadata(this.level.registryAccess());
        //?}
    }

    //? if <1.21 {
    /*@Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean(TAG_OPENED, this.opened);
    }

    @Override
    protected void load(CompoundTag tag) {
        super.load(tag);
        this.opened = tag.getBoolean(TAG_OPENED);
    }
    *///?} else {
    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean(TAG_OPENED, this.opened);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.opened = tag.getBoolean(TAG_OPENED);
    }
    //?}
}

package com.lumentech.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// GeckoLib 4.x (MC 1.20.1 / 1.21.1) vs 5.x (MC 26.x) are incompatible APIs, and
// GeckoLib is not wired on 26.x yet, so the animated block entity is compiled out
// there and replaced with a plain shell. The common block leaves ENTITY_FACTORY
// null on 26.x, so this shell is never instantiated - it only keeps the
// FabricSignalReceiver registration compiling.
//? if <1.22 {
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.nbt.CompoundTag;
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
 * <p>The dish deploys the moment the block is placed: {@link #deploy()} runs on the
 * server on the first tick after placement, sets the {@code open} flag and syncs it to
 * tracking clients. The animation controller then plays the {@code open} animation once
 * and holds its last frame.</p>
 *
 * <p>Because the flag is driven by the <em>server</em>, players who are already nearby
 * see the dish open, and anyone who loads the world later finds it already open &mdash;
 * the flag is persisted, so the animation is not re-played on every chunk load.</p>
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

    /**
     * Server tick hook, wired through {@link com.lumentech.block.SignalReceiverBlock#TICKER}.
     *
     * <p>Vanilla 1.20.1 has no {@code BlockEntity#onLoad} ({@code onLoad} is a 1.21+ /
     * NeoForge hook), so the block ticker is the portable "first tick after placement"
     * trigger. It is idempotent: {@link #deploy()} bails out as soon as the flag is set,
     * and the flag survives world saves.</p>
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntity be) {
        if (level.isClientSide()) {
            return;
        }
        if (be instanceof SignalReceiverBlockEntity self) {
            self.deploy();
        }
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

    /** Client-side mirror of {@link #deploy()}, driven by the data packet. */
    public void setOpenedFromServer(boolean opened) {
        this.opened = opened;
    }

    // Deliberately no @Override and no super call: this hook's exact signature differs
    // between 1.20.1 (ClientboundBlockEntityDataPacket) and 26.x (ValueInput), and
    // annotating it makes the 1.20.1 node fail to compile. Without the annotation it
    // still overrides the supertype method wherever that method exists.
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
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

    // getUpdateTag gained a HolderLookup.Provider parameter in 1.20.5, so 1.20.1 overrides
    // the no-arg form while 1.21.1+ receives the registries. Splitting on the signature
    // (not just the body) is required: on 1.21.1 a no-arg getUpdateTag() does not exist,
    // and @Override alone fails the node.
    //? if <1.20.5 {
    /*@Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }
    *///?} else {
    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }
    //?}

    //? if <1.21 {
    /*@Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean(TAG_OPENED, this.opened);
    }

    // 1.20.1 declares BlockEntity#load as public; 1.20.5+ renamed it to loadAdditional.
    @Override
    public void load(CompoundTag tag) {
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
//?} else {
/** 26.x shell: GeckoLib 5.x not ported yet; never instantiated (ENTITY_FACTORY stays null). */
public class SignalReceiverBlockEntity extends BlockEntity {
    public SignalReceiverBlockEntity(BlockPos pos, BlockState state) {
        super(FabricSignalReceiver.SIGNAL_RECEIVER.get(), pos, state);
    }
}
//?}

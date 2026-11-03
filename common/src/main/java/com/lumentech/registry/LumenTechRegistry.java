package com.lumentech.registry;

import com.iamkaf.amber.api.registry.v1.DeferredRegister;
import com.iamkaf.amber.api.registry.v1.RegistrySupplier;
import com.lumentech.LumenTech;
import com.lumentech.block.LumenGeneratorBlock;
import com.lumentech.block.SignalReceiverBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * All LumenTech registrations, expressed once in common code and applied to every
 * loader by Amber's {@link DeferredRegister}.
 *
 * <p>Blocks must be registered before their {@link BlockItem}s, and both before the
 * creative tab that references them &mdash; hence the ordering in {@link #init()}.</p>
 */
public final class LumenTechRegistry {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(LumenTech.MOD_ID, Registries.BLOCK);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(LumenTech.MOD_ID, Registries.ITEM);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(LumenTech.MOD_ID, Registries.CREATIVE_MODE_TAB);

    // --- Blocks ---
    public static final RegistrySupplier<Block> LUMEN_GENERATOR = BLOCKS.register("lumen_generator",
            () -> new LumenGeneratorBlock(BlockBehaviour.Properties.of().strength(3.0F, 6.0F)));
    public static final RegistrySupplier<Block> SIGNAL_RECEIVER = BLOCKS.register("signal_receiver",
            () -> new SignalReceiverBlock(BlockBehaviour.Properties.of()
                    .strength(3.5F, 6.0F)
                    .noOcclusion()));

    // --- Items ---
    public static final RegistrySupplier<Item> LUMEN_INGOT = ITEMS.register("lumen_ingot",
            () -> new Item(new Item.Properties()));
    public static final RegistrySupplier<Item> RAW_LUMEN = ITEMS.register("raw_lumen",
            () -> new Item(new Item.Properties()));
    public static final RegistrySupplier<Item> LUMEN_CORE = ITEMS.register("lumen_core",
            () -> new Item(new Item.Properties()));
    public static final RegistrySupplier<Item> LUMEN_GENERATOR_ITEM = ITEMS.register("lumen_generator",
            () -> new BlockItem(LUMEN_GENERATOR.get(), new Item.Properties()));
    public static final RegistrySupplier<Item> SIGNAL_RECEIVER_ITEM = ITEMS.register("signal_receiver",
            () -> new BlockItem(SIGNAL_RECEIVER.get(), new Item.Properties()));

    // --- Creative tab ---
    public static final RegistrySupplier<CreativeModeTab> TAB = TABS.register("lumentech", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.lumentech"))
            .icon(() -> new ItemStack(LUMEN_INGOT.get()))
            .displayItems((parameters, output) -> {
                output.accept(LUMEN_INGOT.get());
                output.accept(RAW_LUMEN.get());
                output.accept(LUMEN_CORE.get());
                output.accept(LUMEN_GENERATOR_ITEM.get());
                output.accept(SIGNAL_RECEIVER_ITEM.get());
            })
            .build());

    public static void init() {
        BLOCKS.register();
        ITEMS.register();
        TABS.register();
    }
}

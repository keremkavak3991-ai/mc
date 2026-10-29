package dev.quietledger;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Common-side registry: assets are resolved by the client resource manager. */
public final class LedgerSounds {
    public static final DeferredRegister<SoundEvent> EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, QuietLedger.ID);
    public static final DeferredHolder<SoundEvent, SoundEvent> DISTANT_CREAK = register("distant_creak");
    public static final DeferredHolder<SoundEvent, SoundEvent> HOLLOW_DRONE = register("hollow_drone");
    public static final DeferredHolder<SoundEvent, SoundEvent> DEEP_KNOCKS = register("deep_knocks");
    public static final DeferredHolder<SoundEvent, SoundEvent> NEAR_BREATH = register("near_breath");
    public static final DeferredHolder<SoundEvent, SoundEvent> UNSEEN_STEPS = register("unseen_steps");
    private LedgerSounds() {}
    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(QuietLedger.ID, name)));
    }
}

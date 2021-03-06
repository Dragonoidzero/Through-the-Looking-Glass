package azzy.fabric.lookingglass.item;

import io.github.ladysnake.pal.VanillaAbilities;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import static azzy.fabric.lookingglass.LookingGlassConstants.*;

// This Ring will have an expensive recipe.  This is really not a ring.  It's an item that on use, rewards the user with creative flight - forever.
// On use again, it will remove creative flight.  So you get flight without having to carry around the item.
// The recipe cost will reflect the same.
// For a simpler item that gives creative flight on equip, check out the SimpleAngelRing class.
public class AdvancedAngelRingItem extends Item {
    public AdvancedAngelRingItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            // Check if the item grants flight.
            if (ADVANCED_ANGEL_RING_ABILITY_SOURCE.grants(user, VanillaAbilities.ALLOW_FLYING)) {
                // If the item is giving flight, remove it.
                ADVANCED_ANGEL_RING_ABILITY_SOURCE.revokeFrom(user, VanillaAbilities.ALLOW_FLYING);
                user.clearStatusEffects();
                // Stupid network handler will be null if the client is just starting up.
                if (((ServerPlayerEntity) user).networkHandler != null)
                    user.sendMessage(new TranslatableText(ITEM_LOOKINGGLASS_ANGEL_RING_FLIGHT_DISABLED), true);
            } else {
                // Otherwise, grant it.
                ADVANCED_ANGEL_RING_ABILITY_SOURCE.grantTo(user, VanillaAbilities.ALLOW_FLYING);
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.HEALTH_BOOST, Integer.MAX_VALUE, 5, false, false));
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false));
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, Integer.MAX_VALUE, 5, false, false));
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, Integer.MAX_VALUE, 5, false, false));
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 1, false, false));
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, Integer.MAX_VALUE, 1, false, false));
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, Integer.MAX_VALUE, 3, false, false));
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, Integer.MAX_VALUE, 3, false, false));
                // Stupid network handler will be null if the client is just starting up.
                if (((ServerPlayerEntity) user).networkHandler != null)
                    user.sendMessage(new TranslatableText(ITEM_LOOKINGGLASS_ANGEL_RING_FLIGHT_ENABLED), true);
            }
        }

        return TypedActionResult.success(user.getStackInHand(hand));
    }
}

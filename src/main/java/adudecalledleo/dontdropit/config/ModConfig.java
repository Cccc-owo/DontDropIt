package adudecalledleo.dontdropit.config;

import java.util.*;
import java.util.stream.Collectors;

import adudecalledleo.dontdropit.DontDropIt;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.annotation.Nullable;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.tag.api.TagRegistry;

import static adudecalledleo.dontdropit.config.ModConfigLogger.LOGGER;

@Config(name = DontDropIt.MOD_ID)
public class ModConfig implements ConfigData {
    public static ModConfig get() {
        return AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    public static void save() {
        AutoConfig.getConfigHolder(ModConfig.class).save();
    }

    public static class General {
        @ConfigEntry.Gui.Tooltip(count = 2)
        public DropBehaviorOverride oobDropClickOverride = DropBehaviorOverride.FAVORITE_ITEMS;
        @ConfigEntry.Gui.Tooltip(count = 2)
        public DropBehaviorOverride cursorCloseDropOverride = DropBehaviorOverride.ALL_ITEMS;
    }

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.TransitiveObject
    public General general = new General();

    public static class DropDelay {
        @ConfigEntry.Gui.Tooltip(count = 3)
        @Nullable
        public DelayActivationMode mode = DelayActivationMode.ENABLED;
        @ConfigEntry.BoundedDiscrete(max = 200, min = 1)
        @ConfigEntry.Gui.Tooltip(count = 3)
        public long ticks = 10;
        @ConfigEntry.Gui.Tooltip(count = 3)
        public boolean doDelayOnce = false;
    }

    @ConfigEntry.Category("drop_delay")
    @ConfigEntry.Gui.TransitiveObject
    public DropDelay dropDelay = new DropDelay();

    public static class Favorites {
        @ConfigEntry.Gui.Tooltip(count = 2)
        public boolean enabled = true;
        @ConfigEntry.Gui.Tooltip(count = 2)
        public boolean disableShiftClick = false;
        @ConfigEntry.Gui.Tooltip(count = 2)
        public boolean drawOverlay = true;
        @ConfigEntry.Gui.Tooltip(count = 3)
        public Boolean restoreDefaults = null;
        @ConfigEntry.Gui.PrefixText
        @ConfigEntry.Gui.Tooltip
        public List<String> items = null;
        @ConfigEntry.Gui.PrefixText
        @ConfigEntry.Gui.Tooltip
        public List<String> enchantments = null;
        @ConfigEntry.Gui.Tooltip(count = 3)
        public boolean enchIgnoreInvalidTargets = true;
        @ConfigEntry.Gui.PrefixText
        @ConfigEntry.Gui.Tooltip(count = 2)
        public List<String> tags = null;

        private static List<String> getRareItemIds() {
            ArrayList<String> itemIds = new ArrayList<>();
            for (Identifier id : Registry.ITEM.getIds()) {
                Item item = Registry.ITEM.get(id);
                if (item.getRarity(new ItemStack(item)) != Rarity.COMMON)
                    itemIds.add(id.toString());
            }
            return itemIds;
        }

        private static List<String> getEnchantmentIds() {
            ArrayList<String> enchIds = new ArrayList<>();
            for (Identifier id : Registry.ENCHANTMENT.getIds()) {
                Enchantment enchantment = Registry.ENCHANTMENT.get(id);
                if (enchantment == null || enchantment.isCursed())
                    continue;
                enchIds.add(id.toString());
            }
            return enchIds;
        }

        void postLoad() {
            items = init(items);
            enchantments = init(enchantments);
            tags = init(tags);

            boolean removeInvalidIds = true;
            if (restoreDefaults == null || restoreDefaults == Boolean.TRUE) {
                // resets favored ID lists IF:
                // 1. restoreDefaults was set to true (user wants to reset)
                // 2. restoreDefaults is null and all lists are currently empty (newly created config)
                if (restoreDefaults == Boolean.TRUE || (items.isEmpty() && enchantments.isEmpty() && tags.isEmpty())) {
                    items = getRareItemIds();
                    enchantments = getEnchantmentIds();
                    tags.clear();
                    removeInvalidIds = false; // can safely be skipped, since default items/enchantment IDs are always valid
                }
            }
            restoreDefaults = Boolean.FALSE;
            if (removeInvalidIds) {
                removeInvalidIdsFrom(items, "items", Registry.ITEM);
                removeInvalidIdsFrom(enchantments, "enchantments", Registry.ENCHANTMENT);
                removeInvalidTagIdsFrom(tags);
            }
        }

        private List<String> init(List<String> list) {
            if (list == null)
                return new ArrayList<>();
            else
                return new ArrayList<>(new LinkedHashSet<>(list)); // deduplicate
        }

        private void removeInvalidIdsFrom(List<String> list, String listName, Registry<?> registry) {
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                String idStr = it.next();
                Identifier id = Identifier.tryParse(idStr);
                if (id == null) {
                    LOGGER.warn("Favorites: Found invalid identifier \"{}\" in favored {} list, removing", idStr, listName);
                    it.remove();
                    continue;
                }
                if (!registry.containsId(id)) {
                    LOGGER.warn("Favorites: Found unregistered identifier \"{}\" in favored {} list, removing", id.toString(), listName);
                    it.remove();
                }
            }
        }

        private void removeInvalidTagIdsFrom(List<String> list) {
            // collect all valid item tag IDs
            Set<Identifier> tagIds = TagRegistry.stream(Registry.ITEM_KEY)
                    .map(entry -> entry.key().id())
                    .collect(Collectors.toSet());

            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                String idStr = it.next();
                Identifier id = Identifier.tryParse(idStr);
                if (id == null) {
                    LOGGER.warn("Favorites: Found invalid identifier \"{}\" in favored tags list, removing", idStr);
                    it.remove();
                    continue;
                }
                if (!tagIds.contains(id)) {
                    LOGGER.warn("Favorites: Found unregistered identifier \"{}\" in favored tags list, removing", id.toString());
                    it.remove();
                }
            }
        }
    }

    @ConfigEntry.Category("favorites")
    @ConfigEntry.Gui.TransitiveObject
    public Favorites favorites = new Favorites();

    @Override
    public void validatePostLoad() {
        if (general == null) {
            LOGGER.warn("General section is missing, resetting it to default values");
            general = new General();
        }
        if (dropDelay == null) {
            LOGGER.warn("Drop Delay section is missing, resetting it to default values");
            dropDelay = new DropDelay();
        }
        if (favorites == null) {
            LOGGER.warn("Favorites section is missing, resetting it to default values");
            favorites = new Favorites();
        }
        postUpdate();
    }

    public void postUpdate() {
        favorites.postLoad();
        FavoredChecker.updateFavoredSets(this);
    }
}

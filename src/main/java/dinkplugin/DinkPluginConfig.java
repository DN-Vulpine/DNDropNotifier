package dinkplugin;

import dinkplugin.domain.AccountType;
import dinkplugin.domain.ChatPrivacyMode;
import dinkplugin.domain.CombatAchievementTier;
import dinkplugin.domain.ConfigImportPolicy;
import dinkplugin.domain.FilterMode;
import dinkplugin.domain.PlayerLookupService;
import dinkplugin.domain.SeasonalPolicy;
import dinkplugin.util.Utils;
import net.runelite.api.Experience;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

import java.awt.Color;
import java.util.EnumSet;
import java.util.Set;

@ConfigGroup(SettingsManager.CONFIG_GROUP)
public interface DinkPluginConfig extends Config {

    @ConfigSection(
        name = "Webhook Overrides",
        description = "Allows webhook data to be sent to a different URL, for the various notifiers",
        position = -20,
        closedByDefault = true
    )
    String webhookSection = "Webhook Overrides";

    @ConfigSection(
        name = "Pet",
        description = "Settings for notifying when obtaining a pet",
        position = 10,
        closedByDefault = true
    )
    String petSection = "Pet";

    @ConfigSection(
        name = "Levels",
        description = "Settings for notifying when levelling a skill",
        position = 20,
        closedByDefault = true
    )
    String levelSection = "Levels";

    @ConfigSection(
        name = "Loot",
        description = "Settings for notifying when loot is dropped",
        position = 30,
        closedByDefault = true
    )
    String lootSection = "Loot";

    @ConfigSection(
        name = "Combat Tasks",
        description = "Settings for notifying when you complete a combat achievement",
        position = 100,
        closedByDefault = true
    )
    String combatTaskSection = "Combat Tasks";


    /*
    @ConfigSection(
        name = "Group Storage",
        description = "Settings for notifying when you deposit or withdraw items from group ironman shared storage",
        position = 140,
        closedByDefault = true
    )
    String groupStorageSection = "Group Storage";
    /*

    /*
    @ConfigSection(
        name = "Player Trades",
        description = "Settings for notifying when you trade with another player",
        position = 160,
        closedByDefault = true
    )
    String tradeSection = "Player Trades";
    */


    @ConfigSection(
        name = "Advanced",
        description = "Do not modify without fully understanding these settings",
        position = 1000,
        closedByDefault = true
    )
    String advancedSection = "Advanced";

    @ConfigItem(
        keyName = VersionManager.VERSION_CONFIG_KEY,
        name = "Plugin Version",
        description = "The latest dink version used by the player that has a notable changelog entry",
        hidden = true
    )
    default String pluginVersion() {
        return "";
    }

    @ConfigItem(
        keyName = "maxRetries",
        name = "Webhook Max Retries",
        description = "The maximum number of retry attempts for sending a webhook message. Negative implies no attempts",
        position = 1000,
        section = advancedSection,
        hidden = true
    )
    default int maxRetries() {
        return 3;
    }

    @ConfigItem(
        keyName = "baseRetryDelay",
        name = "Webhook Retry Base Delay",
        description = "The base number of milliseconds to wait before attempting a retry for a webhook message",
        position = 1001,
        section = advancedSection,
        hidden = true
    )
    @Units(Units.MILLISECONDS)
    default int baseRetryDelay() {
        return 2000;
    }

    @ConfigItem(
        keyName = "imageWriteTimeout",
        name = "Image Upload Timeout",
        description = "The maximum number of seconds that uploading a screenshot can take before timing out",
        position = 1002,
        section = advancedSection,
        hidden = true
    )
    @Units(Units.SECONDS)
    default int imageWriteTimeout() {
        return 30; // elevated from okhttp default of 10
    }

    @ConfigItem(
        keyName = "screenshotScale",
        name = "Screenshot Scale",
        description = "Resizes screenshots in each dimension by the specified percentage.<br/>" +
            "Useful to avoid Discord's max upload size of 8MB or reduce bandwidth",
        position = 1003,
        section = advancedSection,
        hidden = true
    )
    @Units(Units.PERCENT)
    @Range(min = 1, max = 100)
    default int screenshotScale() {
        return 100;
    }

    @ConfigItem(
        keyName = "discordRichEmbeds",
        name = "Use Rich Embeds",
        description = "Whether Discord's rich embed format should be used for webhooks",
        position = 1004,
        section = advancedSection,
        hidden = true
    )
    default boolean discordRichEmbeds() {
        return true;
    }

    @ConfigItem(
        keyName = "embedFooterText",
        name = "Embed Footer Text",
        description = "The text in the footer of rich embed webhook messages. If empty, no footer will be sent",
        position = 1005,
        section = advancedSection,
        hidden = true
    )
    default String embedFooterText() {
        return "Powered by DN";
    }

    @ConfigItem(
        keyName = "embedFooterIcon",
        name = "Embed Footer Icon",
        description = "The URL for the footer icon image of rich embed webhooks. Requires footer text to not be empty",
        position = 1006,
        section = advancedSection,
        hidden = true
    )
    default String embedFooterIcon() {
        return "https://github.com/pajlads/DinkPlugin/raw/master/icon.png";
    }

    @ConfigItem(
        keyName = "ignoredNames", // historical name, preserved for backwards compatibility
        name = "Filtered RSNs",
        description = "Restrict what player names can trigger notifications (One name per line)<br/>" +
            "This acts as an allowlist or denylist based on the 'RSN Filter Mode' setting below.",
        position = 1007,
        section = advancedSection,
        hidden = true
    )
    default String filteredNames() {
        return "";
    }

    @ConfigItem(
        keyName = "nameFilterMode",
        name = "RSN Filter Mode",
        description = "Allow Mode: Only allow notifications for RSNs on the list above (discouraged).<br/>" +
            "Deny Mode: Prevent notifications from RSNs on the list above (default/recommended).",
        position = 1008,
        section = advancedSection,
        hidden = true
    )
    default FilterMode nameFilterMode() {
        return FilterMode.DENY;
    }

    @ConfigItem(
        keyName = "nameFilterMode",
        name = "",
        description = ""
    )
    void setNameFilterMode(FilterMode filterMode);

    @ConfigItem(
        keyName = "playerLookupService",
        name = "Player Lookup Service",
        description = "The service used to lookup a players account, to make their name clickable in Discord embeds",
        position = 1009,
        section = advancedSection,
        hidden = true
    )
    default PlayerLookupService playerLookupService() {
        return PlayerLookupService.OSRS_HISCORE;
    }

    @ConfigItem(
        keyName = "chatPrivacy",
        name = "Hide Chat in Images",
        description = "Whether to hide the chat box and private messages when capturing screenshots.<br/>" +
            "Note: visually you may notice the chat box momentarily flicker as it is hidden for the screenshot.<br/>" +
            "Warning: 'Hide Split PMs' has no effect if 'Split friends private chat' is not enabled in the game settings",
        position = 1010,
        section = advancedSection,
        hidden = true
    )
    default ChatPrivacyMode chatPrivacy() {
        return ChatPrivacyMode.HIDE_SPLIT_PM;
    }

    @ConfigItem(
        keyName = "chatPrivacy",
        name = "",
        description = ""
    )
    void setChatPrivacy(ChatPrivacyMode mode);

    @ConfigItem(
        keyName = "sendDiscordUser",
        name = "Send Discord Profile",
        description = "Whether to send your discord user information to the webhook server via metadata",
        position = 1011,
        section = advancedSection,
        hidden = true
    )
    default boolean sendDiscordUser() {
        return false;
    }

    @ConfigItem(
        keyName = "sendClanName",
        name = "Send Clan Name",
        description = "Whether to send your clan information to the webhook server via metadata",
        position = 1012,
        section = advancedSection,
        hidden = true
    )
    default boolean sendClanName() {
        return false;
    }

    @ConfigItem(
        keyName = "sendGroupIronClanName",
        name = "Send GIM Clan Name",
        description = "Whether to send your group ironman clan information to the webhook server via metadata",
        position = 1013,
        section = advancedSection,
        hidden = true
    )
    default boolean sendGroupIronClanName() {
        return false;
    }

    @ConfigItem(
        keyName = "threadNameTemplate",
        name = "Forum Thread Name",
        description = "Thread name template to use for Discord Forum Channels<br/>" +
            "Use %TYPE% to insert the notification type<br/>" +
            "Use %MESSAGE% to insert the notification message<br/>" +
            "Use %USERNAME% to insert the player name",
        position = 1013,
        section = advancedSection,
        hidden = true
    )
    default String threadNameTemplate() {
        return "[%TYPE%] %MESSAGE%";
    }

    @ConfigItem(
        keyName = "screenshotFilenameTemplate",
        name = "Screenshot Filename Template",
        description = "Format of the screenshot filename. Discord may use this as the text in mobile notifications. " +
            "Leave empty for default filenames.<br/>" +
            "Use %USERNAME% to insert your username<br/>" +
            "Use %TYPE% to insert the notification type<br/>" +
            "Use %CLAN% to insert your clan name",
        position = 1013,
        section = advancedSection,
        hidden = true
    )
    default String screenshotFilenameTemplate() {
        return "";
    }

    @ConfigItem(
        keyName = "metadataWebhook",
        name = "Custom Metadata Handler",
        description = "Webhook URL for custom handlers to receive regular information about the player.<br/>" +
            "Not recommended for use with Discord webhooks, as it could cause spam.<br/>" +
            "You can target multiple webhooks by specifying their URLs on separate lines",
        position = 1014,
        section = advancedSection,
        hidden = true
    )
    default String metadataWebhook() {
        return "";
    }

    @ConfigItem(
        keyName = "seasonalPolicy",
        name = "Seasonal Policy",
        description = "Whether to send notifications that occur on seasonal worlds like Leagues.<br/>" +
            "If 'Use Leagues URL' is enabled but no Leagues Override URL is set, notifications will still be sent to your normal webhook URLs.<br/>" +
            "Note: the Leagues-specific notifier uses an independent config option to toggle messages",
        position = 1015,
        section = advancedSection,
        hidden = true
    )
    default SeasonalPolicy seasonalPolicy() {
        return SeasonalPolicy.FORWARD_TO_LEAGUES;
    }

    @ConfigItem(
        keyName = "seasonalPolicy",
        name = "",
        description = ""
    )
    void setSeasonalPolicy(SeasonalPolicy policy);

    @ConfigItem(
        keyName = "includeLocation",
        name = "Include Location",
        description = "Whether to include the player location and world in notification metadata.",
        position = 1016,
        section = advancedSection,
        hidden = true
    )
    default boolean includeLocation() {
        return true;
    }

    @ConfigItem(
        keyName = SettingsManager.DYNAMIC_IMPORT_CONFIG_KEY,
        name = "Dynamic Config URL",
        description = "Synchronizes your Dink configuration with the specified URL.<br/>" +
            "Whenever Dink starts, it imports the config offered by the URL.<br/>" +
            "The config applies to all webhooks, so ensure you trust this URL.<br/>" +
            "Only one URL is supported",
        position = 1017,
        section = advancedSection,
        hidden = true
    )
    default String dynamicConfigUrl() {
        return "";
    }

    @ConfigItem(
        keyName = "importPolicy",
        name = "Import Policy",
        description = "Whether certain settings should be overwritten on import, rather than merging.<br/>" +
            "Relevant for both ::DinkImport and 'Dynamic Config URL'",
        position = 1018,
        section = advancedSection,
        hidden = true
    )
    default Set<ConfigImportPolicy> importPolicy() {
        return EnumSet.noneOf(ConfigImportPolicy.class);
    }

    @ConfigItem(
        keyName = "includeClientFrame",
        name = "Include Client Frame",
        description = "Whether to include the client frame in screenshots.",
        position = 1019,
        section = advancedSection,
        hidden = true
    )
    default boolean includeClientFrame() {
        return false;
    }

    @ConfigItem(
        keyName = "embedColor",
        name = "Embed Color",
        description = "The highlight color for rich embeds.",
        position = 1020,
        section = advancedSection,
        hidden = true
    )
    default Color embedColor() {
        return Utils.PINK;
    }

    @ConfigItem(
        keyName = "deniedAccountTypes",
        name = "Denied Account Types",
        description = "Types of accounts that should not trigger notifications.<br/>" +
            "Has no effect if 'RSN Filter Mode' is set to 'Exclusively Allow'",
        position = 1021,
        section = advancedSection,
        hidden = true
    )
    default Set<AccountType> deniedAccountTypes() {
        return EnumSet.noneOf(AccountType.class);
    }

    @ConfigItem(
        keyName = "useSlayerWidgetKc",
        name = "Use Slayer Log KC",
        description = "Whether to parse and utilize kill counts from the slayer kill log interface.<br/>" +
            "For more accurate kc reporting, have this setting enabled and open the slayer kill log",
        position = 1022,
        section = advancedSection,
        hidden = true
    )
    default boolean useSlayerWidgetKc() {
        return true;
    }

    @ConfigItem(
        keyName = "customPlayerBadge",
        name = "Custom Badge URL",
        description = "Custom image URL to display next to player name in Discord rich embed headers.<br/>" +
            "Leave blank to utilize the default chat badge logic.<br/>" +
            "Warning: malformed URLs may break Discord notifications",
        position = 1023,
        section = advancedSection,
        hidden = true
    )
    default String customPlayerBadge() {
        return "";
    }

    @ConfigItem(
        keyName = "discordWebhook", // do not rename; would break old configs
        name = "Primary Webhook URLs",
        description = "The default webhook URL to send notifications to, if no override is specified.<br/>" +
            "You can target multiple webhooks by specifying their URLs on separate lines",
        position = -20
    )
    default String primaryWebhook() {
        return "";
    }

    @ConfigItem(
        keyName = "petWebhook",
        name = "Pet Webhook Override",
        description = "If non-empty, pet messages are sent to this URL, instead of the primary URL",
        position = -18,
        section = webhookSection,
        hidden = true
    )
    default String petWebhook() {
        return "";
    }

    @ConfigItem(
        keyName = "levelWebhook",
        name = "Level Webhook Override",
        description = "If non-empty, level up messages are sent to this URL, instead of the primary URL",
        position = -17,
        section = webhookSection
    )
    default String levelWebhook() {
        return "";
    }

    @ConfigItem(
        keyName = "lootWebhook",
        name = "Loot Webhook Override",
        description = "If non-empty, loot messages are sent to this URL, instead of the primary URL",
        position = -16,
        section = webhookSection,
        hidden = true
    )
    default String lootWebhook() {
        return "";
    }

    @ConfigItem(
        keyName = "combatTaskWebhook",
        name = "Combat Task Webhook Override",
        description = "If non-empty, combat task messages are sent to this URL, instead of the primary URL",
        position = -9,
        section = webhookSection
    )
    default String combatTaskWebhook() {
        return "";
    }

    /*@ConfigItem(
        keyName = "groupStorageWebhook",
        name = "Group Storage Webhook Override",
        description = "If non-empty, Group Storage messages are sent to this URL, instead of the primary URL",
        position = -5,
        section = webhookSection
    )
    default String groupStorageWebhook() {
        return "";
    }*/

    /*@ConfigItem(
        keyName = "tradeWebhook",
        name = "Trade Webhook Override",
        description = "If non-empty, Trading messages are sent to this URL, instead of the primary URL",
        position = -3,
        section = webhookSection
    )
    default String tradeWebhook() {
        return "";
    }*/

    @ConfigItem(
        keyName = "petEnabled",
        name = "Enable pets",
        description = "Enable notifications for obtaining pets",
        position = 10,
        section = petSection
    )
    default boolean notifyPet() {
        return true;
    }

    @ConfigItem(
        keyName = "petSendImage",
        name = "Send Image",
        description = "Send image with the notification",
        position = 11,
        section = petSection
    )
    default boolean petSendImage() {
        return true;
    }

    @ConfigItem(
        keyName = "petNotifMessage",
        name = "Notification Message",
        description = "The message to be sent through the webhook.<br/>" +
            "Use %USERNAME% to insert your username<br/>" +
            "Use %GAME_MESSAGE% to insert the game message associated with this type of pet drop",
        position = 12,
        section = petSection,
        hidden = true
    )
    default String petNotifyMessage() {
        return "%USERNAME% %GAME_MESSAGE%";
    }

    @ConfigItem(
        keyName = "levelEnabled",
        name = "Enable level",
        description = "Enable notifications for gaining levels",
        position = 20,
        section = levelSection
    )
    default boolean notifyLevel() {
        return true;
    }

    @ConfigItem(
        keyName = "levelSendImage",
        name = "Send Image",
        description = "Send image with the notification",
        position = 21,
        section = levelSection
    )
    default boolean levelSendImage() {
        return true;
    }

    @ConfigItem(
        keyName = "levelNotifyVirtual",
        name = "Notify on Virtual Levels",
        description = "Whether level notifications should be fired beyond level 99.<br/>" +
            "Will also notify upon reaching 200M XP",
        position = 22,
        section = levelSection,
        hidden = true
    )
    default boolean levelNotifyVirtual() {
        return false;
    }

    @ConfigItem(
        keyName = "levelNotifyCombat",
        name = "Notify for Combat Levels",
        description = "Whether notifications should occur for combat level increases",
        position = 23,
        section = levelSection,
        hidden = true
    )
    default boolean levelNotifyCombat() {
        return false;
    }

    @ConfigItem(
        keyName = "levelInterval",
        name = "Notify Interval",
        description = "Level interval between when a notification should be sent",
        position = 24,
        section = levelSection,
        hidden = true
    )
    default int levelInterval() {
        return 1;
    }

    @ConfigItem(
        keyName = "levelMinValue",
        name = "Minimum Skill Level",
        description = "The minimum skill level required to send a notification.<br/>" +
            "Useful for filtering out low-level notifications",
        position = 25,
        section = levelSection,
        hidden = true
    )
    default int levelMinValue() {
        return 99;
    }

    @ConfigItem(
        keyName = "levelMinScreenshotValue",
        name = "Min Screenshot Level",
        description = "Minimum level of the advanced skill to warrant a screenshot.<br/>" +
            "Requires 'Send Image' to be enabled",
        position = 26,
        section = levelSection,
        hidden = true
    )
    @Range(min = 1, max = 99)
    default int levelMinScreenshotValue() {
        return 99;
    }

    @ConfigItem(
        keyName = "levelIntervalOverride",
        name = "Interval Override Level",
        description = "All level ups starting from this override level send a notification, disregarding the configured Notify Interval.<br/>" +
            "Disabled when set to 0",
        position = 27,
        section = levelSection,
        hidden = true
    )
    default int levelIntervalOverride() {
        return 0;
    }

    @ConfigItem(
        keyName = "xpInterval",
        name = "Post-99 XP Interval",
        description = "XP interval at which to fire notifications (in millions).<br/>" +
            "Does not apply to skills that are below level 99.<br/>" +
            "Does <i>not</i> depend on the 'Notify on Virtual Levels' setting.<br/>" +
            "If enabled, fires for 200M XP, even if not divisible by the interval.<br/>" +
            "Disabled if set to 0",
        position = 27,
        section = levelSection,
        hidden = true
    )
    @Units("M xp")
    @Range(max = Experience.MAX_SKILL_XP / 1_000_000) // [0, 200]
    default int xpInterval() {
        return 0;
    }

    @ConfigItem(
        keyName = "levelNotifMessage",
        name = "Notification Message",
        description = "The message to be sent through the webhook.<br/>" +
            "Use %USERNAME% to insert your username<br/>" +
            "Use %SKILL% to insert the levelled skill(s)<br/>" +
            "Use %TOTAL_LEVEL% to insert the updated total level<br/>" +
            "Use %TOTAL_XP% to insert the updated overall experience",
        position = 29,
        section = levelSection,
        hidden = true
    )
    default String levelNotifyMessage() {
        return "%USERNAME% has achieved 99 in %SKILL%";
    }

    @ConfigItem(
        keyName = "lootEnabled",
        name = "Enable loot",
        description = "Enable notifications for gaining loot",
        position = 30,
        section = lootSection
    )
    default boolean notifyLoot() {
        return true;
    }

    @ConfigItem(
        keyName = "lootSendImage",
        name = "Send Image",
        description = "Send image with the notification",
        position = 31,
        section = lootSection
    )
    default boolean lootSendImage() {
        return true;
    }

    @ConfigItem(
        keyName = "lootIcons",
        name = "Show loot icons",
        description = "Show icons for the loot obtained as additional embeds",
        position = 32,
        section = lootSection,
        hidden = true
    )
    default boolean lootIcons() {
        return true;
    }

    @ConfigItem(
        keyName = "minLootValue",
        name = "Min Loot value",
        description = "The minimum value of an item for a notification to be sent.<br/>" +
            "For PK chests, the <i>total</i> value of the items is compared with this threshold",
        position = 33,
        section = lootSection,
        hidden = true
    )
    default int minLootValue() {
        return 1000000;
    }

    @ConfigItem(
        keyName = "lootImageMinValue",
        name = "Screenshot Min Value",
        description = "The minimum combined loot value to send a screenshot.<br/>" +
            "Must have 'Send Image' enabled",
        position = 34,
        section = lootSection,
        hidden = true
    )
    default int lootImageMinValue() {
        return 1000000;
    }

    @ConfigItem(
        keyName = "lootIncludePlayer",
        name = "Include PK Loot",
        description = "Allow notifications for loot from player kills",
        position = 35,
        section = lootSection
    )
    default boolean includePlayerLoot() {
        return true;
    }

    @ConfigItem(
        keyName = "lootRedirectPlayerKill",
        name = "Send PK Loot to PK URL",
        description = "Whether to send PK loot to the PK override webhook URL, rather than the loot URL.<br/>" +
            "Must have 'Include PK Loot' (above) enabled.<br/>" +
            "Has no effect if the Player Kills notifier override URL is absent",
        position = 35,
        section = lootSection,
        hidden = true
    )
    default boolean lootRedirectPlayerKill() {
        return true;
    }

    @ConfigItem(
        keyName = "lootIncludeClueScrolls",
        name = "Include Clue Loot",
        description = "Allow notifications for loot from Clue Scrolls",
        position = 36,
        section = lootSection
    )
    default boolean lootIncludeClueScrolls() {
        return true;
    }

    @ConfigItem(
        keyName = "lootIncludeGambles",
        name = "Include BA Gambles",
        description = "Allow notifications for barbarian assault high gambles",
        position = 36,
        section = lootSection,
        hidden = true
    )
    default boolean lootIncludeGambles() {
        return false;
    }

    @ConfigItem(
        keyName = "lootItemAllowlist",
        name = "Item Allowlist",
        description = "Always fire notifications for these items, despite value settings.<br/>" +
            "Place one item name per line (case-insensitive; asterisks are wildcards)",
        position = 37,
        section = lootSection,
        hidden = true
    )
    default String lootItemAllowlist() {
        return "";
    }

    @ConfigItem(
        keyName = "lootItemDenylist",
        name = "Item Denylist",
        description = "Never fire notifications for these items, despite value or rarity settings.<br/>" +
            "Place one item name per line (case-insensitive; asterisks are wildcards)",
        position = 37,
        section = lootSection,
        hidden = true
    )
    default String lootItemDenylist() {
        return "";
    }

    @ConfigItem(
        keyName = "lootSourceDenylist",
        name = "Source Denylist",
        description = "Never fire notifications for these loot sources, despite value or rarity settings.<br/>" +
            "Place one NPC/source name per line (case-insensitive).<br/>" +
            "Does <i>not</i> apply to player names for PK loot",
        position = 37,
        section = lootSection,
        hidden = true
    )
    default String lootSourceDenylist() {
        return "Einar\n";
    }

    @ConfigItem(
        keyName = "lootRarityThreshold",
        name = "Rarity Override (1 in X)",
        description = "Fires notifications for sufficiently rare drops, despite the 'Min Loot value' threshold.<br/>" +
            "Corresponds to a 1 in X chance. For example, 100 notifies for items with 1% drop rate or rarer.<br/>" +
            "Has no effect when set to zero.<br/>" +
            "Currently only applies to NPC drops",
        position = 38,
        section = lootSection,
        hidden = true
    )
    default int lootRarityThreshold() {
        return 0;
    }

    @ConfigItem(
        keyName = "lootRarityValueIntersection",
        name = "Require both Rarity and Value",
        description = "Whether items must exceed <i>both</i> the Min Value AND Rarity thresholds to be notified.<br/>" +
            "Does not apply to drops where Dink lacks rarity data.<br/>" +
            "Currently only impacts NPC drops",
        position = 39,
        section = lootSection,
        hidden = true
    )
    default boolean lootRarityValueIntersection() {
        return false;
    }

    @ConfigItem(
        keyName = "lootNotifMessage",
        name = "Notification Message",
        description = "The message to be sent through the webhook.<br/>" +
            "Use %USERNAME% to insert your username<br/>" +
            "Use %LOOT% to insert the loot<br/>" +
            "Use %SOURCE% to show the source of the loot<br/>" +
            "Use %COUNT% to insert the associated kill count (unnecessary if rich embeds are enabled)",
        position = 40,
        section = lootSection,
        hidden = true
    )
    default String lootNotifyMessage() {
        return "%USERNAME% has looted: \n\n%LOOT%\nFrom: %SOURCE%";
    }

    @ConfigItem(
        keyName = "combatTaskEnabled",
        name = "Enable Combat Tasks",
        description = "Enable notifications for combat achievements",
        position = 100,
        section = combatTaskSection
    )
    default boolean notifyCombatTask() {
        return false;
    }

    @ConfigItem(
        keyName = "combatTaskSendImage",
        name = "Send Image",
        description = "Send image with the notification",
        position = 101,
        section = combatTaskSection,
        hidden = true
    )
    default boolean combatTaskSendImage() {
        return true;
    }

    @ConfigItem(
        keyName = "combatTaskMinTier",
        name = "Min Tier",
        description = "Minimum combat achievement tier to warrant a notification",
        position = 102,
        section = combatTaskSection,
        hidden = true
    )
    default CombatAchievementTier minCombatAchievementTier() {
        return CombatAchievementTier.ELITE;
    }

    @ConfigItem(
        keyName = "combatTaskMessage",
        name = "Notification Message",
        description = "The message to be sent to the webhook.<br/>" +
            "Use %USERNAME% to insert your username<br/>" +
            "Use %TIER% to insert the task tier<br/>" +
            "Use %TASK% to insert the task name<br/>" +
            "Use %POINTS% to insert the task points<br/>" +
            "Use %TOTAL_POINTS% to insert the total points earned across tasks",
        position = 103,
        section = combatTaskSection,
        hidden = true
    )
    default String combatTaskMessage() {
        return "%USERNAME% has completed %TIER% combat task: %TASK%";
    }

    @ConfigItem(
        keyName = "combatTaskUnlockMessage",
        name = "Reward Unlock Notification Message",
        description = "The message to be sent to the webhook upon unlocking the rewards for a tier completion.<br/>" +
            "Use %USERNAME% to insert your username<br/>" +
            "Use %TIER% to insert the task tier<br/>" +
            "Use %TASK% to insert the task name<br/>" +
            "Use %POINTS% to insert the task points<br/>" +
            "Use %TOTAL_POINTS% to insert the total points earned across tasks<br/>" +
            "Use %COMPLETED% to insert the completed tier",
        position = 104,
        section = combatTaskSection,
        hidden = true
    )
    default String combatTaskUnlockMessage() {
        return "%USERNAME% has unlocked the rewards for the %COMPLETED% tier, by completing the combat task: %TASK%";
    }

    /**
    @ConfigItem(
        keyName = "groupStorageEnabled",
        name = "Enable Transactions",
        description = "Enable notifications upon group storage transactions",
        position = 140,
        section = groupStorageSection
    )
    default boolean notifyGroupStorage() {
        return false;
    }

    @ConfigItem(
        keyName = "groupStorageSendImage",
        name = "Send Image",
        description = "Send image with the notification",
        position = 141,
        section = groupStorageSection
    )
    default boolean groupStorageSendImage() {
        return true;
    }

    @ConfigItem(
        keyName = "groupStorageMinValue",
        name = "Min Value",
        description = "The minimum value of the deposits or withdrawals to send a notification",
        position = 142,
        section = groupStorageSection
    )
    default int groupStorageMinValue() {
        return 0;
    }

    @ConfigItem(
        keyName = "groupStorageIncludeClan",
        name = "Include Group Name",
        description = "Whether notifications should include the GIM clan name",
        position = 143,
        section = groupStorageSection
    )
    default boolean groupStorageIncludeClan() {
        return true;
    }

    @ConfigItem(
        keyName = "groupStorageIncludePrice",
        name = "Include Price",
        description = "Whether price should be included on individual items,<br/>" +
            "and a Net Value field generated for notifications",
        position = 144,
        section = groupStorageSection
    )
    default boolean groupStorageIncludePrice() {
        return true;
    }

    @ConfigItem(
        keyName = "groupStorageNotifyMessage",
        name = "Notification Message",
        description = "The message to be sent through the webhook.<br/>" +
            "Use %USERNAME% to insert your username<br/>" +
            "Use %DEPOSITED% to insert the list of deposited items<br/>" +
            "Use %WITHDRAWN% to insert the list of withdrawn items",
        position = 145,
        section = groupStorageSection
    )
    default String groupStorageNotifyMessage() {
        return "%USERNAME% has deposited:\n%DEPOSITED%\n\n%USERNAME% has withdrawn:\n%WITHDRAWN%";
    }

    @ConfigItem(
        keyName = "notifyTrades",
        name = "Enable Trades",
        description = "Enable notifications upon completed player trades",
        position = 160,
        section = tradeSection
    )
    default boolean notifyTrades() {
        return false;
    }

    @ConfigItem(
        keyName = "tradeSendImage",
        name = "Send Image",
        description = "Send image with the notification",
        position = 161,
        section = tradeSection
    )
    default boolean tradeSendImage() {
        return true;
    }

    @ConfigItem(
        keyName = "tradeMinValue",
        name = "Min Value",
        description = "The minimum total value of the traded items to send a notification",
        position = 162,
        section = tradeSection
    )
    default int tradeMinValue() {
        return 0;
    }

    @ConfigItem(
        keyName = "tradeNotifyMessage",
        name = "Notification Message",
        description = "The message to be sent through the webhook.<br/>" +
            "Use %USERNAME% to insert your username<br/>" +
            "Use %COUNTERPARTY% to insert the name of the other player<br/>" +
            "Use %IN_VALUE% to insert the value of the items received from the counterparty<br/>" +
            "Use %OUT_VALUE% to insert the value of the items given to the counterparty",
        position = 163,
        section = tradeSection
    )*/


}

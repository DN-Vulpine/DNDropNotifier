package dinkplugin;

import com.google.inject.Provides;
import dinkplugin.notifiers.CombatTaskNotifier;
import dinkplugin.notifiers.LevelNotifier;
import dinkplugin.notifiers.LootNotifier;
import dinkplugin.notifiers.MetaNotifier;
import dinkplugin.notifiers.PetNotifier;
import dinkplugin.util.AccountTypeTracker;
import dinkplugin.util.KillCountService;
import dinkplugin.util.Utils;
import dinkplugin.util.WorldTypeTracker;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.GameState;
import net.runelite.api.events.AccountHashChanged;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GrandExchangeOfferChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.UsernameChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.events.WorldChanged;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.RuneLite;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.NotificationFired;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.events.PlayerLootReceived;
import net.runelite.client.events.PluginMessage;
import net.runelite.client.events.ProfileChanged;
import net.runelite.client.events.ServerNpcLoot;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.loottracker.LootReceived;
import net.runelite.client.util.ColorUtil;

import javax.inject.Inject;
import java.awt.Color;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@PluginDescriptor(
    name = "Dink",
    description = "Discord-compatible webhook notifications for Loot, Death, Levels, CLog, KC, Diary, Quests, etc.",
    tags = { "loot", "logger", "collection", "pet", "death", "xp", "level", "notifications", "discord", "speedrun",
        "diary", "combat achievements", "combat task", "barbarian assault", "high level gambles" }
)
public class DinkPlugin extends Plugin {
    public static final String USER_AGENT = RuneLite.USER_AGENT + " (Dink/1.x)";

    private @Inject ChatMessageManager chatManager;

    private @Inject SettingsManager settingsManager;
    private @Inject VersionManager versionManager;
    private @Inject AccountTypeTracker accountTracker;
    private @Inject WorldTypeTracker worldTracker;

    private @Inject KillCountService killCountService;

    private @Inject PetNotifier petNotifier;
    private @Inject LevelNotifier levelNotifier;
    private @Inject LootNotifier lootNotifier;
    //    private @Inject LeaguesNotifier leaguesNotifier;
    private @Inject CombatTaskNotifier combatTaskNotifier;
    private @Inject MetaNotifier metaNotifier;
    private final AtomicReference<GameState> gameState = new AtomicReference<>();

    private Map<String, Runnable> configDisabledTasks;

    @Inject
    protected void init() {
        // clear out state that could be stale if notifier is enabled again
        this.configDisabledTasks = Map.of(
            "levelEnabled", levelNotifier::reset
        );
    }

    @Override
    protected void startUp() {
        log.debug("Started up Dink");
        settingsManager.init();
        versionManager.onStart();
        accountTracker.init();
        worldTracker.init();
        lootNotifier.init();
        // leaguesNotifier.init();
    }

    @Override
    protected void shutDown() {
        log.debug("Shutting down Dink");
        this.resetNotifiers();
        gameState.lazySet(null);
        accountTracker.clear();
        worldTracker.clear();
    }

    void resetNotifiers() {
        petNotifier.reset();
        levelNotifier.reset();
    }

    @Provides
    DinkPluginConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(DinkPluginConfig.class);
    }

    @Subscribe
    public void onAccountHashChanged(AccountHashChanged event) {
        accountTracker.onAccountChange();
    }

    @Subscribe
    public void onCommandExecuted(CommandExecuted event) {
        settingsManager.onCommand(event);
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (!SettingsManager.CONFIG_GROUP.equals(event.getGroup())) {
            return;
        }

        settingsManager.onConfigChanged(event);
        accountTracker.onConfig(event.getKey());
        worldTracker.onConfig(event.getKey());
        lootNotifier.onConfigChanged(event.getKey(), event.getNewValue());

        if ("false".equals(event.getNewValue())) {
            Runnable task = configDisabledTasks.get(event.getKey());
            if (task != null) task.run();
        }
    }

    @Subscribe
    public void onUsernameChanged(UsernameChanged usernameChanged) {
        levelNotifier.reset();
        killCountService.reset();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        GameState newState = gameStateChanged.getGameState();
        if (newState == GameState.LOADING) {
            // an intermediate state that is irrelevant for our notifiers; ignore
            return;
        }

        GameState previousState = gameState.getAndSet(newState);
        if (previousState == newState) {
            // no real change occurred (just momentarily went through LOADING); ignore
            return;
        }

        versionManager.onGameState(previousState, newState);
        settingsManager.onGameState(previousState, newState);
        levelNotifier.onGameStateChanged(gameStateChanged);
        metaNotifier.onGameState(previousState, newState);
    }

    @Subscribe
    public void onStatChanged(StatChanged statChange) {
        levelNotifier.onStatChanged(statChange);
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        settingsManager.onTick();
        accountTracker.onTick();
        worldTracker.onTick();
        petNotifier.onTick();
        levelNotifier.onTick();
        combatTaskNotifier.onTick();
        metaNotifier.onTick();
    }

    @Subscribe(priority = 1) // run before the base loot tracker plugin
    public void onChatMessage(ChatMessage message) {
        String chatMessage = Utils.sanitize(message.getMessage());
        String source = message.getName() != null && !message.getName().isEmpty() ? message.getName() : message.getSender();
        switch (message.getType()) {
            case GAMEMESSAGE:
                if ("runelite".equals(source)) {
                    // filter out plugin-sourced chat messages
                    return;
                }

                lootNotifier.onGameMessage(chatMessage);
                petNotifier.onChatMessage(chatMessage);
                killCountService.onGameMessage(chatMessage);
                combatTaskNotifier.onGameMessage(chatMessage);
//                leaguesNotifier.onGameMessage(chatMessage);
                break;

            case FRIENDSCHATNOTIFICATION:
                // intentional fallthrough to clan notifications

            case CLAN_MESSAGE:
            case CLAN_GUEST_MESSAGE:
            case CLAN_GIM_MESSAGE:
                petNotifier.onClanNotification(chatMessage);
                break;

            case MESBOX:
                break;

            case TRADE:
                break;

            default:
                // do nothing
                break;
        }
    }

    @Subscribe(priority = 1) // run before the base GE plugin
    public void onGrandExchangeOfferChanged(GrandExchangeOfferChanged event) {
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied event) {
    }

    @Subscribe
    public void onActorDeath(ActorDeath actor) {
    }

    @Subscribe
    public void onInteractingChanged(InteractingChanged event) {
    }

    @Subscribe
    public void onScriptPreFired(ScriptPreFired event) {
        petNotifier.onScript(event.getScriptId());
    }

    @Subscribe(priority = 1) // run before the base loot tracker plugin
    public void onServerNpcLoot(ServerNpcLoot event) {
        // temporarily only use new event when needed
        int npcId = event.getComposition().getId();
        var name = event.getComposition().getName();
        if (npcId != NpcID.YAMA && npcId != NpcID.HESPORI && !name.startsWith("Hallowed Sepulchre")) {
            return;
        }

        killCountService.onServerNpcLoot(event);
        lootNotifier.onServerNpcLoot(event);
    }

    @Subscribe(priority = 1) // run before the base loot tracker plugin
    public void onNpcLootReceived(NpcLootReceived npcLootReceived) {
        if (npcLootReceived.getNpc().getId() == NpcID.YAMA) {
            // handled by ServerNpcLoot, but return just in case
            return;
        }

        killCountService.onNpcKill(npcLootReceived);
        lootNotifier.onNpcLootReceived(npcLootReceived);
    }

    @Subscribe
    public void onPlayerLootReceived(PlayerLootReceived playerLootReceived) {
        killCountService.onPlayerKill(playerLootReceived);
        lootNotifier.onPlayerLootReceived(playerLootReceived);
    }

    @Subscribe
    public void onProfileChanged(ProfileChanged event) {
        versionManager.onProfileChange();
    }

    @Subscribe
    public void onLootReceived(LootReceived lootReceived) {
        killCountService.onLoot(lootReceived);
        lootNotifier.onLootReceived(lootReceived);
    }

    @Subscribe
    public void onNotificationFired(NotificationFired event) {
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        settingsManager.onVarbitChanged(event);
        accountTracker.onVarbit(event);
        metaNotifier.onVarbit(event);
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event) {
        killCountService.onWidget(event);
    }

    @Subscribe
    public void onWidgetClosed(WidgetClosed event) {
    }

    @Subscribe
    public void onWorldChanged(WorldChanged event) {
        worldTracker.onWorldChange();
    }

    @Subscribe
    public void onPluginMessage(PluginMessage event) {
        if ("dink".equalsIgnoreCase(event.getNamespace()) && "notify".equalsIgnoreCase(event.getName())) {
        }
    }

    public void addChatSuccess(String message) {
        addChatMessage("Success", Utils.GREEN, message);
    }

    public void addChatWarning(String message) {
        addChatMessage("Warning", Utils.RED, message);
    }

    void addChatMessage(String category, Color color, String message) {
        String formatted = String.format("[%s] %s: %s",
            ColorUtil.wrapWithColorTag(getName(), Utils.PINK),
            category,
            ColorUtil.wrapWithColorTag(message, color)
        );

        chatManager.queue(
            QueuedMessage.builder()
                .type(ChatMessageType.CONSOLE)
                .runeLiteFormattedMessage(formatted)
                .build()
        );
    }
}

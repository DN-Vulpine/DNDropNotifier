# DN Discord Notifier

DN Discord Notifier sends webhook messages upon noteworthy in-game events.
While Dink supports the Discord webhook format (with rich embeds and optional screenshots), it also includes additional metadata that allows custom webhook servers to analyze messages or even generate their own messages.
Examples of the additional metadata can be found [here](docs/json-examples.md).
This project was forked from Dink, but was intentionally limited to only include certain categories of notable events, and also took control of what exactly triggers a notification out of the hands of the users to ensure that discord channels avoid spam. Drop values, CA tiers, etc are all hardcoded into the plugin with the intent of allowing Discord/clan admins to avoid the headache of trying to walk everyone through properly setting up the plugin. 

## Basic Setup

To use this plugin, a webhook URL is required; you can obtain one from Discord with the following steps:  
<sub>If you already have a link, skip to step 4.</sub>

1. Click the server name (at the top-left of your screen) and select `Server Settings`.
2. Select the `Integrations` tab on the left side and click `Create Webhook` (if other webhooks already exist, click `View Webhooks` and `New Webhook`).
3. Click the newly created webhook, select the target Discord channel, and click `Copy Webhook URL`.
4. Paste the copied link into the `Primary Webhook URLs` box in the Dink plugin settings.
5. (Optional): If you would like different webhook URLs to receive different sets of notifications, you can instead paste the link into each relevant box in the `Webhook Overrides` section. Note: when a notifier has an override URL, the notifier ignores the primary URL box.

## Notifiers

- [Level](#level): Send a webhook message upon leveling up a skill (with support for virtual levels and XP milestones)
- [Loot](#loot): Send a webhook message upon receiving valuable loot (with item rarity for monster drops)
- [Achievement Diaries](#achievement-diary): Send a webhook message upon completing an achievement diary (with customizable difficulty threshold)
- [Pet](#pet): Send a webhook message upon receiving a pet

## Other Setup

One notifier requires in-game settings to be configured to send chat messages upon certain events (so these events can serve as triggers for webhook notifications).

- Pet notifier recommends `Settings > All Settings > Chat > Untradeable loot notifications` to be enabled (which requires `Settings > All Settings > Chat > Loot drop notifications`) in order to determine the name of the pet

### Example

![img.png](img.png)

## Advanced Features

## Notifier Configuration

Most of the config options are self-explanatory. But the notification messages for each notification type also
contain some words that will be replaced with in-game values.

## Credits

This plugin uses code from [Universal Discord Notifier](https://github.com/MidgetJake/UniversalDiscordNotifier).

This plugin is primarily the code from [dink](https://github.com/pajlads/DinkPlugin).

Item rarity data is sourced from the OSRS Wiki (licensed under [CC BY-NC-SA 3.0](https://creativecommons.org/licenses/by-nc-sa/3.0/)),
which was conveniently parsed by [Flipping Utilities](https://github.com/Flipping-Utilities/parsed-osrs) (and [transformed](https://github.com/pajlads/DinkPlugin/blob/master/src/test/java/dinkplugin/RarityCalculator.java) by pajlads).

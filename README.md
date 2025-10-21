# TalkGroups

A comprehensive Minecraft plugin for managing custom chat channels with configurable properties, GUI management, and persistent player preferences.

## Features

### Core Features
- **Custom Chat Channels**: Create unlimited TalkGroups with unique properties
- **Permission-Based Access**: Control who can access each channel
- **Cooldown System**: Prevent spam with configurable per-channel cooldowns
- **Mute/Unmute Channels**: Players can silence channels they don't want to see
- **Smart Notifications**: Get notified about missed messages in muted channels
- **Quick Aliases**: Send messages with simple commands like `/staff <message>`
- **Modern GUI**: Intuitive inventory-based interface for managing channels
- **Persistent Data**: SQLite database stores player preferences across sessions

### Technical Features
- Paper 1.20+ compatible
- Async database operations for optimal performance
- Robust GUI system with UUID tracking (no title/item name checking)
- Hot-reload configuration without server restart
- Full localization support via messages.yml
- Comprehensive JavaDocs
- Design patterns (Builder, Factory, Manager)

## Installation

1. Download the latest release JAR file
2. Place it in your server's `plugins` folder
3. Restart your server
4. Configure `config.yml` and `messages.yml` to your liking
5. Use `/tg reload` to apply changes without restarting

## Configuration

### config.yml

Define your TalkGroups in `config.yml`:

```yaml
talkgroups:
  staff:
    name: "&c&lSTAFF"                    # Display name (supports color codes)
    permission: "talkgroups.staff"       # Required permission
    cooldown: 0                          # Cooldown in seconds (0 = none)
    silencable: true                     # Can players mute this?
    notify: true                         # Send notifications when muted?
    notify-delay: 60                     # Seconds between notifications
    alias: "staff"                       # Command alias (/staff)
    prefix: "&8[&c&lSTAFF&8]"           # Chat prefix
    suffix: ""                           # Chat suffix (optional)
```

### messages.yml

Customize all plugin messages with color code support:

```yaml
channel:
  muted: "&aYou have muted {channel}&a."
  unmuted: "&aYou have unmuted {channel}&a."
  notification: "&e&l! &eYou missed &6{count} &emessage(s) in {channel} &ein the last &6{time}&e."
  format: "{prefix} &r{player}&7: &f{message}{suffix}"
```

## Commands

### Main Commands
- `/talkgroups` or `/tg` - Opens the TalkGroups GUI
- `/tg mute <channel>` - Mute a specific channel
- `/tg unmute <channel>` - Unmute a specific channel
- `/tg toggle <channel>` - Toggle mute status for a channel
- `/tg list` - List all accessible channels and their status
- `/tg help` - Display help information
- `/tg reload` - Reload configuration (requires permission)

### Alias Commands
Each TalkGroup has its own alias command:
- `/<alias> <message>` - Send a message to that channel
- Example: `/staff Need backup at spawn!`

## Permissions

### General Permissions
- `talkgroups.use` - Use the /talkgroups command (default: true)
- `talkgroups.bypass.cooldown` - Bypass channel cooldowns (default: op)
- `talkgroups.admin.reload` - Reload configuration (default: op)
- `talkgroups.*` - All permissions

### Channel Permissions
Define custom permissions for each channel in config.yml:
- `talkgroups.staff` - Access to staff channel
- `talkgroups.moderator` - Access to moderator channel
- `talkgroups.admin` - Access to admin channel
- etc.

## GUI System

The plugin features a modern, paginated GUI system:

### Features
- **Visual Indicators**: Green wool = unmuted, Red wool = muted
- **Hover Information**: See cooldown, notification settings, and more
- **Click to Toggle**: Simply click to mute/unmute channels
- **Pagination**: Automatically handles many channels
- **Robust Tracking**: Uses InventoryHolder pattern for reliable identification

### GUI Controls
- **Left/Right Arrows**: Navigate between pages
- **Channel Items**: Click to toggle mute status
- **Barrier**: Close the GUI

## Database

TalkGroups uses SQLite for persistent storage:
- **Location**: `plugins/TalkGroups/data.db`
- **Async Operations**: All database queries run asynchronously
- **Auto-Save**: Player data saved on quit and plugin disable
- **Auto-Load**: Player data loaded on join

## Examples

### Creating a VIP Channel

```yaml
vip:
  name: "&6&lVIP"
  permission: "talkgroups.vip"
  cooldown: 5
  silencable: true
  notify: true
  notify-delay: 120
  alias: "vip"
  prefix: "&8[&6&lVIP&8]"
  suffix: ""
```

Players with `talkgroups.vip` permission can:
- Use `/vip <message>` to send messages
- See VIP channel messages
- Mute/unmute the channel via GUI or commands
- Receive notifications every 2 minutes when muted

### Creating a Non-Silencable Admin Channel

```yaml
admin:
  name: "&4&lADMIN"
  permission: "talkgroups.admin"
  cooldown: 0
  silencable: false  # Cannot be muted
  notify: false
  notify-delay: 60
  alias: "admin"
  prefix: "&8[&4&lADMIN&8]"
  suffix: ""
```

## Building from Source

### Requirements
- Java 17 or higher
- Gradle 7.0 or higher

### Build Steps
```bash
# Clone the repository
git clone https://github.com/xef5000/TalkGroups.git
cd TalkGroups

# Build with Gradle
./gradlew shadowJar

# Output JAR will be in build/libs/
```

## Project Structure

```
src/main/java/ca/xef5000/talkGroups/
├── TalkGroups.java              # Main plugin class
├── command/
│   ├── TalkGroupCommand.java    # Main command handler
│   └── AliasCommand.java        # Alias command handler
├── config/
│   └── ConfigManager.java       # Configuration management
├── database/
│   └── DatabaseManager.java     # SQLite database handler
├── gui/
│   ├── GUIManager.java          # GUI event handler
│   └── TalkGroupGUI.java        # GUI implementation
├── listener/
│   └── PlayerListener.java      # Player join/quit events
├── manager/
│   └── PlayerDataManager.java   # Player data management
└── model/
    ├── TalkGroup.java           # TalkGroup data model
    └── PlayerData.java          # Player data model
```

## API Usage

### Getting Player Data
```java
PlayerDataManager dataManager = plugin.getPlayerDataManager();
PlayerData data = dataManager.getPlayerData(player);

// Check if channel is muted
boolean isMuted = data.isChannelMuted("staff");

// Toggle mute status
dataManager.toggleMute(player, "staff").thenAccept(nowMuted -> {
    // Handle result
});
```

### Getting TalkGroups
```java
ConfigManager configManager = plugin.getConfigManager();

// Get by ID
TalkGroup group = configManager.getTalkGroup("staff");

// Get by alias
TalkGroup group = configManager.getTalkGroupByAlias("staff");

// Get all groups
Map<String, TalkGroup> groups = configManager.getAllTalkGroups();
```

## Support

For issues, questions, or contributions:
- GitHub Issues: https://github.com/xef5000/TalkGroups/issues
- Documentation: https://github.com/xef5000/TalkGroups/wiki

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Credits

- Author: xef5000
- Built with Paper API
- Uses SQLite for data persistence


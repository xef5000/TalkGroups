# TalkGroups - Quick Start Guide

## Installation

1. **Build the Plugin**
   ```bash
   ./gradlew shadowJar
   ```
   The JAR file will be in `build/libs/TalkGroups-1.0.0.jar`

2. **Install on Server**
   - Copy the JAR to your server's `plugins` folder
   - Start or restart your server
   - The plugin will create default configuration files

3. **Verify Installation**
   - Check console for: `TalkGroups enabled successfully!`
   - Run `/tg help` in-game to confirm it's working

## First Steps

### 1. Configure Your Channels

Edit `plugins/TalkGroups/config.yml`:

```yaml
talkgroups:
  staff:
    name: "&c&lSTAFF"
    permission: "talkgroups.staff"
    cooldown: 0
    silencable: true
    notify: true
    notify-delay: 60
    alias: "staff"
    prefix: "&8[&c&lSTAFF&8]"
    suffix: ""
```

### 2. Set Up Permissions

Using your permissions plugin (LuckPerms, etc.):

```
/lp group admin permission set talkgroups.staff true
/lp group moderator permission set talkgroups.moderator true
```

### 3. Reload Configuration

```
/tg reload
```

## Basic Usage

### For Players

**Open the GUI:**
```
/tg
```

**Send a message to a channel:**
```
/staff Hello everyone!
/mod Need help at spawn
```

**Mute/Unmute channels:**
```
/tg mute staff
/tg unmute staff
/tg toggle staff
```

**List all channels:**
```
/tg list
```

### For Administrators

**Reload configuration:**
```
/tg reload
```

**Grant channel access:**
```
/lp user PlayerName permission set talkgroups.vip true
```

**Bypass cooldowns:**
```
/lp user PlayerName permission set talkgroups.bypass.cooldown true
```

## Common Configurations

### 1. Staff-Only Channel (No Cooldown, Cannot Mute)

```yaml
admin:
  name: "&4&lADMIN"
  permission: "talkgroups.admin"
  cooldown: 0
  silencable: false
  notify: false
  notify-delay: 60
  alias: "admin"
  prefix: "&8[&4&lADMIN&8]"
```

### 2. VIP Channel (5s Cooldown, Can Mute)

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
```

### 3. Public Channel (10s Cooldown, Can Mute)

```yaml
global:
  name: "&a&lGLOBAL"
  permission: "talkgroups.global"
  cooldown: 10
  silencable: true
  notify: false
  notify-delay: 60
  alias: "g"
  prefix: "&8[&a&lGLOBAL&8]"
```

## Customizing Messages

Edit `plugins/TalkGroups/messages.yml`:

### Change GUI Title
```yaml
gui:
  title: "&8My Custom Title"
```

### Change Chat Format
```yaml
channel:
  format: "{prefix} &r{player}&7: &f{message}{suffix}"
```

### Change Notification Message
```yaml
channel:
  notification: "&e&l! &eYou missed &6{count} &emessage(s) in {channel} &ein the last &6{time}&e."
```

## Troubleshooting

### Plugin Won't Load
- Check console for errors
- Ensure you're using Paper 1.20+
- Verify Java 17+ is installed

### Commands Not Working
- Check permissions are set correctly
- Verify config.yml syntax (use a YAML validator)
- Try `/tg reload` after config changes

### Database Errors
- Check file permissions on `plugins/TalkGroups/data.db`
- Ensure SQLite is bundled (it should be with shadowJar)
- Check console for specific error messages

### GUI Not Opening
- Ensure player has `talkgroups.use` permission
- Check for inventory conflicts with other plugins
- Verify player has access to at least one channel

## Performance Tips

1. **Cooldowns**: Use reasonable cooldowns to prevent spam
2. **Notifications**: Set appropriate notify-delay values (60-120s recommended)
3. **Permissions**: Use permission groups instead of individual permissions
4. **Database**: The plugin uses async operations, but avoid excessive reloads

## Advanced Features

### Custom Chat Format

You can customize the chat format per channel:

```yaml
staff:
  prefix: "&8[&c&lSTAFF&8]"
  suffix: "&7 &8|&7"
```

With format in messages.yml:
```yaml
channel:
  format: "{prefix} &r{player}{suffix} &f{message}"
```

Result: `[STAFF] PlayerName | Hello!`

### Notification System

When a player mutes a channel with notifications enabled:
- Messages are counted while muted
- Notifications sent at configured intervals
- Shows total missed messages and time elapsed

Example notification:
```
! You missed 15 message(s) in STAFF in the last 2 minutes.
```

### Cooldown Bypass

Grant specific players or groups cooldown bypass:
```
/lp group vip permission set talkgroups.bypass.cooldown true
```

## Next Steps

1. **Customize your channels** in config.yml
2. **Set up permissions** for your groups
3. **Customize messages** in messages.yml
4. **Test with players** and gather feedback
5. **Adjust cooldowns and settings** as needed

## Getting Help

- Check the full README.md for detailed documentation
- Review console logs for error messages
- Ensure all configuration files are valid YAML
- Test with minimal configuration first, then add complexity

## Example Server Setup

Here's a complete example for a typical server:

```yaml
talkgroups:
  # Staff channels
  admin:
    name: "&4&lADMIN"
    permission: "talkgroups.admin"
    cooldown: 0
    silencable: false
    alias: "admin"
    prefix: "&8[&4&lADMIN&8]"
  
  staff:
    name: "&c&lSTAFF"
    permission: "talkgroups.staff"
    cooldown: 0
    silencable: true
    notify: true
    alias: "staff"
    prefix: "&8[&c&lSTAFF&8]"
  
  # Player channels
  vip:
    name: "&6&lVIP"
    permission: "talkgroups.vip"
    cooldown: 5
    silencable: true
    notify: true
    alias: "vip"
    prefix: "&8[&6&lVIP&8]"
  
  global:
    name: "&a&lGLOBAL"
    permission: "talkgroups.global"
    cooldown: 10
    silencable: true
    alias: "g"
    prefix: "&8[&a&lG&8]"
```

Permissions setup:
```
/lp group admin permission set talkgroups.admin true
/lp group admin permission set talkgroups.staff true
/lp group mod permission set talkgroups.staff true
/lp group vip permission set talkgroups.vip true
/lp group default permission set talkgroups.global true
```

Now your server has a complete chat channel system!


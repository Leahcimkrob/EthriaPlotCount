# EthriaPlotAddon

[![Download](https://img.shields.io/badge/Download-v1.2-brightgreen.svg)](https://github.com/leahcimkrob/EthriaPlotAddon/releases/tag/v1.2)
[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Paper](https://img.shields.io/badge/Paper-1.21+-blue.svg)](https://papermc.io/)
[![PlotSquared](https://img.shields.io/badge/PlotSquared-7.5.9+-green.svg)](https://github.com/IntellectualSites/PlotSquared)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Ein leistungsstarkes Minecraft-Plugin für PlotSquared-Server mit Entity-Zählung, Plot-Management und Admin-Tools.

*A powerful Minecraft plugin for PlotSquared servers with entity counting, plot management, and admin tools.*

---

## 🚀 Features

### 🔢 Entity Counting
- Count specific entities on plots: `/plotaddon count sheep`
- Multi-language support: `/plotcount schaf` (German) or `/plotcount sheep` (English)
- Admin overview: `/plotaddon count all`
- Merged plot support with automatic detection

### 📊 Plot Management  
- **Plot Check**: `/plotaddon check` - Shows plot owner and online status
- **Extended Info**: `/plotaddon check -v` - Shows trusted players and merge info
- **Real-time Status**: "Online now" vs "Last seen 2 days ago"

### 🔧 Administration
- **Configuration Reload**: `/plotaddon reload`
- **Dynamic Help**: `/plotaddon help` - Shows only available commands
- **Custom Aliases**: Configure in `config.yml`

### 🛡️ Permission System
- **Granular Control**: Per-entity or per-group permissions
- **Player/Admin Separation**: Different permission sets
- **Smart Tab-Completion**: Only shows allowed entities

---

## 📦 Quick Start

1. **Download** the latest release
2. **Place** in your `plugins/` folder  
3. **Restart** your server
4. **Configure** permissions (see [Wiki](https://github.com/leahcimkrob/EthriaPlotAddon/wiki))
5. **Use** `/plotaddon help` in-game

---

## 🎮 Example Usage

```bash
# Count entities
/plotaddon count sheep          # Count sheep on plot
/plotcount kuh                  # Count cows (German name)
/plotaddon count all            # All entities (Admin only)

# Plot information  
/plotaddon check                # Show plot owner
/plotaddon check -v             # Extended plot info

# Administration
/plotaddon reload               # Reload config
/plotaddon help                 # Show help
```

---

## 🔐 Permissions

### Player
- `ethriaplotaddon.count.own` - Count on own plots
- `ethriaplotaddon.count.group.animals` - Count animals  
- `ethriaplotaddon.plotcheck.use` - Check plot info

### Admin
- `ethriaplotaddon.admin` - Full access
- `ethriaplotaddon.count.reload` - Reload config

**Full permission list**: [Wiki Permissions](https://github.com/leahcimkrob/EthriaPlotAddon/wiki/Permissions)

---

## 📚 Documentation

- **[🏠 Wiki Home](https://github.com/leahcimkrob/EthriaPlotAddon/wiki)** - Complete documentation
- **[📋 Commands](https://github.com/leahcimkrob/EthriaPlotAddon/wiki/Commands)** - All available commands  
- **[🔐 Permissions](https://github.com/leahcimkrob/EthriaPlotAddon/wiki/Permissions)** - Permission system

---

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/Leahcimkrob/EthriaPlotAddon/issues)
- **Wiki**: [Documentation](https://github.com/Leahcimkrob/EthriaPlotAddon/wiki)

---

## 📝 License

MIT License - see [LICENSE](LICENSE) file

---

*Made with ❤️ for the Minecraft community*

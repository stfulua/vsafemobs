<div align="center">

<h1>vSafemobs</h1>

[![Version](https://img.shields.io/modrinth/v/vsafemobs?label=Version&color=24b47e)](https://modrinth.com/plugin/vsafemobs)
[![Downloads](https://img.shields.io/modrinth/dt/vsafemobs?label=Downloads&color=24b47e)](https://modrinth.com/plugin/vsafemobs)
[![License](https://img.shields.io/badge/License-vProLabs%20General%20License-blue)](https://www.vprolabs.xyz/projects/license/raw)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![Platform](https://img.shields.io/badge/Platform-Paper%20%2F%20Spigot-red)](https://papermc.io)

<p>Safe mob spawning and management plugin for Paper/Spigot 1.21.x</p>
<p>Control mob spawning with advanced rules and protections.</p>

</div>

---

### Features

- **Spawn Control**, Configure which mobs can spawn in which worlds
- **Mob Limits**, Set per-world mob caps to prevent lag
- **Safe Zones**, Define regions where hostile mobs cannot spawn
- **Time-Based Rules**, Different spawn rules for day/night
- **Light Level Control**, Configure minimum light levels for mob spawning
- **Chunk Limits**, Limit mobs per chunk to prevent overcrowding

---

### Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/vsafemobs reload` | Reload configuration | `vsafemobs.admin` |
| `/vsafemobs status` | Check plugin status | `vsafemobs.admin` |

---

### Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `vsafemobs.admin` | All commands | op |

---

### Configuration

<details>
<summary><b>View config.yml</b></summary>

```yaml
# World-specific mob spawning rules
worlds:
  world:
    enabled: true
    mob-cap: 70
    chunk-limit: 10
    safe-zones:
      - x1: 0
        z1: 0
        x2: 100
        z2: 100
    blocked-mobs:
      - CREEPER
```

</details>

---

### Links

- 🌐 **Website:** https://vprolabs.xyz
- 💬 **Discord:** https://discord.gg/SNzUYWbc5Q
- 📦 **Modrinth:** https://modrinth.com/plugin/vsafemobs
- ☕ **Support:** https://ko-fi.com/v4bi

---

### License

This project is licensed under the **vProLabs General License**.

- Non-Commercial Use Only
- Attribution Required
- Share Alike
- [View Full License](https://www.vprolabs.xyz/projects/license/raw)

---

<div align="center">

<sub>Made with ❤️ by <strong>vProLabs</strong></sub>

</div>

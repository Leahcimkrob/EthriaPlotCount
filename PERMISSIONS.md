# Permissions System

Das EthriaPlotCount-Plugin verfügt über ein umfangreiches und granulares Berechtigungssystem, das es Administratoren ermöglicht, präzise zu kontrollieren, welche Spieler welche Entity-Typen auf welchen Plots zählen können.

---

## 🔐 Basis-Berechtigungen

### Core Permissions
Diese Berechtigungen sind für die grundlegende Plugin-Funktionalität erforderlich:

| Permission | Beschreibung | Standard |
|------------|--------------|----------|
| `ethriaplotcount.use` | Grundlegende Plugin-Nutzung | `false` |
| `ethriaplotcount.admin` | Admin-Befehle (`all`, `*`) | `false` |
| `ethriaplotcount.reload` | Config neu laden | `false` |

### Beispiel-Konfiguration
```yaml
# LuckPerms Beispiel
lp group default permission set ethriaplotcount.use true
lp group admin permission set ethriaplotcount.admin true
lp group admin permission set ethriaplotcount.reload true
```

---

## 🐾 Entity-Gruppen-Berechtigungen

Das Plugin organisiert Entities in logische Gruppen. Spieler benötigen Berechtigung für die entsprechende Gruppe, um diese Entity-Typen zählen zu können.

### Verfügbare Gruppen

#### `ethriaplotcount.group.animals`
**Tiere und passive Mobs**
- Schafe (Sheep)
- Kühe (Cow) 
- Schweine (Pig)
- Hühner (Chicken)
- Pferde (Horse)
- Lamas (Llama)
- Katzen (Cat)
- Hunde/Wölfe (Wolf)
- Dorfbewohner (Villager)
- Eisengolem (Iron Golem)
- *und weitere...*

#### `ethriaplotcount.group.mobs`
**Feindliche Monster**
- Zombies (Zombie)
- Skelette (Skeleton)  
- Creeper (Creeper)
- Spinnen (Spider)
- Endermen (Enderman)
- Hexen (Witch)
- Phantome (Phantom)
- *und weitere...*

#### `ethriaplotcount.group.entities`
**Objekte und Dekorationen**
- Rüstungsständer (Armor Stand)
- Itemframes (Item Frame)
- Leuchtende Itemframes (Glow Item Frame)
- Gemälde (Painting)

#### `ethriaplotcount.group.vehicles`
**Fahrzeuge und Transport**
- Boote (Boat)
- Boote mit Truhe (Chest Boat)
- Loren (Minecart)
- Güterloren (Chest Minecart)
- Trichterloren (Hopper Minecart)
- TNT-Loren (TNT Minecart)

#### `ethriaplotcount.group.items`
**Items und Drops**
- Gegenstände auf dem Boden (Item)
- Erfahrungskugeln (Experience Orb)

#### `ethriaplotcount.group.projectiles`
**Projektile und Wurfgeschosse**
- Pfeile (Arrow)
- Spektralpfeile (Spectral Arrow)
- Dreizack (Trident)
- Schneebälle (Snowball)
- Eier (Egg)
- Enderperlen (Ender Pearl)
- Tränke (Potion)
- Feuerbälle (Fireball)

### Gruppen-Permission-Beispiele
```yaml
# Normale Spieler - nur Tiere
lp group player permission set ethriaplotcount.group.animals true

# VIP - Tiere und Objekte  
lp group vip permission set ethriaplotcount.group.animals true
lp group vip permission set ethriaplotcount.group.entities true

# Moderator - Alle außer Items
lp group moderator permission set ethriaplotcount.group.animals true
lp group moderator permission set ethriaplotcount.group.mobs true
lp group moderator permission set ethriaplotcount.group.entities true
lp group moderator permission set ethriaplotcount.group.vehicles true

# Admin - Alle Gruppen
lp group admin permission set ethriaplotcount.group.* true
```

---

## 🎯 Spezifische Entity-Berechtigungen

Für noch granularere Kontrolle können spezifische Entity-Berechtigungen vergeben werden. Diese haben Vorrang vor Gruppen-Berechtigungen.

### Format
```
ethriaplotcount.entity.<entity_name>
```

### Beliebte spezifische Berechtigungen
```yaml
# Nur bestimmte Tiere erlauben
ethriaplotcount.entity.sheep: true      # Nur Schafe
ethriaplotcount.entity.cow: true        # Nur Kühe  
ethriaplotcount.entity.pig: true        # Nur Schweine

# Bestimmte Objekte erlauben
ethriaplotcount.entity.armor_stand: true    # Nur Rüstungsständer
ethriaplotcount.entity.item_frame: true     # Nur Itemframes

# Spezifische Monster
ethriaplotcount.entity.zombie: true     # Nur Zombies
ethriaplotcount.entity.skeleton: true   # Nur Skelette
```

### Wildcard-Berechtigungen
```yaml
# Alle Entities erlauben (Admin)
ethriaplotcount.entity.*: true

# Negative Berechtigung - spezifische Entity verbieten
ethriaplotcount.entity.wither: false    # Wither ausschließen
```

---

## 🏠 Plot-Zugriff-Berechtigungen

Diese Berechtigungen kontrollieren, auf welchen Plots Spieler Entity-Zählungen durchführen können.

### `ethriaplotcount.own`
**Eigene und vertraute Plots**
- Plots im Besitz des Spielers
- Plots, wo der Spieler als "trusted" eingetragen ist  
- Plots, wo der Spieler als "added" eingetragen ist (nur wenn Plot-Owner online)

### `ethriaplotcount.other` 
**Fremde Plots**
- Plots anderer Spieler
- Auch ohne Trust/Add-Status
- Praktisch für Moderatoren/Admins

### Beispiel-Konfiguration
```yaml
# Normale Spieler - nur eigene Plots
lp group player permission set ethriaplotcount.own true

# Helfer - eigene + fremde Plots  
lp group helper permission set ethriaplotcount.own true
lp group helper permission set ethriaplotcount.other true

# Admin - alle Plots
lp group admin permission set ethriaplotcount.admin true  # Beinhaltet automatisch alle Plot-Zugriffe
```

---

## 👑 Admin-Berechtigungen

### `ethriaplotcount.admin`
**Master-Berechtigung für Administratoren**

Diese Berechtigung gewährt automatisch:
- ✅ Alle Entity-Gruppen (`group.*`)
- ✅ Alle spezifischen Entities (`entity.*`)
- ✅ Zugriff auf alle Plots (`own` + `other`)
- ✅ Admin-Befehle (`all`, `*`)
- ✅ Reload-Berechtigung

### `ethriaplotcount.reload`
**Config-Reload-Berechtigung**
- Ermöglicht `/plotcount reload`
- Kann unabhängig von Admin-Status vergeben werden

---

## 🔄 Permission-Hierarchie

Das Plugin prüft Berechtigungen in folgender Reihenfolge:

### 1. Admin-Check
```
ethriaplotcount.admin → Alle Rechte
```

### 2. Plot-Zugriff
```
ethriaplotcount.other → Fremde Plots
ethriaplotcount.own   → Eigene Plots
```

### 3. Entity-Berechtigung
```
ethriaplotcount.entity.<specific> → Spezifische Entity
ethriaplotcount.group.<group>     → Entity-Gruppe
```

### Beispiel-Prüfung für `/plotcount sheep`:
1. ✅ Hat Spieler `ethriaplotcount.admin`? → **Alle Rechte**
2. ✅ Ist Spieler auf eigenem/vertrautem Plot? → Prüfe `ethriaplotcount.own`
3. ✅ Hat Spieler `ethriaplotcount.entity.sheep`? → **Erlaubt**
4. ✅ Hat Spieler `ethriaplotcount.group.animals`? → **Erlaubt**
5. ❌ Keine Berechtigung → **Verweigert**

---

## 📊 Permission-Templates

### Template: Normale Spieler
```yaml
# Basis-Berechtigung
ethriaplotcount.use: true
ethriaplotcount.own: true

# Nur harmlose Tiere
ethriaplotcount.group.animals: true
```

### Template: VIP-Spieler
```yaml
# Basis + erweitert
ethriaplotcount.use: true
ethriaplotcount.own: true

# Tiere und Dekorationen
ethriaplotcount.group.animals: true
ethriaplotcount.group.entities: true
ethriaplotcount.group.vehicles: true
```

### Template: Moderator
```yaml
# Moderator-Rechte
ethriaplotcount.use: true
ethriaplotcount.own: true
ethriaplotcount.other: true

# Alle außer gefährlichen Items
ethriaplotcount.group.animals: true
ethriaplotcount.group.mobs: true
ethriaplotcount.group.entities: true
ethriaplotcount.group.vehicles: true
ethriaplotcount.group.projectiles: true
```

### Template: Administrator
```yaml
# Admin-Vollzugriff
ethriaplotcount.admin: true
ethriaplotcount.reload: true
```

---

## 🛠️ Permission-Plugin Integration

### LuckPerms
```bash
# Gruppe erstellen
lp creategroup plotcount_users

# Berechtigungen setzen
lp group plotcount_users permission set ethriaplotcount.use true
lp group plotcount_users permission set ethriaplotcount.own true
lp group plotcount_users permission set ethriaplotcount.group.animals true

# Spieler zur Gruppe hinzufügen
lp user <spielername> parent add plotcount_users
```

### GroupManager
```yaml
# groups.yml
groups:
  plotcount_users:
    permissions:
      - ethriaplotcount.use
      - ethriaplotcount.own
      - ethriaplotcount.group.animals
```

### PermissionsEx (Legacy)
```yaml
groups:
  plotcount_users:
    permissions:
      - ethriaplotcount.use
      - ethriaplotcount.own
      - ethriaplotcount.group.animals
```

---

## 🔍 Permission-Debugging

### Check-Commands
```bash
# LuckPerms - Spieler-Berechtigungen prüfen
lp user <spielername> permission check ethriaplotcount.group.animals

# Effektive Berechtigungen anzeigen
lp user <spielername> permission info
```

### Debug-Aktivierung
```yaml
# config.yml
settings:
  debug_enabled: true
```

### Debug-Output
```
[DEBUG] Permission-Check für Spieler 'TestUser':
[DEBUG] - ethriaplotcount.use: true ✅
[DEBUG] - ethriaplotcount.own: true ✅  
[DEBUG] - ethriaplotcount.group.animals: false ❌
[DEBUG] - ethriaplotcount.entity.sheep: false ❌
[DEBUG] → Entity 'sheep' verweigert
```

---

## ⚠️ Häufige Permission-Probleme

### Problem: "Du hast keine Berechtigung"
**Lösung:**
1. Basis-Berechtigung prüfen: `ethriaplotcount.use`
2. Plot-Zugriff prüfen: `ethriaplotcount.own` oder `ethriaplotcount.other`
3. Entity-Berechtigung prüfen: Gruppe oder spezifische Entity

### Problem: Entity wird nicht vorgeschlagen (Tab-Completion)
**Lösung:**
Tab-Completion zeigt nur Entities an, für die der Spieler Berechtigung hat. Berechtigung für gewünschte Entity/Gruppe vergeben.

### Problem: Admin kann keine Entities zählen
**Lösung:**
`ethriaplotcount.admin` gewährt alle Rechte. Prüfen, ob PlotSquared korrekt installiert ist.

---

## 📋 Permission-Checkliste

### ✅ Neue Spieler-Setup
- [ ] `ethriaplotcount.use` vergeben
- [ ] `ethriaplotcount.own` vergeben  
- [ ] Mindestens eine Entity-Gruppe vergeben
- [ ] Permissions testen mit `/plotcount help`

### ✅ Admin-Setup
- [ ] `ethriaplotcount.admin` vergeben
- [ ] `ethriaplotcount.reload` vergeben
- [ ] Admin-Befehle testen (`/plotcount all`)

### ✅ Permission-Optimierung
- [ ] Gruppen-basierte Berechtigungen verwenden
- [ ] Wildcard-Permissions nur bei Bedarf
- [ ] Regelmäßige Permission-Audits
- [ ] Debug-Mode für Troubleshooting

---

*Das Berechtigungssystem ist flexibel und skalierbar. Für weitere Fragen nutzen Sie bitte die GitHub Issues oder den Discord-Support.*

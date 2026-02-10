**去gradle.properties修改为你的Java路径才能进行构建！**

**Change gradle.properties to your Java path so the build can proceed!**

# Wings

这是 Wings Mod 的一个社区维护分支。

## 📜 项目历史

- **原始项目**: 由 [pau101](https://github.com/pau101/Wings) 创建。
- **1.18.2 移植版**: 由 [jt789](https://github.com/jt789/WingsPort) 移植和维护。

# Wings

This is a community-maintained fork of the Wings mod.

## 📜 Project History

- **Original Project**: Created by [pau101](https://github.com/pau101/Wings).
- **1.18.2 Port**: Ported and maintained by [jt789](https://github.com/jt789/WingsPort).

这是一个由pau101发起，并由jt789移植到1.18.2后，再次开发的一个版本。
通过喝下药水，你可以获得不同的翅膀，相比之前的版本，这个模组还添加了一个超级翅膀，但在生存模式下无法获取，可以获得更强力的药水效果，以及驱逐14格内的敌对生物。
每个翅膀都会在飞行期间、着陆、起飞时消耗你的饱食度。

## Introduction

This version of Wings is a community-developed continuation project, originally started by pau101 and later ported to Minecraft 1.18.2 by jt789. When you drink a wings potion, you receive different types of wings with unique attributes.

Compared to earlier versions, this branch adds a "Super Wings" item (obtainable only in Creative mode), stronger potion effects, and an effect that repels hostile mobs within a 14-block diameter. While wearing wings, press R to enter flight. Each type of wing consumes your hunger while flying, taking off, and landing.

There are plans to continue maintaining the project in the future.

## Crafting and Brewing

Base Material:

- Bat Blood Bottle: Obtain by right-clicking a bat with an empty glass bottle.

Wing Potion Brewing:

All wing potions are brewed in a Brewing Stand using a Slow Falling potion or a Long Slow Falling potion as the base.

| Wing Name | Brewing Ingredient |
|---------|---------|
| Angel Wings | Feather |
| Parrot Wings | Red Dye |
| Bat Wings | Bat Blood Bottle |
| Blue Butterfly Wings | Blue Dye |
| Dragon Wings | Leather |
| Evil Wings | Bone |
| Fairy Wings | Oxeye Daisy |
| Fire Wings | Blaze Powder |
| Monarch Butterfly Wings | Orange Dye |
| Slime Wings | Slime Ball |
| Lvjia Super Wings | Creative mode only |

Brewing steps:
1. Place a Slow Falling potion into the Brewing Stand as the base.
2. Put the corresponding ingredient into the top slot.
3. Wait for brewing to complete to obtain the wing potion.

## Custom Changes (1.20.1)

This section documents the custom changes added in this fork.

### Wearable Wings Item
- Added a new `Wings` chest item that equips angel wings when worn and removes them when unequipped.
- The item is dyeable in the crafting grid (like leather armor). The dye color tints the wing model.
- The chest armor model is hidden, so only the wings are visible.
- Wings are crafted by surrounding an Elytra with 4 Angel Wings Bottles.

Usage:
- Equip `Wings` in the chest slot to enable flight and render angel wings.
- Combine `Wings` with any dye in a crafting grid to recolor.

### Flight Poses
- Added a pose cycle with four styles:
  - Default (both arms forward)
  - Main-hand forward
  - Hands at sides
  - Hands at sides (outward)
- Pose changes are only allowed while flying or hovering.
- The pose preview overlay is disabled by default. Enable it in the mod config screen (`ESC -> Mods -> Wings -> Config`) via `client.enablePosePreview`.
- When enabled, a small inventory-style paper-doll preview appears briefly when the pose changes.

Usage:
- Press `G` while flying/hovering to cycle poses.

### Hover Mode
- Added hover mode to hold position midair with wing flapping.
- Hover auto-disables when movement keys are pressed (WASD, Space, Shift).

Usage:
- Press `H` to toggle hover.
- Press any movement key to exit hover.

## 介绍

这个版本的 Wings 是一个由社区开发的延续项目，最初由 pau101 启动，后来由 jt789 移植到 Minecraft 1.18.2。当你饮用翅膀药水时，会获得具有独特属性的不同类型翅膀。

与早期版本相比，此分支新增了一个“超级翅膀”物品（仅能在创造模式中获得）、更强的药水效果，以及一个在直径 14 格的范围内驱逐敌对生物的效果。佩戴翅膀时，按 R 进入飞行。每种翅膀在飞行、起飞和降落时都会消耗你的饱食度。

未来计划将继续维护该项目。

## 🧪 合成表

### 基础材料获取

**蝙蝠血瓶 (Bat Blood Bottle)**:
- 用玻璃瓶右键点击蝙蝠获得

### 翅膀药水酿造

所有翅膀药水都通过药水酿造台酿造，使用**缓慢坠落药水**或**长效缓慢坠落药水**作为基础药水：

| 翅膀名称 | 中文名称 | 酿造材料 |
|---------|---------|---------|
| Angel Wings | 天使翅膀 | 羽毛|
| Parrot Wings | 鹦鹉翅膀 | 红色染料|
| Bat Wings | 蝙蝠翅膀 | 蝙蝠血瓶|
| Blue Butterfly Wings | 蓝蝴蝶翅膀 | 蓝色染料 |
| Dragon Wings | 龙翅膀 | 皮革  |
| Evil Wings | 邪恶翅膀 | 骨头 |
| Fairy Wings | 精灵翅膀 | 滨菊  |
| Fire Wings | 火焰翅膀 | 烈焰粉  |
| Monarch Butterfly Wings | 帝王蝶翅膀 | 橙色染料  |
| Slime Wings | 史莱姆翅膀 | 粘液球  |
| Lvjia Super Wings | LVJIA超级翅膀 | 仅限创造模式 |

**酿造步骤**:
1. 在药水酿造台中放入**缓降药水**
2. 在上方槽位放入对应的材料
3. 等待酿造完成即可获得对应的翅膀药水

## 自定义改动 (1.20.1)

本节记录此分支版本中添加的自定义改动。

### 可穿戴翅膀物品
- 添加了一个新的胸部装备物品“翅膀”，穿戴后会显示天使翅膀，卸下后则会移除。
- 该物品可在合成台中染色（类似于皮革盔甲）。染料颜色会影响翅膀模型的颜色。
- 胸部盔甲模型会被隐藏，因此只会显示翅膀
- 翅膀的制作方法是：用四个天使之翼药水瓶围绕一个鞘翅。

使用方法：
- 将“翅膀”装备到胸部槽位即可启用飞行并显示天使翅膀。
- 在合成台中将“翅膀”与任何染料组合即可更改颜色。

### 飞行姿势
- 添加了三种飞行姿势：
  - 默认（双臂向前）
  - 主手向前
  - 双手放在两侧
- 姿势切换仅在飞行或悬停状态下有效。
- 切换姿势时，屏幕上会短暂显示一个类似物品栏界面的小人偶预览。

使用方法：
- 在飞行/悬停状态下按 `G` 键切换姿势。

### 悬停模式
- 添加了悬停模式，可在空中保持位置并扇动翅膀。
- 按下移动键（WASD、空格、Shift）时，悬停模式会自动禁用。

使用方法：
- 按 `H` 键切换悬停模式。
- 按任意移动键退出悬停模式。
## Zì dìngyì gǎidòng (1.20.1)

Běn jié jìlù cǐ fēnzhī bǎnběn zhōng tiānjiā de zì dìngyì gǎidòng.

### Kě chuāndài chìbǎng wùpǐn
- tiānjiāle yīgè xīn de xiōngbù zhuāngbèi wùpǐn “chìbǎng”, chuāndài hòu huì xiǎnshì tiānshǐ chìbǎng, xiè xià hòu zé huì yí chú.
- Gāi wùpǐn kě zài héchéng táizhōng rǎnsè (lèisì yú pígé kuījiǎ). Rǎnliào yánsè huì yǐngxiǎng chìbǎng móxíng de yánsè.
- Xiōngbù kuījiǎ móxíng huì bèi yǐncáng, yīncǐ zhǐ huì xiǎnshì chìbǎng.

Shǐyòng fāngfǎ:
- Jiāng “chìbǎng” zhuāngbèi dào xiōngbù cáo wèi jí kě qǐyòng fēi háng bìng xiǎnshì tiānshǐ chìbǎng.
- Zài héchéng táizhōng jiàng “chìbǎng” yǔ rènhé rǎnliào zǔhé jí kě gēnggǎi yánsè.

### Fēixíng zīshì
- tiānjiāle sān zhǒng fēixíng zīshì:
  - Mòrèn (shuāng bì xiàng qián)
  - zhǔ shǒu xiàng qián
  - shuāngshǒu fàng zài liǎng cè
- zīshì qiēhuàn jǐn zài fēixíng huò xuán tíng zhuàngtài xià yǒuxiào.
- Qiēhuàn zīshì shí, píngmù shàng huì duǎnzàn xiǎnshì yīgè lèisì wùpǐn lán jièmiàn de xiǎo rén ǒu yùlǎn.

Shǐyòng fāngfǎ:
- Zài fēixíng/xuán tíng zhuàngtài xià àn `G`jiàn qiēhuàn zīshì.

### Xuán tíng móshì
- tiānjiāle xuán tíng móshì, kě zài kōngzhōng bǎochí wèizhì bìng shāndòng chìbǎng.
- Àn xià yídòng jiàn (WASD, kònggé,Shift) shí, xuán tíng móshì huì zìdòng jìnyòng.

Shǐyòng fāngfǎ:
- Àn `H`jiàn qiēhuàn xuán tíng móshì.
- Àn rènyì yídòng jiàn tuìchū xuán tíng móshì.

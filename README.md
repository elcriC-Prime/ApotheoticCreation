# Apotheotic Creation (NeoForge 1.21.1 Port)

A simple addon that allows defining Create attribute filters for Apotheosis rarities and affixes.

**Note:** This repository is an unofficial experimental port to Minecraft 1.21.1 using NeoForge, successfully upgraded and refactored with the assistance of [Google Gemini](https://gemini.google.com).

## Features
- Filter items processed by Create machinery based on Apothic Equipment (Apotheosis) **Loot Rarities**.
- Filter items based on Apothic Equipment (Apotheosis) **Affixes** (e.g., Stalwart, Ironforged).

## Usage
1. Place a filter in a Create component (e.g. Smart Observer, Brass Funnel).
2. Insert an item with an Apothic rarity or an affix into the filter's interface. 
3. The filter UI will read the nested attributes and allow you to configure conditions like:
    - `"has 'Uncommon' rarity"`
    - `"has the 'Stalwart' affix"`

## Credits & Mod Details
* **Original Author:** [futorX](https://github.com/futorX/ApotheoticCreation) (Created for Forge 1.20.1)
* **Ported by:** User & Gemini AI
* **Tech Stack:** NeoForge 1.21.1, Create 6.0.9, Apothic Equipment (Apotheosis) 8.5.2
* **Changes:** Re-implemented the Create `ItemAttributeType` system to modern Minecraft Data Components (`StreamCodec` and `MapCodec`), and localized the Affix names dynamically.

## Download
Originally downloaded from [GitHub (1.20.1)](https://github.com/futorX/ApotheoticCreation)

*(You can download the compiled 1.21.1 NeoForge `.jar` from the GitHub Releases page of this repository once uploaded.)*

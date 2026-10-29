# The Quiet Ledger — early development foundation

An original, incomplete psychological-horror mod project for Minecraft Java 1.21.1 / NeoForge 21.1.252 / Java 21. It does not depend on or include assets from The Broken Script. **This is not a finished or verified installable mod.** No mod JAR has been produced in this environment.

## Implemented in source (not yet verified in Minecraft)

* A server-owned director runs at most once per second while players are online. It advances a shared active-time clock, makes bounded random anomaly opportunities and selects at most one online player per successful roll. A weighted catalog filters by stage, enabled channel, cooldown and (for Deep Knock) altitude below Y=40. Failed rolls and opportunities without an eligible target also consume the interval. There are no custom packets or client authority; vanilla sound and chat delivery target the selected player.
* A world-wide ledger in overworld SavedData records active ticks, the last opportunity, total emitted events, per-kind cooldowns, up to 1024 player cooldowns, and emergency suppression. Stage is derived from active time and capped at 5. It advances while at least one player is online, including while anomalies are suppressed; disabling the master switch stops progression as well.
* Seven event definitions: distant creak, hollow drone, deep knocks (below Y=40), breathing, unseen footsteps, and two original text messages. Five original synthesized mono sound files are selected by stage and cooldown; the ambient sound category and subtitles are used. They do not alter terrain, inventory, health, or permissions. Stage 0 has no message, and if audio is disabled there is no eligible event at stage 0.
* A craftable Torn Ledger Page (one paper plus one charcoal) opens a player-private journal. Scheduled sound encounters and dimension travel can add unique clues; `/ledgerjournal` rereads discoveries. The world stores a bounded set of up to 1,024 player journals, without eviction; new players stop recording discoveries if the cap is reached. Item use has a two-second cooldown.
* Two data-defined dimensions, `quietledger:empty_expanse` and `quietledger:forgotten_corridor`, using the vanilla End dimension type and flat void-biome terrain. They are early terrain foundations, not finished environments; neither has structures, puzzles or a procedural maze. Vanilla End-type rules apply: beds and respawn anchors do not work and can explode.
* Operator-level `/quietledger status`, `suppress`, `resume`, `test`, `sound <creak|drone|knocks|breath|steps>`, `enter expanse` and `enter corridor`. The sound command previews a specific cue for the invoking player, bypassing stage and cooldown without incrementing the event counter; it respects the audio enable and volume settings. Suppression stops scheduled opportunities, persists across restarts, and does not block the explicit test command. Test requires an in-game player and respects audio/message options but bypasses the master switch, chance and suppression. Test events are included in the event count. Any player in either Quiet Ledger dimension may run `/ledgerescape` to return near the overworld shared spawn, even when dimension travel is disabled. This is not a return to the previous coordinates. Entry and escape check up to 81 columns for solid ground and two empty blocks, reject several hazardous floor blocks, and refuse to teleport if no candidate is found. The check cannot guarantee safety from mobs or every possible modded hazard; if it fails, ask an operator for assistance. No blocks are changed. Travel may synchronously load up to nine destination chunks and can cause a short pause. Both commands require an in-game player.

## Configuration

NeoForge's per-world server config is `serverconfig/quietledger-server.toml` (server defaults can be placed in `defaultconfigs`). NeoForge validates ranges. Changes may require reloading/restarting the world.

| Key | Default | Meaning |
| --- | --- | --- |
| `enabled` | true | Master switch for scheduled events and progression |
| `audio` | true | Enable original sound cues |
| `audioVolumePercent` | 50 | Server-side cue volume, range 0–100; 0 silences audio events. Clients can additionally adjust the Ambient/Environment slider |
| `messages` | true | Narrative chat cues (stage 1+) |
| `intervalTicks` | 12000 | Minimum ticks between opportunities, range 1200–1200000 |
| `minutesPerStage` | 90 | Active minutes per stage, range 1–10080 |
| `chancePercent` | 30 | Chance per opportunity, range 0–100 |
| `playerCooldownTicks` | 12000 | Minimum active ticks between scheduled events for a player, range 1200–1200000 |
| `dimensionTravel` | true | Permit operator-initiated entry; never disables escape |
| `dimensionMinimumStage` | 0 | Minimum progression stage for operator entry, range 0–5 |

No destructive world modification is implemented, regardless of settings. There are no flashing effects or camera manipulation.

## Architecture and dependencies

The `core` package contains deterministic progression rules; `server` owns persistence, scheduling and commands. The director does not scan chunks or allocate long-lived per-player state. The template uses NeoForge ModDevGradle 2.0.148, NeoForge 21.1.252, Minecraft 1.21.1, Parchment mappings 2024.11.17 and Gradle 9.2.1. NeoForge and Minecraft provide the mod APIs and runtime; ModDevGradle supplies the development environment, mappings and packaging; Gradle supplies the build. No third-party runtime libraries are bundled. Five original procedural Ogg Vorbis sound assets are bundled; `tools/generate_sounds.py` regenerates them with NumPy and SoundFile, which are development-only dependencies. The sound assets and generator are MIT licensed under `LICENSE`, and contain no external samples. The official 1.21.1 MDK was used as a template (see `TEMPLATE_LICENSE.txt`).

References: [NeoForge 1.21.1 getting started](https://docs.neoforged.net/docs/1.21.1/gettingstarted/), [Saved Data](https://docs.neoforged.net/docs/1.21.1/datastorage/saveddata/), [official MDK](https://github.com/NeoForgeMDKs/MDK-1.21.1-ModDevGradle/tree/7819b902a351b03fe71db00754d103b5a31c4ebf). Mojang mappings plus the MDK's Parchment parameter mappings are used.

## Build, install and test

On a machine with a full 64-bit JDK 21 and access to Gradle, NeoForge, Minecraft and Parchment dependency repositories, run `./gradlew build`. A successful build is expected under `build/libs/quietledger-0.1.0.jar`; that path is **not currently an existing artifact**. Install only a successfully built JAR into the `mods` folder of a matching NeoForge 1.21.1 profile, and back up test worlds first. For development use `./gradlew runClient` or `./gradlew runServer`. `./gradlew progressionTest anomalyCatalogTest landingOffsetsTest journalTest` executes the dependency-free rules checks; `check` also runs them.

The development sandbox could not contact `services.gradle.org` or `maven.neoforged.net`. A Java 21 runtime from PyPI was used to try the wrapper, which failed downloading Gradle with an SSL handshake error; the sandbox has no installed full JDK. This prevents compilation against Minecraft/NeoForge and a real build, startup, GameTest or multiplayer test. The pure progression, anomaly catalog and landing-search classes and their executable checks were compiled and executed independently with the Eclipse Java batch compiler and Java 21 runtime: all four test programs passed, including 10,000 weighted selections, 1,000 surface-restriction checks, the 81-position landing search pattern, and persistent journal flag rules. `python3 -m unittest discover -s tests -v` passed static dimension JSON and sound-reference checks; these do not validate Minecraft codecs. All five Ogg Vorbis files were decoded with SoundFile and checked for mono format, 22,050 Hz sample rate, duration and peak level. The sounds have not been auditioned or tested in Minecraft. Java source parsing via tree-sitter also succeeded, which is not a substitute for type checking.

Manual validation after a successful build: verify loading on client and dedicated server; config creation and range handling; operator/non-operator command access; scheduled effects, chance and intervals; player-specific delivery with two clients and late join; save/reload of stage, counters and suppression; dimension registration in new and existing worlds; both destinations and survival-mode escape after restarting; blocked landing and disabled entry; crafting, journal discovery and per-player save/reload; audio/message disable switches; each operator sound preview, subtitles, and the client ambient volume slider; server shutdown/restart; and repeated-event performance. Do not use in an important world until these pass.

## Explicitly missing / known limitations

There are no custom entities, entity AI, animations, custom entity textures, custom blocks, structures, portals, procedural corridor generation, lunar visuals, custom damage, player exposure, visual or GUI effects, datapack-driven event definitions, event presets, world-edit policies, advanced multiplayer synchronization, or automatic portals and natural entry mechanisms. The two dimensions are flat, featureless prototype terrain, not the specified final dimensions, and the travel system has not been tested in Minecraft. The catalog is hard-coded rather than reloadable data; it has one global opportunity interval, per-kind cooldowns and a per-player cooldown, but no priorities or concurrent events. When the 1024-player ledger limit is reached, the oldest cooldown entry is evicted. Intensity only changes available cues/messages. Sounds are sent directly to a player rather than simulated as positional audio, and have not been verified with a Minecraft client. Existing source is unverified against the resolved 1.21.1 APIs; compile or runtime defects may remain. Persistence, config, commands and multiplayer behavior have not been integration-tested. These omissions are not planned features that can be enabled through configuration.

Code is MIT licensed; the separately retained MDK template license applies to template-derived material. No external original-mod content is used.

## GitHub Actions build

The `Build mod` workflow installs Java 21 and attempts the Gradle build on GitHub-hosted runners. If successful, download `quiet-ledger-mod` from the workflow run artifacts under the Actions tab. The workflow cannot fix unavailable repositories or compilation errors by itself; inspect its result before treating any artifact as usable. The initial workflow attempt failed and the cause is being investigated.

GPT how-tos: https://chatgpt.com/share/68a788eb-4118-8012-8e69-225af208eaca

# Beetrap (btfmc) — Fabric Mod

A Minecraft Fabric mod that runs a **bee & flower “Beetrap” game** inside a bounded garden area.  
Gameplay logic is orchestrated on the server and exposed to players via custom payloads, chat, boss bars, and a scoreboard.  
An **LLM-driven Agent** can observe events and issue high-level commands that are translated into in‑game actions via the game/state managers.

---

## Packages & What They Do

> Package names follow what appears in the code you shared. If you have more packages in your repo, add them here using the same pattern.

### `beetrap.btfmc` (root)
Core game/runtime layer.
- **`BeetrapGame`** — The central orchestrator. Holds references to the world, networking, flower systems, bee nest controller, boss bar/scoreboard display, the `BeetrapStateManager`, and the `Agent`. Exposes event entry points such as:
  - `onWorldTick()` — server tick update hook
  - `onChatMessageMessage(SignedMessage, ServerPlayerEntity, Parameters)` — forwards chat text to the Agent
  - `onMultipleChoiceSelectionResultReceived(questionId, option)` — client UI → server choice handling
  - `onPlayerTargetNewEntity(...)`, `onPlayerPollinate(...)`, `onPlayerRequestTimeTravel(...)`, `onPollinationCircleRadiusIncreaseRequested(...)`
  - `dispose()` — tears down blocks/UI and cleans up services
- **Other core services referenced by `BeetrapGame`** (defined under this or sibling packages):
  - `BeeNestController` — places/controls the bee nest (positioned from the garden bounds)
  - `GardenInformationBossBar` — top-of-screen boss bar with live info
  - `PlayerInteractionService` — server-side helper for prompting/collecting player inputs
  - `FlowerManager`, `FlowerValueScoreboardDisplayerService` — garden content and score display
  - `NetworkingService` — convenience API for sending typed S2C messages

### `beetrap.btfmc.handler`
Fabric wiring layer. Registers and dispatches events/commands/entities.
- **`BeetrapGameHandler`**
  - Keeps the **current `BeetrapGame` instance**
  - Registers world tick and chat message listeners
  - Bridges custom **C2S payloads** to the active `BeetrapGame`:
    - `MultipleChoiceSelectionResultC2SPayload`
    - `PlayerTargetNewEntityC2SPayload`
    - `PlayerPollinateC2SPayload`
    - `PlayerTimeTravelRequestC2SPayload`
    - `PollinationCircleRadiusIncreaseRequestC2SPayload`
- **`CommandHandler`** — Registers Brigadier commands that create/destroy/control the game (e.g., define garden bounds, start/stop, debug).
- **`NetworkHandler`** — Registers custom payload types and packet receivers.
- **`EntityHandler`** — Registers any custom entities used by the mod.

### `beetrap.btfmc.state`
Authoritative **gameplay state machine**.
- **`BeetrapStateManager`** — Manages high-level activities/rounds and time-travel history; coordinates `FlowerManager`, `BeeNestController`, UI surfaces, and Agent interactions.
- Concrete states (e.g., `ActivitySelectionState`, others) implement player prompts, transitions, scoring rules, withering logic, pollination circle radius changes, etc.

### `beetrap.btfmc.agent`
LLM-backed Agent & state machine that reacts to events and emits commands.
- **`Agent`** — Holds the current `AgentState`, an instruction builder, a **queue of `AgentCommand`s**, and the OpenAI client handle. Key methods:
  - `tick()` → drives the agent each server tick
  - `onChatMessageReceived(ServerPlayerEntity, String)` → forwards text to current state
  - `onGameStart()` → lifecycle hook
  - `onGptResponseReceived(Response, Throwable)` → parses JSON into `AgentCommand`s and enqueues them
- Subpackages (seen in imports):
  - `agent.physical.PhysicalAgent` — a concrete agent for “physical” action level (`AGENT_LEVEL_PHYSICAL`)
  - `agent.empty.EmptyAgent` — a no‑op or placeholder agent
  - `agent.event.*` — event message types delivered to the agent

### `beetrap.btfmc.flower`
Garden & scoring subsystem.
- `Flower`, `FlowerPool`, `FlowerManager` — track flowers in the garden, spawn/despawn, query values and colors
- `FlowerValueScoreboardDisplayerService` — renders the flower value ranking on the scoreboard

### `beetrap.btfmc.networking`
Typed networking payloads and helpers.
- **C2S payloads** (client → server): `PlayerPollinateC2SPayload`, `PlayerTargetNewEntityC2SPayload`, `PlayerTimeTravelRequestC2SPayload`, `PollinationCircleRadiusIncreaseRequestC2SPayload`, `MultipleChoiceSelectionResultC2SPayload`
- **S2C payloads** (server → client): e.g., `BeetrapLogS2CPayload` and other UI/logging/choice prompts
- **`NetworkingService`** — one-stop helper for sending server → client messages

### `beetrap.btfmc.openai`
OpenAI integration & client configuration.
- **`OpenAiUtil`** — loads credentials, constructs an `OpenAIClient`, and exposes it to the `Agent`

---

## Initialization Flow (What Runs When the Mod Loads)

### High level
1. **Fabric** calls `ModInitializer.onInitialize()` in **`beetrap.btfmc.Beetrapfabricmc`**.
2. **Environment & OpenAI** are initialized.
3. **Handlers** are registered (events, commands, networking, entities).
4. The mod is now idle until commands create a `BeetrapGame`. Once a game exists, ticks/chat/payloads flow into it.

### Exact code path
- `Beetrapfabricmc.onInitialize()`:
  1. `loadEnv()`  
     - Collects required properties (from process env / system props; if any are missing, attempts to load a local **`.env`** file).  
       **Required keys** (as seen in code): `OPENAI_API_KEY`, `OPENAI_BASE_URL`, `OPENAI_ORG_ID`, `OPENAI_PROJECT_ID`, `TYPECAST_API_KEY`.
  2. `OpenAiUtil.load()` → build the OpenAI client used by `Agent`
  3. `BeetrapGameHandler.registerEvents()` →
     - `ServerTickEvents.START_WORLD_TICK` → `BeetrapGameHandler.onWorldTick(...)`
     - `ServerMessageEvents.CHAT_MESSAGE` → `BeetrapGameHandler.onChatMessageReceived(...)`
  4. `CommandHandler.registerCommands()` → registers server commands (Brigadier)
  5. `NetworkHandler.registerCustomPayloads()` → registers C2S/S2C packet handlers
  6. `EntityHandler.registerEntities()` → registers custom entities

> At this point, the mod is initialized. **No game is running yet**.

### After a game is created (via a command)
- **Tick path**
  - `ServerTickEvents.START_WORLD_TICK`  
    → `BeetrapGameHandler.onWorldTick(server)`  
    → if a game exists: `BeetrapGame.onWorldTick()`  
    → ticks the **`Agent`** and **`BeetrapStateManager`**; sends updates via `NetworkingService`.
- **Chat path**
  - `ServerMessageEvents.CHAT_MESSAGE`  
    → `BeetrapGameHandler.onChatMessageReceived(signedMessage, player, params)`  
    → `BeetrapGame.onChatMessageMessage(...)`  
    → `Agent.onChatMessageReceived(player, text)` → state-driven handling; may call OpenAI and enqueue `AgentCommand`s; when responses arrive: `Agent.onGptResponseReceived(...)` → commands executed via state/services.
- **Custom payloads (client → server)**
  - `MultipleChoiceSelectionResultC2SPayload` → `BeetrapGame.onMultipleChoiceSelectionResultReceived(id, option)`
  - `PlayerTargetNewEntityC2SPayload` → `BeetrapGame.onPlayerTargetNewEntity(player, exists, entityId)`
  - `PlayerPollinateC2SPayload` → `BeetrapGame.onPlayerPollinate(player, exists, entityId)`
  - `PlayerTimeTravelRequestC2SPayload` → `BeetrapGame.onPlayerRequestTimeTravel(player, n, operation)`
  - `PollinationCircleRadiusIncreaseRequestC2SPayload` → `BeetrapGame.onPollinationCircleRadiusIncreaseRequested(player)`

### Game construction (what happens when a new game starts)
When `CommandHandler` creates a new `BeetrapGame(server, bottomLeft, topRight, aiLevel)`:
- Initializes world & **garden bounds**
- Creates `NetworkingService`, `FlowerManager(FLOWER_POOL_FLOWER_COUNT)`, and **`BeeNestController`** (nest base derived from garden center)
- Sets up **`FlowerValueScoreboardDisplayerService`** and **`GardenInformationBossBar`**
- Creates **`PlayerInteractionService`** for prompts/choices
- Builds **`BeetrapStateManager`** and the **`Agent`** (e.g., `PhysicalAgent` for `AGENT_LEVEL_PHYSICAL`, otherwise `EmptyAgent`)
- Seeds initial flowers/pollination radius and displays UI

---

## Configuration

Create a `.env` in the server working directory (or set real env vars) with:
```dotenv
OPENAI_API_KEY=...
OPENAI_BASE_URL=...
OPENAI_ORG_ID=...
OPENAI_PROJECT_ID=...
TYPECAST_API_KEY=...
```

> On startup, you should see a log line confirming that all required keys were found.

---

## Building & Running (quick notes)

- git pull
- Use a **Fabric**-compatible JDK and Gradle setup.
- Make sure your `.env` (or environment variables) is present before server launch.
- Use the registered commands (see `CommandHandler`) to define the garden area and **start a game
**.
- cd to beetrap-fabricmc-1.21.4
- .gradlew runClient
---

## Troubleshooting

- **Missing/OpenAI keys**: startup logs will warn if required keys cannot be found. Ensure `.env` exists or set actual environment variables.
- **No game activity**: the mod initializes quietly; run the start command from `CommandHandler` to create a game.
- **Packets not received**: verify `NetworkHandler.registerCustomPayloads()` ran (it does during `onInitialize`) and that the client mod is aligned with the same payload ids.

---

## Credits

- Fabric Loader/API  
- OpenAI client (used by `Agent` via `OpenAiUtil`)

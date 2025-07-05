
# Beetrap Minecraft Mod Project Structure

This project is a Minecraft mod built using the Fabric modding framework. It appears to be an educational mod focused on bees, flowers, and pollination with AI-powered agents. Here's a breakdown of the different modules and their relationship to the Minecraft server and client:

## Project Structure Overview

The project follows a standard Fabric mod structure with a clear separation between server-side and client-side code:

- `src/main/` - Contains server-side and common code (runs on both server and client)
- `src/client/` - Contains client-only code (runs only on the client)

## Server-Side Modules (src/main/)

1. **Core Module** (`Beetrapfabricmc.java`) - The main entry point for the server-side mod that initializes various handlers.

2. **Agent System** - AI-powered agents that interact with players:
    - `PhysicalAgent.java` - Creates a bee entity in the world that can move and chat with players
    - `ChatOnlyAgent.java` - A text-only agent that communicates through chat without a physical presence
    - `EmptyAgent.java` - A minimal agent implementation

3. **Game Logic**:
    - `BeeNestController.java` - Manages bee nests in the game
    - `BeetrapGame.java` - Core game mechanics
    - `GardenInformationBossBar.java` - UI element for garden information

4. **Entity System**:
    - `entity/` - Custom entity implementations
    - `flower/` - Flower-related code

5. **OpenAI Integration**:
    - `openai/` - Integration with OpenAI's API for AI-powered interactions

6. **Text-to-Speech**:
    - `tts/TextToSpeechUtil.java` - Server-side text-to-speech functionality

7. **Networking**:
    - `networking/` - Server-to-client (S2C) and client-to-server (C2S) communication packets

## Client-Side Modules (src/client/)

1. **Client Core** (`BeetrapfabricmcClient.java`) - The main entry point for client-side code

2. **Speech-to-Text**:
    - `stt/` - Client-side speech recognition functionality
    - `stt/SimpleVoiceChatPlugin.java` - Integration with a voice chat mod

3. **Rendering**:
    - `render/` - Custom entity renderers and visual effects

4. **UI**:
    - `screen/` - Custom UI screens and interfaces

5. **Client-Side Entity Management**:
    - `ClientGlowingEntityManager.java` - Manages glowing effects for entities on the client

## Communication Between Server and Client

The mod uses a robust networking system with clearly defined payloads:
- S2C (Server to Client) payloads: Send data from server to client (e.g., `ShowTextScreenS2CPayload`)
- C2S (Client to Server) payloads: Send data from client to server (e.g., `PlayerPollinateC2SPayload`)

## Summary

- **Server-side code** handles game mechanics, AI agents, entity behavior, and coordinates the overall game experience
- **Client-side code** handles rendering, user interface, speech recognition, and player input processing

This separation follows Minecraft's client-server architecture, ensuring that game logic runs on the server while visual and input-related code runs on the client.
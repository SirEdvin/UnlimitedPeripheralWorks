## ADDED Requirements

### Requirement: Wired network tunnel attunement
The system SHALL add an AE2 P2P tunnel type named `wired_network_p2p_tunnel` that can be attuned from a standard AE2 P2P tunnel using a CC:Tweaked networking cable, wired modem, or full-block wired modem.

#### Scenario: Attune with a CC cable
- **WHEN** a player uses a CC:Tweaked networking cable on an unattuned AE2 P2P tunnel
- **THEN** AE2 converts it to a wired network P2P tunnel

#### Scenario: Attune with either wired modem form
- **WHEN** a player uses a wired modem item or full-block wired modem item on an unattuned AE2 P2P tunnel
- **THEN** AE2 converts it to the same wired network P2P tunnel type

### Requirement: Bidirectional wired network bridging
Active linked wired network P2P endpoints SHALL join their attached CC:Tweaked wired nodes into one bidirectional wired network regardless of which endpoint AE2 designates as input or output.

#### Scenario: Input and one output are active
- **WHEN** an active linked input and output each face a CC:Tweaked wired element
- **THEN** computers on either side can discover peripherals and exchange wired-modem packets across the tunnel

#### Scenario: Input has multiple outputs
- **WHEN** one active input is linked to multiple active outputs with attached wired elements
- **THEN** all endpoint wired networks are joined to each other

#### Scenario: Endpoints span dimensions
- **WHEN** active linked endpoints are in different dimensions
- **THEN** their attached CC:Tweaked wired networks remain joined while both endpoint chunks are loaded

### Requirement: External-face connection
Each wired network P2P endpoint SHALL connect only to a CC:Tweaked wired element adjacent to the outward-facing side of that AE2 part.

#### Scenario: Wired element on the tunnel face
- **WHEN** a wired element is placed against the outward-facing side of an active endpoint
- **THEN** the endpoint connects that element to the tunneled wired network

#### Scenario: Unrelated neighboring wired element
- **WHEN** a wired element touches another side of the AE2 cable host but not the endpoint's outward face
- **THEN** that element is not connected by the tunnel part

### Requirement: Topology and lifecycle cleanup
The tunnel SHALL update CC:Tweaked node connections when AE2 tunnel links, AE2 activity, adjacent wired elements, chunks, or parts change, and SHALL remove connections that are no longer valid.

#### Scenario: Tunnel loses power or channel
- **WHEN** a linked endpoint becomes inactive
- **THEN** the formerly joined CC:Tweaked wired networks separate and stale endpoint nodes are removed

#### Scenario: Memory-card link changes
- **WHEN** a player relinks an endpoint to a different P2P input
- **THEN** its wired node disconnects from the old endpoint set and connects only to the new active endpoint set

#### Scenario: Adjacent cable is removed
- **WHEN** the wired element on an endpoint's outward face is removed
- **THEN** the endpoint drops that external wired-node connection

#### Scenario: Part unload or removal
- **WHEN** a wired network P2P endpoint unloads or is removed
- **THEN** all CC:Tweaked node connections owned by that endpoint are removed without affecting unrelated wired networks

### Requirement: Optional AE2 content
The wired network P2P tunnel SHALL be registered and presented only when AE2 is loaded, without making AE2 a required runtime dependency.

#### Scenario: AE2 is loaded
- **WHEN** the game starts with AE2
- **THEN** the tunnel part item, models, attunement tag entries, localization, and creative inventory entry are available

#### Scenario: AE2 is absent
- **WHEN** the game starts without AE2
- **THEN** Unlimited Peripheral Works starts normally and does not load or expose the AE2 tunnel part

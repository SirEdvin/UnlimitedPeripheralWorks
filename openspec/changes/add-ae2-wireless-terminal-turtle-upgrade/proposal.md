## Why

Turtles cannot directly use a linked AE2 wireless terminal to inspect network items or exchange items with their own inventory. Existing item-storage methods require a separately named source or target peripheral, which is awkward for a turtle whose local 16-slot inventory is the intended endpoint.

## What Changes

- Allow a linked standard AE2 Wireless Terminal to be equipped directly as a turtle peripheral upgrade while preserving the terminal's complete item state.
- Add an AE2 wireless terminal peripheral that lists network items and transfers items between the AE2 network and the owning turtle's inventory without a peripheral-name argument.
- Apply vanilla AE2 link, loaded-network, active-access-point, dimension, and wireless-range constraints on every peripheral call without loading chunks.
- Charge one turtle fuel for each valid item-transfer operation through Tweakium's fuel boon while leaving the terminal's AE charge unchanged.
- Add the proposed typed contract at `projects/typed-peripheral-unlimitedperipheralworks/integrations/ae2WirelessTerminal.ts` and keep generated declarations as build output.
- Limit the first version to the standard AE2 Wireless Terminal and item storage; defer the Wireless Crafting Terminal, fluids, crafting requests, and cross-dimensional access.

## Capabilities

### New Capabilities

- `ae2-wireless-terminal-turtle-upgrade`: Direct terminal equipping, state preservation, wireless connection validation, network item inspection, turtle-local transfers, fuel charging, and the typed Lua contract.

### Modified Capabilities

None.

## Impact

- AE2 integrations in the Fabric and Forge projects.
- Turtle upgrade registration, model registration, language data, and generated upgrade data.
- Persistent CC:Tweaked turtle upgrade NBT containing the equipped terminal state.
- Tweakium turtle ownership, inventory storage, item-query, and fuel-boon integration.
- Typed peripheral source at `projects/typed-peripheral-unlimitedperipheralworks/integrations/ae2WirelessTerminal.ts`.
- Fabric and Forge GameTests covering connection constraints, transfers, fuel, and item-state round trips.

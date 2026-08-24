local function wait_for_storage(side)
    for _ = 1, 100 do
        local target = peripheral.wrap(side)
        if target and target.items and target.tanks then return target end
        sleep(0.05)
    end
    error("ME network peripheral did not become available on " .. side)
end

local function item_count(storage)
    for _, item in ipairs(storage.items()) do
        if item.name == "minecraft:diamond" then return item.count end
    end
    return 0
end

local function water_amount(storage)
    for _, fluid in ipairs(storage.tanks()) do
        if fluid.name == "minecraft:water" then return fluid.amount end
    end
    return 0
end

local front = wait_for_storage("front")
local back = wait_for_storage("back")

for _ = 1, 100 do
    if item_count(front) == 8 or item_count(back) == 8 then break end
    sleep(0.05)
end

local primary, secondary, secondary_name
if item_count(front) == 8 then
    primary, secondary, secondary_name = front, back, "back"
else
    primary, secondary, secondary_name = back, front, "front"
end

local function subscription(name)
    for _, value in ipairs(primary.getSubscriptions()) do
        if value.name == name then return value end
    end
end

local function expect_change(expected_name, expected_type, expected_resource, expected_old, expected_new)
    local _, name, resource, old_amount, new_amount = os.pullEvent("ae2_storage_change")
    assert(name == expected_name, "unexpected subscription event: " .. tostring(name))
    assert(resource.type == expected_type and resource.name == expected_resource, "unexpected subscription resource")
    assert(old_amount == expected_old and new_amount == expected_new, "unexpected subscription amounts")
end

assert(item_count(primary) == 8, "item listing did not expose the seeded diamonds")
assert(water_amount(primary) == 1000, "fluid listing did not expose the seeded water")

primary.subscribe("all-items", "item")
primary.subscribe("diamond", "item", { all = { { name = "minecraft:diamond" } } })
primary.subscribe("all-fluids", "fluid")
primary.subscribe("water", "fluid", "minecraft:water")
primary.subscribe("replace", "item", "minecraft:diamond")
assert(not pcall(function() primary.subscribe("replace", "fluid", "not:a_fluid") end), "invalid replacement was accepted")
assert(subscription("replace").type == "item", "invalid replacement changed the subscription")
primary.subscribe("replace", "fluid", "minecraft:water")
local subscriptions = primary.getSubscriptions()
assert(subscriptions[1].name == "all-fluids" and subscriptions[2].name == "all-items" and subscriptions[5].name == "water", "subscriptions were not sorted")

assert(primary.pushItem(secondary_name, { name = "minecraft:diamond" }, 3) == 3, "item extraction failed")
expect_change("all-items", "item", "minecraft:diamond", 8, 5)
expect_change("diamond", "item", "minecraft:diamond", 8, 5)
assert(item_count(primary) == 5 and item_count(secondary) == 3, "item insertion was not reflected in listings")
assert(primary.unsubscribe("diamond"), "item subscription was not removed")
assert(not primary.unsubscribe("diamond"), "removed item subscription still existed")
assert(primary.pullItem(secondary_name, { name = "minecraft:diamond" }, 2) == 2, "item insertion failed")
expect_change("all-items", "item", "minecraft:diamond", 5, 7)
assert(item_count(primary) == 7 and item_count(secondary) == 1, "item extraction was not reflected in listings")
assert(primary.pushItem(secondary_name, { name = "minecraft:diamond" }, 7) == 7, "item removal failed")
expect_change("all-items", "item", "minecraft:diamond", 7, 0)
assert(primary.pullItem(secondary_name, { name = "minecraft:diamond" }, 8) == 8, "item reinsertion failed")
expect_change("all-items", "item", "minecraft:diamond", 0, 8)

assert(primary.pushFluid(secondary_name, 250, "minecraft:water") == 250, "fluid extraction failed")
expect_change("all-fluids", "fluid", "minecraft:water", 1000, 750)
expect_change("replace", "fluid", "minecraft:water", 1000, 750)
expect_change("water", "fluid", "minecraft:water", 1000, 750)
assert(water_amount(primary) == 750 and water_amount(secondary) == 250, "fluid insertion was not reflected in listings")
assert(primary.pullFluid(secondary_name, 100, "minecraft:water") == 100, "fluid insertion failed")
expect_change("all-fluids", "fluid", "minecraft:water", 750, 850)
expect_change("replace", "fluid", "minecraft:water", 750, 850)
expect_change("water", "fluid", "minecraft:water", 750, 850)
assert(water_amount(primary) == 850 and water_amount(secondary) == 150, "fluid extraction was not reflected in listings")
assert(primary.pushFluid(secondary_name, 850, "minecraft:water") == 850, "fluid removal failed")
expect_change("all-fluids", "fluid", "minecraft:water", 850, 0)
expect_change("replace", "fluid", "minecraft:water", 850, 0)
expect_change("water", "fluid", "minecraft:water", 850, 0)
assert(primary.pullFluid(secondary_name, 1000, "minecraft:water") == 1000, "fluid reinsertion failed")
expect_change("all-fluids", "fluid", "minecraft:water", 0, 1000)
expect_change("replace", "fluid", "minecraft:water", 0, 1000)
expect_change("water", "fluid", "minecraft:water", 0, 1000)

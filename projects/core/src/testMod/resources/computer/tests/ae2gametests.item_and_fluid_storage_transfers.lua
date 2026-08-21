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

assert(item_count(primary) == 8, "item listing did not expose the seeded diamonds")
assert(water_amount(primary) == 1000, "fluid listing did not expose the seeded water")
assert(primary.pushItem(secondary_name, { name = "minecraft:diamond" }, 3) == 3, "item extraction failed")
assert(item_count(primary) == 5 and item_count(secondary) == 3, "item insertion was not reflected in listings")
assert(primary.pullItem(secondary_name, { name = "minecraft:diamond" }, 2) == 2, "item insertion failed")
assert(item_count(primary) == 7 and item_count(secondary) == 1, "item extraction was not reflected in listings")

assert(primary.pushFluid(secondary_name, 250, "minecraft:water") == 250, "fluid extraction failed")
assert(water_amount(primary) == 750 and water_amount(secondary) == 250, "fluid insertion was not reflected in listings")
assert(primary.pullFluid(secondary_name, 100, "minecraft:water") == 100, "fluid insertion failed")
assert(water_amount(primary) == 850 and water_amount(secondary) == 150, "fluid extraction was not reflected in listings")

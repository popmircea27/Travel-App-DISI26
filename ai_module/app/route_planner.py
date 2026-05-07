import math


def distance(a: dict, b: dict) -> float:
    return math.sqrt(
        (a["latitude"] - b["latitude"]) ** 2 +
        (a["longitude"] - b["longitude"]) ** 2
    )


def build_route(locations: list[dict], start_latitude=None, start_longitude=None) -> list[dict]:
    if not locations:
        return []

    remaining = locations[:]

    if start_latitude is not None and start_longitude is not None:
        start = {
            "latitude": start_latitude,
            "longitude": start_longitude
        }
        current = min(remaining, key=lambda loc: distance(start, loc))
        remaining.remove(current)
        route = [current]
    else:
        route = [remaining.pop(0)]

    while remaining:
        current = route[-1]
        nearest = min(remaining, key=lambda loc: distance(current, loc))
        route.append(nearest)
        remaining.remove(nearest)

    for index, location in enumerate(route, start=1):
        location["order"] = index

    return route
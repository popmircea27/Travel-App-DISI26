db = db.getSiblingDB("travelapp_geo");

// Create MongoDB application user for the travelapp_geo database.
db.createUser({
    user: "travelapp_user",
    pwd: "travelapp_password",
    roles: [
        {
            role: "readWrite",
            db: "travelapp_geo"
        }
    ]
});

// Create required collections.
db.createCollection("user_profiles");
db.createCollection("user_locations");

db.user_profiles.createIndex({ userId: 1 }, { unique: true });

db.user_locations.createIndex({ userId: 1 }, { unique: true });

db.user_locations.createIndex({
    coordinates: "2dsphere"
});

db.user_locations.createIndex({ geohash: 1 });
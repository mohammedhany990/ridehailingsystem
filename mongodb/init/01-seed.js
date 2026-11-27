db = db.getSiblingDB("location_db");
db.createCollection("driver_locations");
db.driver_locations.createIndex({ location: "2dsphere" });
db.driver_locations.createIndex({ driverId: 1 }, { unique: true });

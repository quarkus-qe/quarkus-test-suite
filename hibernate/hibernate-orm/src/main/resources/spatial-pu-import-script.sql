INSERT INTO JtsRegion(id, area) VALUES(100, ST_GeomFromText('POLYGON((0 0,10 0,10 10,0 10,0 0))', 4326));
INSERT INTO GeolatteRoute(id, path) VALUES (200, ST_GeomFromText('LINESTRING(-5 5,15 5)', 4326));

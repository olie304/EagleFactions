ALTER TABLE Factions ADD IsPublic TINYINT(1) NOT NULL DEFAULT 0 AFTER Motd;

-- Set database version to 3
INSERT INTO Version VALUES (3);
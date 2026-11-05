-- V2__reference_data.sql — référentiel global des catégories de permis (doc §3.1 / §7.2).
-- min_driving_hours = minimum RÉGALEMENT à atteindre avant l'examen de conduite,
-- paramétrable par le gérant dans school.settings (doc §14 : ne pas coder en dur).
INSERT INTO license_category (id, code, label, min_driving_hours) VALUES
  (1, 'A', 'Moto', 10),
  (2, 'B', 'Voiture', 20),
  (3, 'C', 'Poids lourd', 30),
  (4, 'D', 'Transport en commun', 30);

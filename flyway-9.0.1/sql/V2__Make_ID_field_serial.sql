CREATE SEQUENCE instrument_id_seq OWNED BY instrument.id;
SELECT setval('instrument_id_seq', max(id)) FROM instrument;
ALTER TABLE instrument ALTER COLUMN id SET DEFAULT nextval('instrument_id_seq');
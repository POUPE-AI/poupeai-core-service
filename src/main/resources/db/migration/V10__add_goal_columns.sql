ALTER TABLE goals
  ADD COLUMN description TEXT,
  ADD COLUMN color_hex VARCHAR(20),
  ADD COLUMN initial_balance DECIMAL(15,2) DEFAULT 0 NOT NULL;

-- Backfill existing rows with defaults if needed
UPDATE goals SET initial_balance = 0 WHERE initial_balance IS NULL;
UPDATE goals SET description = '' WHERE description IS NULL;
UPDATE goals SET color_hex = '#000000' WHERE color_hex IS NULL;
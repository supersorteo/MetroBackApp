ALTER TABLE admin_panel ADD COLUMN mensaje_burbuja VARCHAR(200);
UPDATE admin_panel SET mensaje_burbuja = 'Precios de {mes} Actualizados' WHERE mensaje_burbuja IS NULL;

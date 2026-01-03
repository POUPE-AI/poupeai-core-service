CREATE TYPE institution_type AS ENUM ('BANK', 'CARD_ISSUER', 'BOTH');

CREATE TABLE institutions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    main_color_hex VARCHAR(7),
    logo_name VARCHAR(50) NOT NULL,
    type institution_type NOT NULL
);

CREATE INDEX idx_institutions_type ON institutions(type);

INSERT INTO institutions (name, main_color_hex, logo_name, type) VALUES
    ('Nubank', '#820AD1', 'nubank', 'BOTH'),
    ('Itaú', '#EC7000', 'itau', 'BOTH'),
    ('Bradesco', '#CC092F', 'bradesco', 'BOTH'),
    ('C6 Bank', '#1A1A1A', 'c6bank', 'BANK'),
    ('Inter', '#FF7A00', 'inter', 'BOTH'),
    ('Santander', '#EC0000', 'santander', 'BOTH'),
    ('Banco do Brasil', '#FFEF00', 'bb', 'BOTH'),
    ('Caixa', '#005CA9', 'caixa', 'BOTH'),
    ('BTG Pactual', '#001E62', 'btg', 'BANK'),
    ('XP', '#FFD100', 'xp', 'BANK'),
    ('PicPay', '#21C25E', 'picpay', 'BOTH'),
    ('Mercado Pago', '#009EE3', 'mercadopago', 'BOTH'),
    ('PagBank', '#FFC800', 'pagbank', 'BOTH'),
    ('Neon', '#00E5A0', 'neon', 'BANK'),
    ('Original', '#00A650', 'original', 'BANK'),
    ('Sicredi', '#00543E', 'sicredi', 'BANK'),
    ('Sicoob', '#003641', 'sicoob', 'BANK'),
    ('Elo', '#FFCB05', 'elo', 'CARD_ISSUER'),
    ('Mastercard', '#EB001B', 'mastercard', 'CARD_ISSUER'),
    ('Visa', '#1A1F71', 'visa', 'CARD_ISSUER');


CREATE TABLE fridge
(
    id              SERIAL      PRIMARY KEY,
    name            VARCHAR     NOT NULL,
    height          INT         NOT NULL,
    width           INT         NOT NULL,
    depth           INT         NOT NULL
)
;

CREATE TABLE fridge_record
(
    id              SERIAL      PRIMARY KEY,
    fridge_id       INT         NOT NULL,
    item_id         INT         NOT NULL,
    x               INT         NOT NULL,
    y               INT         NOT NULL,
    z               INT         NOT NULL
)
;

CREATE TABLE item
(
    id              SERIAL      PRIMARY KEY,
    name            VARCHAR     NOT NULL,
    expiry_date     DATE        NOT NULL,
    owner_id        INT         NOT NULL
)
;

CREATE TABLE owner
(
    id              SERIAL      PRIMARY KEY,
    name            VARCHAR     NOT NULL
)
;
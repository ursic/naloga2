CREATE TABLE IF NOT EXISTS vehicles
    (id INT IDENTITY,
     year SMALLINT,
     make VARCHAR(20),
     model VARCHAR(50),
     comment VARCHAR(1000),
     price DECIMAL (12, 4))
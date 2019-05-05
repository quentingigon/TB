DROP DATABASE IF EXISTS tb;

CREATE DATABASE tb;

\c tb;

DROP TABLE IF EXISTS site CASCADE;
CREATE TABLE site
  (
    site_id SERIAL PRIMARY KEY,
    name VARCHAR(15)
  );

INSERT INTO site (name) VALUES ('Cheseaux');


DROP TABLE IF EXISTS users CASCADE;
CREATE TABLE users
  (
    user_id SERIAL PRIMARY KEY,
    email VARCHAR(30),
    password VARCHAR(20)
  );


DROP TABLE IF EXISTS team CASCADE;
CREATE TABLE team
  (
    team_id SERIAL PRIMARY KEY,
    name VARCHAR(20),
    screens INTEGER[],
    screen_groups INTEGER[],
    schedules INTEGER[],
    diffusers INTEGER[],
    fluxes INTEGER[],
    members INTEGER[]
  );


DROP TABLE IF EXISTS teammember CASCADE;
CREATE TABLE teammember
  (
    member_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users (user_id),
    team_id INTEGER NOT NULL REFERENCES team (team_id)
  );


DROP TABLE IF EXISTS admin CASCADE;
CREATE TABLE admin
  (
    admin_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users (user_id)
  );


DROP TABLE IF EXISTS schedule CASCADE;
CREATE TABLE schedule
  (
    schedule_id SERIAL PRIMARY KEY,
    name VARCHAR(20),
    fluxes INTEGER[] NOT NULL,
    fallbacks INTEGER[]
  );


DROP TABLE IF EXISTS runningschedule CASCADE;
CREATE TABLE runningschedule
  (
    runningschedule_id SERIAL PRIMARY KEY,
    schedule_id INTEGER NOT NULL REFERENCES schedule (schedule_id),
    screens INTEGER[] NOT NULL
  );


DROP TABLE IF EXISTS diffuser CASCADE;
CREATE TABLE diffuser
  (
    diffuser_id SERIAL PRIMARY KEY,
    name VARCHAR(20),
    validity BIGINT
  );


DROP TABLE IF EXISTS runningdiffuser CASCADE;
CREATE TABLE runningdiffuser
  (
    diffuser_id INTEGER NOT NULL REFERENCES diffuser (diffuser_id),
    screens INTEGER[],
    flux_id INTEGER NOT NULL REFERENCES flux(flux_id),
    PRIMARY KEY(diffuser_id, screens)
  );


DROP TABLE IF EXISTS screen CASCADE;
CREATE TABLE screen
  (
    screen_id SERIAL PRIMARY KEY,
    site_id INTEGER REFERENCES site (site_id),
    runningschedule_id INTEGER REFERENCES runningschedule (runningschedule_id),
    next_to INTEGER REFERENCES screen (screen_id),
    name VARCHAR(20),
    mac_address VARCHAR(20),
    resolution VARCHAR(10),
    logged BOOLEAN
  );


DROP TABLE IF EXISTS waitingscreen CASCADE;
CREATE TABLE waitingscreen
  (
    waitingscreen_id SERIAL PRIMARY KEY,
    mac_address VARCHAR(20),
    code VARCHAR(10) NOT NULL
  );


DROP TABLE IF EXISTS screengroup CASCADE;
CREATE TABLE screengroup
  (
    group_id SERIAL PRIMARY KEY,
    screens INTEGER[]
  );


DROP TABLE IF EXISTS flux CASCADE;
CREATE TABLE flux
  (
    flux_id SERIAL PRIMARY KEY,
    name VARCHAR(20),
    url VARCHAR(30) NOT NULL,
    type VARCHAR(10),
    phase_n INTEGER,
    phase_duration INTEGER
  );


DROP TABLE IF EXISTS locatedflux CASCADE;
CREATE TABLE locatedflux
  (
    flux_id INTEGER NOT NULL REFERENCES flux (flux_id),
    site_id INTEGER NOT NULL REFERENCES site (site_id),
    PRIMARY KEY(flux_id, site_id)
  );


DROP TABLE IF EXISTS generalflux CASCADE;
CREATE TABLE generalflux
  (
    flux_id INTEGER NOT NULL REFERENCES flux (flux_id)
  );


DROP TABLE IF EXISTS fallbackflux CASCADE;
CREATE TABLE fallbackflux
  (
    flux_id INTEGER NOT NULL REFERENCES flux (flux_id)
  );



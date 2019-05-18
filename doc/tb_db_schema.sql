DROP DATABASE IF EXISTS tb;

CREATE DATABASE tb;

\c tb;

DROP TABLE IF EXISTS site CASCADE;
CREATE TABLE site
  (
    site_id SERIAL PRIMARY KEY,
    name VARCHAR(15)
  );

INSERT INTO site (name) VALUES ('cheseaux');
INSERT INTO site (name) VALUES ('y-park');
INSERT INTO site (name) VALUES ('st-roch');


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
    members INTEGER[],
    admins INTEGER[]
  );


DROP TABLE IF EXISTS team_screens CASCADE;
CREATE TABLE team_screens
  (
    team_team_id INTEGER NOT NULL REFERENCES team (team_id),
    screens INTEGER NOT NULL REFERENCES screen (screen_id),
    PRIMARY KEY (team_team_id, screens)
  );


DROP TABLE IF EXISTS team_schedules CASCADE;
CREATE TABLE team_schedules
  (
    team_team_id INTEGER NOT NULL REFERENCES team (team_id),
    schedules INTEGER NOT NULL REFERENCES schedule (schedule_id),
    PRIMARY KEY (team_team_id, schedules)
  );


DROP TABLE IF EXISTS team_diffusers CASCADE;
CREATE TABLE team_diffusers
  (
    team_team_id INTEGER NOT NULL REFERENCES team (team_id),
    diffusers INTEGER NOT NULL REFERENCES diffuser (diffuser_id),
    PRIMARY KEY (team_team_id, diffusers)
  );


DROP TABLE IF EXISTS team_fluxes CASCADE;
CREATE TABLE team_fluxes
  (
    team_team_id INTEGER NOT NULL REFERENCES team (team_id),
    fluxes INTEGER NOT NULL REFERENCES flux (flux_id),
    PRIMARY KEY (team_team_id, fluxes)
  );


DROP TABLE IF EXISTS team_members CASCADE;
CREATE TABLE team_members
  (
    team_team_id INTEGER NOT NULL REFERENCES team (team_id),
    members INTEGER NOT NULL REFERENCES teammember (member_id),
    PRIMARY KEY (team_team_id, members)
  );


DROP TABLE IF EXISTS team_admins CASCADE;
CREATE TABLE team_admins
  (
    team_team_id INTEGER NOT NULL REFERENCES team (team_id),
    admins INTEGER NOT NULL REFERENCES admins (admin_id),
    PRIMARY KEY (team_team_id, admins)
  );


DROP TABLE IF EXISTS teammember CASCADE;
CREATE TABLE teammember
  (
    member_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users (user_id),
    team_id INTEGER NOT NULL REFERENCES team (team_id)
  );


DROP TABLE IF EXISTS admins CASCADE;
CREATE TABLE admins
  (
    admin_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users (user_id)
  );


DROP TABLE IF EXISTS schedule CASCADE;
CREATE TABLE schedule
  (
    schedule_id SERIAL PRIMARY KEY,
    name VARCHAR(20),
    fluxes INTEGER[],
    fallbacks INTEGER[]
  );


DROP TABLE IF EXISTS schedule_fluxes CASCADE;
CREATE TABLE schedule_fluxes
  (
    schedule_schedule_id INTEGER NOT NULL REFERENCES schedule (schedule_id),
    fluxes INTEGER NOT NULL REFERENCES flux (flux_id),
    PRIMARY KEY (schedule_schedule_id, fluxes)
  );


DROP TABLE IF EXISTS schedule_fallbacks CASCADE;
CREATE TABLE schedule_fallbacks
  (
    schedule_schedule_id INTEGER NOT NULL REFERENCES schedule (schedule_id),
    fallbacks INTEGER NOT NULL REFERENCES flux (flux_id),
    PRIMARY KEY (schedule_schedule_id, fallbacks)
  );


DROP TABLE IF EXISTS runningschedule CASCADE;
CREATE TABLE runningschedule
  (
    runningschedule_id SERIAL PRIMARY KEY,
    schedule_id INTEGER NOT NULL REFERENCES schedule (schedule_id),
    screens INTEGER[]
  );


DROP TABLE IF EXISTS runningschedule_screens CASCADE;
CREATE TABLE runningschedule_screens
  (
    runningschedule_runningschedule_id INTEGER NOT NULL REFERENCES runningschedule (runningschedule_id),
    screens INTEGER NOT NULL REFERENCES screen (screen_id),
    PRIMARY KEY (runningschedule_runningschedule_id, screens)
  );


DROP TABLE IF EXISTS diffuser CASCADE;
CREATE TABLE diffuser
  (
    diffuser_id SERIAL PRIMARY KEY,
    flux_id INTEGER,
    name VARCHAR(20),
    start_block INT,
    validity BIGINT
  );


DROP TABLE IF EXISTS runningdiffuser CASCADE;
CREATE TABLE runningdiffuser
  (
    runningdiffuser_id SERIAL PRIMARY KEY,
    diffuser_id INTEGER NOT NULL REFERENCES diffuser (diffuser_id),
    screens INTEGER[],
    flux_id INTEGER NOT NULL REFERENCES flux(flux_id)
  );


DROP TABLE IF EXISTS runningdiffuser_screens CASCADE;
CREATE TABLE runningdiffuser_screens
  (
    runningdiffuser_runningdiffuser_id INTEGER NOT NULL REFERENCES runningdiffuser (runningdiffuser_id),
    screens INTEGER NOT NULL REFERENCES screen (screen_id),
    PRIMARY KEY (runningdiffuser_runningdiffuser_id, screens)
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
    url VARCHAR(50) NOT NULL,
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

-- Functions

DROP FUNCTION IF EXISTS delete_screens_of_runningschedule();
CREATE FUNCTION delete_screens_of_runningschedule()
RETURNS TRIGGER
AS $$
BEGIN
    DELETE FROM runningschedule_screens
    WHERE runningschedule_runningschedule_id = OLD.runningschedule_id;

    DELETE FROM runningdiffuser
    WHERE schedule_id = OLD.schedule_id;
RETURN OLD;
END;
$$
LANGUAGE plpgsql;


DROP FUNCTION IF EXISTS delete_screens_of_runningdiffuser();
CREATE FUNCTION delete_screens_of_runningdiffuser()
RETURNS TRIGGER
AS $$
BEGIN
    DELETE FROM runningdiffuser_screens
    WHERE runningdiffuser_runningdiffuser_id = OLD.runningdiffuser_id;
RETURN OLD;
END;
$$
LANGUAGE plpgsql;


DROP FUNCTION IF EXISTS delete_data_of_team();
CREATE FUNCTION delete_data_of_team()
RETURNS TRIGGER
AS $$
BEGIN

    DELETE FROM team_admins
    WHERE team_team_id = OLD.team_id;

    DELETE FROM team_fluxes
    WHERE team_team_id = OLD.team_id;

    DELETE FROM team_members
    WHERE team_team_id = OLD.team_id;

    DELETE FROM team_diffusers
    WHERE team_team_id = OLD.team_id;

    DELETE FROM team_schedules
    WHERE team_team_id = OLD.team_id;

    DELETE FROM team_screens
    WHERE team_team_id = OLD.team_id;

RETURN OLD;
END;
$$
LANGUAGE plpgsql;

-- Triggers

CREATE TRIGGER on_runningschedule_delete BEFORE DELETE
   ON runningschedule
   FOR EACH ROW EXECUTE PROCEDURE delete_screens_of_runningschedule();

CREATE TRIGGER on_runningdiffuser_delete BEFORE DELETE
   ON runningdiffuser
   FOR EACH ROW EXECUTE PROCEDURE delete_screens_of_runningdiffuser();

CREATE TRIGGER on_team_delete BEFORE DELETE
   ON team
   FOR EACH ROW EXECUTE PROCEDURE delete_data_of_team();
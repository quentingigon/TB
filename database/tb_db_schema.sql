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
    email VARCHAR(100),
    password VARCHAR(50)
  );


DROP TABLE IF EXISTS team CASCADE;
CREATE TABLE team
  (
    team_id SERIAL PRIMARY KEY,
    name VARCHAR(100),
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
    admins INTEGER NOT NULL REFERENCES teammember (member_id),
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
    name VARCHAR(100),
    scheduledfluxes INTEGER[],
    fluxes INTEGER[],
    fallbacks INTEGER[],
    keep_order BOOLEAN
  );


DROP TABLE IF EXISTS scheduled_flux CASCADE;
CREATE TABLE scheduled_flux
  (
    scheduled_flux_id SERIAL PRIMARY KEY,
    schedule_id INTEGER REFERENCES schedule (schedule_id),
    flux_id INTEGER NOT NULL REFERENCES flux (flux_id),
    start_block INTEGER
  );


DROP TABLE IF EXISTS schedule_scheduledfluxes CASCADE;
CREATE TABLE schedule_scheduledfluxes
  (
    schedule_schedule_id INTEGER REFERENCES schedule (schedule_id),
    scheduledfluxes INTEGER REFERENCES scheduled_flux(scheduled_flux_id),
    PRIMARY KEY (schedule_schedule_id, scheduledfluxes)
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
    name VARCHAR(100),
    start_block INT,
    validity BIGINT,
    overwrite BOOLEAN
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
    name VARCHAR(100),
    mac_address VARCHAR(50),
    resolution VARCHAR(300),
    logged BOOLEAN,
    active BOOLEAN,
    current_flux_name VARCHAR(100)
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
    name VARCHAR(100),
    url VARCHAR(500) NOT NULL,
    data_check_url VARCHAR(500),
    type VARCHAR(30),
    phase_n INTEGER,
    phase_duration INTEGER
  );


INSERT INTO flux(name, url, type, phase_duration, phase_n) VALUES ('Waiting Page', '/waiting', 'URL', 1, 1);
INSERT INTO flux(name, url, type, phase_duration, phase_n) VALUES ('Maintenance', '/maintenance', 'URL', 1, 1);
INSERT INTO flux(name, url, type, phase_duration, phase_n) VALUES ('No Schedule', '/no_schedule', 'URL', 1, 1);
INSERT INTO flux(name, url, type, phase_duration, phase_n) VALUES ('Site error', '/site_error', 'URL', 1, 1);


DROP TABLE IF EXISTS locatedflux CASCADE;
CREATE TABLE locatedflux
  (
    locatedflux_id SERIAL PRIMARY KEY,
    flux_id INTEGER NOT NULL REFERENCES flux (flux_id),
    site_id INTEGER NOT NULL REFERENCES site (site_id)
  );


DROP TABLE IF EXISTS generalflux CASCADE;
CREATE TABLE generalflux
  (
    generalflux_id SERIAL PRIMARY KEY,
    flux_id INTEGER NOT NULL REFERENCES flux (flux_id)
  );


DROP TABLE IF EXISTS fallbackflux CASCADE;
CREATE TABLE fallbackflux
  (
    fallbackflux_id SERIAL PRIMARY KEY,
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

    UPDATE screen
    SET runningschedule_id = NULL
    WHERE runningschedule_id = OLD.runningschedule_id;
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


DROP FUNCTION IF EXISTS add_teammember_to_team();
CREATE FUNCTION add_teammember_to_team()
RETURNS TRIGGER
AS $$
BEGIN
    INSERT INTO team_members (team_team_id, members)
    VALUES (NEW.team_id, NEW.member_id);
RETURN OLD;
END;
$$
LANGUAGE plpgsql;


DROP FUNCTION IF EXISTS delete_fluxes_of_schedule();
CREATE FUNCTION delete_fluxes_of_schedule()
RETURNS TRIGGER
AS $$
BEGIN
    DELETE FROM schedule_fluxes
    WHERE schedule_schedule_id = OLD.schedule_id;

    DELETE FROM schedule_fallbacks
    WHERE schedule_schedule_id = OLD.schedule_id;

    DELETE FROM schedule_scheduledfluxes
    WHERE schedule_schedule_id = OLD.schedule_id;

    DELETE FROM scheduled_flux
    WHERE schedule_id = OLD.schedule_id;
RETURN OLD;
END;
$$
LANGUAGE plpgsql;


DROP FUNCTION IF EXISTS delete_general_or_located_or_scheduled();
CREATE FUNCTION delete_general_or_located_or_scheduled()
RETURNS TRIGGER
AS $$
BEGIN
    DELETE FROM locatedflux
    WHERE flux_id = OLD.flux_id;

    DELETE FROM generalflux
    WHERE flux_id = OLD.flux_id;

    DELETE FROM scheduled_flux
    WHERE flux_id = OLD.flux_id;
RETURN OLD;
END;
$$
LANGUAGE plpgsql;

DROP FUNCTION IF EXISTS delete_teammember_or_admin();
CREATE FUNCTION delete_teammember_or_admin()
RETURNS TRIGGER
AS $$
BEGIN
    DELETE FROM teammember
    WHERE user_id = OLD.user_id;

    DELETE FROM admins
    WHERE user_id = OLD.user_id;
RETURN OLD;
END;
$$
LANGUAGE plpgsql;


-- Triggers --

-- Deletes
CREATE TRIGGER on_runningschedule_delete BEFORE DELETE
   ON runningschedule
   FOR EACH ROW EXECUTE PROCEDURE delete_screens_of_runningschedule();

CREATE TRIGGER on_runningdiffuser_delete BEFORE DELETE
   ON runningdiffuser
   FOR EACH ROW EXECUTE PROCEDURE delete_screens_of_runningdiffuser();

CREATE TRIGGER on_team_delete BEFORE DELETE
   ON team
   FOR EACH ROW EXECUTE PROCEDURE delete_data_of_team();

CREATE TRIGGER on_schedule_delete BEFORE DELETE
   ON schedule
   FOR EACH ROW EXECUTE PROCEDURE delete_fluxes_of_schedule();

CREATE TRIGGER on_flux_delete BEFORE DELETE
   ON flux
   FOR EACH ROW EXECUTE PROCEDURE delete_general_or_located_or_scheduled();

CREATE TRIGGER on_user_delete BEFORE DELETE
   ON users
   FOR EACH ROW EXECUTE PROCEDURE delete_teammember_or_admin();


-- Inserts
CREATE TRIGGER on_teammember_insert AFTER INSERT
   ON teammember
   FOR EACH ROW EXECUTE PROCEDURE add_teammember_to_team();

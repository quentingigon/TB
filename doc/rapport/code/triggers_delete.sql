-- Fonction
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

-- Trigger
CREATE TRIGGER on_schedule_delete BEFORE DELETE
   ON schedule
   FOR EACH ROW EXECUTE PROCEDURE delete_fluxes_of_schedule();
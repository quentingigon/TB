public void execute(JobExecutionContext context) throws JobExecutionException {
    // recuperation des parametres stockes dans le trigger
    JobDataMap triggerDataMap = context.getTrigger().getJobDataMap();

    String screenIds = triggerDataMap.getString("screenIds");
    int fluxId = triggerDataMap.getInt("fluxId");

    // attribution des parametres du job
    if (!screenIds.isEmpty()) {
        event = new FluxEvent(fluxId, screenIds);
    }
    source = triggerDataMap.getString("source");
    entityId = triggerDataMap.getInt("entityId");
}
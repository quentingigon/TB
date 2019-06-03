// Serveur
public Result index() {
    return ok(team_page.render(dataUtils.getAllTeams(), null));
}

// Client: team_page.scala.html
@(teams: util.List[TeamData], error: String)

@if(teams != null) {
    @for(team <- teams) {
        <tr>
            <td>@team.getName()</td>
            <td>@if(team.getMembers() != null) {
                team.getMembers().length
            }</td>
        </tr>
    }
}
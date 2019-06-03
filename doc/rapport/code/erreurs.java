// UserController.java
public Result index() {
    return ok(user_page.render(servicePicker.getUserService().getAllUsers(),
        null));
}
public Result indexWithErrorMessage(String error) {
    return badRequest(user_page.render(servicePicker.getUserService().getAllUsers(),
        error));
}

// user_page.scala.html
@(users: util.List[UserData],
        error: String)
@main("Users Main Page", error) {
    <div>
        ...
    </div>
}

// main.scala.html
...
@if(error != null) {
    <div class="alert">
        <span class="closebtn" onclick="this.parentElement.style.display='none';">&times;</span>
        <strong>@error</strong>
    </div>
}
...
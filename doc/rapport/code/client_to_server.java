// Client
@helper.form(routes.UserController.register()) {
    @helper.CSRF.formField
    <div class="form-group">
        <input name="email" type="email" class="form-control">
    </div>
    <div class="form-group">
        <input name="password" type="password" class="form-control">
    </div>
    <button type="submit" class="btn btn-primary">Register</button>
}


// Serveur: UserController.java
public Result register(Http.Request request) {
    final Form<UserData> boundForm = form.bindFromRequest(request);
    UserData data = boundForm.get();
    User newUser = new User(data.getEmail(), data.getPassword());

    // email is not unique
    if (userRepository.getByEmail(newUser.getEmail()) != null) {
        return registerViewWithErrorMessage("Email is already used");
    }
    else 
        ...
}
package org.cmdbuild.services.json.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthenticationController extends ExceptionHandlingController {

	// public interface GroupMinimalInfo {
	// String getName();
	//
	// String getDescription();
	// }
	//
	// private final DefaultAuthenticationService authService;
	//
	// @Autowired
	// public AuthenticationController(final DefaultAuthenticationService
	// authService) {
	// this.authService = authService;
	// }
	//
	// @RequestMapping(value = "/login")
	// public @ResponseBody
	// JsonResponse login(@RequestParam(required = false, value = "username")
	// final String username,
	// @RequestParam(required = false, value = "password") final String
	// password,
	// @RequestParam(required = false, value = "role") final String groupName) {
	// final AuthenticatedUser user = authenticateIfValidCredentials(username,
	// password);
	// if (user.isAnonymous()) {
	// return failureWrongCredentials();
	// }
	// if (groupName != null) {
	// user.selectGroup(groupName);
	// user.filterPrivileges(groupName);
	// }
	// if (user.isValid()) {
	// return JsonResponse.success();
	// } else {
	// return failureMultipleGroups(user.getGroups());
	// }
	// }
	//
	// private AuthenticatedUser authenticateIfValidCredentials(final String
	// username, final String password) {
	// final AuthenticatedUser user;
	// if (username != null && password != null) {
	// final Login login = Login.newInstance(username);
	// user = authService.authenticate(login, password);
	// } else {
	// user = authService.getOperationUser();
	// }
	// return user;
	// }
	//
	// private final JsonResponse failureWrongCredentials() {
	// final Throwable t = AuthExceptionType.AUTH_LOGIN_WRONG.createException();
	// return JsonResponse.failure(t);
	// }
	//
	// private final JsonResponse failureMultipleGroups(final Set<CMGroup>
	// groups) {
	// final Throwable t =
	// AuthExceptionType.AUTH_MULTIPLE_GROUPS.createException();
	// return JsonResponse.failure(serializeGroups(groups), t);
	// }
	//
	// private final GroupMinimalInfo[] serializeGroups(final Set<CMGroup>
	// groups) {
	// final GroupMinimalInfo out[] = new GroupMinimalInfo[groups.size()];
	// int i = 0;
	// for (final CMGroup g : groups) {
	// out[i++] = new GroupMinimalInfo() {
	//
	// @Override
	// public String getName() {
	// return g.getName();
	// }
	//
	// @Override
	// public String getDescription() {
	// return g.getDescription();
	// }
	//
	// };
	// }
	// return out;
	// }
}

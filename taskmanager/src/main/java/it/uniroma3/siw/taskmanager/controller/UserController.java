package it.uniroma3.siw.taskmanager.controller;

import it.uniroma3.siw.taskmanager.controller.session.SessionData;
import it.uniroma3.siw.taskmanager.controller.validation.CredentialsValidator;
import it.uniroma3.siw.taskmanager.controller.validation.ProjectValidator;
import it.uniroma3.siw.taskmanager.controller.validation.UserValidator;
import it.uniroma3.siw.taskmanager.model.Credentials;
import it.uniroma3.siw.taskmanager.model.User;
import it.uniroma3.siw.taskmanager.repository.UserRepository;
import it.uniroma3.siw.taskmanager.service.CredentialsService;
import it.uniroma3.siw.taskmanager.service.ProjectService;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * The UserController handles all interactions involving User data.
 */
@Controller
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserValidator userValidator;

    @Autowired
    PasswordEncoder passwordEncoder;
    
    @Autowired
    ProjectService projectService;
    
    @Autowired
    ProjectValidator projectValidator;
    
    @Autowired
    CredentialsService credentialsService;
    
    @Autowired
    CredentialsValidator credentialsValidator;

    @Autowired
    SessionData sessionData;

    /**
     * This method is called when a GET request is sent by the user to URL "/users/user_id".
     * This method prepares and dispatches the User registration view.
     *
     * @param model the Request model
     * @return the name of the target view, that in this case is "register"
     */
    @RequestMapping(value = { "/home" }, method = RequestMethod.GET)
    public String home(Model model) {
        User loggedUser = sessionData.getLoggedUser();
        model.addAttribute("user", loggedUser);
        return "home";
    }

    /**
     * This method is called when a GET request is sent by the user to URL "/users/user_id".
     * This method prepares and dispatches the User registration view.
     *
     * @param model the Request model
     * @return the name of the target view, that in this case is "register"
     */
    @RequestMapping(value = { "/users/me" }, method = RequestMethod.GET)
    public String me(Model model) {
        User loggedUser = sessionData.getLoggedUser();
        Credentials credentials = sessionData.getLoggedCredentials();
        System.out.println(credentials.getPassword());
        model.addAttribute("user", loggedUser);
        model.addAttribute("credentials", credentials);

        return "userProfile";
    }

    /**
     * This method is called when a GET request is sent by the user to URL "/users/user_id".
     * This method prepares and dispatches the User registration view.
     *
     * @param model the Request model
     * @return the name of the target view, that in this case is "register"
     */
    @RequestMapping(value = { "/admin" }, method = RequestMethod.GET)
    public String admin(Model model) {
        User loggedUser = sessionData.getLoggedUser();
        model.addAttribute("user", loggedUser);
        return "admin";
    }
    
    /**
	 * This method is called when a GET request is sent by the user to URL "/admin/users".
	 * This method prepares and dispatches the all users view.
	 *
	 * @param model the Request model 
	 * @return the name of the target view, that in this case is "allUsers"
	 */
    @RequestMapping(value = {"/admin/users"}, method = RequestMethod.GET)
    public String allUsers(Model model) {
    	User loggedUser = this.sessionData.getLoggedUser();
    	List<Credentials> allCredentials = this.credentialsService.getAllCredentials();
    	
    	model.addAttribute("loggedUser", loggedUser);
    	model.addAttribute("credentialsList", allCredentials);
    	    	
    	return "allUsers";
    }
    
    
    /**
	 * This method is called when a POST request is sent by the user to URL "/admin/users/{username}/delete".
	 * This method collects the delete request from the user and starts the deleting process.
	 *
	 * @param model the Request model 
	 * @param username the username
	 * @return the name of the target view, that in this case is "/admin/users"
	 */
    @RequestMapping(value = {"/admin/users/{username}/delete"}, method = RequestMethod.POST)
    public String removeUser(Model model, @PathVariable String username) {
    	this.credentialsService.deleteCredentials(username);    	
    	
    	return "redirect:/admin/users";
    }
    
    /**
	 * This method is called when a GET request is sent by the user to URL "/users/me/updateProfile".
	 * This method prepares and dispatches the update profile view.
	 *
	 * @param model the Request model 
	 * @return the name of the target view, that in this case is "updateProfile"
	 */
    @RequestMapping(value = "/users/me/updateProfile", method = RequestMethod.GET)
    public String updateProfile(Model model) {
    	User user = this.sessionData.getLoggedUser();
    	Credentials credentials = this.sessionData.getLoggedCredentials();
    	
    	model.addAttribute("userForm", user);
    	model.addAttribute("credentialsForm", credentials);
    	    	
    	return "updateProfile";
    }
    
    /**
	 * This method is called when a POST request is sent by the user to URL "/users/me/updateProfile".
	 * This method collects data from the model and starts their the persistence process on the db.
	 *
	 * @param model the Request model 
	 * @param user the user
	 * @return the name of the target view, that in this case is "/users/me"
	 */
    @RequestMapping(value = "{/users/me/updateProfile}", method = RequestMethod.POST)
    public String confirmUpdateProfile(@Valid @ModelAttribute("userForm") User user,
            BindingResult userBindingResult,
            @Valid @ModelAttribute("credentialsForm") Credentials credentials,
            BindingResult credentialsBindingResult,
            Model model) {
    	
    	// validate user and credentials fields
    	this.userValidator.validate(user, userBindingResult);
        this.credentialsValidator.validate(credentials, credentialsBindingResult);
        
        // if neither of them had invalid contents, update the User and the Credentials into the DB
        if(!userBindingResult.hasErrors() && ! credentialsBindingResult.hasErrors()) {
        	user.setId(this.sessionData.getLoggedUser().getId());
        	credentials.setId(this.sessionData.getLoggedCredentials().getId());
            credentials.setUser(user);
            
            credentialsService.saveCredentials(credentials);
        }
    	    	
    	return "redirect:/users/me";
    }
    

}

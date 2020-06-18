package it.uniroma3.siw.taskmanager.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import it.uniroma3.siw.taskmanager.controller.session.SessionData;
import it.uniroma3.siw.taskmanager.controller.validation.ProjectValidator;
import it.uniroma3.siw.taskmanager.model.Credentials;
import it.uniroma3.siw.taskmanager.model.Project;
import it.uniroma3.siw.taskmanager.model.Tag;
import it.uniroma3.siw.taskmanager.model.Task;
import it.uniroma3.siw.taskmanager.model.User;
import it.uniroma3.siw.taskmanager.service.CredentialsService;
import it.uniroma3.siw.taskmanager.service.ProjectService;
import it.uniroma3.siw.taskmanager.service.TagService;
import it.uniroma3.siw.taskmanager.service.TaskService;
import it.uniroma3.siw.taskmanager.service.UserService;

/**
 * Controller for handling all interactions that involve operations on projects.
 */
@Controller
public class ProjectController {

	@Autowired
	ProjectService projectService;

	@Autowired
	UserService userService;

	@Autowired
	ProjectValidator projectValidator;

	@Autowired
	TaskService taskService;

	@Autowired
	CredentialsService credentialsService;

	@Autowired
	SessionData sessionData;
	
	@Autowired
	TagService tagService;


	/**
	 * This method is called when a GET request is sent by the user to URL "/projects".
	 * This method prepares and dispatches the owned projects view.
	 *
	 * @param model the Request model
	 * @return the name of the target view, that in this case is "myOwnedProjects"
	 */
	@RequestMapping(value = "/projects", method = RequestMethod.GET)
	public String myOwnedProjects(Model model) {
		User loggedUser = this.sessionData.getLoggedUser();
		List<Project> projectsList = this.projectService.retrieveProjectsOwnedBy(loggedUser);

		model.addAttribute("loggedUser", loggedUser);
		model.addAttribute("projectList", projectsList);

		return "myOwnedProjects";

	}

	/**
	 * This method is called when a GET request is sent by the user to URL "/projects/{projectId}".
	 * This method prepares and dispatches the single project view.
	 *
	 * @param model the Request model 
	 * @param projectId the project identificator
	 * @return the name of the target view, that in this case is "project", or it redirects to "/projects"
	 */
	@RequestMapping(value = {"/projects/{projectId}"}, method = RequestMethod.GET)
	public String project(Model model, @PathVariable Long projectId) {
		Project project = this.projectService.getProject(projectId);
		//if doesn't exist such project
		if(project==null) {
			return "redirect:/projects";
		}

		User loggedUser = this.sessionData.getLoggedUser();
		List<User> members = this.userService.getMembers(project);
		//if the logged user isn't owner or member of the project
		if(!project.getOwner().equals(loggedUser) && !members.contains(loggedUser)) {
			return "redirect:/projects";
		}
		List<Task> tasks = project.getTasks();
		List<Tag> tags = project.getTags();

		model.addAttribute("loggedUser", loggedUser);
		model.addAttribute("project", project);
		model.addAttribute("members", members);
		model.addAttribute("tasks", tasks);
		model.addAttribute("tags", tags);

		return "project";		
	}


	/**
	 * This method is called when a GET request is sent by the user to URL "/projects/shared".
	 * This method prepares and dispatches the shared projects view.
	 *
	 * @param model the Request model 
	 * @return the name of the target view, that in this case is "sharedProjects"
	 */
	@RequestMapping(value = "/projects/shared", method = RequestMethod.GET)
	public String sharedProjects(Model model) {
		User loggedUser = this.sessionData.getLoggedUser();
		List<Project> projects = this.projectService.retrieveProjectsSharedWith(loggedUser);

		model.addAttribute("loggedUser", loggedUser);
		model.addAttribute("projectList", projects);

		return "sharedProjects";
	}


	/**
	 * This method is called when a GET request is sent by the user to URL "/projects/{projectId}/update".
	 * This method prepares and dispatches the update project view.
	 *
	 * @param model the Request model 
	 * @param projectId the project identificator
	 * @return the name of the target view, that in this case is "updateProject", or it redirects to "/projects"
	 */
	@RequestMapping(value = {"/projects/{projectId}/update"}, method = RequestMethod.GET)
	public String projectUpdate(Model model, @PathVariable Long projectId) {
		Project project = this.projectService.getProject(projectId);
		//if doesn't exist such project
		if(project==null) {
			return "redirect:/projects";
		}

		User loggedUser = this.sessionData.getLoggedUser();
		//if the logged user isnt't the owner of the project
		if(!project.getOwner().equals(loggedUser)) {
			return "redirect:/projects";
		}

		model.addAttribute("loggedUser", loggedUser);
		model.addAttribute("projectForm", project);

		return "updateProject";		
	}


	/**
	 * This method is called when a POST request is sent by the user to URL "/projects/update/{id}".
	 * This method collects data from the model and starts the persisting process into the db.
	 *
	 * @param model the Request model 
	 * @param id the project identificator
	 * @param project an attribute of the model
	 * @param 
	 * @return the name of the target view, that in this case is "/projects/id", or "updateproject"
	 */
	@RequestMapping(value = {"/projects/update/{id}"}, method = RequestMethod.POST)
	public String saveProjectUpdate(@Valid @ModelAttribute("projectForm") Project project, BindingResult
			projectBindingresult, @PathVariable Long id, Model model) {

		User loggedUser = this.sessionData.getLoggedUser();
		//validates project
		projectValidator.validate(project, projectBindingresult);
		//if no detected errors
		if(!projectBindingresult.hasErrors()) {
			Project oldOne = this.projectService.getProject(id);
			project.setMembers(oldOne.getMembers());
			project.setTasks(oldOne.getTasks());
			project.setId(id);
			project.setOwner(loggedUser);
			this.projectService.saveProject(project);
			return "redirect:/projects/" + project.getId();
		}

		model.addAttribute("loggedUser", loggedUser);
		return "updateProject";		
	}


	/**
	 * This method is called when a GET request is sent by the user to URL "/projects/{projectId}/delete".
	 * This method prepares and dispatches the delete project view.
	 *
	 * @param model the Request model 
	 * @param projectId the project identificator
	 * @return the name of the target view, that in this case is "confirmDeleteProject"
	 */
	@RequestMapping(value = {"/projects/{projectId}/delete"}, method = RequestMethod.GET)
	public String projectDelete(Model model, @PathVariable Long projectId) {
		Project project = this.projectService.getProject(projectId);
		//if doesn't exist such project
		if(project==null) {
			return "redirect:/projects";
		}

		User loggedUser = this.sessionData.getLoggedUser();
		//if the logged user isn't the owner of the project
		if(!project.getOwner().equals(loggedUser)) {
			return "redirect:/projects";
		}

		model.addAttribute("loggedUser", loggedUser);
		model.addAttribute("projectForm", project);

		return "confirmDeleteProject";		
	}

	/**
	 * This method is called when a POST request is sent by the user to URL "/projects/delete/{projectId}".
	 * This method collects the remove request from the user and starts the deleting process.
	 *
	 * @param model the Request model 
	 * @param projectId the project identificator
	 * @return the name of the target view, that in this case is "home"
	 */
	@RequestMapping(value = {"/projects/delete/{projectId}"}, method = RequestMethod.POST)
	public String confirmProjectDelete(Model model, @PathVariable Long projectId) {
		Project project = this.projectService.getProject(projectId);
		if(!project.equals(null)) {
			this.projectService.deleteProject(project);
			return "redirect:/home";
		}
		return "home";		
	}


	/**
	 * This method is called when a GET request is sent by the user to URL "/projects/{projectId}/share".
	 * This method prepares and dispatches the share project view.
	 *
	 * @param model the Request model 
	 * @param projectId the project identificator
	 * @return the name of the target view, that in this case is "shareProject", or redirects to "/projects"
	 */
	@RequestMapping(value = {"/projects/{projectId}/share"}, method = RequestMethod.GET)
	public String projectShare(Model model, @PathVariable Long projectId) {
		Project project = this.projectService.getProject(projectId);
		//id doesn't exist such project
		if(project==null) {
			return "redirect:/projects";
		}

		User loggedUser = this.sessionData.getLoggedUser();
		//if the logged user isn't the owner of the project
		if(!project.getOwner().equals(loggedUser)) {
			return "redirect:/projects";
		}

		String username = new String();
		model.addAttribute("loggedUser", loggedUser);
		model.addAttribute("project", project);
		model.addAttribute("username", username );

		return "shareProject";		
	}

	/**
	 * This method is called when a POST request is sent by the user to URL "/projects/{projectId}/share".
	 * This method collects the share request from the user and starts the sharing process.
	 *
	 * @param model the Request model 
	 * @param projectId the project identificator
	 * @param username an attribute of the model
	 * @return the name of the target view, that in this case is "/projects", or "/home"
	 */
	@RequestMapping(value = {"/projects/{projectId}/share"}, method = RequestMethod.POST)
	public String attemptProjectShare(@Valid @ModelAttribute("username") String username,
			Model model, @PathVariable Long projectId) {
		Project project = this.projectService.getProject(projectId);
		Credentials credential = this.credentialsService.getCredentials(username);
		//if project or credential is null
		if(!(project==null) && !(credential==null)) {
			this.projectService.shareProjectWithUser(project, credential.getUser());
			return "redirect:/projects";
		}
		return "redirect:/home";		
	}

	/**
	 * This method is called when a GET request is sent by the user to URL "/projects/{projectId}/addTask".
	 * This method prepares and dispatches the add task view.
	 *
	 * @param model the Request model 
	 * @param projectId the project identificator
	 * @return the name of the target view, that in this case is "addTask", or redirects to "/project"
	 */
	@RequestMapping(value = {"/projects/{projectId}/addTask"}, method = RequestMethod.GET)
	public String addTaskToProject(Model model, @PathVariable Long projectId) {
		Project project = this.projectService.getProject(projectId);
		//if doesn't exist such project
		if(project==null) {
			return "redirect:/projects";
		}

		User loggedUser = this.sessionData.getLoggedUser();
		//if the logged user isn't the owner of the project
		if(!project.getOwner().equals(loggedUser)) {
			return "redirect:/projects";
		}

		model.addAttribute("loggedUser", loggedUser);
		model.addAttribute("project", project);
		model.addAttribute("taskForm", new Task());

		return "addTask";		
	}

	/**
	 * This method is called when a POST request is sent by the user to URL "/projects/{projectId}/addtask".
	 * This method collects the add task request from the user and starts the adding process.
	 *
	 * @param model the Request model 
	 * @param projectId the project identificator
	 * @param task an attribute of the model
	 * @return the name of the target view, that in this case is "/projects".
	 */
	@RequestMapping(value = {"/projects/{projectId}/addTask"}, method = RequestMethod.POST)
	public String confirmAddTaskToProject(@Valid @ModelAttribute("taskForm") Task task ,
			Model model, @PathVariable Long projectId) {

		Project project = this.projectService.getProject(projectId);
		//if project or task is null
		if(project==null || task==null) {
			return "redirect:/projects";
		}

		task.setCompleted(false); //just created, by default not yet completed
		List<Task> lista = project.getTasks();
		lista.add(task);
		this.projectService.saveProject(project); 

		return "redirect:/projects";
	}
	
	/**
	 * This method is called when a GET request is sent by the user to URL "/projects/add".
	 * This method prepares and dispatches the add project view.
	 *
	 * @param model the Request model 
	 * @return the name of the target view, that in this case is "addProject"
	 */
    @RequestMapping(value = "/projects/add", method = RequestMethod.GET)
    public String createProjectForm(Model model) {
		User loggedUser = this.sessionData.getLoggedUser();
		model.addAttribute("loggedUser", loggedUser);
		model.addAttribute("projectForm", new Project());
    	
    	return "addProject";
    }
    
    /**
	 * This method is called when a POST request is sent by the user to URL "/projects/add".
	 * This method prepares and dispatches the add task view.
	 *
	 * @param model the Request model 
	 * @param project a model attribute
	 * @return the name of the target view, that in this case is "addTask", or redirects to "/project"
	 */
    @RequestMapping(value = "/projects/add", method = RequestMethod.POST)
    public String createProject(@Valid @ModelAttribute("projectForm") Project project, BindingResult
    		projectBindingresult, Model model) {
    	
    	User loggedUser = this.sessionData.getLoggedUser();
    	projectValidator.validate(project, projectBindingresult);
    	if(!projectBindingresult.hasErrors()) {
    		project.setOwner(loggedUser);
    		this.projectService.saveProject(project);
    		return "redirect:/projects/" + project.getId();
    	}
    	
    	model.addAttribute("loggedUser", loggedUser);
    	
    	return "addProject";
    }
    
    /**
	 * This method is called when a GET request is sent by the user to URL "/projects/{projectId}/addTag".
	 * This method prepares and dispatches the add tag view.
	 *
	 * @param model the Request model 
	 * @param projectId the project identificator
	 * @return the name of the target view, that in this case is "addTask", or redirects to "/project"
	 */
	@RequestMapping(value = {"/projects/{projectId}/addTag"}, method = RequestMethod.GET)
	public String addTagToProject(Model model, @PathVariable Long projectId) {
		Project project = this.projectService.getProject(projectId);
		//if doesn't exist such project
		if(project==null) {
			return "redirect:/projects";
		}

		User loggedUser = this.sessionData.getLoggedUser();
		//if the logged user isn't the owner of the project
		if(!project.getOwner().equals(loggedUser)) {
			return "redirect:/projects";
		}

		model.addAttribute("loggedUser", loggedUser);
		model.addAttribute("project", project);
		model.addAttribute("tagForm", new Tag());

		return "addTag";		
	}
	
	@RequestMapping(value = {"/projects/{projectId}/addTag"}, method = RequestMethod.POST)
	public String confirmAddTagToProject(@Valid @ModelAttribute("tagForm") Tag tag ,
			Model model, @PathVariable Long projectId) {

		Project project = this.projectService.getProject(projectId);
		//if project or task is null
		if(project==null || tag==null) {
			return "redirect:/projects";
		}
		
		project.getTags().add(tag);
		this.projectService.saveProject(project); //new tag persisted into the db by cascade

		return "redirect:/projects";
	}


}

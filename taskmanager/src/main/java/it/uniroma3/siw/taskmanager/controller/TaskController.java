package it.uniroma3.siw.taskmanager.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import it.uniroma3.siw.taskmanager.controller.session.SessionData;
import it.uniroma3.siw.taskmanager.model.Project;
import it.uniroma3.siw.taskmanager.model.Tag;
import it.uniroma3.siw.taskmanager.model.Task;
import it.uniroma3.siw.taskmanager.model.User;
import it.uniroma3.siw.taskmanager.service.ProjectService;
import it.uniroma3.siw.taskmanager.service.TagService;
import it.uniroma3.siw.taskmanager.service.TaskService;
import it.uniroma3.siw.taskmanager.service.UserService;

/**
 * Controller for handling all interactions that involve operations on tasks.
 */
@Controller
public class TaskController {
	
	@Autowired
	TaskService taskService;
	
	@Autowired
	ProjectService projectService;
	
	@Autowired
	UserService userService;	
	
	@Autowired
	TagService tagService;
	
	@Autowired
	SessionData sessionData;
	
	
	/**
	 * This method is called when a GET request is sent by the user to URL "/task/{taskId}/update".
	 * This method prepares and dispatches the update task view.
	 *
	 * @param model the Request model 
	 * @param taskId the identificative of the task
	 * @return the name of the target view, that in this case is "updateTask", or redirects to "/projects"
	 */
	@RequestMapping(value = {"task/{taskId}/update"}, method = RequestMethod.GET)
	public String taskUpdate(Model model, @PathVariable Long taskId) {
		Task task = this.taskService.getTask(taskId);
		
		if(task.equals(null)) {
			return "redirect:/projects";
		}
		
		model.addAttribute("taskForm", task);
		return "updateTask";
	}
	
	/**
	 * This method is called when a POST request is sent by the user to URL "/task/update/{taskId}".
	 * This method collects data from the model and starts their persistence process on the db.
	 *
	 * @param model the Request model 
	 * @param taskId the identificative of the task
	 * @param task the task
	 * @return the name of the target view, that in this case is "/projects"
	 */
	@RequestMapping(value = {"/task/update/{taskId}"}, method = RequestMethod.POST)
	public String confirmTaskUpdate(@Valid @ModelAttribute("taskForm") Task task ,
			Model model, @PathVariable Long taskId) {
		
		if(task.equals(null)) {
			return "redirect:/projects";
		}
		
		task.setId(taskId);
		this.taskService.saveTask(task);
		
		return "redirect:/projects";
	}
	
	/**
	 * This method is called when a GET request is sent by the user to URL "/task/{taskId}/delete".
	 * This method prepares and dispatches the delete task view.
	 *
	 * @param model the Request model 
	 * @param taskId the identificative of the task
	 * @return the name of the target view, that in this case is "confirmTaskDelete", or redirects to "/projects"
	 */
	@RequestMapping(value = {"task/{taskId}/delete"}, method = RequestMethod.GET)
	public String deleteTask(Model model, @PathVariable Long taskId) {
		Task task = this.taskService.getTask(taskId);
		if(task!=null) {
			model.addAttribute("task", task);
			return "confirmTaskDelete";
		}
		
		return "redirect:/projects";
	}
	
	/**
	 * This method is called when a POST request is sent by the user to URL "/task/delete/{taskId}".
	 * This method collects the delete request from the user and starts the deleting process.
	 *
	 * @param model the Request model 
	 * @param taskId the identificative of the task
	 * @param task the task
	 * @return the name of the target view, that in this case is "/projects"
	 */
	@RequestMapping(value = {"task/{taskId}/delete"}, method = RequestMethod.POST)
	public String taskDelete(Model model, @PathVariable Long taskId) {
		Task task = this.taskService.getTask(taskId);
		if(task!=null) {
			this.taskService.deleteTask(task);
		}
		
		return "redirect:/projects";
	}
	
	/**
	 * This method is called when a GET request is sent by the user to URL "/task/{taskId}/assignTo/{projectId}".
	 * This method prepares and dispatches the assign task view.
	 *
	 * @param model the Request model 
	 * @param taskId the identificative of the task
	 * @param projectId the identificative of the project
	 * @return the name of the target view, that in this case is "assignToMember", or redirects to "/projects/{projectId}"
	 */
	@RequestMapping(value = {"task/{taskId}/assignTo/{projectId}"}, method = RequestMethod.GET)
	public String assignTaskTo(Model model, @PathVariable Long taskId, @PathVariable Long projectId) {
		Task task = this.taskService.getTask(taskId);
		Project project = this.projectService.getProject(projectId);
		
		if(task!=null && project!=null) {
			List<User> members = project.getMembers();
			model.addAttribute("task", task);
			model.addAttribute("members", members);
			return "assignToMember";
		}
		return "redirect:/project/" + projectId.toString();
	}
	
	/**
	 * This method is called when a POST request is sent by the user to URL "/task/{taskId}/assignTo/{userId}".
	 * This method collects the assign request from the user.
	 *
	 * @param model the Request model 
	 * @param taskId the identificative of the task
	 * @param userId the identificative of the user
	 * @return the name of the target view, that in this case is "/projects".
	 */
	@RequestMapping(value = {"task/{taskId}/assignTo/{userId}"}, method = RequestMethod.POST)
	public String confirmAssignTaskTo(Model model, @PathVariable Long taskId, @PathVariable Long userId) {
		Task task = this.taskService.getTask(taskId);
		User user = this.userService.getUser(userId);
		
		if(task!=null && user!=null) {
			task.setAssignedUser(user);
			user.getTasksToDo().add(task);
			
			this.taskService.saveTask(task);
			this.userService.saveUser(user); 
		}
		return "redirect:/projects";
	}
	
	/**
	 * This method is called when a GET request is sent by the user to URL "/task/{taskId}".
	 * This method prepares and dispatches the task view.
	 *
	 * @param model the Request model 
	 * @param taskId the identificative of the task
	 * @return the name of the target view, that in this case is "task".
	 */
	@RequestMapping(value = {"task/{taskId}"}, method = RequestMethod.GET)
	public String task(Model model, @PathVariable Long taskId) {
		Task task = this.taskService.getTask(taskId);
		
		if(task.equals(null)) {
			return "redirect:/projects";
		}
		model.addAttribute("task", task);
		
		return "task";
	}
	
	/**
	 * This method is called when a GET request is sent by the user to URL "/task/{taskId}/assignTag/{projectId}".
	 * This method prepares and dispatches the assign tag view.
	 *
	 * @param model the Request model 
	 * @param taskId the identificative of the task
	 * @param projectId the identificative of the project
	 * @return the name of the target view, that in this case is "assignTag", or redirects to "/projects/{projectId}"
	 */
	@RequestMapping(value = {"task/{taskId}/assignTag/{projectId}"}, method = RequestMethod.GET)
	public String assignTagTo(Model model, @PathVariable Long taskId, @PathVariable Long projectId) {
		Task task = this.taskService.getTask(taskId);
		Project project = this.projectService.getProject(projectId);
		
		if(task!=null && project!=null) {
			List<Tag> tags = project.getTags();
			model.addAttribute("task", task);
			model.addAttribute("tags", tags);
			return "assignTag";
		}
		return "redirect:/project/" + projectId.toString();
	}
	
	/**
	 * This method is called when a POST request is sent by the user to URL "/task/{taskId}/assignTo/{projectId}".
	 * This method prepares and dispatches the assign task view.
	 *
	 * @param model the Request model 
	 * @param taskId the identificative of the task
	 * @param projectId the identificative of the project
	 * @return the name of the target view, that in this case is "assignToMember", or redirects to "/projects/{projectId}"
	 */
	@RequestMapping(value = {"task/{taskId}/assignTag/{tagId}"}, method = RequestMethod.POST)
	public String confirmAssignTagTo(Model model, @PathVariable Long taskId, @PathVariable Long tagId) {
		Task task = this.taskService.getTask(taskId);
		Tag tag = this.tagService.getTag(tagId);
		
		if(task!=null && tag!=null) {
			task.getTags().add(tag);
			this.taskService.saveTask(task);
		}
		return "redirect:/projects/";
	}
	
	/**
	 * This method is called when a GET request is sent by the user to URL "/task/{taskId}/addComment/{projectId}".
	 * This method prepares and dispatches the add comment view.
	 *
	 * @param model the Request model 
	 * @param taskId the identificative of the task
	 * @param projectId the identificative of the project
	 * @return the name of the target view, that in this case is "assignTag", or redirects to "/projects/{projectId}"
	 */
	@RequestMapping(value = {"task/{taskId}/addComment/{projectId}"}, method = RequestMethod.GET)
	public String addcommentTo(Model model, @PathVariable Long taskId, @PathVariable Long projectId) {
		Task task = this.taskService.getTask(taskId);
		Project project = this.projectService.getProject(projectId);
		
		if(task!=null && project!=null) {
			User loggedUser = this.sessionData.getLoggedUser();
			//if the logged user is the owner of the project or has visibility of it
			if(loggedUser.equals(project.getOwner()) || project.getMembers().contains(loggedUser)) {
			model.addAttribute("task", task);
			model.addAttribute("comment", new String());
			return "addComment";
			}
		}
		return "redirect:/project/" + projectId.toString();
	}
	
	/**
	 * This method is called when a POST request is sent by the user to URL "/task/{taskId}/assignTo/{projectId}".
	 * This method prepares and dispatches the assign task view.
	 *
	 * @param model the Request model 
	 * @param taskId the identificative of the task
	 * @param projectId the identificative of the project
	 * @return the name of the target view, that in this case is "assignToMember", or redirects to "/projects/{projectId}"
	 */
	@RequestMapping(value = {"task/{taskId}/addcomment"}, method = RequestMethod.POST)
	public String confirmAddcommentTo(Model model, @PathVariable Long taskId, 
			@Valid @ModelAttribute("comment") String comment) {
		Task task = this.taskService.getTask(taskId);
		
		if(task!=null && comment!=null) {
			task.getComments().add(comment);
			this.taskService.saveTask(task);
		}
		return "redirect:/projects/";
	}
	

}

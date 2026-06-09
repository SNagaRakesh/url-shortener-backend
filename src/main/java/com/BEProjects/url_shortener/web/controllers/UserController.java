package com.BEProjects.url_shortener.web.controllers;

import com.BEProjects.url_shortener.domain.entities.User;
import com.BEProjects.url_shortener.domain.models.CreateUserCmd;
import com.BEProjects.url_shortener.domain.models.Role;
import com.BEProjects.url_shortener.domain.services.UserService;
import com.BEProjects.url_shortener.web.dtos.CreateUserForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    String loginForm() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model  model) {
        model.addAttribute("user", new CreateUserForm("", "", ""));
        return "register";
    }

    @PostMapping("/register")
    public String createUser(@ModelAttribute("user")
                             @Valid CreateUserForm form,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if(bindingResult.hasErrors()) {
            return "register";
        }
        try {
            var cmd = new CreateUserCmd(
                    form.email(),
                    form.password(),
                    form.name(),
                    Role.ROLE_USER);

            userService.createUser(cmd);
            redirectAttributes.addFlashAttribute("successMessage", "User has been created, please login");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating User: " + e.getMessage());
            return "redirect:/register";
        }
    }
}

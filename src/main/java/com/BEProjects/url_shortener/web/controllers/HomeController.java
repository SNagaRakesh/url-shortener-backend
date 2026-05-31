package com.BEProjects.url_shortener.web.controllers;

import com.BEProjects.url_shortener.ApplicationProperties;
import com.BEProjects.url_shortener.domain.models.CreateShortUrlCmd;
import com.BEProjects.url_shortener.domain.models.ShortUrlDto;
import com.BEProjects.url_shortener.domain.services.ShortUrlService;
import com.BEProjects.url_shortener.web.dtos.CreateShortUrlForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class HomeController {

    private final ShortUrlService shortUrlService;
    private final ApplicationProperties applicationProperties;

    public HomeController(ShortUrlService shortUrlService, ApplicationProperties applicationProperties) {
        this.shortUrlService = shortUrlService;
        this.applicationProperties = applicationProperties;
    }

    @GetMapping("/")
    public String home(Model model) {

        List<ShortUrlDto> shortUrls = shortUrlService.findAllPublicShortUrls();
        model.addAttribute("shortUrls", shortUrls);
        model.addAttribute("baseUrl", applicationProperties.baseUrl());
        model.addAttribute("createShortUrlForm", new CreateShortUrlForm(""));

        return "home";
    }

    @PostMapping("/short-urls")
    public String createShortUrl(@ModelAttribute("createShortUrlForm")
                                 @Valid CreateShortUrlForm form,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model){
        if (bindingResult.hasErrors()) {

            List<ShortUrlDto> shortUrls = shortUrlService.findAllPublicShortUrls();
            model.addAttribute("shortUrls", shortUrls);
            model.addAttribute("baseUrl", applicationProperties.baseUrl());

            return "home";
        }

        try {
            CreateShortUrlCmd cmd = new CreateShortUrlCmd(form.originalUrl());
            var shortUrlDto = shortUrlService.createShortUrl(cmd);
            redirectAttributes.addFlashAttribute("successMessage", "successfully created short URL. " +
                                                                                            applicationProperties.baseUrl() +
                                                                                            "/s/" +
                                                                                            shortUrlDto.shortKey());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "failed ot create short URL");
        }

        return "redirect:/";
    }
}
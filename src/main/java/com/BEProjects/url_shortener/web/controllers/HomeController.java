package com.BEProjects.url_shortener.web.controllers;

import com.BEProjects.url_shortener.ApplicationProperties;
import com.BEProjects.url_shortener.domain.exceptions.ShortUrlNotFoundException;
import com.BEProjects.url_shortener.domain.models.CreateShortUrlCmd;
import com.BEProjects.url_shortener.domain.models.PagedResult;
import com.BEProjects.url_shortener.domain.models.ShortUrlDto;
import com.BEProjects.url_shortener.domain.services.ShortUrlService;
import com.BEProjects.url_shortener.web.dtos.CreateShortUrlForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    private final ShortUrlService shortUrlService;
    private final ApplicationProperties applicationProperties;
    private final SecurityUtils securityUtils;

    public HomeController(ShortUrlService shortUrlService,
                          ApplicationProperties applicationProperties,
                          SecurityUtils securityUtils) {
        this.shortUrlService = shortUrlService;
        this.applicationProperties = applicationProperties;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/")
    public String home(
            @RequestParam(defaultValue = "1")
            Integer page,

            Model model) {

        this.addShortUrlsDataToModel(model, page);

        model.addAttribute("createShortUrlForm",
                new CreateShortUrlForm("", false, null));

        return "home";
    }

    private void addShortUrlsDataToModel(Model model, int pageNo) {
        PagedResult<ShortUrlDto> shortUrls = shortUrlService.findAllPublicShortUrls(pageNo, applicationProperties.pageSize());
        model.addAttribute("shortUrls", shortUrls);
        model.addAttribute("baseUrl", applicationProperties.baseUrl());
        model.addAttribute("paginationUrl", "/");
    }

    @GetMapping("/my-urls")
    public String showUserUrls(@RequestParam(defaultValue = "1") int page,
                               Model model) {
        var currUserId = securityUtils.getCurrentUserId();

        PagedResult<ShortUrlDto> myUrls = shortUrlService.getUserShortUrls(currUserId, page, applicationProperties.pageSize());
        model.addAttribute("shortUrls", myUrls);
        model.addAttribute("baseUrl", applicationProperties.baseUrl());
        model.addAttribute("paginationUrl", "/my-urls");
        return "my-urls";
    }

    @PostMapping("/delete-urls")
    public String deleteUrls(
            @RequestParam(value = "ids", required = false)
            List<Long> ids,
            RedirectAttributes redirectAttributes) {
        if(ids == null || ids.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "No URLs selected for deletion");
            return "redirect:/my-urls";
        }
        try {
            var userId = securityUtils.getCurrentUserId();
            shortUrlService.deleteUserShortUrls(ids, userId);
            redirectAttributes.addFlashAttribute("successMessage", "Selected URLs have been deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting URLs: " + e.getMessage());
        }
        return "redirect:/my-urls";
    }

    @PostMapping("/short-urls")
    public String createShortUrl(@ModelAttribute("createShortUrlForm")
                                 @Valid CreateShortUrlForm form,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model){
        if (bindingResult.hasErrors()) {

            this.addShortUrlsDataToModel(model, 1);

            return "home";
        }

        try {

            Long userId = securityUtils.getCurrentUserId();

            CreateShortUrlCmd cmd = new CreateShortUrlCmd(
                    form.originalUrl(),
                    form.isPrivate(),
                    form.expirationInDays(),
                    userId
            );
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

    @GetMapping("/s/{shortKey}")
    String redirectToOriginalUrl(@PathVariable String shortKey) {

        Long userId = securityUtils.getCurrentUserId();

        Optional<ShortUrlDto> shortUrlDtoOptional = shortUrlService.accessShortUrl(shortKey, userId);

        if(shortUrlDtoOptional.isEmpty()) {
            throw new ShortUrlNotFoundException("Invalid short key: " +  shortKey);
        }

        ShortUrlDto shortUrlDto = shortUrlDtoOptional.get();

        return "redirect:" + shortUrlDto.originalUrl();
    }
}
package com.smartbake.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ContactController {

    @Value("${app.contact-phone}")
    private String contactPhone;

    @Value("${app.whatsapp-link}")
    private String whatsappLink;

    @GetMapping("/contact")
    public String showContact(Model model) {
        model.addAttribute("contactPhone", contactPhone);
        model.addAttribute("whatsappLink", whatsappLink);
        return "contact";  // will look for templates/contact.html
    }
}
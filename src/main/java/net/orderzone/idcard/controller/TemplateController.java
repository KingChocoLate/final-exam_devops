package net.orderzone.idcard.controller;

import lombok.RequiredArgsConstructor;
import net.orderzone.idcard.model.Template;
import net.orderzone.idcard.service.TemplateService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("templates", templateService.getAll());
        return "template/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("template", new Template());
        return "template/form";
    }

    @PostMapping
    public String create(@ModelAttribute Template template) {
        templateService.create(template);
        return "redirect:/templates";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("template", templateService.getById(id)
                .orElseThrow(() -> new RuntimeException("Not found")));
        return "template/form";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id, @ModelAttribute Template template) {
        templateService.update(id, template);
        return "redirect:/templates";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        templateService.delete(id);
        return "redirect:/templates";
    }
}
package net.orderzone.idcard.service;

import lombok.RequiredArgsConstructor;
import net.orderzone.idcard.model.Template;
import net.orderzone.idcard.repository.TemplateRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;

    public List<Template> getAll() { return templateRepository.findAll(); }

    public Optional<Template> getById(Long id) { return templateRepository.findById(id); }

    public Optional<Template> getByCode(String code) { return templateRepository.findByCode(code); }

    public Template create(Template template) {
        if (templateRepository.existsByCode(template.getCode()))
            throw new RuntimeException("Template code already exists");
        return templateRepository.save(template);
    }

    public Template update(Long id, Template updated) {
        Template existing = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        existing.setName(updated.getName());
        existing.setCode(updated.getCode());
        existing.setOrganizationName(updated.getOrganizationName());
        existing.setLayout(updated.getLayout());
        existing.setPrimaryColor(updated.getPrimaryColor());
        existing.setSecondaryColor(updated.getSecondaryColor());
        existing.setTextColor(updated.getTextColor());
        existing.setTagline(updated.getTagline());
        return templateRepository.save(existing);
    }

    public void delete(Long id) { templateRepository.deleteById(id); }
}
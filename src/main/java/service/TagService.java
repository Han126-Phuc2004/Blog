package service;

import entity.Tag;
import org.springframework.stereotype.Service;
import repository.TagRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TagService {
    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public Tag save(Tag tag) {
        return tagRepository.save(tag);
    }

    public List<Tag> findAll() {
        return tagRepository.findAll();
    }

    public Tag findById(Integer id) {
        return tagRepository.findById(id).orElse(null);
    }

    public Tag findByName(String name) {
        return tagRepository.findByTagName(name).orElse(null);
    }

    public void deleteById(Integer id) {
        tagRepository.deleteById(id);
    }
}

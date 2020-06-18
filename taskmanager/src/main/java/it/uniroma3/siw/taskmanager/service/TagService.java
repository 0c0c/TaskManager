package it.uniroma3.siw.taskmanager.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.taskmanager.model.Tag;
import it.uniroma3.siw.taskmanager.repository.TagRepository;

@Service
public class TagService {
	
	@Autowired
	TagRepository tagRepository;
	
	@Transactional
	public Tag saveTag(Tag tag) {
		return this.tagRepository.save(tag);
	}
	
	@Transactional
	public Tag getTag(Long id) {
		Optional<Tag> tag = this.tagRepository.findById(id);
		return tag.orElse(null);
	}

}

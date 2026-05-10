package com.henrique.stickermarker.service;

import com.henrique.stickermarker.model.Collection;
import com.henrique.stickermarker.repository.CollectionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CollectionService {

    private final CollectionRepository collectionRepository;

    public CollectionService(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    public Collection createCollection(Collection collection) {
        return collectionRepository.save(collection);
    }

    public List<Collection> getAllCollections() {
        return collectionRepository.findAll();
    }

    public Collection getByName(String name) {
        return collectionRepository.findByName(name);
    }

    public Collection getById(Long id) {
        return collectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collection not found"));
    }
}

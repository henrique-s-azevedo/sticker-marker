package com.henrique.stickermarker.controller;

import com.henrique.stickermarker.model.Collection;
import com.henrique.stickermarker.service.CollectionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/collections")
public class CollectionController {

    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @PostMapping
    public Collection create(@RequestBody Collection collection) {
        return collectionService.createCollection(collection);
    }

    @GetMapping
    public List<Collection> getAll() {
        return collectionService.getAllCollections();
    }

    @GetMapping("/{id}")
    public Collection getById(@PathVariable Long id) {
        return collectionService.getById(id);
    }
}

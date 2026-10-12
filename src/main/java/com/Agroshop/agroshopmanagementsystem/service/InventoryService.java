package com.Agroshop.agroshopmanagementsystem.service;

import com.Agroshop.agroshopmanagementsystem.entity.Inventory;
import com.Agroshop.agroshopmanagementsystem.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    public Inventory saveOrUpdateInventory(Inventory inventory) {
        return inventoryRepository.findByProductId(inventory.getProductId())
                .map(inv -> {
                    inv.setQuantity(inventory.getQuantity());
                    return inventoryRepository.save(inv);
                })
                .orElseGet(() -> inventoryRepository.save(inventory));
    }

    public Inventory updateInventory(Long id, Inventory inventoryDetails) {
        Inventory inv = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory item not found"));
        inv.setQuantity(inventoryDetails.getQuantity());
        return inventoryRepository.save(inv);
    }
}
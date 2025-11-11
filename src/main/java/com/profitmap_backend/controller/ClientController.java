package com.profitmap_backend.controller;

import com.profitmap_backend.dto.ClientDto;
import com.profitmap_backend.model.ClientType;
import com.profitmap_backend.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {
    
    private final ClientService clientService;
    
    @PostMapping
    public ResponseEntity<ClientDto> createClient(@RequestBody ClientDto clientDto) {
        ClientDto createdClient = clientService.createClient(clientDto);
        return new ResponseEntity<>(createdClient, HttpStatus.CREATED);
    }
    
    @GetMapping
    public ResponseEntity<List<ClientDto>> getAllClients() {
        List<ClientDto> clients = clientService.getAllClients();
        return ResponseEntity.ok(clients);
    }
    
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<ClientDto>> getClientsByCompanyId(@PathVariable Long companyId) {
        List<ClientDto> clients = clientService.getClientsByCompanyId(companyId);
        return ResponseEntity.ok(clients);
    }
    
    @GetMapping("/type/{clientType}")
    public ResponseEntity<List<ClientDto>> getClientsByType(@PathVariable ClientType clientType) {
        List<ClientDto> clients = clientService.getClientsByType(clientType);
        return ResponseEntity.ok(clients);
    }
    
    @GetMapping("/company/{companyId}/type/{clientType}")
    public ResponseEntity<List<ClientDto>> getClientsByCompanyIdAndType(
            @PathVariable Long companyId, 
            @PathVariable ClientType clientType) {
        List<ClientDto> clients = clientService.getClientsByCompanyIdAndType(companyId, clientType);
        return ResponseEntity.ok(clients);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ClientDto> getClientById(@PathVariable Long id) {
        ClientDto client = clientService.getClientById(id);
        return ResponseEntity.ok(client);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ClientDto> updateClient(@PathVariable Long id, @RequestBody ClientDto clientDto) {
        ClientDto updatedClient = clientService.updateClient(id, clientDto);
        return ResponseEntity.ok(updatedClient);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }
}

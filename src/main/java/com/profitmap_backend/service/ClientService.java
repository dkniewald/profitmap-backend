package com.profitmap_backend.service;

import com.profitmap_backend.dto.ClientDto;
import com.profitmap_backend.model.Client;
import com.profitmap_backend.model.ClientCompany;
import com.profitmap_backend.model.ClientPerson;
import com.profitmap_backend.model.ClientType;
import com.profitmap_backend.repository.ClientCompanyRepository;
import com.profitmap_backend.repository.ClientPersonRepository;
import com.profitmap_backend.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {
    
    private final ClientRepository clientRepository;
    private final ClientCompanyRepository clientCompanyRepository;
    private final ClientPersonRepository clientPersonRepository;
    
    @Transactional
    public ClientDto createClient(ClientDto clientDto) {
        // Create the main client entity
        Client client = Client.builder()
                .companyId(clientDto.getCompanyId())
                .clientType(clientDto.getClientType())
                .name(clientDto.getName())
                .contact(clientDto.getContact())
                .email(clientDto.getEmail())
                .build();
        
        client = clientRepository.save(client);
        
        // Create the specific client type entity
        if (clientDto.getClientType() == ClientType.COMPANY) {
            ClientCompany clientCompany = ClientCompany.builder()
                    .client(client)
                    .oib(clientDto.getOib())
                    .address(clientDto.getAddress())
                    .build();
            clientCompanyRepository.save(clientCompany);
        } else if (clientDto.getClientType() == ClientType.PERSON) {
            ClientPerson clientPerson = ClientPerson.builder()
                    .client(client)
                    .surname(clientDto.getSurname())
                    .build();
            clientPersonRepository.save(clientPerson);
        }
        
        return convertToDto(client);
    }
    
    public List<ClientDto> getAllClients() {
        return clientRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<ClientDto> getClientsByCompanyId(Long companyId) {
        return clientRepository.findByCompanyId(companyId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<ClientDto> getClientsByType(ClientType clientType) {
        return clientRepository.findByClientType(clientType).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<ClientDto> getClientsByCompanyIdAndType(Long companyId, ClientType clientType) {
        return clientRepository.findByCompanyIdAndClientType(companyId, clientType).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public ClientDto getClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + id));
        return convertToDto(client);
    }
    
    @Transactional
    public ClientDto updateClient(Long id, ClientDto clientDto) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + id));
        
        // Update basic client fields
        client.setName(clientDto.getName());
        client.setContact(clientDto.getContact());
        client.setEmail(clientDto.getEmail());
        
        client = clientRepository.save(client);
        
        // Update specific client type fields
        if (clientDto.getClientType() == ClientType.COMPANY) {
            ClientCompany clientCompany = clientCompanyRepository.findByClientId(id);
            if (clientCompany != null) {
                clientCompany.setOib(clientDto.getOib());
                clientCompany.setAddress(clientDto.getAddress());
                clientCompanyRepository.save(clientCompany);
            }
        } else if (clientDto.getClientType() == ClientType.PERSON) {
            ClientPerson clientPerson = clientPersonRepository.findByClientId(id);
            if (clientPerson != null) {
                clientPerson.setSurname(clientDto.getSurname());
                clientPersonRepository.save(clientPerson);
            }
        }
        
        return convertToDto(client);
    }
    
    @Transactional
    public void deleteClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + id));
        
        // Delete specific client type entity first
        if (client.getClientType() == ClientType.COMPANY) {
            ClientCompany clientCompany = clientCompanyRepository.findByClientId(id);
            if (clientCompany != null) {
                clientCompanyRepository.delete(clientCompany);
            }
        } else if (client.getClientType() == ClientType.PERSON) {
            ClientPerson clientPerson = clientPersonRepository.findByClientId(id);
            if (clientPerson != null) {
                clientPersonRepository.delete(clientPerson);
            }
        }
        
        // Delete the main client entity
        clientRepository.delete(client);
    }
    
    private ClientDto convertToDto(Client client) {
        ClientDto.ClientDtoBuilder builder = ClientDto.builder()
                .id(client.getId())
                .companyId(client.getCompanyId())
                .clientType(client.getClientType())
                .name(client.getName())
                .contact(client.getContact())
                .email(client.getEmail())
                .createdAt(client.getCreatedAt());
        
        // Add specific fields based on client type
        if (client.getClientType() == ClientType.COMPANY && client.getClientCompany() != null) {
            builder.oib(client.getClientCompany().getOib())
                   .address(client.getClientCompany().getAddress());
        } else if (client.getClientType() == ClientType.PERSON && client.getClientPerson() != null) {
            builder.surname(client.getClientPerson().getSurname());
        }
        
        return builder.build();
    }
}

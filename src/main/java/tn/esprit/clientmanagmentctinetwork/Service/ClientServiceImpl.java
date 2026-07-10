package tn.esprit.clientmanagmentctinetwork.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.clientmanagmentctinetwork.Dto.ClientDto;
import tn.esprit.clientmanagmentctinetwork.Model.ClientModel;
import tn.esprit.clientmanagmentctinetwork.Model.ClientPhone;
import tn.esprit.clientmanagmentctinetwork.Repository.ClientPhoneRepository;
import tn.esprit.clientmanagmentctinetwork.Repository.ClientRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientPhoneRepository clientPhoneRepository;

    public ClientServiceImpl(ClientRepository clientRepository, ClientPhoneRepository clientPhoneRepository) {
        this.clientRepository = clientRepository;
        this.clientPhoneRepository = clientPhoneRepository;
    }

    @Override
    public List<ClientModel> getAllClients() {
        return clientRepository.findAll();
    }

    @Override
    public ClientModel saveClient(ClientDto clientDto) {
        ClientModel client = new ClientModel();
        client.setName(clientDto.getName());
        client.setEmail(clientDto.getEmail());
        client.setDescription(clientDto.getDescription());

        for (String num : clientDto.getPhones()) {
            if (num != null && !num.trim().isEmpty()) {
                client.addPhone(new ClientPhone(num.trim()));
            }
        }
        return clientRepository.save(client);
    }

    @Override
    public ClientModel updateClient(String existingPhone, ClientDto clientDto) {
        ClientModel client = clientRepository.findByPhoneNumber(existingPhone)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        client.setName(clientDto.getName());
        client.setEmail(clientDto.getEmail());
        client.setDescription(clientDto.getDescription());

        // 1. Clear the old collection and flush immediately to delete old phone records
        client.getPhones().clear();
        clientRepository.saveAndFlush(client); // This ensures the deletes execute first!

        // 2. Add the updated list of phone numbers
        for (String num : clientDto.getPhones()) {
            if (num != null && !num.trim().isEmpty()) {
                client.addPhone(new ClientPhone(num.trim()));
            }
        }

        // 3. Save the client with the new phone records
        return clientRepository.save(client);
    }

    @Override
    public Optional<ClientModel> findByPhoneNumber(String phoneNumber) {
        return clientRepository.findByPhoneNumber(phoneNumber);
    }

    @Override
    public void deleteClientByPhoneNumber(String phoneNumber) {
        ClientModel client = clientRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));
        clientRepository.delete(client);
    }

    @Override
    public ClientDto convertToDto(ClientModel client) {
        ClientDto dto = new ClientDto();
        dto.setId(client.getId());
        dto.setName(client.getName());
        dto.setEmail(client.getEmail());
        dto.setDescription(client.getDescription());
        dto.setPhones(client.getPhones().stream()
                .map(ClientPhone::getPhoneNumber)
                .collect(Collectors.toList()));
        return dto;
    }

    @Override
    public boolean isPhoneUniqueExcludingClient(String phoneNumber, Long clientId) {
        Optional<ClientPhone> existing = clientPhoneRepository.findByPhoneNumber(phoneNumber);
        if (existing.isEmpty()) {
            return true;
        }
        if (clientId == null) {
            return false;
        }
        return existing.get().getClient().getId().equals(clientId);
    }

    @Override
    public List<ClientModel> searchClients(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return clientRepository.searchClients(keyword.trim());
        }
        return getAllClients();
    }
}
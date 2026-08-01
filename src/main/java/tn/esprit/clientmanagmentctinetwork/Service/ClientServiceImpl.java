package tn.esprit.clientmanagmentctinetwork.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.clientmanagmentctinetwork.Dto.ClientDto;
import tn.esprit.clientmanagmentctinetwork.Model.ClientModel;
import tn.esprit.clientmanagmentctinetwork.Model.ClientPhone;
import tn.esprit.clientmanagmentctinetwork.Model.SectorModel;
import tn.esprit.clientmanagmentctinetwork.Repository.ClientPhoneRepository;
import tn.esprit.clientmanagmentctinetwork.Repository.ClientRepository;
import tn.esprit.clientmanagmentctinetwork.Repository.SectorRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientPhoneRepository clientPhoneRepository;
    private final SectorRepository sectorRepository;

    public ClientServiceImpl(ClientRepository clientRepository,
                             ClientPhoneRepository clientPhoneRepository,
                             SectorRepository sectorRepository) {
        this.clientRepository = clientRepository;
        this.clientPhoneRepository = clientPhoneRepository;
        this.sectorRepository = sectorRepository;
    }

    @Override
    public List<ClientModel> getAllClients() {
        return clientRepository.findAll();
    }

    // Restores the missing searchClients method used by the search bar
    @Override
    public List<ClientModel> searchClients(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return clientRepository.searchClients(keyword.trim());
        }
        return getAllClients();
    }

    @Override
    public ClientModel saveClient(ClientDto clientDto) {
        ClientModel client = new ClientModel();

        // Auto-generate sequential Client Code based on the maximum database ID [1.1.2]
        Long maxId = clientRepository.findMaxId();
        String generatedCode = "CL" + String.format("%04d", maxId + 1); // e.g. "CL0001", "CL0002" [1.1.2]
        client.setClientCode(generatedCode);

        client.setName(clientDto.getName());
        client.setEmail(clientDto.getEmail());
        client.setDescription(clientDto.getDescription());

        // Map optional profile fields [1.2.6]
        client.setAddress(clientDto.getAddress());
        client.setCity(clientDto.getCity());
        client.setContact(clientDto.getContact());
        client.setWebsite(clientDto.getWebsite());

        // Map sector reference if present [1.2.6]
        if (clientDto.getSectorId() != null) {
            SectorModel sector = sectorRepository.findById(clientDto.getSectorId()).orElse(null);
            client.setSector(sector);
        }

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

        // Update optional profile fields [1.2.6]
        client.setAddress(clientDto.getAddress());
        client.setCity(clientDto.getCity());
        client.setContact(clientDto.getContact());
        client.setWebsite(clientDto.getWebsite());

        // Update sector reference if present [1.2.6]
        if (clientDto.getSectorId() != null) {
            SectorModel sector = sectorRepository.findById(clientDto.getSectorId()).orElse(null);
            client.setSector(sector);
        } else {
            client.setSector(null);
        }

        // Safe phone re-write: delete old phone records and flush first
        client.getPhones().clear();
        clientRepository.saveAndFlush(client);

        for (String num : clientDto.getPhones()) {
            if (num != null && !num.trim().isEmpty()) {
                client.addPhone(new ClientPhone(num.trim()));
            }
        }

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

        // Map optional fields back to DTO for the edit form [1.2.6]
        dto.setAddress(client.getAddress());
        dto.setCity(client.getCity());
        dto.setContact(client.getContact());
        dto.setWebsite(client.getWebsite());
        if (client.getSector() != null) {
            dto.setSectorId(client.getSector().getId());
        }

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
}
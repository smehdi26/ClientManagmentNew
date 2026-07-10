package tn.esprit.clientmanagmentctinetwork.Service;

import tn.esprit.clientmanagmentctinetwork.Dto.ClientDto;
import tn.esprit.clientmanagmentctinetwork.Model.ClientModel;
import java.util.List;
import java.util.Optional;

public interface ClientService {
    List<ClientModel> getAllClients();
    ClientModel saveClient(ClientDto clientDto);
    ClientModel updateClient(String existingPhone, ClientDto clientDto);
    Optional<ClientModel> findByPhoneNumber(String phoneNumber);
    void deleteClientByPhoneNumber(String phoneNumber);
    ClientDto convertToDto(ClientModel client);
    boolean isPhoneUniqueExcludingClient(String phoneNumber, Long clientId);
    List<ClientModel> searchClients(String keyword);
}
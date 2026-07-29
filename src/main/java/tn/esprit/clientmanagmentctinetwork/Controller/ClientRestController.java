package tn.esprit.clientmanagmentctinetwork.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.clientmanagmentctinetwork.Dto.ClientDto;
import tn.esprit.clientmanagmentctinetwork.Model.ClientModel;
import tn.esprit.clientmanagmentctinetwork.Model.ReservationModel;
import tn.esprit.clientmanagmentctinetwork.Service.ClientService;
import tn.esprit.clientmanagmentctinetwork.Service.ReservationService;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientRestController {

    private final ClientService clientService;
    private final ReservationService reservationService;

    public ClientRestController(ClientService clientService, ReservationService reservationService) {
        this.clientService = clientService;
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ClientModel>> getAllClients() {
        return ResponseEntity.ok(clientService.getAllClients());
    }

    @GetMapping("/{phoneNumber}")
    public ResponseEntity<ClientModel> getClientByPhone(@PathVariable String phoneNumber) {
        return clientService.findByPhoneNumber(phoneNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ClientModel> createClient(@RequestBody ClientDto clientDto) {
        ClientModel saved = clientService.saveClient(clientDto);
        return ResponseEntity.ok(saved);
    }

    // PUT Update mapping
    @PutMapping("/{existingPhone}")
    public ResponseEntity<ClientModel> updateClient(@PathVariable String existingPhone, @RequestBody ClientDto clientDto) {
        ClientModel updated = clientService.updateClient(existingPhone, clientDto);
        return ResponseEntity.ok(updated);
    }

    // GET Client specific reservations history log
    @GetMapping("/{phoneNumber}/reservations")
    public ResponseEntity<List<ReservationModel>> getClientReservations(@PathVariable String phoneNumber) {
        ClientModel client = clientService.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));
        return ResponseEntity.ok(reservationService.getReservationsByClientId(client.getId()));
    }

    @DeleteMapping("/{phoneNumber}")
    public ResponseEntity<Void> deleteClient(@PathVariable String phoneNumber) {
        clientService.deleteClientByPhoneNumber(phoneNumber);
        return ResponseEntity.noContent().build();
    }
}
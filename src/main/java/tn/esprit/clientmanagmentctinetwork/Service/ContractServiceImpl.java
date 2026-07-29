package tn.esprit.clientmanagmentctinetwork.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.clientmanagmentctinetwork.Dto.ContractDto;
import tn.esprit.clientmanagmentctinetwork.Model.ClientModel;
import tn.esprit.clientmanagmentctinetwork.Model.ContractModel;
import tn.esprit.clientmanagmentctinetwork.Repository.ClientRepository;
import tn.esprit.clientmanagmentctinetwork.Repository.ContractRepository;

import java.util.List;

@Service
@Transactional
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final ClientRepository clientRepository;
    private final NotificationService notificationService;

    public ContractServiceImpl(ContractRepository contractRepository,
                               ClientRepository clientRepository,
                               NotificationService notificationService) {
        this.contractRepository = contractRepository;
        this.clientRepository = clientRepository;
        this.notificationService = notificationService;
    }

    @Override
    public ContractModel createContract(ContractDto dto) {
        ClientModel client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        ContractModel contract = new ContractModel();
        contract.setName(dto.getName());
        contract.setRedevance(dto.getRedevance());
        contract.setDateSignature(dto.getDateSignature());
        contract.setMonthsOfVisits(dto.getMonthsOfVisits());
        contract.setClient(client);

        // Auto-calculate Number of Visits (N.D.V) based on Redevance
        int visits = 0;
        String redevanceUpper = dto.getRedevance().toUpperCase();
        if ("ANNUELLE".equals(redevanceUpper)) {
            visits = 6;
        } else if ("SEMESTRIELLE".equals(redevanceUpper)) {
            visits = 2;
        } else if ("TRIMESTRIELLE".equals(redevanceUpper)) {
            visits = 4;
        }
        contract.setNumberOfVisits(visits);

        ContractModel saved = contractRepository.save(contract);

        // Log to Notification center
        notificationService.createNotification(
                "New maintenance contract '" + saved.getName() + "' registered for client " + client.getName() + " with " + visits + " annual visits.",
                "SUCCESS"
        );

        return saved;
    }

    @Override
    public List<ContractModel> getContractsByClientId(Long clientId) {
        return contractRepository.findByClientId(clientId);
    }

    @Override
    public void deleteContract(Long id) {
        ContractModel contract = contractRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));
        contractRepository.delete(contract);

        // Log termination
        notificationService.createNotification(
                "Maintenance contract '" + contract.getName() + "' for client " + contract.getClient().getName() + " has been terminated.",
                "DANGER"
        );
    }

    @Override
    public List<ContractModel> searchAndFilterContracts(String keyword, String redevance) {
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String cleanRedevance = (redevance != null && !redevance.trim().isEmpty()) ? redevance.trim() : null;

        if (cleanKeyword == null && cleanRedevance == null) {
            return contractRepository.findAll();
        }
        return contractRepository.searchAndFilterContracts(cleanKeyword, cleanRedevance);
    }

    @Override
    public ContractModel updateContractSchedule(Long id, String months) {
        ContractModel contract = contractRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));
        contract.setMonthsOfVisits(months);

        ContractModel saved = contractRepository.save(contract);

        // Log notification
        notificationService.createNotification(
                "Scheduled visit months for maintenance contract '" + saved.getName() + "' updated to: " + months + ".",
                "INFO"
        );

        return saved;
    }
}
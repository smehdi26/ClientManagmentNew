package tn.esprit.clientmanagmentctinetwork.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.clientmanagmentctinetwork.Dto.ContractDto;
import tn.esprit.clientmanagmentctinetwork.Model.ClientModel;
import tn.esprit.clientmanagmentctinetwork.Model.ContractModel;
import tn.esprit.clientmanagmentctinetwork.Model.NotificationModel;
import tn.esprit.clientmanagmentctinetwork.Repository.ClientRepository;
import tn.esprit.clientmanagmentctinetwork.Repository.ContractRepository;
import tn.esprit.clientmanagmentctinetwork.Repository.NotificationRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final ClientRepository clientRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    public ContractServiceImpl(ContractRepository contractRepository,
                               ClientRepository clientRepository,
                               NotificationService notificationService,
                               NotificationRepository notificationRepository) {
        this.contractRepository = contractRepository;
        this.clientRepository = clientRepository;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public ContractModel createContract(ContractDto dto) {
        ClientModel client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        ContractModel contract = new ContractModel();
        contract.setName(dto.getName());
        contract.setRedevance(dto.getRedevance());
        contract.setDateSignature(dto.getDateSignature());
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

        // Log notification to Notification Center
        notificationService.createNotification(
                "New maintenance contract '" + saved.getName() + "' registered for client " + client.getName() + " with " + visits + " annual visits.",
                "SUCCESS",
                "CONTRACT"
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

        // Log termination to Notification Center
        notificationService.createNotification(
                "Maintenance contract '" + contract.getName() + "' for client " + contract.getClient().getName() + " has been terminated.",
                "DANGER",
                "CONTRACT"
        );
    }

    @Override
    public List<ContractModel> getMonthlySchedules(int month, int year) {
        return contractRepository.findByVisitMonthAndYear(month, year);
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
    public ContractModel updateContract(Long id, ContractDto dto) {
        ContractModel contract = getContractById(id);
        contract.setName(dto.getName());
        contract.setRedevance(dto.getRedevance());
        contract.setDateSignature(dto.getDateSignature());

        // Re-calculate visits count in case the redevance has been updated
        int visits = 0;
        String redevanceUpper = dto.getRedevance().toUpperCase();
        if ("ANNUELLE".equals(redevanceUpper)) visits = 6;
        else if ("SEMESTRIELLE".equals(redevanceUpper)) visits = 2;
        else if ("TRIMESTRIELLE".equals(redevanceUpper)) visits = 4;
        contract.setNumberOfVisits(visits);

        return contractRepository.save(contract);
    }

    @Override
    public ContractModel updateStatus(Long id, String status) {
        ContractModel contract = getContractById(id);
        contract.setStatus(status);

        notificationService.createNotification(
                "Contract '" + contract.getName() + "' status manually set to " + status + ".",
                "INFO",
                "CONTRACT"
        );
        return contractRepository.save(contract);
    }

    @Override
    public ContractModel renewContract(Long id) {
        ContractModel contract = getContractById(id);

        LocalDate originalSignature = contract.getDateSignature();
        if (originalSignature != null) {
            // Add exactly 1 year to extend the contract [1.1.4]
            contract.setDateSignature(originalSignature.plusYears(1));
        }

        // Clear physical date columns so the admin can schedule visits for the new year [1.1.4]
        contract.setVisitDate1(null);
        contract.setVisitDate2(null);
        contract.setVisitDate3(null);
        contract.setVisitDate4(null);
        contract.setVisitDate5(null);
        contract.setVisitDate6(null);

        notificationService.createNotification(
                "Contract '" + contract.getName() + "' has been successfully RENEWED/EXTENDED to " + contract.getDateSignature() + ".",
                "SUCCESS",
                "CONTRACT"
        );

        return contractRepository.save(contract);
    }

    @Override
    public ContractModel updateContractScheduleDates(Long id, List<LocalDate> dates) {
        ContractModel contract = getContractById(id);

        contract.setVisitDate1(dates.size() > 0 ? dates.get(0) : null);
        contract.setVisitDate2(dates.size() > 1 ? dates.get(1) : null);
        contract.setVisitDate3(dates.size() > 2 ? dates.get(2) : null);
        contract.setVisitDate4(dates.size() > 3 ? dates.get(3) : null);
        contract.setVisitDate5(dates.size() > 4 ? dates.get(4) : null);
        contract.setVisitDate6(dates.size() > 5 ? dates.get(5) : null);

        ContractModel saved = contractRepository.save(contract);

        // Log schedule update to Notification Center
        notificationService.createNotification(
                "Scheduled visit dates for contract '" + saved.getName() + "' updated. Active months: " + saved.getMonthsOfVisits() + ".",
                "INFO",
                "CONTRACT"
        );

        return saved;
    }

    @Override
    public void checkContractNotifications() {
        List<ContractModel> contracts = contractRepository.findAll();
        LocalDate now = LocalDate.now();

        for (ContractModel c : contracts) {
            LocalDate signature = c.getDateSignature();
            if (signature == null || now.isBefore(signature)) {
                continue; // Contract has not started yet
            }

            int totalVisits = c.getNumberOfVisits();
            if (totalVisits == 0) {
                continue;
            }

            int t = 2; // Default for ANNUELLE
            String redevanceUpper = c.getRedevance().toUpperCase();
            if ("SEMESTRIELLE".equals(redevanceUpper)) {
                t = 6;
            } else if ("TRIMESTRIELLE".equals(redevanceUpper)) {
                t = 3;
            }

            long monthsElapsed = java.time.temporal.ChronoUnit.MONTHS.between(signature, now);
            int currentPeriod = (int) (monthsElapsed / t) + 1;

            String monthsStr = c.getMonthsOfVisits();
            String[] filledMonths = (monthsStr == null || monthsStr.trim().isEmpty()) ? new String[0] : monthsStr.split(", ");
            int filledCount = filledMonths.length;

            // Rule 1: Contract has started - Period 1 Notification
            if (currentPeriod == 1 && filledCount == 0) {
                String trigger = "CONTRACT_START_" + c.getId();
                saveContractNotification(trigger,
                        "Contract '" + c.getName() + "' has started. You are in the period of the first scheduled visit. Please contact client " + c.getClient().getName() + " (" + c.getClient().getPrimaryPhoneNumber() + ").",
                        "INFO", c.getId());
            }

            // Rule 2: 1 Month before period ends, and the current period slot has not been scheduled yet
            if (currentPeriod <= totalVisits) {
                LocalDate periodEnd = signature.plusMonths(currentPeriod * t);
                LocalDate warningStart = periodEnd.minusMonths(1);

                if ((now.isAfter(warningStart) || now.isEqual(warningStart)) && now.isBefore(periodEnd)) {
                    if (filledCount < currentPeriod) {
                        String trigger = "CONTRACT_WARNING_PERIOD_" + currentPeriod + "_" + c.getId();
                        saveContractNotification(trigger,
                                "Reminder: Maintenance contract '" + c.getName() + "' (Period " + currentPeriod + ") ends on " + periodEnd + ". Please visit client " + c.getClient().getName() + " and register this visit month.",
                                "WARNING", c.getId());
                    }
                }
            }

            // Rule 3: All visits are fully configured and completed
            if (filledCount == totalVisits) {
                String trigger = "CONTRACT_DONE_" + c.getId();
                saveContractNotification(trigger,
                        "Success: All " + totalVisits + " scheduled visits for maintenance contract '" + c.getName() + "' have been fully completed.",
                        "SUCCESS", c.getId());
            }

            // Rule 4: Missed period - current period exceeds filled count
            if (currentPeriod > 1 && currentPeriod <= totalVisits) {
                if (filledCount < currentPeriod - 1) {
                    String trigger = "CONTRACT_MISSED_PERIOD_" + currentPeriod + "_" + c.getId();
                    saveContractNotification(trigger,
                            "Urgent: A scheduled visit period was missed for contract '" + c.getName() + "' of client " + c.getClient().getName() + ". Please schedule a visit immediately.",
                            "DANGER", c.getId());
                }
            }
        }
    }

    @Override
    public ContractModel getContractById(Long id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contract record not found."));
    }

    private void saveContractNotification(String triggerKey, String message, String type, Long contractId) {
        boolean exists = notificationRepository.existsByTriggerKey(triggerKey);

        if (!exists) {
            NotificationModel notification = new NotificationModel();
            notification.setTriggerKey(triggerKey);
            notification.setMessage(message);
            notification.setType(type);
            notification.setContractId(contractId);
            notification.setCreatedAt(java.time.LocalDateTime.now());
            notification.setReadStatus(false);
            notification.setCategory("CONTRACT"); // Tag dynamically as CONTRACT

            notificationRepository.save(notification);
        }
    }
}
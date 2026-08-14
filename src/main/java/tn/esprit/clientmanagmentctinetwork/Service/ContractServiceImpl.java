package tn.esprit.clientmanagmentctinetwork.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.clientmanagmentctinetwork.Dto.ContractDto;
import tn.esprit.clientmanagmentctinetwork.Dto.VisitScheduleDto;
import tn.esprit.clientmanagmentctinetwork.Dto.VisitValidationDto;
import tn.esprit.clientmanagmentctinetwork.Model.ClientModel;
import tn.esprit.clientmanagmentctinetwork.Model.ContractModel;
import tn.esprit.clientmanagmentctinetwork.Model.ContractHistoryModel;
import tn.esprit.clientmanagmentctinetwork.Model.NotificationModel;
import tn.esprit.clientmanagmentctinetwork.Repository.ClientRepository;
import tn.esprit.clientmanagmentctinetwork.Repository.ContractRepository;
import tn.esprit.clientmanagmentctinetwork.Repository.NotificationRepository;

import java.time.LocalDate;
import java.util.ArrayList;
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

        String primaryPhone = client.getPrimaryPhoneNumber();

        // Log notification to Notification Center with 10-parameter signature [1.2.1, 1.2.6]
        notificationService.createDetailedNotification(
                "Nouveau contrat",
                "New maintenance contract '" + saved.getName() + "' registered for client " + client.getName() + " with " + visits + " annual visits.",
                "SUCCESS", "CONTRACT", "SAFE", "GREEN", "LOW",
                "CONTRACT_CREATION_" + saved.getId(), saved.getId(), primaryPhone
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

        String primaryPhone = contract.getClient() != null ? contract.getClient().getPrimaryPhoneNumber() : null;

        // Log termination to Notification Center with 10-parameter signature [1.2.1, 1.2.6]
        notificationService.createDetailedNotification(
                "Contrat résilié",
                "Maintenance contract '" + contract.getName() + "' for client " + contract.getClient().getName() + " has been terminated.",
                "DANGER", "CONTRACT", "SAFE", "GREEN", "LOW",
                "CONTRACT_TERMINATION_" + contract.getId(), contract.getId(), primaryPhone
        );
    }

    @Override
    public List<ContractModel> getMonthlySchedules(int month, int year) {
        return contractRepository.findByVisitMonthAndYear(month, year);
    }

    @Override
    public List<ContractModel> searchAndFilterContracts(String keyword, String redevance, String status) {
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String cleanRedevance = (redevance != null && !redevance.trim().isEmpty()) ? redevance.trim() : null;
        String cleanStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;

        if (cleanKeyword == null && cleanRedevance == null && cleanStatus == null) {
            return contractRepository.findAll();
        }
        return contractRepository.searchAndFilterContracts(cleanKeyword, cleanRedevance, cleanStatus);
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

        String primaryPhone = contract.getClient() != null ? contract.getClient().getPrimaryPhoneNumber() : null;

        notificationService.createDetailedNotification(
                "Statut du contrat modifié",
                "Contract '" + contract.getName() + "' status manually set to " + status + ".",
                "INFO", "CONTRACT", "SAFE", "GREEN", "LOW",
                "CONTRACT_STATUS_" + contract.getId() + "_" + System.currentTimeMillis(), contract.getId(), primaryPhone
        );
        return contractRepository.save(contract);
    }

    @Override
    public ContractModel renewContract(Long id) {
        ContractModel contract = getContractById(id);
        LocalDate originalSignature = contract.getDateSignature();

        if (originalSignature != null) {
            // 1. Compile active date objects into a clean, formatted history string [1.1.4, 1.2.6]
            java.util.List<String> formattedDates = new java.util.ArrayList<>();
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

            if (contract.getVisitDate1() != null) formattedDates.add(contract.getVisitDate1().format(dtf));
            if (contract.getVisitDate2() != null) formattedDates.add(contract.getVisitDate2().format(dtf));
            if (contract.getVisitDate3() != null) formattedDates.add(contract.getVisitDate3().format(dtf));
            if (contract.getVisitDate4() != null) formattedDates.add(contract.getVisitDate4().format(dtf));
            if (contract.getVisitDate5() != null) formattedDates.add(contract.getVisitDate5().format(dtf));
            if (contract.getVisitDate6() != null) formattedDates.add(contract.getVisitDate6().format(dtf));

            String datesString = String.join(", ", formattedDates);

            // 2. Create the permanent contract history record [1.1.4, 1.2.6]
            ContractHistoryModel historyItem = new ContractHistoryModel();
            historyItem.setYear(originalSignature.getYear());
            historyItem.setRedevance(contract.getRedevance());
            historyItem.setVisitDates(datesString.isEmpty() ? "Aucune visite effectuée" : datesString);
            historyItem.setContract(contract);

            contract.getHistory().add(historyItem); // Save to historical collection

            // 3. Extend active contract signature by 1 year [1.1.4]
            contract.setDateSignature(originalSignature.plusYears(1));
        }

        // 4. Reset active current-year dates back to null [1.1.4]
        contract.setVisitDate1(null);
        contract.setVisitDate2(null);
        contract.setVisitDate3(null);
        contract.setVisitDate4(null);
        contract.setVisitDate5(null);
        contract.setVisitDate6(null);

        ContractModel saved = contractRepository.save(contract);
        String primaryPhone = saved.getClient() != null ? saved.getClient().getPrimaryPhoneNumber() : null;

        notificationService.createDetailedNotification(
                "Contrat renouvelé",
                "Contract '" + saved.getName() + "' successfully RENEWED to " + saved.getDateSignature() + ". Previous year saved to history.",
                "SUCCESS", "CONTRACT", "SAFE", "GREEN", "LOW",
                "CONTRACT_RENEW_" + saved.getId(), saved.getId(), primaryPhone
        );

        return saved;
    }

    // UPDATED: Correctly maps parameterized list parameters matching your interface [1.2.1, 1.2.6]
    @Override
    public ContractModel updateContractScheduleDates(Long id, List<VisitScheduleDto> visits) {
        ContractModel contract = getContractById(id);

        // Reset all active date and file columns
        contract.setVisitDate1(null); contract.setVisitFile1(null); contract.setVisitFileName1(null);
        contract.setVisitDate2(null); contract.setVisitFile2(null); contract.setVisitFileName2(null);
        contract.setVisitDate3(null); contract.setVisitFile3(null); contract.setVisitFileName3(null);
        contract.setVisitDate4(null); contract.setVisitFile4(null); contract.setVisitFileName4(null);
        contract.setVisitDate5(null); contract.setVisitFile5(null); contract.setVisitFileName5(null);
        contract.setVisitDate6(null); contract.setVisitFile6(null); contract.setVisitFileName6(null);

        // Map elements sequentially
        if (visits.size() > 0) mapVisit1(contract, visits.get(0));
        if (visits.size() > 1) mapVisit2(contract, visits.get(1));
        if (visits.size() > 2) mapVisit3(contract, visits.get(2));
        if (visits.size() > 3) mapVisit4(contract, visits.get(3));
        if (visits.size() > 4) mapVisit5(contract, visits.get(4));
        if (visits.size() > 5) mapVisit6(contract, visits.get(5));

        ContractModel saved = contractRepository.save(contract);
        String primaryPhone = saved.getClient() != null ? saved.getClient().getPrimaryPhoneNumber() : null;

        // Log schedule update to Notification Center
        notificationService.createDetailedNotification(
                "Dates des visites planifiées",
                "Scheduled visit dates for contract '" + saved.getName() + "' updated. Active months: " + saved.getMonthsOfVisits() + ".",
                "INFO", "CONTRACT", "SAFE", "GREEN", "LOW",
                "CONTRACT_SCHEDULE_" + saved.getId() + "_" + System.currentTimeMillis(), saved.getId(), primaryPhone
        );

        return saved;
    }

    private void mapVisit1(ContractModel c, VisitScheduleDto dto) {
        if (dto.getDate() != null && !dto.getDate().trim().isEmpty()) {
            c.setVisitDate1(LocalDate.parse(dto.getDate().trim()));
            c.setVisitFile1(dto.getFilePath());
            c.setVisitFileName1(dto.getFileName());
        }
    }
    private void mapVisit2(ContractModel c, VisitScheduleDto dto) {
        if (dto.getDate() != null && !dto.getDate().trim().isEmpty()) {
            c.setVisitDate2(LocalDate.parse(dto.getDate().trim()));
            c.setVisitFile2(dto.getFilePath());
            c.setVisitFileName2(dto.getFileName());
        }
    }
    private void mapVisit3(ContractModel c, VisitScheduleDto dto) {
        if (dto.getDate() != null && !dto.getDate().trim().isEmpty()) {
            c.setVisitDate3(LocalDate.parse(dto.getDate().trim()));
            c.setVisitFile3(dto.getFilePath());
            c.setVisitFileName3(dto.getFileName());
        }
    }
    private void mapVisit4(ContractModel c, VisitScheduleDto dto) {
        if (dto.getDate() != null && !dto.getDate().trim().isEmpty()) {
            c.setVisitDate4(LocalDate.parse(dto.getDate().trim()));
            c.setVisitFile4(dto.getFilePath());
            c.setVisitFileName4(dto.getFileName());
        }
    }
    private void mapVisit5(ContractModel c, VisitScheduleDto dto) {
        if (dto.getDate() != null && !dto.getDate().trim().isEmpty()) {
            c.setVisitDate5(LocalDate.parse(dto.getDate().trim()));
            c.setVisitFile5(dto.getFilePath());
            c.setVisitFileName5(dto.getFileName());
        }
    }
    private void mapVisit6(ContractModel c, VisitScheduleDto dto) {
        if (dto.getDate() != null && !dto.getDate().trim().isEmpty()) {
            c.setVisitDate6(LocalDate.parse(dto.getDate().trim()));
            c.setVisitFile6(dto.getFilePath());
            c.setVisitFileName6(dto.getFileName());
        }
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

            // Corrected: ANNUELLE (6 visits/year) maps to exactly 2 months interval length [1.1.4]
            int pLen = 2; // Default for ANNUELLE
            String redevanceUpper = c.getRedevance().toUpperCase();
            if ("SEMESTRIELLE".equals(redevanceUpper)) {
                pLen = 6; // (2 visits/year = 1 visit every 6 months)
            } else if ("TRIMESTRIELLE".equals(redevanceUpper)) {
                pLen = 3; // (4 visits/year = 1 visit every 3 months)
            }

            // Calculate active cycle index (1-based)
            long monthsElapsed = java.time.temporal.ChronoUnit.MONTHS.between(signature, now);
            int currentCycle = (int) (monthsElapsed / pLen) + 1;

            // Determine cycle date boundaries
            LocalDate cycleStart = signature.plusMonths((long) (currentCycle - 1) * pLen);
            LocalDate cycleEnd = signature.plusMonths((long) currentCycle * pLen);

            long totalDays = java.time.temporal.ChronoUnit.DAYS.between(cycleStart, cycleEnd);
            long daysElapsed = java.time.temporal.ChronoUnit.DAYS.between(cycleStart, now);

            // Extract scheduled dates safely
            List<LocalDate> scheduledDates = new ArrayList<>();
            if (c.getVisitDate1() != null) scheduledDates.add(c.getVisitDate1());
            if (c.getVisitDate2() != null) scheduledDates.add(c.getVisitDate2());
            if (c.getVisitDate3() != null) scheduledDates.add(c.getVisitDate3());
            if (c.getVisitDate4() != null) scheduledDates.add(c.getVisitDate4());
            if (c.getVisitDate5() != null) scheduledDates.add(c.getVisitDate5());
            if (c.getVisitDate6() != null) scheduledDates.add(c.getVisitDate6());

            int filledCount = scheduledDates.size();
            String phone = (c.getClient() != null) ? c.getClient().getPrimaryPhoneNumber() : null;

            // 1. CRITICAL OVERDUE CASE: Check if previous period's required visit was missed
            if (currentCycle > 1) {
                int previousIndex = currentCycle - 2; // 0-based
                if (scheduledDates.size() <= previousIndex || scheduledDates.get(previousIndex) == null) {
                    String triggerKey = "CONTRACT_OVERDUE_" + (currentCycle - 1) + "_" + c.getId();
                    notificationService.createDetailedNotification(
                            "Critical Overdue Alert",
                            "Critical: The visit for contract '" + c.getName() + "' (Period " + (currentCycle - 1) + ") was not completed. The contract has entered the next visit period. Immediate intervention is required.",
                            "DANGER", "CONTRACT", "OVERDUE", "RED", "CRITICAL", triggerKey, c.getId(), phone
                    );
                }
            }

            // 2. ACTIVE CYCLE EVALUATION (If visit for the current cycle is not scheduled yet)
            int currentIdx = currentCycle - 1;
            if (scheduledDates.size() <= currentIdx || scheduledDates.get(currentIdx) == null) {
                double ratio = (double) daysElapsed / totalDays;
                String triggerKey = "CONTRACT_CYCLE_" + currentCycle + "_STATE_" + c.getId();

                if (ratio < 1.0 / 3.0) {
                    // Safe Period (First third)
                    notificationService.createDetailedNotification(
                            "Visit Period Active",
                            "The visit period " + currentCycle + " for contract '" + c.getName() + "' is active. The client can schedule the visit normally.",
                            "SUCCESS", "CONTRACT", "SAFE", "GREEN", "LOW", triggerKey, c.getId(), phone
                    );
                } else if (ratio < 2.0 / 3.0) {
                    // Reminder Period (Second third)
                    notificationService.createDetailedNotification(
                            "Visit Reminder",
                            "Reminder: The scheduled visit " + currentCycle + " for contract '" + c.getName() + "' should be completed soon.",
                            "WARNING", "CONTRACT", "REMINDER", "YELLOW", "MEDIUM", triggerKey, c.getId(), phone
                    );
                } else if (ratio < 1.0) {
                    // Urgent Period (Final third)
                    notificationService.createDetailedNotification(
                            "Urgent Visit Deadline",
                            "Urgent: The visit deadline " + currentCycle + " for contract '" + c.getName() + "' is approaching. Immediate action is required.",
                            "DANGER", "CONTRACT", "URGENT", "RED", "HIGH", triggerKey, c.getId(), phone
                    );
                }
            }

            // 3. NEW BILLING/FACTURATION WARNING (Fired if all planned visits are completed) [1.1.4, 1.2.6]
            if (filledCount == totalVisits) {
                String triggerKey = "CONTRACT_BILLING_" + currentCycle + "_" + c.getId();

                // Retrieve completion date of the last visit safely
                LocalDate lastVisitDate = signature;
                if (totalVisits == 6 && c.getVisitDate6() != null) lastVisitDate = c.getVisitDate6();
                else if (totalVisits == 4 && c.getVisitDate4() != null) lastVisitDate = c.getVisitDate4();
                else if (totalVisits == 2 && c.getVisitDate2() != null) lastVisitDate = c.getVisitDate2();

                String message = String.format(
                        "Toutes les visites prévues pour le contrat %s ont été réalisées. Le client %s (Code: %s) est prêt pour la facturation. (Nombre de visites: %d, Période concernée: %d, Dernière visite: %s, Prêt le: %s).",
                        c.getName(), c.getClient().getName(), c.getClient().getClientCode(), totalVisits, currentCycle, lastVisitDate, now
                );

                notificationService.createDetailedNotification(
                        "Client prêt pour la facturation",
                        message,
                        "INFO", "CONTRACT", "READY_FOR_BILLING", "BLUE", "MEDIUM", triggerKey, c.getId(), phone // Added 'phone' as the 10th parameter [1.2.1]
                );
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
            ContractModel c = contractRepository.findById(contractId).orElse(null);
            String phone = (c != null && c.getClient() != null) ? c.getClient().getPrimaryPhoneNumber() : null;

            NotificationModel notification = new NotificationModel();
            notification.setTriggerKey(triggerKey);
            notification.setMessage(message);
            notification.setType(type);
            notification.setContractId(contractId);
            notification.setClientPhone(phone); // Map client phone redirection reference [1.2.6]
            notification.setCreatedAt(java.time.LocalDateTime.now());
            notification.setReadStatus(false);
            notification.setCategory("CONTRACT"); // Tag dynamically as CONTRACT

            notificationRepository.save(notification);
        }
    }

    @Override
    public ContractModel validateVisit(Long id, VisitValidationDto dto, String currentUserName) {
        ContractModel contract = getContractById(id);
        int index = dto.getVisitIndex();

        // 1. SEQUENCE CONTROL (The logic you requested)
        if (index > 1) {
            if (!isVisitValidated(contract, index - 1)) {
                throw new IllegalStateException("Impossible de valider la visite #" + index +
                        " car la visite #" + (index - 1) + " n'est pas encore faite.");
            }
        }

        // 2. MAP DATA BASED ON INDEX
        LocalDate date = LocalDate.parse(dto.getDate());
        String obs = dto.getObservations();
        String file = dto.getFilePath();
        String fileName = dto.getFileName();

        switch (index) {
            case 1 -> { contract.setVisitDate1(date); contract.setVisitObs1(obs); contract.setVisitUser1(currentUserName); contract.setVisitFile1(file); contract.setVisitFileName1(fileName); }
            case 2 -> { contract.setVisitDate2(date); contract.setVisitObs2(obs); contract.setVisitUser2(currentUserName); contract.setVisitFile2(file); contract.setVisitFileName2(fileName); }
            case 3 -> { contract.setVisitDate3(date); contract.setVisitObs3(obs); contract.setVisitUser3(currentUserName); contract.setVisitFile3(file); contract.setVisitFileName3(fileName); }
            case 4 -> { contract.setVisitDate4(date); contract.setVisitObs4(obs); contract.setVisitUser4(currentUserName); contract.setVisitFile4(file); contract.setVisitFileName4(fileName); }
            case 5 -> { contract.setVisitDate5(date); contract.setVisitObs5(obs); contract.setVisitUser5(currentUserName); contract.setVisitFile5(file); contract.setVisitFileName5(fileName); }
            case 6 -> { contract.setVisitDate6(date); contract.setVisitObs6(obs); contract.setVisitUser6(currentUserName); contract.setVisitFile6(file); contract.setVisitFileName6(fileName); }
        }

        return contractRepository.save(contract);
    }

    @Override
    public ContractModel deleteVisitData(Long id, int index) {
        ContractModel contract = getContractById(id);
        switch (index) {
            case 1 -> { contract.setVisitDate1(null); contract.setVisitObs1(null); contract.setVisitUser1(null); contract.setVisitFile1(null); contract.setVisitFileName1(null); }
            case 2 -> { contract.setVisitDate2(null); contract.setVisitObs2(null); contract.setVisitUser2(null); contract.setVisitFile2(null); contract.setVisitFileName2(null); }
            case 3 -> { contract.setVisitDate3(null); contract.setVisitObs3(null); contract.setVisitUser3(null); contract.setVisitFile3(null); contract.setVisitFileName3(null); }
            case 4 -> { contract.setVisitDate4(null); contract.setVisitObs4(null); contract.setVisitUser4(null); contract.setVisitFile4(null); contract.setVisitFileName4(null); }
            case 5 -> { contract.setVisitDate5(null); contract.setVisitObs5(null); contract.setVisitUser5(null); contract.setVisitFile5(null); contract.setVisitFileName5(null); }
            case 6 -> { contract.setVisitDate6(null); contract.setVisitObs6(null); contract.setVisitUser6(null); contract.setVisitFile6(null); contract.setVisitFileName6(null); }
        }
        return contractRepository.save(contract);
    }

    // Helper for sequence check
    private boolean isVisitValidated(ContractModel c, int idx) {
        return switch (idx) {
            case 1 -> c.getVisitDate1() != null;
            case 2 -> c.getVisitDate2() != null;
            case 3 -> c.getVisitDate3() != null;
            case 4 -> c.getVisitDate4() != null;
            case 5 -> c.getVisitDate5() != null;
            case 6 -> c.getVisitDate6() != null;
            default -> false;
        };
    }

    private boolean isVisitDone(ContractModel c, int idx) {
        if (idx == 1) return c.getVisitDate1() != null;
        if (idx == 2) return c.getVisitDate2() != null;
        if (idx == 3) return c.getVisitDate3() != null;
        if (idx == 4) return c.getVisitDate4() != null;
        if (idx == 5) return c.getVisitDate5() != null;
        return c.getVisitDate6() != null;
    }

    private void applyVisitData(ContractModel c, int idx, LocalDate date, String obs, String file, String fileName, String user) {
        switch (idx) {
            case 1 -> { c.setVisitDate1(date); c.setVisitObs1(obs); c.setVisitFile1(file); c.setVisitFileName1(fileName); c.setVisitUser1(user); }
            case 2 -> { c.setVisitDate2(date); c.setVisitObs2(obs); c.setVisitFile2(file); c.setVisitFileName2(fileName); c.setVisitUser2(user); }
            case 3 -> { c.setVisitDate3(date); c.setVisitObs3(obs); c.setVisitFile3(file); c.setVisitFileName3(fileName); c.setVisitUser3(user); }
            case 4 -> { c.setVisitDate4(date); c.setVisitObs4(obs); c.setVisitFile4(file); c.setVisitFileName4(fileName); c.setVisitUser4(user); }
            case 5 -> { c.setVisitDate5(date); c.setVisitObs5(obs); c.setVisitFile5(file); c.setVisitFileName5(fileName); c.setVisitUser5(user); }
            case 6 -> { c.setVisitDate6(date); c.setVisitObs6(obs); c.setVisitFile6(file); c.setVisitFileName6(fileName); c.setVisitUser6(user); }
        }
    }
}
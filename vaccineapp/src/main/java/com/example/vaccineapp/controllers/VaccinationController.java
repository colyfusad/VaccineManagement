package com.example.vaccineapp.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.vaccineapp.models.Citizen;
import com.example.vaccineapp.models.Vaccination;
import com.example.vaccineapp.models.Vaccine;
import com.example.vaccineapp.services.CitizenService;
import com.example.vaccineapp.services.VaccinationService;
import com.example.vaccineapp.services.VaccineService;

@Controller
@RequestMapping("/")
public class VaccinationController {

    private final VaccinationService vaccinationService;
    private final VaccineService vaccineService;
    private final CitizenService citizenService;

    // Constructor-based injection
    public VaccinationController(VaccinationService vaccinationService, VaccineService vaccineService, CitizenService citizenService) {
        this.vaccinationService = vaccinationService;
        this.vaccineService = vaccineService;
        this.citizenService = citizenService;
    }

    @GetMapping("/vaccinations")
    public String getRegistrations(
            @RequestParam(required = false) String vaccineName,
            @RequestParam(required = false) String vaccinationDate,
            @RequestParam(required = false) String status,
            Model model) {

        // Get full vaccination list
        List<Vaccination> vaccinations = vaccinationService.getAllVaccinations();

        // Apply filters if parameters are provided
        if (vaccineName != null && !vaccineName.isEmpty()) {
            vaccinations = vaccinations.stream()
                    .filter(r -> r.getVaccine().getName().equalsIgnoreCase(vaccineName))
                    .collect(Collectors.toList());
        }
        if (vaccinationDate != null && !vaccinationDate.isEmpty()) {
            vaccinations = vaccinations.stream()
                    .filter(r -> r.getVaccinationDate().toString().equals(vaccinationDate))
                    .collect(Collectors.toList());
        }
        if (status != null && !status.isEmpty()) {
            Vaccination.Status selectedStatus = Vaccination.Status.valueOf(status.toUpperCase());
            vaccinations = vaccinations.stream()
                    .filter(r -> r.getStatus() == selectedStatus)
                    .collect(Collectors.toList());
        }

        // Get vaccines for dropdown display
        List<Vaccine> vaccines = vaccineService.findAll();

        // Populate model attributes
        model.addAttribute("vaccinations", vaccinations);
        model.addAttribute("vaccines", vaccines);
        model.addAttribute("vaccineName", vaccineName);
        model.addAttribute("vaccinationDate", vaccinationDate);
        model.addAttribute("status", status);
        return "vaccinations";
    }
    
    @GetMapping("/vaccinations/register")
    public String showRegisterVaccinationPage(Model model) {
    	List<Vaccine> vaccines = vaccineService.findAll();
    	model.addAttribute("vaccines", vaccines);
    	
    	List<Citizen> citizens = citizenService.findAllCitizens();
    	model.addAttribute("citizens", citizens);
    	
    	return "vaccination/register-vaccination";
    }
    
    @PostMapping("/vaccinations/register")
    public String registerVaccination(@RequestParam Long citizen, 
                                       @RequestParam Long vaccine, 
                                       @RequestParam String vaccinationDate) {
        // Chuyển đổi String thành LocalDate
        LocalDate date = LocalDate.parse(vaccinationDate);

        // Gọi phương thức trong service để lưu vào cơ sở dữ liệu
        vaccinationService.registerVaccination(citizen, vaccine, date);

        return "redirect:/vaccinations"; // Hoặc redirect đến trang khác sau khi thêm thành công
    }


    @PostMapping("/updateStatus")
    public String updateVaccinationStatus(
            @RequestParam Long vaccinationId,
            @RequestParam String action,
            RedirectAttributes redirectAttributes) {

        // Update status based on the action
        Vaccination.Status newStatus = Vaccination.Status.valueOf(action.toUpperCase());
        vaccinationService.updateVaccinationStatus(vaccinationId, newStatus);

        redirectAttributes.addFlashAttribute("message", "Trạng thái đã được cập nhật thành công!");
        // Redirect to the vaccination list page
        return "redirect:/vaccinations";
    }
}

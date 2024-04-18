package fr.eseo.tauri.controller;

import fr.eseo.tauri.model.GradeScale;
import fr.eseo.tauri.repository.GradeScaleRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grade-scales")
@Tag(name = "grade-scales")
public class GradeScaleController {

    private final GradeScaleRepository gradeScaleRepository;

    @Autowired
    public GradeScaleController(GradeScaleRepository gradeScaleRepository) {
        this.gradeScaleRepository = gradeScaleRepository;
    }

    @PostMapping("/")
    public GradeScale addGradeScale(@RequestBody GradeScale gradeScale) {
        return gradeScaleRepository.save(gradeScale);
    }

    @GetMapping("/")
    public Iterable<GradeScale> getAllGradeScales() {
        return gradeScaleRepository.findAll();
    }

    @GetMapping("/{id}")
    public GradeScale getGradeScaleById(@PathVariable Integer id) {
        return gradeScaleRepository.findById(id).orElse(null);
    }

    @PutMapping("/{id}")
    public GradeScale updateGradeScale(@PathVariable Integer id, @RequestBody GradeScale gradeScaleDetails) {
        GradeScale gradeScale = gradeScaleRepository.findById(id).orElse(null);
        if (gradeScale != null) {
            gradeScale.type(gradeScaleDetails.type());
            gradeScale.content(gradeScaleDetails.content());
            return gradeScaleRepository.save(gradeScale);
        }
        return null;
    }

    @DeleteMapping("/{id}")
    public String deleteGradeScale(@PathVariable Integer id) {
        gradeScaleRepository.deleteById(id);
        return "GradeScale deleted";
    }
}

package com.example.transportfirm.Controller;

import com.example.transportfirm.Entity.DriverInfo;
import com.example.transportfirm.Service.DriverService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@CrossOrigin(origins = "http://localhost:4200")
public class DriverController {
    private final DriverService service;
    public DriverController(DriverService service) { this.service = service; }

    @GetMapping public List<DriverInfo> getAll() { return service.getAll(); }

    @GetMapping("/{egn}") public DriverInfo getByEgn(@PathVariable String egn) { return service.getByEgn(egn); }

    @PostMapping
    public DriverInfo create(@Valid @RequestBody DriverInfo driver) {
        return service.save(driver);
    }

    @PutMapping("/{egn}")
    public ResponseEntity<DriverInfo> update(@PathVariable String egn,
                                             @Valid @RequestBody DriverInfo driver) {
        DriverInfo updated = service.update(egn, driver);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{egn}")
    public void delete(@PathVariable String egn) {
        service.delete(egn);
    }


}

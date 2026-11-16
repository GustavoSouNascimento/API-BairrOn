package br.com.fiap.bairron.controller;

import br.com.fiap.bairron.dto.OcorrenciaResponse;
import br.com.fiap.bairron.dto.ResumoBairroResponse;
import br.com.fiap.bairron.service.PainelService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bairros/{bairroId}")
public class PainelController {

    private final PainelService service;

    public PainelController(PainelService service) {
        this.service = service;
    }

    @GetMapping("/resumo")
    public ResumoBairroResponse resumo(@PathVariable Long bairroId) {
        return service.resumo(bairroId);
    }

    @GetMapping("/ocorrencias/prioritarias")
    public List<OcorrenciaResponse> prioritarias(@PathVariable Long bairroId,
                                                 @RequestParam(defaultValue = "5") int limite) {
        return service.prioritarias(bairroId, limite);
    }
}

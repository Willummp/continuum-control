package br.infnet.continuum.control.occurrence;

import java.util.Map;

public sealed interface OcorrenciaDados permits OcorrenciaDados.DistorcaoTemporal,
        OcorrenciaDados.EvidenciaTemporal, OcorrenciaDados.Generica {

    Map<String, Object> brutos();

    record DistorcaoTemporal(double intensidade, String duracao, Map<String, Object> brutos) implements OcorrenciaDados {}

    record EvidenciaTemporal(String objeto, String periodoEstimado, String confiabilidade,
                             Map<String, Object> brutos) implements OcorrenciaDados {}

    record Generica(Map<String, Object> brutos) implements OcorrenciaDados {}
}

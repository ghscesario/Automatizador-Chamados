package com.project.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.Model.Chamado;
import com.project.Repository.ChamadoRepository;

@Service
public class NotificacaoChamadosResolvidosService {

    @Autowired
    private BuscadorResolvidosService buscadorResolvidosService;

    @Autowired
    private ChamadoRepository chamadoRepository;

    @Autowired
    private EvolutionApiService evolutionApiService;

    
    private static final String EVOLUTION_INSTANCE_ID = "teste";

    public void notificarChamadosResolvidos() {
        List<String> numeros = buscadorResolvidosService.buscarNumerosDeChamadosResolvidos();

        if (numeros.isEmpty()) {
            System.out.println("Nenhum número de chamado resolvido foi encontrado.");
            return;
        }

        List<Chamado> chamados = chamadoRepository.findByNumeroChamadoInAndMensagemEnviadaFalse(numeros);

        if (chamados.isEmpty()) {
            System.out.println("Nenhum novo chamado para notificar.");
            return;
        }

        for (Chamado chamado : chamados) {
            String telefone = chamado.getTelefone();
            String numero = chamado.getNumeroChamado();
            String titulo = chamado.getResumo();
            String telefoneFormatado = "55" + telefone;

            if (telefone != null && !telefone.isBlank()) {
                String mensagem = "Tecnologia da informação HUGO informa! O chamado: "+ titulo +", de número: "+ numero + ", foi resolvido. Deixe sua opinião na pesquisa de satisfação através do link: https://docs.google.com/forms/d/e/1FAIpQLSfpH6KGbP4sanyIsBtOtQuCgyUGdInB4LEMOq9Pmz-N1Ch-nQ/viewform?usp=header";

                // Enviar via EvolutionAPI
                evolutionApiService.sendTextMessage("teste", telefoneFormatado, mensagem);

                // Marcar como enviado
                chamado.setMensagemEnviada(true);
                chamadoRepository.save(chamado);
            }
        }

        System.out.println("Mensagens enviadas com sucesso.");
    }

    // Método que cria texto da mensagem
    private String criarMensagem(Chamado chamado) {
        return "Tecnologia da informação informa! Seu chamado número " + chamado.getNumeroChamado() +
            " foi resolvido.\nResumo: " + chamado.getResumo() +
            "\nDescrição: " + chamado.getDescricao();
    }
}

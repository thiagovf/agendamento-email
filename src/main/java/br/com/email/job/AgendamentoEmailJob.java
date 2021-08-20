package br.com.email.job;

import java.util.List;

import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.inject.Inject;

import br.com.email.entidade.AgendamentoEmail;
import br.com.email.servico.AgendamentoEmailServico;

@Stateless
public class AgendamentoEmailJob {

	@Inject
	private AgendamentoEmailServico agendamentoEmailServico;
	
	@Schedule(hour = "*", minute = "*", second = "*/10")
	public void enviarEmails() {
		List<AgendamentoEmail> listaAgendamentos = agendamentoEmailServico.listarPorNaoAgendado();
		listaAgendamentos.forEach(agendamentoNaoEnviado -> {
			agendamentoEmailServico.enviar(agendamentoNaoEnviado);
			agendamentoEmailServico.alterar(agendamentoNaoEnviado);
		});
	}
}

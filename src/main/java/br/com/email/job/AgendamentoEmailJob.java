package br.com.email.job;

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.Queue;

import br.com.email.entidade.AgendamentoEmail;
import br.com.email.servico.AgendamentoEmailServico;

@Singleton
public class AgendamentoEmailJob {
	
	@Inject
	private AgendamentoEmailServico agendamentoEmailServico;
	
	@Inject
	@JMSConnectionFactory("java:jboss/DefaultJMSConnectionFactory")
	private JMSContext context;
	
	@Resource(mappedName = "java:/jms/queue/EmailQueue")
	private Queue queue;

	@Schedule(hour = "*", minute = "*", second = "*/10")
	public void enviarEmails() {
		List<AgendamentoEmail> listaAgendamentos = agendamentoEmailServico.listarPorNaoAgendado();
		listaAgendamentos.forEach(agendamentoNaoEnviado -> {
			context.createProducer().send(queue, agendamentoNaoEnviado);
			agendamentoEmailServico.alterar(agendamentoNaoEnviado);
		});
	}
}

package br.com.email.servico;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;

import br.com.email.dao.AgendamentoEmailDAO;
import br.com.email.entidade.AgendamentoEmail;

@Stateless
public class AgendamentoEmailServico {
	
	private static final Logger LOGGER = Logger.getLogger(AgendamentoEmailServico.class.getName());

	@Inject
	private AgendamentoEmailDAO dao;

	public List<AgendamentoEmail> listar() {
		return dao.listar();
	}
	
	public List<AgendamentoEmail> listarPorNaoAgendado() {
		return dao.listarPorNaoAgendado();
	}
	
	public void alterar(AgendamentoEmail agendamentoEmail) {
		agendamentoEmail.setAgendado(true);
		dao.alterar(agendamentoEmail);
	}
	
	public void inserir(AgendamentoEmail agendamentoEmail) {
		agendamentoEmail.setAgendado(false);
		dao.inserir(agendamentoEmail);
	}
	
	public void enviar(AgendamentoEmail agendamentoEmail) {
		/*
		 * Simulação de e-mail. Para envio de e-mail real, usar JavaMail.
		 */
		try {
			Thread.sleep(5000);
			LOGGER.info("E-mail do usuário " + agendamentoEmail.getEmail() + " foi enviado.");
		} catch (Exception e) {
			LOGGER.warning(e.getMessage());
		}
	}
}

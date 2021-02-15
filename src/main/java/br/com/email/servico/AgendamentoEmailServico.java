package br.com.email.servico;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import br.com.email.dao.AgendamentoEmailDAO;
import br.com.email.entidade.AgendamentoEmail;

@Stateless
public class AgendamentoEmailServico {

	@Inject
	private AgendamentoEmailDAO dao;

	public List<AgendamentoEmail> listar() {
		return dao.listar();
	}
}
